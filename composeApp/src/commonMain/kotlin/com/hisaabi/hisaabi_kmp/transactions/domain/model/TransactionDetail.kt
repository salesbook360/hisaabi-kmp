package com.hisaabi.hisaabi_kmp.transactions.domain.model

import com.hisaabi.hisaabi_kmp.products.domain.model.Product
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit

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
    
    fun getDisplayQuantity(): String {
        return if (quantityUnit != null) {
            "$quantity ${quantityUnit.title}"
        } else {
            quantity.toString()
        }
    }
}

