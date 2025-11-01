package com.hisaabi.hisaabi_kmp.products.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.products.domain.model.Product
import com.hisaabi.hisaabi_kmp.products.domain.model.ProductType
import com.hisaabi.hisaabi_kmp.products.domain.usecase.DeleteProductUseCase
import com.hisaabi.hisaabi_kmp.products.domain.usecase.GetProductsUseCase
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.settings.domain.model.TransactionSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductsViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val sessionManager: AppSessionManager,
    private val preferencesManager: PreferencesManager
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
        
        // Filter products based on search query
        val filteredProducts = if (query.isBlank()) {
            _uiState.value.allProducts
        } else {
            val searchQuery = query.lowercase().trim()
            _uiState.value.allProducts.filter { product ->
                product.title.lowercase().contains(searchQuery) ||
                product.description?.lowercase()?.contains(searchQuery) == true
            }
        }
        
        _uiState.value = _uiState.value.copy(products = filteredProducts)
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
    
    fun deleteProduct(productSlug: String) {
        viewModelScope.launch {
            val result = deleteProductUseCase(productSlug)
            result.fold(
                onSuccess = {
                    // Refresh the products list after successful deletion
                    loadProducts()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to delete product"
                    )
                }
            )
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
                    val filteredProducts = if (_uiState.value.searchQuery.isBlank()) {
                        products
                    } else {
                        products.filter { product ->
                            val query = _uiState.value.searchQuery.lowercase().trim()
                            product.title.lowercase().contains(query) ||
                            product.description?.lowercase()?.contains(query) == true
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        allProducts = products,
                        products = filteredProducts,
                        isLoading = false,
                        error = null
                    )
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

data class ProductsUiState(
    val allProducts: List<Product> = emptyList(),
    val products: List<Product> = emptyList(),
    val selectedProductType: ProductType? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
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


