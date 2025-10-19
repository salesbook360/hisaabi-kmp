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
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.settings.domain.model.*
import com.hisaabi.hisaabi_kmp.settings.presentation.viewmodel.ReceiptSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptSettingsScreen(
    viewModel: ReceiptSettingsViewModel,
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
            snackbarHostState.showSnackbar("Receipt settings saved successfully")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipt Settings") },
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
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Text(
                "Configure receipt/invoice settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // General Settings
            SettingsSectionHeader("General Settings")
            
            SettingsSwitchCard(
                title = "Generate Receipt",
                description = "Enable receipt generation for transactions",
                checked = config.isReceiptEnabled,
                onCheckedChange = { 
                    viewModel.updateConfig(config.copy(isReceiptEnabled = it))
                }
            )
            
            SettingsDropdownCard(
                title = "Receipt Type",
                description = "How to generate receipts",
                selectedIndex = config.generateReceiptType.ordinal,
                options = ReceiptGenerateOption.entries.map { it.displayName },
                onSelectionChange = { 
                    viewModel.updateConfig(config.copy(generateReceiptType = ReceiptGenerateOption.fromOrdinal(it)))
                }
            )
            
            SettingsDropdownCard(
                title = "Default Printer",
                description = "Thermal printer paper size",
                selectedIndex = config.thermalPrinterType.ordinal,
                options = ThermalPrinterType.entries.map { it.displayName },
                onSelectionChange = { 
                    viewModel.updateConfig(config.copy(thermalPrinterType = ThermalPrinterType.fromOrdinal(it)))
                }
            )
            
            // Invoice Details
            Spacer(Modifier.height(16.dp))
            SettingsSectionHeader("Invoice Details")
            
            SettingsSwitchCard(
                title = "Show Transaction Date",
                description = "Display transaction date on receipt",
                checked = config.showTransactionDate,
                onCheckedChange = { 
                    viewModel.updateConfig(config.copy(showTransactionDate = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Show Order No",
                description = "Display order/invoice number",
                checked = config.showOrderNo,
                onCheckedChange = { 
                    viewModel.updateConfig(config.copy(showOrderNo = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Show Transaction Type",
                description = "Display transaction type (Sale/Purchase)",
                checked = config.showTransactionType,
                onCheckedChange = { 
                    viewModel.updateConfig(config.copy(showTransactionType = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Show Payment Method",
                description = "Display payment method used",
                checked = config.showPaymentMethod,
                onCheckedChange = { 
                    viewModel.updateConfig(config.copy(showPaymentMethod = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Show Tax Amount",
                description = "Display tax amount on receipt",
                checked = config.showTax,
                onCheckedChange = { 
                    viewModel.updateConfig(config.copy(showTax = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Show Discount Amount",
                description = "Display discount amount on receipt",
                checked = config.showDiscount,
                onCheckedChange = { 
                    viewModel.updateConfig(config.copy(showDiscount = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Show Additional Charges",
                description = "Display additional charges",
                checked = config.showAdditionalCharges,
                onCheckedChange = { 
                    viewModel.updateConfig(config.copy(showAdditionalCharges = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Show Total Items",
                description = "Display total number of items",
                checked = config.showTotalItems,
                onCheckedChange = { 
                    viewModel.updateConfig(config.copy(showTotalItems = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Show Previous Balance",
                description = "Display customer's previous balance",
                checked = config.showPreviousBalance,
                onCheckedChange = { 
                    viewModel.updateConfig(config.copy(showPreviousBalance = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Show Current Balance",
                description = "Display customer's current balance",
                checked = config.showCurrentBalance,
                onCheckedChange = { 
                    viewModel.updateConfig(config.copy(showCurrentBalance = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Show Payable Amount",
                description = "Display total payable amount",
                checked = config.showPayableAmount,
                onCheckedChange = { 
                    viewModel.updateConfig(config.copy(showPayableAmount = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Show Invoice Terms",
                description = "Display invoice terms and conditions",
                checked = config.showInvoiceTerms,
                onCheckedChange = { 
                    viewModel.updateConfig(config.copy(showInvoiceTerms = it))
                }
            )
            
            SettingsTextFieldCard(
                title = "Invoice Terms",
                value = config.invoiceTerms,
                placeholder = "Due on Receipt",
                onValueChange = { 
                    viewModel.updateConfig(config.copy(invoiceTerms = it))
                }
            )
            
            // Customer Details
            Spacer(Modifier.height(16.dp))
            SettingsSectionHeader("Customer Details")
            
            SettingsSwitchCard(
                title = "Show Customer Name",
                description = "Display customer name on receipt",
                checked = config.showCustomerName,
                onCheckedChange = { 
                    viewModel.updateConfig(config.copy(showCustomerName = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Show Customer Phone",
                description = "Display customer phone number",
                checked = config.showCustomerPhone,
                onCheckedChange = { 
                    viewModel.updateConfig(config.copy(showCustomerPhone = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Show Customer Address",
                description = "Display customer address",
                checked = config.showCustomerAddress,
                onCheckedChange = { 
                    viewModel.updateConfig(config.copy(showCustomerAddress = it))
                }
            )
            
            // Business Details
            Spacer(Modifier.height(16.dp))
            SettingsSectionHeader("Business Details")
            
            SettingsSwitchCard(
                title = "Show Logo on Receipt",
                description = "Display business logo",
                checked = config.showLogoOnReceipt,
                onCheckedChange = { 
                    viewModel.updateConfig(config.copy(showLogoOnReceipt = it))
                }
            )
            
            // TODO: Add image picker for logo
            if (config.showLogoOnReceipt) {
                Card(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Image, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Business Logo", style = MaterialTheme.typography.titleSmall)
                            Text("Tap to upload logo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
            }
            
            SettingsSwitchCard(
                title = "Show Business Name",
                description = "Display business name on receipt",
                checked = config.showBusinessName,
                onCheckedChange = { 
                    viewModel.updateConfig(config.copy(showBusinessName = it))
                }
            )
            
            SettingsTextFieldCard(
                title = "Business Name",
                value = config.businessName,
                placeholder = "Enter business name",
                onValueChange = { 
                    viewModel.updateConfig(config.copy(businessName = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Show Business Email",
                description = "Display business email",
                checked = config.showBusinessEmail,
                onCheckedChange = { 
                    viewModel.updateConfig(config.copy(showBusinessEmail = it))
                }
            )
            
            SettingsTextFieldCard(
                title = "Business Email",
                value = config.businessEmail,
                placeholder = "Enter business email",
                onValueChange = { 
                    viewModel.updateConfig(config.copy(businessEmail = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Show Phone No",
                description = "Display business phone number",
                checked = config.showBusinessPhone,
                onCheckedChange = { 
                    viewModel.updateConfig(config.copy(showBusinessPhone = it))
                }
            )
            
            SettingsTextFieldCard(
                title = "Phone No",
                value = config.businessPhone,
                placeholder = "Enter phone number",
                onValueChange = { 
                    viewModel.updateConfig(config.copy(businessPhone = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Show Address",
                description = "Display business address",
                checked = config.showBusinessAddress,
                onCheckedChange = { 
                    viewModel.updateConfig(config.copy(showBusinessAddress = it))
                }
            )
            
            SettingsTextFieldCard(
                title = "Address",
                value = config.businessAddress,
                placeholder = "Enter business address",
                onValueChange = { 
                    viewModel.updateConfig(config.copy(businessAddress = it))
                }
            )
            
            SettingsSwitchCard(
                title = "Show Regards Message",
                description = "Display thank you message",
                checked = config.showRegardsMessage,
                onCheckedChange = { 
                    viewModel.updateConfig(config.copy(showRegardsMessage = it))
                }
            )
            
            SettingsTextFieldCard(
                title = "Regards Message",
                value = config.regardsMessage ?: "",
                placeholder = "Thank you for your business!",
                onValueChange = { 
                    viewModel.updateConfig(config.copy(regardsMessage = it.ifEmpty { null }))
                }
            )
            
            // Bottom padding for FAB
            Spacer(Modifier.height(80.dp))
        }
    }
}

