// --- ui/createlisting/CreateListingViewModel.kt ---
// --- ui/createlisting/CreateListingViewModel.kt ---
package com.muhammadahmedmufii.comfybuy.ui.createlisting

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.muhammadahmedmufii.comfybuy.data.local.AppDatabase
import com.muhammadahmedmufii.comfybuy.data.repository.ProductRepository
import com.muhammadahmedmufii.comfybuy.domain.model.Product
import kotlinx.coroutines.launch
import java.util.UUID

class CreateListingViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "CreateListingVM"
    private val productRepository: ProductRepository
    private val firebaseAuth: FirebaseAuth

    private val _saveStatus = MutableLiveData<ListingSaveStatus>()
    val saveStatus: LiveData<ListingSaveStatus> get() = _saveStatus

    init {
//        val db = AppDatabase.getDatabase(application)
        firebaseAuth = FirebaseAuth.getInstance()
//        val firestore = FirebaseFirestore.getInstance()
        val rtdb = FirebaseDatabase.getInstance("https://messamfaizanahmed-default-rtdb.asia-southeast1.firebasedatabase.app")
        productRepository = ProductRepository(rtdb, firebaseAuth)
        Log.d(TAG, "ViewModel initialized with RTDB-only ProductRepository.")
    }

    fun createListing(
        title: String,
        category: String,
        condition: String,
        price: String,
        description: String?,
        location: String?,
        imageBitmaps: List<Bitmap>
    ) {
        _saveStatus.value = ListingSaveStatus.Loading
        val currentUserId = firebaseAuth.currentUser?.uid

        if (currentUserId == null) {
            _saveStatus.value = ListingSaveStatus.Error("User not authenticated. Please log in.")
            return
        }

        val productId = UUID.randomUUID().toString()
        val currentTimestamp = System.currentTimeMillis() // Ensure consistent timestamp

        val newProduct = Product(
            productId = productId,
            ownerId = currentUserId,
            title = title,
            description = description,
            price = price,
            location = location,
            category = category,
            condition = condition,
            imageBitmaps = imageBitmaps,
            timestamp = currentTimestamp // Use consistent timestamp
        )
        Log.i(TAG, "createListing: Attempting to save product via repository. Product ID: ${newProduct.productId}, Image count: ${newProduct.imageBitmaps.size}")
        viewModelScope.launch {
            try {
                // Log.d("CreateListingVM", "Saving product with ${imageBitmaps.size} images: $newProduct") // Redundant with above
                productRepository.saveProduct(newProduct)
                Log.i(TAG, "createListing: productRepository.saveProduct call successful for ${newProduct.productId}")
                _saveStatus.postValue(ListingSaveStatus.Success("Listing created successfully!"))
            } catch (e: Exception) {
                Log.e(TAG, "createListing: Failed for product ${newProduct.productId}", e)
                _saveStatus.postValue(ListingSaveStatus.Error("Failed to create listing: ${e.message}"))
            }
        }
    }
}
// ListingSaveStatus sealed class remains the same
sealed class ListingSaveStatus {
    object Idle : ListingSaveStatus()
    object Loading : ListingSaveStatus()
    data class Success(val message: String) : ListingSaveStatus()
    data class Error(val message: String) : ListingSaveStatus()
}

