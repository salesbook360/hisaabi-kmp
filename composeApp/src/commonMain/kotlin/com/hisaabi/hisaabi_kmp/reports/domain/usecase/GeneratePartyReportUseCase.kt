package com.hisaabi.hisaabi_kmp.reports.domain.usecase

import com.hisaabi.hisaabi_kmp.business.data.datasource.BusinessPreferencesDataSource
import com.hisaabi.hisaabi_kmp.database.dao.InventoryTransactionDao
import com.hisaabi.hisaabi_kmp.database.dao.PartyDao
import com.hisaabi.hisaabi_kmp.database.dao.ProductDao
import com.hisaabi.hisaabi_kmp.database.dao.TransactionDetailDao
import com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType
import com.hisaabi.hisaabi_kmp.reports.domain.model.*
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import com.hisaabi.hisaabi_kmp.utils.formatEntryDateTime
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*
import kotlin.math.abs

/**
 * Use case for generating Party Report (Customer, Vendor, Investor)
 * Based on the legacy Android Native PersonReportGenerator
 */
class GeneratePartyReportUseCase(
    private val transactionDao: InventoryTransactionDao,
    private val transactionDetailDao: TransactionDetailDao,
    private val partyDao: PartyDao,
    private val productDao: ProductDao,
    private val businessPreferences: BusinessPreferencesDataSource,
    private val preferencesManager: PreferencesManager
) {
    
    suspend fun execute(filters: ReportFilters): ReportResult {
        val currencySymbol = preferencesManager.getSelectedCurrency().symbol
        val businessSlug = businessPreferences.observeSelectedBusinessSlug().first()
            ?: throw IllegalStateException("No business selected")
        
        val reportType = filters.reportType
            ?: throw IllegalStateException("Report type is required")
        
        val selectedPartyId = when (reportType) {
            ReportType.CUSTOMER_REPORT -> filters.selectedPartyId
            ReportType.VENDOR_REPORT -> filters.selectedPartyId
            ReportType.INVESTOR_REPORT -> filters.selectedInvestorId
            else -> throw IllegalStateException("Invalid report type for Party Report")
        } ?: throw IllegalStateException("Party selection is required for ${reportType.name}")
        
        // Get party info
        val parties = partyDao.getPartiesByBusiness(businessSlug).first()
        val party = parties.find { it.slug == selectedPartyId }
            ?: throw IllegalStateException("Party not found")
        
        // Calculate date range based on filters
        val (fromDateMillis, toDateMillis) = calculateDateRange(filters)
        
        // Get all party-related transactions
        val allTransactionTypes = AllTransactionTypes.entries.map { it.value }
        val transactions = transactionDao.getTransactionsForReport(
            businessSlug = businessSlug,
            transactionTypes = allTransactionTypes,
            fromDate = fromDateMillis,
            toDate = toDateMillis
        ).filter { it.party_slug == selectedPartyId }
        
        // Get transaction details
        val transactionSlugs = transactions.mapNotNull { it.slug }
        val allDetails = transactionDetailDao.getDetailsByTransactionSlugs(transactionSlugs)
        
        // Get products for lookup
        val products = productDao.getProductsByBusiness(businessSlug).first()
        val productMap = products.associateBy { it.slug ?: "" }
        
        // Generate report based on filter type
        val additionalFilter = filters.additionalFilter
        val (columns, rows, summary) = when (additionalFilter) {
            ReportAdditionalFilter.LEDGER -> generateLedgerReport(
                transactions, party, currencySymbol, filters, businessSlug
            )
            ReportAdditionalFilter.CASH_FLOW -> generateCashFlowReport(
                transactions, party, currencySymbol, filters
            )
            ReportAdditionalFilter.OVERALL -> generateOverallReport(
                transactions, allDetails, productMap, currencySymbol, reportType
            )
            ReportAdditionalFilter.DAILY -> generateIntervalReport(
                transactions, allDetails, currencySymbol, "DAILY", reportType
            )
            ReportAdditionalFilter.WEEKLY -> generateIntervalReport(
                transactions, allDetails, currencySymbol, "WEEKLY", reportType
            )
            ReportAdditionalFilter.MONTHLY -> generateIntervalReport(
                transactions, allDetails, currencySymbol, "MONTHLY", reportType
            )
            ReportAdditionalFilter.YEARLY -> generateIntervalReport(
                transactions, allDetails, currencySymbol, "YEARLY", reportType
            )
            else -> generateOverallReport(transactions, allDetails, productMap, currencySymbol, reportType)
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
     * Generate LEDGER report - detailed transaction list with balance
     */
    private suspend fun generateLedgerReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        party: com.hisaabi.hisaabi_kmp.database.entity.PartyEntity,
        currencySymbol: String,
        filters: ReportFilters,
        businessSlug: String
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Date", "Type", "Transaction ID", "Total Bill", "Paid", "Debit", "Credit", "Balance")
        val rows = mutableListOf<ReportRow>()
        
        // Sort by date based on sort option
        val sortedTransactions = when (filters.sortBy) {
            ReportSortBy.DATE_ASC -> {
                transactions.sortedBy { transaction ->
                    transaction.created_at?.let { 
                        try {
                            Instant.parse(it).toEpochMilliseconds()
                        } catch (e: Exception) {
                            0L
                        }
                    } ?: 0L
                }
            }
            ReportSortBy.DATE_DESC -> {
                transactions.sortedByDescending { transaction ->
                    transaction.created_at?.let { 
                        try {
                            Instant.parse(it).toEpochMilliseconds()
                        } catch (e: Exception) {
                            0L
                        }
                    } ?: 0L
                }
            }
            else -> {
                // Default: newest first (descending)
                transactions.sortedByDescending { transaction ->
                    transaction.created_at?.let { 
                        try {
                            Instant.parse(it).toEpochMilliseconds()
                        } catch (e: Exception) {
                            0L
                        }
                    } ?: 0L
                }
            }
        }
        
        // Calculate opening balance (balance before the date range)
        var openingBalance = party.opening_balance
        // Get transactions before the date range to calculate opening balance
        val (fromDateMillis, _) = calculateDateRange(filters)
        val earlierTransactions = transactionDao.getTransactionsForReport(
            businessSlug = party.business_slug ?: "",
            transactionTypes = AllTransactionTypes.entries.map { it.value },
            fromDate = 0L,
            toDate = fromDateMillis - 1
        ).filter { it.party_slug == party.slug }
        
        earlierTransactions.forEach { transaction ->
            openingBalance += calculateTransactionEffect(transaction, filters.reportType)
        }
        
        var runningBalance = openingBalance
        var totalDebit = 0.0
        var totalCredit = 0.0
        
        sortedTransactions.forEach { transaction ->
            val dateStr = transaction.created_at?.let { formatEntryDateTime(it) } ?: "Unknown"
            val transactionType = AllTransactionTypes.getDisplayName(transaction.transaction_type)
            val transactionId =  transaction.slug ?: ""
            
            // Calculate total bill (total_bill + additional_charges + flat_tax - flat_discount)
            val totalBill = transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
            val paid = transaction.total_paid
            
            val effect = calculateTransactionEffect(transaction, filters.reportType)
            runningBalance += effect
            
            val (debit, credit) = if (effect > 0) {
                totalDebit += effect
                (effect to 0.0)
            } else {
                totalCredit += abs(effect)
                (0.0 to abs(effect))
            }
            
            rows.add(
                ReportRow(
                    id = transaction.slug ?: "",
                    values = listOf(
                        dateStr,
                        transactionType,
                        transactionId,
                        "$currencySymbol ${String.format("%,.2f", totalBill)}",
                        "$currencySymbol ${String.format("%,.2f", paid)}",
                        if (debit > 0) "$currencySymbol ${String.format("%,.2f", debit)}" else "",
                        if (credit > 0) "$currencySymbol ${String.format("%,.2f", credit)}" else "",
                        "$currencySymbol ${String.format("%,.2f", runningBalance)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = runningBalance,
            recordCount = rows.size,
            additionalInfo = mapOf(
                "Opening Balance" to String.format("%,.2f", openingBalance),
                "Total Debit" to String.format("%,.2f", totalDebit),
                "Total Credit" to String.format("%,.2f", totalCredit),
                "Closing Balance" to String.format("%,.2f", runningBalance)
            )
        )
        
        return Triple(columns, rows, summary)
    }
    
    /**
     * Generate CASH_FLOW report - balance statement
     */
    private fun generateCashFlowReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        party: com.hisaabi.hisaabi_kmp.database.entity.PartyEntity,
        currencySymbol: String,
        filters: ReportFilters
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Date", "Transaction Type", "Previous", "Debit", "Credit")
        val rows = mutableListOf<ReportRow>()
        
        // Sort by date based on sort option
        val sortedTransactions = when (filters.sortBy) {
            ReportSortBy.DATE_ASC -> {
                transactions.sortedBy { transaction ->
                    transaction.created_at?.let { 
                        try {
                            Instant.parse(it).toEpochMilliseconds()
                        } catch (e: Exception) {
                            0L
                        }
                    } ?: 0L
                }
            }
            ReportSortBy.DATE_DESC -> {
                transactions.sortedByDescending { transaction ->
                    transaction.created_at?.let { 
                        try {
                            Instant.parse(it).toEpochMilliseconds()
                        } catch (e: Exception) {
                            0L
                        }
                    } ?: 0L
                }
            }
            else -> {
                // Default: newest first (descending)
                transactions.sortedByDescending { transaction ->
                    transaction.created_at?.let { 
                        try {
                            Instant.parse(it).toEpochMilliseconds()
                        } catch (e: Exception) {
                            0L
                        }
                    } ?: 0L
                }
            }
        }
        
        // Start with current balance and work backwards
        var endingBalance = party.balance
        var totalDebit = 0.0
        var totalCredit = 0.0
        
        sortedTransactions.forEach { transaction ->
            val dateStr = transaction.created_at?.let { formatEntryDateTime(it) } ?: "Unknown"
            val transactionType = AllTransactionTypes.getDisplayName(transaction.transaction_type)
            
            val effect = calculateTransactionEffect(transaction, filters.reportType)
            val startingBalance = endingBalance + effect // Reverse the effect to get previous balance
            
            val debitCredit = endingBalance - startingBalance
            val (debit, credit) = if (debitCredit > 0) {
                totalDebit += debitCredit
                (debitCredit to 0.0)
            } else {
                totalCredit += abs(debitCredit)
                (0.0 to abs(debitCredit))
            }
            
            rows.add(
                ReportRow(
                    id = transaction.slug ?: "",
                    values = listOf(
                        dateStr,
                        transactionType,
                        "$currencySymbol ${String.format("%,.2f", startingBalance)}",
                        if (debit > 0) "$currencySymbol ${String.format("%,.2f", debit)}" else "",
                        if (credit > 0) "$currencySymbol ${String.format("%,.2f", credit)}" else ""
                    )
                )
            )
            
            endingBalance = startingBalance
        }
        
        val summary = ReportSummary(
            totalAmount = party.balance,
            recordCount = rows.size,
            additionalInfo = mapOf(
                "Current Balance" to String.format("%,.2f", party.balance),
                "Total Debit" to String.format("%,.2f", totalDebit),
                "Total Credit" to String.format("%,.2f", totalCredit)
            )
        )
        
        return Triple(columns, rows, summary)
    }
    
    /**
     * Generate OVERALL report - product-wise summary
     */
    private fun generateOverallReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        allDetails: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        productMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.ProductEntity>,
        currencySymbol: String,
        reportType: ReportType
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Product", "Quantity Sold", "Returned", "Net Sold", "Total Amount")
        val rows = mutableListOf<ReportRow>()
        
        // Group by product
        val productStatsMap = mutableMapOf<String, ProductStats>()
        
        allDetails.forEach { detail ->
            val productSlug = detail.product_slug ?: ""
            if (productSlug.isEmpty()) return@forEach
            
            val stats = productStatsMap.getOrPut(productSlug) {
                ProductStats(productSlug)
            }
            
            val transaction = transactions.find { it.slug == detail.transaction_slug }
            if (transaction != null) {
                when (reportType) {
                    ReportType.CUSTOMER_REPORT -> {
                        when (transaction.transaction_type) {
                            AllTransactionTypes.SALE.value -> {
                                stats.quantitySold += detail.quantity
                                stats.totalAmount += detail.price * detail.quantity
                            }
                            AllTransactionTypes.CUSTOMER_RETURN.value -> {
                                stats.quantityReturned += detail.quantity
                                stats.totalAmount -= detail.price * detail.quantity
                            }
                        }
                    }
                    ReportType.VENDOR_REPORT -> {
                        when (transaction.transaction_type) {
                            AllTransactionTypes.PURCHASE.value -> {
                                stats.quantitySold += detail.quantity
                                stats.totalAmount += detail.price * detail.quantity
                            }
                            AllTransactionTypes.VENDOR_RETURN.value -> {
                                stats.quantityReturned += detail.quantity
                                stats.totalAmount -= detail.price * detail.quantity
                            }
                        }
                    }
                    ReportType.INVESTOR_REPORT -> {
                        // For investor, track all transactions
                        when (transaction.transaction_type) {
                            AllTransactionTypes.SALE.value,
                            AllTransactionTypes.PURCHASE.value -> {
                                stats.quantitySold += detail.quantity
                                stats.totalAmount += detail.price * detail.quantity
                            }
                            AllTransactionTypes.CUSTOMER_RETURN.value,
                            AllTransactionTypes.VENDOR_RETURN.value -> {
                                stats.quantityReturned += detail.quantity
                                stats.totalAmount -= detail.price * detail.quantity
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
        
        var totalQuantitySold = 0.0
        var totalReturned = 0.0
        var totalAmount = 0.0
        
        productStatsMap.values.forEach { stats ->
            val product = productMap[stats.productSlug]
            val productTitle = product?.title ?: "Unknown Product"
            
            totalQuantitySold += stats.quantitySold
            totalReturned += stats.quantityReturned
            totalAmount += stats.totalAmount
            
            rows.add(
                ReportRow(
                    id = stats.productSlug,
                    values = listOf(
                        productTitle,
                        String.format("%.2f", stats.quantitySold),
                        String.format("%.2f", stats.quantityReturned),
                        String.format("%.2f", stats.netQuantitySold),
                        "$currencySymbol ${String.format("%,.2f", stats.totalAmount)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalAmount,
            totalQuantity = totalQuantitySold - totalReturned,
            recordCount = rows.size,
            additionalInfo = mapOf(
                "Total Quantity Sold" to String.format("%.2f", totalQuantitySold),
                "Total Returned" to String.format("%.2f", totalReturned)
            )
        )
        
        return Triple(columns, rows, summary)
    }
    
    /**
     * Generate interval-based report (Daily, Weekly, Monthly, Yearly)
     */
    private fun generateIntervalReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        allDetails: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        currencySymbol: String,
        intervalType: String,
        reportType: ReportType
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Date", "Quantity Sold", "Returned", "Net Sold", "Total Amount")
        val rows = mutableListOf<ReportRow>()
        
        // Group by date interval
        val intervalMap = mutableMapOf<String, IntervalStats>()
        
        transactions.forEach { transaction ->
            val dateKey = getDateKey(transaction.created_at, intervalType)
            val stats = intervalMap.getOrPut(dateKey) {
                IntervalStats(dateKey)
            }
            
            val details = allDetails.filter { it.transaction_slug == transaction.slug }
            val quantity = details.sumOf { it.quantity }
            val amount = transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
            
            when (reportType) {
                ReportType.CUSTOMER_REPORT -> {
                    when (transaction.transaction_type) {
                        AllTransactionTypes.SALE.value -> {
                            stats.quantitySold += quantity
                            stats.totalAmount += amount
                        }
                        AllTransactionTypes.CUSTOMER_RETURN.value -> {
                            stats.quantityReturned += quantity
                            stats.totalAmount -= amount
                        }
                    }
                }
                ReportType.VENDOR_REPORT -> {
                    when (transaction.transaction_type) {
                        AllTransactionTypes.PURCHASE.value -> {
                            stats.quantitySold += quantity
                            stats.totalAmount += amount
                        }
                        AllTransactionTypes.VENDOR_RETURN.value -> {
                            stats.quantityReturned += quantity
                            stats.totalAmount -= amount
                        }
                    }
                }
                ReportType.INVESTOR_REPORT -> {
                    when (transaction.transaction_type) {
                        AllTransactionTypes.SALE.value,
                        AllTransactionTypes.PURCHASE.value -> {
                            stats.quantitySold += quantity
                            stats.totalAmount += amount
                        }
                        AllTransactionTypes.CUSTOMER_RETURN.value,
                        AllTransactionTypes.VENDOR_RETURN.value -> {
                            stats.quantityReturned += quantity
                            stats.totalAmount -= amount
                        }
                    }
                }
                else -> {}
            }
        }
        
        // Sort by date (newest first)
        val sortedIntervals = intervalMap.values.sortedByDescending { it.dateKey }
        
        var totalQuantitySold = 0.0
        var totalReturned = 0.0
        var totalAmount = 0.0
        
        sortedIntervals.forEach { stats ->
            totalQuantitySold += stats.quantitySold
            totalReturned += stats.quantityReturned
            totalAmount += stats.totalAmount
            
            val displayDate = formatDateKey(stats.dateKey, intervalType)
            
            rows.add(
                ReportRow(
                    id = stats.dateKey,
                    values = listOf(
                        displayDate,
                        String.format("%.2f", stats.quantitySold),
                        String.format("%.2f", stats.quantityReturned),
                        String.format("%.2f", stats.quantitySold - stats.quantityReturned),
                        "$currencySymbol ${String.format("%,.2f", stats.totalAmount)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalAmount,
            totalQuantity = totalQuantitySold - totalReturned,
            recordCount = rows.size,
            additionalInfo = mapOf(
                "Total Quantity Sold" to String.format("%.2f", totalQuantitySold),
                "Total Returned" to String.format("%.2f", totalReturned)
            )
        )
        
        return Triple(columns, rows, summary)
    }
    
    /**
     * Calculate the effect of a transaction on party balance
     * For Customer: Positive = customer owes more (debit), Negative = customer owes less (credit)
     * For Vendor: Positive = we owe vendor more (debit), Negative = we owe vendor less (credit)
     * For Investor: Similar logic based on transaction type
     */
    private fun calculateTransactionEffect(
        transaction: com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity,
        reportType: ReportType?
    ): Double {
        return when (reportType) {
            ReportType.CUSTOMER_REPORT -> {
                when (transaction.transaction_type) {
                    AllTransactionTypes.SALE.value -> {
                        // Sale increases what customer owes (debit)
                        val grandTotal = transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                        grandTotal - transaction.total_paid
                    }
                    AllTransactionTypes.CUSTOMER_RETURN.value -> {
                        // Return decreases what customer owes (credit)
                        val grandTotal = transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                        -(grandTotal - transaction.total_paid)
                    }
                    AllTransactionTypes.GET_FROM_CUSTOMER.value -> {
                        // Getting payment from customer decreases what they owe (credit)
                        -transaction.total_paid
                    }
                    AllTransactionTypes.PAY_TO_CUSTOMER.value -> {
                        // Paying customer increases what they owe (debit)
                        transaction.total_paid
                    }
                    else -> 0.0
                }
            }
            ReportType.VENDOR_REPORT -> {
                when (transaction.transaction_type) {
                    AllTransactionTypes.PURCHASE.value -> {
                        // Purchase increases what we owe vendor (debit)
                        val grandTotal = transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                        grandTotal - transaction.total_paid
                    }
                    AllTransactionTypes.VENDOR_RETURN.value -> {
                        // Return decreases what we owe vendor (credit)
                        val grandTotal = transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                        -(grandTotal - transaction.total_paid)
                    }
                    AllTransactionTypes.PAY_TO_VENDOR.value -> {
                        // Paying vendor decreases what we owe (credit)
                        -transaction.total_paid
                    }
                    AllTransactionTypes.GET_FROM_VENDOR.value -> {
                        // Getting from vendor increases what we owe (debit)
                        transaction.total_paid
                    }
                    else -> 0.0
                }
            }
            ReportType.INVESTOR_REPORT -> {
                // For investor, similar to customer logic
                when (transaction.transaction_type) {
                    AllTransactionTypes.SALE.value,
                    AllTransactionTypes.PURCHASE.value -> {
                        val grandTotal = transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                        grandTotal - transaction.total_paid
                    }
                    AllTransactionTypes.CUSTOMER_RETURN.value,
                    AllTransactionTypes.VENDOR_RETURN.value -> {
                        val grandTotal = transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                        -(grandTotal - transaction.total_paid)
                    }
                    AllTransactionTypes.GET_FROM_CUSTOMER.value,
                    AllTransactionTypes.PAY_TO_VENDOR.value -> {
                        -transaction.total_paid
                    }
                    AllTransactionTypes.PAY_TO_CUSTOMER.value,
                    AllTransactionTypes.GET_FROM_VENDOR.value -> {
                        transaction.total_paid
                    }
                    else -> 0.0
                }
            }
            else -> 0.0
        }
    }
    
    private fun getDateKey(timestamp: String?, intervalType: String): String {
        if (timestamp == null) return "Unknown"
        
        return try {
            val instant = Instant.parse(timestamp)
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
    private data class ProductStats(
        val productSlug: String,
        var quantitySold: Double = 0.0,
        var quantityReturned: Double = 0.0,
        var totalAmount: Double = 0.0
    ) {
        val netQuantitySold: Double
            get() = quantitySold - quantityReturned
    }
    
    private data class IntervalStats(
        val dateKey: String,
        var quantitySold: Double = 0.0,
        var quantityReturned: Double = 0.0,
        var totalAmount: Double = 0.0
    )
}

