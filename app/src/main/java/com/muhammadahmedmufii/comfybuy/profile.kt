package com.muhammadahmedmufii.comfybuy // Use your main package name

import android.content.Intent
import android.os.Bundle
import android.view.View // Import View
import android.widget.TextView // Import TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider // Import ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.muhammadahmedmufii.comfybuy.ui.profile.ProfileViewModel // Import ViewModel
import com.muhammadahmedmufii.comfybuy.ui.profile.ProfileViewModelFactory // Import ViewModel Factory
import com.bumptech.glide.Glide // Import Glide for loading profile image
import com.muhammadahmedmufii.comfybuy.ui.productdetail.ProductDetailFragment
import de.hdodenhof.circleimageview.CircleImageView // Import CircleImageView for profile image


class profile : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var productAdapter: ProductAdapter // Use the updated ProductAdapter
    private lateinit var profileViewModel: ProfileViewModel // Declare the ViewModel

    // UI elements from profile.xml
    private lateinit var avatarImageView: CircleImageView
    private lateinit var cameraIconImageView: CircleImageView // Assuming this is for changing avatar
    private lateinit var nameTextView: TextView
    private lateinit var subtitleTextView: TextView
    private lateinit var btnEditProfile: TextView // Assuming this is a TextView styled as a button
    private lateinit var countListingsTextView: TextView
    private lateinit var countFollowersTextView: TextView
    private lateinit var countFollowingTextView: TextView
    private lateinit var tabListingsTextView: TextView
    private lateinit var tabFavoritesTextView: TextView
    private lateinit var tabReviewsTextView: TextView

    // References to bottom navigation LinearLayouts (assuming these are included in profile.xml)
    private lateinit var navHome: View
    private lateinit var navSearch: View
    private lateinit var navSell: View
    private lateinit var navMessages: View
    private lateinit var navProfile: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile) // Use your profile.xml layout

        // Initialize ViewModel
        val factory = ProfileViewModelFactory(application)
        profileViewModel = ViewModelProvider(this, factory).get(ProfileViewModel::class.java)

        initViews() // Initialize UI elements
        setupRecyclerView() // Setup RecyclerView and Adapter
        observeViewModel() // Observe data from ViewModel
        setupClickListeners() // Setup click listeners for profile elements
        setupBottomNavigation() // Setup Bottom Navigation click listeners
        setupTabListeners() // Setup Tab click listeners
    }

    private fun initViews() {
        // Initialize UI elements from profile.xml
        avatarImageView = findViewById(R.id.avatar)
        cameraIconImageView = findViewById(R.id.cameraIcon)
        nameTextView = findViewById(R.id.name)
        subtitleTextView = findViewById(R.id.subtitle)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        countListingsTextView = findViewById(R.id.countListings)
        countFollowersTextView = findViewById(R.id.countFollowers)
        countFollowingTextView = findViewById(R.id.countFollowing)
        tabListingsTextView = findViewById(R.id.tabListings)
        tabFavoritesTextView = findViewById(R.id.tabFavorites)
        tabReviewsTextView = findViewById(R.id.tabReviews)
        recycler = findViewById(R.id.recyclerProducts) // Assuming this ID in profile.xml

        // Initialize bottom navigation views (assuming IDs from layout_bottom_navigation.xml are included)
        navHome = findViewById(R.id.navHome)
        navSearch = findViewById(R.id.navSearch)
        navSell = findViewById(R.id.navSell)
        navMessages = findViewById(R.id.navMessages)
        navProfile = findViewById(R.id.navProfile)
    }

    private fun setupRecyclerView() {
        // 1) setup RecyclerView as 2-column grid
        recycler.layoutManager = GridLayoutManager(this, 2)

        // Initialize the adapter with the item click lambda
        productAdapter = ProductAdapter { product ->
            // Handle item click - Navigate to Product Detail Activity
            val intent = Intent(this, ProductDetailFragment::class.java) // Assuming ProductDetailActivity exists
            intent.putExtra("PRODUCT_ID", product.productId) // Pass the product ID
            startActivity(intent)
        }
        recycler.adapter = productAdapter // Set the adapter

        // Disable nested scrolling for the RecyclerView since it's inside a NestedScrollView
        recycler.isNestedScrollingEnabled = false
    }

    private fun observeViewModel() {
        // Observe the current user LiveData from the ViewModel
        profileViewModel.currentUser.observe(this) { user ->
            user?.let {
                // Update UI with user details
                nameTextView.text = it.fullName
                // TODO: Set subtitleTextView if you add a subtitle field to User model

                // Load profile image with Glide
                it.profileImageBitmap?.let { imageUrl ->
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.avatar_placeholder) // Replace with your placeholder
                        .error(R.drawable.avatar_placeholder) // Replace with your error drawable
                        .into(avatarImageView)
                } ?: run {
                    // Set a default profile image if URL is null
                    avatarImageView.setImageResource(R.drawable.avatar_placeholder) // Replace with your default drawable
                }

                // TODO: Update Followers and Following counts if you implement this logic
                countFollowersTextView.text = "N/A" // Placeholder
                countFollowingTextView.text = "N/A" // Placeholder

            } ?: run {
                // Handle case where user is not logged in or data not found
                nameTextView.text = "Guest User"
                subtitleTextView.text = ""
                avatarImageView.setImageResource(R.drawable.avatar_placeholder)
                countListingsTextView.text = "0"
                countFollowersTextView.text = "0"
                countFollowingTextView.text = "0"
                // Maybe redirect to login screen if user must be logged in to view profile
            }
        }

        // Observe the user's products LiveData from the ViewModel
        profileViewModel.userProducts.observe(this) { productList ->
            // Update the adapter with the new list of products whenever it changes
            productAdapter.submitList(productList) // Use submitList with ListAdapter

            // Update the listings count
            countListingsTextView.text = productList.size.toString()
        }

        // TODO: Observe LiveData for Favorites and Reviews if you implement those tabs
    }

    private fun setupClickListeners() {
        // Click listener for the camera icon (for changing profile picture)
        cameraIconImageView.setOnClickListener {
            // TODO: Implement logic to select a new profile picture (e.g., from gallery/camera)
            Toast.makeText(this, "Change profile picture clicked", Toast.LENGTH_SHORT).show()
        }

        // Click listener for the Edit Profile button
        btnEditProfile.setOnClickListener {
            // TODO: Navigate to an Edit Profile Activity
            val intent = Intent(this, EditProfileActivity::class.java) // Assuming EditProfileActivity exists
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation() {
        // Set click listeners for bottom navigation icons
        navHome.setOnClickListener {
            val intent = Intent(this, home::class.java) // Assuming 'home' is your HomeActivity class
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish() // Close ProfileActivity
        }
        navSearch.setOnClickListener {
            // val intent = Intent(this, SearchActivity::class.java) // Assuming SearchActivity exists
            // startActivity(intent)
            // finish() // Optional: finish current activity
            Toast.makeText(this, "Search screen coming soon!", Toast.LENGTH_SHORT).show()
        }
        navSell.setOnClickListener {
//            val intent = Intent(this, PostAdActivity::class.java) // Assuming PostAdActivity exists
//            startActivity(intent)
//            finish() // Optional: finish current activity
        }
        navMessages.setOnClickListener {
            // val intent = Intent(this, MessagesActivity::class.java) // Assuming MessagesActivity exists
            // startActivity(intent)
            // finish() // Optional: finish current activity
            Toast.makeText(this, "Messages screen coming soon!", Toast.LENGTH_SHORT).show()
        }
        navProfile.setOnClickListener {
            // Already on Profile
            Toast.makeText(this, "Already on Profile", Toast.LENGTH_SHORT).show()
        }

        // TODO: Update the appearance of the selected bottom navigation icon (e.g., highlight the Profile icon)
    }

    private fun setupTabListeners() {
        // Basic click listeners for tabs (filtering/display logic comes later)
        tabListingsTextView.setOnClickListener {
            // TODO: Implement logic to show user's listings (this is the default)
            Toast.makeText(this, "Listings tab selected", Toast.LENGTH_SHORT).show()
            // Update tab appearance (e.g., highlight Listings tab)
        }
        tabFavoritesTextView.setOnClickListener {
            // TODO: Implement logic to show user's favorite products (requires fetching favorite data)
            Toast.makeText(this, "Favorites tab selected", Toast.LENGTH_SHORT).show()
            // Update tab appearance
        }
        tabReviewsTextView.setOnClickListener {
            // TODO: Implement logic to show user's reviews (requires fetching review data)
            Toast.makeText(this, "Reviews tab selected", Toast.LENGTH_SHORT).show()
            // Update tab appearance
        }
    }
}
