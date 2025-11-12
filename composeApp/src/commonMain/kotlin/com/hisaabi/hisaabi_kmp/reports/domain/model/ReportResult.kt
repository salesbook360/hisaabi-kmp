package com.hisaabi.hisaabi_kmp.reports.domain.model

data class ReportResult(
    val reportType: ReportType,
    val filters: ReportFilters,
    val generatedAt: Long = System.currentTimeMillis(),
    val columns: List<String>,
    val rows: List<ReportRow>,
    val summary: ReportSummary? = null
)

data class ReportRow(
    val id: String,
    val values: List<String>
)

data class ReportSummary(
    val totalAmount: Double? = null,
    val totalProfit: Double? = null,
    val totalQuantity: Double? = null,
    val recordCount: Int = 0,
    val additionalInfo: Map<String, String> = emptyMap()
)

