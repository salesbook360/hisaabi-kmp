package com.hisaabi.hisaabi_kmp.transactions.domain.model

/**
 * Centralized Transaction Types
 * 
 * This enum contains ALL transaction types used in the application.
 * Each type has a unique value to avoid conflicts.
 * 
 * IMPORTANT: Do not change existing values as they are stored in the database!
 * Only add new types with new unique values.
 */
enum class AllTransactionTypes(val value: Int, val displayName: String, val category: TransactionCategory) {
    // Basic Transaction Types (1-3)
    SALE(1, "Sale", TransactionCategory.BASIC),
    SALE_ORDER(2, "Sale Order", TransactionCategory.BASIC),
    PURCHASE(3, "Purchase", TransactionCategory.BASIC),
    
    // Pay/Get Cash Types (4-7, 11-12)
    PAY_TO_VENDOR(4, "Pay Payment to Vendor", TransactionCategory.CASH_PAYMENT),
    GET_FROM_VENDOR(5, "Get Payment from Vendor", TransactionCategory.CASH_PAYMENT),
    PAY_TO_CUSTOMER(6, "Pay Payment to Customer", TransactionCategory.CASH_PAYMENT),
    GET_FROM_CUSTOMER(7, "Get Payment from Customer", TransactionCategory.CASH_PAYMENT),
    INVESTMENT_DEPOSIT(11, "Investment Deposit", TransactionCategory.CASH_PAYMENT),
    INVESTMENT_WITHDRAW(12, "Investment Withdraw", TransactionCategory.CASH_PAYMENT),
    
    // Expense & Income (8-9)
    EXPENSE(8, "Expense", TransactionCategory.EXPENSE_INCOME),
    EXTRA_INCOME(9, "Extra Income", TransactionCategory.EXPENSE_INCOME),
    
    // Payment Transfer (10)
    PAYMENT_TRANSFER(10, "Payment Transfer", TransactionCategory.OTHER),
    
    // Stock Adjustment Types (13-15)
    STOCK_TRANSFER(13, "Stock Transfer", TransactionCategory.STOCK_ADJUSTMENT),
    STOCK_INCREASE(14, "Stock Increase", TransactionCategory.STOCK_ADJUSTMENT),
    STOCK_REDUCE(15, "Stock Reduce", TransactionCategory.STOCK_ADJUSTMENT),
    
    // Manufacture (16) - NEW
    MANUFACTURE(16, "Manufacture", TransactionCategory.BASIC),
    
    // Returns (17-18) - REASSIGNED from 5-6 to avoid conflicts
    CUSTOMER_RETURN(17, "Customer Return", TransactionCategory.BASIC),
    VENDOR_RETURN(18, "Vendor Return", TransactionCategory.BASIC),
    
    // Journal Voucher (19)
    JOURNAL_VOUCHER(19, "Journal Voucher", TransactionCategory.OTHER),
    
    // Quotation (20) - REASSIGNED from 7 to avoid conflicts
    QUOTATION(20, "Quotation", TransactionCategory.BASIC),
    
    // Record Types (21-25)
    MEETING(21, "Meeting", TransactionCategory.RECORD),
    TASK(22, "Task", TransactionCategory.RECORD),
    CLIENT_NOTE(23, "Client Note", TransactionCategory.RECORD),
    SELF_NOTE(24, "Self Note", TransactionCategory.RECORD),
    CASH_REMINDER(25, "Cash Reminder", TransactionCategory.RECORD),
    
    // Purchase Order (26) - REASSIGNED from 4 to avoid conflicts  
    PURCHASE_ORDER(26, "Purchase Order", TransactionCategory.BASIC),
    
    // Stock Adjustment Generic (27) - REASSIGNED from 8 to avoid conflicts
    STOCK_ADJUSTMENT(27, "Stock Adjustment", TransactionCategory.STOCK_ADJUSTMENT),

    ;

