package com.muhammadahmedmufii.comfybuy

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up click listener for the Personal Info option
        findViewById<View>(R.id.layoutPersonalInfo).setOnClickListener {
            val intent = Intent(this, PersonalInfoActivity::class.java)
            startActivity(intent)
        }

        // Set up bottom navigation
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        // Initialize navigation views
        val navHome = findViewById<android.widget.LinearLayout>(R.id.navHome)
        val navSearch = findViewById<android.widget.LinearLayout>(R.id.navSearch)
        val navMessages = findViewById<android.widget.LinearLayout>(R.id.navMessages)
        val navProfile = findViewById<android.widget.LinearLayout>(R.id.navProfile)

        navHome.setOnClickListener {
            try {
                val intent = Intent(this, Class.forName("com.muhammadahmedmufii.comfybuy.HomeActivity"))
                startActivity(intent)
                finish()
            } catch (e: ClassNotFoundException) {
                android.widget.Toast.makeText(this, "Home screen coming soon!", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        navSearch.setOnClickListener {
            try {
                val intent = Intent(this, Class.forName("com.muhammadahmedmufii.comfybuy.SearchActivity"))
                startActivity(intent)
                finish()
            } catch (e: ClassNotFoundException) {
                android.widget.Toast.makeText(this, "Search screen coming soon!", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        navMessages.setOnClickListener {
            try {
                val intent = Intent(this, Class.forName("com.muhammadahmedmufii.comfybuy.Messages"))
                startActivity(intent)
                finish()
            } catch (e: ClassNotFoundException) {
                android.widget.Toast.makeText(this, "Messages screen coming soon!", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        // Profile is already selected
    }
}
