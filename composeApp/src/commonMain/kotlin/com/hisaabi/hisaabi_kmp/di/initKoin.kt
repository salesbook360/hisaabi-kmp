package com.hisaabi.hisaabi_kmp.di

import com.hisaabi.hisaabi_kmp.auth.di.authModule
import com.hisaabi.hisaabi_kmp.categories.di.categoriesModule
import com.hisaabi.hisaabi_kmp.database.di.databaseModule
import com.hisaabi.hisaabi_kmp.parties.di.partiesModule
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
            platformModule()
        )
    }
}

/**
 * Platform-specific module provider
 * Each platform will provide its own implementation
 */
expect fun platformModule(): org.koin.core.module.Module