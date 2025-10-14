package com.hisaabi.hisaabi_kmp.home.dashboard

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Time interval options for dashboard data filtering
 */
enum class IntervalEnum(val title: String) {
    LAST_7_DAYS("Last 7 Days"),
    LAST_15_DAYS("Last 15 Days"),
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    THIS_YEAR("This Year"),
    LAST_YEAR("Last Year"),
    ALL_RECORD("All Records")
}

/**
 * Dashboard section data model containing section items and filtering options
 */
data class DashboardSectionDataModel(
    val title: String,
    val sectionItems: List<SectionItem>,
    val options: List<IntervalEnum>?,
    val selectedOption: IntervalEnum?,
    val onOptionSelected: (IntervalEnum) -> Unit = {}
) {
    data class SectionItem(
        val title: String,
        val value: Double,
        val icon: ImageVector
    )
}

/**
 * Network response wrapper for dashboard data
 */
sealed class DashboardDataState<out T> {
    data class Success<T>(val data: T) : DashboardDataState<T>()
    data class Error(val message: String) : DashboardDataState<Nothing>()
    object Loading : DashboardDataState<Nothing>()
    object NoData : DashboardDataState<Nothing>()
}

