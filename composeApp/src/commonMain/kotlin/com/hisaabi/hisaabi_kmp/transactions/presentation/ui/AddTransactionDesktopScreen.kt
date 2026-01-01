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
import com.hisaabi.hisaabi_kmp.core.ui.FilterChipWithColors
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import com.hisaabi.hisaabi_kmp.transactions.domain.model.FlatOrPercent
import com.hisaabi.hisaabi_kmp.transactions.domain.model.PriceType
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionCategory
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddTransactionViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.TransactionDetailItem
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse
import com.hisaabi.hisaabi_kmp.utils.SimpleDateTimePickerDialog
import com.hisaabi.hisaabi_kmp.utils.formatDateTime
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Combined Add Transaction Screen for Desktop
 * Shows both Step 1 (Products) and Step 2 (Payment) side-by-side
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDesktopScreen(
    viewModel: AddTransactionViewModel,
    onNavigateBack: () -> Unit,
    onSelectParty: () -> Unit,
    onSelectProducts: () -> Unit,
    onSelectWarehouse: () -> Unit,
    onSelectPaymentMethod: () -> Unit,
    onTransactionSaved: (transactionSlug: String?, transactionType: Int) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDateTimePicker by remember { mutableStateOf(false) }
    
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
    
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { message ->
            val transactionSlug = state.savedTransactionSlug
            val transactionType = state.transactionType.value
            viewModel.clearSuccess()
            onTransactionSaved(transactionSlug, transactionType)
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
            // Always show save button - let save function validate and show appropriate errors
            val hasProducts = state.transactionDetails.isNotEmpty()
            
            ExtendedFloatingActionButton(
                onClick = { viewModel.saveTransaction() },
                icon = { Icon(Icons.Default.Save, "Save") },
                text = { Text("Save Transaction") },
                containerColor = if (hasProducts) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Column - Step 1: Products
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Section Header
                Text(
                    "Products & Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Party Selection Card
                    item {
                        DesktopPartySelectionCard(
                            selectedParty = state.selectedParty,
                            transactionType = state.transactionType,
                            onSelectParty = onSelectParty,
                            currencySymbol = currencySymbol
                        )
                    }

                    // Warehouse Selection Card (if needed)
                    val isEditMode = state.editingTransactionSlug != null
                    if (AllTransactionTypes.requiresWarehouse(state.transactionType.value)) {
                        item {
                            DesktopWarehouseSelectionCard(
                                selectedWarehouse = state.selectedWarehouse,
                                onSelectWarehouse = onSelectWarehouse,
                                isMandatory = true,
                                enabled = !isEditMode
                            )
                        }
                    } else if (AllTransactionTypes.affectsStock(state.transactionType.value)) {
                        item {
                            DesktopWarehouseSelectionCard(
                                selectedWarehouse = state.selectedWarehouse,
                                onSelectWarehouse = onSelectWarehouse,
                                isMandatory = false,
                                enabled = !isEditMode
                            )
                        }
                    }

                    // Price Type and Date in a row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Price Type Selector (compact)
                            if (!AllTransactionTypes.isDealingWithVendor(state.transactionType.value)) {
                                Card(modifier = Modifier.weight(1f)) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            "Price Type",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf(PriceType.RETAIL, PriceType.WHOLESALE).forEach { priceType ->
                                                FilterChipWithColors(
                                                    selected = state.priceType == priceType,
                                                    onClick = { viewModel.setPriceType(priceType) },
                                                    label = priceType.displayName
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // Date & Time (compact)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(onClick = { showDateTimePicker = true })
                            ) {
                                OutlinedTextField(
                                    value = formatDateTime(state.transactionDateTime),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Date & Time") },
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
                                "Products (${state.transactionDetails.size})",
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
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        if (requiresWarehouse && !warehouseSelected) 
                                            Icons.Default.Warehouse 
                                        else 
                                            Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        if (requiresWarehouse && !warehouseSelected)
                                            "Select a warehouse first"
                                        else
                                            "No products added",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        itemsIndexed(state.transactionDetails) { index, item ->
                            DesktopProductItemCard(
                                item = item,
                                index = index,
                                viewModel = viewModel,
                                currencySymbol = currencySymbol
                            )
                        }
                    }

                    // Subtotal Summary (compact)
                    if (state.transactionDetails.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Subtotal (${state.transactionDetails.size} items)",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        "$currencySymbol ${"%.2f".format(viewModel.calculateSubtotal())}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                    
                    // Bottom spacing
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
            
            // Vertical Divider
            VerticalDivider(
                modifier = Modifier.fillMaxHeight().padding(vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            // Right Column - Step 2: Payment Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                // Section Header
                Text(
                    "Payment Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Previous Balance Card (compact)
                DesktopPreviousBalanceCard(
                    partyName = state.selectedParty?.name ?: "Select Party",
                    balance = state.previousBalance,
                    currencySymbol = currencySymbol
                )
                
                Spacer(Modifier.height(12.dp))
                
                // Bill Summary Card
                DesktopBillSummaryCard(
                    subtotal = viewModel.calculateSubtotal(),
                    productsDiscount = viewModel.calculateProductsDiscount(),
                    productsTax = viewModel.calculateProductsTax(),
                    additionalCharges = state.additionalCharges,
                    transactionDiscount = viewModel.calculateTransactionDiscount(),
                    transactionTax = viewModel.calculateTransactionTax(),
                    grandTotal = viewModel.calculateGrandTotal(),
                    currencySymbol = currencySymbol
                )
                
                Spacer(Modifier.height(12.dp))
                
                // Additional Charges & Discount in a row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Additional Charges (compact)
                    var additionalChargesText by remember { 
                        mutableStateOf(if (state.additionalCharges > 0) state.additionalCharges.toString() else "") 
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Additional Charges",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = additionalChargesText,
                                onValueChange = { 
                                    additionalChargesText = it
                                    it.toDoubleOrNull()?.let { amt ->
                                        viewModel.updateAdditionalCharges(amt, state.additionalChargesDesc)
                                    }
                                },
                                label = { Text("Amount") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                prefix = { Text("$currencySymbol ") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                    }
                    
                    // Transaction Discount (compact)
                    var discountText by remember { 
                        mutableStateOf(if (state.flatDiscount > 0) state.flatDiscount.toString() else "") 
                    }
                    var discountType by remember { mutableStateOf(state.discountType) }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Discount",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = discountText,
                                    onValueChange = { 
                                        discountText = it
                                        it.toDoubleOrNull()?.let { amt ->
                                            viewModel.updateDiscount(amt, discountType)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                                FilterChipWithColors(
                                    selected = discountType == FlatOrPercent.FLAT,
                                    onClick = { 
                                        discountType = FlatOrPercent.FLAT
                                        discountText.toDoubleOrNull()?.let { amt ->
                                            viewModel.updateDiscount(amt, discountType)
                                        }
                                    },
                                    label = currencySymbol
                                )
                                FilterChipWithColors(
                                    selected = discountType == FlatOrPercent.PERCENT,
                                    onClick = { 
                                        discountType = FlatOrPercent.PERCENT
                                        discountText.toDoubleOrNull()?.let { amt ->
                                            viewModel.updateDiscount(amt, discountType)
                                        }
                                    },
                                    label = "%"
                                )
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                // Tax Section (compact)
                var taxText by remember { 
                    mutableStateOf(if (state.flatTax > 0) state.flatTax.toString() else "") 
                }
                var taxType by remember { mutableStateOf(state.taxType) }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Transaction Tax",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = taxText,
                                onValueChange = { 
                                    taxText = it
                                    it.toDoubleOrNull()?.let { amt ->
                                        viewModel.updateTax(amt, taxType)
                                    }
                                },
                                label = { Text("Tax") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            FilterChipWithColors(
                                selected = taxType == FlatOrPercent.FLAT,
                                onClick = { 
                                    taxType = FlatOrPercent.FLAT
                                    taxText.toDoubleOrNull()?.let { amt ->
                                        viewModel.updateTax(amt, taxType)
                                    }
                                },
                                label = currencySymbol
                            )
                            FilterChipWithColors(
                                selected = taxType == FlatOrPercent.PERCENT,
                                onClick = { 
                                    taxType = FlatOrPercent.PERCENT
                                    taxText.toDoubleOrNull()?.let { amt ->
                                        viewModel.updateTax(amt, taxType)
                                    }
                                },
                                label = "%"
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                // Paid Now Section
                var paidNowText by remember { 
                    mutableStateOf(if (state.paidNow > 0) state.paidNow.toString() else "") 
                }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Paid Now",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = { 
                                    val grandTotal = viewModel.calculateGrandTotal()
                                    paidNowText = grandTotal.toString()
                                    viewModel.updatePaidNow(grandTotal)
                                }
                            ) {
                                Text("Pay Full")
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = paidNowText,
                            onValueChange = { 
                                paidNowText = it
                                it.toDoubleOrNull()?.let { amt ->
                                    viewModel.updatePaidNow(amt)
                                }
                            },
                            label = { Text("Amount") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            prefix = { Text("$currencySymbol ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                // Payable Amount Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.totalPayable > 0) 
                            MaterialTheme.colorScheme.errorContainer 
                        else 
                            MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (state.totalPayable > 0) "To be paid" else "Remaining",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "$currencySymbol ${"%.2f".format(kotlin.math.abs(state.totalPayable))}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                // Payment Method
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onSelectPaymentMethod),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Payment,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Payment Method",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                state.selectedPaymentMethod?.title ?: "Select Payment Method",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                // Remarks & Shipping in a row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.remarks,
                        onValueChange = { viewModel.updateRemarks(it) },
                        label = { Text("Remarks (Optional)") },
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        minLines = 2
                    )
                    OutlinedTextField(
                        value = state.shippingAddress,
                        onValueChange = { viewModel.updateShippingAddress(it) },
                        label = { Text("Shipping Address (Optional)") },
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        minLines = 2
                    )
                }
                
                // Bottom spacing for FAB
                Spacer(Modifier.height(100.dp))
            }
        }
    }
    
    // Date Time Picker Dialog
    if (showDateTimePicker) {
        SimpleDateTimePickerDialog(
            initialTimestamp = state.transactionDateTime,
            onConfirm = { timestamp ->
                viewModel.setTransactionDateTime(timestamp)
                showDateTimePicker = false
            },
            onDismiss = { showDateTimePicker = false }
        )
    }
}

@Composable
private fun DesktopPartySelectionCard(
    selectedParty: Party?,
    transactionType: AllTransactionTypes,
    onSelectParty: () -> Unit,
    currencySymbol: String
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (selectedParty == null)
                    MaterialTheme.colorScheme.onErrorContainer
                else
                    MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (AllTransactionTypes.isDealingWithVendor(transactionType.value)) "Vendor" else "Customer",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selectedParty == null)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    selectedParty?.name ?: "Select ${
                        if (AllTransactionTypes.isDealingWithVendor(transactionType.value)) "Vendor" else "Customer"
                    }",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedParty == null)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            selectedParty?.let {
                Text(
                    "$currencySymbol ${"%.2f".format(kotlin.math.abs(it.balance))}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.width(8.dp))
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
private fun DesktopWarehouseSelectionCard(
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
                if (enabled) Modifier.clickable(onClick = onSelectWarehouse) else Modifier
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warehouse,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (showError)
                    MaterialTheme.colorScheme.onErrorContainer
                else if (selectedWarehouse != null)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Warehouse",
                        style = MaterialTheme.typography.labelSmall
                    )
                    if (isMandatory && enabled) {
                        Text(" *", color = MaterialTheme.colorScheme.error)
                    }
                }
                Text(
                    selectedWarehouse?.title ?: "Select Warehouse",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            if (enabled) {
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DesktopProductItemCard(
    item: TransactionDetailItem,
    index: Int,
    viewModel: AddTransactionViewModel,
    currencySymbol: String
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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Product Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.product.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$currencySymbol ${"%.2f".format(item.calculateTotal())}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            "Expand",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.removeProduct(index) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            "Remove",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Price, Quantity, Unit Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var priceText by remember(item.product.slug) {
                    mutableStateOf(
                        if (item.price == 0.0) ""
                        else item.price.toString().trimEnd('0').trimEnd('.')
                    )
                }
                
                LaunchedEffect(item.price) {
                    val newPriceText = if (item.price == 0.0) "" 
                        else item.price.toString().trimEnd('0').trimEnd('.')
                    val currentValue = priceText.toDoubleOrNull() ?: 0.0
                    if (kotlin.math.abs(currentValue - item.price) > 0.001) {
                        priceText = newPriceText
                    }
                }

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { newText ->
                        if (newText.isEmpty() || newText.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                            priceText = newText
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
                
                var quantityText by remember(item.product.slug) { 
                    mutableStateOf(
                        if (item.quantity == 0.0) "" 
                        else item.quantity.toString().trimEnd('0').trimEnd('.')
                    )
                }
                
                LaunchedEffect(item.quantity) {
                    val newQuantityText = if (item.quantity == 0.0) "" 
                        else item.quantity.toString().trimEnd('0').trimEnd('.')
                    val currentValue = quantityText.toDoubleOrNull() ?: 0.0
                    if (kotlin.math.abs(currentValue - item.quantity) > 0.001) {
                        quantityText = newQuantityText
                    }
                }
                
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { newText ->
                        if (newText.isEmpty() || newText.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                            quantityText = newText
                            if (newText.isNotEmpty() && !newText.endsWith(".") && newText != "-" && newText != "-.") {
                                newText.toDoubleOrNull()?.let { qty ->
                                    viewModel.updateProductQuantity(index, qty)
                                }
                            }
                        }
                    },
                    label = { Text("Qty") },
                    modifier = Modifier.weight(0.7f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                
                val unitText = item.selectedUnit?.title ?: "Unit"
                val hasUnit = item.selectedUnit != null
                
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            isLoadingUnits = true
                            showUnitSelectionSheet = true
                            siblingUnits = viewModel.getSiblingUnits(item.selectedUnit)
                            isLoadingUnits = false
                        }
                    },
                    modifier = Modifier.weight(0.5f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    enabled = hasUnit
                ) {
                    Text(
                        unitText,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Expanded Details (Discount, Tax, Description)
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                // Discount Row
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

                // Tax Row
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
private fun DesktopPreviousBalanceCard(
    partyName: String,
    balance: Double,
    currencySymbol: String
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
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Previous Balance",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    partyName,
                    style = MaterialTheme.typography.titleSmall,
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
                    "$currencySymbol ${"%.2f".format(kotlin.math.abs(balance))}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DesktopBillSummaryCard(
    subtotal: Double,
    productsDiscount: Double,
    productsTax: Double,
    additionalCharges: Double,
    transactionDiscount: Double,
    transactionTax: Double,
    grandTotal: Double,
    currencySymbol: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Bill Summary",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            
            DesktopSummaryRow("Subtotal", subtotal, currencySymbol = currencySymbol)
            if (productsDiscount > 0) {
                DesktopSummaryRow("Products Discount", -productsDiscount, isNegative = true, currencySymbol = currencySymbol)
            }
            if (productsTax > 0) {
                DesktopSummaryRow("Products Tax", productsTax, currencySymbol = currencySymbol)
            }
            if (additionalCharges > 0) {
                DesktopSummaryRow("Additional Charges", additionalCharges, currencySymbol = currencySymbol)
            }
            if (transactionDiscount > 0) {
                DesktopSummaryRow("Transaction Discount", -transactionDiscount, isNegative = true, currencySymbol = currencySymbol)
            }
            if (transactionTax > 0) {
                DesktopSummaryRow("Transaction Tax", transactionTax, currencySymbol = currencySymbol)
            }
            
            Spacer(Modifier.height(4.dp))
            HorizontalDivider()
            Spacer(Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Grand Total",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$currencySymbol ${"%.2f".format(grandTotal)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun DesktopSummaryRow(
    label: String,
    amount: Double,
    isNegative: Boolean = false,
    currencySymbol: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            "$currencySymbol ${"%.2f".format(kotlin.math.abs(amount))}",
            style = MaterialTheme.typography.bodySmall,
            color = if (isNegative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

