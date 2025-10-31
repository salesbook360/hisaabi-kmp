package com.hisaabi.hisaabi_kmp.products.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
    initialProductType: ProductType? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
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
                title = { Text("Products") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Search Button
                    IconButton(onClick = { /* TODO: Show search bar */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddProductClick(uiState.selectedProductType) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
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
                        ProductItem(
                            product = product,
                            onClick = { 
                                selectedProduct = product
                                showBottomSheet = true
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
    onClick: () -> Unit
) {
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Icon/Avatar
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
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Product Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = product.productType.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
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
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Price
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₨ %.2f".format(product.retailPrice),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (product.wholesalePrice > 0 && product.wholesalePrice != product.retailPrice) {
                    Text(
                        text = "W: ₨ %.2f".format(product.wholesalePrice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

