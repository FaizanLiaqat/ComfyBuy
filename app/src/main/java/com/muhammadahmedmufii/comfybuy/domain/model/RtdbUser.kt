package com.muhammadahmedmufii.comfybuy.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class RtdbUser(
    var userId: String = "",
    var fullName: String? = null,
    var email: String? = null,
    var username: String? = null,
    var bio: String? = null,
    var location: String? = null,
    var phoneNumber: String? = null,
    var gender: String? = null,
    var dateOfBirth: String? = null,
    var timestamp: Long = System.currentTimeMillis(),
    var profileImageBase64: String? = null // User's profile image Base64
) {
    // No-arg constructor for Firebase deserialization
    constructor() : this("", null, null, null, null, null, null, null, null, 0L, null)
}