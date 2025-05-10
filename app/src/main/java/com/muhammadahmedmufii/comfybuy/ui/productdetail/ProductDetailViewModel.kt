package com.muhammadahmedmufii.comfybuy.ui.productdetail // Example package name

import android.app.Application
import androidx.lifecycle.AndroidViewModel // Use AndroidViewModel to get Application context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData // To convert Flow to LiveData
 // Use viewModelScope for coroutines in ViewModel
import androidx.lifecycle.switchMap // Import switchMap
import androidx.lifecycle.liveData // *** NEW: Import liveData builder ***

import com.muhammadahmedmufii.comfybuy.data.local.AppDatabase // Import AppDatabase
import com.muhammadahmedmufii.comfybuy.data.repository.ProductRepository // Import ProductRepository
import com.muhammadahmedmufii.comfybuy.data.repository.UserRepository // Import UserRepository
import com.muhammadahmedmufii.comfybuy.domain.model.Product // Import Product domain model
import com.muhammadahmedmufii.comfybuy.domain.model.User // Import User domain model
import com.google.firebase.firestore.FirebaseFirestore // Import FirebaseFirestore
import com.google.firebase.database.FirebaseDatabase // Import FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth // Import FirebaseAuth
import kotlinx.coroutines.launch // Import launch for coroutines


// ViewModel for the ProductDetailActivity
class ProductDetailViewModel(application: Application, private val productId: String) : AndroidViewModel(application) {

    // Manually get repository instances (ideally use Dependency Injection)
    private val database = AppDatabase.getDatabase(application)
    private val productDao = database.productDao()
    private val userDao = database.userDao() // Needed to fetch seller info
    private val firestore = FirebaseFirestore.getInstance()
    private val realtimeDatabase = FirebaseDatabase.getInstance("https://messamfaizanahmed-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val firebaseAuth = FirebaseAuth.getInstance() // Might need for current user info

    private val productRepository = ProductRepository(productDao, firestore, realtimeDatabase)
    val firebaseDatabase = FirebaseDatabase.getInstance("https://messamfaizanahmed-default-rtdb.asia-southeast1.firebasedatabase.app") // Get RTDB instance
    private val userRepository = UserRepository(userDao, firebaseAuth, firestore, firebaseDatabase) // Initialize UserRepository

    // Expose the product data from the repository as LiveData
    // The UI will observe this LiveData
    val product: LiveData<Product?> = productRepository.getProductById(productId).asLiveData()

    // LiveData to hold the seller's user data
    val seller: LiveData<User?> = product.switchMap { product ->
        val ownerId = product?.ownerId // Get the ownerId from the product
        if (ownerId != null) {
            // Fetch the seller's user data from the UserRepository using the NEW getUserById function
            userRepository.getUserById(ownerId).asLiveData()
        } else {
            // If ownerId is null, return a LiveData with null using the liveData builder
            liveData { emit(null) }
        }
    }


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

