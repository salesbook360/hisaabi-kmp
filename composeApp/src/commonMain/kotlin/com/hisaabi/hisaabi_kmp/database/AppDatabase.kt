package com.hisaabi.hisaabi_kmp.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.hisaabi.hisaabi_kmp.database.dao.*
import com.hisaabi.hisaabi_kmp.database.entity.*

@Database(
    entities = [
        PartyEntity::class,
        ProductEntity::class,
        InventoryTransactionEntity::class,
        TransactionDetailEntity::class,
        QuantityUnitEntity::class,
        PaymentMethodEntity::class,
        WareHouseEntity::class,
        ProductQuantitiesEntity::class,
        CategoryEntity::class,
        DeletedRecordsEntity::class,
        EntityMediaEntity::class,
        RecipeIngredientsEntity::class,
        BusinessEntity::class,
        UserAuthEntity::class
    ],
    version = 5,
    exportSchema = true
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun partyDao(): PartyDao
    abstract fun productDao(): ProductDao
    abstract fun inventoryTransactionDao(): InventoryTransactionDao
    abstract fun transactionDetailDao(): TransactionDetailDao
    abstract fun quantityUnitDao(): QuantityUnitDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun wareHouseDao(): WareHouseDao
    abstract fun productQuantitiesDao(): ProductQuantitiesDao
    abstract fun categoryDao(): CategoryDao
    abstract fun deletedRecordsDao(): DeletedRecordsDao
    abstract fun entityMediaDao(): EntityMediaDao
    abstract fun recipeIngredientsDao(): RecipeIngredientsDao
    abstract fun businessDao(): BusinessDao
    abstract fun userAuthDao(): UserAuthDao
    abstract fun transactionProcessorDao(): TransactionProcessorDao
    
    companion object {
        const val DATABASE_NAME = "hisaabi_database.db"
    }
}

// RoomDatabase constructor for multiplatform support
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

