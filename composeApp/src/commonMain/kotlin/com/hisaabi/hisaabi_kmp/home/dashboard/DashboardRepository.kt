package com.hisaabi.hisaabi_kmp.home.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.hisaabi.hisaabi_kmp.database.dao.*
import com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType
import kotlin.math.abs

/**
 * Dashboard repository to fetch dashboard metrics from the database
 * Based on native Android app's DashboardRepository
 */
class DashboardRepository(
    private val inventoryTransactionDao: InventoryTransactionDao,
    private val transactionDetailDao: TransactionDetailDao,
    private val partyDao: PartyDao,
    private val productDao: ProductDao,
    private val productQuantitiesDao: ProductQuantitiesDao,
    private val paymentMethodDao: PaymentMethodDao,
    private val categoryDao: CategoryDao
) {
    
    /**
     * Get balance overview section
     */
    suspend fun getBalanceOverview(
        businessSlug: String,
        interval: IntervalEnum
    ): DashboardSectionDataModel {
        val items = mutableListOf<DashboardSectionDataModel.SectionItem>()
        
        // Total Cash in Hand
        val totalCash = paymentMethodDao.getTotalCashInHand(businessSlug) ?: 0.0
        items.add(
            DashboardSectionDataModel.SectionItem(
                title = "Total Balance",
                value = totalCash,
                icon = Icons.Default.AccountBalance
            )
        )
        
        // Customer Balance (Receivable) - Use correct PartyType constants
        val customerBalance = partyDao.getTotalBalance(
            listOf(PartyType.CUSTOMER.type, PartyType.WALK_IN_CUSTOMER.type),
            businessSlug
        ) ?: 0.0
        items.add(
            DashboardSectionDataModel.SectionItem(
                title = if (customerBalance >= 0) "Customers (Payable)" else "Customers (Receivable)",
                value = abs(customerBalance),
                icon = Icons.Default.People
            )
        )
        
        // Vendor Balance (Payable) - Use correct PartyType constants
        val vendorBalance = partyDao.getTotalBalance(
            listOf(PartyType.VENDOR.type, PartyType.DEFAULT_VENDOR.type),
            businessSlug
        ) ?: 0.0
        items.add(
            DashboardSectionDataModel.SectionItem(
                title = if (vendorBalance >= 0) "Vendors (Payable)" else "Vendors (Receivable)",
                value = abs(vendorBalance),
                icon = Icons.Default.Store
            )
        )
        
        // Net Balance
        val netBalance = customerBalance + vendorBalance
        items.add(
            DashboardSectionDataModel.SectionItem(
                title = "Net Balance",
                value = abs(netBalance),
                icon = Icons.Default.Wallet
            )
        )
        
        return DashboardSectionDataModel(
            title = "Balance Overview",
            sectionItems = items,
            options = null,
            selectedOption = null
        )
    }
    
    /**
     * Get payment overview section
     */
    suspend fun getPaymentOverview(
        businessSlug: String,
        interval: IntervalEnum
    ): DashboardSectionDataModel {
        val range = DateRangeHelper.getDateRange(interval)
        val items = mutableListOf<DashboardSectionDataModel.SectionItem>()
        
        // Total Received
        val totalReceived = inventoryTransactionDao.getTotalPaid(
            businessSlug,
            range.fromMilli,
            range.toMilli,
            TransactionTypeHelper.getPaymentInTransactionTypes()
        ) ?: 0.0
        
        items.add(
            DashboardSectionDataModel.SectionItem(
                title = "Total Received",
                value = totalReceived,
                icon = Icons.Default.Payment
            )
        )
        
        // Total Paid
        val totalPaid = inventoryTransactionDao.getTotalPaid(
            businessSlug,
            range.fromMilli,
            range.toMilli,
            TransactionTypeHelper.getPaymentOutTransactionTypes()
        ) ?: 0.0
        
        items.add(
            DashboardSectionDataModel.SectionItem(
                title = "Total Paid",
                value = totalPaid,
                icon = Icons.Default.AccountBalance
            )
        )
        
        // Net Received/Paid
        val netAmount = totalReceived - totalPaid
        items.add(
            DashboardSectionDataModel.SectionItem(
                title = if (netAmount >= 0) "Net Received" else "Net Paid",
                value = abs(netAmount),
                icon = Icons.Default.AttachMoney
            )
        )
        
        return DashboardSectionDataModel(
            title = "Payment Overview",
            sectionItems = items,
            options = listOf(IntervalEnum.LAST_7_DAYS, IntervalEnum.THIS_MONTH, IntervalEnum.LAST_MONTH, IntervalEnum.THIS_YEAR, IntervalEnum.ALL_RECORD),
            selectedOption = interval
        )
    }
    
    /**
     * Get sales overview section
     */
    suspend fun getSalesOverview(
        businessSlug: String,
        interval: IntervalEnum
    ): DashboardSectionDataModel {
        val range = DateRangeHelper.getDateRange(interval)
        val items = mutableListOf<DashboardSectionDataModel.SectionItem>()
        
        val saleTypes = TransactionTypeHelper.getSaleTransactionTypes()
        
        // Total Sales Count
        val salesCount = inventoryTransactionDao.getTotalTransactionsCount(
            businessSlug,
            range.fromMilli,
            range.toMilli,
            saleTypes
        )?.toDouble() ?: 0.0
        
        items.add(
            DashboardSectionDataModel.SectionItem(
                title = "Total Sales",
                value = salesCount,
                icon = Icons.Default.ShoppingCart
            )
        )
        
        // Total Revenue
        val totalRevenue = inventoryTransactionDao.getTotalRevenue(
            businessSlug,
            range.fromMilli,
            range.toMilli,
            saleTypes
        ) ?: 0.0
        
        items.add(
            DashboardSectionDataModel.SectionItem(
                title = "Total Revenue",
                value = totalRevenue,
                icon = Icons.Default.AttachMoney
            )
        )
        
        // Total Cost
        val transactionSlugs = inventoryTransactionDao.getTransactionSlugs(
            businessSlug,
            range.fromMilli,
            range.toMilli,
            saleTypes
        )
        val totalCost = if (transactionSlugs.isNotEmpty()) {
            transactionDetailDao.calculateTotalCost(transactionSlugs) ?: 0.0
        } else 0.0
        
        items.add(
            DashboardSectionDataModel.SectionItem(
                title = "Total Cost",
                value = totalCost,
                icon = Icons.Default.Paid
            )
        )
        
        // Total Profit
        val totalProfit = if (transactionSlugs.isNotEmpty()) {
            transactionDetailDao.calculateTotalProfit(transactionSlugs) ?: 0.0
        } else 0.0
        
        items.add(
            DashboardSectionDataModel.SectionItem(
                title = "Total Profit",
                value = totalProfit,
                icon = Icons.Default.TrendingUp
            )
        )
        
        return DashboardSectionDataModel(
            title = "Sales Overview",
            sectionItems = items,
            options = listOf(IntervalEnum.LAST_7_DAYS, IntervalEnum.THIS_MONTH, IntervalEnum.LAST_MONTH, IntervalEnum.THIS_YEAR, IntervalEnum.ALL_RECORD),
            selectedOption = interval
        )
    }
    
    /**
     * Get purchase overview section
     */
    suspend fun getPurchaseOverview(
        businessSlug: String,
        interval: IntervalEnum
    ): DashboardSectionDataModel {
        val range = DateRangeHelper.getDateRange(interval)
        val items = mutableListOf<DashboardSectionDataModel.SectionItem>()
        
        val purchaseTypes = TransactionTypeHelper.getPurchaseTransactionTypes()
        
        // No. of Purchases
        val purchaseCount = inventoryTransactionDao.getTotalTransactionsCount(
            businessSlug,
            range.fromMilli,
            range.toMilli,
            purchaseTypes
        )?.toDouble() ?: 0.0
        
        items.add(
            DashboardSectionDataModel.SectionItem(
                title = "No. of Purchase",
                value = purchaseCount,
                icon = Icons.Default.ShoppingBag
            )
        )
        
        // Purchase Cost
        val purchaseCost = inventoryTransactionDao.getTotalRevenue(
            businessSlug,
            range.fromMilli,
            range.toMilli,
            purchaseTypes
        ) ?: 0.0
        
        items.add(
            DashboardSectionDataModel.SectionItem(
                title = "Purchase Cost",
                value = purchaseCost,
                icon = Icons.Default.Paid
            )
        )
        
        // Purchase Orders
        val purchaseOrderCount = inventoryTransactionDao.getTotalTransactionsCount(
            businessSlug,
            range.fromMilli,
            range.toMilli,
            TransactionTypeHelper.getPurchaseOrderTransactionTypes()
        )?.toDouble() ?: 0.0
        
        items.add(
            DashboardSectionDataModel.SectionItem(
                title = "Purchase Orders",
                value = purchaseOrderCount,
                icon = Icons.Default.Assignment
            )
        )
        
        // Returns to Vendor
        val returnCount = inventoryTransactionDao.getTotalTransactionsCount(
            businessSlug,
            range.fromMilli,
            range.toMilli,
            TransactionTypeHelper.getReturnToVendorTransactionTypes()
        )?.toDouble() ?: 0.0
        
        items.add(
            DashboardSectionDataModel.SectionItem(
                title = "Returns",
                value = returnCount,
                icon = Icons.Default.AssignmentReturn
            )
        )
        
        return DashboardSectionDataModel(
            title = "Purchase Overview",
            sectionItems = items,
            options = listOf(IntervalEnum.LAST_7_DAYS, IntervalEnum.THIS_MONTH, IntervalEnum.LAST_MONTH, IntervalEnum.THIS_YEAR, IntervalEnum.ALL_RECORD),
            selectedOption = interval
        )
    }
    
    /**
     * Get inventory summary section
     */
    suspend fun getInventorySummary(businessSlug: String): DashboardSectionDataModel {
        val items = mutableListOf<DashboardSectionDataModel.SectionItem>()
        
        // Quantity in Hand
        val qtyInHand = productQuantitiesDao.getTotalQuantityInHand(businessSlug) ?: 0.0
        items.add(
            DashboardSectionDataModel.SectionItem(
                title = "Qty in Hand",
                value = qtyInHand,
                icon = Icons.Default.Inventory2
            )
        )
        
        // Will be Received (from Purchase Orders)
        val purchaseOrderSlugs = inventoryTransactionDao.getTransactionSlugs(
            businessSlug,
            0,
            System.currentTimeMillis(),
            TransactionTypeHelper.getPurchaseOrderTransactionTypes()
        )
        val willBeReceived = if (purchaseOrderSlugs.isNotEmpty()) {
            transactionDetailDao.calculateTotalQuantity(purchaseOrderSlugs) ?: 0.0
        } else 0.0
        
        items.add(
            DashboardSectionDataModel.SectionItem(
                title = "Will be Received",
                value = willBeReceived,
                icon = Icons.Default.LocalShipping
            )
        )
        
        // Low Stock Products
        val lowStockCount = productQuantitiesDao.getLowStockCount(businessSlug)?.toDouble() ?: 0.0
        items.add(
            DashboardSectionDataModel.SectionItem(
                title = "Low Stock Products",
                value = lowStockCount,
                icon = Icons.Default.Warning
            )
        )
        
        return DashboardSectionDataModel(
            title = "Inventory Summary",
            sectionItems = items,
            options = null,
            selectedOption = null
        )
    }
    
    /**
     * Get parties summary section
     */
    suspend fun getPartiesSummary(businessSlug: String): DashboardSectionDataModel {
        val items = mutableListOf<DashboardSectionDataModel.SectionItem>()
        
        // Total Customers - Use correct PartyType constants
        val totalCustomers = partyDao.getCountByRole(PartyType.CUSTOMER.type, businessSlug)?.toDouble() ?: 0.0
        items.add(
            DashboardSectionDataModel.SectionItem(
                title = "Total Customers",
                value = totalCustomers,
                icon = Icons.Default.People
            )
        )
        
        // Total Suppliers - Use correct PartyType constants
        val totalSuppliers = partyDao.getCountByRole(PartyType.VENDOR.type, businessSlug)?.toDouble() ?: 0.0
        items.add(
            DashboardSectionDataModel.SectionItem(
                title = "Total Suppliers",
                value = totalSuppliers,
                icon = Icons.Default.Store
            )
        )
        
        // Total Investors (if exists) - Use correct PartyType constants
        val totalInvestors = partyDao.getCountByRole(PartyType.INVESTOR.type, businessSlug)?.toDouble() ?: 0.0
        if (totalInvestors > 0) {
            items.add(
                DashboardSectionDataModel.SectionItem(
                    title = "Total Investors",
                    value = totalInvestors,
                    icon = Icons.Default.AccountBalance
                )
            )
        }
        
        return DashboardSectionDataModel(
            title = "Parties Summary",
            sectionItems = items,
            options = null,
            selectedOption = null
        )
    }
    
    /**
     * Get products summary section
     */
    suspend fun getProductsSummary(businessSlug: String): DashboardSectionDataModel {
        val items = mutableListOf<DashboardSectionDataModel.SectionItem>()
        
        // Total Products
        val totalProducts = productDao.getTotalProductsCount(businessSlug)?.toDouble() ?: 0.0
        items.add(
            DashboardSectionDataModel.SectionItem(
                title = "Total Products",
                value = totalProducts,
                icon = Icons.Default.Inventory
            )
        )
        
        // Low Stock Products
        val lowStockCount = productQuantitiesDao.getLowStockCount(businessSlug)?.toDouble() ?: 0.0
        items.add(
            DashboardSectionDataModel.SectionItem(
                title = "Low Stock Products",
                value = lowStockCount,
                icon = Icons.Default.Warning
            )
        )
        
        // Product Categories
        val categoryCount = categoryDao.getTotalProductCategories(businessSlug)?.toDouble() ?: 0.0
        items.add(
            DashboardSectionDataModel.SectionItem(
                title = "Categories",
                value = categoryCount,
                icon = Icons.Default.Category
            )
        )
        
        return DashboardSectionDataModel(
            title = "Products Summary",
            sectionItems = items,
            options = null,
            selectedOption = null
        )
    }
}

