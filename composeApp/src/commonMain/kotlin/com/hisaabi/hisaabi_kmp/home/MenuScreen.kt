package com.hisaabi.hisaabi_kmp.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaabi.hisaabi_kmp.core.ui.LocalWindowSizeClass
import com.hisaabi.hisaabi_kmp.core.ui.WindowWidthSizeClass
import com.hisaabi.hisaabi_kmp.core.ui.adaptiveGridCells
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMenuScreen(
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
    onNavigateToReports: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                windowInsets = WindowInsets(0)
            )
        }
    ) { paddingValues ->
        val windowSizeClass = LocalWindowSizeClass.current
        val gridCells = adaptiveGridCells(
            compactColumns = 4,
            mediumColumns = 6,
            expandedMinItemWidth = 110.dp
        )
        
        // Calculate column count for span
        val columnCount = when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.COMPACT -> 4
            WindowWidthSizeClass.MEDIUM -> 6
            WindowWidthSizeClass.EXPANDED -> 8 // approximate for adaptive
        }
        
        // Adaptive padding based on screen size
        val horizontalPadding = when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.COMPACT -> 12.dp
            WindowWidthSizeClass.MEDIUM -> 24.dp
            WindowWidthSizeClass.EXPANDED -> 48.dp
        }
        
        // Center content on large screens
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier.widthIn(max = windowSizeClass.maxContentWidth)
            ) {
                LazyVerticalGrid(
                    columns = gridCells,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = horizontalPadding,
                        top = 16.dp,
                        end = horizontalPadding,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
            // Add New Transaction Section
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Add New Transaction",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(newTransactionOptions.size) { index ->
                val option = newTransactionOptions[index]
                HomeGridCell(
                    option = option,
                    onClick = {
                        when (option.title) {
                            "New Record" -> onNavigateToAddRecord()
                            "Payment In/Out" -> onNavigateToPayGetCash()
                            "Expense" -> onNavigateToExpense()
                            "Extra Income" -> onNavigateToExtraIncome()
                            "Payment Transfer" -> onNavigateToPaymentTransfer()
                            "Journal Voucher" -> onNavigateToJournalVoucher()
                            "Stock Adjustment" -> onNavigateToStockAdjustment()
                            "Manufacture" -> onNavigateToManufacture()
                            "Sale" -> onNavigateToAddTransaction(com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes.SALE)
                            "Sale Order" -> onNavigateToAddTransaction(com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes.SALE_ORDER)
                            "Purchase" -> onNavigateToAddTransaction(com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes.PURCHASE)
                            "Purchase Order" -> onNavigateToAddTransaction(com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes.PURCHASE_ORDER)
                            "Customer Return" -> onNavigateToAddTransaction(com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes.CUSTOMER_RETURN)
                            "Vendor Return" -> onNavigateToAddTransaction(com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes.VENDOR_RETURN)
                            "Quotation" -> onNavigateToAddTransaction(com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes.QUOTATION)
                            else -> { /* Handle other transaction types later */ }
                        }
                    }
                )
            }
            
            // Other Options Section
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Other Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(otherOptions.size) { index ->
                val option = otherOptions[index]
                HomeGridCell(
                    option = option,
                    onClick = {
                        when (option.title) {
                            "Transactions" -> onNavigateToTransactions()
                            "Customers" -> onNavigateToParties(com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.CUSTOMER)
                            "Vendors" -> onNavigateToParties(com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.VENDOR)
                            "Investors" -> onNavigateToParties(com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.INVESTOR)
                            "Products" -> onNavigateToProducts(null) // Show all products
                            "Services" -> onNavigateToProducts(com.hisaabi.hisaabi_kmp.products.domain.model.ProductType.SERVICE)
                            "Recipes" -> onNavigateToProducts(com.hisaabi.hisaabi_kmp.products.domain.model.ProductType.RECIPE)
                            "Payment Methods" -> onNavigateToPaymentMethods()
                            "Warehouse" -> onNavigateToWarehouses()
                            "Reports" -> onNavigateToReports()
                            "My Business" -> onNavigateToMyBusiness()
                            else -> { /* Handle other clicks */ }
                        }
                    }
                )
            }
                }
            }
        }
    }
}

@Composable
fun HomeGridCell(
    option: MenuOption,
    onClick: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val isDesktop = windowSizeClass.widthSizeClass == WindowWidthSizeClass.EXPANDED
    
    // Adaptive sizing for desktop
    val iconSize = if (isDesktop) 36.dp else 30.dp
    val fontSize = if (isDesktop) 12.sp else 10.sp
    val padding = if (isDesktop) 12.dp else 8.dp
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = option.title,
                modifier = Modifier.size(iconSize),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(if (isDesktop) 8.dp else 6.dp))
            Text(
                text = option.title,
                style = MaterialTheme.typography.bodySmall,
                fontSize = fontSize,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

data class MenuOption(
    val title: String,
    val icon: ImageVector
)

// Add New Transaction Options (based on native app)
val newTransactionOptions = listOf(
    MenuOption("New Record", Icons.Default.Note),
    MenuOption("Sale", Icons.Default.ShoppingCart),
    MenuOption("Sale Order", Icons.Default.Assignment),
    MenuOption("Purchase", Icons.Default.ShoppingBag),
    MenuOption("Purchase Order", Icons.Default.ShoppingBasket),
    MenuOption("Customer Return", Icons.Default.AssignmentReturn),
    MenuOption("Vendor Return", Icons.Default.Undo),
    MenuOption("Payment In/Out", Icons.Default.Payment),
    MenuOption("Payment Transfer", Icons.Default.SwapHoriz),
    MenuOption("Journal Voucher", Icons.Default.AccountBalance),
    MenuOption("Expense", Icons.Default.MoneyOff),
    MenuOption("Extra Income", Icons.Default.AttachMoney),
    MenuOption("Quotation", Icons.Default.Description),
    MenuOption("Stock Adjustment", Icons.Default.Tune),
    MenuOption("Manufacture", Icons.Default.Build)
)

// Other Options (based on native app)
val otherOptions = listOf(
    MenuOption("Transactions", Icons.Default.Receipt),
    MenuOption("Customers", Icons.Default.People),
    MenuOption("Vendors", Icons.Default.Store),
    MenuOption("Investors", Icons.Default.TrendingUp),
    MenuOption("Products", Icons.Default.Inventory2),
    MenuOption("Services", Icons.Default.Build),
    MenuOption("Recipes", Icons.Default.Restaurant),
    MenuOption("Warehouse", Icons.Default.Warehouse),
    MenuOption("Reports", Icons.Default.BarChart),
    MenuOption("Payment Methods", Icons.Default.Payment),
    MenuOption("My Business", Icons.Default.Business)
)

