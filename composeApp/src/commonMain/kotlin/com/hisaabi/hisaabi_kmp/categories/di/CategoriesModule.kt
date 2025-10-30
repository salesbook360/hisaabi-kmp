package com.hisaabi.hisaabi_kmp.categories.di

import com.hisaabi.hisaabi_kmp.categories.data.repository.CategoriesRepository
import com.hisaabi.hisaabi_kmp.categories.data.repository.CategoriesRepositoryImpl
import com.hisaabi.hisaabi_kmp.categories.domain.usecase.AddCategoryUseCase
import com.hisaabi.hisaabi_kmp.categories.domain.usecase.GetCategoriesUseCase
import com.hisaabi.hisaabi_kmp.categories.presentation.viewmodel.AddCategoryViewModel
import com.hisaabi.hisaabi_kmp.categories.presentation.viewmodel.CategoriesViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val categoriesModule = module {
    // Repository
    single<CategoriesRepository> { 
        CategoriesRepositoryImpl(
            categoryDao = get(),
            slugGenerator = get()
        ) 
    }
    
    // Use Cases
    singleOf(::GetCategoriesUseCase)
    singleOf(::AddCategoryUseCase)
    
    // ViewModels
    singleOf(::CategoriesViewModel)
    singleOf(::AddCategoryViewModel)
}



