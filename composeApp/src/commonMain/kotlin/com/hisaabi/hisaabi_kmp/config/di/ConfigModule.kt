package com.hisaabi.hisaabi_kmp.config.di

import com.hisaabi.hisaabi_kmp.config.AppConfig
import com.hisaabi.hisaabi_kmp.config.AppConfigImpl
import com.hisaabi.hisaabi_kmp.config.Environment
import com.hisaabi.hisaabi_kmp.config.getDefaultEnvironment
import org.koin.dsl.module

/**
 * Configuration module for dependency injection
 * Provides AppConfig instance based on the current environment
 */
val configModule = module {
    single<AppConfig> {
        // Uses getDefaultEnvironment() which defaults to STAGE
        // Can be overridden by platform-specific implementations
        AppConfigImpl(environment = getDefaultEnvironment())
    }
    
    single<Environment> {
        get<AppConfig>().environment
    }
}

