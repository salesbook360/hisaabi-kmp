package com.hisaabi.hisaabi_kmp.products.domain.usecase

import com.hisaabi.hisaabi_kmp.products.data.repository.ProductsRepository
import com.hisaabi.hisaabi_kmp.utils.getCurrentTimestamp

class DeleteProductUseCase(
    private val repository: ProductsRepository
) {
    suspend operator fun invoke(productSlug: String): Result<Unit> {
        return try {
            // Get the product to delete (including soft-deleted ones)
            val product = repository.getProductBySlugAnyStatus(productSlug) 
                ?: return Result.failure(IllegalArgumentException("Product not found"))
            
            // Perform soft delete by updating status_id to 2
            val updatedProduct = product.copy(
                statusId = 2, // 2 = Deleted
                updatedAt = getCurrentTimestamp()
            )
            repository.updateProduct(updatedProduct)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

