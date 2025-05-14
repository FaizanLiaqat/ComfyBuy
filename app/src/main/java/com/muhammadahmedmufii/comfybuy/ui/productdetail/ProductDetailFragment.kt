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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.tabs.TabLayout // For Page Indicator
import com.google.android.material.tabs.TabLayoutMediator // For Page Indicator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muhammadahmedmufii.comfybuy.ChatActivity
import com.muhammadahmedmufii.comfybuy.MainActivity
import com.muhammadahmedmufii.comfybuy.R
import com.muhammadahmedmufii.comfybuy.SellerProfileActivity
import com.muhammadahmedmufii.comfybuy.databinding.FragmentProductDetailBinding // Using ViewBinding
import de.hdodenhof.circleimageview.CircleImageView
import java.util.UUID

class ProductDetailFragment : Fragment() {
    private val TAG = "ProductDetailFrag"


    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProductDetailViewModel
    private lateinit var imageSliderAdapter: ImageSliderAdapter

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
//
//    private lateinit var viewModel: ProductDetailViewModel
//    private lateinit var imageViewPager: ViewPager2
//    private lateinit var imageSliderAdapter: ImageSliderAdapter
//
//    // UI elements from product_view.xml
//    private lateinit var backButton: ImageView
//    private lateinit var likeButton: ImageView
//    private lateinit var optionsMenuButton: ImageView
//    private lateinit var titleText: TextView
//    private lateinit var priceText: TextView
//    private lateinit var conditionText: TextView
//    private lateinit var descriptionText: TextView
//    private lateinit var sellerImage: CircleImageView
//    private lateinit var sellerName: TextView
//    private lateinit var sellerRating: TextView
//    private lateinit var responseTime: TextView
//    private lateinit var chatButton: Button
//
//    private var productId: String? = null
//    companion object {
//        private const val ARG_PRODUCT_ID = "product_id"
//
//        fun newInstance(productId: String): ProductDetailFragment {
//            val fragment = ProductDetailFragment()
//            val args = Bundle()
//            args.putString(ARG_PRODUCT_ID, productId)
//            fragment.arguments = args
//            return fragment
//        }
//    }

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.let {
        productId = it.getString(ARG_PRODUCT_ID)
    }
    if (productId == null) {
        Log.e(TAG, "Product ID is null! Popping back stack.")
        parentFragmentManager.popBackStack()
    }
}
//
//
override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
): View { // Return type is View, not View? because binding will be non-null
    _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
    Log.d(TAG, "onCreateView: Binding inflated.")
    return binding.root // Return the root of the binding
}
//
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    Log.d(TAG, "onViewCreated for productId: $productId")

    if (productId == null) {
        Log.e(TAG, "Product ID is null in onViewCreated, cannot proceed.")
        return
    }

    val factory = ProductDetailViewModelFactory(requireActivity().application, productId!!)
    viewModel = ViewModelProvider(this, factory).get(ProductDetailViewModel::class.java)
    Log.d(TAG, "ViewModel initialized.")

    // initViews(view) // Not needed, using binding directly
    setupViewPager()
    observeViewModel()
    setupClickListeners()
}
    private fun setupViewPager() {
        Log.d(TAG, "setupViewPager called.")
        imageSliderAdapter = ImageSliderAdapter()
        binding.imageViewPager.adapter = imageSliderAdapter // Use binding

        TabLayoutMediator(binding.tabLayoutIndicator, binding.imageViewPager) { tab, position ->
            // Optional: Customize tab appearance
        }.attach()
        Log.d(TAG, "ViewPager and TabLayoutMediator setup complete.")
    }

    private fun observeViewModel() {
        Log.d(TAG, "Setting up ViewModel observers.")
        viewModel.product.observe(viewLifecycleOwner) { product ->
            if (product != null) {
                Log.i(TAG, "Product observed: '${product.title}', Images: ${product.imageBitmaps.size}")
                binding.toolbarProductTitle.text = product.title
                binding.titleTextProductDetail.text = product.title
                binding.priceText.text = product.price
                binding.descriptionText.text = product.description ?: "No description available."
                binding.conditionText.text = product.condition ?: "N/A"

                if (product.imageBitmaps.isNotEmpty()) {
                    imageSliderAdapter.submitList(product.imageBitmaps)
                    binding.imageViewPager.visibility = View.VISIBLE
                    binding.tabLayoutIndicator.visibility = if (product.imageBitmaps.size > 1) View.VISIBLE else View.GONE
                    Log.d(TAG, "Displaying ${product.imageBitmaps.size} images.")
                } else {
                    imageSliderAdapter.submitList(emptyList())
                    binding.imageViewPager.visibility = View.GONE
                    binding.tabLayoutIndicator.visibility = View.GONE
                    Log.d(TAG, "No images to display for product ${product.productId}")
                }
            } else {
                Toast.makeText(requireContext(), "Product not found or deleted.", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Product data is null. Navigating back.")
                if (isAdded) parentFragmentManager.popBackStack()
            }
        }

        viewModel.seller.observe(viewLifecycleOwner) { seller ->
            if (seller != null) {
                Log.d(TAG, "Seller observed: ${seller.fullName}")
                binding.sellerName.text = seller.fullName ?: "Unknown Seller"
                seller.profileImageBitmap?.let { bitmap ->
                    Glide.with(this).load(bitmap)
                        .placeholder(R.drawable.avatar_placeholder)
                        .error(R.drawable.avatar_placeholder)
                        .into(binding.sellerImage)
                } ?: binding.sellerImage.setImageResource(R.drawable.avatar_placeholder)
                binding.sellerRating.text = "Rating N/A"
                binding.responseTime.text = "Response Time N/A"
            } else {
                Log.d(TAG, "Seller data is null.")
                binding.sellerName.text = "Seller information unavailable"
                binding.sellerImage.setImageResource(R.drawable.avatar_placeholder)
                binding.sellerRating.text = ""
                binding.responseTime.text = ""
            }
        }
    }

    private fun setupClickListeners() {
        binding.chatButton.setOnClickListener {
            Log.d(TAG, "💬 chatButton clicked!")
            lookupOrCreateChat()
        }
    }

    // Replace with your own URL:
    private val RTDB_URL = "https://messamfaizanahmed-default-rtdb.asia-southeast1.firebasedatabase.app"

    private fun lookupOrCreateChat() {
        val sellerId = viewModel.seller.value?.userId
        val buyerId  = FirebaseAuth.getInstance().currentUser?.uid
        if (sellerId.isNullOrEmpty() || buyerId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Cannot start chat.", Toast.LENGTH_SHORT).show()
            return
        }
        if (sellerId == buyerId) {
            Toast.makeText(requireContext(), "That’s your own product!", Toast.LENGTH_SHORT).show()
            return
        }

        // 1) deterministic pair key
        val sortedUids = listOf(buyerId, sellerId).sorted()
        val pairKey    = sortedUids.joinToString("_")  // e.g. "A_B"
        Log.d(TAG, "Looking for existing chat with participantsPair=$pairKey")

        val db       = FirebaseDatabase.getInstance(RTDB_URL).reference
        val chatsRef = db.child("chats")

        // 2) query for existing
        chatsRef.orderByChild("participantsPair")
            .equalTo(pairKey)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // there should be exactly one
                        val existingChatId = snapshot.children.first().key!!
                        Log.d(TAG, "Found existing chatId=$existingChatId")
                        openChat(existingChatId, sellerId, viewModel.seller.value!!.fullName!!)
                    } else {
                        Log.d(TAG, "No existing chat; creating new one")
                        createChatNode(pairKey, sortedUids, sellerId)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Lookup failed", error.toException())
                    Toast.makeText(requireContext(), "Chat lookup failed.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun createChatNode(
        pairKey: String,
        sortedUids: List<String>,
        sellerId: String
    ) {
        val db       = FirebaseDatabase.getInstance(RTDB_URL).reference
        val chatsRef = db.child("chats")
        // push a new chat
        val newChatRef = chatsRef.push()
        val chatId     = newChatRef.key!!
        Log.d(TAG, "Creating chatId=$chatId for pair=$pairKey")

        // build initial data
        val chatData = mapOf<String, Any>(
            "participantsPair" to pairKey,
            "participants/${sortedUids[0]}" to true,
            "participants/${sortedUids[1]}" to true
        )
        // per‑user index
        val userChatsData = mapOf(
            "userChats/${sortedUids[0]}/$chatId" to true,
            "userChats/${sortedUids[1]}/$chatId" to true
        )

        // atomic write: chatData + userChatsData
        val updates = mutableMapOf<String, Any>()
        for ((k, v) in chatData)      updates["chats/$chatId/$k"] = v
        for ((k, v) in userChatsData) updates[k] = v

        db.updateChildren(updates)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Created chat and indexed under users")
                openChat(chatId, sellerId, viewModel.seller.value!!.fullName!!)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "🚨 Failed to create chat", e)
                Toast.makeText(requireContext(), "Could not start chat.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openChat(chatId: String, opponentId: String, opponentName: String) {
        Log.d(TAG, "Opening ChatActivity for chatId=$chatId")
        val intent = Intent(requireContext(), ChatActivity::class.java).apply {
            putExtra("CHAT_ID",      chatId)
            putExtra("CHAT_USER_ID", opponentId)
            putExtra("CHAT_NAME",    opponentName)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView called.")
        // It's good practice to clear the adapter of the ViewPager2 to prevent leaks,
        // especially if the adapter holds context or complex listeners.
        binding.imageViewPager.adapter = null
        _binding = null // Crucial for ViewBinding in Fragments
    }

//
//    private fun initViews(view: View) {
//        imageViewPager = view.findViewById(R.id.imageViewPager)
//        // Optional: tabLayoutIndicator = view.findViewById(R.id.tabLayoutIndicator)
//
//        backButton = view.findViewById(R.id.backButton)
//        likeButton = view.findViewById(R.id.likeButton)
//        optionsMenuButton = view.findViewById(R.id.optionsMenuButton)
//        titleText = view.findViewById(R.id.titleTextProductDetail) // Ensure unique ID if toolbar title is different
//        priceText = view.findViewById(R.id.priceText)
//        conditionText = view.findViewById(R.id.conditionText)
//        descriptionText = view.findViewById(R.id.descriptionText)
//        sellerImage = view.findViewById(R.id.sellerImage)
//        sellerName = view.findViewById(R.id.sellerName)
//        sellerRating = view.findViewById(R.id.sellerRating)
//        responseTime = view.findViewById(R.id.responseTime)
//        chatButton = view.findViewById(R.id.chatButton)
//    }
//
//    private fun setupViewPager() {
//        // Initialize the ImageSliderAdapter with the ListAdapter pattern
//        imageSliderAdapter = ImageSliderAdapter()
//        imageViewPager.adapter = imageSliderAdapter
//    }
//
//    private fun observeViewModel() {
//        viewModel.product.observe(viewLifecycleOwner) { product -> // Use viewLifecycleOwner
//            product?.let {
//                Log.i(TAG, "Product observed: ${it.title}, Total ImageBitmaps: ${it.imageBitmaps.size}")
//                titleText.text = it.title // Update the main product title TextView
//                // Update the title in the custom toolbar if you have one inside fragment_product_detail.xml
//                // view?.findViewById<TextView>(R.id.toolbarProductTitle)?.text = it.title
//
//                priceText.text = it.price
//                descriptionText.text = it.description
//                conditionText.text = it.condition ?: "N/A"
//
//                if (it.imageBitmaps.isNotEmpty()) {
//                    imageSliderAdapter.submitList(it.imageBitmaps)
//                    imageViewPager.visibility = View.VISIBLE
//                    Log.d(TAG, "First bitmap for detail view (width: ${it.imageBitmaps[0].width})")
//                    // Optional: tabLayoutIndicator.visibility = if (it.imageBitmaps.size > 1) View.VISIBLE else View.GONE
//                } else {
//                    imageSliderAdapter.submitList(emptyList())
//                    imageViewPager.visibility = View.GONE
//                    // Optional: tabLayoutIndicator.visibility = View.GONE
//                }
//            } ?: run {
//                Toast.makeText(requireContext(), "Product not found", Toast.LENGTH_SHORT).show()
//                Log.e("ProductDetailFragment", "Product data is null, navigating back.")
//                if (isAdded) { // Check if fragment is added before popping
//                    parentFragmentManager.popBackStack()
//                }
//            }
//        }
//
//        viewModel.seller.observe(viewLifecycleOwner) { seller -> // Use viewLifecycleOwner
//            seller?.let {
//                sellerName.text = it.fullName ?: "Unknown Seller"
//                it.profileImageBitmap?.let { bitmap ->
//                    Glide.with(this).load(bitmap) // 'this' is Fragment context for Glide
//                        .placeholder(R.drawable.avatar_placeholder)
//                        .error(R.drawable.avatar_placeholder)
//                        .into(sellerImage)
//                } ?: sellerImage.setImageResource(R.drawable.avatar_placeholder)
//                sellerRating.text = "Rating N/A"
//                responseTime.text = "Response Time N/A"
//            } ?: run {
//                sellerName.text = "Unknown Seller"
//                sellerImage.setImageResource(R.drawable.avatar_placeholder)
//                sellerRating.text = ""
//                responseTime.text = ""
//            }
//        }
//    }
//
//    private fun setupClickListeners() {
//        backButton.setOnClickListener {
//            parentFragmentManager.popBackStack()
//        }
//
//        likeButton.setOnClickListener {
//            Toast.makeText(requireContext(), "Like button clicked", Toast.LENGTH_SHORT).show()
//        }
//
//        optionsMenuButton.setOnClickListener {
//            Toast.makeText(requireContext(), "Options menu clicked", Toast.LENGTH_SHORT).show()
//        }
//
//        chatButton.setOnClickListener {
//            val sellerId = viewModel.seller.value?.userId
//            if (sellerId != null) {
//                Toast.makeText(requireContext(), "Chat with seller $sellerId clicked", Toast.LENGTH_SHORT).show()
//                // TODO: Navigate to ChatFragment, passing sellerId
//                // Example with Nav Component:
//                // val action = ProductDetailFragmentDirections.actionProductDetailFragmentToChatFragment(sellerId)
//                // findNavController().navigate(action)
//            } else {
//                Toast.makeText(requireContext(), "Seller information not available for chat", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        val sellerClickListener = View.OnClickListener {
//            viewModel.seller.value?.userId?.let { sellerId ->
//                val currentAuthUserId = FirebaseAuth.getInstance().currentUser?.uid
//                if (sellerId == currentAuthUserId) {
//                    // It's the current user's own product, navigate to their main profile tab in MainActivity
//                    Log.d("ProductDetailFragment", "Clicked on own seller info, navigating to main Profile tab.")
//                    (activity as? MainActivity)?.navigateToMainProfileTab() // You need this in MainActivity
//                } else {
//                    // It's another user's product, navigate to SellerProfileActivity via MainActivity method
//                    Log.d("ProductDetailFragment", "Seller info clicked, navigating to SellerProfileActivity for user: $sellerId")
//                    (activity as? MainActivity)?.navigateToSellerProfile(sellerId)
//                }
//            } ?: Toast.makeText(requireContext(), "Seller ID not available.", Toast.LENGTH_SHORT).show()
//        }
//        sellerImage.setOnClickListener(sellerClickListener)
//        sellerName.setOnClickListener(sellerClickListener)
//    }


}