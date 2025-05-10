package com.muhammadahmedmufii.comfybuy

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Back button
        findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.btnBack).setOnClickListener {
            onBackPressed()
        }

        // Account section
        findViewById<ConstraintLayout>(R.id.layoutChangeEmail).setOnClickListener {
            Toast.makeText(this, "Personal Information clicked", Toast.LENGTH_SHORT).show()
            // Navigate to personal information screen
        }

        findViewById<ConstraintLayout>(R.id.layoutChangePassword).setOnClickListener {
            Toast.makeText(this, "Account Settings clicked", Toast.LENGTH_SHORT).show()
            // Navigate to account settings screen
        }

        findViewById<ConstraintLayout>(R.id.layoutManageNotifications).setOnClickListener {
            Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show()
            // Navigate to notifications screen
        }

        // Preferences section
        findViewById<ConstraintLayout>(R.id.layoutLocation).setOnClickListener {
            Toast.makeText(this, "Location clicked", Toast.LENGTH_SHORT).show()
            // Navigate to location settings screen
        }

        findViewById<ConstraintLayout>(R.id.layoutHelpCenter).setOnClickListener {
            Toast.makeText(this, "Help Center clicked", Toast.LENGTH_SHORT).show()
            // Navigate to help center screen
        }

        // Log Out
        findViewById<ConstraintLayout>(R.id.cardDeleteAccount).setOnClickListener {
            showLogOutConfirmation()
        }
    }

    private fun showLogOutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out") { dialog, _ ->
                // Implement logout functionality
                Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()
                // Navigate to login screen or clear user session
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }
}