package com.muhammadahmedmufii.comfybuy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String = "",
    val fullName: String? = null,
    val email: String? = null,
    var profileImageBase64: String? = null,
    val username: String? = null,
    val bio: String? = null,
    val location: String? = null,
    val phoneNumber: String? = null, // NEW
    val gender: String? = null,      // NEW
    val dateOfBirth: String? = null, // NEW (Store as String, e.g., "YYYY-MM-DD" or "MMMM d, yyyy")
    val timestamp: Long = System.currentTimeMillis()
) {
    // No-arg constructor for Firestore/Room
    constructor() : this(
        userId = "",
        fullName = null,
        email = null,
        profileImageBase64 = null,
        username = null,
        bio = null,
        location = null,
        phoneNumber = null, // NEW
        gender = null,      // NEW
        dateOfBirth = null, // NEW
        timestamp = System.currentTimeMillis()
    )
}