package com.hisaabi.hisaabi_kmp.profile.di

import com.hisaabi.hisaabi_kmp.profile.data.ProfileRepository
import com.hisaabi.hisaabi_kmp.profile.presentation.viewmodel.UpdateProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {
    // Repository
    single { ProfileRepository(get()) }
    
    // ViewModel
    viewModel { UpdateProfileViewModel(get()) }
}

