package com.hisaabi.hisaabi_kmp.quantityunits.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
import com.hisaabi.hisaabi_kmp.quantityunits.presentation.viewmodel.QuantityUnitsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantityUnitsScreen(
    viewModel: QuantityUnitsViewModel,
    onUnitClick: (QuantityUnit) -> Unit = {},
    onAddUnitClick: () -> Unit = {},
    onAddUnitTypeClick: (currentSelectedParentSlug: String?) -> Unit = {},
    onAddChildUnitClick: (QuantityUnit) -> Unit = {}, // Pass selected parent unit
    onNavigateBack: () -> Unit = {},
    refreshTrigger: Int = 0,
    initialSelectedParentSlug: String? = null
) {
    val state by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<QuantityUnit?>(null) }
    var unitTypeDropdownExpanded by remember { mutableStateOf(false) }
    
    // Restore selected parent unit when navigating back
    LaunchedEffect(initialSelectedParentSlug) {
        if (initialSelectedParentSlug != null) {
            viewModel.setInitialSelectedParentSlug(initialSelectedParentSlug)
        }
    }
    
    LaunchedEffect(refreshTrigger) {
        viewModel.loadUnits()
    }
    
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quantity Units") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            // Only show FAB to add child unit if a parent unit type is selected
            if (state.selectedParentUnit != null) {
                FloatingActionButton(
                    onClick = { state.selectedParentUnit?.let { onAddChildUnitClick(it) } },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, "Add Unit")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Unit Type Selector (Dropdown)
            UnitTypeSelector(
                parentUnitTypes = state.parentUnitTypes,
                selectedParentUnit = state.selectedParentUnit,
                expanded = unitTypeDropdownExpanded,
                onExpandedChange = { unitTypeDropdownExpanded = it },
                onParentUnitSelected = { 
                    viewModel.selectParentUnit(it)
                    unitTypeDropdownExpanded = false
                },
                onAddNewUnitType = {
                    unitTypeDropdownExpanded = false
                    onAddUnitTypeClick(state.selectedParentUnit?.slug)
                },
                isLoading = state.isLoading
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Content based on state
            when {
                state.isLoading && state.parentUnitTypes.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                
                state.parentUnitTypes.isEmpty() -> {
                    // No unit types yet - prompt to create one
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Category, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(16.dp))
                            Text("No unit types found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Create unit types like Weight, Quantity, Liquid, etc.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { onAddUnitTypeClick(state.selectedParentUnit?.slug) }) {
                                Icon(Icons.Default.Add, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Add Unit Type")
                            }
                        }
                    }
                }
                
                state.selectedParentUnit == null -> {
                    // Parent types exist but none selected - prompt to select
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Please select a unit type from above",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                state.isLoadingChildUnits -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                
                state.childUnits.isEmpty() -> {
                    // Unit type selected but no child units
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Scale, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No units in ${state.selectedParentUnit?.title ?: "this category"}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Add units like KG, MG, Ton for Weight",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { state.selectedParentUnit?.let { onAddChildUnitClick(it) } }) {
                                Icon(Icons.Default.Add, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Add Unit")
                            }
                        }
                    }
                }
                
                else -> {
                    // Show child units header
                    Text(
                        "${state.selectedParentUnit?.title ?: ""} Units",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    // Get base unit for conversion display
                    val baseUnit = viewModel.getBaseUnitForSelectedParent()
                    
                    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                        items(state.childUnits, key = { it.id }) { unit ->
                            UnitItem(
                                unit = unit,
                                baseUnit = baseUnit,
                                onClick = { onUnitClick(unit) },
                                onEditClick = { onUnitClick(unit) },
                                onDeleteClick = { showDeleteDialog = unit }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
    
    showDeleteDialog?.let { unit ->
        val deleteMessage = if (unit.isParentUnitType) {
            "Are you sure you want to delete '${unit.title}' unit type? This will also affect all units under this type."
        } else {
            "Are you sure you want to delete '${unit.title}'?"
        }
        
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(if (unit.isParentUnitType) "Delete Unit Type" else "Delete Unit") },
            text = { Text(deleteMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteUnit(unit); showDeleteDialog = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitTypeSelector(
    parentUnitTypes: List<QuantityUnit>,
    selectedParentUnit: QuantityUnit?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onParentUnitSelected: (QuantityUnit) -> Unit,
    onAddNewUnitType: () -> Unit,
    isLoading: Boolean
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            "Unit Type",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (!isLoading) onExpandedChange(it) }
        ) {
            OutlinedTextField(
                value = selectedParentUnit?.title ?: "Select Unit Type",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                },
                leadingIcon = {
                    Icon(Icons.Default.Category, "Unit Type")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                // Existing parent unit types
                parentUnitTypes.forEach { unitType ->
                    DropdownMenuItem(
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Category, 
                                    null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(unitType.title)
                            }
                        },
                        onClick = { onParentUnitSelected(unitType) }
                    )
                }
                
                // Divider before "Add New"
                if (parentUnitTypes.isNotEmpty()) {
                    HorizontalDivider()
                }
                
                // Add new unit type option
                DropdownMenuItem(
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Add, 
                                null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Add New Unit Type",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    onClick = onAddNewUnitType
                )
            }
        }
    }
}

@Composable
private fun UnitItem(
    unit: QuantityUnit,
    baseUnit: QuantityUnit?,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Scale,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    unit.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                
                // Show conversion factor with base unit
                val conversionText = if (baseUnit != null && unit.slug != baseUnit.slug && unit.conversionFactor != 1.0) {
                    "1 ${unit.title} = ${unit.conversionFactor} ${baseUnit.title}"
                } else if (baseUnit?.slug == unit.slug) {
                    "Base Unit"
                } else {
                    "Conversion Factor: ${unit.conversionFactor}"
                }
                
                Text(
                    conversionText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (!unit.isActive) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Inactive",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(Modifier.width(8.dp))
            
            // Edit button
            IconButton(onClick = onEditClick) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // Delete button
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

