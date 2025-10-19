package com.hisaabi.hisaabi_kmp.quantityunits.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
import com.hisaabi.hisaabi_kmp.quantityunits.domain.usecase.QuantityUnitUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AddQuantityUnitViewModel(
    private val useCases: QuantityUnitUseCases
) : ViewModel() {
    
    private val _state = MutableStateFlow(AddQuantityUnitState())
    val state: StateFlow<AddQuantityUnitState> = _state.asStateFlow()
    
    fun onTitleChanged(title: String) {
        _state.update { it.copy(title = title, titleError = null) }
    }
    
    fun onConversionFactorChanged(factor: String) {
        _state.update { it.copy(conversionFactor = factor, conversionFactorError = null) }
    }
    
    fun onSortOrderChanged(order: String) {
        _state.update { it.copy(sortOrder = order) }
    }
    
    fun setUnitToEdit(unit: QuantityUnit) {
        _state.update {
            it.copy(
                unitToEdit = unit,
                title = unit.title,
                conversionFactor = unit.conversionFactor.toString(),
                sortOrder = unit.sortOrder.toString(),
                isEditMode = true
            )
        }
    }
    
    fun saveUnit() {
        val currentState = _state.value
        
        // Validate
        if (currentState.title.isBlank()) {
            _state.update { it.copy(titleError = "Unit name is required") }
            return
        }
        
        val factor = currentState.conversionFactor.toDoubleOrNull()
        if (factor == null || factor <= 0) {
            _state.update { it.copy(conversionFactorError = "Invalid conversion factor") }
            return
        }
        
        val order = currentState.sortOrder.toIntOrNull() ?: 0
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            if (currentState.isEditMode && currentState.unitToEdit != null) {
                // Update existing unit
                val updatedUnit = currentState.unitToEdit.copy(
                    title = currentState.title,
                    conversionFactor = factor,
                    sortOrder = order
                )
                
                val result = useCases.updateUnit(updatedUnit)
                
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
                                error = error.message ?: "Failed to update unit"
                            )
                        }
                    }
                )
            } else {
                // Add new unit
                val result = useCases.addUnit(
                    title = currentState.title,
                    sortOrder = order,
                    conversionFactor = factor,
                    businessSlug = null, // TODO: Get from business context
                    createdBy = null // TODO: Get from auth context
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
                                error = error.message ?: "Failed to save unit"
                            )
                        }
                    }
                )
            }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    fun resetState() {
        _state.value = AddQuantityUnitState()
    }
}

data class AddQuantityUnitState(
    val title: String = "",
    val conversionFactor: String = "1.0",
    val sortOrder: String = "0",
    val titleError: String? = null,
    val conversionFactorError: String? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false,
    val unitToEdit: QuantityUnit? = null
)

