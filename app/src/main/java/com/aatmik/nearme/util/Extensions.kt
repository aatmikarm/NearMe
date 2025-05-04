package com.aatmik.nearme.util

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.aatmik.nearme.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

/**
 * Show toast message
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

// Add this to Extensions.kt
/**
 * Load image with a fallback for empty URLs
 */
fun ImageView.loadUserPhoto(url: String?, userId: String, placeholder: Int = R.drawable.ic_person) {
    if (url.isNullOrEmpty()) {
        // Try to load from other sources or use placeholder
        val storage = FirebaseStorage.getInstance()
        val photoRef = storage.reference.child("profile_photos/$userId/original")

        // Try to list files in the user's photos directory
        photoRef.listAll()
            .addOnSuccessListener { result ->
                if (result.items.isNotEmpty()) {
                    // Get the first photo's download URL
                    result.items[0].downloadUrl
                        .addOnSuccessListener { uri ->
                            Glide.with(this.context)
                                .load(uri)
                                .placeholder(placeholder)
                                .error(placeholder)
                                .into(this)
                        }
                        .addOnFailureListener {
                            setImageResource(placeholder)
                        }
                } else {
                    setImageResource(placeholder)
                }
            }
            .addOnFailureListener {
                setImageResource(placeholder)
            }
    } else {
        // URL is not empty, load normally
        Glide.with(this.context)
            .load(url)
            .placeholder(placeholder)
            .error(placeholder)
            .into(this)
    }
}

/**
 * Show Snackbar
 */
fun View.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(this, message, duration).show()
}

/**
 * Show Snackbar with action
 */
fun View.showSnackbarWithAction(
    message: String,
    actionText: String,
    action: () -> Unit,
    duration: Int = Snackbar.LENGTH_LONG
) {
    Snackbar.make(this, message, duration)
        .setAction(actionText) { action() }
        .show()
}

/**
 * Load image using Glide
 */
fun ImageView.loadImage(url: String?, placeholder: Int = R.drawable.ic_person) {
    Glide.with(this.context)
        .load(url)
        .placeholder(placeholder)
        .error(placeholder)
        .into(this)
}

/**
 * Load image with rounded corners
 */
fun ImageView.loadRoundedImage(url: String?, cornerRadius: Int = 8, placeholder: Int = R.drawable.ic_person) {
    val requestOptions = RequestOptions().transforms(
        CenterCrop(),
        RoundedCorners(cornerRadius)
    )

    Glide.with(this.context)
        .load(url)
        .apply(requestOptions)
        .placeholder(placeholder)
        .error(placeholder)
        .into(this)
}

/**
 * Show view (used in chaining)
 */
fun View.show() {
    this.visibility = View.VISIBLE
}

/**
 * Hide view (used in chaining)
 */
fun View.hide() {
    this.visibility = View.GONE
}

/**
 * Make view invisible (used in chaining)
 */
fun View.invisible() {
    this.visibility = View.INVISIBLE
}

/**
 * Format date to string
 */
fun Date.formatToString(pattern: String = "dd/MM/yyyy"): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(this)
}

/**
 * Convert timestamp to date string
 */
fun Long.toDateString(pattern: String = "dd/MM/yyyy"): String {
    val date = Date(this)
    return date.formatToString(pattern)
}

/**
 * Convert timestamp to time string
 */
fun Long.toTimeString(pattern: String = "HH:mm"): String {
    val date = Date(this)
    return date.formatToString(pattern)
}