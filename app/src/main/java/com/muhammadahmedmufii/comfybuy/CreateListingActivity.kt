package com.muhammadahmedmufii.comfybuy

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.muhammadahmedmufii.comfybuy.ui.createlisting.CreateListingViewModel
import com.muhammadahmedmufii.comfybuy.ui.createlisting.CreateListingViewModelFactory
import com.muhammadahmedmufii.comfybuy.ui.createlisting.ListingSaveStatus
import java.io.IOException

class CreateListingActivity : AppCompatActivity() {
    private val TAG = "CreateListingActivity"
    private lateinit var viewModel: CreateListingViewModel

    private lateinit var addPhotosArea: FrameLayout // Changed to FrameLayout for consistency
    private lateinit var selectedScroll: HorizontalScrollView
    private lateinit var selectedContainer: LinearLayout
    private lateinit var inputTitle: EditText
    private lateinit var tvCategory: TextView // Assuming this will display selected category
    private lateinit var tvCondition: TextView // Assuming this will display selected condition
    private lateinit var inputPrice: EditText
    private lateinit var inputDescription: EditText
    private lateinit var btnPost: Button
    private lateinit var btnClose: ImageView

    // We'll store URIs first, then convert the primary one to Bitmap before saving
    private val selectedImageUris = mutableListOf<Uri>()
    private val selectedBitmaps = mutableListOf<Bitmap>()

    private lateinit var pickImagesLauncher: ActivityResultLauncher<Intent>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri?> // For camera (optional)

    // Placeholder for camera URI
    // private var cameraImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_listing)

        val factory = CreateListingViewModelFactory(application)
        viewModel = ViewModelProvider(this, factory).get(CreateListingViewModel::class.java)

