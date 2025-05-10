package com.muhammadahmedmufii.comfybuy // Use your main package name

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar // Import ProgressBar if you add one
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.muhammadahmedmufii.comfybuy.domain.model.User
import com.muhammadahmedmufii.comfybuy.ui.editprofile.EditProfileViewModel
import com.muhammadahmedmufii.comfybuy.ui.editprofile.EditProfileViewModelFactory
import de.hdodenhof.circleimageview.CircleImageView
import com.google.firebase.auth.FirebaseAuth
import java.io.IOException

class EditProfileActivity : AppCompatActivity() {

    private lateinit var editProfileViewModel: EditProfileViewModel
    private lateinit var profilePhoto: CircleImageView
    private lateinit var cameraIcon: ImageView
    private lateinit var changePhotoText: TextView
    private lateinit var nameEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var bioEditText: EditText
    private lateinit var locationEditText: EditText
    private lateinit var saveChangesButton: CardView
    private lateinit var saveChangesButtonText: TextView
    // Optional: private lateinit var loadingProgressBar: ProgressBar


    private var selectedImageBitmap: Bitmap? = null
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val factory = EditProfileViewModelFactory(application)
        editProfileViewModel = ViewModelProvider(this, factory).get(EditProfileViewModel::class.java)
        Log.d("EditProfileActivity", "onCreate: ViewModel initialized")

