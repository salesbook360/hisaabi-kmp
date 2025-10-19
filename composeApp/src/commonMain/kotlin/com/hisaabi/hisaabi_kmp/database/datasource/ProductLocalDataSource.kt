package com.hisaabi.hisaabi_kmp.database.datasource

import com.hisaabi.hisaabi_kmp.database.dao.ProductDao
import com.hisaabi.hisaabi_kmp.database.entity.ProductEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface ProductLocalDataSource {
    fun getAllProducts(): Flow<List<ProductEntity>>
    fun getProductsByBusiness(businessSlug: String): Flow<List<ProductEntity>>
    suspend fun getProductsByBusinessList(businessSlug: String): List<ProductEntity>
    suspend fun getProductBySlug(slug: String): ProductEntity?
    suspend fun insertProduct(product: ProductEntity): Long
    suspend fun updateProduct(product: ProductEntity)
    suspend fun deleteProduct(product: ProductEntity)
}

class ProductLocalDataSourceImpl(
    private val productDao: ProductDao
) : ProductLocalDataSource {
    
    override fun getAllProducts(): Flow<List<ProductEntity>> {
        return productDao.getAllProducts()
    }
    
    override fun getProductsByBusiness(businessSlug: String): Flow<List<ProductEntity>> {
        return productDao.getProductsByBusiness(businessSlug)
    }
    
    override suspend fun getProductsByBusinessList(businessSlug: String): List<ProductEntity> {
        return productDao.getProductsByBusiness(businessSlug).first()
    }
    
    override suspend fun getProductBySlug(slug: String): ProductEntity? {
        return productDao.getProductBySlug(slug)
    }
    
    override suspend fun insertProduct(product: ProductEntity): Long {
        return productDao.insertProduct(product)
    }
    
    override suspend fun updateProduct(product: ProductEntity) {
        productDao.updateProduct(product)
    }
    
    override suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)
    }
}
