package com.hisaabi.hisaabi_kmp.reports.domain.usecase

import com.hisaabi.hisaabi_kmp.business.data.datasource.BusinessPreferencesDataSource
import com.hisaabi.hisaabi_kmp.database.dao.InventoryTransactionDao
import com.hisaabi.hisaabi_kmp.database.dao.PartyDao
import com.hisaabi.hisaabi_kmp.reports.domain.model.*
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*
import kotlin.math.abs

/**
 * Use case for generating Expense and Extra Income Reports
 * Based on the legacy Android Native ExtraIncomeOrExpenseReportGenerator
 */
class GenerateExpenseIncomeReportUseCase(
    private val transactionDao: InventoryTransactionDao,
    private val partyDao: PartyDao,
    private val businessPreferences: BusinessPreferencesDataSource,
    private val preferencesManager: PreferencesManager
) {
    
    suspend fun execute(filters: ReportFilters): ReportResult {
        val currencySymbol = preferencesManager.getSelectedCurrency().symbol
        val businessSlug = businessPreferences.observeSelectedBusinessSlug().first()
            ?: throw IllegalStateException("No business selected")
        
        val reportType = filters.reportType
            ?: throw IllegalStateException("Report type is required")
        
        // Determine transaction type based on report type
        val transactionType = when (reportType) {
            ReportType.EXPENSE_REPORT -> AllTransactionTypes.EXPENSE.value
            ReportType.EXTRA_INCOME_REPORT -> AllTransactionTypes.EXTRA_INCOME.value
            else -> throw IllegalStateException("Invalid report type for Expense/Income Report")
        }
        
        // Calculate date range based on filters
        val (fromDateMillis, toDateMillis) = calculateDateRange(filters)
        
        // Get expense or income transactions
        val transactions = transactionDao.getTransactionsForReport(
            businessSlug = businessSlug,
            transactionTypes = listOf(transactionType),
            fromDate = fromDateMillis,
            toDate = toDateMillis
        )
        
        // Get parties for lookup
        val parties = partyDao.getPartiesByBusiness(businessSlug).first()
        val partyMap = parties.associateBy { it.slug ?: "" }
        
        // Generate report based on filter type
        val additionalFilter = filters.additionalFilter
        val (columns, rows, summary) = when (additionalFilter) {
            ReportAdditionalFilter.OVERALL -> generateOverallReport(
                transactions, partyMap, currencySymbol
            )
            ReportAdditionalFilter.DAILY -> generateIntervalReport(
                transactions, currencySymbol, "DAILY"
            )
            ReportAdditionalFilter.WEEKLY -> generateIntervalReport(
                transactions, currencySymbol, "WEEKLY"
            )
            ReportAdditionalFilter.MONTHLY -> generateIntervalReport(
                transactions, currencySymbol, "MONTHLY"
            )
            ReportAdditionalFilter.YEARLY -> generateIntervalReport(
                transactions, currencySymbol, "YEARLY"
            )
            else -> generateOverallReport(transactions, partyMap, currencySymbol)
        }
        
        return ReportResult(
            reportType = reportType,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = summary
        )
    }
    
    /**
     * Generate OVERALL report - grouped by expense/income type (party)
     */
    private fun generateOverallReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        partyMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.PartyEntity>,
        currencySymbol: String
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Type", "Amount")
        val rows = mutableListOf<ReportRow>()
        
        // Group by expense/income type (party)
        val typeStatsMap = mutableMapOf<String, TypeStats>()
        
        transactions.forEach { transaction ->
            val partySlug = transaction.party_slug ?: ""
            val stats = typeStatsMap.getOrPut(partySlug) {
                TypeStats(partySlug)
            }
            
            // Use totalPaid as the amount (as per legacy app)
            stats.totalAmount += transaction.total_paid
            transaction.slug?.let { stats.transactionIds.add(it) }
        }
        
        var totalAmount = 0.0
        
        typeStatsMap.values.forEach { stats ->
            val party = partyMap[stats.partySlug]
            val typeName = party?.name ?: "Other"
            
            totalAmount += stats.totalAmount
            
            rows.add(
                ReportRow(
                    id = stats.partySlug.ifEmpty { "other" },
                    values = listOf(
                        typeName,
                        "$currencySymbol ${String.format("%,.2f", stats.totalAmount)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalAmount,
            recordCount = rows.size,
            additionalInfo = emptyMap()
        )
        
        return Triple(columns, rows, summary)
    }
    
    /**
     * Generate interval-based report (Daily, Weekly, Monthly, Yearly)
     */
    private fun generateIntervalReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        currencySymbol: String,
        intervalType: String
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Date", "Amount")
        val rows = mutableListOf<ReportRow>()
        
        // Group by date interval
        val intervalMap = mutableMapOf<String, IntervalStats>()
        
        transactions.forEach { transaction ->
            val dateKey = getDateKey(transaction.timestamp, intervalType)
            val stats = intervalMap.getOrPut(dateKey) {
                IntervalStats(dateKey)
            }
            
            // Use totalPaid as the amount (as per legacy app)
            stats.totalAmount += transaction.total_paid
            transaction.slug?.let { stats.transactionIds.add(it) }
        }
        
        // Sort by date (newest first)
        val sortedIntervals = intervalMap.values.sortedByDescending { it.dateKey }
        
        var totalAmount = 0.0
        
        sortedIntervals.forEach { stats ->
            totalAmount += stats.totalAmount
            
            val displayDate = formatDateKey(stats.dateKey, intervalType)
            
            rows.add(
                ReportRow(
                    id = stats.dateKey,
                    values = listOf(
                        displayDate,
                        "$currencySymbol ${String.format("%,.2f", stats.totalAmount)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalAmount,
            recordCount = rows.size,
            additionalInfo = emptyMap()
        )
        
        return Triple(columns, rows, summary)
    }
    
    private fun getDateKey(timestamp: String?, intervalType: String): String {
        if (timestamp == null) return "Unknown"
        
        return try {
            // timestamp is in milliseconds format (as String), convert to Long first
            val timestampMillis = timestamp.toLongOrNull()
                ?: return "Unknown"
            val instant = Instant.fromEpochMilliseconds(timestampMillis)
            val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            
            when (intervalType) {
                "DAILY" -> {
                    val date = dateTime.date
                    "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
                }
                "WEEKLY" -> {
                    val date = dateTime.date
                    val weekStart = date.minus(date.dayOfWeek.ordinal, DateTimeUnit.DAY)
                    "${weekStart.year}-${weekStart.monthNumber.toString().padStart(2, '0')}-${weekStart.dayOfMonth.toString().padStart(2, '0')}"
                }
                "MONTHLY" -> {
                    val date = dateTime.date
                    "${date.year}-${date.monthNumber.toString().padStart(2, '0')}"
                }
                "YEARLY" -> {
                    val date = dateTime.date
                    date.year.toString()
                }
                else -> timestamp
            }
        } catch (e: Exception) {
            timestamp
        }
    }
    
    private fun formatDateKey(dateKey: String, intervalType: String): String {
        return when (intervalType) {
            "DAILY" -> {
                try {
                    val parts = dateKey.split("-")
                    if (parts.size == 3) {
                        "${parts[2]}/${parts[1]}/${parts[0]}"
                    } else dateKey
                } catch (e: Exception) {
                    dateKey
                }
            }
            "WEEKLY" -> {
                try {
                    val parts = dateKey.split("-")
                    if (parts.size == 3) {
                        "Week of ${parts[2]}/${parts[1]}/${parts[0]}"
                    } else dateKey
                } catch (e: Exception) {
                    dateKey
                }
            }
            "MONTHLY" -> {
                try {
                    val parts = dateKey.split("-")
                    if (parts.size == 2) {
                        val monthNames = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                        val monthNum = parts[1].toIntOrNull() ?: 0
                        "${monthNames.getOrElse(monthNum) { parts[1] }} ${parts[0]}"
                    } else dateKey
                } catch (e: Exception) {
                    dateKey
                }
            }
            "YEARLY" -> dateKey
            else -> dateKey
        }
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
    
    // Helper data classes
    private data class TypeStats(
        val partySlug: String,
        var totalAmount: Double = 0.0,
        val transactionIds: MutableList<String> = mutableListOf()
    )
    
    private data class IntervalStats(
        val dateKey: String,
        var totalAmount: Double = 0.0,
        val transactionIds: MutableList<String> = mutableListOf()
    )
}

