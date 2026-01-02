package com.hisaabi.hisaabi_kmp.transactions.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.core.ui.FilterChipWithColors
import com.hisaabi.hisaabi_kmp.core.ui.LocalWindowSizeClass
import com.hisaabi.hisaabi_kmp.core.ui.WindowWidthSizeClass
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.PayGetCashType
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.PayGetCashViewModel
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.core.ui.SingleDatePickerDialog
import com.hisaabi.hisaabi_kmp.utils.format
import com.hisaabi.hisaabi_kmp.utils.formatDateTime
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayGetCashScreen(
    viewModel: PayGetCashViewModel,
    onNavigateBack: (successMessage: String?, transactionSlug: String?) -> Unit,
    onSelectParty: (PartyType) -> Unit,
    onSelectPaymentMethod: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDateTimePicker by remember { mutableStateOf(false) }
    
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
                title = { Text("Pay or Get Payment") },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack(null, null) }) {
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
        val windowSizeClass = LocalWindowSizeClass.current
        val isDesktop = windowSizeClass.widthSizeClass == WindowWidthSizeClass.EXPANDED
        val maxContentWidth = if (isDesktop) 700.dp else Dp.Unspecified
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
                        PayGetCashType.values().forEach { type ->
                            FilterChipWithColors(
                                selected = state.payGetCashType == type,
                                onClick = { viewModel.setPayGetCashType(type) },
                                label = type.displayName,
                                modifier = Modifier.weight(1f),
                                leadingIcon = {
                                    Icon(
                                        tint = if (state.payGetCashType == type) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        imageVector = if (type == PayGetCashType.GET_CASH)
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

            // Party Type Selection - Two columns on desktop/tablet
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

                    if (isDesktop || windowSizeClass.widthSizeClass == WindowWidthSizeClass.MEDIUM) {
                        // Two-column layout on desktop/tablet
                        val partyTypes = PartyType.values()
                        val halfSize = (partyTypes.size + 1) / 2 // Round up division
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Left column
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                partyTypes.take(halfSize).forEach { type ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .selectable(
                                                selected = state.partyType == type,
                                                onClick = { viewModel.setPartyType(type) },
                                                role = Role.RadioButton
                                            )
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = state.partyType == type,
                                            onClick = null
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            type.displayName,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                            
                            // Right column
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                partyTypes.drop(halfSize).forEach { type ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .selectable(
                                                selected = state.partyType == type,
                                                onClick = { viewModel.setPartyType(type) },
                                                role = Role.RadioButton
                                            )
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = state.partyType == type,
                                            onClick = null
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            type.displayName,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Single column layout on mobile
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
            }

            // Party Selection
            PartySelectionCard(
                selectedParty = state.selectedParty,
                partyType = state.partyType,
                onSelectParty = { onSelectParty(state.partyType) },
                onRemoveParty = { viewModel.selectParty(null) },
                currencySymbol
            )

            // Amount and Date/Time - Side by side on desktop
            if (isDesktop) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Amount
                    OutlinedTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        value = state.amount,
                        onValueChange = { viewModel.setAmount(it) },
                        label = { Text("Amount *") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.Money, "Amount") },
                        prefix = { Text("$currencySymbol ") },
                        singleLine = true,
                        placeholder = { Text("0.00") }
                    )

                    // Date & Time
                    Box(
                        modifier = Modifier
                            .weight(1f)
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
                }
            } else {
                // Amount - Full width on mobile
                OutlinedTextField(
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    value = state.amount,
                    onValueChange = { viewModel.setAmount(it) },
                    label = { Text("Amount *") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Money, "Amount") },
                    prefix = { Text("$currencySymbol ") },
                    singleLine = true,
                    placeholder = { Text("0.00") }
                )

                // Date & Time - Full width on mobile
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
            }

            // Payment Method
            PaymentMethodCard(
                selectedPaymentMethod = state.selectedPaymentMethod,
                onSelectPaymentMethod = onSelectPaymentMethod,
                onRemovePaymentMethod = { viewModel.selectPaymentMethod(null) }
            )

            // Description/Remarks - More compact on desktop
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.setDescription(it) },
                label = { Text("Remarks (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = if (isDesktop) 2 else 3,
                maxLines = if (isDesktop) 4 else 6
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
        SingleDatePickerDialog(
            initialDate = state.dateTime,
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
    onRemoveParty: () -> Unit,
    currencySymbol:String
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
                            "Balance: $currencySymbol ${"%.2f".format(selectedParty.balance)}",
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
