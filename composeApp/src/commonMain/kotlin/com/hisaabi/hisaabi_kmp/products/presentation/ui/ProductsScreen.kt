package com.hisaabi.hisaabi_kmp.products.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaabi.hisaabi_kmp.core.ui.SegmentedControl
import com.hisaabi.hisaabi_kmp.products.domain.model.Product
import com.hisaabi.hisaabi_kmp.products.domain.model.ProductType
import com.hisaabi.hisaabi_kmp.products.presentation.viewmodel.ProductsViewModel
import com.hisaabi.hisaabi_kmp.settings.domain.model.TransactionSettings
import com.hisaabi.hisaabi_kmp.utils.format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: ProductsViewModel,
    onProductClick: (Product) -> Unit = {},
    onEditProductClick: (Product) -> Unit = {},
    onAddProductClick: (ProductType?) -> Unit = {},
    onNavigateToIngredients: (Product) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    refreshTrigger: Int = 0,
    initialProductType: ProductType? = null,
    isSelectionMode: Boolean = false,
    selectedProducts: Map<String, Int> = emptyMap(),
    onSelectionChanged: (Map<String, Int>) -> Unit = {},
    onSelectionDone: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val transactionSettings by viewModel.transactionSettings.collectAsState()
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(isSelectionMode) }
    var selectedProductQuantities by remember { mutableStateOf(selectedProducts.toMutableMap()) }
    var isSearchActive by remember { mutableStateOf(false) }
    
    // Set initial product type when screen loads
    LaunchedEffect(initialProductType) {
        if (initialProductType != null) {
            viewModel.onProductTypeChanged(initialProductType)
        }
    }
    
    // Refresh when trigger changes
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            viewModel.refresh()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (selectionMode) {
                        Text("Select Products (${selectedProductQuantities.size})")
                    } else {
                        Text("Products")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearSearch()
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Search Button - show in both modes
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    
                    if (selectionMode) {
                        // Done button with count
                        val totalItems = selectedProductQuantities.values.sum()
                        TextButton(
                            onClick = {
                                onSelectionChanged(selectedProductQuantities.toMap())
                                onSelectionDone()
                            },
                            enabled = selectedProductQuantities.isNotEmpty()
                        ) {
                            Text(
                                "Done ($totalItems)",
                                color = if (selectedProductQuantities.isNotEmpty()) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!selectionMode) {
                FloatingActionButton(
                    onClick = { onAddProductClick(uiState.selectedProductType) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Product")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Product Type Filter
            ProductTypeFilter(
                selected = uiState.selectedProductType,
                onTypeSelected = { viewModel.onProductTypeChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            
            // Search Bar
            if (isSearchActive) {
                ProductSearchBar(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                    onClearSearch = { 
                        viewModel.onSearchQueryChanged("")
                        isSearchActive = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
            
            // Products List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: "An error occurred",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Retry")
                        }
                    }
                }
            } else if (uiState.products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No products found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { onAddProductClick(uiState.selectedProductType) }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Product")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(uiState.products, key = { it.id }) { product ->
                        val currentQuantity = selectedProductQuantities[product.slug] ?: 0
                        ProductItem(
                            product = product,
                            transactionSettings = transactionSettings,
                            onClick = { 
                                if (selectionMode) {
                                    // Increment quantity on tap
                                    val newQuantities = selectedProductQuantities.toMutableMap()
                                    val newQuantity = currentQuantity + 1
                                    newQuantities[product.slug] = newQuantity
                                    selectedProductQuantities = newQuantities
                                    onSelectionChanged(selectedProductQuantities.toMap())
                                } else {
                                    selectedProduct = product
                                    showBottomSheet = true
                                }
                            },
                            isSelected = false, // Not used anymore
                            showCheckbox = selectionMode,
                            quantity = currentQuantity,
                            onIncrementQuantity = {
                                // Increment quantity
                                val newQuantities = selectedProductQuantities.toMutableMap()
                                val newQuantity = currentQuantity + 1
                                newQuantities[product.slug] = newQuantity
                                selectedProductQuantities = newQuantities
                                onSelectionChanged(selectedProductQuantities.toMap())
                            },
                            onResetQuantity = {
                                // Reset quantity for this product
                                val newQuantities = selectedProductQuantities.toMutableMap()
                                newQuantities.remove(product.slug)
                                selectedProductQuantities = newQuantities
                                onSelectionChanged(selectedProductQuantities.toMap())
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
    
    // Product Actions Bottom Sheet
    if (showBottomSheet && selectedProduct != null) {
        ProductActionsBottomSheet(
            product = selectedProduct!!,
            onDismiss = { 
                showBottomSheet = false
                selectedProduct = null
            },
            onEdit = {
                showBottomSheet = false
                onEditProductClick(selectedProduct!!)
            },
            onDelete = {
                showBottomSheet = false
                showDeleteConfirmation = true
            },
            onIngredients = {
                showBottomSheet = false
                onNavigateToIngredients(selectedProduct!!)
            }
        )
    }
    
    // Delete Confirmation Dialog
    if (showDeleteConfirmation && selectedProduct != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteConfirmation = false
                selectedProduct = null
            },
            title = { Text("Delete Product") },
            text = { 
                Text("Are you sure you want to delete ${selectedProduct?.title}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProduct(selectedProduct!!.slug)
                        showDeleteConfirmation = false
                        selectedProduct = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteConfirmation = false
                        selectedProduct = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductActionsBottomSheet(
    product: Product,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onIngredients: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Product Info
            Text(
                text = product.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (!product.description.isNullOrBlank()) {
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Edit Action
            ProductActionItem(
                icon = Icons.Default.Edit,
                title = "Edit",
                onClick = onEdit
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Delete Action
            ProductActionItem(
                icon = Icons.Default.Delete,
                title = "Delete",
                onClick = onDelete,
                iconTint = MaterialTheme.colorScheme.error,
                textColor = MaterialTheme.colorScheme.error
            )
            
            // Ingredients Action (only for Recipe products)
            if (product.isRecipe) {
                Spacer(modifier = Modifier.height(8.dp))
                ProductActionItem(
                    icon = Icons.Default.Restaurant,
                    title = "Ingredients",
                    onClick = onIngredients
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Cancel Button
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProductActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )
    }
}

@Composable
private fun ProductTypeFilter(
    selected: ProductType?,
    onTypeSelected: (ProductType?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Create list of all product types including "All"
    val allTypes = listOf(null) + ProductType.entries
    
    SegmentedControl(
        items = allTypes,
        selectedItem = selected,
        onItemSelected = onTypeSelected,
        modifier = modifier,
        itemDisplayName = { type ->
            type?.displayName ?: "All"
        }
    )
}

@Composable
private fun ProductItem(
    product: Product,
    transactionSettings: TransactionSettings,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    showCheckbox: Boolean = false,
    quantity: Int = 0,
    onIncrementQuantity: () -> Unit = {},
    onResetQuantity: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Product Icon/Avatar with count overlay
                Box {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                when (product.productType) {
                                    ProductType.SIMPLE_PRODUCT -> Color(0xFF4CAF50)
                                    ProductType.SERVICE -> Color(0xFF2196F3)
                                    ProductType.RECIPE -> Color(0xFFFF9800)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (product.productType) {
                                ProductType.SIMPLE_PRODUCT -> Icons.Default.Inventory
                                ProductType.SERVICE -> Icons.Default.Build
                                ProductType.RECIPE -> Icons.Default.Restaurant
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Count badge overlay with cross button
                    if (showCheckbox && quantity > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 8.dp, y = (-4).dp)
                        ) {
                            // Badge background
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = quantity.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                    // Cross button to reset
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Reset",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clickable(
                                                onClick = onResetQuantity,
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Product Info - takes available space
                Column(modifier = Modifier.weight(1f)) {
                    // Title row with prices on right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        // Prices on the right side
                        val prices = buildList {
                            // Retail Price
                            if (transactionSettings.showRetailPrice && product.retailPrice > 0) {
                                add("retail" to product.retailPrice)
                            }
                            
                            // Wholesale Price
                            if (transactionSettings.showWholeSalePrice && product.wholesalePrice > 0) {
                                add("wholesale" to product.wholesalePrice)
                            }
                            
                            // Purchase Price (only for non-service products)
                            if (transactionSettings.showPurchasePrice && product.purchasePrice > 0 && product.productType != ProductType.SERVICE) {
                                add("purchase" to product.purchasePrice)
                            }
                            
                            // Average Purchase Price
                            if (transactionSettings.showAvgPurchasePrice && product.avgPurchasePrice > 0) {
                                add("avgPurchase" to product.avgPurchasePrice)
                            }
                        }
                        
                        if (prices.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            if (prices.size == 1) {
                                // Single price - show without label
                                val (_, price) = prices[0]
                                Text(
                                    text = "₨ %.2f".format(price),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.End
                                )
                            } else {
                                // Multiple prices - show all horizontally with labels
                                Column(horizontalAlignment = Alignment.End) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        prices.forEachIndexed { index, (type, price) ->
                                            val (label, isPrimary) = when (type) {
                                                "retail" -> "Retail" to true
                                                "wholesale" -> "Wholesale" to true
                                                "purchase" -> "Purchase" to true
                                                "avgPurchase" -> "Avg" to false
                                                else -> "" to false
                                            }
                                            
                                            Column(horizontalAlignment = Alignment.End) {
                                                if (label.isNotEmpty()) {
                                                    // Show label for all prices when multiple are displayed
                                                    Text(
                                                        text = label,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 9.sp
                                                    )
                                                }
                                                Text(
                                                    text = "₨ %.2f".format(price),
                                                    style = if (isPrimary) {
                                                        MaterialTheme.typography.bodySmall
                                                    } else {
                                                        MaterialTheme.typography.bodySmall
                                                    },
                                                    fontWeight = if (isPrimary) {
                                                        FontWeight.Bold
                                                    } else {
                                                        FontWeight.Normal
                                                    },
                                                    color = if (isPrimary) {
                                                        MaterialTheme.colorScheme.primary
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Category and description row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = product.productType.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (!product.slug.isBlank()) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Slug: ${product.slug}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        if (!product.description.isNullOrBlank()) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = product.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductSearchBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        modifier = modifier,
        placeholder = { Text("Search products...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = onClearSearch) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search"
                    )
                }
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

