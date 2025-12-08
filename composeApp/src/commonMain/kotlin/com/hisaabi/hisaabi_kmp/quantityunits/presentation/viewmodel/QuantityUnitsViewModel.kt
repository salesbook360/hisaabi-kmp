package com.hisaabi.hisaabi_kmp.quantityunits.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
import com.hisaabi.hisaabi_kmp.quantityunits.domain.usecase.QuantityUnitUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QuantityUnitsViewModel(
    private val useCases: QuantityUnitUseCases,
    private val sessionManager: AppSessionManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(QuantityUnitsState())
    val state: StateFlow<QuantityUnitsState> = _state.asStateFlow()
    
    private var businessSlug: String? = null
    
    init {
        // Observe business slug changes to reload data when business changes
        viewModelScope.launch {
            println("DEBUG QuantityUnitsViewModel: Starting to observe businessSlug")
            sessionManager.observeBusinessSlug().collect { newBusinessSlug ->
                println("DEBUG QuantityUnitsViewModel: Received businessSlug = $newBusinessSlug")
                businessSlug = newBusinessSlug
                if (newBusinessSlug != null) {
                    loadParentUnitTypes()
                } else {
                    println("DEBUG QuantityUnitsViewModel: No business found, setting empty state")
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "No active business found",
                            parentUnitTypes = emptyList(),
                            childUnits = emptyList()
                        )
                    }
                }
            }
        }
    }
    
    private fun loadBusinessAndUnits() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            // Get active business slug
            businessSlug = sessionManager.getBusinessSlug()
            
            if (businessSlug == null) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "No active business found"
                    )
                }
                return@launch
            }
            
            loadParentUnitTypes()
        }
    }
    
    fun loadUnits() {
        loadBusinessAndUnits()
    }
    
    private fun loadParentUnitTypes() {
        viewModelScope.launch {
            val bSlug = businessSlug ?: run {
                println("DEBUG QuantityUnitsViewModel: loadParentUnitTypes - businessSlug is null, returning")
                return@launch
            }
            
            println("DEBUG QuantityUnitsViewModel: loadParentUnitTypes - Loading for businessSlug = $bSlug")
            
            useCases.getUnits.getParentUnitTypes(bSlug)
                .catch { error ->
                    println("DEBUG QuantityUnitsViewModel: loadParentUnitTypes - Error: ${error.message}")
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            error = error.message ?: "Failed to load unit types"
                        )
                    }
                }
                .collect { parentTypes ->
                    println("DEBUG QuantityUnitsViewModel: loadParentUnitTypes - Received ${parentTypes.size} parent types")
                    parentTypes.forEach { 
                        println("DEBUG QuantityUnitsViewModel:   - Parent: ${it.title}, slug=${it.slug}, businessSlug=${it.businessSlug}, parentSlug=${it.parentSlug}")
                    }
                    
                    _state.update { 
                        it.copy(
                            parentUnitTypes = parentTypes,
                            isLoading = false,
                            error = null
                        )
                    }
                    
                    // If no parent type is selected and we have parent types, select the first one
                    if (_state.value.selectedParentUnit == null && parentTypes.isNotEmpty()) {
                        selectParentUnit(parentTypes.first())
                    } else if (_state.value.selectedParentUnit != null) {
                        // Reload child units for the selected parent
                        loadChildUnitsForParent(_state.value.selectedParentUnit!!.slug!!)
                    }
                }
        }
    }
    
    fun selectParentUnit(parentUnit: QuantityUnit) {
        _state.update { it.copy(selectedParentUnit = parentUnit) }
        parentUnit.slug?.let { loadChildUnitsForParent(it) }
    }
    
    private fun loadChildUnitsForParent(parentSlug: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingChildUnits = true) }
            
            useCases.getUnits.getUnitsByParent(parentSlug)
                .catch { error ->
                    _state.update { 
                        it.copy(
                            isLoadingChildUnits = false,
                            error = error.message ?: "Failed to load units"
                        )
                    }
                }
                .collect { childUnits ->
                    _state.update { 
                        it.copy(
                            childUnits = childUnits,
                            isLoadingChildUnits = false,
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
                    // Reload the parent types and child units
                    if (unit.isParentUnitType) {
                        loadParentUnitTypes()
                    } else {
                        _state.value.selectedParentUnit?.slug?.let { loadChildUnitsForParent(it) }
                    }
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
    
    fun getBaseUnitForSelectedParent(): QuantityUnit? {
        val selectedParent = _state.value.selectedParentUnit ?: return null
        val baseSlug = selectedParent.baseConversionUnitSlug ?: return _state.value.childUnits.firstOrNull()
        return _state.value.childUnits.find { it.slug == baseSlug } ?: _state.value.childUnits.firstOrNull()
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class QuantityUnitsState(
    val parentUnitTypes: List<QuantityUnit> = emptyList(),
    val childUnits: List<QuantityUnit> = emptyList(),
    val selectedParentUnit: QuantityUnit? = null,
    val units: List<QuantityUnit> = emptyList(), // For backward compatibility
    val isLoading: Boolean = false,
    val isLoadingChildUnits: Boolean = false,
    val error: String? = null
)

