package com.hisaabi.hisaabi_kmp.transactions.domain.usecase

import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.transactions.data.repository.TransactionsRepository
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest

class GetTransactionsUseCase(
    private val repository: TransactionsRepository,
    private val sessionManager: AppSessionManager
) {
    /**
     * Returns transactions filtered by the currently selected business.
     * Uses flatMapLatest to automatically re-fetch when business selection changes.
     */
    operator fun invoke(): Flow<List<Transaction>> {
        return sessionManager.observeBusinessSlug().flatMapLatest { businessSlug ->
            if (businessSlug != null) {
                repository.getTransactionsByBusiness(businessSlug)
            } else {
                emptyFlow()
            }
        }
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

