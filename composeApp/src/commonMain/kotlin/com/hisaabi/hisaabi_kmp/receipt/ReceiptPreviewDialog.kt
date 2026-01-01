package com.hisaabi.hisaabi_kmp.receipt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hisaabi.hisaabi_kmp.core.ui.LocalWindowSizeClass
import com.hisaabi.hisaabi_kmp.core.ui.WindowWidthSizeClass
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.settings.domain.model.ReceiptConfig
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptPreviewDialog(
    transaction: Transaction,
    config: ReceiptConfig,
    isGenerating: Boolean,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    // Currency
    val preferencesManager: PreferencesManager = koinInject()
    val selectedCurrency by preferencesManager.selectedCurrency.collectAsState(null)
    val currencySymbol = selectedCurrency?.symbol ?: ""
    
    // Window size class for responsive layout
    val windowSizeClass = LocalWindowSizeClass.current
    val isDesktop = windowSizeClass.widthSizeClass == WindowWidthSizeClass.EXPANDED
    val isMedium = windowSizeClass.widthSizeClass == WindowWidthSizeClass.MEDIUM
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        // Responsive container for the dialog
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .then(
                        if (isDesktop) {
                            Modifier
                                .widthIn(max = 700.dp)
                                .heightIn(max = 700.dp)
                                .padding(32.dp)
                        } else if (isMedium) {
                            Modifier
                                .widthIn(max = 450.dp)
                                .fillMaxHeight(0.9f)
                                .padding(24.dp)
                        } else {
                            Modifier.fillMaxSize()
                        }
                    ),
                shape = if (isDesktop || isMedium) RoundedCornerShape(16.dp) else RoundedCornerShape(0.dp),
                tonalElevation = if (isDesktop || isMedium) 6.dp else 0.dp,
                shadowElevation = if (isDesktop || isMedium) 8.dp else 0.dp
            ) {
                Scaffold(
                    topBar = {
                        if (isDesktop || isMedium) {
                            TopAppBar(
                                title = { Text("Receipt Preview") },
                                navigationIcon = {
                                    IconButton(onClick = onDismiss) {
                                        Icon(Icons.Default.Close, "Close")
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    },
                    bottomBar = {
                        Surface(
                            tonalElevation = 3.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = if (isDesktop) 24.dp else 16.dp,
                                        vertical = if (isDesktop) 16.dp else 16.dp
                                    ),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (!isDesktop && !isMedium) {
                                    // Show Close button only on mobile (no top bar)
                                    OutlinedButton(
                                        onClick = onDismiss,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Close")
                                    }
                                }
                                Button(
                                    onClick = onShare,
                                    modifier = if (isDesktop || isMedium) Modifier.fillMaxWidth() else Modifier.weight(1f),
                                    enabled = !isGenerating
                                ) {
                                    if (isGenerating) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Spacer(Modifier.width(8.dp))
                                    } else {
                                        Icon(Icons.Default.Share, null, Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                    }
                                    Text(if (isGenerating) "Generating..." else "Share Receipt")
                                }
                            }
                        }
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        // Generate HTML content for preview
                        val htmlContent = remember(transaction, config, currencySymbol) {
                            ReceiptHtmlGenerator.generateHtmlReceipt(transaction, config, currencySymbol)
                        }
                        
                        HtmlView(
                            htmlContent = htmlContent,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

