package com.hisaabi.hisaabi_kmp.reports.domain.util

import com.hisaabi.hisaabi_kmp.reports.domain.model.ReportResult

/**
 * Platform-specific PDF generator for reports
 * Each platform implementation can define its own constructor parameters
 */
expect class ReportPdfGenerator {
    /**
     * Generate PDF from report result and return the file path
     * @return Path to the generated PDF file
     */
    suspend fun generatePdf(reportResult: ReportResult): String?
}

