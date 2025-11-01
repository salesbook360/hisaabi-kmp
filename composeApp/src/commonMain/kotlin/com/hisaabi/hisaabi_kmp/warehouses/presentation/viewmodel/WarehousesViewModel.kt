package com.hisaabi.hisaabi_kmp.warehouses.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse
import com.hisaabi.hisaabi_kmp.warehouses.domain.usecase.WarehouseUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WarehousesViewModel(
    private val useCases: WarehouseUseCases,
    private val sessionManager: AppSessionManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(WarehousesState())
    val state: StateFlow<WarehousesState> = _state.asStateFlow()
    
    private var businessSlug: String? = null
    
    init {
        viewModelScope.launch {
            sessionManager.observeBusinessSlug().collect { newBusinessSlug ->
                businessSlug = newBusinessSlug
                if (newBusinessSlug != null) {
                    loadWarehouses()
                }
            }
        }
    }
    
    fun loadWarehouses() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val slug = businessSlug
            if (slug == null) {
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        warehouses = emptyList(),
                        error = "No business selected"
                    )
                }
                return@launch
            }
            
            useCases.getWarehouses(slug)
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

