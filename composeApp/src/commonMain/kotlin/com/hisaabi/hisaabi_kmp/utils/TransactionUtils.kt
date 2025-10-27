package com.hisaabi.hisaabi_kmp.utils

import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction

/**
 * Calculates the total manufacturing cost for a manufacture transaction.
 * Formula: (ingredient purchase price * ingredient quantity) * recipe quantity
 * 
 * @param transaction The transaction to calculate manufacturing cost for
 * @return The total manufacturing cost
 */
fun calculateManufacturingCost(transaction: Transaction): Double {
    // Get the manufactured product (first detail is the output product)
    val manufacturedProduct = transaction.transactionDetails.firstOrNull() ?: return 0.0
    val recipeQuantity = manufacturedProduct.quantity
    
    // Calculate total cost of all ingredient details (excluding the first one which is the output)
    val ingredientDetails = transaction.transactionDetails.drop(1)
    val totalIngredientCost = ingredientDetails.sumOf { ingredient ->
        val purchasePrice = ingredient.product?.purchasePrice ?: 0.0
        purchasePrice * ingredient.quantity
    }
    
    // Total cost = ingredient cost * recipe quantity
    return totalIngredientCost * recipeQuantity
}
