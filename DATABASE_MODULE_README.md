# Database Module Documentation

## Overview

This document describes the Room database implementation for the Hisaabi Kotlin Multiplatform project. The database module provides local storage across all platforms: Android, iOS, Desktop (JVM), and Web (WasmJS).

## Architecture

The database module follows Clean Architecture principles with three layers:

### 1. Entity Layer (`database/entity/`)
Room entity classes that map to database tables:
- `PartyEntity` - Customers/suppliers management
- `ProductEntity` - Product catalog
- `InventoryTransactionEntity` - Transaction records
- `TransactionDetailEntity` - Transaction line items
- `QuantityUnitEntity` - Units of measurement
- `PaymentMethodEntity` - Payment methods
- `WareHouseEntity` - Warehouse/location management
- `ProductQuantitiesEntity` - Product stock levels per warehouse
- `CategoryEntity` - Product/party categories
- `DeletedRecordsEntity` - Soft delete tracking
- `EntityMediaEntity` - Media files for entities
- `RecipeIngredientsEntity` - Recipe/manufacturing data
- `BusinessEntity` - Business information

### 2. DAO Layer (`database/dao/`)
Data Access Objects that define database operations:
- Each entity has a corresponding DAO interface
- DAOs provide CRUD operations and custom queries
- Support for Flow-based reactive queries
- Bulk operations for syncing

### 3. Data Source Layer (`database/datasource/`)
Local data sources that wrap DAO operations:
- Provides a clean abstraction layer over DAOs
- Easier to mock for testing
- Consistent API across the application

## Database Schema

The database schema is based on the existing SQLite database with the following tables:

### Core Tables
1. **Party** - Customer/Supplier Management
   - Stores party information (name, phone, address, balance)
   - Unique slug index for fast lookups
   - Supports role-based categorization

2. **Product** - Product Catalog
   - Product details (title, description, pricing)
   - Multiple price types (retail, wholesale, purchase)
   - Tax and discount configuration
   - Expiry date tracking

3. **InventoryTransaction** - Transaction Records
   - Complete transaction information
   - Payment method tracking
   - Tax and discount calculations
   - Warehouse transfer support

4. **TransactionDetail** - Transaction Line Items
   - Individual product entries in transactions
   - Quantity and price tracking
   - Recipe support for manufactured products

### Supporting Tables
5. **QuantityUnit** - Units of Measurement
6. **PaymentMethod** - Payment Methods
7. **WareHouse** - Warehouse Management
8. **ProductQuantities** - Stock Levels
9. **Category** - Categorization
10. **DeletedRecords** - Soft Delete Tracking
11. **EntityMedia** - Media Files
12. **RecipeIngredients** - Recipe Components
13. **Business** - Business Information

## Platform Support

The database works across most KMP targets:

### Android ✅
- Uses Room's native Android implementation
- Database stored in app's database directory
- SQLite via BundledSQLiteDriver
- **Fully Supported**

### iOS ✅
- Uses Room's iOS implementation
- Database stored in app's documents directory
- SQLite via BundledSQLiteDriver
- **Fully Supported**

### Desktop (JVM) ✅
- Uses Room's JVM implementation
- Database stored in system temp directory
- SQLite via BundledSQLiteDriver
- **Fully Supported**

### Web (WasmJS) ❌
- **Not Supported** - Room doesn't support WasmJS yet
- Stub implementation throws informative error
- Consider using browser localStorage as alternative
- Database operations will fail on this platform

## Dependencies

### Room KMP
- `androidx.room:room-runtime:2.7.0-alpha12`
- `androidx.room:room-compiler:2.7.0-alpha12` (KSP)
- `androidx.sqlite:sqlite-bundled:2.5.0-alpha12`

### Build Configuration
```kotlin
plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

room {
    schemaDirectory("$projectDir/schemas")
}
```

## Usage

### 1. Dependency Injection

The database is automatically configured through Koin:

```kotlin
// In your Application or initialization code
initKoin()
```

### 2. Accessing Data Sources

Inject data sources in your ViewModels or repositories:

```kotlin
class ProductViewModel(
    private val productDataSource: ProductLocalDataSource
) : ViewModel() {
    
    val products: Flow<List<ProductEntity>> = 
        productDataSource.getAllProducts()
    
    suspend fun addProduct(product: ProductEntity) {
        productDataSource.insertProduct(product)
    }
}
```

### 3. Reactive Queries

Use Flow for reactive data:

```kotlin
// Observe all products
productDataSource.getAllProducts()
    .collectLatest { products ->
        // Update UI
    }

// Observe products by category
productDataSource.getProductsByCategory("electronics")
    .collectLatest { products ->
        // Update UI
    }
```

### 4. CRUD Operations

```kotlin
// Insert
val productId = productDataSource.insertProduct(product)

// Update
productDataSource.updateProduct(product.copy(title = "New Title"))

// Delete
productDataSource.deleteProduct(product)

// Query
val product = productDataSource.getProductBySlug("product-123")
```

### 5. Bulk Operations

```kotlin
// Insert multiple items
productDataSource.insertProducts(productList)

// Get unsynced items for server upload
val unsyncedProducts = productDataSource.getUnsyncedProducts()
```

