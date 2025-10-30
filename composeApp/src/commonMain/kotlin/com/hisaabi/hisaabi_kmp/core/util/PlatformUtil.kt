package com.hisaabi.hisaabi_kmp.core.util

import com.hisaabi.hisaabi_kmp.Platform
import com.hisaabi.hisaabi_kmp.getPlatform

/**
 * Utility object for platform-related operations.
 */
object PlatformUtil {
    
    /**
     * Returns the device slug based on the current platform.
     * - A: Android
     * - I: iOS
     * - W: Web (Wasm)
     * - D: Desktop (JVM)
     */
    fun getDeviceSlug(): String {
        val platform = getPlatform()
        return when (platform::class.simpleName) {
            "AndroidPlatform" -> "A"
            "IOSPlatform" -> "I"
            "WasmPlatform" -> "W"
            "JVMPlatform" -> "D"
            else -> "U" // Unknown platform
        }
    }
}

