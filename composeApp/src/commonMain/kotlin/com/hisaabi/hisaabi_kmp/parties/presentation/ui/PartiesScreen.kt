package com.hisaabi.hisaabi_kmp.parties.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaabi.hisaabi_kmp.core.ui.SegmentedControl
import com.hisaabi.hisaabi_kmp.parties.domain.model.*
import com.hisaabi.hisaabi_kmp.parties.presentation.viewmodel.PartiesViewModel
import com.hisaabi.hisaabi_kmp.utils.format
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartiesScreen(
    viewModel: PartiesViewModel,
    onPartyClick: (Party) -> Unit = {},
    onAddPartyClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onSegmentChanged: (PartySegment) -> Unit = {},  // Callback to notify parent of segment changes
    initialSegment: PartySegment? = null,
    refreshTrigger: Int = 0,  // Increment this to trigger a refresh
    isExpenseIncomeContext: Boolean = false,  // Flag to show only Expense/Income segments
    // Navigation callbacks for bottom sheet actions
    onPayGetPayment: (Party) -> Unit = {},
    onEditParty: (Party) -> Unit = {},
    onViewTransactions: (Party) -> Unit = {},
    onViewBalanceHistory: (Party) -> Unit = {},
    onPaymentReminder: (Party) -> Unit = {},
    onNewTransaction: (Party, Int) -> Unit = { _, _ -> }  // Party and transaction type
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var showFilterMenu by remember { mutableStateOf(false) }
    var selectedParty by remember { mutableStateOf<Party?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    
    // Set initial segment if provided
    LaunchedEffect(initialSegment) {
        initialSegment?.let { viewModel.onSegmentChanged(it) }
    }
    
    // Refresh when trigger changes
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            viewModel.refresh()
        }
    }
    
    // Detect scroll to bottom for pagination
    LaunchedEffect(listState.canScrollForward) {
        if (!listState.canScrollForward && !uiState.isLoadingMore) {
            viewModel.loadMoreParties()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parties") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Filter Button
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                        
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            PartiesFilter.entries.forEach { filter ->
                                DropdownMenuItem(
                                    text = { Text(filter.getDisplayName()) },
                                    onClick = {
                                        viewModel.onFilterChanged(filter)
                                        showFilterMenu = false
                                    },
                                    leadingIcon = {
                                        if (uiState.selectedFilter == filter) {
                                            Icon(Icons.Default.Check, contentDescription = null)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    
                    // Search Button
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPartyClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Party")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            if (showSearchBar || uiState.searchQuery.isNotEmpty()) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search parties...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged(""); showSearchBar = false }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true
                )
            }
            // Segment Control
            PartySegmentControl(
                selected = uiState.selectedSegment,
                onSegmentSelected = { segment ->
                    viewModel.onSegmentChanged(segment)
                    onSegmentChanged(segment)  // Notify parent of segment change
                },
                isExpenseIncomeContext = isExpenseIncomeContext,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            
            // Stats Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total ${uiState.selectedSegment.name}s",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${uiState.totalCount}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Balance",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = formatBalance(uiState.totalBalance),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                uiState.totalBalance > 0 -> Color(0xFF4CAF50)
                                uiState.totalBalance < 0 -> Color(0xFFF44336)
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Parties List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: "An error occurred",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Retry")
                        }
                    }
                }
            } else if (uiState.parties.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No ${uiState.selectedSegment.name.lowercase()}s found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onAddPartyClick) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add ${uiState.selectedSegment.name}")
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(uiState.parties, key = { it.id }) { party ->
                        PartyItem(
                            party = party,
                            onClick = { 
                                selectedParty = party
                                onPartyClick(party)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Loading more indicator
                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Bottom Sheet for Party Actions
        selectedParty?.let { party ->
            PartyActionsBottomSheet(
                party = party,
                onDismiss = { selectedParty = null },
                onPayGetPayment = { onPayGetPayment(party) },
                onEdit = { onEditParty(party) },
                onDelete = { 
                    showDeleteDialog = true
                },
                onTransactions = { onViewTransactions(party) },
                onBalanceHistory = { onViewBalanceHistory(party) },
                onPaymentReminder = { onPaymentReminder(party) },
                onNewTransaction = { transactionType ->
                    onNewTransaction(party, transactionType)
                }
            )
        }
        
        // Delete Confirmation Dialog
        if (showDeleteDialog && selectedParty != null) {
            DeletePartyDialog(
                party = selectedParty!!,
                onConfirm = {
                    viewModel.deleteParty(selectedParty!!)
                    showDeleteDialog = false
                    selectedParty = null  // This closes the bottom sheet too
                },
                onDismiss = {
                    showDeleteDialog = false
                    // Keep selectedParty so bottom sheet stays open
                }
            )
        }
    }
}

@Composable
private fun PartySegmentControl(
    selected: PartySegment,
    onSegmentSelected: (PartySegment) -> Unit,
    isExpenseIncomeContext: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Filter segments based on context
    val visibleSegments = if (isExpenseIncomeContext) {
        listOf(PartySegment.EXPENSE, PartySegment.EXTRA_INCOME)
    } else {
        listOf(PartySegment.CUSTOMER, PartySegment.VENDOR, PartySegment.INVESTOR)
    }
    
    SegmentedControl(
        items = visibleSegments,
        selectedItem = selected,
        onItemSelected = onSegmentSelected,
        modifier = modifier,
        itemDisplayName = { segment ->
            when (segment) {
                PartySegment.EXPENSE -> "Expense"
                PartySegment.EXTRA_INCOME -> "Extra Income"
                else -> segment.name
            }
        }
    )
}

@Composable
private fun PartyItem(
    party: Party,
    onClick: () -> Unit
) {
    // Check if this is an expense/income type (roleId 14 or 15)
    val isExpenseIncomeType = party.roleId in listOf(14, 15)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isExpenseIncomeType) {
                            if (party.roleId == 14) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isExpenseIncomeType) {
                    Icon(
                        imageVector = if (party.roleId == 14) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = if (party.roleId == 14) 
                            MaterialTheme.colorScheme.onErrorContainer 
                        else 
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Text(
                        text = party.name.firstOrNull()?.uppercase()?.toString() ?: "?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Party Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = party.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Slug
                if (!party.slug.isBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Slug: ${party.slug}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // For expense/income types, show type label instead of phone/address
                if (isExpenseIncomeType) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (party.roleId == 14) "Expense Type" else "Income Type",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    if (!party.phone.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = party.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    if (!party.address.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = party.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            // Show balance only for regular parties (not expense/income types)
            if (!isExpenseIncomeType) {
                Spacer(modifier = Modifier.width(16.dp))
                
                // Balance
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatBalance(abs(party.balance)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (party.balanceStatus) {
                            BalanceStatus.PAYABLE -> Color(0xFF4CAF50)  // Green - you will pay
                            BalanceStatus.RECEIVABLE -> Color(0xFFF44336)  // Red - you will get
                            BalanceStatus.ZERO -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    Text(
                        text = when (party.balanceStatus) {
                            BalanceStatus.PAYABLE -> "You'll Pay"
                            BalanceStatus.RECEIVABLE -> "You'll Get"
                            BalanceStatus.ZERO -> "Settled"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = when (party.balanceStatus) {
                            BalanceStatus.PAYABLE -> Color(0xFF4CAF50)
                            BalanceStatus.RECEIVABLE -> Color(0xFFF44336)
                            BalanceStatus.ZERO -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

private fun formatBalance(balance: Double): String {
    return "₨ %.2f".format(balance)
}

private fun PartiesFilter.getDisplayName(): String = when (this) {
    PartiesFilter.ALL_PARTIES -> "All Parties"
    PartiesFilter.BALANCE_RECEIVABLE -> "Balance Receivable"
    PartiesFilter.BALANCE_PAYABLE -> "Balance Payable"
    PartiesFilter.BALANCE_ZERO -> "Balance Zero"
}

