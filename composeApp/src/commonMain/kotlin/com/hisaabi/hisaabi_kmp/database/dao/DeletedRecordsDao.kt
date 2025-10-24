package com.hisaabi.hisaabi_kmp.database.dao

import androidx.room.*
import com.hisaabi.hisaabi_kmp.database.entity.DeletedRecordsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeletedRecordsDao {
    @Query("SELECT * FROM DeletedRecords")
    fun getAllDeletedRecords(): Flow<List<DeletedRecordsEntity>>
    
    @Query("SELECT * FROM DeletedRecords WHERE id = :id")
    suspend fun getDeletedRecordById(id: Int): DeletedRecordsEntity?
    
    @Query("SELECT * FROM DeletedRecords WHERE record_type = :recordType")
    fun getDeletedRecordsByType(recordType: String): Flow<List<DeletedRecordsEntity>>
    
    @Query("SELECT * FROM DeletedRecords WHERE sync_status != 2 AND business_slug = :businessSlug")
    suspend fun getUnsyncedDeletedRecords(businessSlug: String): List<DeletedRecordsEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeletedRecord(record: DeletedRecordsEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeletedRecords(records: List<DeletedRecordsEntity>)
    
    @Update
    suspend fun updateDeletedRecord(record: DeletedRecordsEntity)
    
    @Delete
    suspend fun deleteDeletedRecord(record: DeletedRecordsEntity)
    
    @Query("DELETE FROM DeletedRecords WHERE id = :id")
    suspend fun deleteDeletedRecordById(id: Int)
    
    @Query("DELETE FROM DeletedRecords")
    suspend fun deleteAllDeletedRecords()
}

