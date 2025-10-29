package com.hisaabi.hisaabi_kmp.settings.data

/**
 * Platform-specific key-value storage interface
 * Implementations:
 * - Android: SharedPreferences
 * - iOS: UserDefaults
 * - JVM: Properties file
 * - WasmJS: localStorage
 */
interface KeyValueStorage {
    fun getString(key: String, defaultValue: String? = null): String?
    fun putString(key: String, value: String)
    
    fun getLong(key: String, defaultValue: Long = 0L): Long
    fun putLong(key: String, value: Long)
    
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    fun putBoolean(key: String, value: Boolean)
    
    fun getInt(key: String, defaultValue: Int = 0): Int
    fun putInt(key: String, value: Int)
    
    fun remove(key: String)
    fun clear()
    
    fun contains(key: String): Boolean
}

/**
 * Factory function to create platform-specific storage instance
 */
expect fun createKeyValueStorage(): KeyValueStorage

