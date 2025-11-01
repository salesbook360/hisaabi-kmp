package com.hisaabi.hisaabi_kmp.products.presentation.ui

import androidx.compose.foundation.layout.*
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
import com.hisaabi.hisaabi_kmp.products.domain.model.Product
import com.hisaabi.hisaabi_kmp.products.domain.model.ProductType
import com.hisaabi.hisaabi_kmp.products.presentation.viewmodel.AddProductViewModel
import com.hisaabi.hisaabi_kmp.utils.format
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: AddProductViewModel,
    productType: ProductType,
    productToEdit: Product? = null,
    onNavigateBack: () -> Unit,
    onNavigateToIngredients: ((String) -> Unit)? = null,  // Recipe slug
    onNavigateToWarehouses: () -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var retailPrice by remember { mutableStateOf("") }
    var wholesalePrice by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var taxPercentage by remember { mutableStateOf("") }
    var discountPercentage by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    
    val uiState by viewModel.uiState.collectAsState()
    
    // Update fields when productToEdit changes
    LaunchedEffect(productToEdit) {
        if (productToEdit != null) {
            viewModel.setProductToEdit(productToEdit)
            title = productToEdit.title
            description = productToEdit.description ?: ""
            retailPrice = "%.2f".format(productToEdit.retailPrice)
            wholesalePrice = "%.2f".format(productToEdit.wholesalePrice)
            purchasePrice = "%.2f".format(productToEdit.purchasePrice)
            taxPercentage = "%.2f".format(productToEdit.taxPercentage)
            discountPercentage = "%.2f".format(productToEdit.discountPercentage)
            manufacturer = productToEdit.manufacturer ?: ""
        } else {
            viewModel.resetState()
        }
    }
    
    // Reset state when screen is first opened
    LaunchedEffect(Unit) {
        if (productToEdit == null) {
            viewModel.resetState()
        }
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
                value = title,
                onValueChange = { title = it },
                label = { Text("Product Title *") },
                leadingIcon = {
                    Icon(Icons.Default.Title, contentDescription = "Title")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = title.isBlank() && uiState.error != null
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description Field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
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
                value = retailPrice,
                onValueChange = { retailPrice = it },
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
                value = wholesalePrice,
                onValueChange = { wholesalePrice = it },
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
                    value = purchasePrice,
                    onValueChange = { purchasePrice = it },
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
                value = taxPercentage,
                onValueChange = { taxPercentage = it },
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
                value = discountPercentage,
                onValueChange = { discountPercentage = it },
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
                    value = manufacturer,
                    onValueChange = { manufacturer = it },
                    label = { Text("Manufacturer") },
                    leadingIcon = {
                        Icon(Icons.Default.Factory, contentDescription = "Manufacturer")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Quantity Information Section
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
                OutlinedTextField(
                    value = uiState.openingQuantity,
                    onValueChange = { viewModel.setOpeningQuantity(it) },
                    label = { Text("Opening Quantity") },
                    leadingIcon = {
                        Icon(Icons.Default.Inventory, contentDescription = "Opening Quantity")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Minimum Quantity
                OutlinedTextField(
                    value = uiState.minimumQuantity,
                    onValueChange = { viewModel.setMinimumQuantity(it) },
                    label = { Text("Minimum Quantity") },
                    leadingIcon = {
                        Icon(Icons.Default.Warning, contentDescription = "Minimum Quantity")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
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
            
            // Save Button
            Button(
                onClick = {
                    viewModel.saveProduct(
                        productToEdit = productToEdit,
                        title = title,
                        description = description.takeIf { it.isNotBlank() },
                        productType = productType,
                        retailPrice = retailPrice.toDoubleOrNull() ?: 0.0,
                        wholesalePrice = wholesalePrice.toDoubleOrNull() ?: 0.0,
                        purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                        taxPercentage = taxPercentage.toDoubleOrNull() ?: 0.0,
                        discountPercentage = discountPercentage.toDoubleOrNull() ?: 0.0,
                        manufacturer = manufacturer.takeIf { it.isNotBlank() },
                        warehouseSlug = uiState.selectedWarehouse?.slug,
                        openingQuantity = uiState.openingQuantity.toDoubleOrNull() ?: 0.0,
                        minimumQuantity = uiState.minimumQuantity.toDoubleOrNull() ?: 0.0
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && title.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isEditMode) {
                        when (productType) {
                            ProductType.SIMPLE_PRODUCT -> "Update Product"
                            ProductType.SERVICE -> "Update Service"
                            ProductType.RECIPE -> "Update Recipe"
                        }
                    } else {
                        when (productType) {
                            ProductType.SIMPLE_PRODUCT -> "Add Product"
                            ProductType.SERVICE -> "Add Service"
                            ProductType.RECIPE -> "Add Recipe"
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
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

