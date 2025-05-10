package com.muhammadahmedmufii.comfybuy.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.muhammadahmedmufii.comfybuy.data.local.ProductDao
import com.muhammadahmedmufii.comfybuy.data.local.ProductEntity
import com.muhammadahmedmufii.comfybuy.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val productDao: ProductDao,
    private val firestore: FirebaseFirestore,
    private val realtimeDatabase: FirebaseDatabase
) {

    private val productsCollection = firestore.collection("products")
    private val productImagesRtdbRef = realtimeDatabase.getReference("product_images_multiple") // New path for list of images

    // --- Base64 Conversion Helpers (bitmapToBase64, base64ToBitmap) remain the same ---
    private fun bitmapToBase64(bitmap: Bitmap?): String? {
        if (bitmap == null) return null
        return try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream) // Quality to 75
            val byteArray = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error converting bitmap to Base64", e)
            null
        }
    }

    private fun base64ToBitmap(base64String: String?): Bitmap? {
        if (base64String == null) return null
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error converting Base64 to bitmap", e)
            null
        }
    }

    // getAllProducts, getProductById, getUserProducts structure is fine,
    // but they will benefit from the updated toDomainModel()

    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun getProductById(productId: String): Flow<Product?> {
        return productDao.getProductById(productId).map { entity ->
            entity?.toDomainModel()
        }
    }

    fun getUserProducts(userId: String): Flow<List<Product>> {
        return productDao.getUserProducts(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun saveProduct(product: Product) { // No longer need separate imageBitmap param
        try {
            // 1. Convert List<Bitmap> to List<String> (Base64)
            val imageBase64List = product.imageBitmaps.mapNotNull { bitmap ->
                bitmapToBase64(bitmap) // mapNotNull will skip nulls if conversion fails
            }

            // 2. Create ProductEntity
            val productEntity = product.toEntity(imageBase64List)

            // 3. Save to Room
            productDao.insertProduct(productEntity)
            Log.d("ProductRepository", "Saved product ${product.productId} to Room with ${imageBase64List.size} images")

            // 4. Save metadata to Firestore
            val productMetadata = product.toFirestoreMap() // This map should NOT contain image data
            productsCollection.document(product.productId).set(productMetadata, SetOptions.merge()).await()
            Log.d("ProductRepository", "Saved product metadata ${product.productId} to Firestore")

            // 5. Save list of image Base64 strings to Realtime Database
            // Store as a map with indexed keys for easier individual access/deletion if needed later
            val imagesMapForRtdb = imageBase64List.mapIndexed { index, base64String ->
                index.toString() to base64String
            }.toMap()

            if (imagesMapForRtdb.isNotEmpty()) {
                productImagesRtdbRef.child(product.productId).setValue(imagesMapForRtdb).await()
                Log.d("ProductRepository", "Saved ${imagesMapForRtdb.size} product images to RTDB for ${product.productId}")
            } else {
                // If no images, ensure any old images for this product ID are removed
                productImagesRtdbRef.child(product.productId).removeValue().await()
                Log.d("ProductRepository", "No images to save, cleared RTDB images for ${product.productId}")
            }

        } catch (e: Exception) {
            Log.e("ProductRepository", "Error saving product ${product.productId}", e)
            throw e // Re-throw to be caught by ViewModel
        }
    }

    suspend fun updateProduct(product: Product, newImageBitmaps: List<Bitmap>?) { // Accept list of bitmaps
        try {
            var imageBase64ListToSave: List<String> = emptyList()
            val existingProductEntity = productDao.getProductById(product.productId).firstOrNull()

            if (newImageBitmaps != null) { // User provided new images (could be empty list for removal)
                imageBase64ListToSave = newImageBitmaps.mapNotNull { bitmapToBase64(it) }

                val imagesMapForRtdb = imageBase64ListToSave.mapIndexed { index, base64String ->
                    index.toString() to base64String
                }.toMap()

                if (imagesMapForRtdb.isNotEmpty()) {
                    productImagesRtdbRef.child(product.productId).setValue(imagesMapForRtdb).await()
                    Log.d("ProductRepository", "Updated product images in RTDB for ${product.productId}")
                } else {
                    productImagesRtdbRef.child(product.productId).removeValue().await() // All images removed
                    Log.d("ProductRepository", "All images removed from RTDB for ${product.productId}")
                }
            } else {
                // No new images provided, keep existing images from Room
                imageBase64ListToSave = existingProductEntity?.imageBase64List ?: emptyList()
                Log.d("ProductRepository", "No new image list provided, keeping existing ${imageBase64ListToSave.size} images for ${product.productId}")
            }

            val updatedProductEntity = ProductEntity(
                productId = product.productId,
                ownerId = product.ownerId, // Ensure ownerId is correctly populated
                title = product.title,
                description = product.description,
                price = product.price,
                location = product.location,
                category = product.category,
                condition = product.condition,
                imageBase64List = imageBase64ListToSave,
                timestamp = System.currentTimeMillis(), // Update timestamp on edit
                isDeleted = existingProductEntity?.isDeleted ?: false
            )
            productDao.updateProduct(updatedProductEntity)
            Log.d("ProductRepository", "Updated product ${product.productId} in Room")

            val productMetadataUpdates = product.toFirestoreMap()
            productsCollection.document(product.productId).set(productMetadataUpdates, SetOptions.merge()).await() // Use set with merge
            Log.d("ProductRepository", "Updated product metadata ${product.productId} in Firestore")

        } catch (e: Exception) {
            Log.e("ProductRepository", "Error updating product ${product.productId}", e)
            throw e
        }
    }


    suspend fun deleteProduct(productId: String) { /* ... (logic to delete from productImagesRtdbRef.child(productId) remains the same) ... */ }

    suspend fun syncProductsFromRemote() {
        try {
            val firestoreProducts = productsCollection.get().await().toObjects(ProductEntity::class.java)
            Log.d("ProductRepository", "Fetched ${firestoreProducts.size} product metadata from Firestore")

            val productsToSaveLocally = mutableListOf<ProductEntity>()

            for (fsProductStub in firestoreProducts) {
                var imageBase64ListFromRtdb: List<String> = emptyList()
                try {
                    val rtdbSnapshot = productImagesRtdbRef.child(fsProductStub.productId).get().await()
                    if (rtdbSnapshot.exists()) {
                        // Assuming images are stored as a map {"0": "base64_0", "1": "base64_1", ...}
                        val imagesMap = rtdbSnapshot.value as? Map<String, String>
                        if (imagesMap != null) {
                            // Sort by key ("0", "1", ...) to maintain order if necessary
                            imageBase64ListFromRtdb = imagesMap.entries.sortedBy { it.key.toIntOrNull() ?: Int.MAX_VALUE }.map { it.value }
                            Log.d("ProductRepository","Fetched ${imageBase64ListFromRtdb.size} images from RTDB for ${fsProductStub.productId}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ProductRepository", "Error fetching images from RTDB for ${fsProductStub.productId}", e)
                }
                productsToSaveLocally.add(
                    fsProductStub.copy(imageBase64List = imageBase64ListFromRtdb)
                )
            }

            if (productsToSaveLocally.isNotEmpty()) {
                productDao.insertAllProducts(productsToSaveLocally)
                Log.d("ProductRepository", "Saved/updated ${productsToSaveLocally.size} products in Room from remote sync")
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error syncing products from remote", e)
        }
    }

    suspend fun syncProductsToRemote(lastSyncTimestamp: Long) {
        try {
            val productsToSync = productDao.getProductsToSync(lastSyncTimestamp)
            Log.d("ProductRepository", "Found ${productsToSync.size} products to sync to remote")

            for (productEntity in productsToSync) {
                if (productEntity.isDeleted) {
                    productsCollection.document(productEntity.productId).delete().await()
                    productImagesRtdbRef.child(productEntity.productId).removeValue().await()
                    Log.d("ProductRepository", "Deleted product ${productEntity.productId} from remote")
                    productDao.deleteProductById(productEntity.productId)
                } else {
                    val product = productEntity.toDomainModel() // Domain model has text fields
                    val productMetadata = product.toFirestoreMap() // Map for Firestore
                    productsCollection.document(productEntity.productId).set(productMetadata, SetOptions.merge()).await()
                    Log.d("ProductRepository", "Synced product metadata ${productEntity.productId} to Firestore")

                    // Sync list of image Base64 strings
                    val imagesMapForRtdb = productEntity.imageBase64List.mapIndexed { index, base64String ->
                        index.toString() to base64String
                    }.toMap()

                    if (imagesMapForRtdb.isNotEmpty()) {
                        productImagesRtdbRef.child(productEntity.productId).setValue(imagesMapForRtdb).await()
                        Log.d("ProductRepository", "Synced ${imagesMapForRtdb.size} product images to RTDB for ${productEntity.productId}")
                    } else {
                        productImagesRtdbRef.child(productEntity.productId).removeValue().await()
                        Log.d("ProductRepository", "No local images, removed from RTDB for ${productEntity.productId}")
                    }
                }
            }
        } catch (e: Exception) { Log.e("ProductRepository", "Error syncing products to remote", e) }
    }


    // Mappers
    fun ProductEntity.toDomainModel(): Product {
        return Product(
            productId = productId,
            ownerId = ownerId,
            title = title,
            description = description,
            price = price,
            location = location,
            category = category, // NEW
            condition = condition, // NEW
            imageBitmaps = imageBase64List.mapNotNull { base64String -> base64ToBitmap(base64String) }
        )
    }

    // This toEntity is now primarily for converting domain to entity for saving
    fun Product.toEntity(providedBase64List: List<String>): ProductEntity {
        return ProductEntity(
            productId = productId,
            ownerId = ownerId,
            title = title,
            description = description,
            price = price,
            location = location,
            category = category, // NEW
            condition = condition, // NEW
            imageBase64List = providedBase64List,
            timestamp = System.currentTimeMillis() // Always update timestamp on save/creation
        )
    }

    fun Product.toFirestoreMap(): HashMap<String, Any?> {
        return hashMapOf(
            "productId" to productId,
            "ownerId" to ownerId,
            "title" to title,
            "description" to description,
            "price" to price,
            "location" to location,
            "category" to category, // NEW
            "condition" to condition, // NEW
            "timestamp" to System.currentTimeMillis() // Good to have a server-side like timestamp here too
        )
    }
}