package com.hisaabi.hisaabi_kmp.transactions.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import com.hisaabi.hisaabi_kmp.transactions.domain.model.JournalAccount
import com.hisaabi.hisaabi_kmp.transactions.domain.model.JournalAccountType
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddJournalVoucherViewModel
import com.hisaabi.hisaabi_kmp.utils.SimpleDateTimePickerDialog
import com.hisaabi.hisaabi_kmp.utils.formatDateTime
import com.hisaabi.hisaabi_kmp.core.ui.FilterChipWithColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddJournalVoucherScreen(
    viewModel: AddJournalVoucherViewModel,
    onNavigateBack: () -> Unit,
    onSelectAccountType: () -> Unit,
    onSelectPaymentMethod: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDateTimePicker by remember { mutableStateOf(false) }

    // Show error message
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    // Navigate back on success
    LaunchedEffect(state.success) {
        if (state.success) {
            snackbarHostState.showSnackbar("Journal voucher saved successfully")
            viewModel.clearSuccess()
            onNavigateBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Journal Voucher") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.saveJournalVoucher() },
                icon = { Icon(Icons.Default.Save, "Save") },
                text = { Text("Save Voucher") },
                containerColor = if (state.isBalanced) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Section with Date and Totals
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Date
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDateTimePicker = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Date:",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                formatDateTime(state.dateTime),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.CalendarToday, "Change Date", modifier = Modifier.size(20.dp))
                        }
                    }

                    HorizontalDivider()

                    // Debit/Credit Totals
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Total Debit",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                "₨ ${"%.2f".format(state.totalDebit)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Total Credit",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                "₨ ${"%.2f".format(state.totalCredit)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Balance Status
                    if (state.accounts.isNotEmpty()) {
                        Surface(
                            color = if (state.isBalanced) 
                                MaterialTheme.colorScheme.tertiaryContainer 
                            else 
                                MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (state.isBalanced) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (state.isBalanced) "Balanced" else "Not Balanced",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Accounts List
            if (state.accounts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            "No accounts added",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = onSelectAccountType) {
                            Icon(Icons.Default.Add, "Add")
                            Spacer(Modifier.width(8.dp))
                            Text("Add Account")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(state.accounts) { index, account ->
                        JournalAccountCard(
                            account = account,
                            onAmountChange = { amount ->
                                viewModel.updateAccountAmount(index, amount)
                            },
                            onToggleDebitCredit = {
                                viewModel.toggleAccountDebitCredit(index)
                            },
                            onRemove = {
                                viewModel.removeAccount(index)
                            }
                        )
                    }

                    item {
                        Button(
                            onClick = onSelectAccountType,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, "Add")
                            Spacer(Modifier.width(8.dp))
                            Text("Add Another Account")
                        }
                    }
                }
            }

            // Bottom Section - Payment Method and Description
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Payment Method Selection
                PaymentMethodSelectionCard(
                    selectedPaymentMethod = state.selectedPaymentMethod,
                    onSelectPaymentMethod = onSelectPaymentMethod
                )

                // Description
                OutlinedTextField(
                    value = state.description,
                    onValueChange = { viewModel.setDescription(it) },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Description, "Description") },
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(Modifier.height(60.dp))
            }
        }

        // Loading overlay
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
private fun JournalAccountCard(
    account: JournalAccount,
    onAmountChange: (Double) -> Unit,
    onToggleDebitCredit: () -> Unit,
    onRemove: () -> Unit
) {
    var amountText by remember(account.amount) { 
        mutableStateOf(if (account.amount == 0.0) "" else account.amount.toString()) 
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header - Account Name and Remove Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        account.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        account.accountType.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Amount and Debit/Credit Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { 
                        amountText = it
                        it.toDoubleOrNull()?.let { amount -> onAmountChange(amount) }
                    },
                    label = { Text("Amount") },
                    modifier = Modifier.weight(1f),
                    prefix = { Text("₨ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                // Debit/Credit Toggle
                FilterChipWithColors(
                    selected = account.isDebit,
                    onClick = onToggleDebitCredit,
                    label = if (account.isDebit) "Debit" else "Credit",
                    leadingIcon = {
                        Icon(
                            if (account.isDebit) Icons.Default.Remove else Icons.Default.Add,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodSelectionCard(
    selectedPaymentMethod: PaymentMethod?,
    onSelectPaymentMethod: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelectPaymentMethod),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Payment Method *",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (selectedPaymentMethod != null) {
                    Text(
                        text = selectedPaymentMethod.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                } else {
                    Text(
                        text = "Tap to select",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
            Icon(
                if (selectedPaymentMethod != null) Icons.Default.Edit else Icons.Default.ArrowForwardIos,
                contentDescription = "Select Payment Method",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

