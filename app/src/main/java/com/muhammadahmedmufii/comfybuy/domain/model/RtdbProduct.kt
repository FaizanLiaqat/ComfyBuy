package com.muhammadahmedmufii.comfybuy.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class RtdbProduct(
    var productId: String = "",
    var ownerId: String = "",
    var title: String = "",
    var description: String? = null,
    var price: String = "",
    var location: String? = null,
    var category: String? = null,
    var condition: String? = null,
    var timestamp: Long = System.currentTimeMillis(),
    var isDeleted: Boolean = false, // For soft deletes in RTDB if needed
    var images: List<String>? = null// Map of "0" -> "base64_string"
) {
    // No-arg constructor for Firebase
    constructor() : this("", "", "", null, "", null, null, null, 0L, false, null)
}