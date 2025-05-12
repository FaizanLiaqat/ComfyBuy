package com.muhammadahmedmufii.comfybuy.domain.model

import android.graphics.Bitmap

data class Product(
    val productId: String,
    val ownerId: String,
    val title: String,
    val description: String?,
    val price: String,
    val location: String?,
    var imageBitmaps: List<Bitmap> = emptyList(), // Changed from imageBitmap: Bitmap?
    // Optional: Add category and condition
    val category: String? = null,
    val condition: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)