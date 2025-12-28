package com.hisaabi.hisaabi_kmp.transactions.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.products.domain.model.Product
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionDetail
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddManufactureViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.ManufactureState
import com.hisaabi.hisaabi_kmp.utils.format
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.utils.formatDateTime
import com.hisaabi.hisaabi_kmp.utils.SimpleDateTimePickerDialog
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddManufactureScreen(
    viewModel: AddManufactureViewModel,
    onNavigateBack: () -> Unit,
    onSelectWarehouse: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    
    // Currency
    val preferencesManager: PreferencesManager = koinInject()
    val selectedCurrency by preferencesManager.selectedCurrency.collectAsState(null)
    val currencySymbol = selectedCurrency?.symbol ?: ""

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!state.isSaving) {
                ExtendedFloatingActionButton(
                    onClick = {
                        viewModel.saveManufactureTransaction(onSuccess = { _ ->
                            onSaveSuccess()
                            onNavigateBack()
                        })
                    },
                    icon = { Icon(Icons.Default.Save, "Save") },
                    text = { Text("Save Transaction") }
                )
            }
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
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                currencySymbol
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
    modifier: Modifier = Modifier,
    currencySymbol:String
) {
    var showRecipeSelector by remember { mutableStateOf(false) }
    var showDateTimePicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date Selection
        item {
            DateTimeField(
                label = "Transaction Date & Time",
                timestamp = state.transactionTimestamp,
                onDateTimeClick = { showDateTimePicker = true }
            )
        }

        // Warehouse Selection Card
        item {
            WarehouseSelectionCard(
                selectedWarehouse = state.selectedWarehouse,
                onSelectWarehouse = onSelectWarehouse,
                isMandatory = false
            )
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
                            currencySymbol = currencySymbol,
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
                    
                    val focusManager = LocalFocusManager.current

                    OutlinedTextField(
                        value = chargesText,
                        onValueChange = {
                            chargesText = it
                            it.toDoubleOrNull()?.let { value ->
                                onAdditionalChargesChanged(value)
                            } ?: onAdditionalChargesChanged(0.0)
                        },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Text(currencySymbol) },
                        singleLine = true
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
                            "$currencySymbol ${"%.2f".format(state.totalCost)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (state.recipeQuantity > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Unit Price: $currencySymbol ${"%.2f".format(state.recipeUnitPrice)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Bottom padding for FAB
        item {
            Spacer(Modifier.height(80.dp))
        }
    }

    // Date Time Picker Dialog
    if (showDateTimePicker) {
        SimpleDateTimePickerDialog(
            initialTimestamp = state.transactionTimestamp,
            onConfirm = { timestamp ->
                onDateChanged(timestamp)
                showDateTimePicker = false
            },
            onDismiss = { showDateTimePicker = false }
        )
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
    currencySymbol: String,
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
                    "$currencySymbol ${"%.2f".format(unitPrice)} per $unitTitle",
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

@Composable
private fun DateTimeField(
    label: String,
    timestamp: Long,
    onDateTimeClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDateTimeClick)
    ) {
        OutlinedTextField(
            value = formatDateTime(timestamp),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.CalendarToday, "Date") },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun WarehouseSelectionCard(
    selectedWarehouse: Warehouse?,
    onSelectWarehouse: () -> Unit,
    isMandatory: Boolean = false
) {
    val isNotSelected = selectedWarehouse == null
    val showError = isMandatory && isNotSelected
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelectWarehouse),
        colors = CardDefaults.cardColors(
            containerColor = if (showError)
                MaterialTheme.colorScheme.errorContainer
            else if (selectedWarehouse != null)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warehouse,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (showError)
                    MaterialTheme.colorScheme.onErrorContainer
                else if (selectedWarehouse != null)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Warehouse",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (showError)
                            MaterialTheme.colorScheme.onErrorContainer
                        else if (selectedWarehouse != null)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isMandatory) {
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "*",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Text(
                    selectedWarehouse?.title ?: "Select Warehouse",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (showError)
                        MaterialTheme.colorScheme.onErrorContainer
                    else if (selectedWarehouse != null)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (showError) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Required before adding products",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (showError)
                    MaterialTheme.colorScheme.onErrorContainer
                else if (selectedWarehouse != null)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

