package com.hisaabi.hisaabi_kmp.categories.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.categories.domain.model.Category
import com.hisaabi.hisaabi_kmp.categories.domain.model.CategoryType
import com.hisaabi.hisaabi_kmp.categories.presentation.viewmodel.CategoriesViewModel
import com.hisaabi.hisaabi_kmp.core.ui.SegmentedControl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    categoryType: CategoryType? = null, // Optional for backward compatibility
    onCategorySelected: (Category?) -> Unit = {},
    onAddCategoryClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onEditCategoryClick: (Category) -> Unit ,
    refreshTrigger: Int = 0
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Initialize with provided categoryType or default to CUSTOMER_CATEGORY
    val initialCategoryType = categoryType ?: CategoryType.CUSTOMER_CATEGORY
    var selectedCategoryType by remember { mutableStateOf(initialCategoryType) }
    
    // Initialize category type on first load
    LaunchedEffect(Unit) {
        if (uiState.selectedCategoryType == null) {
            viewModel.onCategoryTypeChanged(initialCategoryType)
        } else {
            selectedCategoryType = uiState.selectedCategoryType!!
        }
    }
    
    // Update selectedCategoryType when uiState changes
    LaunchedEffect(uiState.selectedCategoryType) {
        uiState.selectedCategoryType?.let {
            selectedCategoryType = it
        }
    }
    
    // Load categories when category type changes or refresh trigger changes
    LaunchedEffect(selectedCategoryType, refreshTrigger) {
        viewModel.onCategoryTypeChanged(selectedCategoryType)
    }
    
    // Handle external categoryType parameter changes
    LaunchedEffect(categoryType) {
        categoryType?.let {
            if (it != selectedCategoryType) {
                selectedCategoryType = it
                viewModel.onCategoryTypeChanged(it)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
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
                onClick = {
                    // Pass the current category type to the callback
                    onAddCategoryClick()
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add ${selectedCategoryType.displayName}")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Type Filter - Segmented Control
            CategoryTypeFilter(
                selected = selectedCategoryType,
                onTypeSelected = { type ->
                    selectedCategoryType = type
                    viewModel.onCategoryTypeChanged(type)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.error != null -> {
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
                            Button(onClick = { viewModel.onCategoryTypeChanged(selectedCategoryType) }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                
                uiState.categories.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No ${selectedCategoryType.displayName.lowercase()}s found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = onAddCategoryClick) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add ${selectedCategoryType.displayName}")
                            }
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(uiState.categories, key = { it.id }) { category ->
                            CategoryItem(
                                category = category,
                                onClick = { onCategorySelected(category) },
                                onEditClick = { onEditCategoryClick(category) },
                                onDeleteClick = { viewModel.deleteCategory(category) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(
    category: Category,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onClick)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick)
            ) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                if (!category.description.isNullOrBlank()) {
                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Edit button
            IconButton(onClick = onEditClick) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // Delete button
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Category") },
            text = { Text("Are you sure you want to delete \"${category.title}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CategoryTypeFilter(
    selected: CategoryType,
    onTypeSelected: (CategoryType) -> Unit,
    modifier: Modifier = Modifier
) {
    // Create list of category types: Customer Category, Customer Area, Product Category
    val categoryTypes = listOf(
        CategoryType.CUSTOMER_CATEGORY,
        CategoryType.AREA,
        CategoryType.PRODUCTS
    )
    
    SegmentedControl(
        items = categoryTypes,
        selectedItem = selected,
        onItemSelected = onTypeSelected,
        modifier = modifier,
        itemDisplayName = { type ->
            when (type) {
                CategoryType.CUSTOMER_CATEGORY -> "Customer Category"
                CategoryType.AREA -> "Customer Area"
                CategoryType.PRODUCTS -> "Product Category"
            }
        }
    )
}



