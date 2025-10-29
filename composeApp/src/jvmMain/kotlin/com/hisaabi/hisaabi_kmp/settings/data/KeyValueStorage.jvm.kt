package com.hisaabi.hisaabi_kmp.settings.data

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

/**
 * JVM implementation using Properties file
 */
class JVMKeyValueStorage : KeyValueStorage {
    private val properties = Properties()
    private val propertiesFile: File
    
    init {
        val userHome = System.getProperty("user.home")
        val appDir = File(userHome, ".hisaabi")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        propertiesFile = File(appDir, "preferences.properties")
        
        // Load existing properties if file exists
        if (propertiesFile.exists()) {
            FileInputStream(propertiesFile).use { input ->
                properties.load(input)
            }
        }
    }
    
    private fun save() {
        FileOutputStream(propertiesFile).use { output ->
            properties.store(output, "Hisaabi Preferences")
        }
    }
    
    override fun getString(key: String, defaultValue: String?): String? {
        return properties.getProperty(key, defaultValue)
    }
    
    override fun putString(key: String, value: String) {
        properties.setProperty(key, value)
        save()
    }
    
    override fun getLong(key: String, defaultValue: Long): Long {
        val value = properties.getProperty(key) ?: return defaultValue
        return value.toLongOrNull() ?: defaultValue
    }
    
    override fun putLong(key: String, value: Long) {
        properties.setProperty(key, value.toString())
        save()
    }
    
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val value = properties.getProperty(key) ?: return defaultValue
        return value.toBoolean()
    }
    
    override fun putBoolean(key: String, value: Boolean) {
        properties.setProperty(key, value.toString())
        save()
    }
    
    override fun getInt(key: String, defaultValue: Int): Int {
        val value = properties.getProperty(key) ?: return defaultValue
        return value.toIntOrNull() ?: defaultValue
    }
    
    override fun putInt(key: String, value: Int) {
        properties.setProperty(key, value.toString())
        save()
    }
    
    override fun remove(key: String) {
        properties.remove(key)
        save()
    }
    
    override fun clear() {
        properties.clear()
        save()
    }
    
    override fun contains(key: String): Boolean {
        return properties.containsKey(key)
    }
}

actual fun createKeyValueStorage(): KeyValueStorage {
    return JVMKeyValueStorage()
}

