package com.hisaabi.hisaabi_kmp.auth.presentation

import androidx.compose.runtime.Composable

/**
 * Composable expect function to get platform-specific GoogleSignInHelper
 * Returns null on platforms where Google Sign-In is not implemented
 */
@Composable
expect fun rememberGoogleSignInHelper(): GoogleSignInHelper?


