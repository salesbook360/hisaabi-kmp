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
    private val preferencesManager: PreferencesManager
) {

    suspend fun execute(filters: ReportFilters): ReportResult {
        val reportType =
            filters.reportType ?: throw IllegalArgumentException("Report type is required")
        val currencySymbol = preferencesManager.getSelectedCurrency().symbol

        return when (reportType) {
            ReportType.SALE_REPORT -> generateSalesReportUseCase.execute(filters)
            ReportType.PURCHASE_REPORT -> generatePurchaseReport(filters)
            ReportType.EXPENSE_REPORT -> generateExpenseReport(filters, currencySymbol)
            ReportType.EXTRA_INCOME_REPORT -> generateExtraIncomeReport(filters, currencySymbol)
            ReportType.TOP_PRODUCTS -> generateTopProductsReportUseCase.execute(filters)
            ReportType.TOP_CUSTOMERS -> generateTopCustomersReportUseCase.execute(filters)
            ReportType.STOCK_REPORT -> generateStockReportUseCase.execute(filters)
            ReportType.PRODUCT_REPORT -> generateProductReportUseCase.execute(filters)
            ReportType.CUSTOMER_REPORT -> generatePartyReportUseCase.execute(filters)
            ReportType.VENDOR_REPORT -> generatePartyReportUseCase.execute(filters)
            ReportType.PROFIT_LOSS_REPORT -> generateProfitLossByAvgPriceUseCase.execute(filters)
            ReportType.CASH_IN_HAND -> generateCashInHandReport(filters, currencySymbol)
            ReportType.BALANCE_REPORT -> generateBalanceReport(filters, currencySymbol)
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

    private fun generateExpenseReport(
        filters: ReportFilters,
        currencySymbol: String
    ): ReportResult {
        val columns = listOf("Date", "Category", "Description", "Amount")
        val rows = listOf(
            ReportRow(
                "1",
                listOf("2024-01-15", "Rent", "Office Rent - Jan", "$currencySymbol 50,000")
            ),
            ReportRow(
                "2",
                listOf("2024-01-16", "Utilities", "Electricity Bill", "$currencySymbol 8,500")
            ),
            ReportRow(
                "3",
                listOf("2024-01-17", "Salaries", "Staff Salary", "$currencySymbol 150,000")
            ),
            ReportRow(
                "4",
                listOf("2024-01-18", "Marketing", "Social Media Ads", "$currencySymbol 12,000")
            )
        )

        return ReportResult(
            reportType = ReportType.EXPENSE_REPORT,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = ReportSummary(
                totalAmount = 220500.0,
                recordCount = 4
            )
        )
    }

    private fun generateExtraIncomeReport(
        filters: ReportFilters,
        currencySymbol: String
    ): ReportResult {
        val columns = listOf("Date", "Source", "Description", "Amount")
        val rows = listOf(
            ReportRow(
                "1",
                listOf("2024-01-15", "Commission", "Sale Commission", "$currencySymbol 5,000")
            ),
            ReportRow(
                "2",
                listOf("2024-01-18", "Interest", "Bank Interest", "$currencySymbol 2,500")
            )
        )

        return ReportResult(
            reportType = ReportType.EXTRA_INCOME_REPORT,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = ReportSummary(
                totalAmount = 7500.0,
                recordCount = 2
            )
        )
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

    private fun generateBalanceReport(
        filters: ReportFilters,
        currencySymbol: String
    ): ReportResult {
        val columns = listOf("Party", "Type", "Total Sales", "Total Paid", "Balance")
        val rows = listOf(
            ReportRow(
                "1",
                listOf(
                    "John Doe",
                    "Customer",
                    "$currencySymbol 100,000",
                    "$currencySymbol 80,000",
                    "$currencySymbol 20,000"
                )
            ),
            ReportRow(
                "2",
                listOf(
                    "Jane Smith",
                    "Customer",
                    "$currencySymbol 75,000",
                    "$currencySymbol 75,000",
                    "$currencySymbol 0"
                )
            ),
            ReportRow(
                "3",
                listOf(
                    "ABC Suppliers",
                    "Vendor",
                    "$currencySymbol 150,000",
                    "$currencySymbol 100,000",
                    "$currencySymbol -50,000"
                )
            )
        )

        return ReportResult(
            reportType = ReportType.BALANCE_REPORT,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = ReportSummary(
                totalAmount = -30000.0,
                recordCount = 3
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

