package com.hisaabi.hisaabi_kmp.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A data table header row for desktop layouts
 */
@Composable
fun DataTableHeader(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * A header cell for the data table
 */
@Composable
fun RowScope.DataTableHeaderCell(
    text: String,
    weight: Float = 1f,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    Box(
        modifier = Modifier.weight(weight),
        contentAlignment = when (alignment) {
            Alignment.Start -> Alignment.CenterStart
            Alignment.End -> Alignment.CenterEnd
            else -> Alignment.Center
        }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * A data table row for desktop layouts
 */
@Composable
fun DataTableRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        Color.Transparent
    }
    
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * A cell for the data table row
 */
@Composable
fun RowScope.DataTableCell(
    weight: Float = 1f,
    alignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.weight(weight),
        contentAlignment = when (alignment) {
            Alignment.Start -> Alignment.CenterStart
            Alignment.End -> Alignment.CenterEnd
            else -> Alignment.Center
        }
    ) {
        content()
    }
}

/**
 * A text cell for simple text content
 */
@Composable
fun RowScope.DataTableTextCell(
    text: String,
    weight: Float = 1f,
    alignment: Alignment.Horizontal = Alignment.Start,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    DataTableCell(weight = weight, alignment = alignment) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = fontWeight,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Check if data table layout should be used
 */
@Composable
fun shouldUseDataTable(): Boolean {
    val windowSizeClass = LocalWindowSizeClass.current
    return windowSizeClass.widthSizeClass == WindowWidthSizeClass.EXPANDED
}

/**
 * Adaptive list that shows as cards on mobile and table on desktop
 */
@Composable
fun <T> AdaptiveList(
    items: List<T>,
    modifier: Modifier = Modifier,
    headerContent: (@Composable RowScope.() -> Unit)? = null,
    mobileItemContent: @Composable (T, Boolean) -> Unit,
    desktopRowContent: @Composable RowScope.(T, Boolean) -> Unit,
    selectedItem: T? = null,
    onItemClick: (T) -> Unit = {}
) {
    val useTable = shouldUseDataTable()
    
    if (useTable && headerContent != null) {
        Column(modifier = modifier) {
            // Table header
            DataTableHeader {
                headerContent()
            }
            
            // Table rows
            items.forEach { item ->
                val isSelected = item == selectedItem
                DataTableRow(
                    onClick = { onItemClick(item) },
                    selected = isSelected
                ) {
                    desktopRowContent(item, isSelected)
                }
            }
        }
    } else {
        // Mobile card list
        Column(modifier = modifier) {
            items.forEach { item ->
                val isSelected = item == selectedItem
                mobileItemContent(item, isSelected)
            }
        }
    }
}

