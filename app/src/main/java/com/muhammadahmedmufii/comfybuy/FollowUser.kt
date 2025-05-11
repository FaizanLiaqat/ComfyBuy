package com.muhammadahmedmufii.comfybuy

import com.google.firebase.firestore.DocumentId

data class FollowUser(
    @DocumentId
    val userId: String = "",
    val name: String = "",
    val username: String = "",
    val profilePicUrl: String = "",
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val bio: String = "",
    var isFollowing: Boolean = false
) {
    // Empty constructor for Firestore
    constructor() : this("", "", "", "", 0, 0, "", false)

    // Constructor that accepts a resource ID for profile picture (for backward compatibility)
    constructor(
        name: String,
        username: String,
        profilePic: Int,
        isFollowing: Boolean
    ) : this("", name, username, "", 0, 0, "", isFollowing) {
        // This constructor maintains compatibility with your existing code
        // The profilePic Int is not stored, but you can use it locally before saving to Firebase
    }
}