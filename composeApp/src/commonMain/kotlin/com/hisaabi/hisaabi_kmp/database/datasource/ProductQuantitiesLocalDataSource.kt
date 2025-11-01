package com.hisaabi.hisaabi_kmp.database.datasource

import com.hisaabi.hisaabi_kmp.database.dao.ProductQuantitiesDao
import com.hisaabi.hisaabi_kmp.database.entity.ProductQuantitiesEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface ProductQuantitiesLocalDataSource {
    fun getQuantitiesByWarehouse(warehouseSlug: String): Flow<List<ProductQuantitiesEntity>>
    suspend fun getQuantityByProductAndWarehouse(
        productSlug: String,
        warehouseSlug: String
    ): ProductQuantitiesEntity?
    suspend fun getQuantitiesByWarehouseList(warehouseSlug: String): List<ProductQuantitiesEntity>
    suspend fun saveProductQuantity(quantity: ProductQuantitiesEntity)
}

class ProductQuantitiesLocalDataSourceImpl(
    private val productQuantitiesDao: ProductQuantitiesDao
) : ProductQuantitiesLocalDataSource {
    
    override fun getQuantitiesByWarehouse(warehouseSlug: String): Flow<List<ProductQuantitiesEntity>> {
        return productQuantitiesDao.getQuantitiesByWarehouse(warehouseSlug)
    }
    
    override suspend fun getQuantityByProductAndWarehouse(
        productSlug: String,
        warehouseSlug: String
    ): ProductQuantitiesEntity? {
        return productQuantitiesDao.getProductQuantityByProductAndWarehouse(productSlug, warehouseSlug)
    }
    
    override suspend fun getQuantitiesByWarehouseList(warehouseSlug: String): List<ProductQuantitiesEntity> {
        return productQuantitiesDao.getQuantitiesByWarehouse(warehouseSlug).first()
    }
    
    override suspend fun saveProductQuantity(quantity: ProductQuantitiesEntity) {
        if (quantity.id > 0) {
            productQuantitiesDao.updateProductQuantity(quantity)
        } else {
            productQuantitiesDao.insertProductQuantity(quantity)
        }
    }
}

