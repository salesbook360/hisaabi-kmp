package com.hisaabi.hisaabi_kmp.transactions.domain.usecase

import com.hisaabi.hisaabi_kmp.transactions.data.repository.TransactionsRepository
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

class GetTransactionsUseCase(
    private val repository: TransactionsRepository
) {
    operator fun invoke(): Flow<List<Transaction>> {
        return repository.getAllTransactions()
    }
    
    fun byCustomer(customerSlug: String): Flow<List<Transaction>> {
        return repository.getTransactionsByCustomer(customerSlug)
    }
    
    fun byType(transactionType: Int): Flow<List<Transaction>> {
        return repository.getTransactionsByType(transactionType)
    }
    
    suspend fun byId(id: Int): Transaction? {
        return repository.getTransactionById(id)
    }
    
    suspend fun bySlug(slug: String): Transaction? {
        return repository.getTransactionBySlug(slug)
    }
    
    suspend fun withDetails(slug: String): Transaction? {
        return repository.getTransactionWithDetails(slug)
    }
}

