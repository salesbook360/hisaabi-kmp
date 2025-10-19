package com.hisaabi.hisaabi_kmp.settings.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DashboardConfig(
    val balanceOverview: BalanceOverviewConfig = BalanceOverviewConfig.DEFAULT,
    val paymentOverview: PaymentOverviewConfig = PaymentOverviewConfig.DEFAULT,
    val salesOverview: SalesOverviewConfig = SalesOverviewConfig.DEFAULT,
    val purchaseOverview: PurchaseOverviewConfig = PurchaseOverviewConfig.DEFAULT,
    val inventorySummary: InventorySummaryConfig = InventorySummaryConfig.DEFAULT,
    val partiesSummary: PartiesSummaryConfig = PartiesSummaryConfig.DEFAULT,
    val productsSummary: ProductsSummaryConfig = ProductsSummaryConfig.DEFAULT,
    val profitLossGraph: ProfitLossGraphConfig = ProfitLossGraphConfig.DEFAULT,
    val salePurchaseGraph: SalePurchaseGraphConfig = SalePurchaseGraphConfig.DEFAULT
) {
    companion object {
        val DEFAULT = DashboardConfig()
    }
}

// Balance Overview Section
@Serializable
data class BalanceOverviewConfig(
    val showSection: Boolean = true,
    val showTotalAvailable: Boolean = true,
    val showCustomersBalance: Boolean = true,
    val showVendorBalance: Boolean = true,
    val showInvestorBalance: Boolean = true
) {
    companion object {
        val DEFAULT = BalanceOverviewConfig()
    }
}

// Payment Overview Section
@Serializable
data class PaymentOverviewConfig(
    val showSection: Boolean = true,
    val showTotalReceived: Boolean = true,
    val showTotalPaid: Boolean = true,
    val showNetPaidReceived: Boolean = true
) {
    companion object {
        val DEFAULT = PaymentOverviewConfig()
    }
}

// Sales Overview Section
@Serializable
data class SalesOverviewConfig(
    val showSection: Boolean = true,
    val showTotalSaleOrders: Boolean = true,
    val showTotalSales: Boolean = true,
    val showTotalRevenue: Boolean = true,
    val showTotalCost: Boolean = true,
    val showAmountReceived: Boolean = true,
    val showTaxReceived: Boolean = true,
    val showProfit: Boolean = true
) {
    companion object {
        val DEFAULT = SalesOverviewConfig()
    }
}

// Purchase Overview Section
@Serializable
data class PurchaseOverviewConfig(
    val showSection: Boolean = true,
    val showNoOfPurchases: Boolean = true,
    val showPurchaseCost: Boolean = true,
    val showPurchaseOrders: Boolean = true,
    val showReturnedToVendor: Boolean = true,
    val showTaxPaid: Boolean = true
) {
    companion object {
        val DEFAULT = PurchaseOverviewConfig()
    }
}

// Inventory Summary Section
@Serializable
data class InventorySummaryConfig(
    val showSection: Boolean = true,
    val showQuantityInHand: Boolean = true,
    val showQuantityWillBeReceived: Boolean = true
) {
    companion object {
        val DEFAULT = InventorySummaryConfig()
    }
}

// Parties Summary Section
@Serializable
data class PartiesSummaryConfig(
    val showSection: Boolean = true,
    val showTotalCustomers: Boolean = true,
    val showTotalSuppliers: Boolean = true,
    val showTotalInvestors: Boolean = true
) {
    companion object {
        val DEFAULT = PartiesSummaryConfig()
    }
}

// Products Summary Section
@Serializable
data class ProductsSummaryConfig(
    val showSection: Boolean = true,
    val showTotalProducts: Boolean = true,
    val showTotalProductCategories: Boolean = true,
    val showTotalRecipes: Boolean = true,
    val showLowStockProducts: Boolean = true
) {
    companion object {
        val DEFAULT = ProductsSummaryConfig()
    }
}

// Profit/Loss Graph Section
@Serializable
data class ProfitLossGraphConfig(
    val showSection: Boolean = true
) {
    companion object {
        val DEFAULT = ProfitLossGraphConfig()
    }
}

// Sale/Purchase Graph Section
@Serializable
data class SalePurchaseGraphConfig(
    val showSection: Boolean = true
) {
    companion object {
        val DEFAULT = SalePurchaseGraphConfig()
    }
}

