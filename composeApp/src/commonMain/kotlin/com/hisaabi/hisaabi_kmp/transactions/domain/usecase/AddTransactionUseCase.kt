package com.hisaabi.hisaabi_kmp.transactions.domain.usecase

import com.hisaabi.hisaabi_kmp.transactions.data.repository.TransactionsRepository
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.model.RecordType

class AddTransactionUseCase(
    private val repository: TransactionsRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<String> {
        // Check if this is a record type (Meeting, Task, Note, Cash Reminder)
        val isRecordType = RecordType.fromValue(transaction.transactionType) != null
        
        // Check if this is a Pay/Get Cash transaction (types 4, 5, 6, 7, 11, 12)
        val isPayGetCashTransaction = transaction.transactionType in listOf(4, 5, 6, 7, 11, 12)
        
        // Validate transaction - records and pay/get cash don't need products
        val requiresProducts = !isRecordType && !isPayGetCashTransaction
        if (requiresProducts && transaction.transactionDetails.isEmpty()) {
            return Result.failure(Exception("Transaction must have at least one product"))
        }
        
        // Records may not require a party (e.g., Self Note)
        if (!isRecordType && !isPayGetCashTransaction && transaction.customerSlug.isNullOrBlank() && transaction.party == null) {
            return Result.failure(Exception("Transaction must have a party (customer/vendor)"))
        }
        
        // Insert transaction
        return repository.insertTransaction(transaction)
    }
}

