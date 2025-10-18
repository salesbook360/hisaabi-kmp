package com.hisaabi.hisaabi_kmp.utils

/**
 * Android implementation of String.format extension.
 * Uses Java's String.format() method.
 */
actual fun String.format(vararg args: Any?): String {
    return String.format(this, *args)
}

