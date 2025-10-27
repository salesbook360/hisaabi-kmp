package com.hisaabi.hisaabi_kmp.database.di

import com.hisaabi.hisaabi_kmp.database.AppDatabase
import com.hisaabi.hisaabi_kmp.database.DatabaseBuilder
import com.hisaabi.hisaabi_kmp.database.datasource.*
import com.hisaabi.hisaabi_kmp.home.dashboard.DashboardRepository
import com.hisaabi.hisaabi_kmp.home.dashboard.DashboardViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val databaseModule = module {
    
    // Database instance
    single<AppDatabase> {
        get<DatabaseBuilder>().build()
    }
    
    // DAOs
    single { get<AppDatabase>().partyDao() }
    single { get<AppDatabase>().productDao() }
    single { get<AppDatabase>().inventoryTransactionDao() }
    single { get<AppDatabase>().transactionDetailDao() }
    single { get<AppDatabase>().quantityUnitDao() }
    single { get<AppDatabase>().paymentMethodDao() }
    single { get<AppDatabase>().wareHouseDao() }
    single { get<AppDatabase>().productQuantitiesDao() }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().deletedRecordsDao() }
    single { get<AppDatabase>().entityMediaDao() }
    single { get<AppDatabase>().recipeIngredientsDao() }
    single { get<AppDatabase>().userAuthDao() }
    single { get<AppDatabase>().businessDao() }
    
    // Data Sources
    single<PartyLocalDataSource> { PartyLocalDataSourceImpl(get()) }
    single<ProductLocalDataSource> { ProductLocalDataSourceImpl(get()) }
    single<InventoryTransactionLocalDataSource> { InventoryTransactionLocalDataSourceImpl(get()) }
    single<TransactionDetailLocalDataSource> { TransactionDetailLocalDataSourceImpl(get()) }
    single<CategoryLocalDataSource> { CategoryLocalDataSourceImpl(get()) }
    single<BusinessLocalDataSource> { BusinessLocalDataSourceImpl(get()) }
    
    // Dashboard
    single { 
        DashboardRepository(
            inventoryTransactionDao = get(),
            transactionDetailDao = get(),
            partyDao = get(),
            productDao = get(),
            productQuantitiesDao = get(),
            paymentMethodDao = get(),
            categoryDao = get()
        )
    }
    singleOf(::DashboardViewModel)
}

