package com.hisaabi.hisaabi_kmp.receipt

import com.hisaabi.hisaabi_kmp.settings.domain.model.ReceiptConfig
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction

actual fun getReceiptCapture(): ReceiptCapture {
    return object : ReceiptCapture {
        override suspend fun captureReceipt(transaction: Transaction, config: ReceiptConfig): ReceiptResult {
            return ReceiptResult.Error("WASM receipt capture not yet implemented")
        }
        
        override suspend fun shareReceipt(result: ReceiptResult, transaction: Transaction) {
            // WASM implementation pending
        }
    }
}

