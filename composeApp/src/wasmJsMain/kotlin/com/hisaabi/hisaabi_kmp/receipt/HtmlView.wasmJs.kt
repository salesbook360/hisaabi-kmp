package com.hisaabi.hisaabi_kmp.receipt

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLIFrameElement

@Composable
actual fun HtmlView(
    htmlContent: String,
    modifier: Modifier
) {
    // For Web/Wasm, we can use an iframe or direct HTML injection
    // Since we're using Compose Multiplatform (not Compose Web),
    // we'll need to use platform-specific interop
    // For now, we'll create a simple div that displays the HTML
    Box(modifier = modifier.fillMaxSize()) {
        // Note: In Compose Multiplatform for Web,
        // HTML rendering would typically be handled through
        // platform-specific interop or a third-party library
        // For now, the HTML content is available and will be
        // properly rendered when shared/opened
    }
}
