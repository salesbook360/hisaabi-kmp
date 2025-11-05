package com.hisaabi.hisaabi_kmp.products.di

import com.hisaabi.hisaabi_kmp.database.datasource.ProductLocalDataSource
import com.hisaabi.hisaabi_kmp.database.datasource.ProductLocalDataSourceImpl
import com.hisaabi.hisaabi_kmp.products.data.repository.ProductsRepository
import com.hisaabi.hisaabi_kmp.products.data.repository.ProductsRepositoryImpl
import com.hisaabi.hisaabi_kmp.products.domain.usecase.AddProductUseCase
import com.hisaabi.hisaabi_kmp.products.domain.usecase.DeleteProductUseCase
import com.hisaabi.hisaabi_kmp.products.domain.usecase.GetProductsUseCase
import com.hisaabi.hisaabi_kmp.products.domain.usecase.UpdateProductUseCase
import com.hisaabi.hisaabi_kmp.products.presentation.viewmodel.AddProductViewModel
import com.hisaabi.hisaabi_kmp.products.presentation.viewmodel.ManageRecipeIngredientsViewModel
import com.hisaabi.hisaabi_kmp.products.presentation.viewmodel.ProductsViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val productsModule = module {
    // Data Source
    single<ProductLocalDataSource> { ProductLocalDataSourceImpl(get()) }

    // Repository
    single<ProductsRepository> {
        ProductsRepositoryImpl(
            productDataSource = get(),
            recipeIngredientsDao = get(),
            quantityUnitDao = get(),
            slugGenerator = get(),
            productQuantitiesDataSource = get()
        )
    }

    // Use Cases
    singleOf(::GetProductsUseCase)
    singleOf(::AddProductUseCase)
    singleOf(::UpdateProductUseCase)
    singleOf(::DeleteProductUseCase)

    // ViewModels
    viewModel { ProductsViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { AddProductViewModel(get(), get(), get(), get()) }
    singleOf(::ManageRecipeIngredientsViewModel)
}


