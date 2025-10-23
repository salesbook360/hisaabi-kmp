package com.hisaabi.hisaabi_kmp.business.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.business.domain.model.Business
import com.hisaabi.hisaabi_kmp.business.presentation.viewmodel.MyBusinessViewModel

/**
 * Business Selection Gate Screen
 * Shown after login when no business is selected.
 * User MUST select or create a business to proceed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessSelectionGateScreen(
    viewModel: MyBusinessViewModel,
    onBusinessSelected: () -> Unit,
    onAddBusinessClick: () -> Unit,
    onExitApp: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    // Load businesses when screen appears
    LaunchedEffect(Unit) {
        viewModel.loadBusinesses()
    }
    
    // Navigate to home when a business is selected
    LaunchedEffect(state.selectedBusinessId) {
        if (state.selectedBusinessId != null) {
            onBusinessSelected()
        }
    }
    
    // Show error snackbar
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
                title = { Text("Select Business") },
                actions = {
                    // Close/Exit button
                    IconButton(onClick = onExitApp) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = "Exit"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading && state.businesses.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                state.businesses.isEmpty() -> {
                    // No businesses found - show create business prompt
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Business,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Welcome to Hisaabi!",
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "To get started, create your first business.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "All your transactions, parties, and products will be organized by business.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(
                                onClick = onAddBusinessClick,
                                modifier = Modifier.fillMaxWidth(0.7f)
                            ) {
                                Icon(Icons.Default.Business, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create Your First Business")
                            }
                        }
                    }
                }
                
                else -> {
                    // Show businesses list with selection prompt
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Selection instruction card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Select a Business",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Choose which business you want to work with. You can switch businesses anytime from the More menu.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        // Business list (reuse MyBusinessScreen content)
                        MyBusinessScreen(
                            viewModel = viewModel,
                            onBusinessClick = { /* Not used in gate mode */ },
                            onAddBusinessClick = onAddBusinessClick,
                            onNavigateBack = onExitApp, // Back exits app
                            showTopBar = false // Hide top bar to avoid duplicate headers
                        )
                    }
                }
            }
        }
    }
}

