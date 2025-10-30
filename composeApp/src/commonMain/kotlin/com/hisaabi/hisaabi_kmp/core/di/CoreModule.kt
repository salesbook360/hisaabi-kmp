package com.hisaabi.hisaabi_kmp.core.di

import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManagerImpl
import com.hisaabi.hisaabi_kmp.core.util.SlugGenerator
import org.koin.dsl.module

/**
 * Core module for application-wide services
 */
val coreModule = module {
    single<AppSessionManager> {
        AppSessionManagerImpl(
            authLocalDataSource = get(),
            businessPreferences = get(),
            getSelectedBusinessUseCase = get()
        )
    }
    
    single {
        SlugGenerator(
            sessionManager = get(),
            partyDao = get(),
            categoryDao = get(),
            productDao = get(),
            inventoryTransactionDao = get(),
            transactionDetailDao = get(),
            paymentMethodDao = get(),
            quantityUnitDao = get(),
            wareHouseDao = get(),
            entityMediaDao = get(),
            deletedRecordsDao = get(),
            recipeIngredientsDao = get(),
            productQuantitiesDao = get()
        )
    }
}

