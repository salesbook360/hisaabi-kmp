# Sync Module Implementation

## Overview
The Sync module provides offline-first data synchronization between the local database and cloud backend. It ensures users can work offline and automatically syncs changes when internet connectivity is available.

## Architecture

### Module Structure
```
sync/
├── data/
│   ├── datasource/
│   │   ├── SyncRemoteDataSource.kt      # API calls for sync
│   │   └── SyncPreferencesDataSource.kt # Last sync time tracking
│   ├── mapper/
│   │   └── SyncMappers.kt               # Entity ↔ DTO conversions
│   ├── model/
│   │   ├── SyncDtos.kt                  # API data models
│   │   ├── SyncRequest.kt               # Generic sync request
│   │   └── SyncResponse.kt              # Generic sync response
│   └── repository/
│       └── SyncRepository.kt            # Sync operations coordinator
├── domain/
│   ├── model/
│   │   ├── SyncConfig.kt                # Configuration constants
│   │   ├── SyncProgress.kt              # Progress tracking
│   │   ├── SyncState.kt                 # Sync state sealed class
│   │   └── SyncStatus.kt                # Entity sync status enum
│   └── manager/
│       └── SyncManager.kt               # Business logic coordinator
└── di/
    └── SyncModule.kt                    # Dependency injection setup
```

## Key Features

### 1. Bidirectional Sync
- **Sync Up**: Local changes → Cloud
- **Sync Down**: Cloud changes → Local

### 2. Business-Scoped Sync
- All sync operations are scoped to the selected business
- Uses `AppSessionManager` to get current business slug
- Only syncs data for the active business

### 3. Sync Status Tracking
Each entity has a `sync_status` field with three states:
- `NONE (1)`: Not synced yet (new local record)
- `SYNCED (2)`: Successfully synced with cloud
- `UPDATED (3)`: Updated locally after sync (needs re-sync)

### 4. Last Sync Time
- Tracks last successful sync time per user-business combination
- Only fetches records updated since last sync (efficient)
- Stored in preferences: `last_sync_time_{userSlug}_{businessSlug}`

### 5. Automatic Sync Interval
- Configurable sync interval (default: 2 minutes)
- Prevents excessive API calls
- Can be adjusted via `SyncConfig.SYNC_INTERVAL`

### 6. Background Sync
- Continues syncing even when app is in background
- Uses platform-specific background workers
- Survives app restarts

### 7. Progress Tracking
- Real-time sync progress updates
- Tracks which entity types are being synced
- Progress percentage calculation
- UI can observe sync state via Flow

## Usage

### Manual Sync
```kotlin
// Inject SyncManager
val syncManager: SyncManager

// Trigger sync
syncManager.syncData(
    syncUp = true,    // Sync local changes to cloud
    syncDown = true   // Sync cloud changes to local
)

// Observe sync state
syncManager.syncState.collect { state ->
    when (state) {
        is SyncState.Idle -> // Not syncing
        is SyncState.InProgress -> // Sync started
        is SyncState.Progress -> // Show progress
        is SyncState.Success -> // Sync completed
        is SyncState.Error -> // Handle error
    }
}
```

### Automatic Background Sync
```kotlin
// Start background sync worker
syncManager.startBackgroundSync()

// Stop background sync
syncManager.stopBackgroundSync()
```

### Check Sync Status
```kotlin
// Check if sync is needed
val shouldSync = syncManager.shouldSync()

// Get last sync time
val lastSyncTime = syncManager.getLastSyncTime()
```

## Sync Flow

### Sync Up Flow
1. Query local database for records with `sync_status != SYNCED`
2. Convert entities to DTOs
3. Send to API in batches
4. Update `sync_status` to `SYNCED` on success
5. Update last sync time

### Sync Down Flow
1. Get last sync time for current business
2. Request records updated after last sync time
3. Convert DTOs to entities
4. Insert/update in local database
5. Mark as `SYNCED`
6. Update last sync time

### Sync Order
**Sync Up** (10 entity types):
1. Media
2. Categories
3. Payment Methods
4. Quantity Units
5. Warehouses
6. Products
7. Recipe Ingredients
8. Parties (Customers/Suppliers)
9. Transactions
10. Deleted Records

