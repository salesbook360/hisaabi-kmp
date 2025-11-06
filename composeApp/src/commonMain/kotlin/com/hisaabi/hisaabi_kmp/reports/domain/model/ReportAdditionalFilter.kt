package com.hisaabi.hisaabi_kmp.reports.domain.model

enum class ReportAdditionalFilter(val id: Int, val title: String) {
    OVERALL(10, "Overall"),
    DAILY(9, "Daily"),
    WEEKLY(8, "Weekly"),
    MONTHLY(7, "Monthly"),
    YEARLY(6, "Yearly"),
    TOP_PROFIT(1, "Top by Profit"),
    TOP_CREDIT(2, "Top by Credit"),
    TOP_CASH_PAID(3, "Top Cash Transactions"),
    TOP_PURCHASED(4, "Top Purchased"),
    TOP_SOLD(5, "Top Sold"),
    CASH_IN_HAND_HISTORY(11, "Cash in Hand History"),
    CASH_IN_HAND_TYPE(12, "Cash in Hand by Type"),
    STOCK_WORTH(13, "Stock Worth"),
    CUSTOMER_DEBIT(14, "Customer Debit"),
    CUSTOMER_CREDIT(15, "Customer Credit"),
    VENDOR_DEBIT(16, "Vendor Debit"),
    VENDOR_CREDIT(17, "Vendor Credit"),
    ALL_VENDORS(18, "All Vendors"),
    ALL_CUSTOMERS(19, "All Customers"),
    LEDGER(20, "Ledger"),
    CASH_FLOW(21, "Cash Flow"),
    PROFIT_ON_AVG_PRICE(22, "Profit on Avg Price"),
    PROFIT_ON_PURCHASE_COST(23, "Profit on Purchase Cost"),
    OUT_OF_STOCK(24, "Out of Stock Products");

    companion object {
        fun fromId(id: Int): ReportAdditionalFilter? = entries.find { it.id == id }
        
        fun getFiltersForReportType(reportType: ReportType): List<ReportAdditionalFilter> {
            return when (reportType) {
                ReportType.SALE_REPORT, ReportType.PURCHASE_REPORT, 
                ReportType.EXPENSE_REPORT, ReportType.EXTRA_INCOME_REPORT -> 
                    listOf(OVERALL, DAILY, WEEKLY, MONTHLY, YEARLY)
                
                ReportType.TOP_PRODUCTS -> 
                    listOf(TOP_SOLD, TOP_PROFIT, TOP_PURCHASED)
                
                ReportType.TOP_CUSTOMERS -> 
                    listOf(TOP_PROFIT, TOP_CREDIT, TOP_CASH_PAID)
                
                ReportType.STOCK_REPORT -> 
                    listOf(STOCK_WORTH, OUT_OF_STOCK)
                
                ReportType.CASH_IN_HAND -> 
                    listOf(CASH_IN_HAND_HISTORY, CASH_IN_HAND_TYPE)
                
                ReportType.BALANCE_REPORT -> 
                    listOf(ALL_CUSTOMERS, ALL_VENDORS, CUSTOMER_DEBIT, CUSTOMER_CREDIT, VENDOR_DEBIT, VENDOR_CREDIT)
                
                ReportType.PROFIT_LOSS_REPORT, ReportType.PROFIT_LOSS_BY_PURCHASE -> 
                    listOf(OVERALL, DAILY, WEEKLY, MONTHLY, YEARLY, PROFIT_ON_AVG_PRICE, PROFIT_ON_PURCHASE_COST)
                
                else -> emptyList()
            }
        }
    }
}

