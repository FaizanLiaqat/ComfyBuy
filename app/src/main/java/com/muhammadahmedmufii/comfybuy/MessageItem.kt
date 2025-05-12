// MessageItem.kt
package com.muhammadahmedmufii.comfybuy

data class MessageItem(
    val userId: String, // ID of the other user in the chat
    val name: String,
    val message: String,
    val time: String,
    val profilePicResId: Int, // Keep as ResId for sample, change to String? for real data
    val hasUnreadMessages: Boolean = false
)