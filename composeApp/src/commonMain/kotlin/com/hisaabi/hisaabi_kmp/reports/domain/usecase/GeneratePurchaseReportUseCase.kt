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
 * Use case for generating purchase reports from actual database
 */
class GeneratePurchaseReportUseCase(
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
        
        // Get purchase and vendor return transactions
        val transactionTypes = listOf(
            AllTransactionTypes.PURCHASE.value,
            AllTransactionTypes.VENDOR_RETURN.value
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
        
        // Get products, parties, and categories for mapping
        val products = productDao.getProductsByBusiness(businessSlug).first()
        val productMap = products.associateBy { it.slug ?: "" }
        
        val parties = partyDao.getPartiesByBusiness(businessSlug).first()
        val partyMap = parties.associateBy { it.slug ?: "" }
        
        val allCategories = categoryDao.getCategoriesByBusiness(businessSlug).first()
        val categoryMap = allCategories.associateBy { it.slug ?: "" }
        
        // Group transactions by the specified grouping
        val groupBy = filters.groupBy ?: ReportGroupBy.PRODUCT
        
        val (columns, rows, summary) = when (groupBy) {
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
        
        return ReportResult(
            reportType = ReportType.PURCHASE_REPORT,
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
        val columns = listOf("Product", "Purchased Qty", "Returned Qty", "Total Amount")
        val rows = mutableListOf<ReportRow>()
        
        // Group details by product
        val groupedByProduct = allDetails.groupBy { it.product_slug ?: "" }
            .filterKeys { it.isNotEmpty() }
        
        var totalPurchasedQty = 0.0
        var totalReturnedQty = 0.0
        var totalAmount = 0.0
        
        groupedByProduct.forEach { (productSlug, details) ->
            val product = productMap[productSlug]
            val productTitle = product?.title ?: "Unknown Product"
            
            var purchasedQty = 0.0
            var returnedQty = 0.0
            var totalItemAmount = 0.0
            
            // Sum quantities and amounts by transaction type
            details.forEach { detail ->
                val transaction = transactions.find { it.slug == detail.transaction_slug }
                if (transaction != null) {
                    val detailAmount = detail.price * detail.quantity
                    when (transaction.transaction_type) {
                        AllTransactionTypes.PURCHASE.value -> {
                            purchasedQty += detail.quantity
                            totalItemAmount += detailAmount
                        }
                        AllTransactionTypes.VENDOR_RETURN.value -> {
                            returnedQty += detail.quantity
                            totalItemAmount -= detailAmount
                        }
                    }
                }
            }
            
            totalPurchasedQty += purchasedQty
            totalReturnedQty += returnedQty
            totalAmount += totalItemAmount
            
            rows.add(
                ReportRow(
                    id = productSlug,
                    values = listOf(
                        productTitle,
                        String.format("%.2f", purchasedQty),
                        String.format("%.2f", returnedQty),
                        "$currencySymbol ${String.format("%,.2f", totalItemAmount)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalAmount,
            totalQuantity = totalPurchasedQty - totalReturnedQty,
            recordCount = rows.size,
            additionalInfo = mapOf(
                "Total Purchased Qty" to String.format("%.2f", totalPurchasedQty),
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
        val columns = listOf("Category", "Purchased Qty", "Returned Qty", "Total Bill")
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
        
        var totalPurchasedQty = 0.0
        var totalReturnedQty = 0.0
        var totalBill = 0.0
        
        categoryGroups.forEach { (categorySlug, details) ->
            val category = categoryMap[categorySlug]
            val categoryTitle = category?.title ?: "Uncategorized"
            
            var purchasedQty = 0.0
            var returnedQty = 0.0
            var categoryTotalBill = 0.0
            
            details.forEach { detail ->
                val transaction = transactions.find { it.slug == detail.transaction_slug }
                if (transaction != null) {
                    val amount = detail.price * detail.quantity
                    when (transaction.transaction_type) {
                        AllTransactionTypes.PURCHASE.value -> {
                            purchasedQty += detail.quantity
                            categoryTotalBill += amount
                        }
                        AllTransactionTypes.VENDOR_RETURN.value -> {
                            returnedQty += detail.quantity
                            categoryTotalBill -= amount
                        }
                    }
                }
            }
            
            totalPurchasedQty += purchasedQty
            totalReturnedQty += returnedQty
            totalBill += categoryTotalBill
            
            rows.add(
                ReportRow(
                    id = categorySlug,
                    values = listOf(
                        categoryTitle,
                        String.format("%.2f", purchasedQty),
                        String.format("%.2f", returnedQty),
                        "$currencySymbol ${String.format("%,.2f", categoryTotalBill)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalBill,
            totalQuantity = totalPurchasedQty - totalReturnedQty,
            recordCount = rows.size,
            additionalInfo = mapOf(
                "Total Purchased Qty" to String.format("%.2f", totalPurchasedQty),
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
        val columns = listOf("Vendor", "Purchased Qty", "Returned Qty", "Total Bill")
        val rows = mutableListOf<ReportRow>()
        
        // Group details by party (vendor)
        val groupedByParty = transactions.groupBy { it.party_slug ?: "" }
            .filterKeys { it.isNotEmpty() }
        
        var totalPurchasedQty = 0.0
        var totalReturnedQty = 0.0
        var totalBill = 0.0
        
        groupedByParty.forEach { (partySlug, partyTransactions) ->
            val party = partyMap[partySlug]
            val partyName = party?.name ?: "Unknown Vendor"
            
            var purchasedQty = 0.0
            var returnedQty = 0.0
            var partyTotalBill = 0.0
            
            partyTransactions.forEach { transaction ->
                val details = allDetails.filter { it.transaction_slug == transaction.slug }
                
                when (transaction.transaction_type) {
                    AllTransactionTypes.PURCHASE.value -> {
                        val qty = details.sumOf { it.quantity }
                        purchasedQty += qty
                        partyTotalBill += transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                    }
                    AllTransactionTypes.VENDOR_RETURN.value -> {
                        val qty = details.sumOf { it.quantity }
                        returnedQty += qty
                        partyTotalBill -= transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                    }
                }
            }
            
            totalPurchasedQty += purchasedQty
            totalReturnedQty += returnedQty
            totalBill += partyTotalBill
            
            rows.add(
                ReportRow(
                    id = partySlug,
                    values = listOf(
                        partyName,
                        String.format("%.2f", purchasedQty),
                        String.format("%.2f", returnedQty),
                        "$currencySymbol ${String.format("%,.2f", partyTotalBill)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalBill,
            totalQuantity = totalPurchasedQty - totalReturnedQty,
            recordCount = rows.size,
            additionalInfo = mapOf(
                "Total Purchased Qty" to String.format("%.2f", totalPurchasedQty),
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
        val columns = listOf("Area", "Purchased Qty", "Returned Qty", "Total Bill")
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
        
        var totalPurchasedQty = 0.0
        var totalReturnedQty = 0.0
        var totalBill = 0.0
        
        areaGroups.forEach { (areaSlug, areaTransactions) ->
            val category = categoryMap[areaSlug]
            val areaName = category?.title ?: "Uncategorized Area"
            
            var purchasedQty = 0.0
            var returnedQty = 0.0
            var areaTotalBill = 0.0
            
            areaTransactions.forEach { transaction ->
                val details = allDetails.filter { it.transaction_slug == transaction.slug }
                
                when (transaction.transaction_type) {
                    AllTransactionTypes.PURCHASE.value -> {
                        val qty = details.sumOf { it.quantity }
                        purchasedQty += qty
                        areaTotalBill += transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                    }
                    AllTransactionTypes.VENDOR_RETURN.value -> {
                        val qty = details.sumOf { it.quantity }
                        returnedQty += qty
                        areaTotalBill -= transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                    }
                }
            }
            
            totalPurchasedQty += purchasedQty
            totalReturnedQty += returnedQty
            totalBill += areaTotalBill
            
            rows.add(
                ReportRow(
                    id = areaSlug,
                    values = listOf(
                        areaName,
                        String.format("%.2f", purchasedQty),
                        String.format("%.2f", returnedQty),
                        "$currencySymbol ${String.format("%,.2f", areaTotalBill)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalBill,
            totalQuantity = totalPurchasedQty - totalReturnedQty,
            recordCount = rows.size,
            additionalInfo = mapOf(
                "Total Purchased Qty" to String.format("%.2f", totalPurchasedQty),
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
        val columns = listOf("Category", "Purchased Qty", "Returned Qty", "Total Bill")
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
        
        var totalPurchasedQty = 0.0
        var totalReturnedQty = 0.0
        var totalBill = 0.0
        
        categoryGroups.forEach { (categorySlug, categoryTransactions) ->
            val category = categoryMap[categorySlug]
            val categoryName = category?.title ?: "Uncategorized"
            
            var purchasedQty = 0.0
            var returnedQty = 0.0
            var categoryTotalBill = 0.0
            
            categoryTransactions.forEach { transaction ->
                val details = allDetails.filter { it.transaction_slug == transaction.slug }
                
                when (transaction.transaction_type) {
                    AllTransactionTypes.PURCHASE.value -> {
                        val qty = details.sumOf { it.quantity }
                        purchasedQty += qty
                        categoryTotalBill += transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                    }
                    AllTransactionTypes.VENDOR_RETURN.value -> {
                        val qty = details.sumOf { it.quantity }
                        returnedQty += qty
                        categoryTotalBill -= transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                    }
                }
            }
            
            totalPurchasedQty += purchasedQty
            totalReturnedQty += returnedQty
            totalBill += categoryTotalBill
            
            rows.add(
                ReportRow(
                    id = categorySlug,
                    values = listOf(
                        categoryName,
                        String.format("%.2f", purchasedQty),
                        String.format("%.2f", returnedQty),
                        "$currencySymbol ${String.format("%,.2f", categoryTotalBill)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalBill,
            totalQuantity = totalPurchasedQty - totalReturnedQty,
            recordCount = rows.size,
            additionalInfo = mapOf(
                "Total Purchased Qty" to String.format("%.2f", totalPurchasedQty),
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
}

