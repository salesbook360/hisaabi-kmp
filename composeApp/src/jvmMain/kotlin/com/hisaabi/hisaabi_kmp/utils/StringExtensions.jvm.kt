package com.hisaabi.hisaabi_kmp.utils

/**
 * JVM implementation of String.format extension.
 * Uses Java's String.format() method.
 */
actual fun String.format(vararg args: Any?): String {
    return String.format(this, *args)
}

/**
 * JVM implementation of currentTimeMillis
 */
actual fun currentTimeMillis(): Long {
    return System.currentTimeMillis()
}

