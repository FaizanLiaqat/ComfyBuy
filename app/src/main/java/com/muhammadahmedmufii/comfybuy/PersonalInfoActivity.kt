package com.muhammadahmedmufii.comfybuy

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.muhammadahmedmufii.comfybuy.ui.personalinfo.PersonalInfoViewModel
import com.muhammadahmedmufii.comfybuy.ui.personalinfo.PersonalInfoViewModelFactory
import com.muhammadahmedmufii.comfybuy.ui.personalinfo.SaveOperationStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PersonalInfoActivity : AppCompatActivity() {
    private lateinit var viewModel: PersonalInfoViewModel

    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvDob: TextView

    // Assuming you add a Save button to your activity_personal_info.xml
    private lateinit var btnSaveChanges: Button // Example, use your actual ID

    private lateinit var cardFullName: CardView
    private lateinit var cardEmail: CardView
    private lateinit var cardPhone: CardView
    private lateinit var cardGender: CardView
    private lateinit var cardDob: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_personal_info)

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val factory = PersonalInfoViewModelFactory(application)
        viewModel = ViewModelProvider(this, factory).get(PersonalInfoViewModel::class.java)

        initViews()
        setupClickListeners()
        observeViewModel()
    }

    private fun initViews() {
        // These are the TextViews displaying the data
        tvFullName = findViewById(R.id.tvFullName)
        tvEmail = findViewById(R.id.tvEmail)
        tvPhone = findViewById(R.id.tvPhone)
        tvGender = findViewById(R.id.tvGender)
        tvDob = findViewById(R.id.tvDob)

        cardFullName = findViewById(R.id.cardFullName)
        cardEmail = findViewById(R.id.cardEmail)
        cardPhone = findViewById(R.id.cardPhone)
        cardGender = findViewById(R.id.cardGender)
        cardDob = findViewById(R.id.cardDob)

        btnSaveChanges = findViewById(R.id.btnSavePersonalInfo)
    }

    private fun setupClickListeners() {
        findViewById<CardView>(R.id.cardFullName).setOnClickListener { showEditToast("full name (Not editable here)") }
        findViewById<CardView>(R.id.cardEmail).setOnClickListener { showEditToast("email (Not editable here)") }
        findViewById<CardView>(R.id.cardPhone).setOnClickListener {
            showEditPhoneNumberDialog()
        }
        findViewById<CardView>(R.id.cardGender).setOnClickListener { showGenderSelectionDialog() }
        findViewById<CardView>(R.id.cardDob).setOnClickListener { showDatePickerDialog() }

        btnSaveChanges.setOnClickListener {
            savePersonalInformation()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            // TODO: Show/hide loading indicator
            btnSaveChanges.isEnabled = !isLoading
            Log.d("PersonalInfoActivity", "Loading state: $isLoading")
        }

        viewModel.currentUserDetails.observe(this) { user ->
            if (user != null) {
                Log.d("PersonalInfoActivity", "User details observed: $user")
                tvFullName.text = user.fullName ?: "N/A"
                tvEmail.text = user.email ?: "N/A"
                tvPhone.text = user.phoneNumber ?: "Not Set"
                tvGender.text = user.gender ?: "Not Set"
                tvDob.text = user.dateOfBirth ?: "Not Set"
            } else {
                Log.d("PersonalInfoActivity", "User details null. Clearing fields.")
                tvFullName.text = "N/A"
                tvEmail.text = "N/A"
                tvPhone.text = "Not Set"
                tvGender.text = "Not Set"
                tvDob.text = "Not Set"
                // Optionally finish or show error if user data must exist
            }
        }

        viewModel.saveStatus.observe(this) { status ->
            when (status) {
                is SaveOperationStatus.Loading -> {
                    Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show()
                    btnSaveChanges.isEnabled = false
                }
                is SaveOperationStatus.Success -> {
                    Toast.makeText(this, status.message, Toast.LENGTH_SHORT).show()
                    btnSaveChanges.isEnabled = true
                    finish() // Close activity after successful save
                }
                is SaveOperationStatus.Error -> {
                    Toast.makeText(this, status.message, Toast.LENGTH_LONG).show()
                    btnSaveChanges.isEnabled = true
                }
                else -> btnSaveChanges.isEnabled = true
            }
        }
    }


    private fun savePersonalInformation() {
        val currentUser = viewModel.currentUserDetails.value
        if (currentUser == null) {
            Toast.makeText(this, "Cannot save, user data not loaded.", Toast.LENGTH_SHORT).show()
            return
        }

        // Create an updated User object with values from the TextViews
        // Note: FullName and Email are shown but not made editable in this simple example.
        // If they were editable, you'd get values from EditTexts.
        // Phone number editing also needs a proper input method.
        val updatedUser = currentUser.copy(
            // Assuming fullName and email are not changed on this screen
            // If phone was editable via a dialog that updated an EditText:
             phoneNumber = if (tvPhone.text.toString()== "Not Set") null else tvPhone.text.toString(),
            gender = if (tvGender.text.toString() == "Not Set") null else tvGender.text.toString(),
            dateOfBirth = if (tvDob.text.toString() == "Not Set") null else tvDob.text.toString()
        )
        Log.d("PersonalInfoActivity", "Attempting to save: $updatedUser")
        viewModel.updatePersonalInformation(updatedUser)
    }

    private fun showEditToast(field: String) {
        Toast.makeText(this, "Edit $field", Toast.LENGTH_SHORT).show()
    }

    private fun showGenderSelectionDialog() {
        val options = arrayOf("Male", "Female", "Non-binary", "Prefer not to say")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Gender")
            .setItems(options) { dialog, which ->
                // Update the gender TextView with the selected option
                findViewById<android.widget.TextView>(R.id.tvGender).text = options[which]
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()

        // Parse the current date if possible
        try {
            val currentDate = findViewById<android.widget.TextView>(R.id.tvDob).text.toString()
            val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.US)
            val date = sdf.parse(currentDate)
            if (date != null) {
                calendar.time = date
            }
        } catch (e: Exception) {
            // Use current date if parsing fails
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)

                // Format the date
                val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US)
                val formattedDate = dateFormat.format(selectedCalendar.time)

                // Update the TextView
                findViewById<android.widget.TextView>(R.id.tvDob).text = formattedDate
            },
            year,
            month,
            day
        )

        // Set max date to current date (no future dates)
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

        datePickerDialog.show()
    }
    // --- NEW FUNCTION for Phone Number Editing ---
    private fun showEditPhoneNumberDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null)
        val editText = dialogView.findViewById<EditText>(R.id.etDialogInput)
        val currentPhoneNumber = tvPhone.text.toString()

        editText.hint = "Enter phone number"
        editText.inputType = InputType.TYPE_CLASS_PHONE
        if (currentPhoneNumber != "Not Set" && currentPhoneNumber.isNotBlank()) {
            editText.setText(currentPhoneNumber)
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Phone Number")
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ ->
                val newPhoneNumber = editText.text.toString().trim()
                // TODO: Add validation for phone number format if needed
                if (newPhoneNumber.isNotEmpty()) {
                    tvPhone.text = newPhoneNumber // Update TextView immediately
                } else {
                    tvPhone.text = "Not Set" // Or keep old value, or show error
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
