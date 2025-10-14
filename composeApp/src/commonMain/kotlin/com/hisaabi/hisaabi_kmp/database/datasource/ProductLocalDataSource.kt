package com.hisaabi.hisaabi_kmp.database.datasource

import com.hisaabi.hisaabi_kmp.database.dao.ProductDao
import com.hisaabi.hisaabi_kmp.database.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

interface ProductLocalDataSource {
    fun getAllProducts(): Flow<List<ProductEntity>>
    suspend fun getProductById(id: Int): ProductEntity?
    suspend fun getProductBySlug(slug: String): ProductEntity?
    fun getProductsByCategory(categorySlug: String): Flow<List<ProductEntity>>
    fun getProductsByBusiness(businessSlug: String): Flow<List<ProductEntity>>
    suspend fun getUnsyncedProducts(): List<ProductEntity>
    fun getProductsByStatus(statusId: Int): Flow<List<ProductEntity>>
    suspend fun insertProduct(product: ProductEntity): Long
    suspend fun insertProducts(products: List<ProductEntity>)
    suspend fun updateProduct(product: ProductEntity)
    suspend fun deleteProduct(product: ProductEntity)
    suspend fun deleteProductById(id: Int)
    suspend fun deleteAllProducts()
}

class ProductLocalDataSourceImpl(
    private val productDao: ProductDao
) : ProductLocalDataSource {
    override fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()
    
    override suspend fun getProductById(id: Int): ProductEntity? = productDao.getProductById(id)
    
    override suspend fun getProductBySlug(slug: String): ProductEntity? = productDao.getProductBySlug(slug)
    
    override fun getProductsByCategory(categorySlug: String): Flow<List<ProductEntity>> = 
        productDao.getProductsByCategory(categorySlug)
    
    override fun getProductsByBusiness(businessSlug: String): Flow<List<ProductEntity>> = 
        productDao.getProductsByBusiness(businessSlug)
    
    override suspend fun getUnsyncedProducts(): List<ProductEntity> = 
        productDao.getUnsyncedProducts()
    
    override fun getProductsByStatus(statusId: Int): Flow<List<ProductEntity>> = 
        productDao.getProductsByStatus(statusId)
    
    override suspend fun insertProduct(product: ProductEntity): Long = 
        productDao.insertProduct(product)
    
    override suspend fun insertProducts(products: List<ProductEntity>) = 
        productDao.insertProducts(products)
    
    override suspend fun updateProduct(product: ProductEntity) = 
        productDao.updateProduct(product)
    
    override suspend fun deleteProduct(product: ProductEntity) = 
        productDao.deleteProduct(product)
    
    override suspend fun deleteProductById(id: Int) = 
        productDao.deleteProductById(id)
    
    override suspend fun deleteAllProducts() = 
        productDao.deleteAllProducts()
}

