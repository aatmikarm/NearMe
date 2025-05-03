package com.aatmik.nearme.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.aatmik.nearme.R
import com.aatmik.nearme.model.UserLocation
import com.aatmik.nearme.repository.LocationRepository
import com.aatmik.nearme.ui.main.MainActivity
import com.aatmik.nearme.util.GeoHashUtil
import com.aatmik.nearme.util.PreferenceManager
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {

    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    // Process the location update
                    processLocationUpdate(location)
                }
            }
        }

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start foreground service with notification
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Start location updates
        startLocationUpdates()

        return START_STICKY
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle(getString(R.string.app_name))
        .setContentText(getString(R.string.location_service_running))
        .setSmallIcon(R.drawable.ic_notification)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Service Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used for location tracking service"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            TimeUnit.MINUTES.toMillis(2) // Update every 2 minutes to save battery
        ).apply {
            setMinUpdateIntervalMillis(TimeUnit.MINUTES.toMillis(1))
            setMaxUpdateDelayMillis(TimeUnit.MINUTES.toMillis(5))
            setMinUpdateDistanceMeters(10f) // Only update if moved at least 10 meters
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // Handle permission denied
        }
    }

    private fun processLocationUpdate(location: Location) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Create UserLocation object
        val userLocation = UserLocation(
            userId = userId,
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            geohash = GeoHashUtil.encode(location.latitude, location.longitude),
            timestamp = System.currentTimeMillis(),
            isVisible = preferenceManager.isLocationSharingEnabled()
        )

        // Update location in repository (will update Firestore)
        serviceScope.launch {
            locationRepository.updateUserLocation(userLocation)

            // Check for nearby users
            checkProximity(userLocation)
        }
    }

    private fun checkProximity(userLocation: UserLocation) {
        serviceScope.launch {
            // Find nearby users
            val nearbyUsers = locationRepository.findNearbyUsers(userLocation)

            if (nearbyUsers.isNotEmpty()) {
                // Process proximity events
                locationRepository.processProximityEvents(userLocation, nearbyUsers)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 12345
        private const val CHANNEL_ID = "location_service_channel"
    }
}