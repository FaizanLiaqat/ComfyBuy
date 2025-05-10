package com.muhammadahmedmufii.comfybuy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class productview : AppCompatActivity() {

    private lateinit var viewPager2: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_product_detail)

        // Initialize ViewPager2
        viewPager2 = findViewById(R.id.imageViewPager)

        // Prepare the list of images to display
        val images = listOf(
            R.drawable.ic_apple,  // Replace with your actual image resources
            R.drawable.ic_google,  // Replace with your actual image resources
            R.drawable.ic_facebook   // Replace with your actual image resources
        )

        // Set the adapter for the ViewPager2
        val adapter = ImageSliderAdapter(images)
        viewPager2.adapter = adapter
    }
}
