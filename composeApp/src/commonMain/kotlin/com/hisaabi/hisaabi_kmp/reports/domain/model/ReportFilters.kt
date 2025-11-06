package com.hisaabi.hisaabi_kmp.reports.domain.model

data class ReportFilters(
    val reportType: ReportType? = null,
    val additionalFilter: ReportAdditionalFilter? = null,
    val dateFilter: ReportDateFilter = ReportDateFilter.THIS_MONTH,
    val groupBy: ReportGroupBy? = null,
    val sortBy: ReportSortBy = ReportSortBy.DATE_DESC,
    val customStartDate: String? = null,
    val customEndDate: String? = null,
    val selectedPartyId: String? = null,
    val selectedProductId: String? = null,
    val selectedWarehouseId: String? = null
) {
    fun isValid(): Boolean {
        return reportType != null
    }
    
    fun requiresPartySelection(): Boolean {
        return reportType in listOf(
            ReportType.CUSTOMER_REPORT,
            ReportType.VENDOR_REPORT
        )
    }
    
    fun requiresProductSelection(): Boolean {
        return reportType == ReportType.PRODUCT_REPORT
    }
    
    fun requiresWarehouseSelection(): Boolean {
        return reportType == ReportType.WAREHOUSE_REPORT
    }
}

