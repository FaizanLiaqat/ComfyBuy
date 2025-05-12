package com.muhammadahmedmufii.comfybuy.ui.home // Example package name for UI components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData // To convert Flow to LiveData
import com.muhammadahmedmufii.comfybuy.data.repository.ProductRepository
import com.muhammadahmedmufii.comfybuy.domain.model.Product
import com.muhammadahmedmufii.comfybuy.data.local.AppDatabase // Import AppDatabase to get DAO instances
import com.google.firebase.firestore.FirebaseFirestore // Import FirebaseFirestore
import com.google.firebase.database.FirebaseDatabase // Import FirebaseDatabase
import android.app.Application // ViewModelProvider.Factory needs Application context
import android.util.Log
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart

// ViewModel for the HomeActivity
class HomeViewModel(application: Application) : ViewModel() {
    private val TAG = "HomeVM"
    private val productRepository: ProductRepository

    init {
        val database = AppDatabase.getDatabase(application)
        val productDao = database.productDao()
        val firestore = FirebaseFirestore.getInstance()
        val realtimeDatabase = FirebaseDatabase.getInstance("https://messamfaizanahmed-default-rtdb.asia-southeast1.firebasedatabase.app")
        productRepository = ProductRepository(productDao, firestore, realtimeDatabase)
        Log.d(TAG, "ViewModel initialized. products LiveData created.")
    }

    // Use the real-time flow
    val products: LiveData<List<Product>> = productRepository.getRealtimeAllProducts()
        .onStart { Log.d(TAG, "products flow collection started.") } // Log when collection starts
        .catch { exception ->
            Log.e(TAG, "Error in getRealtimeAllProducts flow", exception)
            emit(emptyList())
        }
        .asLiveData()

    // You can add functions here for:
    // - Filtering products (interact with repository or filter the LiveData stream)
    // - Searching products (interact with repository or filter the LiveData stream)
    // - Handling product clicks (can be done in the Activity or passed back via callbacks)
    // - Triggering manual sync (call SyncWorkScheduler.triggerOneTimeSync)

    // Example: Function to trigger a manual sync (requires Context)
    // You might pass Context from the Activity or use AndroidViewModel
    // fun triggerSync(context: Context) {
    //     SyncWorkScheduler.triggerOneTimeSync(context)
    // }
}

// Factory for creating HomeViewModel with Application context
class HomeViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
