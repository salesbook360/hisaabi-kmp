package com.hisaabi.hisaabi_kmp.products.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.categories.data.repository.CategoriesRepository
import com.hisaabi.hisaabi_kmp.categories.domain.model.Category
import com.hisaabi.hisaabi_kmp.categories.domain.model.CategoryType
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
    private val productsRepository: ProductsRepository,
    private val categoriesRepository: CategoriesRepository
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
    
    fun resetState(sessionKey: Int = -1) {
        _uiState.value = AddProductUiState(sessionKey = sessionKey)
    }

    fun initializeForm(sessionKey: Int, productToEdit: com.hisaabi.hisaabi_kmp.products.domain.model.Product?) {
        val currentState = _uiState.value
        if (currentState.sessionKey == sessionKey) {
            if (productToEdit != null && currentState.productToEdit?.slug != productToEdit.slug) {
                applyProductToState(productToEdit)
            } else if (productToEdit == null && currentState.productToEdit != null) {
                _uiState.value = currentState.copy(productToEdit = null)
            }
            return
        }

        resetState(sessionKey)
        productToEdit?.let { applyProductToState(it) }
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
    
    fun setSelectedCategory(category: Category?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }
    
    fun setProductToEdit(product: com.hisaabi.hisaabi_kmp.products.domain.model.Product?) {
        if (product == null) {
            _uiState.value = _uiState.value.copy(productToEdit = null)
            return
        }
        applyProductToState(product)
    }

    private fun applyProductToState(product: com.hisaabi.hisaabi_kmp.products.domain.model.Product) {
        _uiState.value = _uiState.value.copy(
            productToEdit = product,
            title = product.title,
            description = product.description ?: "",
            retailPrice = "%.2f".format(product.retailPrice),
            wholesalePrice = "%.2f".format(product.wholesalePrice),
            purchasePrice = "%.2f".format(product.purchasePrice),
            taxPercentage = "%.2f".format(product.taxPercentage),
            discountPercentage = "%.2f".format(product.discountPercentage),
            manufacturer = product.manufacturer ?: ""
        )
        // Load category if product has one
        if (product.categorySlug != null) {
            loadCategory(product.categorySlug)
        } else {
            _uiState.value = _uiState.value.copy(selectedCategory = null)
        }
        if (_uiState.value.selectedWarehouse != null) {
            loadProductQuantities(product.slug, _uiState.value.selectedWarehouse!!.slug ?: "")
        }
    }
    
    private fun loadCategory(categorySlug: String) {
        viewModelScope.launch {
            try {
                val category = categoriesRepository.getCategoryBySlug(categorySlug)
                _uiState.value = _uiState.value.copy(selectedCategory = category)
            } catch (e: Exception) {
                // Silently handle errors - category might not exist
                _uiState.value = _uiState.value.copy(selectedCategory = null)
            }
        }
    }

    fun updateTitle(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }

    fun updateDescription(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun updateRetailPrice(value: String) {
        _uiState.value = _uiState.value.copy(retailPrice = value)
    }

    fun updateWholesalePrice(value: String) {
        _uiState.value = _uiState.value.copy(wholesalePrice = value)
    }

    fun updatePurchasePrice(value: String) {
        _uiState.value = _uiState.value.copy(purchasePrice = value)
    }

    fun updateTaxPercentage(value: String) {
        _uiState.value = _uiState.value.copy(taxPercentage = value)
    }

    fun updateDiscountPercentage(value: String) {
        _uiState.value = _uiState.value.copy(discountPercentage = value)
    }

    fun updateManufacturer(value: String) {
        _uiState.value = _uiState.value.copy(manufacturer = value)
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
                categorySlug = categorySlug,
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
        categorySlug: String? = null,
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
                categorySlug = categorySlug,
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
    val selectedCategory: Category? = null,
    val openingQuantity: String = "",
    val minimumQuantity: String = "",
    val productToEdit: com.hisaabi.hisaabi_kmp.products.domain.model.Product? = null,
    val sessionKey: Int = -1,
    val title: String = "",
    val description: String = "",
    val retailPrice: String = "",
    val wholesalePrice: String = "",
    val purchasePrice: String = "",
    val taxPercentage: String = "",
    val discountPercentage: String = "",
    val manufacturer: String = ""
)


