package com.hisaabi.hisaabi_kmp.core.ui

import androidx.compose.ui.graphics.Color
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionState

/**
 * Data class for status badge colors
 */
data class StatusBadgeColors(
    val backgroundColor: Color,
    val textColor: Color
)

/**
 * Get custom colors for transaction status badges
 * 
 * @param stateId The transaction state ID
 * @return StatusBadgeColors with background and text colors
 */
fun getStatusBadgeColors(stateId: Int): StatusBadgeColors {
    return when (stateId) {
        TransactionState.PENDING.value -> StatusBadgeColors(
            backgroundColor = Color(0xFFFFF8E1), // Light Amber
            textColor = Color(0xFFF57C00) // Amber
        )
        TransactionState.IN_PROGRESS.value -> StatusBadgeColors(
            backgroundColor = Color(0xFFE1F5FE), // Light Cyan Blue
            textColor = Color(0xFF0277BD) // Blue
        )
        TransactionState.COMPLETED.value -> StatusBadgeColors(
            backgroundColor = Color(0xFFE8F5E9), // Light Green
            textColor = Color(0xFF388E3C) // Green
        )
        TransactionState.CANCELLED.value -> StatusBadgeColors(
            backgroundColor = Color(0xFFFFEBEE), // Light Red
            textColor = Color(0xFFD32F2F) // Red
        )
        else -> StatusBadgeColors(
            backgroundColor = Color(0xFFF5F5F5), // Light Gray
            textColor = Color(0xFF616161) // Gray
        )
    }
}

