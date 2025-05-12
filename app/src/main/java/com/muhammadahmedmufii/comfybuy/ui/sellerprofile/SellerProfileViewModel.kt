package com.muhammadahmedmufii.comfybuy.ui.sellerprofile // New or existing package

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.muhammadahmedmufii.comfybuy.data.local.AppDatabase
import com.muhammadahmedmufii.comfybuy.data.repository.ProductRepository
import com.muhammadahmedmufii.comfybuy.data.repository.UserRepository
import com.muhammadahmedmufii.comfybuy.domain.model.Product
import com.muhammadahmedmufii.comfybuy.domain.model.User
import androidx.lifecycle.asLiveData // For converting Flow to LiveData

class SellerProfileViewModel(application: Application, private val sellerId: String) : AndroidViewModel(application) {

    private val userRepository: UserRepository
    private val productRepository: ProductRepository

    val sellerDetails: LiveData<User?>
    val sellerProducts: LiveData<List<Product>>

    init {
        Log.d("SellerProfileVM", "Initializing for sellerId: $sellerId")
        val database = AppDatabase.getDatabase(application)
        val firebaseAuth = FirebaseAuth.getInstance() // Needed by UserRepository
        val firestore = FirebaseFirestore.getInstance()
        val realtimeDatabase = FirebaseDatabase.getInstance("https://messamfaizanahmed-default-rtdb.asia-southeast1.firebasedatabase.app")

        userRepository = UserRepository(database.userDao(), firebaseAuth, firestore, realtimeDatabase)
        productRepository = ProductRepository(database.productDao(), firestore, realtimeDatabase)

        // Fetch details for the specific sellerId
        sellerDetails = userRepository.getUserById(sellerId).asLiveData()
        // Fetch products for the specific sellerId
        sellerProducts = productRepository.getUserProducts(sellerId).asLiveData()
    }
}