package com.hisaabi.hisaabi_kmp.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.core.ui.LocalWindowSizeClass
import com.hisaabi.hisaabi_kmp.core.ui.ResponsiveContainer
import com.hisaabi.hisaabi_kmp.core.ui.WindowWidthSizeClass
import com.hisaabi.hisaabi_kmp.home.dashboard.*
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.sync.presentation.SyncStatusComponent
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    // Inject DashboardViewModel from Koin
    val viewModel: DashboardViewModel = koinInject()
    val preferencesManager: PreferencesManager = koinInject()
    val selectedCurrency by preferencesManager.selectedCurrency.collectAsState(null)
    val currencySymbol = selectedCurrency?.symbol?:""
    
    // Collect states from ViewModel
    val balanceOverview by viewModel.balanceOverview.collectAsState()
    val paymentOverview by viewModel.paymentOverview.collectAsState()
    val salesOverview by viewModel.salesOverview.collectAsState()
    val purchaseOverview by viewModel.purchaseOverview.collectAsState()
    val inventorySummary by viewModel.inventorySummary.collectAsState()
    val partiesSummary by viewModel.partiesSummary.collectAsState()
    val productsSummary by viewModel.productsSummary.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                windowInsets = WindowInsets(0),
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        val windowSizeClass = LocalWindowSizeClass.current
        val isExpandedLayout = windowSizeClass.widthSizeClass == WindowWidthSizeClass.EXPANDED
        
        // Wrap content in a centered container with max width
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = windowSizeClass.maxContentWidth)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sync Status Component
                    SyncStatusComponent()
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    if (isExpandedLayout) {
                        // Two-column layout for desktop with matched heights per row
                        
                        // Row 1: Balance Overview + Payment Overview
                        Row(
                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            DashboardCard(modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 0.dp)) {
                                OverviewSection(
                                    modifier = Modifier.fillMaxWidth(),
                                    dataState = balanceOverview,
                                    currencySymbol = currencySymbol
                                )
                            }
                            DashboardCard(modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 0.dp)) {
                                OverviewSection(
                                    modifier = Modifier.fillMaxWidth(),
                                    dataState = paymentOverview,
                                    currencySymbol = currencySymbol
                                )
                            }
                        }
                        
                        // Row 2: Sales Overview + Purchase Overview
                        Row(
                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            DashboardCard(modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 0.dp)) {
                                OverviewSection(
                                    modifier = Modifier.fillMaxWidth(),
                                    dataState = salesOverview,
                                    currencySymbol = currencySymbol
                                )
                            }
                            DashboardCard(modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 0.dp)) {
                                OverviewSection(
                                    modifier = Modifier.fillMaxWidth(),
                                    dataState = purchaseOverview,
                                    currencySymbol = currencySymbol
                                )
                            }
                        }
                        
                        // Row 3: Inventory Summary + Parties Summary
                        Row(
                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            DashboardCard(modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 0.dp)) {
                                SummarySection(
                                    modifier = Modifier.fillMaxWidth(),
                                    dataState = inventorySummary
                                )
                            }
                            DashboardCard(modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 0.dp)) {
                                SummarySection(
                                    modifier = Modifier.fillMaxWidth(),
                                    dataState = partiesSummary
                                )
                            }
                        }
                        
                        // Row 4: Products Summary (single, centered)
                        DashboardCard(modifier = Modifier.fillMaxWidth(0.5f).padding(horizontal = 0.dp)) {
                            SummarySection(
                                modifier = Modifier.fillMaxWidth(),
                                dataState = productsSummary
                            )
                        }
                    } else {
                        // Single column layout for mobile/tablet
                        // Balance Overview Section
                        DashboardCard(modifier = Modifier.padding(horizontal = 0.dp)) {
                            OverviewSection(
                                modifier = Modifier.fillMaxWidth(),
                                dataState = balanceOverview,
                                currencySymbol = currencySymbol
                            )
                        }
                        
                        // Payment Overview Section
                        DashboardCard(modifier = Modifier.padding(horizontal = 0.dp)) {
                            OverviewSection(
                                modifier = Modifier.fillMaxWidth(),
                                dataState = paymentOverview,
                                currencySymbol = currencySymbol
                            )
                        }
                        
                        // Sales Overview Section
                        DashboardCard(modifier = Modifier.padding(horizontal = 0.dp)) {
                            OverviewSection(
                                modifier = Modifier.fillMaxWidth(),
                                dataState = salesOverview,
                                currencySymbol = currencySymbol
                            )
                        }
                        
                        // Purchase Overview Section
                        DashboardCard(modifier = Modifier.padding(horizontal = 0.dp)) {
                            OverviewSection(
                                modifier = Modifier.fillMaxWidth(),
                                dataState = purchaseOverview,
                                currencySymbol = currencySymbol
                            )
                        }
                        
                        // Inventory Summary Section
                        DashboardCard(modifier = Modifier.padding(horizontal = 0.dp)) {
                            SummarySection(
                                modifier = Modifier.fillMaxWidth(),
                                dataState = inventorySummary
                            )
                        }
                        
                        // Parties Summary Section
                        DashboardCard(modifier = Modifier.padding(horizontal = 0.dp)) {
                            SummarySection(
                                modifier = Modifier.fillMaxWidth(),
                                dataState = partiesSummary
                            )
                        }
                        
                        // Products Summary Section
                        DashboardCard(modifier = Modifier.padding(horizontal = 0.dp)) {
                            SummarySection(
                                modifier = Modifier.fillMaxWidth(),
                                dataState = productsSummary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

