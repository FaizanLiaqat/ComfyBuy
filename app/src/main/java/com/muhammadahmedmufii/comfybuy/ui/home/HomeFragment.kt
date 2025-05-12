package com.muhammadahmedmufii.comfybuy.ui.home // Recommended package structure for UI

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment // *** CHANGE: Extend Fragment ***
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.muhammadahmedmufii.comfybuy.MainActivity
import com.muhammadahmedmufii.comfybuy.ProductAdapter
import com.muhammadahmedmufii.comfybuy.ui.productdetail.ProductDetailFragment
import com.muhammadahmedmufii.comfybuy.R
import com.muhammadahmedmufii.comfybuy.SyncWorkScheduler


// CHANGE: Renamed from 'home' to 'HomeFragment' and extends Fragment
class HomeFragment : Fragment() {
    private val TAG = "HomeFragment"
    companion object {
        fun newInstance() = HomeFragment()
    }
    private lateinit var recycler: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var homeViewModel: HomeViewModel

    // References to category TextViews
    private lateinit var allCategoryButton: TextView
    private lateinit var furnitureCategoryButton: TextView
    private lateinit var homeCategoryButton: TextView
    private lateinit var fashionCategoryButton: TextView

    // References to search bar elements
    private lateinit var searchEdit: EditText
    private lateinit var filterIcon: ImageView

    // --- Bottom navigation views removed from here ---
    // These will be in the hosting activity (MainActivity) now.
    // private lateinit var homeIcon: ImageView
    // private lateinit var searchIconBottom: ImageView
    // private lateinit var sellIcon: ImageView
    // private lateinit var messagesIcon: ImageView
    // private lateinit var profileIcon: ImageView


    // --- CHANGE: Use onCreateView to inflate the layout ---
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_home, container, false) // Use the existing home layout
    }

    // --- CHANGE: Use onViewCreated for view setup after inflation ---
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel using requireActivity() to scope it to the hosting Activity
        val factory = HomeViewModelFactory(requireActivity().application)
        homeViewModel = ViewModelProvider(requireActivity(), factory).get(HomeViewModel::class.java)

        // Schedule the periodic sync work
        // Consider moving this to MainActivity or an Application class for better lifecycle management
        // if you want to ensure it runs as long as the app is open, not just when HomeFragment is visible.
        SyncWorkScheduler.schedulePeriodicSync(requireContext())


        initViews(view) // Initialize UI elements from the inflated view
        setupRecyclerView() // Setup RecyclerView and Adapter
        Log.d(TAG, "onViewCreated. Setting up observers.")
        observeViewModel() // Observe data from ViewModel
        setupCategoryListeners() // Setup Category button click listeners
        setupSearchBarListeners() // Setup Search bar/Filter icon listeners


    }

    // --- CHANGE: Pass the inflated view to initViews ---
    private fun initViews(view: View) {
        recycler = view.findViewById(R.id.productRecycler)

        // Initialize category buttons
        allCategoryButton = view.findViewById(R.id.allCategoryButton)
        furnitureCategoryButton = view.findViewById(R.id.furnitureCategoryButton)
        homeCategoryButton = view.findViewById(R.id.homeCategoryButton)
        fashionCategoryButton = view.findViewById(R.id.fashionCategoryButton)

        // Initialize search bar elements
        searchEdit = view.findViewById(R.id.searchEdit)
        filterIcon = view.findViewById(R.id.filterIcon)

        // --- Bottom navigation views initialization removed from here ---
        // homeIcon = view.findViewById(R.id.homeIcon)
        // searchIconBottom = view.findViewById(R.id.searchIconBottom)
        // sellIcon = view.findViewById(R.id.sellIcon)
        // messagesIcon = view.findViewById(R.id.messagesIcon)
        // profileIcon = view.findViewById(R.id.profileIcon)
    }

    private fun setupRecyclerView() {
        // 1) setup RecyclerView as 2-column grid
        // --- CHANGE: Use requireContext() or context instead of 'this' ---
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)

        // Initialize the adapter with an empty list initially
        productAdapter = ProductAdapter { product ->
            (activity as? MainActivity)?.navigateToProductDetail(product.productId)
        }
        recycler.adapter = productAdapter // Set the adapter to the RecyclerView
    }

   private fun observeViewModel() { // <<< UNCOMMENTED AND KEPT YOUR LOGGING >>>
        homeViewModel.products.observe(viewLifecycleOwner) { productList ->
            Log.i(TAG, "Products LiveData observer triggered. List size: ${productList.size}")
            if (productList.isNotEmpty()) {
                // Log details for a few products to check image counts
                productList.take(3).forEachIndexed { index, product ->
                    Log.d(TAG, "Product[$index]: ${product.title}, Image count: ${product.imageBitmaps.size}")
                    if (product.imageBitmaps.isNotEmpty()) {
                        Log.d(TAG, "Product[$index]: First bitmap valid: ${product.imageBitmaps[0] != null} (Width: ${product.imageBitmaps[0]?.width})")
                    } else {
                         Log.d(TAG, "Product[$index]: '${product.title}' has no bitmaps.")
                    }
                }
            }
            productAdapter.submitList(productList)
            Log.d(TAG, "Submitted list to adapter. Adapter current itemCount: ${productAdapter.itemCount}")
        }
    }




    private fun setupCategoryListeners() {
        // Basic click listeners for category buttons (filtering logic comes later)
        allCategoryButton.setOnClickListener {
            // --- CHANGE: Use requireContext() instead of context ---
            Toast.makeText(requireContext(), "All category selected", Toast.LENGTH_SHORT).show()
            // TODO: Implement filtering logic to show all products (in ViewModel)
        }
        furnitureCategoryButton.setOnClickListener {
            // --- CHANGE: Use requireContext() instead of context ---
            Toast.makeText(requireContext(), "Furniture category selected", Toast.LENGTH_SHORT).show()
            // TODO: Implement filtering logic for Furniture (in ViewModel)
        }
        homeCategoryButton.setOnClickListener {
            // --- CHANGE: Use requireContext() instead of context ---
            Toast.makeText(requireContext(), "Home category selected", Toast.LENGTH_SHORT).show()
            // TODO: Implement filtering logic for Home (in ViewModel)
        }
        fashionCategoryButton.setOnClickListener {
            // --- CHANGE: Use requireContext() instead of context ---
            Toast.makeText(requireContext(), "Fashion category selected", Toast.LENGTH_SHORT).show()
            // TODO: Implement filtering logic for Fashion (in ViewModel)
        }
    }

    private fun setupSearchBarListeners() {
        // Assuming you have EditText with ID searchEdit and ImageView with ID filterIcon
        // --- CHANGE: Use the 'view' parameter to find views ---
        // Already done in initViews()


        searchEdit.setOnClickListener {
            // --- CHANGE: Use requireContext() instead of context ---
            Toast.makeText(requireContext(), "Search bar clicked", Toast.LENGTH_SHORT).show()
            // TODO: Implement search functionality (e.g., navigate to a search screen or show search results)
            // Now you would typically navigate to the SearchResultsFragment from the hosting Activity
            // Example (requires MainActivity to have a method to navigate):
            // (activity as? MainActivity)?.navigateToSearch("initial query")
        }

        filterIcon.setOnClickListener {
            // --- CHANGE: Use requireContext() instead of context ---
            Toast.makeText(requireContext(), "Filter icon clicked", Toast.LENGTH_SHORT).show()
            // TODO: Implement filter functionality (e.g., show a filter dialog)
        }
    }
}