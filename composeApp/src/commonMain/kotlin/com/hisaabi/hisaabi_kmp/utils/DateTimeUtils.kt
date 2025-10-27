package com.hisaabi.hisaabi_kmp.utils

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

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

