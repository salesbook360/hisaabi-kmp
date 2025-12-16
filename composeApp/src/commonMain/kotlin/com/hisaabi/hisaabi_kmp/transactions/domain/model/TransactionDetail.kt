package com.hisaabi.hisaabi_kmp.transactions.domain.model

import com.hisaabi.hisaabi_kmp.products.domain.model.Product
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
import com.hisaabi.hisaabi_kmp.transactions.domain.util.TransactionCalculator

data class TransactionDetail(
    val id: Int = 0,
    val transactionSlug: String? = null,
    val productSlug: String?,
    val product: Product? = null,
    val quantity: Double = 1.0,
    val price: Double = 0.0,
    val flatTax: Double = 0.0,
    val taxType: Int = FlatOrPercent.FLAT.value,
    val flatDiscount: Double = 0.0,
    val discountType: Int = FlatOrPercent.FLAT.value,
    val profit: Double = 0.0,
    val description: String? = null,
    val quantityUnitSlug: String?,
    val quantityUnit: QuantityUnit? = null,
    val recipeSlug: String = "0",
    val slug: String? = null,
    val businessSlug: String? = null,
    val createdBy: String? = null,
    val syncStatus: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun calculateBill(): Double {
        val totalBill = price * quantity
        return totalBill + flatTax - flatDiscount
    }

    fun calculateSubtotal(): Double {
        return price * quantity
    }

    fun getConvertedQuantityIntoSelectedUnit(): Double {
      return  if ((quantityUnit?.conversionFactor ?: 0.0) > 1.0) {
            (quantity / (quantityUnit?.conversionFactor ?: 0.0))
        } else quantity
    }

    fun getDisplayQuantity(): String {
        return if (quantityUnit != null) {
            "${getConvertedQuantityIntoSelectedUnit()} ${quantityUnit.title}"
        } else {
            quantity.toString()
        }
    }

    /**
     * Calculate and return a copy with updated profit based on transaction type.
     *
     * Profit is calculated as:
     * - Sale: (price - avgPurchasePrice) * quantity
     * - Customer Return: -1 * (price - avgPurchasePrice) * quantity
     * - Other transactions: 0.0
     *
     * @param transactionType The type of transaction
     * @return A copy of this TransactionDetail with calculated profit
     */
    fun withCalculatedProfit(transactionType: Int): TransactionDetail {
        val avgPurchasePrice = product?.avgPurchasePrice ?: 0.0
        val calculatedProfit = TransactionCalculator.calculateProfit(
            salePrice = price,
            avgPurchasePrice = avgPurchasePrice,
            quantity = quantity,
            transactionType = transactionType
        )
        return copy(profit = calculatedProfit)
    }
}

