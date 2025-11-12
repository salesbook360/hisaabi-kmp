package com.hisaabi.hisaabi_kmp.reports.domain.usecase

import com.hisaabi.hisaabi_kmp.business.data.datasource.BusinessPreferencesDataSource
import com.hisaabi.hisaabi_kmp.database.dao.InventoryTransactionDao
import com.hisaabi.hisaabi_kmp.database.dao.TransactionDetailDao
import com.hisaabi.hisaabi_kmp.reports.domain.model.*
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*

/**
 * Use case for generating sales reports from actual database
 */
class GenerateSalesReportUseCase(
    private val transactionDao: InventoryTransactionDao,
    private val transactionDetailDao: TransactionDetailDao,
    private val businessPreferences: BusinessPreferencesDataSource
) {
    
    suspend fun execute(filters: ReportFilters): ReportResult {
        val businessSlug = businessPreferences.observeSelectedBusinessSlug().first() 
            ?: throw IllegalStateException("No business selected")
        
        // Calculate date range based on filters
        val dateRange = calculateDateRange(filters.dateFilter)
        
        // Get sale and customer return transactions
        val transactionTypes = listOf(
            AllTransactionTypes.SALE.value,
            AllTransactionTypes.CUSTOMER_RETURN.value
        )
        
        val transactions = transactionDao.getTransactionsForReport(
            businessSlug = businessSlug,
            transactionTypes = transactionTypes,
            fromDate = dateRange.first,
            toDate = dateRange.second
        )
        
        // Get transaction details for all transactions
        val transactionSlugs = transactions.mapNotNull { it.slug }
        val allDetails = transactionDetailDao.getDetailsByTransactionSlugs(transactionSlugs)
        
        // Generate report data
        val columns = listOf("Date", "Invoice #", "Customer", "Qty Sold", "Qty Returned", "Net Qty", "Amount", "Profit")
        val rows = mutableListOf<ReportRow>()
        
        var totalAmount = 0.0
        var totalProfit = 0.0
        var totalQtySold = 0.0
        var totalQtyReturned = 0.0
        
        transactions.forEach { transaction ->
            val details = allDetails.filter { it.transaction_slug == transaction.slug }
            
            val qtySold = if (transaction.transaction_type == AllTransactionTypes.SALE.value) {
                details.sumOf { it.quantity }
            } else 0.0
            
            val qtyReturned = if (transaction.transaction_type == AllTransactionTypes.CUSTOMER_RETURN.value) {
                details.sumOf { it.quantity }
            } else 0.0
            
            val netQty = qtySold - qtyReturned
            val amount = transaction.total_bill + transaction.additional_charges + transaction.tax - transaction.discount
            val profit = details.sumOf { it.profit * it.quantity }
            
            totalQtySold += qtySold
            totalQtyReturned += qtyReturned
            totalAmount += if (transaction.transaction_type == AllTransactionTypes.SALE.value) amount else -amount
            totalProfit += if (transaction.transaction_type == AllTransactionTypes.SALE.value) profit else -profit
            
            val date = formatTimestamp(transaction.timestamp)
            val invoiceNo = transaction.slug?.takeLast(8)?.uppercase() ?: "-"
            val customerName = transaction.customer_slug?.takeLast(10) ?: "Walk-in Customer"
            
            rows.add(
                ReportRow(
                    id = transaction.slug ?: "",
                    values = listOf(
                        date,
                        invoiceNo,
                        customerName,
                        String.format("%.2f", qtySold),
                        String.format("%.2f", qtyReturned),
                        String.format("%.2f", netQty),
                        "Rs ${String.format("%,.0f", amount)}",
                        "Rs ${String.format("%,.0f", profit)}"
                    )
                )
            )
        }
        
        return ReportResult(
            reportType = ReportType.SALE_REPORT,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = ReportSummary(
                totalAmount = totalAmount,
                totalProfit = totalProfit,
                totalQuantity = totalQtySold - totalQtyReturned,
                recordCount = transactions.size,
                additionalInfo = mapOf(
                    "Total Qty Sold" to String.format("%.0f", totalQtySold),
                    "Total Qty Returned" to String.format("%.0f", totalQtyReturned)
                )
            )
        )
    }
    
    private fun calculateDateRange(dateFilter: ReportDateFilter): Pair<String, String> {
        val now = Clock.System.now()
        val timezone = TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(timezone).date
        
        val (startDate, endDate) = when (dateFilter) {
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
                // TODO: Implement custom date range
                today.minus(30, DateTimeUnit.DAY) to today
            }
            ReportDateFilter.ALL_TIME -> {
                LocalDate(2020, 1, 1) to today
            }
        }
        
        // Convert to timestamp strings (ISO format)
        val startDateTime = LocalDateTime(startDate, LocalTime(0, 0, 0))
        val endDateTime = LocalDateTime(endDate, LocalTime(23, 59, 59))
        
        return startDateTime.toString() to endDateTime.toString()
    }
    
    private fun formatTimestamp(timestamp: String?): String {
        if (timestamp == null) return "-"
        
        return try {
            val instant = Instant.parse(timestamp)
            val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            "${dateTime.date}"
        } catch (e: Exception) {
            timestamp.take(10)
        }
    }
}

