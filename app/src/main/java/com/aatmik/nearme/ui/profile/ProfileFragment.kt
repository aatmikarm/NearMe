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
import com.aatmik.nearme.databinding.FragmentProfileBinding
import com.aatmik.nearme.ui.auth.AuthActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
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

        // Setup other profile UI components and listeners
        binding.btnSettings.setOnClickListener {
            Log.d(TAG, "Settings button clicked")
            // Navigate to settings screen
        }

        binding.btnPremium.setOnClickListener {
            Log.d(TAG, "Premium button clicked")
            // Navigate to premium screen
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
                // Set profile image
                // Glide.with(this).load(it.photos.firstOrNull { it.isPrimary }?.url).into(binding.ivProfilePhoto)

                // Set name and age
                binding.tvNameAge.text = "${it.displayName}, ${it.age}"

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
                // Add interest chips here
            }
        }
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView")
        super.onDestroyView()
        _binding = null
    }
}