package com.hisaabi.hisaabi_kmp.parties.data.repository

import com.hisaabi.hisaabi_kmp.common.Status
import com.hisaabi.hisaabi_kmp.core.domain.model.EntityTypeEnum
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.core.util.SlugGenerator
import com.hisaabi.hisaabi_kmp.database.dao.DeletedRecordsDao
import com.hisaabi.hisaabi_kmp.database.dao.PartyDao
import com.hisaabi.hisaabi_kmp.database.entity.DeletedRecordsEntity
import com.hisaabi.hisaabi_kmp.database.entity.PartyEntity
import com.hisaabi.hisaabi_kmp.parties.domain.model.PartiesFilter
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.sync.domain.model.SyncStatus
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
    
    suspend fun softDeleteParty(party: Party): Result<Unit>
    
    suspend fun deleteParty(partySlug: String)
    
    suspend fun getPartyBySlug(slug: String): Party?
}

class PartiesRepositoryImpl(
    private val partyDao: PartyDao,
    private val slugGenerator: SlugGenerator,
    private val deletedRecordsDao: DeletedRecordsDao,
    private val appSessionManager: AppSessionManager
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
        // Update sync status to UnSynced and updated_at timestamp, preserve created_at
        val now = getCurrentTimestamp()
        val entity = party.toEntity().copy(
            sync_status = SyncStatus.NONE.value, // UnSynced
            updated_at = now
        )
        partyDao.updateParty(entity)
        return party.slug
    }
    
    override suspend fun softDeleteParty(party: Party): Result<Unit> {
        return try {
            // Get session context for business slug and user slug
            val sessionContext = appSessionManager.getSessionContext()
            if (!sessionContext.isValid) {
                return Result.failure(IllegalStateException("Invalid session context: userSlug or businessSlug is null"))
            }
            
            val businessSlug = sessionContext.businessSlug!!
            val userSlug = sessionContext.userSlug!!
            
            // Soft delete: Update party status to DELETED
            val now = getCurrentTimestamp()
            val updatedParty = party.copy(
                personStatus = Status.DELETED.value,
                syncStatus = SyncStatus.NONE.value, // UnSynced
                updatedAt = now
            )
            val updatedEntity = updatedParty.toEntity()
            partyDao.updateParty(updatedEntity)
            
            // Add entry to DeletedRecords table
            val deletedRecordSlug = slugGenerator.generateSlug(EntityTypeEnum.ENTITY_TYPE_DELETED_RECORDS)
                ?: return Result.failure(IllegalStateException("Failed to generate slug for deleted record: Invalid session context"))
            
            val deletedRecord = DeletedRecordsEntity(
                id = 0,
                record_slug = party.slug,
                record_type = "party",
                deletion_type = "soft",
                slug = deletedRecordSlug,
                business_slug = businessSlug,
                created_by = userSlug,
                sync_status = SyncStatus.NONE.value, // UnSynced
                created_at = now,
                updated_at = now
            )
            
            deletedRecordsDao.insertDeletedRecord(deletedRecord)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
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


