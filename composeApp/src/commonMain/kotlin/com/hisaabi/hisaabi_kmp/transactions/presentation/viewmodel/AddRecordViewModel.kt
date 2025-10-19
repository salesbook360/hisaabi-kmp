package com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.transactions.domain.model.RecordType
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionState
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.TransactionUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class AddRecordState(
    val recordType: RecordType? = null,
    val state: TransactionState = TransactionState.PENDING,
    val selectedParty: Party? = null,
    val description: String = "",
    val amount: String = "",
    val dateTime: Long = Clock.System.now().toEpochMilliseconds(),
    val remindDateTime: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class AddRecordViewModel(
    private val transactionUseCases: TransactionUseCases
) : ViewModel() {
    
    private val _state = MutableStateFlow(AddRecordState())
    val state: StateFlow<AddRecordState> = _state.asStateFlow()
    
    fun setRecordType(recordType: RecordType) {
        _state.update { it.copy(recordType = recordType) }
    }
    
    fun setState(transactionState: TransactionState) {
        _state.update { it.copy(state = transactionState) }
    }
    
    fun selectParty(party: Party?) {
        _state.update { it.copy(selectedParty = party) }
    }
    
    fun setDescription(description: String) {
        _state.update { it.copy(description = description) }
    }
    
    fun setAmount(amount: String) {
        _state.update { it.copy(amount = amount) }
    }
    
    fun setDateTime(dateTime: Long) {
        _state.update { it.copy(dateTime = dateTime) }
    }
    
    fun setRemindDateTime(dateTime: Long?) {
        _state.update { it.copy(remindDateTime = dateTime) }
    }
    
    fun saveRecord() {
        val currentState = _state.value
        
        // Validation
        if (currentState.recordType == null) {
            _state.update { it.copy(error = "Please select a record type") }
            return
        }
        
        if (RecordType.requiresParty(currentState.recordType) && currentState.selectedParty == null) {
            _state.update { it.copy(error = "Please select a party") }
            return
        }
        
        if (currentState.description.isBlank()) {
            _state.update { it.copy(error = "Please enter a description") }
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                val transaction = Transaction(
                    customerSlug = currentState.selectedParty?.slug,
                    party = currentState.selectedParty,
                    transactionType = currentState.recordType.value,
                    stateId = currentState.state.value,
                    description = currentState.description,
                    totalPaid = currentState.amount.toDoubleOrNull() ?: 0.0,
                    timestamp = currentState.dateTime.toString(),
                    remindAtMilliseconds = currentState.remindDateTime ?: 0L,
                    totalBill = 0.0,
                    flatDiscount = 0.0,
                    flatTax = 0.0,
                    additionalCharges = 0.0
                )
                
                transactionUseCases.addTransaction(transaction)
                    .onSuccess {
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                successMessage = "Record saved successfully"
                            )
                        }
                    }
                    .onFailure { e ->
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to save record"
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

