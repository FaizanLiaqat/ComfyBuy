package com.muhammadahmedmufii.comfybuy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar // Import Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhammadahmedmufii.comfybuy.databinding.ActivitySellerProfileBinding // Use ViewBinding
import com.muhammadahmedmufii.comfybuy.ui.sellerprofile.SellerProfileViewModel
import com.muhammadahmedmufii.comfybuy.ui.sellerprofile.SellerProfileViewModelFactory
import de.hdodenhof.circleimageview.CircleImageView

class SellerProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySellerProfileBinding // ViewBinding
    private lateinit var viewModel: SellerProfileViewModel
    private lateinit var productAdapter: ProductAdapter

    companion object {
        const val EXTRA_SELLER_ID = "extra_seller_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sellerId = intent.getStringExtra(EXTRA_SELLER_ID)
        if (sellerId == null) {
            Toast.makeText(this, "Seller ID not found.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupToolbar()

        val factory = SellerProfileViewModelFactory(application, sellerId)
        viewModel = ViewModelProvider(this, factory).get(SellerProfileViewModel::class.java)


        setupRecyclerView()
        observeViewModel()
        setupTabListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarSellerProfile) // Assuming ID toolbarSellerProfile in XML
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false) // We use our custom title TextView
        binding.toolbarSellerProfile.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }



    private fun setupRecyclerView() {
        binding.recyclerProducts.layoutManager = GridLayoutManager(this, 2)
        productAdapter = ProductAdapter { product ->
            // OPTION 2: Signal MainActivity to show ProductDetailFragment
            val intent = Intent(this, MainActivity::class.java).apply {
                action = MainActivity.ACTION_SHOW_PRODUCT_DETAIL
                putExtra(MainActivity.EXTRA_PRODUCT_ID_TO_SHOW, product.productId)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish() // Finish SellerProfileActivity
        }
        binding.recyclerProducts.adapter = productAdapter
        binding.recyclerProducts.isNestedScrollingEnabled = false
    }

    private fun observeViewModel() {
        viewModel.sellerDetails.observe(this) { seller ->
            if (seller != null) {
                binding.toolbarSellerProfile.title = "${seller.fullName ?: "Seller"}'s Profile" // Set toolbar title
                binding.name.text = seller.fullName ?: "Seller Not Found"
                binding.subtitle.text = seller.bio ?: "No bio available."

                seller.profileImageBitmap?.let { bitmap ->
                    Glide.with(this).load(bitmap)
                        .placeholder(R.drawable.avatar_placeholder)
                        .error(R.drawable.avatar_placeholder)
                        .into(binding.avatar)
                } ?: binding.avatar.setImageResource(R.drawable.avatar_placeholder)

                // Update stats - these would ideally come from separate LiveData/aggregations
                binding.countFollowers.text = "-" // Placeholder
                binding.countFollowing.text = "-" // Placeholder
            } else {
                binding.toolbarSellerProfile.title = "Profile Not Found"
                binding.name.text = "Seller Not Found"
                binding.subtitle.text = ""
                binding.avatar.setImageResource(R.drawable.avatar_placeholder)
                binding.countListings.text = "0"
            }
        }

        viewModel.sellerProducts.observe(this) { productList ->
            productAdapter.submitList(productList)
            binding.countListings.text = productList.size.toString()
        }
    }

    private fun setupTabListeners() {
        binding.tabListings.setOnClickListener {
            Toast.makeText(this, "Viewing seller's listings", Toast.LENGTH_SHORT).show()
        }
        binding.tabFavorites.setOnClickListener {
            Toast.makeText(this, "Seller's favorites (not implemented)", Toast.LENGTH_SHORT).show()
        }
        binding.tabReviews.setOnClickListener {
            Toast.makeText(this, "Seller's reviews (not implemented)", Toast.LENGTH_SHORT).show()
        }
    }
}