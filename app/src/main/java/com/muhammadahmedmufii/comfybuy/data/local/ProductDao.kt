package com.muhammadahmedmufii.comfybuy.data.local // Example package name

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    // Note: Room doesn't have a direct @Delete for marking as deleted.
    // We handle "deletion" by updating the isDeleted flag or removing from the table.
    // Here's a query to delete by ID:
    @Query("DELETE FROM products WHERE productId = :productId")
    suspend fun deleteProductById(productId: String)

    // Get all products
    @Query("SELECT * FROM products WHERE isDeleted = 0 ORDER BY timestamp ASC") // Only get products not marked as deleted
    fun getAllProducts(): Flow<List<ProductEntity>> // Use Flow to observe changes

    // Get products by user ID
    @Query("SELECT * FROM products WHERE ownerId = :userId AND isDeleted = 0")
    fun getUserProducts(userId: String): Flow<List<ProductEntity>>

    // Get a single product by ID
    @Query("SELECT * FROM products WHERE productId = :productId LIMIT 1")
    fun getProductById(productId: String): Flow<ProductEntity?>

    // Query for products that need to be synced (e.g., newly created, updated, or marked for deletion)
    // The criteria for syncing will depend on your sync strategy.
    // Example: get products modified after a certain timestamp or marked as not synced yet
    @Query("SELECT * FROM products WHERE timestamp > :lastSyncTimestamp OR isDeleted = 1")
    fun getProductsToSync(lastSyncTimestamp: Long): List<ProductEntity> // Use List for one-time fetch

    // Add other query methods for search, filtering, etc.
}