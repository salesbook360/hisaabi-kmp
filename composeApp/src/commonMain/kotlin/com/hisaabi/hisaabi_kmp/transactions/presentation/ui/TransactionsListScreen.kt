package com.hisaabi.hisaabi_kmp.transactions.presentation.ui

import androidx.compose.foundation.clickable
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
import com.hisaabi.hisaabi_kmp.transactions.domain.model.RecordType
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionState
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionType
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.TransactionsListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsListScreen(
    viewModel: TransactionsListViewModel,
    onNavigateBack: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onAddTransactionClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState()
    var showSearchBar by remember { mutableStateOf(false) }
    
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
                title = { Text("Transactions") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Filter button
                    IconButton(onClick = { viewModel.toggleFilters() }) {
                        Icon(
                            Icons.Default.FilterList,
                            "Filters",
                            tint = if (state.selectedTransactionType != null || state.selectedParty != null) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Search button
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(Icons.Default.Search, "Search")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransactionClick,
                icon = { Icon(Icons.Default.Add, "Add") },
                text = { Text("New Transaction") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            if (showSearchBar || state.searchQuery.isNotEmpty()) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search transactions...") },
                    leadingIcon = { Icon(Icons.Default.Search, "Search") },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, "Clear")
                            }
                        }
                    },
                    singleLine = true
                )
            }
            
            // Transactions list
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.transactions.isEmpty() -> {
                    EmptyTransactionsView(onAddClick = onAddTransactionClick)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.transactions) { transaction ->
                            TransactionCard(
                                transaction = transaction,
                                onClick = { onTransactionClick(transaction) },
                                onDeleteClick = { viewModel.deleteTransaction(transaction) }
                            )
                        }
                        
                        // Bottom padding for FAB
                        item {
                            Spacer(Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
    
    // Filter Bottom Sheet
    if (state.showFilters) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleFilters() },
            sheetState = sheetState
        ) {
            FiltersBottomSheetContent(
                selectedType = state.selectedTransactionType,
                onTypeSelected = { viewModel.setTransactionTypeFilter(it) },
                onClearFilters = { 
                    viewModel.clearFilters()
                    viewModel.toggleFilters()
                },
                onApplyFilters = { viewModel.toggleFilters() }
            )
        }
    }
}

