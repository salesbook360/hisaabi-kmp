package com.hisaabi.hisaabi_kmp.templates.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.templates.data.TemplatesRepository
import com.hisaabi.hisaabi_kmp.templates.domain.model.MessageTemplate
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TemplatesViewModel(
    private val repository: TemplatesRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(TemplatesState())
    val state: StateFlow<TemplatesState> = _state.asStateFlow()
    
    init {
        loadTemplates()
    }
    
    private fun loadTemplates() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            repository.templates.collect { templates ->
                _state.update { 
                    it.copy(
                        templates = templates,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun deleteTemplate(templateId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTemplate(templateId)
                _state.update { it.copy(message = "Template deleted successfully") }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Failed to delete template") }
            }
        }
    }
    
    fun resetToDefaults() {
        viewModelScope.launch {
            try {
                repository.resetToDefaults()
                _state.update { it.copy(message = "Templates reset to defaults") }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Failed to reset templates") }
            }
        }
    }
    
    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class TemplatesState(
    val templates: List<MessageTemplate> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

