package com.hisaabi.hisaabi_kmp.settings.data

import com.hisaabi.hisaabi_kmp.settings.domain.model.Currency
import com.hisaabi.hisaabi_kmp.settings.domain.model.DashboardConfig
import com.hisaabi.hisaabi_kmp.settings.domain.model.ReceiptConfig
import com.hisaabi.hisaabi_kmp.settings.domain.model.TransactionSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Preferences manager with platform-specific persistent storage
 * Uses SharedPreferences (Android), UserDefaults (iOS), localStorage (WasmJS), or Properties file (JVM)
 */
class PreferencesManager(
    private val storage: KeyValueStorage = createKeyValueStorage()
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    // State flows with data loaded from storage
    private val _transactionSettings = MutableStateFlow(loadTransactionSettings())
    val transactionSettings: Flow<TransactionSettings> = _transactionSettings.asStateFlow()
    
    private val _receiptConfig = MutableStateFlow(loadReceiptConfig())
    val receiptConfig: Flow<ReceiptConfig> = _receiptConfig.asStateFlow()
    
    private val _dashboardConfig = MutableStateFlow(loadDashboardConfig())
    val dashboardConfig: Flow<DashboardConfig> = _dashboardConfig.asStateFlow()
    
    // App-level settings
    private val _biometricAuthEnabled = MutableStateFlow(loadBiometricAuthEnabled())
    val biometricAuthEnabled: Flow<Boolean> = _biometricAuthEnabled.asStateFlow()
    
    private val _selectedLanguage = MutableStateFlow(loadSelectedLanguage())
    val selectedLanguage: Flow<Language> = _selectedLanguage.asStateFlow()
    
    private val _selectedCurrency = MutableStateFlow(loadSelectedCurrency())
    val selectedCurrency: Flow<Currency> = _selectedCurrency.asStateFlow()
    
    // Generic key-value storage for sync and other features
    private val _longPreferences = mutableMapOf<String, MutableStateFlow<Long>>()
    
    fun getLong(key: String, default: Long): Long {
        return storage.getLong(key, default)
    }
    
    fun setLong(key: String, value: Long) {
        storage.putLong(key, value)
        if (_longPreferences.containsKey(key)) {
            _longPreferences[key]?.value = value
        } else {
            _longPreferences[key] = MutableStateFlow(value)
        }
    }
    
    fun observeLong(key: String, default: Long): Flow<Long> {
        if (!_longPreferences.containsKey(key)) {
            val initialValue = storage.getLong(key, default)
            _longPreferences[key] = MutableStateFlow(initialValue)
        }
        return _longPreferences[key]!!.asStateFlow()
    }
    
    // ========== Load Methods ==========
    
    private fun loadTransactionSettings(): TransactionSettings {
        val json = storage.getString(KEY_TRANSACTION_SETTINGS) ?: return TransactionSettings.DEFAULT
        return try {
            this.json.decodeFromString<TransactionSettings>(json)
        } catch (e: Exception) {
            TransactionSettings.DEFAULT
        }
    }
    
    private fun loadReceiptConfig(): ReceiptConfig {
        val json = storage.getString(KEY_RECEIPT_CONFIG) ?: return ReceiptConfig.DEFAULT
        return try {
            this.json.decodeFromString<ReceiptConfig>(json)
        } catch (e: Exception) {
            ReceiptConfig.DEFAULT
        }
    }
    
    private fun loadDashboardConfig(): DashboardConfig {
        val json = storage.getString(KEY_DASHBOARD_CONFIG) ?: return DashboardConfig.DEFAULT
        return try {
            this.json.decodeFromString<DashboardConfig>(json)
        } catch (e: Exception) {
            DashboardConfig.DEFAULT
        }
    }
    
    private fun loadBiometricAuthEnabled(): Boolean {
        return storage.getBoolean(KEY_BIOMETRIC_AUTH, false)
    }
    
    private fun loadSelectedLanguage(): Language {
        val code = storage.getString(KEY_LANGUAGE, Language.ENGLISH.code) ?: Language.ENGLISH.code
        return Language.entries.find { it.code == code } ?: Language.ENGLISH
    }
    
    private fun loadSelectedCurrency(): Currency {
        val code = storage.getString(KEY_CURRENCY, Currency.PKR.code) ?: Currency.PKR.code
        return Currency.findByCode(code) ?: Currency.PKR
    }
    
    // ========== Transaction Settings Methods ==========
    
    fun getTransactionSettings(): TransactionSettings {
        return _transactionSettings.value
    }
    
    fun saveTransactionSettings(settings: TransactionSettings) {
        _transactionSettings.value = settings
        val jsonString = json.encodeToString(settings)
        storage.putString(KEY_TRANSACTION_SETTINGS, jsonString)
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
    
    // ========== Receipt Config Methods ==========
    
    fun getReceiptConfig(): ReceiptConfig {
        return _receiptConfig.value
    }
    
    fun saveReceiptConfig(config: ReceiptConfig) {
        _receiptConfig.value = config
        val jsonString = json.encodeToString(config)
        storage.putString(KEY_RECEIPT_CONFIG, jsonString)
    }
    
    fun updateReceiptConfig(update: (ReceiptConfig) -> ReceiptConfig) {
        val updated = update(_receiptConfig.value)
        saveReceiptConfig(updated)
    }
    
    // ========== Dashboard Config Methods ==========
    
    fun getDashboardConfig(): DashboardConfig {
        return _dashboardConfig.value
    }
    
    fun saveDashboardConfig(config: DashboardConfig) {
        _dashboardConfig.value = config
        val jsonString = json.encodeToString(config)
        storage.putString(KEY_DASHBOARD_CONFIG, jsonString)
    }
    
    fun updateDashboardConfig(update: (DashboardConfig) -> DashboardConfig) {
        val updated = update(_dashboardConfig.value)
        saveDashboardConfig(updated)
    }
    
    // ========== Biometric Authentication Methods ==========
    
    fun getBiometricAuthEnabled(): Boolean {
        return _biometricAuthEnabled.value
    }
    
    fun setBiometricAuthEnabled(enabled: Boolean) {
        _biometricAuthEnabled.value = enabled
        storage.putBoolean(KEY_BIOMETRIC_AUTH, enabled)
    }
    
    // ========== Language Methods ==========
    
    fun getSelectedLanguage(): Language {
        return _selectedLanguage.value
    }
    
    fun setSelectedLanguage(language: Language) {
        _selectedLanguage.value = language
        storage.putString(KEY_LANGUAGE, language.code)
        // TODO: Apply language change to app
    }
    
    // ========== Currency Methods ==========
    
    fun getSelectedCurrency(): Currency {
        return _selectedCurrency.value
    }
    
    fun setSelectedCurrency(currency: Currency) {
        _selectedCurrency.value = currency
        storage.putString(KEY_CURRENCY, currency.code)
    }
    
    // ========== Last Selected Warehouse Methods ==========
    
    /**
     * Get the last selected warehouse slug for transactions.
     * Returns null if no warehouse has been selected before.
     */
    fun getLastSelectedWarehouseSlug(): String? {
        return storage.getString(KEY_LAST_WAREHOUSE_SLUG)
    }
    
    /**
     * Save the last selected warehouse slug for transactions.
     * This is used to pre-populate the warehouse selection for speedy transactions.
     */
    fun setLastSelectedWarehouseSlug(warehouseSlug: String) {
        storage.putString(KEY_LAST_WAREHOUSE_SLUG, warehouseSlug)
    }
    
    // ========== Storage Keys ==========
    
    companion object {
        private const val KEY_TRANSACTION_SETTINGS = "transaction_settings"
        private const val KEY_RECEIPT_CONFIG = "receipt_config"
        private const val KEY_DASHBOARD_CONFIG = "dashboard_config"
        private const val KEY_BIOMETRIC_AUTH = "biometric_auth_enabled"
        private const val KEY_LANGUAGE = "selected_language"
        private const val KEY_CURRENCY = "selected_currency"
        private const val KEY_LAST_WAREHOUSE_SLUG = "last_selected_warehouse_slug"
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

