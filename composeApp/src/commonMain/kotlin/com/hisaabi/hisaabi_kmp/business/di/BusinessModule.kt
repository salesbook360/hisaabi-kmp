package com.hisaabi.hisaabi_kmp.business.di

import com.hisaabi.hisaabi_kmp.business.data.datasource.BusinessPreferencesDataSource
import com.hisaabi.hisaabi_kmp.business.data.datasource.BusinessPreferencesDataSourceImpl
import com.hisaabi.hisaabi_kmp.business.data.datasource.BusinessRemoteDataSource
import com.hisaabi.hisaabi_kmp.business.data.datasource.BusinessRemoteDataSourceImpl
import com.hisaabi.hisaabi_kmp.business.data.repository.BusinessRepository
import com.hisaabi.hisaabi_kmp.business.domain.usecase.*
import com.hisaabi.hisaabi_kmp.business.presentation.viewmodel.AddBusinessViewModel
import com.hisaabi.hisaabi_kmp.business.presentation.viewmodel.MyBusinessViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val businessModule = module {
    // Remote Data Source - uses HttpClient from authModule
    single<BusinessRemoteDataSource> { BusinessRemoteDataSourceImpl(get()) }
    
    // Preferences Data Source - for selected business persistence
    single<BusinessPreferencesDataSource> { BusinessPreferencesDataSourceImpl(get()) }
    
    // Repository
    single { BusinessRepository(get()) }
    
    // Use Cases
    single { GetBusinessesUseCase(get()) }
    single { AddBusinessUseCase(get()) }
    single { UpdateBusinessUseCase(get()) }
    single { DeleteBusinessUseCase(get()) }
    single { GetSelectedBusinessUseCase(get(), get()) }
    
    single {
        BusinessUseCases(
            getBusinesses = get(),
            addBusiness = get(),
            updateBusiness = get(),
            deleteBusiness = get()
        )
    }
    
    // ViewModels
    viewModel { MyBusinessViewModel(get(), get()) }
    viewModel { AddBusinessViewModel(get()) }
}

