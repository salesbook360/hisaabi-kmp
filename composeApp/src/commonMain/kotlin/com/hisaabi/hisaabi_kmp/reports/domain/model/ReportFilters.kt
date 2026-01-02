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
    val selectedWarehouseId: String? = null,
    val selectedInvestorId: String? = null
) {
    fun isValid(): Boolean {
        return reportType != null
    }
    
    /**
     * Checks if the report requires any entity selection
     */
    fun requiresEntitySelection(): Boolean {
        return requiresPartySelection() || 
               requiresProductSelection() || 
               requiresWarehouseSelection() || 
               requiresInvestorSelection()
    }
    
    /**
     * Checks if the report requires party selection (Customer or Vendor)
     */
    fun requiresPartySelection(): Boolean {
        return reportType in listOf(
            ReportType.CUSTOMER_REPORT,
            ReportType.VENDOR_REPORT
        )
    }
    
    /**
     * Checks if the report requires investor selection
     */
    fun requiresInvestorSelection(): Boolean {
        return reportType == ReportType.INVESTOR_REPORT
    }
    
    /**
     * Checks if the report requires product selection
     */
    fun requiresProductSelection(): Boolean {
        return reportType == ReportType.PRODUCT_REPORT
    }
    
    /**
     * Checks if the report requires warehouse selection
     */
    fun requiresWarehouseSelection(): Boolean {
        return reportType == ReportType.WAREHOUSE_REPORT
    }
    
    /**
     * Gets the type of entity selection needed (for navigation purposes)
     */
    fun getRequiredEntityType(): RequiredEntityType? {
        return when {
            requiresWarehouseSelection() -> RequiredEntityType.WAREHOUSE
            requiresProductSelection() -> RequiredEntityType.PRODUCT
            requiresInvestorSelection() -> RequiredEntityType.INVESTOR
            requiresPartySelection() -> when (reportType) {
                ReportType.CUSTOMER_REPORT -> RequiredEntityType.CUSTOMER
                ReportType.VENDOR_REPORT -> RequiredEntityType.VENDOR
                else -> null
            }
            else -> null
        }
    }
    
    /**
     * Checks if the required entity has been selected
     */
    fun hasRequiredEntitySelected(): Boolean {
        return when (getRequiredEntityType()) {
            RequiredEntityType.WAREHOUSE -> selectedWarehouseId != null
            RequiredEntityType.PRODUCT -> selectedProductId != null
            RequiredEntityType.INVESTOR -> selectedInvestorId != null
            RequiredEntityType.CUSTOMER, RequiredEntityType.VENDOR -> selectedPartyId != null
            null -> true // No entity selection required
        }
    }
}

/**
 * Enum to represent the type of entity selection required for a report
 */
enum class RequiredEntityType {
    WAREHOUSE,
    PRODUCT,
    CUSTOMER,
    VENDOR,
    INVESTOR
}

