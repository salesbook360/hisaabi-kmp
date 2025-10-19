package com.hisaabi.hisaabi_kmp.settings.data

import com.hisaabi.hisaabi_kmp.settings.domain.model.DashboardConfig
import com.hisaabi.hisaabi_kmp.settings.domain.model.ReceiptConfig
import com.hisaabi.hisaabi_kmp.settings.domain.model.TransactionSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Simple in-memory preferences manager for Transaction Settings, Receipt Config, Dashboard Config, and App Settings
 * TODO: Replace with platform-specific persistent storage (SharedPreferences/UserDefaults/localStorage)
 */
class PreferencesManager {
    private val _transactionSettings = MutableStateFlow(TransactionSettings.DEFAULT)
    val transactionSettings: Flow<TransactionSettings> = _transactionSettings.asStateFlow()
    
    private val _receiptConfig = MutableStateFlow(ReceiptConfig.DEFAULT)
    val receiptConfig: Flow<ReceiptConfig> = _receiptConfig.asStateFlow()
    
    private val _dashboardConfig = MutableStateFlow(DashboardConfig.DEFAULT)
    val dashboardConfig: Flow<DashboardConfig> = _dashboardConfig.asStateFlow()
    
    // App-level settings
    private val _biometricAuthEnabled = MutableStateFlow(false)
    val biometricAuthEnabled: Flow<Boolean> = _biometricAuthEnabled.asStateFlow()
    
    private val _selectedLanguage = MutableStateFlow(Language.ENGLISH)
    val selectedLanguage: Flow<Language> = _selectedLanguage.asStateFlow()
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    fun getTransactionSettings(): TransactionSettings {
        return _transactionSettings.value
    }
    
    fun saveTransactionSettings(settings: TransactionSettings) {
        _transactionSettings.value = settings
        // TODO: Persist to platform-specific storage
        // For now, just keeping in memory
    }
    
    fun updateTransactionSettings(update: (TransactionSettings) -> TransactionSettings) {
        val updated = update(_transactionSettings.value)
        saveTransactionSettings(updated)
    }
    
    // Helper methods for specific settings
    fun toggleCashInOut(enabled: Boolean) {
        updateTransactionSettings { it.copy(isCashInOutEnabled = enabled) }
    }
    
    fun toggleCustomers(enabled: Boolean) {
        updateTransactionSettings { it.copy(isCustomersEnabled = enabled) }
    }
    
    fun toggleProducts(enabled: Boolean) {
        updateTransactionSettings { it.copy(isProductsEnabled = enabled) }
    }
    
    fun setDecimalPlacesInAmount(places: Int) {
        updateTransactionSettings { it.copy(decimalPlacesInAmount = places.coerceIn(0, 4)) }
    }
    
    fun setDecimalPlacesInQuantity(places: Int) {
        updateTransactionSettings { it.copy(decimalPlacesInQuantity = places.coerceIn(0, 4)) }
    }
    
    fun resetToDefaults() {
        saveTransactionSettings(TransactionSettings.DEFAULT)
    }
    
    // Receipt Config Methods
    fun getReceiptConfig(): ReceiptConfig {
        return _receiptConfig.value
    }
    
    fun saveReceiptConfig(config: ReceiptConfig) {
        _receiptConfig.value = config
        // TODO: Persist to platform-specific storage
    }
    
    fun updateReceiptConfig(update: (ReceiptConfig) -> ReceiptConfig) {
        val updated = update(_receiptConfig.value)
        saveReceiptConfig(updated)
    }
    
    // Dashboard Config Methods
    fun getDashboardConfig(): DashboardConfig {
        return _dashboardConfig.value
    }
    
    fun saveDashboardConfig(config: DashboardConfig) {
        _dashboardConfig.value = config
        // TODO: Persist to platform-specific storage
    }
    
    fun updateDashboardConfig(update: (DashboardConfig) -> DashboardConfig) {
        val updated = update(_dashboardConfig.value)
        saveDashboardConfig(updated)
    }
    
    // Biometric Authentication Methods
    fun getBiometricAuthEnabled(): Boolean {
        return _biometricAuthEnabled.value
    }
    
    fun setBiometricAuthEnabled(enabled: Boolean) {
        _biometricAuthEnabled.value = enabled
        // TODO: Persist to platform-specific storage
    }
    
    // Language Methods
    fun getSelectedLanguage(): Language {
        return _selectedLanguage.value
    }
    
    fun setSelectedLanguage(language: Language) {
        _selectedLanguage.value = language
        // TODO: Persist to platform-specific storage
        // TODO: Apply language change to app
    }
}

/**
 * Supported languages in the app
 */
enum class Language(val displayName: String, val code: String) {
    ENGLISH("English", "en"),
    URDU("Urdu", "ur"),
    ARABIC("Arabic", "ar")
}

