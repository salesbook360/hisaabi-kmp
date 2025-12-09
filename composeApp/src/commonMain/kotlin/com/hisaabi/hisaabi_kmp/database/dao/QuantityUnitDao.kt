package com.hisaabi.hisaabi_kmp.database.dao

import androidx.room.*
import com.hisaabi.hisaabi_kmp.database.entity.QuantityUnitEntity
import com.hisaabi.hisaabi_kmp.sync.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface QuantityUnitDao {
    companion object {
        private const val SYNCED_STATUS = SyncStatus.SYNCED_VALUE
        const val PARENT_UNIT_SLUG = "0" // Parent unit types have parent_slug = "0"
    }

    @Query("SELECT * FROM QuantityUnit WHERE slug = :slug")
    suspend fun getUnitBySlug(slug: String): QuantityUnitEntity?
    
    @Query("SELECT * FROM QuantityUnit WHERE parent_slug = :parentSlug AND status_id != 2 ORDER BY sort_order ASC")
    fun getUnitsByParent(parentSlug: String): Flow<List<QuantityUnitEntity>>
    
    @Query("SELECT * FROM QuantityUnit WHERE parent_slug = :parentSlug AND status_id != 2 ORDER BY sort_order ASC")
    suspend fun getUnitsByParentSuspend(parentSlug: String): List<QuantityUnitEntity>
    
    // Get all parent unit types (Unit Types like Weight, Quantity, Liquid, Length)
    // Parent units have parent_slug = "0"
    @Query("SELECT * FROM QuantityUnit WHERE parent_slug = $PARENT_UNIT_SLUG AND business_slug = :businessSlug AND status_id != 2 ORDER BY sort_order ASC")
    fun getParentUnitTypes(businessSlug: String): Flow<List<QuantityUnitEntity>>
    

    @Query("SELECT * FROM QuantityUnit WHERE business_slug = :businessSlug AND sync_status != $SYNCED_STATUS")
    suspend fun getUnsyncedUnits(businessSlug: String): List<QuantityUnitEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: QuantityUnitEntity): Long
    

    @Update
    suspend fun updateUnit(unit: QuantityUnitEntity)
    
    @Delete
    suspend fun deleteUnit(unit: QuantityUnitEntity)
    
    @Query("DELETE FROM QuantityUnit WHERE id = :id")
    suspend fun deleteUnitById(id: Int)

    @Query("SELECT MAX(id) FROM QuantityUnit")
    suspend fun getMaxId(): Int?
    
    // Update base conversion unit for a parent unit type
    @Query("UPDATE QuantityUnit SET base_conversion_unit_slug = :baseUnitSlug WHERE slug = :parentSlug")
    suspend fun updateBaseConversionUnit(parentSlug: String, baseUnitSlug: String)
}

