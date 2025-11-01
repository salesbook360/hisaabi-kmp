package com.hisaabi.hisaabi_kmp.warehouses.di

import com.hisaabi.hisaabi_kmp.database.datasource.WareHouseLocalDataSource
import com.hisaabi.hisaabi_kmp.warehouses.data.repository.WarehousesRepository
import com.hisaabi.hisaabi_kmp.warehouses.domain.usecase.*
import com.hisaabi.hisaabi_kmp.warehouses.presentation.viewmodel.AddWarehouseViewModel
import com.hisaabi.hisaabi_kmp.warehouses.presentation.viewmodel.WarehousesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val warehousesModule = module {
    // Data Source
    single { WareHouseLocalDataSource(get()) }
    
    // Repository
    single { WarehousesRepository(get()) }
    
    // Use Cases
    single { GetWarehousesUseCase(get()) }
    single { AddWarehouseUseCase(repository = get(), slugGenerator = get()) }
    single { UpdateWarehouseUseCase(get()) }
    single { DeleteWarehouseUseCase(get()) }
    
    single {
        WarehouseUseCases(
            getWarehouses = get(),
            addWarehouse = get(),
            updateWarehouse = get(),
            deleteWarehouse = get()
        )
    }
    
    // ViewModels
    viewModel { WarehousesViewModel(get(), get()) }
    viewModel { AddWarehouseViewModel(get(), get()) }
}

