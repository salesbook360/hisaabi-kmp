package com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.GetTransactionWithDetailsUseCase
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.TransactionUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class PaymentTransferState(
    val paymentMethodFrom: PaymentMethod? = null,
    val paymentMethodTo: PaymentMethod? = null,
    val amount: String = "",
    val description: String = "",
    val dateTime: Long = Clock.System.now().toEpochMilliseconds(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val editingTransactionSlug: String? = null
)

class PaymentTransferViewModel(
    private val transactionUseCases: TransactionUseCases,
    private val getTransactionWithDetailsUseCase: GetTransactionWithDetailsUseCase,
    private val appSessionManager: AppSessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(PaymentTransferState())
    val state: StateFlow<PaymentTransferState> = _state.asStateFlow()

    fun selectPaymentMethodFrom(paymentMethod: PaymentMethod?) {
        _state.update { it.copy(paymentMethodFrom = paymentMethod) }
    }

    fun selectPaymentMethodTo(paymentMethod: PaymentMethod?) {
        _state.update { it.copy(paymentMethodTo = paymentMethod) }
    }

    fun setAmount(amount: String) {
        _state.update { it.copy(amount = amount) }
    }

    fun setDescription(description: String) {
        _state.update { it.copy(description = description) }
    }

    fun setDateTime(timestamp: Long) {
        _state.update { it.copy(dateTime = timestamp) }
    }

    fun loadTransactionForEdit(transactionSlug: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Load full transaction with all details
                val transaction = getTransactionWithDetailsUseCase(transactionSlug)
                    ?: throw IllegalStateException("Transaction not found")
                
                // Parse timestamp
                val timestamp = transaction.timestamp?.toLongOrNull() ?: Clock.System.now().toEpochMilliseconds()
                
                // Set state with transaction data
                _state.update { 
                    it.copy(
                        paymentMethodFrom = transaction.paymentMethodFrom,
                        paymentMethodTo = transaction.paymentMethodTo,
                        amount = transaction.totalPaid.toString(),
                        description = transaction.description ?: "",
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
        if (currentState.paymentMethodFrom == null) {
            _state.update { it.copy(error = "Please select payment method from") }
            return
        }

        if (currentState.paymentMethodTo == null) {
            _state.update { it.copy(error = "Please select payment method to") }
            return
        }

        if (currentState.paymentMethodFrom.slug == currentState.paymentMethodTo.slug) {
            _state.update { it.copy(error = "Sender and receiver can't be the same") }
            return
        }

        val amountValue = currentState.amount.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) {
            _state.update { it.copy(error = "Please enter a valid amount") }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val transaction = Transaction(
                    slug = currentState.editingTransactionSlug, // Include slug if editing
                    transactionType = 10, // TRANSACTION_TYPE_CASH_TRANSFER
                    totalPaid = amountValue,
                    totalBill = 0.0,
                    description = currentState.description.ifBlank { null },
                    timestamp = currentState.dateTime.toString(),
                    paymentMethodFromSlug = currentState.paymentMethodFrom.slug,
                    paymentMethodFrom = currentState.paymentMethodFrom,
                    paymentMethodToSlug = currentState.paymentMethodTo.slug,
                    paymentMethodTo = currentState.paymentMethodTo,
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
                                    success = true,
                                    // Reset form
                                    paymentMethodFrom = null,
                                    paymentMethodTo = null,
                                    amount = "",
                                    description = "",
                                    dateTime = Clock.System.now().toEpochMilliseconds(),
                                    editingTransactionSlug = null
                                )
                            }
                        }
                        .onFailure { exception ->
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    error = exception.message ?: "Failed to update transaction"
                                )
                            }
                        }
                } else {
                    // Create new transaction
                    transactionUseCases.addTransaction(transaction)
                        .onSuccess {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    success = true,
                                    // Reset form
                                    paymentMethodFrom = null,
                                    paymentMethodTo = null,
                                    amount = "",
                                    description = "",
                                    dateTime = Clock.System.now().toEpochMilliseconds()
                                )
                            }
                        }
                        .onFailure { exception ->
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    error = exception.message ?: "Failed to save transaction"
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
        _state.update { it.copy(success = false) }
    }

    fun resetForm() {
        _state.update {
            PaymentTransferState(
                dateTime = Clock.System.now().toEpochMilliseconds()
            )
        }
    }
}

