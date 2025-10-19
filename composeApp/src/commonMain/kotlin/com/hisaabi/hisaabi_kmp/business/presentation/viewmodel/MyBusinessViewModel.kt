package com.hisaabi.hisaabi_kmp.business.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.business.domain.model.Business
import com.hisaabi.hisaabi_kmp.business.domain.usecase.BusinessUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MyBusinessViewModel(
    private val useCases: BusinessUseCases
) : ViewModel() {
    
    private val _state = MutableStateFlow(MyBusinessState())
    val state: StateFlow<MyBusinessState> = _state.asStateFlow()
    
    init {
        loadBusinesses()
    }
    
    fun loadBusinesses() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            useCases.getBusinesses()
                .catch { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            error = error.message ?: "Failed to load businesses"
                        )
                    }
                }
                .collect { businesses ->
                    _state.update { 
                        it.copy(
                            businesses = businesses,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }
    
    fun deleteBusiness(business: Business) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val result = useCases.deleteBusiness(business)
            
            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, error = null) }
                    // Businesses will be automatically updated via Flow
                },
                onFailure = { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to delete business"
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

data class MyBusinessState(
    val businesses: List<Business> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

