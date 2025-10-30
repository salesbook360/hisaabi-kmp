package com.hisaabi.hisaabi_kmp.parties.di

import com.hisaabi.hisaabi_kmp.parties.data.repository.PartiesRepository
import com.hisaabi.hisaabi_kmp.parties.data.repository.PartiesRepositoryImpl
import com.hisaabi.hisaabi_kmp.parties.domain.usecase.AddPartyUseCase
import com.hisaabi.hisaabi_kmp.parties.domain.usecase.GetPartiesCountUseCase
import com.hisaabi.hisaabi_kmp.parties.domain.usecase.GetPartiesUseCase
import com.hisaabi.hisaabi_kmp.parties.domain.usecase.GetTotalBalanceUseCase
import com.hisaabi.hisaabi_kmp.parties.presentation.viewmodel.AddPartyViewModel
import com.hisaabi.hisaabi_kmp.parties.presentation.viewmodel.PartiesViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val partiesModule = module {
    // Repository
    single<PartiesRepository> { 
        PartiesRepositoryImpl(
            partyDao = get(),
            slugGenerator = get()
        ) 
    }
    
    // Use Cases
    singleOf(::GetPartiesUseCase)
    singleOf(::GetPartiesCountUseCase)
    singleOf(::GetTotalBalanceUseCase)
    singleOf(::AddPartyUseCase)
    
    // ViewModels
    singleOf(::PartiesViewModel)
    singleOf(::AddPartyViewModel)
}


