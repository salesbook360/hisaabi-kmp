package com.hisaabi.hisaabi_kmp.business.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.business.data.datasource.BusinessPreferencesDataSource
import com.hisaabi.hisaabi_kmp.business.domain.model.Business
import com.hisaabi.hisaabi_kmp.business.domain.usecase.BusinessUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MyBusinessViewModel(
    private val useCases: BusinessUseCases,
    private val preferencesDataSource: BusinessPreferencesDataSource
) : ViewModel() {
    
    private val _state = MutableStateFlow(MyBusinessState())
    val state: StateFlow<MyBusinessState> = _state.asStateFlow()
    
    init {
        loadBusinesses()
        observeSelectedBusiness()
    }
    
    private fun observeSelectedBusiness() {
        viewModelScope.launch {
            preferencesDataSource.observeSelectedBusinessId()
                .collect { selectedId ->
                    _state.update { it.copy(selectedBusinessId = selectedId) }
                }
        }
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
    
    fun selectBusiness(business: Business) {
        viewModelScope.launch {
            preferencesDataSource.setSelectedBusinessId(business.id)
            // State will be automatically updated via observeSelectedBusiness
        }
    }
    
    fun deleteBusiness(business: Business) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val result = useCases.deleteBusiness(business)
            
            result.fold(
                onSuccess = {
                    // If deleted business was selected, clear selection
                    if (_state.value.selectedBusinessId == business.id) {
                        preferencesDataSource.setSelectedBusinessId(null)
                    }
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
    val selectedBusinessId: Int? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

