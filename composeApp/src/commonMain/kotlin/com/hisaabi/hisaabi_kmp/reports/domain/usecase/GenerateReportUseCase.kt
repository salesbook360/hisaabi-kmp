package com.hisaabi.hisaabi_kmp.reports.domain.usecase

import com.hisaabi.hisaabi_kmp.reports.domain.model.*
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager

/**
 * Use case for generating report data based on filters.
 */
class GenerateReportUseCase(
    private val generateSalesReportUseCase: GenerateSalesReportUseCase,
    private val generateBalanceSheetReportUseCase: GenerateBalanceSheetReportUseCase,
    private val generatePurchaseReportUseCase: GeneratePurchaseReportUseCase,
    private val preferencesManager: PreferencesManager
) {
    
    suspend fun execute(filters: ReportFilters): ReportResult {
        val reportType = filters.reportType ?: throw IllegalArgumentException("Report type is required")
        val currencySymbol = preferencesManager.getSelectedCurrency().symbol
        
        return when (reportType) {
            ReportType.SALE_REPORT -> generateSalesReportUseCase.execute(filters)
            ReportType.PURCHASE_REPORT -> generatePurchaseReport(filters)
            ReportType.EXPENSE_REPORT -> generateExpenseReport(filters, currencySymbol)
            ReportType.EXTRA_INCOME_REPORT -> generateExtraIncomeReport(filters, currencySymbol)
            ReportType.TOP_PRODUCTS -> generateTopProductsReport(filters, currencySymbol)
            ReportType.TOP_CUSTOMERS -> generateTopCustomersReport(filters, currencySymbol)
            ReportType.STOCK_REPORT -> generateStockReport(filters, currencySymbol)
            ReportType.PRODUCT_REPORT -> generateProductReport(filters, currencySymbol)
            ReportType.CUSTOMER_REPORT -> generateCustomerReport(filters, currencySymbol)
            ReportType.VENDOR_REPORT -> generateVendorReport(filters, currencySymbol)
            ReportType.PROFIT_LOSS_REPORT -> generateProfitLossReport(filters, currencySymbol)
            ReportType.CASH_IN_HAND -> generateCashInHandReport(filters, currencySymbol)
            ReportType.BALANCE_REPORT -> generateBalanceReport(filters, currencySymbol)
            ReportType.PROFIT_LOSS_BY_PURCHASE -> generateProfitLossByPurchaseReport(filters, currencySymbol)
            ReportType.BALANCE_SHEET -> generateBalanceSheetReportUseCase.execute(filters)
            ReportType.INVESTOR_REPORT -> generateInvestorReport(filters, currencySymbol)
            ReportType.WAREHOUSE_REPORT -> generateWarehouseReport(filters, currencySymbol)
        }
    }
    
    private fun generateSaleReport(filters: ReportFilters, currencySymbol: String): ReportResult {
        val columns = listOf("Date", "Invoice #", "Customer", "Amount", "Profit")
        val rows = listOf(
            ReportRow("1", listOf("2024-01-15", "INV-001", "John Doe", "$currencySymbol 15,000", "$currencySymbol 3,000")),
            ReportRow("2", listOf("2024-01-16", "INV-002", "Jane Smith", "$currencySymbol 25,000", "$currencySymbol 5,500")),
            ReportRow("3", listOf("2024-01-17", "INV-003", "Bob Wilson", "$currencySymbol 8,500", "$currencySymbol 1,700")),
            ReportRow("4", listOf("2024-01-18", "INV-004", "Alice Brown", "$currencySymbol 32,000", "$currencySymbol 7,200")),
            ReportRow("5", listOf("2024-01-19", "INV-005", "Charlie Davis", "$currencySymbol 18,500", "$currencySymbol 3,900"))
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
    
    private fun generateExpenseReport(filters: ReportFilters, currencySymbol: String): ReportResult {
        val columns = listOf("Date", "Category", "Description", "Amount")
        val rows = listOf(
            ReportRow("1", listOf("2024-01-15", "Rent", "Office Rent - Jan", "$currencySymbol 50,000")),
            ReportRow("2", listOf("2024-01-16", "Utilities", "Electricity Bill", "$currencySymbol 8,500")),
            ReportRow("3", listOf("2024-01-17", "Salaries", "Staff Salary", "$currencySymbol 150,000")),
            ReportRow("4", listOf("2024-01-18", "Marketing", "Social Media Ads", "$currencySymbol 12,000"))
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
    
    private fun generateExtraIncomeReport(filters: ReportFilters, currencySymbol: String): ReportResult {
        val columns = listOf("Date", "Source", "Description", "Amount")
        val rows = listOf(
            ReportRow("1", listOf("2024-01-15", "Commission", "Sale Commission", "$currencySymbol 5,000")),
            ReportRow("2", listOf("2024-01-18", "Interest", "Bank Interest", "$currencySymbol 2,500"))
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
    
    private fun generateTopProductsReport(filters: ReportFilters, currencySymbol: String): ReportResult {
        val columns = listOf("Rank", "Product", "Quantity Sold", "Revenue", "Profit")
        val rows = listOf(
            ReportRow("1", listOf("#1", "Product A", "150", "$currencySymbol 75,000", "$currencySymbol 15,000")),
            ReportRow("2", listOf("#2", "Product B", "120", "$currencySymbol 60,000", "$currencySymbol 13,500")),
            ReportRow("3", listOf("#3", "Product C", "100", "$currencySymbol 50,000", "$currencySymbol 12,000")),
            ReportRow("4", listOf("#4", "Product D", "85", "$currencySymbol 42,500", "$currencySymbol 9,500")),
            ReportRow("5", listOf("#5", "Product E", "75", "$currencySymbol 37,500", "$currencySymbol 8,000"))
        )
        
        return ReportResult(
            reportType = ReportType.TOP_PRODUCTS,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = ReportSummary(
                totalAmount = 265000.0,
                totalProfit = 58000.0,
                totalQuantity = 530.0,
                recordCount = 5
            )
        )
    }
    
    private fun generateTopCustomersReport(filters: ReportFilters, currencySymbol: String): ReportResult {
        val columns = listOf("Rank", "Customer", "Orders", "Total Amount", "Profit")
        val rows = listOf(
            ReportRow("1", listOf("#1", "John Doe", "25", "$currencySymbol 250,000", "$currencySymbol 50,000")),
            ReportRow("2", listOf("#2", "Jane Smith", "20", "$currencySymbol 200,000", "$currencySymbol 45,000")),
            ReportRow("3", listOf("#3", "Bob Wilson", "18", "$currencySymbol 180,000", "$currencySymbol 40,000")),
            ReportRow("4", listOf("#4", "Alice Brown", "15", "$currencySymbol 150,000", "$currencySymbol 35,000")),
            ReportRow("5", listOf("#5", "Charlie Davis", "12", "$currencySymbol 120,000", "$currencySymbol 28,000"))
        )
        
        return ReportResult(
            reportType = ReportType.TOP_CUSTOMERS,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = ReportSummary(
                totalAmount = 900000.0,
                totalProfit = 198000.0,
                recordCount = 5
            )
        )
    }
    
    private fun generateStockReport(filters: ReportFilters, currencySymbol: String): ReportResult {
        val columns = listOf("Product", "Category", "Stock", "Value", "Status")
        val rows = listOf(
            ReportRow("1", listOf("Product A", "Electronics", "50", "$currencySymbol 25,000", "Low")),
            ReportRow("2", listOf("Product B", "Clothing", "200", "$currencySymbol 40,000", "Good")),
            ReportRow("3", listOf("Product C", "Food", "0", "$currencySymbol 0", "Out of Stock")),
            ReportRow("4", listOf("Product D", "Electronics", "150", "$currencySymbol 75,000", "Good"))
        )
        
        return ReportResult(
            reportType = ReportType.STOCK_REPORT,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = ReportSummary(
                totalAmount = 140000.0,
                totalQuantity = 400.0,
                recordCount = 4
            )
        )
    }
    
    private fun generateProductReport(filters: ReportFilters, currencySymbol: String): ReportResult {
        return generateStockReport(filters, currencySymbol).copy(reportType = ReportType.PRODUCT_REPORT)
    }
    
    private fun generateCustomerReport(filters: ReportFilters, currencySymbol: String): ReportResult {
        val columns = listOf("Date", "Transaction", "Type", "Amount", "Balance")
        val rows = listOf(
            ReportRow("1", listOf("2024-01-15", "INV-001", "Sale", "$currencySymbol 15,000", "$currencySymbol 15,000")),
            ReportRow("2", listOf("2024-01-16", "PAY-001", "Payment", "$currencySymbol -10,000", "$currencySymbol 5,000")),
            ReportRow("3", listOf("2024-01-17", "INV-002", "Sale", "$currencySymbol 25,000", "$currencySymbol 30,000"))
        )
        
        return ReportResult(
            reportType = ReportType.CUSTOMER_REPORT,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = ReportSummary(
                totalAmount = 30000.0,
                recordCount = 3,
                additionalInfo = mapOf("Current Balance" to "$currencySymbol 30,000")
            )
        )
    }
    
    private fun generateVendorReport(filters: ReportFilters, currencySymbol: String): ReportResult {
        val columns = listOf("Date", "Transaction", "Type", "Amount", "Balance")
        val rows = listOf(
            ReportRow("1", listOf("2024-01-15", "BILL-001", "Purchase", "$currencySymbol 50,000", "$currencySymbol -50,000")),
            ReportRow("2", listOf("2024-01-16", "PAY-001", "Payment", "$currencySymbol 30,000", "$currencySymbol -20,000"))
        )
        
        return ReportResult(
            reportType = ReportType.VENDOR_REPORT,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = ReportSummary(
                totalAmount = -20000.0,
                recordCount = 2,
                additionalInfo = mapOf("Outstanding Balance" to "$currencySymbol -20,000")
            )
        )
    }
    
    private fun generateProfitLossReport(filters: ReportFilters, currencySymbol: String): ReportResult {
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
    
    private fun generateCashInHandReport(filters: ReportFilters, currencySymbol: String): ReportResult {
        val columns = listOf("Payment Method", "Opening", "Received", "Paid", "Closing")
        val rows = listOf(
            ReportRow("1", listOf("Cash", "$currencySymbol 50,000", "$currencySymbol 200,000", "$currencySymbol 150,000", "$currencySymbol 100,000")),
            ReportRow("2", listOf("Bank Account", "$currencySymbol 100,000", "$currencySymbol 300,000", "$currencySymbol 250,000", "$currencySymbol 150,000"))
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
    
    private fun generateBalanceReport(filters: ReportFilters, currencySymbol: String): ReportResult {
        val columns = listOf("Party", "Type", "Total Sales", "Total Paid", "Balance")
        val rows = listOf(
            ReportRow("1", listOf("John Doe", "Customer", "$currencySymbol 100,000", "$currencySymbol 80,000", "$currencySymbol 20,000")),
            ReportRow("2", listOf("Jane Smith", "Customer", "$currencySymbol 75,000", "$currencySymbol 75,000", "$currencySymbol 0")),
            ReportRow("3", listOf("ABC Suppliers", "Vendor", "$currencySymbol 150,000", "$currencySymbol 100,000", "$currencySymbol -50,000"))
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
    
    private fun generateProfitLossByPurchaseReport(filters: ReportFilters, currencySymbol: String): ReportResult {
        return generateProfitLossReport(filters, currencySymbol).copy(
            reportType = ReportType.PROFIT_LOSS_BY_PURCHASE
        )
    }
    
    
    private fun generateInvestorReport(filters: ReportFilters, currencySymbol: String): ReportResult {
        val columns = listOf("Date", "Transaction", "Type", "Amount", "Balance")
        val rows = listOf(
            ReportRow("1", listOf("2024-01-01", "Initial Investment", "Investment", "$currencySymbol 500,000", "$currencySymbol 500,000")),
            ReportRow("2", listOf("2024-01-15", "Profit Share", "Withdrawal", "$currencySymbol -50,000", "$currencySymbol 450,000"))
        )
        
        return ReportResult(
            reportType = ReportType.INVESTOR_REPORT,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = ReportSummary(
                totalAmount = 450000.0,
                recordCount = 2
            )
        )
    }
    
    private fun generateWarehouseReport(filters: ReportFilters, currencySymbol: String): ReportResult {
        val columns = listOf("Product", "Warehouse", "Stock", "Value")
        val rows = listOf(
            ReportRow("1", listOf("Product A", "Main Warehouse", "100", "$currencySymbol 50,000")),
            ReportRow("2", listOf("Product B", "Main Warehouse", "200", "$currencySymbol 40,000")),
            ReportRow("3", listOf("Product A", "Branch Warehouse", "50", "$currencySymbol 25,000"))
        )
        
        return ReportResult(
            reportType = ReportType.WAREHOUSE_REPORT,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = ReportSummary(
                totalAmount = 115000.0,
                totalQuantity = 350.0,
                recordCount = 3
            )
        )
    }
}

