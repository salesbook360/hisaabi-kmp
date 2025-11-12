package com.hisaabi.hisaabi_kmp.reports.di

import com.hisaabi.hisaabi_kmp.reports.domain.usecase.GenerateReportUseCase
import com.hisaabi.hisaabi_kmp.reports.domain.usecase.GenerateSalesReportUseCase
import com.hisaabi.hisaabi_kmp.reports.presentation.viewmodel.ReportViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val reportsModule = module {
    // Use Cases
    singleOf(::GenerateSalesReportUseCase)
    single { GenerateReportUseCase(get()) }
    
    // Platform-specific utilities are defined in platformModule
    
    // ViewModels
    viewModel { ReportViewModel(get(), get(), get()) }
}

