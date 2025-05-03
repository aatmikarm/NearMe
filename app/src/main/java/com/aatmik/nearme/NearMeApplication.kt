package com.aatmik.nearme
import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NearMeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase, etc.
    }
}