package com.muhammadahmedmufii.comfybuy.ui.productdetail // Example package name

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider // Keep if factory is used, but not strictly needed if default factory is ok
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
// import com.google.firebase.firestore.FirebaseFirestore // REMOVED
// import com.muhammadahmedmufii.comfybuy.data.local.AppDatabase // REMOVED
// import com.muhammadahmedmufii.comfybuy.data.local.ProductDao // REMOVED
// import com.muhammadahmedmufii.comfybuy.data.local.UserDao // REMOVED
import com.muhammadahmedmufii.comfybuy.data.repository.ProductRepository
import com.muhammadahmedmufii.comfybuy.data.repository.UserRepository
import com.muhammadahmedmufii.comfybuy.domain.model.Product
import com.muhammadahmedmufii.comfybuy.domain.model.User
import kotlinx.coroutines.Dispatchers

// ViewModel for the ProductDetailActivity
class ProductDetailViewModel(application: Application, private val productId: String) : AndroidViewModel(application) {
    private val TAG = "ProductDetailVM"
    // Manually get repository instances (ideally use Dependency Injection)
    private val productRepository: ProductRepository
    private val userRepository: UserRepository
    private val firebaseAuth: FirebaseAuth// Initialize UserRepository

    // Expose the product data from the repository as LiveData
    // The UI will observe this LiveData
//    val product: LiveData<Product?> = productRepository.getProductById(productId).asLiveData()

    // LiveData to hold the seller's user data
//    val seller: LiveData<User?> = product.switchMap { product ->
//        val ownerId = product?.ownerId // Get the ownerId from the product
//        if (ownerId != null) {
//            // Fetch the seller's user data from the UserRepository using the NEW getUserById function
//            userRepository.getUserById(ownerId).asLiveData()
//        } else {
//            // If ownerId is null, return a LiveData with null using the liveData builder
//            liveData { emit(null) }
//        }
//    }
    init {
        Log.d(TAG, "Initializing for productId: $productId")
        // Room and Firestore components are removed
        val realtimeDatabase = FirebaseDatabase.getInstance("https://messamfaizanahmed-default-rtdb.asia-southeast1.firebasedatabase.app")
        firebaseAuth = FirebaseAuth.getInstance()

        // Instantiate repositories with RTDB and FirebaseAuth only
        productRepository = ProductRepository(realtimeDatabase, firebaseAuth)
        userRepository = UserRepository(firebaseAuth, realtimeDatabase) // Ensure constructor matches
        Log.d(TAG, "Repositories initialized for RTDB.")
    }

    val product: LiveData<Product?> = productRepository.getProductById(productId)
        .asLiveData(viewModelScope.coroutineContext + Dispatchers.IO) // Specify dispatcher for flow collection


    val seller: LiveData<User?> = product.switchMap { productData ->
        val ownerId = productData?.ownerId
        Log.d(TAG, "Product LiveData changed, ownerId: $ownerId for productId: ${productData?.productId}")
        if (ownerId != null) {
            Log.d(TAG, "Fetching seller details for ownerId: $ownerId")
            userRepository.getUserById(ownerId)
                .asLiveData(viewModelScope.coroutineContext + Dispatchers.IO) // Collect on IO thread
        } else {
            Log.w(TAG, "Product ownerId is null for productId: ${productData?.productId}, emitting null for seller.")
            liveData { emit(null) }
        }
    }



    // Example: Placeholder for like functionality
    // fun toggleLikeProduct() {
    //     val currentProduct = product.value
    //     val currentUser = firebaseAuth.currentUser
    //     if (currentProduct != null && currentUser != null) {
    //         Log.d(TAG, "Like toggled for product: ${currentProduct.productId} by user: ${currentUser.uid}")
    //         // TODO: Implement actual like logic (e.g., update a 'likes' node in RTDB)
    //     }
    // }


    // You can add functions here for:
    // - Handling the "Like" button click (update local DB and sync)
    // - Handling the "Chat with Seller" button click (start a new chat)
    // - Handling the "Options" menu (e.g., edit/delete product if current user is the owner)

    // Example: Function to handle liking/unliking a product
    // This would require adding a 'isLiked' field to your Product entity/domain model
    // fun toggleLikeStatus(productId: String, isLiked: Boolean) {
    //     viewModelScope.launch {
    //         // Update the liked status in the repository (which updates Room and syncs to Firebase)
    //         productRepository.updateProductLikedStatus(productId, isLiked) // You'll need to add this method to ProductRepository
    //     }
    // }
}

