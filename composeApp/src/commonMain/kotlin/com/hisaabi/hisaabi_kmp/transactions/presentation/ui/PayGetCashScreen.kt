package com.hisaabi.hisaabi_kmp.transactions.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import com.hisaabi.hisaabi_kmp.transactions.domain.model.PayGetCashType
import com.hisaabi.hisaabi_kmp.transactions.domain.model.PartyType
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.PayGetCashViewModel
import com.hisaabi.hisaabi_kmp.utils.SimpleDateTimePickerDialog
import com.hisaabi.hisaabi_kmp.utils.formatDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayGetCashScreen(
    viewModel: PayGetCashViewModel,
    onNavigateBack: () -> Unit,
    onSelectParty: (PartyType) -> Unit,
    onSelectPaymentMethod: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDateTimePicker by remember { mutableStateOf(false) }
    
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
                title = { Text("Pay or Get Payment") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.saveTransaction() },
                icon = { Icon(Icons.Default.Save, "Save") },
                text = { Text("Save Transaction") }
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
            // Pay/Get Payment Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .selectableGroup()
                ) {
                    Text(
                        "Select Transaction Type",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PartyType.values().forEach { type ->
                            FilterChip(
                                selected = state.payGetCashType == type,
                                onClick = { viewModel.setPayGetCashType(type) },
                                label = { Text(type.displayName) },
                                modifier = Modifier.weight(1f),
                                leadingIcon = {
                                    Icon(
                                        if (type == PayGetCashType.GET_CASH) 
                                            Icons.Default.CallReceived 
                                        else 
                                            Icons.Default.CallMade,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
            }
            
            // Party Type Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .selectableGroup()
                ) {
                    Text(
                        "${if (state.payGetCashType == PayGetCashType.GET_CASH) "From" else "To"} Party Type",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    PartyType.values().forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = state.partyType == type,
                                    onClick = { viewModel.setPartyType(type) },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.partyType == type,
                                onClick = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${if (state.payGetCashType == PayGetCashType.GET_CASH) "From" else "To"} ${type.displayName}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
            
            // Party Selection
            PartySelectionCard(
                selectedParty = state.selectedParty,
                partyType = state.partyType,
                onSelectParty = { onSelectParty(state.partyType) },
                onRemoveParty = { viewModel.selectParty(null) }
            )
            
            // Amount
            OutlinedTextField(
                value = state.amount,
                onValueChange = { viewModel.setAmount(it) },
                label = { Text("Amount *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Money, "Amount") },
                prefix = { Text("₨ ") },
                singleLine = true,
                placeholder = { Text("0.00") }
            )
            
            // Payment Method
            PaymentMethodCard(
                selectedPaymentMethod = state.selectedPaymentMethod,
                onSelectPaymentMethod = onSelectPaymentMethod,
                onRemovePaymentMethod = { viewModel.selectPaymentMethod(null) }
            )
            
            // Date & Time
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDateTimePicker = true }
            ) {
                OutlinedTextField(
                    value = formatDateTime(state.dateTime),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date & Time") },
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
            
            // Description/Remarks
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.setDescription(it) },
                label = { Text("Remarks (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6
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
}

@Composable
private fun PartySelectionCard(
    selectedParty: Party?,
    partyType: PartyType,
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
                        "Select ${partyType.displayName} *",
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
                        // Show balance
                        Text(
                            "Balance: ₨ ${"%.2f".format(selectedParty.balance)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedParty.balance >= 0) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.error
                        )
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
private fun PaymentMethodCard(
    selectedPaymentMethod: PaymentMethod?,
    onSelectPaymentMethod: () -> Unit,
    onRemovePaymentMethod: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selectedPaymentMethod == null) 
                MaterialTheme.colorScheme.errorContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        if (selectedPaymentMethod == null) {
            // Show selection button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSelectPaymentMethod)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Payment,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Select Payment Method *",
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
            // Show selected payment method
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
                        Icons.Default.Payment,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        selectedPaymentMethod.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onRemovePaymentMethod) {
                    Icon(Icons.Default.Close, "Remove")
                }
            }
        }
    }
}
