package com.hisaabi.hisaabi_kmp.reports.domain.usecase

import com.hisaabi.hisaabi_kmp.business.data.datasource.BusinessPreferencesDataSource
import com.hisaabi.hisaabi_kmp.database.dao.InventoryTransactionDao
import com.hisaabi.hisaabi_kmp.database.dao.ProductDao
import com.hisaabi.hisaabi_kmp.database.dao.TransactionDetailDao
import com.hisaabi.hisaabi_kmp.reports.domain.model.*
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import com.hisaabi.hisaabi_kmp.utils.formatEntryDateTime
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*

/**
 * Use case for generating Product Report (ProductSummary)
 * Based on the legacy Android Native ProductReportGenerator
 */
class GenerateProductReportUseCase(
    private val transactionDao: InventoryTransactionDao,
    private val transactionDetailDao: TransactionDetailDao,
    private val productDao: ProductDao,
    private val businessPreferences: BusinessPreferencesDataSource,
    private val preferencesManager: PreferencesManager
) {
    
    suspend fun execute(filters: ReportFilters): ReportResult {
        val currencySymbol = preferencesManager.getSelectedCurrency().symbol
        val businessSlug = businessPreferences.observeSelectedBusinessSlug().first()
            ?: throw IllegalStateException("No business selected")
        
        val selectedProductId = filters.selectedProductId
            ?: throw IllegalStateException("Product selection is required for Product Report")
        
        // Calculate date range based on filters
        val (fromDateMillis, toDateMillis) = calculateDateRange(filters)
        
        // Get all transactions (we'll filter by product in details)
        // Product report needs all transaction types to show complete ledger
        val allTransactionTypes = listOf(
            AllTransactionTypes.SALE.value,
            AllTransactionTypes.PURCHASE.value,
            AllTransactionTypes.STOCK_REDUCE.value,
            AllTransactionTypes.STOCK_INCREASE.value,
            AllTransactionTypes.CUSTOMER_RETURN.value,
            AllTransactionTypes.VENDOR_RETURN.value
        )
        val allTransactions = transactionDao.getTransactionsForReport(
            businessSlug = businessSlug,
            transactionTypes = allTransactionTypes,
            fromDate = fromDateMillis,
            toDate = toDateMillis
        )
        
        // Get transaction details for the selected product
        val allDetails = transactionDetailDao.getDetailsByTransactionSlugs(
            allTransactions.mapNotNull { it.slug }
        ).filter { it.product_slug == selectedProductId }
        
        // Filter transactions that contain the selected product
        val relevantTransactions = allTransactions.filter { transaction ->
            allDetails.any { it.transaction_slug == transaction.slug }
        }
        
        // Get product info
        val products = productDao.getProductsByBusiness(businessSlug).first()
        val product = products.find { it.slug == selectedProductId }
        val productTitle = product?.title ?: "Unknown Product"
        
        // Generate report based on filter type
        val additionalFilter = filters.additionalFilter
        val (columns, rows, summary) = when (additionalFilter) {
            ReportAdditionalFilter.LEDGER -> generateLedgerReport(
                relevantTransactions, allDetails, currencySymbol
            )
            ReportAdditionalFilter.OVERALL -> generateOverallReport(
                relevantTransactions, allDetails, currencySymbol
            )
            ReportAdditionalFilter.DAILY -> generateIntervalReport(
                relevantTransactions, allDetails, currencySymbol, "DAILY"
            )
            ReportAdditionalFilter.WEEKLY -> generateIntervalReport(
                relevantTransactions, allDetails, currencySymbol, "WEEKLY"
            )
            ReportAdditionalFilter.MONTHLY -> generateIntervalReport(
                relevantTransactions, allDetails, currencySymbol, "MONTHLY"
            )
            ReportAdditionalFilter.YEARLY -> generateIntervalReport(
                relevantTransactions, allDetails, currencySymbol, "YEARLY"
            )
            else -> generateOverallReport(relevantTransactions, allDetails, currencySymbol)
        }
        
        return ReportResult(
            reportType = ReportType.PRODUCT_REPORT,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = summary
        )
    }
    
    /**
     * Generate LEDGER report - detailed transaction list
     */
    private fun generateLedgerReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        details: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        currencySymbol: String
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Date", "Type", "Transaction ID", "Debit", "Credit", "Price", "Profit")
        val rows = mutableListOf<ReportRow>()
        
        // Sort by date (newest first)
        val sortedTransactions = transactions.sortedByDescending { transaction ->
            transaction.created_at?.let { 
                try {
                    Instant.parse(it).toEpochMilliseconds()
                } catch (e: Exception) {
                    0L
                }
            } ?: 0L
        }
        
        var totalDebit = 0.0
        var totalCredit = 0.0
        var totalProfit = 0.0
        
        sortedTransactions.forEach { transaction ->
            val detail = details.find { it.transaction_slug == transaction.slug }
            if (detail == null) return@forEach
            
            val dateStr = transaction.created_at?.let { formatEntryDateTime(it) } ?: "Unknown"
            val transactionType = AllTransactionTypes.getDisplayName(transaction.transaction_type)
            val transactionId = transaction.slug ?: ""
            val description = transaction.description ?: ""
            
            val quantity = detail.quantity
            val price = detail.price
            val profit = detail.profit
            
            // Determine debit/credit based on transaction type
            val (debit, credit) = when (transaction.transaction_type) {
                AllTransactionTypes.SALE.value,
                AllTransactionTypes.VENDOR_RETURN.value,
                AllTransactionTypes.STOCK_REDUCE.value -> {
                    // Credit (outgoing)
                    0.0 to quantity
                }
                AllTransactionTypes.STOCK_TRANSFER.value -> {
                    // Both debit and credit for transfers
                    quantity to quantity
                }
                else -> {
                    // Debit (incoming)
                    quantity to 0.0
                }
            }
            
            totalDebit += debit
            totalCredit += credit
            totalProfit += profit
            
            rows.add(
                ReportRow(
                    id = transaction.slug ?: "",
                    values = listOf(
                        dateStr,
                        transactionType,
                        transactionId,
                        String.format("%.2f", debit),
                        String.format("%.2f", credit),
                        "$currencySymbol ${String.format("%,.2f", price)}",
                        "$currencySymbol ${String.format("%,.2f", profit)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalQuantity = totalDebit - totalCredit,
            totalProfit = totalProfit,
            recordCount = rows.size,
            additionalInfo = mapOf(
                "Total Debit" to String.format("%.2f", totalDebit),
                "Total Credit" to String.format("%.2f", totalCredit)
            )
        )
        
        return Triple(columns, rows, summary)
    }
    
    /**
     * Generate OVERALL report - transaction type-wise summary
     */
    private fun generateOverallReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        details: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        currencySymbol: String
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Transaction Type", "Quantity", "Amount")
        val rows = mutableListOf<ReportRow>()
        
        // Group by transaction type
        val typeStatsMap = mutableMapOf<Int, TransactionTypeStats>()
        
        transactions.forEach { transaction ->
            val detail = details.find { it.transaction_slug == transaction.slug }
            if (detail == null) return@forEach
            
            val stats = typeStatsMap.getOrPut(transaction.transaction_type) {
                TransactionTypeStats(transaction.transaction_type)
            }
            
            stats.totalQuantity += detail.quantity
            stats.totalAmount += detail.price * detail.quantity
            transaction.slug?.let { stats.transactions.add(it) }
        }
        
        var totalQuantity = 0.0
        var totalAmount = 0.0
        
        typeStatsMap.values.forEach { stats ->
            val transactionType = AllTransactionTypes.getDisplayName(stats.transactionType)
            
            totalQuantity += stats.totalQuantity
            totalAmount += stats.totalAmount
            
            rows.add(
                ReportRow(
                    id = stats.transactionType.toString(),
                    values = listOf(
                        transactionType,
                        String.format("%.2f", stats.totalQuantity),
                        "$currencySymbol ${String.format("%,.2f", stats.totalAmount)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalAmount,
            totalQuantity = totalQuantity,
            recordCount = rows.size
        )
        
        return Triple(columns, rows, summary)
    }
    
    /**
     * Generate interval-based report (Daily, Weekly, Monthly, Yearly)
     */
    private fun generateIntervalReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        details: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        currencySymbol: String,
        intervalType: String
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Date", "Purchase Amount", "Total Sale Amount", "Quantity Sold", "Quantity Returned")
        val rows = mutableListOf<ReportRow>()
        
        // Group by date interval
        val intervalMap = mutableMapOf<String, IntervalStats>()
        
        transactions.forEach { transaction ->
            val detail = details.find { it.transaction_slug == transaction.slug }
            if (detail == null) return@forEach
            
            val dateKey = getDateKey(transaction.timestamp, intervalType)
            val stats = intervalMap.getOrPut(dateKey) {
                IntervalStats(dateKey)
            }
            
            val amount = detail.price * detail.quantity
            
            when (transaction.transaction_type) {
                AllTransactionTypes.SALE.value -> {
                    stats.netSaleAmount += amount
                    stats.quantitySold += detail.quantity
                }
                AllTransactionTypes.CUSTOMER_RETURN.value -> {
                    stats.netSaleAmount -= amount
                    stats.quantityReturned += detail.quantity
                }
                AllTransactionTypes.PURCHASE.value -> {
                    stats.purchaseAmount += amount
                }
                AllTransactionTypes.VENDOR_RETURN.value -> {
                    stats.purchaseAmount -= amount
                }
            }
        }
        
        // Sort by date (newest first)
        val sortedIntervals = intervalMap.values.sortedByDescending { it.dateKey }
        
        var totalPurchaseAmount = 0.0
        var totalSaleAmount = 0.0
        var totalQuantitySold = 0.0
        var totalQuantityReturned = 0.0
        
        sortedIntervals.forEach { stats ->
            totalPurchaseAmount += stats.purchaseAmount
            totalSaleAmount += stats.netSaleAmount
            totalQuantitySold += stats.quantitySold
            totalQuantityReturned += stats.quantityReturned
            
            val displayDate = formatDateKey(stats.dateKey, intervalType)
            
            rows.add(
                ReportRow(
                    id = stats.dateKey,
                    values = listOf(
                        displayDate,
                        "$currencySymbol ${String.format("%,.2f", stats.purchaseAmount)}",
                        "$currencySymbol ${String.format("%,.2f", stats.netSaleAmount)}",
                        String.format("%.2f", stats.quantitySold),
                        String.format("%.2f", stats.quantityReturned)
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalSaleAmount,
            totalQuantity = totalQuantitySold - totalQuantityReturned,
            recordCount = rows.size,
            additionalInfo = mapOf(
                "Total Purchase Amount" to String.format("%,.2f", totalPurchaseAmount),
                "Total Quantity Sold" to String.format("%.2f", totalQuantitySold),
                "Total Quantity Returned" to String.format("%.2f", totalQuantityReturned)
            )
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
    private data class TransactionTypeStats(
        val transactionType: Int,
        var totalQuantity: Double = 0.0,
        var totalAmount: Double = 0.0,
        val transactions: MutableList<String> = mutableListOf()
    )
    
    private data class IntervalStats(
        val dateKey: String,
        var purchaseAmount: Double = 0.0,
        var netSaleAmount: Double = 0.0,
        var quantitySold: Double = 0.0,
        var quantityReturned: Double = 0.0
    )
}

