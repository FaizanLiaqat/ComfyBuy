package com.muhammadahmedmufii.comfybuy.ui.profile // Recommended package structure

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View // Import View
import android.view.ViewGroup // Import ViewGroup
import android.widget.LinearLayout // Import LinearLayout
import android.widget.Toast // Import Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment // *** CHANGE: Extend Fragment ***
import com.muhammadahmedmufii.comfybuy.PersonalInfoActivity // Assuming this is an Activity
import com.muhammadahmedmufii.comfybuy.R // Import R
// Import necessary components from your project's profile layout (activity_profile.xml)
import de.hdodenhof.circleimageview.CircleImageView // For ivProfilePic
import android.widget.TextView // For tvUserName, tvUserEmail, btnEditProfile, tvProfileTitle, tvAccountSettings
import androidx.cardview.widget.CardView // For cardUserInfo, cardSettings
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth // Import FirebaseAuth if needed to show current user info
import com.muhammadahmedmufii.comfybuy.EditProfileActivity


// CHANGE: Renamed from 'ProfileActivity' to 'ProfileFragment' and extends Fragment
class ProfileFragment : Fragment() {

    // UI elements from activity_profile.xml that have click listeners or need data
    private lateinit var layoutPersonalInfo: View // The LinearLayout for Personal Info
    // Add other settings layouts if they have click listeners:
    // private lateinit var layoutPaymentMethods: View
    // private lateinit var layoutNotifications: View
    private lateinit var btnEditProfile: View // The Edit Profile Button/TextView



    private lateinit var profileViewModel: ProfileViewModel

    // Views for user info
    private lateinit var ivProfilePic: CircleImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView


    // --- CHANGE: Use onCreateView to inflate the layout ---
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_profile, container, false) // Use the EXISTING activity_profile.xml


        return view // Return the inflated view
    }

    // --- CHANGE: Use onViewCreated for view setup after inflation ---
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel - scope it to the Fragment or Activity
        // Scoping to requireActivity() makes it shared if other fragments in MainActivity need it
        val factory = ProfileViewModelFactory(requireActivity().application)
        profileViewModel = ViewModelProvider(requireActivity(), factory).get(ProfileViewModel::class.java)
        Log.d("ProfileFragment", "ViewModel initialized")

        initViews(view) // Initialize UI elements from the inflated view
        setupClickListeners() // Setup click listeners for profile elements

        observeViewModelData()

        // --- REMOVE THIS FUNCTION: Bottom navigation handled in MainActivity ---
        // setupBottomNavigation()
    }


    // --- CHANGE: Pass the inflated view to initViews ---
    private fun initViews(view: View) {
        // Initialize UI elements from activity_profile.xml using the 'view' parameter
        layoutPersonalInfo = view.findViewById(R.id.layoutPersonalInfo)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)

        // Add other settings layouts here if they have click listeners:
        // layoutPaymentMethods = view.findViewById(R.id.layoutPaymentMethods)
        // layoutNotifications = view.findViewById(R.id.layoutNotifications)

        // Note: Views like tvProfileTitle, cardUserInfo, etc. can be accessed directly
        // if needed, e.g., view.findViewById<TextView>(R.id.tvProfileTitle)
        // if you don't need them as lateinit properties with class-level scope.

        // Initialize user info views
        ivProfilePic = view.findViewById(R.id.ivProfilePic)
        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        // Initialize other views for bio, location etc. if you add them to activity_profile.xml
    }


    private fun setupClickListeners() {
        // Set up click listener for the Personal Info option
        layoutPersonalInfo.setOnClickListener {
            // --- CHANGE: Use requireActivity() or requireContext() instead of 'this' ---
            val intent = Intent(requireActivity(), PersonalInfoActivity::class.java) // Assuming PersonalInfoActivity exists
            startActivity(intent)
        }

        // Set up click listener for the Edit Profile button
        btnEditProfile.setOnClickListener {
            // TODO: Navigate to an Edit Profile Activity
            val intent = Intent(requireActivity(), EditProfileActivity::class.java) // Assuming EditProfileActivity exists
            startActivity(intent)
            // Note: If EditProfileActivity also becomes a Fragment, you'd handle navigation in MainActivity
        }

        // TODO: Add click listeners for Payment Methods and Notifications layouts if you want them to do something
        // Example: view.findViewById<View>(R.id.layoutPaymentMethods).setOnClickListener { /* navigate or perform action */ }
        // Example: view.findViewById<View>(R.id.layoutNotifications).setOnClickListener { /* navigate or perform action */ }
    }






    private fun observeViewModelData() {
        Log.d("ProfileFragment", "Observing currentUser from ProfileViewModel")
        profileViewModel.currentUser.observe(viewLifecycleOwner) { user -> // user is User?
            Log.d("ProfileFragment", "currentUser observer triggered with user: $user")
            if (user != null) {
                tvUserName.text = user.fullName ?: "N/A"
                tvUserEmail.text = user.email ?: "N/A" // Your User model has email

                user.profileImageBitmap?.let { bitmap ->
                    Log.d("ProfileFragment", "Loading profileImageBitmap into ivProfilePic")
                    Glide.with(this) // Use 'this' for Fragment context with Glide
                        .load(bitmap)
                        .placeholder(R.drawable.ic_person) // Ensure ic_person is a suitable placeholder
                        .error(R.drawable.ic_person)
                        .into(ivProfilePic)
                } ?: run {
                    Log.d("ProfileFragment", "profileImageBitmap is null, setting placeholder.")
                    ivProfilePic.setImageResource(R.drawable.ic_person)
                }
                // TODO: Populate other fields like bio, username, location if they are in your R.layout.activity_profile
                // view?.findViewById<TextView>(R.id.tvUserBio)?.text = user.bio ?: ""
            } else {
                Log.d("ProfileFragment", "currentUser is null. Displaying guest info.")
                // Handle case where user is not logged in or data not available
                tvUserName.text = "Guest User"
                tvUserEmail.text = "N/A"
                ivProfilePic.setImageResource(R.drawable.ic_person)
                // You might want to hide the "Edit Profile" button if user is null
                btnEditProfile.visibility = View.GONE
            }
        }

        // You would also observe userProducts, favorites, reviews LiveData here if needed
        // profileViewModel.userProducts.observe(viewLifecycleOwner) { products -> ... }
    }
}