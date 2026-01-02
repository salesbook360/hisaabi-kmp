package com.hisaabi.hisaabi_kmp.reports.domain.model

/**
 * Factory class that provides filter options (Report Types, Date Filters, Sort Options)
 * based on the selected report and report type.
 *
 * This factory ensures that filter options are dynamically updated when the report
 * or report type changes, providing a consistent and maintainable way to manage
 * report filter configurations.
 */
object ReportFiltersFactory {

    /**
     * Returns the available report types (Overall, Daily, Weekly, etc.) for a given report.
     *
     * @param report The selected report type
     * @return List of available report type filters (e.g., Overall, Daily, Weekly)
     */
    fun getReportTypes(report: ReportType): List<ReportAdditionalFilter> {
        return when (report) {
            ReportType.SALE_REPORT,
            ReportType.PURCHASE_REPORT,
            ReportType.EXPENSE_REPORT,
            ReportType.EXTRA_INCOME_REPORT,
            ReportType.PROFIT_LOSS_REPORT,
            ReportType.CASH_IN_HAND -> {
                // Same values for Sale Report and Purchase Report
                listOf(
                    ReportAdditionalFilter.OVERALL,
                    ReportAdditionalFilter.DAILY,
                    ReportAdditionalFilter.WEEKLY,
                    ReportAdditionalFilter.MONTHLY,
                    ReportAdditionalFilter.YEARLY,
                )
            }

            ReportType.BALANCE_SHEET -> {
                // No report type for Balance Sheet
                emptyList()
            }

            ReportType.STOCK_REPORT,
            ReportType.WAREHOUSE_REPORT -> {
                listOf(
                    ReportAdditionalFilter.OVERALL,
                    ReportAdditionalFilter.STOCK_WORTH,
                    ReportAdditionalFilter.OUT_OF_STOCK,
                )
            }

            ReportType.TOP_PRODUCTS -> {
                listOf(
                    ReportAdditionalFilter.TOP_PROFIT,
                    ReportAdditionalFilter.TOP_SOLD
                )
            }

            ReportType.TOP_CUSTOMERS -> {
                listOf(
                    ReportAdditionalFilter.TOP_CREDIT,
                    ReportAdditionalFilter.TOP_PURCHASED,
                    ReportAdditionalFilter.TOP_CASH_PAID,
                )
            }

            ReportType.PRODUCT_REPORT -> {
                listOf(
                    ReportAdditionalFilter.LEDGER,
                    ReportAdditionalFilter.OVERALL,
                    ReportAdditionalFilter.DAILY,
                    ReportAdditionalFilter.WEEKLY,
                    ReportAdditionalFilter.MONTHLY,
                    ReportAdditionalFilter.YEARLY,
                )
            }

            ReportType.CUSTOMER_REPORT -> {
                listOf(
                    ReportAdditionalFilter.LEDGER,
                    ReportAdditionalFilter.CASH_FLOW,
                    ReportAdditionalFilter.OVERALL,
                    ReportAdditionalFilter.DAILY,
                    ReportAdditionalFilter.WEEKLY,
                    ReportAdditionalFilter.MONTHLY,
                    ReportAdditionalFilter.YEARLY,
                )
            }

            ReportType.VENDOR_REPORT -> {
                listOf(
                    ReportAdditionalFilter.LEDGER,
                    ReportAdditionalFilter.CASH_FLOW,
                )
            }

            ReportType.INVESTOR_REPORT -> {
                listOf(
                    ReportAdditionalFilter.LEDGER,
                    ReportAdditionalFilter.CASH_FLOW,
                    ReportAdditionalFilter.OVERALL
                )
            }

            ReportType.BALANCE_REPORT -> {
                listOf(
                    ReportAdditionalFilter.ALL_CUSTOMERS,
                    ReportAdditionalFilter.ALL_VENDORS,
                    ReportAdditionalFilter.VENDOR_DEBIT,
                    ReportAdditionalFilter.CUSTOMER_DEBIT,
                    ReportAdditionalFilter.VENDOR_CREDIT,
                    ReportAdditionalFilter.CUSTOMER_CREDIT,
                )
            }

            ReportType.CASH_IN_HAND -> {
                listOf(
                    ReportAdditionalFilter.CASH_IN_HAND_HISTORY,
                    ReportAdditionalFilter.DAILY,
                    ReportAdditionalFilter.WEEKLY,
                    ReportAdditionalFilter.MONTHLY,
                    ReportAdditionalFilter.YEARLY,
                )
            }

            // Rest of reports left empty for now
            else -> emptyList()
        }
    }

