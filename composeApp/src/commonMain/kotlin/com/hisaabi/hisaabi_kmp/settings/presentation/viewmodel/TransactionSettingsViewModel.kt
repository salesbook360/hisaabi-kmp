package com.hisaabi.hisaabi_kmp.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.settings.domain.model.TransactionSettings
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransactionSettingsViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(TransactionSettingsState())
    val state: StateFlow<TransactionSettingsState> = _state.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            preferencesManager.transactionSettings.collect { settings ->
                _state.update { it.copy(settings = settings, isLoading = false) }
            }
        }
    }
    
    fun updateSettings(settings: TransactionSettings) {
        _state.update { it.copy(settings = settings) }
    }
    
    fun saveSettings() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            
            try {
                // Validate: At least one of cash in/out or products must be enabled
                if (!_state.value.settings.isCashInOutEnabled && !_state.value.settings.isProductsEnabled) {
                    _state.update { 
                        it.copy(
                            isSaving = false,
                            error = "Please enable at least Cash In/Out or Products"
                        )
                    }
                    return@launch
                }
                
                preferencesManager.saveTransactionSettings(_state.value.settings)
                
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
        _state.update { it.copy(settings = TransactionSettings.DEFAULT) }
    }
}

data class TransactionSettingsState(
    val settings: TransactionSettings = TransactionSettings.DEFAULT,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

