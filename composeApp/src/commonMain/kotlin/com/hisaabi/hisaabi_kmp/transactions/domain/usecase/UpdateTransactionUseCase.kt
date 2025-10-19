package com.hisaabi.hisaabi_kmp.transactions.domain.usecase

import com.hisaabi.hisaabi_kmp.transactions.data.repository.TransactionsRepository
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction

class UpdateTransactionUseCase(
    private val repository: TransactionsRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<Unit> {
        // Validate transaction
        if (transaction.slug.isNullOrBlank()) {
            return Result.failure(Exception("Transaction slug is required for update"))
        }
        
        if (transaction.transactionDetails.isEmpty()) {
            return Result.failure(Exception("Transaction must have at least one product"))
        }
        
        // Update transaction
        return repository.updateTransaction(transaction)
    }
}

