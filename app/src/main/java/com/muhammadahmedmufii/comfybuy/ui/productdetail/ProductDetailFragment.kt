package com.muhammadahmedmufii.comfybuy.ui.productdetail // Or your fragment package

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback // For custom back press handling
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.muhammadahmedmufii.comfybuy.MainActivity
import com.muhammadahmedmufii.comfybuy.R
import com.muhammadahmedmufii.comfybuy.SellerProfileActivity
import de.hdodenhof.circleimageview.CircleImageView

class ProductDetailFragment : Fragment() {
    private val TAG = "ProductDetailFrag"

    private lateinit var viewModel: ProductDetailViewModel
    private lateinit var imageViewPager: ViewPager2
    private lateinit var imageSliderAdapter: ImageSliderAdapter

    // UI elements from product_view.xml
    private lateinit var backButton: ImageView
    private lateinit var likeButton: ImageView
    private lateinit var optionsMenuButton: ImageView
    private lateinit var titleText: TextView
    private lateinit var priceText: TextView
    private lateinit var conditionText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var sellerImage: CircleImageView
    private lateinit var sellerName: TextView
    private lateinit var sellerRating: TextView
    private lateinit var responseTime: TextView
    private lateinit var chatButton: Button

    private var productId: String? = null
    companion object {
        private const val ARG_PRODUCT_ID = "product_id"

        fun newInstance(productId: String): ProductDetailFragment {
            val fragment = ProductDetailFragment()
            val args = Bundle()
            args.putString(ARG_PRODUCT_ID, productId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            productId = it.getString(ARG_PRODUCT_ID)
        }
        if (productId == null) {
            Log.e("ProductDetailFragment", "Product ID is null!")
            parentFragmentManager.popBackStack() // Go back if no ID
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (productId == null) return // Already handled in onCreate, but as a safeguard

        // Use requireActivity().application for Application context
        val factory = ProductDetailViewModelFactory(requireActivity().application, productId!!)
        viewModel = ViewModelProvider(this, factory).get(ProductDetailViewModel::class.java) // Scope VM to this Fragment

        initViews(view)
        setupViewPager()
        observeViewModel()
        setupClickListeners()

        // Handle back press within the fragment if needed (e.g., for custom animations or confirmation)
        // Or rely on default NavController/FragmentManager back stack behavior
        // requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
        //     override fun handleOnBackPressed() {
        //         // Custom back press logic or pop back stack
        //         findNavController().popBackStack() // If using Navigation Component
        //         // OR parentFragmentManager.popBackStack()
        //     }
        // })
    }

    private fun initViews(view: View) {
        imageViewPager = view.findViewById(R.id.imageViewPager)
        // Optional: tabLayoutIndicator = view.findViewById(R.id.tabLayoutIndicator)

        backButton = view.findViewById(R.id.backButton)
        likeButton = view.findViewById(R.id.likeButton)
        optionsMenuButton = view.findViewById(R.id.optionsMenuButton)
        titleText = view.findViewById(R.id.titleTextProductDetail) // Ensure unique ID if toolbar title is different
        priceText = view.findViewById(R.id.priceText)
        conditionText = view.findViewById(R.id.conditionText)
        descriptionText = view.findViewById(R.id.descriptionText)
        sellerImage = view.findViewById(R.id.sellerImage)
        sellerName = view.findViewById(R.id.sellerName)
        sellerRating = view.findViewById(R.id.sellerRating)
        responseTime = view.findViewById(R.id.responseTime)
        chatButton = view.findViewById(R.id.chatButton)
    }

    private fun setupViewPager() {
        // Initialize the ImageSliderAdapter with the ListAdapter pattern
        imageSliderAdapter = ImageSliderAdapter()
        imageViewPager.adapter = imageSliderAdapter
    }

    private fun observeViewModel() {
        viewModel.product.observe(viewLifecycleOwner) { product -> // Use viewLifecycleOwner
            product?.let {
                Log.i(TAG, "Product observed: ${it.title}, Total ImageBitmaps: ${it.imageBitmaps.size}")
                titleText.text = it.title // Update the main product title TextView
                // Update the title in the custom toolbar if you have one inside fragment_product_detail.xml
                // view?.findViewById<TextView>(R.id.toolbarProductTitle)?.text = it.title

                priceText.text = it.price
                descriptionText.text = it.description
                conditionText.text = it.condition ?: "N/A"

                if (it.imageBitmaps.isNotEmpty()) {
                    imageSliderAdapter.submitList(it.imageBitmaps)
                    imageViewPager.visibility = View.VISIBLE
                    Log.d(TAG, "First bitmap for detail view (width: ${it.imageBitmaps[0].width})")
                    // Optional: tabLayoutIndicator.visibility = if (it.imageBitmaps.size > 1) View.VISIBLE else View.GONE
                } else {
                    imageSliderAdapter.submitList(emptyList())
                    imageViewPager.visibility = View.GONE
                    // Optional: tabLayoutIndicator.visibility = View.GONE
                }
            } ?: run {
                Toast.makeText(requireContext(), "Product not found", Toast.LENGTH_SHORT).show()
                Log.e("ProductDetailFragment", "Product data is null, navigating back.")
                if (isAdded) { // Check if fragment is added before popping
                    parentFragmentManager.popBackStack()
                }
            }
        }

        viewModel.seller.observe(viewLifecycleOwner) { seller -> // Use viewLifecycleOwner
            seller?.let {
                sellerName.text = it.fullName ?: "Unknown Seller"
                it.profileImageBitmap?.let { bitmap ->
                    Glide.with(this).load(bitmap) // 'this' is Fragment context for Glide
                        .placeholder(R.drawable.avatar_placeholder)
                        .error(R.drawable.avatar_placeholder)
                        .into(sellerImage)
                } ?: sellerImage.setImageResource(R.drawable.avatar_placeholder)
                sellerRating.text = "Rating N/A"
                responseTime.text = "Response Time N/A"
            } ?: run {
                sellerName.text = "Unknown Seller"
                sellerImage.setImageResource(R.drawable.avatar_placeholder)
                sellerRating.text = ""
                responseTime.text = ""
            }
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        likeButton.setOnClickListener {
            Toast.makeText(requireContext(), "Like button clicked", Toast.LENGTH_SHORT).show()
        }

        optionsMenuButton.setOnClickListener {
            Toast.makeText(requireContext(), "Options menu clicked", Toast.LENGTH_SHORT).show()
        }

        chatButton.setOnClickListener {
            val sellerId = viewModel.seller.value?.userId
            if (sellerId != null) {
                Toast.makeText(requireContext(), "Chat with seller $sellerId clicked", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to ChatFragment, passing sellerId
                // Example with Nav Component:
                // val action = ProductDetailFragmentDirections.actionProductDetailFragmentToChatFragment(sellerId)
                // findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "Seller information not available for chat", Toast.LENGTH_SHORT).show()
            }
        }

        val sellerClickListener = View.OnClickListener {
            viewModel.seller.value?.userId?.let { sellerId ->
                val currentAuthUserId = FirebaseAuth.getInstance().currentUser?.uid
                if (sellerId == currentAuthUserId) {
                    // It's the current user's own product, navigate to their main profile tab in MainActivity
                    Log.d("ProductDetailFragment", "Clicked on own seller info, navigating to main Profile tab.")
                    (activity as? MainActivity)?.navigateToMainProfileTab() // You need this in MainActivity
                } else {
                    // It's another user's product, navigate to SellerProfileActivity via MainActivity method
                    Log.d("ProductDetailFragment", "Seller info clicked, navigating to SellerProfileActivity for user: $sellerId")
                    (activity as? MainActivity)?.navigateToSellerProfile(sellerId)
                }
            } ?: Toast.makeText(requireContext(), "Seller ID not available.", Toast.LENGTH_SHORT).show()
        }
        sellerImage.setOnClickListener(sellerClickListener)
        sellerName.setOnClickListener(sellerClickListener)
    }


}