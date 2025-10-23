package com.hisaabi.hisaabi_kmp.home.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaabi.hisaabi_kmp.utils.format

/**
 * Dashboard card wrapper component
 */
@Composable
fun DashboardCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        content()
    }
}

/**
 * Section header with optional dropdown filter
 */
@Composable
fun SectionHeader(
    modifier: Modifier = Modifier,
    headingText: String,
    popupOptions: List<IntervalEnum>?,
    selectedOption: IntervalEnum?,
    onMenuItemSelected: (IntervalEnum) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = headingText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        if (popupOptions != null && selectedOption != null) {
            Box {
                TextButton(
                    onClick = { expanded = true }
                ) {
                    Text(
                        text = selectedOption.title,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    popupOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.title,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp
                                )
                            },
                            onClick = {
                                expanded = false
                                onMenuItemSelected(option)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Overview section with grid of items (2 columns)
 */
@Composable
fun OverviewSection(
    modifier: Modifier = Modifier,
    dataState: DashboardDataState<DashboardSectionDataModel>,
    currencySymbol: String = "₹"
) {
    when (dataState) {
        is DashboardDataState.Success -> {
            val data = dataState.data
            Column(modifier) {
                SectionHeader(
                    headingText = data.title,
                    popupOptions = data.options,
                    selectedOption = data.selectedOption,
                    onMenuItemSelected = data.onOptionSelected
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(calculateGridHeight(data.sectionItems.size, 2)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(data.sectionItems) { item ->
                        OverviewItem(
                            item = item,
                            currencySymbol = currencySymbol
                        )
                    }
                }
            }
        }
        is DashboardDataState.Loading -> {
            DashboardSectionLoader()
        }
        is DashboardDataState.Error -> {
            DashboardSectionError(message = dataState.message)
        }
        is DashboardDataState.NoData -> {
            // Don't show anything
        }
    }
}

/**
 * Single overview item with icon and values
 */
@Composable
fun OverviewItem(
    item: DashboardSectionDataModel.SectionItem,
    currencySymbol: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            Text(
                text = formatCurrency(item.value, currencySymbol),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Summary section with centered items (3 columns)
 */
@Composable
fun SummarySection(
    modifier: Modifier = Modifier,
    dataState: DashboardDataState<DashboardSectionDataModel>
) {
    when (dataState) {
        is DashboardDataState.Success -> {
            val data = dataState.data
            Column(modifier) {
                SectionHeader(
                    headingText = data.title,
                    popupOptions = data.options,
                    selectedOption = data.selectedOption,
                    onMenuItemSelected = data.onOptionSelected
                )
                
                val columns = minOf(3, data.sectionItems.size)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(calculateGridHeight(data.sectionItems.size, columns)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(data.sectionItems) { item ->
                        SummaryItem(item = item)
                    }
                }
            }
        }
        is DashboardDataState.Loading -> {
            DashboardSectionLoader()
        }
        is DashboardDataState.Error -> {
            DashboardSectionError(message = dataState.message)
        }
        is DashboardDataState.NoData -> {
            // Don't show anything
        }
    }
}

/**
 * Single summary item with centered icon and text
 */
@Composable
fun SummaryItem(item: DashboardSectionDataModel.SectionItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
        Text(
            text = item.value.toInt().toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Loading state for dashboard sections
 */
@Composable
fun DashboardSectionLoader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Error state for dashboard sections
 */
@Composable
fun DashboardSectionError(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Helper function to calculate grid height
 */
private fun calculateGridHeight(itemCount: Int, columns: Int): androidx.compose.ui.unit.Dp {
    val rows = (itemCount + columns - 1) / columns
    return (rows * 100).dp
}

/**
 * Helper function to format currency
 */
private fun formatCurrency(value: Double, symbol: String): String {
    return when {
        value >= 10000000 -> String.format("%s%.2fCr", symbol, value / 10000000)
        value >= 100000 -> String.format("%s%.2fL", symbol, value / 100000)
        value >= 1000 -> String.format("%s%.2fK", symbol, value / 1000)
        else -> String.format("%s%.2f", symbol, value)
    }
}

