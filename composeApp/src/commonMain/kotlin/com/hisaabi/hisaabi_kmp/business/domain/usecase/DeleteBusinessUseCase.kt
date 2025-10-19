package com.hisaabi.hisaabi_kmp.business.domain.usecase

import com.hisaabi.hisaabi_kmp.business.data.repository.BusinessRepository
import com.hisaabi.hisaabi_kmp.business.domain.model.Business

class DeleteBusinessUseCase(
    private val repository: BusinessRepository
) {
    suspend operator fun invoke(business: Business): Result<Unit> {
        return repository.deleteBusiness(business)
    }
    
    suspend fun deleteById(id: Int): Result<Unit> {
        return repository.deleteBusinessById(id)
    }
}