@Composable
private fun FiltersBottomSheetContent(
    selectedType: TransactionType?,
    onTypeSelected: (TransactionType?) -> Unit,
    onClearFilters: () -> Unit,
    onApplyFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Filter Transactions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onClearFilters) {
                Text("Clear All")
            }
        }
        
        HorizontalDivider()
        
        // Transaction type filter
        Text(
            "Transaction Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        // First row of chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedType == null,
                onClick = { onTypeSelected(null) },
                label = { Text("All") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedType == TransactionType.SALE,
                onClick = { onTypeSelected(TransactionType.SALE) },
                label = { Text("Sale") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedType == TransactionType.PURCHASE,
                onClick = { onTypeSelected(TransactionType.PURCHASE) },
                label = { Text("Purchase") },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Second row of chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedType == TransactionType.CUSTOMER_RETURN,
                onClick = { onTypeSelected(TransactionType.CUSTOMER_RETURN) },
                label = { Text("Return") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedType == TransactionType.SALE_ORDER,
                onClick = { onTypeSelected(TransactionType.SALE_ORDER) },
                label = { Text("Order") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.weight(1f))
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Apply button
        Button(
            onClick = onApplyFilters,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Apply Filters")
        }
        
        // Bottom padding for safe area
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun EmptyTransactionsView(
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Receipt,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "No Transactions Found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Start by creating your first transaction",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, "Add", Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add Transaction")
        }
    }
}

@Composable
private fun TransactionCard(
    transaction: Transaction,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showOptions by remember { mutableStateOf(false) }
    val isRecordType = RecordType.fromValue(transaction.transactionType) != null
    val isPayGetCashType = transaction.transactionType in listOf(4, 5, 6, 7, 11, 12)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Transaction type badge
                Surface(
                    color = if (isRecordType) {
                        // Different colors for record types
                        when (RecordType.fromValue(transaction.transactionType)) {
                            RecordType.MEETING -> MaterialTheme.colorScheme.tertiaryContainer
                            RecordType.TASK -> MaterialTheme.colorScheme.secondaryContainer
                            RecordType.CLIENT_NOTE -> MaterialTheme.colorScheme.primaryContainer
                            RecordType.SELF_NOTE -> MaterialTheme.colorScheme.surfaceVariant
                            RecordType.CASH_REMINDER -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    } else if (isPayGetCashType) {
                        // Colors for Pay/Get Cash transactions
                        when (transaction.transactionType) {
                            5, 7, 12 -> MaterialTheme.colorScheme.primaryContainer // Get Cash (incoming)
                            4, 6, 11 -> MaterialTheme.colorScheme.secondaryContainer // Pay Cash (outgoing)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    } else {
                        // Original colors for transaction types
                        when (TransactionType.fromValue(transaction.transactionType)) {
                            TransactionType.SALE -> MaterialTheme.colorScheme.primaryContainer
                            TransactionType.PURCHASE -> MaterialTheme.colorScheme.secondaryContainer
                            TransactionType.CUSTOMER_RETURN -> MaterialTheme.colorScheme.errorContainer
                            TransactionType.VENDOR_RETURN -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        transaction.getTransactionTypeName(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        transaction.timestamp ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = { showOptions = !showOptions }) {
                        Icon(Icons.Default.MoreVert, "Options")
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Party info
            transaction.party?.let { party ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            party.name,
                            style = MaterialTheme.typography.titleMedium,
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
                
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
            }
            
            // Transaction details - different for records vs pay/get cash vs regular transactions
            if (isRecordType) {
                // For records, show description prominently
                transaction.description?.let { desc ->
                    if (desc.isNotBlank()) {
                        Text(
                            desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 3
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
                
                // Show amount only for Cash Reminder
                if (RecordType.fromValue(transaction.transactionType) == RecordType.CASH_REMINDER && transaction.totalPaid > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Promised Amount:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "₨ ${String.format("%.2f", transaction.totalPaid)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Show state
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Status:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        transaction.getStateName(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (transaction.stateId) {
                            TransactionState.COMPLETED.value -> MaterialTheme.colorScheme.primary
                            TransactionState.PENDING.value -> MaterialTheme.colorScheme.tertiary
                            TransactionState.CANCELLED.value -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            } else if (isPayGetCashType) {
                // For Pay/Get Cash transactions, show amount prominently
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (transaction.transactionType in listOf(5, 7, 12)) "Received:" else "Paid:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "₨ ${"%.2f".format(transaction.totalPaid)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.transactionType in listOf(5, 7, 12)) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.secondary
                    )
                }
                
                // Show payment method if available
                transaction.paymentMethodTo?.let { paymentMethod ->
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Payment Method:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            paymentMethod.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Show description if available
                transaction.description?.let { desc ->
                    if (desc.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Remarks: $desc",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
            } else {
                // Original transaction display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Total Bill",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "₨ ${String.format("%.2f", transaction.totalBill)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    if (transaction.totalPaid > 0) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Paid",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "₨ ${String.format("%.2f", transaction.totalPaid)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Additional details
                if (transaction.flatDiscount > 0 || transaction.flatTax > 0 || transaction.additionalCharges > 0) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (transaction.flatDiscount > 0) {
                            DetailChip("Discount: ₨${String.format("%.2f", transaction.flatDiscount)}")
                        }
                        if (transaction.flatTax > 0) {
                            DetailChip("Tax: ₨${String.format("%.2f", transaction.flatTax)}")
                        }
                        if (transaction.additionalCharges > 0) {
                            DetailChip("Charges: ₨${String.format("%.2f", transaction.additionalCharges)}")
                        }
                    }
                }
                
                // Description for transactions
                transaction.description?.let { desc ->
                    if (desc.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
            }
            
            // Options dropdown
            DropdownMenu(
                expanded = showOptions,
                onDismissRequest = { showOptions = false }
            ) {
                DropdownMenuItem(
                    text = { Text("View Details") },
                    onClick = {
                        showOptions = false
                        onClick()
                    },
                    leadingIcon = { Icon(Icons.Default.Visibility, null) }
                )
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        showOptions = false
                        // TODO: Navigate to edit
                    },
                    leadingIcon = { Icon(Icons.Default.Edit, null) }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        showOptions = false
                        onDeleteClick()
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                )
                DropdownMenuItem(
                    text = { Text("Generate Receipt") },
                    onClick = {
                        showOptions = false
                        // TODO: Generate receipt
                    },
                    leadingIcon = { Icon(Icons.Default.Receipt, null) }
                )
            }
        }
    }
}

@Composable
private fun DetailChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

