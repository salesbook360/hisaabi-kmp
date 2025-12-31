package com.hisaabi.hisaabi_kmp.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.awt.SwingPanel
import javax.swing.JEditorPane
import javax.swing.JScrollPane
import javax.swing.text.html.HTMLEditorKit
import javax.swing.text.html.StyleSheet

@Composable
actual fun HtmlView(
    htmlContent: String,
    modifier: Modifier
) {
    // Use Swing's JEditorPane with HTML support for embedded HTML rendering
    // This is a simple, lightweight solution that works across all desktop platforms
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        SwingPanel(
            modifier = modifier.fillMaxSize(),
            factory = {
                val editorKit = HTMLEditorKit()
                val styleSheet = editorKit.styleSheet
                
                // HTMLEditorKit doesn't support flexbox, so we override with inline-block layout
                // This ensures key-value pairs are properly spaced
                styleSheet.addRule("""
                    .info-row {
                        width: 100%;
                        overflow: hidden;
                    }
                    .info-label {
                        display: inline-block;
                        width: 45%;
                        vertical-align: top;
                    }
                    .info-value {
                        display: inline-block;
                        width: 54%;
                        text-align: right;
                        vertical-align: top;
                    }
                    .total-row {
                        width: 100%;
                        overflow: hidden;
                    }
                    .total-row span:first-child {
                        display: inline-block;
                        width: 48%;
                        vertical-align: top;
                    }
                    .total-row span:last-child {
                        display: inline-block;
                        width: 51%;
                        text-align: right;
                        vertical-align: top;
                    }
                """.trimIndent())
                
                val editorPane = JEditorPane().apply {
                    this.editorKit = editorKit
                    isEditable = false
                    contentType = "text/html"
                    text = htmlContent
                    background = java.awt.Color.WHITE
                }
                
                JScrollPane(editorPane).apply {
                    verticalScrollBarPolicy = javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
                    horizontalScrollBarPolicy = javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
                }
            },
            update = { scrollPane ->
                val editorPane = scrollPane.viewport.view as? JEditorPane
                if (editorPane != null) {
                    // Reload content to apply styles
                    editorPane.text = htmlContent
                }
            }
        )
    }
}
