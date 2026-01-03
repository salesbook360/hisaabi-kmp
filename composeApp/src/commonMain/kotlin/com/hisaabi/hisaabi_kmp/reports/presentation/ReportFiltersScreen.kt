package com.hisaabi.hisaabi_kmp.reports.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaabi.hisaabi_kmp.core.ui.FilterChipWithColors
import com.hisaabi.hisaabi_kmp.core.ui.LocalWindowSizeClass
import com.hisaabi.hisaabi_kmp.core.ui.WindowWidthSizeClass
import com.hisaabi.hisaabi_kmp.core.ui.DateRangePickerDialog
import com.hisaabi.hisaabi_kmp.core.ui.DateRangeField
import androidx.compose.ui.Alignment
import com.hisaabi.hisaabi_kmp.reports.domain.model.*
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType
import com.hisaabi.hisaabi_kmp.products.domain.model.Product
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFiltersScreen(
    reportType: ReportType,
    filters: ReportFilters = ReportFilters(reportType = reportType),
    selectedEntity: Any? = null, // Can be Party, Product, or Warehouse
    onBackClick: () -> Unit = {},
    onFiltersChanged: (ReportFilters) -> Unit = {},
    onGenerateReport: (ReportFilters) -> Unit = {}
) {
    // Get report types (Overall, Daily, Weekly, etc.) using the factory
    val reportTypes = ReportFiltersFactory.getReportTypes(reportType)
    
    // Initialize filters with default OVERALL report type if available and not already set
    var currentFilters by remember(reportType, filters) {
        val defaultAdditionalFilter = if (reportTypes.isNotEmpty() && filters.additionalFilter == null) {
            // Set OVERALL as default if it's available in report types
            reportTypes.firstOrNull { it == ReportAdditionalFilter.OVERALL } ?: reportTypes.firstOrNull()
        } else {
            filters.additionalFilter
        }
        mutableStateOf(
            if (defaultAdditionalFilter != null && filters.additionalFilter == null) {
                filters.copy(additionalFilter = defaultAdditionalFilter)
            } else {
                filters
            }
        )
    }
    
    // Get date filters based on report and selected report type - updates when report type changes
    val dateFilters = remember(reportType, currentFilters.additionalFilter) {
        ReportFiltersFactory.getDateFilters(reportType, currentFilters.additionalFilter)
    }
    
    // Get sort options based on report and selected report type - updates when report type changes
    val sortOptions = remember(reportType, currentFilters.additionalFilter) {
        ReportFiltersFactory.getSortOptions(reportType, currentFilters.additionalFilter)
    }
    
    // Get group by options based on report and selected report type - updates when report type changes
    val groupByOptions = remember(reportType, currentFilters.additionalFilter) {
        ReportFiltersFactory.getGroupByOptions(reportType, currentFilters.additionalFilter)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(reportType.title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 8.dp
            ) {
                val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                Button(
                    onClick = { onGenerateReport(currentFilters) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp + bottomInset),
                    enabled = currentFilters.isValid()
                ) {
                    Text("Generate Report", fontSize = 16.sp)
                }
            }
        }
    ) { paddingValues ->
        val windowSizeClass = LocalWindowSizeClass.current
        val isDesktop = windowSizeClass.widthSizeClass == WindowWidthSizeClass.EXPANDED
        val isMedium = windowSizeClass.widthSizeClass == WindowWidthSizeClass.MEDIUM
        val maxContentWidth = if (isDesktop) 800.dp else Dp.Unspecified
        val horizontalPadding = if (isDesktop) 24.dp else 16.dp
        
        // Adaptive columns for filter chips: 3 on mobile, 4 on tablet, 5 on desktop
        val filterColumns = when {
            isDesktop -> 5
            isMedium -> 4
            else -> 3
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .then(if (isDesktop) Modifier.widthIn(max = maxContentWidth) else Modifier.fillMaxWidth())
                    .padding(horizontal = horizontalPadding, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Selected Entity Section - only show if entity is relevant to current report type
            val shouldShowEntity = selectedEntity != null && filters.requiresEntitySelection()
            if (shouldShowEntity) {
                item {
                    SelectedEntityCard(
                        entity = selectedEntity,
                        reportType = reportType,
                        isDesktop = isDesktop
                    )
                }
            }
            
            // Additional Filters Section (Report Types: Overall, Daily, Weekly, etc.)
            if (reportTypes.isNotEmpty()) {
                item {
                    FilterSection(
                        title = "Report Type",
                        items = reportTypes,
                        selectedItem = currentFilters.additionalFilter,
                        onItemSelected = { filter ->
                            currentFilters = currentFilters.copy(additionalFilter = filter)
                            // Reset date filter and sort options when report type changes
                            // as they may have different options now
                            val newDateFilters = ReportFiltersFactory.getDateFilters(reportType, filter)
                            val newSortOptions = ReportFiltersFactory.getSortOptions(reportType, filter)
                            
                            // Reset to first available option if current selection is not available
                            val updatedDateFilter = if (newDateFilters.isNotEmpty() && 
                                currentFilters.dateFilter !in newDateFilters) {
                                newDateFilters.first()
                            } else {
                                currentFilters.dateFilter
                            }
                            
                            val updatedSortBy = if (newSortOptions.isNotEmpty() && 
                                currentFilters.sortBy !in newSortOptions) {
                                newSortOptions.first()
                            } else {
                                currentFilters.sortBy
                            }
                            
                            currentFilters = currentFilters.copy(
                                additionalFilter = filter,
                                dateFilter = updatedDateFilter,
                                sortBy = updatedSortBy
                            )
                            onFiltersChanged(currentFilters)
                        },
                        itemLabel = { it.title },
                        columns = filterColumns
                    )
                }
            }
            
            // Date Filter Section - dynamically updates based on selected report type
            if (dateFilters.isNotEmpty()) {
                item {
                    FilterSection(
                        title = "Date Range",
                        items = dateFilters,
                        selectedItem = currentFilters.dateFilter,
                        onItemSelected = { dateFilter ->
                            currentFilters = currentFilters.copy(dateFilter = dateFilter)
                            onFiltersChanged(currentFilters)
                        },
                        itemLabel = { it.title },
                        columns = filterColumns
                    )
                }
            }
            
            // Custom Date Range (if selected)
            if (currentFilters.dateFilter == ReportDateFilter.CUSTOM_DATE) {
                item {
                    CustomDateRangeSection(
                        startDate = currentFilters.customStartDate,
                        endDate = currentFilters.customEndDate,
                        onStartDateChanged = { date ->
                            currentFilters = currentFilters.copy(customStartDate = date)
                            onFiltersChanged(currentFilters)
                        },
                        onEndDateChanged = { date ->
                            currentFilters = currentFilters.copy(customEndDate = date)
                            onFiltersChanged(currentFilters)
                        },
                        isDesktop = isDesktop
                    )
                }
            }
            
            // Group By Section - dynamically updates based on selected report type
            if (groupByOptions.isNotEmpty()) {
                item {
                    FilterSection(
                        title = "Group By",
                        items = groupByOptions,
                        selectedItem = currentFilters.groupBy,
                        onItemSelected = { groupBy ->
                            currentFilters = currentFilters.copy(groupBy = groupBy)
                            onFiltersChanged(currentFilters)
                        },
                        itemLabel = { it.title },
                        optional = true,
                        columns = filterColumns
                    )
                }
            }
            
            // Sort By Section - dynamically updates based on selected report type
            if (sortOptions.isNotEmpty()) {
                item {
                    FilterSection(
                        title = "Sort By",
                        items = sortOptions,
                        selectedItem = currentFilters.sortBy,
                        onItemSelected = { sortBy ->
                            currentFilters = currentFilters.copy(sortBy = sortBy)
                            onFiltersChanged(currentFilters)
                        },
                        itemLabel = { it.title },
                        columns = filterColumns
                    )
                }
            }
            
            // Info about required selections
            if (currentFilters.requiresPartySelection() || 
                currentFilters.requiresProductSelection() || 
                currentFilters.requiresWarehouseSelection()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Note:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val requirements = buildList {
                                if (currentFilters.requiresPartySelection()) {
                                    add("• This report requires party selection")
                                }
                                if (currentFilters.requiresProductSelection()) {
                                    add("• This report requires product selection")
                                }
                                if (currentFilters.requiresWarehouseSelection()) {
                                    add("• This report requires warehouse selection")
                                }
                            }
                            requirements.forEach { requirement ->
                                Text(
                                    text = requirement,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun <T> FilterSection(
    title: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    itemLabel: (T) -> String,
    optional: Boolean = false,
    columns: Int = 3
) {
    Column {
        Text(
            text = title + if (optional) " (Optional)" else "",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 400.dp)
        ) {
            items(items) { item ->
                FilterChipWithColors(
                    selected = item == selectedItem,
                    onClick = { onItemSelected(item) },
                    label = itemLabel(item),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomDateRangeSection(
    startDate: String?,
    endDate: String?,
    onStartDateChanged: (String) -> Unit,
    onEndDateChanged: (String) -> Unit,
    isDesktop: Boolean = false
) {
    var showDateRangePicker by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Custom Date Range",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            if (isDesktop) {
                // Side by side on desktop
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DateRangeField(
                        label = "Start Date",
                        dateString = startDate,
                        onClick = { showDateRangePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                    
                    DateRangeField(
                        label = "End Date",
                        dateString = endDate,
                        onClick = { showDateRangePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Stacked on mobile
                DateRangeField(
                    label = "Start Date",
                    dateString = startDate,
                    onClick = { showDateRangePicker = true },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                DateRangeField(
                    label = "End Date",
                    dateString = endDate,
                    onClick = { showDateRangePicker = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    
    // Date Range Picker Dialog
    if (showDateRangePicker) {
        DateRangePickerDialog(
            initialStartDate = startDate,
            initialEndDate = endDate,
            onConfirm = { newStartDate, newEndDate ->
                onStartDateChanged(newStartDate)
                onEndDateChanged(newEndDate)
                showDateRangePicker = false
            },
            onDismiss = { showDateRangePicker = false }
        )
    }
}

// Note: shouldShowGroupBy, shouldShowSortBy, and getSortOptionsForReportType are no longer needed
// as they are now handled by ReportFiltersFactory methods:
// - getGroupByOptions() for group by options
// - getSortOptions() for sort options

@Composable
private fun SelectedEntityCard(
    entity: Any,
    reportType: ReportType,
    isDesktop: Boolean
) {
    val entityName = when (entity) {
        is Party -> entity.displayName
        is Product -> entity.displayName
        is Warehouse -> entity.displayName
        else -> "Unknown"
    }
    
    val entityType = when (entity) {
        is Party -> {
            when (entity.roleId) {
                PartyType.CUSTOMER.type -> "Customer"
                PartyType.VENDOR.type -> "Vendor"
                PartyType.INVESTOR.type -> "Investor"
                else -> "Party"
            }
        }
        is Product -> "Product"
        is Warehouse -> "Warehouse"
        else -> "Entity"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Selected $entityType",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entityName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

