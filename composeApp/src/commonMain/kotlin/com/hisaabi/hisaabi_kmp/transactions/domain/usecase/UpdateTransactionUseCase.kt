package com.hisaabi.hisaabi_kmp.transactions.domain.usecase

import com.hisaabi.hisaabi_kmp.transactions.data.repository.TransactionsRepository
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes

class UpdateTransactionUseCase(
    private val repository: TransactionsRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<Unit> {
        // Validate transaction
        if (transaction.slug.isNullOrBlank()) {
            return Result.failure(Exception("Transaction slug is required for update"))
        }
        
        // Check if this is a record type (Meeting, Task, Note, Cash Reminder)
        val isRecordType = AllTransactionTypes.isRecord(transaction.transactionType)
        
        // Check if this is a Pay/Get Cash transaction
        val isPayGetCashTransaction = AllTransactionTypes.isPayGetCash(transaction.transactionType)
        
        // Check if this is an Expense or Extra Income transaction
        val isExpenseIncomeTransaction = AllTransactionTypes.isExpenseIncome(transaction.transactionType)
        
        // Check if this is a Payment Transfer transaction
        val isPaymentTransferTransaction = AllTransactionTypes.isPaymentTransfer(transaction.transactionType)
        
        // Check if this is a Journal Voucher transaction
        val isJournalVoucherTransaction = AllTransactionTypes.isJournalVoucher(transaction.transactionType)
        
        // Check if this is a Stock Adjustment transaction
        val isStockAdjustmentTransaction = AllTransactionTypes.isStockAdjustment(transaction.transactionType)
        
        // Validate transaction - records, pay/get cash, expense/income, payment transfer, and journal voucher don't need products
        // Stock adjustments DO need products
        val requiresProducts = !isRecordType && !isPayGetCashTransaction && !isExpenseIncomeTransaction && !isPaymentTransferTransaction && !isJournalVoucherTransaction
        if (requiresProducts && transaction.transactionDetails.isEmpty()) {
            return Result.failure(Exception("Transaction must have at least one product"))
        }
        
        // Update transaction
        return repository.updateTransaction(transaction)
    }
}

