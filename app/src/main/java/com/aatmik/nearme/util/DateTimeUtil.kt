package com.aatmik.nearme.util

import android.text.format.DateFormat
import android.text.format.DateUtils
import java.util.*

object DateTimeUtil {

    /**
     * Format message time
     * If today: show HH:MM
     * If this week: show day name
     * Otherwise: show date
     */
    fun formatMessageTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val date = Date(timestamp)

        return when {
            DateUtils.isToday(timestamp) -> {
                // Today: show time
                DateFormat.format("HH:mm", date).toString()
            }
            timestamp > now - DateUtils.WEEK_IN_MILLIS -> {
                // This week: show day name
                DateFormat.format("EEE", date).toString()
            }
            else -> {
                // Older: show date
                DateFormat.format("dd/MM/yy", date).toString()
            }
        }
    }

    /**
     * Format match time
     * "Just now" if less than a minute
     * "X minutes ago" if less than an hour
     * "X hours ago" if less than a day
     * "Yesterday" if yesterday
     * Date otherwise
     */
    fun formatMatchTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < DateUtils.MINUTE_IN_MILLIS -> "Just now"
            diff < DateUtils.HOUR_IN_MILLIS -> "${diff / DateUtils.MINUTE_IN_MILLIS} min ago"
            diff < DateUtils.DAY_IN_MILLIS -> "${diff / DateUtils.HOUR_IN_MILLIS} hours ago"
            DateUtils.isToday(timestamp + DateUtils.DAY_IN_MILLIS) -> "Yesterday"
            else -> DateFormat.format("dd/MM/yy", Date(timestamp)).toString()
        }
    }

    /**
     * Format chat preview time (for conversation list)
     * Today: HH:MM
     * Yesterday: "Yesterday"
     * This week: day name
     * This year: MM/DD
     * Older: MM/DD/YY
     */
    fun formatChatPreviewTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val date = Date(timestamp)

        return when {
            DateUtils.isToday(timestamp) -> {
                // Today: show time
                DateFormat.format("HH:mm", date).toString()
            }
            DateUtils.isToday(timestamp + DateUtils.DAY_IN_MILLIS) -> {
                // Yesterday
                "Yesterday"
            }
            timestamp > now - DateUtils.WEEK_IN_MILLIS -> {
                // This week: show day name
                DateFormat.format("EEE", date).toString()
            }
            timestamp > now - DateUtils.YEAR_IN_MILLIS -> {
                // This year: month/day
                DateFormat.format("MM/dd", date).toString()
            }
            else -> {
                // Older: month/day/year
                DateFormat.format("MM/dd/yy", date).toString()
            }
        }
    }

    /**
     * Format distance in meters to readable format
     */
    fun formatDistance(distance: Double): String {
        return when {
            distance < 1000 -> "${distance.toInt()}m away"
            else -> String.format("%.1fkm away", distance / 1000)
        }
    }
}