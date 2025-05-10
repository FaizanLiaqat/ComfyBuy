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
import androidx.lifecycle.LiveData

// ViewModel for the HomeActivity
class HomeViewModel(application: Application) : ViewModel() {

    // Manually get repository instance (ideally use Dependency Injection)
    private val database = AppDatabase.getDatabase(application)
    private val productDao = database.productDao()
    private val firestore = FirebaseFirestore.getInstance()
    private val realtimeDatabase = FirebaseDatabase.getInstance("https://messamfaizanahmed-default-rtdb.asia-southeast1.firebasedatabase.app")

    private val productRepository = ProductRepository(productDao, firestore, realtimeDatabase)

    // Expose the list of products from the repository as LiveData
    // The UI (Activity/Fragment) will observe this LiveData
    val products: LiveData<List<Product>> = productRepository.getAllProducts().asLiveData()

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
