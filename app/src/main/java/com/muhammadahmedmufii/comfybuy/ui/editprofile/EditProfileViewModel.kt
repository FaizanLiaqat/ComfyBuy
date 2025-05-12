package com.muhammadahmedmufii.comfybuy.ui.editprofile

import android.app.Application
import android.graphics.Bitmap
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
import kotlinx.coroutines.flow.collectLatest // Import collectLatest
import kotlinx.coroutines.launch

class EditProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository
    private val firebaseAuth: FirebaseAuth // Added for direct access if needed

    private val _currentUserProfile = MutableLiveData<User?>()
    val currentUserProfile: LiveData<User?> get() = _currentUserProfile

    private val _isLoadingProfile = MutableLiveData<Boolean>(true)
    val isLoadingProfile: LiveData<Boolean> get() = _isLoadingProfile

    private val _saveStatus = MutableLiveData<SaveStatus>()
    val saveStatus: LiveData<SaveStatus> get() = _saveStatus

    init {
        Log.d("EditProfileViewModel", "init: ViewModel initialized")
//        val database = AppDatabase.getDatabase(application)
//        val userDao = database.userDao()
        firebaseAuth = FirebaseAuth.getInstance() // Initialize FirebaseAuth
        val firestore = FirebaseFirestore.getInstance()
        // Ensure RTDB URL is correct here
        val firebaseDatabase = FirebaseDatabase.getInstance("https://messamfaizanahmed-default-rtdb.asia-southeast1.firebasedatabase.app")
        userRepository = UserRepository(firebaseAuth, firebaseDatabase)

        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _isLoadingProfile.postValue(true)
            Log.d("EditProfileViewModel", "loadCurrentUser: Starting to collect from getCurrentUser()")

            // Use collectLatest to ensure that if a new user logs in while collection is active,
            // it cancels the previous collection and starts a new one.
            // For this specific ViewModel instance tied to one user session, simple collect is also fine.
            userRepository.getCurrentUser().collectLatest { user ->
                if (user != null) {
                    _currentUserProfile.postValue(user)
                    _isLoadingProfile.postValue(false)
                    Log.d("EditProfileViewModel", "loadCurrentUser: User data collected: $user")
                } else {
                    // User is null from Room, attempt a fetch
                    Log.d("EditProfileViewModel", "loadCurrentUser: User is null from Room. Attempting fetch.")
                    firebaseAuth.currentUser?.uid?.let { userIdToFetch ->
                        // Perform fetch in a separate job but ensure isLoadingProfile is handled correctly
                        viewModelScope.launch(Dispatchers.IO) { // Fetch on IO thread
                            try {
                                userRepository.getUserById(userIdToFetch)
                                Log.d("EditProfileViewModel", "loadCurrentUser: fetchAndSaveUser completed for $userIdToFetch.")
                                // After successful fetch, the original getCurrentUser().collectLatest
                                // should emit the new user from Room, which will then set
                                // _currentUserProfile and _isLoadingProfile to false.
                                // If fetchAndSaveUser succeeds but getCurrentUser still emits null shortly after,
                                // it means the Room update isn't propagating quickly enough or there's an issue.
                                // For now, we rely on the outer collect to handle the final state.
                            } catch (e: Exception) {
                                Log.e("EditProfileViewModel", "loadCurrentUser: Error in fetchAndSaveUser", e)
                                // If fetch fails, ensure loading is stopped and profile is null.
                                // The outer collect might still emit null again, which is fine.
                                _currentUserProfile.postValue(null) // Explicitly set to null on fetch error
                                _isLoadingProfile.postValue(false)  // Critical: set loading to false
                            }
                        }
                    } ?: run {
                        // No authenticated user, so no profile to fetch.
                        Log.d("EditProfileViewModel", "loadCurrentUser: No authenticated user ID. Setting profile to null and loading to false.")
                        _currentUserProfile.postValue(null)
                        _isLoadingProfile.postValue(false)
                    }
                }
            }
            // This line might be reached if the flow itself completes, which is rare for Room flows.
            // It's a safeguard. The primary path to set _isLoadingProfile to false is within the collect block.
            if (_isLoadingProfile.value == true) {
                Log.d("EditProfileViewModel", "loadCurrentUser: Flow collection ended (unexpectedly?) while still loading. Forcing loading to false.")
                _isLoadingProfile.postValue(false)
            }
        }
    }

    fun saveUserProfileWithImage(userDetails: User, imageBitmap: Bitmap?) {
        Log.d("EditProfileViewModel", "saveUserProfileWithImage called with user: $userDetails, hasBitmap: ${imageBitmap != null}")
        _saveStatus.postValue(SaveStatus.Loading) // Use postValue if called from non-main thread, value if from main
        _isLoadingProfile.postValue(true) // Indicate loading during save

        viewModelScope.launch {
            Log.d("EditProfileViewModel", "saveUserProfileWithImage: Coroutine launched")
            try {
                userRepository.saveUserProfileDetails(userDetails, imageBitmap)
                Log.d("EditProfileViewModel", "userRepository.saveUserWithProfileImage called successfully")
                _saveStatus.postValue(SaveStatus.Success("Profile updated successfully!"))
                // After successful save, trigger a refresh of the current user data
                // This isn't strictly necessary if Room Flow emits automatically, but can ensure freshness.
                // loadCurrentUser() // Or rely on Room's Flow emission. For now, let Room handle it.
            } catch (e: Exception) {
                Log.e("EditProfileViewModel", "saveUserProfileWithImage: Save failed", e)
                val errorMessage = if (e is kotlinx.coroutines.CancellationException) {
                    "Save operation was cancelled. Please try again."
                } else {
                    "Failed to save profile: ${e.localizedMessage ?: "Unknown error"}"
                }
                _saveStatus.postValue(SaveStatus.Error(errorMessage))
            } finally {
                _isLoadingProfile.postValue(false) // Ensure loading state is reset after save attempt
                Log.d("EditProfileViewModel", "saveUserProfileWithImage: Finally block, isLoadingProfile set to false.")
            }
        }
    }

    sealed class SaveStatus {
        object Idle : SaveStatus()
        object Loading : SaveStatus()
        data class Success(val message: String) : SaveStatus()
        data class Error(val message: String) : SaveStatus()
    }
}