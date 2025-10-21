package com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionType
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.TransactionUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TransactionsListState(
    val transactions: List<Transaction> = emptyList(),
    val transactionDetailsCounts: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTransactionType: TransactionType? = null,
    val selectedParty: Party? = null,
    val searchQuery: String = "",
    val startDate: String? = null,
    val endDate: String? = null,
    val showFilters: Boolean = false
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
                        // Load detail counts for stock adjustment transactions
                        val stockAdjustmentSlugs = transactions
                            .filter { it.transactionType in listOf(12, 13, 14) }  // Stock adjustment types
                            .mapNotNull { it.slug }
                        
                        val counts = mutableMapOf<String, Int>()
                        stockAdjustmentSlugs.forEach { slug ->
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
        
        // Sort by timestamp (newest first)
        return filtered.sortedByDescending { it.timestamp }
    }
    
    fun setTransactionTypeFilter(type: TransactionType?) {
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
                endDate = null
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