    companion object {
        /**
         * Get transaction type by value
         */
        fun fromValue(value: Int): AllTransactionTypes? {
            return entries.find { it.value == value }
        }
        
        /**
         * Get display name for a transaction type value
         */
        fun getDisplayName(value: Int): String {
            return fromValue(value)?.displayName ?: "Unknown"
        }
        
        /**
         * Check if transaction type deals with vendor
         */
        fun isDealingWithVendor(type: Int): Boolean {
            return type in listOf(
                PURCHASE.value,
                PURCHASE_ORDER.value,
                VENDOR_RETURN.value,
                PAY_TO_VENDOR.value,
                GET_FROM_VENDOR.value
            )
        }
        
        /**
         * Check if transaction type deals with customer
         */
        fun isDealingWithCustomer(type: Int): Boolean {
            return type in listOf(
                SALE.value,
                SALE_ORDER.value,
                CUSTOMER_RETURN.value,
                QUOTATION.value,
                PAY_TO_CUSTOMER.value,
                GET_FROM_CUSTOMER.value
            )
        }
        
        /**
         * Check if transaction type is returning products
         */
        fun isReturningProducts(type: Int): Boolean {
            return type in listOf(CUSTOMER_RETURN.value, VENDOR_RETURN.value)
        }
        
        /**
         * Check if transaction type is an order
         */
        fun isOrder(type: Int): Boolean {
            return type in listOf(SALE_ORDER.value, PURCHASE_ORDER.value)
        }
        
        /**
         * Check if transaction type affects stock
         */
        fun affectsStock(type: Int): Boolean {
            return type in listOf(
                SALE.value,
                PURCHASE.value,
                CUSTOMER_RETURN.value,
                VENDOR_RETURN.value,
                STOCK_TRANSFER.value,
                STOCK_INCREASE.value,
                STOCK_REDUCE.value,
                STOCK_ADJUSTMENT.value,
                MANUFACTURE.value
            )
        }
        
        /**
         * Check if transaction type requires a party
         */
        fun requiresParty(type: Int): Boolean {
            return fromValue(type)?.let { transactionType ->
                transactionType.category in listOf(
                    TransactionCategory.BASIC,
                    TransactionCategory.CASH_PAYMENT
                ) || (transactionType.category == TransactionCategory.RECORD && 
                      transactionType != SELF_NOTE)
            } ?: false
        }
        
        /**
         * Get transaction types by category
         */
        fun getByCategory(category: TransactionCategory): List<AllTransactionTypes> {
            return entries.filter { it.category == category }
        }
        
        /**
         * Check if transaction type is a record type (Meeting, Task, Note, etc.)
         */
        fun isRecord(type: Int): Boolean {
            return type in listOf(
                MEETING.value,
                TASK.value,
                CLIENT_NOTE.value,
                SELF_NOTE.value,
                CASH_REMINDER.value
            )
        }
        
        /**
         * Check if transaction type is a Pay/Get Cash transaction
         */
        fun isPayGetCash(type: Int): Boolean {
            return type in listOf(
                PAY_TO_CUSTOMER.value,
                GET_FROM_CUSTOMER.value,
                PAY_TO_VENDOR.value,
                GET_FROM_VENDOR.value,
                INVESTMENT_DEPOSIT.value,
                INVESTMENT_WITHDRAW.value
            )
        }
        
        /**
         * Check if transaction type is an Expense or Extra Income transaction
         */
        fun isExpenseIncome(type: Int): Boolean {
            return type in listOf(EXPENSE.value, EXTRA_INCOME.value)
        }
        
        /**
         * Check if transaction type is a Payment Transfer
         */
        fun isPaymentTransfer(type: Int): Boolean {
            return type == PAYMENT_TRANSFER.value
        }
        
        /**
         * Check if transaction type is a Journal Voucher
         */
        fun isJournalVoucher(type: Int): Boolean {
            return type == JOURNAL_VOUCHER.value
        }
        
        /**
         * Check if transaction type is a Stock Adjustment
         */
        fun isStockAdjustment(type: Int): Boolean {
            return type in listOf(
                STOCK_INCREASE.value,
                STOCK_REDUCE.value,
                STOCK_TRANSFER.value,
                STOCK_ADJUSTMENT.value
            )
        }
    }
}

/**
 * Transaction categories for grouping
 */
enum class TransactionCategory {
    BASIC,              // Sale, Purchase, Returns, Orders, Quotation, Manufacture
    CASH_PAYMENT,       // Pay/Get Cash transactions
    EXPENSE_INCOME,     // Expense and Extra Income
    STOCK_ADJUSTMENT,   // Stock adjustments and transfers
    RECORD,             // Meetings, Tasks, Notes, Reminders
    OTHER               // Payment Transfer, Journal Voucher, etc.
}

