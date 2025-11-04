package com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionSortOption
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.TransactionUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TransactionsListState(
    val transactions: List<Transaction> = emptyList(),
    val transactionDetailsCounts: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTransactionType: AllTransactionTypes? = null,
    val selectedParty: Party? = null,
    val searchQuery: String = "",
    val startDate: String? = null,
    val endDate: String? = null,
    val showFilters: Boolean = false,
    val sortBy: TransactionSortOption = TransactionSortOption.TRANSACTION_DATE
)

class TransactionsListViewModel(
    private val transactionUseCases: TransactionUseCases
) : ViewModel() {
    
    private val _state = MutableStateFlow(TransactionsListState())
    val state: StateFlow<TransactionsListState> = _state.asStateFlow()
    
    init {
        loadTransactions()
    }
    
    fun loadTransactions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                transactionUseCases.getTransactions()
                    .catch { e ->
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to load transactions"
                            )
                        }
                    }
                    .collect { transactions ->
                        // Load detail counts for all transaction types that have products/items
                        // Includes: Sale, Purchase, Returns, Orders, Quotations, Manufacture, Stock Adjustments
                        val transactionTypesWithItems = listOf(
                            AllTransactionTypes.SALE.value,                    // 0
                            AllTransactionTypes.CUSTOMER_RETURN.value,         // 1
                            AllTransactionTypes.PURCHASE.value,                // 2
                            AllTransactionTypes.VENDOR_RETURN.value,           // 3
                            AllTransactionTypes.STOCK_TRANSFER.value,          // 13
                            AllTransactionTypes.STOCK_INCREASE.value,          // 14
                            AllTransactionTypes.STOCK_REDUCE.value,            // 15
                            AllTransactionTypes.PURCHASE_ORDER.value,          // 16
                            AllTransactionTypes.SALE_ORDER.value,              // 17
                            AllTransactionTypes.QUOTATION.value,               // 18
                            AllTransactionTypes.MANUFACTURE.value              // 20
                        )
                        
                        val transactionSlugs = transactions
                            .filter { it.transactionType in transactionTypesWithItems }
                            .mapNotNull { it.slug }
                        
                        val counts = mutableMapOf<String, Int>()
                        transactionSlugs.forEach { slug ->
                            try {
                                counts[slug] = transactionUseCases.getTransactionDetailsCount(slug)
                            } catch (e: Exception) {
                                counts[slug] = 0
                            }
                        }
                        
                        _state.update { 
                            it.copy(
                                transactions = applyFilters(transactions),
                                transactionDetailsCounts = counts,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load transactions"
                    )
                }
            }
        }
    }
    
    private fun applyFilters(transactions: List<Transaction>): List<Transaction> {
        val state = _state.value
        var filtered = transactions
        
        // Filter by transaction type
        state.selectedTransactionType?.let { type ->
            filtered = filtered.filter { it.transactionType == type.value }
        }
        
        // Filter by party
        state.selectedParty?.let { party ->
            filtered = filtered.filter { it.customerSlug == party.slug }
        }
        
        // Filter by search query
        if (state.searchQuery.isNotBlank()) {
            filtered = filtered.filter { transaction ->
                transaction.party?.name?.contains(state.searchQuery, ignoreCase = true) == true ||
                transaction.description?.contains(state.searchQuery, ignoreCase = true) == true ||
                transaction.slug?.contains(state.searchQuery, ignoreCase = true) == true
            }
        }
        
        // Sort by selected option
        return when (state.sortBy) {
            TransactionSortOption.ENTRY_DATE -> {
                filtered.sortedByDescending { it.createdAt }
            }
            TransactionSortOption.TRANSACTION_DATE -> {
                filtered.sortedByDescending { it.timestamp }
            }
        }
    }
    
    fun setTransactionTypeFilter(type: AllTransactionTypes?) {
        _state.update { it.copy(selectedTransactionType = type) }
        refreshFilters()
    }
    
    fun setPartyFilter(party: Party?) {
        _state.update { it.copy(selectedParty = party) }
        refreshFilters()
    }
    
    fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        refreshFilters()
    }
    
    fun setSortBy(sortBy: TransactionSortOption) {
        _state.update { it.copy(sortBy = sortBy) }
        refreshFilters()
    }
    
    fun setDateRange(startDate: String?, endDate: String?) {
        _state.update { it.copy(startDate = startDate, endDate = endDate) }
        refreshFilters()
    }
    
    fun toggleFilters() {
        _state.update { it.copy(showFilters = !it.showFilters) }
    }
    
    fun clearFilters() {
        _state.update { 
            it.copy(
                selectedTransactionType = null,
                selectedParty = null,
                searchQuery = "",
                startDate = null,
                endDate = null,
                sortBy = TransactionSortOption.TRANSACTION_DATE
            )
        }
        refreshFilters()
    }
    
    private fun refreshFilters() {
        val currentTransactions = _state.value.transactions
        _state.update { it.copy(transactions = applyFilters(currentTransactions)) }
    }
    
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                transactionUseCases.deleteTransaction(transaction)
                    .onSuccess {
                        // Transaction deleted successfully
                        // The list will automatically update via Flow
                    }
                    .onFailure { e ->
                        _state.update { 
                            it.copy(error = e.message ?: "Failed to delete transaction")
                        }
                    }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(error = e.message ?: "Failed to delete transaction")
                }
            }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

