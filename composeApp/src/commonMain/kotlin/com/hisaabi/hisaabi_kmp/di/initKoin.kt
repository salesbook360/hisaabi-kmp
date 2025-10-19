package com.hisaabi.hisaabi_kmp.di

import com.hisaabi.hisaabi_kmp.auth.di.authModule
import com.hisaabi.hisaabi_kmp.business.di.businessModule
import com.hisaabi.hisaabi_kmp.categories.di.categoriesModule
import com.hisaabi.hisaabi_kmp.database.di.databaseModule
import com.hisaabi.hisaabi_kmp.parties.di.partiesModule
import com.hisaabi.hisaabi_kmp.paymentmethods.di.paymentMethodsModule
import com.hisaabi.hisaabi_kmp.products.di.productsModule
import com.hisaabi.hisaabi_kmp.profile.di.profileModule
import com.hisaabi.hisaabi_kmp.quantityunits.di.quantityUnitsModule
import com.hisaabi.hisaabi_kmp.settings.di.settingsModule
import com.hisaabi.hisaabi_kmp.templates.di.templatesModule
import com.hisaabi.hisaabi_kmp.transactions.di.transactionsModule
import com.hisaabi.hisaabi_kmp.warehouses.di.warehousesModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            authModule,
            databaseModule,
            partiesModule,
            categoriesModule,
            productsModule,
            paymentMethodsModule,
            warehousesModule,
            businessModule,
            quantityUnitsModule,
            settingsModule,
            templatesModule,
            profileModule,
            transactionsModule,
            platformModule()
        )
    }
}

/**
 * Platform-specific module provider
 * Each platform will provide its own implementation
 */
expect fun platformModule(): org.koin.core.module.Module