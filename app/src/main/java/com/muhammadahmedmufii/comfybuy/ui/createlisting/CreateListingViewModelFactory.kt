// --- ui/createlisting/CreateListingViewModelFactory.kt ---
package com.muhammadahmedmufii.comfybuy.ui.createlisting

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CreateListingViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateListingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateListingViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}