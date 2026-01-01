package com.hisaabi.hisaabi_kmp.reports.domain.model

/**
 * Detailed breakdown for balance sheet items
 */
data class BalanceSheetBreakdown(
    val itemType: BalanceSheetItemType,
    val breakdown: Map<String, Double> = emptyMap()
)

enum class BalanceSheetItemType {
    RECEIVABLE,
    AVAILABLE_STOCK,
    CASH_IN_HAND,
    CAPITAL_INVESTMENT,
    PAYABLES,
    CURRENT_PROFIT_LOSS,
    TOTAL_ASSETS,
    TOTAL_LIABILITIES
}

/**
 * Detailed breakdown for Current Profit/Loss calculation
 */
data class ProfitLossBreakdown(
    val saleAmount: Double,
    val costOfSoldProducts: Double,
    val profitOrLoss: Double, // Sale amount - cost of sold products
    val discountTaken: Double,
    val discountGiven: Double,
    val totalExpenses: Double,
    val totalIncome: Double,
    val additionalChargesReceived: Double,
    val additionalChargesPaid: Double,
    val taxPaid: Double,
    val taxReceived: Double,
    val totalProfitLoss: Double // Final calculated value
)

/**
 * Detailed breakdown for Receivables
 */
data class ReceivableBreakdown(
    val customerReceivables: Double,
    val vendorReceivables: Double,
    val investorReceivables: Double,
    val totalReceivables: Double
)

/**
 * Detailed breakdown for Payables
 */
data class PayableBreakdown(
    val customerPayables: Double,
    val vendorPayables: Double,
    val investorPayables: Double,
    val totalPayables: Double
)

/**
 * Detailed breakdown for Cash in Hand
 */
data class CashInHandBreakdown(
    val paymentMethodBreakdowns: Map<String, Double>, // Map of payment method name to amount
    val totalCashInHand: Double
)

/**
 * Detailed breakdown for Capital Investment
 */
data class CapitalInvestmentBreakdown(
    val openingStockWorth: Double,
    val openingPartyBalances: Double, // Sum of all opening party balances
    val openingPaymentAmounts: Double,
    val totalCapitalInvestment: Double
)

/**
 * Detailed breakdown for Available Stock
 */
data class AvailableStockBreakdown(
    val totalAvailableStock: Double // For now, just total. Can be extended with warehouse breakdown later
)

