package com.hisaabi.hisaabi_kmp.paymentmethods.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.usecase.PaymentMethodUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AddPaymentMethodViewModel(
    private val useCases: PaymentMethodUseCases
) : ViewModel() {
    
    private val _state = MutableStateFlow(AddPaymentMethodState())
    val state: StateFlow<AddPaymentMethodState> = _state.asStateFlow()
    
    fun onTitleChanged(title: String) {
        _state.update { it.copy(title = title, titleError = null) }
    }
    
    fun onDescriptionChanged(description: String) {
        _state.update { it.copy(description = description) }
    }
    
    fun onOpeningAmountChanged(amount: String) {
        _state.update { it.copy(openingAmount = amount, amountError = null) }
    }
    
    fun setPaymentMethodToEdit(paymentMethod: PaymentMethod) {
        _state.update {
            it.copy(
                paymentMethodToEdit = paymentMethod,
                title = paymentMethod.title,
                description = paymentMethod.description ?: "",
                openingAmount = paymentMethod.openingAmount.toString(),
                isEditMode = true
            )
        }
    }
    
    fun savePaymentMethod() {
        val currentState = _state.value
        
        // Validate
        if (currentState.title.isBlank()) {
            _state.update { it.copy(titleError = "Title is required") }
            return
        }
        
        val amount = currentState.openingAmount.toDoubleOrNull()
        if (amount == null) {
            _state.update { it.copy(amountError = "Invalid amount") }
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            if (currentState.isEditMode && currentState.paymentMethodToEdit != null) {
                // Update existing payment method
                val updatedPaymentMethod = currentState.paymentMethodToEdit.copy(
                    title = currentState.title,
                    description = currentState.description.ifBlank { null }
                )
                
                val result = useCases.updatePaymentMethod(updatedPaymentMethod)
                
                result.fold(
                    onSuccess = {
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                isSaved = true,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to update payment method"
                            )
                        }
                    }
                )
            } else {
                // Add new payment method
                val result = useCases.addPaymentMethod(
                    title = currentState.title,
                    description = currentState.description.ifBlank { null },
                    openingAmount = amount,
                    businessSlug = null, // TODO: Get from business context
                    createdBy = null // TODO: Get from auth context
                )
                
                result.fold(
                    onSuccess = {
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                isSaved = true,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to save payment method"
                            )
                        }
                    }
                )
            }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    fun resetState() {
        _state.value = AddPaymentMethodState()
    }
}

data class AddPaymentMethodState(
    val title: String = "",
    val description: String = "",
    val openingAmount: String = "0",
    val titleError: String? = null,
    val amountError: String? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false,
    val paymentMethodToEdit: PaymentMethod? = null
)

