package com.hisaabi.hisaabi_kmp.products.domain.usecase

import com.hisaabi.hisaabi_kmp.products.data.repository.ProductsRepository
import com.hisaabi.hisaabi_kmp.products.domain.model.Product

class UpdateProductUseCase(
    private val repository: ProductsRepository
) {
    suspend operator fun invoke(product: Product): Result<String> {
        return try {
            val slug = repository.updateProduct(product)
            Result.success(slug)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


