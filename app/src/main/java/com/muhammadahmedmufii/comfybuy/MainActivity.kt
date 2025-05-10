package com.muhammadahmedmufii.comfybuy // Use your main package name

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout // Import LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.muhammadahmedmufii.comfybuy.ui.home.HomeFragment // Import HomeFragment
import com.muhammadahmedmufii.comfybuy.ui.productdetail.ProductDetailFragment
import com.muhammadahmedmufii.comfybuy.ui.profile.ProfileFragment

// TODO: Import other Fragment classes as you create them
// import com.muhammadahmedmufii.comfybuy.ui.searchresults.SearchResultsFragment
// import com.muhammadahmedmufii.comfybuy.ui.profile.ProfileFragment
// import com.muhammadahmedmufii.comfybuy.ui.messages.MessagesFragment
// import com.muhammadahmedmufii.comfybuy.ui.postad.PostAdFragment


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Use the new main activity layout

        // Set up initial fragment
        if (savedInstanceState == null) {
            // Display the Home Fragment initially when the activity is first created
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()) // Use the container ID from activity_main.xml
                .commit()
        }

        setupBottomNavigationListeners()
        // TODO: Handle potential intent data if MainActivity is launched with a specific tab in mind
    }

    private fun setupBottomNavigationListeners() {
        // Find the LinearLayouts from the included layout_bottom_navigation.xml
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            replaceFragment(HomeFragment()) // Display HomeFragment
            // TODO: Update UI state of bottom nav icons (e.g., highlight Home)
        }
        findViewById<LinearLayout>(R.id.navSearch).setOnClickListener {
            // TODO: Replace with SearchResultsFragment when created
            // replaceFragment(SearchResultsFragment())
            showPlaceholderToast("Search")
            // TODO: Update UI state of bottom nav icons
        }
        findViewById<LinearLayout>(R.id.navSell).setOnClickListener {
            val intent = Intent(this, CreateListingActivity::class.java)
            startActivity(intent)
        }
        findViewById<LinearLayout>(R.id.navMessages).setOnClickListener {
            // TODO: Replace with MessagesFragment when created
            // replaceFragment(MessagesFragment())
            showPlaceholderToast("Messages")
            // TODO: Update UI state of bottom nav icons
        }
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
    // TODO: You might need methods here to navigate to specific fragments from other parts of the app
    // Example: fun navigateToSearch(query: String) { replaceFragment(SearchResultsFragment.newInstance(query)) }
}