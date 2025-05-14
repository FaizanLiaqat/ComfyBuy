package com.muhammadahmedmufii.comfybuy

/*
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView // Import ImageView
import android.widget.TextView // Import TextView for category buttons
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider // Import ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.muhammadahmedmufii.comfybuy.ui.home.HomeViewModel // Import your ViewModel
import com.muhammadahmedmufii.comfybuy.ui.home.HomeViewModelFactory // Import your ViewModel Factory
import com.muhammadahmedmufii.comfybuy.ui.productdetail.ProductDetailFragment

class home : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var productAdapter: ProductAdapter // Declare the adapter
    private lateinit var homeViewModel: HomeViewModel // Declare the ViewModel

    // References to bottom navigation icons
    private lateinit var homeIcon: ImageView
    private lateinit var searchIconBottom: ImageView
    private lateinit var sellIcon: ImageView
    private lateinit var messagesIcon: ImageView
    private lateinit var profileIcon: ImageView

    // References to category TextViews (basic click listeners for now)
    private lateinit var allCategoryButton: TextView
    private lateinit var furnitureCategoryButton: TextView
    private lateinit var homeCategoryButton: TextView
    private lateinit var fashionCategoryButton: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize ViewModel
        val factory = HomeViewModelFactory(application)
        homeViewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        // Schedule the periodic sync work (can also be done in Application class)
        SyncWorkScheduler.schedulePeriodicSync(this)

        initViews() // Initialize UI elements
        setupRecyclerView() // Setup RecyclerView and Adapter
        observeViewModel() // Observe data from ViewModel
        setupBottomNavigation() // Setup Bottom Navigation click listeners
        setupCategoryListeners() // Setup Category button click listeners
        setupSearchBarListeners() // Setup Search bar/Filter icon listeners
    }

    private fun initViews() {
        recycler = findViewById(R.id.productRecycler)



        // Initialize category buttons
        allCategoryButton = findViewById(R.id.allCategoryButton)
        furnitureCategoryButton = findViewById(R.id.ElectronicsCategoryButton)
        homeCategoryButton = findViewById(R.id.homeCategoryButton)
        fashionCategoryButton = findViewById(R.id.ClothingCategoryButton)

        // Initialize search bar elements (assuming IDs from activity_home.xml)
        // val searchEdit = findViewById<EditText>(R.id.searchEdit) // Assuming EditText
        // val filterIcon = findViewById<ImageView>(R.id.filterIcon) // Assuming ImageView
    }

    private fun setupRecyclerView() {
        // 1) setup RecyclerView as 2-column grid
        recycler.layoutManager = GridLayoutManager(this, 2)

        // Initialize the adapter with an empty list initially
        productAdapter = ProductAdapter { product ->
            // TODO: Implement actual product click handling
            // Navigate to product details screen, passing the product ID
            val intent = Intent(this, ProductDetailFragment::class.java) // Create ProductDetailActivity
            intent.putExtra("PRODUCT_ID", product.productId) // Pass product ID
            startActivity(intent)
        }
        recycler.adapter = productAdapter // Set the adapter to the RecyclerView
    }

    private fun observeViewModel() {
        // Observe the products LiveData from the ViewModel
        homeViewModel.products.observe(this) { productList ->
            // Update the adapter with the new list of products whenever it changes
            productAdapter.submitList(productList) // Use submitList with ListAdapter (recommended)
            // Or if using a regular RecyclerView.Adapter:
            // productAdapter.updateData(productList) // You'll need to implement this method
        }
    }

    private fun setupBottomNavigation() {
        // Set click listeners for bottom navigation icons
        homeIcon.setOnClickListener { /* Already on Home, maybe refresh or scroll to top */ }
        searchIconBottom.setOnClickListener {
            // Navigate to Search Activity
            // val intent = Intent(this, SearchActivity::class.java) // Create SearchActivity
            // startActivity(intent)
        }
        sellIcon.setOnClickListener {
            // Navigate to Post Ad Activity
//            val intent = Intent(this, PostAdActivity::class.java) // Create PostAdActivity
//            startActivity(intent)
        }
        messagesIcon.setOnClickListener {
            // Navigate to Messages Activity
            // val intent = Intent(this, MessagesActivity::class.java) // Create MessagesActivity
            // startActivity(intent)
        }
        profileIcon.setOnClickListener {
            // Navigate to Profile Activity (EditProfileActivity or a view profile)
            val intent = Intent(this, EditProfileActivity::class.java) // Or ViewProfileActivity
            startActivity(intent)
        }

        // TODO: Update the appearance of the selected bottom navigation icon
        // You might want to change the icon source or color to indicate the active screen.
    }

    private fun setupCategoryListeners() {
        // Basic click listeners for category buttons (filtering logic comes later)
        allCategoryButton.setOnClickListener {
            Toast.makeText(this, "All category selected", Toast.LENGTH_SHORT).show()
            // TODO: Implement filtering logic to show all products
        }
        furnitureCategoryButton.setOnClickListener {
            Toast.makeText(this, "Furniture category selected", Toast.LENGTH_SHORT).show()
            // TODO: Implement filtering logic for Furniture
        }
        homeCategoryButton.setOnClickListener {
            Toast.makeText(this, "Home category selected", Toast.LENGTH_SHORT).show()
            // TODO: Implement filtering logic for Home
        }
        fashionCategoryButton.setOnClickListener {
            Toast.makeText(this, "Fashion category selected", Toast.LENGTH_SHORT).show()
            // TODO: Implement filtering logic for Fashion
        }
    }

    private fun setupSearchBarListeners() {
        // Assuming you have EditText with ID searchEdit and ImageView with ID filterIcon
        val searchEdit = findViewById<EditText>(R.id.searchEdit)
        val filterIcon = findViewById<ImageView>(R.id.filterIcon)

        searchEdit.setOnClickListener {
            Toast.makeText(this, "Search bar clicked", Toast.LENGTH_SHORT).show()
            // TODO: Implement search functionality (e.g., navigate to a search screen or show search results)
        }

        filterIcon.setOnClickListener {
            Toast.makeText(this, "Filter icon clicked", Toast.LENGTH_SHORT).show()
            // TODO: Implement filter functionality (e.g., show a filter dialog or navigate to a filter screen)
        }
    }
}
*/