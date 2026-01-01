package com.hisaabi.hisaabi_kmp.quantityunits.presentation.ui

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.core.ui.LocalWindowSizeClass
import com.hisaabi.hisaabi_kmp.core.ui.WindowWidthSizeClass
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
import com.hisaabi.hisaabi_kmp.quantityunits.presentation.viewmodel.AddQuantityUnitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuantityUnitScreen(
    viewModel: AddQuantityUnitViewModel,
    unitToEdit: QuantityUnit? = null,
    isAddingParentUnitType: Boolean = false,
    parentUnit: QuantityUnit? = null,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var baseUnitDropdownExpanded by remember { mutableStateOf(false) }
    
    LaunchedEffect(unitToEdit, isAddingParentUnitType, parentUnit) {
        if (unitToEdit != null) {
            viewModel.setUnitToEdit(unitToEdit)
        } else {
            viewModel.resetState()
            viewModel.setAddingParentUnitType(isAddingParentUnitType)
            if (parentUnit != null) {
                viewModel.setParentUnit(parentUnit)
            }
        }
    }
    
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onNavigateBack()
    }
    
    val title = when {
        state.isEditMode && state.isAddingParentUnitType -> "Edit Unit Type"
        state.isEditMode -> "Edit Unit"
        state.isAddingParentUnitType -> "Add Unit Type"
        else -> "Add Unit"
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = {
                    if (!state.isLoading) {
                        viewModel.saveUnit()
                    }
                },
                modifier = Modifier
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Icon(
                        imageVector = if (state.isEditMode) Icons.Default.Edit else Icons.Default.Check,
                        contentDescription = if (state.isEditMode) "Update" else "Save"
                    )
                }
            }
        }
    ) { paddingValues ->
        val windowSizeClass = LocalWindowSizeClass.current
        val isDesktop = windowSizeClass.widthSizeClass == WindowWidthSizeClass.EXPANDED
        val maxContentWidth = if (isDesktop) 700.dp else Dp.Unspecified
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
                    .padding(horizontal = horizontalPadding, vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Show info card based on what we're adding
            if (!state.isEditMode) {
                InfoCard(
                    isAddingParentUnitType = state.isAddingParentUnitType,
                    parentUnitTitle = state.selectedParentUnit?.title
                )
                Spacer(Modifier.height(16.dp))
            }
            
            // Title/Name field
            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.onTitleChanged(it) },
                label = { 
                    Text(if (state.isAddingParentUnitType) "Unit Type Name *" else "Unit Name *") 
                },
                placeholder = {
                    Text(
                        if (state.isAddingParentUnitType) 
                            "e.g., Weight, Quantity, Liquid, Length" 
                        else 
                            "e.g., KG, Gram, Piece, Liter"
                    )
                },
                leadingIcon = { 
                    Icon(
                        if (state.isAddingParentUnitType) Icons.Default.Category else Icons.Default.Scale, 
                        "Name"
                    ) 
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.titleError != null,
                supportingText = state.titleError?.let { { Text(it) } }
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Only show conversion factor and base unit for child units
            if (!state.isAddingParentUnitType) {
                // Parent Unit Type display (read-only)
                if (state.selectedParentUnit != null) {
                    OutlinedTextField(
                        value = state.selectedParentUnit?.title ?: "",
                        onValueChange = {},
                        label = { Text("Unit Type") },
                        leadingIcon = { Icon(Icons.Default.Category, "Unit Type") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false
                    )
                    
                    Spacer(Modifier.height(16.dp))
                }
                
                // Base Unit Selector (for conversion reference)
                if (state.availableBaseUnits.isNotEmpty()) {
                    Text(
                        "Base Unit for Conversion",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = baseUnitDropdownExpanded,
                        onExpandedChange = { baseUnitDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = state.selectedBaseUnit?.title ?: "Select Base Unit",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = baseUnitDropdownExpanded)
                            },
                            leadingIcon = { Icon(Icons.Default.Straighten, "Base Unit") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = baseUnitDropdownExpanded,
                            onDismissRequest = { baseUnitDropdownExpanded = false }
                        ) {
                            state.availableBaseUnits.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit.title) },
                                    onClick = {
                                        viewModel.setBaseUnit(unit)
                                        baseUnitDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                }
                
                // Conversion Factor
                OutlinedTextField(
                    value = state.conversionFactor,
                    onValueChange = { viewModel.onConversionFactorChanged(it) },
                    label = { Text("Conversion Factor *") },
                    leadingIcon = { Icon(Icons.Default.Calculate, "Factor") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = state.conversionFactorError != null,
                    supportingText = {
                        val baseUnitName = state.selectedBaseUnit?.title ?: "base unit"
                        val unitName = state.title.ifBlank { "this unit" }
                        Column {
                            state.conversionFactorError?.let { Text(it) }
                            if (state.conversionFactorError == null && state.title.isNotBlank()) {
                                Text(
                                    "1 $unitName = ${state.conversionFactor} $baseUnitName",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                )
                
                Spacer(Modifier.height(16.dp))
            }
            
            // Sort Order
            OutlinedTextField(
                value = state.sortOrder,
                onValueChange = { viewModel.onSortOrderChanged(it) },
                label = { Text("Sort Order") },
                leadingIcon = { Icon(Icons.Default.Sort, "Sort Order") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = { Text("Lower numbers appear first") }
            )
            
            Spacer(Modifier.height(24.dp))
            
            // Error display
            state.error?.let { error ->
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(error, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            }
        }
    }
}

@Composable
private fun InfoCard(
    isAddingParentUnitType: Boolean,
    parentUnitTitle: String?
) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column {
                if (isAddingParentUnitType) {
                    Text(
                        "Creating a Unit Type",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Unit types categorize units. Examples: Weight (for KG, Gram), Quantity (for Piece, Dozen), Liquid (for Liter, ML).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "Creating a Unit under ${parentUnitTitle ?: ""}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Specify the conversion factor relative to the base unit. For example, if base unit is Gram, then 1 KG = 1000 Gram.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

