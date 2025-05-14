package com.muhammadahmedmufii.comfybuy

data class MessageDto(
    val senderId:  String = "",
    val text:      String?  = null,
    val imageUrl:  String?  = null, // if this is an HTTPS URL, we treat it as Storage URL
    val timestamp: Long     = 0L
)
