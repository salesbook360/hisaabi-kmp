package com.hisaabi.hisaabi_kmp.receipt

import com.hisaabi.hisaabi_kmp.settings.domain.model.ReceiptConfig
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.utils.formatTransactionDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File
import java.io.FileWriter
import kotlin.math.abs

actual fun getReceiptCapture(): ReceiptCapture {
    return object : ReceiptCapture {
        override suspend fun captureReceipt(
            transaction: Transaction,
            config: ReceiptConfig,
            currencySymbol: String
        ): ReceiptResult = withContext(Dispatchers.IO) {
            try {
                // Create receipts directory in user home
                val userHome = System.getProperty("user.home")
                val receiptsDir = File(userHome, ".hisaabi/receipts")
                if (!receiptsDir.exists()) {
                    receiptsDir.mkdirs()
                }
                
                val fileName = "receipt_${transaction.slug ?: System.currentTimeMillis()}.html"
                val receiptFile = File(receiptsDir, fileName)
                
                // Generate HTML receipt
                val htmlContent = generateHtmlReceipt(transaction, config, currencySymbol)
                
                // Write to file
                FileWriter(receiptFile).use { writer ->
                    writer.write(htmlContent)
                }
                
                ReceiptResult.PdfFile(receiptFile.absolutePath) // Using PdfFile type for consistency, even though it's HTML
            } catch (e: Exception) {
                ReceiptResult.Error("Failed to generate receipt: ${e.message}")
            }
        }
        
        override suspend fun shareReceipt(result: ReceiptResult, transaction: Transaction) {
            if (!Desktop.isDesktopSupported()) {
                return
            }
            
            val filePath = when (result) {
                is ReceiptResult.PdfFile -> result.filePath
                is ReceiptResult.ImageFile -> result.filePath
                is ReceiptResult.Error -> return
            }
            
            val file = File(filePath)
            if (file.exists()) {
                val desktop = Desktop.getDesktop()
                try {
                    desktop.browseFileDirectory(file)
                } catch (e: Exception) {
                    try {
                        desktop.open(file)
                    } catch (e2: Exception) {
                        e2.printStackTrace()
                    }
                }
            }
        }
    }
}

