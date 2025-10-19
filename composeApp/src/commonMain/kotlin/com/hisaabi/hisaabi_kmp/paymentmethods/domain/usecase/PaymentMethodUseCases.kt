package com.hisaabi.hisaabi_kmp.paymentmethods.domain.usecase

data class PaymentMethodUseCases(
    val getPaymentMethods: GetPaymentMethodsUseCase,
    val addPaymentMethod: AddPaymentMethodUseCase,
    val updatePaymentMethod: UpdatePaymentMethodUseCase,
    val deletePaymentMethod: DeletePaymentMethodUseCase
)

