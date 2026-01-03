package com.hisaabi.hisaabi_kmp.reports.domain.usecase

import com.hisaabi.hisaabi_kmp.business.data.datasource.BusinessPreferencesDataSource
import com.hisaabi.hisaabi_kmp.database.dao.InventoryTransactionDao
import com.hisaabi.hisaabi_kmp.database.dao.ProductDao
import com.hisaabi.hisaabi_kmp.database.dao.TransactionDetailDao
import com.hisaabi.hisaabi_kmp.reports.domain.model.*
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*

/**
 * Use case for generating Top Products report
 * Based on the legacy Android Native SalesReportGenerator with TOP_PROFIT, TOP_SOLD, TOP_PURCHASED filters
 */
class GenerateTopProductsReportUseCase(
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
        
        // Calculate date range based on filters
        val (fromDateMillis, toDateMillis) = calculateDateRange(filters)
        
        // Get all sale and customer return transactions
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
        
        // Get products for lookup
        val products = productDao.getProductsByBusiness(businessSlug).first()
        val productMap = products.associateBy { it.slug ?: "" }
        
        // Create product stats
        val productStatsMap = mutableMapOf<String, ProductStats>()
        
        allDetails.forEach { detail ->
            val productSlug = detail.product_slug ?: ""
            if (productSlug.isEmpty()) return@forEach
            
            val stats = productStatsMap.getOrPut(productSlug) {
                ProductStats(productSlug)
            }
            
            val transaction = transactions.find { it.slug == detail.transaction_slug }
            if (transaction != null) {
                when (transaction.transaction_type) {
                    AllTransactionTypes.SALE.value -> {
                        stats.quantitySold += detail.quantity
                        stats.totalSaleAmount += detail.price * detail.quantity
                        stats.totalProfit += detail.profit
                    }
                    AllTransactionTypes.CUSTOMER_RETURN.value -> {
                        stats.quantityReturned += detail.quantity
                        stats.totalSaleAmount -= detail.price * detail.quantity
                        stats.totalProfit -= detail.profit
                    }
                }
            }
        }
        
        // Convert to list and sort based on filter
        val productStatsList = productStatsMap.values.toList()
        val sortedStats = when (filters.additionalFilter) {
            ReportAdditionalFilter.TOP_PROFIT -> {
                productStatsList.sortedByDescending { it.totalProfit }
            }
            ReportAdditionalFilter.TOP_SOLD -> {
                productStatsList.sortedByDescending { it.netQuantitySold }
            }
            ReportAdditionalFilter.TOP_PURCHASED -> {
                // For TOP_PURCHASED, we need to get purchase transactions
                val purchaseTransactions = transactionDao.getTransactionsForReport(
                    businessSlug = businessSlug,
                    transactionTypes = listOf(
                        AllTransactionTypes.PURCHASE.value,
                        AllTransactionTypes.VENDOR_RETURN.value
                    ),
                    fromDate = fromDateMillis,
                    toDate = toDateMillis
                )
                val purchaseDetails = transactionDetailDao.getDetailsByTransactionSlugs(
                    purchaseTransactions.mapNotNull { it.slug }
                )
                
                // Add purchase quantities to stats
                val purchaseStatsMap = productStatsMap.toMutableMap()
                purchaseDetails.forEach { detail ->
                    val productSlug = detail.product_slug ?: ""
                    if (productSlug.isEmpty()) return@forEach
                    
                    val stats = purchaseStatsMap.getOrPut(productSlug) {
                        ProductStats(productSlug)
                    }
                    
                    val transaction = purchaseTransactions.find { it.slug == detail.transaction_slug }
                    if (transaction != null) {
                        when (transaction.transaction_type) {
                            AllTransactionTypes.PURCHASE.value -> {
                                stats.purchaseQuantity += detail.quantity
                            }
                            AllTransactionTypes.VENDOR_RETURN.value -> {
                                stats.purchaseQuantity -= detail.quantity
                            }
                        }
                    }
                }
                
                purchaseStatsMap.values.toList().sortedByDescending { it.purchaseQuantity }
            }
            else -> {
                // Default to TOP_SOLD
                productStatsList.sortedByDescending { it.netQuantitySold }
            }
        }
        
        // Generate columns and rows
        val columns = listOf("Product", "Quantity Sold", "Returned", "Net Sold", "Total Sale Amount")
        val rows = mutableListOf<ReportRow>()
        
        var totalQuantitySold = 0.0
        var totalReturned = 0.0
        var totalNetSold = 0.0
        var totalSaleAmount = 0.0
        var totalProfit = 0.0
        
        sortedStats.forEach { stats ->
            val product = productMap[stats.productSlug]
            val productTitle = product?.title ?: "Unknown Product"
            
            totalQuantitySold += stats.quantitySold
            totalReturned += stats.quantityReturned
            totalNetSold += stats.netQuantitySold
            totalSaleAmount += stats.totalSaleAmount
            totalProfit += stats.totalProfit
            
            rows.add(
                ReportRow(
                    id = stats.productSlug,
                    values = listOf(
                        productTitle,
                        String.format("%.2f", stats.quantitySold),
                        String.format("%.2f", stats.quantityReturned),
                        String.format("%.2f", stats.netQuantitySold),
                        "$currencySymbol ${String.format("%,.2f", stats.totalSaleAmount)}"
                    )
                )
            )
        }
        
        val summary = ReportSummary(
            totalAmount = totalSaleAmount,
            totalQuantity = totalNetSold,
            totalProfit = totalProfit,
            recordCount = rows.size,
            additionalInfo = mapOf(
                "Total Quantity Sold" to String.format("%.2f", totalQuantitySold),
                "Total Returned" to String.format("%.2f", totalReturned)
            )
        )
        
        return ReportResult(
            reportType = ReportType.TOP_PRODUCTS,
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
    
    // Helper data class for product statistics
    private data class ProductStats(
        val productSlug: String,
        var quantitySold: Double = 0.0,
        var quantityReturned: Double = 0.0,
        var purchaseQuantity: Double = 0.0,
        var totalSaleAmount: Double = 0.0,
        var totalProfit: Double = 0.0
    ) {
        val netQuantitySold: Double
            get() = quantitySold - quantityReturned
    }
}

