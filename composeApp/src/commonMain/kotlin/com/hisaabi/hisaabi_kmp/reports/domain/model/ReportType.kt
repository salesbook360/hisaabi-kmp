package com.hisaabi.hisaabi_kmp.reports.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class ReportType(
    val id: Int,
    val title: String,
    val icon: ImageVector,
    val description: String
) {
    SALE_REPORT(1, "Sale Report", Icons.Default.ShoppingCart, "View sales analytics"),
    PURCHASE_REPORT(2, "Purchase Report", Icons.Default.ShoppingBag, "View purchase analytics"),
    EXPENSE_REPORT(3, "Expense Report", Icons.Default.MoneyOff, "View expense details"),
    EXTRA_INCOME_REPORT(4, "Extra Income Report", Icons.Default.AttachMoney, "View additional income"),
    TOP_PRODUCTS(5, "Top Products", Icons.Default.Star, "Best performing products"),
    TOP_CUSTOMERS(6, "Top Customers", Icons.Default.People, "Best customers"),
    STOCK_REPORT(7, "Stock Report", Icons.Default.Inventory, "Current stock levels"),
    PRODUCT_REPORT(8, "Product Report", Icons.Default.Inventory2, "Product analytics"),
    CUSTOMER_REPORT(9, "Customer Report", Icons.Default.PersonSearch, "Customer analytics"),
    VENDOR_REPORT(10, "Vendor Report", Icons.Default.Store, "Vendor analytics"),
    PROFIT_LOSS_REPORT(11, "Profit & Loss", Icons.Default.TrendingUp, "P&L statement"),
    CASH_IN_HAND(12, "Cash in Hand", Icons.Default.AccountBalance, "Current cash position"),
    BALANCE_REPORT(13, "Balance Report", Icons.Default.AccountBalanceWallet, "Account balances"),
    PROFIT_LOSS_BY_PURCHASE(17, "P&L by Purchase Cost", Icons.Default.Calculate, "P&L based on purchase cost"),
    BALANCE_SHEET(18, "Balance Sheet", Icons.Default.Description, "Financial position"),
    INVESTOR_REPORT(19, "Investor Report", Icons.Default.TrendingUp, "Investor analytics"),
    WAREHOUSE_REPORT(20, "Warehouse Report", Icons.Default.Warehouse, "Warehouse analytics");

    companion object {
        fun fromId(id: Int): ReportType? = entries.find { it.id == id }
    }
}

