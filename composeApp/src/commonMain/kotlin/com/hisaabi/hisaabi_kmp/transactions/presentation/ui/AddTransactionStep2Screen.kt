package com.hisaabi.hisaabi_kmp.transactions.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
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
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import com.hisaabi.hisaabi_kmp.transactions.domain.model.FlatOrPercent
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddTransactionViewModel
import com.hisaabi.hisaabi_kmp.core.ui.FilterChipWithColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionStep2Screen(
    viewModel: AddTransactionViewModel,
    onNavigateBack: () -> Unit,
    onSelectPaymentMethod: () -> Unit,
    onTransactionSaved: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
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
            onTransactionSaved()
        }
    }
    
    // Navigate back to step 1 when user goes back
    LaunchedEffect(state.currentStep) {
        if (state.currentStep == 1) {
            onNavigateBack()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Payment Details") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goToStep1() }) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Previous Balance Card
            item {
                PreviousBalanceCard(
                    partyName = state.selectedParty?.name ?: "",
                    balance = state.previousBalance
                )
            }
            
            // Bill Summary Card
            item {
                BillSummaryCard(
                    subtotal = viewModel.calculateSubtotal(),
                    productsDiscount = viewModel.calculateProductsDiscount(),
                    productsTax = viewModel.calculateProductsTax(),
                    additionalCharges = state.additionalCharges,
                    transactionDiscount = viewModel.calculateTransactionDiscount(),
                    transactionTax = viewModel.calculateTransactionTax(),
                    grandTotal = viewModel.calculateGrandTotal()
                )
            }
            
            // Additional Charges
            item {
                AdditionalChargesSection(
                    amount = state.additionalCharges,
                    description = state.additionalChargesDesc,
                    onAmountChange = { amount, desc -> 
                        viewModel.updateAdditionalCharges(amount, desc)
                    }
                )
            }
            
            // Transaction Discount
            item {
                DiscountSection(
                    amount = state.flatDiscount,
                    type = state.discountType,
                    onUpdate = { amount, type ->
                        viewModel.updateDiscount(amount, type)
                    }
                )
            }
            
            // Transaction Tax
            item {
                TaxSection(
                    amount = state.flatTax,
                    type = state.taxType,
                    onUpdate = { amount, type ->
                        viewModel.updateTax(amount, type)
                    }
                )
            }
            
            // Paid Now
            item {
                PaidNowSection(
                    amount = state.paidNow,
                    grandTotal = viewModel.calculateGrandTotal(),
                    onAmountChange = { viewModel.updatePaidNow(it) }
                )
            }
            
            // Payable Amount Display
            item {
                PayableAmountCard(
                    payable = state.totalPayable
                )
            }
            
            // Payment Method
            item {
                PaymentMethodSection(
                    selectedMethod = state.selectedPaymentMethod,
                    onSelectMethod = onSelectPaymentMethod,
                    showError = state.error?.contains("payment method", ignoreCase = true) == true
                )
            }
            
            // Remarks
            item {
                RemarksSection(
                    remarks = state.remarks,
                    onRemarksChange = { viewModel.updateRemarks(it) }
                )
            }
            
            // Shipping Address
            item {
                ShippingAddressSection(
                    address = state.shippingAddress,
                    onAddressChange = { viewModel.updateShippingAddress(it) }
                )
            }
            
            // Attachments
            item {
                AttachmentsSection(
                    attachments = state.attachments,
                    onAddAttachment = { /* TODO: Implement file picker */ },
                    onRemoveAttachment = { index -> viewModel.removeAttachment(index) }
                )
            }
            
            // Bottom padding for FAB
            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun PreviousBalanceCard(
    partyName: String,
    balance: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                balance > 0 -> MaterialTheme.colorScheme.errorContainer
                balance < 0 -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Previous Balance",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    partyName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    when {
                        balance > 0 -> "Payable"
                        balance < 0 -> "Receivable"
                        else -> "Settled"
                    },
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    "₨ ${String.format("%.2f", kotlin.math.abs(balance))}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BillSummaryCard(
    subtotal: Double,
    productsDiscount: Double,
    productsTax: Double,
    additionalCharges: Double,
    transactionDiscount: Double,
    transactionTax: Double,
    grandTotal: Double
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Bill Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            
            SummaryRow("Subtotal", subtotal)
            if (productsDiscount > 0) {
                SummaryRow("Products Discount", -productsDiscount, isNegative = true)
            }
            if (productsTax > 0) {
                SummaryRow("Products Tax", productsTax)
            }
            if (additionalCharges > 0) {
                SummaryRow("Additional Charges", additionalCharges)
            }
            if (transactionDiscount > 0) {
                SummaryRow("Transaction Discount", -transactionDiscount, isNegative = true)
            }
            if (transactionTax > 0) {
                SummaryRow("Transaction Tax", transactionTax)
            }
            
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Grand Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "₨ ${String.format("%.2f", grandTotal)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    amount: Double,
    isNegative: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "₨ ${String.format("%.2f", kotlin.math.abs(amount))}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isNegative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AdditionalChargesSection(
    amount: Double,
    description: String,
    onAmountChange: (Double, String) -> Unit
) {
    var amountText by remember { mutableStateOf(if (amount > 0) amount.toString() else "") }
    var descText by remember { mutableStateOf(description) }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Additional Charges",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            
            OutlinedTextField(
                value = amountText,
                onValueChange = { 
                    amountText = it
                    it.toDoubleOrNull()?.let { amt ->
                        onAmountChange(amt, descText)
                    }
                },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text("₨ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            
            Spacer(Modifier.height(8.dp))
            
            OutlinedTextField(
                value = descText,
                onValueChange = { 
                    descText = it
                    amountText.toDoubleOrNull()?.let { amt ->
                        onAmountChange(amt, it)
                    }
                },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )
        }
    }
}

@Composable
private fun DiscountSection(
    amount: Double,
    type: FlatOrPercent,
    onUpdate: (Double, FlatOrPercent) -> Unit
) {
    var amountText by remember { mutableStateOf(if (amount > 0) amount.toString() else "") }
    var selectedType by remember { mutableStateOf(type) }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Transaction Discount",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { 
                        amountText = it
                        it.toDoubleOrNull()?.let { amt ->
                            onUpdate(amt, selectedType)
                        }
                    },
                    label = { Text("Discount") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                
                FilterChipWithColors(
                    selected = selectedType == FlatOrPercent.FLAT,
                    onClick = { 
                        selectedType = FlatOrPercent.FLAT
                        amountText.toDoubleOrNull()?.let { amt ->
                            onUpdate(amt, selectedType)
                        }
                    },
                    label = "₨"
                )
                FilterChipWithColors(
                    selected = selectedType == FlatOrPercent.PERCENT,
                    onClick = { 
                        selectedType = FlatOrPercent.PERCENT
                        amountText.toDoubleOrNull()?.let { amt ->
                            onUpdate(amt, selectedType)
                        }
                    },
                    label = "%"
                )
            }
        }
    }
}

