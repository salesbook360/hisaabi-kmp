package com.hisaabi.hisaabi_kmp.core.di

import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManagerImpl
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
}

