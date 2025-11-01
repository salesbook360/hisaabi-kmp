package com.hisaabi.hisaabi_kmp.products.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.products.data.repository.ProductsRepository
import com.hisaabi.hisaabi_kmp.products.domain.model.Product
import com.hisaabi.hisaabi_kmp.products.domain.model.ProductType
import com.hisaabi.hisaabi_kmp.products.domain.usecase.DeleteProductUseCase
import com.hisaabi.hisaabi_kmp.products.domain.usecase.GetProductsUseCase
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.settings.domain.model.TransactionSettings
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse
import com.hisaabi.hisaabi_kmp.warehouses.domain.usecase.WarehouseUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductsViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val sessionManager: AppSessionManager,
    private val preferencesManager: PreferencesManager,
    private val warehouseUseCases: WarehouseUseCases,
    private val productsRepository: ProductsRepository
) : ViewModel() {
    
    private var businessSlug: String? = null
    
    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()
    
    val transactionSettings: StateFlow<TransactionSettings> = preferencesManager.transactionSettings.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = preferencesManager.getTransactionSettings()
    )
    
    init {
        viewModelScope.launch {
            sessionManager.observeBusinessSlug().collect { newBusinessSlug ->
                businessSlug = newBusinessSlug
                if (newBusinessSlug != null) {
                    loadWarehouses()
                    loadProducts()
                }
            }
        }
    }
    
    fun onProductTypeChanged(productType: ProductType?) {
        if (_uiState.value.selectedProductType != productType) {
            _uiState.value = _uiState.value.copy(selectedProductType = productType)
            loadProducts()
        }
    }
    
    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }
    
    fun refresh() {
        loadProducts()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            products = _uiState.value.allProducts
        )
    }
    
    fun selectWarehouse(warehouse: Warehouse?) {
        _uiState.value = _uiState.value.copy(selectedWarehouse = warehouse)
        if (warehouse != null) {
            loadQuantities(warehouse.slug ?: "")
        } else {
            _uiState.value = _uiState.value.copy(
                productQuantities = emptyMap(),
                productQuantitiesWithMinimum = emptyMap()
            )
        }
        applyFilters()
    }
    
    fun setQuantityFilter(filter: QuantityFilter) {
        _uiState.value = _uiState.value.copy(quantityFilter = filter)
        applyFilters()
    }
    
    private fun applyFilters() {
        val state = _uiState.value
        var filteredProducts = state.allProducts
        
        // Apply search filter first
        if (state.searchQuery.isNotBlank()) {
            val searchQuery = state.searchQuery.lowercase().trim()
            filteredProducts = filteredProducts.filter { product ->
                product.title.lowercase().contains(searchQuery) ||
                product.description?.lowercase()?.contains(searchQuery) == true
            }
        }
        
        // Apply quantity filter
        if (state.quantityFilter != QuantityFilter.ALL && state.selectedWarehouse != null) {
            filteredProducts = filteredProducts.filter { product ->
                val quantityData = state.productQuantitiesWithMinimum[product.slug]
                if (quantityData == null) {
                    // If no quantity data, show for ALL filter only
                    state.quantityFilter == QuantityFilter.ALL
                } else {
                    val (currentQuantity, minimumQuantity) = quantityData
                    when (state.quantityFilter) {
                        QuantityFilter.ALL -> true
                        QuantityFilter.GREATER_THAN_MINIMUM -> currentQuantity > minimumQuantity
                        QuantityFilter.GREATER_THAN_ZERO -> currentQuantity > 0
                        QuantityFilter.LESS_THAN_MINIMUM -> currentQuantity < minimumQuantity && minimumQuantity > 0
                        QuantityFilter.ZERO_QUANTITY -> currentQuantity == 0.0
                    }
                }
            }
        }
        
        _uiState.value = state.copy(products = filteredProducts)
    }
    
    fun deleteProduct(productSlug: String) {
        viewModelScope.launch {
            val result = deleteProductUseCase(productSlug)
            result.fold(
                onSuccess = {
                    // Refresh the products list after successful deletion
                    loadProducts()
                    // Reload quantities if warehouse is selected
                    _uiState.value.selectedWarehouse?.slug?.let { warehouseSlug ->
                        loadQuantities(warehouseSlug)
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to delete product"
                    )
                }
            )
        }
    }
    
    private fun loadWarehouses() {
        viewModelScope.launch {
            val slug = businessSlug
            if (slug == null) return@launch
            
            warehouseUseCases.getWarehouses(slug).collect { warehouses ->
                _uiState.value = _uiState.value.copy(availableWarehouses = warehouses)
                
                // Auto-select first warehouse if none selected and warehouses are available
                if (_uiState.value.selectedWarehouse == null && warehouses.isNotEmpty()) {
                    selectWarehouse(warehouses.first())
                }
            }
        }
    }
    
    private fun loadQuantities(warehouseSlug: String) {
        viewModelScope.launch {
            try {
                val quantities = productsRepository.getQuantitiesByWarehouse(warehouseSlug)
                val quantitiesWithMinimum = productsRepository.getQuantitiesWithMinimumByWarehouse(warehouseSlug)
                _uiState.value = _uiState.value.copy(
                    productQuantities = quantities,
                    productQuantitiesWithMinimum = quantitiesWithMinimum
                )
                applyFilters()
            } catch (e: Exception) {
                // Silently handle errors - quantities might not exist for all products
                _uiState.value = _uiState.value.copy(
                    productQuantities = emptyMap(),
                    productQuantitiesWithMinimum = emptyMap()
                )
                applyFilters()
            }
        }
    }
    
    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            val slug = businessSlug
            if (slug == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No business selected"
                )
                return@launch
            }
            
            val result = getProductsUseCase(
                businessSlug = slug,
                productType = _uiState.value.selectedProductType
            )
            
            result.fold(
                onSuccess = { products ->
                    _uiState.value = _uiState.value.copy(
                        allProducts = products,
                        isLoading = false,
                        error = null
                    )
                    applyFilters()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load products"
                    )
                }
            )
        }
    }
}

enum class QuantityFilter {
    ALL,
    GREATER_THAN_MINIMUM,
    GREATER_THAN_ZERO,
    LESS_THAN_MINIMUM,
    ZERO_QUANTITY
}

data class ProductsUiState(
    val allProducts: List<Product> = emptyList(),
    val products: List<Product> = emptyList(),
    val selectedProductType: ProductType? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val availableWarehouses: List<Warehouse> = emptyList(),
    val selectedWarehouse: Warehouse? = null,
    val productQuantities: Map<String, Double> = emptyMap(), // productSlug -> currentQuantity
    val productQuantitiesWithMinimum: Map<String, Pair<Double, Double>> = emptyMap(), // productSlug -> (currentQuantity, minimumQuantity)
    val quantityFilter: QuantityFilter = QuantityFilter.ALL
) {
    fun filterProducts(): List<Product> {
        if (searchQuery.isBlank()) {
            return allProducts
        }
        val query = searchQuery.lowercase().trim()
        return allProducts.filter { product ->
            product.title.lowercase().contains(query) ||
            product.description?.lowercase()?.contains(query) == true
        }
    }
}


