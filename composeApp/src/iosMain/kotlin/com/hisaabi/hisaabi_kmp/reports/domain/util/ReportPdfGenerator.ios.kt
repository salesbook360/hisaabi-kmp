package com.hisaabi.hisaabi_kmp.reports.domain.util

import com.hisaabi.hisaabi_kmp.reports.domain.model.ReportResult

actual class ReportPdfGenerator {
    actual suspend fun generatePdf(reportResult: ReportResult): String? {
        // TODO: Implement iOS PDF generation
        return null
    }
}

