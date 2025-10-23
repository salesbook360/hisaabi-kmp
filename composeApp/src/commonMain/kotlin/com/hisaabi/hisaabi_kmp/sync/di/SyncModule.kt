package com.hisaabi.hisaabi_kmp.sync.di

import com.hisaabi.hisaabi_kmp.sync.data.datasource.SyncPreferencesDataSource
import com.hisaabi.hisaabi_kmp.sync.data.datasource.SyncPreferencesDataSourceImpl
import com.hisaabi.hisaabi_kmp.sync.data.datasource.SyncRemoteDataSource
import com.hisaabi.hisaabi_kmp.sync.data.datasource.SyncRemoteDataSourceImpl
import com.hisaabi.hisaabi_kmp.sync.data.repository.SyncRepository
import com.hisaabi.hisaabi_kmp.sync.data.repository.SyncRepositoryImpl
import com.hisaabi.hisaabi_kmp.sync.domain.manager.SyncManager
import com.hisaabi.hisaabi_kmp.sync.domain.manager.SyncManagerImpl
import org.koin.dsl.module

/**
 * Dependency injection module for Sync functionality
 */
val syncModule = module {
    // Data Sources
    single<SyncRemoteDataSource> {
        SyncRemoteDataSourceImpl(
            httpClient = get()
        )
    }
    
    single<SyncPreferencesDataSource> {
        SyncPreferencesDataSourceImpl(
            preferencesManager = get()
        )
    }
    
    // Repository
    single<SyncRepository> {
        SyncRepositoryImpl(
            remoteDataSource = get(),
            preferencesDataSource = get(),
            sessionManager = get(),
            categoryDao = get(),
            productDao = get(),
            partyDao = get(),
            paymentMethodDao = get(),
            quantityUnitDao = get(),
            warehouseDao = get(),
            transactionDao = get(),
            transactionDetailDao = get(),
            productQuantitiesDao = get(),
            entityMediaDao = get(),
            recipeIngredientsDao = get(),
            deletedRecordsDao = get()
        )
    }
    
    // Domain Manager
    single<SyncManager> {
        SyncManagerImpl(
            syncRepository = get(),
            preferencesDataSource = get(),
            sessionManager = get()
        )
    }
}

