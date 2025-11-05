package com.hisaabi.hisaabi_kmp.products.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.products.data.repository.ProductsRepository
import com.hisaabi.hisaabi_kmp.products.domain.model.ProductType
import com.hisaabi.hisaabi_kmp.products.domain.usecase.AddProductUseCase
import com.hisaabi.hisaabi_kmp.products.domain.usecase.UpdateProductUseCase
import com.hisaabi.hisaabi_kmp.utils.getCurrentTimestamp
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.hisaabi.hisaabi_kmp.utils.format

class AddProductViewModel(
    private val addProductUseCase: AddProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val sessionManager: AppSessionManager,
    private val productsRepository: ProductsRepository
) : ViewModel() {
    
    private var businessSlug: String? = null
    private var userSlug: String? = null
    
    private val _uiState = MutableStateFlow(AddProductUiState())
    val uiState: StateFlow<AddProductUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            sessionManager.observeSessionContext().collect { context ->
                businessSlug = context.businessSlug
                userSlug = context.userSlug
            }
        }
    }
    
    fun resetState() {
        _uiState.value = AddProductUiState()
    }
    
    fun setSelectedWarehouse(warehouse: Warehouse?) {
        _uiState.value = _uiState.value.copy(selectedWarehouse = warehouse)
        if (warehouse != null && _uiState.value.productToEdit != null) {
            loadProductQuantities(_uiState.value.productToEdit!!.slug, warehouse.slug ?: "")
        } else {
            _uiState.value = _uiState.value.copy(
                openingQuantity = "",
                minimumQuantity = ""
            )
        }
    }
    
    fun setOpeningQuantity(quantity: String) {
        _uiState.value = _uiState.value.copy(openingQuantity = quantity)
    }
    
    fun setMinimumQuantity(quantity: String) {
        _uiState.value = _uiState.value.copy(minimumQuantity = quantity)
    }
    
    fun setProductToEdit(product: com.hisaabi.hisaabi_kmp.products.domain.model.Product?) {
        _uiState.value = _uiState.value.copy(productToEdit = product)
        if (product != null && _uiState.value.selectedWarehouse != null) {
            loadProductQuantities(product.slug, _uiState.value.selectedWarehouse!!.slug ?: "")
        }
    }
    
    private fun loadProductQuantities(productSlug: String, warehouseSlug: String) {
        viewModelScope.launch {
            try {
                val quantities = productsRepository.getProductQuantityForWarehouse(productSlug, warehouseSlug)
                if (quantities != null) {
                    val (opening, minimum) = quantities
                    _uiState.value = _uiState.value.copy(
                        openingQuantity = if (opening > 0) "%.2f".format(opening) else "",
                        minimumQuantity = if (minimum > 0) "%.2f".format(minimum) else ""
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        openingQuantity = "",
                        minimumQuantity = ""
                    )
                }
            } catch (e: Exception) {
                // Silently handle errors - quantities might not exist
                _uiState.value = _uiState.value.copy(
                    openingQuantity = "",
                    minimumQuantity = ""
                )
            }
        }
    }
    
    fun addProduct(
        title: String,
        description: String?,
        productType: ProductType,
        retailPrice: Double = 0.0,
        wholesalePrice: Double = 0.0,
        purchasePrice: Double = 0.0,
        taxPercentage: Double = 0.0,
        discountPercentage: Double = 0.0,
        categorySlug: String? = null,
        manufacturer: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val bSlug = businessSlug
            val uSlug = userSlug
            if (bSlug == null || uSlug == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No business or user context available"
                )
                return@launch
            }
            
            val result = addProductUseCase(
                title = title,
                description = description,
                productType = productType,
                retailPrice = retailPrice,
                wholesalePrice = wholesalePrice,
                purchasePrice = purchasePrice,
                taxPercentage = taxPercentage,
                discountPercentage = discountPercentage,
                categorySlug = categorySlug,
                manufacturer = manufacturer,
                businessSlug = bSlug,
                userSlug = uSlug
            )
            
            result.fold(
                onSuccess = { slug ->
                    println("Product added successfully with slug: $slug")
                    // Save product quantities if warehouse is selected
                    val state = _uiState.value
                    if (state.selectedWarehouse != null && bSlug != null) {
                        saveProductQuantities(
                            productSlug = slug,
                            warehouseSlug = state.selectedWarehouse!!.slug ?: "",
                            openingQuantity = state.openingQuantity.toDoubleOrNull() ?: 0.0,
                            minimumQuantity = state.minimumQuantity.toDoubleOrNull() ?: 0.0,
                            businessSlug = bSlug
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                },
                onFailure = { error ->
                    println("Failed to add product: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = error.message ?: "Failed to add product"
                    )
                }
            )
        }
    }
    
    private suspend fun saveProductQuantities(
        productSlug: String,
        warehouseSlug: String,
        openingQuantity: Double,
        minimumQuantity: Double,
        businessSlug: String
    ) {
        if (openingQuantity > 0 || minimumQuantity > 0) {
            val result = productsRepository.saveProductQuantity(
                productSlug = productSlug,
                warehouseSlug = warehouseSlug,
                openingQuantity = openingQuantity,
                minimumQuantity = minimumQuantity,
                businessSlug = businessSlug
            )
            result.fold(
                onSuccess = {
                    println("Product quantities saved successfully")
                },
                onFailure = { error ->
                    println("Failed to save product quantities: ${error.message}")
                }
            )
        }
    }
    
    fun saveProduct(
        productToEdit: com.hisaabi.hisaabi_kmp.products.domain.model.Product?,
        title: String,
        description: String?,
        productType: ProductType,
        retailPrice: Double = 0.0,
        wholesalePrice: Double = 0.0,
        purchasePrice: Double = 0.0,
        taxPercentage: Double = 0.0,
        discountPercentage: Double = 0.0,
        categorySlug: String? = null,
        manufacturer: String? = null,
        warehouseSlug: String? = null,
        openingQuantity: Double = 0.0,
        minimumQuantity: Double = 0.0
    ) {
        if (productToEdit != null) {
            // Update existing product
            updateProduct(
                productToEdit = productToEdit,
                title = title,
                description = description,
                productType = productType,
                retailPrice = retailPrice,
                wholesalePrice = wholesalePrice,
                purchasePrice = purchasePrice,
                taxPercentage = taxPercentage,
                discountPercentage = discountPercentage,
                manufacturer = manufacturer,
                warehouseSlug = warehouseSlug,
                openingQuantity = openingQuantity,
                minimumQuantity = minimumQuantity
            )
        } else {
            // Add new product
            addProduct(
                title = title,
                description = description,
                productType = productType,
                retailPrice = retailPrice,
                wholesalePrice = wholesalePrice,
                purchasePrice = purchasePrice,
                taxPercentage = taxPercentage,
                discountPercentage = discountPercentage,
                categorySlug = categorySlug,
                manufacturer = manufacturer
            )
        }
    }
    
    private fun updateProduct(
        productToEdit: com.hisaabi.hisaabi_kmp.products.domain.model.Product,
        title: String,
        description: String?,
        productType: ProductType,
        retailPrice: Double = 0.0,
        wholesalePrice: Double = 0.0,
        purchasePrice: Double = 0.0,
        taxPercentage: Double = 0.0,
        discountPercentage: Double = 0.0,
        manufacturer: String? = null,
        warehouseSlug: String? = null,
        openingQuantity: Double = 0.0,
        minimumQuantity: Double = 0.0
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val bSlug = businessSlug
            if (bSlug == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No business context available"
                )
                return@launch
            }
            
            // Create updated product with all fields from the original
            val updatedProduct = productToEdit.copy(
                title = title,
                description = description,
                retailPrice = retailPrice,
                wholesalePrice = wholesalePrice,
                purchasePrice = purchasePrice,
                taxPercentage = taxPercentage,
                discountPercentage = discountPercentage,
                manufacturer = manufacturer,
                updatedAt = getCurrentTimestamp()
            )
            
            val result = updateProductUseCase(updatedProduct)
            
            result.fold(
                onSuccess = { slug ->
                    println("Product updated successfully with slug: $slug")
                    // Save product quantities if warehouse is selected
                    if (warehouseSlug != null && bSlug != null) {
                        saveProductQuantities(
                            productSlug = slug,
                            warehouseSlug = warehouseSlug,
                            openingQuantity = openingQuantity,
                            minimumQuantity = minimumQuantity,
                            businessSlug = bSlug
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                },
                onFailure = { error ->
                    println("Failed to update product: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = error.message ?: "Failed to update product"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class AddProductUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val selectedWarehouse: Warehouse? = null,
    val openingQuantity: String = "",
    val minimumQuantity: String = "",
    val productToEdit: com.hisaabi.hisaabi_kmp.products.domain.model.Product? = null
)


