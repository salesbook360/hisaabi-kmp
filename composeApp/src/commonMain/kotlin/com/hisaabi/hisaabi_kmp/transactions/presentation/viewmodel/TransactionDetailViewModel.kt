package com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.GetTransactionWithDetailsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TransactionDetailState(
    val transaction: Transaction? = null,
    val childTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class TransactionDetailViewModel(
    private val getTransactionWithDetailsUseCase: GetTransactionWithDetailsUseCase,
    private val transactionsRepository: com.hisaabi.hisaabi_kmp.transactions.data.repository.TransactionsRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(TransactionDetailState())
    val state: StateFlow<TransactionDetailState> = _state.asStateFlow()
    
    fun loadTransaction(transactionSlug: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val transaction = getTransactionWithDetailsUseCase(transactionSlug)
                
                // If it's a journal voucher, load child transactions
                val childTransactions = if (transaction?.transactionType == 19) {
                    transactionsRepository.getChildTransactions(transactionSlug)
                } else {
                    emptyList()
                }
                
                _state.value = _state.value.copy(
                    transaction = transaction,
                    childTransactions = childTransactions,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load transaction"
                )
            }
        }
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}

