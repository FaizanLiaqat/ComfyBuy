package com.muhammadahmedmufii.comfybuy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException // Import the specific exception
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.muhammadahmedmufii.comfybuy.data.local.AppDatabase // Import AppDatabase
import com.muhammadahmedmufii.comfybuy.data.repository.UserRepository // Import UserRepository
import kotlinx.coroutines.CoroutineScope // Import CoroutineScope
import kotlinx.coroutines.Dispatchers // Import Dispatchers
import kotlinx.coroutines.launch // Import launch
import kotlinx.coroutines.withContext // Import withContext
import java.util.regex.Pattern

class signup : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var userRepository: UserRepository // Declare UserRepository

    private lateinit var fullNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var createAccountButton: Button
    private lateinit var loginText: TextView
    private lateinit var googleSignInButton: ImageView

    // Google Sign-In variables
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    // Coroutine scope for launching suspend functions
    private val activityScope = CoroutineScope(Dispatchers.Main)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore

        // Initialize UserRepository (Manual dependency injection for now)
        val database = AppDatabase.getDatabase(applicationContext)
        val userDao = database.userDao()
        val firebaseDatabase = FirebaseDatabase.getInstance() // Get RTDB instance

        // Pass RTDB instance to UserRepository

        userRepository = UserRepository(userDao, auth, db,firebaseDatabase)


        initViews()
        setupGoogleSignIn()
        setupClickListeners()
    }

    // Override onDestroy to cancel the coroutine scope
    override fun onDestroy() {
        super.onDestroy()
        // activityScope.cancel() // Consider cancelling the scope to prevent leaks
    }


    private fun initViews() {
        fullNameEditText = findViewById(R.id.fullName)
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        confirmPasswordEditText = findViewById(R.id.confirmPassword)
        createAccountButton = findViewById(R.id.createAccountButton)
        loginText = findViewById(R.id.loginText)
        googleSignInButton = findViewById(R.id.googleSignInButton)
    }

    private fun setupGoogleSignIn() {
        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize ActivityResultLauncher for Google Sign-In
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d("GoogleSignIn", "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Log.w("GoogleSignIn", "Google sign in failed", e)
                    Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d("GoogleSignIn", "Google sign in cancelled")
                Toast.makeText(this, "Google sign in cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        loginText.setOnClickListener {

            val intent = Intent(this, login::class.java)
            startActivity(intent)
            finish()
        }

        createAccountButton.setOnClickListener {
            performSignup()
        }

        // Set click listener for Google Sign-In button
        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }
        // TODO: Add click listeners for Apple and Facebook buttons if implementing those
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("GoogleSignIn", "signInWithCredential successful")
                    val user = auth.currentUser

                    user?.let { firebaseUser ->
                        // Check if new user and save data (Firestore part)
                        checkIfNewUserAndSaveData(firebaseUser)

                        // *** NEW: Trigger data fetch from Firestore to Room after successful auth ***
                        activityScope.launch {
                            try {
                                Log.d("Signup", "Triggering fetchAndSaveUser for Google user: ${firebaseUser.uid}")
                                userRepository.fetchAndSaveUser(firebaseUser.uid)
                                Log.d("Signup", "fetchAndSaveUser completed for Google user.")
                            } catch (e: Exception) {
                                Log.e("Signup", "Error fetching and saving user data after Google sign-in", e)
                            }
                        }
                    }

                    Toast.makeText(baseContext, "Google Sign-In successful.",
                        Toast.LENGTH_SHORT).show()

                    // Navigate to MainActivity - TODO: Update this to navigate to MainActivity
                    val intent = Intent(this, MainActivity::class.java) // Change this to MainActivity
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()

                } else {
                    Log.w("GoogleSignIn", "signInWithCredential failed", task.exception)
                    Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    // This function saves data to Firestore *if* the document doesn't exist.
    // It's called after both email/password and Google sign-up/in.
    // The fetchAndSaveUser call in the auth success callbacks ensures it gets synced to Room.
    private fun checkIfNewUserAndSaveData(firebaseUser: com.google.firebase.auth.FirebaseUser) {
        // Check Firestore for existing user data
        db.collection("users").document(firebaseUser.uid).get()
            .addOnSuccessListener { documentSnapshot ->
                if (!documentSnapshot.exists()) {
                    Log.d("Firestore", "User data does not exist in Firestore for UID: ${firebaseUser.uid}, saving basic data.")
                    // User data does not exist in Firestore, save it
                    val userMap = hashMapOf(
                        "userId" to firebaseUser.uid,
                        "fullName" to firebaseUser.displayName,
                        "email" to firebaseUser.email,
                        "profileImageUrl" to firebaseUser.photoUrl?.toString(),
                        "username" to null,
                        "bio" to null,
                        "location" to null,
                        "timestamp" to System.currentTimeMillis() // CRITICAL: Add timestamp
                    )

                    db.collection("users").document(firebaseUser.uid).set(userMap)
                        .addOnSuccessListener {
                            Log.d("Firestore", "New Google user data successfully written to Firestore!")
                            // No need to call fetchAndSaveUser here again, it's called in the auth success callback
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error writing new Google user data to Firestore", e)
                            // TODO: Decide how to handle this error - maybe log it and allow login anyway?
                        }
                } else {
                    Log.d("Firestore", "User data already exists in Firestore for UID: ${firebaseUser.uid}")
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error checking for existing user data in Firestore", e)
                // TODO: Decide how to handle this error - maybe log it and allow login anyway?
            }
    }


    private fun performSignup() {
        val fullName = fullNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        // Basic Client-side Validation
        if (fullName.isEmpty()) {
            fullNameEditText.error = "Full Name is required"
            fullNameEditText.requestFocus()
            return
        }

        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            emailEditText.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Enter a valid email address"
            emailEditText.requestFocus()
            return
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            passwordEditText.requestFocus()
            return
        }

        // Assuming isValidPassword checks for at least 8 characters, 1 uppercase, 1 number
        if (!isValidPassword(password)) {
            passwordEditText.error = "Password must be at least 8 characters long with 1 uppercase letter and 1 number"
            passwordEditText.requestFocus()
            return
        }


        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.error = "Confirm Password is required"
            confirmPasswordEditText.requestFocus()
            return
        }

        if (password != confirmPassword) {
            confirmPasswordEditText.error = "Passwords do not match"
            confirmPasswordEditText.requestFocus()
            return
        }

        // Create user with Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Signup success
                    Log.d("Signup", "createUserWithEmail:success")
                    val user = auth.currentUser

                    user?.let { firebaseUser ->
                        // Save additional user data (like fullName) to Firestore
                        saveUserDataToFirestore(firebaseUser.uid, fullName, email)

                        // *** NEW: Trigger data fetch from Firestore to Room after successful auth ***
                        activityScope.launch {
                            try {
                                userRepository.clearUserProfileImageFromRtdb(firebaseUser.uid)
                                Log.d("Signup", "Triggering fetchAndSaveUser for email/password user: ${firebaseUser.uid}")
                                userRepository.fetchAndSaveUser(firebaseUser.uid)
                                Log.d("Signup", "fetchAndSaveUser completed for email/password user.")
                            } catch (e: Exception) {
                                Log.e("Signup", "Error fetching and saving user data after email/password signup", e)
                            }
                        }
                    }

                } else {
                    // If signup fails, display a message to the user.
                    Log.w("Signup", "createUserWithEmail:failure", task.exception)

                    val errorMessage = when (task.exception) {
                        is FirebaseAuthUserCollisionException -> {
                            // Handle the case where the email is already in use
                            "This email is already registered. Please log in instead."
                        }
                        // You can add checks for other specific Firebase Auth exceptions here
                        // is FirebaseAuthWeakPasswordException -> "Password is too weak."
                        // is FirebaseAuthInvalidCredentialsException -> "Invalid email address."
                        else -> {
                            // Handle other general authentication failures
                            "Authentication failed: ${task.exception?.message}"
                        }
                    }

                    Toast.makeText(baseContext, errorMessage, Toast.LENGTH_LONG).show() // Use LENGTH_LONG for better visibility
                }
            }
    }

    // Save user data to Firestore (reusing this function)
    // Note: This function is called after successful Firebase Auth creation
    // The fetchAndSaveUser call in the auth success callbacks ensures it gets synced to Room.
    private fun saveUserDataToFirestore(userId: String, fullName: String?, email: String?) {
        // Create a new user map or data class object
        val user = hashMapOf(
            "userId" to userId,
            "fullName" to fullName,
            "email" to email,
            "username" to null, // Add default null values for new fields
            "bio" to null,
            "location" to null
            // Add any other default fields for a new user
        )

        // Add a new document with the Firebase Auth UID as the document ID
        db.collection("users")
            .document(userId) // Use the Firebase Auth UID as the document ID
            .set(user)
            .addOnSuccessListener {
                Log.d("Firestore", "User data successfully written!")
                Toast.makeText(baseContext, "Account created and data saved.",
                    Toast.LENGTH_SHORT).show()

                // Navigate to MainActivity after successful data saving - TODO: Update this to navigate to MainActivity
                val intent = Intent(this, MainActivity::class.java) // Change this to MainActivity
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish() // Close SignupActivity
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error writing user data", e)
                // Handle the error - maybe log out the Firebase Auth user if data saving fails?
                Toast.makeText(baseContext, "Account created but failed to save data.",
                    Toast.LENGTH_LONG).show() // Use LENGTH_LONG

                // TODO: Decide navigation logic here. If saving data is critical, you might
                // want to prevent proceeding or offer a retry. For now, navigating to login
                // might be safer if the user profile data is essential for the app.
                val intent = Intent(this, login::class.java) // Or MainActivity if login is a fragment
                startActivity(intent)
                finish()
            }
    }

    // Basic password validation based on hint
    private fun isValidPassword(password: String): Boolean {
        // Checks for at least 8 characters, at least one uppercase letter, and at least one digit
        val passwordPattern = Pattern.compile("^(?=.*[A-Z])(?=.*[0-9]).{8,}$")
        return passwordPattern.matcher(password).matches()
    }
}
