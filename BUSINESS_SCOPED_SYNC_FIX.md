# Business-Scoped Sync Fix

## Problem
The sync mechanism was syncing data across ALL businesses instead of only for the currently selected business. This could lead to:
- Data from multiple businesses being mixed up
- Unnecessary network traffic
- Potential data integrity issues
- Privacy concerns

## Solution
Updated all DAO queries and repository methods to filter sync operations by `business_slug`.

## Changes Made

### 1. LocalDataSource Files (7 files)
Updated all `getUnsynced*` methods in LocalDataSource files to accept `businessSlug` parameter:

**Files Updated:**
- `CategoryLocalDataSource.kt`
- `PartyLocalDataSource.kt`
- `PaymentMethodLocalDataSource.kt`
- `QuantityUnitLocalDataSource.kt`
- `WareHouseLocalDataSource.kt`
- `InventoryTransactionLocalDataSource.kt`
- `TransactionDetailLocalDataSource.kt`

**Example Change:**
```kotlin
// Before (Interface)
suspend fun getUnsyncedCategories(): List<CategoryEntity>

// After (Interface)
suspend fun getUnsyncedCategories(businessSlug: String): List<CategoryEntity>

// Before (Implementation)
override suspend fun getUnsyncedCategories(): List<CategoryEntity> = 
    categoryDao.getUnsyncedCategories()

// After (Implementation)
override suspend fun getUnsyncedCategories(businessSlug: String): List<CategoryEntity> = 
    categoryDao.getUnsyncedCategories(businessSlug)
```

### 2. DAO Queries (12 files)
All `getUnsynced*` methods now accept a `businessSlug` parameter and filter by both `sync_status` and `business_slug`:

**Files Updated:**
- `CategoryDao.kt`
- `ProductDao.kt`
- `PartyDao.kt`
- `PaymentMethodDao.kt`
- `QuantityUnitDao.kt`
- `WareHouseDao.kt`
- `InventoryTransactionDao.kt`
- `TransactionDetailDao.kt`
- `ProductQuantitiesDao.kt`
- `EntityMediaDao.kt`
- `RecipeIngredientsDao.kt`
- `DeletedRecordsDao.kt`

**Example Change:**
```kotlin
// Before
@Query("SELECT * FROM Category WHERE sync_status != 2")
suspend fun getUnsyncedCategories(): List<CategoryEntity>

// After
@Query("SELECT * FROM Category WHERE sync_status != 2 AND business_slug = :businessSlug")
suspend fun getUnsyncedCategories(businessSlug: String): List<CategoryEntity>
```

### 3. SyncRepository (1 file)
Updated all `syncUp` methods in `SyncRepository.kt` to pass the `businessSlug` to DAO methods.

**Example Change:**
```kotlin
// Before
val unsynced = categoryDao.getUnsyncedCategories()

// After
val unsynced = categoryDao.getUnsyncedCategories(businessSlug)
```

**Methods Updated:**
- `syncCategoriesUp()`
- `syncProductsUp()`
- `syncPartiesUp()`
- `syncPaymentMethodsUp()`
- `syncQuantityUnitsUp()`
- `syncWarehousesUp()`
- `syncTransactionsUp()`
- `syncTransactionDetailsUp()`
- `syncProductQuantitiesUp()`
- `syncMediaUp()`
- `syncDeletedRecordsUp()`

## How It Works

1. **Business Selection**: The `AppSessionManager` provides the current business slug via `getBusinessSlug()`
2. **Sync Up**: When syncing up, the repository fetches unsynced records ONLY for the selected business
3. **Sync Down**: Downloaded records are stored with the business slug for proper isolation
4. **Data Integrity**: Each business's data remains isolated and syncs independently

## Testing Checklist

- [ ] Create records in Business A
- [ ] Switch to Business B
- [ ] Create records in Business B
- [ ] Verify sync only sends Business B records to the server
- [ ] Verify Business A records are NOT synced when Business B is selected
- [ ] Switch back to Business A
- [ ] Verify sync now sends Business A records

## Impact
- ✅ Data isolation between businesses
- ✅ Improved sync performance (fewer records to process)
- ✅ Better privacy and data integrity
- ✅ Reduced network bandwidth usage

## Summary of Updates
- **7 LocalDataSource files** - Updated interface and implementation signatures
- **12 DAO files** - Updated queries with business_slug filter
- **1 SyncRepository file** - Updated to pass business_slug to DAO methods

## Related Files
- `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/core/session/AppSessionManager.kt` - Provides business slug
- `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/database/datasource/*.kt` - Updated LocalDataSource interfaces
- `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/database/dao/*.kt` - Updated queries
- `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/sync/data/repository/SyncRepository.kt` - Updated sync logic

