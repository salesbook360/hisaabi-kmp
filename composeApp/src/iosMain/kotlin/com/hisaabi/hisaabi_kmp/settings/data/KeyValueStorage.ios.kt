package com.hisaabi.hisaabi_kmp.settings.data

import platform.Foundation.NSUserDefaults

/**
 * iOS implementation using NSUserDefaults
 */
class IOSKeyValueStorage : KeyValueStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    
    override fun getString(key: String, defaultValue: String?): String? {
        return userDefaults.stringForKey(key) ?: defaultValue
    }
    
    override fun putString(key: String, value: String) {
        userDefaults.setObject(value, forKey = key)
        userDefaults.synchronize()
    }
    
    override fun getLong(key: String, defaultValue: Long): Long {
        if (userDefaults.objectForKey(key) == null) {
            return defaultValue
        }
        return userDefaults.integerForKey(key)
    }
    
    override fun putLong(key: String, value: Long) {
        userDefaults.setInteger(value, forKey = key)
        userDefaults.synchronize()
    }
    
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        if (userDefaults.objectForKey(key) == null) {
            return defaultValue
        }
        return userDefaults.boolForKey(key)
    }
    
    override fun putBoolean(key: String, value: Boolean) {
        userDefaults.setBool(value, forKey = key)
        userDefaults.synchronize()
    }
    
    override fun getInt(key: String, defaultValue: Int): Int {
        if (userDefaults.objectForKey(key) == null) {
            return defaultValue
        }
        return userDefaults.integerForKey(key).toInt()
    }
    
    override fun putInt(key: String, value: Int) {
        userDefaults.setInteger(value.toLong(), forKey = key)
        userDefaults.synchronize()
    }
    
    override fun remove(key: String) {
        userDefaults.removeObjectForKey(key)
        userDefaults.synchronize()
    }
    
    override fun clear() {
        val appDomain = platform.Foundation.NSBundle.mainBundle.bundleIdentifier
        if (appDomain != null) {
            userDefaults.removePersistentDomainForName(appDomain)
            userDefaults.synchronize()
        }
    }
    
    override fun contains(key: String): Boolean {
        return userDefaults.objectForKey(key) != null
    }
}

actual fun createKeyValueStorage(): KeyValueStorage {
    return IOSKeyValueStorage()
}

