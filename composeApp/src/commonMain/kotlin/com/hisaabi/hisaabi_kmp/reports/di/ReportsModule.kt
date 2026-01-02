package com.hisaabi.hisaabi_kmp.reports.di

import com.hisaabi.hisaabi_kmp.reports.domain.usecase.GenerateBalanceSheetReportUseCase
import com.hisaabi.hisaabi_kmp.reports.domain.usecase.GeneratePurchaseReportUseCase
import com.hisaabi.hisaabi_kmp.reports.domain.usecase.GenerateReportUseCase
import com.hisaabi.hisaabi_kmp.reports.domain.usecase.GenerateSalesReportUseCase
import com.hisaabi.hisaabi_kmp.reports.presentation.viewmodel.ReportViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val reportsModule = module {
    // Use Cases
    single { GenerateSalesReportUseCase(get(), get(), get(), get()) }
    single { GenerateBalanceSheetReportUseCase(get(), get(), get(), get(), get(), get(), get(), get()) }
    single {
        GeneratePurchaseReportUseCase(
            transactionDao = get(),
            transactionDetailDao = get(),
            productDao = get(),
            partyDao = get(),
            categoryDao = get(),
            businessPreferences = get(),
            preferencesManager = get()
        )
    }
    single { GenerateReportUseCase(get(), get(), get(), get()) }
    
    // Platform-specific utilities are defined in platformModule
    
    // ViewModels
    viewModel { ReportViewModel(get(), get(), get(), get()) }
}

