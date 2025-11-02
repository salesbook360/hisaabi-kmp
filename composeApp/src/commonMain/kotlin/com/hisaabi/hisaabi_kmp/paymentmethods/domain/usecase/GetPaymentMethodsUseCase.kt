package com.hisaabi.hisaabi_kmp.paymentmethods.domain.usecase

import com.hisaabi.hisaabi_kmp.paymentmethods.data.repository.PaymentMethodsRepository
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import kotlinx.coroutines.flow.Flow

class GetPaymentMethodsUseCase(
    private val repository: PaymentMethodsRepository
) {
    operator fun invoke(businessSlug: String?): Flow<List<PaymentMethod>> {
        return if (businessSlug != null) {
            repository.getPaymentMethodsByBusiness(businessSlug)
        } else {
            repository.getAllPaymentMethods()
        }
    }
    
    fun getActivePaymentMethods(): Flow<List<PaymentMethod>> {
        return repository.getActivePaymentMethods()
    }
}

