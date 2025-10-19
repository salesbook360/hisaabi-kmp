package com.hisaabi.hisaabi_kmp.templates.presentation.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.templates.domain.model.TemplatePlaceholder
import com.hisaabi.hisaabi_kmp.templates.presentation.viewmodel.AddTemplateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTemplateScreen(
    viewModel: AddTemplateViewModel,
    templateId: String? = null,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(templateId) {
        viewModel.loadTemplate(templateId)
    }
    
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Edit Template" else "Add Template") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveTemplate(onNavigateBack) },
                        enabled = !state.isSaving
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Check, "Save")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.saveTemplate(onNavigateBack) },
                icon = { 
                    if (state.isSaving) {
                        CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                    } else {
                        Icon(Icons.Default.Save, "Save")
                    }
                },
                text = { Text(if (state.isSaving) "Saving..." else "Save Template") },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Title Input
            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Template Title") },
                placeholder = { Text("e.g., Payment Reminder") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Template Text Input
            OutlinedTextField(
                value = state.templateText,
                onValueChange = { viewModel.updateTemplateText(it) },
                label = { Text("Template Text") },
                placeholder = { Text("Enter your message template...") },
                modifier = Modifier.fillMaxWidth().height(200.dp),
                minLines = 8
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Placeholders Section
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Available Placeholders",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Text(
                        "Tap a placeholder to insert it into your template:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // Placeholder chips - Scrollable horizontally
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TemplatePlaceholder.getAllPlaceholders().forEach { placeholder ->
                            FilterChip(
                                selected = false,
                                onClick = { viewModel.insertPlaceholder(placeholder) },
                                label = { Text(placeholder.key) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Divider()
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // Placeholder descriptions
                    Text(
                        "Placeholder Descriptions:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    TemplatePlaceholder.getAllPlaceholders().forEach { placeholder ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                placeholder.key,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(180.dp)
                            )
                            Text(
                                placeholder.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Example Template Card
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Example Template",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        "Dear [CUSTOMER_NAME], your current balance is [CURRENT_BALANCE]. Please contact [BUSINESS_NAME] at [CONTACT_NUMBER].",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            
            // Bottom padding for FAB
            Spacer(Modifier.height(80.dp))
        }
    }
}

