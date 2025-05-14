// --- ui/searchresults/SearchResultsFragment.kt ---
package com.muhammadahmedmufii.comfybuy.ui.searchresults

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.muhammadahmedmufii.comfybuy.MainActivity
import com.muhammadahmedmufii.comfybuy.ProductAdapter
import com.muhammadahmedmufii.comfybuy.R
import com.muhammadahmedmufii.comfybuy.databinding.FragmentSearchResultsBinding

class SearchResultsFragment : Fragment() {
    companion object {
        private const val ARG_SEARCH_QUERY = "search_query"
        /**
         * Factory method to create a new instance of this fragment using the provided query.
         */
        @JvmStatic
        fun newInstance(query: String?) = SearchResultsFragment().apply {
            arguments = Bundle().apply { putString(ARG_SEARCH_QUERY, query) }
        }
    }
    private val tag = "SearchResults"
    private var _binding: FragmentSearchResultsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SearchResultsViewModel
    private lateinit var adapter: ProductAdapter
    private var initialSearchQuery: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialSearchQuery = arguments?.getString("search_query")
        Log.d(tag, "Initial query: $initialSearchQuery")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            SearchResultsViewModelFactory(requireActivity().application, initialSearchQuery)
        ).get(SearchResultsViewModel::class.java)

        setupRecyclerView()
        setupSearch()
        setupFilters()
        observe()

        initialSearchQuery?.let { binding.etSearch.setText(it) }
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter { product ->
            (activity as? MainActivity)?.navigateToProductDetail(product.productId)
        }
        binding.recyclerViewProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerViewProducts.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                viewModel.setSearchQuery(binding.etSearch.text.toString())
                (activity?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager)
                    ?.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
                binding.etSearch.clearFocus()
                true
            } else false
        }
    }

    private fun setupFilters() {
        // Sort
        binding.sortRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val sort = when (checkedId) {
                R.id.rbNewest -> SearchResultsViewModel.SortBy.NEWEST
                R.id.rbPriceLowHigh -> SearchResultsViewModel.SortBy.PRICE_LOW_HIGH
                R.id.rbPriceHighLow -> SearchResultsViewModel.SortBy.PRICE_HIGH_LOW
                else -> SearchResultsViewModel.SortBy.NEWEST
            }
            viewModel.setSortOrder(sort)
        }

        // Category & Price
        val categoryChips = listOf(
            binding.chipElectronics,
            binding.chipHome,
            binding.chipClothing
        )
        fun clearCategories() {
            categoryChips.forEach { it.isChecked = false }
        }
        categoryChips.forEach { chip ->
            chip.setOnCheckedChangeListener { button, isChecked ->
                if (isChecked) {
                    // clear others and price
                    categoryChips.filter { it != chip }.forEach { it.isChecked = false }
                    binding.chipPrice.isChecked = false
                    viewModel.setPriceRangeFilter(null, null)
                    viewModel.setCategoryFilter(chip.text.toString())
                } else {
                    viewModel.setCategoryFilter(null)
                }
            }
        }

        // Price
        binding.chipPrice.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                clearCategories()
                showPriceDialog()
            } else {
                viewModel.setPriceRangeFilter(null, null)
            }
        }

        // Location placeholder
        binding.chipLocation.setOnClickListener {
            Toast.makeText(requireContext(), "Welcome to ComfyBuy!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observe() {
        viewModel.products.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            if (list.isEmpty() && binding.etSearch.text.isNotEmpty()) {
                Toast.makeText(requireContext(), "No products found.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPriceDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32)
        }
        val etMin = EditText(requireContext()).apply { hint = "Min price"; inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL }
        val etMax = EditText(requireContext()).apply { hint = "Max price"; inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL }
        layout.addView(etMin)
        layout.addView(etMax)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Price Range")
            .setView(layout)
            .setPositiveButton("Apply") { _, _ ->
                val min = etMin.text.toString().toDoubleOrNull() ?: 0.0
                val max = etMax.text.toString().toDoubleOrNull() ?: Double.MAX_VALUE
                viewModel.setPriceRangeFilter(min, max)
            }
            .setNegativeButton("Clear") { _, _ ->
                binding.chipPrice.isChecked = false
                viewModel.setPriceRangeFilter(null, null)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}