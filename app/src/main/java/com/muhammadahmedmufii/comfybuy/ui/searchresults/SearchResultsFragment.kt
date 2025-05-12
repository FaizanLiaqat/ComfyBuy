// --- ui/searchresults/SearchResultsFragment.kt ---
package com.muhammadahmedmufii.comfybuy.ui.searchresults

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip // For filter chips
import com.muhammadahmedmufii.comfybuy.MainActivity
import com.muhammadahmedmufii.comfybuy.ProductAdapter
import com.muhammadahmedmufii.comfybuy.R
import com.muhammadahmedmufii.comfybuy.databinding.FragmentSearchResultsBinding // Use ViewBinding

class SearchResultsFragment : Fragment() {
    private val TAG = "SearchResultsFragment"

    private var _binding: FragmentSearchResultsBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private lateinit var viewModel: SearchResultsViewModel

    private var initialSearchQuery: String? = null

    companion object {
        private const val ARG_SEARCH_QUERY = "search_query"

        fun newInstance(query: String? = null): SearchResultsFragment {
            val fragment = SearchResultsFragment()
            val args = Bundle()
            query?.let { args.putString(ARG_SEARCH_QUERY, it) }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            initialSearchQuery = it.getString(ARG_SEARCH_QUERY)
        }
        Log.d(TAG, "onCreate: Initial search query: $initialSearchQuery")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        // Pass the initialSearchQuery to the factory
        val factory = SearchResultsViewModelFactory(requireActivity().application, initialSearchQuery)
        viewModel = ViewModelProvider(this, factory).get(SearchResultsViewModel::class.java)

        setupRecyclerView()
        setupSearchListeners() // For the EditText in this fragment
        setupSortAndFilterListeners()
        observeViewModel()

        // If there was an initial query, set it in the EditText
        initialSearchQuery?.let {
            binding.etSearch.setText(it) // Also triggers onTextChanged if listener is active
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        productAdapter = ProductAdapter { product ->
            (activity as? MainActivity)?.navigateToProductDetail(product.productId)
        }
        binding.recyclerViewProducts.adapter = productAdapter
    }

    private fun observeViewModel() {
        Log.d(TAG, "Setting up products observer.")
        viewModel.products.observe(viewLifecycleOwner) { productList ->
            Log.i(TAG, "Products LiveData updated. List size: ${productList.size}")
            productAdapter.submitList(productList)
            if (productList.isEmpty() && !binding.etSearch.text.isNullOrEmpty()) {
                // Optionally show a "no results" message
                Toast.makeText(requireContext(), "No products found for your search.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearchListeners() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString())
                Log.d(TAG, "Search query changed to: $s")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        // Consider handling search IME action
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                viewModel.setSearchQuery(binding.etSearch.text.toString())
                // Hide keyboard
                val imm = activity?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
                imm?.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
                binding.etSearch.clearFocus()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun setupSortAndFilterListeners() {
        binding.sortRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val sortBy = when (checkedId) {
                R.id.rbNewest -> SearchResultsViewModel.SortBy.NEWEST
                R.id.rbPriceLowHigh -> SearchResultsViewModel.SortBy.PRICE_LOW_HIGH
                R.id.rbPriceHighLow -> SearchResultsViewModel.SortBy.PRICE_HIGH_LOW // Added
                // R.id.rbClosest -> SearchResultsViewModel.SortBy.CLOSEST // Requires location
                else -> SearchResultsViewModel.SortBy.NEWEST // Default
            }
            Log.d(TAG, "Sort option selected: $sortBy")
            viewModel.setSortOrder(sortBy)
        }

        binding.chipCategory.setOnClickListener {
            // TODO: Show category selection dialog, then call viewModel.setCategoryFilter(selectedCategory)
            Toast.makeText(requireContext(), "Category filter clicked (TODO)", Toast.LENGTH_SHORT).show()
        }
        binding.chipPrice.setOnClickListener {
            // TODO: Show price range dialog, then call viewModel.setPriceRangeFilter(min, max)
            Toast.makeText(requireContext(), "Price filter clicked (TODO)", Toast.LENGTH_SHORT).show()
        }
        // Location chip might be more complex, involving map or distance input
        binding.chipLocation.setOnClickListener {
            Toast.makeText(requireContext(), "Location filter clicked (TODO)", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}