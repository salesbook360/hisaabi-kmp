package com.hisaabi.hisaabi_kmp.templates.data

import com.hisaabi.hisaabi_kmp.templates.domain.model.MessageTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing message templates
 * Currently uses in-memory storage
 * TODO: Replace with platform-specific persistent storage
 */
class TemplatesRepository {
    
    private val _templates = MutableStateFlow(MessageTemplate.getDefaultTemplates())
    val templates: Flow<List<MessageTemplate>> = _templates.asStateFlow()
    
    fun getTemplates(): List<MessageTemplate> {
        return _templates.value
    }
    
    fun getTemplateById(id: String): MessageTemplate? {
        return _templates.value.find { it.id == id }
    }
    
    fun addTemplate(template: MessageTemplate) {
        val currentList = _templates.value.toMutableList()
        val newId = (currentList.size + 1).toString()
        val newTemplate = template.copy(id = newId)
        currentList.add(newTemplate)
        _templates.value = currentList
    }
    
    fun updateTemplate(template: MessageTemplate) {
        val currentList = _templates.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == template.id }
        if (index != -1) {
            currentList[index] = template
            _templates.value = currentList
        }
    }
    
    fun deleteTemplate(id: String) {
        val currentList = _templates.value.toMutableList()
        currentList.removeAll { it.id == id }
        _templates.value = currentList
    }
    
    fun resetToDefaults() {
        _templates.value = MessageTemplate.getDefaultTemplates()
    }
}

