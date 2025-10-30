package com.hisaabi.hisaabi_kmp.parties.data.repository

import com.hisaabi.hisaabi_kmp.core.domain.model.EntityTypeEnum
import com.hisaabi.hisaabi_kmp.core.util.SlugGenerator
import com.hisaabi.hisaabi_kmp.database.dao.PartyDao
import com.hisaabi.hisaabi_kmp.database.entity.PartyEntity
import com.hisaabi.hisaabi_kmp.parties.domain.model.PartiesFilter
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.utils.getCurrentTimestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface PartiesRepository {
    suspend fun searchParties(
        roleIds: List<Int>,
        businessSlug: String,
        searchQuery: String?,
        filter: PartiesFilter,
        pageSize: Int,
        pageNumber: Int
    ): List<Party>
    
    suspend fun getPartiesCount(
        roleIds: List<Int>,
        businessSlug: String,
        searchQuery: String?
    ): Int
    
    suspend fun getTotalBalance(
        roleIds: List<Int>,
        businessSlug: String
    ): Double
    
    suspend fun addParty(
        party: Party,
        businessSlug: String,
        userSlug: String
    ): String
    
    suspend fun updateParty(party: Party): String
    
    suspend fun deleteParty(partySlug: String)
    
    suspend fun getPartyBySlug(slug: String): Party?
}

class PartiesRepositoryImpl(
    private val partyDao: PartyDao,
    private val slugGenerator: SlugGenerator
) : PartiesRepository {
    
    override suspend fun searchParties(
        roleIds: List<Int>,
        businessSlug: String,
        searchQuery: String?,
        filter: PartiesFilter,
        pageSize: Int,
        pageNumber: Int
    ): List<Party> {
        val offset = pageNumber * pageSize
        val query = searchQuery?.takeIf { it.isNotBlank() }
        
        val entities = when (filter) {
            PartiesFilter.ALL_PARTIES -> partyDao.searchParties(
                roleIds, businessSlug, query, pageSize, offset
            )
            PartiesFilter.BALANCE_PAYABLE -> partyDao.searchPartiesPayable(
                roleIds, businessSlug, query, pageSize, offset
            )
            PartiesFilter.BALANCE_RECEIVABLE -> partyDao.searchPartiesReceivable(
                roleIds, businessSlug, query, pageSize, offset
            )
            PartiesFilter.BALANCE_ZERO -> partyDao.searchPartiesZeroBalance(
                roleIds, businessSlug, query, pageSize, offset
            )
        }
        
        return entities.map { it.toDomainModel() }
    }
    
    override suspend fun getPartiesCount(
        roleIds: List<Int>,
        businessSlug: String,
        searchQuery: String?
    ): Int {
        val query = searchQuery?.takeIf { it.isNotBlank() }
        return partyDao.getPartiesCount(roleIds, businessSlug, query)
    }
    
    override suspend fun getTotalBalance(
        roleIds: List<Int>,
        businessSlug: String
    ): Double {
        return partyDao.getTotalBalance(roleIds, businessSlug) ?: 0.0
    }
    
    override suspend fun addParty(
        party: Party,
        businessSlug: String,
        userSlug: String
    ): String {
        // Generate slug using centralized slug generator
        val slug = slugGenerator.generateSlug(EntityTypeEnum.ENTITY_TYPE_PARTY)
            ?: throw IllegalStateException("Failed to generate slug: Invalid session context")
        
        // Get current timestamp for both created_at and updated_at
        val now = getCurrentTimestamp()
        
        // Create entity with generated slug, business info, and timestamps
        val entity = party.toEntity().copy(
            slug = slug,
            business_slug = businessSlug,
            created_by = userSlug,
            created_at = now,
            updated_at = now
        )
        
        partyDao.insertParty(entity)
        return slug
    }
    
    override suspend fun updateParty(party: Party): String {
        // Update only the updated_at timestamp, preserve created_at
        val entity = party.toEntity().copy(
            updated_at = getCurrentTimestamp()
        )
        partyDao.updateParty(entity)
        return party.slug
    }
    
    override suspend fun deleteParty(partySlug: String) {
        val party = partyDao.getPartyBySlug(partySlug)
        if (party != null) {
            partyDao.deleteParty(party)
        }
    }
    
    override suspend fun getPartyBySlug(slug: String): Party? {
        return partyDao.getPartyBySlug(slug)?.toDomainModel()
    }
}

// Extension function to convert entity to domain model
fun PartyEntity.toDomainModel(): Party {
    return Party(
        id = id,
        name = name ?: "",
        phone = phone,
        address = address,
        balance = balance,
        openingBalance = opening_balance,
        thumbnail = thumbnail,
        roleId = role_id,
        personStatus = person_status,
        digitalId = digital_id,
        latLong = lat_long,
        areaSlug = area_slug,
        categorySlug = category_slug,
        email = email,
        description = description,
        slug = slug ?: "",
        businessSlug = business_slug,
        createdBy = created_by,
        syncStatus = sync_status,
        createdAt = created_at,
        updatedAt = updated_at
    )
}

// Extension function to convert domain model to entity
fun Party.toEntity(): PartyEntity {
    return PartyEntity(
        id = id,
        name = name,
        phone = phone,
        address = address,
        balance = balance,
        opening_balance = openingBalance,
        thumbnail = thumbnail,
        role_id = roleId,
        person_status = personStatus,
        digital_id = digitalId,
        lat_long = latLong,
        area_slug = areaSlug,
        category_slug = categorySlug,
        email = email,
        description = description,
        slug = slug,
        business_slug = businessSlug,
        created_by = createdBy,
        sync_status = syncStatus,
        created_at = createdAt,
        updated_at = updatedAt
    )
}


