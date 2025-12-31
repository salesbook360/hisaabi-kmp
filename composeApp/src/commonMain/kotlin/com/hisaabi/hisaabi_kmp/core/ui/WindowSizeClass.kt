package com.hisaabi.hisaabi_kmp.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Window width size classes following Material Design 3 guidelines.
 * Used to create adaptive layouts that respond to different screen sizes.
 * 
 * - COMPACT: Phones in portrait mode (< 600dp)
 * - MEDIUM: Tablets, foldables, small laptops (600dp - 840dp)
 * - EXPANDED: Desktop, large tablets, laptops (> 840dp)
 */
enum class WindowWidthSizeClass {
    COMPACT,
    MEDIUM,
    EXPANDED;
    
    companion object {
        /**
         * Calculate the WindowWidthSizeClass based on window width in dp
         */
        fun fromWidth(widthDp: Dp): WindowWidthSizeClass {
            return when {
                widthDp < 600.dp -> COMPACT
                widthDp < 840.dp -> MEDIUM
                else -> EXPANDED
            }
        }
    }
}

/**
 * Window height size classes for vertical layout adaptations.
 */
enum class WindowHeightSizeClass {
    COMPACT,    // < 480dp
    MEDIUM,     // 480dp - 900dp
    EXPANDED;   // > 900dp
    
    companion object {
        fun fromHeight(heightDp: Dp): WindowHeightSizeClass {
            return when {
                heightDp < 480.dp -> COMPACT
                heightDp < 900.dp -> MEDIUM
                else -> EXPANDED
            }
        }
    }
}

/**
 * Combined window size class containing both width and height classifications.
 */
data class WindowSizeClass(
    val widthSizeClass: WindowWidthSizeClass,
    val heightSizeClass: WindowHeightSizeClass
) {
    /**
     * Returns true if this is a desktop-like expanded layout
     */
    val isDesktop: Boolean
        get() = widthSizeClass == WindowWidthSizeClass.EXPANDED
    
    /**
     * Returns true if this is a tablet-like medium layout
     */
    val isTablet: Boolean
        get() = widthSizeClass == WindowWidthSizeClass.MEDIUM
    
    /**
     * Returns true if this is a phone-like compact layout
     */
    val isPhone: Boolean
        get() = widthSizeClass == WindowWidthSizeClass.COMPACT
    
    /**
     * Returns true if we should show side navigation (rail or drawer)
     */
    val shouldShowSideNavigation: Boolean
        get() = widthSizeClass != WindowWidthSizeClass.COMPACT
    
    /**
     * Returns true if we should show permanent navigation drawer
     */
    val shouldShowPermanentDrawer: Boolean
        get() = widthSizeClass == WindowWidthSizeClass.EXPANDED
    
    /**
     * Returns true if we should use list-detail (master-detail) layout
     */
    val shouldShowListDetail: Boolean
        get() = widthSizeClass == WindowWidthSizeClass.EXPANDED
    
    /**
     * Returns the recommended number of columns for a grid based on width
     */
    fun gridColumns(minItemWidth: Dp = 100.dp): Int {
        return when (widthSizeClass) {
            WindowWidthSizeClass.COMPACT -> 4
            WindowWidthSizeClass.MEDIUM -> 6
            WindowWidthSizeClass.EXPANDED -> 8
        }
    }
    
    /**
     * Returns recommended max content width for centered layouts
     */
    val maxContentWidth: Dp
        get() = when (widthSizeClass) {
            WindowWidthSizeClass.COMPACT -> Dp.Unspecified // Full width
            WindowWidthSizeClass.MEDIUM -> 840.dp
            WindowWidthSizeClass.EXPANDED -> 1200.dp
        }
}

