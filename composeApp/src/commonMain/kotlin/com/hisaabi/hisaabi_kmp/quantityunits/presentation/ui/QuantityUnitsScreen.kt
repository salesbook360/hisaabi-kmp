package com.hisaabi.hisaabi_kmp.quantityunits.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
import com.hisaabi.hisaabi_kmp.quantityunits.presentation.viewmodel.QuantityUnitsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantityUnitsScreen(
    viewModel: QuantityUnitsViewModel,
    onUnitClick: (QuantityUnit) -> Unit = {},
    onAddUnitClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    refreshTrigger: Int = 0
) {
    val state by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<QuantityUnit?>(null) }
    
    LaunchedEffect(refreshTrigger) {
        viewModel.loadUnits()
    }
    
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quantity Units") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddUnitClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Add Unit")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading && state.units.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                
                state.units.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Scale, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(16.dp))
                            Text("No quantity units found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = onAddUnitClick) {
                                Icon(Icons.Default.Add, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Add Unit")
                            }
                        }
                    }
                }
                
                else -> {
                    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                        items(state.units, key = { it.id }) { unit ->
                            UnitItem(unit, { onUnitClick(unit) }, { showDeleteDialog = unit })
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
    
    showDeleteDialog?.let { unit ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Unit") },
            text = { Text("Are you sure you want to delete '${unit.title}'?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteUnit(unit); showDeleteDialog = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun UnitItem(unit: QuantityUnit, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Scale, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(unit.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("Conversion Factor: ${unit.conversionFactor}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (unit.sortOrder > 0) {
                    Text("Sort Order: ${unit.sortOrder}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (!unit.isActive) {
                    Text("Inactive", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

