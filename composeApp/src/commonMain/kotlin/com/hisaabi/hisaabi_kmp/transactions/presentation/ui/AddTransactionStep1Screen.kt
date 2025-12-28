package com.hisaabi.hisaabi_kmp.transactions.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.core.ui.FilterChipWithColors
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import com.hisaabi.hisaabi_kmp.transactions.domain.model.FlatOrPercent
import com.hisaabi.hisaabi_kmp.transactions.domain.model.PriceType
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionCategory
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddTransactionViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.TransactionDetailItem
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse
import com.hisaabi.hisaabi_kmp.utils.format
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

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
                    onSelectParty = onSelectParty,
                    currencySymbol
                )
            }

            // Warehouse Selection Card (mandatory for specific transaction types)
            val isEditMode = state.editingTransactionSlug != null
            if (AllTransactionTypes.requiresWarehouse(state.transactionType.value)) {
                item {
                    WarehouseSelectionCard(
                        selectedWarehouse = state.selectedWarehouse,
                        onSelectWarehouse = onSelectWarehouse,
                        isMandatory = true,
                        enabled = !isEditMode
                    )
                }
            } else if (AllTransactionTypes.affectsStock(state.transactionType.value)) {
                // For other stock-affecting transactions (like Manufacture), show optional warehouse selection
                item {
                    WarehouseSelectionCard(
                        selectedWarehouse = state.selectedWarehouse,
                        onSelectWarehouse = onSelectWarehouse,
                        isMandatory = false,
                        enabled = !isEditMode
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
                val canAddProducts = viewModel.isWarehouseSelectedOrNotRequired()
                
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
                        modifier = Modifier.height(36.dp),
                        enabled = canAddProducts
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
                    val requiresWarehouse = AllTransactionTypes.requiresWarehouse(state.transactionType.value)
                    val warehouseSelected = state.selectedWarehouse != null
                    
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
                                if (requiresWarehouse && !warehouseSelected) 
                                    Icons.Default.Warehouse 
                                else 
                                    Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                if (requiresWarehouse && !warehouseSelected)
                                    "Select a warehouse first"
                                else
                                    "No products added",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (requiresWarehouse && !warehouseSelected)
                                    "Please select a warehouse to view product quantities"
                                else
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
                        viewModel = viewModel,
                        currencySymbol
                    )
                }
            }

            // Summary Card
            if (state.transactionDetails.isNotEmpty()) {
                item {
                    SummaryCard(
                        subtotal = viewModel.calculateSubtotal(),
                        itemCount = state.transactionDetails.size,
                        currencySymbol
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
    onSelectParty: () -> Unit,
    currencySymbol:String
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
                    selectedParty?.name ?: "Select ${
                        if (AllTransactionTypes.isDealingWithVendor(
                                transactionType.value
                            )
                        ) "Vendor" else "Customer"
                    }",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedParty == null)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
                selectedParty?.let {
                    Text(
                        "Balance: $currencySymbol ${"%.2f".format(kotlin.math.abs(it.balance))}",
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
    onSelectWarehouse: () -> Unit,
    isMandatory: Boolean = false,
    enabled: Boolean = true
) {
    val isNotSelected = selectedWarehouse == null
    val showError = isMandatory && isNotSelected && enabled
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (enabled) {
                    Modifier.clickable(onClick = onSelectWarehouse)
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (!enabled)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            else if (showError)
                MaterialTheme.colorScheme.errorContainer
            else if (selectedWarehouse != null)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
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
                modifier = Modifier.size(40.dp),
                tint = if (!enabled)
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                else if (showError)
                    MaterialTheme.colorScheme.onErrorContainer
                else if (selectedWarehouse != null)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Warehouse",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (!enabled)
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        else if (showError)
                            MaterialTheme.colorScheme.onErrorContainer
                        else if (selectedWarehouse != null)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isMandatory && enabled) {
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "*",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Text(
                    selectedWarehouse?.title ?: "Select Warehouse",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (!enabled)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else if (showError)
                        MaterialTheme.colorScheme.onErrorContainer
                    else if (selectedWarehouse != null)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (showError) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Required before adding products",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            if (enabled) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = if (showError)
                        MaterialTheme.colorScheme.onErrorContainer
                    else if (selectedWarehouse != null)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                    FilterChipWithColors(
                        selected = selectedPriceType == priceType,
                        onClick = { onPriceTypeSelected(priceType) },
                        label = priceType.displayName,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductItemCard(
    item: TransactionDetailItem,
    index: Int,
    viewModel: AddTransactionViewModel,
    currencySymbol:String
) {
    var expanded by remember { mutableStateOf(false) }
    var showUnitSelectionSheet by remember { mutableStateOf(false) }
    var siblingUnits by remember { mutableStateOf<List<QuantityUnit>>(emptyList()) }
    var isLoadingUnits by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    // Unit Selection Bottom Sheet
    if (showUnitSelectionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showUnitSelectionSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Select Unit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                
                if (isLoadingUnits) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (siblingUnits.isEmpty()) {
                    Text(
                        "No units available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    siblingUnits.forEach { unit ->
                        val isSelected = item.selectedUnit?.slug == unit.slug
                        ListItem(
                            headlineContent = { 
                                Text(
                                    unit.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            supportingContent = if (unit.conversionFactor != 1.0) {
                                { Text("Factor: ${unit.conversionFactor}") }
                            } else null,
                            leadingContent = {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.updateProductUnit(index, unit)
                                        showUnitSelectionSheet = false
                                    }
                                )
                            },
                            modifier = Modifier.clickable {
                                viewModel.updateProductUnit(index, unit)
                                showUnitSelectionSheet = false
                            }
                        )
                    }
                }
                
                Spacer(Modifier.height(32.dp))
            }
        }
    }

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
                        "$currencySymbol ${"%.2f".format(item.calculateTotal())}",
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

            // Quantity, Unit and Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Price - Simple string input with stable key using product slug
                var priceText by remember(item.product.slug) {
                    mutableStateOf(
                        if (item.price == 0.0) ""
                        else item.price.toString().trimEnd('0').trimEnd('.')
                    )
                }
                
                // Sync price from ViewModel when it changes externally (e.g., price type change)
                LaunchedEffect(item.price) {
                    val newPriceText = if (item.price == 0.0) "" 
                        else item.price.toString().trimEnd('0').trimEnd('.')
                    // Only update if the values are different (avoid overwriting during typing)
                    val currentValue = priceText.toDoubleOrNull() ?: 0.0
                    if (kotlin.math.abs(currentValue - item.price) > 0.001) {
                        priceText = newPriceText
                    }
                }

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { newText ->
                        // Allow any text input that matches number pattern
                        if (newText.isEmpty() || newText.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                            priceText = newText
                            // Only update viewModel when there's a valid complete number
                            // Don't update for empty, incomplete decimals, or invalid input
                            if (newText.isNotEmpty() && !newText.endsWith(".") && newText != "-" && newText != "-.") {
                                newText.toDoubleOrNull()?.let { price ->
                                    viewModel.updateProductPrice(index, price)
                                }
                            }
                        }
                    },
                    label = { Text("Price") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    prefix = { Text("$currencySymbol ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                
                // Quantity - Simple string input with stable key using product slug
                var quantityText by remember(item.product.slug) { 
                    mutableStateOf(
                        if (item.quantity == 0.0) "" 
                        else item.quantity.toString().trimEnd('0').trimEnd('.')
                    )
                }
                
                // Sync quantity from ViewModel when it changes externally (e.g., loading for edit)
                LaunchedEffect(item.quantity) {
                    val newQuantityText = if (item.quantity == 0.0) "" 
                        else item.quantity.toString().trimEnd('0').trimEnd('.')
                    // Only update if the values are different (avoid overwriting during typing)
                    val currentValue = quantityText.toDoubleOrNull() ?: 0.0
                    if (kotlin.math.abs(currentValue - item.quantity) > 0.001) {
                        quantityText = newQuantityText
                    }
                }
                
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { newText ->
                        // Allow any text input that matches number pattern
                        if (newText.isEmpty() || newText.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                            quantityText = newText
                            // Only update viewModel when there's a valid complete number
                            // Don't update for empty, incomplete decimals, or invalid input
                            if (newText.isNotEmpty() && !newText.endsWith(".") && newText != "-" && newText != "-.") {
                                newText.toDoubleOrNull()?.let { qty ->
                                    viewModel.updateProductQuantity(index, qty)
                                }
                            }
                        }
                    },
                    label = { Text("Qty") },
                    modifier = Modifier.weight(0.8f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                
                // Unit Selection Button
                val unitText = item.selectedUnit?.title ?: "Unit"
                val hasUnit = item.selectedUnit != null
                
                OutlinedButton(
                    onClick = {
                        // Load sibling units when button is clicked
                        coroutineScope.launch {
                            isLoadingUnits = true
                            showUnitSelectionSheet = true
                            siblingUnits = viewModel.getSiblingUnits(item.selectedUnit)
                            isLoadingUnits = false
                        }
                    },
                    modifier = Modifier.weight(0.6f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    enabled = hasUnit // Only enable if there's a unit to get siblings from
                ) {
                    Text(
                        unitText,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (hasUnit) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Select unit",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }


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

                    FilterChipWithColors(
                        selected = discountType == FlatOrPercent.FLAT,
                        onClick = {
                            discountType = FlatOrPercent.FLAT
                            discountValue.toDoubleOrNull()?.let { disc ->
                                viewModel.updateProductDiscount(index, disc, discountType)
                            }
                        },
                        label = currencySymbol
                    )
                    FilterChipWithColors(
                        selected = discountType == FlatOrPercent.PERCENT,
                        onClick = {
                            discountType = FlatOrPercent.PERCENT
                            discountValue.toDoubleOrNull()?.let { disc ->
                                viewModel.updateProductDiscount(index, disc, discountType)
                            }
                        },
                        label = "%"
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

                    FilterChipWithColors(
                        selected = taxType == FlatOrPercent.FLAT,
                        onClick = {
                            taxType = FlatOrPercent.FLAT
                            taxValue.toDoubleOrNull()?.let { tax ->
                                viewModel.updateProductTax(index, tax, taxType)
                            }
                        },
                        label = currencySymbol
                    )
                    FilterChipWithColors(
                        selected = taxType == FlatOrPercent.PERCENT,
                        onClick = {
                            taxType = FlatOrPercent.PERCENT
                            taxValue.toDoubleOrNull()?.let { tax ->
                                viewModel.updateProductTax(index, tax, taxType)
                            }
                        },
                        label = "%"
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
    itemCount: Int,
    currencySymbol:String
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
                    "$currencySymbol ${"%.2f".format(subtotal)}",
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

