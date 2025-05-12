
package com.muhammadahmedmufii.comfybuy.ui.messages

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.muhammadahmedmufii.comfybuy.MessageItem // Your data class
import com.muhammadahmedmufii.comfybuy.R // Your resource class

class MessagesViewModel(application: Application) : AndroidViewModel(application) {

    private val _chatList = MutableLiveData<List<MessageItem>>()
    val chatList: LiveData<List<MessageItem>> get() = _chatList

    // TODO: Implement logic to fetch chat list from Firebase RTDB
    // This would involve:
    // 1. Getting current user's ID.
    // 2. Querying an RTDB node like /user-chats/{currentUserId} which lists all their chats.
    // 3. For each chat, fetching details of the other user (name, profile pic) and last message.
    // 4. Populating _chatList.

    fun loadUserChats() {
        // Placeholder - replace with actual RTDB fetching
        val sampleMessages = listOf(
            MessageItem("user123", "Emily Parker", "The vintage lamp is still available?", "2m ago", R.drawable.avatar_placeholder, true),
            MessageItem("user456", "Michael Chen", "Great! I'll pick it up tomorrow", "1h ago", R.drawable.avatar_placeholder, false),
            MessageItem("user789", "Sophie Williams", "Can you do $40 for the chair?", "2h ago", R.drawable.avatar_placeholder, true)
        )
        _chatList.value = sampleMessages
    }
}

