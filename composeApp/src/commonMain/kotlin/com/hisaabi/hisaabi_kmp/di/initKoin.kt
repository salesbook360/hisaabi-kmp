package com.hisaabi.hisaabi_kmp.di

import com.hisaabi.hisaabi_kmp.auth.di.authModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(authModule)
    }
}