## Data Synchronization

The database includes `sync_status` fields for tracking data sync state:

- `sync_status = 0` - Synced
- `sync_status = 1` - Created (not synced)
- `sync_status = 2` - Updated (not synced)
- `sync_status = 3` - Deleted (not synced)

Use the `getUnsynced*()` methods to fetch items that need to be synced with the server.

## Database Migration

To update the database schema:

1. Modify the entity classes
2. Increment the database version in `AppDatabase`
3. Add migration strategy if needed:

```kotlin
@Database(
    entities = [...],
    version = 2, // Increment version
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    // ...
}
```

Room will handle the schema changes automatically when using `exportSchema = true`.

## Testing

The data source pattern makes testing easier:

```kotlin
class FakeProductDataSource : ProductLocalDataSource {
    private val products = mutableListOf<ProductEntity>()
    
    override fun getAllProducts(): Flow<List<ProductEntity>> = 
        flow { emit(products) }
    
    override suspend fun insertProduct(product: ProductEntity): Long {
        products.add(product)
        return product.id.toLong()
    }
    
    // Implement other methods...
}
```

## Key Features

### ✅ Implemented
- Multi-platform database support (Android, iOS, Desktop, Web)
- Reactive queries with Flow
- CRUD operations for all entities
- Bulk operations
- Sync status tracking
- Unique constraints and indices
- Soft delete support
- Media attachment support
- Recipe/manufacturing support
- Warehouse management
- Stock level tracking

### 🔄 Sync Features
- Track unsynced changes
- Business-specific data filtering
- Slug-based relationships
- Timestamp tracking

## File Structure

```
database/
├── entity/                  # Room entities
│   ├── PartyEntity.kt
│   ├── ProductEntity.kt
│   ├── InventoryTransactionEntity.kt
│   ├── TransactionDetailEntity.kt
│   ├── QuantityUnitEntity.kt
│   ├── PaymentMethodEntity.kt
│   ├── WareHouseEntity.kt
│   ├── ProductQuantitiesEntity.kt
│   ├── CategoryEntity.kt
│   ├── DeletedRecordsEntity.kt
│   ├── EntityMediaEntity.kt
│   ├── RecipeIngredientsEntity.kt
│   └── BusinessEntity.kt
│
├── dao/                     # Data Access Objects
│   ├── PartyDao.kt
│   ├── ProductDao.kt
│   ├── InventoryTransactionDao.kt
│   ├── TransactionDetailDao.kt
│   ├── QuantityUnitDao.kt
│   ├── PaymentMethodDao.kt
│   ├── WareHouseDao.kt
│   ├── ProductQuantitiesDao.kt
│   ├── CategoryDao.kt
│   ├── DeletedRecordsDao.kt
│   ├── EntityMediaDao.kt
│   ├── RecipeIngredientsDao.kt
│   └── BusinessDao.kt
│
├── datasource/              # Data sources
│   ├── PartyLocalDataSource.kt
│   ├── ProductLocalDataSource.kt
│   ├── InventoryTransactionLocalDataSource.kt
│   ├── TransactionDetailLocalDataSource.kt
│   ├── CategoryLocalDataSource.kt
│   └── BusinessLocalDataSource.kt
│
├── di/                      # Dependency injection
│   └── DatabaseModule.kt
│
├── AppDatabase.kt           # Room database class
└── DatabaseBuilder.kt       # Platform-specific builders
    ├── DatabaseBuilder.android.kt
    ├── DatabaseBuilder.ios.kt
    ├── DatabaseBuilder.jvm.kt
    └── DatabaseBuilder.wasmJs.kt
```

## Best Practices

1. **Always use data sources** instead of accessing DAOs directly
2. **Use Flow for UI updates** to get reactive data
3. **Mark items for sync** using sync_status field
4. **Use slugs for relationships** instead of numeric IDs
5. **Handle nullability properly** - some fields can be null in the API
6. **Use transactions** for bulk operations
7. **Index frequently queried fields** (already configured)

## Troubleshooting

### Build Issues
- Ensure KSP plugin is applied
- Check Room version compatibility with Kotlin version
- Clean and rebuild project: `./gradlew clean build`

### Runtime Issues
- Verify database initialization in Koin
- Check platform-specific database builder implementations
- Ensure proper context is provided for Android

### Migration Issues
- Export schema and review changes
- Test migrations thoroughly
- Provide fallback migration strategy if needed

## Future Enhancements

- [ ] Database encryption
- [ ] Multi-database support for multiple businesses
- [ ] Advanced query builders
- [ ] Database backup/restore
- [ ] Data export functionality
- [ ] Full-text search
- [ ] Automated sync service
- [ ] Conflict resolution strategy
- [ ] Database compression
- [ ] Performance monitoring

## References

- [Room KMP Documentation](https://developer.android.com/kotlin/multiplatform/room)
- [SQLite Documentation](https://www.sqlite.org/docs.html)
- [Koin Documentation](https://insert-koin.io/)
- [Kotlin Coroutines Flow](https://kotlinlang.org/docs/flow.html)

