package com.hisaabi.hisaabi_kmp.core.ui

import androidx.compose.runtime.Composable

/**
 * iOS implementation of BackHandler.
 * iOS uses swipe gestures for navigation, so this is a no-op.
 * Navigation is typically handled by UINavigationController.
 */
@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on iOS
    // iOS navigation is handled by the system with swipe gestures
}

