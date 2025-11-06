package com.hisaabi.hisaabi_kmp.reports.domain.model

enum class ReportDateFilter(val id: Int, val title: String) {
    TODAY(1, "Today"),
    YESTERDAY(2, "Yesterday"),
    LAST_7_DAYS(4, "Last 7 Days"),
    THIS_MONTH(5, "This Month"),
    LAST_MONTH(6, "Last Month"),
    THIS_YEAR(9, "This Year"),
    LAST_YEAR(10, "Last Year"),
    CUSTOM_DATE(7, "Custom Date Range"),
    ALL_TIME(8, "All Time");

    companion object {
        fun fromId(id: Int): ReportDateFilter? = entries.find { it.id == id }
    }
}

