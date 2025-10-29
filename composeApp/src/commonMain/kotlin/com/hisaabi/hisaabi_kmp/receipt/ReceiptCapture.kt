package com.hisaabi.hisaabi_kmp.receipt

import com.hisaabi.hisaabi_kmp.settings.domain.model.ReceiptConfig
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction

/**
 * Platform-specific receipt capture interface
 */
interface ReceiptCapture {
    suspend fun captureReceipt(transaction: Transaction, config: ReceiptConfig): ReceiptResult
    suspend fun shareReceipt(result: ReceiptResult, transaction: Transaction)
}

sealed class ReceiptResult {
    data class ImageFile(val filePath: String) : ReceiptResult()
    data class PdfFile(val filePath: String) : ReceiptResult()
    data class Error(val message: String) : ReceiptResult()
}

expect fun getReceiptCapture(): ReceiptCapture

