package com.hisaabi.hisaabi_kmp.reports.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.hisaabi.hisaabi_kmp.reports.domain.model.ProfitLossBreakdown
import com.hisaabi.hisaabi_kmp.reports.domain.model.ReceivableBreakdown
import com.hisaabi.hisaabi_kmp.reports.domain.model.PayableBreakdown
import com.hisaabi.hisaabi_kmp.reports.domain.model.CashInHandBreakdown
import com.hisaabi.hisaabi_kmp.reports.domain.model.CapitalInvestmentBreakdown
import com.hisaabi.hisaabi_kmp.reports.domain.model.AvailableStockBreakdown
import com.hisaabi.hisaabi_kmp.reports.domain.model.ReportRow
import com.hisaabi.hisaabi_kmp.reports.domain.model.ReportSummary
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
                            BalanceSheetTableWithDialog(
                                rows = reportResult.rows,
                                columns = reportResult.columns,
                                profitLossBreakdown = reportResult.profitLossBreakdown,
                                currencySymbol = currencySymbol,
                                receivableBreakdown = reportResult.receivableBreakdown,
                                payablesBreakdown = reportResult.payablesBreakdown,
                                cashInHandBreakdown = reportResult.cashInHandBreakdown,
                                capitalInvestmentBreakdown = reportResult.capitalInvestmentBreakdown,
                                availableStockBreakdown = reportResult.availableStockBreakdown
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
private fun SummaryCard(summary: ReportSummary, currencySymbol:String) {
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
private fun BalanceSheetTableWithDialog(
    rows: List<ReportRow>,
    columns: List<String>,
    profitLossBreakdown: ProfitLossBreakdown?,
    receivableBreakdown: ReceivableBreakdown?,
    payablesBreakdown: PayableBreakdown?,
    cashInHandBreakdown: CashInHandBreakdown?,
    capitalInvestmentBreakdown: CapitalInvestmentBreakdown?,
    availableStockBreakdown: AvailableStockBreakdown?,
    currencySymbol: String
) {
    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogType by remember { mutableStateOf<BreakdownType?>(null) }

    BalanceSheetTable(
        rows = rows,
        columns = columns,
        onCellClick = { label, value ->
            dialogType = when {
                label.equals("Receivable", ignoreCase = true) && receivableBreakdown != null -> {
                    dialogTitle = "Receivables Breakdown"
                    BreakdownType.RECEIVABLE
                }
                label.equals("Available Stock", ignoreCase = true) && availableStockBreakdown != null -> {
                    dialogTitle = "Available Stock Breakdown"
                    BreakdownType.AVAILABLE_STOCK
                }
                label.equals("Cash in Hand", ignoreCase = true) && cashInHandBreakdown != null -> {
                    dialogTitle = "Cash in Hand Breakdown"
                    BreakdownType.CASH_IN_HAND
                }
                label.equals("Capital Investment", ignoreCase = true) && capitalInvestmentBreakdown != null -> {
                    dialogTitle = "Capital Investment Breakdown"
                    BreakdownType.CAPITAL_INVESTMENT
                }
                label.equals("Payables", ignoreCase = true) && payablesBreakdown != null -> {
                    dialogTitle = "Payables Breakdown"
                    BreakdownType.PAYABLES
                }
                label.equals("Current Profit/Loss", ignoreCase = true) && profitLossBreakdown != null -> {
                    dialogTitle = "Current Profit/Loss Calculation"
                    BreakdownType.PROFIT_LOSS
                }
                else -> null
            }
            if (dialogType != null) {
                showDialog = true
            }
        }
    )

    // Dialog for showing breakdown details
    if (showDialog && dialogType != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(dialogTitle)
            },
            text = {
                when (dialogType) {
                    BreakdownType.RECEIVABLE -> receivableBreakdown?.let {
                        ReceivableBreakdownContent(it, currencySymbol)
                    }
                    BreakdownType.AVAILABLE_STOCK -> availableStockBreakdown?.let {
                        AvailableStockBreakdownContent(it, currencySymbol)
                    }
                    BreakdownType.CASH_IN_HAND -> cashInHandBreakdown?.let {
                        CashInHandBreakdownContent(it, currencySymbol)
                    }
                    BreakdownType.CAPITAL_INVESTMENT -> capitalInvestmentBreakdown?.let {
                        CapitalInvestmentBreakdownContent(it, currencySymbol)
                    }
                    BreakdownType.PAYABLES -> payablesBreakdown?.let {
                        PayablesBreakdownContent(it, currencySymbol)
                    }
                    BreakdownType.PROFIT_LOSS -> profitLossBreakdown?.let {
                        ProfitLossBreakdownContent(it, currencySymbol)
                    }
                    null -> {}
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

private enum class BreakdownType {
    RECEIVABLE,
    AVAILABLE_STOCK,
    CASH_IN_HAND,
    CAPITAL_INVESTMENT,
    PAYABLES,
    PROFIT_LOSS
}

@Composable
private fun ReceivableBreakdownContent(
    breakdown: ReceivableBreakdown,
    currencySymbol: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BreakdownRow("Customers", breakdown.customerReceivables, currencySymbol)
        BreakdownRow("Vendors", breakdown.vendorReceivables, currencySymbol)
        BreakdownRow("Investors", breakdown.investorReceivables, currencySymbol)
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        BreakdownRow("Total Receivables", breakdown.totalReceivables, currencySymbol, isTotal = true)
    }
}

@Composable
private fun PayablesBreakdownContent(
    breakdown: PayableBreakdown,
    currencySymbol: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BreakdownRow("Customers", breakdown.customerPayables, currencySymbol)
        BreakdownRow("Vendors", breakdown.vendorPayables, currencySymbol)
        BreakdownRow("Investors", breakdown.investorPayables, currencySymbol)
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        BreakdownRow("Total Payables", breakdown.totalPayables, currencySymbol, isTotal = true)
    }
}

@Composable
private fun CashInHandBreakdownContent(
    breakdown: CashInHandBreakdown,
    currencySymbol: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        breakdown.paymentMethodBreakdowns.forEach { (name, amount) ->
            BreakdownRow(name, amount, currencySymbol)
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        BreakdownRow("Total Cash in Hand", breakdown.totalCashInHand, currencySymbol, isTotal = true)
    }
}

@Composable
private fun CapitalInvestmentBreakdownContent(
    breakdown: CapitalInvestmentBreakdown,
    currencySymbol: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BreakdownRow("Opening Stock Worth", breakdown.openingStockWorth, currencySymbol)
        BreakdownRow("Opening Party Balances", breakdown.openingPartyBalances, currencySymbol)
        BreakdownRow("Opening Payment Methods", breakdown.openingPaymentAmounts, currencySymbol)
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        BreakdownRow("Total Capital Investment", breakdown.totalCapitalInvestment, currencySymbol, isTotal = true)
    }
}

@Composable
private fun AvailableStockBreakdownContent(
    breakdown: AvailableStockBreakdown,
    currencySymbol: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BreakdownRow("Total Available Stock", breakdown.totalAvailableStock, currencySymbol, isTotal = true)
    }
}

@Composable
private fun ProfitLossBreakdownContent(
    breakdown: ProfitLossBreakdown,
    currencySymbol: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BreakdownRow("Sale Amount", breakdown.saleAmount, currencySymbol)
        BreakdownRow("Cost of Sold Products", breakdown.costOfSoldProducts, currencySymbol)
        BreakdownRow("Profit/Loss", breakdown.profitOrLoss, currencySymbol, isSubItem = true)
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        BreakdownRow("Discount Taken", breakdown.discountTaken, currencySymbol)
        BreakdownRow("Discount Given", breakdown.discountGiven, currencySymbol)
        BreakdownRow("Total Expenses", breakdown.totalExpenses, currencySymbol)
        BreakdownRow("Total Income", breakdown.totalIncome, currencySymbol)
        BreakdownRow("Additional Charges Received", breakdown.additionalChargesReceived, currencySymbol)
        BreakdownRow("Additional Charges Paid", breakdown.additionalChargesPaid, currencySymbol)
        BreakdownRow("Tax Paid", breakdown.taxPaid, currencySymbol)
        BreakdownRow("Tax Received", breakdown.taxReceived, currencySymbol)
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        BreakdownRow("Total Profit/Loss", breakdown.totalProfitLoss, currencySymbol, isTotal = true)
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    value: Double,
    currencySymbol: String,
    isSubItem: Boolean = false,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isSubItem) "  $label" else label,
            style = if (isTotal) {
                MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = if (isTotal) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$currencySymbol ${String.format("%,.2f", value)}",
            style = if (isTotal) {
                MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = if (isTotal) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun BalanceSheetTable(
    rows: List<ReportRow>,
    columns: List<String>,
    onCellClick: (String, String) -> Unit = { _, _ -> }
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
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = assetLabel.isNotEmpty() && !isTotalRow) {
                                    onCellClick(assetLabel, assetValue)
                                }
                        ) {
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
                                textAlign = TextAlign.Start
                            )
                        }
                        
                        // Column 2: Assets Value
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = assetValue.isNotEmpty() && !isTotalRow) {
                                    onCellClick(assetLabel, assetValue)
                                }
                        ) {
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
                                textAlign = TextAlign.Start
                            )
                        }
                        
                        // Column 3: Liabilities Label
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = liabilityLabel.isNotEmpty() && !isTotalRow) {
                                    onCellClick(liabilityLabel, liabilityValue)
                                }
                        ) {
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
                                textAlign = TextAlign.Start
                            )
                        }
                        
                        // Column 4: Liabilities Value
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = liabilityValue.isNotEmpty() && !isTotalRow) {
                                    onCellClick(liabilityLabel, liabilityValue)
                                }
                        ) {
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
                                textAlign = TextAlign.Start
                            )
                        }
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
            
            // Only show date filter if it's applicable (not for STOCK_WORTH or OUT_OF_STOCK)
            val shouldShowDateFilter = reportResult.filters.additionalFilter !in listOf(
                com.hisaabi.hisaabi_kmp.reports.domain.model.ReportAdditionalFilter.STOCK_WORTH,
                com.hisaabi.hisaabi_kmp.reports.domain.model.ReportAdditionalFilter.OUT_OF_STOCK
            )
            
            if (shouldShowDateFilter) {
                Text(
                    text = "Date: ${reportResult.filters.dateFilter.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
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

