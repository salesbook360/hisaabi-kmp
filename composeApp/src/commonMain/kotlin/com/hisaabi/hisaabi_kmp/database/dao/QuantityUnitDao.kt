package com.hisaabi.hisaabi_kmp.database.dao

import androidx.room.*
import com.hisaabi.hisaabi_kmp.database.entity.QuantityUnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuantityUnitDao {
    @Query("SELECT * FROM QuantityUnit ORDER BY sort_order ASC")
    fun getAllUnits(): Flow<List<QuantityUnitEntity>>
    
    @Query("SELECT * FROM QuantityUnit WHERE id = :id")
    suspend fun getUnitById(id: Int): QuantityUnitEntity?
    
    @Query("SELECT * FROM QuantityUnit WHERE slug = :slug")
    suspend fun getUnitBySlug(slug: String): QuantityUnitEntity?
    
    @Query("SELECT * FROM QuantityUnit WHERE parent_slug = :parentSlug")
    fun getUnitsByParent(parentSlug: String): Flow<List<QuantityUnitEntity>>
    
    @Query("SELECT * FROM QuantityUnit WHERE business_slug = :businessSlug")
    fun getUnitsByBusiness(businessSlug: String): Flow<List<QuantityUnitEntity>>
    
    @Query("SELECT * FROM QuantityUnit WHERE sync_status != 0")
    suspend fun getUnsyncedUnits(): List<QuantityUnitEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: QuantityUnitEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<QuantityUnitEntity>)
    
    @Update
    suspend fun updateUnit(unit: QuantityUnitEntity)
    
    @Delete
    suspend fun deleteUnit(unit: QuantityUnitEntity)
    
    @Query("DELETE FROM QuantityUnit WHERE id = :id")
    suspend fun deleteUnitById(id: Int)
    
    @Query("DELETE FROM QuantityUnit")
    suspend fun deleteAllUnits()
}

