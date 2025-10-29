package com.hisaabi.hisaabi_kmp.settings.data

import kotlinx.browser.localStorage

/**
 * WasmJS implementation using browser localStorage
 */
class WasmJsKeyValueStorage : KeyValueStorage {
    
    override fun getString(key: String, defaultValue: String?): String? {
        return localStorage.getItem(key) ?: defaultValue
    }
    
    override fun putString(key: String, value: String) {
        localStorage.setItem(key, value)
    }
    
    override fun getLong(key: String, defaultValue: Long): Long {
        val value = localStorage.getItem(key) ?: return defaultValue
        return value.toLongOrNull() ?: defaultValue
    }
    
    override fun putLong(key: String, value: Long) {
        localStorage.setItem(key, value.toString())
    }
    
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val value = localStorage.getItem(key) ?: return defaultValue
        return value.toBoolean()
    }
    
    override fun putBoolean(key: String, value: Boolean) {
        localStorage.setItem(key, value.toString())
    }
    
    override fun getInt(key: String, defaultValue: Int): Int {
        val value = localStorage.getItem(key) ?: return defaultValue
        return value.toIntOrNull() ?: defaultValue
    }
    
    override fun putInt(key: String, value: Int) {
        localStorage.setItem(key, value.toString())
    }
    
    override fun remove(key: String) {
        localStorage.removeItem(key)
    }
    
    override fun clear() {
        localStorage.clear()
    }
    
    override fun contains(key: String): Boolean {
        return localStorage.getItem(key) != null
    }
}

actual fun createKeyValueStorage(): KeyValueStorage {
    return WasmJsKeyValueStorage()
}

