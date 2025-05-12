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
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
// import com.google.firebase.firestore.FirebaseFirestore // REMOVED
import com.muhammadahmedmufii.comfybuy.data.model.RtdbUser // For direct RTDB save
import com.muhammadahmedmufii.comfybuy.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern


class signup : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
//    private lateinit var db: FirebaseFirestore
    private lateinit var userRepository: UserRepository // Declare UserRepository

    private lateinit var fullNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var createAccountButton: Button
    private lateinit var loginText: TextView
    private lateinit var googleSignInButton: ImageView
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private val activityScope = CoroutineScope(Dispatchers.Main)

    private lateinit var usersRtdbRefForSignup: com.google.firebase.database.DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
//        db = Firebase.firestore

        // Initialize UserRepository (Manual dependency injection for now)
//        val database = AppDatabase.getDatabase(applicationContext)
//        val userDao = database.userDao()

        val firebaseDatabase = FirebaseDatabase.getInstance("https://messamfaizanahmed-default-rtdb.asia-southeast1.firebasedatabase.app")
        usersRtdbRefForSignup = firebaseDatabase.getReference("users") // Direct ref for signup write
        userRepository = UserRepository(auth , firebaseDatabase) // DAO and Firestore are null

        // Pass RTDB instance to UserRepository

        initViews()
        setupGoogleSignIn()
        setupClickListeners()
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

    private fun setupGoogleSignIn() { /* ... same as login.kt ... */
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().requestProfile().build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) { Log.w("GoogleSignIn", "Google sign in failed", e); Toast.makeText(this, "Google sign in failed.", Toast.LENGTH_SHORT).show(); }
            } else { Log.d("GoogleSignIn", "Google sign in cancelled by user.") }
        }
    }

    private fun setupClickListeners() {
        loginText.setOnClickListener { startActivity(Intent(this, login::class.java)); finish() }
        createAccountButton.setOnClickListener { performSignup() }
        googleSignInButton.setOnClickListener { signInWithGoogle() }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) { // Same as in login.kt
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let { firebaseUser ->
                        activityScope.launch {
                            // This will create the user in RTDB if they don't exist
                            userRepository.fetchUserAndEnsureExistsInRtdb(
                                firebaseUser.uid,
                                firebaseUser.email,
                                firebaseUser.displayName
                            )
                            Log.d("Signup", "fetchUserAndEnsureExistsInRtdb completed for Google user.")
                            navigateToMain()
                        }
                    } ?: navigateToMain()
                }  else {
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
//                        "profileImageUrl" to firebaseUser.photoUrl?.toString(),
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
                    Log.d("Signup", "createUserWithEmail:success")
                    val user = auth.currentUser
                    user?.let { firebaseUser ->
                        // Save initial user data directly to RTDB
                        saveInitialUserDataToRtdb(firebaseUser.uid, fullName, email)
                    }
                }else {
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
//    private fun saveUserDataToFirestore(userId: String, fullName: String?, email: String?) {
//        // Create a new user map or data class object
//        val user = hashMapOf(
//            "userId" to userId,
//            "fullName" to fullName,
//            "email" to email,
//            "username" to null, // Add default null values for new fields
//            "bio" to null,
//            "location" to null
//            // Add any other default fields for a new user
//        )
//
//        // Add a new document with the Firebase Auth UID as the document ID
//        db.collection("users")
//            .document(userId) // Use the Firebase Auth UID as the document ID
//            .set(user)
//            .addOnSuccessListener {
//                Log.d("Firestore", "User data successfully written!")
//                Toast.makeText(baseContext, "Account created and data saved.",
//                    Toast.LENGTH_SHORT).show()
//
//                // Navigate to MainActivity after successful data saving - TODO: Update this to navigate to MainActivity
//                val intent = Intent(this, MainActivity::class.java) // Change this to MainActivity
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(intent)
//                finish() // Close SignupActivity
//            }
//            .addOnFailureListener { e ->
//                Log.w("Firestore", "Error writing user data", e)
//                // Handle the error - maybe log out the Firebase Auth user if data saving fails?
//                Toast.makeText(baseContext, "Account created but failed to save data.",
//                    Toast.LENGTH_LONG).show() // Use LENGTH_LONG
//
//                // TODO: Decide navigation logic here. If saving data is critical, you might
//                // want to prevent proceeding or offer a retry. For now, navigating to login
//                // might be safer if the user profile data is essential for the app.
//                val intent = Intent(this, login::class.java) // Or MainActivity if login is a fragment
//                startActivity(intent)
//                finish()
//            }
//    }


//    private fun saveUserDataToRtdb(userId: String, fullName: String?, email: String?) {
//        val newUser = RtdbUser( // Use the RtdbUser POJO
//            userId = userId,
//            fullName = fullName,
//            email = email,
//            timestamp = System.currentTimeMillis()
//            // Other fields (username, bio, etc.) will be null by default in RtdbUser
//        )
//        usersRtdbRef.child(userId).setValue(newUser) // usersRtdbRef needs to be accessible or use repo method
//            .addOnSuccessListener {
//                Log.d("Signup", "User data successfully written to RTDB for $userId!")
//                Toast.makeText(baseContext, "Account created.", Toast.LENGTH_SHORT).show()
//
//                // Also ensure this user is "fetched" by UserRepository to populate its internal state if needed by flows immediately
//                activityScope.launch { userRepository.fetchUserAndEnsureExistsInRtdb(userId, email, fullName) }
//
//                val intent = Intent(this, MainActivity::class.java)
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(intent)
//                finish()
//            }
//            .addOnFailureListener { e ->
//                Log.w("Signup", "Error writing user data to RTDB for $userId", e)
//                Toast.makeText(baseContext, "Failed to save user data.", Toast.LENGTH_LONG).show()
//            }
//    }

    private fun saveInitialUserDataToRtdb(userId: String, fullName: String?, email: String?) {
        Log.d("Signup", "saveInitialUserDataToRtdb for userId: $userId")
        val newUser = RtdbUser(
            userId = userId,
            fullName = fullName,
            email = email,
            timestamp = System.currentTimeMillis(),
            profileImageBase64 = null // No image on initial email/pass signup
            // Other fields default to null
        )

        usersRtdbRefForSignup.child(userId).setValue(newUser)
            .addOnSuccessListener {
                Log.i("Signup", "Initial user data successfully written to RTDB for $userId!")
                // No separate fetchAndSaveUser call needed here as RTDB is the source of truth
                // The listeners in ViewModels will pick up this new user.
                navigateToMain()
            }
            .addOnFailureListener { e ->
                Log.w("Signup", "Error writing initial user data to RTDB for $userId", e)
                Toast.makeText(baseContext, "Failed to save user profile data.", Toast.LENGTH_LONG).show()
                // Consider what to do here - user is authenticated but profile save failed.
                // Maybe sign out the user? Or let them proceed and try editing profile later?
                // For now, still navigate to main, but log the error.
                navigateToMain()
            }
    }
    private fun navigateToMain() {
        Toast.makeText(baseContext, "Account process complete.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }


    // Basic password validation based on hint
    private fun isValidPassword(password: String): Boolean {
        // Checks for at least 8 characters, at least one uppercase letter, and at least one digit
        val passwordPattern = Pattern.compile("^(?=.*[A-Z])(?=.*[0-9]).{8,}$")
        return passwordPattern.matcher(password).matches()
    }
}
