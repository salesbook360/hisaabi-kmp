package com.hisaabi.hisaabi_kmp.receipt

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.hisaabi.hisaabi_kmp.settings.domain.model.ReceiptConfig
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

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
            
            val fileName = "receipt_${transaction.slug ?: System.currentTimeMillis()}.html"
            val htmlFile = File(receiptDir, fileName)
            
            // Generate HTML receipt using common generator
            val htmlContent = ReceiptHtmlGenerator.generateHtmlReceipt(transaction, config, currencySymbol)
            
            // Write HTML to file
            FileWriter(htmlFile).use { writer ->
                writer.write(htmlContent)
            }
            
            ReceiptResult.PdfFile(htmlFile.absolutePath)
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
                if (file.exists()) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/html"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "Receipt - ${transaction.slug ?: "Transaction"}")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    
                    val chooserIntent = Intent.createChooser(shareIntent, "Share Receipt")
                    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooserIntent)
                }
            }
            is ReceiptResult.ImageFile -> {
                val file = File(result.filePath)
                if (file.exists()) {
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
            }
            is ReceiptResult.Error -> {
                // Handle error - could show a toast
            }
        }
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
