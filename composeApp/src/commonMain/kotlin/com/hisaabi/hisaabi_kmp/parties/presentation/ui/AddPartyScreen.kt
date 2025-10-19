package com.hisaabi.hisaabi_kmp.parties.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity
import com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType
import com.hisaabi.hisaabi_kmp.parties.presentation.viewmodel.AddPartyViewModel
import com.hisaabi.hisaabi_kmp.utils.format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPartyScreen(
    viewModel: AddPartyViewModel,
    partyType: PartyType,
    onNavigateBack: () -> Unit,
    onNavigateToCategories: () -> Unit = {},
    onNavigateToAreas: () -> Unit = {},
    selectedCategoryFromNav: CategoryEntity? = null,
    selectedAreaFromNav: CategoryEntity? = null
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var openingBalance by remember { mutableStateOf("") }
    var isBalancePayable by remember { mutableStateOf(true) }
    
    // Category and Area
    var selectedCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var selectedArea by remember { mutableStateOf<CategoryEntity?>(null) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showAreaPicker by remember { mutableStateOf(false) }
    
    // Location
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var showLocationPicker by remember { mutableStateOf(false) }
    
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val areas by viewModel.areas.collectAsState()
    
    // Reset state when screen is first opened
    LaunchedEffect(Unit) {
        viewModel.resetState()
    }
    
    // Navigate back on success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }
    
    // Update selected category from navigation
    LaunchedEffect(selectedCategoryFromNav) {
        selectedCategoryFromNav?.let { selectedCategory = it }
    }
    
    // Update selected area from navigation
    LaunchedEffect(selectedAreaFromNav) {
        selectedAreaFromNav?.let { selectedArea = it }
    }
    
    // Check if this is an expense/income type
    val isExpenseIncomeType = partyType == PartyType.EXPENSE || partyType == PartyType.EXTRA_INCOME
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        when (partyType) {
                            PartyType.CUSTOMER -> "Add New Customer"
                            PartyType.VENDOR -> "Add New Vendor"
                            PartyType.INVESTOR -> "Add New Investor"
                            PartyType.EXPENSE -> "Add Expense Type"
                            PartyType.EXTRA_INCOME -> "Add Income Type"
                            else -> "Add New Party"
                        }
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Name Field (Required)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { 
                    Text(
                        if (isExpenseIncomeType) {
                            if (partyType == PartyType.EXPENSE) "Expense Type Name *" else "Income Type Name *"
                        } else {
                            "Name *"
                        }
                    ) 
                },
                leadingIcon = {
                    Icon(
                        if (isExpenseIncomeType) {
                            if (partyType == PartyType.EXPENSE) Icons.Default.TrendingDown else Icons.Default.TrendingUp
                        } else {
                            Icons.Default.Person
                        },
                        contentDescription = "Name"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = name.isBlank() && uiState.error != null
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description Field (always shown)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description${if (isExpenseIncomeType) " (Optional)" else ""}") },
                leadingIcon = {
                    Icon(Icons.Default.Description, contentDescription = "Description")
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            // Show additional fields only for regular parties (not expense/income)
            if (!isExpenseIncomeType) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Phone Field
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = "Phone")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Address Field
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = "Address")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = "Email")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Category Field
                OutlinedTextField(
                    value = selectedCategory?.title ?: "",
                    onValueChange = { },
                    label = { Text("Category") },
                    placeholder = { Text("Tap to select or add") },
                    leadingIcon = {
                        Icon(Icons.Default.Category, contentDescription = "Category")
                    },
                    trailingIcon = {
                        if (categories.isNotEmpty()) {
                            IconButton(onClick = { showCategoryPicker = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Category")
                            }
                        } else {
                            IconButton(onClick = onNavigateToCategories) {
                                Icon(Icons.Default.Add, contentDescription = "Add Category")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (categories.isNotEmpty()) {
                                showCategoryPicker = true
                            } else {
                                onNavigateToCategories()
                            }
                        },
                    readOnly = true,
                    enabled = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Area Field
                OutlinedTextField(
                    value = selectedArea?.title ?: "",
                    onValueChange = { },
                    label = { Text("Area") },
                    placeholder = { Text("Tap to select or add") },
                    leadingIcon = {
                        Icon(Icons.Default.Place, contentDescription = "Area")
                    },
                    trailingIcon = {
                        if (areas.isNotEmpty()) {
                            IconButton(onClick = { showAreaPicker = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Area")
                            }
                        } else {
                            IconButton(onClick = onNavigateToAreas) {
                                Icon(Icons.Default.Add, contentDescription = "Add Area")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (areas.isNotEmpty()) {
                                showAreaPicker = true
                            } else {
                                onNavigateToAreas()
                            }
                        },
                    readOnly = true,
                    enabled = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Map Coordinates Field
                OutlinedTextField(
                    value = if (latitude != null && longitude != null) 
                        "Lat: %.6f, Long: %.6f".format(latitude, longitude)
                    else "",
                    onValueChange = { },
                    label = { Text("Location") },
                    leadingIcon = {
                        Icon(Icons.Default.MyLocation, contentDescription = "Location")
                    },
                    trailingIcon = {
                        Row {
                            if (latitude != null && longitude != null) {
                                IconButton(onClick = { 
                                    latitude = null
                                    longitude = null
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear Location")
                                }
                            }
                            IconButton(onClick = { showLocationPicker = true }) {
                                Icon(Icons.Default.Map, contentDescription = "Pick Location")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true
                )
            }
            
            // Opening Balance Section (Hidden for Investors and Expense/Income types)
            if (partyType != PartyType.INVESTOR && !isExpenseIncomeType) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Opening Balance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = openingBalance,
                    onValueChange = { openingBalance = it },
                    label = { Text("Amount") },
                    leadingIcon = {
                        Icon(Icons.Default.AccountBalance, contentDescription = "Balance")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Balance Type Radio Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isBalancePayable,
                            onClick = { isBalancePayable = true }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Payable (You'll Pay)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !isBalancePayable,
                            onClick = { isBalancePayable = false }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Receivable (You'll Get)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Error Message
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Save Button
            Button(
                onClick = {
                    viewModel.addParty(
                        name = name,
                        phone = phone.takeIf { it.isNotBlank() },
                        address = address.takeIf { it.isNotBlank() },
                        email = email.takeIf { it.isNotBlank() },
                        description = description.takeIf { it.isNotBlank() },
                        openingBalance = openingBalance.toDoubleOrNull() ?: 0.0,
                        isBalancePayable = isBalancePayable,
                        partyType = partyType,
                        categorySlug = selectedCategory?.slug,
                        areaSlug = selectedArea?.slug,
                        latitude = latitude,
                        longitude = longitude
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && name.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = when (partyType) {
                        PartyType.CUSTOMER -> "Add Customer"
                        PartyType.VENDOR -> "Add Vendor"
                        PartyType.INVESTOR -> "Add Investor"
                        else -> "Add Party"
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Category Picker Dialog
    if (showCategoryPicker) {
        CategoryPickerDialog(
            categories = categories,
            onCategorySelected = { category ->
                selectedCategory = category
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false }
        )
    }
    
    // Area Picker Dialog
    if (showAreaPicker) {
        CategoryPickerDialog(
            categories = areas,
            title = "Select Area",
            onCategorySelected = { area ->
                selectedArea = area
                showAreaPicker = false
            },
            onDismiss = { showAreaPicker = false }
        )
    }
    
    // Location Picker Dialog
    if (showLocationPicker) {
        LocationPickerDialog(
            currentLatitude = latitude,
            currentLongitude = longitude,
            onLocationSelected = { lat, long ->
                latitude = lat
                longitude = long
                showLocationPicker = false
            },
            onDismiss = { showLocationPicker = false }
        )
    }
}

@Composable
private fun CategoryPickerDialog(
    categories: List<com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity>,
    title: String = "Select Category",
    onCategorySelected: (com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            if (categories.isEmpty()) {
                Text("No categories available. Please create categories first.")
            } else {
                Column {
                    categories.forEach { category ->
                        TextButton(
                            onClick = { onCategorySelected(category) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = category.title ?: "Unknown",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun LocationPickerDialog(
    currentLatitude: Double?,
    currentLongitude: Double?,
    onLocationSelected: (Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var latInput by remember { mutableStateOf(currentLatitude?.toString() ?: "") }
    var longInput by remember { mutableStateOf(currentLongitude?.toString() ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Location") },
        text = {
            Column {
                Text(
                    text = "Enter coordinates manually or use a map picker",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = latInput,
                    onValueChange = { latInput = it },
                    label = { Text("Latitude") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = longInput,
                    onValueChange = { longInput = it },
                    label = { Text("Longitude") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val lat = latInput.toDoubleOrNull()
                    val long = longInput.toDoubleOrNull()
                    if (lat != null && long != null) {
                        onLocationSelected(lat, long)
                    }
                },
                enabled = latInput.toDoubleOrNull() != null && longInput.toDoubleOrNull() != null
            ) {
                Text("Set")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

