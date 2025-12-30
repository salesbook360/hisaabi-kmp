package com.hisaabi.hisaabi_kmp.parties.domain.usecase

import com.hisaabi.hisaabi_kmp.parties.data.repository.PartiesRepository
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party

class DeletePartyUseCase(
    private val repository: PartiesRepository
) {
    suspend operator fun invoke(party: Party): Result<Unit> {
        return repository.softDeleteParty(party)
    }
}

