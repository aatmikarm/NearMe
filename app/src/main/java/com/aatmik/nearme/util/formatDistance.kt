package com.aatmik.nearme.util

/**
 * Format distance to human readable string
 */
fun formatDistance(distance: Double): String {
    return when {
        distance < 1000 -> "${distance.toInt()}m away"
        else -> String.format("%.1fkm away", distance / 1000)
    }
}