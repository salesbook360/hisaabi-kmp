package com.hisaabi.hisaabi_kmp.reports.domain.usecase

import com.hisaabi.hisaabi_kmp.business.data.datasource.BusinessPreferencesDataSource
import com.hisaabi.hisaabi_kmp.database.dao.InventoryTransactionDao
import com.hisaabi.hisaabi_kmp.database.dao.PartyDao
import com.hisaabi.hisaabi_kmp.database.dao.TransactionDetailDao
import com.hisaabi.hisaabi_kmp.reports.domain.model.*
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*

/**
 * Use case for generating Top Customers report
 * Based on the legacy Android Native TopCustomersReportGenerator
 */
class GenerateTopCustomersReportUseCase(
    private val transactionDao: InventoryTransactionDao,
    private val transactionDetailDao: TransactionDetailDao,
    private val partyDao: PartyDao,
    private val businessPreferences: BusinessPreferencesDataSource,
    private val preferencesManager: PreferencesManager
) {
    
    suspend fun execute(filters: ReportFilters): ReportResult {
        val currencySymbol = preferencesManager.getSelectedCurrency().symbol
        val businessSlug = businessPreferences.observeSelectedBusinessSlug().first()
            ?: throw IllegalStateException("No business selected")
        
        // Calculate date range based on filters
        val (fromDateMillis, toDateMillis) = calculateDateRange(filters)
        
        // Get all customer-related transactions
        val transactionTypes = listOf(
            AllTransactionTypes.SALE.value,
            AllTransactionTypes.CUSTOMER_RETURN.value,
            AllTransactionTypes.GET_FROM_CUSTOMER.value,
            AllTransactionTypes.PAY_TO_CUSTOMER.value
        )
        
        val transactions = transactionDao.getTransactionsForReport(
            businessSlug = businessSlug,
            transactionTypes = transactionTypes,
            fromDate = fromDateMillis,
            toDate = toDateMillis
        )
        
        // Get parties for lookup
        val parties = partyDao.getPartiesByBusiness(businessSlug).first()
        val partyMap = parties.associateBy { it.slug ?: "" }
        
        // Create customer stats
        val customerStatsMap = mutableMapOf<String, CustomerStats>()
        
        transactions.forEach { transaction ->
            val customerSlug = transaction.party_slug ?: ""
            if (customerSlug.isEmpty()) return@forEach
            
            val stats = customerStatsMap.getOrPut(customerSlug) {
                CustomerStats(customerSlug)
            }
            
            when (transaction.transaction_type) {
                AllTransactionTypes.SALE.value -> {
                    stats.totalPurchase += transaction.total_bill
                    stats.totalPaid += transaction.total_paid
                    stats.totalDiscount += transaction.flat_discount
                    stats.totalAdditionalCharges += transaction.additional_charges
                    stats.totalTaxPaid += transaction.flat_tax
                }
                AllTransactionTypes.CUSTOMER_RETURN.value -> {
                    stats.totalPurchase -= transaction.total_bill
                    stats.totalPaid -= transaction.total_paid
                    stats.totalDiscount -= transaction.flat_discount
                    stats.totalAdditionalCharges -= transaction.additional_charges
                    stats.totalTaxPaid -= transaction.flat_tax
                }
                AllTransactionTypes.GET_FROM_CUSTOMER.value -> {
                    stats.totalPaid += transaction.total_paid
                }
                AllTransactionTypes.PAY_TO_CUSTOMER.value -> {
                    stats.totalPaid -= transaction.total_paid
                }
            }
            
            transaction.slug?.let { stats.transactions.add(it) }
        }
        
        // Convert to list and sort based on filter
        val customerStatsList = customerStatsMap.values.toList()
        val sortedStats = when (filters.additionalFilter) {
            ReportAdditionalFilter.TOP_PROFIT -> {
                // TOP_PROFIT sorts by totalPurchase (total sale amount)
                customerStatsList.sortedByDescending { it.totalPurchase }
            }
            ReportAdditionalFilter.TOP_CASH_PAID -> {
                customerStatsList.sortedByDescending { it.totalPaid }
            }
            ReportAdditionalFilter.TOP_CREDIT -> {
                // TOP_CREDIT sorts by totalPending (credit/due amount)
                customerStatsList.sortedByDescending { it.totalPending }
            }
            else -> {
                // Default to TOP_CREDIT
                customerStatsList.sortedByDescending { it.totalPending }
            }
        }
        
        // Generate columns and rows
        val columns = listOf("Customer", "Total Sale", "Total Paid", "Total Due", "Discount")
        val rows = mutableListOf<ReportRow>()
        
        var totalEarned = 0.0
        var totalCreditSale = 0.0
        var totalCashReceived = 0.0
        var totalDiscountGiven = 0.0
        
        sortedStats.forEach { stats ->
            val party = partyMap[stats.customerSlug]
            val customerName = party?.name ?: "Unknown Customer"
            
            totalEarned += stats.totalPurchase
            totalCashReceived += stats.totalPaid
            totalCreditSale += stats.totalPending
            totalDiscountGiven += stats.totalDiscount
            
            rows.add(
                ReportRow(
                    id = stats.customerSlug,
                    values = listOf(
                        customerName,
                        "$currencySymbol ${String.format("%,.2f", stats.totalPurchase)}",
                        "$currencySymbol ${String.format("%,.2f", stats.totalPaid)}",
                        "$currencySymbol ${String.format("%,.2f", stats.totalPending)}",
                        "$currencySymbol ${String.format("%,.2f", stats.totalDiscount)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalEarned,
            recordCount = rows.size,
            additionalInfo = mapOf(
                "Total Credit Sale" to String.format("%,.2f", totalCreditSale),
                "Total Cash Received" to String.format("%,.2f", totalCashReceived),
                "Total Discount Given" to String.format("%,.2f", totalDiscountGiven)
            )
        )
        
        return ReportResult(
            reportType = ReportType.TOP_CUSTOMERS,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = summary
        )
    }
    
    private fun calculateDateRange(filters: ReportFilters): Pair<Long, Long> {
        val now = Clock.System.now()
        val timezone = TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(timezone).date
        
        val (startDate, endDate) = when (filters.dateFilter) {
            ReportDateFilter.TODAY -> {
                today to today
            }
            ReportDateFilter.YESTERDAY -> {
                val yesterday = today.minus(1, DateTimeUnit.DAY)
                yesterday to yesterday
            }
            ReportDateFilter.LAST_7_DAYS -> {
                val sevenDaysAgo = today.minus(7, DateTimeUnit.DAY)
                sevenDaysAgo to today
            }
            ReportDateFilter.THIS_MONTH -> {
                val firstDayOfMonth = LocalDate(today.year, today.month, 1)
                firstDayOfMonth to today
            }
            ReportDateFilter.LAST_MONTH -> {
                val firstDayOfLastMonth = LocalDate(today.year, today.month, 1).minus(1, DateTimeUnit.MONTH)
                val lastDayOfLastMonth = firstDayOfLastMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
                firstDayOfLastMonth to lastDayOfLastMonth
            }
            ReportDateFilter.THIS_YEAR -> {
                val firstDayOfYear = LocalDate(today.year, 1, 1)
                firstDayOfYear to today
            }
            ReportDateFilter.LAST_YEAR -> {
                val firstDayOfLastYear = LocalDate(today.year - 1, 1, 1)
                val lastDayOfLastYear = LocalDate(today.year - 1, 12, 31)
                firstDayOfLastYear to lastDayOfLastYear
            }
            ReportDateFilter.CUSTOM_DATE -> {
                if (filters.customStartDate != null && filters.customEndDate != null) {
                    try {
                        val start = LocalDate.parse(filters.customStartDate)
                        val end = LocalDate.parse(filters.customEndDate)
                        start to end
                    } catch (e: Exception) {
                        today.minus(30, DateTimeUnit.DAY) to today
                    }
                } else {
                    today.minus(30, DateTimeUnit.DAY) to today
                }
            }
            ReportDateFilter.ALL_TIME -> {
                LocalDate(2020, 1, 1) to today
            }
            else -> {
                LocalDate(2020, 1, 1) to today
            }
        }
        
        val startMillis = startDate.atStartOfDayIn(timezone).toEpochMilliseconds()
        val endMillis = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timezone).toEpochMilliseconds()
        
        return Pair(startMillis, endMillis)
    }
    
    // Helper data class for customer statistics
    private data class CustomerStats(
        val customerSlug: String,
        var totalPurchase: Double = 0.0,
        var totalPaid: Double = 0.0,
        var totalDiscount: Double = 0.0,
        var totalAdditionalCharges: Double = 0.0,
        var totalTaxPaid: Double = 0.0,
        val transactions: MutableList<String> = mutableListOf()
    ) {
        val totalPending: Double
            get() = totalPurchase - totalPaid - totalDiscount + totalAdditionalCharges + totalTaxPaid
    }
}

