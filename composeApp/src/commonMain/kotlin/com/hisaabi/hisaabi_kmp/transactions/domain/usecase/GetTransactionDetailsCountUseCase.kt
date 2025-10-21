package com.hisaabi.hisaabi_kmp.transactions.domain.usecase

import com.hisaabi.hisaabi_kmp.transactions.data.repository.TransactionsRepository

class GetTransactionDetailsCountUseCase(
    private val repository: TransactionsRepository
) {
    suspend operator fun invoke(transactionSlug: String): Int {
        return repository.getTransactionDetailsCount(transactionSlug)
    }
}
