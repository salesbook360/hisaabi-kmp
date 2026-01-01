package com.hisaabi.hisaabi_kmp.database.dao

import androidx.room.*
import com.hisaabi.hisaabi_kmp.database.entity.PaymentMethodEntity
import com.hisaabi.hisaabi_kmp.sync.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodDao {
    companion object {
        private const val SYNCED_STATUS = SyncStatus.SYNCED_VALUE
    }
    @Query("SELECT * FROM PaymentMethod")
    fun getAllPaymentMethods(): Flow<List<PaymentMethodEntity>>
    
    @Query("SELECT * FROM PaymentMethod WHERE id = :id")
    suspend fun getPaymentMethodById(id: Int): PaymentMethodEntity?
    
    @Query("SELECT * FROM PaymentMethod WHERE slug = :slug")
    suspend fun getPaymentMethodBySlug(slug: String): PaymentMethodEntity?
    
    @Query("SELECT * FROM PaymentMethod WHERE status_id = :statusId")
    fun getPaymentMethodsByStatus(statusId: Int): Flow<List<PaymentMethodEntity>>
    
    @Query("SELECT * FROM PaymentMethod WHERE business_slug = :businessSlug")
    fun getPaymentMethodsByBusiness(businessSlug: String): Flow<List<PaymentMethodEntity>>
    
    @Query("SELECT * FROM PaymentMethod WHERE sync_status != $SYNCED_STATUS AND business_slug = :businessSlug")
    suspend fun getUnsyncedPaymentMethods(businessSlug: String): List<PaymentMethodEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethod(paymentMethod: PaymentMethodEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethods(paymentMethods: List<PaymentMethodEntity>)
    
    @Update
    suspend fun updatePaymentMethod(paymentMethod: PaymentMethodEntity)
    
    @Delete
    suspend fun deletePaymentMethod(paymentMethod: PaymentMethodEntity)
    
    @Query("DELETE FROM PaymentMethod WHERE id = :id")
    suspend fun deletePaymentMethodById(id: Int)
    
    @Query("DELETE FROM PaymentMethod")
    suspend fun deleteAllPaymentMethods()
    
    // Dashboard Queries
    @Query("""
        SELECT SUM(amount) FROM PaymentMethod 
        WHERE status_id != 2 
        AND business_slug = :businessSlug
    """)
    suspend fun getTotalCashInHand(businessSlug: String): Double?
    
    @Query("""
        SELECT SUM(opening_amount) FROM PaymentMethod 
        WHERE status_id != 2 
        AND business_slug = :businessSlug
    """)
    suspend fun getTotalOpeningAmount(businessSlug: String): Double?
    
    @Query("SELECT MAX(id) FROM PaymentMethod")
    suspend fun getMaxId(): Int?
}

