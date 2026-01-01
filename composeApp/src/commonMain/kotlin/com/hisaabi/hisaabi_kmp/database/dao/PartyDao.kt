package com.hisaabi.hisaabi_kmp.database.dao

import androidx.room.*
import com.hisaabi.hisaabi_kmp.database.entity.PartyEntity
import com.hisaabi.hisaabi_kmp.sync.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyDao {
    companion object {
        private const val SYNCED_STATUS = SyncStatus.SYNCED_VALUE
    }
    @Query("SELECT * FROM Party")
    fun getAllParties(): Flow<List<PartyEntity>>
    
    @Query("SELECT * FROM Party WHERE id = :id")
    suspend fun getPartyById(id: Int): PartyEntity?
    
    @Query("SELECT * FROM Party WHERE slug = :slug")
    suspend fun getPartyBySlug(slug: String): PartyEntity?
    
    @Query("SELECT * FROM Party WHERE role_id = :roleId")
    fun getPartiesByRole(roleId: Int): Flow<List<PartyEntity>>
    
    @Query("SELECT * FROM Party WHERE business_slug = :businessSlug")
    fun getPartiesByBusiness(businessSlug: String): Flow<List<PartyEntity>>
    
    @Query("SELECT * FROM Party WHERE sync_status != $SYNCED_STATUS AND business_slug = :businessSlug")
    suspend fun getUnsyncedParties(businessSlug: String): List<PartyEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParty(party: PartyEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParties(parties: List<PartyEntity>)
    
    @Update
    suspend fun updateParty(party: PartyEntity)
    
    @Delete
    suspend fun deleteParty(party: PartyEntity)
    
    @Query("DELETE FROM Party WHERE id = :id")
    suspend fun deletePartyById(id: Int)
    
    @Query("DELETE FROM Party")
    suspend fun deleteAllParties()
    
    // Dashboard Queries
    @Query("""
        SELECT COUNT(*) FROM Party 
        WHERE role_id = :roleId 
        AND person_status NOT IN (2, 3)
        AND business_slug = :businessSlug
    """)
    suspend fun getCountByRole(roleId: Int, businessSlug: String): Int?
    
    @Query("""
        SELECT SUM(balance) FROM Party 
        WHERE role_id IN (:roleIds) 
        AND person_status NOT IN (2, 3)
        AND business_slug = :businessSlug
    """)
    suspend fun getTotalBalance(roleIds: List<Int>, businessSlug: String): Double?
    
    @Query("""
        SELECT SUM(opening_balance) FROM Party 
        WHERE role_id IN (:roleIds) 
        AND person_status NOT IN (2, 3)
        AND business_slug = :businessSlug
    """)
    suspend fun getTotalOpeningBalance(roleIds: List<Int>, businessSlug: String): Double?
    
    @Query("SELECT MAX(id) FROM Party")
    suspend fun getMaxId(): Int?
    
    // Parties List Screen Queries
    @Query("""
        SELECT * FROM Party 
        WHERE role_id IN (:roleIds)
        AND person_status NOT IN (2, 3)
        AND business_slug = :businessSlug
        AND (:searchQuery IS NULL OR name LIKE '%' || :searchQuery || '%' 
             OR address LIKE '%' || :searchQuery || '%' 
             OR phone LIKE '%' || :searchQuery || '%')
        ORDER BY name ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchParties(
        roleIds: List<Int>,
        businessSlug: String,
        searchQuery: String?,
        limit: Int,
        offset: Int
    ): List<PartyEntity>
    
    @Query("""
        SELECT * FROM Party 
        WHERE role_id IN (:roleIds)
        AND person_status NOT IN (2, 3)
        AND business_slug = :businessSlug
        AND balance > 0
        AND (:searchQuery IS NULL OR name LIKE '%' || :searchQuery || '%' 
             OR address LIKE '%' || :searchQuery || '%' 
             OR phone LIKE '%' || :searchQuery || '%')
        ORDER BY name ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchPartiesPayable(
        roleIds: List<Int>,
        businessSlug: String,
        searchQuery: String?,
        limit: Int,
        offset: Int
    ): List<PartyEntity>
    
    @Query("""
        SELECT * FROM Party 
        WHERE role_id IN (:roleIds)
        AND person_status NOT IN (2, 3)
        AND business_slug = :businessSlug
        AND balance < 0
        AND (:searchQuery IS NULL OR name LIKE '%' || :searchQuery || '%' 
             OR address LIKE '%' || :searchQuery || '%' 
             OR phone LIKE '%' || :searchQuery || '%')
        ORDER BY name ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchPartiesReceivable(
        roleIds: List<Int>,
        businessSlug: String,
        searchQuery: String?,
        limit: Int,
        offset: Int
    ): List<PartyEntity>
    
    @Query("""
        SELECT * FROM Party 
        WHERE role_id IN (:roleIds)
        AND person_status NOT IN (2, 3)
        AND business_slug = :businessSlug
        AND balance = 0
        AND (:searchQuery IS NULL OR name LIKE '%' || :searchQuery || '%' 
             OR address LIKE '%' || :searchQuery || '%' 
             OR phone LIKE '%' || :searchQuery || '%')
        ORDER BY name ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchPartiesZeroBalance(
        roleIds: List<Int>,
        businessSlug: String,
        searchQuery: String?,
        limit: Int,
        offset: Int
    ): List<PartyEntity>
    
    @Query("""
        SELECT COUNT(*) FROM Party 
        WHERE role_id IN (:roleIds)
        AND person_status NOT IN (2, 3)
        AND business_slug = :businessSlug
        AND (:searchQuery IS NULL OR name LIKE '%' || :searchQuery || '%' 
             OR address LIKE '%' || :searchQuery || '%' 
             OR phone LIKE '%' || :searchQuery || '%')
    """)
    suspend fun getPartiesCount(
        roleIds: List<Int>,
        businessSlug: String,
        searchQuery: String?
    ): Int
}

