package com.hisaabi.hisaabi_kmp.warehouses.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.WarehouseType
import com.hisaabi.hisaabi_kmp.warehouses.presentation.viewmodel.AddWarehouseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWarehouseScreen(
    viewModel: AddWarehouseViewModel,
    warehouseToEdit: Warehouse? = null,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showTypeDialog by remember { mutableStateOf(false) }
    
    // Set warehouse to edit if provided
    LaunchedEffect(warehouseToEdit) {
        if (warehouseToEdit != null) {
            viewModel.setWarehouseToEdit(warehouseToEdit)
        } else {
            viewModel.resetState()
        }
    }
    
    // Navigate back on success
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (state.isEditMode) "Edit Warehouse" else "Add Warehouse") 
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
            // Title Field (Required)
            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.onTitleChanged(it) },
                label = { Text("Title *") },
                leadingIcon = {
                    Icon(Icons.Default.Title, contentDescription = "Title")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.titleError != null,
                supportingText = state.titleError?.let { { Text(it) } }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Warehouse Type Selection
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showTypeDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Warehouse Type *",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row {
                            Icon(
                                Icons.Default.Warehouse,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.selectedType.displayName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Address Field
            OutlinedTextField(
                value = state.address,
                onValueChange = { viewModel.onAddressChanged(it) },
                label = { Text("Address") },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = "Address")
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description Field
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.onDescriptionChanged(it) },
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
            state.error?.let { error ->
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
                onClick = { viewModel.saveWarehouse() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (state.isEditMode) "Update Warehouse" else "Add Warehouse")
                }
            }
        }
    }
    
    // Warehouse Type Selection Dialog
    if (showTypeDialog) {
        AlertDialog(
            onDismissRequest = { showTypeDialog = false },
            title = { Text("Select Warehouse Type") },
            text = {
                Column {
                    WarehouseType.entries.forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = state.selectedType == type,
                                onClick = {
                                    viewModel.onWarehouseTypeChanged(type)
                                    showTypeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = type.displayName,
                                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTypeDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

