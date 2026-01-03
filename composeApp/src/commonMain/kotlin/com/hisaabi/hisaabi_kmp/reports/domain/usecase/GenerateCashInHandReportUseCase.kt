package com.hisaabi.hisaabi_kmp.reports.domain.usecase

import com.hisaabi.hisaabi_kmp.business.data.datasource.BusinessPreferencesDataSource
import com.hisaabi.hisaabi_kmp.database.dao.InventoryTransactionDao
import com.hisaabi.hisaabi_kmp.database.dao.PartyDao
import com.hisaabi.hisaabi_kmp.database.dao.PaymentMethodDao
import com.hisaabi.hisaabi_kmp.reports.domain.model.*
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import com.hisaabi.hisaabi_kmp.utils.formatTransactionDateTime
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*
import kotlin.math.abs

/**
 * Use case for generating Cash in Hand Report
 * Based on the legacy Android Native CashInHandReportGenerator
 */
class GenerateCashInHandReportUseCase(
    private val transactionDao: InventoryTransactionDao,
    private val partyDao: PartyDao,
    private val paymentMethodDao: PaymentMethodDao,
    private val businessPreferences: BusinessPreferencesDataSource,
    private val preferencesManager: PreferencesManager
) {
    
    suspend fun execute(filters: ReportFilters): ReportResult {
        val currencySymbol = preferencesManager.getSelectedCurrency().symbol
        val businessSlug = businessPreferences.observeSelectedBusinessSlug().first()
            ?: throw IllegalStateException("No business selected")
        
        // Get selected payment method if HISTORY report
        val selectedPaymentMethodId = if (filters.additionalFilter == ReportAdditionalFilter.CASH_IN_HAND_HISTORY) {
            filters.selectedPaymentMethodId
        } else {
            null
        }
        
        // Get current cash in hand balance
        val currentCashInHand = if (selectedPaymentMethodId != null) {
            // Get balance for selected payment method
            val paymentMethod = paymentMethodDao.getPaymentMethodBySlug(selectedPaymentMethodId)
            paymentMethod?.amount ?: 0.0
        } else {
            paymentMethodDao.getTotalCashInHand(businessSlug) ?: 0.0
        }
        
        // Get all payment methods to identify cash transactions
        val paymentMethods = paymentMethodDao.getPaymentMethodsByBusiness(businessSlug).first()
        val cashPaymentMethodSlugs = if (selectedPaymentMethodId != null) {
            // Only use selected payment method for HISTORY
            setOf(selectedPaymentMethodId)
        } else {
            paymentMethods.mapNotNull { it.slug }.toSet()
        }
        
        // Calculate date range based on filters
        val (fromDateMillis, toDateMillis) = calculateDateRange(filters)
        
        // Get all transactions that affect cash in hand
        val allTransactionTypes = AllTransactionTypes.entries.map { it.value }
        val allTransactions = transactionDao.getTransactionsForReport(
            businessSlug = businessSlug,
            transactionTypes = allTransactionTypes,
            fromDate = fromDateMillis,
            toDate = toDateMillis
        )
        
        // Filter transactions that affect cash in hand
        // For HISTORY report with selected payment method, filter by that payment method
        val cashTransactions = if (selectedPaymentMethodId != null && filters.additionalFilter == ReportAdditionalFilter.CASH_IN_HAND_HISTORY) {
            allTransactions.filter { transaction ->
                // Check if transaction uses the selected payment method
                val usesSelectedPaymentMethod = transaction.payment_method_to_slug == selectedPaymentMethodId ||
                        transaction.payment_method_from_slug == selectedPaymentMethodId
                usesSelectedPaymentMethod && isCashTransaction(transaction, cashPaymentMethodSlugs)
            }
        } else {
            allTransactions.filter { transaction ->
                isCashTransaction(transaction, cashPaymentMethodSlugs)
            }
        }
        
        // Get parties for lookup
        val parties = partyDao.getPartiesByBusiness(businessSlug).first()
        val partyMap = parties.associateBy { it.slug ?: "" }
        
        // Generate report based on filter type
        val additionalFilter = filters.additionalFilter
        val (columns, rows, summary) = when (additionalFilter) {
            ReportAdditionalFilter.CASH_IN_HAND_HISTORY -> {
                if (selectedPaymentMethodId == null) {
                    throw IllegalStateException("Payment method selection is required for Cash in Hand History report")
                }
                generateHistoryReport(
                    cashTransactions, partyMap, currencySymbol, currentCashInHand, cashPaymentMethodSlugs
                )
            }
            ReportAdditionalFilter.DAILY -> generateIntervalReport(
                cashTransactions, currencySymbol, "DAILY", cashPaymentMethodSlugs
            )
            ReportAdditionalFilter.WEEKLY -> generateIntervalReport(
                cashTransactions, currencySymbol, "WEEKLY", cashPaymentMethodSlugs
            )
            ReportAdditionalFilter.MONTHLY -> generateIntervalReport(
                cashTransactions, currencySymbol, "MONTHLY", cashPaymentMethodSlugs
            )
            ReportAdditionalFilter.YEARLY -> generateIntervalReport(
                cashTransactions, currencySymbol, "YEARLY", cashPaymentMethodSlugs
            )
            else -> generateOverallReport(cashTransactions, currencySymbol)
        }
        
        return ReportResult(
            reportType = ReportType.CASH_IN_HAND,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = summary
        )
    }
    
    /**
     * Check if transaction affects cash in hand
     */
    private fun isCashTransaction(
        transaction: com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity,
        cashPaymentMethodSlugs: Set<String>
    ): Boolean {
        // Transactions that always affect cash (if they have totalPaid > 0)
        val alwaysCashTypes = listOf(
            AllTransactionTypes.SALE.value,
            AllTransactionTypes.CUSTOMER_RETURN.value,
            AllTransactionTypes.PURCHASE.value,
            AllTransactionTypes.VENDOR_RETURN.value,
            AllTransactionTypes.PAY_TO_VENDOR.value,
            AllTransactionTypes.GET_FROM_VENDOR.value,
            AllTransactionTypes.PAY_TO_CUSTOMER.value,
            AllTransactionTypes.GET_FROM_CUSTOMER.value,
            AllTransactionTypes.EXPENSE.value,
            AllTransactionTypes.EXTRA_INCOME.value,
            AllTransactionTypes.INVESTMENT_DEPOSIT.value,
            AllTransactionTypes.INVESTMENT_WITHDRAW.value
        )
        
        if (transaction.transaction_type in alwaysCashTypes && transaction.total_paid > 0) {
            return true
        }
        
        // Payment transfer affects cash if payment method is cash
        if (transaction.transaction_type == AllTransactionTypes.PAYMENT_TRANSFER.value) {
            val paymentMethodTo = transaction.payment_method_to_slug
            val paymentMethodFrom = transaction.payment_method_from_slug
            return (paymentMethodTo != null && paymentMethodTo in cashPaymentMethodSlugs) ||
                   (paymentMethodFrom != null && paymentMethodFrom in cashPaymentMethodSlugs)
        }
        
        return false
    }
    
    /**
     * Check if transaction is credit (increases cash) or debit (decreases cash)
     */
    private fun isCreditInCashInHand(
        transaction: com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity,
        cashPaymentMethodSlugs: Set<String>
    ): Boolean {
        return when (transaction.transaction_type) {
            AllTransactionTypes.SALE.value -> true
            AllTransactionTypes.VENDOR_RETURN.value -> true
            AllTransactionTypes.GET_FROM_VENDOR.value -> true
            AllTransactionTypes.GET_FROM_CUSTOMER.value -> true
            AllTransactionTypes.EXTRA_INCOME.value -> true
            AllTransactionTypes.INVESTMENT_DEPOSIT.value -> true
            AllTransactionTypes.CUSTOMER_RETURN.value -> false
            AllTransactionTypes.PURCHASE.value -> false
            AllTransactionTypes.PAY_TO_VENDOR.value -> false
            AllTransactionTypes.PAY_TO_CUSTOMER.value -> false
            AllTransactionTypes.EXPENSE.value -> false
            AllTransactionTypes.INVESTMENT_WITHDRAW.value -> false
            AllTransactionTypes.PAYMENT_TRANSFER.value -> {
                // Credit if money is transferred TO cash payment method
                val paymentMethodTo = transaction.payment_method_to_slug
                paymentMethodTo != null && paymentMethodTo in cashPaymentMethodSlugs
            }
            else -> false
        }
    }
    
    /**
     * Generate CASH_IN_HAND_HISTORY report - transaction history with running balance
     */
    private fun generateHistoryReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        partyMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.PartyEntity>,
        currencySymbol: String,
        currentCashInHand: Double,
        cashPaymentMethodSlugs: Set<String>
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Name", "Transaction ID", "Date", "Type", "Debit/Credit", "Balance")
        val rows = mutableListOf<ReportRow>()
        
        // Sort transactions by date (newest first for history calculation)
        val sortedTransactions = transactions.sortedByDescending { 
            it.timestamp?.toLongOrNull() ?: 0L 
        }
        
        // Calculate running balance (starting from current cash in hand)
        var runningBalance = currentCashInHand
        
        // Process transactions in reverse order (oldest to newest) to calculate balance
        sortedTransactions.reversed().forEach { transaction ->
            if (transaction.total_paid == 0.0) return@forEach
            
            val isCredit = isCreditInCashInHand(transaction, cashPaymentMethodSlugs)
            val amount = transaction.total_paid
            
            // Update running balance
            if (isCredit) {
                runningBalance -= amount // Subtract because we're going backwards
            } else {
                runningBalance += amount // Add because we're going backwards
            }
            
            // Get party name
            val party = transaction.party_slug?.let { partyMap[it] }
            val partyName = party?.name ?: ""
            
            // Get transaction type name
            val transactionTypeName = AllTransactionTypes.fromValue(transaction.transaction_type)
                ?.displayName ?: "Unknown"
            
            // Format date
            val dateStr = formatTransactionDateTime(transaction.timestamp)
            
            // Format debit/credit with + for credit (money in) and - for debit (money out)
            val debitCreditStr = if (isCredit) {
                "+$currencySymbol ${String.format("%,.2f", amount)}"
            } else {
                "-$currencySymbol ${String.format("%,.2f", amount)}"
            }
            
            // Format balance
            val balanceStr = "$currencySymbol ${String.format("%,.2f", runningBalance)}"
            
            rows.add(
                ReportRow(
                    id = transaction.slug ?: transaction.id.toString(),
                    values = listOf(
                        partyName,
                        transaction.slug ?: "",
                        dateStr,
                        transactionTypeName,
                        debitCreditStr,
                        balanceStr
                    )
                )
            )
        }
        
        // Reverse rows to show newest first
        val finalRows = rows.reversed()
        
        val summary = ReportSummary(
            totalAmount = currentCashInHand,
            recordCount = finalRows.size,
            additionalInfo = mapOf(
                "Current Cash in Hand" to "$currencySymbol ${String.format("%,.2f", currentCashInHand)}"
            )
        )
        
        return Triple(columns, finalRows, summary)
    }
    
    /**
     * Generate OVERALL report - summary by transaction type
     */
    private fun generateOverallReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        currencySymbol: String
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Transaction Type", "Amount")
        val rows = mutableListOf<ReportRow>()
        
        val stats = CashInHandStats()
        
        transactions.forEach { transaction ->
            stats.updateWith(transaction)
        }
        
        var totalAmount = 0.0
        
        // Received cash from sale
        if (stats.receivedCashFromSale > 0) {
            rows.add(
                ReportRow(
                    id = "sale",
                    values = listOf(
                        "Received Cash from Sale",
                        "$currencySymbol ${String.format("%,.2f", stats.receivedCashFromSale)}"
                    )
                )
            )
            totalAmount += stats.receivedCashFromSale
        }
        
        // Paid cash for purchase
        if (stats.paidCashOfPurchase > 0) {
            rows.add(
                ReportRow(
                    id = "purchase",
                    values = listOf(
                        "Paid Cash for Purchase",
                        "$currencySymbol ${String.format("%,.2f", stats.paidCashOfPurchase)}"
                    )
                )
            )
            totalAmount += stats.paidCashOfPurchase
        }
        
        // Paid cash for customer return
        if (stats.paidCashOfCustomerReturn > 0) {
            rows.add(
                ReportRow(
                    id = "customer_return",
                    values = listOf(
                        "Paid Cash for Customer Return",
                        "$currencySymbol ${String.format("%,.2f", stats.paidCashOfCustomerReturn)}"
                    )
                )
            )
            totalAmount += stats.paidCashOfCustomerReturn
        }
        
        // Received cash from vendor return
        if (stats.receivedCashOfReturnToVendor > 0) {
            rows.add(
                ReportRow(
                    id = "vendor_return",
                    values = listOf(
                        "Received Cash from Vendor Return",
                        "$currencySymbol ${String.format("%,.2f", stats.receivedCashOfReturnToVendor)}"
                    )
                )
            )
            totalAmount += stats.receivedCashOfReturnToVendor
        }
        
        // Cash received from customer
        if (stats.cashReceivedFromCustomer > 0) {
            rows.add(
                ReportRow(
                    id = "get_from_customer",
                    values = listOf(
                        "Cash Received from Customer",
                        "$currencySymbol ${String.format("%,.2f", stats.cashReceivedFromCustomer)}"
                    )
                )
            )
            totalAmount += stats.cashReceivedFromCustomer
        }
        
        // Cash paid to customer
        if (stats.cashPaidToCustomer > 0) {
            rows.add(
                ReportRow(
                    id = "pay_to_customer",
                    values = listOf(
                        "Cash Paid to Customer",
                        "$currencySymbol ${String.format("%,.2f", stats.cashPaidToCustomer)}"
                    )
                )
            )
            totalAmount += stats.cashPaidToCustomer
        }
        
        // Cash received from vendor
        if (stats.cashReceivedFromVendor > 0) {
            rows.add(
                ReportRow(
                    id = "get_from_vendor",
                    values = listOf(
                        "Cash Received from Vendor",
                        "$currencySymbol ${String.format("%,.2f", stats.cashReceivedFromVendor)}"
                    )
                )
            )
            totalAmount += stats.cashReceivedFromVendor
        }
        
        // Cash paid to vendor
        if (stats.cashPaidToVendor > 0) {
            rows.add(
                ReportRow(
                    id = "pay_to_vendor",
                    values = listOf(
                        "Cash Paid to Vendor",
                        "$currencySymbol ${String.format("%,.2f", stats.cashPaidToVendor)}"
                    )
                )
            )
            totalAmount += stats.cashPaidToVendor
        }
        
        // Cash paid for expenses
        if (stats.cashPaidForExpenses > 0) {
            rows.add(
                ReportRow(
                    id = "expense",
                    values = listOf(
                        "Cash Paid for Expenses",
                        "$currencySymbol ${String.format("%,.2f", stats.cashPaidForExpenses)}"
                    )
                )
            )
            totalAmount += stats.cashPaidForExpenses
        }
        
        // Cash received from extra income
        if (stats.cashReceivedFromEIncome > 0) {
            rows.add(
                ReportRow(
                    id = "extra_income",
                    values = listOf(
                        "Cash Received from Extra Income",
                        "$currencySymbol ${String.format("%,.2f", stats.cashReceivedFromEIncome)}"
                    )
                )
            )
            totalAmount += stats.cashReceivedFromEIncome
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
        intervalType: String,
        cashPaymentMethodSlugs: Set<String>
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Date", "Debit/Credit")
        val rows = mutableListOf<ReportRow>()
        
        // Group by date interval
        val intervalMap = mutableMapOf<String, IntervalStats>()
        
        transactions.forEach { transaction ->
            if (transaction.total_paid == 0.0) return@forEach
            
            val dateKey = getDateKey(transaction.timestamp, intervalType)
            val stats = intervalMap.getOrPut(dateKey) {
                IntervalStats(dateKey)
            }
            
            // Add or subtract based on credit/debit
            // For interval reports, we show net cash change
            val isCredit = isCreditInCashInHand(transaction, cashPaymentMethodSlugs)
            if (isCredit) {
                stats.netCash += transaction.total_paid
            } else {
                stats.netCash -= transaction.total_paid
            }
            
            transaction.slug?.let { stats.transactionIds.add(it) }
        }
        
        // Sort by date (newest first)
        val sortedIntervals = intervalMap.values.sortedByDescending { it.dateKey }
        
        var totalNetCash = 0.0
        
        sortedIntervals.forEach { stats ->
            totalNetCash += stats.netCash
            
            val displayDate = formatDateKey(stats.dateKey, intervalType)
            
            // Format debit/credit
            // Positive = Debit (Db) = money coming in
            // Negative = Credit (Cr) = money going out
            val amountStr = if (stats.netCash > 0) {
                "Db $currencySymbol ${String.format("%,.2f", stats.netCash)}"
            } else {
                "Cr $currencySymbol ${String.format("%,.2f", abs(stats.netCash))}"
            }
            
            rows.add(
                ReportRow(
                    id = stats.dateKey,
                    values = listOf(
                        displayDate,
                        amountStr
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalNetCash,
            recordCount = rows.size,
            additionalInfo = emptyMap()
        )
        
        return Triple(columns, rows, summary)
    }
    
    private fun getDateKey(timestamp: String?, intervalType: String): String {
        if (timestamp == null) return "Unknown"
        
        return try {
            val timestampMillis = timestamp.toLongOrNull() ?: return "Unknown"
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
    private data class CashInHandStats(
        var receivedCashFromSale: Double = 0.0,
        var paidCashOfPurchase: Double = 0.0,
        var paidCashOfCustomerReturn: Double = 0.0,
        var receivedCashOfReturnToVendor: Double = 0.0,
        var cashReceivedFromCustomer: Double = 0.0,
        var cashPaidToCustomer: Double = 0.0,
        var cashReceivedFromVendor: Double = 0.0,
        var cashPaidToVendor: Double = 0.0,
        var cashPaidForExpenses: Double = 0.0,
        var cashReceivedFromEIncome: Double = 0.0
    ) {
        fun updateWith(transaction: com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity) {
            when (transaction.transaction_type) {
                AllTransactionTypes.CUSTOMER_RETURN.value -> {
                    paidCashOfCustomerReturn += transaction.total_paid
                }
                AllTransactionTypes.SALE.value -> {
                    receivedCashFromSale += transaction.total_paid
                }
                AllTransactionTypes.PURCHASE.value -> {
                    paidCashOfPurchase += transaction.total_paid
                }
                AllTransactionTypes.VENDOR_RETURN.value -> {
                    receivedCashOfReturnToVendor += transaction.total_paid
                }
                AllTransactionTypes.PAY_TO_VENDOR.value -> {
                    cashPaidToVendor += transaction.total_paid
                }
                AllTransactionTypes.GET_FROM_VENDOR.value -> {
                    cashReceivedFromVendor += transaction.total_paid
                }
                AllTransactionTypes.PAY_TO_CUSTOMER.value -> {
                    cashPaidToCustomer += transaction.total_paid
                }
                AllTransactionTypes.GET_FROM_CUSTOMER.value -> {
                    cashReceivedFromCustomer += transaction.total_paid
                }
                AllTransactionTypes.EXPENSE.value -> {
                    cashPaidForExpenses += transaction.total_paid
                }
                AllTransactionTypes.EXTRA_INCOME.value -> {
                    cashReceivedFromEIncome += transaction.total_paid
                }
            }
        }
    }
    
    private data class IntervalStats(
        val dateKey: String,
        var netCash: Double = 0.0,
        val transactionIds: MutableList<String> = mutableListOf()
    )
}
