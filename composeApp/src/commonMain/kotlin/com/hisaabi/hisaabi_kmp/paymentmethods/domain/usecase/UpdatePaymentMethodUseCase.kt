package com.hisaabi.hisaabi_kmp.paymentmethods.domain.usecase

import com.hisaabi.hisaabi_kmp.paymentmethods.data.repository.PaymentMethodsRepository
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import kotlinx.datetime.Clock

class UpdatePaymentMethodUseCase(
    private val repository: PaymentMethodsRepository
) {
    suspend operator fun invoke(paymentMethod: PaymentMethod): Result<Unit> {
        // Validate input
        if (paymentMethod.title.isBlank()) {
            return Result.failure(IllegalArgumentException("Title cannot be empty"))
        }
        
        val now = Clock.System.now().toString()
        
        val updatedPaymentMethod = paymentMethod.copy(
            syncStatus = 1, // Needs sync
            updatedAt = now
        )
        
        return repository.updatePaymentMethod(updatedPaymentMethod)
    }
}

