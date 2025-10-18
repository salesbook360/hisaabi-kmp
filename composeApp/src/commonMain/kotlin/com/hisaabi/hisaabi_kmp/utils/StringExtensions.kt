package com.hisaabi.hisaabi_kmp.utils

/**
 * Extension function for String formatting in Kotlin Multiplatform.
 * Provides a platform-independent way to format strings with arguments.
 * 
 * Usage:
 * "Hello %s, you have %d messages".format("John", 5)
 * "Price: %.2f".format(19.99)
 * "Lat: %.6f, Long: %.6f".format(latitude, longitude)
 */
expect fun String.format(vararg args: Any?): String

