package com.aatmik.nearme.ui.main

import androidx.lifecycle.ViewModel
import com.aatmik.nearme.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    // Handle user location updates and other business logic

    // Functions for main activity coordination

}
