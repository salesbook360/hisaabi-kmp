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
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionState
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionSortOption
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.TransactionsListViewModel
import com.hisaabi.hisaabi_kmp.utils.formatTransactionDate
import com.hisaabi.hisaabi_kmp.utils.formatEntryDate
import com.hisaabi.hisaabi_kmp.utils.calculateManufacturingCost
import com.hisaabi.hisaabi_kmp.receipt.ReceiptViewModel
import com.hisaabi.hisaabi_kmp.receipt.ReceiptPreviewDialog
import com.hisaabi.hisaabi_kmp.core.ui.FilterChipWithColors
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsListScreen(
    viewModel: TransactionsListViewModel,
    onNavigateBack: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onAddTransactionClick: () -> Unit,
    onEditTransaction: (Transaction) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState()
    var showSearchBar by remember { mutableStateOf(false) }
    
    // Receipt functionality
    val receiptViewModel: ReceiptViewModel = koinInject()
    val receiptState by receiptViewModel.state.collectAsState()
    
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    // Handle receipt state
    LaunchedEffect(receiptState.error) {
        receiptState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            receiptViewModel.clearError()
        }
    }
    
    LaunchedEffect(receiptState.successMessage) {
        receiptState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            receiptViewModel.clearSuccess()
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
                                onDeleteClick = { viewModel.deleteTransaction(transaction) },
                                onEditClick = { onEditTransaction(transaction) },
                                transactionDetailsCounts = state.transactionDetailsCounts,
                                receiptViewModel = receiptViewModel
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
                selectedSortBy = state.sortBy,
                onTypeSelected = { viewModel.setTransactionTypeFilter(it) },
                onSortBySelected = { viewModel.setSortBy(it) },
                onClearFilters = { 
                    viewModel.clearFilters()
                    viewModel.toggleFilters()
                },
                onApplyFilters = { viewModel.toggleFilters() }
            )
        }
    }
    
    // Receipt Preview Dialog
    if (receiptState.showPreview && receiptState.currentTransaction != null) {
        ReceiptPreviewDialog(
            transaction = receiptState.currentTransaction!!,
            config = receiptState.receiptConfig,
            isGenerating = receiptState.isGenerating,
            onDismiss = { receiptViewModel.hidePreview() },
            onShare = { 
                receiptState.currentTransaction?.let { transaction ->
                    receiptViewModel.generateAndShareReceipt(transaction)
                }
            }
        )
    }
}