    /**
     * Returns the available date filters (Daily, Weekly, Monthly, Custom, etc.)
     * for a given report and selected report type.
     *
     * @param report The selected report type
     * @param selectedReportType The selected report type filter (e.g., Overall, Daily, Weekly)
     * @return List of available date filters
     */
    fun getDateFilters(
        report: ReportType,
        selectedReportType: ReportAdditionalFilter?
    ): List<ReportDateFilter> {
        return when (report) {
            ReportType.SALE_REPORT,
            ReportType.PURCHASE_REPORT -> {
                // Same values for Sale Report and Purchase Report
                // Date filters can vary based on selected report type
                when (selectedReportType) {
                    else -> {
                        // Default date filters
                        listOf(
                            ReportDateFilter.TODAY,
                            ReportDateFilter.YESTERDAY,
                            ReportDateFilter.LAST_7_DAYS,
                            ReportDateFilter.THIS_MONTH,
                            ReportDateFilter.LAST_MONTH,
                            ReportDateFilter.THIS_YEAR,
                            ReportDateFilter.LAST_YEAR,
                            ReportDateFilter.ALL_TIME,
                            ReportDateFilter.CUSTOM_DATE
                        )
                    }
                }
            }
            ReportType.STOCK_REPORT -> {
                // Stock In/Out Report (default, when no additional filter) needs date filters
                // Stock Worth and Out of Stock reports don't need date filters
                when (selectedReportType) {
                    ReportAdditionalFilter.STOCK_WORTH,
                    ReportAdditionalFilter.OUT_OF_STOCK -> {
                        // Stock Worth and Out of Stock don't use date filters
                        emptyList()
                    }
                    else -> {
                        // Default Stock In/Out Report (null or any other value) uses date filters
                        listOf(
                            ReportDateFilter.TODAY,
                            ReportDateFilter.YESTERDAY,
                            ReportDateFilter.LAST_7_DAYS,
                            ReportDateFilter.THIS_MONTH,
                            ReportDateFilter.LAST_MONTH,
                            ReportDateFilter.THIS_YEAR,
                            ReportDateFilter.LAST_YEAR,
                            ReportDateFilter.ALL_TIME,
                            ReportDateFilter.CUSTOM_DATE
                        )
                    }
                }
            }

            ReportType.BALANCE_SHEET -> {
                // No date filter for Balance Sheet
                emptyList()
            }
            // Rest of reports left empty for now
            else -> listOf(
                ReportDateFilter.TODAY,
                ReportDateFilter.YESTERDAY,
                ReportDateFilter.LAST_7_DAYS,
                ReportDateFilter.THIS_MONTH,
                ReportDateFilter.LAST_MONTH,
                ReportDateFilter.CUSTOM_DATE
            )
        }
    }

    /**
     * Returns the available sort options for a given report and selected report type.
     *
     * @param report The selected report type
     * @param selectedReportType The selected report type filter (e.g., Overall, Daily, Weekly)
     * @return List of available sort options
     */
    fun getSortOptions(
        report: ReportType,
        selectedReportType: ReportAdditionalFilter?
    ): List<ReportSortBy> {
        return when (report) {
            ReportType.SALE_REPORT,
            ReportType.PURCHASE_REPORT -> {
                // Same values for Sale Report and Purchase Report
                when (selectedReportType) {
                    ReportAdditionalFilter.OVERALL -> {
                        listOf(
                            ReportSortBy.TITLE_ASC,
                            ReportSortBy.PROFIT_DESC,
                            ReportSortBy.SALE_AMOUNT_DESC,
                        )
                    }

                    else -> {
                        // Default sort options
                        emptyList()
                    }
                }
            }

            ReportType.BALANCE_SHEET -> {
                // No sort option for Balance Sheet
                emptyList()
            }
            // Rest of reports left empty for now
            else -> emptyList()
        }
    }

    /**
     * Returns the available group by options for a given report and selected report type.
     * This is an optional helper method that can be used if group by options also need
     * to be dynamic based on report and report type.
     *
     * @param report The selected report type
     * @param selectedReportType The selected report type filter
     * @return List of available group by options
     */
    fun getGroupByOptions(
        report: ReportType,
        selectedReportType: ReportAdditionalFilter?
    ): List<ReportGroupBy> {
        return when (report) {
            ReportType.SALE_REPORT,
            ReportType.PURCHASE_REPORT -> {
                if (selectedReportType == ReportAdditionalFilter.OVERALL) {
                    listOf(
                        ReportGroupBy.PRODUCT,
                        ReportGroupBy.PARTY,
                        ReportGroupBy.PRODUCT_CATEGORY,
                        ReportGroupBy.PARTY_AREA,
                        ReportGroupBy.PARTY_CATEGORY,
                    )
                } else emptyList()
                // Same values for Sale Report and Purchase Report

            }

            ReportType.BALANCE_SHEET -> {
                // No group by for Balance Sheet
                emptyList()
            }
            // Rest of reports left empty for now
            else -> emptyList()
        }
    }
}

