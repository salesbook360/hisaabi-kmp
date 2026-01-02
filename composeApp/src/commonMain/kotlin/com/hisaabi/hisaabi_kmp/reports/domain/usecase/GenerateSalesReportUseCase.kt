package com.hisaabi.hisaabi_kmp.reports.domain.usecase

import com.hisaabi.hisaabi_kmp.business.data.datasource.BusinessPreferencesDataSource
import com.hisaabi.hisaabi_kmp.database.dao.CategoryDao
import com.hisaabi.hisaabi_kmp.database.dao.InventoryTransactionDao
import com.hisaabi.hisaabi_kmp.database.dao.PartyDao
import com.hisaabi.hisaabi_kmp.database.dao.ProductDao
import com.hisaabi.hisaabi_kmp.database.dao.TransactionDetailDao
import com.hisaabi.hisaabi_kmp.reports.domain.model.*
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*

/**
 * Use case for generating sales reports from actual database
 */
class GenerateSalesReportUseCase(
    private val transactionDao: InventoryTransactionDao,
    private val transactionDetailDao: TransactionDetailDao,
    private val productDao: ProductDao,
    private val partyDao: PartyDao,
    private val categoryDao: CategoryDao,
    private val businessPreferences: BusinessPreferencesDataSource,
    private val preferencesManager: PreferencesManager
) {
    
    suspend fun execute(filters: ReportFilters): ReportResult {
        val currencySymbol = preferencesManager.getSelectedCurrency().symbol
        val businessSlug = businessPreferences.observeSelectedBusinessSlug().first()
            ?: throw IllegalStateException("No business selected")
        
        // Calculate date range based on filters
        val (fromDateMillis, toDateMillis) = calculateDateRange(filters)
        
        // Get sale and customer return transactions
        val transactionTypes = listOf(
            AllTransactionTypes.SALE.value,
            AllTransactionTypes.CUSTOMER_RETURN.value
        )
        
        val transactions = transactionDao.getTransactionsForReport(
            businessSlug = businessSlug,
            transactionTypes = transactionTypes,
            fromDate = fromDateMillis,
            toDate = toDateMillis
        )
        
        // Get transaction details for all transactions
        val transactionSlugs = transactions.mapNotNull { it.slug }
        val allDetails = transactionDetailDao.getDetailsByTransactionSlugs(transactionSlugs)
        
        // Check if it's a time-based report (Daily, Weekly, Monthly, Yearly)
        val additionalFilter = filters.additionalFilter
        val (columns, rows, summary) = when (additionalFilter) {
            ReportAdditionalFilter.DAILY -> generateDailyReport(transactions, allDetails, currencySymbol)
            ReportAdditionalFilter.WEEKLY -> generateWeeklyReport(transactions, allDetails, currencySymbol)
            ReportAdditionalFilter.MONTHLY -> generateMonthlyReport(transactions, allDetails, currencySymbol)
            ReportAdditionalFilter.YEARLY -> generateYearlyReport(transactions, allDetails, currencySymbol)
            else -> {
                // Group transactions by the specified grouping (OVERALL)
                val products = productDao.getProductsByBusiness(businessSlug).first()
                val productMap = products.associateBy { it.slug ?: "" }
                
                val parties = partyDao.getPartiesByBusiness(businessSlug).first()
                val partyMap = parties.associateBy { it.slug ?: "" }
                
                val allCategories = categoryDao.getCategoriesByBusiness(businessSlug).first()
                val categoryMap = allCategories.associateBy { it.slug ?: "" }
                
                val groupBy = filters.groupBy ?: ReportGroupBy.PRODUCT
                
                when (groupBy) {
                    ReportGroupBy.PRODUCT -> generateProductGroupedReport(
                        transactions, allDetails, productMap, currencySymbol
                    )
                    ReportGroupBy.PRODUCT_CATEGORY -> generateProductCategoryGroupedReport(
                        transactions, allDetails, productMap, categoryMap, currencySymbol
                    )
                    ReportGroupBy.PARTY -> generatePartyGroupedReport(
                        transactions, allDetails, partyMap, currencySymbol
                    )
                    ReportGroupBy.PARTY_AREA -> generatePartyAreaGroupedReport(
                        transactions, allDetails, partyMap, categoryMap, currencySymbol
                    )
                    ReportGroupBy.PARTY_CATEGORY -> generatePartyCategoryGroupedReport(
                        transactions, allDetails, partyMap, categoryMap, currencySymbol
                    )
                }
            }
        }
        
        return ReportResult(
            reportType = ReportType.SALE_REPORT,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = summary
        )
    }
    
    private fun generateProductGroupedReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        allDetails: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        productMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.ProductEntity>,
        currencySymbol: String
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Product", "Sold Qty", "Returned Qty", "Total Amount")
        val rows = mutableListOf<ReportRow>()
        
        // Group details by product
        val groupedByProduct = allDetails.groupBy { it.product_slug ?: "" }
            .filterKeys { it.isNotEmpty() }
        
        var totalSoldQty = 0.0
        var totalReturnedQty = 0.0
        var totalAmount = 0.0
        
        groupedByProduct.forEach { (productSlug, details) ->
            val product = productMap[productSlug]
            val productTitle = product?.title ?: "Unknown Product"
            
            var soldQty = 0.0
            var returnedQty = 0.0
            var totalItemAmount = 0.0
            
            // Sum quantities and amounts by transaction type
            details.forEach { detail ->
                val transaction = transactions.find { it.slug == detail.transaction_slug }
                if (transaction != null) {
                    val detailAmount = detail.price * detail.quantity
                    when (transaction.transaction_type) {
                        AllTransactionTypes.SALE.value -> {
                            soldQty += detail.quantity
                            totalItemAmount += detailAmount
                        }
                        AllTransactionTypes.CUSTOMER_RETURN.value -> {
                            returnedQty += detail.quantity
                            totalItemAmount -= detailAmount
                        }
                    }
                }
            }
            
            totalSoldQty += soldQty
            totalReturnedQty += returnedQty
            totalAmount += totalItemAmount
            
            rows.add(
                ReportRow(
                    id = productSlug,
                    values = listOf(
                        productTitle,
                        String.format("%.2f", soldQty),
                        String.format("%.2f", returnedQty),
                        "$currencySymbol ${String.format("%,.2f", totalItemAmount)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalAmount,
            totalQuantity = totalSoldQty - totalReturnedQty,
            recordCount = rows.size,
            additionalInfo = mapOf(
                "Total Sold Qty" to String.format("%.2f", totalSoldQty),
                "Total Returned Qty" to String.format("%.2f", totalReturnedQty)
            )
        )
        
        return Triple(columns, rows, summary)
    }
    
    private fun generateProductCategoryGroupedReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        allDetails: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        productMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.ProductEntity>,
        categoryMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity>,
        currencySymbol: String
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Category", "Sold Qty", "Returned Qty", "Total Bill")
        val rows = mutableListOf<ReportRow>()
        
        // Group by category
        val categoryGroups = mutableMapOf<String, MutableList<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>>()
        
        allDetails.forEach { detail ->
            val product = productMap[detail.product_slug ?: ""]
            val categorySlug = product?.category_slug ?: ""
            if (categorySlug.isNotEmpty()) {
                categoryGroups.getOrPut(categorySlug) { mutableListOf() }.add(detail)
            }
        }
        
        var totalSoldQty = 0.0
        var totalReturnedQty = 0.0
        var totalBill = 0.0
        
        categoryGroups.forEach { (categorySlug, details) ->
            val category = categoryMap[categorySlug]
            val categoryTitle = category?.title ?: "Uncategorized"
            
            var soldQty = 0.0
            var returnedQty = 0.0
            var categoryTotalBill = 0.0
            
            details.forEach { detail ->
                val transaction = transactions.find { it.slug == detail.transaction_slug }
                if (transaction != null) {
                    val amount = detail.price * detail.quantity
                    when (transaction.transaction_type) {
                        AllTransactionTypes.SALE.value -> {
                            soldQty += detail.quantity
                            categoryTotalBill += amount
                        }
                        AllTransactionTypes.CUSTOMER_RETURN.value -> {
                            returnedQty += detail.quantity
                            categoryTotalBill -= amount
                        }
                    }
                }
            }
            
            totalSoldQty += soldQty
            totalReturnedQty += returnedQty
            totalBill += categoryTotalBill
            
            rows.add(
                ReportRow(
                    id = categorySlug,
                    values = listOf(
                        categoryTitle,
                        String.format("%.2f", soldQty),
                        String.format("%.2f", returnedQty),
                        "$currencySymbol ${String.format("%,.2f", categoryTotalBill)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalBill,
            totalQuantity = totalSoldQty - totalReturnedQty,
            recordCount = rows.size,
            additionalInfo = mapOf(
                "Total Sold Qty" to String.format("%.2f", totalSoldQty),
                "Total Returned Qty" to String.format("%.2f", totalReturnedQty)
            )
        )
        
        return Triple(columns, rows, summary)
    }
    
    private fun generatePartyGroupedReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        allDetails: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        partyMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.PartyEntity>,
        currencySymbol: String
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Customer", "Sold Qty", "Returned Qty", "Total Bill")
        val rows = mutableListOf<ReportRow>()
        
        // Group details by party (customer)
        val groupedByParty = transactions.groupBy { it.party_slug ?: "" }
            .filterKeys { it.isNotEmpty() }
        
        var totalSoldQty = 0.0
        var totalReturnedQty = 0.0
        var totalBill = 0.0
        
        groupedByParty.forEach { (partySlug, partyTransactions) ->
            val party = partyMap[partySlug]
            val partyName = party?.name ?: "Unknown Customer"
            
            var soldQty = 0.0
            var returnedQty = 0.0
            var partyTotalBill = 0.0
            
            partyTransactions.forEach { transaction ->
                val details = allDetails.filter { it.transaction_slug == transaction.slug }
                
                when (transaction.transaction_type) {
                    AllTransactionTypes.SALE.value -> {
                        val qty = details.sumOf { it.quantity }
                        soldQty += qty
                        partyTotalBill += transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                    }
                    AllTransactionTypes.CUSTOMER_RETURN.value -> {
                        val qty = details.sumOf { it.quantity }
                        returnedQty += qty
                        partyTotalBill -= transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                    }
                }
            }
            
            totalSoldQty += soldQty
            totalReturnedQty += returnedQty
            totalBill += partyTotalBill
            
            rows.add(
                ReportRow(
                    id = partySlug,
                    values = listOf(
                        partyName,
                        String.format("%.2f", soldQty),
                        String.format("%.2f", returnedQty),
                        "$currencySymbol ${String.format("%,.2f", partyTotalBill)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalBill,
            totalQuantity = totalSoldQty - totalReturnedQty,
            recordCount = rows.size,
            additionalInfo = mapOf(
                "Total Sold Qty" to String.format("%.2f", totalSoldQty),
                "Total Returned Qty" to String.format("%.2f", totalReturnedQty)
            )
        )
        
        return Triple(columns, rows, summary)
    }
    
    private fun generatePartyAreaGroupedReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        allDetails: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        partyMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.PartyEntity>,
        categoryMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity>,
        currencySymbol: String
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Area", "Sold Qty", "Returned Qty", "Total Bill")
        val rows = mutableListOf<ReportRow>()
        
        // Group by area
        val areaGroups = mutableMapOf<String, MutableList<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>>()
        
        transactions.forEach { transaction ->
            val party = partyMap[transaction.party_slug ?: ""]
            val areaSlug = party?.area_slug ?: ""
            if (areaSlug.isNotEmpty()) {
                areaGroups.getOrPut(areaSlug) { mutableListOf() }.add(transaction)
            }
        }
        
        var totalSoldQty = 0.0
        var totalReturnedQty = 0.0
        var totalBill = 0.0
        
        areaGroups.forEach { (areaSlug, areaTransactions) ->
            val category = categoryMap[areaSlug]
            val areaName = category?.title ?: "Uncategorized Area"
            
            var soldQty = 0.0
            var returnedQty = 0.0
            var areaTotalBill = 0.0
            
            areaTransactions.forEach { transaction ->
                val details = allDetails.filter { it.transaction_slug == transaction.slug }
                
                when (transaction.transaction_type) {
                    AllTransactionTypes.SALE.value -> {
                        val qty = details.sumOf { it.quantity }
                        soldQty += qty
                        areaTotalBill += transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                    }
                    AllTransactionTypes.CUSTOMER_RETURN.value -> {
                        val qty = details.sumOf { it.quantity }
                        returnedQty += qty
                        areaTotalBill -= transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                    }
                }
            }
            
            totalSoldQty += soldQty
            totalReturnedQty += returnedQty
            totalBill += areaTotalBill
            
            rows.add(
                ReportRow(
                    id = areaSlug,
                    values = listOf(
                        areaName,
                        String.format("%.2f", soldQty),
                        String.format("%.2f", returnedQty),
                        "$currencySymbol ${String.format("%,.2f", areaTotalBill)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalBill,
            totalQuantity = totalSoldQty - totalReturnedQty,
            recordCount = rows.size,
            additionalInfo = mapOf(
                "Total Sold Qty" to String.format("%.2f", totalSoldQty),
                "Total Returned Qty" to String.format("%.2f", totalReturnedQty)
            )
        )
        
        return Triple(columns, rows, summary)
    }
    
    private fun generatePartyCategoryGroupedReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        allDetails: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        partyMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.PartyEntity>,
        categoryMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity>,
        currencySymbol: String
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Category", "Sold Qty", "Returned Qty", "Total Bill")
        val rows = mutableListOf<ReportRow>()
        
        // Group by party category
        val categoryGroups = mutableMapOf<String, MutableList<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>>()
        
        transactions.forEach { transaction ->
            val party = partyMap[transaction.party_slug ?: ""]
            val categorySlug = party?.category_slug ?: ""
            if (categorySlug.isNotEmpty()) {
                categoryGroups.getOrPut(categorySlug) { mutableListOf() }.add(transaction)
            }
        }
        
        var totalSoldQty = 0.0
        var totalReturnedQty = 0.0
        var totalBill = 0.0
        
        categoryGroups.forEach { (categorySlug, categoryTransactions) ->
            val category = categoryMap[categorySlug]
            val categoryName = category?.title ?: "Uncategorized"
            
            var soldQty = 0.0
            var returnedQty = 0.0
            var categoryTotalBill = 0.0
            
            categoryTransactions.forEach { transaction ->
                val details = allDetails.filter { it.transaction_slug == transaction.slug }
                
                when (transaction.transaction_type) {
                    AllTransactionTypes.SALE.value -> {
                        val qty = details.sumOf { it.quantity }
                        soldQty += qty
                        categoryTotalBill += transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                    }
                    AllTransactionTypes.CUSTOMER_RETURN.value -> {
                        val qty = details.sumOf { it.quantity }
                        returnedQty += qty
                        categoryTotalBill -= transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                    }
                }
            }
            
            totalSoldQty += soldQty
            totalReturnedQty += returnedQty
            totalBill += categoryTotalBill
            
            rows.add(
                ReportRow(
                    id = categorySlug,
                    values = listOf(
                        categoryName,
                        String.format("%.2f", soldQty),
                        String.format("%.2f", returnedQty),
                        "$currencySymbol ${String.format("%,.2f", categoryTotalBill)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalBill,
            totalQuantity = totalSoldQty - totalReturnedQty,
            recordCount = rows.size,
            additionalInfo = mapOf(
                "Total Sold Qty" to String.format("%.2f", totalSoldQty),
                "Total Returned Qty" to String.format("%.2f", totalReturnedQty)
            )
        )
        
        return Triple(columns, rows, summary)
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
                // Use custom dates if provided, otherwise default to last 30 days
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
        }
        
        // Convert to milliseconds
        val startDateTime = LocalDateTime(startDate, LocalTime(0, 0, 0))
        val endDateTime = LocalDateTime(endDate, LocalTime(23, 59, 59))
        
        val startMillis = startDateTime.toInstant(timezone).toEpochMilliseconds()
        val endMillis = endDateTime.toInstant(timezone).toEpochMilliseconds()
        
        return startMillis to endMillis
    }
    
    private fun generateDailyReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        allDetails: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        currencySymbol: String
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Date", "Quantity", "Amount", "Paid")
        val rows = mutableListOf<ReportRow>()
        
        // Group transactions by date (day)
        val groupedByDate = transactions.groupBy { transaction ->
            val timestamp = transaction.timestamp?.toLongOrNull() ?: return@groupBy ""
            val instant = Instant.fromEpochMilliseconds(timestamp)
            val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            // Use date as key (YYYY-MM-DD format for grouping)
            "${dateTime.year}-${dateTime.monthNumber.toString().padStart(2, '0')}-${dateTime.dayOfMonth.toString().padStart(2, '0')}"
        }.filterKeys { it.isNotEmpty() }
        
        var totalQuantity = 0.0
        var totalAmount = 0.0
        var totalPaid = 0.0
        
        // Sort by date (ascending)
        val sortedEntries = groupedByDate.toList().sortedBy { it.first }
        
        sortedEntries.forEach { (dateKey, dayTransactions) ->
            var dayQuantity = 0.0
            var dayAmount = 0.0
            var dayPaid = 0.0
            
            dayTransactions.forEach { transaction ->
                val details = allDetails.filter { it.transaction_slug == transaction.slug }
                when (transaction.transaction_type) {
                    AllTransactionTypes.SALE.value -> {
                        val qty = details.sumOf { it.quantity }
                        dayQuantity += qty
                        val amount = transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                        dayAmount += amount
                        dayPaid += transaction.total_paid
                    }
                    AllTransactionTypes.CUSTOMER_RETURN.value -> {
                        val qty = details.sumOf { it.quantity }
                        dayQuantity -= qty
                        val amount = transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                        dayAmount -= amount
                        dayPaid -= transaction.total_paid
                    }
                }
            }
            
            totalQuantity += dayQuantity
            totalAmount += dayAmount
            totalPaid += dayPaid
            
            // Format date as "1 Jan", "2 Jan", etc.
            val dateParts = dateKey.split("-")
            val year = dateParts[0].toInt()
            val month = dateParts[1].toInt()
            val day = dateParts[2].toInt()
            val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val monthName = monthNames.getOrNull(month - 1) ?: ""
            val formattedDate = "$day $monthName"
            
            rows.add(
                ReportRow(
                    id = dateKey,
                    values = listOf(
                        formattedDate,
                        String.format("%.2f", dayQuantity),
                        "$currencySymbol ${String.format("%,.2f", dayAmount)}",
                        "$currencySymbol ${String.format("%,.2f", dayPaid)}"
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
    
    private fun generateWeeklyReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        allDetails: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        currencySymbol: String
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Date", "Quantity", "Amount", "Paid")
        val rows = mutableListOf<ReportRow>()
        
        // Group transactions by calendar month week (1-7, 8-14, 15-21, 22-28, etc.)
        val groupedByWeek = transactions.groupBy { transaction ->
            val timestamp = transaction.timestamp?.toLongOrNull() ?: return@groupBy ""
            val instant = Instant.fromEpochMilliseconds(timestamp)
            val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val date = dateTime.date
            
            // Calculate week start (first day of the week in calendar month: 1, 8, 15, 22)
            val dayOfMonth = date.dayOfMonth
            val weekStartDay = ((dayOfMonth - 1) / 7 * 7) + 1
            val weekStart = LocalDate(date.year, date.month, weekStartDay)
            
            // Use week start date as key
            "${weekStart.year}-${weekStart.monthNumber.toString().padStart(2, '0')}-${weekStart.dayOfMonth.toString().padStart(2, '0')}"
        }.filterKeys { it.isNotEmpty() }
        
        var totalQuantity = 0.0
        var totalAmount = 0.0
        var totalPaid = 0.0
        
        // Sort by week start date (ascending)
        val sortedEntries = groupedByWeek.toList().sortedBy { it.first }
        
        sortedEntries.forEach { (weekStartKey, weekTransactions) ->
            var weekQuantity = 0.0
            var weekAmount = 0.0
            var weekPaid = 0.0
            
            weekTransactions.forEach { transaction ->
                val details = allDetails.filter { it.transaction_slug == transaction.slug }
                when (transaction.transaction_type) {
                    AllTransactionTypes.SALE.value -> {
                        val qty = details.sumOf { it.quantity }
                        weekQuantity += qty
                        val amount = transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                        weekAmount += amount
                        weekPaid += transaction.total_paid
                    }
                    AllTransactionTypes.CUSTOMER_RETURN.value -> {
                        val qty = details.sumOf { it.quantity }
                        weekQuantity -= qty
                        val amount = transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                        weekAmount -= amount
                        weekPaid -= transaction.total_paid
                    }
                }
            }
            
            totalQuantity += weekQuantity
            totalAmount += weekAmount
            totalPaid += weekPaid
            
            // Format date range as "1 Jan - 7 Jan"
            val dateParts = weekStartKey.split("-")
            val year = dateParts[0].toInt()
            val month = dateParts[1].toInt()
            val day = dateParts[2].toInt()
            val weekStartDate = LocalDate(year, month, day)
            // Calculate week end (7 days from start, but cap at end of month)
            val monthEndDay = weekStartDate.dayOfMonth + 6
            val lastDayOfMonth = LocalDate(year, month, 1).plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).dayOfMonth
            val weekEndDay = minOf(monthEndDay, lastDayOfMonth)
            val weekEndDate = LocalDate(year, month, weekEndDay)
            
            val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val startMonthName = monthNames.getOrNull(month - 1) ?: ""
            val endMonthName = monthNames.getOrNull(weekEndDate.monthNumber - 1) ?: ""
            
            val formattedDate = if (weekStartDate.month == weekEndDate.month) {
                "${weekStartDate.dayOfMonth} $startMonthName - ${weekEndDate.dayOfMonth} $endMonthName"
            } else {
                "${weekStartDate.dayOfMonth} $startMonthName - ${weekEndDate.dayOfMonth} $endMonthName"
            }
            
            rows.add(
                ReportRow(
                    id = weekStartKey,
                    values = listOf(
                        formattedDate,
                        String.format("%.2f", weekQuantity),
                        "$currencySymbol ${String.format("%,.2f", weekAmount)}",
                        "$currencySymbol ${String.format("%,.2f", weekPaid)}"
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
    
    private fun generateMonthlyReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        allDetails: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        currencySymbol: String
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Date", "Quantity", "Amount", "Paid")
        val rows = mutableListOf<ReportRow>()
        
        // Group transactions by month
        val groupedByMonth = transactions.groupBy { transaction ->
            val timestamp = transaction.timestamp?.toLongOrNull() ?: return@groupBy ""
            val instant = Instant.fromEpochMilliseconds(timestamp)
            val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            // Use year-month as key (YYYY-MM format)
            "${dateTime.year}-${dateTime.monthNumber.toString().padStart(2, '0')}"
        }.filterKeys { it.isNotEmpty() }
        
        var totalQuantity = 0.0
        var totalAmount = 0.0
        var totalPaid = 0.0
        
        // Sort by month (ascending)
        val sortedEntries = groupedByMonth.toList().sortedBy { it.first }
        
        sortedEntries.forEach { (monthKey, monthTransactions) ->
            var monthQuantity = 0.0
            var monthAmount = 0.0
            var monthPaid = 0.0
            
            monthTransactions.forEach { transaction ->
                val details = allDetails.filter { it.transaction_slug == transaction.slug }
                when (transaction.transaction_type) {
                    AllTransactionTypes.SALE.value -> {
                        val qty = details.sumOf { it.quantity }
                        monthQuantity += qty
                        val amount = transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                        monthAmount += amount
                        monthPaid += transaction.total_paid
                    }
                    AllTransactionTypes.CUSTOMER_RETURN.value -> {
                        val qty = details.sumOf { it.quantity }
                        monthQuantity -= qty
                        val amount = transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                        monthAmount -= amount
                        monthPaid -= transaction.total_paid
                    }
                }
            }
            
            totalQuantity += monthQuantity
            totalAmount += monthAmount
            totalPaid += monthPaid
            
            // Format date as "Jan", "Feb", etc.
            val dateParts = monthKey.split("-")
            val month = dateParts[1].toInt()
            val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val monthName = monthNames.getOrNull(month - 1) ?: ""
            
            rows.add(
                ReportRow(
                    id = monthKey,
                    values = listOf(
                        monthName,
                        String.format("%.2f", monthQuantity),
                        "$currencySymbol ${String.format("%,.2f", monthAmount)}",
                        "$currencySymbol ${String.format("%,.2f", monthPaid)}"
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
    
    private fun generateYearlyReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        allDetails: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        currencySymbol: String
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Date", "Quantity", "Amount", "Paid")
        val rows = mutableListOf<ReportRow>()
        
        // Group transactions by year
        val groupedByYear = transactions.groupBy { transaction ->
            val timestamp = transaction.timestamp?.toLongOrNull() ?: return@groupBy ""
            val instant = Instant.fromEpochMilliseconds(timestamp)
            val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            // Use year as key
            dateTime.year.toString()
        }.filterKeys { it.isNotEmpty() }
        
        var totalQuantity = 0.0
        var totalAmount = 0.0
        var totalPaid = 0.0
        
        // Sort by year (ascending)
        val sortedEntries = groupedByYear.toList().sortedBy { it.first.toIntOrNull() ?: 0 }
        
        sortedEntries.forEach { (yearKey, yearTransactions) ->
            var yearQuantity = 0.0
            var yearAmount = 0.0
            var yearPaid = 0.0
            
            yearTransactions.forEach { transaction ->
                val details = allDetails.filter { it.transaction_slug == transaction.slug }
                when (transaction.transaction_type) {
                    AllTransactionTypes.SALE.value -> {
                        val qty = details.sumOf { it.quantity }
                        yearQuantity += qty
                        val amount = transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                        yearAmount += amount
                        yearPaid += transaction.total_paid
                    }
                    AllTransactionTypes.CUSTOMER_RETURN.value -> {
                        val qty = details.sumOf { it.quantity }
                        yearQuantity -= qty
                        val amount = transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                        yearAmount -= amount
                        yearPaid -= transaction.total_paid
                    }
                }
            }
            
            totalQuantity += yearQuantity
            totalAmount += yearAmount
            totalPaid += yearPaid
            
            rows.add(
                ReportRow(
                    id = yearKey,
                    values = listOf(
                        yearKey,
                        String.format("%.2f", yearQuantity),
                        "$currencySymbol ${String.format("%,.2f", yearAmount)}",
                        "$currencySymbol ${String.format("%,.2f", yearPaid)}"
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
}

