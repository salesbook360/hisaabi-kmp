package com.hisaabi.hisaabi_kmp.business.di

import com.hisaabi.hisaabi_kmp.business.data.repository.BusinessRepository
import com.hisaabi.hisaabi_kmp.business.domain.usecase.*
import com.hisaabi.hisaabi_kmp.business.presentation.viewmodel.AddBusinessViewModel
import com.hisaabi.hisaabi_kmp.business.presentation.viewmodel.MyBusinessViewModel
import com.hisaabi.hisaabi_kmp.database.datasource.BusinessLocalDataSource
import com.hisaabi.hisaabi_kmp.database.datasource.BusinessLocalDataSourceImpl
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val businessModule = module {
    // Data Source
    single<BusinessLocalDataSource> { BusinessLocalDataSourceImpl(get()) }
    
    // Repository
    single { BusinessRepository(get()) }
    
    // Use Cases
    single { GetBusinessesUseCase(get()) }
    single { AddBusinessUseCase(get()) }
    single { UpdateBusinessUseCase(get()) }
    single { DeleteBusinessUseCase(get()) }
    
    single {
        BusinessUseCases(
            getBusinesses = get(),
            addBusiness = get(),
            updateBusiness = get(),
            deleteBusiness = get()
        )
    }
    
    // ViewModels
    viewModel { MyBusinessViewModel(get()) }
    viewModel { AddBusinessViewModel(get()) }
}

