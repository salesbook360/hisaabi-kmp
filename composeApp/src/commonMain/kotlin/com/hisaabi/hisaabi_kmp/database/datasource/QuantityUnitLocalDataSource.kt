package com.hisaabi.hisaabi_kmp.database.datasource

import com.hisaabi.hisaabi_kmp.database.dao.QuantityUnitDao
import com.hisaabi.hisaabi_kmp.database.entity.QuantityUnitEntity
import kotlinx.coroutines.flow.Flow

class QuantityUnitLocalDataSource(
    private val quantityUnitDao: QuantityUnitDao
) {
    fun getAllUnits(): Flow<List<QuantityUnitEntity>> {
        return quantityUnitDao.getAllUnits()
    }
    
    suspend fun getUnitById(id: Int): QuantityUnitEntity? {
        return quantityUnitDao.getUnitById(id)
    }
    
    suspend fun getUnitBySlug(slug: String): QuantityUnitEntity? {
        return quantityUnitDao.getUnitBySlug(slug)
    }
    
    fun getUnitsByParent(parentSlug: String): Flow<List<QuantityUnitEntity>> {
        return quantityUnitDao.getUnitsByParent(parentSlug)
    }
    
    fun getUnitsByBusiness(businessSlug: String): Flow<List<QuantityUnitEntity>> {
        return quantityUnitDao.getUnitsByBusiness(businessSlug)
    }
    
    suspend fun getUnsyncedUnits(): List<QuantityUnitEntity> {
        return quantityUnitDao.getUnsyncedUnits()
    }
    
    suspend fun insertUnit(unit: QuantityUnitEntity): Long {
        return quantityUnitDao.insertUnit(unit)
    }
    
    suspend fun insertUnits(units: List<QuantityUnitEntity>) {
        quantityUnitDao.insertUnits(units)
    }
    
    suspend fun updateUnit(unit: QuantityUnitEntity) {
        quantityUnitDao.updateUnit(unit)
    }
    
    suspend fun deleteUnit(unit: QuantityUnitEntity) {
        quantityUnitDao.deleteUnit(unit)
    }
    
    suspend fun deleteUnitById(id: Int) {
        quantityUnitDao.deleteUnitById(id)
    }
    
    suspend fun deleteAllUnits() {
        quantityUnitDao.deleteAllUnits()
    }
}

