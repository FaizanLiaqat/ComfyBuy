package com.muhammadahmedmufii.comfybuy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
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


class login: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var userRepository: UserRepository // Declare UserRepository

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signupText: TextView
    private lateinit var forgotPasswordText: TextView
    private lateinit var googleSignInButton: ImageView

    // Google Sign-In variables
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>


    // Coroutine scope for launching suspend functions
    private val activityScope = CoroutineScope(Dispatchers.Main)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
//        db = Firebase.firestore

//        // Initialize UserRepository (Manual dependency injection for now)
//        val database = AppDatabase.getDatabase(applicationContext)
//        val userDao = database.userDao()
        val firebaseDatabase = FirebaseDatabase.getInstance() // Get RTDB instance

        userRepository = UserRepository( auth,  firebaseDatabase)


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
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)
        signupText = findViewById(R.id.signupText)
        forgotPasswordText = findViewById(R.id.forgotPassword)
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
        signupText.setOnClickListener {
            val intent = Intent(this, signup::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            performLogin()
        }

        forgotPasswordText.setOnClickListener {
            showForgotPasswordDialog()
        }

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
                        activityScope.launch {
                            try {
                                // This will now ensure user exists in RTDB
                                userRepository.fetchUserAndEnsureExistsInRtdb(
                                    firebaseUser.uid,
                                    firebaseUser.email,
                                    firebaseUser.displayName
                                )
                                Log.d("Login", "fetchUserAndEnsureExistsInRtdb completed for Google user.")
                                // No separate fetchAndSaveUser to Room needed now
                            } catch (e: Exception){
                                Log.e("Login", "Error fetching and saving user data after Google sign-in", e)
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
//    private fun checkIfNewUserAndSaveData(firebaseUser: com.google.firebase.auth.FirebaseUser) {
//        // Check Firestore for existing user data
//        db.collection("users").document(firebaseUser.uid).get()
//            .addOnSuccessListener { documentSnapshot ->
//                if (!documentSnapshot.exists()) {
//                    Log.d("Firestore", "User data does not exist in Firestore for UID: ${firebaseUser.uid}, saving basic data.")
//                    // User data does not exist in Firestore, save it
//                    val userMap = hashMapOf(
//                        "userId" to firebaseUser.uid,
//                        "fullName" to firebaseUser.displayName,
//                        "email" to firebaseUser.email,
//                        "username" to null,
//                        "bio" to null,
//                        "location" to null,
//                        "timestamp" to System.currentTimeMillis() // CRITICAL: Add timestamp
//                    )
//
//                    db.collection("users").document(firebaseUser.uid).set(userMap)
//                        .addOnSuccessListener {
//                            Log.d("Firestore", "New Google user data successfully written to Firestore!")
//                            // No need to call fetchAndSaveUser here again, it's called in the auth success callback
//                        }
//                        .addOnFailureListener { e ->
//                            Log.w("Firestore", "Error writing new Google user data to Firestore", e)
//                            // TODO: Decide how to handle this error - maybe log it and allow login anyway?
//                        }
//                } else {
//                    Log.d("Firestore", "User data already exists in Firestore for UID: ${firebaseUser.uid}")
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.w("Firestore", "Error checking for existing user data in Firestore", e)
//                // TODO: Decide how to handle this error - maybe log it and allow login anyway?
//            }
//    }


    private fun performLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        // Basic Validation
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

        // Sign in user with Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("Login", "signInWithEmail:success")
                    val user = auth.currentUser
                    user?.let { firebaseUser ->
                        activityScope.launch {
                            try {
                                userRepository.fetchUserAndEnsureExistsInRtdb(
                                    firebaseUser.uid,
                                    firebaseUser.email,
                                    firebaseUser.displayName // For email/pass, displayName might be null initially
                                )
                                Log.d("Login", "fetchUserAndEnsureExistsInRtdb completed for email/password user.")
                            } catch (e: Exception) {
                                Log.e("Login", "Error fetching and saving user data after email/password sign-in", e)
                            }
                        }
                    }

                    Toast.makeText(baseContext, "Authentication successful.",
                        Toast.LENGTH_SHORT).show()

                    // Navigate to MainActivity after successful login
                    val intent = Intent(this, MainActivity::class.java) // *** CHANGE THIS ***
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    Log.w("Login", "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Reset Password")

        val input = EditText(this)
        input.hint = "Enter your email"
        input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        val container = LinearLayout(this)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(50, 0, 50, 0)
        container.addView(input, params)
        builder.setView(container)

        builder.setPositiveButton("Send Reset Link") { dialog, which ->
            val email = input.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address.", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password reset email sent to $email.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to send reset email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }

        builder.show()
    }
}
