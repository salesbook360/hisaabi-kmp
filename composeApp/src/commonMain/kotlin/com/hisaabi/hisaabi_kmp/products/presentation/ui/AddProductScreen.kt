package com.hisaabi.hisaabi_kmp.products.presentation.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import com.hisaabi.hisaabi_kmp.categories.domain.model.Category
import com.hisaabi.hisaabi_kmp.products.domain.model.Product
import com.hisaabi.hisaabi_kmp.products.domain.model.ProductType
import com.hisaabi.hisaabi_kmp.products.presentation.viewmodel.AddProductViewModel
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: AddProductViewModel,
    productType: ProductType,
    productToEdit: Product? = null,
    formSessionKey: Int,
    onNavigateBack: () -> Unit,
    onNavigateToIngredients: ((String) -> Unit)? = null,  // Recipe slug
    onNavigateToWarehouses: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(formSessionKey, productToEdit?.slug) {
        viewModel.initializeForm(formSessionKey, productToEdit)
    }
    
    // Navigate back on success (or to ingredients for recipes)
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            // For recipes, optionally navigate to ingredients screen
            // For now, just go back
            onNavigateBack()
        }
    }
    
    val isEditMode = productToEdit != null
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isEditMode) {
                            when (productType) {
                                ProductType.SIMPLE_PRODUCT -> "Edit Simple Product"
                                ProductType.SERVICE -> "Edit Service"
                                ProductType.RECIPE -> "Edit Recipe"
                            }
                        } else {
                            when (productType) {
                                ProductType.SIMPLE_PRODUCT -> "Add Simple Product"
                                ProductType.SERVICE -> "Add Service"
                                ProductType.RECIPE -> "Add Recipe"
                            }
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
            // Check if unit is required but not selected (for products and recipes only)
            val unitRequired = productType != ProductType.SERVICE
            val unitMissing = unitRequired && uiState.selectedBaseUnit == null
            val canSave = !uiState.isLoading && uiState.title.isNotBlank() && !unitMissing
            
            FloatingActionButton(
                containerColor = if (canSave) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                onClick = {
                    if (canSave) {
                        viewModel.saveProduct(
                            productToEdit = productToEdit,
                            title = uiState.title,
                            description = uiState.description.takeIf { it.isNotBlank() },
                            productType = productType,
                            retailPrice = uiState.retailPrice.toDoubleOrNull() ?: 0.0,
                            wholesalePrice = uiState.wholesalePrice.toDoubleOrNull() ?: 0.0,
                            purchasePrice = uiState.purchasePrice.toDoubleOrNull() ?: 0.0,
                            taxPercentage = uiState.taxPercentage.toDoubleOrNull() ?: 0.0,
                            discountPercentage = uiState.discountPercentage.toDoubleOrNull() ?: 0.0,
                            categorySlug = uiState.selectedCategory?.slug,
                            manufacturer = uiState.manufacturer.takeIf { it.isNotBlank() },
                            warehouseSlug = uiState.selectedWarehouse?.slug,
                            openingQuantity = uiState.openingQuantity.toDoubleOrNull() ?: 0.0,
                            minimumQuantity = uiState.minimumQuantity.toDoubleOrNull() ?: 0.0,
                            defaultUnitSlug = uiState.selectedBaseUnit?.slug,
                            openingQuantityUnitSlug = uiState.selectedOpeningQuantityUnit?.slug,
                            minimumQuantityUnitSlug = uiState.selectedMinimumQuantityUnit?.slug,
                            openingQuantityConversionFactor = uiState.selectedOpeningQuantityUnit?.conversionFactor ?: 1.0,
                            minimumQuantityConversionFactor = uiState.selectedMinimumQuantityUnit?.conversionFactor ?: 1.0
                        )
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
                        imageVector = if (isEditMode) Icons.Default.Edit else Icons.Default.Check,
                        contentDescription = if (isEditMode) "Update" else "Save"
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
            // Product Type Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (productType) {
                        ProductType.SIMPLE_PRODUCT -> MaterialTheme.colorScheme.primaryContainer
                        ProductType.SERVICE -> MaterialTheme.colorScheme.secondaryContainer
                        ProductType.RECIPE -> MaterialTheme.colorScheme.tertiaryContainer
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = productType.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (productType) {
                            ProductType.SIMPLE_PRODUCT -> "Can be purchased and sold independently"
                            ProductType.SERVICE -> "Can only be sold, not purchased"
                            ProductType.RECIPE -> "Manufactured product with ingredients"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Title Field (Required)
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Product Title *") },
                leadingIcon = {
                    Icon(Icons.Default.Title, contentDescription = "Title")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.title.isBlank() && uiState.error != null
            )
            
            // Base Unit Selection (only for Products and Recipes)
            if (productType != ProductType.SERVICE) {
                Spacer(modifier = Modifier.height(16.dp))
                
                BaseUnitSelectionCard(
                    selectedUnit = uiState.selectedBaseUnit,
                    isRequired = true,
                    onSelectUnit = { viewModel.showBaseUnitSheet() }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description Field
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Description") },
                leadingIcon = {
                    Icon(Icons.Default.Description, contentDescription = "Description")
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Pricing Section
            Text(
                text = "Pricing",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Retail Price
            OutlinedTextField(
                value = uiState.retailPrice,
                onValueChange = { viewModel.updateRetailPrice(it) },
                label = { Text("Retail Price") },
                leadingIcon = {
                    Icon(Icons.Default.AttachMoney, contentDescription = "Retail Price")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Wholesale Price
            OutlinedTextField(
                value = uiState.wholesalePrice,
                onValueChange = { viewModel.updateWholesalePrice(it) },
                label = { Text("Wholesale Price") },
                leadingIcon = {
                    Icon(Icons.Default.Money, contentDescription = "Wholesale Price")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Purchase Price (Hidden for Services)
            if (productType != ProductType.SERVICE) {
                OutlinedTextField(
                    value = uiState.purchasePrice,
                    onValueChange = { viewModel.updatePurchasePrice(it) },
                    label = { Text("Purchase Price") },
                    leadingIcon = {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Purchase Price")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Tax Percentage
            OutlinedTextField(
                value = uiState.taxPercentage,
                onValueChange = { viewModel.updateTaxPercentage(it) },
                label = { Text("Tax %") },
                leadingIcon = {
                    Icon(Icons.Default.Percent, contentDescription = "Tax")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Discount Percentage
            OutlinedTextField(
                value = uiState.discountPercentage,
                onValueChange = { viewModel.updateDiscountPercentage(it) },
                label = { Text("Discount %") },
                leadingIcon = {
                    Icon(Icons.Default.Discount, contentDescription = "Discount")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Additional Information
            Text(
                text = "Additional Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Manufacturer (for Simple Products and Recipes)
            if (productType != ProductType.SERVICE) {
                OutlinedTextField(
                    value = uiState.manufacturer,
                    onValueChange = { viewModel.updateManufacturer(it) },
                    label = { Text("Manufacturer") },
                    leadingIcon = {
                        Icon(Icons.Default.Factory, contentDescription = "Manufacturer")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Category Selection
            CategorySelectionCard(
                selectedCategory = uiState.selectedCategory,
                onSelectCategory = onNavigateToCategories
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Quantity Information Section (Hidden for Services)
            if (productType != ProductType.SERVICE) {
                Text(
                    text = "Quantity Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Warehouse Selection
                WarehouseSelectionCard(
                    selectedWarehouse = uiState.selectedWarehouse,
                    onSelectWarehouse = onNavigateToWarehouses
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Opening Quantity (only shown when warehouse is selected)
                if (uiState.selectedWarehouse != null) {
                    // Opening Quantity with Unit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.openingQuantity,
                            onValueChange = { viewModel.setOpeningQuantity(it) },
                            label = { Text("Opening Quantity") },
                            leadingIcon = {
                                Icon(Icons.Default.Inventory, contentDescription = "Opening Quantity")
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        
                        // Opening Quantity Unit Selection
                        QuantityUnitChip(
                            selectedUnit = uiState.selectedOpeningQuantityUnit,
                            onClick = { 
                                if (uiState.selectedBaseUnit != null) {
                                    viewModel.showOpeningQuantityUnitSheet()
                                }
                            },
                            enabled = uiState.selectedBaseUnit != null
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Minimum Quantity with Unit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.minimumQuantity,
                            onValueChange = { viewModel.setMinimumQuantity(it) },
                            label = { Text("Minimum Quantity") },
                            leadingIcon = {
                                Icon(Icons.Default.Warning, contentDescription = "Minimum Quantity")
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        
                        // Minimum Quantity Unit Selection
                        QuantityUnitChip(
                            selectedUnit = uiState.selectedMinimumQuantityUnit,
                            onClick = { 
                                if (uiState.selectedBaseUnit != null) {
                                    viewModel.showMinimumQuantityUnitSheet()
                                }
                            },
                            enabled = uiState.selectedBaseUnit != null
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // Recipe Note and Ingredients Button
            if (productType == ProductType.RECIPE) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Create the recipe first, then you'll be able to add ingredients.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        
                        // Show button if recipe was successfully created
                        if (uiState.isSuccess && onNavigateToIngredients != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { /* Navigate to ingredients */ },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Icon(Icons.Default.Restaurant, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Ingredients Now")
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
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
    
    // Base Unit Selection Bottom Sheet
    if (uiState.showBaseUnitSheet) {
        BaseUnitSelectionBottomSheet(
            parentUnitTypes = uiState.parentUnitTypes,
            childUnitsForParent = uiState.childUnitsForBaseParent,
            selectedUnit = uiState.selectedBaseUnit,
            onUnitSelected = { unit ->
                viewModel.setSelectedBaseUnit(unit)
                viewModel.hideBaseUnitSheet()
            },
            onParentTypeSelected = { parentTypeSlug ->
                // When parent type is selected, load its child units using the parent's slug
                viewModel.loadChildUnitsForParentType(parentTypeSlug)
            },
            onDismiss = { viewModel.hideBaseUnitSheet() }
        )
    }
    
    // Opening Quantity Unit Selection Bottom Sheet
    if (uiState.showOpeningQuantityUnitSheet) {
        ChildUnitSelectionBottomSheet(
            title = "Select Opening Quantity Unit",
            childUnits = uiState.childUnitsForBaseParent,
            selectedUnit = uiState.selectedOpeningQuantityUnit,
            onUnitSelected = { unit ->
                viewModel.setSelectedOpeningQuantityUnit(unit)
                viewModel.hideOpeningQuantityUnitSheet()
            },
            onDismiss = { viewModel.hideOpeningQuantityUnitSheet() }
        )
    }
    
    // Minimum Quantity Unit Selection Bottom Sheet
    if (uiState.showMinimumQuantityUnitSheet) {
        ChildUnitSelectionBottomSheet(
            title = "Select Minimum Quantity Unit",
            childUnits = uiState.childUnitsForBaseParent,
            selectedUnit = uiState.selectedMinimumQuantityUnit,
            onUnitSelected = { unit ->
                viewModel.setSelectedMinimumQuantityUnit(unit)
                viewModel.hideMinimumQuantityUnitSheet()
            },
            onDismiss = { viewModel.hideMinimumQuantityUnitSheet() }
        )
    }
}

@Composable
private fun WarehouseSelectionCard(
    selectedWarehouse: Warehouse?,
    onSelectWarehouse: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelectWarehouse),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warehouse,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Warehouse",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    selectedWarehouse?.title ?: "Select Warehouse",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Change Warehouse",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CategorySelectionCard(
    selectedCategory: Category?,
    onSelectCategory: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelectCategory),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Category,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    selectedCategory?.title ?: "Select Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Change Category",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BaseUnitSelectionCard(
    selectedUnit: QuantityUnit?,
    isRequired: Boolean,
    onSelectUnit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelectUnit),
        colors = CardDefaults.cardColors(
            containerColor = if (selectedUnit == null && isRequired) 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Scale,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (selectedUnit == null && isRequired) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (isRequired) "Base Unit *" else "Base Unit",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    selectedUnit?.title ?: "Select Base Unit",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedUnit == null && isRequired)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Change Base Unit",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuantityUnitChip(
    selectedUnit: QuantityUnit?,
    onClick: () -> Unit,
    enabled: Boolean
) {
    FilterChip(
        selected = selectedUnit != null,
        onClick = onClick,
        enabled = enabled,
        label = { 
            Text(
                selectedUnit?.title ?: "Unit",
                style = MaterialTheme.typography.bodySmall
            ) 
        },
        leadingIcon = {
            Icon(
                Icons.Default.Scale,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        modifier = Modifier.height(56.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BaseUnitSelectionBottomSheet(
    parentUnitTypes: List<QuantityUnit>,
    childUnitsForParent: List<QuantityUnit>,
    selectedUnit: QuantityUnit?,
    onUnitSelected: (QuantityUnit) -> Unit,
    onParentTypeSelected: (String) -> Unit,  // Pass parent type's slug to load its children
    onDismiss: () -> Unit
) {
    // Track selected parent unit type locally within the bottom sheet
    var selectedParentTypeSlug by remember { mutableStateOf<String?>(null) }
    
    // Initialize with parent of currently selected unit (for edit mode)
    LaunchedEffect(Unit) {
        if (selectedUnit != null && selectedParentTypeSlug == null) {
            // The selected unit's parentSlug points to its parent unit type
            selectedParentTypeSlug = selectedUnit.parentSlug
            // Load child units for this parent
            selectedUnit.parentSlug?.let { onParentTypeSelected(it) }
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Select Base Unit",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Step 1: Select Unit Type (Parent)
            Text(
                text = "Step 1: Select Unit Type",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            if (parentUnitTypes.isEmpty()) {
                Text(
                    text = "No unit types available. Please add unit types first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    parentUnitTypes.forEach { parentUnit ->
                        FilterChip(
                            selected = selectedParentTypeSlug == parentUnit.slug,
                            onClick = { 
                                // Update local state first
                                selectedParentTypeSlug = parentUnit.slug
                                // Then load child units using the parent type's own slug
                                parentUnit.slug?.let { onParentTypeSelected(it) }
                            },
                            label = { Text(parentUnit.title) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Step 2: Select Child Unit (only show if parent type is selected)
            if (selectedParentTypeSlug != null) {
                val selectedParentTitle = parentUnitTypes.find { it.slug == selectedParentTypeSlug }?.title ?: ""
                
                Text(
                    text = "Step 2: Select Unit",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                if (childUnitsForParent.isEmpty()) {
                    Text(
                        text = "No units available for $selectedParentTitle. Please add units first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(childUnitsForParent) { unit ->
                            UnitSelectionItem(
                                unit = unit,
                                isSelected = selectedUnit?.slug == unit.slug,
                                onClick = { onUnitSelected(unit) }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChildUnitSelectionBottomSheet(
    title: String,
    childUnits: List<QuantityUnit>,
    selectedUnit: QuantityUnit?,
    onUnitSelected: (QuantityUnit) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (childUnits.isEmpty()) {
                Text(
                    text = "No units available. Please select a base unit first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    items(childUnits) { unit ->
                        UnitSelectionItem(
                            unit = unit,
                            isSelected = selectedUnit?.slug == unit.slug,
                            onClick = { onUnitSelected(unit) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun UnitSelectionItem(
    unit: QuantityUnit,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Scale,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    unit.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                if (unit.conversionFactor != 1.0) {
                    Text(
                        "Conversion: ${unit.conversionFactor}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

