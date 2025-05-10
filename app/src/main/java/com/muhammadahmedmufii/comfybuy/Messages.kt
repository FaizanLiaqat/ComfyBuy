package com.muhammadahmedmufii.comfybuy

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Messages : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter

    // Bottom navigation views
    private lateinit var navHome: LinearLayout
    private lateinit var navSearch: LinearLayout
    private lateinit var navMessages: LinearLayout
    private lateinit var navProfile: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_messages)

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up RecyclerView
        setupRecyclerView()

        // Set up bottom navigation
        setupBottomNavigation()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewChats)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Create sample data
        val messageList = getSampleMessages()

        // Set up adapter
        messageAdapter = MessageAdapter(messageList)
        recyclerView.adapter = messageAdapter
    }

    private fun setupBottomNavigation() {
        // Initialize navigation views
        navHome = findViewById(R.id.navHome)
        navSearch = findViewById(R.id.navSearch)
        navMessages = findViewById(R.id.navMessages)
        navProfile = findViewById(R.id.navProfile)

        // Set up click listeners for bottom navigation items
        navHome.setOnClickListener {
            // Navigate to Home activity
            try {
                val intent = Intent(this, Class.forName("com.muhammadahmedmufii.comfybuy.HomeActivity"))
                startActivity(intent)
                updateNavSelection(navHome)
                finish() // Close this activity
            } catch (e: ClassNotFoundException) {
                // Handle case where HomeActivity doesn't exist yet
                showToast("Home screen coming soon!")
            }
        }

        navSearch.setOnClickListener {
            // Navigate to Search activity
            try {
                val intent = Intent(this, Class.forName("com.muhammadahmedmufii.comfybuy.SearchActivity"))
                startActivity(intent)
                updateNavSelection(navSearch)
                finish() // Close this activity
            } catch (e: ClassNotFoundException) {
                // Handle case where SearchActivity doesn't exist yet
                showToast("Search screen coming soon!")
            }
        }

        // Messages is already selected and we're already on this screen

        navProfile.setOnClickListener {
            // Navigate to Profile activity
            try {
                val intent = Intent(this, Class.forName("com.muhammadahmedmufii.comfybuy.ProfileActivity"))
                startActivity(intent)
                updateNavSelection(navProfile)
                finish() // Close this activity
            } catch (e: ClassNotFoundException) {
                // Handle case where ProfileActivity doesn't exist yet
                showToast("Profile screen coming soon!")
            }
        }
    }

    private fun updateNavSelection(selectedNav: LinearLayout) {
        // Update colors for all navigation items
        val defaultColor = ContextCompat.getColor(this, android.R.color.darker_gray)
        val selectedColor = ContextCompat.getColor(this, android.R.color.black)

        // Reset all to default
        updateNavItemColor(navHome, defaultColor)
        updateNavItemColor(navSearch, defaultColor)
        updateNavItemColor(navMessages, defaultColor)
        updateNavItemColor(navProfile, defaultColor)

        // Set selected to active color
        updateNavItemColor(selectedNav, selectedColor)
    }

    private fun updateNavItemColor(navItem: LinearLayout, color: Int) {
        val icon = navItem.getChildAt(0) as ImageView
        val text = navItem.getChildAt(1) as TextView

        icon.setColorFilter(color)
        text.setTextColor(color)
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun getSampleMessages(): List<MessageItem> {
        // Sample data for testing
        return listOf(
            MessageItem(
                "Emily Parker",
                "The vintage lamp is still available?",
                "2m ago",
                R.drawable.avatar_placeholder
            ),
            MessageItem(
                "Michael Chen",
                "Great! I'll pick it up tomorrow",
                "1h ago",
                R.drawable.avatar_placeholder
            ),
            MessageItem(
                "Sophie Williams",
                "Can you do $40 for the chair?",
                "2h ago",
                R.drawable.avatar_placeholder
            ),
            MessageItem(
                "James Wilson",
                "I'm interested in the coffee table",
                "3h ago",
                R.drawable.avatar_placeholder
            ),
            MessageItem(
                "Olivia Martinez",
                "Is the bookshelf still for sale?",
                "4h ago",
                R.drawable.avatar_placeholder
            ),
            MessageItem(
                "Noah Johnson",
                "When can I come see the desk?",
                "5h ago",
                R.drawable.avatar_placeholder
            ),
            MessageItem(
                "Emma Brown",
                "Do you deliver to Brooklyn?",
                "6h ago",
                R.drawable.avatar_placeholder
            ),
            MessageItem(
                "Liam Davis",
                "I'll take the dining set",
                "7h ago",
                R.drawable.avatar_placeholder
            )
        )
    }
}
