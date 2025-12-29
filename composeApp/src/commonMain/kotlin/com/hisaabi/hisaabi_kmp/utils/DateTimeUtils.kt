package com.hisaabi.hisaabi_kmp.utils

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
 * Converts a timestamp (in milliseconds) to ISO 8601 format.
 * Format: 2024-10-01T16:30:42.000Z
 * This is used for converting transaction timestamps to the format expected by the API.
 */
fun millisToIsoTimestamp(millis: Long): String {
    val instant = Instant.fromEpochMilliseconds(millis)
    val dateTime = instant.toLocalDateTime(TimeZone.UTC)
    
    return "${dateTime.year}-" +
           "${dateTime.monthNumber.toString().padStart(2, '0')}-" +
           "${dateTime.dayOfMonth.toString().padStart(2, '0')}T" +
           "${dateTime.hour.toString().padStart(2, '0')}:" +
           "${dateTime.minute.toString().padStart(2, '0')}:" +
           "${dateTime.second.toString().padStart(2, '0')}.000Z"
}

/**
 * Converts a timestamp string (milliseconds as string) to ISO 8601 format.
 * If the string is already in ISO format or null, returns it as-is.
 * Format: 2024-10-01T16:30:42.000Z
 */
fun timestampStringToIso(timestampString: String?): String? {
    if (timestampString == null) return null
    
    // Check if already in ISO format (contains 'T' and 'Z')
    if (timestampString.contains("T") && timestampString.contains("Z")) {
        return timestampString
    }
    
    // Try to parse as milliseconds
    val millis = timestampString.toLongOrNull() ?: return timestampString
    return millisToIsoTimestamp(millis)
}

/**
 * Formats an entry date (ISO 8601 string format "yyyy-MM-DDTHH:mm.000Z") to "dd MMM, yyyy" format.
 * Example: 15 Jan, 2024
 */
fun formatEntryDate(isoDateString: String?): String {
    if (isoDateString == null || isoDateString.isEmpty()) return ""
    
    return try {
        // Parse ISO 8601 format: yyyy-MM-DDTHH:mm.000Z
        val parts = isoDateString.replace(".000Z", "Z").split("T")
        if (parts.size != 2) return isoDateString
        
        val dateParts = parts[0].split("-")
        if (dateParts.size != 3) return isoDateString
        
        val year = dateParts[0].toIntOrNull() ?: return isoDateString
        val month = dateParts[1].toIntOrNull() ?: return isoDateString
        val day = dateParts[2].toIntOrNull() ?: return isoDateString
        
        val monthNames = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        val monthName = monthNames.getOrNull(month - 1) ?: ""
        
        "${day.toString().padStart(2, '0')} $monthName, $year"
    } catch (e: Exception) {
        isoDateString
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
        "${dateTime.dayOfMonth.toString().padStart(2, '0')} $monthName, ${dateTime.year} ${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
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
        
        "${localDateTime.dayOfMonth.toString().padStart(2, '0')} $monthName, ${localDateTime.year} ${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
    } catch (e: Exception) {
        isoDateString
    }
}

/**
 * A reusable date-time picker dialog for selecting date and time.
 * 
 * @param initialTimestamp The initial timestamp to display in milliseconds
 * @param onConfirm Callback when user confirms the selection with the new timestamp
 * @param onDismiss Callback when user dismisses the dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDateTimePickerDialog(
    initialTimestamp: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val instant = Instant.fromEpochMilliseconds(initialTimestamp)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    var day by remember { mutableStateOf(localDateTime.dayOfMonth.toString()) }
    var month by remember { mutableStateOf(localDateTime.monthNumber.toString()) }
    var year by remember { mutableStateOf(localDateTime.year.toString()) }
    var hour by remember { mutableStateOf(localDateTime.hour.toString()) }
    var minute by remember { mutableStateOf(localDateTime.minute.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date & Time") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Date", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = day,
                        onValueChange = { if (it.length <= 2) day = it },
                        label = { Text("Day") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = month,
                        onValueChange = { if (it.length <= 2) month = it },
                        label = { Text("Month") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = year,
                        onValueChange = { if (it.length <= 4) year = it },
                        label = { Text("Year") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text("Time", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hour,
                        onValueChange = { if (it.length <= 2) hour = it },
                        label = { Text("Hour") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minute,
                        onValueChange = { if (it.length <= 2) minute = it },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        val selectedDateTime = kotlinx.datetime.LocalDateTime(
                            year = year.toIntOrNull() ?: localDateTime.year,
                            monthNumber = month.toIntOrNull()?.coerceIn(1, 12) ?: localDateTime.monthNumber,
                            dayOfMonth = day.toIntOrNull()?.coerceIn(1, 31) ?: localDateTime.dayOfMonth,
                            hour = hour.toIntOrNull()?.coerceIn(0, 23) ?: localDateTime.hour,
                            minute = minute.toIntOrNull()?.coerceIn(0, 59) ?: localDateTime.minute
                        )
                        val timestamp = selectedDateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                        onConfirm(timestamp)
                    } catch (e: Exception) {
                        onDismiss()
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * A reusable date-only picker dialog for selecting a date (without time).
 * 
 * @param initialTimestamp The initial timestamp to display in milliseconds
 * @param onConfirm Callback when user confirms the selection with the new timestamp (at start of day, 00:00:00)
 * @param onDismiss Callback when user dismisses the dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDatePickerDialog(
    initialTimestamp: Long?,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val currentTime = Clock.System.now().toEpochMilliseconds()
    val initialTime = initialTimestamp ?: currentTime
    val instant = Instant.fromEpochMilliseconds(initialTime)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    var day by remember { mutableStateOf(localDateTime.dayOfMonth.toString()) }
    var month by remember { mutableStateOf(localDateTime.monthNumber.toString()) }
    var year by remember { mutableStateOf(localDateTime.year.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = day,
                        onValueChange = { if (it.length <= 2) day = it },
                        label = { Text("Day") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = month,
                        onValueChange = { if (it.length <= 2) month = it },
                        label = { Text("Month") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = year,
                        onValueChange = { if (it.length <= 4) year = it },
                        label = { Text("Year") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        val selectedDate = kotlinx.datetime.LocalDateTime(
                            year = year.toIntOrNull() ?: localDateTime.year,
                            monthNumber = month.toIntOrNull()?.coerceIn(1, 12) ?: localDateTime.monthNumber,
                            dayOfMonth = day.toIntOrNull()?.coerceIn(1, 31) ?: localDateTime.dayOfMonth,
                            hour = 0,
                            minute = 0
                        )
                        val timestamp = selectedDate.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                        onConfirm(timestamp)
                    } catch (e: Exception) {
                        onDismiss()
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

