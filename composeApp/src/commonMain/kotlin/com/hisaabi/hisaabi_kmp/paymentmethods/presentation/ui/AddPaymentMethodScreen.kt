package com.hisaabi.hisaabi_kmp.paymentmethods.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import com.hisaabi.hisaabi_kmp.paymentmethods.presentation.viewmodel.AddPaymentMethodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentMethodScreen(
    viewModel: AddPaymentMethodViewModel,
    paymentMethodToEdit: PaymentMethod? = null,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    // Set payment method to edit if provided
    LaunchedEffect(paymentMethodToEdit) {
        if (paymentMethodToEdit != null) {
            viewModel.setPaymentMethodToEdit(paymentMethodToEdit)
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
                    Text(if (state.isEditMode) "Edit Payment Method" else "Add Payment Method") 
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Opening Amount Field (Only for new payment methods)
            if (!state.isEditMode) {
                OutlinedTextField(
                    value = state.openingAmount,
                    onValueChange = { viewModel.onOpeningAmountChanged(it) },
                    label = { Text("Opening Balance *") },
                    leadingIcon = {
                        Icon(Icons.Default.AttachMoney, contentDescription = "Amount")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = state.amountError != null,
                    supportingText = state.amountError?.let { { Text(it) } }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Note: Opening balance can only be set when creating a new payment method",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                // Show current balance for edit mode
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Current Balance",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%.2f", state.paymentMethodToEdit?.amount ?: 0.0),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Balance cannot be edited directly. It will be updated through transactions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
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
                onClick = { viewModel.savePaymentMethod() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (state.isEditMode) "Update Payment Method" else "Add Payment Method")
                }
            }
        }
    }
}

