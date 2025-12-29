package com.hisaabi.hisaabi_kmp.transactions.domain.model

import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse

data class Transaction(
    val id: Int = 0,
    val partySlug: String? = null,
    val party: Party? = null,
    val parentSlug: String? = null,
    val totalBill: Double = 0.0,
    val totalPaid: Double = 0.0,
    val timestamp: String? = null,
    val flatDiscount: Double = 0.0,
    val discountTypeId: Int = FlatOrPercent.FLAT.value,
    val flatTax: Double = 0.0,
    val taxTypeId: Int = FlatOrPercent.FLAT.value,
    val additionalCharges: Double = 0.0,
    val additionalChargesType: String? = null,
    val additionalChargesDesc: String? = null,
    val paymentMethodToSlug: String? = null,
    val paymentMethodFromSlug: String? = null,
    val paymentMethodTo: PaymentMethod? = null,
    val paymentMethodFrom: PaymentMethod? = null,
    val transactionType: Int = AllTransactionTypes.SALE.value,
    val priceTypeId: Int = PriceType.RETAIL.value,
    val description: String? = null,
    val shippingAddress: String? = null,
    val statusId: Int = 0, // 0=Active, 2=Deleted
    val stateId: Int = TransactionState.COMPLETED.value,
    val remindAtMilliseconds: Long = 0,
    val wareHouseSlugFrom: String? = null,
    val wareHouseSlugTo: String? = null,
    val warehouseFrom: Warehouse? = null,
    val warehouseTo: Warehouse? = null,
    val transactionDetails: List<TransactionDetail> = emptyList(),
    val slug: String? = null,
    val businessSlug: String? = null,
    val createdBy: String? = null,
    val syncStatus: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun calculateSubtotal(): Double {
        return transactionDetails.sumOf { it.calculateSubtotal() }
    }
    
    fun calculateProductsDiscount(): Double {
        return transactionDetails.sumOf { it.flatDiscount }
    }
    
    fun calculateProductsTax(): Double {
        return transactionDetails.sumOf { it.flatTax }
    }
    
    fun calculateTotalDiscount(): Double {
        return flatDiscount + calculateProductsDiscount()
    }
    
    fun calculateTotalTax(): Double {
        return flatTax + calculateProductsTax()
    }
    
    fun calculateGrandTotal(): Double {
        val subtotal = calculateSubtotal()
        return subtotal + additionalCharges + calculateTotalTax() - calculateTotalDiscount()
    }
    
    fun calculatePayable(): Double {
        return calculateGrandTotal() - totalPaid
    }
    
    fun calculateTotalProfit(): Double {
        return transactionDetails.sumOf { it.profit }
    }
    
    fun calculateTotalQuantity(): Double {
        return transactionDetails.sumOf { it.quantity }
    }
    
    fun getTransactionTypeName(): String {
        // Use centralized AllTransactionTypes for all transaction type names
        return AllTransactionTypes.getDisplayName(transactionType)
    }
    
    fun getPriceTypeName(): String {
        return PriceType.fromValue(priceTypeId)?.displayName ?: "Unknown"
    }
    
    fun getStateName(): String {
        return TransactionState.fromValue(stateId)?.displayName ?: "Unknown"
    }
    
    fun isDealingWithVendor(): Boolean {
        return AllTransactionTypes.isDealingWithVendor(transactionType)
    }
    
    fun isDealingWithCustomer(): Boolean {
        return AllTransactionTypes.isDealingWithCustomer(transactionType)
    }
    
    fun isReturningProducts(): Boolean {
        return AllTransactionTypes.isReturningProducts(transactionType)
    }

    val isActive: Boolean
        get() = statusId == 0
}


