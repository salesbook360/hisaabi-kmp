package com.hisaabi.hisaabi_kmp.reports.domain.usecase

import com.hisaabi.hisaabi_kmp.business.data.datasource.BusinessPreferencesDataSource
import com.hisaabi.hisaabi_kmp.database.dao.*
import com.hisaabi.hisaabi_kmp.database.entity.ProductQuantitiesEntity
import com.hisaabi.hisaabi_kmp.products.data.repository.ProductsRepository
import com.hisaabi.hisaabi_kmp.products.data.repository.toDomainModel
import com.hisaabi.hisaabi_kmp.reports.domain.model.*
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*

/**
 * Use case for generating stock reports from actual database
 * Based on legacy app StockReportGenerator
 */
class GenerateStockReportUseCase(
    private val transactionDao: InventoryTransactionDao,
    private val transactionDetailDao: TransactionDetailDao,
    private val productDao: ProductDao,
    private val productQuantitiesDao: ProductQuantitiesDao,
    private val productsRepository: ProductsRepository,
    private val businessPreferences: BusinessPreferencesDataSource,
    private val preferencesManager: PreferencesManager
) {
    
    suspend fun execute(filters: ReportFilters): ReportResult {
        val currencySymbol = preferencesManager.getSelectedCurrency().symbol
        val businessSlug = businessPreferences.observeSelectedBusinessSlug().first()
            ?: throw IllegalStateException("No business selected")
        
        // Get all active products
        val productEntities = productDao.getProductsByBusiness(businessSlug).first()
            .filter { it.status_id == 0 } // Only active products
        
        // Get product quantities and group by product slug (sum across all warehouses)
        val productQuantities = productQuantitiesDao.getQuantitiesByBusiness(businessSlug)
        val quantitiesByProduct = productQuantities
            .filter { it.product_slug != null }
            .groupBy { it.product_slug!! }
            .mapValues { (_, quantities) ->
                // Sum quantities across all warehouses for this product
                AggregatedProductQuantity(
                    currentQuantity = quantities.sumOf { it.current_quantity },
                    openingQuantity = quantities.sumOf { it.opening_quantity },
                    minimumQuantity = quantities.sumOf { it.minimum_quantity }, // Sum minimum quantities
                    maximumQuantity = quantities.sumOf { it.maximum_quantity } // Sum maximum quantities
                )
            }
        
        // Convert to domain models
        val products = productEntities.map { entity ->
            entity.toDomainModel()
        }
        
        // Calculate stock stats
        val stockStats = StockStats(totalProducts = products.size)
        products.forEach { product ->
            val aggregatedQuantity = quantitiesByProduct[product.slug]
            stockStats.updateWith(product, aggregatedQuantity)
        }
        
        // Generate report based on additional filter
        val (columns, rows, summary) = when (filters.additionalFilter) {
            ReportAdditionalFilter.STOCK_WORTH -> {
                generateStockWorthReport(products, quantitiesByProduct, currencySymbol, stockStats)
            }
            ReportAdditionalFilter.OUT_OF_STOCK -> {
                generateOutOfStockReport(products, quantitiesByProduct, currencySymbol, stockStats)
            }
            else -> {
                // Default: Stock In/Out Report
                generateStockInOutReport(filters, products, quantitiesByProduct, currencySymbol, stockStats, businessSlug)
            }
        }
        
        return ReportResult(
            reportType = ReportType.STOCK_REPORT,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = summary
        )
    }
    
    /**
     * Generate Stock In/Out Report - shows purchased quantity, sold quantity, available quantity, etc.
     */
    private suspend fun generateStockInOutReport(
        filters: ReportFilters,
        products: List<com.hisaabi.hisaabi_kmp.products.domain.model.Product>,
        quantitiesByProduct: Map<String, AggregatedProductQuantity>,
        currencySymbol: String,
        stockStats: StockStats,
        businessSlug: String
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        // Calculate date range
        val (fromDateMillis, toDateMillis) = calculateDateRange(filters)
        
        // Get transactions for stock calculation (Sale, Purchase, Customer Return, Vendor Return)
        val transactionTypes = listOf(
            AllTransactionTypes.SALE.value,
            AllTransactionTypes.PURCHASE.value,
            AllTransactionTypes.CUSTOMER_RETURN.value,
            AllTransactionTypes.VENDOR_RETURN.value
        )
        
        val transactions = transactionDao.getTransactionsForReport(
            businessSlug = businessSlug,
            transactionTypes = transactionTypes,
            fromDate = fromDateMillis,
            toDate = toDateMillis
        )
        
        // Get transaction details
        val transactionSlugs = transactions.mapNotNull { it.slug }
        val allDetails = transactionDetailDao.getDetailsByTransactionSlugs(transactionSlugs)
        
        // Create product stats map
        val productStatsMap = mutableMapOf<String, ProductStats>()
        products.forEach { product ->
            val aggregatedQuantity = quantitiesByProduct[product.slug]
            productStatsMap[product.slug] = ProductStats(product, aggregatedQuantity)
        }
        
        // Update stats from transactions
        transactions.forEach { transaction ->
            val details = allDetails.filter { it.transaction_slug == transaction.slug }
            details.forEach { detail ->
                val productSlug = detail.product_slug ?: return@forEach
                productStatsMap[productSlug]?.updateWith(transaction, detail)
            }
        }
        
        // Build columns based on what we want to show
        val columns = mutableListOf<String>()
        columns.add("Product")
        columns.add("Purchased Qty")
        columns.add("Sold Qty")
        columns.add("Available Qty")
        columns.add("Avg Purchase Price")
        columns.add("Purchase Price")
        columns.add("Sale Price")
        columns.add("Wholesale Price")
        columns.add("Opening Qty")
        
        // Build rows
        val rows = mutableListOf<ReportRow>()
        val sortedStats = productStatsMap.values.sortedBy { it.product.displayName }
        
        sortedStats.forEachIndexed { index, stats ->
            val aggregatedQuantity = quantitiesByProduct[stats.product.slug]
            val currentQuantity = aggregatedQuantity?.currentQuantity ?: 0.0
            val openingQuantity = aggregatedQuantity?.openingQuantity ?: 0.0
            
            rows.add(
                ReportRow(
                    id = index.toString(),
                    values = listOf(
                        stats.product.displayName,
                        String.format("%.2f", stats.netPurchaseQty),
                        String.format("%.2f", stats.getNetSalesQuantity(null)),
                        String.format("%.2f", currentQuantity),
                        "$currencySymbol ${String.format("%,.2f", stats.product.avgPurchasePrice)}",
                        "$currencySymbol ${String.format("%,.2f", stats.product.purchasePrice)}",
                        "$currencySymbol ${String.format("%,.2f", stats.product.retailPrice)}",
                        "$currencySymbol ${String.format("%,.2f", stats.product.wholesalePrice)}",
                        String.format("%.2f", openingQuantity)
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalQuantity = stockStats.totalStockCount,
            recordCount = rows.size,
            additionalInfo = mapOf(
                "Total Products" to stockStats.totalProducts.toString(),
                "Total Stock Count" to String.format("%.2f", stockStats.totalStockCount),
                "Stock Sale Worth" to "$currencySymbol ${String.format("%,.2f", stockStats.totalStockSaleWorth)}"
            )
        )
        
        return Triple(columns, rows, summary)
    }
    
    /**
     * Generate Stock Worth Report - shows product, quantity, avg purchase price, stock purchase worth
     */
    private fun generateStockWorthReport(
        products: List<com.hisaabi.hisaabi_kmp.products.domain.model.Product>,
        quantitiesByProduct: Map<String, AggregatedProductQuantity>,
        currencySymbol: String,
        stockStats: StockStats
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Product", "Quantity", "Avg Purchase Price", "Stock Purchase Worth")
        val rows = mutableListOf<ReportRow>()
        
        products.forEachIndexed { index, product ->
            val aggregatedQuantity = quantitiesByProduct[product.slug]
            val currentQuantity = aggregatedQuantity?.currentQuantity ?: 0.0
            val stockWorth = product.avgPurchasePrice * currentQuantity
            
            rows.add(
                ReportRow(
                    id = index.toString(),
                    values = listOf(
                        product.displayName,
                        String.format("%.2f", currentQuantity),
                        "$currencySymbol ${String.format("%,.2f", product.avgPurchasePrice)}",
                        "$currencySymbol ${String.format("%,.2f", stockWorth)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalQuantity = stockStats.totalStockCount,
            totalAmount = stockStats.totalStockPurchaseWorth,
            recordCount = rows.size,
            additionalInfo = mapOf(
                "Total Products" to stockStats.totalProducts.toString(),
                "Total Stock Count" to String.format("%.2f", stockStats.totalStockCount),
                "Total Stock Purchase Worth" to "$currencySymbol ${String.format("%,.2f", stockStats.totalStockPurchaseWorth)}"
            )
        )
        
        return Triple(columns, rows, summary)
    }
    
    /**
     * Generate Out of Stock Products Report - shows products with current quantity <= minimum quantity
     */
    private fun generateOutOfStockReport(
        products: List<com.hisaabi.hisaabi_kmp.products.domain.model.Product>,
        quantitiesByProduct: Map<String, AggregatedProductQuantity>,
        currencySymbol: String,
        stockStats: StockStats
    ): Triple<List<String>, List<ReportRow>, ReportSummary> {
        val columns = listOf("Product", "Quantity", "Minimum Quantity", "Avg Purchase Price")
        val rows = mutableListOf<ReportRow>()
        
        products.forEachIndexed { index, product ->
            val aggregatedQuantity = quantitiesByProduct[product.slug]
            val currentQuantity = aggregatedQuantity?.currentQuantity ?: 0.0
            val minimumQuantity = aggregatedQuantity?.minimumQuantity ?: 0.0
            
            // Only include products that are out of stock (current <= minimum and minimum > 0)
            if (currentQuantity <= minimumQuantity && minimumQuantity > 0) {
                rows.add(
                    ReportRow(
                        id = index.toString(),
                    values = listOf(
                        product.displayName,
                        String.format("%.2f", currentQuantity),
                        String.format("%.2f", minimumQuantity),
                        "$currencySymbol ${String.format("%,.2f", product.avgPurchasePrice)}"
                    )
                    )
                )
            }
        }
        
        val summary = ReportSummary(
            recordCount = rows.size,
            additionalInfo = mapOf(
                "Total Products" to stockStats.totalProducts.toString(),
                "Out of Stock Products" to stockStats.outOfStockProduct.toString()
            )
        )
        
        return Triple(columns, rows, summary)
    }
    
    /**
     * Calculate date range from filters
     */
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

/**
 * Product statistics for stock reports
 */
data class ProductStats(
    val product: com.hisaabi.hisaabi_kmp.products.domain.model.Product,
    val aggregatedQuantity: AggregatedProductQuantity? = null
) {
    var netPurchaseQty: Double = 0.0
    var netSalesQty: Double = 0.0
    
    fun updateWith(transaction: com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity, detail: com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity) {
        when (transaction.transaction_type) {
            AllTransactionTypes.PURCHASE.value, AllTransactionTypes.VENDOR_RETURN.value -> {
                if (transaction.transaction_type == AllTransactionTypes.PURCHASE.value) {
                    netPurchaseQty += detail.quantity
                } else {
                    netPurchaseQty -= detail.quantity // Vendor return reduces purchase
                }
            }
            AllTransactionTypes.SALE.value, AllTransactionTypes.CUSTOMER_RETURN.value -> {
                if (transaction.transaction_type == AllTransactionTypes.SALE.value) {
                    netSalesQty += detail.quantity
                } else {
                    netSalesQty -= detail.quantity // Customer return reduces sales
                }
            }
        }
    }
    
    fun getNetSalesQuantity(warehouseSlug: String?): Double {
        return netSalesQty
    }
}

