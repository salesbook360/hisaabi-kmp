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
     * Validate a transaction before processing.
     * Checks if stock would go negative for any product.
     * 
     * @param transaction The transaction to validate
     * @param oldTransaction The old transaction (for updates), null for new transactions
     * @return Result.success if valid, Result.failure with error message if invalid
     */
    suspend fun validateTransaction(
        transaction: Transaction,
        oldTransaction: Transaction? = null
    ): Result<Unit> {
        // Validate stock for products
        val stockValidation = validateStock(transaction, oldTransaction)
        if (stockValidation.isFailure) {
            return stockValidation
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Validate that stock won't go negative after this transaction.
     * 
     * @param transaction The new transaction
     * @param oldTransaction The old transaction (for updates), null for new transactions
     * @return Result.success if valid, Result.failure with error message if invalid
     */
    private suspend fun validateStock(
        transaction: Transaction,
        oldTransaction: Transaction?
    ): Result<Unit> {
        val warehouseSlug = transaction.wareHouseSlugFrom ?: return Result.success(Unit)
        
        // Check each product in the transaction
        for (detail in transaction.transactionDetails) {
            // Skip service products (they don't have physical stock)
            if (detail.product?.isService == true) {
                continue
            }
            
            // Skip recipes in non-Purchase transactions (recipes can only be added to stock via Purchase/manufacture)
            if (detail.product?.isRecipe == true && transaction.transactionType != AllTransactionTypes.PURCHASE.value) {
                continue
            }
            
            val productSlug = detail.productSlug ?: continue
            val productName = detail.product?.title ?: "Unknown Product"
            
            // Calculate new quantity adjustment for this transaction
            val newQuantityAdjustment = calculateProductQuantityAdjustment(transaction, detail)
            
            // Calculate old quantity adjustment if this is an update
            val oldQuantityAdjustment = if (oldTransaction != null) {
                val oldDetail = oldTransaction.transactionDetails.find { 
                    it.productSlug == productSlug 
                }
                if (oldDetail != null) {
                    calculateProductQuantityAdjustment(oldTransaction, oldDetail)
                } else {
                    0.0
                }
            } else {
                0.0
            }
            
            // Net change in stock (reversing old and applying new)
            val netStockChange = newQuantityAdjustment - oldQuantityAdjustment
            
            // Skip if no change
            if (netStockChange == 0.0) {
                continue
            }
            
            // Get current quantity in warehouse
            val currentQuantity = transactionProcessorDao.getAvailableQuantity(
                productSlug,
                warehouseSlug
            ) ?: 0.0
            
            // Calculate final quantity after transaction
            val finalQuantity = currentQuantity + netStockChange
            
            // Check if stock would go negative
            if (finalQuantity < 0.0) {
                val shortfall = -finalQuantity
                return Result.failure(
                    Exception(
                        "Insufficient stock for '$productName'. " +
                        "Available: ${currentQuantity}, " +
                        "Required: ${-netStockChange}, " +
                        "Short by: ${shortfall}"
                    )
                )
            }
            
            // Also validate stock transfer destination if applicable
            if (transaction.transactionType == AllTransactionTypes.STOCK_TRANSFER.value &&
                transaction.wareHouseSlugTo != null) {
                // For stock transfer, the destination warehouse receives stock (positive adjustment)
                // No need to validate destination as it's receiving stock
            }
        }
        
        return Result.success(Unit)
    }
    
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
        
        // Update product average purchase price (BEFORE quantity update)
        updateProductAvgPurchasePrice(transaction, isReverse)
        
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
     * Update product average purchase price based on transaction type.
     * This implements the POS average cost calculation for profit/loss tracking.
     * 
     * The average purchase price is recalculated for Purchase and Return to Vendor transactions.
     * Formula: New Avg = (Current Total Value + Transaction Value) / (Current Qty + Transaction Qty)
     */
    private suspend fun updateProductAvgPurchasePrice(transaction: Transaction, isReverse: Boolean) {
        transaction.transactionDetails.forEach { detail ->
            // Skip service products (they don't have average purchase price)
            if (detail.product?.isService == true) {
                return@forEach
            }
            
            // Recipes can have average purchase price when manufactured (Purchase transaction)
            // Skip recipes in non-Purchase transactions
            if (detail.product?.isRecipe == true && transaction.transactionType != AllTransactionTypes.PURCHASE.value) {
                return@forEach
            }
            
            val productSlug = detail.productSlug ?: return@forEach
            
            // Get current sum of quantities across all warehouses
            val sumOfCurrentQty = transactionProcessorDao.getSumOfProductAvailableQuantity(productSlug) ?: 0.0
            
            // Get current average purchase price
            val currentAvgPrice = transactionProcessorDao.getAvgPurchasePriceOfProduct(productSlug) ?: 0.0
            
            // Calculate new average price
            val newAvgPrice = calculateAvgPurchasePrice(
                currentQuantity = sumOfCurrentQty,
                currentAvgPrice = currentAvgPrice,
                transactionQuantity = detail.quantity,
                transactionPrice = detail.price,
                transactionType = transaction.transactionType,
                isReverse = isReverse
            )
            
            // Update the product's average purchase price
            if (newAvgPrice.isFinite() && !newAvgPrice.isNaN()) {
                transactionProcessorDao.updateAvgPurchasePrice(productSlug, newAvgPrice)
            }
        }
    }
    
    /**
     * Calculate average purchase price using weighted average formula.
     * Based on legacy MathUtils.calculateAvgPurchasePrice implementation.
     * 
     * @param currentQuantity Current total quantity in stock across all warehouses
     * @param currentAvgPrice Current average purchase price
     * @param transactionQuantity Quantity in this transaction
     * @param transactionPrice Price per unit in this transaction
     * @param transactionType Type of transaction
     * @param isReverse Whether this is a reversal (for updates/deletes)
     * @return New average purchase price
     */
    private fun calculateAvgPurchasePrice(
        currentQuantity: Double,
        currentAvgPrice: Double,
        transactionQuantity: Double,
        transactionPrice: Double,
        transactionType: Int,
        isReverse: Boolean
    ): Double {
        // Ensure valid numbers
        val quantityA = currentQuantity.takeIf { it.isFinite() } ?: 0.0
        val priceA = currentAvgPrice.takeIf { it.isFinite() } ?: 0.0
        val quantityB = kotlin.math.abs(transactionQuantity.takeIf { it.isFinite() } ?: 0.0)
        val priceB = transactionPrice.takeIf { it.isFinite() } ?: 0.0
        
        // Determine effective transaction type (reverse if needed)
        val effectiveTransactionType = if (isReverse) {
            reverseTransactionType(transactionType)
        } else {
            transactionType
        }
        
        // Calculate new average based on transaction type
        return when (effectiveTransactionType) {
            // Purchase or Stock Increase: Add to existing stock
            AllTransactionTypes.PURCHASE.value,
            AllTransactionTypes.STOCK_INCREASE.value -> {
                val totalQuantity = quantityA + quantityB
                if (totalQuantity == 0.0) {
                    priceA
                } else {
                    val totalAmount = (quantityA * priceA) + (quantityB * priceB)
                    totalAmount / totalQuantity
                }
            }
            
            // Return to Vendor or Stock Reduce: Remove from existing stock
            AllTransactionTypes.VENDOR_RETURN.value,
            AllTransactionTypes.STOCK_REDUCE.value -> {
                val totalQuantity = quantityA - quantityB
                if (totalQuantity == 0.0) {
                    priceA
                } else {
                    val totalAmount = (quantityA * priceA) - (quantityB * priceB)
                    totalAmount / totalQuantity
                }
            }
            
            // Other transaction types don't affect average purchase price
            else -> priceA
        }
    }
    
    /**
     * Reverse transaction type for average purchase price calculation.
     * When deleting/updating, we need to reverse the effect on average price.
     */
    private fun reverseTransactionType(transactionType: Int): Int {
        return when (transactionType) {
            AllTransactionTypes.PURCHASE.value -> AllTransactionTypes.VENDOR_RETURN.value
            AllTransactionTypes.STOCK_INCREASE.value -> AllTransactionTypes.STOCK_REDUCE.value
            AllTransactionTypes.VENDOR_RETURN.value -> AllTransactionTypes.PURCHASE.value
            AllTransactionTypes.STOCK_REDUCE.value -> AllTransactionTypes.STOCK_INCREASE.value
            else -> -1
        }
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
            // Skip service products (they don't have physical stock)
            if (detail.product?.isService == true) {
                return@forEach
            }
            
            // Skip recipes in non-Purchase transactions (recipes can only be added to stock via Purchase/manufacture)
            // Recipes can be manufactured (Purchase transaction) but shouldn't be sold as physical products
            if (detail.product?.isRecipe == true && transaction.transactionType != AllTransactionTypes.PURCHASE.value) {
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


