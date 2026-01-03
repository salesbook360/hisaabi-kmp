package com.hisaabi.hisaabi_kmp.reports.domain.usecase

import com.hisaabi.hisaabi_kmp.reports.domain.model.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * HTML generator for all reports
 * Generates a printable, exportable HTML report with support for breakdown data
 */
object ReportHtmlGenerator {
    
    fun generateHtmlReport(
        reportResult: ReportResult,
        currencySymbol: String
    ): String {
        val html = StringBuilder()
        html.appendLine("<!DOCTYPE html>")
        html.appendLine("<html>")
        html.appendLine("<head>")
        html.appendLine("<meta charset='UTF-8'>")
        html.appendLine("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
        html.appendLine("<title>${reportResult.reportType.title}</title>")
        html.appendLine("<style>")
        html.appendLine(getReportStyles())
        html.appendLine("</style>")
        html.appendLine("</head>")
        html.appendLine("<body>")
        html.appendLine("<div class='report'>")
        
        // Header
        html.appendLine(generateHeader(reportResult))
        
        // Summary
        reportResult.summary?.let { summary ->
            html.appendLine(generateSummary(summary, currencySymbol))
        }
        
        // Filters Info
        html.appendLine(generateFiltersInfo(reportResult))
        
        // Table
        html.appendLine(generateTable(reportResult, currencySymbol))
        
        // Footer
        html.appendLine(generateFooter(reportResult))
        
        html.appendLine("</div>")
        html.appendLine("</body>")
        html.appendLine("</html>")
        
        return html.toString()
    }
    
    private fun getReportStyles(): String {
        return """
        @media print {
            body {
                margin: 0;
                padding: 0;
            }
            .report {
                box-shadow: none;
            }
            .no-print {
                display: none;
            }
        }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            margin: 0;
            padding: 20px;
            background: #f5f5f5;
        }
        .report {
            background: white;
            padding: 30px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            max-width: 1200px;
            margin: 0 auto;
            overflow-x: auto;
            overflow-y: visible;
        }
        .header {
            text-align: center;
            border-bottom: 3px solid #1976D2;
            padding-bottom: 20px;
            margin-bottom: 30px;
        }
        .header h1 {
            color: #1976D2;
            margin: 0;
            font-size: 28px;
            font-weight: bold;
        }
        .header .subtitle {
            color: #616161;
            font-size: 14px;
            margin-top: 8px;
        }
        .summary-section {
            background: #E3F2FD;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 20px;
        }
        .summary-title {
            font-weight: bold;
            font-size: 16px;
            color: #1976D2;
            margin-bottom: 15px;
        }
        .summary-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
        }
        .summary-item {
            display: flex;
            flex-direction: column;
        }
        .summary-label {
            font-size: 12px;
            color: #616161;
            margin-bottom: 4px;
        }
        .summary-value {
            font-size: 16px;
            font-weight: bold;
            color: #212121;
        }
        .filters-info {
            background: #F5F5F5;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 12px;
            color: #616161;
        }
        .filters-info span {
            margin-right: 20px;
        }
        .table-container {
            width: 100%;
            overflow-x: auto;
            margin: 20px 0;
            -webkit-overflow-scrolling: touch;
        }
        .report-table {
            width: 100%;
            min-width: 100%;
            border-collapse: collapse;
            font-size: 12px;
        }
        .report-table thead {
            background: #1976D2;
            color: white;
        }
        .report-table th {
            padding: 12px 8px;
            text-align: left;
            font-weight: bold;
            font-size: 11px;
            white-space: nowrap;
        }
        .report-table th.text-right {
            text-align: right;
        }
        .report-table th.text-center {
            text-align: center;
        }
        .report-table tbody tr {
            border-bottom: 1px solid #e0e0e0;
        }
        .report-table tbody tr:nth-child(even) {
            background: #fafafa;
        }
        .report-table tbody tr:hover {
            background: #E3F2FD;
        }
        .report-table td {
            padding: 10px 8px;
            color: #212121;
        }
        .report-table td.text-right {
            text-align: right;
        }
        .report-table td.text-center {
            text-align: center;
        }
        .report-table td.product-name {
            font-weight: 500;
            color: #1976D2;
        }
        .report-table tr.breakdown-row {
            background: #fafafa !important;
        }
        .report-table tr.breakdown-row td {
            padding-left: 30px;
            font-size: 11px;
            color: #757575;
            font-style: italic;
        }
        .report-table tr.total-row {
            background: #E3F2FD !important;
            font-weight: bold;
        }
        .report-table tr.total-row td {
            border-top: 2px solid #1976D2;
            padding-top: 12px;
        }
        .balance-sheet-table {
            width: 100%;
            min-width: 100%;
            border-collapse: collapse;
            font-size: 12px;
        }
        .balance-sheet-table thead {
            background: #1976D2;
            color: white;
        }
        .balance-sheet-table th {
            padding: 12px;
            text-align: left;
            font-weight: bold;
            font-size: 11px;
        }
        .balance-sheet-table tbody tr {
            border-bottom: 1px solid #e0e0e0;
        }
        .balance-sheet-table tbody tr:nth-child(even) {
            background: #fafafa;
        }
        .balance-sheet-table td {
            padding: 10px 12px;
            color: #212121;
        }
        .balance-sheet-table tr.breakdown-row td {
            padding-left: 40px;
            font-size: 11px;
            color: #757575;
            font-style: italic;
        }
        .balance-sheet-table tr.total-row {
            background: #E3F2FD !important;
            font-weight: bold;
        }
        .balance-sheet-table tr.total-row td {
            border-top: 2px solid #1976D2;
            padding-top: 12px;
            color: #1976D2;
        }
        .footer {
            margin-top: 30px;
            padding-top: 20px;
            border-top: 2px solid #e0e0e0;
            text-align: center;
            font-size: 11px;
            color: #757575;
        }
        """.trimIndent()
    }
    
    private fun generateHeader(reportResult: ReportResult): String {
        val generatedDate = Instant.fromEpochMilliseconds(reportResult.generatedAt)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val dateStr = "${generatedDate.date} ${generatedDate.time}"
        
        return """
        <div class='header'>
            <h1>${escapeHtml(reportResult.reportType.title)}</h1>
            <div class='subtitle'>Generated on: ${escapeHtml(dateStr)}</div>
        </div>
        """.trimIndent()
    }
    
    private fun generateSummary(summary: ReportSummary, currencySymbol: String): String {
        val html = StringBuilder()
        html.appendLine("<div class='summary-section'>")
        html.appendLine("<div class='summary-title'>Summary</div>")
        html.appendLine("<div class='summary-grid'>")
        
        summary.totalAmount?.let {
            html.appendLine("""
            <div class='summary-item'>
                <div class='summary-label'>Total Amount</div>
                <div class='summary-value'>$currencySymbol ${String.format("%,.2f", it)}</div>
            </div>
            """.trimIndent())
        }
        
        summary.totalProfit?.let {
            html.appendLine("""
            <div class='summary-item'>
                <div class='summary-label'>Total Profit</div>
                <div class='summary-value'>$currencySymbol ${String.format("%,.2f", it)}</div>
            </div>
            """.trimIndent())
        }
        
        summary.totalQuantity?.let {
            html.appendLine("""
            <div class='summary-item'>
                <div class='summary-label'>Total Quantity</div>
                <div class='summary-value'>${String.format("%,.2f", it)}</div>
            </div>
            """.trimIndent())
        }
        
        html.appendLine("""
        <div class='summary-item'>
            <div class='summary-label'>Record Count</div>
            <div class='summary-value'>${summary.recordCount}</div>
        </div>
        """.trimIndent())
        
        // Additional info - always show all items
        if (summary.additionalInfo.isNotEmpty()) {
            summary.additionalInfo.forEach { (key, value) ->
                html.appendLine("""
                <div class='summary-item'>
                    <div class='summary-label'>${escapeHtml(key)}</div>
                    <div class='summary-value'>${escapeHtml(value)}</div>
                </div>
                """.trimIndent())
            }
        }
        
        html.appendLine("</div>")
        html.appendLine("</div>")
        return html.toString()
    }
    
    private fun generateFiltersInfo(reportResult: ReportResult): String {
        val html = StringBuilder()
        html.appendLine("<div class='filters-info'>")
        
        // Only show date filter if it's applicable
        val shouldShowDateFilter = reportResult.filters.additionalFilter !in listOf(
            com.hisaabi.hisaabi_kmp.reports.domain.model.ReportAdditionalFilter.STOCK_WORTH,
            com.hisaabi.hisaabi_kmp.reports.domain.model.ReportAdditionalFilter.OUT_OF_STOCK
        )
        
        if (shouldShowDateFilter) {
            html.appendLine("<span><strong>Date:</strong> ${escapeHtml(reportResult.filters.dateFilter.title)}</span>")
        }
        
        reportResult.filters.additionalFilter?.let {
            html.appendLine("<span><strong>Filter:</strong> ${escapeHtml(it.title)}</span>")
        }
        
        reportResult.filters.groupBy?.let {
            html.appendLine("<span><strong>Grouped by:</strong> ${escapeHtml(it.title)}</span>")
        }
        
        html.appendLine("<span><strong>Sorted by:</strong> ${escapeHtml(reportResult.filters.sortBy.title)}</span>")
        
        html.appendLine("</div>")
        return html.toString()
    }
    
    private fun generateTable(reportResult: ReportResult, currencySymbol: String): String {
        val html = StringBuilder()
        
        // Check if this is a Balance Sheet (has special 4-column layout)
        val isBalanceSheet = reportResult.reportType == ReportType.BALANCE_SHEET
        
        if (isBalanceSheet) {
            html.appendLine("<div class='table-container'>")
            html.appendLine("<table class='balance-sheet-table'>")
            html.appendLine("<thead>")
            html.appendLine("<tr>")
            html.appendLine("<th style='width: 25%;'>Assets</th>")
            html.appendLine("<th style='width: 25%; text-align: right;'>Amount</th>")
            html.appendLine("<th style='width: 25%;'>Liabilities</th>")
            html.appendLine("<th style='width: 25%; text-align: right;'>Amount</th>")
            html.appendLine("</tr>")
            html.appendLine("</thead>")
            html.appendLine("<tbody>")
            
            if (reportResult.rows.isEmpty()) {
                html.appendLine("""
                <tr>
                    <td colspan='4' style='text-align: center; padding: 30px; color: #757575;'>
                        No data available
                    </td>
                </tr>
                """.trimIndent())
            } else {
                reportResult.rows.forEach { row ->
                    val assetLabel = row.values.getOrNull(0)?.trim() ?: ""
                    val assetValue = row.values.getOrNull(1)?.trim() ?: ""
                    val liabilityLabel = row.values.getOrNull(2)?.trim() ?: ""
                    val liabilityValue = row.values.getOrNull(3)?.trim() ?: ""
                    
                    val isTotalRow = assetLabel.contains("Total", ignoreCase = true) || 
                                   liabilityLabel.contains("Total", ignoreCase = true)
                    val isEmptyRow = row.values.all { it.trim().isEmpty() }
                    
                    if (!isEmptyRow) {
                        html.appendLine("<tr${if (isTotalRow) " class='total-row'" else ""}>")
                        html.appendLine("<td>${escapeHtml(assetLabel)}</td>")
                        html.appendLine("<td style='text-align: right;'>${escapeHtml(assetValue)}</td>")
                        html.appendLine("<td>${escapeHtml(liabilityLabel)}</td>")
                        html.appendLine("<td style='text-align: right;'>${escapeHtml(liabilityValue)}</td>")
                        html.appendLine("</tr>")
                        
                        // Add breakdown rows for assets
                        if (assetLabel.isNotEmpty() && !isTotalRow) {
                            generateBreakdownRows(html, assetLabel, assetValue, reportResult, currencySymbol, isAsset = true)
                        }
                        
                        // Add breakdown rows for liabilities
                        if (liabilityLabel.isNotEmpty() && !isTotalRow) {
                            generateBreakdownRows(html, liabilityLabel, liabilityValue, reportResult, currencySymbol, isAsset = false)
                        }
                    }
                }
            }
            
            html.appendLine("</tbody>")
            html.appendLine("</table>")
            html.appendLine("</div>")
        } else {
            // Regular table for other reports
            html.appendLine("<div class='table-container'>")
            html.appendLine("<table class='report-table'>")
            html.appendLine("<thead>")
            html.appendLine("<tr>")
            
            reportResult.columns.forEachIndexed { index, column ->
                val isNumeric = index > 0 && (column.contains("Qty", ignoreCase = true) || 
                                              column.contains("Price", ignoreCase = true) ||
                                              column.contains("Amount", ignoreCase = true) ||
                                              column.contains("Worth", ignoreCase = true) ||
                                              column.contains("Balance", ignoreCase = true) ||
                                              column.contains("Profit", ignoreCase = true))
                val alignClass = if (isNumeric) "text-right" else ""
                html.appendLine("<th class='$alignClass'>${escapeHtml(column)}</th>")
            }
            
            html.appendLine("</tr>")
            html.appendLine("</thead>")
            html.appendLine("<tbody>")
            
            if (reportResult.rows.isEmpty()) {
                html.appendLine("""
                <tr>
                    <td colspan='${reportResult.columns.size}' style='text-align: center; padding: 30px; color: #757575;'>
                        No data available for the selected filters
                    </td>
                </tr>
                """.trimIndent())
            } else {
                reportResult.rows.forEach { row ->
                    val firstColumnValue = row.values.getOrNull(0) ?: ""
                    val isTotalRow = firstColumnValue.contains("Total", ignoreCase = true)
                    
                    html.appendLine("<tr${if (isTotalRow) " class='total-row'" else ""}>")
                    row.values.forEachIndexed { index, value ->
                        val isNumeric = index > 0 && (value.contains(currencySymbol) || 
                                                     value.matches(Regex("^[\\d.,\\-\\s]+$")))
                        val alignClass = if (isNumeric) "text-right" else ""
                        val cellClass = if (index == 0) "product-name" else ""
                        html.appendLine("<td class='$alignClass $cellClass'>${escapeHtml(value)}</td>")
                    }
                    html.appendLine("</tr>")
                    
                    // Add breakdown rows if applicable
                    if (firstColumnValue.isNotEmpty() && !isTotalRow) {
                        generateBreakdownRows(html, firstColumnValue, "", reportResult, currencySymbol, isAsset = true)
                    }
                }
            }
            
            html.appendLine("</tbody>")
            html.appendLine("</table>")
            html.appendLine("</div>")
        }
        
        return html.toString()
    }
    
    private fun generateBreakdownRows(
        html: StringBuilder,
        label: String,
        value: String,
        reportResult: ReportResult,
        currencySymbol: String,
        isAsset: Boolean
    ) {
        val breakdown = when {
            label.equals("Receivable", ignoreCase = true) -> reportResult.receivableBreakdown?.let {
                listOf(
                    "Customers" to it.customerReceivables,
                    "Vendors" to it.vendorReceivables,
                    "Investors" to it.investorReceivables
                )
            }
            label.equals("Payables", ignoreCase = true) -> reportResult.payablesBreakdown?.let {
                listOf(
                    "Customers" to it.customerPayables,
                    "Vendors" to it.vendorPayables,
                    "Investors" to it.investorPayables
                )
            }
            label.equals("Cash in Hand", ignoreCase = true) -> reportResult.cashInHandBreakdown?.let {
                it.paymentMethodBreakdowns.map { (name, amount) -> name to amount }
            }
            label.equals("Capital Investment", ignoreCase = true) -> reportResult.capitalInvestmentBreakdown?.let {
                listOf(
                    "Opening Stock Worth" to it.openingStockWorth,
                    "Opening Party Balances" to it.openingPartyBalances,
                    "Opening Payment Methods" to it.openingPaymentAmounts
                )
            }
            label.equals("Current Profit/Loss", ignoreCase = true) -> reportResult.profitLossBreakdown?.let {
                listOf(
                    "Sale Amount" to it.saleAmount,
                    "Cost of Sold Products" to it.costOfSoldProducts,
                    "Profit/Loss" to it.profitOrLoss,
                    "Discount Taken" to it.discountTaken,
                    "Discount Given" to it.discountGiven,
                    "Total Expenses" to it.totalExpenses,
                    "Total Income" to it.totalIncome,
                    "Additional Charges Received" to it.additionalChargesReceived,
                    "Additional Charges Paid" to it.additionalChargesPaid,
                    "Tax Paid" to it.taxPaid,
                    "Tax Received" to it.taxReceived
                )
            }
            label.equals("Available Stock", ignoreCase = true) -> reportResult.availableStockBreakdown?.let {
                listOf("Total Available Stock" to it.totalAvailableStock)
            }
            else -> null
        }
        
        breakdown?.forEach { (breakdownLabel, breakdownValue) ->
            if (reportResult.reportType == ReportType.BALANCE_SHEET) {
                // Balance sheet has 4 columns
                html.appendLine("<tr class='breakdown-row'>")
                if (isAsset) {
                    html.appendLine("<td>${escapeHtml(breakdownLabel)}</td>")
                    html.appendLine("<td style='text-align: right;'>$currencySymbol ${String.format("%,.2f", breakdownValue)}</td>")
                    html.appendLine("<td></td>")
                    html.appendLine("<td></td>")
                } else {
                    html.appendLine("<td></td>")
                    html.appendLine("<td></td>")
                    html.appendLine("<td>${escapeHtml(breakdownLabel)}</td>")
                    html.appendLine("<td style='text-align: right;'>$currencySymbol ${String.format("%,.2f", breakdownValue)}</td>")
                }
                html.appendLine("</tr>")
            } else {
                // Regular reports - add breakdown as additional columns
                html.appendLine("<tr class='breakdown-row'>")
                html.appendLine("<td>${escapeHtml(breakdownLabel)}</td>")
                // Fill remaining columns with empty cells or breakdown value if appropriate
                for (i in 1 until reportResult.columns.size) {
                    if (i == reportResult.columns.size - 1) {
                        // Last column shows the value
                        html.appendLine("<td class='text-right'>$currencySymbol ${String.format("%,.2f", breakdownValue)}</td>")
                    } else {
                        html.appendLine("<td></td>")
                    }
                }
                html.appendLine("</tr>")
            }
        }
    }
    
    private fun generateFooter(reportResult: ReportResult): String {
        return """
        <div class='footer'>
            <p>This report was generated by Hisaabi</p>
            <p>Report Type: ${escapeHtml(reportResult.reportType.title)}</p>
        </div>
        """.trimIndent()
    }
    
    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}

