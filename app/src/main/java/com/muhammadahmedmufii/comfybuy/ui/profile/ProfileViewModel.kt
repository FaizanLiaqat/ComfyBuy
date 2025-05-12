package com.muhammadahmedmufii.comfybuy.ui.profile // Example package name for UI components

import android.app.Application
import androidx.lifecycle.AndroidViewModel // Use AndroidViewModel to get Application context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData // To convert Flow to LiveData
import androidx.lifecycle.liveData
// Use viewModelScope for coroutines in ViewModel
import androidx.lifecycle.switchMap // Import switchMap
import com.muhammadahmedmufii.comfybuy.data.local.AppDatabase // Import AppDatabase
import com.muhammadahmedmufii.comfybuy.data.repository.ProductRepository // Import ProductRepository
import com.muhammadahmedmufii.comfybuy.data.repository.UserRepository // Import UserRepository
import com.muhammadahmedmufii.comfybuy.domain.model.Product // Import Product domain model
import com.muhammadahmedmufii.comfybuy.domain.model.User // Import User domain model
import com.google.firebase.auth.FirebaseAuth // Import FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore // Import FirebaseFirestore
import com.google.firebase.database.FirebaseDatabase // Import FirebaseDatabase


// ViewModel for the ProfileActivity
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    // Manually get repository instances (ideally use Dependency Injection)
    private val database = AppDatabase.getDatabase(application)
    private val userDao = database.userDao()
    private val productDao = database.productDao()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val realtimeDatabase = FirebaseDatabase.getInstance("https://messamfaizanahmed-default-rtdb.asia-southeast1.firebasedatabase.app")
    val firebaseDatabase = FirebaseDatabase.getInstance("https://messamfaizanahmed-default-rtdb.asia-southeast1.firebasedatabase.app") // Get RTDB instance

    // Pass RTDB instance to UserRepository
//    private val userRepository = UserRepository(userDao, firebaseAuth, firestore, firebaseDatabase)
//    private val productRepository = ProductRepository(productDao, firestore, realtimeDatabase)
    private val userRepository = UserRepository( firebaseAuth, firebaseDatabase)

    // Get the current authenticated user's ID
    private val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    // Expose the current user's data from the repository as LiveData
    val currentUser: LiveData<User?> = userRepository.getCurrentUser().asLiveData()

    // Expose the current user's products from the repository as LiveData
    // We use switchMap to get the user's ID from the currentUser LiveData
    // and then fetch their products using that ID.
//    val userProducts: LiveData<List<Product>> = currentUser.switchMap { user ->
//        val userId = user?.userId
//        if (userId != null) {
//            // If user is logged in, get their products from the repository
//            productRepository.getUserProducts(userId).asLiveData() // Assuming ProductRepository has getUserProducts
//        } else {
//            // If no user is logged in, return a LiveData with an empty list
//            liveData { emit(emptyList()) } // Requires import androidx.lifecycle.liveData
//        }
//    }

    // TODO: Add LiveData for Favorites and Reviews if you implement those tabs

    // You can add functions here for:
    // - Updating profile picture (interact with UserRepository)
    // - Updating other user details (interact with UserRepository)
    // - Handling tab selections (filter the userProducts LiveData or fetch different data)

    // Example: Function to update profile picture (requires a Bitmap or Uri)
    // fun updateProfilePicture(bitmap: Bitmap) {
    //     val userId = currentUserId
    //     if (userId != null) {
    //         viewModelScope.launch {
    //             // Convert bitmap to Base64 and update user in UserRepository
    //             // This would require adding a function to UserRepository to update user with new image
    //             // userRepository.updateProfileImage(userId, bitmapToBase64(bitmap))
    //         }
    //     }
    // }
}

// Factory for creating ProfileViewModel with Application context
class ProfileViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
