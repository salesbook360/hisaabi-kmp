package com.hisaabi.hisaabi_kmp.database.datasource

import com.hisaabi.hisaabi_kmp.database.dao.PartyDao
import com.hisaabi.hisaabi_kmp.database.entity.PartyEntity
import kotlinx.coroutines.flow.Flow

interface PartyLocalDataSource {
    fun getAllParties(): Flow<List<PartyEntity>>
    suspend fun getPartyById(id: Int): PartyEntity?
    suspend fun getPartyBySlug(slug: String): PartyEntity?
    fun getPartiesByRole(roleId: Int): Flow<List<PartyEntity>>
    fun getPartiesByBusiness(businessSlug: String): Flow<List<PartyEntity>>
    suspend fun getUnsyncedParties(): List<PartyEntity>
    suspend fun insertParty(party: PartyEntity): Long
    suspend fun insertParties(parties: List<PartyEntity>)
    suspend fun updateParty(party: PartyEntity)
    suspend fun deleteParty(party: PartyEntity)
    suspend fun deletePartyById(id: Int)
    suspend fun deleteAllParties()
}

class PartyLocalDataSourceImpl(
    private val partyDao: PartyDao
) : PartyLocalDataSource {
    override fun getAllParties(): Flow<List<PartyEntity>> = partyDao.getAllParties()
    
    override suspend fun getPartyById(id: Int): PartyEntity? = partyDao.getPartyById(id)
    
    override suspend fun getPartyBySlug(slug: String): PartyEntity? = partyDao.getPartyBySlug(slug)
    
    override fun getPartiesByRole(roleId: Int): Flow<List<PartyEntity>> = 
        partyDao.getPartiesByRole(roleId)
    
    override fun getPartiesByBusiness(businessSlug: String): Flow<List<PartyEntity>> = 
        partyDao.getPartiesByBusiness(businessSlug)
    
    override suspend fun getUnsyncedParties(): List<PartyEntity> = 
        partyDao.getUnsyncedParties()
    
    override suspend fun insertParty(party: PartyEntity): Long = 
        partyDao.insertParty(party)
    
    override suspend fun insertParties(parties: List<PartyEntity>) = 
        partyDao.insertParties(parties)
    
    override suspend fun updateParty(party: PartyEntity) = 
        partyDao.updateParty(party)
    
    override suspend fun deleteParty(party: PartyEntity) = 
        partyDao.deleteParty(party)
    
    override suspend fun deletePartyById(id: Int) = 
        partyDao.deletePartyById(id)
    
    override suspend fun deleteAllParties() = 
        partyDao.deleteAllParties()
}

