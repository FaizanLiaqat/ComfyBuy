package com.muhammadahmedmufii.comfybuy.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query // For Query.Direction
import com.google.firebase.firestore.SetOptions
import com.muhammadahmedmufii.comfybuy.data.local.ProductDao
import com.muhammadahmedmufii.comfybuy.data.local.ProductEntity
import com.muhammadahmedmufii.comfybuy.domain.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi // For callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val productDao: ProductDao,
    private val firestore: FirebaseFirestore,
    private val realtimeDatabase: FirebaseDatabase
) {
    private val TAG = "ProductRepo" // Logging Tag
    private val productsCollection = firestore.collection("products")
    private val productImagesRtdbRef = realtimeDatabase.getReference("product_images_multiple") // New path for list of images

    // --- Base64 Conversion Helpers (bitmapToBase64, base64ToBitmap) remain the same ---
    private fun bitmapToBase64(bitmap: Bitmap?): String? {
        if (bitmap == null) {
            Log.w(TAG, "bitmapToBase64: Received null bitmap.")
            return null
        }
        return try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream) // Quality to 75
            val byteArray = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            Log.d(TAG, "bitmapToBase64: Converted bitmap (${bitmap.width}x${bitmap.height}) to Base64 string (length: ${base64String.length})")
            base64String
        } catch (e: Exception) {
            Log.e(TAG, "bitmapToBase64: Error converting bitmap", e)
            null
        }
    }

    private fun base64ToBitmap(base64String: String?): Bitmap? {
        if (base64String == null) {
            Log.w(TAG, "base64ToBitmap: Received null or empty Base64 string.")
            return null
        }
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            if (bitmap != null) {
                Log.d(TAG, "base64ToBitmap: Converted Base64 (length: ${base64String.length}) to Bitmap (${bitmap.width}x${bitmap.height})")
            } else {
                Log.e(TAG, "base64ToBitmap: BitmapFactory.decodeByteArray returned null for Base64 (length: ${base64String.length})")
            }
            bitmap
        } catch (e: IllegalArgumentException) { // Catch specific Base64 decode errors
            Log.e(TAG, "base64ToBitmap: IllegalArgumentException - Invalid Base64 string (length: ${base64String.length})", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "base64ToBitmap: Generic error converting Base64 (length: ${base64String.length})", e)
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
        Log.i(TAG, "saveProduct: Starting for productId: ${product.productId}, image count: ${product.imageBitmaps.size}")
        try {
            val imageBase64List = product.imageBitmaps.mapNotNull { bitmap ->
                bitmapToBase64(bitmap)
            }
            Log.d(TAG, "saveProduct: Converted ${product.imageBitmaps.size} bitmaps to ${imageBase64List.size} Base64 strings for ${product.productId}.")

            val productEntity = product.toEntity(imageBase64List)
            productDao.insertProduct(productEntity)
            Log.d(TAG, "saveProduct: Saved to Room: ${product.productId}, imageBase64List size: ${productEntity.imageBase64List.size}")

            val productMetadata = product.toFirestoreMap()
            productsCollection.document(product.productId).set(productMetadata, SetOptions.merge()).await()
            Log.d(TAG, "saveProduct: Saved metadata to Firestore: ${product.productId}")

            val imagesMapForRtdb = imageBase64List.mapIndexed { index, base64String -> index.toString() to base64String }.toMap()
            if (imagesMapForRtdb.isNotEmpty()) {
                productImagesRtdbRef.child(product.productId).setValue(imagesMapForRtdb).await()
                Log.i(TAG, "saveProduct: Saved ${imagesMapForRtdb.size} images to RTDB for ${product.productId}")
            } else {
//                productImagesRtdbRef.child(product.productId).removeValue().await()
                Log.d(TAG, "saveProduct: No images to save, cleared RTDB for ${product.productId}")
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

    // --- NEW: Real-time Flow from Firestore & RTDB ---
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getRealtimeAllProducts(): Flow<List<Product>> = callbackFlow {
        Log.i(TAG, "getRealtimeAllProducts: Setting up Firestore listener for 'products' collection, where isDeleted==false, orderBy timestamp ASC.")

        val listenerRegistration = productsCollection
            .whereEqualTo("isDeleted", false) // Make sure 'isDeleted' is reliably in your Firestore docs
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(TAG, "Firestore listen error in callbackFlow", error)
                    close(error) // Propagate the error to close the flow
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    Log.i(TAG, "getRealtimeAllProducts: Firestore snapshot received. isEmpty: ${snapshots.isEmpty}, Size: ${snapshots.size()}, Metadata HasPendingWrites: ${snapshots.metadata.hasPendingWrites()}")
                    val productStubsFromFirestore = snapshots.toObjects(ProductEntity::class.java)
                    Log.d(TAG, "getRealtimeAllProducts: Converted to ${productStubsFromFirestore.size} ProductEntity stubs from Firestore.")

                    // Launch a new coroutine for asynchronous RTDB fetching and processing
                    // Use the scope of the callbackFlow (this) which is a ProducerScope
                    this.launch(Dispatchers.IO) {
                        val productListWithImages = mutableListOf<Product>()
                        Log.d(TAG, "getRealtimeAllProducts/launch: Processing ${productStubsFromFirestore.size} stubs.")

                        for (stubFromFirestore in productStubsFromFirestore) {
                            Log.i(TAG, "getRealtimeAllProducts/launch: --- Processing FS Stub Product ID: ${stubFromFirestore.productId} --- Title: ${stubFromFirestore.title}")

                            var finalImageBase64List: List<String> = emptyList()
                            var imageDataSource = "None" // To track where images came from

                            // 1. Attempt to fetch from RTDB
                            try {
                                val rtdbPathChecked = productImagesRtdbRef.child(stubFromFirestore.productId).toString()
                                Log.d(TAG, "getRealtimeAllProducts/launch: Attempting RTDB get for path: $rtdbPathChecked")
                                val rtdbSnapshot = productImagesRtdbRef.child(stubFromFirestore.productId).get().await()

                                if (rtdbSnapshot.exists()) {
                                    Log.i(TAG, "getRealtimeAllProducts/launch: RTDB snapshot EXISTS for ${stubFromFirestore.productId}. Value type: ${rtdbSnapshot.value?.javaClass?.name}")
                                    val imagesMap = rtdbSnapshot.value as? Map<String, String>
                                    if (imagesMap != null && imagesMap.isNotEmpty()) {
                                        finalImageBase64List = imagesMap.entries
                                            .sortedBy { entry -> entry.key.toIntOrNull() ?: Int.MAX_VALUE }
                                            .map { entry -> entry.value }
                                        imageDataSource = "RTDB"
                                        Log.i(TAG, "getRealtimeAllProducts/launch: Got ${finalImageBase64List.size} images from RTDB for ${stubFromFirestore.productId}")
                                    } else {
                                        Log.w(TAG, "getRealtimeAllProducts/launch: RTDB imagesMap is null or empty for ${stubFromFirestore.productId} despite snapshot existing. Raw value: ${rtdbSnapshot.value}")
                                    }
                                } else {
                                    Log.w(TAG, "getRealtimeAllProducts/launch: No RTDB images node at $rtdbPathChecked (snapshot does not exist).")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "getRealtimeAllProducts/launch: EXCEPTION fetching RTDB images for ${stubFromFirestore.productId}", e)
                                // Don't assign to rtdbFetchError, just let finalImageBase64List remain empty
                            }

                            // 2. <<< FALLBACK TO ROOM IF RTDB FETCH YIELDED NO IMAGES >>>
                            //    This is especially for newly created items where RTDB might have a slight propagation delay.
                            if (finalImageBase64List.isEmpty()) {
                                Log.d(TAG, "getRealtimeAllProducts/launch: RTDB yielded no images for ${stubFromFirestore.productId}. Checking Room as fallback.")
                                val entityFromRoom = productDao.getProductById(stubFromFirestore.productId).firstOrNull() // One-time read from Room
                                if (entityFromRoom != null && entityFromRoom.imageBase64List.isNotEmpty()) {
                                    finalImageBase64List = entityFromRoom.imageBase64List
                                    imageDataSource = "Room (Fallback)"
                                    Log.i(TAG, "getRealtimeAllProducts/launch: Got ${finalImageBase64List.size} images from ROOM FALLBACK for ${stubFromFirestore.productId}")
                                } else {
                                    Log.w(TAG, "getRealtimeAllProducts/launch: No images found in Room fallback either for ${stubFromFirestore.productId}")
                                }
                            }

                            val bitmaps = finalImageBase64List.mapNotNull { base64Str -> base64ToBitmap(base64Str) }
                            Log.i(TAG, "getRealtimeAllProducts/launch: Product ID ${stubFromFirestore.productId}, Image Source: $imageDataSource, Final Bitmaps: ${bitmaps.size}")

                            productListWithImages.add(
                                Product( // Construct domain model directly
                                    productId = stubFromFirestore.productId,
                                    ownerId = stubFromFirestore.ownerId,
                                    title = stubFromFirestore.title,
                                    description = stubFromFirestore.description,
                                    price = stubFromFirestore.price,
                                    location = stubFromFirestore.location,
                                    category = stubFromFirestore.category,
                                    condition = stubFromFirestore.condition,
                                    timestamp = stubFromFirestore.timestamp,
                                    imageBitmaps = bitmaps
                                )
                            )
                        }
                        Log.i(TAG, "getRealtimeAllProducts/launch: Finished processing all stubs. Emitting list of ${productListWithImages.size} products.")
                        trySend(productListWithImages) // Firestore query already orders by timestamp ASC
                    }
                } else {
                    Log.d(TAG, "getRealtimeAllProducts: Firestore snapshot is null in callbackFlow.")
                    trySend(emptyList())
                }
            }
        awaitClose {
            Log.d(TAG, "getRealtimeAllProducts: Closing Firestore listener.")
            listenerRegistration.remove()
        }
    }

    // This is the existing Room-based flow (good for offline/initial load before listener is ready)
    fun getLocalProductsSorted(): Flow<List<Product>> { // Renamed for clarity
        // Ensure your DAO query sorts by timestamp ASC
        return productDao.getAllProducts().map { entities ->
            Log.d("ProductRepository", "getLocalProductsSorted emitting ${entities.size} products from Room")
            entities.map { it.toDomainModel() }
        }
    }
    fun ProductEntity.toDomainModel(): Product {
        Log.d(TAG, "ProductEntity.toDomainModel for ${this.productId}, Entity.imageBase64List size: ${this.imageBase64List.size}")
        val bitmaps = this.imageBase64List.mapNotNull { base64String ->
            val bitmap = base64ToBitmap(base64String)
            if (bitmap == null) Log.w(TAG, "toDomainModel: base64ToBitmap returned null for product ${this.productId} (Base64 len: ${base64String.length})")
            bitmap
        }
        Log.d(TAG, "ProductEntity.toDomainModel for ${this.productId}, converted to ${bitmaps.size} bitmaps.")
        return Product(
            productId = productId,
            ownerId = ownerId,
            title = title,
            description = description,
            price = price,
            location = location,
            category = category,
            condition = condition,
            imageBitmaps = bitmaps,
            timestamp = timestamp // Ensure timestamp is mapped
        )
    }

    fun Product.toEntity(providedBase64List: List<String>): ProductEntity {
        return ProductEntity(
            productId = productId,
            ownerId = ownerId,
            title = title,
            description = description,
            price = price,
            location = location,
            category = category,
            condition = condition,
            imageBase64List = providedBase64List,
            timestamp = timestamp, // Use product's timestamp
            isDeleted = false // Assume not deleted when creating/updating entity from domain
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
            "category" to category,
            "condition" to condition,
            "timestamp" to timestamp,
            "isDeleted" to false
        )
    }

}