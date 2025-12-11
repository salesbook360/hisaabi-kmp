package com.hisaabi.hisaabi_kmp.transactions.di

import com.hisaabi.hisaabi_kmp.database.datasource.TransactionLocalDataSource
import com.hisaabi.hisaabi_kmp.transactions.data.repository.TransactionsRepository
import com.hisaabi.hisaabi_kmp.transactions.data.repository.TransactionProcessor
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.*
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddTransactionViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.TransactionsListViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddRecordViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.PayGetCashViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddExpenseIncomeViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.PaymentTransferViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddJournalVoucherViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.StockAdjustmentViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.TransactionDetailViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddManufactureViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val transactionsModule = module {
    // Data Source
    single { 
        TransactionLocalDataSource(
            transactionDao = get(),
            transactionDetailDao = get()
        )
    }
    
    // Transaction Processor
    single {
        TransactionProcessor(
            transactionProcessorDao = get(),
            productQuantitiesDao = get()
        )
    }
    
    // Repository
    single { 
        TransactionsRepository(
            localDataSource = get(),
            partiesRepository = get(),
            paymentMethodsRepository = get(),
            warehousesRepository = get(),
            productsRepository = get(),
            quantityUnitsRepository = get(),
            slugGenerator = get(),
            transactionProcessor = get()
        ) 
    }
    
    // Use Cases
    single { GetTransactionsUseCase(repository = get(), sessionManager = get()) }
    singleOf(::AddTransactionUseCase)
    singleOf(::UpdateTransactionUseCase)
    singleOf(::DeleteTransactionUseCase)
    singleOf(::GetTransactionDetailsCountUseCase)
    singleOf(::GetTransactionWithDetailsUseCase)
    singleOf(::GetChildTransactionsUseCase)
    
    // Use Cases Aggregator
    single {
        TransactionUseCases(
            getTransactions = get(),
            addTransaction = get(),
            updateTransaction = get(),
            deleteTransaction = get(),
            getTransactionDetailsCount = get(),
            getChildTransactions = get()
        )
    }
    
    // ViewModels - use viewModel scope instead of single to reset state when navigating
    viewModel { AddTransactionViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { TransactionsListViewModel(get()) }
    viewModel { AddRecordViewModel(get(), get()) }
    viewModel { PayGetCashViewModel(get()) }
    viewModel { AddExpenseIncomeViewModel(get()) }
    viewModel { PaymentTransferViewModel(get()) }
    viewModel { AddJournalVoucherViewModel(get()) }
    viewModel { StockAdjustmentViewModel(get()) }
    viewModel {
        TransactionDetailViewModel(
            getTransactionWithDetailsUseCase = get(),
            transactionsRepository = get()
        )
    }
    viewModel {
        AddManufactureViewModel(
            transactionsRepository = get(),
            productsRepository = get(),
            quantityUnitsRepository = get(),
            paymentMethodsRepository = get(),
            warehousesRepository = get(),
            sessionManager = get()
        )
    }
}

