package com.hisaabi.hisaabi_kmp.transactions.domain.usecase

import com.hisaabi.hisaabi_kmp.transactions.data.repository.TransactionsRepository
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction

class GetTransactionWithDetailsUseCase(
    private val repository: TransactionsRepository
) {
    suspend operator fun invoke(slug: String): Transaction? {
        return repository.getTransactionWithDetails(slug)
    }
}

