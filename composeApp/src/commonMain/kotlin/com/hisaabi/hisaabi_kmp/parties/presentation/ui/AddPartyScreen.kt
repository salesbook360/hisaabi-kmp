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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType
import com.hisaabi.hisaabi_kmp.parties.presentation.viewmodel.AddPartyViewModel
import com.hisaabi.hisaabi_kmp.utils.format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPartyScreen(
    viewModel: AddPartyViewModel,
    partyType: PartyType,
    partyToEdit: Party? = null,
    onNavigateBack: () -> Unit,
    onNavigateToCategories: () -> Unit = {},
    onNavigateToAreas: () -> Unit = {},
    selectedCategoryFromNav: CategoryEntity? = null,
    selectedAreaFromNav: CategoryEntity? = null
) {
    val formStateKey = partyToEdit?.id.takeIf { it != 0 } ?: "new_${partyType.name}"
    val latLongParts = partyToEdit?.latLong?.split(",")
    val initialLatitude = latLongParts?.getOrNull(0)?.toDoubleOrNull()
    val initialLongitude = latLongParts?.getOrNull(1)?.toDoubleOrNull()
    
    var name by rememberSaveable(formStateKey) { mutableStateOf(partyToEdit?.name ?: "") }
    var phone by rememberSaveable(formStateKey) { mutableStateOf(partyToEdit?.phone ?: "") }
    var address by rememberSaveable(formStateKey) { mutableStateOf(partyToEdit?.address ?: "") }
    var email by rememberSaveable(formStateKey) { mutableStateOf(partyToEdit?.email ?: "") }
    var description by rememberSaveable(formStateKey) { mutableStateOf(partyToEdit?.description ?: "") }
    var openingBalance by rememberSaveable(formStateKey) { mutableStateOf(partyToEdit?.openingBalance?.toString() ?: "") }
    var isBalancePayable by rememberSaveable(formStateKey) { mutableStateOf(partyToEdit?.openingBalance?.let { it >= 0 } ?: true) }
    
    // Category and Area
    var selectedCategory by rememberSaveable(
        formStateKey,
        stateSaver = CategorySelectionSaver
    ) { mutableStateOf<CategorySelection?>(null) }
    var selectedArea by rememberSaveable(
        formStateKey,
        stateSaver = CategorySelectionSaver
    ) { mutableStateOf<CategorySelection?>(null) }
    
    // Location
    var latitude by rememberSaveable(formStateKey) { mutableStateOf(initialLatitude) }
    var longitude by rememberSaveable(formStateKey) { mutableStateOf(initialLongitude) }
    var showLocationPicker by remember { mutableStateOf(false) }
    
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val areas by viewModel.areas.collectAsState()
    
    // Reset state and set party to edit when partyToEdit changes
    // This ensures clean state every time we navigate to this screen
    LaunchedEffect(partyToEdit) {
        if (partyToEdit != null) {
            viewModel.setPartyToEdit(partyToEdit)
        } else {
            viewModel.resetState()
        }
    }
    
    // Set initial category and area from partyToEdit when categories/areas are loaded
    LaunchedEffect(categories, partyToEdit) {
        if (partyToEdit != null && selectedCategory == null) {
            // Find and set category if it exists
            partyToEdit.categorySlug?.let { slug ->
                categories.find { it.slug == slug }?.let { category ->
                    selectedCategory = category.toSelection()
                }
            }
        }
    }
    
    LaunchedEffect(areas, partyToEdit) {
        if (partyToEdit != null && selectedArea == null) {
            // Find and set area if it exists
            partyToEdit.areaSlug?.let { slug ->
                areas.find { it.slug == slug }?.let { area ->
                    selectedArea = area.toSelection()
                }
            }
        }
    }
    
    // Navigate back on success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }
    
    // Update selected category from navigation
    LaunchedEffect(selectedCategoryFromNav) {
        selectedCategoryFromNav?.let { selectedCategory = it.toSelection() }
    }
    
    // Update selected area from navigation
    LaunchedEffect(selectedAreaFromNav) {
        selectedAreaFromNav?.let { selectedArea = it.toSelection() }
    }
    
    // Check if this is an expense/income type
    val isExpenseIncomeType = partyType == PartyType.EXPENSE || partyType == PartyType.EXTRA_INCOME
    val isEditing = partyToEdit != null
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        when (partyType) {
                            PartyType.CUSTOMER -> "${if (isEditing) "Edit" else "Add New"} Customer"
                            PartyType.VENDOR -> "${if (isEditing) "Edit" else "Add New"} Vendor"
                            PartyType.INVESTOR -> "${if (isEditing) "Edit" else "Add New"} Investor"
                            PartyType.EXPENSE -> "${if (isEditing) "Edit" else "Add"} Expense Type"
                            PartyType.EXTRA_INCOME -> "${if (isEditing) "Edit" else "Add"} Income Type"
                            else -> "${if (isEditing) "Edit" else "Add New"} Party"
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
        },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = {
                    if (!uiState.isLoading && name.isNotBlank()) {
                        if (isEditing && partyToEdit != null) {
                            viewModel.updateParty(
                                party = partyToEdit,
                                name = name,
                                phone = phone.takeIf { it.isNotBlank() },
                                address = address.takeIf { it.isNotBlank() },
                                email = email.takeIf { it.isNotBlank() },
                                description = description.takeIf { it.isNotBlank() },
                                openingBalance = openingBalance.toDoubleOrNull() ?: 0.0,
                                isBalancePayable = isBalancePayable,
                                categorySlug = selectedCategory?.slug,
                                areaSlug = selectedArea?.slug,
                                latitude = latitude,
                                longitude = longitude
                            )
                        } else {
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
                        }
                    }
                },
                modifier = Modifier
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.Check,
                        contentDescription = if (isEditing) "Update" else "Save"
                    )
                }
            }
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
                        IconButton(onClick = onNavigateToCategories) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Category")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToCategories() },
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
                        IconButton(onClick = onNavigateToAreas) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Area")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAreas() },
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
                    text = "Opening Balance${if (isEditing) " (Read-only)" else ""}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = openingBalance,
                    onValueChange = { if (!isEditing) openingBalance = it },
                    label = { Text("Amount") },
                    leadingIcon = {
                        Icon(Icons.Default.AccountBalance, contentDescription = "Balance")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isEditing,
                    colors = if (isEditing) {
                        OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        OutlinedTextFieldDefaults.colors()
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Balance Type Radio Buttons (Disabled when editing)
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
                            onClick = { if (!isEditing) isBalancePayable = true },
                            enabled = !isEditing
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Payable (You'll Pay)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isEditing) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !isBalancePayable,
                            onClick = { if (!isEditing) isBalancePayable = false },
                            enabled = !isEditing
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Receivable (You'll Get)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isEditing) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface
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
        }
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

private data class CategorySelection(
    val slug: String?,
    val title: String?
)

private val CategorySelectionSaver = Saver<CategorySelection?, List<String?>>(
    save = { selection ->
        selection?.let { listOf(it.slug, it.title) }
    },
    restore = { saved ->
        saved?.let {
            val slug = it.getOrNull(0)
            val title = it.getOrNull(1)

            if (slug == null && title == null) {
                null
            } else {
                CategorySelection(slug, title)
            }
        }
    }
)

private fun CategoryEntity.toSelection(): CategorySelection =
    CategorySelection(slug = slug, title = title)

