package com.hisaabi.hisaabi_kmp.quantityunits.domain.usecase

data class QuantityUnitUseCases(
    val getUnits: GetQuantityUnitsUseCase,
    val addUnit: AddQuantityUnitUseCase,
    val updateUnit: UpdateQuantityUnitUseCase,
    val deleteUnit: DeleteQuantityUnitUseCase
)

