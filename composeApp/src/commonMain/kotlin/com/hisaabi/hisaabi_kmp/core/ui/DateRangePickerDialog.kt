package com.hisaabi.hisaabi_kmp.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*

/**
 * Selection mode for the date picker dialog
 */
enum class DatePickerMode {
    SINGLE_DATE,    // Single date selection - dismisses on click
    DATE_RANGE      // Range selection - requires OK button
}

/**
 * A reusable date range picker dialog for selecting start and end dates using a calendar view.
 * 
 * @param initialStartDate The initial start date in YYYY-MM-DD format (string)
 * @param initialEndDate The initial end date in YYYY-MM-DD format (string)
 * @param mode The selection mode - SINGLE_DATE or DATE_RANGE
 * @param onConfirm Callback when user confirms the selection with start and end dates (YYYY-MM-DD format)
 * @param onDismiss Callback when user dismisses the dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    initialStartDate: String? = null,
    initialEndDate: String? = null,
    mode: DatePickerMode = DatePickerMode.DATE_RANGE,
    onConfirm: (startDate: String, endDate: String) -> Unit,
    onDismiss: () -> Unit
) {
    val currentTime = Clock.System.now().toEpochMilliseconds()
    val instant = Instant.fromEpochMilliseconds(currentTime)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    
    // Parse initial dates
    val initialStart = parseDateStringToLocalDate(initialStartDate) ?: localDateTime.date
    val initialEnd = parseDateStringToLocalDate(initialEndDate) ?: localDateTime.date
    
    // State for selected dates
    var selectedStartDate by remember { mutableStateOf<LocalDate?>(if (initialStartDate != null) initialStart else null) }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(if (initialEndDate != null) initialEnd else null) }
    
    // State for current displayed month
    var currentMonth by remember { 
        mutableStateOf(
            if (initialStartDate != null) initialStart 
            else if (initialEndDate != null) initialEnd
            else localDateTime.date
        )
    }
    
    // State for year picker dialog
    var showYearPicker by remember { mutableStateOf(false) }
    
    // Selection state: null = no selection, 1 = selecting start, 2 = selecting end
    var selectionState by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(if (mode == DatePickerMode.SINGLE_DATE) "Select Date" else "Select Date Range")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Month navigation header
                MonthNavigationHeader(
                    currentMonth = currentMonth,
                    onPreviousMonth = {
                        currentMonth = currentMonth.minus(1L, DateTimeUnit.MONTH)
                    },
                    onNextMonth = {
                        currentMonth = currentMonth.plus(1L, DateTimeUnit.MONTH)
                    },
                    onMonthYearClick = {
                        showYearPicker = true
                    }
                )
                
                // Calendar view
                CalendarView(
                    month = currentMonth,
                    selectedStartDate = selectedStartDate,
                    selectedEndDate = selectedEndDate,
                    onDateClick = { date ->
                        if (mode == DatePickerMode.SINGLE_DATE) {
                            // Single date mode: select and dismiss immediately
                            selectedStartDate = date
                            val dateStr = formatLocalDateToString(date)
                            onConfirm(dateStr, dateStr)
                        } else {
                            // Range mode: handle range selection
                            when {
                                selectedStartDate == null || (selectedStartDate != null && selectedEndDate != null) -> {
                                    // Start new selection
                                    selectedStartDate = date
                                    selectedEndDate = null
                                    selectionState = 1
                                }
                                selectedStartDate != null && selectedEndDate == null -> {
                                    // Complete the range
                                    if (date >= selectedStartDate!!) {
                                        selectedEndDate = date
                                        selectionState = 2
                                    } else {
                                        // If clicked date is before start, make it the new start
                                        selectedEndDate = selectedStartDate
                                        selectedStartDate = date
                                        selectionState = 2
                                    }
                                }
                            }
                        }
                    }
                )
                
                // Selected dates display (only for range mode)
                if (mode == DatePickerMode.DATE_RANGE && (selectedStartDate != null || selectedEndDate != null)) {
                    Text(
                        text = buildString {
                            append("From: ${formatLocalDate(selectedStartDate)}")
                            if (selectedEndDate != null) {
                                append(" → To: ${formatLocalDate(selectedEndDate)}")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Single date display
                if (mode == DatePickerMode.SINGLE_DATE && selectedStartDate != null) {
                    Text(
                        text = "Selected: ${formatLocalDate(selectedStartDate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            if (mode == DatePickerMode.DATE_RANGE) {
                TextButton(
                    onClick = {
                        if (selectedStartDate != null && selectedEndDate != null) {
                            val startStr = formatLocalDateToString(selectedStartDate!!)
                            val endStr = formatLocalDateToString(selectedEndDate!!)
                            onConfirm(startStr, endStr)
                        }
                    },
                    enabled = selectedStartDate != null && selectedEndDate != null
                ) {
                    Text("OK")
                }
            } else {
                // No OK button for single date mode
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
    
    // Year Picker Dialog
    if (showYearPicker) {
        YearPickerDialog(
            currentYear = currentMonth.year,
            onYearSelected = { year ->
                currentMonth = LocalDate(year, currentMonth.monthNumber, 1)
                showYearPicker = false
            },
            onDismiss = {
                showYearPicker = false
            }
        )
    }
}

@Composable
private fun MonthNavigationHeader(
    currentMonth: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMonthYearClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
        }
        
        Text(
            text = formatMonthYear(currentMonth),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = onMonthYearClick),
            color = MaterialTheme.colorScheme.primary
        )
        
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
        }
    }
}

@Composable
private fun YearPickerDialog(
    currentYear: Int,
    onYearSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // Generate years (current year ± 50 years)
    val years = remember(currentYear) {
        (currentYear - 50..currentYear + 50).toList()
    }
    
    // Calculate the index of the current year (should be 50, since it's in the middle of the range)
    val currentYearIndex = remember(currentYear) {
        years.indexOf(currentYear).coerceAtLeast(0)
    }
    
    // Create LazyGridState
    val gridState = rememberLazyGridState()
    
    // Scroll to current year when dialog opens
    LaunchedEffect(currentYearIndex) {
        if (currentYearIndex >= 0) {
            gridState.animateScrollToItem(currentYearIndex)
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text("Select Year")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(years) { year ->
                        val isSelected = currentYear == year
                        
                        FilterChip(
                            selected = isSelected,
                            onClick = { 
                                onYearSelected(year)
                            },
                            label = { 
                                Text(
                                    year.toString(),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CalendarView(
    month: LocalDate,
    selectedStartDate: LocalDate?,
    selectedEndDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit
) {
    // Get first day of the month
    val firstDayOfMonth = LocalDate(month.year, month.monthNumber, 1)
    
    // Get the day of week for the first day (0 = Monday, 6 = Sunday)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal
    
    // Calculate days in the month by getting first day of next month and subtracting 1 day
    val nextMonth = if (month.monthNumber == 12) {
        LocalDate(month.year + 1, 1, 1)
    } else {
        LocalDate(month.year, month.monthNumber + 1, 1)
    }
    val lastDayOfMonth = nextMonth.minus(1L, DateTimeUnit.DAY)
    val daysInMonth = lastDayOfMonth.dayOfMonth
    
    // Calculate total cells needed (6 weeks = 42 days)
    val totalCells = 42
    val daysBeforeMonth = firstDayOfWeek
    val daysAfterMonth = totalCells - daysBeforeMonth - daysInMonth
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Day headers (Mon, Tue, Wed, etc.)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            dayNames.forEach { dayName ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Calendar grid (6 weeks)
        repeat(6) { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(7) { dayOfWeek ->
                    val dayIndex = week * 7 + dayOfWeek
                    val date = when {
                        dayIndex < daysBeforeMonth -> {
                            // Previous month
                            val prevMonth = firstDayOfMonth.minus(1L, DateTimeUnit.MONTH)
                            val prevMonthLastDay = if (prevMonth.monthNumber == 12) {
                                LocalDate(prevMonth.year + 1, 1, 1).minus(1L, DateTimeUnit.DAY)
                            } else {
                                LocalDate(prevMonth.year, prevMonth.monthNumber + 1, 1).minus(1L, DateTimeUnit.DAY)
                            }
                            val daysInPrevMonth = prevMonthLastDay.dayOfMonth
                            val day = daysInPrevMonth - (daysBeforeMonth - dayIndex - 1)
                            LocalDate(prevMonth.year, prevMonth.monthNumber, day)
                        }
                        dayIndex < daysBeforeMonth + daysInMonth -> {
                            // Current month
                            val day = dayIndex - daysBeforeMonth + 1
                            LocalDate(month.year, month.monthNumber, day)
                        }
                        else -> {
                            // Next month
                            val day = dayIndex - daysBeforeMonth - daysInMonth + 1
                            val nextMonthDate = firstDayOfMonth.plus(1L, DateTimeUnit.MONTH)
                            LocalDate(nextMonthDate.year, nextMonthDate.monthNumber, day)
                        }
                    }
                    
                    val isCurrentMonth = date.month == month.month && date.year == month.year
                    val isSelected = date == selectedStartDate || date == selectedEndDate
                    val isInRange = selectedStartDate != null && selectedEndDate != null && 
                                   date >= selectedStartDate && date <= selectedEndDate
                    val isToday = date == Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                    
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        CalendarDay(
                            date = date,
                            dayNumber = date.dayOfMonth,
                            isCurrentMonth = isCurrentMonth,
                            isSelected = isSelected,
                            isInRange = isInRange,
                            isToday = isToday,
                            onClick = { onDateClick(date) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    dayNumber: Int,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    isInRange: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isInRange -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else -> Color.Transparent
    }
    
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = if (isToday) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
            .clickable(enabled = true, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dayNumber.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Helper function to parse a date string in YYYY-MM-DD format to LocalDate.
 */
