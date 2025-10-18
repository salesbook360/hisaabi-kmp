package com.hisaabi.hisaabi_kmp.utils

/**
 * WasmJS implementation of String.format extension.
 * Provides basic formatting support for common format specifiers.
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
                        is Double -> {
                            val multiplier = Math.pow(10.0, precision.toDouble())
                            val rounded = Math.round(arg * multiplier) / multiplier
                            rounded.toString().let { str ->
                                val parts = str.split('.')
                                if (parts.size == 1) {
                                    "$str.${"0".repeat(precision)}"
                                } else {
                                    val decimals = parts[1].take(precision).padEnd(precision, '0')
                                    "${parts[0]}.$decimals"
                                }
                            }
                        }
                        is Float -> {
                            val multiplier = Math.pow(10.0, precision.toDouble())
                            val rounded = Math.round(arg.toDouble() * multiplier) / multiplier
                            rounded.toString().let { str ->
                                val parts = str.split('.')
                                if (parts.size == 1) {
                                    "$str.${"0".repeat(precision)}"
                                } else {
                                    val decimals = parts[1].take(precision).padEnd(precision, '0')
                                    "${parts[0]}.$decimals"
                                }
                            }
                        }
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

// WasmJS doesn't have Math class, so we need to use external declarations
private object Math {
    fun pow(base: Double, exponent: Double): Double {
        var result = 1.0
        val exp = exponent.toInt()
        repeat(exp) {
            result *= base
        }
        return result
    }
    
    fun round(value: Double): Double {
        val floor = kotlin.math.floor(value)
        return if (value - floor >= 0.5) floor + 1.0 else floor
    }
}

