package com.hisaabi.hisaabi_kmp.sync.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaabi.hisaabi_kmp.sync.domain.manager.SyncManager
import com.hisaabi.hisaabi_kmp.sync.domain.model.SyncState
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.seconds
import kotlin.time.times

/**
 * Sync status indicator component to show on home screen
 * Displays current sync status, progress, and last sync time
 */
@Composable
fun SyncStatusComponent(
    modifier: Modifier = Modifier,
    syncManager: SyncManager = koinInject()
) {
    val syncState by syncManager.syncState.collectAsState()
    val lastSyncTime by syncManager.lastSyncTime.collectAsState()
    val scope = rememberCoroutineScope()
    
    // Always show the sync component (removed AnimatedVisibility hiding on Idle)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                scope.launch {
                    syncManager.syncData()
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = when (syncState) {
                is SyncState.InProgress, is SyncState.Progress -> MaterialTheme.colorScheme.primaryContainer
                is SyncState.Success -> MaterialTheme.colorScheme.tertiaryContainer
                is SyncState.Error -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Icon
                    Icon(
                        imageVector = when (syncState) {
                            is SyncState.InProgress, is SyncState.Progress -> Icons.Default.Refresh
                            is SyncState.Success -> Icons.Default.Check
                            is SyncState.Error -> Icons.Default.Error
                            else -> Icons.Default.CloudOff
                        },
                        contentDescription = "Sync Status",
                        tint = when (syncState) {
                            is SyncState.InProgress, is SyncState.Progress -> MaterialTheme.colorScheme.primary
                            is SyncState.Success -> MaterialTheme.colorScheme.tertiary
                            is SyncState.Error -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    
                    // Status Text
                    Column {
                        Text(
                            text = when (syncState) {
                                is SyncState.Idle -> "Tap to sync"
                                is SyncState.InProgress -> "Syncing..."
                                is SyncState.Progress -> {
                                    val progress = (syncState as SyncState.Progress).progress
                                    "${progress.recordType}: ${progress.completed}/${progress.total}"
                                }
                                is SyncState.Success -> "Synced successfully"
                                is SyncState.Error -> "Sync Failed"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        // Last sync time or instruction
                        if (lastSyncTime != null) {
                            Text(
                                text = "Last synced ${formatLastSyncTime(lastSyncTime!!)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        } else {
                            Text(
                                text = "Never synced",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                
                // Progress or Action Button
                when (val state = syncState) {
                    is SyncState.Progress -> {
                        CircularProgressIndicator(
                            progress = { state.progress.percentage / 100f },
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )
                    }
                    is SyncState.InProgress -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )
                    }
                    is SyncState.Error -> {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    syncManager.syncData()
                                }
                            }
                        ) {
                            Text("Retry", fontSize = 12.sp)
                        }
                    }
                    is SyncState.Idle -> {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    else -> {
                        // Show nothing for other states
                    }
                }
            }
        }
    }


/**
 * Compact sync status indicator for toolbar/app bar
 */
@Composable
fun CompactSyncStatus(
    modifier: Modifier = Modifier,
    syncManager: SyncManager = koinInject()
) {
    val syncState by syncManager.syncState.collectAsState()
    val scope = rememberCoroutineScope()
    
    IconButton(
        onClick = {
            scope.launch {
                syncManager.syncData()
            }
        },
        modifier = modifier
    ) {
        Box {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Sync",
                tint = when (syncState) {
                    is SyncState.InProgress, is SyncState.Progress -> MaterialTheme.colorScheme.primary
                    is SyncState.Success -> MaterialTheme.colorScheme.tertiary
                    is SyncState.Error -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            
            // Progress indicator overlay
            if (syncState is SyncState.InProgress || syncState is SyncState.Progress) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

/**
 * Format last sync time as relative time
 */
private fun formatLastSyncTime(time: Instant): String {
    val now = Clock.System.now()
    val duration = now - time
    
    return when {
        duration < 1.seconds -> "Just now"
        duration < 60.seconds -> "${duration.inWholeSeconds}s ago"
        duration < 60 * 60.seconds -> "${duration.inWholeMinutes}m ago"
        duration < 24 * 60 * 60.seconds -> "${duration.inWholeHours}h ago"
        else -> "${duration.inWholeDays}d ago"
    }
}

/**
 * Floating sync button for manual sync trigger
 */
@Composable
fun SyncFab(
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    syncManager: SyncManager = koinInject()
) {
    val syncState by syncManager.syncState.collectAsState()
    val scope = rememberCoroutineScope()
    
    FloatingActionButton(
        onClick = {
            onClick()
            scope.launch {
                syncManager.syncData()
            }
        },
        modifier = modifier,
        containerColor = when (syncState) {
            is SyncState.InProgress, is SyncState.Progress -> MaterialTheme.colorScheme.primaryContainer
            is SyncState.Error -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.secondaryContainer
        }
    ) {
        if (syncState is SyncState.InProgress || syncState is SyncState.Progress) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 3.dp
            )
        } else {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Sync"
            )
        }
    }
}