@Composable
private fun TaxSection(
    amount: Double,
    type: FlatOrPercent,
    onUpdate: (Double, FlatOrPercent) -> Unit
) {
    var amountText by remember { mutableStateOf(if (amount > 0) amount.toString() else "") }
    var selectedType by remember { mutableStateOf(type) }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Transaction Tax",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { 
                        amountText = it
                        it.toDoubleOrNull()?.let { amt ->
                            onUpdate(amt, selectedType)
                        }
                    },
                    label = { Text("Tax") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                
                FilterChipWithColors(
                    selected = selectedType == FlatOrPercent.FLAT,
                    onClick = { 
                        selectedType = FlatOrPercent.FLAT
                        amountText.toDoubleOrNull()?.let { amt ->
                            onUpdate(amt, selectedType)
                        }
                    },
                    label = "₨"
                )
                FilterChipWithColors(
                    selected = selectedType == FlatOrPercent.PERCENT,
                    onClick = { 
                        selectedType = FlatOrPercent.PERCENT
                        amountText.toDoubleOrNull()?.let { amt ->
                            onUpdate(amt, selectedType)
                        }
                    },
                    label = "%"
                )
            }
        }
    }
}

@Composable
private fun PaidNowSection(
    amount: Double,
    grandTotal: Double,
    onAmountChange: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf(if (amount > 0) amount.toString() else "") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Paid Now",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { 
                        amountText = grandTotal.toString()
                        onAmountChange(grandTotal)
                    }
                ) {
                    Text("Pay Full")
                }
            }
            Spacer(Modifier.height(12.dp))
            
            OutlinedTextField(
                value = amountText,
                onValueChange = { 
                    amountText = it
                    it.toDoubleOrNull()?.let { amt ->
                        onAmountChange(amt)
                    }
                },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text("₨ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
    }
}

@Composable
private fun PayableAmountCard(
    payable: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (payable > 0) 
                MaterialTheme.colorScheme.errorContainer 
            else 
                MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (payable > 0) "To be paid" else "Remaining balance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "₨ ${String.format("%.2f", kotlin.math.abs(payable))}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PaymentMethodSection(
    selectedMethod: PaymentMethod?,
    onSelectMethod: () -> Unit,
    showError: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelectMethod),
        colors = CardDefaults.cardColors(
            containerColor = if (showError && selectedMethod == null)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Payment,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (showError && selectedMethod == null)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Payment Method",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (showError && selectedMethod == null)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    selectedMethod?.title ?: "Select Payment Method",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (showError && selectedMethod == null)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                if (showError && selectedMethod == null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Payment method is required",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
private fun RemarksSection(
    remarks: String,
    onRemarksChange: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Remarks / Notes",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            
            OutlinedTextField(
                value = remarks,
                onValueChange = onRemarksChange,
                label = { Text("Add notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                minLines = 2
            )
        }
    }
}

@Composable
private fun ShippingAddressSection(
    address: String,
    onAddressChange: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Shipping Address",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            
            OutlinedTextField(
                value = address,
                onValueChange = onAddressChange,
                label = { Text("Enter address (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                minLines = 2
            )
        }
    }
}

@Composable
private fun AttachmentsSection(
    attachments: List<String>,
    onAddAttachment: () -> Unit,
    onRemoveAttachment: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Attachments",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onAddAttachment) {
                    Icon(Icons.Default.AttachFile, "Add", Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add")
                }
            }
            
            if (attachments.isEmpty()) {
                Text(
                    "No attachments added",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Spacer(Modifier.height(8.dp))
                attachments.forEachIndexed { index, attachment ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.InsertDriveFile,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                attachment,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        }
                        IconButton(onClick = { onRemoveAttachment(index) }) {
                            Icon(
                                Icons.Default.Close,
                                "Remove",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

