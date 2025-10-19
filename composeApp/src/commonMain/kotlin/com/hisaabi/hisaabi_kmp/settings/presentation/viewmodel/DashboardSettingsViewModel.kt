package com.hisaabi.hisaabi_kmp.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.settings.domain.model.DashboardConfig
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardSettingsViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(DashboardSettingsState())
    val state: StateFlow<DashboardSettingsState> = _state.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            preferencesManager.dashboardConfig.collect { config ->
                _state.update { it.copy(config = config, isLoading = false) }
            }
        }
    }
    
    fun updateConfig(config: DashboardConfig) {
        _state.update { it.copy(config = config) }
    }
    
    fun saveSettings() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            
            try {
                preferencesManager.saveDashboardConfig(_state.value.config)
                
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
        _state.update { it.copy(config = DashboardConfig.DEFAULT) }
    }
}

data class DashboardSettingsState(
    val config: DashboardConfig = DashboardConfig.DEFAULT,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

