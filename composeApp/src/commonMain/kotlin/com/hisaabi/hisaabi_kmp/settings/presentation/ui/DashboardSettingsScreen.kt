package com.hisaabi.hisaabi_kmp.settings.presentation.ui

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.core.ui.LocalWindowSizeClass
import com.hisaabi.hisaabi_kmp.core.ui.WindowWidthSizeClass
import com.hisaabi.hisaabi_kmp.settings.domain.model.*
import com.hisaabi.hisaabi_kmp.settings.presentation.viewmodel.DashboardSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardSettingsScreen(
    viewModel: DashboardSettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val config = state.config
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            snackbarHostState.showSnackbar("Dashboard settings saved successfully")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveSettings() },
                        enabled = !state.isSaving
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Check, "Save")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.saveSettings() },
                icon = { 
                    if (state.isSaving) {
                        CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                    } else {
                        Icon(Icons.Default.Save, "Save")
                    }
                },
                text = { Text(if (state.isSaving) "Saving..." else "Save Settings") },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    ) { paddingValues ->
        val windowSizeClass = LocalWindowSizeClass.current
        val isDesktop = windowSizeClass.widthSizeClass == WindowWidthSizeClass.EXPANDED
        val maxContentWidth = if (isDesktop) 900.dp else Dp.Unspecified
        val horizontalPadding = if (isDesktop) 24.dp else 16.dp
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                Modifier
                    .then(if (isDesktop) Modifier.widthIn(max = maxContentWidth) else Modifier.fillMaxWidth())
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding, vertical = 16.dp)
            ) {
                Text(
                "Configure dashboard sections",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Balance Overview
            DashboardSectionCard(
                title = "Balance Overview",
                sectionEnabled = config.balanceOverview.showSection,
                onSectionToggle = {
                    viewModel.updateConfig(config.copy(balanceOverview = config.balanceOverview.copy(showSection = it)))
                }
            ) {
                DashboardCheckboxRow(
                    label1 = "Total Available",
                    checked1 = config.balanceOverview.showTotalAvailable,
                    onChecked1 = { viewModel.updateConfig(config.copy(balanceOverview = config.balanceOverview.copy(showTotalAvailable = it))) },
                    label2 = "Customers Balance",
                    checked2 = config.balanceOverview.showCustomersBalance,
                    onChecked2 = { viewModel.updateConfig(config.copy(balanceOverview = config.balanceOverview.copy(showCustomersBalance = it))) }
                )
                DashboardCheckboxRow(
                    label1 = "Vendor Balance",
                    checked1 = config.balanceOverview.showVendorBalance,
                    onChecked1 = { viewModel.updateConfig(config.copy(balanceOverview = config.balanceOverview.copy(showVendorBalance = it))) },
                    label2 = "Investor Balance",
                    checked2 = config.balanceOverview.showInvestorBalance,
                    onChecked2 = { viewModel.updateConfig(config.copy(balanceOverview = config.balanceOverview.copy(showInvestorBalance = it))) }
                )
            }
            
            // Payment Overview
            DashboardSectionCard(
                title = "Payment Overview",
                sectionEnabled = config.paymentOverview.showSection,
                onSectionToggle = {
                    viewModel.updateConfig(config.copy(paymentOverview = config.paymentOverview.copy(showSection = it)))
                }
            ) {
                DashboardCheckboxRow(
                    label1 = "Total Received",
                    checked1 = config.paymentOverview.showTotalReceived,
                    onChecked1 = { viewModel.updateConfig(config.copy(paymentOverview = config.paymentOverview.copy(showTotalReceived = it))) },
                    label2 = "Total Paid",
                    checked2 = config.paymentOverview.showTotalPaid,
                    onChecked2 = { viewModel.updateConfig(config.copy(paymentOverview = config.paymentOverview.copy(showTotalPaid = it))) }
                )
                DashboardCheckboxRow(
                    label1 = "Net Paid/Received",
                    checked1 = config.paymentOverview.showNetPaidReceived,
                    onChecked1 = { viewModel.updateConfig(config.copy(paymentOverview = config.paymentOverview.copy(showNetPaidReceived = it))) },
                    label2 = null,
                    checked2 = false,
                    onChecked2 = {}
                )
            }
            
            // Sales Overview
            DashboardSectionCard(
                title = "Sales Overview",
                sectionEnabled = config.salesOverview.showSection,
                onSectionToggle = {
                    viewModel.updateConfig(config.copy(salesOverview = config.salesOverview.copy(showSection = it)))
                }
            ) {
                DashboardCheckboxRow(
                    label1 = "Total Sale Orders",
                    checked1 = config.salesOverview.showTotalSaleOrders,
                    onChecked1 = { viewModel.updateConfig(config.copy(salesOverview = config.salesOverview.copy(showTotalSaleOrders = it))) },
                    label2 = "Total Sales",
                    checked2 = config.salesOverview.showTotalSales,
                    onChecked2 = { viewModel.updateConfig(config.copy(salesOverview = config.salesOverview.copy(showTotalSales = it))) }
                )
                DashboardCheckboxRow(
                    label1 = "Total Revenue",
                    checked1 = config.salesOverview.showTotalRevenue,
                    onChecked1 = { viewModel.updateConfig(config.copy(salesOverview = config.salesOverview.copy(showTotalRevenue = it))) },
                    label2 = "Total Cost",
                    checked2 = config.salesOverview.showTotalCost,
                    onChecked2 = { viewModel.updateConfig(config.copy(salesOverview = config.salesOverview.copy(showTotalCost = it))) }
                )
                DashboardCheckboxRow(
                    label1 = "Amount Received",
                    checked1 = config.salesOverview.showAmountReceived,
                    onChecked1 = { viewModel.updateConfig(config.copy(salesOverview = config.salesOverview.copy(showAmountReceived = it))) },
                    label2 = "Tax Received",
                    checked2 = config.salesOverview.showTaxReceived,
                    onChecked2 = { viewModel.updateConfig(config.copy(salesOverview = config.salesOverview.copy(showTaxReceived = it))) }
                )
                DashboardCheckboxRow(
                    label1 = "Profit",
                    checked1 = config.salesOverview.showProfit,
                    onChecked1 = { viewModel.updateConfig(config.copy(salesOverview = config.salesOverview.copy(showProfit = it))) },
                    label2 = null,
                    checked2 = false,
                    onChecked2 = {}
                )
            }
            
            // Purchase Overview
            DashboardSectionCard(
                title = "Purchase Overview",
                sectionEnabled = config.purchaseOverview.showSection,
                onSectionToggle = {
                    viewModel.updateConfig(config.copy(purchaseOverview = config.purchaseOverview.copy(showSection = it)))
                }
            ) {
                DashboardCheckboxRow(
                    label1 = "No. of Purchases",
                    checked1 = config.purchaseOverview.showNoOfPurchases,
                    onChecked1 = { viewModel.updateConfig(config.copy(purchaseOverview = config.purchaseOverview.copy(showNoOfPurchases = it))) },
                    label2 = "Purchase Cost",
                    checked2 = config.purchaseOverview.showPurchaseCost,
                    onChecked2 = { viewModel.updateConfig(config.copy(purchaseOverview = config.purchaseOverview.copy(showPurchaseCost = it))) }
                )
                DashboardCheckboxRow(
                    label1 = "Purchase Orders",
                    checked1 = config.purchaseOverview.showPurchaseOrders,
                    onChecked1 = { viewModel.updateConfig(config.copy(purchaseOverview = config.purchaseOverview.copy(showPurchaseOrders = it))) },
                    label2 = "Returned to Vendor",
                    checked2 = config.purchaseOverview.showReturnedToVendor,
                    onChecked2 = { viewModel.updateConfig(config.copy(purchaseOverview = config.purchaseOverview.copy(showReturnedToVendor = it))) }
                )
                DashboardCheckboxRow(
                    label1 = "Tax Paid",
                    checked1 = config.purchaseOverview.showTaxPaid,
                    onChecked1 = { viewModel.updateConfig(config.copy(purchaseOverview = config.purchaseOverview.copy(showTaxPaid = it))) },
                    label2 = null,
                    checked2 = false,
                    onChecked2 = {}
                )
            }
            
            // Inventory Summary
            DashboardSectionCard(
                title = "Inventory Summary",
                sectionEnabled = config.inventorySummary.showSection,
                onSectionToggle = {
                    viewModel.updateConfig(config.copy(inventorySummary = config.inventorySummary.copy(showSection = it)))
                }
            ) {
                DashboardCheckboxRow(
                    label1 = "Quantity in Hand",
                    checked1 = config.inventorySummary.showQuantityInHand,
                    onChecked1 = { viewModel.updateConfig(config.copy(inventorySummary = config.inventorySummary.copy(showQuantityInHand = it))) },
                    label2 = "Qty Will be Received",
                    checked2 = config.inventorySummary.showQuantityWillBeReceived,
                    onChecked2 = { viewModel.updateConfig(config.copy(inventorySummary = config.inventorySummary.copy(showQuantityWillBeReceived = it))) }
                )
            }
            
            // Parties Summary
            DashboardSectionCard(
                title = "Parties Summary",
                sectionEnabled = config.partiesSummary.showSection,
                onSectionToggle = {
                    viewModel.updateConfig(config.copy(partiesSummary = config.partiesSummary.copy(showSection = it)))
                }
            ) {
                DashboardCheckboxRow(
                    label1 = "Total Customers",
                    checked1 = config.partiesSummary.showTotalCustomers,
                    onChecked1 = { viewModel.updateConfig(config.copy(partiesSummary = config.partiesSummary.copy(showTotalCustomers = it))) },
                    label2 = "Total Suppliers",
                    checked2 = config.partiesSummary.showTotalSuppliers,
                    onChecked2 = { viewModel.updateConfig(config.copy(partiesSummary = config.partiesSummary.copy(showTotalSuppliers = it))) }
                )
                DashboardCheckboxRow(
                    label1 = "Total Investors",
                    checked1 = config.partiesSummary.showTotalInvestors,
                    onChecked1 = { viewModel.updateConfig(config.copy(partiesSummary = config.partiesSummary.copy(showTotalInvestors = it))) },
                    label2 = null,
                    checked2 = false,
                    onChecked2 = {}
                )
            }
            
            // Products Summary
            DashboardSectionCard(
                title = "Products Summary",
                sectionEnabled = config.productsSummary.showSection,
                onSectionToggle = {
                    viewModel.updateConfig(config.copy(productsSummary = config.productsSummary.copy(showSection = it)))
                }
            ) {
                DashboardCheckboxRow(
                    label1 = "Total Products",
                    checked1 = config.productsSummary.showTotalProducts,
                    onChecked1 = { viewModel.updateConfig(config.copy(productsSummary = config.productsSummary.copy(showTotalProducts = it))) },
                    label2 = "Product Categories",
                    checked2 = config.productsSummary.showTotalProductCategories,
                    onChecked2 = { viewModel.updateConfig(config.copy(productsSummary = config.productsSummary.copy(showTotalProductCategories = it))) }
                )
                DashboardCheckboxRow(
                    label1 = "Total Recipes",
                    checked1 = config.productsSummary.showTotalRecipes,
                    onChecked1 = { viewModel.updateConfig(config.copy(productsSummary = config.productsSummary.copy(showTotalRecipes = it))) },
                    label2 = "Low Stock Products",
                    checked2 = config.productsSummary.showLowStockProducts,
                    onChecked2 = { viewModel.updateConfig(config.copy(productsSummary = config.productsSummary.copy(showLowStockProducts = it))) }
                )
            }
            
            // Profit/Loss Graph
            DashboardSectionCard(
                title = "Profit/Loss Graph",
                sectionEnabled = config.profitLossGraph.showSection,
                onSectionToggle = {
                    viewModel.updateConfig(config.copy(profitLossGraph = config.profitLossGraph.copy(showSection = it)))
                }
            ) {
                // No sub-options for this section
            }
            
            // Sale/Purchase Graph
            DashboardSectionCard(
                title = "Sale/Purchase Graph",
                sectionEnabled = config.salePurchaseGraph.showSection,
                onSectionToggle = {
                    viewModel.updateConfig(config.copy(salePurchaseGraph = config.salePurchaseGraph.copy(showSection = it)))
                }
            ) {
                // No sub-options for this section
            }
            
            // Bottom padding for FAB
            Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun DashboardSectionCard(
    title: String,
    sectionEnabled: Boolean,
    onSectionToggle: (Boolean) -> Unit,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            // Section Header with toggle
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Switch(checked = sectionEnabled, onCheckedChange = onSectionToggle)
            }
            
            if (sectionEnabled) {
                Spacer(Modifier.height(12.dp))
                content()
            }
        }
    }
}

@Composable
private fun DashboardCheckboxRow(
    label1: String,
    checked1: Boolean,
    onChecked1: (Boolean) -> Unit,
    label2: String?,
    checked2: Boolean,
    onChecked2: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // First checkbox
        Row(
            Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = checked1, onCheckedChange = onChecked1)
            Text(label1, style = MaterialTheme.typography.bodyMedium)
        }
        
        // Second checkbox (if provided)
        if (label2 != null) {
            Row(
                Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = checked2, onCheckedChange = onChecked2)
                Text(label2, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            Spacer(Modifier.weight(1f))
        }
    }
}