@Composable
private fun FiltersBottomSheetContent(
    selectedType: AllTransactionTypes?,
    selectedSortBy: TransactionSortOption,
    onTypeSelected: (AllTransactionTypes?) -> Unit,
    onSortBySelected: (TransactionSortOption) -> Unit,
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
            FilterChipWithColors(
                selected = selectedType == null,
                onClick = { onTypeSelected(null) },
                label = "All",
                modifier = Modifier.weight(1f)
            )
            FilterChipWithColors(
                selected = selectedType == AllTransactionTypes.SALE,
                onClick = { onTypeSelected(AllTransactionTypes.SALE) },
                label = "Sale",
                modifier = Modifier.weight(1f)
            )
            FilterChipWithColors(
                selected = selectedType == AllTransactionTypes.PURCHASE,
                onClick = { onTypeSelected(AllTransactionTypes.PURCHASE) },
                label = "Purchase",
                modifier = Modifier.weight(1f)
            )
        }
        
        // Second row of chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipWithColors(
                selected = selectedType == AllTransactionTypes.CUSTOMER_RETURN,
                onClick = { onTypeSelected(AllTransactionTypes.CUSTOMER_RETURN) },
                label = "Return",
                modifier = Modifier.weight(1f)
            )
            FilterChipWithColors(
                selected = selectedType == AllTransactionTypes.SALE_ORDER,
                onClick = { onTypeSelected(AllTransactionTypes.SALE_ORDER) },
                label = "Order",
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.weight(1f))
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Sort by section
        Text(
            "Sort by",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipWithColors(
                selected = selectedSortBy == TransactionSortOption.ENTRY_DATE,
                onClick = { onSortBySelected(TransactionSortOption.ENTRY_DATE) },
                label = "Entry date",
                modifier = Modifier.weight(1f)
            )
            FilterChipWithColors(
                selected = selectedSortBy == TransactionSortOption.TRANSACTION_DATE,
                onClick = { onSortBySelected(TransactionSortOption.TRANSACTION_DATE) },
                label = "Transaction date",
                modifier = Modifier.weight(1f)
            )
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
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    transactionDetailsCounts: Map<String, Int>,
    receiptViewModel: ReceiptViewModel
) {
    // Determine card type based on transaction type
    when {
        AllTransactionTypes.isRecord(transaction.transactionType) -> {
            RecordTransactionCard(transaction, onClick, onDeleteClick, onEditClick, receiptViewModel)
        }
        AllTransactionTypes.isPayGetCash(transaction.transactionType) -> {
            PayGetCashCard(transaction, onClick, onDeleteClick, onEditClick, receiptViewModel)
        }
        AllTransactionTypes.isExpenseIncome(transaction.transactionType) -> {
            ExpenseIncomeCard(transaction, onClick, onDeleteClick, onEditClick, receiptViewModel)
        }
        transaction.transactionType == AllTransactionTypes.PAYMENT_TRANSFER.value -> {
            PaymentTransferCard(transaction, onClick, onDeleteClick, onEditClick, receiptViewModel)
        }
        transaction.transactionType == AllTransactionTypes.JOURNAL_VOUCHER.value -> {
            JournalVoucherCard(transaction, onClick, onDeleteClick, onEditClick, receiptViewModel)
        }
        AllTransactionTypes.isStockAdjustment(transaction.transactionType) -> {
            StockAdjustmentCard(transaction, onClick, onDeleteClick, onEditClick, receiptViewModel, transactionDetailsCounts)
        }
        transaction.transactionType == AllTransactionTypes.MANUFACTURE.value -> {
            ManufactureCard(transaction, onClick, onDeleteClick, onEditClick, receiptViewModel)
        }
        AllTransactionTypes.isOrder(transaction.transactionType) || transaction.transactionType == AllTransactionTypes.QUOTATION.value -> {
            OrderQuotationCard(transaction, onClick, onDeleteClick, onEditClick, receiptViewModel, transactionDetailsCounts)
        }
        else -> {
            // Basic transaction card (Sale, Purchase, Returns)
            BasicTransactionCard(transaction, onClick, onDeleteClick, onEditClick, receiptViewModel, transactionDetailsCounts)
        }
    }
}

// ============= CARD HEADER COMPONENTS =============

