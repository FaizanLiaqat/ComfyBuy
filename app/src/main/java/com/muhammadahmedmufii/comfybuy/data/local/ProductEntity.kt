package com.muhammadahmedmufii.comfybuy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters // Import TypeConverters

@Entity(tableName = "products")
@TypeConverters(StringListConverter::class) // Add TypeConverter for List<String>
data class ProductEntity(
    @PrimaryKey
    val productId: String,
    val ownerId: String,
    val title: String,
    val description: String?,
    val price: String,
    val location: String?,
    // Changed from imageBase64: String? to a list
    val imageBase64List: List<String> = emptyList(), // Stores list of Base64 image strings
    val timestamp: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    // Optional: Add category and condition if they become structured fields
    val category: String? = null,
    val condition: String? = null
)
// No-arg constructor is automatically generated for data classes with default values for all properties.
// If you had non-defaulted properties, you'd need one.