package com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import com.hisaabi.hisaabi_kmp.transactions.domain.model.ExpenseIncomeType
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.TransactionUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class AddExpenseIncomeState(
    val transactionType: ExpenseIncomeType = ExpenseIncomeType.EXPENSE,
    val selectedParty: Party? = null, // This will be expense/income type (stored as Party with roleId 14 or 15)
    val amount: String = "",
    val description: String = "",
    val selectedPaymentMethod: PaymentMethod? = null,
    val dateTime: Long = Clock.System.now().toEpochMilliseconds(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class AddExpenseIncomeViewModel(
    private val transactionUseCases: TransactionUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(AddExpenseIncomeState())
    val state: StateFlow<AddExpenseIncomeState> = _state.asStateFlow()

    fun setTransactionType(type: ExpenseIncomeType) {
        _state.update { it.copy(transactionType = type, selectedParty = null) }
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

    fun setDateTime(timestamp: Long) {
        _state.update { it.copy(dateTime = timestamp) }
    }

    fun saveTransaction() {
        val currentState = _state.value

        // Validation
        if (currentState.selectedParty == null) {
            _state.update { 
                it.copy(
                    error = if (currentState.transactionType == ExpenseIncomeType.EXPENSE) 
                        "Please select expense type" 
                    else 
                        "Please select income type"
                ) 
            }
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

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val transaction = Transaction(
                    customerSlug = currentState.selectedParty.slug,
                    party = currentState.selectedParty,
                    transactionType = currentState.transactionType.value,
                    totalPaid = amountValue,
                    totalBill = 0.0,
                    description = currentState.description.ifBlank { null },
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
                                success = true,
                                // Reset form
                                selectedParty = null,
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
            AddExpenseIncomeState(
                transactionType = it.transactionType,
                dateTime = Clock.System.now().toEpochMilliseconds()
            ) 
        }
    }
}

