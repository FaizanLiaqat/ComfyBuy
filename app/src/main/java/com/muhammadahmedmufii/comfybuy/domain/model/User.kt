package com.muhammadahmedmufii.comfybuy.domain.model

import android.graphics.Bitmap

data class User(
    val userId: String,
    var fullName: String?,
    var email: String?,
    var profileImageBitmap: Bitmap?,
    var username: String?,
    var bio: String?,
    var location: String?,
    var phoneNumber: String? = null,
    var gender: String? = null,
    var dateOfBirth: String? = null,
    val followerCount: Int = 0,    // These will be harder to maintain in RTDB without transactions
    val followingCount: Int = 0,  // or Cloud Functions. For now, keeping them.
    val productCount: Int = 0,    // This would be a derived count.
    val timestamp: Long = System.currentTimeMillis() // Ensure this exists
)