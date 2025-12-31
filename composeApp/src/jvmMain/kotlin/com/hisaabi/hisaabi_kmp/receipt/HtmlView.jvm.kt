package com.hisaabi.hisaabi_kmp.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.awt.SwingPanel
import javax.swing.JEditorPane
import javax.swing.JScrollPane
import javax.swing.text.html.HTMLEditorKit

@Composable
actual fun HtmlView(
    htmlContent: String,
    modifier: Modifier
) {
    // Use Swing's JEditorPane with HTML support for embedded HTML rendering
    // This is a simple, lightweight solution that works across all desktop platforms
    
    // Transform HTML content to use table-based layout for JVM compatibility
    // HTMLEditorKit doesn't support flexbox, so we need to convert flex divs to tables
    val transformedHtml = transformHtmlForJvm(htmlContent)
    
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
                
                // Add basic styling rules that HTMLEditorKit can understand
                styleSheet.addRule("body { font-family: Arial, sans-serif; margin: 20px; }")
                styleSheet.addRule(".receipt { background-color: white; padding: 20px; }")
                styleSheet.addRule(".header { text-align: center; border-bottom: 3px solid #B71C1C; padding-bottom: 15px; margin-bottom: 15px; }")
                styleSheet.addRule(".header h1 { color: #B71C1C; font-size: 24px; }")
                styleSheet.addRule(".business-info { text-align: center; color: #1976D2; margin-bottom: 15px; }")
                styleSheet.addRule(".section { margin: 15px 0; }")
                styleSheet.addRule(".section-title { font-weight: bold; color: #212121; margin-bottom: 8px; }")
                styleSheet.addRule(".layout-table { width: 100%; border-collapse: collapse; }")
                styleSheet.addRule(".layout-table td { padding: 6px 0; border-bottom: 1px solid #e0e0e0; font-size: 12px; }")
                styleSheet.addRule(".layout-table .label-cell { color: #616161; text-align: left; width: 50%; }")
                styleSheet.addRule(".layout-table .value-cell { color: #212121; font-weight: 500; text-align: right; width: 50%; }")
                styleSheet.addRule(".items-table { width: 100%; border-collapse: collapse; margin: 15px 0; }")
                styleSheet.addRule(".items-table th { background-color: #1976D2; color: white; padding: 8px; font-size: 11px; }")
                styleSheet.addRule(".items-table td { padding: 8px; border-bottom: 1px solid #e0e0e0; font-size: 12px; }")
                styleSheet.addRule(".totals-table { width: 100%; border-collapse: collapse; margin-top: 15px; }")
                styleSheet.addRule(".totals-table td { padding: 6px 0; font-size: 13px; }")
                styleSheet.addRule(".totals-table .label-cell { text-align: left; width: 50%; }")
                styleSheet.addRule(".totals-table .value-cell { text-align: right; width: 50%; }")
                styleSheet.addRule(".totals-table .final-row td { font-weight: bold; font-size: 16px; border-top: 2px solid #212121; padding-top: 10px; }")
                styleSheet.addRule(".paid-amount { color: #43A047; font-weight: bold; text-align: right; }")
                styleSheet.addRule(".totals-table .paid-amount { text-align: right; width: 50%; color: #43A047; font-weight: bold; }")
                styleSheet.addRule(".footer { margin-top: 25px; padding-top: 15px; border-top: 3px solid #B71C1C; text-align: center; font-size: 12px; color: #757575; }")
                styleSheet.addRule(".regards-message { color: #1976D2; font-weight: bold; }")
                styleSheet.addRule(".divider { border-top: 1px solid #e0e0e0; margin: 15px 0; }")
                
                val editorPane = JEditorPane().apply {
                    this.editorKit = editorKit
                    isEditable = false
                    contentType = "text/html"
                    text = transformedHtml
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
                    editorPane.text = transformHtmlForJvm(htmlContent)
                }
            }
        )
    }
}

/**
 * Transform HTML content to use table-based layout instead of flexbox for JVM compatibility.
 * HTMLEditorKit doesn't support flexbox, so we convert info-row and total-row divs to tables.
 */
private fun transformHtmlForJvm(html: String): String {
    var transformed = html
    
    // Transform info-row divs to table rows
    // Pattern: <div class='info-row'><span class='info-label'>Label:</span><span class='info-value'>Value</span></div>
    val infoRowPattern = Regex(
        """<div class='info-row'>\s*<span class='info-label'>([^<]*)</span>\s*<span class='info-value'>([^<]*)</span>\s*</div>""",
        RegexOption.DOT_MATCHES_ALL
    )
    transformed = infoRowPattern.replace(transformed) { match ->
        val label = match.groupValues[1]
        val value = match.groupValues[2]
        "<table class='layout-table'><tr><td class='label-cell'>$label</td><td class='value-cell'>$value</td></tr></table>"
    }
    
    // Transform total-row divs to table rows
    // Pattern: <div class='total-row'><span>Label:</span><span>Value</span></div>
    val totalRowPattern = Regex(
        """<div class='total-row'>(\s*<span>([^<]*)</span>\s*<span([^>]*)>([^<]*)</span>\s*)</div>""",
        RegexOption.DOT_MATCHES_ALL
    )
    transformed = totalRowPattern.replace(transformed) { match ->
        val label = match.groupValues[2]
        val valueAttrs = match.groupValues[3]
        val value = match.groupValues[4]
        // Use inline style for paid-amount to ensure proper alignment (HTMLEditorKit has limited CSS support)
        val valueStyle = if (valueAttrs.contains("paid-amount")) {
            "style='text-align: right; color: #43A047; font-weight: bold;'"
        } else {
            "class='value-cell'"
        }
        "<table class='totals-table'><tr><td class='label-cell'>$label</td><td $valueStyle>$value</td></tr></table>"
    }
    
    // Transform final total row
    val finalRowPattern = Regex(
        """<div class='total-row final'>\s*<span>([^<]*)</span>\s*<span>([^<]*)</span>\s*</div>""",
        RegexOption.DOT_MATCHES_ALL
    )
    transformed = finalRowPattern.replace(transformed) { match ->
        val label = match.groupValues[1]
        val value = match.groupValues[2]
        "<table class='totals-table'><tr class='final-row'><td class='label-cell'>$label</td><td class='value-cell'>$value</td></tr></table>"
    }
    
    return transformed
}
