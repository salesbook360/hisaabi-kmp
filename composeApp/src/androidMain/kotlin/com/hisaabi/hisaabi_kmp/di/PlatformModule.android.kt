package com.hisaabi.hisaabi_kmp.di

import com.hisaabi.hisaabi_kmp.database.DatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual fun platformModule() = module {
    single { DatabaseBuilder(androidContext()) }
}

