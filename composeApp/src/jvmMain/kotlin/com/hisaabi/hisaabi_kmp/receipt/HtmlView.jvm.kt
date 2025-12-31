package com.hisaabi.hisaabi_kmp.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
actual fun HtmlView(
    htmlContent: String,
    modifier: Modifier
) {
    // For Desktop, we'll show a message that the preview uses HTML
    // The actual HTML rendering would require a WebView component
    // which isn't readily available in Compose Desktop without additional dependencies
    // The shared HTML file will contain the exact same content as this preview
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Receipt Preview",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "The receipt is rendered as HTML. The shared file will match this preview exactly. Click 'Share Receipt' to view the formatted HTML receipt.",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
            
            // Note: For a full HTML preview on desktop, you would need to:
            // 1. Use JavaFX WebView (requires additional dependency)
            // 2. Use a third-party HTML rendering library
            // 3. Use JCEF (Java Chromium Embedded Framework)
            
            // For now, Android will show the full HTML preview via WebView,
            // and Desktop will open the HTML file when shared
        }
    }
}
