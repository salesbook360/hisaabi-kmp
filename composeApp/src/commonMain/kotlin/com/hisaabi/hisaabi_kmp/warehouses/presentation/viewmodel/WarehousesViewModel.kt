package com.hisaabi.hisaabi_kmp.warehouses.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse
import com.hisaabi.hisaabi_kmp.warehouses.domain.usecase.WarehouseUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WarehousesViewModel(
    private val useCases: WarehouseUseCases
) : ViewModel() {
    
    private val _state = MutableStateFlow(WarehousesState())
    val state: StateFlow<WarehousesState> = _state.asStateFlow()
    
    init {
        loadWarehouses()
    }
    
    fun loadWarehouses() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            useCases.getWarehouses()
                .catch { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            error = error.message ?: "Failed to load warehouses"
                        )
                    }
                }
                .collect { warehouses ->
                    _state.update { 
                        it.copy(
                            warehouses = warehouses,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }
    
    fun deleteWarehouse(warehouse: Warehouse) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val result = useCases.deleteWarehouse(warehouse)
            
            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, error = null) }
                    // Warehouses will be automatically updated via Flow
                },
                onFailure = { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to delete warehouse"
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

data class WarehousesState(
    val warehouses: List<Warehouse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

