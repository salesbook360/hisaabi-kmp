package com.hisaabi.hisaabi_kmp.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun HomeScreen(
    onNavigateToAuth: () -> Unit = {},
    onNavigateToQuantityUnits: () -> Unit = {},
    onNavigateToTransactionSettings: () -> Unit = {},
    onNavigateToReceiptSettings: () -> Unit = {},
    onNavigateToDashboardSettings: () -> Unit = {},
    onNavigateToTemplates: () -> Unit = {},
    onNavigateToUpdateProfile: () -> Unit = {},
    onNavigateToParties: (com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment) -> Unit = {},
    onNavigateToProducts: () -> Unit = {},
    onNavigateToAddProduct: (com.hisaabi.hisaabi_kmp.products.domain.model.ProductType) -> Unit = {},
    onNavigateToPaymentMethods: () -> Unit = {},
    onNavigateToWarehouses: () -> Unit = {},
    onNavigateToMyBusiness: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToAddRecord: () -> Unit = {},
    onNavigateToPayGetCash: () -> Unit = {},
    onNavigateToExpense: () -> Unit = {},
    onNavigateToExtraIncome: () -> Unit = {},
    onNavigateToPaymentTransfer: () -> Unit = {},
    onNavigateToJournalVoucher: () -> Unit = {},
    onNavigateToAddTransaction: (com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionType) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomNavigationItem.entries.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> DashboardScreen()
                1 -> HomeMenuScreen(
                    onNavigateToParties = onNavigateToParties,
                    onNavigateToProducts = onNavigateToProducts,
                    onNavigateToAddProduct = onNavigateToAddProduct,
                    onNavigateToPaymentMethods = onNavigateToPaymentMethods,
                    onNavigateToWarehouses = onNavigateToWarehouses,
                    onNavigateToMyBusiness = onNavigateToMyBusiness,
                    onNavigateToTransactions = onNavigateToTransactions,
                    onNavigateToAddRecord = onNavigateToAddRecord,
                    onNavigateToPayGetCash = onNavigateToPayGetCash,
                    onNavigateToExpense = onNavigateToExpense,
                    onNavigateToExtraIncome = onNavigateToExtraIncome,
                    onNavigateToPaymentTransfer = onNavigateToPaymentTransfer,
                    onNavigateToJournalVoucher = onNavigateToJournalVoucher,
                    onNavigateToAddTransaction = onNavigateToAddTransaction
                )
                2 -> MoreScreen(
                    onNavigateToAuth = onNavigateToAuth,
                    onNavigateToQuantityUnits = onNavigateToQuantityUnits,
                    onNavigateToTransactionSettings = onNavigateToTransactionSettings,
                    onNavigateToReceiptSettings = onNavigateToReceiptSettings,
                    onNavigateToDashboardSettings = onNavigateToDashboardSettings,
                    onNavigateToTemplates = onNavigateToTemplates,
                    onNavigateToUpdateProfile = onNavigateToUpdateProfile
                )
            }
        }
    }
}

enum class BottomNavigationItem(
    val title: String,
    val icon: ImageVector
) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    HOME("Home", Icons.Default.Home),
    MORE("More", Icons.Default.MoreVert)
}

