package com.hisaabi.hisaabi_kmp.products.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.products.data.repository.ProductsRepository
import com.hisaabi.hisaabi_kmp.products.domain.model.Product
import com.hisaabi.hisaabi_kmp.products.domain.model.ProductType
import com.hisaabi.hisaabi_kmp.products.domain.model.RecipeIngredient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ManageRecipeIngredientsViewModel(
    private val productsRepository: ProductsRepository
) : ViewModel() {
    
    // TODO: Get from session/business context
    private val businessSlug: String = "default_business"
    private val userSlug: String = "default_user"
    
    private val _uiState = MutableStateFlow(RecipeIngredientsUiState())
    val uiState: StateFlow<RecipeIngredientsUiState> = _uiState.asStateFlow()
    
    fun loadIngredients(recipeSlug: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val ingredients = productsRepository.getRecipeIngredients(recipeSlug)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    ingredients = ingredients,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load ingredients"
                )
            }
        }
    }
    
    fun loadSimpleProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingProducts = true, productsError = null)
            
            try {
                // Get only simple products (type 0) - exclude services and recipes
                val allProducts = productsRepository.getProducts(businessSlug)
                val simpleProducts = allProducts.filter { 
                    it.typeId == ProductType.SIMPLE_PRODUCT.type 
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoadingProducts = false,
                    availableProducts = simpleProducts,
                    productsError = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingProducts = false,
                    productsError = e.message ?: "Failed to load products"
                )
            }
        }
    }
    
    fun addIngredient(
        recipeSlug: String,
        ingredientProduct: Product,
        quantity: Double,
        quantityUnitSlug: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveError = null)
            
            try {
                val ingredient = RecipeIngredient(
                    recipeSlug = recipeSlug,
                    ingredientSlug = ingredientProduct.slug,
                    ingredientTitle = ingredientProduct.title,
                    quantity = quantity,
                    quantityUnitSlug = quantityUnitSlug,
                    quantityUnitTitle = quantityUnitSlug, // TODO: Fetch actual unit title
                    slug = null,
                    businessSlug = businessSlug,
                    createdBy = userSlug,
                    syncStatus = 0,
                    createdAt = null,
                    updatedAt = null
                )
                
                productsRepository.addRecipeIngredient(ingredient, businessSlug, userSlug)
                
                // Reload ingredients
                loadIngredients(recipeSlug)
                
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveError = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveError = e.message ?: "Failed to add ingredient"
                )
            }
        }
    }
    
    fun deleteIngredient(ingredientSlug: String, recipeSlug: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                productsRepository.deleteRecipeIngredient(ingredientSlug)
                
                // Reload ingredients
                loadIngredients(recipeSlug)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to delete ingredient"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null, saveError = null, productsError = null)
    }
}

data class RecipeIngredientsUiState(
    val isLoading: Boolean = false,
    val isLoadingProducts: Boolean = false,
    val isSaving: Boolean = false,
    val ingredients: List<RecipeIngredient> = emptyList(),
    val availableProducts: List<Product> = emptyList(),
    val error: String? = null,
    val productsError: String? = null,
    val saveError: String? = null
)

