package com.hisaabi.hisaabi_kmp.warehouses.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.WarehouseType
import com.hisaabi.hisaabi_kmp.warehouses.domain.usecase.WarehouseUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AddWarehouseViewModel(
    private val useCases: WarehouseUseCases
) : ViewModel() {
    
    private val _state = MutableStateFlow(AddWarehouseState())
    val state: StateFlow<AddWarehouseState> = _state.asStateFlow()
    
    fun onTitleChanged(title: String) {
        _state.update { it.copy(title = title, titleError = null) }
    }
    
    fun onAddressChanged(address: String) {
        _state.update { it.copy(address = address) }
    }
    
    fun onDescriptionChanged(description: String) {
        _state.update { it.copy(description = description) }
    }
    
    fun onWarehouseTypeChanged(type: WarehouseType) {
        _state.update { it.copy(selectedType = type) }
    }
    
    fun setWarehouseToEdit(warehouse: Warehouse) {
        _state.update {
            it.copy(
                warehouseToEdit = warehouse,
                title = warehouse.title,
                address = warehouse.address ?: "",
                description = warehouse.description ?: "",
                selectedType = WarehouseType.fromInt(warehouse.typeId) ?: WarehouseType.MAIN,
                isEditMode = true
            )
        }
    }
    
    fun saveWarehouse() {
        val currentState = _state.value
        
        // Validate
        if (currentState.title.isBlank()) {
            _state.update { it.copy(titleError = "Title is required") }
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            if (currentState.isEditMode && currentState.warehouseToEdit != null) {
                // Update existing warehouse
                val updatedWarehouse = currentState.warehouseToEdit.copy(
                    title = currentState.title,
                    address = currentState.address.ifBlank { null },
                    description = currentState.description.ifBlank { null },
                    typeId = currentState.selectedType.typeId
                )
                
                val result = useCases.updateWarehouse(updatedWarehouse)
                
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
                                error = error.message ?: "Failed to update warehouse"
                            )
                        }
                    }
                )
            } else {
                // Add new warehouse
                val result = useCases.addWarehouse(
                    title = currentState.title,
                    address = currentState.address.ifBlank { null },
                    description = currentState.description.ifBlank { null },
                    typeId = currentState.selectedType.typeId,
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
                                error = error.message ?: "Failed to save warehouse"
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
        _state.value = AddWarehouseState()
    }
}

data class AddWarehouseState(
    val title: String = "",
    val address: String = "",
    val description: String = "",
    val selectedType: WarehouseType = WarehouseType.MAIN,
    val titleError: String? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false,
    val warehouseToEdit: Warehouse? = null
)

