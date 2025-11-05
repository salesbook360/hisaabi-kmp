package com.hisaabi.hisaabi_kmp.database.dao

import androidx.room.*
import com.hisaabi.hisaabi_kmp.database.entity.ProductQuantitiesEntity

/**
 * Centralized DAO for transaction processing operations.
 * Handles all balance updates for products, parties, and payment methods.
 */
@Dao
interface TransactionProcessorDao {
    
    // ==================== Product Stock Operations ====================
    
    @Query("""
        SELECT current_quantity FROM ProductQuantities 
        WHERE product_slug = :productSlug 
        AND warehouse_slug = :wareHouseSlug
    """)
    suspend fun getAvailableQuantity(
        productSlug: String?,
        wareHouseSlug: String?
    ): Double?
    
    @Query("""
        UPDATE ProductQuantities 
        SET current_quantity = current_quantity + :quantityToAdd 
        WHERE product_slug = :productSlug 
        AND warehouse_slug = :wareHouseSlug
    """)
    suspend fun updateProductQuantity(
        productSlug: String?,
        wareHouseSlug: String?,
        quantityToAdd: Double
    )
    
    @Query("""
        SELECT COUNT(*) FROM ProductQuantities 
        WHERE product_slug = :productSlug 
        AND warehouse_slug = :wareHouseSlug
    """)
    suspend fun checkExistingWarehouseRecord(
        productSlug: String?,
        wareHouseSlug: String?
    ): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createEmptyQuantities(productQuantity: ProductQuantitiesEntity): Long
    
    // ==================== Payment Method Operations ====================
    
    @Query("""
        UPDATE PaymentMethod 
        SET amount = amount + :amountToAdd 
        WHERE slug = :paymentMethodSlug
    """)
    suspend fun updatePaymentMethodAmount(
        paymentMethodSlug: String?,
        amountToAdd: Double
    )
    
    @Query("""
        SELECT amount FROM PaymentMethod 
        WHERE slug = :paymentMethodSlug
    """)
    suspend fun getPaymentMethodBalance(paymentMethodSlug: String?): Double?
    
    // ==================== Party Balance Operations ====================
    
    @Query("""
        UPDATE Party 
        SET balance = balance + :addUpBalance 
        WHERE slug = :partySlug
    """)
    suspend fun updatePartyBalance(
        partySlug: String?,
        addUpBalance: Double
    )
    
    @Query("""
        SELECT balance FROM Party 
        WHERE slug = :partySlug
    """)
    suspend fun getCurrentPartyBalance(partySlug: String?): Double?
}



