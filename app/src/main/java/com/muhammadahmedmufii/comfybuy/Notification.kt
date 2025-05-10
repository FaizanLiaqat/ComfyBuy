package com.muhammadahmedmufii.comfybuy

data class Notification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val description: String,
    val time: String
)

enum class NotificationType {
    MESSAGE, OFFER, ALERT
}