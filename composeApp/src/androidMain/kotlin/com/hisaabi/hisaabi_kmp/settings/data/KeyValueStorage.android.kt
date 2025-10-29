package com.hisaabi.hisaabi_kmp.settings.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Android implementation using SharedPreferences
 */
class AndroidKeyValueStorage(context: Context) : KeyValueStorage {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    override fun getString(key: String, defaultValue: String?): String? {
        return prefs.getString(key, defaultValue)
    }
    
    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
    
    override fun getLong(key: String, defaultValue: Long): Long {
        return prefs.getLong(key, defaultValue)
    }
    
    override fun putLong(key: String, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }
    
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }
    
    override fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
    
    override fun getInt(key: String, defaultValue: Int): Int {
        return prefs.getInt(key, defaultValue)
    }
    
    override fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }
    
    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
    
    override fun clear() {
        prefs.edit().clear().apply()
    }
    
    override fun contains(key: String): Boolean {
        return prefs.contains(key)
    }
    
    companion object {
        private const val PREFS_NAME = "hisaabi_preferences"
    }
}

// Global storage instance
private var storageInstance: KeyValueStorage? = null

actual fun createKeyValueStorage(): KeyValueStorage {
    if (storageInstance == null) {
        throw IllegalStateException(
            "KeyValueStorage not initialized. Call initKeyValueStorage(context) first."
        )
    }
    return storageInstance!!
}

/**
 * Initialize storage with Android context
 * Should be called from Application.onCreate() or MainActivity
 */
fun initKeyValueStorage(context: Context) {
    if (storageInstance == null) {
        storageInstance = AndroidKeyValueStorage(context.applicationContext)
    }
}

