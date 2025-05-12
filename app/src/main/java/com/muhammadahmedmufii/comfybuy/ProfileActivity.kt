//package com.muhammadahmedmufii.comfybuy
//
//import android.content.Intent
//import android.os.Bundle
//import android.view.View
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//
//class ProfileActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_profile)
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        // Set up click listener for the Personal Info option
//        findViewById<View>(R.id.layoutPersonalInfo).setOnClickListener {
//            val intent = Intent(this, PersonalInfoActivity::class.java)
//            startActivity(intent)
//        }
//
//        // Set up bottom navigation
//        setupBottomNavigation()
//    }
//
//    private fun setupBottomNavigation() {
//        // Initialize navigation views
//        val navHome = findViewById<android.widget.LinearLayout>(R.id.navHome)
//        val navSearch = findViewById<android.widget.LinearLayout>(R.id.navSearch)
//        val navMessages = findViewById<android.widget.LinearLayout>(R.id.navMessages)
//        val navProfile = findViewById<android.widget.LinearLayout>(R.id.navProfile)
//
//        navHome.setOnClickListener {
//            try {
//                val intent = Intent(this, Class.forName("com.muhammadahmedmufii.comfybuy.HomeActivity"))
//                startActivity(intent)
//                finish()
//            } catch (e: ClassNotFoundException) {
//                android.widget.Toast.makeText(this, "Home screen coming soon!", android.widget.Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        navSearch.setOnClickListener {
//            try {
//                val intent = Intent(this, Class.forName("com.muhammadahmedmufii.comfybuy.SearchActivity"))
//                startActivity(intent)
//                finish()
//            } catch (e: ClassNotFoundException) {
//                android.widget.Toast.makeText(this, "Search screen coming soon!", android.widget.Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        navMessages.setOnClickListener {
//            try {
//                val intent = Intent(this, Class.forName("com.muhammadahmedmufii.comfybuy.Messages"))
//                startActivity(intent)
//                finish()
//            } catch (e: ClassNotFoundException) {
//                android.widget.Toast.makeText(this, "Messages screen coming soon!", android.widget.Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        // Profile is already selected
//    }
//}
package com.muhammadahmedmufii.comfybuy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<View>(R.id.layoutPersonalInfo).setOnClickListener {
            val intent = Intent(this, PersonalInfoActivity::class.java)
            startActivity(intent)
        }

        val btnLogout: Button = findViewById(R.id.btnLogout)
        Log.d("ProfileActivity", "Logout button enabled: ${btnLogout.isEnabled}")
        btnLogout.setOnClickListener {
            Log.d("ProfileActivity", "Logout button clicked")
            logoutUser()
        }

        setupBottomNavigation()

        // Debug auth state
        auth.addAuthStateListener { firebaseAuth ->
            Log.d("ProfileActivity", "Auth state changed: ${firebaseAuth.currentUser?.uid}")
        }
    }

    private fun setupBottomNavigation() {
        val navHome = findViewById<android.widget.LinearLayout>(R.id.navHome)
        val navSearch = findViewById<android.widget.LinearLayout>(R.id.navSearch)
        val navMessages = findViewById<android.widget.LinearLayout>(R.id.navMessages)
        val navProfile = findViewById<android.widget.LinearLayout>(R.id.navProfile)
        //val noti = findViewById<android.widget.LinearLayout>(R.id.layoutNotifications)

        navHome.setOnClickListener {
            try {
                val intent = Intent(this, Class.forName("com.muhammadahmedmufii.comfybuy.HomeActivity"))
                startActivity(intent)
                finish()
            } catch (e: ClassNotFoundException) {
                Toast.makeText(this, "Home screen coming soon!", Toast.LENGTH_SHORT).show()
            }
        }

        navSearch.setOnClickListener {
            try {
                val intent = Intent(this, Class.forName("com.muhammadahmedmufii.comfybuy.SearchActivity"))
                startActivity(intent)
                finish()
            } catch (e: ClassNotFoundException) {
                Toast.makeText(this, "Search screen coming soon!", Toast.LENGTH_SHORT).show()
            }
        }

        navMessages.setOnClickListener {
            try {
                val intent = Intent(this, Class.forName("com.muhammadahmedmufii.comfybuy.Messages"))
                startActivity(intent)
                finish()
            } catch (e: ClassNotFoundException) {
                Toast.makeText(this, "Messages screen coming soon!", Toast.LENGTH_SHORT).show()
            }
        }

        val notificationsLayout: View = findViewById(R.id.layoutNotifications)
        notificationsLayout.setOnClickListener {
            Log.d("ProfileActivity", "Notifications layout clicked")
            Toast.makeText(this, "Opening Notifications", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        }
    }

//    private fun logoutUser() {
//        // Sign out from Firebase Auth
//        auth.signOut()
//        Log.d("ProfileActivity", "Firebase user after signOut: ${auth.currentUser}")
//
//        // Sign out from Google
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(getString(R.string.default_web_client_id))
//            .requestEmail()
//            .requestProfile()
//            .build()
//        val googleSignInClient = GoogleSignIn.getClient(this, gso)
//        googleSignInClient.signOut().addOnCompleteListener(this) { task ->
//            if (task.isSuccessful) {
//                Log.d("ProfileActivity", "Google sign-out successful")
//            } else {
//                Log.e("ProfileActivity", "Google sign-out failed", task.exception)
//            }
//        }
//
//        // Navigate to login screen
//        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
//        val intent = Intent(this, login::class.java)
//        Log.d("ProfileActivity", "Starting login activity")
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        startActivity(intent)
//        finish()
//    }
private fun logoutUser() {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(this, gso)

    googleSignInClient.signOut().addOnCompleteListener(this) { task ->
        if (task.isSuccessful) {
            Log.d("ProfileActivity", "Google sign-out successful")

            // Now sign out from Firebase
            auth.signOut()
            Log.d("ProfileActivity", "Firebase user after signOut: ${auth.currentUser}")

            // Navigate to login
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            Log.e("ProfileActivity", "Google sign-out failed", task.exception)
        }
    }
}

}