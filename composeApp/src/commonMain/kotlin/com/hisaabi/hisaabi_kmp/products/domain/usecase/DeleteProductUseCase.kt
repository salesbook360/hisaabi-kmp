package com.hisaabi.hisaabi_kmp.products.domain.usecase

import com.hisaabi.hisaabi_kmp.products.data.repository.ProductsRepository

class DeleteProductUseCase(
    private val repository: ProductsRepository
) {
    suspend operator fun invoke(productSlug: String): Result<Unit> {
        return try {
            // Get the product to delete (including soft-deleted ones)
            val product = repository.getProductBySlugAnyStatus(productSlug) 
                ?: return Result.failure(IllegalArgumentException("Product not found"))
            
            // Perform soft delete with deleted record entry
            repository.softDeleteProduct(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

