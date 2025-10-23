package com.hisaabi.hisaabi_kmp.business.domain.usecase

import com.hisaabi.hisaabi_kmp.business.data.datasource.BusinessPreferencesDataSource
import com.hisaabi.hisaabi_kmp.business.data.repository.BusinessRepository
import com.hisaabi.hisaabi_kmp.business.domain.model.Business
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case to get the currently selected business.
 * Returns null if no business is selected or if the selected business doesn't exist.
 */
class GetSelectedBusinessUseCase(
    private val repository: BusinessRepository,
    private val preferencesDataSource: BusinessPreferencesDataSource
) {
    /**
     * Get the selected business as a one-time value.
     */
    suspend operator fun invoke(): Business? {
        val selectedId = preferencesDataSource.getSelectedBusinessId() ?: return null
        return repository.getBusinessById(selectedId)
    }
    
    /**
     * Observe the selected business as a Flow.
     * Emits null if no business is selected or if selected business is deleted.
     */
    fun observe(): Flow<Business?> {
        return preferencesDataSource.observeSelectedBusinessId().map { selectedId ->
            if (selectedId == null) {
                null
            } else {
                repository.getBusinessById(selectedId)
            }
        }
    }
}

