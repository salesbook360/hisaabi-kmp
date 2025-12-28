package com.hisaabi.hisaabi_kmp.receipt

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.FileProvider
import com.hisaabi.hisaabi_kmp.settings.domain.model.ReceiptConfig
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AndroidReceiptCapture(private val context: Context) : ReceiptCapture {
    
    override suspend fun captureReceipt(
        transaction: Transaction,
        config: ReceiptConfig,
        currencySymbol: String
    ): ReceiptResult = withContext(Dispatchers.IO) {
        try {
            // Create a receipt directory
            val receiptDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "receipts")
            if (!receiptDir.exists()) {
                receiptDir.mkdirs()
            }
            
            val fileName = "receipt_${transaction.slug ?: System.currentTimeMillis()}"
            
            // For now, we'll create a PDF file
            // In a production app, you'd render the compose view to bitmap
            val pdfFile = File(receiptDir, "$fileName.pdf")
            
            // Create PDF from text
            createPdfFromTransaction(transaction, config, currencySymbol, pdfFile)
            
            ReceiptResult.PdfFile(pdfFile.absolutePath)
        } catch (e: Exception) {
            ReceiptResult.Error("Failed to generate receipt: ${e.message}")
        }
    }
    
    override suspend fun shareReceipt(
        result: ReceiptResult,
        transaction: Transaction
    ) = withContext(Dispatchers.Main) {
        when (result) {
            is ReceiptResult.PdfFile -> {
                val file = File(result.filePath)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Receipt - ${transaction.slug ?: "Transaction"}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                val chooserIntent = Intent.createChooser(shareIntent, "Share Receipt")
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooserIntent)
            }
            is ReceiptResult.ImageFile -> {
                val file = File(result.filePath)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Receipt - ${transaction.slug ?: "Transaction"}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                val chooserIntent = Intent.createChooser(shareIntent, "Share Receipt")
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooserIntent)
            }
            is ReceiptResult.Error -> {
                // Handle error - could show a toast
            }
        }
    }
    
    private fun createPdfFromTransaction(
        transaction: Transaction,
        config: ReceiptConfig,
        currencySymbol: String,
        outputFile: File
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        val margin = 40f
        val pageWidth = 595f
        val contentWidth = pageWidth - (margin * 2)
        
        // Setup paint styles
        val headerPaint = Paint().apply {
            color = Color.parseColor("#B71C1C")
            textSize = 28f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        val businessNamePaint = Paint().apply {
            color = Color.parseColor("#1976D2")
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
        }
        
        val semiBoldPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }
        
        val normalPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            isAntiAlias = true
        }
        
        val smallPaint = Paint().apply {
            color = Color.parseColor("#757575")
            textSize = 9f
            isAntiAlias = true
        }
        
        val labelPaint = Paint().apply {
            color = Color.parseColor("#757575")
            textSize = 9f
            isFakeBoldText = true
            isAntiAlias = true
        }
        
        val tablePaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            isAntiAlias = true
        }
        
        val redPaint = Paint().apply {
            color = Color.parseColor("#B71C1C")
        }
        
        val grayPaint = Paint().apply {
            color = Color.parseColor("#F5F5F5")
        }
        
        val lightGrayPaint = Paint().apply {
            color = Color.parseColor("#FAFAFA")
        }
        
        var yPosition = 40f
        
        // Top red border (6dp thick)
        canvas.drawRect(margin, yPosition, pageWidth - margin, yPosition + 6f, redPaint)
        yPosition += 30f  // More space to avoid overlap
        
        // Header - RECEIPT (centered)
        canvas.drawText("RECEIPT", pageWidth / 2, yPosition, headerPaint)
        yPosition += 35f
        
        // Business info (centered)
        if (config.showBusinessName && config.businessName.isNotEmpty()) {
            canvas.drawText(config.businessName, pageWidth / 2, yPosition, businessNamePaint)
            yPosition += 18f
        }
        
        val centerSmallPaint = Paint(smallPaint).apply {
            textAlign = Paint.Align.CENTER
        }
        
        if (config.showBusinessAddress && config.businessAddress.isNotEmpty()) {
            canvas.drawText(config.businessAddress, pageWidth / 2, yPosition, centerSmallPaint)
            yPosition += 14f
        }
        
        if (config.showBusinessPhone && config.businessPhone.isNotEmpty()) {
            canvas.drawText(config.businessPhone, pageWidth / 2, yPosition, centerSmallPaint)
            yPosition += 14f
        }
        
        if (config.showBusinessEmail && config.businessEmail.isNotEmpty()) {
            canvas.drawText(config.businessEmail, pageWidth / 2, yPosition, centerSmallPaint)
            yPosition += 14f
        }
        
        // Only add divider if business info was shown
        val businessInfoShown = (config.showBusinessName && config.businessName.isNotEmpty()) ||
                               (config.showBusinessAddress && config.businessAddress.isNotEmpty()) ||
                               (config.showBusinessPhone && config.businessPhone.isNotEmpty()) ||
                               (config.showBusinessEmail && config.businessEmail.isNotEmpty())
        
        if (businessInfoShown) {
            yPosition += 10f
            
            // Divider line
            canvas.drawRect(margin, yPosition, pageWidth - margin, yPosition + 1f, Paint().apply {
                color = Color.parseColor("#E0E0E0")
            })
            yPosition += 16f
        }
        
        // Transaction info (Date, Receipt, Type, Payment Method)
        val showDate = config.showTransactionDate && transaction.timestamp != null
        val showReceipt = config.showOrderNo && transaction.slug != null
        val showType = config.showTransactionType
        val showPayment = config.showPaymentMethod && (transaction.paymentMethodTo != null || transaction.paymentMethodFrom != null)
        
        if (showDate || showReceipt || showType || showPayment) {
            val infoBoxTop = yPosition
            val boxHeight = if ((showDate || showReceipt) && (showType || showPayment)) 72f else 36f
            canvas.drawRect(margin, infoBoxTop, pageWidth - margin, yPosition + boxHeight, grayPaint)
            yPosition += 12f
            
            // First row: Date and Receipt No
            if (showDate || showReceipt) {
                if (showDate) {
                    canvas.drawText("PAYMENT DATE", margin + 12f, yPosition, labelPaint)
                }
                
                if (showReceipt) {
                    val rightAlignPaint = Paint(labelPaint).apply {
                        textAlign = Paint.Align.RIGHT
                    }
                    canvas.drawText("RECEIPT NO.", pageWidth - margin - 12f, yPosition, rightAlignPaint)
                }
                yPosition += 16f
                
                if (showDate) {
                    canvas.drawText(transaction.timestamp.toString(), margin + 12f, yPosition, semiBoldPaint)
                }
                
                if (showReceipt) {
                    val rightSemiBold = Paint(semiBoldPaint).apply {
                        textAlign = Paint.Align.RIGHT
                    }
                    canvas.drawText(transaction.slug, pageWidth - margin - 12f, yPosition, rightSemiBold)
                }
                yPosition += 18f
            }
            
            // Second row: Transaction Type and Payment Method
            if (showType || showPayment) {
                if (showType) {
                    canvas.drawText("TRANSACTION TYPE", margin + 12f, yPosition, labelPaint)
                }
                
                if (showPayment) {
                    val rightAlignPaint = Paint(labelPaint).apply {
                        textAlign = Paint.Align.RIGHT
                    }
                    canvas.drawText("PAYMENT METHOD", pageWidth - margin - 12f, yPosition, rightAlignPaint)
                }
                yPosition += 16f
                
                if (showType) {
                    canvas.drawText(transaction.getTransactionTypeName(), margin + 12f, yPosition, semiBoldPaint)
                }
                
                if (showPayment) {
                    val paymentMethod = transaction.paymentMethodTo?.title ?: transaction.paymentMethodFrom?.title ?: "N/A"
                    val rightSemiBold = Paint(semiBoldPaint).apply {
                        textAlign = Paint.Align.RIGHT
                    }
                    canvas.drawText(paymentMethod, pageWidth - margin - 12f, yPosition, rightSemiBold)
                }
                yPosition += 18f
            }
            
            yPosition += 6f
        }
        
        // Bill To / Ship To with light gray backgrounds (only if customer info is shown)
        transaction.party?.let { party ->
            val showCustomerInfo = config.showCustomerName || 
                                  (config.showCustomerPhone && party.phone != null) || 
                                  (config.showCustomerAddress && party.address != null)
            
            if (showCustomerInfo) {
                val boxWidth = (contentWidth - 16f) / 2
                val leftBoxX = margin
                val rightBoxX = margin + boxWidth + 16f
                
                // Draw background boxes
                canvas.drawRect(leftBoxX, yPosition, leftBoxX + boxWidth, yPosition + 60f, lightGrayPaint)
                canvas.drawRect(rightBoxX, yPosition, pageWidth - margin, yPosition + 60f, lightGrayPaint)
                
                var boxYPos = yPosition + 12f
                canvas.drawText("BILL TO", leftBoxX + 12f, boxYPos, labelPaint)
                canvas.drawText("SHIP TO", rightBoxX + 12f, boxYPos, labelPaint)
                boxYPos += 16f
                
                if (config.showCustomerName) {
                    canvas.drawText(party.name, leftBoxX + 12f, boxYPos, semiBoldPaint)
                    canvas.drawText(party.name, rightBoxX + 12f, boxYPos, semiBoldPaint)
                    boxYPos += 14f
                }
                
                if (config.showCustomerPhone && party.phone != null) {
                    canvas.drawText(party.phone, leftBoxX + 12f, boxYPos, normalPaint)
                    canvas.drawText(party.phone, rightBoxX + 12f, boxYPos, normalPaint)
                    boxYPos += 12f
                }
                
                if (config.showCustomerAddress && party.address != null) {
                    canvas.drawText(party.address, leftBoxX + 12f, boxYPos, normalPaint)
                    canvas.drawText(party.address, rightBoxX + 12f, boxYPos, normalPaint)
                }
                
                yPosition += 75f
            }
        }
        
        // Table header with red background
        val tableHeaderY = yPosition
        canvas.drawRect(margin, tableHeaderY, pageWidth - margin, tableHeaderY + 24f, redPaint)
        yPosition += 16f
        
        val whiteTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }
        
        val whiteCenterAlign = Paint(whiteTextPaint).apply {
            textAlign = Paint.Align.CENTER
        }
        
        val whiteRightAlign = Paint(whiteTextPaint).apply {
            textAlign = Paint.Align.RIGHT
        }
        
        canvas.drawText("DESCRIPTION", margin + 12f, yPosition, whiteTextPaint)
        canvas.drawText("QTY", pageWidth / 2 - 40f, yPosition, whiteCenterAlign)
        canvas.drawText("PRICE", pageWidth / 2 + 60f, yPosition, whiteCenterAlign)
        canvas.drawText("TOTAL", pageWidth - margin - 12f, yPosition, whiteRightAlign)
        yPosition += 14f
        
        // Table items with alternating backgrounds
        transaction.transactionDetails.forEachIndexed { index, detail ->
            val rowBg = if (index % 2 == 0) lightGrayPaint else Paint().apply { color = Color.WHITE }
            val rowHeight = if (detail.flatDiscount > 0 || detail.flatTax > 0) 40f else 28f
            
            canvas.drawRect(margin, yPosition, pageWidth - margin, yPosition + rowHeight, rowBg)
            
            var rowYPos = yPosition + 16f
            val productName = detail.product?.title ?: "Unknown"
            canvas.drawText(productName, margin + 12f, rowYPos, tablePaint)
            
            val qtyPaint = Paint(tablePaint).apply {
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(String.format("%.1f", detail.quantity), pageWidth / 2 - 40f, rowYPos, qtyPaint)
            
            val pricePaint = Paint(tablePaint).apply {
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("$currencySymbol${String.format("%.2f", detail.price)}", pageWidth / 2 + 60f, rowYPos, pricePaint)
            
            val totalPaint = Paint(tablePaint).apply {
                textAlign = Paint.Align.RIGHT
                isFakeBoldText = true
            }
            canvas.drawText("$currencySymbol${String.format("%.2f", detail.calculateSubtotal())}", pageWidth - margin - 12f, rowYPos, totalPaint)
            
            // Show discounts/taxes if any
            if (detail.flatDiscount > 0 || detail.flatTax > 0) {
                rowYPos += 12f
                val extraInfoPaint = Paint().apply {
                    textSize = 9f
                    textAlign = Paint.Align.RIGHT
                    isAntiAlias = true
                }
                var extraText = ""
                if (detail.flatDiscount > 0) {
                    extraText += "Discount: -$currencySymbol${String.format("%.2f", detail.flatDiscount)}"
                }
                if (detail.flatTax > 0) {
                    if (extraText.isNotEmpty()) extraText += " | "
                    extraText += "Tax: +$currencySymbol${String.format("%.2f", detail.flatTax)}"
                }
                canvas.drawText(extraText, pageWidth - margin - 12f, rowYPos, extraInfoPaint)
            }
            
            yPosition += rowHeight
        }
        
        yPosition += 20f
        
        // Summary section with gray background
        val summaryTop = yPosition
        val productsDiscount = transaction.transactionDetails.sumOf { it.flatDiscount }
        val productsTax = transaction.transactionDetails.sumOf { it.flatTax }
        val summaryHeight = 140f + (if (productsDiscount > 0) 18f else 0f) + (if (productsTax > 0) 18f else 0f) +
                (if (config.showDiscount && transaction.flatDiscount > 0) 18f else 0f) +
                (if (config.showTax && transaction.flatTax > 0) 18f else 0f) +
                (if (config.showAdditionalCharges && transaction.additionalCharges > 0) 18f else 0f) +
                (if (config.showTotalItems) 18f else 0f) +
                (if (config.showPreviousBalance && transaction.party != null && transaction.party.balance != 0.0) 15f else 0f) +
                (if ((config.showPayableAmount || config.showCurrentBalance) && (transaction.calculatePayable() != 0.0 || config.showPayableAmount)) 18f else 0f)
        
        canvas.drawRect(margin, summaryTop, pageWidth - margin, summaryTop + summaryHeight, grayPaint)
        yPosition += 16f
        
        val rightAlignNormal = Paint(normalPaint).apply {
            textAlign = Paint.Align.RIGHT
        }
        
        val rightAlignSemiBold = Paint(semiBoldPaint).apply {
            textAlign = Paint.Align.RIGHT
        }
        
        // Subtotal
        val subtotal = transaction.calculateSubtotal()
        canvas.drawText("Subtotal", margin + 16f, yPosition, normalPaint)
            canvas.drawText("$currencySymbol${String.format("%.2f", subtotal)}", pageWidth - margin - 16f, yPosition, rightAlignSemiBold)
        yPosition += 18f  // More space between items
        
        if (productsDiscount > 0) {
            canvas.drawText("Products Discount", margin + 16f, yPosition, normalPaint)
            canvas.drawText("-$currencySymbol${String.format("%.2f", productsDiscount)}", pageWidth - margin - 16f, yPosition, rightAlignSemiBold)
            yPosition += 18f  // More space between items
        }
        
        if (productsTax > 0) {
            canvas.drawText("Products Tax", margin + 16f, yPosition, normalPaint)
            canvas.drawText("$currencySymbol${String.format("%.2f", productsTax)}", pageWidth - margin - 16f, yPosition, rightAlignSemiBold)
            yPosition += 18f  // More space between items
        }
        
        if (config.showDiscount && transaction.flatDiscount > 0) {
            canvas.drawText("Transaction Discount", margin + 16f, yPosition, normalPaint)
            canvas.drawText("-$currencySymbol${String.format("%.2f", transaction.flatDiscount)}", pageWidth - margin - 16f, yPosition, rightAlignSemiBold)
            yPosition += 18f  // More space between items
        }
        
        if (config.showTax && transaction.flatTax > 0) {
            canvas.drawText("Transaction Tax", margin + 16f, yPosition, normalPaint)
            canvas.drawText("$currencySymbol${String.format("%.2f", transaction.flatTax)}", pageWidth - margin - 16f, yPosition, rightAlignSemiBold)
            yPosition += 18f  // More space between items
        }
        
        if (config.showAdditionalCharges && transaction.additionalCharges > 0) {
            canvas.drawText("Shipping/Handling", margin + 16f, yPosition, normalPaint)
            canvas.drawText("$currencySymbol${String.format("%.2f", transaction.additionalCharges)}", pageWidth - margin - 16f, yPosition, rightAlignSemiBold)
            yPosition += 18f  // More space between items
        }
        
        yPosition += 12f  // More space before red line
        // Red divider
        canvas.drawRect(margin + 16f, yPosition, pageWidth - margin - 16f, yPosition + 2f, redPaint)
        yPosition += 20f  // More space after red line before Grand Total
        
        // Total Items
        if (config.showTotalItems) {
            canvas.drawText("Total Items", margin + 16f, yPosition, semiBoldPaint)
            val itemsText = "${transaction.transactionDetails.size} items (${String.format("%.1f", transaction.calculateTotalQuantity())} qty)"
            canvas.drawText(itemsText, pageWidth - margin - 16f, yPosition, rightAlignSemiBold)
            yPosition += 18f
        }
        
        // Grand Total
        val grandTotalPaint = Paint(titlePaint).apply {
            textSize = 14f
        }
        val grandTotalAmountPaint = Paint().apply {
            color = Color.parseColor("#B71C1C")
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("GRAND TOTAL", margin + 16f, yPosition, grandTotalPaint)
        canvas.drawText("$currencySymbol${String.format("%.2f", transaction.calculateGrandTotal())}", pageWidth - margin - 16f, yPosition, grandTotalAmountPaint)
        yPosition += 18f
        
        // Previous Balance
        if (config.showPreviousBalance && transaction.party != null && transaction.party.balance != 0.0) {
            canvas.drawText("Previous Balance", margin + 16f, yPosition, semiBoldPaint)
            canvas.drawText("$currencySymbol${String.format("%.2f", kotlin.math.abs(transaction.party.balance))}", pageWidth - margin - 16f, yPosition, rightAlignSemiBold)
            yPosition += 15f
        }
        
        // Paid
        canvas.drawText("Paid", margin + 16f, yPosition, semiBoldPaint)
        canvas.drawText("$currencySymbol${String.format("%.2f", transaction.totalPaid)}", pageWidth - margin - 16f, yPosition, rightAlignSemiBold)
        yPosition += 15f
        
        // Payable Amount or Current Balance
        if (config.showPayableAmount || config.showCurrentBalance) {
            val balance = transaction.calculatePayable()
            if (balance != 0.0 || config.showPayableAmount) {
                val balanceLabel = if (balance > 0) {
                    if (config.showCurrentBalance) "Current Balance" else "Balance Due"
                } else {
                    "Change"
                }
                canvas.drawText(balanceLabel, margin + 16f, yPosition, semiBoldPaint)
                canvas.drawText("$currencySymbol${String.format("%.2f", kotlin.math.abs(balance))}", pageWidth - margin - 16f, yPosition, rightAlignSemiBold)
            }
        }
        
        yPosition = summaryTop + summaryHeight + 20f
        
        // Footer
        if (config.showInvoiceTerms && config.invoiceTerms.isNotEmpty()) {
            val centerPaint = Paint(smallPaint).apply {
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(config.invoiceTerms, pageWidth / 2, yPosition, centerPaint)
            yPosition += 14f
        }
        
        if (config.showRegardsMessage && config.regardsMessage != null) {
            val regardsPaint = Paint().apply {
                color = Color.parseColor("#1976D2")
                textSize = 12f
                isFakeBoldText = true
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(config.regardsMessage, pageWidth / 2, yPosition, regardsPaint)
        }
        
        // Bottom red border
        canvas.drawRect(margin, 820f, pageWidth - margin, 826f, redPaint)
        
        pdfDocument.finishPage(page)
        
        // Write to file
        try {
            pdfDocument.writeTo(FileOutputStream(outputFile))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        
        pdfDocument.close()
    }
}

// Global context holder
object ReceiptContextHolder {
    var context: Context? = null
}

actual fun getReceiptCapture(): ReceiptCapture {
    return ReceiptContextHolder.context?.let { context ->
        AndroidReceiptCapture(context)
    } ?: object : ReceiptCapture {
        override suspend fun captureReceipt(transaction: Transaction, config: ReceiptConfig, currencySymbol: String): ReceiptResult {
            return ReceiptResult.Error("Context not available")
        }
        
        override suspend fun shareReceipt(result: ReceiptResult, transaction: Transaction) {
            // No-op
        }
    }
}

