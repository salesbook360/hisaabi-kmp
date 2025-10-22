package com.hisaabi.hisaabi_kmp.transactions.domain.model

/**
 * Enum for price types used in transactions
 */
enum class PriceType(val value: Int, val displayName: String) {
    RETAIL(1, "Retail"),
    WHOLESALE(2, "Wholesale"),
    PURCHASE(3, "Purchase");

    companion object {
        fun fromValue(value: Int): PriceType? {
            return entries.find { it.value == value }
        }
    }
}

/**
 * Enum for flat or percentage values (used for discounts and taxes)
 */
enum class FlatOrPercent(val value: Int, val displayName: String) {
    FLAT(1, "Flat"),
    PERCENT(2, "Percent");

    companion object {
        fun fromValue(value: Int): FlatOrPercent? {
            return entries.find { it.value == value }
        }
    }
}

/**
 * Enum for transaction states
 */
enum class TransactionState(val value: Int, val displayName: String) {
    PENDING(1, "Pending"),
    COMPLETED(2, "Completed"),
    CANCELLED(3, "Cancelled"),
    IN_PROGRESS(4, "In Progress");

    companion object {
        fun fromValue(value: Int): TransactionState? {
            return entries.find { it.value == value }
        }
    }
}

