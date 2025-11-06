package com.hisaabi.hisaabi_kmp.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import com.hisaabi.hisaabi_kmp.reports.presentation.ReportsScreen

@Composable
fun HomeScreen(
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    onNavigateToQuantityUnits: () -> Unit = {},
    onNavigateToTransactionSettings: () -> Unit = {},
    onNavigateToReceiptSettings: () -> Unit = {},
    onNavigateToDashboardSettings: () -> Unit = {},
    onNavigateToTemplates: () -> Unit = {},
    onNavigateToUpdateProfile: () -> Unit = {},
    onNavigateToBusinessSelection: () -> Unit = {},
    onNavigateToParties: (com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment) -> Unit = {},
    onNavigateToProducts: (com.hisaabi.hisaabi_kmp.products.domain.model.ProductType?) -> Unit = {},
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
    onNavigateToStockAdjustment: () -> Unit = {},
    onNavigateToManufacture: () -> Unit = {},
    onNavigateToAddTransaction: (AllTransactionTypes) -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onReportTypeSelected: (com.hisaabi.hisaabi_kmp.reports.domain.model.ReportType) -> Unit = {}
) {
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomNavigationItem.entries.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) }
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
                    onNavigateToStockAdjustment = onNavigateToStockAdjustment,
                    onNavigateToManufacture = onNavigateToManufacture,
                    onNavigateToAddTransaction = onNavigateToAddTransaction,
                    onNavigateToReports = onNavigateToReports
                )
                2 -> MoreScreen(
                    onNavigateToAuth = onNavigateToAuth,
                    onNavigateToQuantityUnits = onNavigateToQuantityUnits,
                    onNavigateToTransactionSettings = onNavigateToTransactionSettings,
                    onNavigateToReceiptSettings = onNavigateToReceiptSettings,
                    onNavigateToDashboardSettings = onNavigateToDashboardSettings,
                    onNavigateToTemplates = onNavigateToTemplates,
                    onNavigateToUpdateProfile = onNavigateToUpdateProfile,
                    onNavigateToBusinessSelection = onNavigateToBusinessSelection,
                    onNavigateToReports = onNavigateToReports
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

@Composable
fun MainAppWithBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomNavigationItem.entries.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            content()
        }
    }
}

