package com.hisaabi.hisaabi_kmp.database.dao

import androidx.room.*
import com.hisaabi.hisaabi_kmp.database.entity.PartyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyDao {
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
    
    @Query("SELECT * FROM Party WHERE sync_status != 0")
    suspend fun getUnsyncedParties(): List<PartyEntity>
    
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
        AND person_status != 3 
        AND business_slug = :businessSlug
    """)
    suspend fun getCountByRole(roleId: Int, businessSlug: String): Int?
    
    @Query("""
        SELECT SUM(balance) FROM Party 
        WHERE role_id IN (:roleIds) 
        AND person_status != 3 
        AND business_slug = :businessSlug
    """)
    suspend fun getTotalBalance(roleIds: List<Int>, businessSlug: String): Double?
}

