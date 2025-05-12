// --- ui/sellerprofile/SellerProfileViewModelFactory.kt ---
package com.muhammadahmedmufii.comfybuy.ui.sellerprofile

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SellerProfileViewModelFactory(
    private val application: Application,
    private val sellerId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SellerProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SellerProfileViewModel(application, sellerId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for SellerProfile")
    }
}