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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart

// ViewModel for the HomeActivity
/*
class HomeViewModel(application: Application) : ViewModel() {
    private val TAG = "HomeVM"
    private val productRepository: ProductRepository

    init {
        // val database = AppDatabase.getDatabase(application) // REMOVE
        // val productDao = database.productDao() // REMOVE
        // val firestore = FirebaseFirestore.getInstance() // REMOVE
        val realtimeDatabase = FirebaseDatabase.getInstance("https://messamfaizanahmed-default-rtdb.asia-southeast1.firebasedatabase.app")
        val firebaseAuth = FirebaseAuth.getInstance() // ProductRepository now takes this

        // Instantiate with null for Dao and Firestore if constructor allows, or remove them
        productRepository = ProductRepository( realtimeDatabase, firebaseAuth) // Adjust constructor if changed
        Log.d(TAG, "ViewModel initialized with RTDB-only ProductRepository.")
    }

    val products: LiveData<List<Product>> = productRepository.getRealtimeAllProducts()
        .onStart { Log.d(TAG, "HomeVM: products flow collection started.") }
        .catch { exception ->
            Log.e(TAG, "HomeVM: Error in getRealtimeAllProducts flow", exception)
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
*/

class HomeViewModel(
    app: Application,
    private val initialSearchQuery: String?
) : AndroidViewModel(app) {
    private val TAG = "HomeVM"

    private val repo: ProductRepository
    private val _searchQuery      = MutableStateFlow(initialSearchQuery ?: "")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _minPrice         = MutableStateFlow<Double?>(null)
    private val _maxPrice         = MutableStateFlow<Double?>(null)
    private val _sortBy           = MutableStateFlow(SortBy.NEWEST)

    enum class SortBy { NEWEST, PRICE_LOW_HIGH, PRICE_HIGH_LOW }

    init {
        val db = FirebaseDatabase.getInstance(
            "https://messamfaizanahmed-default-rtdb.asia-southeast1.firebasedatabase.app"
        )
        repo = ProductRepository(db, FirebaseAuth.getInstance())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val products: LiveData<List<Product>> = combine(
        repo.getRealtimeAllProducts(),
        _searchQuery,
        _selectedCategory,
        _minPrice,
        _maxPrice,
        _sortBy
    ) { values: Array<Any?> ->
        // Extract flows in the same order
        val all       = values[0] as List<Product>
        val query     = values[1] as String
        val category  = values[2] as String?
        val minP      = values[3] as Double?
        val maxP      = values[4] as Double?
        val sortOpt   = values[5] as SortBy

        Log.d(TAG, "Filter combine: query='$query', cat=$category, min=$minP, max=$maxP, sort=$sortOpt, total=${all.size}")

        // Base search filter
        var list = all.filter { p ->
            query.isBlank() || p.title.contains(query, true)
                    || (p.description?.contains(query, true) ?: false)
                    || (p.category?.contains(query, true) ?: false)
        }

        // Category filter
        category?.let { cat ->
            list = list.filter { it.category?.equals(cat, true) == true }
        }

        // Price filter
        if (minP != null && maxP != null) {
            list = list.filter { p ->
                p.price
                    .replace("$", "")
                    .toDoubleOrNull()
                    ?.let { price -> price in minP..maxP }
                    ?: false
            }
        }

        // Sorting
        list = when (sortOpt) {
            SortBy.NEWEST -> list.sortedByDescending { it.timestamp }
            SortBy.PRICE_LOW_HIGH -> list.sortedBy { it.price.replace("$", "").toDoubleOrNull() ?: Double.MAX_VALUE }
            SortBy.PRICE_HIGH_LOW -> list.sortedByDescending { it.price.replace("$", "").toDoubleOrNull() ?: 0.0 }
        }

        list
    }
        .catch { e ->
            Log.e(TAG, "Error in combine", e)
            emit(emptyList())
        }
        .asLiveData(viewModelScope.coroutineContext + Dispatchers.IO)

    fun setSearchQuery(q: String)    { _searchQuery.value = q.trim() }
    fun setCategoryFilter(c: String?) { _selectedCategory.value = c }
    fun setPriceRangeFilter(min: Double?, max: Double?) {
        _minPrice.value = min
        _maxPrice.value = max
    }
    fun setSortOrder(s: SortBy)      { _sortBy.value = s }
}

