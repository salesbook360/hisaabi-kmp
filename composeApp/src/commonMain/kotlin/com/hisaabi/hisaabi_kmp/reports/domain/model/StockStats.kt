package com.hisaabi.hisaabi_kmp.reports.domain.model

import com.hisaabi.hisaabi_kmp.products.domain.model.Product

/**
 * Aggregated product quantity across all warehouses
 */
data class AggregatedProductQuantity(
    val currentQuantity: Double = 0.0,
    val openingQuantity: Double = 0.0,
    val minimumQuantity: Double = 0.0,
    val maximumQuantity: Double = 0.0
)

/**
 * Statistics for stock reports
 */
data class StockStats(
    val totalProducts: Int = 0,
    var totalStockCount: Double = 0.0,
    var outOfStockProduct: Int = 0,
    var totalStockPurchaseWorth: Double = 0.0,
    var totalStockSaleWorth: Double = 0.0
) {
    /**
     * Update stats with a product and its aggregated quantity (summed across all warehouses)
     */
    fun updateWith(product: Product, aggregatedQuantity: AggregatedProductQuantity? = null) {
        val currentQuantity = aggregatedQuantity?.currentQuantity ?: 0.0
        val minimumQuantity = aggregatedQuantity?.minimumQuantity ?: 0.0
        
        if (currentQuantity <= minimumQuantity && minimumQuantity > 0) {
            outOfStockProduct++
        }
        
        totalStockCount += currentQuantity
        totalStockPurchaseWorth += (product.avgPurchasePrice * currentQuantity)
        totalStockSaleWorth += (product.retailPrice * currentQuantity)
    }
}

