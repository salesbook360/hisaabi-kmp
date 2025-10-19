package com.hisaabi.hisaabi_kmp.transactions.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.transactions.domain.model.RecordType
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionState
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddRecordViewModel
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordScreen(
    viewModel: AddRecordViewModel,
    onNavigateBack: () -> Unit,
    onSelectParty: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDateTimePicker by remember { mutableStateOf(false) }
    var showRemindDateTimePicker by remember { mutableStateOf(false) }
    var isSelectingDateTime by remember { mutableStateOf(false) }
    
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccess()
            onNavigateBack()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("New Record") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.saveRecord() },
                icon = { Icon(Icons.Default.Save, "Save") },
                text = { Text("Save Record") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Record Type Selector
            RecordTypeSelector(
                selectedType = state.recordType,
                onTypeSelected = { viewModel.setRecordType(it) }
            )
            
            // Party Selection (conditional)
            val recordType = state.recordType
            if (recordType != null && RecordType.requiresParty(recordType)) {
                PartySelectionCard(
                    selectedParty = state.selectedParty,
                    onSelectParty = onSelectParty,
                    onRemoveParty = { viewModel.selectParty(null) }
                )
            }
            
            // Amount Field (only for Cash Reminder)
            if (recordType != null && RecordType.showsAmountField(recordType)) {
                OutlinedTextField(
                    value = state.amount,
                    onValueChange = { viewModel.setAmount(it) },
                    label = { Text("Promised Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Money, "Amount") },
                    prefix = { Text("₨ ") },
                    singleLine = true
                )
            }
            
            // State Selector
            StateSelector(
                selectedState = state.state,
                onStateSelected = { viewModel.setState(it) }
            )
            
            // Date & Time
            DateTimeField(
                label = "Date & Time",
                timestamp = state.dateTime,
                onDateTimeClick = { 
                    isSelectingDateTime = true
                    showDateTimePicker = true
                }
            )
            
            // Remind Me Date & Time
            DateTimeField(
                label = "Remind Me At",
                timestamp = state.remindDateTime,
                onDateTimeClick = { 
                    isSelectingDateTime = false
                    showRemindDateTimePicker = true
                },
                optional = true,
                onClear = { viewModel.setRemindDateTime(null) }
            )
            
            // Description
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.setDescription(it) },
                label = { Text("Description *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 8
            )
            
            // Bottom padding for FAB
            Spacer(Modifier.height(80.dp))
        }
        
        // Loading overlay
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
    
    // Date Time Picker Dialog
    if (showDateTimePicker) {
        SimpleDateTimePickerDialog(
            initialTimestamp = state.dateTime,
            onConfirm = { timestamp ->
                viewModel.setDateTime(timestamp)
                showDateTimePicker = false
            },
            onDismiss = { showDateTimePicker = false }
        )
    }
    
    // Remind Date Time Picker Dialog
    if (showRemindDateTimePicker) {
        SimpleDateTimePickerDialog(
            initialTimestamp = state.remindDateTime ?: Clock.System.now().toEpochMilliseconds(),
            onConfirm = { timestamp ->
                viewModel.setRemindDateTime(timestamp)
                showRemindDateTimePicker = false
            },
            onDismiss = { showRemindDateTimePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordTypeSelector(
    selectedType: RecordType?,
    onTypeSelected: (RecordType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedType?.displayName ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Record Type *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            RecordType.values().forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.displayName) },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            when (type) {
                                RecordType.MEETING -> Icons.Default.Event
                                RecordType.TASK -> Icons.Default.Task
                                RecordType.CLIENT_NOTE -> Icons.Default.Note
                                RecordType.SELF_NOTE -> Icons.Default.StickyNote2
                                RecordType.CASH_REMINDER -> Icons.Default.Notifications
                            },
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StateSelector(
    selectedState: TransactionState,
    onStateSelected: (TransactionState) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedState.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("State") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            TransactionState.values().forEach { transactionState ->
                DropdownMenuItem(
                    text = { Text(transactionState.displayName) },
                    onClick = {
                        onStateSelected(transactionState)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PartySelectionCard(
    selectedParty: Party?,
    onSelectParty: () -> Unit,
    onRemoveParty: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selectedParty == null) 
                MaterialTheme.colorScheme.errorContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        if (selectedParty == null) {
            // Show selection button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSelectParty)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Select Party *",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        } else {
            // Show selected party
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            selectedParty.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        selectedParty.phone?.let { phone ->
                            Text(
                                phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                IconButton(onClick = onRemoveParty) {
                    Icon(Icons.Default.Close, "Remove")
                }
            }
        }
    }
}

@Composable
private fun DateTimeField(
    label: String,
    timestamp: Long?,
    onDateTimeClick: () -> Unit,
    optional: Boolean = false,
    onClear: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDateTimeClick)
    ) {
        OutlinedTextField(
            value = timestamp?.let { formatDateTime(it) } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(if (optional) "$label (Optional)" else label) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.CalendarToday, "Date") },
            trailingIcon = {
                if (optional && timestamp != null && onClear != null) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Clear, "Clear")
                    }
                }
            },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

private fun formatDateTime(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year} ${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDateTimePickerDialog(
    initialTimestamp: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val instant = Instant.fromEpochMilliseconds(initialTimestamp)
    val initialDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    
    var selectedDate by remember { mutableStateOf(initialDateTime.date) }
    var selectedHour by remember { mutableStateOf(initialDateTime.hour) }
    var selectedMinute by remember { mutableStateOf(initialDateTime.minute) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date & Time") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date Section
                Text(
                    "Date: ${selectedDate.dayOfMonth}/${selectedDate.monthNumber}/${selectedDate.year}",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Day
                    OutlinedTextField(
                        value = selectedDate.dayOfMonth.toString(),
                        onValueChange = { value ->
                            value.toIntOrNull()?.let { day ->
                                if (day in 1..31) {
                                    selectedDate = try {
                                        LocalDate(selectedDate.year, selectedDate.month, day)
                                    } catch (e: Exception) {
                                        selectedDate
                                    }
                                }
                            }
                        },
                        label = { Text("Day") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    // Month
                    OutlinedTextField(
                        value = selectedDate.monthNumber.toString(),
                        onValueChange = { value ->
                            value.toIntOrNull()?.let { month ->
                                if (month in 1..12) {
                                    selectedDate = try {
                                        LocalDate(selectedDate.year, month, selectedDate.dayOfMonth)
                                    } catch (e: Exception) {
                                        selectedDate
                                    }
                                }
                            }
                        },
                        label = { Text("Month") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    // Year
                    OutlinedTextField(
                        value = selectedDate.year.toString(),
                        onValueChange = { value ->
                            value.toIntOrNull()?.let { year ->
                                if (year in 1900..2100) {
                                    selectedDate = try {
                                        LocalDate(year, selectedDate.month, selectedDate.dayOfMonth)
                                    } catch (e: Exception) {
                                        selectedDate
                                    }
                                }
                            }
                        },
                        label = { Text("Year") },
                        modifier = Modifier.weight(1.5f),
                        singleLine = true
                    )
                }
                
                HorizontalDivider()
                
                // Time Section
                Text(
                    "Time: ${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Hour
                    OutlinedTextField(
                        value = selectedHour.toString(),
                        onValueChange = { value ->
                            value.toIntOrNull()?.let { hour ->
                                if (hour in 0..23) {
                                    selectedHour = hour
                                }
                            }
                        },
                        label = { Text("Hour (0-23)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    // Minute
                    OutlinedTextField(
                        value = selectedMinute.toString(),
                        onValueChange = { value ->
                            value.toIntOrNull()?.let { minute ->
                                if (minute in 0..59) {
                                    selectedMinute = minute
                                }
                            }
                        },
                        label = { Text("Minute (0-59)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val dateTime = LocalDateTime(
                        selectedDate.year,
                        selectedDate.month,
                        selectedDate.dayOfMonth,
                        selectedHour,
                        selectedMinute
                    )
                    val timestamp = dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                    onConfirm(timestamp)
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

