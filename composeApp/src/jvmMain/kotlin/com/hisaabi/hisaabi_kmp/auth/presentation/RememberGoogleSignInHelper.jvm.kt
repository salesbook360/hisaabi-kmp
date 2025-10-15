package com.hisaabi.hisaabi_kmp.auth.presentation

import androidx.compose.runtime.Composable

/**
 * JVM implementation - Google Sign-In not implemented for desktop
 */
@Composable
actual fun rememberGoogleSignInHelper(): GoogleSignInHelper? {
    return null
}


