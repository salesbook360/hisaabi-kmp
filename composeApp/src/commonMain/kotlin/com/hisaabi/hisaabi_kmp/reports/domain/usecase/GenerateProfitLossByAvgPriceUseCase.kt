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
import com.hisaabi.hisaabi_kmp.transactions.domain.util.TransactionCalculator
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*
import kotlin.math.abs

/**
 * Use case for generating Profit & Loss report by Average Purchase Price
 * Based on the legacy Android Native AvgProfitLoseReportGenerator
 */
class GenerateProfitLossByAvgPriceUseCase(
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
        
        val transactionSettings = preferencesManager.getTransactionSettings()
        
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
        
        // Get products for average purchase price lookup
        val products = productDao.getProductsByBusiness(businessSlug).first()
        val productMap = products.associateBy { it.slug ?: "" }
        
        // Check if it's a time-based report (Daily, Weekly, Monthly, Yearly)
        val additionalFilter = filters.additionalFilter
        val (columns, rows, summary) = when (additionalFilter) {
            ReportAdditionalFilter.DAILY -> generateDailyReport(transactions, allDetails, productMap, currencySymbol, transactionSettings)
            ReportAdditionalFilter.WEEKLY -> generateWeeklyReport(transactions, allDetails, productMap, currencySymbol, transactionSettings)
            ReportAdditionalFilter.MONTHLY -> generateMonthlyReport(transactions, allDetails, productMap, currencySymbol, transactionSettings)
            ReportAdditionalFilter.YEARLY -> generateYearlyReport(transactions, allDetails, productMap, currencySymbol, transactionSettings)
            else -> {
                // Group transactions by the specified grouping (OVERALL)
                val parties = partyDao.getPartiesByBusiness(businessSlug).first()
                val partyMap = parties.associateBy { it.slug ?: "" }
                
                val allCategories = categoryDao.getCategoriesByBusiness(businessSlug).first()
                val categoryMap = allCategories.associateBy { it.slug ?: "" }
                
                val groupBy = filters.groupBy ?: ReportGroupBy.PRODUCT
                
                when (groupBy) {
                    ReportGroupBy.PRODUCT -> generateProductGroupedReport(
                        transactions, allDetails, productMap, currencySymbol, transactionSettings
                    )
                    ReportGroupBy.PRODUCT_CATEGORY -> generateProductCategoryGroupedReport(
                        transactions, allDetails, productMap, categoryMap, currencySymbol, transactionSettings
                    )
                    ReportGroupBy.PARTY -> generatePartyGroupedReport(
                        transactions, allDetails, productMap, partyMap, currencySymbol, transactionSettings
                    )
                    ReportGroupBy.PARTY_AREA -> generatePartyAreaGroupedReport(
                        transactions, allDetails, productMap, partyMap, categoryMap, currencySymbol, transactionSettings
                    )
                    ReportGroupBy.PARTY_CATEGORY -> generatePartyCategoryGroupedReport(
                        transactions, allDetails, productMap, partyMap, categoryMap, currencySymbol, transactionSettings
                    )
                }
            }
        }
        
        return ReportResult(
            reportType = ReportType.PROFIT_LOSS_REPORT,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = summary
        )
    }
    
    /**
     * Calculate profit using average purchase price
     * Profit = (sale_price - avg_purchase_price) * quantity
     */
    private fun calculateProfitWithAvgPrice(
        salePrice: Double,
        avgPurchasePrice: Double,
        quantity: Double,
        isReturn: Boolean = false
    ): Double {
        val profitPerUnit = salePrice - avgPurchasePrice
        val totalProfit = profitPerUnit * quantity
        return TransactionCalculator.roundTo2Decimal(if (isReturn) -totalProfit else totalProfit)
    }
    
    /**
     * Calculate purchase cost using average purchase price
     * Purchase Cost = avg_purchase_price * quantity
     */
    private fun calculatePurchaseCostWithAvgPrice(
        avgPurchasePrice: Double,
        quantity: Double
    ): Double {
        return TransactionCalculator.roundTo2Decimal(avgPurchasePrice * quantity)
    }
    
    private fun generateProductGroupedReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        allDetails: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        productMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.ProductEntity>,
        currencySymbol: String,
        transactionSettings: com.hisaabi.hisaabi_kmp.settings.domain.model.TransactionSettings
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Product", "Quantity Sold", "Total Sale Amount", "Profit")
        val rows = mutableListOf<ReportRow>()
        
        // Group details by product
        val groupedByProduct = allDetails.groupBy { it.product_slug ?: "" }
            .filterKeys { it.isNotEmpty() }
        
        var totalQuantity = 0.0
        var totalSaleAmount = 0.0
        var totalProfit = 0.0
        
        groupedByProduct.forEach { (productSlug, details) ->
            val product = productMap[productSlug]
            val productTitle = product?.title ?: "Unknown Product"
            val avgPurchasePrice = product?.avg_purchase_price ?: 0.0
            
            var quantitySold = 0.0
            var saleAmount = 0.0
            var profit = 0.0
            
            // Sum quantities and amounts by transaction type
            details.forEach { detail ->
                val transaction = transactions.find { it.slug == detail.transaction_slug }
                if (transaction != null) {
                    val detailAmount = detail.price * detail.quantity
                    val purchaseCost = calculatePurchaseCostWithAvgPrice(avgPurchasePrice, detail.quantity)
                    
                    when (transaction.transaction_type) {
                        AllTransactionTypes.SALE.value -> {
                            quantitySold += detail.quantity
                            saleAmount += detailAmount
                            profit += calculateProfitWithAvgPrice(
                                detail.price,
                                avgPurchasePrice,
                                detail.quantity,
                                isReturn = false
                            )
                        }
                        AllTransactionTypes.CUSTOMER_RETURN.value -> {
                            quantitySold -= detail.quantity
                            saleAmount -= detailAmount
                            profit += calculateProfitWithAvgPrice(
                                detail.price,
                                avgPurchasePrice,
                                detail.quantity,
                                isReturn = true
                            )
                        }
                    }
                }
            }
            
            totalQuantity += quantitySold
            totalSaleAmount += saleAmount
            totalProfit += profit
            
            rows.add(
                ReportRow(
                    id = productSlug,
                    values = listOf(
                        productTitle,
                        String.format("%.2f", quantitySold),
                        "$currencySymbol ${String.format("%,.2f", saleAmount)}",
                        "$currencySymbol ${String.format("%,.2f", profit)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalSaleAmount,
            totalQuantity = totalQuantity,
            totalProfit = totalProfit,
            recordCount = rows.size
        )
        
        return Triple(columns, rows, summary)
    }
    
    private fun generateProductCategoryGroupedReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        allDetails: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        productMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.ProductEntity>,
        categoryMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity>,
        currencySymbol: String,
        transactionSettings: com.hisaabi.hisaabi_kmp.settings.domain.model.TransactionSettings
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Category", "Quantity Sold", "Total Sale Amount", "Profit")
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
        
        var totalQuantity = 0.0
        var totalSaleAmount = 0.0
        var totalProfit = 0.0
        
        categoryGroups.forEach { (categorySlug, details) ->
            val category = categoryMap[categorySlug]
            val categoryTitle = category?.title ?: "Uncategorized"
            
            var quantitySold = 0.0
            var saleAmount = 0.0
            var profit = 0.0
            
            details.forEach { detail ->
                val transaction = transactions.find { it.slug == detail.transaction_slug }
                val product = productMap[detail.product_slug ?: ""]
                val avgPurchasePrice = product?.avg_purchase_price ?: 0.0
                
                if (transaction != null) {
                    val detailAmount = detail.price * detail.quantity
                    
                    when (transaction.transaction_type) {
                        AllTransactionTypes.SALE.value -> {
                            quantitySold += detail.quantity
                            saleAmount += detailAmount
                            profit += calculateProfitWithAvgPrice(
                                detail.price,
                                avgPurchasePrice,
                                detail.quantity,
                                isReturn = false
                            )
                        }
                        AllTransactionTypes.CUSTOMER_RETURN.value -> {
                            quantitySold -= detail.quantity
                            saleAmount -= detailAmount
                            profit += calculateProfitWithAvgPrice(
                                detail.price,
                                avgPurchasePrice,
                                detail.quantity,
                                isReturn = true
                            )
                        }
                    }
                }
            }
            
            totalQuantity += quantitySold
            totalSaleAmount += saleAmount
            totalProfit += profit
            
            rows.add(
                ReportRow(
                    id = categorySlug,
                    values = listOf(
                        categoryTitle,
                        String.format("%.2f", quantitySold),
                        "$currencySymbol ${String.format("%,.2f", saleAmount)}",
                        "$currencySymbol ${String.format("%,.2f", profit)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalSaleAmount,
            totalQuantity = totalQuantity,
            totalProfit = totalProfit,
            recordCount = rows.size
        )
        
        return Triple(columns, rows, summary)
    }
    
    private fun generatePartyGroupedReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        allDetails: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        productMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.ProductEntity>,
        partyMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.PartyEntity>,
        currencySymbol: String,
        transactionSettings: com.hisaabi.hisaabi_kmp.settings.domain.model.TransactionSettings
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Customer", "Quantity Sold", "Total Sale Amount", "Profit")
        val rows = mutableListOf<ReportRow>()
        
        // Group details by party (customer)
        val groupedByParty = transactions.groupBy { it.party_slug ?: "" }
            .filterKeys { it.isNotEmpty() }
        
        var totalQuantity = 0.0
        var totalSaleAmount = 0.0
        var totalProfit = 0.0
        
        groupedByParty.forEach { (partySlug, partyTransactions) ->
            val party = partyMap[partySlug]
            val partyName = party?.name ?: "Unknown Customer"
            
            var quantitySold = 0.0
            var saleAmount = 0.0
            var profit = 0.0
            
            partyTransactions.forEach { transaction ->
                val details = allDetails.filter { it.transaction_slug == transaction.slug }
                
                details.forEach { detail ->
                    val product = productMap[detail.product_slug ?: ""]
                    val avgPurchasePrice = product?.avg_purchase_price ?: 0.0
                    val detailAmount = detail.price * detail.quantity
                    
                    when (transaction.transaction_type) {
                        AllTransactionTypes.SALE.value -> {
                            quantitySold += detail.quantity
                            saleAmount += detailAmount
                            profit += calculateProfitWithAvgPrice(
                                detail.price,
                                avgPurchasePrice,
                                detail.quantity,
                                isReturn = false
                            )
                        }
                        AllTransactionTypes.CUSTOMER_RETURN.value -> {
                            quantitySold -= detail.quantity
                            saleAmount -= detailAmount
                            profit += calculateProfitWithAvgPrice(
                                detail.price,
                                avgPurchasePrice,
                                detail.quantity,
                                isReturn = true
                            )
                        }
                    }
                }
            }
            
            totalQuantity += quantitySold
            totalSaleAmount += saleAmount
            totalProfit += profit
            
            rows.add(
                ReportRow(
                    id = partySlug,
                    values = listOf(
                        partyName,
                        String.format("%.2f", quantitySold),
                        "$currencySymbol ${String.format("%,.2f", saleAmount)}",
                        "$currencySymbol ${String.format("%,.2f", profit)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalSaleAmount,
            totalQuantity = totalQuantity,
            totalProfit = totalProfit,
            recordCount = rows.size
        )
        
        return Triple(columns, rows, summary)
    }
    
    private fun generatePartyAreaGroupedReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        allDetails: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        productMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.ProductEntity>,
        partyMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.PartyEntity>,
        categoryMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity>,
        currencySymbol: String,
        transactionSettings: com.hisaabi.hisaabi_kmp.settings.domain.model.TransactionSettings
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Area", "Quantity Sold", "Total Sale Amount", "Profit")
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
        
        var totalQuantity = 0.0
        var totalSaleAmount = 0.0
        var totalProfit = 0.0
        
        areaGroups.forEach { (areaSlug, areaTransactions) ->
            val area = categoryMap[areaSlug]
            val areaTitle = area?.title ?: "Unknown Area"
            
            var quantitySold = 0.0
            var saleAmount = 0.0
            var profit = 0.0
            
            areaTransactions.forEach { transaction ->
                val details = allDetails.filter { it.transaction_slug == transaction.slug }
                
                details.forEach { detail ->
                    val product = productMap[detail.product_slug ?: ""]
                    val avgPurchasePrice = product?.avg_purchase_price ?: 0.0
                    val detailAmount = detail.price * detail.quantity
                    
                    when (transaction.transaction_type) {
                        AllTransactionTypes.SALE.value -> {
                            quantitySold += detail.quantity
                            saleAmount += detailAmount
                            profit += calculateProfitWithAvgPrice(
                                detail.price,
                                avgPurchasePrice,
                                detail.quantity,
                                isReturn = false
                            )
                        }
                        AllTransactionTypes.CUSTOMER_RETURN.value -> {
                            quantitySold -= detail.quantity
                            saleAmount -= detailAmount
                            profit += calculateProfitWithAvgPrice(
                                detail.price,
                                avgPurchasePrice,
                                detail.quantity,
                                isReturn = true
                            )
                        }
                    }
                }
            }
            
            totalQuantity += quantitySold
            totalSaleAmount += saleAmount
            totalProfit += profit
            
            rows.add(
                ReportRow(
                    id = areaSlug,
                    values = listOf(
                        areaTitle,
                        String.format("%.2f", quantitySold),
                        "$currencySymbol ${String.format("%,.2f", saleAmount)}",
                        "$currencySymbol ${String.format("%,.2f", profit)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalSaleAmount,
            totalQuantity = totalQuantity,
            totalProfit = totalProfit,
            recordCount = rows.size
        )
        
        return Triple(columns, rows, summary)
    }
    
    private fun generatePartyCategoryGroupedReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        allDetails: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        productMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.ProductEntity>,
        partyMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.PartyEntity>,
        categoryMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity>,
        currencySymbol: String,
        transactionSettings: com.hisaabi.hisaabi_kmp.settings.domain.model.TransactionSettings
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Category", "Quantity Sold", "Total Sale Amount", "Profit")
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
        
        var totalQuantity = 0.0
        var totalSaleAmount = 0.0
        var totalProfit = 0.0
        
        categoryGroups.forEach { (categorySlug, categoryTransactions) ->
            val category = categoryMap[categorySlug]
            val categoryTitle = category?.title ?: "Uncategorized"
            
            var quantitySold = 0.0
            var saleAmount = 0.0
            var profit = 0.0
            
            categoryTransactions.forEach { transaction ->
                val details = allDetails.filter { it.transaction_slug == transaction.slug }
                
                details.forEach { detail ->
                    val product = productMap[detail.product_slug ?: ""]
                    val avgPurchasePrice = product?.avg_purchase_price ?: 0.0
                    val detailAmount = detail.price * detail.quantity
                    
                    when (transaction.transaction_type) {
                        AllTransactionTypes.SALE.value -> {
                            quantitySold += detail.quantity
                            saleAmount += detailAmount
                            profit += calculateProfitWithAvgPrice(
                                detail.price,
                                avgPurchasePrice,
                                detail.quantity,
                                isReturn = false
                            )
                        }
                        AllTransactionTypes.CUSTOMER_RETURN.value -> {
                            quantitySold -= detail.quantity
                            saleAmount -= detailAmount
                            profit += calculateProfitWithAvgPrice(
                                detail.price,
                                avgPurchasePrice,
                                detail.quantity,
                                isReturn = true
                            )
                        }
                    }
                }
            }
            
            totalQuantity += quantitySold
            totalSaleAmount += saleAmount
            totalProfit += profit
            
            rows.add(
                ReportRow(
                    id = categorySlug,
                    values = listOf(
                        categoryTitle,
                        String.format("%.2f", quantitySold),
                        "$currencySymbol ${String.format("%,.2f", saleAmount)}",
                        "$currencySymbol ${String.format("%,.2f", profit)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalSaleAmount,
            totalQuantity = totalQuantity,
            totalProfit = totalProfit,
            recordCount = rows.size
        )
        
        return Triple(columns, rows, summary)
    }
    
    // Interval-based reports (Daily, Weekly, Monthly, Yearly)
    private fun generateDailyReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        allDetails: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        productMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.ProductEntity>,
        currencySymbol: String,
        transactionSettings: com.hisaabi.hisaabi_kmp.settings.domain.model.TransactionSettings
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        return generateIntervalReport(
            transactions,
            allDetails,
            productMap,
            currencySymbol,
            transactionSettings,
            intervalType = "DAILY"
        )
    }
    
    private fun generateWeeklyReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        allDetails: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        productMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.ProductEntity>,
        currencySymbol: String,
        transactionSettings: com.hisaabi.hisaabi_kmp.settings.domain.model.TransactionSettings
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        return generateIntervalReport(
            transactions,
            allDetails,
            productMap,
            currencySymbol,
            transactionSettings,
            intervalType = "WEEKLY"
        )
    }
    
    private fun generateMonthlyReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        allDetails: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        productMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.ProductEntity>,
        currencySymbol: String,
        transactionSettings: com.hisaabi.hisaabi_kmp.settings.domain.model.TransactionSettings
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        return generateIntervalReport(
            transactions,
            allDetails,
            productMap,
            currencySymbol,
            transactionSettings,
            intervalType = "MONTHLY"
        )
    }
    
    private fun generateYearlyReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        allDetails: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        productMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.ProductEntity>,
        currencySymbol: String,
        transactionSettings: com.hisaabi.hisaabi_kmp.settings.domain.model.TransactionSettings
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        return generateIntervalReport(
            transactions,
            allDetails,
            productMap,
            currencySymbol,
            transactionSettings,
            intervalType = "YEARLY"
        )
    }
    
    private fun generateIntervalReport(
        transactions: List<com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity>,
        allDetails: List<com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity>,
        productMap: Map<String, com.hisaabi.hisaabi_kmp.database.entity.ProductEntity>,
        currencySymbol: String,
        transactionSettings: com.hisaabi.hisaabi_kmp.settings.domain.model.TransactionSettings,
        intervalType: String
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Date", "Total Sale", "Purchase Cost", "Profit", "Loss")
        val rows = mutableListOf<ReportRow>()
        
        // Group transactions by date interval
        val intervalMap = mutableMapOf<String, IntervalStats>()
        
        transactions.forEach { transaction ->
            val dateKey = getDateKey(transaction.timestamp, intervalType)
            val stats = intervalMap.getOrPut(dateKey) {
                IntervalStats(dateKey)
            }
            
            val details = allDetails.filter { it.transaction_slug == transaction.slug }
            
            details.forEach { detail ->
                val product = productMap[detail.product_slug ?: ""]
                val avgPurchasePrice = product?.avg_purchase_price ?: 0.0
                val detailAmount = detail.price * detail.quantity
                val purchaseCost = calculatePurchaseCostWithAvgPrice(avgPurchasePrice, detail.quantity)
                val profit = calculateProfitWithAvgPrice(
                    detail.price,
                    avgPurchasePrice,
                    detail.quantity,
                    isReturn = transaction.transaction_type == AllTransactionTypes.CUSTOMER_RETURN.value
                )
                
                when (transaction.transaction_type) {
                    AllTransactionTypes.SALE.value -> {
                        stats.netSaleAmount += detailAmount
                        stats.purchaseCost += purchaseCost
                        stats.profit += profit
                    }
                    AllTransactionTypes.CUSTOMER_RETURN.value -> {
                        stats.netSaleAmount -= detailAmount
                        stats.purchaseCost -= purchaseCost
                        stats.profit += profit // profit is already negative for returns
                    }
                }
            }
        }
        
        // Sort by date (newest first)
        val sortedIntervals = intervalMap.values.sortedByDescending { it.dateKey }
        
        var totalSale = 0.0
        var totalPurchaseCost = 0.0
        var totalProfit = 0.0
        var totalLoss = 0.0
        
        sortedIntervals.forEach { stats ->
            totalSale += stats.netSaleAmount
            totalPurchaseCost += stats.purchaseCost
            if (stats.profit > 0) {
                totalProfit += stats.profit
            } else {
                totalLoss += abs(stats.profit)
            }
            
            val displayDate = formatDateKey(stats.dateKey, intervalType)
            
            rows.add(
                ReportRow(
                    id = stats.dateKey,
                    values = listOf(
                        displayDate,
                        "$currencySymbol ${String.format("%,.2f", stats.netSaleAmount)}",
                        "$currencySymbol ${String.format("%,.2f", stats.purchaseCost)}",
                        if (stats.profit > 0) "$currencySymbol ${String.format("%,.2f", stats.profit)}" else "0",
                        if (stats.profit < 0) "$currencySymbol ${String.format("%,.2f", abs(stats.profit))}" else "0"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalSale,
            totalProfit = totalProfit,
            recordCount = rows.size,
            additionalInfo = mapOf(
                "Total Purchase Cost" to String.format("%,.2f", totalPurchaseCost),
                "Total Loss" to String.format("%,.2f", totalLoss)
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
            else -> {
                // Default to all time if no filter specified
                LocalDate(2020, 1, 1) to today
            }
        }
        
        // Convert LocalDate pairs to milliseconds
        val startMillis = startDate.atStartOfDayIn(timezone).toEpochMilliseconds()
        val endMillis = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timezone).toEpochMilliseconds()
        
        return Pair(startMillis, endMillis)
    }
    
    // Helper data class for interval statistics
    private data class IntervalStats(
        val dateKey: String,
        var netSaleAmount: Double = 0.0,
        var purchaseCost: Double = 0.0,
        var profit: Double = 0.0
    )
}

