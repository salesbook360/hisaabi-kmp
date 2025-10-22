package com.hisaabi.hisaabi_kmp.transactions.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaabi.hisaabi_kmp.transactions.domain.model.*
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.TransactionDetailViewModel
import com.hisaabi.hisaabi_kmp.utils.format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    viewModel: TransactionDetailViewModel,
    transactionSlug: String,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(transactionSlug) {
        viewModel.loadTransaction(transactionSlug)
    }
    
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(state.transaction?.getTransactionTypeName() ?: "Transaction Details") 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Edit action
                    IconButton(onClick = { /* TODO: Navigate to edit */ }) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    // Share action
                    IconButton(onClick = { /* TODO: Share transaction */ }) {
                        Icon(Icons.Default.Share, "Share")
                    }
                    // More options
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "More")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Generate Receipt") },
                                onClick = { 
                                    showMenu = false
                                    // TODO: Generate receipt
                                },
                                leadingIcon = { Icon(Icons.Default.Receipt, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Print") },
                                onClick = { 
                                    showMenu = false
                                    // TODO: Print transaction
                                },
                                leadingIcon = { Icon(Icons.Default.Print, null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                onClick = { 
                                    showMenu = false
                                    // TODO: Delete transaction
                                },
                                leadingIcon = { 
                                    Icon(
                                        Icons.Default.Delete, 
                                        null,
                                        tint = MaterialTheme.colorScheme.error
                                    ) 
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.transaction != null -> {
                TransactionDetailContent(
                    transaction = state.transaction!!,
                    childTransactions = state.childTransactions,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Transaction not found")
                }
            }
        }
    }
}

@Composable
private fun TransactionDetailContent(
    transaction: Transaction,
    childTransactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    val isRecordType = AllTransactionTypes.isRecord(transaction.transactionType)
    val isPayGetCashType = AllTransactionTypes.isPayGetCash(transaction.transactionType)
    val isExpenseIncomeType = AllTransactionTypes.isExpenseIncome(transaction.transactionType)
    val isPaymentTransferType = AllTransactionTypes.isPaymentTransfer(transaction.transactionType)
    val isJournalVoucherType = AllTransactionTypes.isJournalVoucher(transaction.transactionType)
    val isStockAdjustmentType = AllTransactionTypes.isStockAdjustment(transaction.transactionType)
    val isRegularTransaction = !isRecordType && !isPayGetCashType && !isExpenseIncomeType && 
                                !isPaymentTransferType && !isJournalVoucherType && !isStockAdjustmentType
    
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Transaction type badge
        item {
            Surface(
                color = getTransactionColor(transaction),
                shape = MaterialTheme.shapes.medium
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
                            transaction.getTransactionTypeName(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            transaction.timestamp ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // State badge
                    Surface(
                        color = when (transaction.stateId) {
                            TransactionState.COMPLETED.value -> MaterialTheme.colorScheme.primaryContainer
                            TransactionState.PENDING.value -> MaterialTheme.colorScheme.tertiaryContainer
                            TransactionState.IN_PROGRESS.value -> MaterialTheme.colorScheme.secondaryContainer
                            TransactionState.CANCELLED.value -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            transaction.getStateName(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        // Party Information (if applicable)
        if (transaction.party != null) {
            item {
                PartyInfoCard(transaction.party)
            }
        }
        
        // Transaction-specific content based on type
        when {
            isRecordType -> {
                item { RecordDetailsCard(transaction) }
            }
            isPayGetCashType -> {
                item { PayGetCashDetailsCard(transaction) }
            }
            isExpenseIncomeType -> {
                item { ExpenseIncomeDetailsCard(transaction) }
            }
            isPaymentTransferType -> {
                item { PaymentTransferDetailsCard(transaction) }
            }
            isJournalVoucherType -> {
                item { JournalVoucherDetailsCard(transaction) }
                
                // Show child transactions for journal voucher
                if (childTransactions.isNotEmpty()) {
                    item {
                        Text(
                            "Related Transactions (${childTransactions.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    items(childTransactions) { childTransaction ->
                        ChildTransactionCard(childTransaction)
                    }
                }
            }
            isStockAdjustmentType -> {
                item { StockAdjustmentDetailsCard(transaction) }
            }
            isRegularTransaction -> {
                // Amount Summary Card
                item { AmountSummaryCard(transaction) }
                
                // Transaction Details (Products)
                if (transaction.transactionDetails.isNotEmpty()) {
                    item {
                        Text(
                            "Products (${transaction.transactionDetails.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    items(transaction.transactionDetails) { detail ->
                        ProductDetailCard(detail)
                    }
                }
                
                // Additional Details Card
                item { AdditionalDetailsCard(transaction) }
            }
        }
        
        // Warehouse Information (if applicable)
        if (transaction.warehouseFrom != null || transaction.warehouseTo != null) {
            item { WarehouseInfoCard(transaction) }
        }
        
        // Payment Method Information (if applicable)
        if (transaction.paymentMethodTo != null || transaction.paymentMethodFrom != null) {
            item { PaymentMethodInfoCard(transaction) }
        }
        
        // Description (if available)
        if (!transaction.description.isNullOrBlank()) {
            item { DescriptionCard(transaction.description!!) }
        }
        
        // Metadata Card
        item { MetadataCard(transaction) }
    }
}

@Composable
private fun PartyInfoCard(party: com.hisaabi.hisaabi_kmp.parties.domain.model.Party) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        party.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    party.phone?.let { phone ->
                        Text(
                            phone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (party.address != null || party.email != null) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                
                party.address?.let { address ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            address,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                party.email?.let { email ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            email,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AmountSummaryCard(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Amount Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Subtotal
            DetailRow(
                label = "Subtotal",
                value = "₨ ${"%.2f".format(transaction.calculateSubtotal())}"
            )
            
            // Discount
            if (transaction.flatDiscount > 0 || transaction.calculateProductsDiscount() > 0) {
                DetailRow(
                    label = "Discount",
                    value = "- ₨ ${"%.2f".format(transaction.calculateTotalDiscount())}",
                    valueColor = MaterialTheme.colorScheme.error
                )
            }
            
            // Tax
            if (transaction.flatTax > 0 || transaction.calculateProductsTax() > 0) {
                DetailRow(
                    label = "Tax",
                    value = "+ ₨ ${"%.2f".format(transaction.calculateTotalTax())}",
                    valueColor = MaterialTheme.colorScheme.tertiary
                )
            }
            
            // Additional Charges
            if (transaction.additionalCharges > 0) {
                DetailRow(
                    label = "Additional Charges${if (transaction.additionalChargesDesc != null) " (${transaction.additionalChargesDesc})" else ""}",
                    value = "+ ₨ ${"%.2f".format(transaction.additionalCharges)}",
                    valueColor = MaterialTheme.colorScheme.tertiary
                )
            }
            
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            
            // Total Bill
            DetailRow(
                label = "Total Bill",
                value = "₨ ${"%.2f".format(transaction.totalBill)}",
                labelStyle = MaterialTheme.typography.titleMedium,
                valueStyle = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            // Paid Amount
            if (transaction.totalPaid > 0) {
                Spacer(Modifier.height(8.dp))
                DetailRow(
                    label = "Paid",
                    value = "₨ ${"%.2f".format(transaction.totalPaid)}",
                    valueColor = MaterialTheme.colorScheme.primary
                )
                
                // Remaining/Payable
                val remaining = transaction.totalBill - transaction.totalPaid
                if (remaining > 0) {
                    DetailRow(
                        label = "Remaining",
                        value = "₨ ${"%.2f".format(remaining)}",
                        valueColor = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductDetailCard(detail: com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionDetail) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        detail.product?.title ?: "Product",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        detail.getDisplayQuantity(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "₨ ${"%.2f".format(detail.calculateBill())}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            
            // Price breakdown
            DetailRow(
                label = "Unit Price",
                value = "₨ ${"%.2f".format(detail.price)}"
            )
            DetailRow(
                label = "Quantity",
                value = detail.quantity.toString()
            )
            DetailRow(
                label = "Subtotal",
                value = "₨ ${"%.2f".format(detail.calculateSubtotal())}"
            )
            
            if (detail.flatDiscount > 0) {
                DetailRow(
                    label = "Discount",
                    value = "- ₨ ${"%.2f".format(detail.flatDiscount)}",
                    valueColor = MaterialTheme.colorScheme.error
                )
            }
            
            if (detail.flatTax > 0) {
                DetailRow(
                    label = "Tax",
                    value = "+ ₨ ${"%.2f".format(detail.flatTax)}",
                    valueColor = MaterialTheme.colorScheme.tertiary
                )
            }
            
            if (detail.profit > 0) {
                DetailRow(
                    label = "Profit",
                    value = "₨ ${"%.2f".format(detail.profit)}",
                    valueColor = MaterialTheme.colorScheme.primary
                )
            }
            
            if (!detail.description.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    detail.description!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecordDetailsCard(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Record Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(12.dp))
            
            // Show amount only for Cash Reminder
            if (AllTransactionTypes.isRecord(transaction.transactionType) && transaction.totalPaid > 0) {
                DetailRow(
                    label = "Promised Amount",
                    value = "₨ ${"%.2f".format(transaction.totalPaid)}",
                    valueColor = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
            }
            
            // Reminder date
            if (transaction.remindAtMilliseconds > 0) {
                DetailRow(
                    label = "Reminder Date",
                    value = kotlinx.datetime.Instant.fromEpochMilliseconds(transaction.remindAtMilliseconds)
                        .toString().substringBefore('T')
                )
            }
        }
    }
}

@Composable
private fun PayGetCashDetailsCard(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (transaction.transactionType in listOf(5, 7, 12))
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                if (transaction.transactionType in listOf(5, 7, 12)) "Received Payment" else "Paid Payment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Amount
            Text(
                "₨ ${"%.2f".format(transaction.totalPaid)}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = if (transaction.transactionType in listOf(5, 7, 12))
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun ExpenseIncomeDetailsCard(transaction: Transaction) {
    val isExpense = transaction.transactionType == AllTransactionTypes.EXPENSE.value
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpense)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                if (isExpense) "Expense" else "Extra Income",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Amount
            Text(
                "₨ ${"%.2f".format(transaction.totalPaid)}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = if (isExpense)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PaymentTransferDetailsCard(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Transfer Amount",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Amount
            Text(
                "₨ ${"%.2f".format(transaction.totalPaid)}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun JournalVoucherDetailsCard(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Journal Voucher",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Debit",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "₨ ${"%.2f".format(transaction.totalPaid)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                VerticalDivider(modifier = Modifier.height(60.dp))
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Credit",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "₨ ${"%.2f".format(transaction.totalPaid)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun StockAdjustmentDetailsCard(transaction: Transaction) {
    val stockType = AllTransactionTypes.fromValue(transaction.transactionType)
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stockType?.let { AllTransactionTypes.getDisplayName(it.value) } ?: "Stock Adjustment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(12.dp))
            
            // Products count
            DetailRow(
                label = "Total Products",
                value = "${transaction.transactionDetails.size} items",
                fontWeight = FontWeight.Bold
            )
            
            DetailRow(
                label = "Total Quantity",
                value = transaction.calculateTotalQuantity().toString()
            )
        }
    }
}

@Composable
private fun AdditionalDetailsCard(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Additional Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(12.dp))
            
            if (transaction.priceTypeId > 0) {
                DetailRow(
                    label = "Price Type",
                    value = transaction.getPriceTypeName()
                )
            }
            
            if (!transaction.shippingAddress.isNullOrBlank()) {
                DetailRow(
                    label = "Shipping Address",
                    value = transaction.shippingAddress!!
                )
            }
            
            if (transaction.calculateTotalProfit() > 0) {
                DetailRow(
                    label = "Total Profit",
                    value = "₨ ${"%.2f".format(transaction.calculateTotalProfit())}",
                    valueColor = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun WarehouseInfoCard(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Warehouse Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(12.dp))
            
            transaction.warehouseFrom?.let { warehouse ->
                DetailRow(
                    label = if (transaction.transactionType == 13) "From Warehouse" else "Warehouse",
                    value = warehouse.title
                )
            }
            
            transaction.warehouseTo?.let { warehouse ->
                DetailRow(
                    label = "To Warehouse",
                    value = warehouse.title
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodInfoCard(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Payment Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(12.dp))
            
            transaction.paymentMethodFrom?.let { method ->
                DetailRow(
                    label = "From Payment Method",
                    value = method.title
                )
            }
            
            transaction.paymentMethodTo?.let { method ->
                DetailRow(
                    label = if (transaction.paymentMethodFrom != null) "To Payment Method" else "Payment Method",
                    value = method.title
                )
            }
        }
    }
}

@Composable
private fun DescriptionCard(description: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Description",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetadataCard(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Transaction Information",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(8.dp))
            
            DetailRow(
                label = "Transaction ID",
                value = transaction.slug ?: "N/A",
                labelStyle = MaterialTheme.typography.bodySmall,
                valueStyle = MaterialTheme.typography.bodySmall
            )
            
            if (transaction.createdAt != null) {
                DetailRow(
                    label = "Created At",
                    value = transaction.createdAt!!,
                    labelStyle = MaterialTheme.typography.bodySmall,
                    valueStyle = MaterialTheme.typography.bodySmall
                )
            }
            
            if (transaction.updatedAt != null) {
                DetailRow(
                    label = "Last Updated",
                    value = transaction.updatedAt!!,
                    labelStyle = MaterialTheme.typography.bodySmall,
                    valueStyle = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ChildTransactionCard(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Transaction type and amount row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Transaction type badge
                Surface(
                    color = getChildTransactionColor(transaction.transactionType),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        transaction.getTransactionTypeName(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Amount
                Text(
                    "₨ ${"%.2f".format(transaction.totalPaid)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.totalPaid >= 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
            }
            
            // Party information (if available)
            transaction.party?.let { party ->
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            party.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        party.phone?.let { phone ->
                            Text(
                                phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Payment method (if available)
            transaction.paymentMethodTo?.let { method ->
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        method.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun getChildTransactionColor(transactionType: Int): androidx.compose.ui.graphics.Color {
    return when (transactionType) {
        4, 6 -> androidx.compose.ui.graphics.Color(0xFFFFEBEE) // Pay (Red)
        5, 7, 11 -> androidx.compose.ui.graphics.Color(0xFFE8F5E9) // Get (Green)
        12 -> androidx.compose.ui.graphics.Color(0xFFFFF3E0) // Withdraw (Orange)
        8 -> androidx.compose.ui.graphics.Color(0xFFFFEBEE) // Expense (Red)
        9 -> androidx.compose.ui.graphics.Color(0xFFE8F5E9) // Income (Green)
        10 -> androidx.compose.ui.graphics.Color(0xFFE0F2F1) // Transfer (Cyan)
        else -> androidx.compose.ui.graphics.Color(0xFFF5F5F5) // Default
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    labelStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    valueStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = labelStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = fontWeight,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = valueStyle,
            color = valueColor,
            fontWeight = fontWeight
        )
    }
}

private fun getTransactionColor(transaction: Transaction): androidx.compose.ui.graphics.Color {
    val isRecordType = AllTransactionTypes.isRecord(transaction.transactionType)
    val isPayGetCashType = AllTransactionTypes.isPayGetCash(transaction.transactionType)
    val isExpenseIncomeType = AllTransactionTypes.isExpenseIncome(transaction.transactionType)
    val isPaymentTransferType = AllTransactionTypes.isPaymentTransfer(transaction.transactionType)
    val isJournalVoucherType = AllTransactionTypes.isJournalVoucher(transaction.transactionType)
    val isStockAdjustmentType = AllTransactionTypes.isStockAdjustment(transaction.transactionType)
    
    return when {
        isRecordType -> {
            when (AllTransactionTypes.fromValue(transaction.transactionType)) {
                AllTransactionTypes.MEETING -> androidx.compose.ui.graphics.Color(0xFFE3F2FD)
                AllTransactionTypes.TASK -> androidx.compose.ui.graphics.Color(0xFFF3E5F5)
                AllTransactionTypes.CLIENT_NOTE -> androidx.compose.ui.graphics.Color(0xFFE8F5E9)
                AllTransactionTypes.SELF_NOTE -> androidx.compose.ui.graphics.Color(0xFFFFF9C4)
                AllTransactionTypes.CASH_REMINDER -> androidx.compose.ui.graphics.Color(0xFFFFEBEE)
                else -> androidx.compose.ui.graphics.Color(0xFFF5F5F5)
            }
        }
        isPayGetCashType -> {
            if (transaction.transactionType in listOf(
                AllTransactionTypes.GET_FROM_CUSTOMER.value, 
                AllTransactionTypes.GET_FROM_VENDOR.value, 
                AllTransactionTypes.INVESTMENT_DEPOSIT.value
            ))
                androidx.compose.ui.graphics.Color(0xFFE3F2FD)
            else
                androidx.compose.ui.graphics.Color(0xFFF3E5F5)
        }
        isExpenseIncomeType -> {
            if (transaction.transactionType == AllTransactionTypes.EXPENSE.value)
                androidx.compose.ui.graphics.Color(0xFFFFEBEE)
            else
                androidx.compose.ui.graphics.Color(0xFFE8F5E9)
        }
        isPaymentTransferType -> androidx.compose.ui.graphics.Color(0xFFE0F2F1)
        isJournalVoucherType -> androidx.compose.ui.graphics.Color(0xFFF3E5F5)
        isStockAdjustmentType -> androidx.compose.ui.graphics.Color(0xFFE0F2F1)
        else -> {
            when (AllTransactionTypes.fromValue(transaction.transactionType)) {
                AllTransactionTypes.SALE -> androidx.compose.ui.graphics.Color(0xFFE3F2FD)
                AllTransactionTypes.PURCHASE -> androidx.compose.ui.graphics.Color(0xFFF3E5F5)
                AllTransactionTypes.CUSTOMER_RETURN -> androidx.compose.ui.graphics.Color(0xFFFFEBEE)
                AllTransactionTypes.VENDOR_RETURN -> androidx.compose.ui.graphics.Color(0xFFE0F2F1)
                else -> androidx.compose.ui.graphics.Color(0xFFF5F5F5)
            }
        }
    }
}

