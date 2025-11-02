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
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.PaymentTransferViewModel
import com.hisaabi.hisaabi_kmp.utils.SimpleDateTimePickerDialog
import com.hisaabi.hisaabi_kmp.utils.formatDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentTransferScreen(
    viewModel: PaymentTransferViewModel,
    onNavigateBack: (success: Boolean) -> Unit,
    onSelectPaymentMethodFrom: () -> Unit,
    onSelectPaymentMethodTo: () -> Unit
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
            viewModel.clearSuccess()
            onNavigateBack(true)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Payment Transfer") },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack(false) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.saveTransaction() },
                icon = { Icon(Icons.Default.Save, "Save") },
                text = { Text("Save Transfer") }
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
            // Date & Time
            DateTimeField(
                label = "Transfer Date & Time",
                timestamp = state.dateTime,
                onDateTimeClick = { showDateTimePicker = true }
            )

            // Payment Method From
            PaymentMethodCard(
                title = "Transfer From *",
                selectedPaymentMethod = state.paymentMethodFrom,
                onSelectPaymentMethod = onSelectPaymentMethodFrom,
                icon = Icons.Default.CallMade,
                containerColor = MaterialTheme.colorScheme.errorContainer
            )

            // Payment Method To
            PaymentMethodCard(
                title = "Transfer To *",
                selectedPaymentMethod = state.paymentMethodTo,
                onSelectPaymentMethod = onSelectPaymentMethodTo,
                icon = Icons.Default.CallReceived,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )

            // Amount
            OutlinedTextField(
                value = state.amount,
                onValueChange = { viewModel.setAmount(it) },
                label = { Text("Amount *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Money, "Amount") },
                prefix = { Text("₨ ") },
                singleLine = true
            )

            // Description
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.setDescription(it) },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Description, "Description") },
                minLines = 3,
                maxLines = 5
            )

            Spacer(Modifier.height(80.dp))
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
private fun DateTimeField(
    label: String,
    timestamp: Long,
    onDateTimeClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDateTimeClick)
    ) {
        OutlinedTextField(
            value = formatDateTime(timestamp),
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

@Composable
private fun PaymentMethodCard(
    title: String,
    selectedPaymentMethod: PaymentMethod?,
    onSelectPaymentMethod: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (selectedPaymentMethod != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    onClick = onSelectPaymentMethod
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                selectedPaymentMethod.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(Icons.Default.Edit, "Change", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                Button(
                    onClick = onSelectPaymentMethod,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, "Add")
                    Spacer(Modifier.width(8.dp))
                    Text("Select Payment Method")
                }
            }
        }
    }
}

