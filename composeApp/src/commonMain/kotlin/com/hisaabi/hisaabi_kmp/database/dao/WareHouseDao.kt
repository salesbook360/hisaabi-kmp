package com.hisaabi.hisaabi_kmp.database.dao

import androidx.room.*
import com.hisaabi.hisaabi_kmp.database.entity.WareHouseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WareHouseDao {
    @Query("SELECT * FROM WareHouse")
    fun getAllWareHouses(): Flow<List<WareHouseEntity>>
    
    @Query("SELECT * FROM WareHouse WHERE id = :id")
    suspend fun getWareHouseById(id: Int): WareHouseEntity?
    
    @Query("SELECT * FROM WareHouse WHERE slug = :slug")
    suspend fun getWareHouseBySlug(slug: String): WareHouseEntity?
    
    @Query("SELECT * FROM WareHouse WHERE type_id = :typeId")
    fun getWareHousesByType(typeId: Int): Flow<List<WareHouseEntity>>
    
    @Query("SELECT * FROM WareHouse WHERE business_slug = :businessSlug")
    fun getWareHousesByBusiness(businessSlug: String): Flow<List<WareHouseEntity>>
    
    @Query("SELECT * FROM WareHouse WHERE business_slug = :businessSlug and sync_status != 0")
    suspend fun getUnsyncedWareHouses(businessSlug: String): List<WareHouseEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWareHouse(wareHouse: WareHouseEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWareHouses(wareHouses: List<WareHouseEntity>)
    
    @Update
    suspend fun updateWareHouse(wareHouse: WareHouseEntity)
    
    @Delete
    suspend fun deleteWareHouse(wareHouse: WareHouseEntity)
    
    @Query("DELETE FROM WareHouse WHERE id = :id")
    suspend fun deleteWareHouseById(id: Int)
    
    @Query("DELETE FROM WareHouse")
    suspend fun deleteAllWareHouses()
}

