package com.hisaabi.hisaabi_kmp.database.dao

import androidx.room.*
import com.hisaabi.hisaabi_kmp.database.entity.BusinessEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {
    @Query("SELECT * FROM Business")
    fun getAllBusinesses(): Flow<List<BusinessEntity>>
    
    @Query("SELECT * FROM Business WHERE id = :id")
    suspend fun getBusinessById(id: Int): BusinessEntity?
    
    @Query("SELECT * FROM Business WHERE slug = :slug")
    suspend fun getBusinessBySlug(slug: String): BusinessEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: BusinessEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusinesses(businesses: List<BusinessEntity>)
    
    @Update
    suspend fun updateBusiness(business: BusinessEntity)
    
    @Delete
    suspend fun deleteBusiness(business: BusinessEntity)
    
    @Query("DELETE FROM Business WHERE id = :id")
    suspend fun deleteBusinessById(id: Int)
    
    @Query("DELETE FROM Business")
    suspend fun deleteAllBusinesses()
}

