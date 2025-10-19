package com.hisaabi.hisaabi_kmp.transactions.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalAccountTypeDialog(
    onDismiss: () -> Unit,
    onSelectExpense: () -> Unit,
    onSelectExtraIncome: () -> Unit,
    onSelectCustomer: () -> Unit,
    onSelectVendor: () -> Unit,
    onSelectInvestor: () -> Unit,
    onSelectPaymentMethod: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Account Type") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AccountTypeOption(
                    icon = Icons.Default.TrendingDown,
                    title = "Expense Account",
                    onClick = onSelectExpense
                )
                AccountTypeOption(
                    icon = Icons.Default.TrendingUp,
                    title = "Extra Income Account",
                    onClick = onSelectExtraIncome
                )
                AccountTypeOption(
                    icon = Icons.Default.Person,
                    title = "Customer",
                    onClick = onSelectCustomer
                )
                AccountTypeOption(
                    icon = Icons.Default.Store,
                    title = "Vendor",
                    onClick = onSelectVendor
                )
                AccountTypeOption(
                    icon = Icons.Default.Business,
                    title = "Investor",
                    onClick = onSelectInvestor
                )
                AccountTypeOption(
                    icon = Icons.Default.AccountBalance,
                    title = "Payment Method",
                    onClick = onSelectPaymentMethod
                )
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
private fun AccountTypeOption(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

