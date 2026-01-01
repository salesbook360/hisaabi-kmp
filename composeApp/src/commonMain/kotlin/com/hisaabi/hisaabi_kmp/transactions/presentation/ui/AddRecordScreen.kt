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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.core.ui.LocalWindowSizeClass
import com.hisaabi.hisaabi_kmp.core.ui.WindowWidthSizeClass
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionState
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddRecordViewModel
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.utils.SimpleDateTimePickerDialog
import com.hisaabi.hisaabi_kmp.utils.formatDateTime
import kotlinx.datetime.Clock
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordScreen(
    viewModel: AddRecordViewModel,
    onNavigateBack: (successMessage: String?, transactionSlug: String?) -> Unit,
    onSelectParty: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDateTimePicker by remember { mutableStateOf(false) }
    var showRemindDateTimePicker by remember { mutableStateOf(false) }
    var isSelectingDateTime by remember { mutableStateOf(false) }
    
    // Currency
    val preferencesManager: PreferencesManager = koinInject()
    val selectedCurrency by preferencesManager.selectedCurrency.collectAsState(null)
    val currencySymbol = selectedCurrency?.symbol ?: ""
    
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { message ->
            val transactionSlug = state.savedTransactionSlug
            viewModel.clearSuccess()
            onNavigateBack(message, transactionSlug)
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("New Record") },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack(null, null) }) {
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
        val windowSizeClass = LocalWindowSizeClass.current
        val isDesktop = windowSizeClass.widthSizeClass == WindowWidthSizeClass.EXPANDED
        val maxContentWidth = if (isDesktop) 800.dp else Dp.Unspecified
        val horizontalPadding = if (isDesktop) 24.dp else 16.dp
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .then(if (isDesktop) Modifier.widthIn(max = maxContentWidth) else Modifier.fillMaxWidth())
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Record Type Selector
                RecordTypeSelector(
                selectedType = state.recordType,
                onTypeSelected = { viewModel.setRecordType(it) }
            )
            
            // Party Selection (conditional)
            val recordType = state.recordType
            if (recordType != null && AllTransactionTypes.requiresParty(recordType.value)) {
                PartySelectionCard(
                    selectedParty = state.selectedParty,
                    onSelectParty = onSelectParty,
                    onRemoveParty = { viewModel.selectParty(null) }
                )
            }
            
            // Amount Field (only for Cash Reminder)
            if (recordType != null && recordType == AllTransactionTypes.CASH_REMINDER) {
                OutlinedTextField(
                    value = state.amount,
                    onValueChange = { viewModel.setAmount(it) },
                    label = { Text("Promised Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Money, "Amount") },
                    prefix = { Text("$currencySymbol ") },
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
    selectedType: AllTransactionTypes?,
    onTypeSelected: (AllTransactionTypes) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val recordTypes = listOf(
        AllTransactionTypes.MEETING,
        AllTransactionTypes.TASK,
        AllTransactionTypes.CLIENT_NOTE,
        AllTransactionTypes.SELF_NOTE,
        AllTransactionTypes.CASH_REMINDER
    )
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedType?.let { AllTransactionTypes.getDisplayName(it.value) } ?: "",
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
            recordTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(AllTransactionTypes.getDisplayName(type.value)) },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            when (type) {
                                AllTransactionTypes.MEETING -> Icons.Default.Event
                                AllTransactionTypes.TASK -> Icons.Default.Task
                                AllTransactionTypes.CLIENT_NOTE -> Icons.Default.Note
                                AllTransactionTypes.SELF_NOTE -> Icons.Default.StickyNote2
                                AllTransactionTypes.CASH_REMINDER -> Icons.Default.Notifications
                                else -> Icons.Default.Description
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

