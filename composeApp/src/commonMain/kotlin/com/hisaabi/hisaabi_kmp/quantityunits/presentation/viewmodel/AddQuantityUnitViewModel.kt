package com.hisaabi.hisaabi_kmp.quantityunits.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
import com.hisaabi.hisaabi_kmp.quantityunits.domain.usecase.QuantityUnitUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AddQuantityUnitViewModel(
    private val useCases: QuantityUnitUseCases,
    private val sessionManager: AppSessionManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(AddQuantityUnitState())
    val state: StateFlow<AddQuantityUnitState> = _state.asStateFlow()
    
    private var businessSlug: String? = null
    private var userSlug: String? = null
    
    init {
        // Observe session context changes to keep business and user info updated
        viewModelScope.launch {
            sessionManager.observeSessionContext().collect { context ->
                businessSlug = context.businessSlug
                userSlug = context.userSlug
            }
        }
    }
    
    fun onTitleChanged(title: String) {
        _state.update { it.copy(title = title, titleError = null) }
    }
    
    fun onConversionFactorChanged(factor: String) {
        _state.update { it.copy(conversionFactor = factor, conversionFactorError = null) }
    }
    
    fun onSortOrderChanged(order: String) {
        _state.update { it.copy(sortOrder = order) }
    }
    
    fun setAddingParentUnitType(isAddingParent: Boolean) {
        _state.update { 
            it.copy(
                isAddingParentUnitType = isAddingParent,
                // Hide conversion factor for parent unit types
                conversionFactor = if (isAddingParent) "1.0" else it.conversionFactor
            ) 
        }
    }
    
    fun setParentUnit(parentUnit: QuantityUnit?) {
        _state.update { it.copy(selectedParentUnit = parentUnit) }
        // Load child units of this parent to show base unit options
        parentUnit?.slug?.let { loadChildUnitsForBaseUnitSelection(it) }
    }
    
    fun setBaseUnit(baseUnit: QuantityUnit?) {
        _state.update { it.copy(selectedBaseUnit = baseUnit) }
    }
    
    private fun loadChildUnitsForBaseUnitSelection(parentSlug: String) {
        viewModelScope.launch {
            val childUnits = useCases.getUnits.getUnitsByParentSuspend(parentSlug)
            _state.update { it.copy(availableBaseUnits = childUnits) }
            
            // If there are existing child units, select the first one as base unit
            if (childUnits.isNotEmpty() && _state.value.selectedBaseUnit == null) {
                _state.update { it.copy(selectedBaseUnit = childUnits.first()) }
            }
        }
    }
    
    fun setUnitToEdit(unit: QuantityUnit) {
        _state.update {
            it.copy(
                unitToEdit = unit,
                title = unit.title,
                conversionFactor = unit.conversionFactor.toString(),
                sortOrder = unit.sortOrder.toString(),
                isEditMode = true,
                isAddingParentUnitType = unit.isParentUnitType
            )
        }
        
        // If editing a child unit, load the parent and base unit info
        if (unit.isChildUnit) {
            viewModelScope.launch {
                unit.parentSlug?.let { parentSlug ->
                    val parentUnit = useCases.getUnits.getUnitBySlug(parentSlug)
                    _state.update { it.copy(selectedParentUnit = parentUnit) }
                    loadChildUnitsForBaseUnitSelection(parentSlug)
                    
                    // Set the base unit
                    unit.baseConversionUnitSlug?.let { baseSlug ->
                        val baseUnit = useCases.getUnits.getUnitBySlug(baseSlug)
                        _state.update { it.copy(selectedBaseUnit = baseUnit) }
                    }
                }
            }
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
        
        // For child units, validate that a parent unit type is selected
        if (!currentState.isAddingParentUnitType && currentState.selectedParentUnit == null) {
            _state.update { it.copy(error = "Please select a unit type first") }
            return
        }
        
        val order = currentState.sortOrder.toIntOrNull() ?: 0
        
        // Validate business context for new units (not needed for edit mode)
        if (!currentState.isEditMode && businessSlug == null) {
            _state.update { it.copy(error = "No business context available. Please try again.") }
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            if (currentState.isEditMode && currentState.unitToEdit != null) {
                // Update existing unit
                val updatedUnit = currentState.unitToEdit.copy(
                    title = currentState.title,
                    conversionFactor = factor,
                    sortOrder = order,
                    baseConversionUnitSlug = if (!currentState.isAddingParentUnitType) 
                        currentState.selectedBaseUnit?.slug 
                    else 
                        currentState.unitToEdit.baseConversionUnitSlug
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
                if (currentState.isAddingParentUnitType) {
                    // Add new parent unit type
                    val result = useCases.addUnit.addParentUnitType(
                        title = currentState.title,
                        sortOrder = order,
                        businessSlug = businessSlug,
                        createdBy = userSlug
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
                                    error = error.message ?: "Failed to save unit type"
                                )
                            }
                        }
                    )
                } else {
                    // Add new child unit
                    val result = useCases.addUnit(
                        title = currentState.title,
                        sortOrder = order,
                        conversionFactor = factor,
                        parentSlug = currentState.selectedParentUnit?.slug,
                        baseConversionUnitSlug = currentState.selectedBaseUnit?.slug,
                        businessSlug = businessSlug,
                        createdBy = userSlug
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
    val unitToEdit: QuantityUnit? = null,
    // New fields for unit type hierarchy
    val isAddingParentUnitType: Boolean = false,
    val selectedParentUnit: QuantityUnit? = null,
    val selectedBaseUnit: QuantityUnit? = null,
    val availableBaseUnits: List<QuantityUnit> = emptyList()
)

