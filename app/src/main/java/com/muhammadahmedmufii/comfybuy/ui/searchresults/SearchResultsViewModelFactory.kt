package com.muhammadahmedmufii.comfybuy.ui.searchresults // Example package name

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// Factory for creating SearchResultsViewModel.
// It allows passing the Application context and the initial search query to the ViewModel constructor.
class SearchResultsViewModelFactory(
    private val application: Application,
    private val initialSearchQuery: String? // Parameter for the initial search query
) : ViewModelProvider.Factory { // Inherit from ViewModelProvider.Factory

    // This method is called by the ViewModelProvider to create a new instance of the ViewModel.
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested ViewModel class is assignable to SearchResultsViewModel.
        if (modelClass.isAssignableFrom(SearchResultsViewModel::class.java)) {
            // If it is, create a new instance of SearchResultsViewModel,
            // passing the Application context and the initial search query.
            @Suppress("UNCHECKED_CAST") // Suppress the unchecked cast warning
            return SearchResultsViewModel(application, initialSearchQuery) as T
        }
        // If the requested ViewModel class is not SearchResultsViewModel, throw an exception.
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