@Composable
private fun getBadgeColor(transactionType: Int): androidx.compose.ui.graphics.Color {
    return when (AllTransactionTypes.fromValue(transactionType)) {
        AllTransactionTypes.SALE -> MaterialTheme.colorScheme.primaryContainer
        AllTransactionTypes.PURCHASE -> MaterialTheme.colorScheme.secondaryContainer
        AllTransactionTypes.CUSTOMER_RETURN -> MaterialTheme.colorScheme.errorContainer
        AllTransactionTypes.VENDOR_RETURN -> MaterialTheme.colorScheme.tertiaryContainer
        AllTransactionTypes.GET_FROM_CUSTOMER, AllTransactionTypes.GET_FROM_VENDOR, 
        AllTransactionTypes.INVESTMENT_WITHDRAW -> MaterialTheme.colorScheme.primaryContainer
        AllTransactionTypes.PAY_TO_CUSTOMER, AllTransactionTypes.PAY_TO_VENDOR, 
        AllTransactionTypes.INVESTMENT_DEPOSIT -> MaterialTheme.colorScheme.secondaryContainer
        AllTransactionTypes.EXPENSE -> MaterialTheme.colorScheme.errorContainer
        AllTransactionTypes.EXTRA_INCOME -> MaterialTheme.colorScheme.primaryContainer
        AllTransactionTypes.PAYMENT_TRANSFER -> MaterialTheme.colorScheme.tertiaryContainer
        AllTransactionTypes.JOURNAL_VOUCHER -> MaterialTheme.colorScheme.secondaryContainer
        AllTransactionTypes.STOCK_TRANSFER, AllTransactionTypes.STOCK_INCREASE, 
        AllTransactionTypes.STOCK_REDUCE -> MaterialTheme.colorScheme.tertiaryContainer
        AllTransactionTypes.MANUFACTURE -> MaterialTheme.colorScheme.tertiaryContainer
        AllTransactionTypes.MEETING -> MaterialTheme.colorScheme.tertiaryContainer
        AllTransactionTypes.TASK -> MaterialTheme.colorScheme.secondaryContainer
        AllTransactionTypes.CLIENT_NOTE -> MaterialTheme.colorScheme.primaryContainer
        AllTransactionTypes.SELF_NOTE -> MaterialTheme.colorScheme.surfaceVariant
        AllTransactionTypes.CASH_REMINDER -> MaterialTheme.colorScheme.errorContainer
        AllTransactionTypes.SALE_ORDER -> MaterialTheme.colorScheme.primaryContainer
        AllTransactionTypes.PURCHASE_ORDER -> MaterialTheme.colorScheme.secondaryContainer
        AllTransactionTypes.QUOTATION -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
}

@Composable
private fun CardHeader(
    transaction: Transaction,
    onOptionsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Transaction type badge
        Surface(
            color = getBadgeColor(transaction.transactionType),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                transaction.getTransactionTypeName(),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Dates and options
        Row(verticalAlignment = Alignment.Top) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                transaction.timestamp?.let { timestamp ->
                    Text(
                        formatTransactionDate(timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                transaction.slug?.let { slug ->
                    Text(
                        "#$slug",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = onOptionsClick) {
                Icon(Icons.Default.MoreVert, "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PartyInfo(transaction: Transaction) {
    transaction.party?.let { party ->
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    party.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            party.phone?.let { phone ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 28.dp)
                ) {
                    Text(
                        phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionOptionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onReceiptClick: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text("View Details") },
            onClick = {
                onDismiss()
                onClick()
            },
            leadingIcon = { Icon(Icons.Default.Visibility, null) }
        )
        DropdownMenuItem(
            text = { Text("Edit") },
            onClick = {
                onDismiss()
                onEditClick()
            },
            leadingIcon = { Icon(Icons.Default.Edit, null) }
        )
        DropdownMenuItem(
            text = { Text("Delete") },
            onClick = {
                onDismiss()
                onDeleteClick()
            },
            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
        )
        DropdownMenuItem(
            text = { Text("Generate Receipt") },
            onClick = {
                onDismiss()
                onReceiptClick()
            },
            leadingIcon = { Icon(Icons.Default.Receipt, null) }
        )
    }
}

// ============= BASIC TRANSACTION CARD (Sale, Purchase, Returns) =============

@Composable
private fun BasicTransactionCard(
    transaction: Transaction,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    receiptViewModel: ReceiptViewModel,
    transactionDetailsCounts: Map<String, Int>
) {
    var showOptions by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CardHeader(transaction, { showOptions = !showOptions })
            
            PartyInfo(transaction)
            
            HorizontalDivider()
            
            // Financial info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "Total",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "₨ ${String.format("%.2f", transaction.totalBill)}",
                        style = MaterialTheme.typography.headlineSmall,
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
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Items",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${transactionDetailsCounts[transaction.slug] ?: transaction.transactionDetails.size}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Additional charges if any
            if (transaction.flatDiscount > 0 || transaction.flatTax > 0 || transaction.additionalCharges > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (transaction.flatDiscount > 0) {
                        DetailChip("−₨${String.format("%.0f", transaction.flatDiscount)}", MaterialTheme.colorScheme.errorContainer)
                    }
                    if (transaction.flatTax > 0) {
                        DetailChip("+₨${String.format("%.0f", transaction.flatTax)}", MaterialTheme.colorScheme.tertiaryContainer)
                    }
                    if (transaction.additionalCharges > 0) {
                        DetailChip("+₨${String.format("%.0f", transaction.additionalCharges)}", MaterialTheme.colorScheme.secondaryContainer)
                    }
                }
            }
            
            // Description if available
            transaction.description?.let { desc ->
                if (desc.isNotBlank()) {
                    Text(
                        desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
        
        TransactionOptionsMenu(
            expanded = showOptions,
            onDismiss = { showOptions = false },
            onClick = onClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onReceiptClick = { receiptViewModel.showPreview(transaction) }
        )
    }
}

// ============= PAY/GET CASH CARD =============

@Composable
private fun PayGetCashCard(
    transaction: Transaction,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    receiptViewModel: ReceiptViewModel
) {
    var showOptions by remember { mutableStateOf(false) }
    
    val isReceiving = transaction.transactionType in listOf(
        AllTransactionTypes.GET_FROM_CUSTOMER.value,
        AllTransactionTypes.GET_FROM_VENDOR.value,
        AllTransactionTypes.INVESTMENT_WITHDRAW.value
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CardHeader(transaction, { showOptions = !showOptions })
            
            PartyInfo(transaction)
            
            HorizontalDivider()
            
            // Amount - centered and prominent
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        if (isReceiving) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (isReceiving) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        if (isReceiving) "Received" else "Paid",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "₨ ${"%.2f".format(transaction.totalPaid)}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isReceiving) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            }
            
            // Payment method
            transaction.paymentMethodTo?.let { paymentMethod ->
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Method",
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
            
            // Description
            transaction.description?.let { desc ->
                if (desc.isNotBlank()) {
                    Text(
                        desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
        
        TransactionOptionsMenu(
            expanded = showOptions,
            onDismiss = { showOptions = false },
            onClick = onClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onReceiptClick = { receiptViewModel.showPreview(transaction) }
        )
    }
}

// ============= EXPENSE/INCOME CARD =============

@Composable
private fun ExpenseIncomeCard(
    transaction: Transaction,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    receiptViewModel: ReceiptViewModel
) {
    var showOptions by remember { mutableStateOf(false) }
    
    val isExpense = transaction.transactionType == AllTransactionTypes.EXPENSE.value
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CardHeader(transaction, { showOptions = !showOptions })
            
            // Amount - centered
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    if (isExpense) "Expense Amount" else "Income Amount",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "₨ ${"%.2f".format(transaction.totalPaid)}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            
            // Payment method
            transaction.paymentMethodTo?.let { paymentMethod ->
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Payment Method",
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
            
            // Description
            transaction.description?.let { desc ->
                if (desc.isNotBlank()) {
                    Text(
                        desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
        
        TransactionOptionsMenu(
            expanded = showOptions,
            onDismiss = { showOptions = false },
            onClick = onClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onReceiptClick = { receiptViewModel.showPreview(transaction) }
        )
    }
}

// ============= PAYMENT TRANSFER CARD =============

@Composable
private fun PaymentTransferCard(
    transaction: Transaction,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    receiptViewModel: ReceiptViewModel
) {
    var showOptions by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CardHeader(transaction, { showOptions = !showOptions })
            
            // Amount
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Transfer Amount",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "₨ ${"%.2f".format(transaction.totalPaid)}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            HorizontalDivider()
            
            // From/To Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // From
                transaction.paymentMethodFrom?.let { paymentMethodFrom ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "From",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            paymentMethodFrom.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                // To
                transaction.paymentMethodTo?.let { paymentMethodTo ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "To",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            paymentMethodTo.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Description
            transaction.description?.let { desc ->
                if (desc.isNotBlank()) {
                    Text(
                        desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
        
        TransactionOptionsMenu(
            expanded = showOptions,
            onDismiss = { showOptions = false },
            onClick = onClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onReceiptClick = { receiptViewModel.showPreview(transaction) }
        )
    }
}

// ============= JOURNAL VOUCHER CARD =============

@Composable
private fun JournalVoucherCard(
    transaction: Transaction,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    receiptViewModel: ReceiptViewModel
) {
    var showOptions by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CardHeader(transaction, { showOptions = !showOptions })
            
            HorizontalDivider()
            
            // Debit/Credit Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Debit
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Debit",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "₨ ${"%.2f".format(transaction.totalPaid)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                VerticalDivider(modifier = Modifier.height(50.dp))
                
                // Credit
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Credit",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "₨ ${"%.2f".format(transaction.totalPaid)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Description
            transaction.description?.let { desc ->
                if (desc.isNotBlank()) {
                    HorizontalDivider()
                    Text(
                        desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
        
        TransactionOptionsMenu(
            expanded = showOptions,
            onDismiss = { showOptions = false },
            onClick = onClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onReceiptClick = { receiptViewModel.showPreview(transaction) }
        )
    }
}

// ============= STOCK ADJUSTMENT CARD =============

@Composable
private fun StockAdjustmentCard(
    transaction: Transaction,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    receiptViewModel: ReceiptViewModel,
    transactionDetailsCounts: Map<String, Int>
) {
    var showOptions by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CardHeader(transaction, { showOptions = !showOptions })
            
            // Warehouse info
            if (transaction.transactionType == AllTransactionTypes.STOCK_TRANSFER.value) {
                // Transfer: From -> To
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    transaction.warehouseFrom?.let { warehouseFrom ->
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "From",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                warehouseFrom.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    transaction.warehouseTo?.let { warehouseTo ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                "To",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                warehouseTo.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // Increase/Decrease: Single warehouse
                transaction.warehouseFrom?.let { warehouse ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Warehouse",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            warehouse.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            HorizontalDivider()
            
            // Product count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Products Adjusted",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${transactionDetailsCounts[transaction.slug] ?: transaction.transactionDetails.size} items",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            // Description
            transaction.description?.let { desc ->
                if (desc.isNotBlank()) {
                    Text(
                        desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
        
        TransactionOptionsMenu(
            expanded = showOptions,
            onDismiss = { showOptions = false },
            onClick = onClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onReceiptClick = { receiptViewModel.showPreview(transaction) }
        )
    }
}

// ============= MANUFACTURE CARD =============

@Composable
private fun ManufactureCard(
    transaction: Transaction,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    receiptViewModel: ReceiptViewModel
) {
    var showOptions by remember { mutableStateOf(false) }
    val manufacturedProduct = transaction.transactionDetails.firstOrNull()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CardHeader(transaction, { showOptions = !showOptions })
            
            // Recipe name
            manufacturedProduct?.product?.let { product ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Build,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        product.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            HorizontalDivider()
            
            // Quantity and Cost
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Quantity
                manufacturedProduct?.let { detail ->
                    Column {
                        Text(
                            "Quantity",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            detail.getDisplayQuantity(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                
                // Manufacturing Cost
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Total Cost",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "₨ ${String.format("%.2f", calculateManufacturingCost(transaction))}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
        
        TransactionOptionsMenu(
            expanded = showOptions,
            onDismiss = { showOptions = false },
            onClick = onClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onReceiptClick = { receiptViewModel.showPreview(transaction) }
        )
    }
}

// ============= ORDER/QUOTATION CARD =============

@Composable
private fun OrderQuotationCard(
    transaction: Transaction,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    receiptViewModel: ReceiptViewModel,
    transactionDetailsCounts: Map<String, Int>
) {
    var showOptions by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CardHeader(transaction, { showOptions = !showOptions })
            
            PartyInfo(transaction)
            
            HorizontalDivider()
            
            // Order/Quotation info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "Amount",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "₨ ${String.format("%.2f", transaction.totalBill)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Items",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${transactionDetailsCounts[transaction.slug] ?: transaction.transactionDetails.size}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Description
            transaction.description?.let { desc ->
                if (desc.isNotBlank()) {
                    Text(
                        desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
        
        TransactionOptionsMenu(
            expanded = showOptions,
            onDismiss = { showOptions = false },
            onClick = onClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onReceiptClick = { receiptViewModel.showPreview(transaction) }
        )
    }
}

// ============= RECORD TRANSACTION CARD =============

@Composable
private fun RecordTransactionCard(
    transaction: Transaction,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    receiptViewModel: ReceiptViewModel
) {
    var showOptions by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CardHeader(transaction, { showOptions = !showOptions })
            
            // Party info for client-related records
            if (transaction.transactionType != AllTransactionTypes.SELF_NOTE.value) {
                PartyInfo(transaction)
                HorizontalDivider()
            }
            
            // Description/Content
            transaction.description?.let { desc ->
                if (desc.isNotBlank()) {
                    Text(
                        desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 3
                    )
                }
            }
            
            // For Cash Reminder, show promised amount
            if (transaction.transactionType == AllTransactionTypes.CASH_REMINDER.value && transaction.totalPaid > 0) {
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Promised Amount",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "₨ ${String.format("%.2f", transaction.totalPaid)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Status
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Status",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = when (transaction.stateId) {
                        TransactionState.COMPLETED.value -> MaterialTheme.colorScheme.primaryContainer
                        TransactionState.PENDING.value -> MaterialTheme.colorScheme.tertiaryContainer
                        TransactionState.CANCELLED.value -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        transaction.getStateName(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        TransactionOptionsMenu(
            expanded = showOptions,
            onDismiss = { showOptions = false },
            onClick = onClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onReceiptClick = { receiptViewModel.showPreview(transaction) }
        )
    }
}

// ============= HELPER COMPONENTS =============

@Composable
private fun DetailChip(
    text: String,
    backgroundColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}
