package com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import com.hisaabi.hisaabi_kmp.transactions.domain.model.PayGetCashType
import com.hisaabi.hisaabi_kmp.transactions.domain.model.PartyTypeForCash
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.TransactionUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class PayGetCashState(
    val payGetCashType: PayGetCashType = PayGetCashType.GET_CASH,
    val partyType: PartyTypeForCash = PartyTypeForCash.CUSTOMER,
    val selectedParty: Party? = null,
    val amount: String = "",
    val description: String = "",
    val selectedPaymentMethod: PaymentMethod? = null,
    val dateTime: Long = Clock.System.now().toEpochMilliseconds(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class PayGetCashViewModel(
    private val transactionUseCases: TransactionUseCases
) : ViewModel() {
    
    private val _state = MutableStateFlow(PayGetCashState())
    val state: StateFlow<PayGetCashState> = _state.asStateFlow()
    
    fun setPayGetCashType(type: PayGetCashType) {
        _state.update { it.copy(payGetCashType = type) }
    }
    
    fun setPartyType(type: PartyTypeForCash) {
        _state.update { it.copy(partyType = type, selectedParty = null) }
    }
    
    fun selectParty(party: Party?) {
        _state.update { it.copy(selectedParty = party) }
    }
    
    fun setAmount(amount: String) {
        _state.update { it.copy(amount = amount) }
    }
    
    fun setDescription(description: String) {
        _state.update { it.copy(description = description) }
    }
    
    fun selectPaymentMethod(paymentMethod: PaymentMethod?) {
        _state.update { it.copy(selectedPaymentMethod = paymentMethod) }
    }
    
    fun setDateTime(dateTime: Long) {
        _state.update { it.copy(dateTime = dateTime) }
    }
    
    fun saveTransaction() {
        val currentState = _state.value
        
        // Validation
        if (currentState.selectedParty == null) {
            _state.update { it.copy(error = "Please select a party") }
            return
        }
        
        val amountValue = currentState.amount.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) {
            _state.update { it.copy(error = "Please enter a valid amount") }
            return
        }
        
        if (currentState.selectedPaymentMethod == null) {
            _state.update { it.copy(error = "Please select a payment method") }
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Determine transaction type based on party type and pay/get selection
                val transactionType = currentState.partyType.getTransactionType(currentState.payGetCashType)
                
                val transaction = Transaction(
                    customerSlug = currentState.selectedParty.slug,
                    party = currentState.selectedParty,
                    transactionType = transactionType,
                    totalPaid = amountValue,
                    totalBill = 0.0,
                    description = currentState.description,
                    timestamp = currentState.dateTime.toString(),
                    paymentMethodToSlug = currentState.selectedPaymentMethod.slug,
                    paymentMethodTo = currentState.selectedPaymentMethod,
                    flatDiscount = 0.0,
                    flatTax = 0.0,
                    additionalCharges = 0.0
                )
                
                transactionUseCases.addTransaction(transaction)
                    .onSuccess {
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                successMessage = "Transaction saved successfully"
                            )
                        }
                    }
                    .onFailure { e ->
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to save transaction"
                            )
                        }
                    }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An error occurred"
                    )
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

