package com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.GetTransactionWithDetailsUseCase
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.TransactionUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

enum class PayGetCashType(val displayName: String) {
    PAY_CASH("Pay Cash"),
    GET_CASH("Get Cash")
}

data class PayGetCashState(
    val payGetCashType: PayGetCashType = PayGetCashType.GET_CASH,
    val partyType: PartyType = PartyType.CUSTOMER,
    val selectedParty: Party? = null,
    val amount: String = "",
    val description: String = "",
    val selectedPaymentMethod: PaymentMethod? = null,
    val dateTime: Long = Clock.System.now().toEpochMilliseconds(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val savedTransactionSlug: String? = null,
    val editingTransactionSlug: String? = null
)

class PayGetCashViewModel(
    private val transactionUseCases: TransactionUseCases,
    private val getTransactionWithDetailsUseCase: GetTransactionWithDetailsUseCase,
    private val appSessionManager: AppSessionManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(PayGetCashState())
    val state: StateFlow<PayGetCashState> = _state.asStateFlow()
    
    fun setPayGetCashType(type: PayGetCashType) {
        _state.update { it.copy(payGetCashType = type) }
    }
    
    fun setPartyType(type: PartyType) {
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
    
    fun loadTransactionForEdit(transactionSlug: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Load full transaction with all details
                val transaction = getTransactionWithDetailsUseCase(transactionSlug)
                    ?: throw IllegalStateException("Transaction not found")
                
                // Determine PayGetCashType and PartyType from transaction type
                val (payGetCashType, partyType) = when (transaction.transactionType) {
                    AllTransactionTypes.PAY_TO_CUSTOMER.value -> 
                        PayGetCashType.PAY_CASH to PartyType.CUSTOMER
                    AllTransactionTypes.GET_FROM_CUSTOMER.value -> 
                        PayGetCashType.GET_CASH to PartyType.CUSTOMER
                    AllTransactionTypes.PAY_TO_VENDOR.value -> 
                        PayGetCashType.PAY_CASH to PartyType.VENDOR
                    AllTransactionTypes.GET_FROM_VENDOR.value -> 
                        PayGetCashType.GET_CASH to PartyType.VENDOR
                    AllTransactionTypes.INVESTMENT_WITHDRAW.value -> 
                        PayGetCashType.PAY_CASH to PartyType.INVESTOR
                    AllTransactionTypes.INVESTMENT_DEPOSIT.value -> 
                        PayGetCashType.GET_CASH to PartyType.INVESTOR
                    else -> throw IllegalStateException("Invalid transaction type for Pay/Get Cash")
                }
                
                // Parse timestamp
                val timestamp = transaction.timestamp?.toLongOrNull() ?: Clock.System.now().toEpochMilliseconds()
                
                // Set state with transaction data
                _state.update { 
                    it.copy(
                        payGetCashType = payGetCashType,
                        partyType = partyType,
                        selectedParty = transaction.party,
                        amount = transaction.totalPaid.toString(),
                        description = transaction.description ?: "",
                        selectedPaymentMethod = transaction.paymentMethodTo,
                        dateTime = timestamp,
                        editingTransactionSlug = transaction.slug,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load transaction for editing"
                    )
                }
            }
        }
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
                val transactionType = when (currentState.partyType) {
                    PartyType.CUSTOMER -> {
                        if (currentState.payGetCashType == PayGetCashType.PAY_CASH) 
                            AllTransactionTypes.PAY_TO_CUSTOMER.value
                        else 
                            AllTransactionTypes.GET_FROM_CUSTOMER.value
                    }
                    PartyType.VENDOR -> {
                        if (currentState.payGetCashType == PayGetCashType.PAY_CASH) 
                            AllTransactionTypes.PAY_TO_VENDOR.value
                        else 
                            AllTransactionTypes.GET_FROM_VENDOR.value
                    }
                    PartyType.INVESTOR -> {
                        if (currentState.payGetCashType == PayGetCashType.PAY_CASH) 
                            AllTransactionTypes.INVESTMENT_WITHDRAW.value
                        else 
                            AllTransactionTypes.INVESTMENT_DEPOSIT.value
                    }

                    else -> {
                        AllTransactionTypes.PAY_TO_CUSTOMER.value
                    }
                }
                
                val transaction = Transaction(
                    slug = currentState.editingTransactionSlug, // Include slug if editing
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
                    additionalCharges = 0.0,
                    businessSlug = appSessionManager.getBusinessSlug(),
                    createdBy = appSessionManager.getUserSlug()
                )
                
                // Check if we're editing or creating
                if (currentState.editingTransactionSlug != null) {
                    // Update existing transaction
                    transactionUseCases.updateTransaction(transaction)
                        .onSuccess {
                            _state.update { 
                                it.copy(
                                    isLoading = false,
                                    successMessage = "Transaction updated successfully",
                                    savedTransactionSlug = currentState.editingTransactionSlug
                                )
                            }
                        }
                        .onFailure { e ->
                            _state.update { 
                                it.copy(
                                    isLoading = false,
                                    error = e.message ?: "Failed to update transaction"
                                )
                            }
                        }
                } else {
                    // Create new transaction
                    transactionUseCases.addTransaction(transaction)
                        .onSuccess { transactionSlug ->
                            _state.update { 
                                it.copy(
                                    isLoading = false,
                                    successMessage = "Transaction saved successfully",
                                    savedTransactionSlug = transactionSlug
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

