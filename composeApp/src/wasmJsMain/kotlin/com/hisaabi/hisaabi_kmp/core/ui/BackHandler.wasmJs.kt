package com.hisaabi.hisaabi_kmp.core.ui

import androidx.compose.runtime.Composable

/**
 * Wasm/JS implementation of BackHandler.
 * Web apps could use browser history API, but for now this is a no-op.
 */
@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on Web
    // Could potentially integrate with browser history API in the future
}