        initViews()
        setupImagePicker()
        setupClickListeners()
        observeViewModel()
    }

    private fun initViews() {
        profilePhoto = findViewById(R.id.ivProfilePhoto)
        cameraIcon = findViewById(R.id.ivCameraIcon)
        changePhotoText = findViewById(R.id.tvChangePhoto)
        nameEditText = findViewById(R.id.etName)
        usernameEditText = findViewById(R.id.etUsername)
        bioEditText = findViewById(R.id.etBio)
        locationEditText = findViewById(R.id.etLocation)
        saveChangesButton = findViewById(R.id.cardSaveChanges)
        saveChangesButtonText = saveChangesButton.findViewById(R.id.tvSaveChangesText)
        // Optional: loadingProgressBar = findViewById(R.id.loadingProgressBar) // Add a ProgressBar to your XML
        Log.d("EditProfileActivity", "initViews: UI elements initialized")
    }

    // setupImagePicker, uriToBitmap, setupClickListeners, openGalleryForImage remain the same

    private fun setupImagePicker() {
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { uri ->
                    try {
                        selectedImageBitmap = uriToBitmap(uri)
                        selectedImageBitmap?.let {
                            profilePhoto.setImageBitmap(it)
                            Log.d("EditProfileActivity", "Image selected and converted to Bitmap.")
                        } ?: run {
                            Log.e("EditProfileActivity", "Failed to convert URI to Bitmap.")
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: IOException) {
                        Log.e("EditProfileActivity", "Error converting URI to Bitmap", e)
                        Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
                    }
                } ?: run {
                    Log.e("EditProfileActivity", "Image selection failed: No data URI.")
                    Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d("EditProfileActivity", "Image selection cancelled.")
            }
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(this.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun setupClickListeners() {
        val photoClickListener = { _: View ->
            openGalleryForImage()
        }

        profilePhoto.setOnClickListener(photoClickListener)
        cameraIcon.setOnClickListener(photoClickListener)
        changePhotoText.setOnClickListener(photoClickListener)

        saveChangesButton.setOnClickListener {
            Log.d("EditProfileActivity", "Save Changes button clicked")
            if (nameEditText.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (usernameEditText.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveProfileChanges()
        }
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun observeViewModel() {
        editProfileViewModel.isLoadingProfile.observe(this) { isLoading ->
            Log.d("EditProfileActivity", "isLoadingProfile changed to: $isLoading")
            // Optional: loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE

            val UIElementsEnabled = !isLoading
            saveChangesButton.isEnabled = UIElementsEnabled
            nameEditText.isEnabled = UIElementsEnabled
            usernameEditText.isEnabled = UIElementsEnabled
            bioEditText.isEnabled = UIElementsEnabled
            locationEditText.isEnabled = UIElementsEnabled
            profilePhoto.isClickable = UIElementsEnabled // Make image clickable only when not loading
            cameraIcon.isClickable = UIElementsEnabled
            changePhotoText.isClickable = UIElementsEnabled


            if (isLoading) {
                Log.d("EditProfileActivity", "UI: Profile is loading...")
                saveChangesButtonText.text = "Loading..." // Also update button text if it's a general load
            } else {
                Log.d("EditProfileActivity", "UI: Profile loading finished.")
                // If saveStatus is not currently 'Loading', reset button text
                if (editProfileViewModel.saveStatus.value !is EditProfileViewModel.SaveStatus.Loading) {
                    saveChangesButtonText.text = "Save Changes"
                }
                // Check current user profile value *after* loading is finished
                if (editProfileViewModel.currentUserProfile.value == null) {
                    Log.w("EditProfileActivity", "UI: Loading finished, but currentUserProfile is null. Clearing fields.")
                    clearUiFields() // Make sure UI is cleared if no data
                    Toast.makeText(this, "Could not load profile details.", Toast.LENGTH_SHORT).show()
                } else {
                    // Data should be populated by the currentUserProfile observer if it's not null
                    Log.d("EditProfileActivity", "UI: Loading finished, currentUserProfile has a value. Populating if needed.")
                    populateUiWithUserData(editProfileViewModel.currentUserProfile.value!!) // Safe call due to check
                }
            }
        }

        editProfileViewModel.currentUserProfile.observe(this) { user ->
            Log.d("EditProfileActivity", "currentUserProfile observer triggered with user: $user")
            // Only populate if not in an initial loading phase (isLoadingProfile observer handles the end of that)
            if (editProfileViewModel.isLoadingProfile.value == false) {
                if (user != null) {
                    populateUiWithUserData(user)
                } else {
                    // This means loading finished and user is still null.
                    Log.d("EditProfileActivity", "currentUserProfile is null and not loading. Clearing fields.")
                    clearUiFields()
                }
            } else {
                Log.d("EditProfileActivity", "currentUserProfile emitted, but UI believes it's still loading. UI update will be handled by isLoadingProfile observer.")
            }
        }

        editProfileViewModel.saveStatus.observe(this) { status ->
            Log.d("EditProfileActivity", "saveStatus observer triggered with status: $status")

            val isSaving = status is EditProfileViewModel.SaveStatus.Loading
            // Enable/disable fields based on whether we are saving
            nameEditText.isEnabled = !isSaving
            usernameEditText.isEnabled = !isSaving
            bioEditText.isEnabled = !isSaving
            locationEditText.isEnabled = !isSaving
            profilePhoto.isClickable = !isSaving
            cameraIcon.isClickable = !isSaving
            changePhotoText.isClickable = !isSaving
            saveChangesButton.isEnabled = !isSaving


            when (status) {
                is EditProfileViewModel.SaveStatus.Idle -> {
                    saveChangesButtonText.text = "Save Changes"
                }
                is EditProfileViewModel.SaveStatus.Loading -> {
                    saveChangesButtonText.text = "Saving..."
                }
                is EditProfileViewModel.SaveStatus.Success -> {
                    Toast.makeText(this, status.message, Toast.LENGTH_SHORT).show()
                    saveChangesButtonText.text = "Saved!"
                    selectedImageBitmap = null
                    finish()
                }
                is EditProfileViewModel.SaveStatus.Error -> {
                    Toast.makeText(this, status.message, Toast.LENGTH_LONG).show()
                    saveChangesButtonText.text = "Save Changes"
                }
            }
        }
    }

    private fun populateUiWithUserData(user: User) {
        Log.d("EditProfileActivity", "Populating UI with user data: ${user.userId}, Name: ${user.fullName}")
        if (selectedImageBitmap == null) {
            user.profileImageBitmap?.let { bitmap ->
                profilePhoto.setImageBitmap(bitmap)
                Log.d("EditProfileActivity", "Set profile image from user.profileImageBitmap")
            } ?: run {
                profilePhoto.setImageResource(R.drawable.ic_person)
                Log.d("EditProfileActivity", "Set default profile image (user.profileImageBitmap was null)")
            }
        } else {
            // If selectedImageBitmap is not null, it's already set by the image picker.
            Log.d("EditProfileActivity", "Retaining user-selected image preview.")
        }
        nameEditText.setText(user.fullName ?: "")
        usernameEditText.setText(user.username ?: "")
        bioEditText.setText(user.bio ?: "")
        locationEditText.setText(user.location ?: "")
    }

    private fun clearUiFields() {
        Log.d("EditProfileActivity", "Clearing UI fields.")
        if (selectedImageBitmap == null) { // Only clear image if user hasn't just picked one
            profilePhoto.setImageResource(R.drawable.ic_person)
        }
        nameEditText.setText("")
        usernameEditText.setText("")
        bioEditText.setText("")
        locationEditText.setText("")
    }

    // saveProfileChanges remains the same
    private fun saveProfileChanges() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("EditProfileActivity", "saveProfileChanges: currentUserId = $currentUserId")

        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            Log.e("EditProfileActivity", "saveProfileChanges: User not logged in, cannot save.")
            return
        }

        val updatedName = nameEditText.text.toString().trim()
        val updatedUsername = usernameEditText.text.toString().trim()
        val updatedBio = bioEditText.text.toString().trim()
        val updatedLocation = locationEditText.text.toString().trim()

        Log.d("EditProfileActivity", "saveProfileChanges: Updated data - Name: $updatedName, Username: $updatedUsername, Bio: $updatedBio, Location: $updatedLocation")

        val currentUserDomain = editProfileViewModel.currentUserProfile.value
        Log.d("EditProfileActivity", "saveProfileChanges: currentUser from ViewModel = $currentUserDomain")

        val userToUpdate = currentUserDomain ?: User(
            userId = currentUserId,
            fullName = null, email = FirebaseAuth.getInstance().currentUser?.email,
            profileImageBitmap = null,
            username = null, bio = null, location = null
        )

        val updatedUser = userToUpdate.copy(
            fullName = updatedName,
            username = usernameEditText.text.toString().trim(), // Use property directly
            bio = bioEditText.text.toString().trim(),           // Use property directly
            location = locationEditText.text.toString().trim(), // Use property directly
            // profileImageBitmap is NOT set here; ViewModel handles it with selectedImageBitmap
        )
        Log.d("EditProfileActivity", "saveProfileChanges: Created updatedUser domain object: $updatedUser")
        editProfileViewModel.saveUserProfileWithImage(updatedUser, selectedImageBitmap)
        Log.d("EditProfileActivity", "saveProfileChanges: Called viewModel.saveUserProfileWithImage")
    }
}