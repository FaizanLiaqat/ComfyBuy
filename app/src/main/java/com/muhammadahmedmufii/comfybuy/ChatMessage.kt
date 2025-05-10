package com.muhammadahmedmufii.comfybuy

import android.net.Uri

sealed class ChatMessage(val time: String) {
    class ReceivedText(val text: String, time: String): ChatMessage(time)
    class SentText(val text: String, time: String): ChatMessage(time)
    class ReceivedImage(val uri: Uri, time: String): ChatMessage(time)
    class SentImage(val uri: Uri, time: String): ChatMessage(time)
}
