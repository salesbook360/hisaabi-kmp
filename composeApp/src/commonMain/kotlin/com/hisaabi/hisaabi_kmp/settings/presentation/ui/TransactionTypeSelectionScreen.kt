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
import com.hisaabi.hisaabi_kmp.settings.presentation.viewmodel.TransactionSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionTypeSelectionScreen(
    viewModel: TransactionSettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val settings = state.settings
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            snackbarHostState.showSnackbar("Settings saved successfully")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Type Selection") },
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
                // Header
                Text(
                "Customize app according to your needs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Cash In/Out Section
            SettingsSwitchCard(
                title = "Manage Cash In/Out",
                description = "This feature allows you to manage your daily cash in/out info",
                checked = settings.isCashInOutEnabled,
                onCheckedChange = { 
                    viewModel.updateSettings(settings.copy(isCashInOutEnabled = it))
                }
            )
            
            // Auto Fill Paid Now (conditional)
            if (settings.isCashInOutEnabled) {
                SettingsSwitchCard(
                    title = "Auto Fill Paid Now",
                    description = "While adding new transaction, auto fill paid field with total bill",
                    checked = settings.isAutoFillPaidNow,
                    onCheckedChange = { 
                        viewModel.updateSettings(settings.copy(isAutoFillPaidNow = it))
                    }
                )
            }
            
            // Manage Customers
            SettingsSwitchCard(
                title = "Manage Customers",
                description = "By enabling this feature you can manage customers",
                checked = settings.isCustomersEnabled,
                onCheckedChange = { 
                    viewModel.updateSettings(settings.copy(isCustomersEnabled = it))
                }
            )
            
            // Allow Purchase from Customer
            SettingsSwitchCard(
                title = "Allow Purchase from Customer",
                description = "Enable this to allow purchase transactions from customers",
                checked = settings.allowPurchaseFromCustomer,
                onCheckedChange = { 
                    viewModel.updateSettings(settings.copy(allowPurchaseFromCustomer = it))
                }
            )
            
            // Customer Grouping
            SettingsDropdownCard(
                title = "Customer Grouping",
                description = "How to group persons/customers",
                selectedIndex = settings.personGrouping,
                options = listOf("By Category", "By Area"),
                onSelectionChange = { 
                    viewModel.updateSettings(settings.copy(personGrouping = it))
                }
            )
            
            // Manage Products
            SettingsSwitchCard(
                title = "Manage Products",
                description = "By enabling this feature you will be able to manage products in/out",
                checked = settings.isProductsEnabled,
                onCheckedChange = { 
                    viewModel.updateSettings(settings.copy(isProductsEnabled = it))
                }
            )
            
            // Manage Services
            SettingsSwitchCard(
                title = "Manage Services",
                description = "Enable this to manage services in your business",
                checked = settings.isServicesEnabled,
                onCheckedChange = { 
                    viewModel.updateSettings(settings.copy(isServicesEnabled = it))
                }
            )
            
            // Product-specific settings
            SettingsSwitchCard(
                title = "Tax on Products",
                description = "Enable tax percentage on individual products",
                checked = settings.enableTaxPercentOnProduct,
                onCheckedChange = { 
                    viewModel.updateSettings(settings.copy(enableTaxPercentOnProduct = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Discount on Products",
                description = "Enable discount percentage on individual products",
                checked = settings.enableDiscountPercentOnProduct,
                onCheckedChange = { 
                    viewModel.updateSettings(settings.copy(enableDiscountPercentOnProduct = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Description with Product",
                description = "Enable description field for each product in transaction",
                checked = settings.enableDescriptionWithProduct,
                onCheckedChange = { 
                    viewModel.updateSettings(settings.copy(enableDescriptionWithProduct = it))
                }
            )
            
            // Price Display Options
            SettingsSwitchCard(
                title = "Show Purchase Price",
                description = "Show purchase price field in product form",
                checked = settings.showPurchasePrice,
                onCheckedChange = { 
                    viewModel.updateSettings(settings.copy(showPurchasePrice = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Show Retail Price",
                description = "Show retail price field in product form",
                checked = settings.showRetailPrice,
                onCheckedChange = { 
                    viewModel.updateSettings(settings.copy(showRetailPrice = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Show Wholesale Price",
                description = "Show wholesale price field in product form",
                checked = settings.showWholeSalePrice,
                onCheckedChange = { 
                    viewModel.updateSettings(settings.copy(showWholeSalePrice = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Show Average Purchase Price",
                description = "Show average purchase price on product details",
                checked = settings.showAvgPurchasePrice,
                onCheckedChange = { 
                    viewModel.updateSettings(settings.copy(showAvgPurchasePrice = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Auto Update Product Prices",
                description = "Automatically update sale/purchase prices from latest transactions",
                checked = settings.autoUpdateNewSalePurchasePrice,
                onCheckedChange = { 
                    viewModel.updateSettings(settings.copy(autoUpdateNewSalePurchasePrice = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Multiple Warehouses",
                description = "Enable multiple warehouse management",
                checked = settings.isMultipleWarehouseEnabled,
                onCheckedChange = { 
                    viewModel.updateSettings(settings.copy(isMultipleWarehouseEnabled = it))
                }
            )
            
            // Stock Management (conditional on products)
            if (settings.isProductsEnabled) {
                SettingsSwitchCard(
                    title = "Manage Stock",
                    description = "By enabling this feature you will be able to manage products stock",
                    checked = settings.isStockEnabled,
                    onCheckedChange = { 
                        viewModel.updateSettings(settings.copy(isStockEnabled = it))
                    }
                )
                
                SettingsSwitchCard(
                    title = "Multiple Vendors",
                    description = "Enable this if you have multiple vendors",
                    checked = settings.isMultipleVendorEnabled,
                    onCheckedChange = { 
                        viewModel.updateSettings(settings.copy(isMultipleVendorEnabled = it))
                    }
                )
            }
            
            // Tax Settings (conditional on cash in/out)
            if (settings.isCashInOutEnabled) {
                SettingsSwitchCard(
                    title = "Manage Tax",
                    description = "Enable tax management with transactions",
                    checked = settings.isHaveTax,
                    onCheckedChange = { 
                        viewModel.updateSettings(settings.copy(isHaveTax = it))
                    }
                )
                
                if (settings.isHaveTax) {
                    SettingsDropdownCard(
                        title = "Tax Formula",
                        description = "Select tax calculation method",
                        selectedIndex = settings.taxCalculationFormulaType,
                        options = listOf("Tax After Discount", "Tax Before Discount"),
                        onSelectionChange = { 
                            viewModel.updateSettings(settings.copy(taxCalculationFormulaType = it))
                        }
                    )
                }
                
                SettingsSwitchCard(
                    title = "Additional Charges",
                    description = "Enable additional charges field in transactions",
                    checked = settings.isHaveAdditionalCharges,
                    onCheckedChange = { 
                        viewModel.updateSettings(settings.copy(isHaveAdditionalCharges = it))
                    }
                )
                
                if (settings.isHaveAdditionalCharges) {
                    SettingsSwitchCard(
                        title = "Description with Additional Charges",
                        description = "Enable description field for additional charges",
                        checked = settings.isTakeDescriptionWithAdditionalCharges,
                        onCheckedChange = { 
                            viewModel.updateSettings(settings.copy(isTakeDescriptionWithAdditionalCharges = it))
                        }
                    )
                }
            }
            
            // Performance
            SettingsSwitchCard(
                title = "Speed Up Transaction Loading",
                description = "Optimize transaction loading for better performance",
                checked = settings.speedUpTransactionLoading,
                onCheckedChange = { 
                    viewModel.updateSettings(settings.copy(speedUpTransactionLoading = it))
                }
            )
            
            // Decimal Places (conditional)
            if (settings.isCashInOutEnabled) {
                SettingsDropdownCard(
                    title = "Decimal Places in Amount",
                    description = "Number of decimal places for amounts",
                    selectedIndex = settings.decimalPlacesInAmount,
                    options = listOf("0", "1", "2", "3", "4"),
                    onSelectionChange = { 
                        viewModel.updateSettings(settings.copy(decimalPlacesInAmount = it))
                    }
                )
            }
            
            if (settings.isProductsEnabled) {
                SettingsDropdownCard(
                    title = "Decimal Places in Quantity",
                    description = "Number of decimal places for quantities",
                    selectedIndex = settings.decimalPlacesInQuantity,
                    options = listOf("0", "1", "2", "3", "4"),
                    onSelectionChange = { 
                        viewModel.updateSettings(settings.copy(decimalPlacesInQuantity = it))
                    }
                )
            }
            
            // Number Formatting
            SettingsDropdownCard(
                title = "Number Formatting",
                description = "Select number display format",
                selectedIndex = settings.numberFormatterType,
                options = listOf("1,234,567", "12,34,567", "1234567"),
                onSelectionChange = { 
                    viewModel.updateSettings(settings.copy(numberFormatterType = it))
                }
            )
            
            // Bottom padding for FAB
            Spacer(Modifier.height(80.dp))
            }
        }
    }
}

