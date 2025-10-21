package com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.paymentmethods.data.repository.PaymentMethodsRepository
import com.hisaabi.hisaabi_kmp.products.data.repository.ProductsRepository
import com.hisaabi.hisaabi_kmp.products.domain.model.Product
import com.hisaabi.hisaabi_kmp.products.domain.model.ProductType
import com.hisaabi.hisaabi_kmp.products.domain.model.RecipeIngredient
import com.hisaabi.hisaabi_kmp.quantityunits.data.repository.QuantityUnitsRepository
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
import com.hisaabi.hisaabi_kmp.transactions.data.repository.TransactionsRepository
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionDetail
import com.hisaabi.hisaabi_kmp.warehouses.data.repository.WarehousesRepository
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddManufactureViewModel(
    private val transactionsRepository: TransactionsRepository,
    private val productsRepository: ProductsRepository,
    private val quantityUnitsRepository: QuantityUnitsRepository,
    private val paymentMethodsRepository: PaymentMethodsRepository,
    private val warehousesRepository: WarehousesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ManufactureState())
    val state: StateFlow<ManufactureState> = _state.asStateFlow()

    private val originalQuantityMap = mutableMapOf<String, Double>()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                // Load all recipes
                val recipes = productsRepository.getProducts(
                    businessSlug = "BUS_1", // TODO: Get from user session
                    productType = ProductType.RECIPE
                )
                _state.value = _state.value.copy(
                    availableRecipes = recipes,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Failed to load recipes",
                    isLoading = false
                )
            }
        }
    }

    fun selectRecipe(recipe: Product) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                // Load recipe ingredients
                val ingredients = productsRepository.getRecipeIngredients(recipe.slug)

                // Store original quantities
                originalQuantityMap.clear()
                ingredients.forEach {
                    originalQuantityMap[it.ingredientSlug] = it.quantity
                }

                // Convert to TransactionDetail
                val ingredientDetails = ingredients.map { ingredient ->
                    val product = productsRepository.getProductBySlug(ingredient.ingredientSlug)
                    val unit = ingredient.quantityUnitSlug?.let {
                        quantityUnitsRepository.getUnitBySlug(it)
                    }

                    TransactionDetail(
                        id = 0,
                        transactionSlug = null,
                        productSlug = ingredient.ingredientSlug,
                        product = product,
                        quantity = ingredient.quantity,
                        price = product?.purchasePrice ?: 0.0,
                        quantityUnitSlug = ingredient.quantityUnitSlug,
                        quantityUnit = unit,
                        slug = null,
                        syncStatus = 0,
                        createdAt = null,
                        updatedAt = null
                    )
                }

                // Get default unit for recipe
                val recipeUnit = recipe.defaultUnitSlug?.let {
                    quantityUnitsRepository.getUnitBySlug(it)
                }

                _state.value = _state.value.copy(
                    selectedRecipe = recipe,
                    recipeQuantity = 1.0,
                    recipeUnit = recipeUnit,
                    ingredients = ingredientDetails,
                    isLoading = false
                )

                // Calculate total cost
                calculateTotalCost()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Failed to load recipe ingredients",
                    isLoading = false
                )
            }
        }
    }

    fun updateRecipeQuantity(quantity: Double) {
        _state.value = _state.value.copy(recipeQuantity = quantity)

        // Update ingredient quantities proportionally
        val updatedIngredients = _state.value.ingredients.map { ingredient ->
            val originalQuantity = originalQuantityMap[ingredient.productSlug] ?: 1.0
            val newQuantity = originalQuantity * quantity
            ingredient.copy(
                quantity = newQuantity
            )
        }

        _state.value = _state.value.copy(ingredients = updatedIngredients)
        calculateTotalCost()
    }

    fun updateIngredientQuantity(ingredientSlug: String, quantity: Double) {
        val updatedIngredients = _state.value.ingredients.map { ingredient ->
            if (ingredient.productSlug == ingredientSlug) {
                ingredient.copy(quantity = quantity)
            } else {
                ingredient
            }
        }
        _state.value = _state.value.copy(ingredients = updatedIngredients)
        calculateTotalCost()
    }

    fun updateIngredientPrice(ingredientSlug: String, price: Double) {
        val updatedIngredients = _state.value.ingredients.map { ingredient ->
            if (ingredient.productSlug == ingredientSlug) {
                ingredient.copy(price = price)
            } else {
                ingredient
            }
        }
        _state.value = _state.value.copy(ingredients = updatedIngredients)
        calculateTotalCost()
    }

    fun updateAdditionalCharges(charges: Double) {
        _state.value = _state.value.copy(additionalCharges = charges)
        calculateTotalCost()
    }

    fun updateAdditionalChargesDescription(description: String) {
        _state.value = _state.value.copy(additionalChargesDescription = description)
    }

    fun selectWarehouse(warehouse: Warehouse) {
        _state.value = _state.value.copy(selectedWarehouse = warehouse)
    }

    fun updateTransactionDate(timestamp: Long) {
        _state.value = _state.value.copy(transactionTimestamp = timestamp)
    }

    private fun calculateTotalCost() {
        val ingredientsCost = _state.value.ingredients.sumOf { it.calculateSubtotal() }
        val totalCost = ingredientsCost + _state.value.additionalCharges

        // Calculate unit price for the recipe
        val recipeUnitPrice = if (_state.value.recipeQuantity > 0) {
            totalCost / _state.value.recipeQuantity
        } else {
            0.0
        }

        _state.value = _state.value.copy(
            totalCost = totalCost,
            recipeUnitPrice = recipeUnitPrice
        )
    }

    fun saveManufactureTransaction(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isSaving = true, error = null)

                val selectedRecipe = _state.value.selectedRecipe
                    ?: throw IllegalStateException("No recipe selected")

                val selectedWarehouse = _state.value.selectedWarehouse
                    ?: throw IllegalStateException("No warehouse selected")

                if (_state.value.recipeQuantity <= 0) {
                    throw IllegalStateException("Recipe quantity must be greater than 0")
                }

                // Get default payment method
                val paymentMethod = paymentMethodsRepository.getActivePaymentMethods()
                    .first()
                    .firstOrNull()

                // Create the recipe transaction detail
                val recipeDetail = TransactionDetail(
                    id = 0,
                    transactionSlug = null,
                    productSlug = selectedRecipe.slug,
                    product = selectedRecipe,
                    quantity = _state.value.recipeQuantity,
                    price = _state.value.recipeUnitPrice,
                    quantityUnitSlug = selectedRecipe.defaultUnitSlug,
                    quantityUnit = _state.value.recipeUnit,
                    slug = null,
                    syncStatus = 0,
                    createdAt = null,
                    updatedAt = null
                )

                // Save manufacture transaction
                transactionsRepository.saveManufactureTransaction(
                    recipeDetail = recipeDetail,
                    ingredients = _state.value.ingredients,
                    additionalCharges = _state.value.additionalCharges,
                    additionalChargesDescription = _state.value.additionalChargesDescription,
                    warehouseSlug = selectedWarehouse.slug.orEmpty(),
                    paymentMethodSlug = paymentMethod?.slug,
                    timestamp = _state.value.transactionTimestamp,
                    businessSlug = "BUS_1", // TODO: Get from user session
                    userSlug = "USER_1" // TODO: Get from user session
                )

                _state.value = _state.value.copy(isSaving = false)
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to save transaction"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun resetState() {
        _state.value = ManufactureState()
        originalQuantityMap.clear()
        loadInitialData()
    }
}

data class ManufactureState(
    val availableRecipes: List<Product> = emptyList(),
    val selectedRecipe: Product? = null,
    val recipeQuantity: Double = 1.0,
    val recipeUnit: QuantityUnit? = null,
    val recipeUnitPrice: Double = 0.0,
    val ingredients: List<TransactionDetail> = emptyList(),
    val additionalCharges: Double = 0.0,
    val additionalChargesDescription: String = "",
    val selectedWarehouse: Warehouse? = null,
    val transactionTimestamp: Long = System.currentTimeMillis(),
    val totalCost: Double = 0.0,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)

