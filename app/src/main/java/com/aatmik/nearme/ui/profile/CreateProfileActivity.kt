package com.aatmik.nearme.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.aatmik.nearme.databinding.ActivityCreateProfileBinding
import com.aatmik.nearme.model.UserPhoto
import com.aatmik.nearme.model.UserProfile
import com.aatmik.nearme.ui.main.MainActivity
import com.aatmik.nearme.util.ImageCompressor
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class CreateProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateProfileBinding
    private val viewModel: CreateProfileViewModel by viewModels()

    // Selected profile photo URI
    private var selectedPhotoUri: Uri? = null

    // Photo picker launcher
    // Update the photo selection in CreateProfileActivity.kt
    private val photoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            // Compress the image before using it
            val compressedUri = ImageCompressor.compressImage(this, it)

            if (compressedUri != null) {
                selectedPhotoUri = compressedUri
                binding.ivProfilePhoto.setImageURI(compressedUri)
                binding.btnAddPhoto.text = "Change Photo"
            } else {
                // If compression fails, use the original URI
                selectedPhotoUri = it
                binding.ivProfilePhoto.setImageURI(it)
                binding.btnAddPhoto.text = "Change Photo"

                // Inform the user that we'll be using the original image
                Toast.makeText(this, "Using original image size", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // Add photo button
        binding.btnAddPhoto.setOnClickListener {
            photoPicker.launch("image/*")
        }

        // Gender selection
        binding.radioMale.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.radioFemale.isChecked = false
                binding.radioNonBinary.isChecked = false
            }
        }

        binding.radioFemale.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.radioMale.isChecked = false
                binding.radioNonBinary.isChecked = false
            }
        }

        binding.radioNonBinary.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.radioMale.isChecked = false
                binding.radioFemale.isChecked = false
            }
        }

        // Continue button
        binding.btnContinue.setOnClickListener {
            if (validateInputs()) {
                createUserProfile()
            }
        }
    }

    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnContinue.isEnabled = !isLoading
        }

        // Observe profile creation result
        viewModel.profileCreationResult.observe(this) { result ->
            result?.let {
                if (it.isSuccess) {
                    // Profile created, navigate to main activity
                    navigateToMainActivity()
                } else {
                    // Profile creation failed
                    Toast.makeText(
                        this,
                        "Failed to create profile: ${result.exceptionOrNull()?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validate name
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            isValid = false
        }

        // Validate age
        val ageText = binding.etAge.text.toString().trim()
        if (ageText.isEmpty()) {
            binding.etAge.error = "Age is required"
            isValid = false
        } else {
            val age = ageText.toIntOrNull()
            if (age == null || age < 18) {
                binding.etAge.error = "Age must be 18 or older"
                isValid = false
            }
        }

        // Validate gender selection
        if (!binding.radioMale.isChecked && !binding.radioFemale.isChecked && !binding.radioNonBinary.isChecked) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        // Validate photo
        if (selectedPhotoUri == null) {
            Toast.makeText(this, "Please add a profile photo", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    // Update in CreateProfileActivity.kt - in createUserProfile
    private fun createUserProfile() {
        val name = binding.etName.text.toString().trim()
        val age = binding.etAge.text.toString().trim().toInt()

        val gender = when {
            binding.radioMale.isChecked -> "male"
            binding.radioFemale.isChecked -> "female"
            binding.radioNonBinary.isChecked -> "non-binary"
            else -> ""
        }

        val bio = binding.etBio.text.toString().trim()

        // Create empty user profile
        val userProfile = UserProfile(
            displayName = name,
            age = age,
            gender = gender,
            bio = bio
        )

        // Upload photo and create profile
        selectedPhotoUri?.let { uri ->
            viewModel.createProfileWithPhoto(userProfile, uri)
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}