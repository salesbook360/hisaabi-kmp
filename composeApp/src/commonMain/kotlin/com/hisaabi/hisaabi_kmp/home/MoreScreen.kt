package com.hisaabi.hisaabi_kmp.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigateToAuth: () -> Unit = {},
    onNavigateToQuantityUnits: () -> Unit = {},
    onNavigateToTransactionSettings: () -> Unit = {},
    onNavigateToReceiptSettings: () -> Unit = {},
    onNavigateToDashboardSettings: () -> Unit = {},
    onNavigateToTemplates: () -> Unit = {},
    onNavigateToUpdateProfile: () -> Unit = {},
    preferencesManager: com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager = org.koin.compose.koinInject()
) {
    val biometricAuthEnabled by preferencesManager.biometricAuthEnabled.collectAsState(initial = false)
    val selectedLanguage by preferencesManager.selectedLanguage.collectAsState(initial = com.hisaabi.hisaabi_kmp.settings.data.Language.ENGLISH)
    val selectedCurrency by preferencesManager.selectedCurrency.collectAsState(initial = com.hisaabi.hisaabi_kmp.settings.domain.model.Currency.PKR)
    
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("More") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            // Profile Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "John Doe",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Active Business",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.clickable { /* Select business */ },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "My Business",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Change business",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Card(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "J",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
            
            // Contact/Social Buttons
            item {
                Spacer(modifier = Modifier.height(1.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SocialButton(
                            icon = Icons.Default.Chat,
                            label = "WhatsApp",
                            onClick = { /* Open WhatsApp */ }
                        )
                        SocialButton(
                            icon = Icons.Default.Email,
                            label = "Email",
                            onClick = { /* Send Email */ }
                        )
                        SocialButton(
                            icon = Icons.Default.Share,
                            label = "Share",
                            onClick = { /* Share app */ }
                        )
                    }
                }
            }
            
            // Settings Options
            item {
                Spacer(modifier = Modifier.height(1.dp))
                SettingsCard {
                    Column {
                        SettingsItem(
                            title = "Transaction Type Selection",
                            icon = Icons.Default.SwapHoriz,
                            onClick = onNavigateToTransactionSettings
                        )
                        SettingsDivider()
                        SettingsItem(
                            title = "Receipt Settings",
                            icon = Icons.Default.Receipt,
                            onClick = onNavigateToReceiptSettings
                        )
                        SettingsDivider()
                        SettingsItem(
                            title = "Dashboard Settings",
                            icon = Icons.Default.Dashboard,
                            onClick = onNavigateToDashboardSettings
                        )
                        SettingsDivider()
                        SettingsItem(
                            title = "Quantity Units",
                            icon = Icons.Default.Scale,
                            onClick = onNavigateToQuantityUnits
                        )
                        SettingsDivider()
                        SettingsItem(
                            title = "Templates Settings",
                            icon = Icons.Default.Article,
                            onClick = onNavigateToTemplates
                        )
                        SettingsDivider()
                        SettingsSwitchItem(
                            title = "Verify identity on app launch",
                            subtitle = "Use biometric authentication or PIN",
                            checked = biometricAuthEnabled,
                            onCheckedChange = { enabled ->
                                preferencesManager.setBiometricAuthEnabled(enabled)
                            }
                        )
                        SettingsDivider()
                        SettingsItem(
                            title = "Language",
                            subtitle = selectedLanguage.displayName,
                            icon = Icons.Default.Language,
                            onClick = { showLanguageDialog = true }
                        )
                        SettingsDivider()
                        SettingsItem(
                            title = "Currency Unit",
                            subtitle = "${selectedCurrency.flag} ${selectedCurrency.name} (${selectedCurrency.symbol})",
                            icon = Icons.Default.CurrencyExchange,
                            onClick = { showCurrencyDialog = true }
                        )
                        SettingsDivider()
                        SettingsItem(
                            title = "Credits Wallet",
                            icon = Icons.Default.AccountBalanceWallet,
                            onClick = { /* Navigate */ }
                        )
                    }
                }
            }
            
            // Support & Info
            item {
                Spacer(modifier = Modifier.height(1.dp))
                SettingsCard {
                    Column {
                        SettingsItem(
                            title = "Update Profile",
                            icon = Icons.Default.Person,
                            onClick = onNavigateToUpdateProfile
                        )
                        SettingsDivider()
                        SettingsItem(
                            title = "Check for Updates",
                            icon = Icons.Default.SystemUpdate,
                            onClick = { /* Check updates */ }
                        )
                        SettingsDivider()
                        SettingsItem(
                            title = "Privacy Policy",
                            icon = Icons.Default.Security,
                            onClick = { /* Navigate */ }
                        )
                        SettingsDivider()
                        SettingsItem(
                            title = "Rate App",
                            icon = Icons.Default.Star,
                            onClick = { /* Open store */ }
                        )
                        SettingsDivider()
                        SettingsItem(
                            title = "More Apps",
                            icon = Icons.Default.Apps,
                            onClick = { /* Open store */ }
                        )
                    }
                }
            }
            
            // Developer Options (Debug)
            item {
                Spacer(modifier = Modifier.height(1.dp))
                SettingsCard {
                    Column {
                        SettingsItem(
                            title = "Share Database",
                            icon = Icons.Default.FolderShared,
                            onClick = { /* Export DB */ }
                        )
                        SettingsDivider()
                        SettingsItem(
                            title = "Export Database Logs",
                            icon = Icons.Default.BugReport,
                            onClick = { /* Export logs */ }
                        )
                        SettingsDivider()
                        SettingsItem(
                            title = "Admin Portal",
                            icon = Icons.Default.AdminPanelSettings,
                            onClick = { /* Navigate */ }
                        )
                    }
                }
            }
            
            // Logout & Delete Account
            item {
                Spacer(modifier = Modifier.height(1.dp))
                SettingsCard {
                    Column {
                        SettingsItem(
                            title = "Logout",
                            icon = Icons.AutoMirrored.Filled.Logout,
                            iconTint = MaterialTheme.colorScheme.error,
                            textColor = MaterialTheme.colorScheme.error,
                            onClick = onNavigateToAuth
                        )
                        SettingsDivider()
                        SettingsItem(
                            title = "Delete Account",
                            icon = Icons.Default.DeleteForever,
                            iconTint = MaterialTheme.colorScheme.error,
                            textColor = MaterialTheme.colorScheme.error,
                            onClick = { /* Delete account */ }
                        )
                    }
                }
            }
            
            // Version Info
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Version 1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    // Language Selection Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { 
                Text(
                    "Select Language",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.hisaabi.hisaabi_kmp.settings.data.Language.values().forEach { language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    preferencesManager.setSelectedLanguage(language)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage == language,
                                onClick = {
                                    preferencesManager.setSelectedLanguage(language)
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = language.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (selectedLanguage == language) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = language.code.uppercase(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
    
    // Currency Selection Dialog
    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            selectedCurrency = selectedCurrency,
            onCurrencySelected = { currency ->
                preferencesManager.setSelectedCurrency(currency)
                showCurrencyDialog = false
            },
            onDismiss = { showCurrencyDialog = false }
        )
    }
}

@Composable
private fun CurrencySelectionDialog(
    selectedCurrency: com.hisaabi.hisaabi_kmp.settings.domain.model.Currency,
    onCurrencySelected: (com.hisaabi.hisaabi_kmp.settings.domain.model.Currency) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredCurrencies = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            com.hisaabi.hisaabi_kmp.settings.domain.model.Currency.ALL_CURRENCIES
        } else {
            com.hisaabi.hisaabi_kmp.settings.domain.model.Currency.ALL_CURRENCIES.filter { currency ->
                currency.name.contains(searchQuery, ignoreCase = true) ||
                currency.code.contains(searchQuery, ignoreCase = true) ||
                currency.symbol.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    "Select Currency",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search currency...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredCurrencies.size) { index ->
                    val currency = filteredCurrencies[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCurrencySelected(currency) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Flag emoji
                        Text(
                            text = currency.flag,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        
                        // Currency info
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currency.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selectedCurrency.code == currency.code) FontWeight.Bold else FontWeight.Normal
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = currency.code,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = currency.symbol,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Selected indicator
                        if (selectedCurrency.code == currency.code) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    if (index < filteredCurrencies.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
                
                if (filteredCurrencies.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No currencies found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// Reusable Components

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        content()
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = iconTint
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (showChevron) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Navigate",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
fun SocialButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(60.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(30.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

