
// --- domain/model/User.kt ---
package com.muhammadahmedmufii.comfybuy.domain.model

import android.graphics.Bitmap

data class User(
    val userId: String,
    var fullName: String?, // Make var if directly editable from this model instance
    var email: String?,    // Make var if directly editable (careful with auth email)
    var profileImageBitmap: Bitmap?,
    var username: String?, // Make var
    var bio: String?,      // Make var
    var location: String?, // Make var
    var phoneNumber: String? = null, // NEW - Make var
    var gender: String? = null,      // NEW - Make var
    var dateOfBirth: String? = null  // NEW - Make var (e.g., "MMMM d, yyyy")
)