package com.hisaabi.hisaabi_kmp.parties.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.utils.format
import com.hisaabi.hisaabi_kmp.utils.formatTransactionDate
import kotlin.math.abs
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceHistoryScreen(
    party: Party,
    transactions: List<Transaction>,
    onNavigateBack: () -> Unit
) {
    // Currency
    val preferencesManager: PreferencesManager = koinInject()
    val selectedCurrency by preferencesManager.selectedCurrency.collectAsState(null)
    val currencySymbol = selectedCurrency?.symbol ?: ""
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Balance History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Party Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = party.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Current Balance",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "$currencySymbol %.2f".format(abs(party.balance)),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    party.balance > 0 -> Color(0xFF4CAF50)
                                    party.balance < 0 -> Color(0xFFF44336)
                                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = when {
                                    party.balance > 0 -> "You'll Pay"
                                    party.balance < 0 -> "You'll Get"
                                    else -> "Settled"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = when {
                                    party.balance > 0 -> Color(0xFF4CAF50)
                                    party.balance < 0 -> Color(0xFFF44336)
                                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                        }
                    }
                }
            }

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transaction history found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Calculate balance history from transactions
                val balanceHistory = calculateBalanceHistory(transactions, party.openingBalance)
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Show opening balance if it exists
                    if (party.openingBalance != 0.0) {
                        item {
                            BalanceHistoryItem(
                                title = "Opening Balance",
                                date = party.createdAt ?: "",
                                amount = party.openingBalance,
                                balance = party.openingBalance,
                                isOpening = true,
                                currencySymbol
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    
                    items(balanceHistory) { item ->
                        BalanceHistoryItem(
                            title = AllTransactionTypes.getDisplayName(item.transaction.transactionType),
                            date = item.transaction.timestamp ?: "",
                            amount = item.amount,
                            balance = item.runningBalance,
                            isOpening = false,
                            currencySymbol
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceHistoryItem(
    title: String,
    date: String,
    amount: Double,
    balance: Double,
    isOpening: Boolean,
    currencySymbol:String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = when {
                            isOpening -> MaterialTheme.colorScheme.surfaceVariant
                            amount > 0 -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                            amount < 0 -> Color(0xFFF44336).copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!isOpening) {
                    Icon(
                        imageVector = if (amount > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (amount > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Transaction Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatTransactionDate(date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Amount and Balance
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (amount > 0) "+" else ""}$currencySymbol %.2f".format(abs(amount)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        amount > 0 -> Color(0xFF4CAF50)
                        amount < 0 -> Color(0xFFF44336)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = "Balance: $currencySymbol %.2f".format(abs(balance)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class BalanceHistoryItem(
    val transaction: Transaction,
    val amount: Double,
    val runningBalance: Double
)

private fun calculateBalanceHistory(
    transactions: List<Transaction>,
    openingBalance: Double
): List<BalanceHistoryItem> {
    var runningBalance = openingBalance
    
    return transactions.map { transaction ->
        // Calculate the effect of this transaction on balance
        val amount = calculateTransactionEffect(transaction)
        runningBalance += amount
        
        BalanceHistoryItem(
            transaction = transaction,
            amount = amount,
            runningBalance = runningBalance
        )
    }
}

private fun calculateTransactionEffect(transaction: Transaction): Double {
    return when (transaction.transactionType) {
        AllTransactionTypes.SALE.value,
        AllTransactionTypes.GET_FROM_CUSTOMER.value,
        AllTransactionTypes.PAY_TO_CUSTOMER.value,
        AllTransactionTypes.VENDOR_RETURN.value,
        AllTransactionTypes.GET_FROM_VENDOR.value -> {
            // These increase balance (you will get)
            -(transaction.totalBill - transaction.totalPaid)
        }
        AllTransactionTypes.PURCHASE.value,
        AllTransactionTypes.PAY_TO_VENDOR.value,
        AllTransactionTypes.GET_FROM_CUSTOMER.value,
        AllTransactionTypes.CUSTOMER_RETURN.value -> {
            // These decrease balance (you will pay)
            transaction.totalBill - transaction.totalPaid
        }
        else -> 0.0
    }
}

