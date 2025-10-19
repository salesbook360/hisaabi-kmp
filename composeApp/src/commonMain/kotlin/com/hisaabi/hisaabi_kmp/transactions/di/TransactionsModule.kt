package com.hisaabi.hisaabi_kmp.transactions.di

import com.hisaabi.hisaabi_kmp.database.datasource.TransactionLocalDataSource
import com.hisaabi.hisaabi_kmp.transactions.data.repository.TransactionsRepository
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.*
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddTransactionViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.TransactionsListViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val transactionsModule = module {
    // Data Source
    single { 
        TransactionLocalDataSource(
            transactionDao = get(),
            transactionDetailDao = get()
        )
    }
    
    // Repository
    single { TransactionsRepository(get()) }
    
    // Use Cases
    singleOf(::GetTransactionsUseCase)
    singleOf(::AddTransactionUseCase)
    singleOf(::UpdateTransactionUseCase)
    singleOf(::DeleteTransactionUseCase)
    
    // Use Cases Aggregator
    single {
        TransactionUseCases(
            getTransactions = get(),
            addTransaction = get(),
            updateTransaction = get(),
            deleteTransaction = get()
        )
    }
    
    // ViewModels
    viewModel { AddTransactionViewModel(get()) }
    viewModel { TransactionsListViewModel(get()) }
}

