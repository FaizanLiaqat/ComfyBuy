package com.muhammadahmedmufii.comfybuy // Use your main package name

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout // Import LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import com.muhammadahmedmufii.comfybuy.ui.home.HomeFragment // Import HomeFragment
import com.muhammadahmedmufii.comfybuy.ui.messages.MessagesFragment
import com.muhammadahmedmufii.comfybuy.ui.productdetail.ProductDetailFragment
import com.muhammadahmedmufii.comfybuy.ui.profile.ProfileFragment
import com.muhammadahmedmufii.comfybuy.ui.searchresults.SearchResultsFragment

// TODO: Import other Fragment classes as you create them
// import com.muhammadahmedmufii.comfybuy.ui.searchresults.SearchResultsFragment
// import com.muhammadahmedmufii.comfybuy.ui.profile.ProfileFragment
// import com.muhammadahmedmufii.comfybuy.ui.messages.MessagesFragment
// import com.muhammadahmedmufii.comfybuy.ui.postad.PostAdFragment

var openSearch = false
var openProfile = false


class MainActivity : AppCompatActivity() {
    companion object {
        const val ACTION_SHOW_PRODUCT_DETAIL = "com.muhammadahmedmufii.comfybuy.ACTION_SHOW_PRODUCT_DETAIL"
        const val EXTRA_PRODUCT_ID_TO_SHOW = "extra_product_id_to_show"
    }
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Use the new main activity layout

        openSearch = intent.getBooleanExtra("openSearchFragment", false)
        if (openSearch) {
            //Toast.makeText(this, "Opening Search Fragment", Toast.LENGTH_SHORT).show()
            replaceFragment(SearchResultsFragment.newInstance(null))
        }
        openProfile = intent.getBooleanExtra("openProfileFragment", false)
        if (openProfile) {
            //Toast.makeText(this, "Opening Profile Fragment", Toast.LENGTH_SHORT).show()
            replaceFragment(ProfileFragment.newInstance())
        }
        // Set up initial fragment
        if (savedInstanceState == null) {
            // Check intent that started this activity first
            handleIntent(intent) // Handle intent that might have started MainActivity
        }

        setupBottomNavigationListeners()
        // TODO: Handle potential intent data if MainActivity is launched with a specific tab in mind
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_PRODUCT_DETAIL) {
            val productId = intent.getStringExtra(EXTRA_PRODUCT_ID_TO_SHOW)
            if (productId != null) {
                Log.d(TAG, "Handling intent to show product detail for ID: $productId")
                navigateToProductDetail(productId)
            } else {
                Log.w(TAG, "ACTION_SHOW_PRODUCT_DETAIL received but no EXTRA_PRODUCT_ID_TO_SHOW found.")
                // Fallback to home if product ID is missing
                if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
                    replaceFragment(HomeFragment.newInstance(""))
                }
            }
        } else {
            // Default action if no specific intent action (e.g., on first launch)
            if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
                Log.d(TAG, "No specific action in intent, showing HomeFragment.")
                replaceFragment(HomeFragment.newInstance("")) // Assuming HomeFragment has newInstance()
            }
        }
    }


    // New method for navigating to SellerProfile (which is now SellerProfileActivity)


    private fun setupBottomNavigationListeners() {
        // Find the LinearLayouts from the included layout_bottom_navigation.xml
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            replaceFragment(HomeFragment()) // Display HomeFragment
            // TODO: Update UI state of bottom nav icons (e.g., highlight Home)
        }
        if (openSearch) { replaceFragment(SearchResultsFragment.newInstance(null))}
            findViewById<LinearLayout>(R.id.navSearch).setOnClickListener {
            replaceFragment(SearchResultsFragment.newInstance(null)) // Pass null or an initial query
        }
        findViewById<LinearLayout>(R.id.navSell).setOnClickListener {
            val intent = Intent(this, CreateListingActivity::class.java)
            startActivity(intent)
        }
        findViewById<LinearLayout>(R.id.navMessages).setOnClickListener {
           //replaceFragment(MessagesFragment.newInstance()) // Navigate to MessagesFragment
            // No longer starts Messages Activity:
            // showPlaceholderToast("Messages")
            //replaceFragment(ChatListActivity.newInstance(null))
            val intent = Intent(this, ChatListActivity::class.java)
            startActivity(intent)
            finish() // Optional, if you want to close the current activity
        }
        if (openProfile) { replaceFragment(ProfileFragment.newInstance())}
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            // TODO: Replace with ProfileFragment when created
            // replaceFragment(ProfileFragment())
            replaceFragment(ProfileFragment())
            // TODO: Update UI state of bottom nav icons
        }
    }

    // Helper function to replace the current fragment in the container
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            // Optional: Add to back stack if you want to navigate back through tabs
            // .addToBackStack(null)
            .commit()
    }

    private fun showPlaceholderToast(screenName: String) {
        Toast.makeText(this, "$screenName screen coming soon!", Toast.LENGTH_SHORT).show()
    }

    fun navigateToProductDetail(productId: String) {
        val fragment = ProductDetailFragment.newInstance(productId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack("productDetail") // So back button works correctly
            .commit()
    }

    fun navigateToSellerProfile(sellerId: String) {
        Log.d(TAG, "Navigating to SellerProfileActivity for sellerId: $sellerId")
        val intent = Intent(this, SellerProfileActivity::class.java).apply {
            putExtra(SellerProfileActivity.EXTRA_SELLER_ID, sellerId)
        }
        startActivity(intent)
    }

    // This method is specifically to bring the logged-in user's profile tab to the front
    fun navigateToMainProfileTab() {
        Log.d(TAG, "navigateToMainProfileTab called - replacing with ProfileFragment")
        replaceFragment(ProfileFragment.newInstance()) // <<< USE YOUR ProfileFragment
        // TODO: Update bottom navigation visual state to highlight profile
    }

    // Method for ProductDetailFragment to initiate a chat
    fun navigateToChat(opponentUserId: String, opponentName: String, opponentProfilePicResId: Int) {
        Log.d(TAG, "navigateToChat called for opponent: $opponentName (ID: $opponentUserId)")
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("CHAT_USER_ID", opponentUserId) // Important: Pass the other user's ID
            putExtra("CHAT_NAME", opponentName)
            putExtra("CHAT_PROFILE_PIC_RES_ID", opponentProfilePicResId) // Change if using URL/Base64
        }
        startActivity(intent)
        // If ChatActivity also becomes ChatFragment:
        // val chatFragment = ChatFragment.newInstance(opponentUserId, opponentName, opponentProfilePicResId)
        // replaceFragment(chatFragment, "chat")
    }

    fun navigateToSearch(query: String?) {
        Log.d(TAG, "Navigating to SearchResultsFragment with query: $query")
        replaceFragment(SearchResultsFragment.newInstance(query))
    }
}