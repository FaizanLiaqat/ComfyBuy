package com.muhammadahmedmufii.comfybuy.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query // For Query.Direction
import com.google.firebase.firestore.SetOptions
import com.muhammadahmedmufii.comfybuy.data.local.ProductDao
import com.muhammadahmedmufii.comfybuy.data.local.ProductEntity
import com.muhammadahmedmufii.comfybuy.data.model.RtdbProduct
import com.muhammadahmedmufii.comfybuy.domain.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi // For callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class ProductRepository @Inject constructor(
//    private val productDao: ProductDao,
//    private val firestore: FirebaseFirestore,
    private val realtimeDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth
) {
    private val TAG = "ProductRepo" // Logging Tag
//    private val productsCollection = firestore.collection("products")
    private val productsRtdbRef = realtimeDatabase.getReference("products")
    // --- Base64 Conversion Helpers (bitmapToBase64, base64ToBitmap) remain the same ---
    private fun bitmapToBase64(bitmap: Bitmap?): String? {
        if (bitmap == null) { Log.w(TAG, "bitmapToBase64: Received null bitmap."); return null }
        return try {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos) // Quality 75
            val byteArray = baos.toByteArray()
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            Log.d(TAG, "bitmapToBase64: Converted bitmap (${bitmap.width}x${bitmap.height}) to Base64 (len: ${base64String.length})")
            base64String
        } catch (e: Exception) { Log.e(TAG, "bitmapToBase64: Error converting bitmap", e); null }
    }

    private fun base64ToBitmap(base64String: String?): Bitmap? {
        if (base64String.isNullOrEmpty()) { Log.w(TAG, "base64ToBitmap: Received null/empty string."); return null }
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            if (bitmap != null) Log.d(TAG, "base64ToBitmap: Converted Base64 (len: ${base64String.length}) to Bitmap (${bitmap.width}x${bitmap.height})")
            else Log.e(TAG, "base64ToBitmap: decodeByteArray null for Base64 (len: ${base64String.length})")
            bitmap
        } catch (e: Exception) { Log.e(TAG, "base64ToBitmap: Error for Base64 (len: ${base64String?.length})", e); null }
    }

    // getAllProducts, getProductById, getUserProducts structure is fine,
    // but they will benefit from the updated toDomainModel()

