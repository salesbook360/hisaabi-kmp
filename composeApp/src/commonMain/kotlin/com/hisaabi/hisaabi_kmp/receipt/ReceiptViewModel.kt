package com.hisaabi.hisaabi_kmp.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.settings.domain.model.ReceiptConfig
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.GetTransactionWithDetailsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReceiptState(
    val isGenerating: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val receiptResult: ReceiptResult? = null,
    val receiptConfig: ReceiptConfig = ReceiptConfig.DEFAULT,
    val showPreview: Boolean = false,
    val currentTransaction: Transaction? = null
)

class ReceiptViewModel(
    private val preferencesManager: PreferencesManager,
    private val getTransactionWithDetailsUseCase: GetTransactionWithDetailsUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(ReceiptState())
    val state: StateFlow<ReceiptState> = _state.asStateFlow()
    
    private val receiptCapture = getReceiptCapture()
    
    init {
        // Load receipt config
        viewModelScope.launch {
            preferencesManager.receiptConfig.collect { config ->
                _state.update { it.copy(receiptConfig = config) }
            }
        }
    }
    
    fun showPreview(transaction: Transaction) {
        viewModelScope.launch {
            _state.update { it.copy(isGenerating = true, error = null) }
            
            try {
                // Load transaction with full details (products, party info, etc.)
                val fullTransaction = transaction.slug?.let { slug ->
                    getTransactionWithDetailsUseCase(slug)
                } ?: transaction
                
                _state.update {
                    it.copy(
                        showPreview = true,
                        currentTransaction = fullTransaction,
                        isGenerating = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isGenerating = false,
                        error = "Failed to load transaction details: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun hidePreview() {
        _state.update {
            it.copy(
                showPreview = false,
                currentTransaction = null,
                receiptResult = null
            )
        }
    }
    
    fun generateAndShareReceipt(transaction: Transaction) {
        viewModelScope.launch {
            _state.update { it.copy(isGenerating = true, error = null) }
            
            try {
                val config = preferencesManager.receiptConfig.first()
                val currencySymbol = preferencesManager.getSelectedCurrency().symbol
                val result = receiptCapture.captureReceipt(transaction, config, currencySymbol)
                
                when (result) {
                    is ReceiptResult.Error -> {
                        _state.update {
                            it.copy(
                                isGenerating = false,
                                error = result.message
                            )
                        }
                    }
                    else -> {
                        receiptCapture.shareReceipt(result, transaction)
                        _state.update {
                            it.copy(
                                isGenerating = false,
                                receiptResult = result,
                                successMessage = "Receipt shared successfully!",
                                showPreview = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isGenerating = false,
                        error = "Failed to generate receipt: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun generateReceipt(transaction: Transaction) {
        viewModelScope.launch {
            _state.update { it.copy(isGenerating = true, error = null) }
            
            try {
                val config = preferencesManager.receiptConfig.first()
                val currencySymbol = preferencesManager.getSelectedCurrency().symbol
                val result = receiptCapture.captureReceipt(transaction, config, currencySymbol)
                
                when (result) {
                    is ReceiptResult.Error -> {
                        _state.update {
                            it.copy(
                                isGenerating = false,
                                error = result.message
                            )
                        }
                    }
                    else -> {
                        _state.update {
                            it.copy(
                                isGenerating = false,
                                receiptResult = result,
                                successMessage = "Receipt generated successfully!"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isGenerating = false,
                        error = "Failed to generate receipt: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun shareReceipt(transaction: Transaction) {
        viewModelScope.launch {
            _state.value.receiptResult?.let { result ->
                try {
                    receiptCapture.shareReceipt(result, transaction)
                    _state.update {
                        it.copy(successMessage = "Receipt shared successfully!")
                    }
                } catch (e: Exception) {
                    _state.update {
                        it.copy(error = "Failed to share receipt: ${e.message}")
                    }
                }
            }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    fun clearSuccess() {
        _state.update { it.copy(successMessage = null) }
    }
}

