package com.hisaabi.hisaabi_kmp.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Navigation item data class for adaptive navigation
 */
data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
    val badge: String? = null
)

/**
 * Adaptive navigation scaffold that switches between:
 * - Bottom Navigation Bar (Compact/Phone)
 * - Navigation Rail (Medium/Tablet)
 * - Permanent Navigation Drawer (Expanded/Desktop)
 */
@Composable
fun AdaptiveNavigationScaffold(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    appTitle: String = "Hisaabi",
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val windowSizeClass = LocalWindowSizeClass.current
    
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.COMPACT -> {
            // Phone: Bottom Navigation Bar
            Scaffold(
                modifier = modifier,
                topBar = topBar,
                bottomBar = {
                    NavigationBar {
                        items.forEachIndexed { index, item ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = if (selectedIndex == index) item.selectedIcon else item.icon,
                                        contentDescription = item.title
                                    )
                                },
                                label = { Text(item.title) },
                                selected = selectedIndex == index,
                                onClick = { onItemSelected(index) }
                            )
                        }
                    }
                },
                content = content
            )
        }
        
        WindowWidthSizeClass.MEDIUM -> {
            // Tablet: Navigation Rail
            Row(modifier = modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    header = {
                        Spacer(Modifier.height(12.dp))
                        // App logo/icon placeholder
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "H",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                ) {
                    items.forEachIndexed { index, item ->
                        NavigationRailItem(
                            icon = {
                                Icon(
                                    imageVector = if (selectedIndex == index) item.selectedIcon else item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title, fontSize = 11.sp) },
                            selected = selectedIndex == index,
                            onClick = { onItemSelected(index) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
                
                // Subtle divider
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                
                // Main content
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = topBar,
                    content = content
                )
            }
        }
        
        WindowWidthSizeClass.EXPANDED -> {
            // Desktop: Permanent Navigation Drawer
            PermanentNavigationDrawer(
                modifier = modifier,
                drawerContent = {
                    PermanentDrawerSheet(
                        modifier = Modifier.width(280.dp),
                        drawerContainerColor = MaterialTheme.colorScheme.surface,
                        drawerContentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        // Drawer Header
                        DrawerHeader(appTitle = appTitle)
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        
                        Spacer(Modifier.height(12.dp))
                        
                        // Navigation Items
                        items.forEachIndexed { index, item ->
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        imageVector = if (selectedIndex == index) item.selectedIcon else item.icon,
                                        contentDescription = item.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (selectedIndex == index) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                },
                                badge = item.badge?.let {
                                    {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                selected = selectedIndex == index,
                                onClick = { onItemSelected(index) },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedContainerColor = Color.Transparent,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        
                        Spacer(Modifier.weight(1f))
                        
                        // Footer
                        DrawerFooter()
                    }
                }
            ) {
                // Main content area
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = topBar,
                    content = content
                )
            }
        }
    }
}

/**
 * Drawer header with app branding
 */
@Composable
private fun DrawerHeader(appTitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // App Logo
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "H",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        // App Title
        Text(
            text = appTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "Business Management",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Drawer footer with version info
 */
@Composable
private fun DrawerFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(
            modifier = Modifier.padding(bottom = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Text(
            text = "Version 1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Simple navigation type enum for conditional logic
 */
enum class NavigationType {
    BOTTOM_NAVIGATION,
    NAVIGATION_RAIL,
    PERMANENT_NAVIGATION_DRAWER
}

/**
 * Get the current navigation type based on window size
 */
@Composable
fun currentNavigationType(): NavigationType {
    val windowSizeClass = LocalWindowSizeClass.current
    return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.COMPACT -> NavigationType.BOTTOM_NAVIGATION
        WindowWidthSizeClass.MEDIUM -> NavigationType.NAVIGATION_RAIL
        WindowWidthSizeClass.EXPANDED -> NavigationType.PERMANENT_NAVIGATION_DRAWER
    }
}

