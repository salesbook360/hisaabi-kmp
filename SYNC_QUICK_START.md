# Sync Module - Quick Start Guide

## Overview
The Sync module is now fully implemented and ready to use. This guide will help you get started quickly.

## Basic Usage

### 1. Automatic Background Sync

The simplest way to enable sync is to start background sync when the app launches:

```kotlin
// In your App initialization (e.g., App.kt or MainActivity)
@Composable
fun App() {
    val syncManager: SyncManager = koinInject()
    
    // Start background sync on app launch
    LaunchedEffect(Unit) {
        syncManager.startBackgroundSync()
    }
    
    // Your app content...
}
```

This will automatically sync every 2 minutes (configurable in `SyncConfig.SYNC_INTERVAL`).

### 2. Manual Sync Trigger

Users can manually trigger sync by:

**Option A: Using the Dashboard**
- The sync status component on the dashboard is already clickable
- Tapping it will trigger a manual sync

**Option B: Programmatic Trigger**
```kotlin
val syncManager: SyncManager = koinInject()
val scope = rememberCoroutineScope()

Button(onClick = {
    scope.launch {
        syncManager.syncData(
            syncUp = true,    // Sync local changes to cloud
            syncDown = true   // Sync cloud changes to local
        )
    }
}) {
    Text("Sync Now")
}
```

### 3. Observe Sync Status

The sync status is automatically displayed on the Dashboard screen. If you want to show it elsewhere:

```kotlin
import com.hisaabi.hisaabi_kmp.sync.presentation.SyncStatusComponent
import com.hisaabi.hisaabi_kmp.sync.presentation.CompactSyncStatus

@Composable
fun MyScreen() {
    Column {
        // Full sync status card
        SyncStatusComponent()
        
        // OR compact version for toolbar
        TopAppBar(
            title = { Text("My Screen") },
            actions = {
                CompactSyncStatus()
            }
        )
    }
}
```

### 4. Custom Sync Behavior

```kotlin
val syncManager: SyncManager = koinInject()

// Only sync up (local to cloud)
syncManager.syncData(syncUp = true, syncDown = false)

// Only sync down (cloud to local)
syncManager.syncData(syncUp = false, syncDown = true)

// Check if sync is needed
val shouldSync = syncManager.shouldSync()
if (shouldSync) {
    syncManager.syncData()
}

// Get last sync time
val lastSyncTime = syncManager.getLastSyncTime()
```

## Configuration

### Change Sync Interval

Edit `SyncConfig.kt`:
```kotlin
object SyncConfig {
    val SYNC_INTERVAL: Duration = 5.minutes  // Changed from 2 minutes
}
```

### Change Batch Size

Edit `SyncConfig.kt`:
```kotlin
object SyncConfig {
    const val SYNC_BATCH_SIZE = 100  // Changed from 50
}
```

## How It Works

### When Data is Created/Updated Locally

1. Data is saved to local database with `sync_status = 1` (NONE)
2. Next sync will detect this record and sync it to cloud
3. After successful sync, `sync_status` is updated to `2` (SYNCED)

### When Data is Updated After Sync

1. Update the entity and set `sync_status = 3` (UPDATED)
2. Next sync will detect this and re-sync to cloud
3. After successful sync, `sync_status` is updated to `2` (SYNCED)

Example:
```kotlin
// When creating a new category
val category = CategoryEntity(
    title = "My Category",
    business_slug = businessSlug,
    sync_status = 1  // NONE - needs to be synced
)
categoryDao.insertCategory(category)

// When updating an existing category
val updated = existingCategory.copy(
    title = "Updated Title",
    sync_status = 3  // UPDATED - needs to be re-synced
)
categoryDao.updateCategory(updated)
```

### When Cloud Data is Synced Down

1. Sync manager requests records updated after last sync time
2. Cloud returns new/updated records
3. Records are inserted/updated in local database with `sync_status = 2` (SYNCED)

## Testing

