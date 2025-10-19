package com.hisaabi.hisaabi_kmp.products.domain.usecase

import com.hisaabi.hisaabi_kmp.products.data.repository.ProductsRepository
import com.hisaabi.hisaabi_kmp.products.domain.model.Product
import com.hisaabi.hisaabi_kmp.products.domain.model.ProductType

class GetProductsUseCase(
    private val repository: ProductsRepository
) {
    suspend operator fun invoke(
        businessSlug: String,
        productType: ProductType? = null
    ): Result<List<Product>> {
        return try {
            val products = repository.getProducts(businessSlug, productType)
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


