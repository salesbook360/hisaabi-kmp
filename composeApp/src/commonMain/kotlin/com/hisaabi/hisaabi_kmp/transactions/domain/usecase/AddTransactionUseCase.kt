package com.hisaabi.hisaabi_kmp.transactions.domain.usecase

import com.hisaabi.hisaabi_kmp.transactions.data.repository.TransactionsRepository
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction

class AddTransactionUseCase(
    private val repository: TransactionsRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<String> {
        // Validate transaction
        if (transaction.transactionDetails.isEmpty()) {
            return Result.failure(Exception("Transaction must have at least one product"))
        }
        
        if (transaction.customerSlug.isNullOrBlank() && transaction.party == null) {
            return Result.failure(Exception("Transaction must have a party (customer/vendor)"))
        }
        
        // Insert transaction
        return repository.insertTransaction(transaction)
    }
}

