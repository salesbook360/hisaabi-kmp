package com.hisaabi.hisaabi_kmp.paymentmethods.di

import com.hisaabi.hisaabi_kmp.database.datasource.PaymentMethodLocalDataSource
import com.hisaabi.hisaabi_kmp.paymentmethods.data.repository.PaymentMethodsRepository
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.usecase.*
import com.hisaabi.hisaabi_kmp.paymentmethods.presentation.viewmodel.AddPaymentMethodViewModel
import com.hisaabi.hisaabi_kmp.paymentmethods.presentation.viewmodel.PaymentMethodsViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val paymentMethodsModule = module {
    // Data Source
    single { PaymentMethodLocalDataSource(get()) }
    
    // Repository
    single { PaymentMethodsRepository(get(), get(), get(), get()) }
    
    // Use Cases
    single { GetPaymentMethodsUseCase(get()) }
    single { AddPaymentMethodUseCase(repository = get(), slugGenerator = get()) }
    single { UpdatePaymentMethodUseCase(get()) }
    single { DeletePaymentMethodUseCase(get()) }
    
    single {
        PaymentMethodUseCases(
            getPaymentMethods = get(),
            addPaymentMethod = get(),
            updatePaymentMethod = get(),
            deletePaymentMethod = get()
        )
    }
    
    // ViewModels
    viewModel { PaymentMethodsViewModel(get(), get()) }
    viewModel { AddPaymentMethodViewModel(get(),get()) }
}

