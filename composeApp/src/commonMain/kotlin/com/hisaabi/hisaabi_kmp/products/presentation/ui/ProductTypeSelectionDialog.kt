package com.hisaabi.hisaabi_kmp.products.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.products.domain.model.ProductType

@Composable
fun ProductTypeSelectionDialog(
    onTypeSelected: (ProductType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "Select Product Type",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProductType.entries.forEach { type ->
                    ProductTypeOption(
                        productType = type,
                        onClick = { onTypeSelected(type) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ProductTypeOption(
    productType: ProductType,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (productType) {
                ProductType.SIMPLE_PRODUCT -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                ProductType.SERVICE -> Color(0xFF2196F3).copy(alpha = 0.1f)
                ProductType.RECIPE -> Color(0xFFFF9800).copy(alpha = 0.1f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (productType) {
                    ProductType.SIMPLE_PRODUCT -> Icons.Default.Inventory
                    ProductType.SERVICE -> Icons.Default.Build
                    ProductType.RECIPE -> Icons.Default.Restaurant
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = when (productType) {
                    ProductType.SIMPLE_PRODUCT -> Color(0xFF4CAF50)
                    ProductType.SERVICE -> Color(0xFF2196F3)
                    ProductType.RECIPE -> Color(0xFFFF9800)
                }
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = productType.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (productType) {
                        ProductType.SIMPLE_PRODUCT -> "Can be purchased and sold"
                        ProductType.SERVICE -> "Can only be sold"
                        ProductType.RECIPE -> "Manufactured with ingredients"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


