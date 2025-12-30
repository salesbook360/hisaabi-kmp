package com.hisaabi.hisaabi_kmp.categories.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.categories.domain.model.CategoryType
import com.hisaabi.hisaabi_kmp.categories.presentation.viewmodel.AddCategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen(
    viewModel: AddCategoryViewModel,
    categoryType: CategoryType,
    onNavigateBack: () -> Unit,
    editingCategorySlug: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Initialize fields - use editing category if available, otherwise empty
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var hasNavigatedBack by remember(editingCategorySlug) { mutableStateOf(false) }
    var isInitialized by remember(editingCategorySlug) { mutableStateOf(false) }
    
    // Reset state when screen is first opened or when editingCategorySlug changes
    LaunchedEffect(editingCategorySlug) {
        hasNavigatedBack = false
        isInitialized = false
        // Clear isSuccess flag to prevent immediate navigation
        viewModel.resetState()
        
        if (editingCategorySlug != null) {
            viewModel.loadCategoryForEditing(editingCategorySlug)
        }
        
        // Mark as initialized after a brief delay to ensure state is reset
        kotlinx.coroutines.delay(50)
        isInitialized = true
    }
    
    // Prefill fields when editing category is loaded
    LaunchedEffect(uiState.editingCategory) {
        uiState.editingCategory?.let { category ->
            title = category.title
            description = category.description ?: ""
        }
    }
    
    // Navigate back on success (only once, and only after initialization and an operation)
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess && !hasNavigatedBack && isInitialized) {
            hasNavigatedBack = true
            onNavigateBack()
        }
    }
    
    val isEditing = uiState.editingCategory != null
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit ${categoryType.displayName}" else "Add New ${categoryType.displayName}") },
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
                    if (!uiState.isLoading && title.isNotBlank()) {
                        if (isEditing && uiState.editingCategory != null) {
                            viewModel.updateCategory(
                                title = title,
                                description = description.takeIf { it.isNotBlank() },
                                category = uiState.editingCategory!!
                            )
                        } else {
                            viewModel.addCategory(
                                title = title,
                                description = description.takeIf { it.isNotBlank() },
                                categoryType = categoryType
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
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save"
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
            // Title Field (Required)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
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
}


