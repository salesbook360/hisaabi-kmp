package com.hisaabi.hisaabi_kmp.di

import com.hisaabi.hisaabi_kmp.database.DatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual fun platformModule() = module {
    single { DatabaseBuilder(androidContext()) }
    
    // Reports platform-specific dependencies
    single { com.hisaabi.hisaabi_kmp.reports.domain.util.ReportPdfGenerator(androidContext()) }
    single { com.hisaabi.hisaabi_kmp.reports.domain.util.ShareHelper(androidContext()) }
}

