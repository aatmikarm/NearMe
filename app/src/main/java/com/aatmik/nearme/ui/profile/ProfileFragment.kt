package com.aatmik.nearme.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.aatmik.nearme.R
import com.aatmik.nearme.databinding.FragmentProfileBinding
import com.aatmik.nearme.ui.auth.AuthActivity
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding
        get() = _binding!!
    private val TAG = "NearMe_ProfileFragment"

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        Log.d(TAG, "Setting up UI elements")

        // Setup logout button
        binding.btnLogout.setOnClickListener {
            Log.d(TAG, "Logout button clicked")
            logoutUser()
        }
    }

    private fun logoutUser() {
        Log.d(TAG, "Logging out user")
        try {
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut()
            Log.d(TAG, "User signed out successfully")

            // Navigate to auth screen
            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            // Show confirmation toast
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error logging out: ${e.message}", e)
            Toast.makeText(requireContext(), "Error logging out", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        Log.d(TAG, "Setting up ViewModel observers")

        // Observe user profile from ViewModel
        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            Log.d(TAG, "User profile updated: ${profile?.displayName}")

            // Update UI with profile information
            profile?.let {
                // Set single profile photo
                val primaryPhoto =
                        it.photos.firstOrNull { photo -> photo.isPrimary }
                                ?: it.photos.firstOrNull()
                Glide.with(this)
                        .load(primaryPhoto?.url)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .centerCrop()
                        .into(binding.ivProfilePhoto)

                // Set name and age
                binding.tvNameAge.text = "${it.displayName}, ${it.age}"
                binding.tvGender.text = it.gender.capitalize()
                binding.tvHeight.text = "173 cm" // Replace with actual value if available
                binding.tvWeight.text = "62 kg" // Replace with actual value if available
                binding.tvDistance.text = "5 km away" // Replace with actual value if available

                // Show verified badge if verified
                binding.ivVerifiedBadge.visibility =
                        if (it.verificationStatus == "verified") View.VISIBLE else View.GONE

                // Set relationship status (dummy for now)
                binding.tvRelationshipStatus?.text = "Dating 👋"

                // Set bio
                binding.tvBio.text = it.bio.ifEmpty { "No bio added yet" }

                // Set Instagram info
                if (it.instagramConnected) {
                    binding.tvInstagram.text = "@${it.instagramId}"
                    binding.btnConnectInstagram.visibility = View.GONE
                } else {
                    binding.tvInstagram.text = "Not connected"
                    binding.btnConnectInstagram.visibility = View.VISIBLE
                }

                // Set interests
                binding.chipGroupInterests.removeAllViews()
                it.interests.forEach { interest ->
                    val chip = Chip(requireContext())
                    chip.text = interest
                    chip.isClickable = false
                    chip.isCheckable = false
                    binding.chipGroupInterests.addView(chip)
                }
            }
        }

        // Social icon click listeners (show Toast for now)
        // Camera icon click
        binding.ivEditPhoto?.setOnClickListener {
            Toast.makeText(requireContext(), "Edit photo clicked", Toast.LENGTH_SHORT).show()
            // TODO: Launch photo picker or edit photo screen
        }
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView")
        super.onDestroyView()
        _binding = null
    }
}
