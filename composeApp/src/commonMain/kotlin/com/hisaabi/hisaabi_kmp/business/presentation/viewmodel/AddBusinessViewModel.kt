package com.hisaabi.hisaabi_kmp.business.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.business.domain.model.Business
import com.hisaabi.hisaabi_kmp.business.domain.usecase.BusinessUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AddBusinessViewModel(
    private val useCases: BusinessUseCases
) : ViewModel() {
    
    private val _state = MutableStateFlow(AddBusinessState())
    val state: StateFlow<AddBusinessState> = _state.asStateFlow()
    
    fun onTitleChanged(title: String) {
        _state.update { it.copy(title = title, titleError = null) }
    }
    
    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email, emailError = null) }
    }
    
    fun onPhoneChanged(phone: String) {
        _state.update { it.copy(phone = phone) }
    }
    
    fun onAddressChanged(address: String) {
        _state.update { it.copy(address = address) }
    }
    
    fun setBusinessToEdit(business: Business) {
        _state.update {
            it.copy(
                businessToEdit = business,
                title = business.title,
                email = business.email ?: "",
                phone = business.phone ?: "",
                address = business.address ?: "",
                isEditMode = true
            )
        }
    }
    
    fun saveBusiness() {
        val currentState = _state.value
        
        // Validate
        if (currentState.title.isBlank()) {
            _state.update { it.copy(titleError = "Business name is required") }
            return
        }
        
        // Validate email if provided
        if (currentState.email.isNotBlank() && !isValidEmail(currentState.email)) {
            _state.update { it.copy(emailError = "Invalid email format") }
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            if (currentState.isEditMode && currentState.businessToEdit != null) {
                // Update existing business
                val updatedBusiness = currentState.businessToEdit.copy(
                    title = currentState.title,
                    email = currentState.email.ifBlank { null },
                    phone = currentState.phone.ifBlank { null },
                    address = currentState.address.ifBlank { null }
                )
                
                val result = useCases.updateBusiness(updatedBusiness)
                
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
                                error = error.message ?: "Failed to update business"
                            )
                        }
                    }
                )
            } else {
                // Add new business
                val result = useCases.addBusiness(
                    title = currentState.title,
                    email = currentState.email.ifBlank { null },
                    phone = currentState.phone.ifBlank { null },
                    address = currentState.address.ifBlank { null },
                    logo = null // TODO: Add logo upload functionality
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
                                error = error.message ?: "Failed to save business"
                            )
                        }
                    }
                )
            }
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    fun resetState() {
        _state.value = AddBusinessState()
    }
}

data class AddBusinessState(
    val title: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val titleError: String? = null,
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false,
    val businessToEdit: Business? = null
)

