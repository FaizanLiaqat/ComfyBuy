// --- ui/messages/MessagesViewModelFactory.kt ---
package com.muhammadahmedmufii.comfybuy.ui.messages

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MessagesViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessagesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MessagesViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}