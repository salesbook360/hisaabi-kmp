package com.hisaabi.hisaabi_kmp.templates.di

import com.hisaabi.hisaabi_kmp.templates.data.TemplatesRepository
import com.hisaabi.hisaabi_kmp.templates.presentation.viewmodel.AddTemplateViewModel
import com.hisaabi.hisaabi_kmp.templates.presentation.viewmodel.TemplatesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val templatesModule = module {
    // Repository
    single { TemplatesRepository() }
    
    // ViewModels
    viewModel { TemplatesViewModel(get()) }
    viewModel { AddTemplateViewModel(get()) }
}

