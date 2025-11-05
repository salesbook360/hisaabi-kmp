package com.hisaabi.hisaabi_kmp.core.ui

import androidx.compose.runtime.Composable

/**
 * Multiplatform BackHandler that intercepts back button presses.
 * 
 * @param enabled Whether the back handler should be enabled
 * @param onBack Callback to be executed when back is pressed
 */
@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)

