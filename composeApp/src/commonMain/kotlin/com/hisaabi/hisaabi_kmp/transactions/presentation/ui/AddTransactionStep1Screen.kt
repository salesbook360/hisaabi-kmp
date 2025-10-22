package com.hisaabi.hisaabi_kmp.transactions.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import com.hisaabi.hisaabi_kmp.transactions.domain.model.FlatOrPercent
import com.hisaabi.hisaabi_kmp.transactions.domain.model.PriceType
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionCategory
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddTransactionViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.TransactionDetailItem
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionStep1Screen(
    viewModel: AddTransactionViewModel,
    onNavigateBack: () -> Unit,
    onSelectParty: () -> Unit,
    onSelectProducts: () -> Unit,
    onSelectWarehouse: () -> Unit,
    onProceedToStep2: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    // Navigate to step 2 when validation passes
    LaunchedEffect(state.currentStep) {
        if (state.currentStep == 2) {
            onProceedToStep2()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("New ${state.transactionType.displayName}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Transaction Type Selector
                    var showTypeMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showTypeMenu = true }) {
                        Icon(Icons.Default.SwapHoriz, "Transaction Type")
                    }
                    DropdownMenu(
                        expanded = showTypeMenu,
                        onDismissRequest = { showTypeMenu = false }
                    ) {
                        AllTransactionTypes.entries.filter {
                            it.category == TransactionCategory.BASIC
                        }.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    viewModel.setTransactionType(type)
                                    showTypeMenu = false
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.transactionDetails.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.goToStep2() },
                    icon = { Icon(Icons.Default.ArrowForward, "Next") },
                    text = { Text("Next Step") }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Party Selection Card
            item {
                PartySelectionCard(
                    selectedParty = state.selectedParty,
                    transactionType = state.transactionType,
                    onSelectParty = onSelectParty
                )
            }
            
            // Warehouse Selection Card (if needed)
            if (AllTransactionTypes.affectsStock(state.transactionType.value)) {
                item {
                    WarehouseSelectionCard(
                        selectedWarehouse = state.selectedWarehouse,
                        onSelectWarehouse = onSelectWarehouse
                    )
                }
            }
            
            // Price Type Selector
            item {
                PriceTypeSelector(
                    selectedPriceType = state.priceType,
                    transactionType = state.transactionType,
                    onPriceTypeSelected = { viewModel.setPriceType(it) }
                )
            }
            
            // Products Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Products",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = onSelectProducts,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Add, "Add", Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Products")
                    }
                }
            }
            
            // Product List
            if (state.transactionDetails.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No products added",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Tap 'Add Products' to get started",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(state.transactionDetails) { index, item ->
                    ProductItemCard(
                        item = item,
                        index = index,
                        viewModel = viewModel
                    )
                }
            }
            
            // Summary Card
            if (state.transactionDetails.isNotEmpty()) {
                item {
                    SummaryCard(
                        subtotal = viewModel.calculateSubtotal(),
                        itemCount = state.transactionDetails.size
                    )
                }
            }
            
            // Bottom padding for FAB
            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun PartySelectionCard(
    selectedParty: Party?,
    transactionType: AllTransactionTypes,
    onSelectParty: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelectParty),
        colors = CardDefaults.cardColors(
            containerColor = if (selectedParty == null) 
                MaterialTheme.colorScheme.errorContainer 
            else 
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (selectedParty == null) 
                    MaterialTheme.colorScheme.onErrorContainer 
                else 
                    MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (AllTransactionTypes.isDealingWithVendor(transactionType.value)) "Vendor" else "Customer",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selectedParty == null) 
                        MaterialTheme.colorScheme.onErrorContainer 
                    else 
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    selectedParty?.name ?: "Select ${if (AllTransactionTypes.isDealingWithVendor(transactionType.value)) "Vendor" else "Customer"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedParty == null) 
                        MaterialTheme.colorScheme.onErrorContainer 
                    else 
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
                selectedParty?.let {
                    Text(
                        "Balance: ₨ ${String.format("%.2f", kotlin.math.abs(it.balance))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selectedParty == null) 
                            MaterialTheme.colorScheme.onErrorContainer 
                        else 
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (selectedParty == null) 
                    MaterialTheme.colorScheme.onErrorContainer 
                else 
                    MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun WarehouseSelectionCard(
    selectedWarehouse: Warehouse?,
    onSelectWarehouse: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelectWarehouse)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warehouse,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Warehouse",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    selectedWarehouse?.title ?: "Select Warehouse",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
private fun PriceTypeSelector(
    selectedPriceType: PriceType,
    transactionType: AllTransactionTypes,
    onPriceTypeSelected: (PriceType) -> Unit
) {
    if (AllTransactionTypes.isDealingWithVendor(transactionType.value)) {
        return // Purchase transactions only use purchase price
    }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Price Type",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(PriceType.RETAIL, PriceType.WHOLESALE).forEach { priceType ->
                    FilterChip(
                        selected = selectedPriceType == priceType,
                        onClick = { onPriceTypeSelected(priceType) },
                        label = { Text(priceType.displayName) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductItemCard(
    item: TransactionDetailItem,
    index: Int,
    viewModel: AddTransactionViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Product Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.product.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "₨ ${String.format("%.2f", item.calculateTotal())}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            "Expand"
                        )
                    }
                    IconButton(onClick = { viewModel.removeProduct(index) }) {
                        Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Quantity and Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Quantity
                OutlinedTextField(
                    value = item.quantity.toString(),
                    onValueChange = { 
                        it.toDoubleOrNull()?.let { qty -> 
                            viewModel.updateProductQuantity(index, qty)
                        }
                    },
                    label = { Text("Quantity") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                // Price
                OutlinedTextField(
                    value = item.price.toString(),
                    onValueChange = { 
                        it.toDoubleOrNull()?.let { price -> 
                            viewModel.updateProductPrice(index, price)
                        }
                    },
                    label = { Text("Price") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    prefix = { Text("₨ ") }
                )
            }
            
            // Expanded Details
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                
                // Discount
                var discountValue by remember { mutableStateOf(item.flatDiscount.toString()) }
                var discountType by remember { mutableStateOf(item.discountType) }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = discountValue,
                        onValueChange = { 
                            discountValue = it
                            it.toDoubleOrNull()?.let { disc -> 
                                viewModel.updateProductDiscount(index, disc, discountType)
                            }
                        },
                        label = { Text("Discount") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    FilterChip(
                        selected = discountType == FlatOrPercent.FLAT,
                        onClick = { 
                            discountType = FlatOrPercent.FLAT
                            discountValue.toDoubleOrNull()?.let { disc ->
                                viewModel.updateProductDiscount(index, disc, discountType)
                            }
                        },
                        label = { Text("₨") }
                    )
                    FilterChip(
                        selected = discountType == FlatOrPercent.PERCENT,
                        onClick = { 
                            discountType = FlatOrPercent.PERCENT
                            discountValue.toDoubleOrNull()?.let { disc ->
                                viewModel.updateProductDiscount(index, disc, discountType)
                            }
                        },
                        label = { Text("%") }
                    )
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Tax
                var taxValue by remember { mutableStateOf(item.flatTax.toString()) }
                var taxType by remember { mutableStateOf(item.taxType) }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = taxValue,
                        onValueChange = { 
                            taxValue = it
                            it.toDoubleOrNull()?.let { tax -> 
                                viewModel.updateProductTax(index, tax, taxType)
                            }
                        },
                        label = { Text("Tax") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    FilterChip(
                        selected = taxType == FlatOrPercent.FLAT,
                        onClick = { 
                            taxType = FlatOrPercent.FLAT
                            taxValue.toDoubleOrNull()?.let { tax ->
                                viewModel.updateProductTax(index, tax, taxType)
                            }
                        },
                        label = { Text("₨") }
                    )
                    FilterChip(
                        selected = taxType == FlatOrPercent.PERCENT,
                        onClick = { 
                            taxType = FlatOrPercent.PERCENT
                            taxValue.toDoubleOrNull()?.let { tax ->
                                viewModel.updateProductTax(index, tax, taxType)
                            }
                        },
                        label = { Text("%") }
                    )
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Description
                OutlinedTextField(
                    value = item.description,
                    onValueChange = { viewModel.updateProductDescription(index, it) },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    subtotal: Double,
    itemCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                    "Subtotal",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    "₨ ${String.format("%.2f", subtotal)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Text(
                "$itemCount ${if (itemCount == 1) "item" else "items"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

