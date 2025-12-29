package com.hisaabi.hisaabi_kmp.transactions.domain.usecase

import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.transactions.data.repository.TransactionsRepository
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest

/**
 * Data class representing pagination filter parameters.
 */
data class TransactionFilterParams(
    val partySlug: String? = null,
    val transactionTypes: List<Int>? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val searchQuery: String = "",
    val sortByEntryDate: Boolean = false,
    val areaSlug: String? = null,
    val categorySlug: String? = null,
    val idOrSlugFilter: String = ""
)

/**
 * Data class representing a paginated result.
 */
data class PaginatedTransactions(
    val transactions: List<Transaction>,
    val totalCount: Int,
    val hasMore: Boolean
)

class GetTransactionsUseCase(
    private val repository: TransactionsRepository,
    private val sessionManager: AppSessionManager
) {
    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
    
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
    
    /**
     * Get paginated transactions with all filters applied at the database level.
     * This ensures correct pagination even when filters match records beyond the current page.
     */
    suspend fun paginated(
        params: TransactionFilterParams,
        page: Int,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): PaginatedTransactions {
        val businessSlug = sessionManager.getBusinessSlug() 
            ?: return PaginatedTransactions(emptyList(), 0, false)
        
        val offset = page * pageSize
        
        // Get paginated transactions from DB with ALL filters applied at DB level
        val transactions = repository.getTransactionsPaginated(
            businessSlug = businessSlug,
            partySlug = params.partySlug,
            transactionTypes = params.transactionTypes,
            startDate = params.startDate,
            endDate = params.endDate,
            searchQuery = params.searchQuery,
            idOrSlugFilter = params.idOrSlugFilter,
            areaSlug = params.areaSlug,
            categorySlug = params.categorySlug,
            sortByEntryDate = params.sortByEntryDate,
            limit = pageSize,
            offset = offset
        )
        
        // Get total count for pagination info (with same filters)
        val totalCount = repository.getTransactionsCount(
            businessSlug = businessSlug,
            partySlug = params.partySlug,
            transactionTypes = params.transactionTypes,
            startDate = params.startDate,
            endDate = params.endDate,
            searchQuery = params.searchQuery,
            idOrSlugFilter = params.idOrSlugFilter,
            areaSlug = params.areaSlug,
            categorySlug = params.categorySlug
        )
        
        val hasMore = (offset + transactions.size) < totalCount
        
        return PaginatedTransactions(
            transactions = transactions,
            totalCount = totalCount,
            hasMore = hasMore
        )
    }
    
    fun byCustomer(partySlug: String): Flow<List<Transaction>> {
        return repository.getTransactionsByCustomer(partySlug)
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

