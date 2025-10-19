package com.hisaabi.hisaabi_kmp.quantityunits.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
import com.hisaabi.hisaabi_kmp.quantityunits.domain.usecase.QuantityUnitUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QuantityUnitsViewModel(
    private val useCases: QuantityUnitUseCases
) : ViewModel() {
    
    private val _state = MutableStateFlow(QuantityUnitsState())
    val state: StateFlow<QuantityUnitsState> = _state.asStateFlow()
    
    init {
        loadUnits()
    }
    
    fun loadUnits() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            useCases.getUnits()
                .catch { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            error = error.message ?: "Failed to load quantity units"
                        )
                    }
                }
                .collect { units ->
                    _state.update { 
                        it.copy(
                            units = units,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }
    
    fun deleteUnit(unit: QuantityUnit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val result = useCases.deleteUnit(unit)
            
            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, error = null) }
                },
                onFailure = { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to delete unit"
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

data class QuantityUnitsState(
    val units: List<QuantityUnit> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

