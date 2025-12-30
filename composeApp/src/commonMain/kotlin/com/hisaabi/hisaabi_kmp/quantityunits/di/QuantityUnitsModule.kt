package com.hisaabi.hisaabi_kmp.quantityunits.di

import com.hisaabi.hisaabi_kmp.database.datasource.QuantityUnitLocalDataSource
import com.hisaabi.hisaabi_kmp.quantityunits.data.repository.QuantityUnitsRepository
import com.hisaabi.hisaabi_kmp.quantityunits.domain.usecase.*
import com.hisaabi.hisaabi_kmp.quantityunits.presentation.viewmodel.AddQuantityUnitViewModel
import com.hisaabi.hisaabi_kmp.quantityunits.presentation.viewmodel.QuantityUnitsViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val quantityUnitsModule = module {
    // Data Source
    single { QuantityUnitLocalDataSource(get()) }
    
    // Repository
    single { QuantityUnitsRepository(get(), get(), get(), get()) }
    
    // Use Cases
    single { GetQuantityUnitsUseCase(get()) }
    single { AddQuantityUnitUseCase(repository = get(), slugGenerator = get()) }
    single { UpdateQuantityUnitUseCase(get()) }
    single { DeleteQuantityUnitUseCase(get()) }
    
    single {
        QuantityUnitUseCases(
            getUnits = get(),
            addUnit = get(),
            updateUnit = get(),
            deleteUnit = get()
        )
    }
    
    // ViewModels - using AppSessionManager for getting active business and user context
    viewModel { QuantityUnitsViewModel(useCases = get(), sessionManager = get()) }
    viewModel { AddQuantityUnitViewModel(useCases = get(), sessionManager = get()) }
}

