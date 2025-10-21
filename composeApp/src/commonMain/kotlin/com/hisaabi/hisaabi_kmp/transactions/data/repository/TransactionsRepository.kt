package com.hisaabi.hisaabi_kmp.transactions.data.repository

import com.hisaabi.hisaabi_kmp.database.datasource.TransactionLocalDataSource
import com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity
import com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionDetail
import com.hisaabi.hisaabi_kmp.parties.data.repository.PartiesRepository
import com.hisaabi.hisaabi_kmp.paymentmethods.data.repository.PaymentMethodsRepository
import com.hisaabi.hisaabi_kmp.warehouses.data.repository.WarehousesRepository
import com.hisaabi.hisaabi_kmp.products.data.repository.ProductsRepository
import com.hisaabi.hisaabi_kmp.quantityunits.data.repository.QuantityUnitsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class TransactionsRepository(
    private val localDataSource: TransactionLocalDataSource,
    private val partiesRepository: PartiesRepository,
    private val paymentMethodsRepository: PaymentMethodsRepository,
    private val warehousesRepository: WarehousesRepository,
    private val productsRepository: ProductsRepository,
    private val quantityUnitsRepository: QuantityUnitsRepository
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
        val transactionEntity = localDataSource.getTransactionBySlug(slug) ?: return null
        
        // Load related entities
        val party = transactionEntity.customer_slug?.let { 
            partiesRepository.getPartyBySlug(it) 
        }
        
        val paymentMethodTo = transactionEntity.payment_method_to_slug?.let {
            paymentMethodsRepository.getPaymentMethodBySlug(it)
        }
        
        val paymentMethodFrom = transactionEntity.payment_method_from_slug?.let {
            paymentMethodsRepository.getPaymentMethodBySlug(it)
        }
        
        val warehouseFrom = transactionEntity.ware_house_slug_from?.let {
            warehousesRepository.getWarehouseBySlug(it)
        }
        
        val warehouseTo = transactionEntity.ware_house_slug_to?.let {
            warehousesRepository.getWarehouseBySlug(it)
        }
        
        // Get transaction details with product and quantity unit information
        val detailEntities = localDataSource.getDetailsByTransaction(slug).first()
        val details = detailEntities.map { detailEntity ->
            val product = detailEntity.product_slug?.let {
                productsRepository.getProductBySlug(it)
            }
            
            val quantityUnit = detailEntity.quantity_unit_slug?.let {
                quantityUnitsRepository.getUnitBySlug(it)
            }
            
            detailEntity.toDomainModel(product, quantityUnit)
        }
        
        return transactionEntity.toDomainModel(
            details = details,
            party = party,
            paymentMethodTo = paymentMethodTo,
            paymentMethodFrom = paymentMethodFrom,
            warehouseFrom = warehouseFrom,
            warehouseTo = warehouseTo
        )
    }
    
    suspend fun getTransactionDetailsCount(slug: String): Int {
        return localDataSource.getDetailsCountByTransaction(slug)
    }
    
    suspend fun getChildTransactions(parentSlug: String): List<Transaction> {
        val childEntities = localDataSource.getChildTransactionsList(parentSlug)
        
        return childEntities.map { entity ->
            // Load related entities for each child
            val party = entity.customer_slug?.let { 
                partiesRepository.getPartyBySlug(it) 
            }
            
            val paymentMethodTo = entity.payment_method_to_slug?.let {
                paymentMethodsRepository.getPaymentMethodBySlug(it)
            }
            
            val paymentMethodFrom = entity.payment_method_from_slug?.let {
                paymentMethodsRepository.getPaymentMethodBySlug(it)
            }
            
            entity.toDomainModel(
                party = party,
                paymentMethodTo = paymentMethodTo,
                paymentMethodFrom = paymentMethodFrom
            )
        }
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
    
    /**
     * Saves a manufacture transaction which creates 3 transactions:
     * 1. Parent manufacture transaction
     * 2. Child Sale transaction (ingredients stock out)
     * 3. Child Purchase transaction (recipe stock in)
     */
    suspend fun saveManufactureTransaction(
        recipeDetail: TransactionDetail,
        ingredients: List<TransactionDetail>,
        additionalCharges: Double,
        additionalChargesDescription: String,
        warehouseSlug: String,
        paymentMethodSlug: String?,
        timestamp: Long,
        businessSlug: String,
        userSlug: String
    ): Result<String> {
        return try {
            // 1. Create and save parent manufacture transaction
            val parentSlug = generateSlug()
            val parentTransaction = Transaction(
                id = 0,
                customerSlug = null, // No customer for manufacture
                party = null,
                priceTypeId = 1, // Purchase price type
                transactionType = 3, // MANUFACTURE type
                timestamp = timestamp.toString(),
                totalPaid = recipeDetail.calculateBill(),
                statusId = 2, // Completed
                wareHouseSlugFrom = warehouseSlug,
                warehouseFrom = null,
                wareHouseSlugTo = null,
                warehouseTo = null,
                paymentMethodFromSlug = paymentMethodSlug,
                paymentMethodFrom = null,
                paymentMethodToSlug = null,
                paymentMethodTo = null,
                additionalCharges = additionalCharges,
                additionalChargesDesc = additionalChargesDescription,
                transactionDetails = emptyList(),
                slug = parentSlug,
                businessSlug = businessSlug,
                createdBy = userSlug,
                syncStatus = 0,
                createdAt = null,
                updatedAt = null,
                parentSlug = null
            )
            
            localDataSource.insertTransaction(parentTransaction.toEntity(parentSlug))
            
            // 2. Create and save child Sale transaction (ingredients stock out)
            val saleSlug = generateSlug()
            val saleTransaction = Transaction(
                id = 0,
                customerSlug = null,
                party = null,
                priceTypeId = 1,
                transactionType = 1, // SALE type
                timestamp = (timestamp + 1).toString(), // +1ms to maintain order
                totalPaid = ingredients.sumOf { it.calculateBill() } + additionalCharges,
                statusId = 2,
                wareHouseSlugFrom = warehouseSlug,
                warehouseFrom = null,
                wareHouseSlugTo = null,
                warehouseTo = null,
                paymentMethodFromSlug = paymentMethodSlug,
                paymentMethodFrom = null,
                paymentMethodToSlug = null,
                paymentMethodTo = null,
                additionalCharges = additionalCharges,
                additionalChargesDesc = additionalChargesDescription,
                transactionDetails = emptyList(),
                slug = saleSlug,
                businessSlug = businessSlug,
                createdBy = userSlug,
                syncStatus = 0,
                createdAt = null,
                updatedAt = null,
                parentSlug = parentSlug
            )
            
            localDataSource.insertTransaction(saleTransaction.toEntity(saleSlug))
            
            // Insert ingredients as sale transaction details
            val saleDetailEntities = ingredients.map { it.toEntity(saleSlug) }
            localDataSource.insertTransactionDetails(saleDetailEntities)
            
            // 3. Create and save child Purchase transaction (recipe stock in)
            val purchaseSlug = generateSlug()
            val purchaseTransaction = Transaction(
                id = 0,
                customerSlug = null,
                party = null,
                priceTypeId = 1,
                transactionType = 2, // PURCHASE type
                timestamp = (timestamp + 2).toString(), // +2ms to maintain order
                totalPaid = recipeDetail.calculateBill(),
                statusId = 2,
                wareHouseSlugFrom = warehouseSlug,
                warehouseFrom = null,
                wareHouseSlugTo = null,
                warehouseTo = null,
                paymentMethodFromSlug = paymentMethodSlug,
                paymentMethodFrom = null,
                paymentMethodToSlug = null,
                paymentMethodTo = null,
                additionalCharges = 0.0,
                additionalChargesDesc = null,
                transactionDetails = emptyList(),
                slug = purchaseSlug,
                businessSlug = businessSlug,
                createdBy = userSlug,
                syncStatus = 0,
                createdAt = null,
                updatedAt = null,
                parentSlug = parentSlug
            )
            
            localDataSource.insertTransaction(purchaseTransaction.toEntity(purchaseSlug))
            
            // Insert recipe as purchase transaction detail
            val purchaseDetailEntity = recipeDetail.toEntity(purchaseSlug)
            localDataSource.insertTransactionDetails(listOf(purchaseDetailEntity))
            
            Result.success(parentSlug)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun generateSlug(): String {
        return UUID.randomUUID().toString().substring(0, 8).uppercase()
    }
    
    // Entity to Domain Model mapping
    private fun InventoryTransactionEntity.toDomainModel(
        details: List<TransactionDetail> = emptyList(),
        party: com.hisaabi.hisaabi_kmp.parties.domain.model.Party? = null,
        paymentMethodTo: com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod? = null,
        paymentMethodFrom: com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod? = null,
        warehouseFrom: com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse? = null,
        warehouseTo: com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse? = null
    ): Transaction {
        return Transaction(
            id = id,
            customerSlug = customer_slug,
            party = party,
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
            paymentMethodTo = paymentMethodTo,
            paymentMethodFrom = paymentMethodFrom,
            transactionType = transaction_type,
            priceTypeId = price_type_id,
            description = description,
            shippingAddress = shipping_address,
            statusId = status_id,
            stateId = state_id,
            remindAtMilliseconds = remind_at_milliseconds,
            wareHouseSlugFrom = ware_house_slug_from,
            wareHouseSlugTo = ware_house_slug_to,
            warehouseFrom = warehouseFrom,
            warehouseTo = warehouseTo,
            transactionDetails = details,
            slug = slug,
            businessSlug = business_slug,
            createdBy = created_by,
            syncStatus = sync_status,
            createdAt = created_at,
            updatedAt = updated_at
        )
    }
    
    private fun TransactionDetailEntity.toDomainModel(
        product: com.hisaabi.hisaabi_kmp.products.domain.model.Product? = null,
        quantityUnit: com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit? = null
    ): TransactionDetail {
        return TransactionDetail(
            id = id,
            transactionSlug = transaction_slug,
            productSlug = product_slug,
            product = product,
            quantity = quantity,
            price = price,
            flatTax = flat_tax,
            taxType = tax_type,
            flatDiscount = flat_discount,
            discountType = discount_type,
            profit = profit,
            description = description,
            quantityUnitSlug = quantity_unit_slug,
            quantityUnit = quantityUnit,
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

