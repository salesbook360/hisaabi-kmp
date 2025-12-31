package com.hisaabi.hisaabi_kmp.core.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * CompositionLocal to provide WindowSizeClass throughout the app.
 * Default is COMPACT for safety, but should be properly provided at the app root.
 */
val LocalWindowSizeClass = compositionLocalOf {
    WindowSizeClass(
        widthSizeClass = WindowWidthSizeClass.COMPACT,
        heightSizeClass = WindowHeightSizeClass.MEDIUM
    )
}

/**
 * Provider composable that calculates and provides WindowSizeClass to its content.
 * Should be used at the root of the app to make window size information available everywhere.
 * 
 * Usage:
 * ```
 * ProvideWindowSizeClass {
 *     val windowSizeClass = LocalWindowSizeClass.current
 *     // Use windowSizeClass.isDesktop, windowSizeClass.shouldShowSideNavigation, etc.
 * }
 * ```
 */
@Composable
fun ProvideWindowSizeClass(
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        
        // Convert constraints to dp
        val widthDp = with(density) { maxWidth }
        val heightDp = with(density) { maxHeight }
        
        val windowSizeClass = remember(widthDp, heightDp) {
            WindowSizeClass(
                widthSizeClass = WindowWidthSizeClass.fromWidth(widthDp),
                heightSizeClass = WindowHeightSizeClass.fromHeight(heightDp)
            )
        }
        
        CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
            content()
        }
    }
}

/**
 * Convenience composable to get current WindowSizeClass.
 * Must be used within a ProvideWindowSizeClass block.
 */
@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    return LocalWindowSizeClass.current
}

