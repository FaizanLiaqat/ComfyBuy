package com.muhammadahmedmufii.comfybuy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider // Import ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.muhammadahmedmufii.comfybuy.ui.searchresults.SearchResultsViewModel // Import your ViewModel
import com.muhammadahmedmufii.comfybuy.ui.searchresults.SearchResultsViewModelFactory // Import your ViewModel Factory
import com.muhammadahmedmufii.comfybuy.ui.productdetail.ProductDetailFragment


class SearchResultsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter // Use the updated ProductAdapter
    private lateinit var searchResultsViewModel: SearchResultsViewModel // Declare the ViewModel

    // UI elements
    private lateinit var sortRadioGroup: RadioGroup
    // Add references to your bottom navigation LinearLayouts if needed for click listeners
    private lateinit var navHome: View // Using View as a generic type for LinearLayout
    private lateinit var navSearch: View
    private lateinit var navSell: View
    private lateinit var navMessages: View
    private lateinit var navProfile: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results) // Assuming you have this layout

        // TODO: Get search query or filters from the Intent if navigating from Search
        val searchQuery = intent.getStringExtra("SEARCH_QUERY") // Example

        // Initialize ViewModel with any necessary parameters (like search query)
        val factory = SearchResultsViewModelFactory(application, searchQuery) // Pass search query
        searchResultsViewModel = ViewModelProvider(this, factory).get(SearchResultsViewModel::class.java)


        initViews() // Initialize UI elements
        setupRecyclerView() // Setup RecyclerView and Adapter
        observeViewModel() // Observe data from ViewModel
        setupBottomNavigation() // Setup Bottom Navigation click listeners
        setupSortAndFilterListeners() // Setup Sort and Filter listeners
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewProducts) // Assuming this ID in activity_search_results.xml
        sortRadioGroup = findViewById(R.id.sortRadioGroup) // Assuming this ID

        // Initialize bottom navigation views (assuming IDs from layout_bottom_navigation.xml are included)
        navHome = findViewById(R.id.navHome)
        navSearch = findViewById(R.id.navSearch)
        navSell = findViewById(R.id.navSell)
        navMessages = findViewById(R.id.navMessages)
        navProfile = findViewById(R.id.navProfile)

        // Initialize filter chip views (assuming IDs)
        // findViewById<View>(R.id.chipCategory)
        // findViewById<View>(R.id.chipPrice)
        // findViewById<View>(R.id.chipLocation)
    }

    private fun setupRecyclerView() {
        // Set up RecyclerView as 2-column grid
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Initialize the adapter with the item click lambda
        productAdapter = ProductAdapter { product ->
            // Handle item click - Navigate to Product Detail Activity
            val intent = Intent(this, ProductDetailFragment::class.java) // Assuming ProductDetailActivity exists
            intent.putExtra("PRODUCT_ID", product.productId) // Pass the product ID
            startActivity(intent)
        }
        recyclerView.adapter = productAdapter // Set the adapter
    }

    private fun observeViewModel() {
        // Observe the products LiveData from the ViewModel
        searchResultsViewModel.products.observe(this) { productList ->
            // Update the adapter with the new list of products whenever it changes
            productAdapter.submitList(productList) // Use submitList with ListAdapter
        }
        // TODO: Observe other LiveData from ViewModel if needed (e.g., loading state, error messages)
    }

    private fun setupBottomNavigation() {
        // Set click listeners for bottom navigation icons
        navHome.setOnClickListener {
            val intent = Intent(this, home::class.java) // Assuming 'home' is your HomeActivity class
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish() // Close SearchResultsActivity
        }
        navSearch.setOnClickListener {
            // Already on Search Results, maybe scroll to top or refresh search
            Toast.makeText(this, "Already on Search Results", Toast.LENGTH_SHORT).show()
        }
        navSell.setOnClickListener {
//            val intent = Intent(this, PostAdActivity::class.java) // Assuming PostAdActivity exists
            startActivity(intent)
            finish() // Optional: finish current activity
        }
        navMessages.setOnClickListener {
            // val intent = Intent(this, MessagesActivity::class.java) // Assuming MessagesActivity exists
            // startActivity(intent)
            // finish() // Optional: finish current activity
            Toast.makeText(this, "Messages screen coming soon!", Toast.LENGTH_SHORT).show()
        }
        navProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java) // Assuming EditProfileActivity exists
            startActivity(intent)
            finish() // Optional: finish current activity
        }

        // TODO: Update the appearance of the selected bottom navigation icon (e.g., highlight the Search icon)
    }

    private fun setupSortAndFilterListeners() {
        // Set up sort radio group
        sortRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbNewest -> {
                    // TODO: Call ViewModel function to sort products by newest
                    Toast.makeText(this, "Sorting by newest", Toast.LENGTH_SHORT).show()
                }
                R.id.rbPriceLowHigh -> {
                    // TODO: Call ViewModel function to sort products by price low to high
                    Toast.makeText(this, "Sorting by price: low to high", Toast.LENGTH_SHORT).show()
                }
                R.id.rbClosest -> {
                    // TODO: Call ViewModel function to sort products by closest
                    Toast.makeText(this, "Sorting by closest", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set up filter chips
        // Assuming you have these IDs in your activity_search_results.xml
        findViewById<View>(R.id.chipCategory).setOnClickListener {
            // TODO: Implement category filter logic (e.g., show a dialog, update ViewModel)
            Toast.makeText(this, "Category filter", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.chipPrice).setOnClickListener {
            // TODO: Implement price filter logic
            Toast.makeText(this, "Price filter", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.chipLocation).setOnClickListener {
            // TODO: Implement location filter logic
            Toast.makeText(this, "Location filter", Toast.LENGTH_SHORT).show()
        }
    }

    // Remove the createSampleProducts() function as data will come from the Repository/ViewModel
    // private fun createSampleProducts(): List<Product> { ... }
}
