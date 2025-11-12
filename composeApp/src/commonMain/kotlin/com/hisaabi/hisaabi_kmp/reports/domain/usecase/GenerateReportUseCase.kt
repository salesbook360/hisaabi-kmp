package com.hisaabi.hisaabi_kmp.reports.domain.usecase

import com.hisaabi.hisaabi_kmp.reports.domain.model.*

/**
 * Use case for generating report data based on filters.
 */
class GenerateReportUseCase(
    private val generateSalesReportUseCase: GenerateSalesReportUseCase
) {
    
    suspend fun execute(filters: ReportFilters): ReportResult {
        val reportType = filters.reportType ?: throw IllegalArgumentException("Report type is required")
        
        return when (reportType) {
            ReportType.SALE_REPORT -> generateSalesReportUseCase.execute(filters)
            ReportType.PURCHASE_REPORT -> generatePurchaseReport(filters)
            ReportType.EXPENSE_REPORT -> generateExpenseReport(filters)
            ReportType.EXTRA_INCOME_REPORT -> generateExtraIncomeReport(filters)
            ReportType.TOP_PRODUCTS -> generateTopProductsReport(filters)
            ReportType.TOP_CUSTOMERS -> generateTopCustomersReport(filters)
            ReportType.STOCK_REPORT -> generateStockReport(filters)
            ReportType.PRODUCT_REPORT -> generateProductReport(filters)
            ReportType.CUSTOMER_REPORT -> generateCustomerReport(filters)
            ReportType.VENDOR_REPORT -> generateVendorReport(filters)
            ReportType.PROFIT_LOSS_REPORT -> generateProfitLossReport(filters)
            ReportType.CASH_IN_HAND -> generateCashInHandReport(filters)
            ReportType.BALANCE_REPORT -> generateBalanceReport(filters)
            ReportType.PROFIT_LOSS_BY_PURCHASE -> generateProfitLossByPurchaseReport(filters)
            ReportType.BALANCE_SHEET -> generateBalanceSheetReport(filters)
            ReportType.INVESTOR_REPORT -> generateInvestorReport(filters)
            ReportType.WAREHOUSE_REPORT -> generateWarehouseReport(filters)
        }
    }
    
    private fun generateSaleReport(filters: ReportFilters): ReportResult {
        val columns = listOf("Date", "Invoice #", "Customer", "Amount", "Profit")
        val rows = listOf(
            ReportRow("1", listOf("2024-01-15", "INV-001", "John Doe", "Rs 15,000", "Rs 3,000")),
            ReportRow("2", listOf("2024-01-16", "INV-002", "Jane Smith", "Rs 25,000", "Rs 5,500")),
            ReportRow("3", listOf("2024-01-17", "INV-003", "Bob Wilson", "Rs 8,500", "Rs 1,700")),
            ReportRow("4", listOf("2024-01-18", "INV-004", "Alice Brown", "Rs 32,000", "Rs 7,200")),
            ReportRow("5", listOf("2024-01-19", "INV-005", "Charlie Davis", "Rs 18,500", "Rs 3,900"))
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
    
    private fun generatePurchaseReport(filters: ReportFilters): ReportResult {
        val columns = listOf("Date", "Bill #", "Vendor", "Amount", "Items")
        val rows = listOf(
            ReportRow("1", listOf("2024-01-15", "BILL-001", "ABC Suppliers", "Rs 50,000", "25")),
            ReportRow("2", listOf("2024-01-16", "BILL-002", "XYZ Traders", "Rs 75,000", "40")),
            ReportRow("3", listOf("2024-01-18", "BILL-003", "Global Imports", "Rs 120,000", "60"))
        )
        
        return ReportResult(
            reportType = ReportType.PURCHASE_REPORT,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = ReportSummary(
                totalAmount = 245000.0,
                totalQuantity = 125.0,
                recordCount = 3
            )
        )
    }
    
    private fun generateExpenseReport(filters: ReportFilters): ReportResult {
        val columns = listOf("Date", "Category", "Description", "Amount")
        val rows = listOf(
            ReportRow("1", listOf("2024-01-15", "Rent", "Office Rent - Jan", "Rs 50,000")),
            ReportRow("2", listOf("2024-01-16", "Utilities", "Electricity Bill", "Rs 8,500")),
            ReportRow("3", listOf("2024-01-17", "Salaries", "Staff Salary", "Rs 150,000")),
            ReportRow("4", listOf("2024-01-18", "Marketing", "Social Media Ads", "Rs 12,000"))
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
    
    private fun generateExtraIncomeReport(filters: ReportFilters): ReportResult {
        val columns = listOf("Date", "Source", "Description", "Amount")
        val rows = listOf(
            ReportRow("1", listOf("2024-01-15", "Commission", "Sale Commission", "Rs 5,000")),
            ReportRow("2", listOf("2024-01-18", "Interest", "Bank Interest", "Rs 2,500"))
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
    
    private fun generateTopProductsReport(filters: ReportFilters): ReportResult {
        val columns = listOf("Rank", "Product", "Quantity Sold", "Revenue", "Profit")
        val rows = listOf(
            ReportRow("1", listOf("#1", "Product A", "150", "Rs 75,000", "Rs 15,000")),
            ReportRow("2", listOf("#2", "Product B", "120", "Rs 60,000", "Rs 13,500")),
            ReportRow("3", listOf("#3", "Product C", "100", "Rs 50,000", "Rs 12,000")),
            ReportRow("4", listOf("#4", "Product D", "85", "Rs 42,500", "Rs 9,500")),
            ReportRow("5", listOf("#5", "Product E", "75", "Rs 37,500", "Rs 8,000"))
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
    
    private fun generateTopCustomersReport(filters: ReportFilters): ReportResult {
        val columns = listOf("Rank", "Customer", "Orders", "Total Amount", "Profit")
        val rows = listOf(
            ReportRow("1", listOf("#1", "John Doe", "25", "Rs 250,000", "Rs 50,000")),
            ReportRow("2", listOf("#2", "Jane Smith", "20", "Rs 200,000", "Rs 45,000")),
            ReportRow("3", listOf("#3", "Bob Wilson", "18", "Rs 180,000", "Rs 40,000")),
            ReportRow("4", listOf("#4", "Alice Brown", "15", "Rs 150,000", "Rs 35,000")),
            ReportRow("5", listOf("#5", "Charlie Davis", "12", "Rs 120,000", "Rs 28,000"))
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
    
    private fun generateStockReport(filters: ReportFilters): ReportResult {
        val columns = listOf("Product", "Category", "Stock", "Value", "Status")
        val rows = listOf(
            ReportRow("1", listOf("Product A", "Electronics", "50", "Rs 25,000", "Low")),
            ReportRow("2", listOf("Product B", "Clothing", "200", "Rs 40,000", "Good")),
            ReportRow("3", listOf("Product C", "Food", "0", "Rs 0", "Out of Stock")),
            ReportRow("4", listOf("Product D", "Electronics", "150", "Rs 75,000", "Good"))
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
    
    private fun generateProductReport(filters: ReportFilters): ReportResult {
        return generateStockReport(filters).copy(reportType = ReportType.PRODUCT_REPORT)
    }
    
    private fun generateCustomerReport(filters: ReportFilters): ReportResult {
        val columns = listOf("Date", "Transaction", "Type", "Amount", "Balance")
        val rows = listOf(
            ReportRow("1", listOf("2024-01-15", "INV-001", "Sale", "Rs 15,000", "Rs 15,000")),
            ReportRow("2", listOf("2024-01-16", "PAY-001", "Payment", "Rs -10,000", "Rs 5,000")),
            ReportRow("3", listOf("2024-01-17", "INV-002", "Sale", "Rs 25,000", "Rs 30,000"))
        )
        
        return ReportResult(
            reportType = ReportType.CUSTOMER_REPORT,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = ReportSummary(
                totalAmount = 30000.0,
                recordCount = 3,
                additionalInfo = mapOf("Current Balance" to "Rs 30,000")
            )
        )
    }
    
    private fun generateVendorReport(filters: ReportFilters): ReportResult {
        val columns = listOf("Date", "Transaction", "Type", "Amount", "Balance")
        val rows = listOf(
            ReportRow("1", listOf("2024-01-15", "BILL-001", "Purchase", "Rs 50,000", "Rs -50,000")),
            ReportRow("2", listOf("2024-01-16", "PAY-001", "Payment", "Rs 30,000", "Rs -20,000"))
        )
        
        return ReportResult(
            reportType = ReportType.VENDOR_REPORT,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = ReportSummary(
                totalAmount = -20000.0,
                recordCount = 2,
                additionalInfo = mapOf("Outstanding Balance" to "Rs -20,000")
            )
        )
    }
    
    private fun generateProfitLossReport(filters: ReportFilters): ReportResult {
        val columns = listOf("Category", "Amount")
        val rows = listOf(
            ReportRow("1", listOf("Sales Revenue", "Rs 500,000")),
            ReportRow("2", listOf("Cost of Goods Sold", "Rs -300,000")),
            ReportRow("3", listOf("Gross Profit", "Rs 200,000")),
            ReportRow("4", listOf("Operating Expenses", "Rs -80,000")),
            ReportRow("5", listOf("Net Profit", "Rs 120,000"))
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
    
    private fun generateCashInHandReport(filters: ReportFilters): ReportResult {
        val columns = listOf("Payment Method", "Opening", "Received", "Paid", "Closing")
        val rows = listOf(
            ReportRow("1", listOf("Cash", "Rs 50,000", "Rs 200,000", "Rs 150,000", "Rs 100,000")),
            ReportRow("2", listOf("Bank Account", "Rs 100,000", "Rs 300,000", "Rs 250,000", "Rs 150,000"))
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
    
    private fun generateBalanceReport(filters: ReportFilters): ReportResult {
        val columns = listOf("Party", "Type", "Total Sales", "Total Paid", "Balance")
        val rows = listOf(
            ReportRow("1", listOf("John Doe", "Customer", "Rs 100,000", "Rs 80,000", "Rs 20,000")),
            ReportRow("2", listOf("Jane Smith", "Customer", "Rs 75,000", "Rs 75,000", "Rs 0")),
            ReportRow("3", listOf("ABC Suppliers", "Vendor", "Rs 150,000", "Rs 100,000", "Rs -50,000"))
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
    
    private fun generateProfitLossByPurchaseReport(filters: ReportFilters): ReportResult {
        return generateProfitLossReport(filters).copy(
            reportType = ReportType.PROFIT_LOSS_BY_PURCHASE
        )
    }
    
    private fun generateBalanceSheetReport(filters: ReportFilters): ReportResult {
        val columns = listOf("Category", "Amount")
        val rows = listOf(
            ReportRow("1", listOf("Assets", "")),
            ReportRow("2", listOf("  Cash & Bank", "Rs 250,000")),
            ReportRow("3", listOf("  Inventory", "Rs 400,000")),
            ReportRow("4", listOf("  Receivables", "Rs 150,000")),
            ReportRow("5", listOf("Total Assets", "Rs 800,000")),
            ReportRow("6", listOf("Liabilities", "")),
            ReportRow("7", listOf("  Payables", "Rs 200,000")),
            ReportRow("8", listOf("Total Liabilities", "Rs 200,000")),
            ReportRow("9", listOf("Net Worth", "Rs 600,000"))
        )
        
        return ReportResult(
            reportType = ReportType.BALANCE_SHEET,
            filters = filters,
            columns = columns,
            rows = rows,
            summary = ReportSummary(
                totalAmount = 600000.0,
                recordCount = 9
            )
        )
    }
    
    private fun generateInvestorReport(filters: ReportFilters): ReportResult {
        val columns = listOf("Date", "Transaction", "Type", "Amount", "Balance")
        val rows = listOf(
            ReportRow("1", listOf("2024-01-01", "Initial Investment", "Investment", "Rs 500,000", "Rs 500,000")),
            ReportRow("2", listOf("2024-01-15", "Profit Share", "Withdrawal", "Rs -50,000", "Rs 450,000"))
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
    
    private fun generateWarehouseReport(filters: ReportFilters): ReportResult {
        val columns = listOf("Product", "Warehouse", "Stock", "Value")
        val rows = listOf(
            ReportRow("1", listOf("Product A", "Main Warehouse", "100", "Rs 50,000")),
            ReportRow("2", listOf("Product B", "Main Warehouse", "200", "Rs 40,000")),
            ReportRow("3", listOf("Product A", "Branch Warehouse", "50", "Rs 25,000"))
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

