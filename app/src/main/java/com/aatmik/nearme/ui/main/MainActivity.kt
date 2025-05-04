package com.aatmik.nearme.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.aatmik.nearme.R
import com.aatmik.nearme.databinding.ActivityMainBinding
import com.aatmik.nearme.service.LocationService
import com.aatmik.nearme.ui.nearby.ProximityMatchActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val TAG = "NearMe_MainActivity"
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate() called")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        checkLocationPermission()
        observeViewModel()
    }

    private fun setupNavigation() {
        try {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController

            binding.bottomNavigation.setupWithNavController(navController)
            Log.d(TAG, "Navigation setup completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up navigation: ${e.message}", e)
        }
    }

    private fun checkLocationPermission() {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // Inform ViewModel about current permission state
        viewModel.setLocationPermissionGranted(hasFineLocationPermission && hasCoarseLocationPermission)

        if (!hasFineLocationPermission || !hasCoarseLocationPermission) {
            Log.i(TAG, "Requesting location permissions")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            Log.i(TAG, "Location permissions already granted")
            startLocationService()

            // Request background location if on Android 10 or higher
            requestBackgroundLocationIfNeeded()
        }
    }

    private fun requestBackgroundLocationIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val hasBackgroundLocationPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            Log.d(TAG, "Background location permission: $hasBackgroundLocationPermission")

            if (!hasBackgroundLocationPermission) {
                Log.i(TAG, "Requesting background location permission")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                val granted = grantResults.isNotEmpty() &&
                        grantResults.all { it == PackageManager.PERMISSION_GRANTED }

                viewModel.setLocationPermissionGranted(granted)

                if (granted) {
                    Log.i(TAG, "Location permissions granted by user")
                    startLocationService()
                    requestBackgroundLocationIfNeeded()
                } else {
                    Log.w(TAG, "Location permissions denied by user")
                    // Show explanation dialog
                    showLocationPermissionExplanation()
                }
            }
            BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Background location permission granted by user")
                } else {
                    Log.w(TAG, "Background location permission denied by user")
                    // Show explanation about background location
                    showBackgroundLocationExplanation()
                }
            }
        }
    }

    private fun showLocationPermissionExplanation() {
        // Show a dialog explaining why location is needed for the app
        // For example, using MaterialAlertDialogBuilder
    }

    private fun showBackgroundLocationExplanation() {
        // Show a dialog explaining benefits of background location
    }

    private fun startLocationService() {
        Log.i(TAG, "Starting location service")
        try {
            val intent = Intent(this, LocationService::class.java)
            ContextCompat.startForegroundService(this, intent)
            Log.i(TAG, "Location service started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start location service: ${e.message}", e)
        }
    }

    private fun observeViewModel() {
        // Observe active proximity events to show notifications/UI
        viewModel.activeProximityEvents.observe(this) { events ->
            if (events.isNotEmpty()) {
                // Show indicator or notification about new proximity events
                showProximityEventIndicator(events.size)
            } else {
                // Hide proximity event indicator
                hideProximityEventIndicator()
            }
        }

        // Observe selected proximity event to open ProximityMatch screen
        viewModel.selectedProximityEvent.observe(this) { event ->
            event?.let {
                // Open ProximityMatch activity when an event is selected
                val intent = ProximityMatchActivity.createIntent(this, it.id)
                startActivity(intent)

                // Clear the selected event after opening
                viewModel.clearSelectedProximityEvent()
            }
        }
    }

    private fun showProximityEventIndicator(count: Int) {
        // Implement notification or UI indicator for proximity events
        // For example, show a badge on the Nearby tab
    }

    private fun hideProximityEventIndicator() {
        // Hide any indicators for proximity events
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
        private const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 101
    }
}