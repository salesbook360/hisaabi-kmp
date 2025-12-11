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
import com.hisaabi.hisaabi_kmp.quantityunits.data.repository.QuantityUnitsRepository
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
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
    private val categoriesRepository: CategoriesRepository,
    private val quantityUnitsRepository: QuantityUnitsRepository
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
                // Load parent unit types when business context is available
                if (context.businessSlug != null) {
                    loadParentUnitTypes(context.businessSlug)
                }
            }
        }
    }
    
    private fun loadParentUnitTypes(businessSlug: String) {
        viewModelScope.launch {
            quantityUnitsRepository.getParentUnitTypes(businessSlug).collect { parentUnits ->
                _uiState.value = _uiState.value.copy(parentUnitTypes = parentUnits)
            }
        }
    }
    
    fun setSelectedBaseUnit(unit: QuantityUnit?) {
        val currentState = _uiState.value
        val previousBaseUnit = currentState.selectedBaseUnit
        
        // Check if the new unit belongs to a different parent
        val parentChanged = previousBaseUnit != null && unit != null && 
            previousBaseUnit.parentSlug != unit.parentSlug
        
        // Reset opening and minimum quantity units if parent changed
        val updatedOpeningUnit = if (parentChanged) unit else currentState.selectedOpeningQuantityUnit ?: unit
        val updatedMinimumUnit = if (parentChanged) unit else currentState.selectedMinimumQuantityUnit ?: unit
        
        _uiState.value = currentState.copy(
            selectedBaseUnit = unit,
            selectedOpeningQuantityUnit = updatedOpeningUnit,
            selectedMinimumQuantityUnit = updatedMinimumUnit
        )
        
        // Load child units for the selected base unit's parent
        unit?.parentSlug?.let { parentSlug ->
            loadChildUnitsForParent(parentSlug)
        }
    }
    
    /**
     * Load child units for a given parent unit type slug.
     * This is used when selecting a parent unit type in the bottom sheet
     * to show available child units without changing the selected base unit.
     */
    fun loadChildUnitsForParentType(parentTypeSlug: String) {
        viewModelScope.launch {
            val childUnits = quantityUnitsRepository.getUnitsByParentSuspend(parentTypeSlug)
            _uiState.value = _uiState.value.copy(childUnitsForBaseParent = childUnits)
        }
    }
    
    private fun loadChildUnitsForParent(parentSlug: String) {
        viewModelScope.launch {
            val childUnits = quantityUnitsRepository.getUnitsByParentSuspend(parentSlug)
            _uiState.value = _uiState.value.copy(childUnitsForBaseParent = childUnits)
        }
    }
    
    fun setSelectedOpeningQuantityUnit(unit: QuantityUnit?) {
        _uiState.value = _uiState.value.copy(selectedOpeningQuantityUnit = unit)
    }
    
    fun setSelectedMinimumQuantityUnit(unit: QuantityUnit?) {
        _uiState.value = _uiState.value.copy(selectedMinimumQuantityUnit = unit)
    }
    
    fun showBaseUnitSheet() {
        _uiState.value = _uiState.value.copy(showBaseUnitSheet = true)
    }
    
    fun hideBaseUnitSheet() {
        _uiState.value = _uiState.value.copy(showBaseUnitSheet = false)
    }
    
    fun showOpeningQuantityUnitSheet() {
        _uiState.value = _uiState.value.copy(showOpeningQuantityUnitSheet = true)
    }
    
    fun hideOpeningQuantityUnitSheet() {
        _uiState.value = _uiState.value.copy(showOpeningQuantityUnitSheet = false)
    }
    
    fun showMinimumQuantityUnitSheet() {
        _uiState.value = _uiState.value.copy(showMinimumQuantityUnitSheet = true)
    }
    
    fun hideMinimumQuantityUnitSheet() {
        _uiState.value = _uiState.value.copy(showMinimumQuantityUnitSheet = false)
    }
    
    fun resetState(sessionKey: Int = -1) {
        val parentUnitTypes = _uiState.value.parentUnitTypes
        _uiState.value = AddProductUiState(sessionKey = sessionKey, parentUnitTypes = parentUnitTypes)
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
        val productToEdit = _uiState.value.productToEdit
        if (warehouse != null && productToEdit != null) {
            loadProductQuantities(
                productSlug = productToEdit.slug,
                warehouseSlug = warehouse.slug ?: "",
                openingQuantityUnitSlug = productToEdit.openingQuantityUnitSlug,
                minimumQuantityUnitSlug = productToEdit.minimumQuantityUnitSlug
            )
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
        
        // Load unit information for edit mode (this also loads quantities with proper conversion)
        loadProductUnits(product)
    }
    
    private fun loadProductUnits(product: com.hisaabi.hisaabi_kmp.products.domain.model.Product) {
        viewModelScope.launch {
            // Load default unit (base unit) if set
            val defaultUnit = product.defaultUnitSlug?.let { 
                quantityUnitsRepository.getUnitBySlug(it) 
            }
            
            // Load opening quantity unit if set
            val openingUnit = product.openingQuantityUnitSlug?.let { 
                quantityUnitsRepository.getUnitBySlug(it) 
            }
            
            // Load minimum quantity unit if set
            val minimumUnit = product.minimumQuantityUnitSlug?.let { 
                quantityUnitsRepository.getUnitBySlug(it) 
            }
            
            _uiState.value = _uiState.value.copy(
                selectedBaseUnit = defaultUnit,
                selectedOpeningQuantityUnit = openingUnit ?: defaultUnit,
                selectedMinimumQuantityUnit = minimumUnit ?: defaultUnit
            )
            
            // Load child units for the base unit's parent
            defaultUnit?.parentSlug?.let { parentSlug ->
                loadChildUnitsForParent(parentSlug)
            }
            
            // Load quantities after units are loaded (with proper conversion factor division)
            val currentState = _uiState.value
            if (currentState.selectedWarehouse != null) {
                loadProductQuantities(
                    productSlug = product.slug,
                    warehouseSlug = currentState.selectedWarehouse!!.slug ?: "",
                    openingQuantityUnitSlug = product.openingQuantityUnitSlug,
                    minimumQuantityUnitSlug = product.minimumQuantityUnitSlug
                )
            }
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
    
    private fun loadProductQuantities(
        productSlug: String, 
        warehouseSlug: String,
        openingQuantityUnitSlug: String? = null,
        minimumQuantityUnitSlug: String? = null
    ) {
        viewModelScope.launch {
            try {
                val quantities = productsRepository.getProductQuantityForWarehouse(productSlug, warehouseSlug)
                if (quantities != null) {
                    var (opening, minimum) = quantities
                    
                    // Divide by conversion factor to show original values for editing
                    if (openingQuantityUnitSlug != null && opening > 0) {
                        val openingUnit = quantityUnitsRepository.getUnitBySlug(openingQuantityUnitSlug)
                        if (openingUnit != null && openingUnit.conversionFactor > 0) {
                            opening = opening / openingUnit.conversionFactor
                        }
                    }
                    
                    if (minimumQuantityUnitSlug != null && minimum > 0) {
                        val minimumUnit = quantityUnitsRepository.getUnitBySlug(minimumQuantityUnitSlug)
                        if (minimumUnit != null && minimumUnit.conversionFactor > 0) {
                            minimum = minimum / minimumUnit.conversionFactor
                        }
                    }
                    
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
        manufacturer: String? = null,
        defaultUnitSlug: String? = null,
        openingQuantityUnitSlug: String? = null,
        minimumQuantityUnitSlug: String? = null,
        openingQuantityConversionFactor: Double = 1.0,
        minimumQuantityConversionFactor: Double = 1.0
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
                defaultUnitSlug = defaultUnitSlug,
                openingQuantityUnitSlug = openingQuantityUnitSlug,
                minimumQuantityUnitSlug = minimumQuantityUnitSlug,
                businessSlug = bSlug,
                userSlug = uSlug
            )
            
            result.fold(
                onSuccess = { slug ->
                    println("Product added successfully with slug: $slug")
                    // Save product quantities if warehouse is selected
                    val state = _uiState.value
                    if (state.selectedWarehouse != null && bSlug != null) {
                        // Apply conversion factor to quantities
                        val openingQty = (state.openingQuantity.toDoubleOrNull() ?: 0.0) * openingQuantityConversionFactor
                        val minimumQty = (state.minimumQuantity.toDoubleOrNull() ?: 0.0) * minimumQuantityConversionFactor
                        
                        saveProductQuantities(
                            productSlug = slug,
                            warehouseSlug = state.selectedWarehouse!!.slug ?: "",
                            openingQuantity = openingQty,
                            minimumQuantity = minimumQty,
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
        minimumQuantity: Double = 0.0,
        defaultUnitSlug: String? = null,
        openingQuantityUnitSlug: String? = null,
        minimumQuantityUnitSlug: String? = null,
        openingQuantityConversionFactor: Double = 1.0,
        minimumQuantityConversionFactor: Double = 1.0
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
                minimumQuantity = minimumQuantity,
                defaultUnitSlug = defaultUnitSlug,
                openingQuantityUnitSlug = openingQuantityUnitSlug,
                minimumQuantityUnitSlug = minimumQuantityUnitSlug,
                openingQuantityConversionFactor = openingQuantityConversionFactor,
                minimumQuantityConversionFactor = minimumQuantityConversionFactor
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
                manufacturer = manufacturer,
                defaultUnitSlug = defaultUnitSlug,
                openingQuantityUnitSlug = openingQuantityUnitSlug,
                minimumQuantityUnitSlug = minimumQuantityUnitSlug,
                openingQuantityConversionFactor = openingQuantityConversionFactor,
                minimumQuantityConversionFactor = minimumQuantityConversionFactor
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
        minimumQuantity: Double = 0.0,
        defaultUnitSlug: String? = null,
        openingQuantityUnitSlug: String? = null,
        minimumQuantityUnitSlug: String? = null,
        openingQuantityConversionFactor: Double = 1.0,
        minimumQuantityConversionFactor: Double = 1.0
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
                baseUnitSlug = defaultUnitSlug,  // Set base unit slug same as default unit slug
                defaultUnitSlug = defaultUnitSlug,
                openingQuantityUnitSlug = openingQuantityUnitSlug,
                minimumQuantityUnitSlug = minimumQuantityUnitSlug,
                updatedAt = getCurrentTimestamp()
            )
            
            val result = updateProductUseCase(updatedProduct)
            
            result.fold(
                onSuccess = { slug ->
                    println("Product updated successfully with slug: $slug")
                    // Save product quantities if warehouse is selected
                    if (warehouseSlug != null && bSlug != null) {
                        // Apply conversion factor to quantities
                        val convertedOpeningQty = openingQuantity * openingQuantityConversionFactor
                        val convertedMinimumQty = minimumQuantity * minimumQuantityConversionFactor
                        
                        saveProductQuantities(
                            productSlug = slug,
                            warehouseSlug = warehouseSlug,
                            openingQuantity = convertedOpeningQty,
                            minimumQuantity = convertedMinimumQty,
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
    val manufacturer: String = "",
    // Unit-related fields
    val parentUnitTypes: List<QuantityUnit> = emptyList(),
    val childUnitsForBaseParent: List<QuantityUnit> = emptyList(),
    val selectedBaseUnit: QuantityUnit? = null,
    val selectedOpeningQuantityUnit: QuantityUnit? = null,
    val selectedMinimumQuantityUnit: QuantityUnit? = null,
    // Bottom sheet visibility states
    val showBaseUnitSheet: Boolean = false,
    val showOpeningQuantityUnitSheet: Boolean = false,
    val showMinimumQuantityUnitSheet: Boolean = false
)


