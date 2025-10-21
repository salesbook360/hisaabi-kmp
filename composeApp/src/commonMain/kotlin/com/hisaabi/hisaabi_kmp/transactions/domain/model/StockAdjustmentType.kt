package com.hisaabi.hisaabi_kmp.transactions.domain.model

enum class StockAdjustmentType(val value: Int, val displayName: String) {
    STOCK_TRANSFER(13, "Stock Transfer"),
    STOCK_INCREASE(14, "Stock Increase"),
    STOCK_REDUCE(15, "Stock Reduce");

    companion object {
        fun fromValue(value: Int): StockAdjustmentType? {
            return entries.find { it.value == value }
        }
    }
}


