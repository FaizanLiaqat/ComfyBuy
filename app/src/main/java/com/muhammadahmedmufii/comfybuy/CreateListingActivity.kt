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
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
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
import java.util.UUID

class CreateListingActivity : AppCompatActivity() {
    private val TAG = "CreateListingActivity"
    private lateinit var viewModel: CreateListingViewModel

    private lateinit var addPhotosArea: FrameLayout
    private lateinit var selectedScroll: HorizontalScrollView
    private lateinit var selectedContainer: LinearLayout
    private lateinit var inputTitle: EditText
    private lateinit var inputPrice: EditText
    private lateinit var inputDescription: EditText
    private lateinit var btnPost: Button
    private lateinit var btnClose: ImageView
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerCondition: Spinner
    private lateinit var location: EditText

    private val selectedImageUris = mutableListOf<Uri>()
    private val selectedBitmaps = mutableListOf<Bitmap>()

    private lateinit var pickImagesLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_listing)

        val factory = CreateListingViewModelFactory(application)
        viewModel = ViewModelProvider(this, factory).get(CreateListingViewModel::class.java)

        initViews()
        setupSpinners()
        setupImagePickers()
        setupClickListeners()
        observeViewModel()
    }

    private fun initViews() {
        addPhotosArea = findViewById(R.id.addPhotosArea)
        selectedScroll = findViewById(R.id.selectedScroll)
        selectedContainer = findViewById(R.id.selectedContainer)
        inputTitle = findViewById(R.id.inputTitle)
        inputPrice = findViewById(R.id.inputPrice)
        inputDescription = findViewById(R.id.inputDescription)
        btnPost = findViewById(R.id.btnPost)
        btnClose = findViewById(R.id.btnClose)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerCondition = findViewById(R.id.spinnerCondition)
        location = findViewById(R.id.location)
    }

    private fun setupSpinners() {
        // Category
        ArrayAdapter.createFromResource(
            this,
            R.array.categories,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = adapter
        }

        // Condition
        ArrayAdapter.createFromResource(
            this,
            R.array.conditions,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCondition.adapter = adapter
        }
    }

    private fun postListing() {
        val title = inputTitle.text.toString().trim()
        val price = inputPrice.text.toString().trim()
        val catPos = spinnerCategory.selectedItemPosition
        val condPos = spinnerCondition.selectedItemPosition

        if (title.isEmpty() || price.isEmpty() || selectedBitmaps.isEmpty()
            || catPos == 0 || condPos == 0) {
            Toast.makeText(this,
                "Please fill all fields, select category & condition, and add at least one photo.",
                Toast.LENGTH_LONG).show()
            return
        }

        val category = spinnerCategory.selectedItem.toString()
        val condition = spinnerCondition.selectedItem.toString()
        val description = inputDescription.text.toString().trim()
        val location = location.text.toString().trim()

        viewModel.createListing(
            title, category, condition, price, description, location, selectedBitmaps
        )
    }
    private fun setupImagePickers() {
        pickImagesLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    var conversionFailed = false

                    // Start index = how many we already have
                    val already = selectedImageUris.size

                    data?.clipData?.let { clip ->
                        // total allowed is 8
                        val spaceLeft = (8 - already).coerceAtLeast(0)
                        val count = clip.itemCount.coerceAtMost(spaceLeft)
                        for (i in 0 until count) {
                            val uri = clip.getItemAt(i).uri
                            selectedImageUris.add(uri)
                            uriToBitmap(uri)?.let { bmp ->
                                selectedBitmaps.add(bmp)
                            } ?: run {
                                conversionFailed = true
                            }
                        }
                    }
                    // single pick falls back here if clipData is null
                        ?: data?.data?.let { uri ->
                            if (selectedImageUris.size < 8) {
                                selectedImageUris.add(uri)
                                uriToBitmap(uri)?.let { bmp ->
                                    selectedBitmaps.add(bmp)
                                } ?: run {
                                    conversionFailed = true
                                }
                            } else {
                                Toast.makeText(this, "You can only add up to 8 photos.", Toast.LENGTH_SHORT).show()
                            }
                        }

                    updateThumbnailsUi()

                    if (conversionFailed) {
                        Toast.makeText(this, "Some images failed to load.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }


    private fun updateThumbnailsUi() {
        selectedContainer.removeAllViews()
        if (selectedBitmaps.isNotEmpty()) {
            selectedScroll.visibility = View.VISIBLE
            selectedBitmaps.forEachIndexed { index, bitmap -> addThumbnailView(bitmap, index) }
        } else selectedScroll.visibility = View.GONE
    }

    private fun addThumbnailView(bitmap: Bitmap, index: Int) {
        val thumbView = LayoutInflater.from(this)
            .inflate(R.layout.item_selected_photo, selectedContainer, false)

        val iv = thumbView.findViewById<ImageView>(R.id.selectedImage)
        val btnX = thumbView.findViewById<ImageView>(R.id.btnRemove)

        // Directly set the bitmap
        iv.scaleType = ImageView.ScaleType.CENTER_CROP
        iv.setImageBitmap(bitmap)

        btnX.setOnClickListener {
            if (index < selectedBitmaps.size) {
                selectedBitmaps.removeAt(index)
                selectedImageUris.removeAt(index)
                updateThumbnailsUi()
            }
        }

        selectedContainer.addView(thumbView)
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT < 28) MediaStore.Images.Media.getBitmap(contentResolver, uri)
            else {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ -> decoder.setTargetSize(1024, 1024) }
            }
        } catch (e: IOException) { e.printStackTrace(); null }
    }

    private fun setupClickListeners() {
        btnClose.setOnClickListener { finish() }
        addPhotosArea.setOnClickListener { showImageSourceDialog() }
        btnPost.setOnClickListener { postListing() }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Choose from Gallery", "Take Photo")
        AlertDialog.Builder(this)
            .setTitle("Add Photo")
            .setItems(options) { dialog, which ->
                if (which == 0) {
                    // Use ACTION_GET_CONTENT for reliable multiple selection support
                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "image/*"
                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }
                    pickImagesLauncher.launch(
                        Intent.createChooser(intent, "Select Pictures (up to 8)")
                    )
                } else {
                    Toast.makeText(this, "Camera feature coming soon", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .show()
    }


    private fun observeViewModel() {
        viewModel.saveStatus.observe(this) { status ->
            when (status) {
                ListingSaveStatus.Loading -> { btnPost.isEnabled = false; btnPost.text = "Posting..." }
                is ListingSaveStatus.Success -> { Toast.makeText(this, status.message, Toast.LENGTH_LONG).show(); finish() }
                is ListingSaveStatus.Error -> Toast.makeText(this, status.message, Toast.LENGTH_LONG).show()
                else -> Unit
            }
            if (status !is ListingSaveStatus.Loading) {
                btnPost.isEnabled = true
                btnPost.text = "Post Listing"
            }
        }
    }
}