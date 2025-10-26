# Status Enum Centralization Summary

## Overview
Centralized all status enums across the application to use consistent values: **Active = 0, Deleted = 2**

## Changes Made

### 1. Created Centralized Status Enum
**File:** `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/common/Status.kt`
- Created base `Status` enum with `ACTIVE(0)` and `DELETED(2)`
- Provides centralized status values for all entities

### 2. Updated Existing Status Enums

#### ProductStatus
**File:** `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/products/domain/model/Product.kt`
- Updated from: `ACTIVE(1), INACTIVE(2), DELETED(3)`
- Updated to: `ACTIVE(0), DELETED(2)`
- Removed `INACTIVE` status
- Updated default `statusId` from `1` to `0`
- Added `isActive` property

#### WarehouseStatus
**File:** `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/warehouses/domain/model/Warehouse.kt`
- Updated from: `ACTIVE(1), INACTIVE(2), DELETED(3)`
- Updated to: `ACTIVE(0), DELETED(2)`
- Removed `INACTIVE` status
- Updated default `statusId` from `1` to `0`
- Updated `isActive` to check for `statusId == 0`

#### PaymentMethodStatus
**File:** `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/paymentmethods/domain/model/PaymentMethod.kt`
- Updated from: `ACTIVE(1), INACTIVE(2), DELETED(3)`
- Updated to: `ACTIVE(0), DELETED(2)`
- Removed `INACTIVE` status
- Updated default `statusId` from `1` to `0`
- Updated `isActive` to check for `statusId == 0`

### 3. Created New Status Enums

#### QuantityUnitStatus
**File:** `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/quantityunits/domain/model/QuantityUnit.kt`
- Created new enum: `ACTIVE(0), DELETED(2)`
- Updated default `statusId` from `1` to `0`
- Updated `isActive` to check for `statusId == 0`
- Added `quantityUnitStatus` property

#### TransactionStatus
**File:** `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/transactions/domain/model/Transaction.kt`
- Created new enum: `ACTIVE(0), DELETED(2)`
- Updated default `statusId` from `1` to `0`
- Added `transactionStatus` and `isActive` properties

#### PartyStatus
**File:** `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/parties/domain/model/Party.kt`
- Created new enum: `ACTIVE(0), DELETED(2)`
- Updated default `personStatus` from `1` to `0`
- Added `partyStatus` and `isActive` properties

### 4. Updated Database Entities

#### InventoryTransactionEntity
**File:** `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/database/entity/InventoryTransactionEntity.kt`
- Updated default `status_id` from `1` to `0`

#### PartyEntity
**File:** `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/database/entity/PartyEntity.kt`
- Updated default `person_status` from `1` to `0`

### 5. Updated DAO Queries

#### InventoryTransactionDao
**File:** `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/database/dao/InventoryTransactionDao.kt`
- Updated all queries from `status_id != 3` to `status_id != 2` (5 occurrences)

#### PaymentMethodDao
**File:** `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/database/dao/PaymentMethodDao.kt`
- Updated query from `status_id != 3` to `status_id != 2`

### 6. Updated Repositories

#### WarehousesRepository
**File:** `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/warehouses/data/repository/WarehousesRepository.kt`
- Updated filter from `status_id == 1` to `status_id == 0`

#### QuantityUnitsRepository
**File:** `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/quantityunits/data/repository/QuantityUnitsRepository.kt`
- Updated filter from `status_id == 1` to `status_id == 0`

### 7. Updated Use Cases

Updated statusId from `1` to `0` in:
- `AddWarehouseUseCase.kt`
- `AddQuantityUnitUseCase.kt`
- `AddPaymentMethodUseCase.kt`
- `AddProductUseCase.kt`

### 8. Updated Sync Mappers

**File:** `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/sync/data/mapper/SyncMappers.kt`
- Updated `status_id` from `1` to `0`

### 9. Updated TransactionsRepository

**File:** `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/transactions/data/repository/TransactionsRepository.kt`
- Fixed incorrect `statusId = 2` to `statusId = 0` (3 occurrences)
- This was incorrectly using status field for transaction state

## Status Value Mapping

| Old Value | Old Meaning | New Value | New Meaning |
|-----------|------------|-----------|--------------|
| 1 | Active | 0 | Active |
| 2 | Inactive/Completed | 2 | Deleted |
| 3 | Deleted | - | Removed |

## Benefits

1. **Consistency**: All entities now use the same status values
2. **Centralization**: Single source of truth for status values
3. **Simplicity**: Removed unnecessary "Inactive" status
4. **Clarity**: Clear distinction between Active (0) and Deleted (2)
5. **Type Safety**: Each entity has its own status enum with proper type safety

## Entities Affected

1. ✅ Product
2. ✅ Warehouse
3. ✅ PaymentMethod
4. ✅ QuantityUnit
5. ✅ Transaction
6. ✅ Party

## Notes

- The old "Inactive" status (value 2) has been removed - entities are either Active (0) or Deleted (2)
- All database entities now default to `status_id = 0` (Active)
- All queries now filter for `status_id != 2` instead of `status_id != 3`
- Transaction state (Pending, Completed, Cancelled) is separate from transaction status (Active, Deleted)

