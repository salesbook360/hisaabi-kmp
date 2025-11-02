package com.hisaabi.hisaabi_kmp.transactions.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.products.domain.model.Product
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionDetail
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddManufactureViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.ManufactureState
import com.hisaabi.hisaabi_kmp.utils.format
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddManufactureScreen(
    viewModel: AddManufactureViewModel,
    onNavigateBack: () -> Unit,
    onSelectWarehouse: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    // Show error dialog if any
    state.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manufacture Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (!state.isSaving) {
                        IconButton(onClick = {
                            viewModel.saveManufactureTransaction(onSuccess = { _ ->
                                onSaveSuccess()
                                onNavigateBack()
                            })
                        }) {
                            Icon(Icons.Default.Save, "Save")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            AddManufactureContent(
                state = state,
                onRecipeSelected = { viewModel.selectRecipe(it) },
                onRecipeQuantityChanged = { viewModel.updateRecipeQuantity(it) },
                onIngredientQuantityChanged = { slug, qty ->
                    viewModel.updateIngredientQuantity(slug, qty)
                },
                onIngredientPriceChanged = { slug, price ->
                    viewModel.updateIngredientPrice(slug, price)
                },
                onAdditionalChargesChanged = { viewModel.updateAdditionalCharges(it) },
                onAdditionalChargesDescChanged = { viewModel.updateAdditionalChargesDescription(it) },
                onSelectWarehouse = onSelectWarehouse,
                onDateChanged = { viewModel.updateTransactionDate(it) },
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            )
        }
    }

    // Show saving progress
    if (state.isSaving) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun AddManufactureContent(
    state: ManufactureState,
    onRecipeSelected: (Product) -> Unit,
    onRecipeQuantityChanged: (Double) -> Unit,
    onIngredientQuantityChanged: (String, Double) -> Unit,
    onIngredientPriceChanged: (String, Double) -> Unit,
    onAdditionalChargesChanged: (Double) -> Unit,
    onAdditionalChargesDescChanged: (String) -> Unit,
    onSelectWarehouse: () -> Unit,
    onDateChanged: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRecipeSelector by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date and Warehouse Selection
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Date Selection (simplified - you might want to add a date picker)
                OutlinedButton(
                    onClick = { /* TODO: Add date picker */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        formatTimestamp(state.transactionTimestamp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Warehouse Selection
                OutlinedButton(
                    onClick = onSelectWarehouse,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Warehouse, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        state.selectedWarehouse?.title ?: "Select Warehouse",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Recipe Selection Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Recipe",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    if (state.selectedRecipe == null) {
                        OutlinedButton(
                            onClick = { showRecipeSelector = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Select Recipe")
                        }
                    } else {
                        RecipeCard(
                            recipe = state.selectedRecipe,
                            quantity = state.recipeQuantity,
                            unitTitle = state.recipeUnit?.title ?: "Unit",
                            unitPrice = state.recipeUnitPrice,
                            onQuantityChanged = onRecipeQuantityChanged,
                            onRemove = { showRecipeSelector = true }
                        )
                    }
                }
            }
        }

        // Ingredients Section
        if (state.ingredients.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Ingredients (${state.ingredients.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))

                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Item",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(0.4f)
                            )
                            Text(
                                "Qty",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(0.2f)
                            )
                            Text(
                                "Price",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(0.2f)
                            )
                            Text(
                                "Total",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(0.2f)
                            )
                        }

                        Spacer(Modifier.height(4.dp))
                    }
                }
            }

            items(state.ingredients) { ingredient ->
                IngredientRow(
                    ingredient = ingredient,
                    onQuantityChanged = { qty ->
                        onIngredientQuantityChanged(ingredient.productSlug ?: "", qty)
                    },
                    onPriceChanged = { price ->
                        onIngredientPriceChanged(ingredient.productSlug ?: "", price)
                    }
                )
            }
        }

        // Additional Charges Section
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Additional Charges",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    var chargesText by remember(state.additionalCharges) {
                        mutableStateOf(if (state.additionalCharges > 0) state.additionalCharges.toString() else "")
                    }

                    OutlinedTextField(
                        value = chargesText,
                        onValueChange = {
                            chargesText = it
                            it.toDoubleOrNull()?.let { value ->
                                onAdditionalChargesChanged(value)
                            } ?: onAdditionalChargesChanged(0.0)
                        },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Text("₨") }
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.additionalChargesDescription,
                        onValueChange = onAdditionalChargesDescChanged,
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }
        }

        // Total Cost Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Total Cost",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "₨ ${"%.2f".format(state.totalCost)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (state.recipeQuantity > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Unit Price: ₨ ${"%.2f".format(state.recipeUnitPrice)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Bottom padding
        item {
            Spacer(Modifier.height(16.dp))
        }
    }

    // Recipe Selector Dialog
    if (showRecipeSelector) {
        RecipeSelectorDialog(
            recipes = state.availableRecipes,
            onRecipeSelected = {
                onRecipeSelected(it)
                showRecipeSelector = false
            },
            onDismiss = { showRecipeSelector = false }
        )
    }
}

@Composable
private fun RecipeCard(
    recipe: Product,
    quantity: Double,
    unitTitle: String,
    unitPrice: Double,
    onQuantityChanged: (Double) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    recipe.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "₨ ${"%.2f".format(unitPrice)} per $unitTitle",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var quantityText by remember(quantity) {
                    mutableStateOf(quantity.toString())
                }

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = {
                        quantityText = it
                        it.toDoubleOrNull()?.let { value ->
                            if (value >= 0) onQuantityChanged(value)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.width(80.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    singleLine = true
                )

                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Close, "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun IngredientRow(
    ingredient: TransactionDetail,
    onQuantityChanged: (Double) -> Unit,
    onPriceChanged: (Double) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product name
            Column(modifier = Modifier.weight(0.4f)) {
                Text(
                    ingredient.product?.title ?: "Unknown",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
                ingredient.quantityUnit?.title?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Quantity
            Text(
                "%.2f".format(ingredient.quantity),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(0.2f)
            )

            // Price
            Text(
                "%.2f".format(ingredient.price),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(0.2f)
            )

            // Total
            Text(
                "%.2f".format(ingredient.calculateSubtotal()),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.2f)
            )
        }
    }
}

@Composable
private fun RecipeSelectorDialog(
    recipes: List<Product>,
    onRecipeSelected: (Product) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Recipe") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (recipes.isEmpty()) {
                    item {
                        Text(
                            "No recipes available. Please create a recipe first.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    items(recipes) { recipe ->
                        Card(
                            onClick = { onRecipeSelected(recipe) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        recipe.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    recipe.description?.let { desc ->
                                        Text(
                                            desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            maxLines = 2
                                        )
                                    }
                                }
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatTimestamp(timestamp: Long): String {
    val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(timestamp)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year}"
}

