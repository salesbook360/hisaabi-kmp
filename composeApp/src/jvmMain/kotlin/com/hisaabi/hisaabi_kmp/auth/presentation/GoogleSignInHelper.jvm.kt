package com.hisaabi.hisaabi_kmp.auth.presentation

/**
 * JVM implementation of getGoogleSignInHelper
 * Google Sign-In is not supported on Desktop
 */
actual fun getGoogleSignInHelper(): GoogleSignInHelper {
    // Return a no-op implementation for Desktop
    return object : GoogleSignInHelper {
        override fun signIn(onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
            onFailure("Google Sign-In is not supported on Desktop")
        }
    }
}

