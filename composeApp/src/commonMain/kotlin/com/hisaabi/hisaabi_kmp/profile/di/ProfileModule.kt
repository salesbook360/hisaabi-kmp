package com.hisaabi.hisaabi_kmp.profile.di

import com.hisaabi.hisaabi_kmp.auth.data.datasource.AuthLocalDataSource
import com.hisaabi.hisaabi_kmp.profile.data.ProfileRepository
import com.hisaabi.hisaabi_kmp.profile.presentation.viewmodel.UpdateProfileViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {
    // Repository
    single { ProfileRepository(get(), get<AuthLocalDataSource>()) }
    
    // ViewModel
    viewModel { UpdateProfileViewModel(get(), get<AuthLocalDataSource>()) }
}

