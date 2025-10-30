package com.hisaabi.hisaabi_kmp.parties.domain.usecase

import com.hisaabi.hisaabi_kmp.parties.data.repository.PartiesRepository
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party

class DeletePartyUseCase(
    private val repository: PartiesRepository
) {
    suspend operator fun invoke(party: Party): Result<Unit> {
        return try {
            // Soft delete by updating the party status to 2 (Deleted)
            val updatedParty = party.copy(personStatus = 2)
            repository.updateParty(updatedParty)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

