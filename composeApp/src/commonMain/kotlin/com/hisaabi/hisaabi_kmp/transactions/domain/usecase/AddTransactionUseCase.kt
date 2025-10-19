package com.hisaabi.hisaabi_kmp.transactions.domain.usecase

import com.hisaabi.hisaabi_kmp.transactions.data.repository.TransactionsRepository
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.model.RecordType
import com.hisaabi.hisaabi_kmp.transactions.domain.model.ExpenseIncomeType

class AddTransactionUseCase(
    private val repository: TransactionsRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<String> {
        // Check if this is a record type (Meeting, Task, Note, Cash Reminder)
        val isRecordType = RecordType.fromValue(transaction.transactionType) != null
        
        // Check if this is a Pay/Get Cash transaction (types 4, 5, 6, 7, 11, 12)
        val isPayGetCashTransaction = transaction.transactionType in listOf(4, 5, 6, 7, 11, 12)
        
        // Check if this is an Expense or Extra Income transaction (types 8, 9)
        val isExpenseIncomeTransaction = ExpenseIncomeType.fromValue(transaction.transactionType) != null
        
        // Check if this is a Payment Transfer transaction (type 10)
        val isPaymentTransferTransaction = transaction.transactionType == 10
        
        // Check if this is a Journal Voucher transaction (type 19)
        val isJournalVoucherTransaction = transaction.transactionType == 19
        
        // Validate transaction - records, pay/get cash, expense/income, payment transfer, and journal voucher don't need products
        val requiresProducts = !isRecordType && !isPayGetCashTransaction && !isExpenseIncomeTransaction && !isPaymentTransferTransaction && !isJournalVoucherTransaction
        if (requiresProducts && transaction.transactionDetails.isEmpty()) {
            return Result.failure(Exception("Transaction must have at least one product"))
        }
        
        // Records may not require a party (e.g., Self Note)
        // Payment transfers and journal vouchers don't need a party either (they use payment methods)
        if (!isRecordType && !isPayGetCashTransaction && !isExpenseIncomeTransaction && !isPaymentTransferTransaction && !isJournalVoucherTransaction && transaction.customerSlug.isNullOrBlank() && transaction.party == null) {
            return Result.failure(Exception("Transaction must have a party (customer/vendor)"))
        }
        
        // Insert transaction
        return repository.insertTransaction(transaction)
    }
}

