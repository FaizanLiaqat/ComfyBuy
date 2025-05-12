package com.muhammadahmedmufii.comfybuy.data.model

import com.google.firebase.Timestamp

data class Follow(
    val followerId: String = "",
    val followingId: String = "",
    val timestamp: Timestamp = Timestamp.now()
) {
    // Empty constructor for Firestore
    constructor() : this("", "", Timestamp.now())
}