package com.hisaabi.hisaabi_kmp.transactions.di

import com.hisaabi.hisaabi_kmp.database.datasource.TransactionLocalDataSource
import com.hisaabi.hisaabi_kmp.transactions.data.repository.TransactionsRepository
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.*
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddTransactionViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.TransactionsListViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddRecordViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.PayGetCashViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddExpenseIncomeViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.PaymentTransferViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddJournalVoucherViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.StockAdjustmentViewModel
import org.koin.core.module.dsl.singleOf
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
    singleOf(::GetTransactionDetailsCountUseCase)
    
    // Use Cases Aggregator
    single {
        TransactionUseCases(
            getTransactions = get(),
            addTransaction = get(),
            updateTransaction = get(),
            deleteTransaction = get(),
            getTransactionDetailsCount = get()
        )
    }
    
    // ViewModels
    singleOf(::AddTransactionViewModel)
    singleOf(::TransactionsListViewModel)
    singleOf(::AddRecordViewModel)
    singleOf(::PayGetCashViewModel)
    singleOf(::AddExpenseIncomeViewModel)
    singleOf(::PaymentTransferViewModel)
    singleOf(::AddJournalVoucherViewModel)
    singleOf(::StockAdjustmentViewModel)
}

