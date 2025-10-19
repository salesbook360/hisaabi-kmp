package com.hisaabi.hisaabi_kmp.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.settings.domain.model.ReceiptConfig
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReceiptSettingsViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(ReceiptSettingsState())
    val state: StateFlow<ReceiptSettingsState> = _state.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            preferencesManager.receiptConfig.collect { config ->
                _state.update { it.copy(config = config, isLoading = false) }
            }
        }
    }
    
    fun updateConfig(config: ReceiptConfig) {
        _state.update { it.copy(config = config) }
    }
    
    fun saveSettings() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            
            try {
                preferencesManager.saveReceiptConfig(_state.value.config)
                
                _state.update { 
                    it.copy(
                        isSaving = false,
                        isSaved = true,
                        error = null
                    )
                }
                
                // Reset saved flag after a delay
                kotlinx.coroutines.delay(1000)
                _state.update { it.copy(isSaved = false) }
                
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to save settings"
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    fun resetToDefaults() {
        _state.update { it.copy(config = ReceiptConfig.DEFAULT) }
    }
}

data class ReceiptSettingsState(
    val config: ReceiptConfig = ReceiptConfig.DEFAULT,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

