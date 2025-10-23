package com.hisaabi.hisaabi_kmp.home.dashboard

/**
 * Transaction type constants matching the native app
 * Based on transaction_type field in InventoryTransaction table
 */
object TransactionTypeHelper {
    
    // Sale transaction types
    const val SALE = 1
    const val SALE_ORDER = 2
    const val SALE_RETURN = 3
    
    // Purchase transaction types
    const val PURCHASE = 4
    const val PURCHASE_ORDER = 5
    const val PURCHASE_RETURN = 6
    
    // Payment transaction types
    const val PAYMENT_IN = 7
    const val PAYMENT_OUT = 8
    
    fun getSaleTransactionTypes() = listOf(SALE)
    
    fun getSaleOrderTransactionTypes() = listOf(SALE_ORDER)
    
    fun getPurchaseTransactionTypes() = listOf(PURCHASE)
    
    fun getPurchaseOrderTransactionTypes() = listOf(PURCHASE_ORDER)
    
    fun getReturnToVendorTransactionTypes() = listOf(PURCHASE_RETURN)
    
    fun getPaymentInTransactionTypes() = listOf(PAYMENT_IN)
    
    fun getPaymentOutTransactionTypes() = listOf(PAYMENT_OUT)
}

