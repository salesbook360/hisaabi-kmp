package com.hisaabi.hisaabi_kmp.reports.domain.util

/**
 * Platform-specific share functionality
 * Each platform implementation can define its own constructor parameters
 */
expect class ShareHelper {
    /**
     * Share a file via platform share dialog
     * @param filePath Path to the file to share
     * @param mimeType MIME type of the file (e.g., "application/pdf")
     * @param title Title for the share dialog
     */
    fun shareFile(filePath: String, mimeType: String, title: String)
}

