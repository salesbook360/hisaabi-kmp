package com.hisaabi.hisaabi_kmp.di

import com.hisaabi.hisaabi_kmp.database.DatabaseBuilder
import org.koin.dsl.module

/**
 * WasmJS platform module
 * Note: Room database is not yet supported on WasmJS
 */
actual fun platformModule() = module {
    single { DatabaseBuilder() }
}

