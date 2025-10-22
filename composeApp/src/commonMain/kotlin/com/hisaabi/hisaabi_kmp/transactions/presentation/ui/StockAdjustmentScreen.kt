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
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionDetail
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.StockAdjustmentViewModel
import com.hisaabi.hisaabi_kmp.utils.SimpleDateTimePickerDialog
import com.hisaabi.hisaabi_kmp.utils.formatDateTime
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockAdjustmentScreen(
    viewModel: StockAdjustmentViewModel,
    onNavigateBack: () -> Unit,
    onSelectWarehouseFrom: () -> Unit,
    onSelectWarehouseTo: () -> Unit,
    onSelectProducts: () -> Unit
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
            snackbarHostState.showSnackbar("Stock adjustment saved successfully")
            viewModel.clearSuccess()
            onNavigateBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Stock Adjustment") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.saveStockAdjustment() },
                icon = { Icon(Icons.Default.Save, "Save") },
                text = { Text("Save") }
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
                label = "Date & Time",
                timestamp = state.dateTime,
                onDateTimeClick = { showDateTimePicker = true }
            )

            // Adjustment Type Segment Control
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "Adjustment Type",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            AllTransactionTypes.STOCK_INCREASE,
                            AllTransactionTypes.STOCK_REDUCE,
                            AllTransactionTypes.STOCK_TRANSFER
                        ).forEach { type ->
                            FilterChip(
                                selected = state.adjustmentType == type,
                                onClick = { viewModel.setAdjustmentType(type) },
                                label = { 
                                    Text(
                                        when (type) {
                                            AllTransactionTypes.STOCK_INCREASE -> "Increase"
                                            AllTransactionTypes.STOCK_REDUCE -> "Reduce"
                                            AllTransactionTypes.STOCK_TRANSFER -> "Transfer"
                                            else -> "Unknown"
                                        }
                                    ) 
                                },
                                modifier = Modifier.weight(1f),
                                leadingIcon = if (state.adjustmentType == type) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            // Warehouse From (always visible)
            WarehouseSelectionCard(
                title = when (state.adjustmentType) {
                    AllTransactionTypes.STOCK_TRANSFER -> "From Warehouse *"
                    else -> "Warehouse *"
                },
                selectedWarehouse = state.warehouseFrom,
                onSelectWarehouse = onSelectWarehouseFrom,
                icon = Icons.Default.Warehouse
            )

            // Warehouse To (only for transfer)
            if (state.adjustmentType == AllTransactionTypes.STOCK_TRANSFER) {
                WarehouseSelectionCard(
                    title = "To Warehouse *",
                    selectedWarehouse = state.warehouseTo,
                    onSelectWarehouse = onSelectWarehouseTo,
                    icon = Icons.Default.MoveToInbox
                )
            }

            // Products Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
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
                        TextButton(onClick = onSelectProducts) {
                            Icon(Icons.Default.Add, "Add Product", modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Product")
                        }
                    }

                    if (state.products.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No products added",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.products.forEach { detail ->
                                ProductCard(
                                    detail = detail,
                                    onQuantityChange = { quantity ->
                                        detail.productSlug?.let { viewModel.updateProductQuantity(it, quantity) }
                                    },
                                    onRemove = {
                                        detail.productSlug?.let { viewModel.removeProduct(it) }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Description
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.setDescription(it) },
                label = { Text("Remarks (Optional)") },
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
private fun WarehouseSelectionCard(
    title: String,
    selectedWarehouse: Warehouse?,
    onSelectWarehouse: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelectWarehouse),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    if (selectedWarehouse != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedWarehouse.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap to select",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            Icon(
                if (selectedWarehouse != null) Icons.Default.Edit else Icons.Default.ArrowForwardIos,
                contentDescription = "Select Warehouse",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun ProductCard(
    detail: TransactionDetail,
    onQuantityChange: (Double) -> Unit,
    onRemove: () -> Unit
) {
    var quantityText by remember(detail.quantity) { 
        mutableStateOf(detail.quantity.toString()) 
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    detail.product?.title ?: "Product",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { 
                        quantityText = it
                        it.toDoubleOrNull()?.let { qty -> onQuantityChange(qty) }
                    },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}


