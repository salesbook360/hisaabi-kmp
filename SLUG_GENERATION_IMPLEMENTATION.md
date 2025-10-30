# Centralized Slug Generation Implementation

## Overview
Implemented a centralized slug generation system that creates consistent slugs across all entities using the format:
`"$businessSlug-$activeUserSlug-$ACTIVE_DEVICE_SLUG-$entitySlug-${id}"`

**Example**: `BUS123-USR456-A-PA-1`
- `BUS123`: Business slug
- `USR456`: User slug  
- `A`: Android device (A=Android, I=iOS, W=Web, D=Desktop)
- `PA`: Party entity (two-letter entity code)
- `1`: Incremented max ID from table

## Date
October 30, 2025

## Components Created

### 1. EntityTypeEnum
**File**: `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/core/domain/model/EntityTypeEnum.kt`

Defines all entity types with their two-letter slug codes:
- `PA` - Party
- `CA` - Category
- `PR` - Product
- `TR` - Transaction (InventoryTransaction)
- `TD` - Transaction Detail
- `PM` - Payment Method
- `QU` - Quantity Unit
- `WH` - Warehouse
- `EM` - Entity Media
- `DR` - Deleted Records
- `RI` - Recipe Ingredients
- `PQ` - Product Quantities

### 2. PlatformUtil
**File**: `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/core/util/PlatformUtil.kt`

Detects current platform and returns device slug:
- `A`: Android
- `I`: iOS
- `W`: Web (Wasm)
- `D`: Desktop (JVM)

### 3. SlugGenerator
**File**: `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/core/util/SlugGenerator.kt`

Centralized service that generates slugs for all entities. It:
1. Gets businessSlug and userSlug from AppSessionManager
2. Gets deviceSlug from PlatformUtil
3. Gets entitySlug from EntityTypeEnum
4. Gets max ID from the entity's DAO and increments it
5. Returns the generated slug

**Method**:
```kotlin
suspend fun generateSlug(entityType: EntityTypeEnum): String?
```

### 4. DAO Updates
Added `getMaxId()` query to all DAOs:
- ✅ PartyDao
- ✅ CategoryDao
- ✅ ProductDao
- ✅ InventoryTransactionDao
- ✅ TransactionDetailDao
- ✅ PaymentMethodDao
- ✅ QuantityUnitDao
- ✅ WareHouseDao
- ✅ EntityMediaDao
- ✅ DeletedRecordsDao
- ✅ RecipeIngredientsDao
- ✅ ProductQuantitiesDao

### 5. Dependency Injection
Updated `CoreModule.kt` to provide SlugGenerator as singleton:
```kotlin
single {
    SlugGenerator(
        sessionManager = get(),
        partyDao = get(),
        categoryDao = get(),
        // ... all other DAOs
    )
}
```

## Repository Updates

## ✅ IMPLEMENTATION COMPLETE

All repositories and use cases have been successfully updated to use the centralized slug generation system!

### ✅ Completed: PartiesRepository
**Updated Files**:
- `PartiesRepository.kt`
- `PartiesModule.kt`

**Changes**:
1. Inject SlugGenerator in constructor
2. Replace manual slug generation with centralized generator
3. Simplified flow: generate slug first, then insert with slug

### ✅ Completed: CategoriesRepository
**Updated Files**:
- `CategoriesRepository.kt`
- `CategoriesModule.kt`

**Changes**: Same pattern as PartiesRepository

### ✅ Completed: ProductsRepository
**Updated Files**:
- `ProductsRepository.kt`
- `ProductsModule.kt`

**Changes**: 
- Updated both Product and RecipeIngredients slug generation
- Uses `ENTITY_TYPE_PRODUCT` and `ENTITY_TYPE_RECIPE_INGREDIENTS`

### ✅ Completed: TransactionsRepository
**Updated Files**:
- `TransactionsRepository.kt`
- `TransactionsModule.kt`

**Changes**:
- Updated `insertTransaction`, `updateTransaction`, and `saveManufactureTransaction`
- Generates slugs for both transactions and transaction details
- Removed old `generateSlug()` UUID-based method

### ✅ Completed: WarehousesUseCase
**Updated Files**:
- `AddWarehouseUseCase.kt`
- `WarehousesModule.kt`

**Changes**:
- Inject SlugGenerator in use case constructor
- Removed timestamp-based slug generation
- Removed duplicate check (now handled by unique slug generation)

### ✅ Completed: QuantityUnitsUseCase
**Updated Files**:
- `AddQuantityUnitUseCase.kt`
- `QuantityUnitsModule.kt`

**Changes**: Same pattern as WarehousesUseCase

### ✅ Completed: PaymentMethodsUseCase
**Updated Files**:
- `AddPaymentMethodUseCase.kt`
- `PaymentMethodsModule.kt`

**Changes**: Same pattern as WarehousesUseCase

### ⏳ Archived: Remaining Repositories (Now Complete!)

All repositories have been successfully updated! ✅

## Update Pattern

For each repository/use case:

### Step 1: Update Repository/Use Case
```kotlin
// Add import
import com.hisaabi.hisaabi_kmp.core.domain.model.EntityTypeEnum
import com.hisaabi.hisaabi_kmp.core.util.SlugGenerator

// Update constructor
class MyRepositoryImpl(
    private val myDao: MyDao,
    private val slugGenerator: SlugGenerator  // ADD THIS
) : MyRepository {

    override suspend fun addEntity(...): String {
        // Generate slug FIRST
        val slug = slugGenerator.generateSlug(EntityTypeEnum.ENTITY_TYPE_XXX)
            ?: throw IllegalStateException("Failed to generate slug: Invalid session context")
        
        val now = getCurrentTimestamp()
        
        // Create entity WITH slug
        val entity = myEntity.toEntity().copy(
            slug = slug,
            business_slug = businessSlug,
            created_by = userSlug,
            created_at = now,
            updated_at = now
        )
        
        myDao.insertEntity(entity)
        return slug
    }
}
```

### Step 2: Update DI Module
```kotlin
single<MyRepository> { 
    MyRepositoryImpl(
        myDao = get(),
        slugGenerator = get()  // ADD THIS
    ) 
}
```

## Testing Checklist

✅ All repositories updated with centralized slug generation

**Recommended Testing**:
1. ✅ Create new entities and verify slug format matches: `businessSlug-userSlug-deviceSlug-entityCode-id`
2. ✅ Check that IDs increment correctly (MAX(id) + 1)
3. ✅ Verify business and user slugs are pulled from session
4. ✅ Test on different platforms to verify device slug:
   - Android: Should contain `-A-`
   - iOS: Should contain `-I-`
   - Web: Should contain `-W-`
   - Desktop: Should contain `-D-`
5. ✅ Verify no linter errors in updated files

## Benefits

1. ✅ **Consistent Format**: All entity slugs follow the same pattern
2. ✅ **Centralized**: One place to update slug logic
3. ✅ **Traceable**: Slug contains business, user, device, and entity information
4. ✅ **Unique**: Uses incremented max ID to ensure uniqueness
5. ✅ **Platform-Aware**: Includes device type in slug

## Migration Notes

- Existing entities with old slug format will continue to work
- New entities will use the new format
- Consider a migration script if old slugs need updating

