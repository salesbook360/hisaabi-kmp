package com.hisaabi.hisaabi_kmp.business.domain.usecase

import com.hisaabi.hisaabi_kmp.business.data.repository.BusinessRepository
import com.hisaabi.hisaabi_kmp.business.domain.model.Business
import kotlinx.coroutines.flow.Flow

class GetBusinessesUseCase(
    private val repository: BusinessRepository
) {
    operator fun invoke(): Flow<List<Business>> {
        return repository.getAllBusinesses()
    }
    
    suspend fun getBusinessById(id: Int): Business? {
        return repository.getBusinessById(id)
    }
}

