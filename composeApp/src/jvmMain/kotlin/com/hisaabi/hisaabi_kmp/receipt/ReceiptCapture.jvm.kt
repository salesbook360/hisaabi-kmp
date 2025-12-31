package com.hisaabi.hisaabi_kmp.receipt

import com.hisaabi.hisaabi_kmp.settings.domain.model.ReceiptConfig
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File
import java.io.FileWriter

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
                
                // Generate HTML receipt using common generator
                val htmlContent = ReceiptHtmlGenerator.generateHtmlReceipt(transaction, config, currencySymbol)
                
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

