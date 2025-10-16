package com.hisaabi.hisaabi_kmp.parties.domain.usecase

import com.hisaabi.hisaabi_kmp.parties.data.repository.PartiesRepository
import com.hisaabi.hisaabi_kmp.parties.domain.model.PartiesFilter
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment

class GetPartiesUseCase(
    private val repository: PartiesRepository
) {
    suspend operator fun invoke(
        segment: PartySegment,
        filter: PartiesFilter,
        businessSlug: String,
        searchQuery: String? = null,
        pageSize: Int = 20,
        pageNumber: Int = 0
    ): Result<List<Party>> {
        return try {
            val roleIds = segment.toPartyTypes()
            val parties = repository.searchParties(
                roleIds = roleIds,
                businessSlug = businessSlug,
                searchQuery = searchQuery,
                filter = filter,
                pageSize = pageSize,
                pageNumber = pageNumber
            )
            Result.success(parties)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class GetPartiesCountUseCase(
    private val repository: PartiesRepository
) {
    suspend operator fun invoke(
        segment: PartySegment,
        businessSlug: String,
        searchQuery: String? = null
    ): Result<Int> {
        return try {
            val roleIds = segment.toPartyTypes()
            val count = repository.getPartiesCount(
                roleIds = roleIds,
                businessSlug = businessSlug,
                searchQuery = searchQuery
            )
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class GetTotalBalanceUseCase(
    private val repository: PartiesRepository
) {
    suspend operator fun invoke(
        segment: PartySegment,
        businessSlug: String
    ): Result<Double> {
        return try {
            val roleIds = segment.toPartyTypes()
            val balance = repository.getTotalBalance(
                roleIds = roleIds,
                businessSlug = businessSlug
            )
            Result.success(balance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


