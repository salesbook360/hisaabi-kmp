package com.hisaabi.hisaabi_kmp.transactions.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionState
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionSortOption
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionCategory
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.TransactionsListViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.ManufactureInfo
import com.hisaabi.hisaabi_kmp.utils.formatTransactionDate
import com.hisaabi.hisaabi_kmp.utils.formatEntryDate
import com.hisaabi.hisaabi_kmp.utils.SimpleDatePickerDialog
import com.hisaabi.hisaabi_kmp.utils.formatDate
import kotlinx.datetime.Clock
import com.hisaabi.hisaabi_kmp.receipt.ReceiptViewModel
import com.hisaabi.hisaabi_kmp.receipt.ReceiptPreviewDialog
import com.hisaabi.hisaabi_kmp.core.ui.FilterChipWithColors
import com.hisaabi.hisaabi_kmp.core.ui.getStatusBadgeColors
import com.hisaabi.hisaabi_kmp.sync.domain.model.SyncStatus
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import org.koin.compose.koinInject
import com.hisaabi.hisaabi_kmp.utils.format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsListScreen(
    viewModel: TransactionsListViewModel,
    onNavigateBack: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onAddTransactionClick: () -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onConvertToSale: ((Transaction) -> Unit)? ,
    onEditAndConvertToSale: ((Transaction) -> Unit)? ,
    onConvertToPurchase: ((Transaction) -> Unit)? ,
    onEditAndConvertToPurchase: ((Transaction) -> Unit)? ,
    onCancelAndRemove: ((Transaction) -> Unit)? ,
    onRestore: ((Transaction) -> Unit)? ,
    onClone: ((Transaction) -> Unit)? ,
    onChangeStateToPending: ((Transaction) -> Unit)? ,
    onChangeStateToInProgress: ((Transaction) -> Unit)? ,
    onChangeStateToCompleted: ((Transaction) -> Unit)? ,
    onChangeStateToCanceled: ((Transaction) -> Unit)? ,
    onOutstandingBalanceReminder: ((Transaction) -> Unit)?,
    onTransactionDeleted: (() -> Unit)? = null,
    onSelectParty: () -> Unit = {},
    onSelectArea: () -> Unit = {},
    onSelectCategory: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState()
    var showSearchBar by remember { mutableStateOf(false) }
    
    // LazyListState for scroll position preservation
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = state.scrollPosition,
        initialFirstVisibleItemScrollOffset = state.scrollOffset
    )
    
    // Sync scroll position changes back to ViewModel
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        viewModel.updateScrollPosition(
            listState.firstVisibleItemIndex,
            listState.firstVisibleItemScrollOffset
        )
    }
    
    // Infinite scroll - load more when near the end
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            // Load more when we're within 5 items of the end
            lastVisibleItem >= totalItems - 5 && totalItems > 0
        }
    }
    
    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && state.hasMore && !state.isLoadingMore && !state.isLoading) {
            viewModel.loadMore()
        }
    }
    
    // Currency
    val preferencesManager: PreferencesManager = koinInject()
    val selectedCurrency by preferencesManager.selectedCurrency.collectAsState(null)
    val currencySymbol = selectedCurrency?.symbol ?: ""
    
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
                            tint = if (state.selectedTransactionTypes.isNotEmpty() || 
                                state.selectedParty != null || 
                                state.selectedArea != null || 
                                state.selectedCategory != null ||
                                state.idOrSlugFilter.isNotEmpty() ||
                                state.startDate != null ||
                                state.endDate != null) 
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
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.transactions) { transaction ->
                            TransactionCard(
                                transaction = transaction,
                                currencySymbol = currencySymbol,
                                onClick = { onTransactionClick(transaction) },
                                onDeleteClick = { 
                                    viewModel.deleteTransaction(transaction) {
                                        onTransactionDeleted?.invoke()
                                    }
                                },
                                onEditClick = { onEditTransaction(transaction) },
                                transactionDetailsCounts = state.transactionDetailsCounts,
                                manufactureInfo = state.manufactureInfo,
                                receiptViewModel = receiptViewModel,
                                onConvertToSale = onConvertToSale?.let { { it(transaction) } },
                                onEditAndConvertToSale = onEditAndConvertToSale?.let { { it(transaction) } },
                                onConvertToPurchase = onConvertToPurchase?.let { { it(transaction) } },
                                onEditAndConvertToPurchase = onEditAndConvertToPurchase?.let { { it(transaction) } },
                                onCancelAndRemove = onCancelAndRemove?.let { { it(transaction) } },
                                onRestore = onRestore?.let { { it(transaction) } },
                                onClone = onClone?.let { { it(transaction) } },
                                onChangeStateToPending = onChangeStateToPending?.let { { it(transaction) } },
                                onChangeStateToInProgress = onChangeStateToInProgress?.let { { it(transaction) } },
                                onChangeStateToCompleted = onChangeStateToCompleted?.let { { it(transaction) } },
                                onChangeStateToCanceled = onChangeStateToCanceled?.let { { it(transaction) } },
                                onOutstandingBalanceReminder = onOutstandingBalanceReminder?.let { { it(transaction) } }
                            )
                        }
                        
                        // Loading more indicator
                        if (state.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
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
                selectedTypes = state.selectedTransactionTypes,
                selectedSortBy = state.sortBy,
                idOrSlugFilter = state.idOrSlugFilter,
                startDate = state.startDate,
                endDate = state.endDate,
                dateFilterType = state.dateFilterType,
                selectedParty = state.selectedParty,
                selectedArea = state.selectedArea,
                selectedCategory = state.selectedCategory,
                showActiveTransactions = state.showActiveTransactions,
                showDeletedTransactions = state.showDeletedTransactions,
                onTypeToggled = { viewModel.toggleTransactionTypeFilter(it) },
                onClearTransactionTypes = { viewModel.clearTransactionTypeFilters() },
                onSortBySelected = { viewModel.setSortBy(it) },
                onIdOrSlugFilterChange = { viewModel.setIdOrSlugFilter(it) },
                onStartDateChange = { viewModel.setDateRange(it, state.endDate) },
                onEndDateChange = { viewModel.setDateRange(state.startDate, it) },
                onDateFilterTypeChange = { viewModel.setDateFilterType(it) },
                onSelectParty = {
                    viewModel.toggleFilters() // Close filter sheet first
                    onSelectParty() // Then navigate to parties screen
                },
                onClearPartyFilter = { viewModel.setPartyFilter(null) },
                onSelectArea = {
                    viewModel.toggleFilters()
                    onSelectArea()
                },
                onClearAreaFilter = { viewModel.setAreaFilter(null) },
                onSelectCategory = {
                    viewModel.toggleFilters()
                    onSelectCategory()
                },
                onClearCategoryFilter = { viewModel.setCategoryFilter(null) },
                onToggleShowActive = { viewModel.toggleShowActiveTransactions() },
                onToggleShowDeleted = { viewModel.toggleShowDeletedTransactions() },
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
    selectedTypes: Set<AllTransactionTypes>,
    selectedSortBy: TransactionSortOption,
    idOrSlugFilter: String,
    startDate: Long?,
    endDate: Long?,
    dateFilterType: TransactionSortOption,
    selectedParty: Party?,
    selectedArea: CategoryEntity?,
    selectedCategory: CategoryEntity?,
    showActiveTransactions: Boolean,
    showDeletedTransactions: Boolean,
    onTypeToggled: (AllTransactionTypes) -> Unit,
    onClearTransactionTypes: () -> Unit,
    onSortBySelected: (TransactionSortOption) -> Unit,
    onIdOrSlugFilterChange: (String) -> Unit,
    onStartDateChange: (Long?) -> Unit,
    onEndDateChange: (Long?) -> Unit,
    onDateFilterTypeChange: (TransactionSortOption) -> Unit,
    onSelectParty: () -> Unit,
    onClearPartyFilter: () -> Unit,
    onSelectArea: () -> Unit,
    onClearAreaFilter: () -> Unit,
    onSelectCategory: () -> Unit,
    onClearCategoryFilter: () -> Unit,
    onToggleShowActive: () -> Unit,
    onToggleShowDeleted: () -> Unit,
    onClearFilters: () -> Unit,
    onApplyFilters: () -> Unit
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val currentTime = Clock.System.now().toEpochMilliseconds()
    val listState = rememberLazyListState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Filter Transactions",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = onClearFilters,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Clear All",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }

        // ID/Slug filter section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "ID/Slug",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        item {
            OutlinedTextField(
                value = idOrSlugFilter,
                onValueChange = onIdOrSlugFilterChange,
                label = { Text("Transaction ID or slug") },
                placeholder = { Text("Enter transaction ID or slug") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    if (idOrSlugFilter.isNotEmpty()) {
                        IconButton(onClick = { onIdOrSlugFilterChange("") }) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    }
                }
            )
        }

        item {
            Spacer(Modifier.height(24.dp))
        }

        // Party filter section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Party",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSelectParty),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedParty == null)
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = if (selectedParty == null)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                selectedParty?.name ?: "Select Party",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selectedParty != null) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedParty == null)
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                else
                                    MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            selectedParty?.phone?.let { phone ->
                                Text(
                                    phone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if (selectedParty != null) {
                        IconButton(onClick = onClearPartyFilter) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    } else {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Select",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
        }

        // Area filter section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Party Area",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSelectArea),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedArea == null)
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (selectedArea == null)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            selectedArea?.title ?: "Select Area",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selectedArea != null) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedArea == null)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    if (selectedArea != null) {
                        IconButton(onClick = onClearAreaFilter) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    } else {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Select",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
        }

        // Category filter section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Category,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Party Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSelectCategory),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedCategory == null)
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Category,
                            contentDescription = null,
                            tint = if (selectedCategory == null)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            selectedCategory?.title ?: "Select Category",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selectedCategory != null) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedCategory == null)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    if (selectedCategory != null) {
                        IconButton(onClick = onClearCategoryFilter) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    } else {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Select",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
        }

        // Date filter section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Date Range",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Date filter type selection
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipWithColors(
                    selected = dateFilterType == TransactionSortOption.ENTRY_DATE,
                    onClick = { onDateFilterTypeChange(TransactionSortOption.ENTRY_DATE) },
                    label = "Entry date",
                    modifier = Modifier.weight(1f)
                )
                FilterChipWithColors(
                    selected = dateFilterType == TransactionSortOption.TRANSACTION_DATE,
                    onClick = { onDateFilterTypeChange(TransactionSortOption.TRANSACTION_DATE) },
                    label = "Transaction date",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(Modifier.height(12.dp))
        }

        // Start date field
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showStartDatePicker = true }
            ) {
                OutlinedTextField(
                    value = startDate?.let { formatDate(it) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Start Date") },
                    placeholder = { Text("Select start date") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.CalendarToday, "Date") },
                    trailingIcon = {
                        if (startDate != null) {
                            IconButton(onClick = { onStartDateChange(null) }) {
                                Icon(Icons.Default.Clear, "Clear")
                            }
                        }
                    },
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

        item {
            Spacer(Modifier.height(12.dp))
        }

        // End date field
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showEndDatePicker = true }
            ) {
                OutlinedTextField(
                    value = endDate?.let { formatDate(it) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("End Date") },
                    placeholder = { Text("Select end date") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.CalendarToday, "Date") },
                    trailingIcon = {
                        if (endDate != null) {
                            IconButton(onClick = { onEndDateChange(null) }) {
                                Icon(Icons.Default.Clear, "Clear")
                            }
                        }
                    },
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

        item {
            Spacer(Modifier.height(24.dp))
        }

        // Sort by section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Sort,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Sort by",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        item {
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
        }

        item {
            Spacer(Modifier.height(24.dp))
        }

        // Transaction Status Filter
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Transaction Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipWithColors(
                    selected = showActiveTransactions,
                    onClick = onToggleShowActive,
                    label = "Active Transactions",
                    modifier = Modifier.weight(1f)
                )
                FilterChipWithColors(
                    selected = showDeletedTransactions,
                    onClick = onToggleShowDeleted,
                    label = "Deleted Transactions",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
        }

        // Transaction type filter
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Transaction Type",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (selectedTypes.isNotEmpty()) {
                    TextButton(
                        onClick = onClearTransactionTypes,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Clear",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        // Group transaction types by category for better organization
        val basicTypes = AllTransactionTypes.entries.filter { it.category == TransactionCategory.BASIC }
        val cashPaymentTypes = AllTransactionTypes.entries.filter { it.category == TransactionCategory.CASH_PAYMENT }
        val expenseIncomeTypes = AllTransactionTypes.entries.filter { it.category == TransactionCategory.EXPENSE_INCOME }
        val stockAdjustmentTypes = AllTransactionTypes.entries.filter { it.category == TransactionCategory.STOCK_ADJUSTMENT }
        val recordTypes = AllTransactionTypes.entries.filter { it.category == TransactionCategory.RECORD }
        val otherTypes = AllTransactionTypes.entries.filter { it.category == TransactionCategory.OTHER }
        
        // Helper function to render chips in rows (3 per row) using items
        fun renderTypeChipsItems(types: List<AllTransactionTypes>) {
            types.chunked(3).forEachIndexed { index, rowTypes ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (index < types.chunked(3).size - 1) 8.dp else 0.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowTypes.forEach { type ->
                            FilterChipWithColors(
                                selected = selectedTypes.contains(type),
                                onClick = { onTypeToggled(type) },
                                label = type.displayName,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Fill remaining space if row has less than 3 items
                        repeat(3 - rowTypes.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        
        // Basic Transaction Types
        if (basicTypes.isNotEmpty()) {
            item {
                Text(
                    "Basic",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }
            renderTypeChipsItems(basicTypes)
            item {
                Spacer(Modifier.height(12.dp))
            }
        }
        
        // Cash Payment Types
        if (cashPaymentTypes.isNotEmpty()) {
            item {
                Text(
                    "Cash Payment",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }
            renderTypeChipsItems(cashPaymentTypes)
            item {
                Spacer(Modifier.height(12.dp))
            }
        }
        
        // Expense & Income Types
        if (expenseIncomeTypes.isNotEmpty()) {
            item {
                Text(
                    "Expense & Income",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }
            renderTypeChipsItems(expenseIncomeTypes)
            item {
                Spacer(Modifier.height(12.dp))
            }
        }
        
        // Stock Adjustment Types
        if (stockAdjustmentTypes.isNotEmpty()) {
            item {
                Text(
                    "Stock Adjustment",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }
            renderTypeChipsItems(stockAdjustmentTypes)
            item {
                Spacer(Modifier.height(12.dp))
            }
        }
        
        // Record Types
        if (recordTypes.isNotEmpty()) {
            item {
                Text(
                    "Records",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }
            renderTypeChipsItems(recordTypes)
            item {
                Spacer(Modifier.height(12.dp))
            }
        }
        
        // Other Types
        if (otherTypes.isNotEmpty()) {
            item {
                Text(
                    "Other",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }
            renderTypeChipsItems(otherTypes)
            item {
                Spacer(Modifier.height(12.dp))
            }
        }
        
   
        // Apply button
        item {
            Button(
                onClick = onApplyFilters,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Text(
                    "Apply Filters",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        // Bottom padding for safe area
        item {
            Spacer(Modifier.height(24.dp))
        }
    }
    
    // Start Date Picker Dialog
    if (showStartDatePicker) {
        SimpleDatePickerDialog(
            initialTimestamp = startDate,
            onConfirm = { timestamp ->
                onStartDateChange(timestamp)
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }
    
    // End Date Picker Dialog
    if (showEndDatePicker) {
        SimpleDatePickerDialog(
            initialTimestamp = endDate,
            onConfirm = { timestamp ->
                onEndDateChange(timestamp)
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
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
        Spacer(Modifier.height(4.dp))
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
    currencySymbol: String,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    transactionDetailsCounts: Map<String, Int>,
    manufactureInfo: Map<String, ManufactureInfo>,
    receiptViewModel: ReceiptViewModel,
    onConvertToSale: (() -> Unit)? = null,
    onEditAndConvertToSale: (() -> Unit)? = null,
    onConvertToPurchase: (() -> Unit)? = null,
    onEditAndConvertToPurchase: (() -> Unit)? = null,
    onCancelAndRemove: (() -> Unit)? = null,
    onRestore: (() -> Unit)? = null,
    onClone: (() -> Unit)? = null,
    onChangeStateToPending: (() -> Unit)? = null,
    onChangeStateToInProgress: (() -> Unit)? = null,
    onChangeStateToCompleted: (() -> Unit)? = null,
    onChangeStateToCanceled: (() -> Unit)? = null,
    onOutstandingBalanceReminder: (() -> Unit)? = null
) {
    // Check if transaction is deleted (status_id = 2)
    val isDeleted = transaction.statusId == 2
    
    Box(modifier = Modifier.fillMaxWidth()) {
        // Determine card type based on transaction type
        when {
            AllTransactionTypes.isRecord(transaction.transactionType) -> {
                RecordTransactionCard(
                    transaction, currencySymbol, onClick, onDeleteClick, onEditClick, receiptViewModel,
                    onRestore, onChangeStateToPending, onChangeStateToInProgress, onChangeStateToCompleted, 
                    onChangeStateToCanceled, onOutstandingBalanceReminder
                )
            }
            AllTransactionTypes.isPayGetCash(transaction.transactionType) -> {
                PayGetCashCard(transaction, currencySymbol, onClick, onDeleteClick, onEditClick, receiptViewModel, onRestore)
            }
            AllTransactionTypes.isExpenseIncome(transaction.transactionType) -> {
                ExpenseIncomeCard(transaction, currencySymbol, onClick, onDeleteClick, onEditClick, receiptViewModel, onRestore)
            }
            transaction.transactionType == AllTransactionTypes.PAYMENT_TRANSFER.value -> {
                PaymentTransferCard(transaction, currencySymbol, onClick, onDeleteClick, onEditClick, receiptViewModel, onRestore)
            }
            transaction.transactionType == AllTransactionTypes.JOURNAL_VOUCHER.value -> {
                JournalVoucherCard(transaction, currencySymbol, onClick, onDeleteClick, onEditClick, receiptViewModel, onRestore)
            }
            AllTransactionTypes.isStockAdjustment(transaction.transactionType) -> {
                StockAdjustmentCard(transaction, currencySymbol, onClick, onDeleteClick, onEditClick, receiptViewModel, transactionDetailsCounts, onRestore)
            }
            transaction.transactionType == AllTransactionTypes.MANUFACTURE.value -> {
                val info = manufactureInfo[transaction.slug]
                ManufactureCard(transaction, currencySymbol, onClick, onDeleteClick, onEditClick, receiptViewModel, info, onRestore)
            }
            AllTransactionTypes.isOrder(transaction.transactionType) || transaction.transactionType == AllTransactionTypes.QUOTATION.value -> {
                OrderQuotationCard(
                    transaction, onClick, onDeleteClick, onEditClick, receiptViewModel, 
                    transactionDetailsCounts, currencySymbol,
                    onConvertToSale, onEditAndConvertToSale, 
                    onConvertToPurchase, onEditAndConvertToPurchase,
                    onCancelAndRemove, onRestore, onClone
                )
            }
            else -> {
                // Basic transaction card (Sale, Purchase, Returns)
                BasicTransactionCard(
                    transaction, onClick, onDeleteClick, onEditClick, receiptViewModel, 
                    transactionDetailsCounts, currencySymbol, onRestore, onClone
                )
            }
        }
        
        // Gray overlay for deleted transactions with rounded corners
        if (isDeleted) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        color = Color.Gray.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
            )
        }
    }
}

// ============= CARD HEADER COMPONENTS =============

// Custom attractive colors for transaction badges
private data class BadgeColors(
    val backgroundColor: Color,
    val textColor: Color
)

@Composable
private fun getBadgeColors(transactionType: Int): BadgeColors {
    return when (AllTransactionTypes.fromValue(transactionType)) {
        // Sales - Blue tones
        AllTransactionTypes.SALE -> BadgeColors(
            backgroundColor = Color(0xFF2196F3), // Bright Blue
            textColor = Color.White
        )
        AllTransactionTypes.SALE_ORDER -> BadgeColors(
            backgroundColor = Color(0xFF42A5F5), // Light Blue
            textColor = Color.White
        )
        
        // Purchases - Green tones
        AllTransactionTypes.PURCHASE -> BadgeColors(
            backgroundColor = Color(0xFF4CAF50), // Green
            textColor = Color.White
        )
        AllTransactionTypes.PURCHASE_ORDER -> BadgeColors(
            backgroundColor = Color(0xFF66BB6A), // Light Green
            textColor = Color.White
        )
        
        // Returns - Red/Orange tones
        AllTransactionTypes.CUSTOMER_RETURN -> BadgeColors(
            backgroundColor = Color(0xFFF44336), // Red
            textColor = Color.White
        )
        AllTransactionTypes.VENDOR_RETURN -> BadgeColors(
            backgroundColor = Color(0xFFFF5722), // Deep Orange
            textColor = Color.White
        )
        
        // Money Incoming - Teal/Cyan tones
        AllTransactionTypes.GET_FROM_CUSTOMER -> BadgeColors(
            backgroundColor = Color(0xFF00BCD4), // Cyan
            textColor = Color.White
        )
        AllTransactionTypes.GET_FROM_VENDOR -> BadgeColors(
            backgroundColor = Color(0xFF00ACC1), // Teal
            textColor = Color.White
        )
        AllTransactionTypes.INVESTMENT_WITHDRAW -> BadgeColors(
            backgroundColor = Color(0xFF0097A7), // Dark Cyan
            textColor = Color.White
        )
        AllTransactionTypes.EXTRA_INCOME -> BadgeColors(
            backgroundColor = Color(0xFF26A69A), // Teal Green
            textColor = Color.White
        )
        
        // Money Outgoing - Purple/Pink tones
        AllTransactionTypes.PAY_TO_CUSTOMER -> BadgeColors(
            backgroundColor = Color(0xFF9C27B0), // Purple
            textColor = Color.White
        )
        AllTransactionTypes.PAY_TO_VENDOR -> BadgeColors(
            backgroundColor = Color(0xFFAB47BC), // Light Purple
            textColor = Color.White
        )
        AllTransactionTypes.INVESTMENT_DEPOSIT -> BadgeColors(
            backgroundColor = Color(0xFF7B1FA2), // Dark Purple
            textColor = Color.White
        )
        
        // Expenses - Deep Red/Orange
        AllTransactionTypes.EXPENSE -> BadgeColors(
            backgroundColor = Color(0xFFE91E63), // Pink/Red
            textColor = Color.White
        )
        
        // Transfers - Indigo
        AllTransactionTypes.PAYMENT_TRANSFER -> BadgeColors(
            backgroundColor = Color(0xFF3F51B5), // Indigo
            textColor = Color.White
        )
        
        // Journal - Deep Blue
        AllTransactionTypes.JOURNAL_VOUCHER -> BadgeColors(
            backgroundColor = Color(0xFF1976D2), // Deep Blue
            textColor = Color.White
        )
        
        // Stock Operations - Teal/Green tones
        AllTransactionTypes.STOCK_TRANSFER -> BadgeColors(
            backgroundColor = Color(0xFF00897B), // Teal
            textColor = Color.White
        )
        AllTransactionTypes.STOCK_INCREASE -> BadgeColors(
            backgroundColor = Color(0xFF43A047), // Green
            textColor = Color.White
        )
        AllTransactionTypes.STOCK_REDUCE -> BadgeColors(
            backgroundColor = Color(0xFFE53935), // Red
            textColor = Color.White
        )
        
        // Manufacture - Orange/Amber
        AllTransactionTypes.MANUFACTURE -> BadgeColors(
            backgroundColor = Color(0xFFFF9800), // Orange
            textColor = Color.White
        )
        
        // Quotations - Amber/Yellow
        AllTransactionTypes.QUOTATION -> BadgeColors(
            backgroundColor = Color(0xFFFFC107), // Amber
            textColor = Color(0xFF424242) // Dark text for contrast
        )
        
        // Records/Notes - Various pastel colors
        AllTransactionTypes.MEETING -> BadgeColors(
            backgroundColor = Color(0xFF9575CD), // Light Purple
            textColor = Color.White
        )
        AllTransactionTypes.TASK -> BadgeColors(
            backgroundColor = Color(0xFF5C6BC0), // Indigo
            textColor = Color.White
        )
        AllTransactionTypes.CLIENT_NOTE -> BadgeColors(
            backgroundColor = Color(0xFF7986CB), // Light Indigo
            textColor = Color.White
        )
        AllTransactionTypes.SELF_NOTE -> BadgeColors(
            backgroundColor = Color(0xFFBDBDBD), // Grey
            textColor = Color(0xFF212121) // Dark text
        )
        AllTransactionTypes.CASH_REMINDER -> BadgeColors(
            backgroundColor = Color(0xFFFF6B6B), // Light Red
            textColor = Color.White
        )
        
        // Default
        else -> BadgeColors(
            backgroundColor = Color(0xFF757575), // Grey
            textColor = Color.White
        )
    }
}

@Composable
private fun CardHeader(
    transaction: Transaction,
    onOptionsClick: () -> Unit
) {
    val badgeColors = getBadgeColors(transaction.transactionType)
    
    Box(modifier = Modifier.fillMaxWidth()) {
        // Transaction type badge - positioned at top-start with negative offset to align with parent
        // Shape that only rounds the top-start corner to match parent Card (Material3 medium corner radius is 12.dp)
        val badgeShape = RoundedCornerShape(
            topStart = 12.dp,
            topEnd = 0.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )
        Surface(
            color = badgeColors.backgroundColor,
            shape = badgeShape
        ) {
            Text(
                transaction.getTransactionTypeName(),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = badgeColors.textColor
            )
        }
        
        // Dates and options - positioned at top-end
        Row(
            modifier = Modifier.align(Alignment.TopEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Sync status and timestamp row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SyncStatusIndicator(syncStatus = transaction.syncStatus)
                    transaction.timestamp?.let { timestamp ->
                        Text(
                            formatTransactionDate(timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
private fun SyncStatusIndicator(syncStatus: Int) {
    val status = SyncStatus.fromValue(syncStatus)
    
    val (icon, tint, description) = when (status) {
        SyncStatus.SYNCED -> Triple(
            Icons.Default.CloudDone,
            Color(0xFF4CAF50), // Green
            "Synced"
        )
        SyncStatus.NONE -> Triple(
            Icons.Default.CloudUpload,
            Color(0xFFFF9800), // Orange
            "Pending sync"
        )
        SyncStatus.UPDATED -> Triple(
            Icons.Default.CloudSync,
            Color(0xFF2196F3), // Blue
            "Updated, pending sync"
        )
    }
    
    Icon(
        imageVector = icon,
        contentDescription = description,
        modifier = Modifier.size(14.dp),
        tint = tint
    )
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
    transaction: Transaction,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onReceiptClick: () -> Unit,
    onConvertToSale: (() -> Unit)? = null,
    onEditAndConvertToSale: (() -> Unit)? = null,
    onConvertToPurchase: (() -> Unit)? = null,
    onEditAndConvertToPurchase: (() -> Unit)? = null,
    onCancelAndRemove: (() -> Unit)? = null,
    onRestore: (() -> Unit)? = null,
    onClone: (() -> Unit)? = null,
    onChangeStateToPending: (() -> Unit)? = null,
    onChangeStateToInProgress: (() -> Unit)? = null,
    onChangeStateToCompleted: (() -> Unit)? = null,
    onChangeStateToCanceled: (() -> Unit)? = null,
    onOutstandingBalanceReminder: (() -> Unit)? = null
) {
    val isDeleted = !transaction.isActive
    val transactionType = AllTransactionTypes.fromValue(transaction.transactionType)
    val isRecordType = AllTransactionTypes.isRecord(transaction.transactionType)
    val isCashTransfer = transaction.transactionType == AllTransactionTypes.PAYMENT_TRANSFER.value
    val isJournal = transaction.transactionType == AllTransactionTypes.JOURNAL_VOUCHER.value
    val currentState = TransactionState.fromValue(transaction.stateId)
    
    // Determine if we should show Generate Receipt and Clone options
    val showGenerateReceipt = !isDeleted && !isCashTransfer && !isJournal && !isRecordType
    val showClone = !isDeleted && !isCashTransfer && !isJournal && !isRecordType
    
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        // For deleted transactions
        if (isDeleted) {
            onRestore?.let {
                DropdownMenuItem(
                    text = { Text("Restore") },
                    onClick = {
                        onDismiss()
                        it()
                    },
                    leadingIcon = { Icon(Icons.Default.Undo, null) }
                )
            }
            DropdownMenuItem(
                text = { Text("View Details") },
                onClick = {
                    onDismiss()
                    onClick()
                },
                leadingIcon = { Icon(Icons.Default.Visibility, null) }
            )
            if (showClone) {
                onClone?.let {
                    DropdownMenuItem(
                        text = { Text("Clone Transaction") },
                        onClick = {
                            onDismiss()
                            it()
                        },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, null) }
                    )
                }
            }
        } else {
            // For active transactions
            
            // Generate Receipt (if applicable)
            if (showGenerateReceipt) {
                DropdownMenuItem(
                    text = { Text("Generate Receipt") },
                    onClick = {
                        onDismiss()
                        onReceiptClick()
                    },
                    leadingIcon = { Icon(Icons.Default.Receipt, null) }
                )
            }
            
            // View Details
            DropdownMenuItem(
                text = { Text("View Details") },
                onClick = {
                    onDismiss()
                    onClick()
                },
                leadingIcon = { Icon(Icons.Default.Visibility, null) }
            )
            
            // Edit
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    onDismiss()
                    onEditClick()
                },
                leadingIcon = { Icon(Icons.Default.Edit, null) }
            )
            
            // Delete
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    onDismiss()
                    onDeleteClick()
                },
                leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
            )
            
            // Clone Transaction (if applicable)
            if (showClone) {
                onClone?.let {
                    DropdownMenuItem(
                        text = { Text("Clone Transaction") },
                        onClick = {
                            onDismiss()
                            it()
                        },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, null) }
                    )
                }
            }
            
            // Transaction type-specific options
            when (transactionType) {
                AllTransactionTypes.SALE_ORDER, AllTransactionTypes.QUOTATION -> {
                    HorizontalDivider()
                    onConvertToSale?.let {
                        DropdownMenuItem(
                            text = { Text("Convert to Sale") },
                            onClick = {
                                onDismiss()
                                it()
                            },
                            leadingIcon = { Icon(Icons.Default.SwapHoriz, null) }
                        )
                    }
                    onEditAndConvertToSale?.let {
                        DropdownMenuItem(
                            text = { Text("Edit and Convert to Sale") },
                            onClick = {
                                onDismiss()
                                it()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                    }
                    onCancelAndRemove?.let {
                        DropdownMenuItem(
                            text = { Text("Cancel and Remove") },
                            onClick = {
                                onDismiss()
                                it()
                            },
                            leadingIcon = { Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
                
                AllTransactionTypes.PURCHASE_ORDER -> {
                    HorizontalDivider()
                    onConvertToPurchase?.let {
                        DropdownMenuItem(
                            text = { Text("Convert to Purchase") },
                            onClick = {
                                onDismiss()
                                it()
                            },
                            leadingIcon = { Icon(Icons.Default.SwapHoriz, null) }
                        )
                    }
                    onEditAndConvertToPurchase?.let {
                        DropdownMenuItem(
                            text = { Text("Edit and Convert to Purchase") },
                            onClick = {
                                onDismiss()
                                it()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                    }
                    onCancelAndRemove?.let {
                        DropdownMenuItem(
                            text = { Text("Cancel and Remove") },
                            onClick = {
                                onDismiss()
                                it()
                            },
                            leadingIcon = { Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
                
                AllTransactionTypes.MEETING, AllTransactionTypes.TASK, AllTransactionTypes.CASH_REMINDER -> {
                    HorizontalDivider()
                    
                    // Outstanding Balance Reminder (only for CASH_REMINDER)
                    if (transactionType == AllTransactionTypes.CASH_REMINDER) {
                        onOutstandingBalanceReminder?.let {
                            DropdownMenuItem(
                                text = { Text("Outstanding Balance Reminder") },
                                onClick = {
                                    onDismiss()
                                    it()
                                },
                                leadingIcon = { Icon(Icons.Default.Notifications, null) }
                            )
                        }
                    }
                    
                    // State management options (only show if not already in that state)
                    if (currentState != TransactionState.PENDING) {
                        onChangeStateToPending?.let {
                            DropdownMenuItem(
                                text = { Text("Convert to Pending") },
                                onClick = {
                                    onDismiss()
                                    it()
                                },
                                leadingIcon = { Icon(Icons.Default.Schedule, null) }
                            )
                        }
                    }
                    
                    if (currentState != TransactionState.IN_PROGRESS) {
                        onChangeStateToInProgress?.let {
                            DropdownMenuItem(
                                text = { Text("Convert to In Progress") },
                                onClick = {
                                    onDismiss()
                                    it()
                                },
                                leadingIcon = { Icon(Icons.Default.PlayArrow, null) }
                            )
                        }
                    }
                    
                    if (currentState != TransactionState.COMPLETED) {
                        onChangeStateToCompleted?.let {
                            DropdownMenuItem(
                                text = { Text("Convert to Completed") },
                                onClick = {
                                    onDismiss()
                                    it()
                                },
                                leadingIcon = { Icon(Icons.Default.CheckCircle, null) }
                            )
                        }
                    }
                    
                    if (currentState != TransactionState.CANCELLED) {
                        onChangeStateToCanceled?.let {
                            DropdownMenuItem(
                                text = { Text("Convert to Cancelled") },
                                onClick = {
                                    onDismiss()
                                    it()
                                },
                                leadingIcon = { Icon(Icons.Default.Close, null) }
                            )
                        }
                    }
                }
                
                else -> {
                    // No additional options for other types
                }
            }
        }
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
    transactionDetailsCounts: Map<String, Int>,
    currencySymbol: String,
    onRestore: (() -> Unit)? = null,
    onClone: (() -> Unit)? = null
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
        Column {
            CardHeader(transaction, { showOptions = !showOptions })
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

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
                            "$currencySymbol ${"%.2f".format(transaction.totalBill)}",
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
                                "$currencySymbol ${"%.2f".format(transaction.totalPaid)}",
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
                            DetailChip("−$currencySymbol${"%.0f".format(transaction.flatDiscount)}", MaterialTheme.colorScheme.errorContainer)
                        }
                        if (transaction.flatTax > 0) {
                            DetailChip("+$currencySymbol${"%.0f".format(transaction.flatTax)}", MaterialTheme.colorScheme.tertiaryContainer)
                        }
                        if (transaction.additionalCharges > 0) {
                            DetailChip("+$currencySymbol${"%.0f".format(transaction.additionalCharges)}", MaterialTheme.colorScheme.secondaryContainer)
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
        }

        
        TransactionOptionsMenu(
            transaction = transaction,
            expanded = showOptions,
            onDismiss = { showOptions = false },
            onClick = onClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onReceiptClick = { receiptViewModel.showPreview(transaction) },
            onRestore = onRestore,
            onClone = onClone
        )
    }
}

// ============= PAY/GET CASH CARD =============

@Composable
private fun PayGetCashCard(
    transaction: Transaction,
    currencySymbol: String,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    receiptViewModel: ReceiptViewModel,
    onRestore: (() -> Unit)? = null
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
        Column {
            CardHeader(transaction, { showOptions = !showOptions })
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

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
                        "$currencySymbol ${"%.2f".format(transaction.totalPaid)}",
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
        }


        
        TransactionOptionsMenu(
            transaction = transaction,
            expanded = showOptions,
            onDismiss = { showOptions = false },
            onClick = onClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onReceiptClick = { receiptViewModel.showPreview(transaction) },
            onRestore = onRestore
        )
    }
}

// ============= EXPENSE/INCOME CARD =============

@Composable
private fun ExpenseIncomeCard(
    transaction: Transaction,
    currencySymbol: String,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    receiptViewModel: ReceiptViewModel,
    onRestore: (() -> Unit)? = null
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
        Column {
            CardHeader(transaction, { showOptions = !showOptions })
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                HorizontalDivider()

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
                        "$currencySymbol ${"%.2f".format(transaction.totalPaid)}",
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
        }

        
        TransactionOptionsMenu(
            transaction = transaction,
            expanded = showOptions,
            onDismiss = { showOptions = false },
            onClick = onClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onReceiptClick = { receiptViewModel.showPreview(transaction) },
            onRestore = onRestore
        )
    }
}

// ============= PAYMENT TRANSFER CARD =============

@Composable
private fun PaymentTransferCard(
    transaction: Transaction,
    currencySymbol: String,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    receiptViewModel: ReceiptViewModel,
    onRestore: (() -> Unit)? = null
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
        Column {
            CardHeader(transaction, { showOptions = !showOptions })
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                        "$currencySymbol ${"%.2f".format(transaction.totalPaid)}",
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
        }

        
        TransactionOptionsMenu(
            transaction = transaction,
            expanded = showOptions,
            onDismiss = { showOptions = false },
            onClick = onClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onReceiptClick = { receiptViewModel.showPreview(transaction) },
            onRestore = onRestore
        )
    }
}

// ============= JOURNAL VOUCHER CARD =============

@Composable
private fun JournalVoucherCard(
    transaction: Transaction,
    currencySymbol: String,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    receiptViewModel: ReceiptViewModel,
    onRestore: (() -> Unit)? = null
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
        Column {
            CardHeader(transaction, { showOptions = !showOptions })
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                HorizontalDivider()

                // Pay Amount/Get Amount Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pay Amount
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Pay Amount",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "$currencySymbol ${"%.2f".format(transaction.totalPaid)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    VerticalDivider(modifier = Modifier.height(50.dp))

                    // Get Amount
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Get Amount",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "$currencySymbol ${"%.2f".format(transaction.totalPaid)}",
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
        }

        
        TransactionOptionsMenu(
            transaction = transaction,
            expanded = showOptions,
            onDismiss = { showOptions = false },
            onClick = onClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onReceiptClick = { receiptViewModel.showPreview(transaction) },
            onRestore = onRestore
        )
    }
}

// ============= STOCK ADJUSTMENT CARD =============

@Composable
private fun StockAdjustmentCard(
    transaction: Transaction,
    currencySymbol: String,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    receiptViewModel: ReceiptViewModel,
    transactionDetailsCounts: Map<String, Int>,
    onRestore: (() -> Unit)? = null
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
        Column {
            CardHeader(transaction, { showOptions = !showOptions })
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

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
        }

        
        TransactionOptionsMenu(
            transaction = transaction,
            expanded = showOptions,
            onDismiss = { showOptions = false },
            onClick = onClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onReceiptClick = { receiptViewModel.showPreview(transaction) },
            onRestore = onRestore
        )
    }
}

// ============= MANUFACTURE CARD =============

@Composable
private fun ManufactureCard(
    transaction: Transaction,
    currencySymbol: String,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    receiptViewModel: ReceiptViewModel,
    manufactureInfo: ManufactureInfo?,
    onRestore: (() -> Unit)? = null
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
        Column {
            CardHeader(transaction, { showOptions = !showOptions })
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Recipe name
                if (manufactureInfo != null) {
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
                            manufactureInfo.recipeName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider()

                // Cost and Quantity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Manufacturing Cost (from transaction.totalPaid which stores the total cost)
                    Column {
                        Text(
                            "Total Cost",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "$currencySymbol ${"%.2f".format(transaction.totalPaid)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    // Quantity
                    if (manufactureInfo != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Quantity",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                manufactureInfo.recipeQuantity,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
        }

        
        TransactionOptionsMenu(
            transaction = transaction,
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
    transactionDetailsCounts: Map<String, Int>,
    currencySymbol: String,
    onConvertToSale: (() -> Unit)? = null,
    onEditAndConvertToSale: (() -> Unit)? = null,
    onConvertToPurchase: (() -> Unit)? = null,
    onEditAndConvertToPurchase: (() -> Unit)? = null,
    onCancelAndRemove: (() -> Unit)? = null,
    onRestore: (() -> Unit)? = null,
    onClone: (() -> Unit)? = null
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
        Column {
            CardHeader(transaction, { showOptions = !showOptions })
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

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
                            "$currencySymbol ${"%.2f".format(transaction.totalBill)}",
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
        }

        
        TransactionOptionsMenu(
            transaction = transaction,
            expanded = showOptions,
            onDismiss = { showOptions = false },
            onClick = onClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onReceiptClick = { receiptViewModel.showPreview(transaction) },
            onConvertToSale = onConvertToSale,
            onEditAndConvertToSale = onEditAndConvertToSale,
            onConvertToPurchase = onConvertToPurchase,
            onEditAndConvertToPurchase = onEditAndConvertToPurchase,
            onCancelAndRemove = onCancelAndRemove,
            onRestore = onRestore,
            onClone = onClone
        )
    }
}

// ============= RECORD TRANSACTION CARD =============

@Composable
private fun RecordTransactionCard(
    transaction: Transaction,
    currencySymbol: String,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    receiptViewModel: ReceiptViewModel,
    onRestore: (() -> Unit)? = null,
    onChangeStateToPending: (() -> Unit)? = null,
    onChangeStateToInProgress: (() -> Unit)? = null,
    onChangeStateToCompleted: (() -> Unit)? = null,
    onChangeStateToCanceled: (() -> Unit)? = null,
    onOutstandingBalanceReminder: (() -> Unit)? = null
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
        Column {
            CardHeader(transaction, { showOptions = !showOptions })
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

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
                            "$currencySymbol ${"%.2f".format(transaction.totalPaid)}",
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
                    val statusColors = getStatusBadgeColors(transaction.stateId)
                    Surface(
                        color = statusColors.backgroundColor,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            transaction.getStateName(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColors.textColor
                        )
                    }
                }
            }
        }

        
        TransactionOptionsMenu(
            transaction = transaction,
            expanded = showOptions,
            onDismiss = { showOptions = false },
            onClick = onClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onReceiptClick = { receiptViewModel.showPreview(transaction) },
            onRestore = onRestore,
            onChangeStateToPending = onChangeStateToPending,
            onChangeStateToInProgress = onChangeStateToInProgress,
            onChangeStateToCompleted = onChangeStateToCompleted,
            onChangeStateToCanceled = onChangeStateToCanceled,
            onOutstandingBalanceReminder = onOutstandingBalanceReminder
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
