package com.hisaabi.hisaabi_kmp.products.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.products.domain.model.Product
import com.hisaabi.hisaabi_kmp.products.domain.model.ProductType
import com.hisaabi.hisaabi_kmp.products.domain.usecase.GetProductsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductsViewModel(
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel() {
    
    // TODO: Get from session/business context
    private val businessSlug: String = "default_business"
    
    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()
    
    init {
        loadProducts()
    }
    
    fun onProductTypeChanged(productType: ProductType?) {
        if (_uiState.value.selectedProductType != productType) {
            _uiState.value = _uiState.value.copy(selectedProductType = productType)
            loadProducts()
        }
    }
    
    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        // TODO: Implement search filtering
    }
    
    fun refresh() {
        loadProducts()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            val result = getProductsUseCase(
                businessSlug = businessSlug,
                productType = _uiState.value.selectedProductType
            )
            
            result.fold(
                onSuccess = { products ->
                    _uiState.value = _uiState.value.copy(
                        products = products,
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
    val products: List<Product> = emptyList(),
    val selectedProductType: ProductType? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)


