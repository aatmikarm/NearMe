package com.aatmik.nearme.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.aatmik.nearme.R
import com.aatmik.nearme.ui.auth.AuthActivity
import com.aatmik.nearme.ui.main.MainActivity
import com.aatmik.nearme.util.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, 2000) // 2 seconds delay
    }

    private fun navigateToNextScreen() {
        val intent = when {
            // If user is logged in, go to main screen
            firebaseAuth.currentUser != null -> Intent(this, MainActivity::class.java)

            // If first time, go to onboarding
            preferenceManager.isFirstTimeLaunch() -> Intent(this, OnboardingActivity::class.java)

            // Otherwise, go to auth screen
            else -> Intent(this, AuthActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}