private fun parseDateStringToLocalDate(dateStr: String?): LocalDate? {
    if (dateStr == null || dateStr.isEmpty()) return null
    
    return try {
        val parts = dateStr.split("-")
        if (parts.size == 3) {
            val year = parts[0].toIntOrNull() ?: return null
            val month = parts[1].toIntOrNull()?.coerceIn(1, 12) ?: return null
            val day = parts[2].toIntOrNull()?.coerceIn(1, 31) ?: return null
            LocalDate(year, month, day)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Formats a LocalDate to YYYY-MM-DD string format.
 */
private fun formatLocalDateToString(date: LocalDate): String {
    return "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
}

/**
 * Formats a LocalDate to readable format (DD MMM, YYYY).
 */
private fun formatLocalDate(date: LocalDate?): String {
    if (date == null) return "Not selected"
    
    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    val monthName = monthNames.getOrNull(date.monthNumber - 1) ?: ""
    return "${date.dayOfMonth} $monthName, ${date.year}"
}

/**
 * Formats month and year for display (e.g., "January 2024").
 */
private fun formatMonthYear(date: LocalDate): String {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val monthName = monthNames.getOrNull(date.monthNumber - 1) ?: ""
    return "$monthName ${date.year}"
}

/**
 * Formats a date string (YYYY-MM-DD) to a readable format (DD MMM, YYYY).
 * Example: "2024-01-15" -> "15 Jan, 2024"
 */
fun formatDateString(dateStr: String?): String {
    if (dateStr == null || dateStr.isEmpty()) return ""
    
    val date = parseDateStringToLocalDate(dateStr) ?: return dateStr
    return formatLocalDate(date)
}

/**
 * A reusable single date picker dialog for selecting a date using a calendar view.
 * Dismisses automatically when a date is clicked.
 * 
 * @param initialDate The initial date as timestamp in milliseconds (optional)
 * @param onConfirm Callback when user selects a date - returns timestamp in milliseconds
 * @param onDismiss Callback when user dismisses the dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleDatePickerDialog(
    initialDate: Long? = null,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val currentTime = Clock.System.now().toEpochMilliseconds()
    val initialTime = initialDate ?: currentTime
    val instant = Instant.fromEpochMilliseconds(initialTime)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val initialDateStr = formatLocalDateToString(localDateTime.date)
    
    DateRangePickerDialog(
        initialStartDate = initialDateStr,
        initialEndDate = null,
        mode = DatePickerMode.SINGLE_DATE,
        onConfirm = { startDate, _ ->
            // Convert YYYY-MM-DD to timestamp (start of day)
            val date = parseDateStringToLocalDate(startDate)
            if (date != null) {
                val dateTime = date.atStartOfDayIn(TimeZone.currentSystemDefault())
                val timestamp = dateTime.toEpochMilliseconds()
                onConfirm(timestamp)
            }
        },
        onDismiss = onDismiss
    )
}

/**
 * A clickable date field that shows a formatted date and opens the date range picker dialog.
 * 
 * @param label The label for the date field
 * @param dateString The date string in YYYY-MM-DD format
 * @param onClick Callback when the field is clicked
 */
@Composable
fun DateRangeField(
    label: String,
    dateString: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        OutlinedTextField(
            value = formatDateString(dateString),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.CalendarToday, "Date") },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}