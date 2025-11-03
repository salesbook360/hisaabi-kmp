package com.hisaabi.hisaabi_kmp.paymentmethods.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.usecase.PaymentMethodUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PaymentMethodsViewModel(
    private val useCases: PaymentMethodUseCases,
    private val sessionManager: AppSessionManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(PaymentMethodsState())
    val state: StateFlow<PaymentMethodsState> = _state.asStateFlow()
    
    private var businessSlug: String? = null
    
    init {
        viewModelScope.launch {
            sessionManager.observeBusinessSlug().collect { newBusinessSlug ->
                businessSlug = newBusinessSlug
                if (newBusinessSlug != null) {
                    loadPaymentMethods()
                }
            }
        }
    }
    
    private var loadJob: kotlinx.coroutines.Job? = null
    
    fun loadPaymentMethods() {
        // Cancel previous load if any
        loadJob?.cancel()
        
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val slug = businessSlug
            if (slug == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        paymentMethods = emptyList(),
                        error = "No business selected"
                    )
                }
                return@launch
            }
            
            useCases.getPaymentMethods(slug)
                .catch { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            error = error.message ?: "Failed to load payment methods"
                        )
                    }
                }
                .collect { paymentMethods ->
                    _state.update { 
                        it.copy(
                            paymentMethods = paymentMethods,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }
    
    fun deletePaymentMethod(paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val result = useCases.deletePaymentMethod(paymentMethod)
            
            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, error = null) }
                    // Payment methods will be automatically updated via Flow
                },
                onFailure = { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to delete payment method"
                        )
                    }
                }
            )
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class PaymentMethodsState(
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

