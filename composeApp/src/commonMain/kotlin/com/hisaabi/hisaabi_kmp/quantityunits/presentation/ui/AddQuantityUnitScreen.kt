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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
import com.hisaabi.hisaabi_kmp.quantityunits.presentation.viewmodel.AddQuantityUnitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuantityUnitScreen(
    viewModel: AddQuantityUnitViewModel,
    unitToEdit: QuantityUnit? = null,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(unitToEdit) {
        if (unitToEdit != null) viewModel.setUnitToEdit(unitToEdit)
        else viewModel.resetState()
    }
    
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onNavigateBack()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Edit Unit" else "Add Unit") },
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
        Column(Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).verticalScroll(rememberScrollState())) {
            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.onTitleChanged(it) },
                label = { Text("Unit Name *") },
                leadingIcon = { Icon(Icons.Default.Scale, "Unit Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.titleError != null,
                supportingText = state.titleError?.let { { Text(it) } }
            )
            
            Spacer(Modifier.height(16.dp))
            
            OutlinedTextField(
                value = state.conversionFactor,
                onValueChange = { viewModel.onConversionFactorChanged(it) },
                label = { Text("Conversion Factor *") },
                leadingIcon = { Icon(Icons.Default.Calculate, "Factor") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = state.conversionFactorError != null,
                supportingText = state.conversionFactorError?.let { { Text(it) } }
            )
            
            Spacer(Modifier.height(16.dp))
            
            OutlinedTextField(
                value = state.sortOrder,
                onValueChange = { viewModel.onSortOrderChanged(it) },
                label = { Text("Sort Order") },
                leadingIcon = { Icon(Icons.Default.Sort, "Sort Order") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            Spacer(Modifier.height(24.dp))
            
            state.error?.let { error ->
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(error, Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

