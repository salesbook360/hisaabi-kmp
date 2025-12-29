package com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import com.hisaabi.hisaabi_kmp.transactions.domain.model.JournalAccount
import com.hisaabi.hisaabi_kmp.transactions.domain.model.JournalAccountType
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.TransactionUseCases
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.GetTransactionWithDetailsUseCase
import com.hisaabi.hisaabi_kmp.transactions.data.repository.TransactionsRepository
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
    val success: Boolean = false,
    val editingTransactionSlug: String? = null
)

class AddJournalVoucherViewModel(
    private val transactionUseCases: TransactionUseCases,
    val appSessionManager: AppSessionManager,
    private val getTransactionWithDetailsUseCase: GetTransactionWithDetailsUseCase,
    private val transactionsRepository: TransactionsRepository
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
     * Toggle pay amount/get amount for an account
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
                _state.update { it.copy(error = "Pay amount and get amount values must be greater than zero") }
            } else {
                _state.update { it.copy(error = "Pay amount and get amount values must be equal") }
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
                val isEditing = currentState.editingTransactionSlug != null
                val parentSlug = currentState.editingTransactionSlug
                
                // If editing, delete old child transactions first
                if (isEditing && parentSlug != null) {
                    val oldChildTransactions = transactionsRepository.getChildTransactions(parentSlug)
                    oldChildTransactions.forEach { childTransaction ->
                        transactionUseCases.deleteTransaction(childTransaction)
                    }
                }
                
                // Step 1: Create or update the main Journal Voucher transaction
                val journalTransaction = Transaction(
                    slug = parentSlug, // Include slug if editing
                    transactionType = AllTransactionTypes.JOURNAL_VOUCHER.value,
                    totalPaid = currentState.totalDebit,
                    totalBill = currentState.totalDebit,
                    description = currentState.description.ifBlank { null },
                    timestamp = currentState.dateTime.toString(),
                    paymentMethodToSlug = currentState.selectedPaymentMethod.slug,
                    paymentMethodTo = currentState.selectedPaymentMethod,
                    flatDiscount = 0.0,
                    flatTax = 0.0,
                    additionalCharges = 0.0,
                    transactionDetails = emptyList(),
                    businessSlug = appSessionManager.getBusinessSlug(),
                    createdBy = appSessionManager.getUserSlug()
                )

                // Save or update the parent journal voucher transaction
                val finalParentSlug = if (isEditing) {
                    // For update, use the existing slug
                    val updateResult = transactionUseCases.updateTransaction(journalTransaction)
                    if (updateResult.isFailure) {
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = updateResult.exceptionOrNull()?.message ?: "Failed to update journal voucher"
                            )
                        }
                        return@launch
                    }
                    parentSlug ?: ""
                } else {
                    // For create, add transaction and get the slug
                    val addResult = transactionUseCases.addTransaction(journalTransaction)
                    if (addResult.isFailure) {
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = addResult.exceptionOrNull()?.message ?: "Failed to save journal voucher"
                            )
                        }
                        return@launch
                    }
                    addResult.getOrNull() ?: ""
                }
                
                // Step 2: Create child transactions for each account
                viewModelScope.launch {
                    try {
                        var timestampCounter = 1L
                        
                        currentState.accounts.forEach { account ->
                            val childTimestamp = (currentState.dateTime + timestampCounter).toString()
                            
                            when {
                                // Party accounts (Customer, Vendor, Investor, Expense, Extra Income)
                                account.party != null -> {
                                    val transactionType = getTransactionTypeFromParty(
                                        account.party.roleId,
                                        account.isDebit
                                    )
                                    
                                    val amount = if (transactionType == AllTransactionTypes.EXPENSE.value || transactionType == AllTransactionTypes.EXTRA_INCOME.value) {
                                        // For expense and income, use signed amounts
                                        if (account.isDebit) account.amount else -account.amount
                                    } else {
                                        account.amount
                                    }
                                    
                                    val childTransaction = Transaction(
                                        parentSlug = finalParentSlug,
                                        transactionType = transactionType,
                                        partySlug = account.party.slug,
                                        party = account.party,
                                        totalPaid = amount,
                                        totalBill = 0.0,
                                        timestamp = childTimestamp,
                                        description = currentState.description.ifBlank { null },
                                        paymentMethodToSlug = currentState.selectedPaymentMethod.slug,
                                        paymentMethodTo = currentState.selectedPaymentMethod,
                                        flatDiscount = 0.0,
                                        flatTax = 0.0,
                                        additionalCharges = 0.0,
                                        businessSlug = appSessionManager.getBusinessSlug(),
                                        createdBy = appSessionManager.getBusinessSlug()
                                    )
                                    
                                    transactionUseCases.addTransaction(childTransaction)
                                }
                                
                                // Payment Method accounts
                                account.paymentMethod != null -> {
                                    val childTransaction = Transaction(
                                        parentSlug = finalParentSlug,
                                        transactionType = AllTransactionTypes.PAYMENT_TRANSFER.value,
                                        totalPaid = account.amount,
                                        totalBill = 0.0,
                                        timestamp = childTimestamp,
                                        description = currentState.description.ifBlank { null },
                                        paymentMethodFromSlug = if (account.isDebit) 
                                            currentState.selectedPaymentMethod.slug 
                                        else 
                                            account.paymentMethod.slug,
                                        paymentMethodFrom = if (account.isDebit) 
                                            currentState.selectedPaymentMethod 
                                        else 
                                            account.paymentMethod,
                                        paymentMethodToSlug = if (account.isDebit) 
                                            account.paymentMethod.slug 
                                        else 
                                            currentState.selectedPaymentMethod.slug,
                                        paymentMethodTo = if (account.isDebit) 
                                            account.paymentMethod 
                                        else 
                                            currentState.selectedPaymentMethod,
                                        flatDiscount = 0.0,
                                        flatTax = 0.0,
                                        additionalCharges = 0.0,
                                        businessSlug = appSessionManager.getBusinessSlug(),
                                        createdBy = appSessionManager.getBusinessSlug()
                                    )
                                    
                                    transactionUseCases.addTransaction(childTransaction)
                                }
                            }
                            
                            timestampCounter++
                        }
                        
                        _state.update { 
                            AddJournalVoucherState(
                                success = true,
                                dateTime = Clock.System.now().toEpochMilliseconds()
                            )
                        }
                    } catch (e: Exception) {
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = "Failed to save child transactions: ${e.message}"
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
    
    /**
     * Get transaction type based on party role and whether it's pay amount or get amount
     */
    private fun getTransactionTypeFromParty(roleId: Int, isDebit: Boolean): Int {
        return when (roleId) {
            // Customer
            PartyType.CUSTOMER.type -> if (isDebit) AllTransactionTypes.PAY_TO_CUSTOMER.value else AllTransactionTypes.GET_FROM_CUSTOMER.value
            
            // Vendor
            PartyType.VENDOR.type -> if (isDebit) AllTransactionTypes.PAY_TO_VENDOR.value else AllTransactionTypes.GET_FROM_VENDOR.value
            
            // Investor
            PartyType.INVESTOR.type -> if (isDebit) AllTransactionTypes.INVESTMENT_WITHDRAW.value else AllTransactionTypes.INVESTMENT_DEPOSIT.value
            
            // Expense
            PartyType.EXPENSE.type -> AllTransactionTypes.EXPENSE.value
            
            // Extra Income
            PartyType.EXTRA_INCOME.type -> AllTransactionTypes.EXTRA_INCOME.value
            
            // Walk-in Customer (treat as Customer)
            PartyType.WALK_IN_CUSTOMER.type -> if (isDebit) AllTransactionTypes.PAY_TO_CUSTOMER.value else AllTransactionTypes.GET_FROM_CUSTOMER.value
            
            // Default Vendor (treat as Vendor)
            PartyType.DEFAULT_VENDOR.type -> if (isDebit) AllTransactionTypes.PAY_TO_VENDOR.value else AllTransactionTypes.GET_FROM_VENDOR.value
            
            else -> if (isDebit) AllTransactionTypes.PAY_TO_CUSTOMER.value else AllTransactionTypes.GET_FROM_CUSTOMER.value  // Default to customer transactions
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

    /**
     * Load transaction for editing
     */
    fun loadTransactionForEdit(transactionSlug: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Load parent journal voucher transaction
                val parentTransaction = getTransactionWithDetailsUseCase(transactionSlug)
                    ?: throw IllegalStateException("Transaction not found")
                
                // Load child transactions
                val childTransactions = transactionsRepository.getChildTransactions(transactionSlug)
                
                // Convert child transactions to journal accounts
                val accounts = mutableListOf<JournalAccount>()
                
                childTransactions.forEach { childTransaction ->
                    when {
                        // Party transactions (Customer, Vendor, Investor, Expense, Extra Income)
                        childTransaction.party != null -> {
                            val accountType = JournalAccountType.fromPartyRoleId(childTransaction.party.roleId)
                            if (accountType != null) {
                                // Determine if it's debit or credit based on transaction type
                                val isDebit = when (childTransaction.transactionType) {
                                    AllTransactionTypes.PAY_TO_CUSTOMER.value,
                                    AllTransactionTypes.PAY_TO_VENDOR.value,
                                    AllTransactionTypes.INVESTMENT_WITHDRAW.value,
                                    AllTransactionTypes.EXPENSE.value -> true
                                    AllTransactionTypes.GET_FROM_CUSTOMER.value,
                                    AllTransactionTypes.GET_FROM_VENDOR.value,
                                    AllTransactionTypes.INVESTMENT_DEPOSIT.value,
                                    AllTransactionTypes.EXTRA_INCOME.value -> false
                                    else -> childTransaction.totalPaid > 0 // Default based on amount sign
                                }
                                
                                // For expense and income, use absolute value
                                val amount = if (childTransaction.transactionType == AllTransactionTypes.EXPENSE.value || 
                                    childTransaction.transactionType == AllTransactionTypes.EXTRA_INCOME.value) {
                                    kotlin.math.abs(childTransaction.totalPaid)
                                } else {
                                    kotlin.math.abs(childTransaction.totalPaid)
                                }
                                
                                accounts.add(
                                    JournalAccount(
                                        title = childTransaction.party.name,
                                        amount = amount,
                                        isDebit = isDebit,
                                        accountType = accountType,
                                        party = childTransaction.party
                                    )
                                )
                            }
                        }
                        
                        // Payment transfer transactions
                        childTransaction.transactionType == AllTransactionTypes.PAYMENT_TRANSFER.value -> {
                            // Determine which payment method is the account (not the selected one)
                            val accountPaymentMethod = if (childTransaction.paymentMethodFrom?.slug == parentTransaction.paymentMethodTo?.slug) {
                                childTransaction.paymentMethodTo
                            } else {
                                childTransaction.paymentMethodFrom
                            }
                            
                            if (accountPaymentMethod != null) {
                                // Determine if it's debit or credit
                                // If paymentMethodFrom is the account, it's debit (paying from it)
                                // If paymentMethodTo is the account, it's credit (receiving to it)
                                val isDebit = childTransaction.paymentMethodFrom?.slug == accountPaymentMethod.slug
                                
                                accounts.add(
                                    JournalAccount(
                                        title = accountPaymentMethod.title,
                                        amount = kotlin.math.abs(childTransaction.totalPaid),
                                        isDebit = isDebit,
                                        accountType = JournalAccountType.PAYMENT_METHOD,
                                        paymentMethod = accountPaymentMethod
                                    )
                                )
                            }
                        }
                    }
                }
                
                // Parse timestamp
                val timestamp = parentTransaction.timestamp?.toLongOrNull() 
                    ?: Clock.System.now().toEpochMilliseconds()
                
                // Update state with loaded data
                val updatedState = AddJournalVoucherState(
                    accounts = accounts,
                    selectedPaymentMethod = parentTransaction.paymentMethodTo,
                    dateTime = timestamp,
                    description = parentTransaction.description ?: "",
                    editingTransactionSlug = transactionSlug
                )
                
                _state.update { calculateTotals(updatedState).copy(isLoading = false) }
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
}

