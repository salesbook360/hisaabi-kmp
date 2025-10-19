package com.hisaabi.hisaabi_kmp.transactions.domain.model

enum class TransactionType(val value: Int, val displayName: String) {
    SALE(1, "Sale"),
    SALE_ORDER(2, "Sale Order"),
    PURCHASE(3, "Purchase"),
    PURCHASE_ORDER(4, "Purchase Order"),
    CUSTOMER_RETURN(5, "Customer Return"),
    VENDOR_RETURN(6, "Vendor Return"),
    QUOTATION(7, "Quotation"),
    STOCK_ADJUSTMENT(8, "Stock Adjustment");

    companion object {
        fun fromValue(value: Int): TransactionType? {
            return values().find { it.value == value }
        }
        
        fun isDealingWithVendor(type: Int): Boolean {
            return type == PURCHASE.value || 
                   type == PURCHASE_ORDER.value || 
                   type == VENDOR_RETURN.value
        }
        
        fun isDealingWithCustomer(type: Int): Boolean {
            return type == SALE.value || 
                   type == SALE_ORDER.value || 
                   type == CUSTOMER_RETURN.value ||
                   type == QUOTATION.value
        }
        
        fun isReturningProducts(type: Int): Boolean {
            return type == CUSTOMER_RETURN.value || type == VENDOR_RETURN.value
        }
        
        fun isOrder(type: Int): Boolean {
            return type == SALE_ORDER.value || type == PURCHASE_ORDER.value
        }
        
        fun affectsStock(type: Int): Boolean {
            return type == SALE.value || 
                   type == PURCHASE.value || 
                   type == CUSTOMER_RETURN.value || 
                   type == VENDOR_RETURN.value ||
                   type == STOCK_ADJUSTMENT.value
        }
    }
}

enum class PriceType(val value: Int, val displayName: String) {
    RETAIL(1, "Retail"),
    WHOLESALE(2, "Wholesale"),
    PURCHASE(3, "Purchase");

    companion object {
        fun fromValue(value: Int): PriceType? {
            return values().find { it.value == value }
        }
    }
}

enum class FlatOrPercent(val value: Int, val displayName: String) {
    FLAT(0, "Flat"),
    PERCENT(1, "Percent");

    companion object {
        fun fromValue(value: Int): FlatOrPercent? {
            return values().find { it.value == value }
        }
    }
}

enum class TransactionState(val value: Int, val displayName: String) {
    PENDING(0, "Pending"),
    IN_PROGRESS(1, "In Progress"),
    COMPLETED(2, "Completed"),
    CANCELLED(3, "Cancelled");

    companion object {
        fun fromValue(value: Int): TransactionState? {
            return values().find { it.value == value }
        }
    }
}

