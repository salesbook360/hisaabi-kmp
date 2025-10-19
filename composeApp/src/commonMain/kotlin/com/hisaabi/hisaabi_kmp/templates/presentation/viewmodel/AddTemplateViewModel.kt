package com.hisaabi.hisaabi_kmp.templates.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.templates.data.TemplatesRepository
import com.hisaabi.hisaabi_kmp.templates.domain.model.MessageTemplate
import com.hisaabi.hisaabi_kmp.templates.domain.model.TemplatePlaceholder
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AddTemplateViewModel(
    private val repository: TemplatesRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(AddTemplateState())
    val state: StateFlow<AddTemplateState> = _state.asStateFlow()
    
    fun loadTemplate(templateId: String?) {
        if (templateId != null) {
            val template = repository.getTemplateById(templateId)
            if (template != null) {
                _state.update {
                    it.copy(
                        templateId = template.id,
                        title = template.title,
                        templateText = template.template,
                        isEditMode = true
                    )
                }
            }
        }
    }
    
    fun updateTitle(title: String) {
        _state.update { it.copy(title = title) }
    }
    
    fun updateTemplateText(text: String) {
        _state.update { it.copy(templateText = text) }
    }
    
    fun insertPlaceholder(placeholder: TemplatePlaceholder) {
        val currentText = _state.value.templateText
        val newText = if (currentText.isEmpty()) {
            placeholder.key
        } else {
            "$currentText ${placeholder.key}"
        }
        _state.update { it.copy(templateText = newText) }
    }
    
    fun saveTemplate(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentState = _state.value
            
            // Validation
            if (currentState.title.isBlank()) {
                _state.update { it.copy(error = "Title is required") }
                return@launch
            }
            
            if (currentState.templateText.isBlank()) {
                _state.update { it.copy(error = "Template text is required") }
                return@launch
            }
            
            _state.update { it.copy(isSaving = true, error = null) }
            
            try {
                val template = MessageTemplate(
                    id = currentState.templateId,
                    title = currentState.title.trim(),
                    template = currentState.templateText.trim()
                )
                
                if (currentState.isEditMode) {
                    repository.updateTemplate(template)
                } else {
                    repository.addTemplate(template)
                }
                
                _state.update { it.copy(isSaving = false) }
                onSuccess()
                
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to save template"
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    fun getGuidelineText(): String {
        return TemplatePlaceholder.getGuidelineText()
    }
}

data class AddTemplateState(
    val templateId: String = "",
    val title: String = "",
    val templateText: String = "",
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

