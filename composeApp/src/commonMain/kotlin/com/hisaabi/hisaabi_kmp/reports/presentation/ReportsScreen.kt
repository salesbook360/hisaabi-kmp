package com.hisaabi.hisaabi_kmp.reports.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.hisaabi.hisaabi_kmp.reports.domain.model.ReportType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onBackClick: () -> Unit = {},
    onReportSelected: (ReportType) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
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
        }
    ) { paddingValues ->
        val windowSizeClass = LocalWindowSizeClass.current
        val isDesktop = windowSizeClass.widthSizeClass == WindowWidthSizeClass.EXPANDED
        val isMedium = windowSizeClass.widthSizeClass == WindowWidthSizeClass.MEDIUM
        val maxContentWidth = if (isDesktop) 900.dp else Dp.Unspecified
        val horizontalPadding = if (isDesktop) 24.dp else 16.dp
        
        // Adaptive columns: 3 on mobile, 4 on tablet, 5 on desktop
        val gridColumns = when {
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
            Column(
                modifier = Modifier
                    .then(if (isDesktop) Modifier.widthIn(max = maxContentWidth) else Modifier.fillMaxWidth())
                    .padding(horizontal = horizontalPadding, vertical = 16.dp)
            ) {
                Text(
                    text = "Select Report Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridColumns),
                    horizontalArrangement = Arrangement.spacedBy(if (isDesktop) 16.dp else 12.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isDesktop) 16.dp else 12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(ReportType.entries) { reportType ->
                        ReportTypeCard(
                            reportType = reportType,
                            onClick = { onReportSelected(reportType) },
                            isDesktop = isDesktop
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportTypeCard(
    reportType: ReportType,
    onClick: () -> Unit,
    isDesktop: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isDesktop) 12.dp else 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = reportType.icon,
                contentDescription = reportType.title,
                modifier = Modifier.size(if (isDesktop) 36.dp else 32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = reportType.title,
                style = MaterialTheme.typography.bodySmall,
                fontSize = if (isDesktop) 12.sp else 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                lineHeight = if (isDesktop) 16.sp else 14.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

