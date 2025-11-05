package com.hisaabi.hisaabi_kmp.utils

import platform.Foundation.NSString
import platform.Foundation.stringWithFormat
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970


/**
 * iOS implementation of String.format extension.
 * Uses NSString's stringWithFormat method.
 */
actual fun String.format(vararg args: Any?): String {
    var result = this
    var argIndex = 0
    
    // Simple implementation that handles common format specifiers
    val formatPattern = Regex("""%([-#+ 0,(]*)(\d+)?(\.\d+)?([diouxXeEfFgGaAcspn%])""")
    
    result = formatPattern.replace(result) { matchResult ->
        if (matchResult.value == "%%") {
            "%"
        } else if (argIndex < args.size) {
            val arg = args[argIndex++]
            when (matchResult.groupValues[4]) {
                "d", "i" -> {
                    when (arg) {
                        is Int -> arg.toString()
                        is Long -> arg.toString()
                        is Short -> arg.toString()
                        is Byte -> arg.toString()
                        else -> arg.toString()
                    }
                }
                "f", "F" -> {
                    val precision = matchResult.groupValues[3].removePrefix(".").toIntOrNull() ?: 6
                    when (arg) {
                        is Double -> "%.${precision}f".format(arg)
                        is Float -> "%.${precision}f".format(arg.toDouble())
                        else -> arg.toString()
                    }
                }
                "s" -> arg?.toString() ?: "null"
                else -> arg?.toString() ?: "null"
            }
        } else {
            matchResult.value
        }
    }
    
    return result
}

// Helper function for iOS native formatting
private fun String.format(value: Double): String {
    return NSString.stringWithFormat(this, value)
}

/**
 * iOS implementation of currentTimeMillis
 */
actual fun currentTimeMillis(): Long {
    val now = NSDate()
    return (now.timeIntervalSince1970 * 1000).toLong()
}
