package com.hisaabi.hisaabi_kmp.reports.presentation

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaabi.hisaabi_kmp.reports.domain.model.ReportResult
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
            
            // Table Header
            item {
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

