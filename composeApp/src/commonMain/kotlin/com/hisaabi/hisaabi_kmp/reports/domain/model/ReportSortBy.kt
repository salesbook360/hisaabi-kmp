package com.hisaabi.hisaabi_kmp.reports.domain.model

enum class ReportSortBy(val id: Int, val title: String) {
    TITLE_ASC(1, "Title (A-Z)"),
    TITLE_DESC(2, "Title (Z-A)"),
    PROFIT_ASC(3, "Profit (Low to High)"),
    PROFIT_DESC(4, "Profit (High to Low)"),
    SALE_AMOUNT_ASC(5, "Sale Amount (Low to High)"),
    SALE_AMOUNT_DESC(6, "Sale Amount (High to Low)"),
    DATE_ASC(7, "Date (Oldest First)"),
    DATE_DESC(8, "Date (Newest First)"),
    BALANCE_ASC(9, "Balance (Low to High)"),
    BALANCE_DESC(10, "Balance (High to Low)");

    companion object {
        fun fromId(id: Int): ReportSortBy? = entries.find { it.id == id }
    }
}

