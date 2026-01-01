package com.hisaabi.hisaabi_kmp.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * An adaptive form container that:
 * - On mobile: Single column layout
 * - On tablet/desktop: Multi-column layout with sections side by side
 * 
 * Use with FormSection components inside.
 */
@Composable
fun AdaptiveFormContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 1200.dp,
    content: @Composable AdaptiveFormScope.() -> Unit
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val isExpandedLayout = windowSizeClass.widthSizeClass == WindowWidthSizeClass.EXPANDED
    val isMediumLayout = windowSizeClass.widthSizeClass == WindowWidthSizeClass.MEDIUM
    
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .fillMaxWidth()
                .padding(horizontal = when {
                    isExpandedLayout -> 24.dp
                    isMediumLayout -> 16.dp
                    else -> 0.dp
                })
        ) {
            val scope = AdaptiveFormScopeImpl(
                isExpanded = isExpandedLayout,
                isMedium = isMediumLayout
            )
            scope.content()
        }
    }
}

/**
 * Scope for adaptive form content
 */
interface AdaptiveFormScope {
    val isExpanded: Boolean
    val isMedium: Boolean
    
    /**
     * Create a form row that arranges its children horizontally on large screens
     */
    @Composable
    fun FormRow(
        modifier: Modifier = Modifier,
        spacing: Dp = 16.dp,
        content: @Composable () -> Unit
    )
    
    /**
     * Create two columns side by side on larger screens, stacked on mobile
     */
    @Composable
    fun TwoColumnLayout(
        leftContent: @Composable () -> Unit,
        rightContent: @Composable () -> Unit,
        modifier: Modifier = Modifier,
        spacing: Dp = 24.dp,
        leftWeight: Float = 1f,
        rightWeight: Float = 1f
    )
    
    /**
     * Create a form field that takes half width on large screens.
     * Must be used inside a Row context.
     */
    @Composable
    fun RowScope.HalfWidthField(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    )
}

private class AdaptiveFormScopeImpl(
    override val isExpanded: Boolean,
    override val isMedium: Boolean
) : AdaptiveFormScope {
    
    @Composable
    override fun FormRow(
        modifier: Modifier,
        spacing: Dp,
        content: @Composable () -> Unit
    ) {
        if (isExpanded || isMedium) {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                content()
            }
        } else {
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                content()
            }
        }
    }
    
    @Composable
    override fun TwoColumnLayout(
        leftContent: @Composable () -> Unit,
        rightContent: @Composable () -> Unit,
        modifier: Modifier,
        spacing: Dp,
        leftWeight: Float,
        rightWeight: Float
    ) {
        if (isExpanded) {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                Box(modifier = Modifier.weight(leftWeight)) {
                    leftContent()
                }
                Box(modifier = Modifier.weight(rightWeight)) {
                    rightContent()
                }
            }
        } else {
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                leftContent()
                rightContent()
            }
        }
    }
    
    @Composable
    override fun RowScope.HalfWidthField(
        modifier: Modifier,
        content: @Composable () -> Unit
    ) {
        if (isExpanded || isMedium) {
            Box(modifier = modifier.weight(1f)) {
                content()
            }
        } else {
            Box(modifier = modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

/**
 * A form section with a title and content.
 * Can be styled as a card on desktop.
 */
@Composable
fun FormSection(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    useCard: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val isDesktop = windowSizeClass.widthSizeClass == WindowWidthSizeClass.EXPANDED
    
    if (useCard && isDesktop) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                subtitle?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                content()
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                content()
            }
        }
    }
}

/**
 * Spacer that adapts size based on screen width
 */
@Composable
fun AdaptiveFormSpacer() {
    val windowSizeClass = LocalWindowSizeClass.current
    val height = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.COMPACT -> 16.dp
        WindowWidthSizeClass.MEDIUM -> 20.dp
        WindowWidthSizeClass.EXPANDED -> 24.dp
    }
    Spacer(modifier = Modifier.height(height))
}

/**
 * A divider with adaptive padding
 */
@Composable
fun AdaptiveFormDivider(
    modifier: Modifier = Modifier
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val verticalPadding = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.COMPACT -> 16.dp
        WindowWidthSizeClass.MEDIUM -> 20.dp
        WindowWidthSizeClass.EXPANDED -> 24.dp
    }
    
    HorizontalDivider(
        modifier = modifier.padding(vertical = verticalPadding),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

