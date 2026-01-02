package com.hisaabi.hisaabi_kmp.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Generates the current timestamp in ISO 8601 format with milliseconds.
 * Format: 2024-10-01T16:30:42.000Z
 * This format is used for database created_at and updated_at fields.
 */
fun getCurrentTimestamp(): String {
    val instant = Clock.System.now()
    val dateTime = instant.toLocalDateTime(TimeZone.UTC)

    return "${dateTime.year}-" +
            "${dateTime.monthNumber.toString().padStart(2, '0')}-" +
            "${dateTime.dayOfMonth.toString().padStart(2, '0')}T" +
            "${dateTime.hour.toString().padStart(2, '0')}:" +
            "${dateTime.minute.toString().padStart(2, '0')}:" +
            "${dateTime.second.toString().padStart(2, '0')}.000Z"
}

/**
 * Formats a timestamp (in milliseconds) to a readable date-time string.
 * Format: DD/MM/YYYY HH:MM
 */
fun formatDateTime(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.dayOfMonth.toString().padStart(2, '0')}/" +
            "${dateTime.monthNumber.toString().padStart(2, '0')}/" +
            "${dateTime.year} " +
            "${dateTime.hour.toString().padStart(2, '0')}:" +
            "${dateTime.minute.toString().padStart(2, '0')}"
}

/**
 * Formats a timestamp (in milliseconds) to a readable date-only string.
 * Format: DD/MM/YYYY
 */
fun formatDate(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.dayOfMonth.toString().padStart(2, '0')}/" +
            "${dateTime.monthNumber.toString().padStart(2, '0')}/" +
            "${dateTime.year}"
}

/**
 * Formats a transaction date (timestamp in milliseconds) to "dd MMM, yyyy" format.
 * Example: 15 Jan, 2024
 */
fun formatTransactionDate(timestampString: String?): String {
    if (timestampString == null) return ""

    return try {
        val timestamp = timestampString.toLongOrNull() ?: return timestampString
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val monthNames = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        val monthName = monthNames.getOrNull(dateTime.monthNumber - 1) ?: ""
        "${dateTime.dayOfMonth.toString().padStart(2, '0')} $monthName, ${dateTime.year}"
    } catch (e: Exception) {
        timestampString
    }
}


/**
 * Formats a transaction date (timestamp in milliseconds) to "dd MMM, yyyy HH:MM" format.
 * Example: 15 Jan, 2024 14:30
 */
fun formatTransactionDateTime(timestampString: String?): String {
    if (timestampString == null) return ""

    return try {
        val timestamp = timestampString.toLongOrNull() ?: return timestampString
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val monthNames = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        val monthName = monthNames.getOrNull(dateTime.monthNumber - 1) ?: ""
        "${
            dateTime.dayOfMonth.toString().padStart(2, '0')
        } $monthName, ${dateTime.year} ${
            dateTime.hour.toString().padStart(2, '0')
        }:${dateTime.minute.toString().padStart(2, '0')}"
    } catch (e: Exception) {
        timestampString
    }
}

/**
 * Formats an entry date (ISO 8601 string format "yyyy-MM-DDTHH:mm.000Z") to "dd MMM, yyyy HH:MM" format.
 * Example: 15 Jan, 2024 14:30
 * Converts from UTC to local timezone before formatting.
 */
fun formatEntryDateTime(isoDateString: String?): String {
    if (isoDateString == null || isoDateString.isEmpty()) return ""

    return try {
        // Parse ISO 8601 format: yyyy-MM-DDTHH:mm:ss.000Z or yyyy-MM-DDTHH:mm.000Z
        // Handle various ISO 8601 formats with or without milliseconds
        var normalizedString = isoDateString.trim()

        // Remove milliseconds if present (e.g., .000, .123, etc.)
        normalizedString = normalizedString.replace(Regex("\\.\\d+"), "")

        // Ensure Z suffix for UTC
        if (!normalizedString.endsWith("Z")) {
            normalizedString += "Z"
        }

        val parts = normalizedString.split("T")
        if (parts.size != 2) return isoDateString

        val dateParts = parts[0].split("-")
        if (dateParts.size != 3) return isoDateString

        // Parse time part, removing Z suffix
        val timePart = parts[1].replace("Z", "").trim()
        val timeParts = timePart.split(":")
        if (timeParts.size < 2) return isoDateString

        val year = dateParts[0].toIntOrNull() ?: return isoDateString
        val month = dateParts[1].toIntOrNull() ?: return isoDateString
        val day = dateParts[2].toIntOrNull() ?: return isoDateString
        val hour = timeParts[0].toIntOrNull() ?: return isoDateString
        val minute = timeParts[1].toIntOrNull() ?: return isoDateString

        // Parse seconds if present, otherwise default to 0
        val secondPart = if (timeParts.size >= 3) timeParts[2].split(".")[0] else "0"
        val second = secondPart.toIntOrNull() ?: 0

        // Create LocalDateTime representing UTC time
        val utcDateTime = LocalDateTime(
            year = year,
            monthNumber = month,
            dayOfMonth = day,
            hour = hour,
            minute = minute,
            second = second
        )

        // Convert UTC LocalDateTime to Instant (treating the LocalDateTime as UTC)
        val instant = utcDateTime.toInstant(TimeZone.UTC)

        // Convert Instant to local timezone
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        val monthNames = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        val monthName = monthNames.getOrNull(localDateTime.monthNumber - 1) ?: ""

        "${
            localDateTime.dayOfMonth.toString().padStart(2, '0')
        } $monthName, ${localDateTime.year} ${
            localDateTime.hour.toString().padStart(2, '0')
        }:${localDateTime.minute.toString().padStart(2, '0')}"
    } catch (e: Exception) {
        isoDateString
    }
}
