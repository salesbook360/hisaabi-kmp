package com.hisaabi.hisaabi_kmp.database.dao

import androidx.room.*
import com.hisaabi.hisaabi_kmp.database.entity.EntityMediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntityMediaDao {
    @Query("SELECT * FROM EntityMedia")
    fun getAllEntityMedia(): Flow<List<EntityMediaEntity>>
    
    @Query("SELECT * FROM EntityMedia WHERE id = :id")
    suspend fun getEntityMediaById(id: Int): EntityMediaEntity?
    
    @Query("SELECT * FROM EntityMedia WHERE entity_slug = :entitySlug")
    fun getMediaByEntitySlug(entitySlug: String): Flow<List<EntityMediaEntity>>
    
    @Query("SELECT * FROM EntityMedia WHERE entity_name = :entityName")
    fun getMediaByEntityName(entityName: String): Flow<List<EntityMediaEntity>>
    
    @Query("SELECT * FROM EntityMedia WHERE sync_status != 2 AND business_slug = :businessSlug")
    suspend fun getUnsyncedMedia(businessSlug: String): List<EntityMediaEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntityMedia(media: EntityMediaEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntityMediaList(mediaList: List<EntityMediaEntity>)
    
    @Update
    suspend fun updateEntityMedia(media: EntityMediaEntity)
    
    @Delete
    suspend fun deleteEntityMedia(media: EntityMediaEntity)
    
    @Query("DELETE FROM EntityMedia WHERE id = :id")
    suspend fun deleteEntityMediaById(id: Int)
    
    @Query("DELETE FROM EntityMedia WHERE entity_slug = :entitySlug")
    suspend fun deleteMediaByEntitySlug(entitySlug: String)
    
    @Query("DELETE FROM EntityMedia")
    suspend fun deleteAllEntityMedia()
}