        initViews()
        setupImagePickers()
        setupClickListeners()
        observeViewModel()
    }

    private fun initViews() {
        addPhotosArea = findViewById(R.id.addPhotosArea)
        selectedScroll = findViewById(R.id.selectedScroll)
        selectedContainer = findViewById(R.id.selectedContainer)
        inputTitle = findViewById(R.id.inputTitle)
        tvCategory = findViewById(R.id.tvCategory) // Make sure this ID exists
        tvCondition = findViewById(R.id.tvCondition) // Make sure this ID exists
        inputPrice = findViewById(R.id.inputPrice)
        inputDescription = findViewById(R.id.inputDescription)
        btnPost = findViewById(R.id.btnPost)
        btnClose = findViewById(R.id.btnClose)
    }

    private fun setupImagePickers() {
        pickImagesLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    selectedImageUris.clear() // Or allow appending up to a limit
                    selectedBitmaps.clear()
                    var conversionFailed = false
                    if (data?.clipData != null) { // Multiple images selected
                        val clipData = data.clipData!!
                        val count = clipData.itemCount.coerceAtMost(8) // Limit to 8
                        Log.d(TAG, "Multiple images selected. Count: $count")
                        for (i in 0 until count) {
                            val uri = clipData.getItemAt(i).uri
                            selectedImageUris.add(uri)
                            uriToBitmap(uri)?.let {
                                selectedBitmaps.add(it)
                                Log.d(TAG, "Converted URI to Bitmap: $uri -> Success")
                            } ?: run {
                                Log.e(TAG, "Failed to convert URI to Bitmap: $uri")
                                conversionFailed = true
                            }
                        }
                    } else if (data?.data != null) { // Single image selected

                        val uri = data.data!!
                        Log.e(TAG, "Failed to convert URI to Bitmap: $uri")
                        selectedImageUris.add(uri)
                        uriToBitmap(uri)?.let {
                            selectedBitmaps.add(it)
                            Log.d(TAG, "Converted URI to Bitmap: $uri -> Success")
                        } ?: run {
                            Log.e(TAG, "Failed to convert URI to Bitmap: $uri")
                            conversionFailed = true
                        }
                    }
                    updateThumbnailsUi()
                    if (conversionFailed) {
                        Toast.makeText(this, "Some images failed to load.", Toast.LENGTH_SHORT).show()
                    }
                    Log.d(TAG, "Final selectedBitmaps count: ${selectedBitmaps.size}")

                }else {
                    Log.d(TAG, "Image selection cancelled or failed.")
                }
            }

        // Optional: Camera Launcher Setup
        // val tempFile = File(filesDir, "temp_image.jpg")
        // cameraImageUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", tempFile)
        // takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        //    if (success && cameraImageUri != null) {
        //        selectedImageUris.add(cameraImageUri!!) // Add to list or handle as primary
        //        updateThumbnails()
        //        try { primaryImageBitmap = uriToBitmap(cameraImageUri!!) } catch (e:IOException) { /* ... */ }
        //    }
        // }
    }


    private fun updateThumbnailsUi() {
        selectedContainer.removeAllViews()
        if (selectedBitmaps.isNotEmpty()) { // Use selectedBitmaps for UI
            selectedScroll.visibility = View.VISIBLE
            selectedBitmaps.forEachIndexed { index, bitmap ->
                addThumbnailView(bitmap, index) // Pass Bitmap
            }
        } else {
            selectedScroll.visibility = View.GONE
        }
    }

    private fun addThumbnailView(bitmap: Bitmap, index: Int) { // Takes Bitmap
        val thumbView = LayoutInflater.from(this)
            .inflate(R.layout.item_selected_photo, selectedContainer, false)
        val iv = thumbView.findViewById<ImageView>(R.id.selectedImage)
        val btnX = thumbView.findViewById<ImageView>(R.id.btnRemove)

        Glide.with(this).load(bitmap).centerCrop().into(iv) // Load Bitmap

        btnX.setOnClickListener {
            if (index < selectedBitmaps.size) {
                selectedBitmaps.removeAt(index)
                if (index < selectedImageUris.size) selectedImageUris.removeAt(index) // Keep URI list in sync
            }
            updateThumbnailsUi()
        }
        selectedContainer.addView(thumbView)
    }


    private fun uriToBitmap(uri: Uri): Bitmap? {
        Log.d(TAG, "uriToBitmap: Attempting to convert URI: $uri")
        return try {
            val originalBitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(this.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.setTargetSize(1024, 1024)
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            }
            Log.d(TAG, "uriToBitmap: Conversion successful for URI: $uri, Bitmap: ${originalBitmap.width}x${originalBitmap.height}")
            originalBitmap
        } catch (e: Exception) {
            Log.e(TAG, "uriToBitmap: Error converting URI: $uri", e)
            e.printStackTrace()
            null
        }
    }

    private fun setupClickListeners() {
        btnClose.setOnClickListener { finish() }

        addPhotosArea.setOnClickListener {
            showImageSourceDialog()
        }

        findViewById<LinearLayout>(R.id.pickCategory).setOnClickListener {
            // TODO: Implement category selection dialog (e.g., AlertDialog with list)
            // For now, let's just allow text input or a placeholder
            showInputDialog("Category", tvCategory)
        }

        findViewById<LinearLayout>(R.id.pickCondition).setOnClickListener {
            // TODO: Implement condition selection dialog
            showInputDialog("Condition", tvCondition)
        }

        btnPost.setOnClickListener {
            postListing()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Choose from Gallery", "Take Photo") // Add "Take Photo"
        AlertDialog.Builder(this)
            .setTitle("Add Photo")
            .setItems(options) { dialog, which ->
                if (which == 0) { // Gallery
                    val intent = Intent(Intent.ACTION_PICK).apply {
                        type = "image/*"
                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    }
                    pickImagesLauncher.launch(Intent.createChooser(intent, "Select Pictures (up to 8)"))
                } else { // Camera
                    // takePictureLauncher.launch(cameraImageUri) // Uncomment if camera implemented
                    Toast.makeText(this, "Camera feature coming soon", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun showInputDialog(title: String, targetTextView: TextView) {
        val editText = EditText(this)
        editText.hint = "Enter $title"
        if (targetTextView.text.toString() != "Select $title" && targetTextView.text.isNotBlank()) {
            editText.setText(targetTextView.text)
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(editText)
            .setPositiveButton("OK") { dialog, _ ->
                targetTextView.text = editText.text.toString().trim().ifEmpty { "Select $title" }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun postListing() {
        val title = inputTitle.text.toString().trim()
        val category = tvCategory.text.toString().takeIf { it != "Select category" && it.isNotBlank() } ?: "Uncategorized"
        val condition = tvCondition.text.toString().takeIf { it != "Select condition" && it.isNotBlank() } ?: "Not Specified"
        val price = inputPrice.text.toString().trim()
        val description = inputDescription.text.toString().trim()
        val location = "User Location" // TODO: Get actual location

        if (title.isEmpty() || category.isEmpty() || price.isEmpty() || selectedBitmaps.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and add at least one photo.", Toast.LENGTH_LONG).show()
            Log.w(TAG, "Post validation failed: Title=$title, Category=$category, Price=$price, Bitmaps=${selectedBitmaps.size}")
            return
        }

        Log.i(TAG, "postListing: Posting with Title: $title, Category: $category, Condition: $condition, Price: $price, Desc: $description, Location: $location, Images: ${selectedBitmaps.size}")

        viewModel.createListing(title, category, condition, price, description, location, selectedBitmaps) // Pass list of bitmaps
    }

    private fun observeViewModel() {
        viewModel.saveStatus.observe(this) { status ->
            Log.d(TAG, "SaveStatus observed: $status")
            when (status) {
                is ListingSaveStatus.Loading -> {
                    btnPost.isEnabled = false
                    btnPost.text = "Posting..."
                    // TODO: Show a more prominent loading indicator
                }
                is ListingSaveStatus.Success -> {
                    btnPost.isEnabled = true
                    btnPost.text = "Post Listing"
                    Toast.makeText(this, status.message, Toast.LENGTH_LONG).show()
                    // Clear fields or finish activity
                    finish() // Finish after successful post
                }
                is ListingSaveStatus.Error -> {
                    btnPost.isEnabled = true
                    btnPost.text = "Post Listing"
                    Toast.makeText(this, status.message, Toast.LENGTH_LONG).show()
                }
                is ListingSaveStatus.Idle -> {
                    btnPost.isEnabled = true
                    btnPost.text = "Post Listing"
                }
            }
        }
    }
}