private fun generateHtmlReceipt(
    transaction: Transaction,
    config: ReceiptConfig,
    currencySymbol: String
): String {
    val isPaymentTransaction = AllTransactionTypes.isPayGetCash(transaction.transactionType)
    
    val html = StringBuilder()
    html.appendLine("<!DOCTYPE html>")
    html.appendLine("<html>")
    html.appendLine("<head>")
    html.appendLine("<meta charset='UTF-8'>")
    html.appendLine("<title>Receipt - ${transaction.slug ?: "Transaction"}</title>")
    html.appendLine("<style>")
    html.appendLine("""
        body {
            font-family: Arial, sans-serif;
            max-width: 600px;
            margin: 20px auto;
            padding: 20px;
            background: #f5f5f5;
        }
        .receipt {
            background: white;
            padding: 30px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .header {
            text-align: center;
            border-bottom: 3px solid #B71C1C;
            padding-bottom: 20px;
            margin-bottom: 20px;
        }
        .header h1 {
            color: #B71C1C;
            margin: 0;
            font-size: 24px;
        }
        .business-info {
            text-align: center;
            margin-bottom: 20px;
            color: #1976D2;
        }
        .business-info p {
            margin: 5px 0;
            font-size: 14px;
        }
        .section {
            margin: 20px 0;
        }
        .section-title {
            font-weight: bold;
            margin-bottom: 10px;
            color: #212121;
            font-size: 14px;
        }
        .info-row {
            display: flex;
            justify-content: space-between;
            padding: 8px 0;
            border-bottom: 1px solid #e0e0e0;
        }
        .info-label {
            color: #616161;
            font-size: 12px;
        }
        .info-value {
            color: #212121;
            font-weight: 500;
            font-size: 12px;
        }
        .items-table {
            width: 100%;
            border-collapse: collapse;
            margin: 20px 0;
        }
        .items-table th {
            background: #1976D2;
            color: white;
            padding: 10px;
            text-align: left;
            font-size: 11px;
            font-weight: bold;
        }
        .items-table td {
            padding: 10px;
            border-bottom: 1px solid #e0e0e0;
            font-size: 12px;
        }
        .items-table tr:nth-child(even) {
            background: #fafafa;
        }
        .totals {
            margin-top: 20px;
        }
        .total-row {
            display: flex;
            justify-content: space-between;
            padding: 8px 0;
            font-size: 13px;
        }
        .total-row.final {
            font-weight: bold;
            font-size: 16px;
            border-top: 2px solid #212121;
            padding-top: 12px;
            margin-top: 8px;
        }
        .footer {
            margin-top: 30px;
            padding-top: 20px;
            border-top: 3px solid #B71C1C;
            text-align: center;
            font-size: 12px;
            color: #757575;
        }
    """.trimIndent())
    html.appendLine("</style>")
    html.appendLine("</head>")
    html.appendLine("<body>")
    html.appendLine("<div class='receipt'>")
    
    // Header
    html.appendLine("<div class='header'>")
    html.appendLine("<h1>RECEIPT</h1>")
    html.appendLine("</div>")
    
    // Business Info
    if (config.showBusinessName && config.businessName.isNotEmpty()) {
        html.appendLine("<div class='business-info'>")
        html.appendLine("<p style='font-weight: bold; font-size: 16px;'>${escapeHtml(config.businessName)}</p>")
        if (config.showBusinessAddress && config.businessAddress.isNotEmpty()) {
            html.appendLine("<p>${escapeHtml(config.businessAddress)}</p>")
        }
        if (config.showBusinessPhone && config.businessPhone.isNotEmpty()) {
            html.appendLine("<p>${escapeHtml(config.businessPhone)}</p>")
        }
        if (config.showBusinessEmail && config.businessEmail.isNotEmpty()) {
            html.appendLine("<p>${escapeHtml(config.businessEmail)}</p>")
        }
        html.appendLine("</div>")
    }
    
    // Divider
    html.appendLine("<hr style='border: none; border-top: 1px solid #e0e0e0; margin: 20px 0;'>")
    
    // Transaction Info
    html.appendLine("<div class='section'>")
    if (config.showOrderNo && transaction.slug != null) {
        html.appendLine("<div class='info-row'>")
        html.appendLine("<span class='info-label'>Receipt No:</span>")
        html.appendLine("<span class='info-value'>${escapeHtml(transaction.slug)}</span>")
        html.appendLine("</div>")
    }
    if (config.showTransactionDate && transaction.timestamp != null) {
        html.appendLine("<div class='info-row'>")
        html.appendLine("<span class='info-label'>Date:</span>")
        html.appendLine("<span class='info-value'>${escapeHtml(formatTransactionDate(transaction.timestamp))}</span>")
        html.appendLine("</div>")
    }
    if (config.showTransactionType) {
        html.appendLine("<div class='info-row'>")
        html.appendLine("<span class='info-label'>Transaction Type:</span>")
        html.appendLine("<span class='info-value'>${escapeHtml(transaction.getTransactionTypeName())}</span>")
        html.appendLine("</div>")
    }
    html.appendLine("</div>")
    
    // Customer/Party Info
    transaction.party?.let { party ->
        if (config.showCustomerName || config.showCustomerPhone || config.showCustomerAddress) {
            html.appendLine("<div class='section'>")
            html.appendLine("<div class='section-title'>Customer Details</div>")
            if (config.showCustomerName) {
                html.appendLine("<div class='info-row'>")
                html.appendLine("<span class='info-label'>Name:</span>")
                html.appendLine("<span class='info-value'>${escapeHtml(party.name)}</span>")
                html.appendLine("</div>")
            }
            if (config.showCustomerPhone && party.phone != null) {
                html.appendLine("<div class='info-row'>")
                html.appendLine("<span class='info-label'>Phone:</span>")
                html.appendLine("<span class='info-value'>${escapeHtml(party.phone)}</span>")
                html.appendLine("</div>")
            }
            if (config.showCustomerAddress && party.address != null) {
                html.appendLine("<div class='info-row'>")
                html.appendLine("<span class='info-label'>Address:</span>")
                html.appendLine("<span class='info-value'>${escapeHtml(party.address)}</span>")
                html.appendLine("</div>")
            }
            html.appendLine("</div>")
        }
    }
    
    // Items (only for non-payment transactions)
    if (!isPaymentTransaction && transaction.transactionDetails.isNotEmpty()) {
        html.appendLine("<div class='section'>")
        html.appendLine("<table class='items-table'>")
        html.appendLine("<thead>")
        html.appendLine("<tr>")
        html.appendLine("<th>Item</th>")
        html.appendLine("<th>Qty</th>")
        html.appendLine("<th style='text-align: right;'>Price</th>")
        html.appendLine("<th style='text-align: right;'>Total</th>")
        html.appendLine("</tr>")
        html.appendLine("</thead>")
        html.appendLine("<tbody>")
        
        transaction.transactionDetails.forEach { detail ->
            html.appendLine("<tr>")
            html.appendLine("<td>${escapeHtml(detail.product?.title ?: "Unknown")}</td>")
            html.appendLine("<td>${"%.1f".format(detail.quantity)}</td>")
            html.appendLine("<td style='text-align: right;'>$currencySymbol${"%.2f".format(detail.price)}</td>")
            html.appendLine("<td style='text-align: right;'>$currencySymbol${"%.2f".format(detail.calculateSubtotal())}</td>")
            html.appendLine("</tr>")
        }
        
        html.appendLine("</tbody>")
        html.appendLine("</table>")
        html.appendLine("</div>")
    }
    
    // Totals
    html.appendLine("<div class='section totals'>")
    if (!isPaymentTransaction) {
        val subtotal = transaction.calculateSubtotal()
        html.appendLine("<div class='total-row'>")
        html.appendLine("<span>Subtotal:</span>")
        html.appendLine("<span>$currencySymbol${"%.2f".format(subtotal)}</span>")
        html.appendLine("</div>")
        
        if (config.showDiscount && transaction.flatDiscount > 0) {
            html.appendLine("<div class='total-row'>")
            html.appendLine("<span>Discount:</span>")
            html.appendLine("<span>$currencySymbol${"%.2f".format(transaction.flatDiscount)}</span>")
            html.appendLine("</div>")
        }
        
        if (config.showTax && transaction.flatTax > 0) {
            html.appendLine("<div class='total-row'>")
            html.appendLine("<span>Tax:</span>")
            html.appendLine("<span>$currencySymbol${"%.2f".format(transaction.flatTax)}</span>")
            html.appendLine("</div>")
        }
        
        if (config.showAdditionalCharges && transaction.additionalCharges > 0) {
            html.appendLine("<div class='total-row'>")
            html.appendLine("<span>Additional Charges:</span>")
            html.appendLine("<span>$currencySymbol${"%.2f".format(transaction.additionalCharges)}</span>")
            html.appendLine("</div>")
        }
        
        val grandTotal = transaction.calculateGrandTotal()
        html.appendLine("<div class='total-row final'>")
        html.appendLine("<span>Total:</span>")
        html.appendLine("<span>$currencySymbol${"%.2f".format(grandTotal)}</span>")
        html.appendLine("</div>")
    }
    
    // Paid amount
    html.appendLine("<div class='total-row'>")
    html.appendLine("<span>Paid:</span>")
    html.appendLine("<span style='color: #43A047; font-weight: bold;'>$currencySymbol${"%.2f".format(transaction.totalPaid)}</span>")
    html.appendLine("</div>")
    
    // Balance
    if (!isPaymentTransaction) {
        val balance = transaction.calculatePayable()
        if (balance != 0.0 || config.showPayableAmount) {
            html.appendLine("<div class='total-row'>")
            html.appendLine("<span>Balance:</span>")
            html.appendLine("<span>$currencySymbol${"%.2f".format(balance)}</span>")
            html.appendLine("</div>")
        }
    }
    
    // Previous Balance
    if (!isPaymentTransaction && config.showPreviousBalance && transaction.party != null && transaction.party.balance != 0.0) {
        html.appendLine("<div class='total-row'>")
        html.appendLine("<span>Previous Balance:</span>")
        html.appendLine("<span>$currencySymbol${"%.2f".format(abs(transaction.party.balance))}</span>")
        html.appendLine("</div>")
    }
    
    html.appendLine("</div>")
    
    // Payment Method
    if (config.showPaymentMethod && (transaction.paymentMethodTo != null || transaction.paymentMethodFrom != null)) {
        html.appendLine("<div class='section'>")
        val paymentMethod = transaction.paymentMethodTo?.title ?: transaction.paymentMethodFrom?.title ?: "N/A"
        html.appendLine("<div class='info-row'>")
        html.appendLine("<span class='info-label'>Payment Method:</span>")
        html.appendLine("<span class='info-value'>${escapeHtml(paymentMethod)}</span>")
        html.appendLine("</div>")
        html.appendLine("</div>")
    }
    
    // Description/Remarks
    if (transaction.description?.isNotEmpty() == true) {
        html.appendLine("<div class='section'>")
        html.appendLine("<div class='section-title'>Remarks</div>")
        html.appendLine("<div style='color: #616161; font-size: 12px;'>${escapeHtml(transaction.description)}</div>")
        html.appendLine("</div>")
    }
    
    // Footer
    html.appendLine("<div class='footer'>")
    if (config.showInvoiceTerms && config.invoiceTerms.isNotEmpty()) {
        html.appendLine("<p>${escapeHtml(config.invoiceTerms)}</p>")
    }
    if (config.showRegardsMessage && config.regardsMessage != null) {
        html.appendLine("<p style='color: #1976D2; font-weight: bold; margin-top: 10px;'>${escapeHtml(config.regardsMessage)}</p>")
    }
    html.appendLine("</div>")
    
    html.appendLine("</div>")
    html.appendLine("</body>")
    html.appendLine("</html>")
    
    return html.toString()
}

private fun escapeHtml(text: String): String {
    return text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")
        .replace("\n", "<br>")
}

