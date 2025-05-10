package com.muhammadahmedmufii.comfybuy

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth // Import Firebase Auth

class splash : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private val ONBOARDING_PREF = "onboarding_pref"
    private val IS_ONBOARDING_COMPLETED = "is_onboarding_completed"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance() // Get Firebase Auth instance
        sharedPreferences = getSharedPreferences(ONBOARDING_PREF, Context.MODE_PRIVATE)

        // Delay for a few seconds
        Handler().postDelayed({
            checkAuthenticationAndOnboarding()
        }, 2000) // 2000 milliseconds = 2 seconds
    }

    private fun checkAuthenticationAndOnboarding() {
        val currentUser = auth.currentUser
        val onboardingCompleted = sharedPreferences.getBoolean(IS_ONBOARDING_COMPLETED, false)

        val intent: Intent
        if (currentUser != null) {
            // User is signed in, go to MainActivity (which hosts the HomeFragment)
            intent = Intent(this, MainActivity::class.java)
        } else {
            // No user is signed in, check onboarding status
            if (onboardingCompleted) {
                // Onboarding completed, go to Login
                intent = Intent(this, login::class.java)
            } else {
                // Onboarding not completed, go to Onboarding flow
                intent = Intent(this, onboarding1::class.java)
            }
        }
        startActivity(intent)
        finish() // Close SplashActivity
    }
}