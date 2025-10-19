package com.hisaabi.hisaabi_kmp.paymentmethods.domain.usecase

import com.hisaabi.hisaabi_kmp.paymentmethods.data.repository.PaymentMethodsRepository
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod

class DeletePaymentMethodUseCase(
    private val repository: PaymentMethodsRepository
) {
    suspend operator fun invoke(paymentMethod: PaymentMethod): Result<Unit> {
        return repository.deletePaymentMethod(paymentMethod)
    }
    
    suspend fun deleteById(id: Int): Result<Unit> {
        return repository.deletePaymentMethodById(id)
    }
}

