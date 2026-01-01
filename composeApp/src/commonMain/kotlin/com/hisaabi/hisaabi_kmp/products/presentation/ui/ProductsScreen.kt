package com.hisaabi.hisaabi_kmp.products.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.hisaabi.hisaabi_kmp.core.ui.LocalWindowSizeClass
import com.hisaabi.hisaabi_kmp.core.ui.SegmentedControl
import com.hisaabi.hisaabi_kmp.core.ui.WindowWidthSizeClass
import com.hisaabi.hisaabi_kmp.products.domain.model.Product
import com.hisaabi.hisaabi_kmp.products.domain.model.ProductType
import com.hisaabi.hisaabi_kmp.products.presentation.viewmodel.ProductsViewModel
import com.hisaabi.hisaabi_kmp.products.presentation.viewmodel.QuantityFilter
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.settings.domain.model.TransactionSettings
import com.hisaabi.hisaabi_kmp.utils.format
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse
import com.hisaabi.hisaabi_kmp.categories.data.repository.CategoriesRepository
import com.hisaabi.hisaabi_kmp.categories.domain.model.Category
import com.hisaabi.hisaabi_kmp.quantityunits.data.repository.QuantityUnitsRepository
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
import org.koin.compose.koinInject

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
    onSelectionDone: () -> Unit = {},
    onNavigateToWarehouses: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val transactionSettings by viewModel.transactionSettings.collectAsState()
    val preferencesManager: PreferencesManager = koinInject()
    val selectedCurrency by preferencesManager.selectedCurrency.collectAsState(null)
    val currencySymbol = selectedCurrency?.symbol ?: ""
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(isSelectionMode) }
    var selectedProductQuantities by remember { mutableStateOf(selectedProducts.toMutableMap()) }
    var isSearchActive by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    
    // Track the last initialProductType we processed to avoid resetting on recomposition
    // This prevents the issue where addProductType is cleared to null, causing recomposition
    // and potentially resetting the product type
    var lastProcessedInitialType by remember { mutableStateOf<ProductType?>(null) }
    
    // Set initial product type when screen loads
    // Only process if initialProductType is not null AND it's different from what we've already processed
    // The ViewModel's onProductTypeChanged has internal checks to avoid unnecessary reloads
    LaunchedEffect(initialProductType) {
        if (initialProductType != null && initialProductType != lastProcessedInitialType) {
            viewModel.onProductTypeChanged(initialProductType)
            lastProcessedInitialType = initialProductType
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
                    
                    // Filter Button - show when not in selection mode
                    if (!selectionMode) {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = if (uiState.quantityFilter != QuantityFilter.ALL)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectionMode) {
                // Done button with count in selection mode
                val totalItems = selectedProductQuantities.values.sum()
                ExtendedFloatingActionButton(
                    onClick = {
                        if (selectedProductQuantities.isNotEmpty()) {
                            onSelectionChanged(selectedProductQuantities.toMap())
                            onSelectionDone()
                        }
                    },
                    containerColor = if (selectedProductQuantities.isNotEmpty()) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant,
                    icon = {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Done"
                        )
                    },
                    text = {
                        Text("Done ($totalItems)")
                    }
                )
            } else {
                // Add Product button in normal mode
                FloatingActionButton(
                    onClick = { onAddProductClick(uiState.selectedProductType) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Product")
                }
            }
        }
    ) { paddingValues ->
        val windowSizeClass = LocalWindowSizeClass.current
        val isDesktop = windowSizeClass.widthSizeClass == WindowWidthSizeClass.EXPANDED
        val maxContentWidth = if (isDesktop) 1000.dp else Dp.Unspecified
        val horizontalPadding = if (isDesktop) 24.dp else 0.dp
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .then(if (isDesktop) Modifier.widthIn(max = maxContentWidth) else Modifier.fillMaxWidth())
                    .padding(horizontal = horizontalPadding)
            ) {
                // Warehouse Selector
                WarehouseSelectorCard(
                selectedWarehouse = uiState.selectedWarehouse,
                availableWarehouses = uiState.availableWarehouses,
                onSelectWarehouse = onNavigateToWarehouses,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Product Type Filter
            ProductTypeFilter(
                selected = uiState.selectedProductType,
                onTypeSelected = { viewModel.onProductTypeChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
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
                // Use 2-column grid on desktop, 1 column on mobile
                val columns = if (isDesktop) 2 else 1
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.products, key = { it.id }) { product ->
                        val currentQuantity = selectedProductQuantities[product.slug] ?: 0
                        val availableQuantity = uiState.productQuantities[product.slug] ?: 0.0
                        ProductItem(
                            product = product,
                            transactionSettings = transactionSettings,
                            availableQuantity = availableQuantity,
                            currencySymbol = currencySymbol,
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
                    }
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
    
    // Filter Bottom Sheet
    if (showFilterSheet && !selectionMode) {
        ProductFilterBottomSheet(
            selectedFilter = uiState.quantityFilter,
            onFilterSelected = { filter ->
                viewModel.setQuantityFilter(filter)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
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

/**
 * Price type enum for mapping price labels
 */
private enum class PriceType(val key: String, val shortLabel: String, val isPrimary: Boolean) {
    RETAIL("retail", "Retail", true),
    WHOLESALE("wholesale", "Wholesale", true),
    PURCHASE("purchase", "Purchase", true),
    AVG_PURCHASE("avgPurchase", "Avg", false);
    
    companion object {
        fun fromKey(key: String): PriceType? = entries.find { it.key == key }
    }
}

@Composable
private fun ProductItem(
    product: Product,
    transactionSettings: TransactionSettings,
    availableQuantity: Double = 0.0,
    currencySymbol: String,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    showCheckbox: Boolean = false,
    quantity: Int = 0,
    onIncrementQuantity: () -> Unit = {},
    onResetQuantity: () -> Unit = {}
) {
    val categoriesRepository: CategoriesRepository = koinInject()
    val quantityUnitsRepository: QuantityUnitsRepository = koinInject()
    var category by remember(product.categorySlug) { mutableStateOf<Category?>(null) }
    var quantityUnit by remember(product.defaultUnitSlug, product.baseUnitSlug) { 
        mutableStateOf<QuantityUnit?>(null) 
    }
    
    // Load category information
    LaunchedEffect(product.categorySlug) {
        if (product.categorySlug != null) {
            category = categoriesRepository.getCategoryBySlug(product.categorySlug!!)
        } else {
            category = null
        }
    }
    
    // Load quantity unit information
    LaunchedEffect(product.defaultUnitSlug, product.baseUnitSlug) {
        val unitSlug = product.defaultUnitSlug ?: product.baseUnitSlug
        if (unitSlug != null) {
            quantityUnit = quantityUnitsRepository.getUnitBySlug(unitSlug)
        } else {
            quantityUnit = null
        }
    }
    
    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product Icon/Avatar with count overlay
                Box {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
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
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    
                    // Count badge overlay with cross button
                    if (showCheckbox && quantity > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = quantity.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
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
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Title
                    Text(
                        text = product.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Category and Quantity in same row
                    if (category != null || availableQuantity > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Category Badge
                            if (category != null) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = category!!.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            
                            // Quantity badge
                            if (availableQuantity > 0) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (quantityUnit != null) {
                                            "%.2f %s".format(availableQuantity, quantityUnit!!.title)
                                        } else {
                                            "%.2f".format(availableQuantity)
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Description
                    if (!product.description.isNullOrBlank()) {
                        Text(
                            text = product.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Prices Column - aligned to right
                val prices = buildList {
                    if (transactionSettings.showRetailPrice && product.retailPrice > 0) {
                        add("retail" to product.retailPrice)
                    }
                    if (transactionSettings.showWholeSalePrice && product.wholesalePrice > 0) {
                        add("wholesale" to product.wholesalePrice)
                    }
                    if (transactionSettings.showPurchasePrice && product.purchasePrice > 0 && product.productType != ProductType.SERVICE) {
                        add("purchase" to product.purchasePrice)
                    }
                    if (transactionSettings.showAvgPurchasePrice && product.avgPurchasePrice > 0) {
                        add("avgPurchase" to product.avgPurchasePrice)
                    }
                }
                
                if (prices.isNotEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        prices.forEachIndexed { index, (typeKey, price) ->
                            val priceType = PriceType.fromKey(typeKey)
                            val label = priceType?.shortLabel ?: ""
                            val isPrimary = priceType?.isPrimary ?: false
                            
                            if (index > 0) {
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            
                            // Price with label at start
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Label at start
                                if (label.isNotEmpty()) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                                // Price value
                                Text(
                                    text = "$currencySymbol ${"%.2f".format(price)}",
                                    style = if (isPrimary || prices.size == 1) {
                                        MaterialTheme.typography.bodyMedium
                                    } else {
                                        MaterialTheme.typography.bodySmall
                                    },
                                    fontWeight = if (isPrimary || prices.size == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isPrimary || prices.size == 1) {
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
}

@Composable
private fun WarehouseSelectorCard(
    selectedWarehouse: Warehouse?,
    availableWarehouses: List<Warehouse>,
    onSelectWarehouse: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelectWarehouse),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warehouse,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Warehouse",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    selectedWarehouse?.title ?: "Select Warehouse",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductFilterBottomSheet(
    selectedFilter: QuantityFilter,
    onFilterSelected: (QuantityFilter) -> Unit,
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
                text = "Filter Products",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            QuantityFilter.values().forEach { filter ->
                FilterOptionItem(
                    title = when (filter) {
                        QuantityFilter.ALL -> "All Products"
                        QuantityFilter.GREATER_THAN_MINIMUM -> "Greater than minimum quantity"
                        QuantityFilter.GREATER_THAN_ZERO -> "Greater than 0 quantity"
                        QuantityFilter.LESS_THAN_MINIMUM -> "Less than minimum quantity"
                        QuantityFilter.ZERO_QUANTITY -> "Having 0 quantity"
                    },
                    isSelected = filter == selectedFilter,
                    onClick = { onFilterSelected(filter) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FilterOptionItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurface
        )
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

