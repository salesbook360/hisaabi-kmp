package com.hisaabi.hisaabi_kmp.auth.presentation

import androidx.compose.runtime.Composable

/**
 * WasmJS implementation - Google Sign-In not implemented for web
 */
@Composable
actual fun rememberGoogleSignInHelper(): GoogleSignInHelper? {
    return null
}


