package com.muhammadahmedmufii.comfybuy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters // Import TypeConverters

@Entity(tableName = "products")
@TypeConverters(StringListConverter::class) // Add TypeConverter for List<String>
data class ProductEntity(
    @PrimaryKey
    val productId: String = "", // Default
    val ownerId: String = "",   // Default
    val title: String = "",     // Default
    val description: String? = null, // Nullable, default is null
    val price: String = "",     // Default
    val location: String? = null, // Nullable, default is null
    val imageBase64List: List<String> = emptyList(), // Default
    val timestamp: Long = System.currentTimeMillis(), // Default
    val isDeleted: Boolean = false, // Default
    val category: String? = null, // Nullable, default is null
    val condition: String? = null // Nullable, default is null
)
// No-arg constructor is automatically generated for data classes with default values for all properties.
// If you had non-defaulted properties, you'd need one.