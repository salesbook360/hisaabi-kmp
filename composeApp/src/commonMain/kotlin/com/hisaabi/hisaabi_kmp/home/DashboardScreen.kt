package com.hisaabi.hisaabi_kmp.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.home.dashboard.*
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    // Inject DashboardViewModel from Koin
    val viewModel: DashboardViewModel = koinInject()
    
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            
            // Balance Overview Section
            DashboardCard {
                OverviewSection(
                    modifier = Modifier.fillMaxWidth(),
                    dataState = balanceOverview,
                    currencySymbol = "₹"
                )
            }
            
            // Payment Overview Section
            DashboardCard {
                OverviewSection(
                    modifier = Modifier.fillMaxWidth(),
                    dataState = paymentOverview,
                    currencySymbol = "₹"
                )
            }
            
            // Sales Overview Section
            DashboardCard {
                OverviewSection(
                    modifier = Modifier.fillMaxWidth(),
                    dataState = salesOverview,
                    currencySymbol = "₹"
                )
            }
            
            // Purchase Overview Section
            DashboardCard {
                OverviewSection(
                    modifier = Modifier.fillMaxWidth(),
                    dataState = purchaseOverview,
                    currencySymbol = "₹"
                )
            }
            
            // Inventory Summary Section
            DashboardCard {
                SummarySection(
                    modifier = Modifier.fillMaxWidth(),
                    dataState = inventorySummary
                )
            }
            
            // Parties Summary Section
            DashboardCard {
                SummarySection(
                    modifier = Modifier.fillMaxWidth(),
                    dataState = partiesSummary
                )
            }
            
            // Products Summary Section
            DashboardCard {
                SummarySection(
                    modifier = Modifier.fillMaxWidth(),
                    dataState = productsSummary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

