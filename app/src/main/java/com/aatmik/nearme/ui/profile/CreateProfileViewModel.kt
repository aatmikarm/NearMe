package com.aatmik.nearme.ui.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aatmik.nearme.model.UserProfile
import com.aatmik.nearme.repository.AuthRepository
import com.aatmik.nearme.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _profileCreationResult = MutableLiveData<Result<Boolean>?>()
    val profileCreationResult: LiveData<Result<Boolean>?> = _profileCreationResult

    /**
     * Create profile with photo
     */
    fun createProfileWithPhoto(userProfile: UserProfile, photoUri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val userId = authRepository.getCurrentUserId() ?: throw Exception("User not authenticated")

                // Complete user profile with user ID and timestamps
                val completeProfile = userProfile.copy(
                    uid = userId,
                    phoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: "",
                    createdAt = System.currentTimeMillis(),
                    lastActive = System.currentTimeMillis()
                )

                // Create user profile in Firestore
                val profileCreated = userRepository.createUserProfile(completeProfile)

                if (profileCreated) {
                    // Upload photo
                    val photo = userRepository.uploadProfilePhoto(userId, photoUri, true)

                    if (photo != null) {
                        _profileCreationResult.value = Result.success(true)
                    } else {
                        _profileCreationResult.value = Result.failure(Exception("Failed to upload photo"))
                    }
                } else {
                    _profileCreationResult.value = Result.failure(Exception("Failed to create profile"))
                }
            } catch (e: Exception) {
                _profileCreationResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}