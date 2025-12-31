package com.hisaabi.hisaabi_kmp.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A container that centers content and limits max width on larger screens.
 * Prevents content from stretching too wide on desktop displays.
 * 
 * @param maxWidth Maximum width of the content. Defaults to the recommended width based on window size.
 * @param horizontalPadding Padding on the sides when content is at max width.
 * @param modifier Modifier for the container.
 * @param contentAlignment Alignment of content within the container.
 * @param content The content to display.
 */
@Composable
fun ResponsiveContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = LocalWindowSizeClass.current.maxContentWidth,
    horizontalPadding: Dp = 16.dp,
    contentAlignment: Alignment = Alignment.TopCenter,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = contentAlignment
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (maxWidth != Dp.Unspecified) {
                        Modifier.widthIn(max = maxWidth)
                    } else {
                        Modifier
                    }
                )
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            content = content
        )
    }
}

/**
 * A responsive column that constrains its max width on larger screens.
 * Use this for scrollable content like lists and forms.
 */
@Composable
fun ResponsiveColumn(
    modifier: Modifier = Modifier,
    maxWidth: Dp = LocalWindowSizeClass.current.maxContentWidth,
    horizontalPadding: Dp = 16.dp,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = modifier
                .then(
                    if (maxWidth != Dp.Unspecified) {
                        Modifier.widthIn(max = maxWidth)
                    } else {
                        Modifier
                    }
                )
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            content = content
        )
    }
}

/**
 * A row that adapts between horizontal (desktop) and vertical (mobile) layouts.
 * On compact screens, items stack vertically. On larger screens, items are side by side.
 * 
 * @param modifier Modifier for the layout.
 * @param forceVertical Force vertical layout regardless of screen size.
 * @param spacing Spacing between items.
 * @param horizontalContent Content to show when using horizontal layout (Row).
 * @param verticalContent Content to show when using vertical layout (Column).
 */
@Composable
fun AdaptiveRow(
    modifier: Modifier = Modifier,
    forceVertical: Boolean = false,
    spacing: Dp = 16.dp,
    horizontalContent: @Composable RowScope.() -> Unit,
    verticalContent: @Composable ColumnScope.() -> Unit = { }
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val useHorizontal = !forceVertical && windowSizeClass.widthSizeClass != WindowWidthSizeClass.COMPACT
    
    if (useHorizontal) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.Top,
            content = horizontalContent
        )
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing),
            horizontalAlignment = Alignment.Start,
            content = verticalContent
        )
    }
}

/**
 * Simpler adaptive row that uses the same content lambda for both modes.
 * Content items should use fillMaxWidth() to expand properly in vertical mode.
 */
@Composable
fun AdaptiveRowSimple(
    modifier: Modifier = Modifier,
    forceVertical: Boolean = false,
    spacing: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val useHorizontal = !forceVertical && windowSizeClass.widthSizeClass != WindowWidthSizeClass.COMPACT
    
    if (useHorizontal) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.Top
        ) {
            content()
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing),
            horizontalAlignment = Alignment.Start
        ) {
            content()
        }
    }
}

/**
 * Check if current layout should be horizontal (desktop/tablet)
 */
@Composable
fun isHorizontalLayout(): Boolean {
    val windowSizeClass = LocalWindowSizeClass.current
    return windowSizeClass.widthSizeClass != WindowWidthSizeClass.COMPACT
}

/**
 * Returns adaptive GridCells based on the current window size.
 * - Compact: Fixed columns (typically 4 for menu grids)
 * - Medium: More fixed columns (6)
 * - Expanded: Adaptive based on minimum item width
 */
@Composable
fun adaptiveGridCells(
    compactColumns: Int = 4,
    mediumColumns: Int = 6,
    expandedMinItemWidth: Dp = 100.dp
): GridCells {
    val windowSizeClass = LocalWindowSizeClass.current
    
    return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.COMPACT -> GridCells.Fixed(compactColumns)
        WindowWidthSizeClass.MEDIUM -> GridCells.Fixed(mediumColumns)
        WindowWidthSizeClass.EXPANDED -> GridCells.Adaptive(minSize = expandedMinItemWidth)
    }
}

/**
 * Returns the recommended number of columns for a multi-column layout.
 * Useful for dashboard cards, form layouts, etc.
 */
@Composable
fun adaptiveColumnCount(): Int {
    val windowSizeClass = LocalWindowSizeClass.current
    
    return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.COMPACT -> 1
        WindowWidthSizeClass.MEDIUM -> 2
        WindowWidthSizeClass.EXPANDED -> 2 // Could be 3 for very wide content
    }
}

/**
 * A composable that renders content in either a single column or multiple columns
 * based on the screen size. Useful for dashboards and settings screens.
 * 
 * @param items List of composable items to distribute across columns.
 * @param modifier Modifier for the container.
 * @param spacing Spacing between items and columns.
 */
@Composable
fun <T> AdaptiveColumnLayout(
    items: List<T>,
    modifier: Modifier = Modifier,
    spacing: Dp = 16.dp,
    itemContent: @Composable (T) -> Unit
) {
    val columnCount = adaptiveColumnCount()
    
    if (columnCount == 1) {
        // Single column layout
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            items.forEach { item ->
                itemContent(item)
            }
        }
    } else {
        // Multi-column layout
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            // Distribute items across columns
            val columns = items.chunked((items.size + columnCount - 1) / columnCount)
            
            columns.forEachIndexed { index, columnItems ->
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    columnItems.forEach { item ->
                        itemContent(item)
                    }
                }
            }
            
            // Add empty columns if needed to maintain equal width
            repeat(columnCount - columns.size) {
                Box(modifier = Modifier.weight(1f))
            }
        }
    }
}

/**
 * Spacing values that adapt to screen size.
 * Larger screens get more generous spacing.
 */
object AdaptiveSpacing {
    @Composable
    fun small(): Dp {
        return when (LocalWindowSizeClass.current.widthSizeClass) {
            WindowWidthSizeClass.COMPACT -> 8.dp
            WindowWidthSizeClass.MEDIUM -> 12.dp
            WindowWidthSizeClass.EXPANDED -> 16.dp
        }
    }
    
    @Composable
    fun medium(): Dp {
        return when (LocalWindowSizeClass.current.widthSizeClass) {
            WindowWidthSizeClass.COMPACT -> 12.dp
            WindowWidthSizeClass.MEDIUM -> 16.dp
            WindowWidthSizeClass.EXPANDED -> 24.dp
        }
    }
    
    @Composable
    fun large(): Dp {
        return when (LocalWindowSizeClass.current.widthSizeClass) {
            WindowWidthSizeClass.COMPACT -> 16.dp
            WindowWidthSizeClass.MEDIUM -> 24.dp
            WindowWidthSizeClass.EXPANDED -> 32.dp
        }
    }
}

