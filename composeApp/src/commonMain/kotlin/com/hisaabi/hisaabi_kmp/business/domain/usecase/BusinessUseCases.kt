package com.hisaabi.hisaabi_kmp.business.domain.usecase

data class BusinessUseCases(
    val getBusinesses: GetBusinessesUseCase,
    val addBusiness: AddBusinessUseCase,
    val updateBusiness: UpdateBusinessUseCase,
    val deleteBusiness: DeleteBusinessUseCase
)

