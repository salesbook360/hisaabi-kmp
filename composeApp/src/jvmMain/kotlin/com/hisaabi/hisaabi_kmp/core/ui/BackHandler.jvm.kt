package com.hisaabi.hisaabi_kmp.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import java.awt.event.WindowEvent
import java.awt.event.WindowAdapter

/**
 * JVM/Desktop implementation of BackHandler.
 * On desktop, there's no standard back button, so this is a no-op.
 * Desktop apps typically use window close handlers or explicit navigation buttons.
 */
@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on Desktop
    // Desktop applications don't have a back button
    // Users typically use explicit navigation buttons or window controls
}

