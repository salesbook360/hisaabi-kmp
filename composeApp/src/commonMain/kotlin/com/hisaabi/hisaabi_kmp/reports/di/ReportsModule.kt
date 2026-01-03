package com.hisaabi.hisaabi_kmp.reports.di

import com.hisaabi.hisaabi_kmp.reports.domain.usecase.GenerateBalanceSheetReportUseCase
import com.hisaabi.hisaabi_kmp.reports.domain.usecase.GeneratePartyReportUseCase
import com.hisaabi.hisaabi_kmp.reports.domain.usecase.GenerateProductReportUseCase
import com.hisaabi.hisaabi_kmp.reports.domain.usecase.GenerateProfitLossByAvgPriceUseCase
import com.hisaabi.hisaabi_kmp.reports.domain.usecase.GeneratePurchaseReportUseCase
import com.hisaabi.hisaabi_kmp.reports.domain.usecase.GenerateReportUseCase
import com.hisaabi.hisaabi_kmp.reports.domain.usecase.GenerateSalesReportUseCase
import com.hisaabi.hisaabi_kmp.reports.domain.usecase.GenerateStockReportUseCase
import com.hisaabi.hisaabi_kmp.reports.domain.usecase.GenerateTopCustomersReportUseCase
import com.hisaabi.hisaabi_kmp.reports.domain.usecase.GenerateTopProductsReportUseCase
import com.hisaabi.hisaabi_kmp.reports.domain.usecase.GenerateWarehouseReportUseCase
import com.hisaabi.hisaabi_kmp.reports.presentation.viewmodel.ReportViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val reportsModule = module {
    // Use Cases
    single {
        GenerateSalesReportUseCase(
            transactionDao = get(),
            transactionDetailDao = get(),
            productDao = get(),
            partyDao = get(),
            categoryDao = get(),
            businessPreferences = get(),
            preferencesManager = get()
        )
    }
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
    single {
        GenerateStockReportUseCase(
            transactionDao = get(),
            transactionDetailDao = get(),
            productDao = get(),
            productQuantitiesDao = get(),
            productsRepository = get(),
            businessPreferences = get(),
            preferencesManager = get()
        )
    }
    single {
        GenerateWarehouseReportUseCase(
            transactionDao = get(),
            transactionDetailDao = get(),
            productDao = get(),
            productQuantitiesDao = get(),
            productsRepository = get(),
            businessPreferences = get(),
            preferencesManager = get()
        )
    }
    single {
        GenerateProfitLossByAvgPriceUseCase(
            transactionDao = get(),
            transactionDetailDao = get(),
            productDao = get(),
            partyDao = get(),
            categoryDao = get(),
            businessPreferences = get(),
            preferencesManager = get()
        )
    }
    single {
        GenerateTopProductsReportUseCase(
            transactionDao = get(),
            transactionDetailDao = get(),
            productDao = get(),
            businessPreferences = get(),
            preferencesManager = get()
        )
    }
    single {
        GenerateTopCustomersReportUseCase(
            transactionDao = get(),
            transactionDetailDao = get(),
            partyDao = get(),
            businessPreferences = get(),
            preferencesManager = get()
        )
    }
    single {
        GenerateProductReportUseCase(
            transactionDao = get(),
            transactionDetailDao = get(),
            productDao = get(),
            businessPreferences = get(),
            preferencesManager = get()
        )
    }
    single {
        GeneratePartyReportUseCase(
            transactionDao = get(),
            transactionDetailDao = get(),
            partyDao = get(),
            productDao = get(),
            businessPreferences = get(),
            preferencesManager = get()
        )
    }
    single { GenerateReportUseCase(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    
    // Platform-specific utilities are defined in platformModule
    
    // ViewModels
    viewModel { ReportViewModel(get(), get(), get(), get()) }
}

