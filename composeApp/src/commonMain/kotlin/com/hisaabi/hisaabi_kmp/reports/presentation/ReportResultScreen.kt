package com.hisaabi.hisaabi_kmp.reports.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaabi.hisaabi_kmp.core.ui.LocalWindowSizeClass
import com.hisaabi.hisaabi_kmp.core.ui.WindowWidthSizeClass
import com.hisaabi.hisaabi_kmp.reports.domain.model.ReportResult
import com.hisaabi.hisaabi_kmp.reports.domain.model.ReportType
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportResultScreen(
    reportResult: ReportResult,
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    val preferencesManager: PreferencesManager = koinInject()
    val selectedCurrency by preferencesManager.selectedCurrency.collectAsState(null)
    val currencySymbol = selectedCurrency?.symbol?:""
    
    val generatedDate = remember(reportResult.generatedAt) {
        val dateTime = Instant.fromEpochMilliseconds(reportResult.generatedAt)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        "${dateTime.date} ${dateTime.time}"
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(reportResult.reportType.title)
                        Text(
                            text = "Generated: $generatedDate",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onShareClick) {
                        Icon(Icons.Default.Share, contentDescription = "Share PDF")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        val windowSizeClass = LocalWindowSizeClass.current
        val isDesktop = windowSizeClass.widthSizeClass == WindowWidthSizeClass.EXPANDED
        val maxContentWidth = if (isDesktop) 1000.dp else Dp.Unspecified
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
                verticalArrangement = Arrangement.spacedBy(if (isDesktop) 20.dp else 16.dp)
            ) {
                // Hide summary and filter info for balance sheet
                val isBalanceSheet = reportResult.reportType == ReportType.BALANCE_SHEET
                
                // Summary and Filter Info - Side by side on desktop (skip for balance sheet)
                if (!isBalanceSheet) {
                    if (isDesktop && reportResult.summary != null) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    SummaryCard(reportResult.summary!!, currencySymbol)
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    FilterInfoCard(reportResult)
                                }
                            }
                        }
                    } else {
                        // Summary Card (if available)
                        reportResult.summary?.let { summary ->
                            item {
                                SummaryCard(summary, currencySymbol)
                            }
                        }
                        
                        // Filter Info Card
                        item {
                            FilterInfoCard(reportResult)
                        }
                    }
                }
            
            // Table Header - Special handling for balance sheet (4 columns with merged headers)
            item {
                if (isBalanceSheet) {
                    BalanceSheetHeader()
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            reportResult.columns.forEach { column ->
                                Text(
                                    text = column,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                }
            }
            
            // Table Rows
            if (reportResult.rows.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No data available for the selected filters",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Use professional table layout for balance sheet, regular cards for other reports
                if (isBalanceSheet) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            BalanceSheetTable(
                                rows = reportResult.rows,
                                columns = reportResult.columns
                            )
                            // Add bottom spacing to ensure card shadow is visible
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                } else {
                    items(reportResult.rows) { row ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                row.values.forEach { value ->
                                    Text(
                                        text = value,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Start
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
}

@Composable
private fun SummaryCard(summary: com.hisaabi.hisaabi_kmp.reports.domain.model.ReportSummary, currencySymbol:String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            
            HorizontalDivider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    summary.totalAmount?.let { amount ->
                        SummaryItem("Total Amount", "$currencySymbol ${String.format("%,.0f", amount)}")
                    }
                    summary.totalProfit?.let { profit ->
                        SummaryItem("Total Profit", "$currencySymbol ${String.format("%,.0f", profit)}")
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    summary.totalQuantity?.let { qty ->
                        SummaryItem("Total Quantity", String.format("%,.0f", qty))
                    }
                    SummaryItem("Record Count", summary.recordCount.toString())
                }
            }
            
            // Additional info
            if (summary.additionalInfo.isNotEmpty()) {
                HorizontalDivider()
                summary.additionalInfo.forEach { (key, value) ->
                    SummaryItem(key, value)
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
private fun BalanceSheetHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Assets header (spans 2 columns)
            Text(
                text = "Assets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(2f),
                textAlign = TextAlign.Center
            )
            // Liabilities header (spans 2 columns)
            Text(
                text = "Liabilities",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(2f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BalanceSheetTable(
    rows: List<com.hisaabi.hisaabi_kmp.reports.domain.model.ReportRow>,
    columns: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            rows.forEachIndexed { index, row ->
                // 4 columns: Assets Label | Assets Value | Liabilities Label | Liabilities Value
                val assetLabel = row.values.getOrNull(0)?.trim() ?: ""
                val assetValue = row.values.getOrNull(1)?.trim() ?: ""
                val liabilityLabel = row.values.getOrNull(2)?.trim() ?: ""
                val liabilityValue = row.values.getOrNull(3)?.trim() ?: ""
                
                val isTotalRow = assetLabel.contains("Total", ignoreCase = true) || 
                                 liabilityLabel.contains("Total", ignoreCase = true)
                val isEmptyRow = row.values.all { it.trim().isEmpty() }
                
                // Skip empty rows
                if (!isEmptyRow) {
                    // Determine background color
                    val backgroundColor = if (isTotalRow) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                    
                    // Row content - 4 columns (all left-aligned)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(backgroundColor)
                            .padding(
                                horizontal = 16.dp,
                                vertical = if (isTotalRow) 14.dp else 11.dp
                            ),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Column 1: Assets Label
                        Text(
                            text = assetLabel,
                            style = if (isTotalRow) {
                                MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                MaterialTheme.typography.bodyMedium
                            },
                            color = if (isTotalRow) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Start
                        )
                        
                        // Column 2: Assets Value
                        Text(
                            text = assetValue,
                            style = if (isTotalRow) {
                                MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                MaterialTheme.typography.bodyMedium
                            },
                            color = if (isTotalRow) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Start
                        )
                        
                        // Column 3: Liabilities Label
                        Text(
                            text = liabilityLabel,
                            style = if (isTotalRow) {
                                MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                MaterialTheme.typography.bodyMedium
                            },
                            color = if (isTotalRow) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Start
                        )
                        
                        // Column 4: Liabilities Value
                        Text(
                            text = liabilityValue,
                            style = if (isTotalRow) {
                                MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                MaterialTheme.typography.bodyMedium
                            },
                            color = if (isTotalRow) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Start
                        )
                    }
                    
                    // Add divider after each row (except last)
                    if (index < rows.size - 1) {
                        val nextRow = rows.getOrNull(index + 1)
                        val nextAssetLabel = nextRow?.values?.getOrNull(0)?.trim() ?: ""
                        val nextIsTotal = nextAssetLabel.contains("Total", ignoreCase = true)
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = if (isTotalRow || nextIsTotal) 1.5.dp else 0.5.dp,
                            color = if (isTotalRow || nextIsTotal) {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterInfoCard(reportResult: ReportResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Filters Applied",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Text(
                text = "Date: ${reportResult.filters.dateFilter.title}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            reportResult.filters.additionalFilter?.let {
                Text(
                    text = "Filter: ${it.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            reportResult.filters.groupBy?.let {
                Text(
                    text = "Grouped by: ${it.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Text(
                text = "Sorted by: ${reportResult.filters.sortBy.title}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

