package com.hisaabi.hisaabi_kmp.reports.domain.usecase

import com.hisaabi.hisaabi_kmp.reports.domain.model.ReportFilters
import com.hisaabi.hisaabi_kmp.reports.domain.model.ReportResult
import com.hisaabi.hisaabi_kmp.reports.domain.model.ReportRow
import com.hisaabi.hisaabi_kmp.reports.domain.model.ReportSummary
import com.hisaabi.hisaabi_kmp.reports.domain.model.ReportType
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager

/**
 * Use case for generating report data based on filters.
 */
class GenerateReportUseCase(
    private val generateSalesReportUseCase: GenerateSalesReportUseCase,
    private val generateBalanceSheetReportUseCase: GenerateBalanceSheetReportUseCase,
    private val generatePurchaseReportUseCase: GeneratePurchaseReportUseCase,
    private val generateStockReportUseCase: GenerateStockReportUseCase,
    private val generateWarehouseReportUseCase: GenerateWarehouseReportUseCase,
    private val generateProfitLossByAvgPriceUseCase: GenerateProfitLossByAvgPriceUseCase,
    private val generateTopProductsReportUseCase: GenerateTopProductsReportUseCase,
    private val generateTopCustomersReportUseCase: GenerateTopCustomersReportUseCase,
    private val generateProductReportUseCase: GenerateProductReportUseCase,
    private val generatePartyReportUseCase: GeneratePartyReportUseCase,
    private val generateBalanceReportUseCase: GenerateBalanceReportUseCase,
    private val generateExpenseIncomeReportUseCase: GenerateExpenseIncomeReportUseCase,
    private val preferencesManager: PreferencesManager
) {

    suspend fun execute(filters: ReportFilters): ReportResult {
        val reportType =
            filters.reportType ?: throw IllegalArgumentException("Report type is required")
        val currencySymbol = preferencesManager.getSelectedCurrency().symbol

        return when (reportType) {
            ReportType.SALE_REPORT -> generateSalesReportUseCase.execute(filters)
            ReportType.PURCHASE_REPORT -> generatePurchaseReport(filters)
            ReportType.EXPENSE_REPORT -> generateExpenseIncomeReportUseCase.execute(filters)
            ReportType.EXTRA_INCOME_REPORT -> generateExpenseIncomeReportUseCase.execute(filters)
            ReportType.TOP_PRODUCTS -> generateTopProductsReportUseCase.execute(filters)
            ReportType.TOP_CUSTOMERS -> generateTopCustomersReportUseCase.execute(filters)
            ReportType.STOCK_REPORT -> generateStockReportUseCase.execute(filters)
            ReportType.PRODUCT_REPORT -> generateProductReportUseCase.execute(filters)
            ReportType.CUSTOMER_REPORT -> generatePartyReportUseCase.execute(filters)
            ReportType.VENDOR_REPORT -> generatePartyReportUseCase.execute(filters)
            ReportType.PROFIT_LOSS_REPORT -> generateProfitLossByAvgPriceUseCase.execute(filters)
            ReportType.CASH_IN_HAND -> generateCashInHandReport(filters, currencySymbol)
            ReportType.BALANCE_REPORT -> generateBalanceReportUseCase.execute(filters)
            ReportType.PROFIT_LOSS_BY_PURCHASE -> generateProfitLossByPurchaseReport(
                filters,
                currencySymbol
            )

            ReportType.BALANCE_SHEET -> generateBalanceSheetReportUseCase.execute(filters)
            ReportType.INVESTOR_REPORT -> generatePartyReportUseCase.execute(filters)
            ReportType.WAREHOUSE_REPORT -> generateWarehouseReportUseCase.execute(filters)
        }
    }

    private fun generateSaleReport(filters: ReportFilters, currencySymbol: String): ReportResult {
        val columns = listOf("Date", "Invoice #", "Customer", "Amount", "Profit")
        val rows = listOf(
            ReportRow(
                "1",
                listOf(
                    "2024-01-15",
                    "INV-001",
                    "John Doe",
                    "$currencySymbol 15,000",
                    "$currencySymbol 3,000"
                )
            ),
            ReportRow(
                "2",
                listOf(
                    "2024-01-16",
                    "INV-002",
                    "Jane Smith",
                    "$currencySymbol 25,000",
                    "$currencySymbol 5,500"
                )
            ),
            ReportRow(
                "3",
                listOf(
                    "2024-01-17",
                    "INV-003",
                    "Bob Wilson",
                    "$currencySymbol 8,500",
                    "$currencySymbol 1,700"
                )
            ),
            ReportRow(
                "4",
                listOf(
                    "2024-01-18",
                    "INV-004",
                    "Alice Brown",
                    "$currencySymbol 32,000",
                    "$currencySymbol 7,200"
                )
            ),
            ReportRow(
                "5",
                listOf(
                    "2024-01-19",
                    "INV-005",
                    "Charlie Davis",
                    "$currencySymbol 18,500",
                    "$currencySymbol 3,900"
                )
            )
        )

        return ReportResult(
            reportType = ReportType.SALE_REPORT,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = ReportSummary(
                totalAmount = 99000.0,
                totalProfit = 21300.0,
                recordCount = 5
            )
        )
    }

    private suspend fun generatePurchaseReport(filters: ReportFilters): ReportResult {
        return generatePurchaseReportUseCase.execute(filters)
    }








    private fun generateProfitLossReport(
        filters: ReportFilters,
        currencySymbol: String
    ): ReportResult {
        val columns = listOf("Category", "Amount")
        val rows = listOf(
            ReportRow("1", listOf("Sales Revenue", "$currencySymbol 500,000")),
            ReportRow("2", listOf("Cost of Goods Sold", "$currencySymbol -300,000")),
            ReportRow("3", listOf("Gross Profit", "$currencySymbol 200,000")),
            ReportRow("4", listOf("Operating Expenses", "$currencySymbol -80,000")),
            ReportRow("5", listOf("Net Profit", "$currencySymbol 120,000"))
        )

        return ReportResult(
            reportType = ReportType.PROFIT_LOSS_REPORT,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = ReportSummary(
                totalProfit = 120000.0,
                recordCount = 5
            )
        )
    }

    private fun generateCashInHandReport(
        filters: ReportFilters,
        currencySymbol: String
    ): ReportResult {
        val columns = listOf("Payment Method", "Opening", "Received", "Paid", "Closing")
        val rows = listOf(
            ReportRow(
                "1",
                listOf(
                    "Cash",
                    "$currencySymbol 50,000",
                    "$currencySymbol 200,000",
                    "$currencySymbol 150,000",
                    "$currencySymbol 100,000"
                )
            ),
            ReportRow(
                "2",
                listOf(
                    "Bank Account",
                    "$currencySymbol 100,000",
                    "$currencySymbol 300,000",
                    "$currencySymbol 250,000",
                    "$currencySymbol 150,000"
                )
            )
        )

        return ReportResult(
            reportType = ReportType.CASH_IN_HAND,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = ReportSummary(
                totalAmount = 250000.0,
                recordCount = 2
            )
        )
    }


    private fun generateProfitLossByPurchaseReport(
        filters: ReportFilters,
        currencySymbol: String
    ): ReportResult {
        return generateProfitLossReport(filters, currencySymbol).copy(
            reportType = ReportType.PROFIT_LOSS_BY_PURCHASE
        )
    }

}

