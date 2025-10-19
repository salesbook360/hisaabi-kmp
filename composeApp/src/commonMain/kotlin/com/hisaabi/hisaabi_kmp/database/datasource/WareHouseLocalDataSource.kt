package com.hisaabi.hisaabi_kmp.database.datasource

import com.hisaabi.hisaabi_kmp.database.dao.WareHouseDao
import com.hisaabi.hisaabi_kmp.database.entity.WareHouseEntity
import kotlinx.coroutines.flow.Flow

class WareHouseLocalDataSource(
    private val wareHouseDao: WareHouseDao
) {
    fun getAllWareHouses(): Flow<List<WareHouseEntity>> {
        return wareHouseDao.getAllWareHouses()
    }
    
    suspend fun getWareHouseById(id: Int): WareHouseEntity? {
        return wareHouseDao.getWareHouseById(id)
    }
    
    suspend fun getWareHouseBySlug(slug: String): WareHouseEntity? {
        return wareHouseDao.getWareHouseBySlug(slug)
    }
    
    fun getWareHousesByType(typeId: Int): Flow<List<WareHouseEntity>> {
        return wareHouseDao.getWareHousesByType(typeId)
    }
    
    fun getWareHousesByBusiness(businessSlug: String): Flow<List<WareHouseEntity>> {
        return wareHouseDao.getWareHousesByBusiness(businessSlug)
    }
    
    suspend fun getUnsyncedWareHouses(): List<WareHouseEntity> {
        return wareHouseDao.getUnsyncedWareHouses()
    }
    
    suspend fun insertWareHouse(wareHouse: WareHouseEntity): Long {
        return wareHouseDao.insertWareHouse(wareHouse)
    }
    
    suspend fun insertWareHouses(wareHouses: List<WareHouseEntity>) {
        wareHouseDao.insertWareHouses(wareHouses)
    }
    
    suspend fun updateWareHouse(wareHouse: WareHouseEntity) {
        wareHouseDao.updateWareHouse(wareHouse)
    }
    
    suspend fun deleteWareHouse(wareHouse: WareHouseEntity) {
        wareHouseDao.deleteWareHouse(wareHouse)
    }
    
    suspend fun deleteWareHouseById(id: Int) {
        wareHouseDao.deleteWareHouseById(id)
    }
    
    suspend fun deleteAllWareHouses() {
        wareHouseDao.deleteAllWareHouses()
    }
}