//    fun getAllProducts(): Flow<List<Product>> {
//        return productDao.getAllProducts().map { entities ->
//            entities.map { it.toDomainModel() }
//        }
//    }
//
//    fun getProductById(productId: String): Flow<Product?> {
//        return productDao.getProductById(productId).map { entity ->
//            entity?.toDomainModel()
//        }
//    }
//
//    fun getUserProducts(userId: String): Flow<List<Product>> {
//        return productDao.getUserProducts(userId).map { entities ->
//            entities.map { it.toDomainModel() }
//        }
//    }

    private fun RtdbProduct.toDomainModel(): Product {
        val imageBase64List = this.images ?: emptyList()

        return Product(
            productId = this.productId,
            ownerId = this.ownerId,
            title = this.title,
            description = this.description,
            price = this.price,
            location = this.location,
            category = this.category,
            condition = this.condition,
            timestamp = this.timestamp,
            imageBitmaps = imageBase64List.mapNotNull { b64 -> base64ToBitmap(b64) }
            // isDeleted from RtdbProduct is used for filtering, not directly part of domain here
        )
    }
    private fun Product.toRtdbData(): RtdbProduct {
        val imageBase64List = this.imageBitmaps.mapNotNull { bitmapToBase64(it) }
        val imagesToStore = imageBase64List.ifEmpty { null } // Store null in RTDB if no images, not an empty map

        return RtdbProduct(
            productId = this.productId,
            ownerId = this.ownerId, // This should be set when Product is created
            title = this.title,
            description = this.description,
            price = this.price,
            location = this.location,
            category = this.category,
            condition = this.condition,
            timestamp = this.timestamp, // Use product's timestamp for consistency
            isDeleted = false,          // Default to false when saving
            images = imagesToStore
        )
    }
    suspend fun saveProduct(product: Product) = withContext(Dispatchers.IO) {
        Log.i(TAG, "saveProduct (RTDB-Only): Starting for productId: ${product.productId}, image count: ${product.imageBitmaps.size}")
        try {
            val rtdbData = product.toRtdbData() // This now includes images map
            productsRtdbRef.child(product.productId).setValue(rtdbData).await()
            Log.i(TAG, "saveProduct (RTDB-Only): Saved to RTDB: ${product.productId}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving product ${product.productId} to RTDB", e)
            throw e
        }
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getProductById(productId: String): Flow<Product?> = callbackFlow {
        Log.d(TAG, "getProductById (RTDB-Only): Listening to product $productId")
        val productRef = productsRtdbRef.child(productId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Log.w(TAG, "getProductById (RTDB-Only): Product $productId does not exist.")
                    trySend(null).isSuccess
                    return
                }
                try {
                    val rtdbProduct = snapshot.getValue(RtdbProduct::class.java)
                    if (rtdbProduct == null || rtdbProduct.isDeleted) {
                        Log.d(TAG, "getProductById (RTDB-Only): Product $productId is null after getValue or marked deleted.")
                        trySend(null).isSuccess
                    } else {
                        val domainProduct = rtdbProduct.toDomainModel()
                        Log.d(TAG, "getProductById (RTDB-Only): Data for $productId. Domain Product: $domainProduct")
                        trySend(domainProduct).isSuccess
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "getProductById (RTDB-Only): Error parsing product data for $productId", e)
                    trySend(null).isSuccess // Send null on parsing error
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "getProductById (RTDB-Only): Listener cancelled for $productId", error.toException())
                close(error.toException())
            }
        }
        productRef.addValueEventListener(listener)
        awaitClose {
            Log.d(TAG, "getProductById (RTDB-Only): Closing listener for $productId")
            productRef.removeEventListener(listener)
        }
    }.flowOn(Dispatchers.IO)



    @OptIn(ExperimentalCoroutinesApi::class)
    fun getUserProducts(userId: String): Flow<List<Product>> = callbackFlow {
        Log.d(TAG, "getUserProducts (RTDB-Only): Listening for ownerId: $userId")
        // Query RTDB for products where 'ownerId' field equals the given userId
        val query = productsRtdbRef.orderByChild("ownerId").equalTo(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = snapshot.children.mapNotNull { productSnapshot ->
                    productSnapshot.getValue(RtdbProduct::class.java)
                        ?.takeIf { !it.isDeleted } // Filter out products marked as deleted
                        ?.toDomainModel()
                }.sortedBy { it.timestamp } // Sort by timestamp client-side for display consistency
                Log.d(TAG, "getUserProducts (RTDB-Only): Snapshot received, ${products.size} products for user $userId")
                trySend(products).isSuccess
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "getUserProducts (RTDB-Only): Listener cancelled for user $userId", error.toException())
                close(error.toException())
            }
        }
        query.addValueEventListener(listener)
        awaitClose {
            Log.d(TAG, "getUserProducts (RTDB-Only): Closing listener for user $userId")
            query.removeEventListener(listener)
        }
    }.flowOn(Dispatchers.IO)


    suspend fun updateProduct(updatedDomainProduct: Product, newImageBitmaps: List<Bitmap>?) = withContext(Dispatchers.IO) {
        Log.i(TAG, "updateProduct (RTDB-Only): Updating productId: ${updatedDomainProduct.productId}")
        try {
            // Determine the final list of bitmaps
            val finalBitmaps = newImageBitmaps ?: updatedDomainProduct.imageBitmaps

            val rtdbDataToUpdate = updatedDomainProduct.copy(imageBitmaps = finalBitmaps).toRtdbData()
            // Ensure timestamp is updated if it's a true update operation
            // If product.timestamp was from the original, it might not reflect the update time.
            // It's better to set it here or ensure toRtdbData() uses current time for updates if desired.
            // For simplicity, toRtdbData will use Product.timestamp. If you need to update it:
            // rtdbDataToUpdate.timestamp = System.currentTimeMillis()

            productsRtdbRef.child(updatedDomainProduct.productId).setValue(rtdbDataToUpdate).await()
            Log.i(TAG, "updateProduct (RTDB-Only): Successfully updated product ${updatedDomainProduct.productId} in RTDB.")

        } catch (e: Exception) {
            Log.e(TAG, "Error updating product ${updatedDomainProduct.productId} in RTDB", e)
            throw e
        }
    }


    // deleteProduct: Soft delete by setting isDeleted = true in RTDB, or hard delete by removing node.
    suspend fun deleteProduct(productId: String, isHardDelete: Boolean = false) = withContext(Dispatchers.IO) {
        Log.i(TAG, "deleteProduct (RTDB-Only): Deleting productId: $productId, HardDelete: $isHardDelete")
        try {
            if (isHardDelete) {
                productsRtdbRef.child(productId).removeValue().await()
                Log.d(TAG, "Hard deleted product $productId from RTDB.")
            } else {
                // Soft delete: update the isDeleted flag
                productsRtdbRef.child(productId).child("isDeleted").setValue(true).await()
                Log.d(TAG, "Soft deleted product $productId in RTDB (set isDeleted=true).")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting product $productId from RTDB", e)
            throw e
        }
    }

    // SyncWorker methods are now effectively placeholders or would need a different purpose.
    // With direct RTDB listeners, explicit sync from remote to Room is for cache priming.
    // Push sync is for offline edits, which we've disabled by removing Room writes.
    suspend fun syncProductsFromRemote() {
        Log.w(TAG, "syncProductsFromRemote (RTDB-Only): This is now for manually priming Room cache if re-enabled. Not actively used by UI flows.")
        // If you re-enable Room, this would fetch all from RTDB and populate Room.
        // Example (if Room were active):
        // try {
        //     val snapshot = productsRtdbRef.orderByChild("timestamp").get().await()
        //     val productList = snapshot.children.mapNotNull { rtdbSnapshotToProduct(it) }
        //          .filter { !(it.isDeleted ?: false) }
        //     if (productList.isNotEmpty()) {
        //         val entities = productList.map { p -> p.toEntity(p.imageBitmaps.mapNotNull{b->bitmapToBase64(b)}) }
        //         productDao?.insertAllProducts(entities)
        //         Log.i(TAG, "syncProductsFromRemote (RTDB-Only): Manually synced ${entities.size} products to Room.")
        //     }
        // } catch (e: Exception) {
        //     Log.e(TAG, "syncProductsFromRemote (RTDB-Only): Error during manual sync", e)
        // }
    }

    suspend fun syncProductsToRemote(lastSyncTimestamp: Long) {
        Log.w(TAG, "syncProductsToRemote (RTDB-Only): This method is for offline changes to Room, which are currently disabled.")
        // If Room and offline edits were enabled, this would push Room changes to RTDB.
    }

    // --- NEW: Real-time Flow from Firestore & RTDB ---
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getRealtimeAllProducts(): Flow<List<Product>> = callbackFlow {
        Log.i(TAG, "getRealtimeAllProducts (RTDB-Only): Setting up listener.")
        // Order by timestamp to get newest items last (so they appear at the bottom of a list)
        val query = productsRtdbRef.orderByChild("timestamp")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i(TAG, "getRealtimeAllProducts (RTDB-Only): Snapshot. Children: ${snapshot.childrenCount}")
                val productList = snapshot.children.mapNotNull { productSnapshot ->
                    productSnapshot.getValue(RtdbProduct::class.java)
                        ?.takeIf { !it.isDeleted } // Filter out products marked as deleted
                        ?.toDomainModel()
                }
                // RTDB query orderByChild("timestamp") sorts numbers in ascending order.
                Log.i(TAG, "getRealtimeAllProducts (RTDB-Only): Emitting ${productList.size} products.")
                trySend(productList).isSuccess
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "getRealtimeAllProducts (RTDB-Only): Listener cancelled", error.toException())
                close(error.toException())
            }
        }
        query.addValueEventListener(listener)
        awaitClose {
            Log.d(TAG, "getRealtimeAllProducts (RTDB-Only): Closing listener.")
            query.removeEventListener(listener)
        }
    }.flowOn(Dispatchers.IO)


//    // This is the existing Room-based flow (good for offline/initial load before listener is ready)
//    fun getLocalProductsSorted(): Flow<List<Product>> { // Renamed for clarity
//        // Ensure your DAO query sorts by timestamp ASC
//        return productDao.getAllProducts().map { entities ->
//            Log.d("ProductRepository", "getLocalProductsSorted emitting ${entities.size} products from Room")
//            entities.map { it.toDomainModel() }
//        }
//    }
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