// --- ui/personalinfo/PersonalInfoViewModel.kt ---
package com.muhammadahmedmufii.comfybuy.ui.personalinfo // New package

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.muhammadahmedmufii.comfybuy.data.local.AppDatabase
import com.muhammadahmedmufii.comfybuy.data.repository.UserRepository
import com.muhammadahmedmufii.comfybuy.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PersonalInfoViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository
    private val firebaseAuth: FirebaseAuth

    private val _currentUserDetails = MutableLiveData<User?>()
    val currentUserDetails: LiveData<User?> get() = _currentUserDetails

    private val _isLoading = MutableLiveData<Boolean>(true)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _saveStatus = MutableLiveData<SaveOperationStatus>()
    val saveStatus: LiveData<SaveOperationStatus> get() = _saveStatus

    init {
        val db = AppDatabase.getDatabase(application)
        firebaseAuth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        // Ensure RTDB URL is correct
        val rtdb = FirebaseDatabase.getInstance("https://messamfaizanahmed-default-rtdb.asia-southeast1.firebasedatabase.app")
        userRepository = UserRepository(db.userDao(), firebaseAuth, firestore, rtdb)

        loadCurrentUserDetails()
    }

    private fun loadCurrentUserDetails() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            userRepository.getCurrentUser().collectLatest { user ->
                if (user != null) {
                    _currentUserDetails.postValue(user)
                    _isLoading.postValue(false)
                    Log.d("PersonalInfoVM", "User details loaded: $user")
                } else {
                    // Initial load is null, attempt fetch
                    firebaseAuth.currentUser?.uid?.let { userId ->
                        viewModelScope.launch(Dispatchers.IO) {
                            try {
                                userRepository.fetchAndSaveUser(userId)
                                // Rely on collectLatest to pick up the change
                            } catch (e: Exception) {
                                Log.e("PersonalInfoVM", "Error fetching user details", e)
                                _currentUserDetails.postValue(null) // Explicitly set null on error
                                _isLoading.postValue(false)       // Stop loading on error
                            }
                        }
                    } ?: run {
                        // No user logged in
                        _currentUserDetails.postValue(null)
                        _isLoading.postValue(false)
                    }
                }
            }
            if (_isLoading.value == true) { // Safety net
                _isLoading.postValue(false)
            }
        }
    }

    fun updatePersonalInformation(updatedUser: User) {
        viewModelScope.launch {
            _saveStatus.postValue(SaveOperationStatus.Loading)
            try {
                // In this simplified version, we are not handling image changes here.
                // EditProfileActivity handles image changes.
                // This save method focuses on text-based personal info.
                // We pass 'null' for newImageBitmap as PersonalInfoActivity doesn't change the image.
                userRepository.saveUserProfileDetails(updatedUser, null) // Pass null for bitmap
                _saveStatus.postValue(SaveOperationStatus.Success("Information updated!"))
                // Optionally re-fetch to ensure UI consistency if needed, though Room flow should handle it.
                // loadCurrentUserDetails() // Or just rely on the flow.
            } catch (e: Exception) {
                Log.e("PersonalInfoVM", "Failed to update personal info", e)
                _saveStatus.postValue(SaveOperationStatus.Error("Update failed: ${e.message}"))
            }
        }
    }
}

// Sealed class for save operation status
sealed class SaveOperationStatus {
    object Idle : SaveOperationStatus()
    object Loading : SaveOperationStatus()
    data class Success(val message: String) : SaveOperationStatus()
    data class Error(val message: String) : SaveOperationStatus()
}

