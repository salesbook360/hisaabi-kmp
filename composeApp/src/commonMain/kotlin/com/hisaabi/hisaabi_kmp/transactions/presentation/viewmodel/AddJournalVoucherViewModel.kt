package com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import com.hisaabi.hisaabi_kmp.transactions.domain.model.JournalAccount
import com.hisaabi.hisaabi_kmp.transactions.domain.model.JournalAccountType
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.TransactionUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class AddJournalVoucherState(
    val accounts: List<JournalAccount> = emptyList(),
    val selectedPaymentMethod: PaymentMethod? = null,
    val dateTime: Long = Clock.System.now().toEpochMilliseconds(),
    val description: String = "",
    val totalDebit: Double = 0.0,
    val totalCredit: Double = 0.0,
    val isBalanced: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class AddJournalVoucherViewModel(
    private val transactionUseCases: TransactionUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(AddJournalVoucherState())
    val state: StateFlow<AddJournalVoucherState> = _state.asStateFlow()

    /**
     * Add a party (customer, vendor, investor, expense, or income type) to the journal
     */
    fun addParty(party: Party) {
        // Check if already exists
        if (_state.value.accounts.any { it.party?.slug == party.slug }) {
            return
        }

        val accountType = JournalAccountType.fromPartyRoleId(party.roleId) ?: return
        val account = JournalAccount(
            title = party.name,
            amount = 0.0,
            isDebit = true,
            accountType = accountType,
            party = party
        )

        _state.update { 
            val newAccounts = it.accounts + account
            val updatedState = it.copy(accounts = newAccounts)
            calculateTotals(updatedState)
        }
    }

    /**
     * Add a payment method to the journal
     */
    fun addPaymentMethod(paymentMethod: PaymentMethod) {
        val account = JournalAccount(
            title = paymentMethod.title,
            amount = 0.0,
            isDebit = true,
            accountType = JournalAccountType.PAYMENT_METHOD,
            paymentMethod = paymentMethod
        )

        _state.update { 
            val newAccounts = it.accounts + account
            val updatedState = it.copy(accounts = newAccounts)
            calculateTotals(updatedState)
        }
    }

    /**
     * Remove an account from the journal
     */
    fun removeAccount(index: Int) {
        _state.update { 
            val newAccounts = it.accounts.toMutableList().apply { removeAt(index) }
            val updatedState = it.copy(accounts = newAccounts)
            calculateTotals(updatedState)
        }
    }

    /**
     * Update the amount for an account
     */
    fun updateAccountAmount(index: Int, amount: Double) {
        _state.update { 
            val newAccounts = it.accounts.toMutableList().apply {
                this[index] = this[index].copy(amount = amount)
            }
            val updatedState = it.copy(accounts = newAccounts)
            calculateTotals(updatedState)
        }
    }

    /**
     * Toggle debit/credit for an account
     */
    fun toggleAccountDebitCredit(index: Int) {
        _state.update { 
            val newAccounts = it.accounts.toMutableList().apply {
                this[index] = this[index].copy(isDebit = !this[index].isDebit)
            }
            val updatedState = it.copy(accounts = newAccounts)
            calculateTotals(updatedState)
        }
    }

    /**
     * Select the payment method for the voucher
     */
    fun selectPaymentMethod(paymentMethod: PaymentMethod?) {
        _state.update { it.copy(selectedPaymentMethod = paymentMethod) }
    }

    /**
     * Set the date and time
     */
    fun setDateTime(timestamp: Long) {
        _state.update { it.copy(dateTime = timestamp) }
    }

    /**
     * Set the description
     */
    fun setDescription(description: String) {
        _state.update { it.copy(description = description) }
    }

    /**
     * Calculate totals and check if balanced
     */
    private fun calculateTotals(state: AddJournalVoucherState): AddJournalVoucherState {
        var totalDebit = 0.0
        var totalCredit = 0.0

        state.accounts.forEach { account ->
            if (account.isDebit) {
                totalDebit += account.amount
            } else {
                totalCredit += account.amount
            }
        }

        val isBalanced = totalDebit > 0 && totalDebit == totalCredit

        return state.copy(
            totalDebit = totalDebit,
            totalCredit = totalCredit,
            isBalanced = isBalanced
        )
    }

    /**
     * Save the journal voucher
     */
    fun saveJournalVoucher() {
        val currentState = _state.value

        // Validation
        if (currentState.accounts.isEmpty()) {
            _state.update { it.copy(error = "Please add at least one account") }
            return
        }

        if (!currentState.isBalanced) {
            if (currentState.totalDebit == 0.0) {
                _state.update { it.copy(error = "Debit and credit values must be greater than zero") }
            } else {
                _state.update { it.copy(error = "Debit and credit values must be equal") }
            }
            return
        }

        if (currentState.selectedPaymentMethod == null) {
            _state.update { it.copy(error = "Please select a payment method") }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                // Create the transaction
                val transaction = Transaction(
                    transactionType = 19, // TRANSACTION_TYPE_JOURNAL
                    totalPaid = currentState.totalDebit,
                    totalBill = currentState.totalDebit,
                    description = currentState.description.ifBlank { null },
                    timestamp = currentState.dateTime.toString(),
                    paymentMethodToSlug = currentState.selectedPaymentMethod.slug,
                    paymentMethodTo = currentState.selectedPaymentMethod,
                    flatDiscount = 0.0,
                    flatTax = 0.0,
                    additionalCharges = 0.0,
                    // Store journal accounts as JSON in description or use a custom field
                    // For now, we'll need to extend the Transaction model or use transactionDetails
                    transactionDetails = emptyList() // Journal vouchers don't have product details
                )

                transactionUseCases.addTransaction(transaction)
                    .onSuccess {
                        _state.update { 
                            AddJournalVoucherState(
                                success = true,
                                dateTime = Clock.System.now().toEpochMilliseconds()
                            )
                        }
                    }
                    .onFailure { exception ->
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "Failed to save journal voucher"
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
            AddJournalVoucherState(
                dateTime = Clock.System.now().toEpochMilliseconds()
            )
        }
    }
}

