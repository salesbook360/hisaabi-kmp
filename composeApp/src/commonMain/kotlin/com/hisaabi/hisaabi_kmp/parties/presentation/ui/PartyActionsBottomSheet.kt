package com.hisaabi.hisaabi_kmp.parties.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyActionsBottomSheet(
    party: Party,
    onDismiss: () -> Unit,
    onPayGetPayment: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTransactions: () -> Unit,
    onBalanceHistory: () -> Unit,
    onPaymentReminder: () -> Unit,
    onNewTransaction: (Int) -> Unit // Transaction type as parameter
) {
    val partyType = PartyType.fromInt(party.roleId)
    val isExpenseIncomeType = party.roleId in listOf(14, 15)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = party.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = partyType?.displayName ?: "Party",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Divider()
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Common Actions (for all party types)
            
            // Pay/Get Payment - Only for regular parties (not expense/income)
            if (!isExpenseIncomeType) {
                BottomSheetAction(
                    icon = Icons.Default.Payment,
                    title = "Pay/Get Payment",
                    subtitle = "Record a payment transaction",
                    onClick = {
                        onPayGetPayment()
                        onDismiss()
                    }
                )
            }
            
            // Edit
            BottomSheetAction(
                icon = Icons.Default.Edit,
                title = "Edit",
                subtitle = "Edit party details",
                onClick = {
                    onEdit()
                    onDismiss()
                }
            )
            
            // Delete
            BottomSheetAction(
                icon = Icons.Default.Delete,
                title = "Delete",
                subtitle = "Remove this party",
                iconTint = Color(0xFFF44336),
                onClick = {
                    onDelete()
                    // Don't dismiss immediately - let the dialog handle dismissal
                }
            )
            
            // Transactions - Only for regular parties
            if (!isExpenseIncomeType) {
                BottomSheetAction(
                    icon = Icons.Default.Receipt,
                    title = "Transactions",
                    subtitle = "View all transactions",
                    onClick = {
                        onTransactions()
                        onDismiss()
                    }
                )
            }
            
            // Balance History - Only for regular parties
            if (!isExpenseIncomeType) {
                BottomSheetAction(
                    icon = Icons.Default.History,
                    title = "Balance History",
                    subtitle = "View balance changes over time",
                    onClick = {
                        onBalanceHistory()
                        onDismiss()
                    }
                )
            }
            
            // Payment Reminder - Only for regular parties with non-zero balance
            if (!isExpenseIncomeType && party.balance != 0.0) {
                BottomSheetAction(
                    icon = Icons.Default.Notifications,
                    title = "Payment Reminder",
                    subtitle = "Send a reminder message",
                    onClick = {
                        onPaymentReminder()
                        onDismiss()
                    }
                )
            }
            
            // Party Type Specific Actions
            when (partyType) {
                PartyType.CUSTOMER, PartyType.WALK_IN_CUSTOMER -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // New Sale
                    BottomSheetAction(
                        icon = Icons.Default.AddShoppingCart,
                        title = "New Sale",
                        subtitle = "Create a new sale transaction",
                        iconTint = Color(0xFF4CAF50),
                        onClick = {
                            onNewTransaction(AllTransactionTypes.SALE.value)
                            onDismiss()
                        }
                    )
                    
                    // Customer Return
                    BottomSheetAction(
                        icon = Icons.Default.AssignmentReturn,
                        title = "Sale Return",
                        subtitle = "Process a customer return",
                        onClick = {
                            onNewTransaction(AllTransactionTypes.CUSTOMER_RETURN.value)
                            onDismiss()
                        }
                    )
                }
                
                PartyType.VENDOR, PartyType.DEFAULT_VENDOR -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // New Purchase
                    BottomSheetAction(
                        icon = Icons.Default.ShoppingCart,
                        title = "New Purchase",
                        subtitle = "Create a new purchase transaction",
                        iconTint = Color(0xFF2196F3),
                        onClick = {
                            onNewTransaction(AllTransactionTypes.PURCHASE.value)
                            onDismiss()
                        }
                    )
                    
                    // Vendor Return
                    BottomSheetAction(
                        icon = Icons.Default.AssignmentReturn,
                        title = "Purchase Return",
                        subtitle = "Process a vendor return",
                        onClick = {
                            onNewTransaction(AllTransactionTypes.VENDOR_RETURN.value)
                            onDismiss()
                        }
                    )
                }
                
                else -> {
                    // No specific actions for other party types
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun BottomSheetAction(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun DeletePartyDialog(
    party: Party,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFF44336)
            )
        },
        title = {
            Text(
                text = "Delete Party?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to delete \"${party.name}\"?"
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (party.balance != 0.0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3CD)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF856404),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "This party has a pending balance. Make sure to settle it first.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF856404)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This action cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF44336)
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

