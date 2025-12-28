package com.hisaabi.hisaabi_kmp.receipt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Scaffold(
            bottomBar = {
                Surface(
                    tonalElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Close")
                        }
                        Button(
                            onClick = onShare,
                            modifier = Modifier.weight(1f),
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    ReceiptContent(
                        transaction = transaction,
                        config = config,
                        currencySymbol = currencySymbol,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

