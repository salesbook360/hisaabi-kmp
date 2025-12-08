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
    
    suspend fun getUnitsByParentSuspend(parentSlug: String): List<QuantityUnitEntity> {
        return quantityUnitDao.getUnitsByParentSuspend(parentSlug)
    }
    
    // Get all parent unit types (Unit Types like Weight, Quantity, Liquid, Length)
    fun getParentUnitTypes(businessSlug: String): Flow<List<QuantityUnitEntity>> {
        return quantityUnitDao.getParentUnitTypes(businessSlug)
    }
    
    suspend fun getParentUnitTypesSuspend(businessSlug: String): List<QuantityUnitEntity> {
        return quantityUnitDao.getParentUnitTypesSuspend(businessSlug)
    }
    
    // Get all child units (units that belong to a parent unit type)
    fun getChildUnits(businessSlug: String): Flow<List<QuantityUnitEntity>> {
        return quantityUnitDao.getChildUnits(businessSlug)
    }
    
    suspend fun getChildUnitsSuspend(businessSlug: String): List<QuantityUnitEntity> {
        return quantityUnitDao.getChildUnitsSuspend(businessSlug)
    }
    
    fun getUnitsByBusiness(businessSlug: String): Flow<List<QuantityUnitEntity>> {
        return quantityUnitDao.getUnitsByBusiness(businessSlug)
    }
    
    suspend fun getUnsyncedUnits(businessSlug: String): List<QuantityUnitEntity> {
        return quantityUnitDao.getUnsyncedUnits(businessSlug)
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
    
    suspend fun updateBaseConversionUnit(parentSlug: String, baseUnitSlug: String) {
        quantityUnitDao.updateBaseConversionUnit(parentSlug, baseUnitSlug)
    }
}

