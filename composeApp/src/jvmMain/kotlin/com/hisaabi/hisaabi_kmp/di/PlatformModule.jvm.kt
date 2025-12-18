package com.hisaabi.hisaabi_kmp.di

import com.hisaabi.hisaabi_kmp.database.DatabaseBuilder
import org.koin.dsl.module

actual fun platformModule() = module {
    single { DatabaseBuilder() }
    
    // Reports platform-specific dependencies
    single { com.hisaabi.hisaabi_kmp.reports.domain.util.ReportPdfGenerator() }
    single { com.hisaabi.hisaabi_kmp.reports.domain.util.ShareHelper() }
}

