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
import androidx.compose.ui.Alignment
import com.hisaabi.hisaabi_kmp.reports.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFiltersScreen(
    reportType: ReportType,
    filters: ReportFilters = ReportFilters(reportType = reportType),
    onBackClick: () -> Unit = {},
    onFiltersChanged: (ReportFilters) -> Unit = {},
    onGenerateReport: (ReportFilters) -> Unit = {}
) {
    var currentFilters by remember { mutableStateOf(filters) }
    val additionalFilters = ReportAdditionalFilter.getFiltersForReportType(reportType)
    
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
        val maxContentWidth = if (isDesktop) 900.dp else Dp.Unspecified
        val horizontalPadding = if (isDesktop) 24.dp else 16.dp
        
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
            // Additional Filters Section
            if (additionalFilters.isNotEmpty()) {
                item {
                    FilterSection(
                        title = "Report Type",
                        items = additionalFilters,
                        selectedItem = currentFilters.additionalFilter,
                        onItemSelected = { filter ->
                            currentFilters = currentFilters.copy(additionalFilter = filter)
                            onFiltersChanged(currentFilters)
                        },
                        itemLabel = { it.title }
                    )
                }
            }
            
            // Date Filter Section
            item {
                FilterSection(
                    title = "Date Range",
                    items = ReportDateFilter.entries,
                    selectedItem = currentFilters.dateFilter,
                    onItemSelected = { dateFilter ->
                        currentFilters = currentFilters.copy(dateFilter = dateFilter)
                        onFiltersChanged(currentFilters)
                    },
                    itemLabel = { it.title }
                )
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
                        }
                    )
                }
            }
            
            // Group By Section (for applicable reports)
            if (shouldShowGroupBy(reportType)) {
                item {
                    FilterSection(
                        title = "Group By",
                        items = ReportGroupBy.entries,
                        selectedItem = currentFilters.groupBy,
                        onItemSelected = { groupBy ->
                            currentFilters = currentFilters.copy(groupBy = groupBy)
                            onFiltersChanged(currentFilters)
                        },
                        itemLabel = { it.title },
                        optional = true
                    )
                }
            }
            
            // Sort By Section
            if (shouldShowSortBy(reportType)) {
                item {
                    FilterSection(
                        title = "Sort By",
                        items = getSortOptionsForReportType(reportType),
                        selectedItem = currentFilters.sortBy,
                        onItemSelected = { sortBy ->
                            currentFilters = currentFilters.copy(sortBy = sortBy)
                            onFiltersChanged(currentFilters)
                        },
                        itemLabel = { it.title }
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
    optional: Boolean = false
) {
    Column {
        Text(
            text = title + if (optional) " (Optional)" else "",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
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
    onEndDateChanged: (String) -> Unit
) {
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
            
            OutlinedTextField(
                value = startDate ?: "",
                onValueChange = onStartDateChanged,
                label = { Text("Start Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = endDate ?: "",
                onValueChange = onEndDateChanged,
                label = { Text("End Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

private fun shouldShowGroupBy(reportType: ReportType): Boolean {
    return reportType in listOf(
        ReportType.SALE_REPORT,
        ReportType.PURCHASE_REPORT,
        ReportType.TOP_PRODUCTS,
        ReportType.TOP_CUSTOMERS,
        ReportType.STOCK_REPORT
    )
}

private fun shouldShowSortBy(reportType: ReportType): Boolean {
    return reportType !in listOf(
        ReportType.BALANCE_SHEET,
        ReportType.CASH_IN_HAND
    )
}

private fun getSortOptionsForReportType(reportType: ReportType): List<ReportSortBy> {
    return when (reportType) {
        ReportType.SALE_REPORT, ReportType.PURCHASE_REPORT -> listOf(
            ReportSortBy.DATE_ASC, ReportSortBy.DATE_DESC,
            ReportSortBy.SALE_AMOUNT_ASC, ReportSortBy.SALE_AMOUNT_DESC,
            ReportSortBy.PROFIT_ASC, ReportSortBy.PROFIT_DESC
        )
        ReportType.TOP_PRODUCTS, ReportType.TOP_CUSTOMERS -> listOf(
            ReportSortBy.TITLE_ASC, ReportSortBy.TITLE_DESC,
            ReportSortBy.PROFIT_ASC, ReportSortBy.PROFIT_DESC,
            ReportSortBy.SALE_AMOUNT_ASC, ReportSortBy.SALE_AMOUNT_DESC
        )
        ReportType.CUSTOMER_REPORT, ReportType.VENDOR_REPORT, ReportType.BALANCE_REPORT -> listOf(
            ReportSortBy.TITLE_ASC, ReportSortBy.TITLE_DESC,
            ReportSortBy.BALANCE_ASC, ReportSortBy.BALANCE_DESC
        )
        else -> listOf(
            ReportSortBy.TITLE_ASC, ReportSortBy.TITLE_DESC,
            ReportSortBy.DATE_ASC, ReportSortBy.DATE_DESC
        )
    }
}