**Sync Down** (12 entity types):
1. Media
2. Categories
3. Payment Methods
4. Products
5. Recipe Ingredients
6. Product Quantities
7. Parties
8. Quantity Units
9. Warehouses
10. Transactions
11. Transaction Details
12. Deleted Records

## Configuration

### Sync Interval
```kotlin
// In SyncConfig.kt
val SYNC_INTERVAL: Duration = 2.minutes  // Change as needed
```

### Batch Size
```kotlin
// In SyncConfig.kt
const val SYNC_BATCH_SIZE = 50  // Records per API call
```

## Error Handling

### Network Errors
- Retry logic for transient failures
- Graceful degradation on network unavailable
- User-friendly error messages

### Conflict Resolution
- Last-write-wins strategy
- Cloud data takes precedence on sync down
- Local unsync'd changes preserved

### Authentication
- Automatic token refresh via AuthInterceptor
- Sync pauses if user logs out
- Resumes after login

## Performance

### Optimizations
1. **Incremental Sync**: Only fetches changed records
2. **Batch Processing**: Processes records in batches
3. **Parallel Execution**: Syncs multiple entity types concurrently
4. **Database Transactions**: Atomic updates for consistency
5. **Efficient Queries**: Indexes on slug and sync_status fields

### Network Usage
- Minimal payload with only changed data
- Compression for large datasets
- Background sync during WiFi only (optional)

## Testing

### Manual Testing
1. Create data offline
2. Verify `sync_status = NONE`
3. Connect to internet
4. Trigger sync
5. Verify `sync_status = SYNCED`
6. Check data on another device

### Unit Tests
```kotlin
// Test sync up
@Test
fun `syncUp should update sync status on success`()

// Test sync down
@Test
fun `syncDown should insert new records from cloud`()

// Test sync interval
@Test
fun `should not sync before interval expires`()
```

## Troubleshooting

### Sync Not Working
1. Check internet connectivity
2. Verify user is logged in
3. Check business is selected
4. Review sync logs
5. Verify API endpoints

### Data Not Appearing
1. Check sync_status in database
2. Verify business_slug matches
3. Check last sync time
4. Review API response
5. Check for errors in logs

### Performance Issues
1. Reduce sync interval
2. Increase batch size
3. Optimize database queries
4. Use WiFi-only sync
5. Sync during idle time

## Future Enhancements

1. **Conflict Resolution UI**: Show conflicts to user
2. **Selective Sync**: Choose which entity types to sync
3. **Priority Sync**: Sync important data first
4. **Delta Sync**: Only sync changed fields
5. **Compression**: Reduce network usage
6. **Encryption**: Secure data in transit
7. **Offline Queue**: Queue operations for sync
8. **Sync Analytics**: Track sync performance

## API Endpoints

### Sync Up (POST)
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

### Sync Down (GET)
- `/sync-categories?last-sync-time=<timestamp>`
- `/sync-product?last-sync-time=<timestamp>`
- `/sync-person?last-sync-time=<timestamp>`
- `/sync-payment-method?last-sync-time=<timestamp>`
- `/sync-quantity-unit?last-sync-time=<timestamp>`
- `/sync-warehouse?last-sync-time=<timestamp>`
- `/sync-transaction?last-sync-time=<timestamp>`
- `/sync-transaction-detail?last-sync-time=<timestamp>`
- `/sync-product-quantities?last-sync-time=<timestamp>`
- `/sync-media?last-sync-time=<timestamp>`
- `/sync-recipe-ingredients?last-sync-time=<timestamp>`
- `/delete-records?last-sync-time=<timestamp>`

### Add New Records (POST)
- `/products`
- `/person`
- `/payment-method`
- `/transaction`
- `/recipe-ingredients`

### Delete Records (POST)
- `/delete-records`

## Dependencies

- `kotlinx-datetime`: For timestamp handling
- `kotlinx-coroutines`: For async operations
- `ktor-client`: For API calls
- `room`: For local database
- `koin`: For dependency injection

## Notes

- All timestamps are in UTC format
- Business slug is required for all operations
- User must be authenticated
- Sync is atomic per entity type
- Failed syncs don't affect other entity types

