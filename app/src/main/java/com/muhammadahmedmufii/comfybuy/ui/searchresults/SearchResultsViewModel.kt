package com.muhammadahmedmufii.comfybuy.ui.searchresults // Example package name

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
 // Required for launching coroutines in ViewModel
import com.muhammadahmedmufii.comfybuy.data.local.AppDatabase // Import AppDatabase
import com.muhammadahmedmufii.comfybuy.data.repository.ProductRepository // Import ProductRepository
import com.muhammadahmedmufii.comfybuy.domain.model.Product // Import Product domain model
import com.google.firebase.firestore.FirebaseFirestore // Import FirebaseFirestore
import com.google.firebase.database.FirebaseDatabase // Import FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow // Required for MutableStateFlow
import kotlinx.coroutines.flow.combine // Required for combining Flows
import kotlinx.coroutines.flow.flatMapLatest // Required for flatMapLatest (if used for dynamic queries)
import kotlinx.coroutines.launch // Required for launching coroutines

// ViewModel for the SearchResultsActivity
// It takes Application context and the initial search query as parameters
class SearchResultsViewModel(application: Application, private val initialSearchQuery: String?) : AndroidViewModel(application) {

    // Manually get repository instance (ideally use Dependency Injection framework like Hilt or Koin)
    // This requires accessing the database and Firebase instances.
    private val database = AppDatabase.getDatabase(application)
    private val productDao = database.productDao() // Get ProductDao from the database
    private val firestore = FirebaseFirestore.getInstance() // Get FirebaseFirestore instance
    private val realtimeDatabase = FirebaseDatabase.getInstance("https://messamfaizanahmed-default-rtdb.asia-southeast1.firebasedatabase.app") // Get FirebaseDatabase instance

    // Initialize the ProductRepository with the necessary DAOs and Firebase instances
//    private val productRepository = ProductRepository(productDao, firestore, realtimeDatabase)

    // MutableStateFlow to hold the current search query.
    // MutableStateFlow is a Flow that holds a single value and emits updates to collectors.
    private val _searchQuery = MutableStateFlow(initialSearchQuery ?: "")
    // Expose the search query as an immutable Flow
    val searchQuery: Flow<String> = _searchQuery

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

    // Function to update the search query from the UI (e.g., when the user types in the search bar).
    fun setSearchQuery(query: String) {
        _searchQuery.value = query // Update the value of the MutableStateFlow, which triggers the 'combine' Flow.
    }

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
