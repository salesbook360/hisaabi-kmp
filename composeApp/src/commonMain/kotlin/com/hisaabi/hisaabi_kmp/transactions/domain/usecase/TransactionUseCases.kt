package com.hisaabi.hisaabi_kmp.transactions.domain.usecase

data class TransactionUseCases(
    val getTransactions: GetTransactionsUseCase,
    val addTransaction: AddTransactionUseCase,
    val updateTransaction: UpdateTransactionUseCase,
    val deleteTransaction: DeleteTransactionUseCase
)

