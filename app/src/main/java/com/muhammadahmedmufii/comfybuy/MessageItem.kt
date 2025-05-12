package com.muhammadahmedmufii.comfybuy

data class MessageItem(
    val name: String,
    val message: String,
    val time: String,
    val profilePic: Int,
    val hasUnreadMessages: Boolean = false
)