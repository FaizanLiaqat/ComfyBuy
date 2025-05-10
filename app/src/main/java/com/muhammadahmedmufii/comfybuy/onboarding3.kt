package com.muhammadahmedmufii.comfybuy

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button // Import Button
import androidx.appcompat.app.AppCompatActivity

class onboarding3: AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val ONBOARDING_PREF = "onboarding_pref"
    private val IS_ONBOARDING_COMPLETED = "is_onboarding_completed"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding3)

        sharedPreferences = getSharedPreferences(ONBOARDING_PREF, Context.MODE_PRIVATE)

        // Corrected: Find nextButton as a Button
        val nextButton = findViewById<Button>(R.id.nextButton)
        nextButton.setOnClickListener {
            // Mark onboarding as completed
            with(sharedPreferences.edit()) {
                putBoolean(IS_ONBOARDING_COMPLETED, true)
                apply()
            }

            val intent = Intent(this, login::class.java)
            startActivity(intent)
            finish()
        }
    }
}