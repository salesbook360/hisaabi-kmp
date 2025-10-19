package com.hisaabi.hisaabi_kmp.warehouses.domain.usecase

data class WarehouseUseCases(
    val getWarehouses: GetWarehousesUseCase,
    val addWarehouse: AddWarehouseUseCase,
    val updateWarehouse: UpdateWarehouseUseCase,
    val deleteWarehouse: DeleteWarehouseUseCase
)

