package com.hisaabi.hisaabi_kmp.receipt

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific HTML view composable
 */
@Composable
expect fun HtmlView(
    htmlContent: String,
    modifier: Modifier = Modifier
)

