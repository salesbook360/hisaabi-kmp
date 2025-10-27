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
    onNavigateToAddTransaction: (AllTransactionTypes) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 12.dp,
                top = 16.dp,
                end = 12.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Add New Transaction Section
            item(span = { GridItemSpan(4) }) {
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
            item(span = { GridItemSpan(4) }) {
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
                            "My Business" -> onNavigateToMyBusiness()
                            else -> { /* Handle other clicks */ }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun HomeGridCell(
    option: MenuOption,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = option.title,
                modifier = Modifier.size(30.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = option.title,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
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

