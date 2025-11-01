package com.hisaabi.hisaabi_kmp.transactions.data.repository

import com.hisaabi.hisaabi_kmp.database.dao.ProductQuantitiesDao
import com.hisaabi.hisaabi_kmp.database.dao.TransactionProcessorDao
import com.hisaabi.hisaabi_kmp.database.entity.ProductQuantitiesEntity
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionDetail

/**
 * Centralized Transaction Processor for POS application.
 * 
 * Handles all balance updates for:
 * - Customer/Vendor balances
 * - Product stock in warehouses
 * - Payment method balances
 * 
 * Also handles nested transactions (Manufacture and Journal Voucher)
 * and transaction updates (reverse old, apply new).
 */
class TransactionProcessor(
    private val transactionProcessorDao: TransactionProcessorDao,
    private val productQuantitiesDao: ProductQuantitiesDao
) {
    
    /**
     * Process a transaction and update all related balances.
     * 
     * @param transaction The transaction to process
     * @param isReverse If true, reverses the transaction (for updates and deletes)
     */
    suspend fun processTransaction(transaction: Transaction, isReverse: Boolean = false) {
        // Update party balance (customer/vendor)
        updatePartyBalance(transaction, isReverse)
        
        // Update payment method balance
        updatePaymentMethodBalance(transaction, isReverse)
        
        // Update product stock quantities
        updateProductQuantities(transaction, isReverse)
    }
    
    /**
     * Process a nested transaction (has parent).
     * This is used for Manufacture and Journal Voucher child transactions.
     */
    suspend fun processNestedTransaction(transaction: Transaction, isReverse: Boolean = false) {
        processTransaction(transaction, isReverse)
    }
    
    /**
     * Reverse a transaction completely (for updates and deletes).
     */
    suspend fun reverseTransaction(transaction: Transaction) {
        processTransaction(transaction, isReverse = true)
    }
    
    /**
     * Update party (customer/vendor) balance based on transaction type.
     */
    private suspend fun updatePartyBalance(transaction: Transaction, isReverse: Boolean) {
        val partySlug = transaction.customerSlug ?: return
        
        // Calculate balance adjustment based on transaction type
        val balanceAdjustment = calculatePartyBalanceAdjustment(transaction)
        
        // Apply or reverse based on isReverse flag
        val finalAdjustment = if (isReverse) -balanceAdjustment else balanceAdjustment
        
        if (finalAdjustment != 0.0) {
            transactionProcessorDao.updatePartyBalance(partySlug, finalAdjustment)
        }
    }
    
    /**
     * Calculate party balance adjustment based on transaction type.
     * This logic matches the legacy implementation.
     */
    private fun calculatePartyBalanceAdjustment(transaction: Transaction): Double {
        val totalBill = transaction.calculateGrandTotal()
        val totalPaid = transaction.totalPaid
        
        return when (transaction.transactionType) {
            // These INCREASE party balance (you will pay/receive less, they owe you less)
            AllTransactionTypes.SALE.value,
            AllTransactionTypes.GET_FROM_CUSTOMER.value,
            AllTransactionTypes.VENDOR_RETURN.value,
            AllTransactionTypes.GET_FROM_VENDOR.value -> {
                -(totalBill - totalPaid)
            }
            
            // These DECREASE party balance (you will pay/receive more, you owe them more)
            AllTransactionTypes.PURCHASE.value,
            AllTransactionTypes.PAY_TO_VENDOR.value,
            AllTransactionTypes.CUSTOMER_RETURN.value,
            AllTransactionTypes.PAY_TO_CUSTOMER.value -> {
                totalBill - totalPaid
            }
            
            // No change for other types
            else -> 0.0
        }
    }
    
    /**
     * Update payment method balance based on transaction type.
     */
    private suspend fun updatePaymentMethodBalance(transaction: Transaction, isReverse: Boolean) {
        val totalPaid = transaction.totalPaid
        
        // Calculate amount adjustment based on transaction type
        val amountAdjustment = calculatePaymentMethodAdjustment(transaction)
        
        // Apply or reverse based on isReverse flag
        val finalAdjustment = if (isReverse) -amountAdjustment else amountAdjustment
        
        // Update primary payment method
        if (finalAdjustment != 0.0 && transaction.paymentMethodToSlug != null) {
            transactionProcessorDao.updatePaymentMethodAmount(
                transaction.paymentMethodToSlug,
                finalAdjustment
            )
        }
        
        // Handle payment transfer (affects both from and to)
        if (transaction.transactionType == AllTransactionTypes.PAYMENT_TRANSFER.value && 
            transaction.paymentMethodFromSlug != null && 
            finalAdjustment != 0.0) {
            transactionProcessorDao.updatePaymentMethodAmount(
                transaction.paymentMethodFromSlug,
                -finalAdjustment
            )
        }
    }
    
    /**
     * Calculate payment method adjustment based on transaction type.
     * This logic matches the legacy implementation.
     */
    private fun calculatePaymentMethodAdjustment(transaction: Transaction): Double {
        val totalPaid = transaction.totalPaid
        
        return when (transaction.transactionType) {
            // These DECREASE payment method balance (money goes out)
            AllTransactionTypes.CUSTOMER_RETURN.value,
            AllTransactionTypes.PURCHASE.value,
            AllTransactionTypes.PAY_TO_VENDOR.value,
            AllTransactionTypes.PAY_TO_CUSTOMER.value,
            AllTransactionTypes.EXPENSE.value,
            AllTransactionTypes.INVESTMENT_WITHDRAW.value -> {
                -totalPaid
            }
            
            // These INCREASE payment method balance (money comes in)
            AllTransactionTypes.INVESTMENT_DEPOSIT.value,
            AllTransactionTypes.EXTRA_INCOME.value,
            AllTransactionTypes.PAYMENT_TRANSFER.value,
            AllTransactionTypes.GET_FROM_CUSTOMER.value,
            AllTransactionTypes.GET_FROM_VENDOR.value,
            AllTransactionTypes.VENDOR_RETURN.value,
            AllTransactionTypes.SALE.value -> {
                totalPaid
            }
            
            // No change for other types
            else -> 0.0
        }
    }
    
    /**
     * Update product stock quantities in warehouses based on transaction type.
     */
    private suspend fun updateProductQuantities(transaction: Transaction, isReverse: Boolean) {
        val warehouseSlug = transaction.wareHouseSlugFrom ?: return
        
        transaction.transactionDetails.forEach { detail ->
            // Skip service products and recipes (they don't have physical stock)
            if (detail.product?.isService == true || detail.product?.isRecipe == true) {
                return@forEach
            }
            
            // Calculate quantity adjustment based on transaction type
            val quantityAdjustment = calculateProductQuantityAdjustment(transaction, detail)
            
            // Apply or reverse based on isReverse flag
            val finalAdjustment = if (isReverse) -quantityAdjustment else quantityAdjustment
            
            if (finalAdjustment != 0.0 && detail.productSlug != null) {
                // Ensure warehouse record exists
                ensureWarehouseRecordExists(
                    detail.productSlug,
                    warehouseSlug,
                    transaction.businessSlug
                )
                
                // Update quantity in source warehouse
                transactionProcessorDao.updateProductQuantity(
                    detail.productSlug,
                    warehouseSlug,
                    finalAdjustment
                )
                
                // Handle stock transfer (affects both from and to warehouses)
                if (transaction.transactionType == AllTransactionTypes.STOCK_TRANSFER.value &&
                    transaction.wareHouseSlugTo != null) {
                    // Ensure destination warehouse record exists
                    ensureWarehouseRecordExists(
                        detail.productSlug,
                        transaction.wareHouseSlugTo,
                        transaction.businessSlug
                    )
                    
                    // Update quantity in destination warehouse (opposite direction)
                    transactionProcessorDao.updateProductQuantity(
                        detail.productSlug,
                        transaction.wareHouseSlugTo,
                        -finalAdjustment
                    )
                }
            }
        }
    }
    
    /**
     * Calculate product quantity adjustment based on transaction type.
     * This logic matches the legacy implementation.
     */
    private fun calculateProductQuantityAdjustment(
        transaction: Transaction,
        detail: TransactionDetail
    ): Double {
        val quantity = detail.quantity
        
        return when (transaction.transactionType) {
            // These INCREASE stock
            AllTransactionTypes.PURCHASE.value,
            AllTransactionTypes.CUSTOMER_RETURN.value,
            AllTransactionTypes.STOCK_INCREASE.value -> {
                quantity
            }
            
            // These DECREASE stock
            AllTransactionTypes.SALE.value,
            AllTransactionTypes.VENDOR_RETURN.value,
            AllTransactionTypes.STOCK_REDUCE.value,
            AllTransactionTypes.STOCK_TRANSFER.value -> {
                -quantity
            }
            
            // No change for other types
            else -> 0.0
        }
    }
    
    /**
     * Ensure a warehouse record exists for a product.
     * Creates an empty record if it doesn't exist.
     */
    private suspend fun ensureWarehouseRecordExists(
        productSlug: String?,
        warehouseSlug: String?,
        businessSlug: String?
    ) {
        if (productSlug == null || warehouseSlug == null || businessSlug == null) return
        
        val existingCount = transactionProcessorDao.checkExistingWarehouseRecord(
            productSlug,
            warehouseSlug
        )
        
        if (existingCount <= 0) {
            val productQuantity = ProductQuantitiesEntity(
                id = 0,
                product_slug = productSlug,
                warehouse_slug = warehouseSlug,
                opening_quantity = 0.0,
                current_quantity = 0.0,
                minimum_quantity = 0.0,
                maximum_quantity = 0.0,
                business_slug = businessSlug,
                sync_status = 0
            )
            transactionProcessorDao.createEmptyQuantities(productQuantity)
        }
    }
    
    /**
     * Get current balance for a party.
     */
    suspend fun getPartyBalance(partySlug: String?): Double {
        if (partySlug == null) return 0.0
        return transactionProcessorDao.getCurrentPartyBalance(partySlug) ?: 0.0
    }
    
    /**
     * Get current balance for a payment method.
     */
    suspend fun getPaymentMethodBalance(paymentMethodSlug: String?): Double {
        if (paymentMethodSlug == null) return 0.0
        return transactionProcessorDao.getPaymentMethodBalance(paymentMethodSlug) ?: 0.0
    }
    
    /**
     * Get available quantity for a product in a warehouse.
     */
    suspend fun getAvailableQuantity(productSlug: String?, warehouseSlug: String?): Double {
        if (productSlug == null || warehouseSlug == null) return 0.0
        return transactionProcessorDao.getAvailableQuantity(productSlug, warehouseSlug) ?: 0.0
    }
}

