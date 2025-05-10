package com.muhammadahmedmufii.comfybuy

import android.content.Intent
import android.os.Bundle
import android.widget.Button // Import Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class onboarding2: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding2)

        // Corrected: Find nextButton as a Button
        val nextButton = findViewById<Button>(R.id.nextButton)
        nextButton.setOnClickListener {
            val intent = Intent(this, onboarding3::class.java)
            startActivity(intent)
        }

        val skipButton = findViewById<TextView>(R.id.skipButton)
        skipButton.setOnClickListener {
            val intent = Intent(this, login::class.java)
            startActivity(intent)
            finish()
        }
    }
}