package com.hisaabi.hisaabi_kmp.reports.domain.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.hisaabi.hisaabi_kmp.reports.domain.model.ReportResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.File
import java.io.FileOutputStream

actual class ReportPdfGenerator(private val context: Context) {
    
    // Professional color scheme
    private val primaryColor = Color.rgb(33, 150, 243) // Material Blue
    private val primaryDark = Color.rgb(25, 118, 210)
    private val accentColor = Color.rgb(255, 152, 0) // Orange accent
    private val textPrimary = Color.rgb(33, 33, 33)
    private val textSecondary = Color.rgb(117, 117, 117)
    private val bgLight = Color.rgb(250, 250, 250)
    private val successGreen = Color.rgb(76, 175, 80)
    
    actual suspend fun generatePdf(reportResult: ReportResult, currencySymbol: String): String? = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595f
            val pageHeight = 842f
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            // Paint definitions
            val headerPaint = Paint().apply {
                color = Color.WHITE
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            
            val subHeaderPaint = Paint().apply {
                color = Color.WHITE
                textSize = 11f
                isAntiAlias = true
            }
            
            val sectionTitlePaint = Paint().apply {
                color = primaryColor
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            
            val bodyPaint = Paint().apply {
                color = textPrimary
                textSize = 10f
                isAntiAlias = true
            }
            
            val bodyBoldPaint = Paint().apply {
                color = textPrimary
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            
            val smallPaint = Paint().apply {
                color = textSecondary
                textSize = 9f
                isAntiAlias = true
            }
            
            val tableBorderPaint = Paint().apply {
                color = Color.rgb(224, 224, 224)
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            
            val fillPaint = Paint()
            
            val xMargin = 40f
            val rightMargin = pageWidth - 40f
            var yPosition = 0f
            
            // ===== HEADER SECTION WITH GRADIENT EFFECT =====
            fillPaint.color = primaryColor
            canvas.drawRect(0f, 0f, pageWidth, 100f, fillPaint)
            
            // Add subtle darker bar at top
            fillPaint.color = primaryDark
            canvas.drawRect(0f, 0f, pageWidth, 8f, fillPaint)
            
            yPosition = 40f
            
            // Company name (you can make this dynamic later)
            canvas.drawText("HISAABI", xMargin, yPosition, headerPaint)
            
            yPosition += 30f
            canvas.drawText(reportResult.reportType.title.uppercase(), xMargin, yPosition, subHeaderPaint)
            
            // Generated date on right
            val generatedDate = Instant.fromEpochMilliseconds(reportResult.generatedAt)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            val dateText = "Generated: ${generatedDate.date}"
            val dateWidth = subHeaderPaint.measureText(dateText)
            canvas.drawText(dateText, rightMargin - dateWidth, yPosition, subHeaderPaint)
            
            yPosition = 120f
            
            // ===== SUMMARY SECTION (HIGHLIGHTED BOX) =====
            reportResult.summary?.let { summary ->
                val summaryBoxTop = yPosition
                val summaryBoxHeight = 80f
                
                // Draw summary box with light background
                fillPaint.color = bgLight
                val summaryRect = RectF(xMargin, summaryBoxTop, rightMargin, summaryBoxTop + summaryBoxHeight)
                canvas.drawRoundRect(summaryRect, 8f, 8f, fillPaint)
                
                // Border
                fillPaint.color = primaryColor
                fillPaint.style = Paint.Style.STROKE
                fillPaint.strokeWidth = 2f
                canvas.drawRoundRect(summaryRect, 8f, 8f, fillPaint)
                fillPaint.style = Paint.Style.FILL
                
                yPosition += 20f
                
                // Summary title
                canvas.drawText("SUMMARY", xMargin + 15f, yPosition, sectionTitlePaint)
                
                yPosition += 20f
                
                // Summary items in grid layout
                val col1X = xMargin + 15f
                val col2X = pageWidth / 2f + 10f
                
                var currentX = col1X
                var itemCount = 0
                
                summary.totalAmount?.let { amount ->
                    canvas.drawText("Total Amount:", currentX, yPosition, bodyPaint)
                    val amountText = "$currencySymbol ${String.format("%,.0f", amount)}"
                    canvas.drawText(amountText, currentX, yPosition + 12f, bodyBoldPaint.apply { color = successGreen })
                    bodyBoldPaint.color = textPrimary
                    currentX = col2X
                    itemCount++
                }
                
                summary.totalProfit?.let { profit ->
                    canvas.drawText("Total Profit:", currentX, yPosition, bodyPaint)
                    val profitText = "$currencySymbol ${String.format("%,.0f", profit)}"
                    canvas.drawText(profitText, currentX, yPosition + 12f, bodyBoldPaint.apply { color = accentColor })
                    bodyBoldPaint.color = textPrimary
                    if (itemCount % 2 == 1) {
                        yPosition += 25f
                        currentX = col1X
                    } else {
                        currentX = col2X
                    }
                    itemCount++
                }
                
                if (itemCount % 2 == 1) yPosition += 25f else yPosition += 12f
                currentX = col1X
                
                summary.totalQuantity?.let { qty ->
                    canvas.drawText("Total Quantity: ${String.format("%,.0f", qty)}", currentX, yPosition, bodyPaint)
                    currentX = col2X
                    itemCount++
                }
                
                if (currentX == col2X || itemCount == 0) {
                    canvas.drawText("Record Count: ${summary.recordCount}", currentX, yPosition, bodyPaint)
                }
                
                yPosition = summaryBoxTop + summaryBoxHeight + 20f
            }
            
            // ===== FILTERS SECTION =====
            yPosition += 10f
            canvas.drawText("APPLIED FILTERS", xMargin, yPosition, sectionTitlePaint)
            yPosition += 18f
            
            // Filters in a subtle box
            val filtersBoxTop = yPosition
            val filterItems = mutableListOf<String>()
            filterItems.add("Date Range: ${reportResult.filters.dateFilter.title}")
            reportResult.filters.additionalFilter?.let { filterItems.add("Filter: ${it.title}") }
            reportResult.filters.groupBy?.let { filterItems.add("Group By: ${it.title}") }
            filterItems.add("Sort By: ${reportResult.filters.sortBy.title}")
            
            val filtersBoxHeight = (filterItems.size * 14f) + 20f
            fillPaint.color = Color.rgb(245, 245, 245)
            canvas.drawRect(xMargin, filtersBoxTop, rightMargin, filtersBoxTop + filtersBoxHeight, fillPaint)
            
            yPosition += 12f
            filterItems.forEach { filter ->
                canvas.drawText("• $filter", xMargin + 10f, yPosition, smallPaint)
                yPosition += 14f
            }
            
            yPosition = filtersBoxTop + filtersBoxHeight + 25f
            
            // ===== DATA TABLE =====
            canvas.drawText("REPORT DATA", xMargin, yPosition, sectionTitlePaint)
            yPosition += 20f
            
            // Table header with background
            val tableTop = yPosition
            val rowHeight = 18f
            
            fillPaint.color = primaryColor
            canvas.drawRect(xMargin, yPosition, rightMargin, yPosition + rowHeight, fillPaint)
            
            // Column headers
            val columnCount = reportResult.columns.size
            val columnWidth = (rightMargin - xMargin) / columnCount
            
            headerPaint.textSize = 10f
            reportResult.columns.forEachIndexed { index, column ->
                val text = column.take(15) // Truncate long headers
                val x = xMargin + (columnWidth * index) + 5f
                canvas.drawText(text, x, yPosition + 12f, headerPaint)
            }
            
            yPosition += rowHeight
            
            // Table rows with alternating colors
            val maxRows = 25
            reportResult.rows.take(maxRows).forEachIndexed { rowIndex, row ->
                if (yPosition > 760f) {
                    canvas.drawText("... more data not shown", xMargin + 5f, yPosition + 12f, smallPaint)
                    return@forEachIndexed
                }
                
                // Alternating row background
                if (rowIndex % 2 == 0) {
                    fillPaint.color = Color.rgb(252, 252, 252)
                    canvas.drawRect(xMargin, yPosition, rightMargin, yPosition + rowHeight, fillPaint)
                }
                
                // Draw cell borders
                canvas.drawRect(xMargin, yPosition, rightMargin, yPosition + rowHeight, tableBorderPaint)
                
                // Row data
                row.values.forEachIndexed { colIndex, value ->
                    val text = value.take(18) // Truncate long values
                    val x = xMargin + (columnWidth * colIndex) + 5f
                    canvas.drawText(text, x, yPosition + 12f, bodyPaint)
                }
                
                yPosition += rowHeight
            }
            
            // Draw table border
            fillPaint.color = primaryColor
            fillPaint.style = Paint.Style.STROKE
            fillPaint.strokeWidth = 2f
            canvas.drawRect(xMargin, tableTop + rowHeight, rightMargin, yPosition, fillPaint)
            fillPaint.style = Paint.Style.FILL
            
            // Record count footer
            if (reportResult.rows.size > maxRows) {
                yPosition += 15f
                val footerText = "Showing ${maxRows} of ${reportResult.rows.size} records"
                canvas.drawText(footerText, xMargin, yPosition, bodyBoldPaint)
            }
            
            // ===== FOOTER =====
            fillPaint.color = bgLight
            canvas.drawRect(0f, pageHeight - 30f, pageWidth, pageHeight, fillPaint)
            
            val footerText = "Generated by Hisaabi POS • Page 1"
            val footerWidth = smallPaint.measureText(footerText)
            canvas.drawText(footerText, (pageWidth - footerWidth) / 2f, pageHeight - 12f, smallPaint)
            
            pdfDocument.finishPage(page)
            
            // Save to file
            val fileName = "Hisaabi_${reportResult.reportType.title.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)
            
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            
            pdfDocument.close()
            
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

