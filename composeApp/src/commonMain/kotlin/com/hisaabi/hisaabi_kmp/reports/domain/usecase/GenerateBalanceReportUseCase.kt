package com.hisaabi.hisaabi_kmp.reports.domain.usecase

import com.hisaabi.hisaabi_kmp.business.data.datasource.BusinessPreferencesDataSource
import com.hisaabi.hisaabi_kmp.categories.domain.model.CategoryType
import com.hisaabi.hisaabi_kmp.database.dao.CategoryDao
import com.hisaabi.hisaabi_kmp.database.dao.PartyDao
import com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType
import com.hisaabi.hisaabi_kmp.reports.domain.model.*
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlin.math.abs

/**
 * Use case for generating Balance Report
 * Based on the legacy Android Native BalanceReportGenerator
 */
class GenerateBalanceReportUseCase(
    private val partyDao: PartyDao,
    private val categoryDao: CategoryDao,
    private val businessPreferences: BusinessPreferencesDataSource,
    private val preferencesManager: PreferencesManager
) {
    
    suspend fun execute(filters: ReportFilters): ReportResult {
        val currencySymbol = preferencesManager.getSelectedCurrency().symbol
        val businessSlug = businessPreferences.observeSelectedBusinessSlug().first()
            ?: throw IllegalStateException("No business selected")
        
        // Get all parties for the business
        val allParties = partyDao.getPartiesByBusiness(businessSlug).first()
        
        // Filter parties based on additional filter
        val filteredParties = filterParties(allParties, filters.additionalFilter)
        
        // Group parties if grouping is specified
        val groupedData = if (filters.groupBy != null) {
            groupParties(filteredParties, filters.groupBy, businessSlug)
        } else {
            // No grouping - use parties directly
            filteredParties.map { party ->
                BalanceReportData(
                    title = party.name?:"",
                    balance = party.balance,
                    entityId = party.slug?:""
                )
            }
        }
        
        // Sort the data
        val sortedData = sortBalanceData(groupedData, filters.sortBy)
        
        // Generate rows
        val columns = listOf("Name", "Current Balance")
        val rows = sortedData.map { data ->
            ReportRow(
                id = data.entityId,
                values = listOf(
                    data.title,
                    formatBalance(data.balance, currencySymbol)
                )
            )
        }
        
        // Calculate total cash (sum of all balances)
        val totalCash = sortedData.sumOf { it.balance }
        
        val summary = ReportSummary(
            totalAmount = totalCash,
            recordCount = rows.size,
            additionalInfo = mapOf(
                "Total Balance" to String.format("%,.2f", totalCash)
            )
        )
        
        return ReportResult(
            reportType = ReportType.BALANCE_REPORT,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = summary
        )
    }
    
    /**
     * Filter parties based on the selected additional filter
     */
    private fun filterParties(
        parties: List<com.hisaabi.hisaabi_kmp.database.entity.PartyEntity>,
        additionalFilter: ReportAdditionalFilter?
    ): List<com.hisaabi.hisaabi_kmp.database.entity.PartyEntity> {
        val minimumValueToIgnore = 0.0 // Can be adjusted based on decimal places
        
        return parties.filter { party ->
            when (additionalFilter) {
                ReportAdditionalFilter.ALL_CUSTOMERS -> {
                    // Show all customers
                    party.role_id == PartyType.CUSTOMER.type || party.role_id == PartyType.WALK_IN_CUSTOMER.type
                }
                ReportAdditionalFilter.ALL_VENDORS -> {
                    // Show all vendors
                    party.role_id == PartyType.VENDOR.type || party.role_id == PartyType.DEFAULT_VENDOR.type
                }
                ReportAdditionalFilter.CUSTOMER_DEBIT -> {
                    // Customers with positive balance (they owe us)
                    (party.role_id == PartyType.CUSTOMER.type || party.role_id == PartyType.WALK_IN_CUSTOMER.type) &&
                    party.balance > minimumValueToIgnore
                }
                ReportAdditionalFilter.CUSTOMER_CREDIT -> {
                    // Customers with negative balance (we owe them)
                    (party.role_id == PartyType.CUSTOMER.type || party.role_id == PartyType.WALK_IN_CUSTOMER.type) &&
                    party.balance < -minimumValueToIgnore
                }
                ReportAdditionalFilter.VENDOR_DEBIT -> {
                    // Vendors with positive balance (we owe them)
                    (party.role_id == PartyType.VENDOR.type || party.role_id == PartyType.DEFAULT_VENDOR.type) &&
                    party.balance > minimumValueToIgnore
                }
                ReportAdditionalFilter.VENDOR_CREDIT -> {
                    // Vendors with negative balance (they owe us)
                    (party.role_id == PartyType.VENDOR.type || party.role_id == PartyType.DEFAULT_VENDOR.type) &&
                    party.balance < -minimumValueToIgnore
                }
                else -> false
            }
        }
    }
    
    /**
     * Group parties by area or category
     */
    private suspend fun groupParties(
        parties: List<com.hisaabi.hisaabi_kmp.database.entity.PartyEntity>,
        groupBy: ReportGroupBy,
        businessSlug: String
    ): List<BalanceReportData> {
        val groupedMap = mutableMapOf<String, BalanceReportData>()
        
        // Get areas for lookup (areas are categories with typeId = 2)
        val areas = categoryDao.getCategoriesByTypeAndBusiness(CategoryType.AREA.type, businessSlug)
        val areaMap = areas.associateBy { it.slug ?: "" }
        
        // Get categories for lookup (party categories are categories with typeId = 3)
        val categories = categoryDao.getCategoriesByTypeAndBusiness(CategoryType.CUSTOMER_CATEGORY.type, businessSlug)
        val categoryMap = categories.associateBy { it.slug ?: "" }
        
        parties.forEach { party ->
            val groupKey = when (groupBy) {
                ReportGroupBy.PARTY_AREA -> {
                    party.area_slug ?: "No Area"
                }
                ReportGroupBy.PARTY_CATEGORY -> {
                    party.category_slug ?: "No Category"
                }
                else -> party.slug ?: ""
            }
            
            val groupTitle = when (groupBy) {
                ReportGroupBy.PARTY_AREA -> {
                    // Get area name from areaMap
                    areaMap[party.area_slug]?.title ?: (party.area_slug ?: "No Area")
                }
                ReportGroupBy.PARTY_CATEGORY -> {
                    // Get category name from categoryMap
                    categoryMap[party.category_slug]?.title ?: (party.category_slug ?: "No Category")
                }
                else -> party.name ?: ""
            }
            
            if (groupedMap.containsKey(groupKey)) {
                groupedMap[groupKey] = groupedMap[groupKey]!!.copy(
                    balance = groupedMap[groupKey]!!.balance + party.balance
                )
            } else {
                groupedMap[groupKey] = BalanceReportData(
                    title = groupTitle,
                    balance = party.balance,
                    entityId = groupKey
                )
            }
        }
        
        return groupedMap.values.toList()
    }
    
    /**
     * Sort balance data based on sort option
     */
    private fun sortBalanceData(
        data: List<BalanceReportData>,
        sortBy: ReportSortBy?
    ): List<BalanceReportData> {
        return when (sortBy) {
            ReportSortBy.TITLE_ASC -> {
                data.sortedBy { it.title }
            }
            ReportSortBy.TITLE_DESC -> {
                data.sortedByDescending { it.title }
            }
            ReportSortBy.BALANCE_ASC -> {
                data.sortedBy { it.balance }
            }
            ReportSortBy.BALANCE_DESC -> {
                data.sortedByDescending { it.balance }
            }
            else -> {
                // Default: sort by title ascending
                data.sortedBy { it.title }
            }
        }
    }
    
    /**
     * Format balance with debit/credit indication
     * Positive balance = debit (they owe us / we owe them)
     * Negative balance = credit (we owe them / they owe us)
     */
    private fun formatBalance(balance: Double, currencySymbol: String): String {
        return "$currencySymbol ${String.format("%,.2f", balance)}"
    }
    
    /**
     * Data class to hold balance report data
     */
    private data class BalanceReportData(
        val title: String,
        var balance: Double,
        val entityId: String
    )
}