### Test Offline Support

1. **Create data offline:**
   ```kotlin
   // Disable WiFi/mobile data
   // Create a category, product, or transaction
   // Verify it appears in the app
   ```

2. **Sync when online:**
   ```kotlin
   // Enable WiFi/mobile data
   // Open the app
   // Wait for automatic sync or tap the sync status
   // Verify data syncs to cloud
   ```

3. **Verify on another device:**
   ```kotlin
   // Open app on another device
   // Login with same user
   // Select same business
   // Trigger sync
   // Verify data appears
   ```

### Test Sync Status

1. **Check NONE status:**
   ```sql
   SELECT * FROM Category WHERE sync_status = 1
   ```

2. **Check SYNCED status:**
   ```sql
   SELECT * FROM Category WHERE sync_status = 2
   ```

3. **Check UPDATED status:**
   ```sql
   SELECT * FROM Category WHERE sync_status = 3
   ```

## Troubleshooting

### Sync Not Working

1. **Check internet connectivity**
   ```kotlin
   // Make sure device has internet
   ```

2. **Check user is logged in**
   ```kotlin
   val session = sessionManager.getSessionContext()
   println("User: ${session.userSlug}, Business: ${session.businessSlug}")
   ```

3. **Check sync status**
   ```kotlin
   val syncState = syncManager.syncState.value
   println("Sync state: $syncState")
   ```

4. **Check last sync time**
   ```kotlin
   val lastSync = syncManager.getLastSyncTime()
   println("Last sync: $lastSync")
   ```

### Data Not Appearing

1. **Verify business slug matches:**
   ```sql
   SELECT * FROM Category WHERE business_slug = 'your-business-slug'
   ```

2. **Check sync status:**
   ```sql
   SELECT sync_status, COUNT(*) FROM Category GROUP BY sync_status
   ```

3. **Review sync logs:**
   ```kotlin
   // Check console for sync errors
   ```

### Performance Issues

1. **Increase sync interval:**
   ```kotlin
   val SYNC_INTERVAL: Duration = 5.minutes  // Instead of 2 minutes
   ```

2. **Increase batch size:**
   ```kotlin
   const val SYNC_BATCH_SIZE = 100  // Instead of 50
   ```

3. **Disable background sync:**
   ```kotlin
   syncManager.stopBackgroundSync()
   ```

## API Endpoints

All sync APIs are accessed via `SyncRemoteDataSource`:

**Base URL:** `http://52.20.167.4:5000`

**Sync Up (POST):**
- `/sync-categories`
- `/sync-product`
- `/sync-person`
- `/sync-payment-method`
- `/sync-quantity-unit`
- `/sync-warehouse`
- `/sync-transaction`
- `/sync-transaction-detail`
- `/sync-product-quantities`
- `/sync-media`

**Sync Down (GET with query param `last-sync-time`):**
- `/sync-categories?last-sync-time=<ISO-8601-timestamp>`
- `/sync-product?last-sync-time=<timestamp>`
- And so on...

## Important Notes

1. **Business Scoped**: All sync operations are scoped to the selected business
2. **User Scoped**: Last sync time is tracked per user-business combination
3. **Automatic Retry**: Sync will automatically retry on failure
4. **Background Sync**: Continues even when app is in background
5. **No Data Loss**: Offline data is preserved and synced when online
6. **Efficient**: Only syncs changed records, not entire database

## Next Steps

1. ✅ Start background sync on app launch
2. ✅ Test offline data creation
3. ✅ Test online sync
4. ✅ Verify data on multiple devices
5. ✅ Monitor sync status in production
6. ✅ Adjust sync interval based on usage patterns

## Support

For issues or questions:
1. Check `SYNC_MODULE_README.md` for detailed documentation
2. Review `SYNC_IMPLEMENTATION_SUMMARY.md` for technical details
3. Look at sync logs in console
4. Check database sync_status values

---

**Happy Syncing! 🚀**

