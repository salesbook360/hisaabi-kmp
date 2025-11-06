package com.hisaabi.hisaabi_kmp.reports.domain.model

enum class ReportGroupBy(val id: Int, val title: String) {
    PRODUCT(1, "Product"),
    PARTY(2, "Party"),
    PRODUCT_CATEGORY(3, "Product Category"),
    PARTY_AREA(4, "Party Area"),
    PARTY_CATEGORY(5, "Party Category");

    companion object {
        fun fromId(id: Int): ReportGroupBy? = entries.find { it.id == id }
    }
}

