package com.hisaabi.hisaabi_kmp.transactions.data.repository

import com.hisaabi.hisaabi_kmp.database.datasource.TransactionLocalDataSource
import com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity
import com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class TransactionsRepository(
    private val localDataSource: TransactionLocalDataSource
) {
    
    fun getAllTransactions(): Flow<List<Transaction>> {
        return localDataSource.getAllTransactions().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun getTransactionById(id: Int): Transaction? {
        return localDataSource.getTransactionById(id)?.toDomainModel()
    }
    
    suspend fun getTransactionBySlug(slug: String): Transaction? {
        return localDataSource.getTransactionBySlug(slug)?.toDomainModel()
    }
    
    fun getTransactionsByCustomer(customerSlug: String): Flow<List<Transaction>> {
        return localDataSource.getTransactionsByCustomer(customerSlug).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getTransactionsByType(transactionType: Int): Flow<List<Transaction>> {
        return localDataSource.getTransactionsByType(transactionType).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun getTransactionWithDetails(slug: String): Transaction? {
        val transaction = localDataSource.getTransactionBySlug(slug) ?: return null
        val details = mutableListOf<TransactionDetail>()
        
        localDataSource.getDetailsByTransaction(slug).collect { detailEntities ->
            details.addAll(detailEntities.map { it.toDomainModel() })
        }
        
        return transaction.toDomainModel(details)
    }
    
    suspend fun insertTransaction(transaction: Transaction): Result<String> {
        return try {
            val slug = transaction.slug ?: generateSlug()
            val entity = transaction.toEntity(slug)
            
            // Insert transaction
            localDataSource.insertTransaction(entity)
            
            // Insert transaction details
            val detailEntities = transaction.transactionDetails.map { detail ->
                detail.toEntity(slug)
            }
            localDataSource.insertTransactionDetails(detailEntities)
            
            Result.success(slug)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        return try {
            val slug = transaction.slug ?: return Result.failure(Exception("Transaction slug is required"))
            val entity = transaction.toEntity(slug)
            
            // Update transaction
            localDataSource.updateTransaction(entity)
            
            // Delete old details and insert new ones
            localDataSource.deleteDetailsByTransaction(slug)
            val detailEntities = transaction.transactionDetails.map { detail ->
                detail.toEntity(slug)
            }
            localDataSource.insertTransactionDetails(detailEntities)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteTransaction(transaction: Transaction): Result<Unit> {
        return try {
            transaction.slug?.let { slug ->
                localDataSource.deleteDetailsByTransaction(slug)
            }
            localDataSource.deleteTransactionById(transaction.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun generateSlug(): String {
        return UUID.randomUUID().toString().substring(0, 8).uppercase()
    }
    
    // Entity to Domain Model mapping
    private fun InventoryTransactionEntity.toDomainModel(details: List<TransactionDetail> = emptyList()): Transaction {
        return Transaction(
            id = id,
            customerSlug = customer_slug,
            parentSlug = parent_slug,
            totalBill = total_bill,
            totalPaid = total_paid,
            timestamp = timestamp,
            flatDiscount = discount,
            discountTypeId = discount_type_id,
            flatTax = tax,
            taxTypeId = tax_type_id,
            additionalCharges = additional_charges,
            additionalChargesType = additional_charges_slug,
            additionalChargesDesc = additional_charges_desc,
            paymentMethodToSlug = payment_method_to_slug,
            paymentMethodFromSlug = payment_method_from_slug,
            transactionType = transaction_type,
            priceTypeId = price_type_id,
            description = description,
            shippingAddress = shipping_address,
            statusId = status_id,
            stateId = state_id,
            remindAtMilliseconds = remind_at_milliseconds,
            wareHouseSlugFrom = ware_house_slug_from,
            wareHouseSlugTo = ware_house_slug_to,
            transactionDetails = details,
            slug = slug,
            businessSlug = business_slug,
            createdBy = created_by,
            syncStatus = sync_status,
            createdAt = created_at,
            updatedAt = updated_at
        )
    }
    
    private fun TransactionDetailEntity.toDomainModel(): TransactionDetail {
        return TransactionDetail(
            id = id,
            transactionSlug = transaction_slug,
            productSlug = product_slug,
            quantity = quantity,
            price = price,
            flatTax = flat_tax,
            taxType = tax_type,
            flatDiscount = flat_discount,
            discountType = discount_type,
            profit = profit,
            description = description,
            quantityUnitSlug = quantity_unit_slug,
            recipeSlug = recipe_slug,
            slug = slug,
            businessSlug = business_slug,
            createdBy = created_by,
            syncStatus = sync_status,
            createdAt = created_at,
            updatedAt = updated_at
        )
    }
    
    // Domain Model to Entity mapping
    private fun Transaction.toEntity(transactionSlug: String): InventoryTransactionEntity {
        return InventoryTransactionEntity(
            id = id,
            customer_slug = customerSlug,
            parent_slug = parentSlug,
            total_bill = totalBill,
            total_paid = totalPaid,
            timestamp = timestamp ?: System.currentTimeMillis().toString(),
            discount = flatDiscount,
            payment_method_to_slug = paymentMethodToSlug,
            payment_method_from_slug = paymentMethodFromSlug,
            transaction_type = transactionType,
            price_type_id = priceTypeId,
            additional_charges_slug = additionalChargesType,
            additional_charges_desc = additionalChargesDesc,
            additional_charges = additionalCharges,
            discount_type_id = discountTypeId,
            tax_type_id = taxTypeId,
            tax = flatTax,
            description = description,
            shipping_address = shippingAddress,
            status_id = statusId,
            state_id = stateId,
            remind_at_milliseconds = remindAtMilliseconds,
            ware_house_slug_from = wareHouseSlugFrom,
            ware_house_slug_to = wareHouseSlugTo,
            slug = transactionSlug,
            business_slug = businessSlug,
            created_by = createdBy,
            sync_status = syncStatus,
            created_at = createdAt,
            updated_at = updatedAt
        )
    }
    
    private fun TransactionDetail.toEntity(transactionSlug: String): TransactionDetailEntity {
        return TransactionDetailEntity(
            id = id,
            transaction_slug = transactionSlug,
            flat_tax = flatTax,
            tax_type = taxType,
            flat_discount = flatDiscount,
            discount_type = discountType,
            product_slug = productSlug,
            description = description,
            recipe_slug = recipeSlug,
            quantity = quantity,
            price = price,
            profit = profit,
            quantity_unit_slug = quantityUnitSlug,
            slug = slug,
            business_slug = businessSlug,
            created_by = createdBy,
            sync_status = syncStatus,
            created_at = createdAt,
            updated_at = updatedAt
        )
    }
}

