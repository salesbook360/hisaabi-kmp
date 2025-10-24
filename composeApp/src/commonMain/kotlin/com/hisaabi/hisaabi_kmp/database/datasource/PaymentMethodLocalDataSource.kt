package com.hisaabi.hisaabi_kmp.database.datasource

import com.hisaabi.hisaabi_kmp.database.dao.PaymentMethodDao
import com.hisaabi.hisaabi_kmp.database.entity.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow

class PaymentMethodLocalDataSource(
    private val paymentMethodDao: PaymentMethodDao
) {
    fun getAllPaymentMethods(): Flow<List<PaymentMethodEntity>> {
        return paymentMethodDao.getAllPaymentMethods()
    }
    
    suspend fun getPaymentMethodById(id: Int): PaymentMethodEntity? {
        return paymentMethodDao.getPaymentMethodById(id)
    }
    
    suspend fun getPaymentMethodBySlug(slug: String): PaymentMethodEntity? {
        return paymentMethodDao.getPaymentMethodBySlug(slug)
    }
    
    fun getPaymentMethodsByStatus(statusId: Int): Flow<List<PaymentMethodEntity>> {
        return paymentMethodDao.getPaymentMethodsByStatus(statusId)
    }
    
    fun getPaymentMethodsByBusiness(businessSlug: String): Flow<List<PaymentMethodEntity>> {
        return paymentMethodDao.getPaymentMethodsByBusiness(businessSlug)
    }
    
    suspend fun getUnsyncedPaymentMethods(businessSlug: String): List<PaymentMethodEntity> {
        return paymentMethodDao.getUnsyncedPaymentMethods(businessSlug)
    }
    
    suspend fun insertPaymentMethod(paymentMethod: PaymentMethodEntity): Long {
        return paymentMethodDao.insertPaymentMethod(paymentMethod)
    }
    
    suspend fun insertPaymentMethods(paymentMethods: List<PaymentMethodEntity>) {
        paymentMethodDao.insertPaymentMethods(paymentMethods)
    }
    
    suspend fun updatePaymentMethod(paymentMethod: PaymentMethodEntity) {
        paymentMethodDao.updatePaymentMethod(paymentMethod)
    }
    
    suspend fun deletePaymentMethod(paymentMethod: PaymentMethodEntity) {
        paymentMethodDao.deletePaymentMethod(paymentMethod)
    }
    
    suspend fun deletePaymentMethodById(id: Int) {
        paymentMethodDao.deletePaymentMethodById(id)
    }
    
    suspend fun deleteAllPaymentMethods() {
        paymentMethodDao.deleteAllPaymentMethods()
    }
    
    suspend fun getTotalCashInHand(businessSlug: String): Double {
        return paymentMethodDao.getTotalCashInHand(businessSlug) ?: 0.0
    }
}

