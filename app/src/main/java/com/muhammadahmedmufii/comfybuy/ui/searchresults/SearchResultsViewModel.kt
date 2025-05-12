package com.muhammadahmedmufii.comfybuy.ui.searchresults // Example package name

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
// Required for launching coroutines in ViewModel
import com.muhammadahmedmufii.comfybuy.data.local.AppDatabase // Import AppDatabase
import com.muhammadahmedmufii.comfybuy.data.repository.ProductRepository // Import ProductRepository
import com.muhammadahmedmufii.comfybuy.domain.model.Product // Import Product domain model
import com.google.firebase.firestore.FirebaseFirestore // Import FirebaseFirestore
import com.google.firebase.database.FirebaseDatabase // Import FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow // Required for MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine // Required for combining Flows
import kotlinx.coroutines.flow.flatMapLatest // Required for flatMapLatest (if used for dynamic queries)
import kotlinx.coroutines.launch // Required for launching coroutines

// ViewModel for the SearchResultsActivity
// It takes Application context and the initial search query as parameters
class SearchResultsViewModel(application: Application, private val initialSearchQuery: String?) : AndroidViewModel(application) {

    private val TAG = "SearchResultsVM"

    private val productRepository: ProductRepository

    private val _searchQuery = MutableStateFlow(initialSearchQuery ?: "")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow() // Expose as StateFlow

    // For filters - these would be updated by UI interactions
    private val _selectedCategory = MutableStateFlow<String?>(null)
    // val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow() // Example

    enum class SortBy { NEWEST, PRICE_LOW_HIGH, PRICE_HIGH_LOW /*, CLOSEST */ }
    private val _sortBy = MutableStateFlow(SortBy.NEWEST)
    val sortBy: StateFlow<SortBy> = _sortBy.asStateFlow()
    // Initialize the ProductRepository with the necessary DAOs and Firebase instances
//    private val productRepository = ProductRepository(productDao, firestore, realtimeDatabase)

    init {
        val realtimeDatabase = FirebaseDatabase.getInstance("https://messamfaizanahmed-default-rtdb.asia-southeast1.firebasedatabase.app")
        val firebaseAuth = FirebaseAuth.getInstance() // ProductRepository needs this
        productRepository = ProductRepository(realtimeDatabase, firebaseAuth) // RTDB-only constructor
        Log.d(TAG, "ViewModel initialized. Initial query: $initialSearchQuery")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val products: LiveData<List<Product>> = combine(
        productRepository.getRealtimeAllProducts(), // Base list from RTDB (already sorted by timestamp ASC)
        _searchQuery,
        _sortBy
        // , _selectedCategory // Add other filters here
    ) { allProducts, query, sortOption /*, category */ ->
        Log.d(TAG, "Combining products. Query: '$query', Sort: $sortOption, AllProducts size: ${allProducts.size}")
        var filteredList = if (query.isBlank()) {
            allProducts
        } else {
            allProducts.filter { product ->
                product.title.contains(query, ignoreCase = true) ||
                        (product.description?.contains(query, ignoreCase = true) ?: false) ||
                        (product.category?.contains(query, ignoreCase = true) ?: false)
            }
        }

        // TODO: Apply category filter if _selectedCategory.value is not null
        // if (category != null) {
        //     filteredList = filteredList.filter { it.category.equals(category, ignoreCase = true) }
        // }

        // Apply sorting
        when (sortOption) {
            SortBy.NEWEST -> filteredList = filteredList.sortedByDescending { it.timestamp } // Newest first
            SortBy.PRICE_LOW_HIGH -> filteredList = filteredList.sortedBy { it.price.replace("$","").toDoubleOrNull() ?: Double.MAX_VALUE }
            SortBy.PRICE_HIGH_LOW -> filteredList = filteredList.sortedByDescending { it.price.replace("$","").toDoubleOrNull() ?: Double.MIN_VALUE }
            // SortBy.CLOSEST -> // Requires location data and calculation
        }
        Log.d(TAG, "Filtered and sorted list size: ${filteredList.size}")
        filteredList
    }.catch { e ->
        Log.e(TAG, "Error in products combine flow", e)
        emit(emptyList()) // Emit empty list on error
    }.asLiveData(viewModelScope.coroutineContext + Dispatchers.IO)

    fun setSearchQuery(query: String) {
        Log.d(TAG, "setSearchQuery called with: $query")
        _searchQuery.value = query.trim()
    }

    fun setSortOrder(sortOption: SortBy) {
        Log.d(TAG, "setSortOrder called with: $sortOption")
        _sortBy.value = sortOption
    }


    // TODO: Add MutableStateFlows for filter and sort options (e.g., category, price range, distance, sort order)
    // private val _selectedCategory = MutableStateFlow<String?>(null)
    // val selectedCategory: Flow<String?> = _selectedCategory

    // Combine the Flow of all products from the repository with the search query Flow
    // and any other filter/sort StateFlows.
    // The 'combine' operator emits a new value whenever any of the source Flows emit a value.
//    val products: LiveData<List<Product>> = combine(
//        productRepository.getAllProducts(), // Flow of all products from the local database
//        searchQuery // Flow of the current search query
//        // TODO: Include other filter/sort StateFlows here (e.g., _selectedCategory)
//    ) { productList, query ->
//        // This block is executed whenever productList or query changes.
//        // It applies filtering and sorting logic to the product list.
//
//        // Apply filtering logic here based on the current search query and other filter states.
//        // This example performs a basic case-insensitive check on product title and description.
//        productList.filter { product ->
//            // Check if the product title contains the query (case-insensitive)
//            product.title.contains(query, ignoreCase = true) ||
//                    // Check if the product description (if not null) contains the query (case-insensitive)
//                    (product.description?.contains(query, ignoreCase = true) ?: false)
//            // TODO: Add more sophisticated filtering based on category, price, location, etc.
//            // Example: && (_selectedCategory.value == null || product.category == _selectedCategory.value)
//        }
//        // TODO: Apply sorting logic here based on the selected sort order.
//        // Example: .sortedBy { it.price } // Sort by price
//        // Example: .sortedByDescending { it.timestamp } // Sort by newest (if timestamp is in Product model)
//    }.asLiveData() // Convert the resulting combined Flow to LiveData for UI observation.


    // TODO: Add functions to set filter and sort options from the UI.
    // These functions would update the corresponding MutableStateFlows, triggering the 'combine' Flow.
    // fun setCategoryFilter(category: String?) { _selectedCategory.value = category }
    // fun setSortOrder(sortOrder: SortOrder) { ... } // Define a SortOrder enum (e.g., NEWEST, PRICE_LOW_HIGH, CLOSEST)

    // You can also add functions here to trigger specific actions, like initiating a search from the repository
    // if you were performing remote searches directly (less common in an offline-first approach).
    // fun performSearch(query: String) {
    //     viewModelScope.launch {
    //         // You might call a specific search function in the repository if needed
    //         // productRepository.searchProducts(query)
    //     }
    // }
}
