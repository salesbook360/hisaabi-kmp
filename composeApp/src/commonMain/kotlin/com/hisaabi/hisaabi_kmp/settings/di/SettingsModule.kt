package com.hisaabi.hisaabi_kmp.settings.di

import com.hisaabi.hisaabi_kmp.receipt.ReceiptViewModel
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.settings.presentation.viewmodel.DashboardSettingsViewModel
import com.hisaabi.hisaabi_kmp.settings.presentation.viewmodel.ReceiptSettingsViewModel
import com.hisaabi.hisaabi_kmp.settings.presentation.viewmodel.TransactionSettingsViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    // Preferences Manager (Singleton)
    single { PreferencesManager() }
    
    // ViewModels
    viewModel { TransactionSettingsViewModel(get()) }
    viewModel { ReceiptSettingsViewModel(get()) }
    viewModel { DashboardSettingsViewModel(get()) }
    viewModel { ReceiptViewModel(get(), get()) }
}

