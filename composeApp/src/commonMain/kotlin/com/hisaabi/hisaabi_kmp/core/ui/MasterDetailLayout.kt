package com.hisaabi.hisaabi_kmp.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Master-Detail layout that adapts between:
 * - Mobile: Full-screen master or detail view with navigation
 * - Tablet: Side-by-side with narrow master list
 * - Desktop: Side-by-side with proper proportions and detail panel
 * 
 * @param showDetail Whether to show the detail view (on mobile, replaces master)
 * @param masterContent The list/master content
 * @param detailContent The detail content when an item is selected
 * @param emptyDetailContent Content to show when no item is selected (desktop only)
 * @param modifier Modifier for the layout
 */
@Composable
fun MasterDetailLayout(
    showDetail: Boolean,
    masterContent: @Composable () -> Unit,
    detailContent: @Composable () -> Unit,
    emptyDetailContent: @Composable () -> Unit = { DefaultEmptyDetailContent() },
    modifier: Modifier = Modifier
) {
    val windowSizeClass = LocalWindowSizeClass.current
    
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.COMPACT -> {
            // Mobile: Show either master OR detail, full screen
            AnimatedContent(
                targetState = showDetail,
                modifier = modifier.fillMaxSize(),
                transitionSpec = {
                    if (targetState) {
                        // Going to detail
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        // Going back to master
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "MasterDetailAnimation"
            ) { isDetailVisible ->
                if (isDetailVisible) {
                    detailContent()
                } else {
                    masterContent()
                }
            }
        }
        
        WindowWidthSizeClass.MEDIUM -> {
            // Tablet: Side by side with 40/60 split
            Row(modifier = modifier.fillMaxSize()) {
                // Master panel - narrower on tablet
                Box(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                ) {
                    masterContent()
                }
                
                // Divider
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                
                // Detail panel
                Box(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    if (showDetail) {
                        detailContent()
                    } else {
                        emptyDetailContent()
                    }
                }
            }
        }
        
        WindowWidthSizeClass.EXPANDED -> {
            // Desktop: Side by side with better proportions
            Row(modifier = modifier.fillMaxSize()) {
                // Master panel - fixed width or proportional
                Box(
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxHeight()
                ) {
                    masterContent()
                }
                
                // Divider
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                
                // Detail panel with card styling
                Box(
                    modifier = Modifier
                        .weight(0.65f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(16.dp)
                ) {
                    if (showDetail) {
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            detailContent()
                        }
                    } else {
                        emptyDetailContent()
                    }
                }
            }
        }
    }
}

/**
 * Default empty state for the detail panel
 */
@Composable
private fun DefaultEmptyDetailContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Select an item",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                text = "Choose an item from the list to view details",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Check if the current screen should use master-detail layout
 */
@Composable
fun shouldUseMasterDetail(): Boolean {
    val windowSizeClass = LocalWindowSizeClass.current
    return windowSizeClass.widthSizeClass != WindowWidthSizeClass.COMPACT
}

/**
 * A list item that's optimized for master-detail layouts.
 * Shows selection state on larger screens.
 */
@Composable
fun MasterListItem(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val useMasterDetail = windowSizeClass.widthSizeClass != WindowWidthSizeClass.COMPACT
    
    val backgroundColor = if (useMasterDetail && selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    } else {
        Color.Transparent
    }
    
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor,
        shape = if (useMasterDetail) RoundedCornerShape(8.dp) else RoundedCornerShape(0.dp)
    ) {
        content()
    }
}

