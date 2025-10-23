package com.hisaabi.hisaabi_kmp.products.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.products.data.repository.ProductsRepository
import com.hisaabi.hisaabi_kmp.products.domain.model.Product
import com.hisaabi.hisaabi_kmp.products.domain.model.ProductType
import com.hisaabi.hisaabi_kmp.products.domain.model.RecipeIngredient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ManageRecipeIngredientsViewModel(
    private val productsRepository: ProductsRepository,
    private val sessionManager: AppSessionManager
) : ViewModel() {
    
    private var businessSlug: String? = null
    private var userSlug: String? = null
    
    private val _uiState = MutableStateFlow(RecipeIngredientsUiState())
    val uiState: StateFlow<RecipeIngredientsUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            sessionManager.observeSessionContext().collect { context ->
                businessSlug = context.businessSlug
                userSlug = context.userSlug
            }
        }
    }
    
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
            
            val slug = businessSlug
            if (slug == null) {
                _uiState.value = _uiState.value.copy(
                    isLoadingProducts = false,
                    productsError = "No business selected"
                )
                return@launch
            }
            
            try {
                // Get only simple products (type 0) - exclude services and recipes
                val allProducts = productsRepository.getProducts(slug)
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
            
            val bSlug = businessSlug
            val uSlug = userSlug
            if (bSlug == null || uSlug == null) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveError = "No business or user context available"
                )
                return@launch
            }
            
            try {
                val ingredient = RecipeIngredient(
                    recipeSlug = recipeSlug,
                    ingredientSlug = ingredientProduct.slug,
                    ingredientTitle = ingredientProduct.title,
                    quantity = quantity,
                    quantityUnitSlug = quantityUnitSlug,
                    quantityUnitTitle = quantityUnitSlug, // TODO: Fetch actual unit title
                    slug = null,
                    businessSlug = bSlug,
                    createdBy = uSlug,
                    syncStatus = 0,
                    createdAt = null,
                    updatedAt = null
                )
                
                productsRepository.addRecipeIngredient(ingredient, bSlug, uSlug)
                
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

