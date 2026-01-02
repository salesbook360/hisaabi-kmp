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
    STOCK_REPORT(3, "Stock Report", Icons.Default.Inventory, "Current stock levels"),
    WAREHOUSE_REPORT(4, "Warehouse Report", Icons.Default.Warehouse, "Warehouse analytics"),
    PROFIT_LOSS_REPORT(5, "Profit & Loss", Icons.Default.TrendingUp, "P&L statement"),
    PROFIT_LOSS_BY_PURCHASE(6, "P&L by Purchase Cost", Icons.Default.Calculate, "P&L based on purchase cost"),
    TOP_PRODUCTS(7, "Top Products", Icons.Default.Star, "Best performing products"),
    TOP_CUSTOMERS(8, "Top Customers", Icons.Default.People, "Best customers"),
    PRODUCT_REPORT(9, "Product Report", Icons.Default.Inventory2, "Product analytics"),
    CUSTOMER_REPORT(10, "Customer Report", Icons.Default.PersonSearch, "Customer analytics"),
    VENDOR_REPORT(11, "Vendor Report", Icons.Default.Store, "Vendor analytics"),
    INVESTOR_REPORT(12, "Investor Report", Icons.Default.TrendingUp, "Investor analytics"),
    BALANCE_REPORT(13, "Balance Report", Icons.Default.AccountBalanceWallet, "Account balances"),
    EXPENSE_REPORT(14, "Expense Report", Icons.Default.MoneyOff, "View expense details"),
    EXTRA_INCOME_REPORT(15, "Extra Income Report", Icons.Default.AttachMoney, "View additional income"),
    CASH_IN_HAND(16, "Cash in Hand", Icons.Default.AccountBalance, "Current cash position"),
    BALANCE_SHEET(17, "Balance Sheet", Icons.Default.Description, "Financial position");

    companion object {
        fun fromId(id: Int): ReportType? = entries.find { it.id == id }
    }
}

