package com.hisaabi.hisaabi_kmp.home.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

/**
 * Provides dummy data for dashboard preview and testing
 */
object DashboardDummyDataProvider {
    
    private fun getDashboardSectionOptions() = listOf(
        IntervalEnum.LAST_7_DAYS,
        IntervalEnum.THIS_MONTH,
        IntervalEnum.LAST_MONTH,
        IntervalEnum.THIS_YEAR,
        IntervalEnum.ALL_RECORD
    )
    
    fun getBalanceOverview(): DashboardSectionDataModel {
        val items = listOf(
            DashboardSectionDataModel.SectionItem(
                title = "Total Balance",
                value = 125000.0,
                icon = Icons.Default.AccountBalance
            ),
            DashboardSectionDataModel.SectionItem(
                title = "Receivable",
                value = 75000.0,
                icon = Icons.Default.TrendingUp
            ),
            DashboardSectionDataModel.SectionItem(
                title = "Payable",
                value = 45000.0,
                icon = Icons.Default.TrendingDown
            ),
            DashboardSectionDataModel.SectionItem(
                title = "Net Balance",
                value = 30000.0,
                icon = Icons.Default.Wallet
            )
        )
        
        return DashboardSectionDataModel(
            title = "Balance Overview",
            sectionItems = items,
            options = getDashboardSectionOptions(),
            selectedOption = IntervalEnum.THIS_MONTH
        )
    }
    
    fun getPaymentOverview(): DashboardSectionDataModel {
        val items = listOf(
            DashboardSectionDataModel.SectionItem(
                title = "Payments Received",
                value = 85000.0,
                icon = Icons.Default.Payment
            ),
            DashboardSectionDataModel.SectionItem(
                title = "Payments Made",
                value = 52000.0,
                icon = Icons.Default.AccountBalance
            ),
            DashboardSectionDataModel.SectionItem(
                title = "Pending Received",
                value = 25000.0,
                icon = Icons.Default.PendingActions
            ),
            DashboardSectionDataModel.SectionItem(
                title = "Pending Payments",
                value = 15000.0,
                icon = Icons.Default.Warning
            )
        )
        
        return DashboardSectionDataModel(
            title = "Payment Overview",
            sectionItems = items,
            options = getDashboardSectionOptions(),
            selectedOption = IntervalEnum.THIS_MONTH
        )
    }
    
    fun getSalesOverview(): DashboardSectionDataModel {
        val items = listOf(
            DashboardSectionDataModel.SectionItem(
                title = "Total Sales",
                value = 245000.0,
                icon = Icons.Default.ShoppingCart
            ),
            DashboardSectionDataModel.SectionItem(
                title = "Total Cost",
                value = 180000.0,
                icon = Icons.Default.Paid
            ),
            DashboardSectionDataModel.SectionItem(
                title = "Total Revenue",
                value = 245000.0,
                icon = Icons.Default.AttachMoney
            ),
            DashboardSectionDataModel.SectionItem(
                title = "Total Profit",
                value = 65000.0,
                icon = Icons.Default.TrendingUp
            )
        )
        
        return DashboardSectionDataModel(
            title = "Sales Overview",
            sectionItems = items,
            options = getDashboardSectionOptions(),
            selectedOption = IntervalEnum.THIS_MONTH
        )
    }
    
    fun getPurchaseOverview(): DashboardSectionDataModel {
        val items = listOf(
            DashboardSectionDataModel.SectionItem(
                title = "No. of Purchase",
                value = 145.0,
                icon = Icons.Default.ShoppingBag
            ),
            DashboardSectionDataModel.SectionItem(
                title = "Cancel Orders",
                value = 8.0,
                icon = Icons.Default.Cancel
            ),
            DashboardSectionDataModel.SectionItem(
                title = "Total Cost",
                value = 180000.0,
                icon = Icons.Default.Paid
            ),
            DashboardSectionDataModel.SectionItem(
                title = "Returns",
                value = 5.0,
                icon = Icons.Default.AssignmentReturn
            )
        )
        
        return DashboardSectionDataModel(
            title = "Purchase Overview",
            sectionItems = items,
            options = getDashboardSectionOptions(),
            selectedOption = IntervalEnum.THIS_MONTH
        )
    }
    
    fun getInventorySummary(): DashboardSectionDataModel {
        val items = listOf(
            DashboardSectionDataModel.SectionItem(
                title = "Qty in Hand",
                value = 2450.0,
                icon = Icons.Default.Inventory2
            ),
            DashboardSectionDataModel.SectionItem(
                title = "Will be Received",
                value = 450.0,
                icon = Icons.Default.LocalShipping
            ),
            DashboardSectionDataModel.SectionItem(
                title = "To be Packed",
                value = 125.0,
                icon = Icons.Default.Backpack
            )
        )
        
        return DashboardSectionDataModel(
            title = "Inventory Summary",
            sectionItems = items,
            options = null,
            selectedOption = null
        )
    }
    
    fun getPartiesSummary(): DashboardSectionDataModel {
        val items = listOf(
            DashboardSectionDataModel.SectionItem(
                title = "Total Customers",
                value = 456.0,
                icon = Icons.Default.People
            ),
            DashboardSectionDataModel.SectionItem(
                title = "Total Suppliers",
                value = 89.0,
                icon = Icons.Default.Store
            ),
            DashboardSectionDataModel.SectionItem(
                title = "Active Parties",
                value = 423.0,
                icon = Icons.Default.CheckCircle
            )
        )
        
        return DashboardSectionDataModel(
            title = "Parties Summary",
            sectionItems = items,
            options = null,
            selectedOption = null
        )
    }
    
    fun getProductsSummary(): DashboardSectionDataModel {
        val items = listOf(
            DashboardSectionDataModel.SectionItem(
                title = "Quantity In Hand",
                value = 2450.0,
                icon = Icons.Default.Inventory
            ),
            DashboardSectionDataModel.SectionItem(
                title = "Low Stock Products",
                value = 23.0,
                icon = Icons.Default.Warning
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

