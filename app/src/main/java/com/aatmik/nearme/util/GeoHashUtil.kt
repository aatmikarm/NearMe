package com.aatmik.nearme.util

import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.firestore.GeoPoint

object GeoHashUtil {

    /**
     * Encode latitude and longitude to geohash
     */
    fun encode(latitude: Double, longitude: Double, precision: Int = 9): String {
        return GeoFireUtils.getGeoHashForLocation(GeoLocation(latitude, longitude), precision)
    }

    /**
     * Calculate distance between two geo points in meters
     */
    fun distanceBetween(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        return GeoFireUtils.getDistanceBetween(
            GeoLocation(lat1, lng1),
            GeoLocation(lat2, lng2)
        )
    }

    /**
     * Calculate geohash query bounds for a radius search
     */
    fun getGeohashQueryBounds(
        latitude: Double,
        longitude: Double,
        radiusInMeters: Double
    ): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()

        val bounds = GeoFireUtils.getGeoHashQueryBounds(
            GeoLocation(latitude, longitude),
            radiusInMeters
        )

        for (bound in bounds) {
            result.add(Pair(bound.startHash, bound.endHash))
        }

        return result
    }
}