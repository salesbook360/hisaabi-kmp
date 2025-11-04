package com.hisaabi.hisaabi_kmp.transactions.domain.usecase

import com.hisaabi.hisaabi_kmp.transactions.data.repository.TransactionsRepository
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction

class GetChildTransactionsUseCase(
    private val repository: TransactionsRepository
) {
    suspend operator fun invoke(parentSlug: String): List<Transaction> {
        return repository.getChildTransactions(parentSlug)
    }
}

