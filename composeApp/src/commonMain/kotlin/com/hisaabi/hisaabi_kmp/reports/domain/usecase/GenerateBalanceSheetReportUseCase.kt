package com.hisaabi.hisaabi_kmp.reports.domain.usecase

import com.hisaabi.hisaabi_kmp.business.data.datasource.BusinessPreferencesDataSource
import com.hisaabi.hisaabi_kmp.database.dao.InventoryTransactionDao
import com.hisaabi.hisaabi_kmp.database.dao.PartyDao
import com.hisaabi.hisaabi_kmp.database.dao.PaymentMethodDao
import com.hisaabi.hisaabi_kmp.database.dao.ProductDao
import com.hisaabi.hisaabi_kmp.database.dao.ProductQuantitiesDao
import com.hisaabi.hisaabi_kmp.database.dao.TransactionDetailDao
import com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType
import com.hisaabi.hisaabi_kmp.reports.domain.model.*
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import kotlinx.coroutines.flow.first

/**
 * Use case for generating balance sheet reports from actual database
 * Based on the Android Native implementation
 */
class GenerateBalanceSheetReportUseCase(
    private val partyDao: PartyDao,
    private val paymentMethodDao: PaymentMethodDao,
    private val productDao: ProductDao,
    private val productQuantitiesDao: ProductQuantitiesDao,
    private val inventoryTransactionDao: InventoryTransactionDao,
    private val transactionDetailDao: TransactionDetailDao,
    private val businessPreferences: BusinessPreferencesDataSource,
    private val preferencesManager: PreferencesManager
) {

    suspend fun execute(filters: ReportFilters): ReportResult {
        val currencySymbol = preferencesManager.getSelectedCurrency().symbol
        val businessSlug = businessPreferences.observeSelectedBusinessSlug().first()
            ?: throw IllegalStateException("No business selected")

        // Balance sheet always uses all records - filters are ignored
        // Create default filters for the report result (no date filtering)
        val defaultFilters = ReportFilters(reportType = ReportType.BALANCE_SHEET)

        // ========== ASSETS CALCULATION ==========

        // 1. Receivables (from parties with negative balance - customers, vendors, investors)
        val receivables = calculateReceivables(businessSlug)

        // 2. Available Stock (current quantity * avg_purchase_price)
        val availableStock = calculateAvailableStock(businessSlug)

        // 3. Cash in Hand (total amount from payment methods)
        val cashInHand = paymentMethodDao.getTotalCashInHand(businessSlug) ?: 0.0

        val totalAssets = receivables + availableStock + cashInHand

        // ========== LIABILITIES CALCULATION ==========

        // 1. Capital Investment
        val capitalInvestment = calculateCapitalInvestment(businessSlug)

        // 2. Payables (parties with positive balance)
        val payables = calculatePayables(businessSlug)

        // 3. Current Profit/Loss
        val currentProfitLoss = calculateCurrentProfitLoss(businessSlug)

        val totalLiabilities = capitalInvestment + payables + currentProfitLoss

        // ========== GENERATE REPORT ROWS ==========
        val columns = listOf("Assets", "Liabilities")
        val rows = mutableListOf<ReportRow>()

        // Build rows - showing Assets and Liabilities side by side
        val assetsRows = mutableListOf<String>()
        val liabilitiesRows = mutableListOf<String>()

        // Assets column
        assetsRows.add("Receivable")
        assetsRows.add("  $currencySymbol ${String.format("%,.0f", receivables)}")
        assetsRows.add("")
        assetsRows.add("Available Stock")
        assetsRows.add("  $currencySymbol ${String.format("%,.0f", availableStock)}")
        assetsRows.add("")
        assetsRows.add("Cash in Hand")
        assetsRows.add("  $currencySymbol ${String.format("%,.0f", cashInHand)}")
        assetsRows.add("")
        assetsRows.add("Total Assets")
        assetsRows.add("  $currencySymbol ${String.format("%,.0f", totalAssets)}")

        // Liabilities column
        liabilitiesRows.add("Capital Investment")
        liabilitiesRows.add("  $currencySymbol ${String.format("%,.0f", capitalInvestment)}")
        liabilitiesRows.add("")
        liabilitiesRows.add("Payables")
        liabilitiesRows.add("  $currencySymbol ${String.format("%,.0f", payables)}")
        liabilitiesRows.add("")
        liabilitiesRows.add("Current Profit/Loss")
        liabilitiesRows.add("  $currencySymbol ${String.format("%,.0f", currentProfitLoss)}")
        liabilitiesRows.add("")
        liabilitiesRows.add("Total Liabilities")
        liabilitiesRows.add("  $currencySymbol ${String.format("%,.0f", totalLiabilities)}")

        // Combine into rows - pad the shorter list
        val maxRows = maxOf(assetsRows.size, liabilitiesRows.size)
        for (i in 0 until maxRows) {
            val assetValue = if (i < assetsRows.size) assetsRows[i] else ""
            val liabilityValue = if (i < liabilitiesRows.size) liabilitiesRows[i] else ""
            rows.add(ReportRow("row_$i", listOf(assetValue, liabilityValue)))
        }

        return ReportResult(
            reportType = ReportType.BALANCE_SHEET,
            filters = defaultFilters, // Use default filters (no filtering)
            columns = columns,
            rows = rows,
            summary = null // No summary for balance sheet
        )
    }

    private suspend fun calculateReceivables(businessSlug: String): Double {
        // Receivables from customers (negative balance)
        val customerBalance = partyDao.getTotalBalance(
            listOf(PartyType.CUSTOMER.type, PartyType.WALK_IN_CUSTOMER.type),
            businessSlug
        ) ?: 0.0
        val customerReceivables = if (customerBalance < 0) -customerBalance else 0.0

        // Receivables from vendors (negative balance)
        val vendorBalance = partyDao.getTotalBalance(
            listOf(PartyType.VENDOR.type, PartyType.DEFAULT_VENDOR.type),
            businessSlug
        ) ?: 0.0
        val vendorReceivables = if (vendorBalance < 0) -vendorBalance else 0.0

        // Receivables from investors (negative balance)
        val investorBalance = partyDao.getTotalBalance(
            listOf(PartyType.INVESTOR.type),
            businessSlug
        ) ?: 0.0
        val investorReceivables = if (investorBalance < 0) -investorBalance else 0.0

        return customerReceivables + vendorReceivables + investorReceivables
    }

    private suspend fun calculateAvailableStock(businessSlug: String): Double {
        // Get all product quantities for this business
        val quantities = productQuantitiesDao.getQuantitiesByBusiness(businessSlug)

        // Get all products for this business
        val products = productDao.getProductsByBusiness(businessSlug).first()

        // Create a map of product slug to avg_purchase_price for quick lookup
        val productPriceMap = products.associateBy(
            { it.slug ?: "" },
            { it.avg_purchase_price }
        )

        // Calculate total available stock value: sum(current_quantity * avg_purchase_price)
        var totalValue = 0.0
        quantities.forEach { quantity ->
            val productSlug = quantity.product_slug ?: return@forEach
            val avgPurchasePrice = productPriceMap[productSlug] ?: 0.0
            val currentQuantity = quantity.current_quantity
            totalValue += currentQuantity * avgPurchasePrice
        }

        return totalValue
    }

    private suspend fun calculateCapitalInvestment(businessSlug: String): Double {
        // 1. Opening Stock Worth (opening_quantity * opening_quantity_purchase_price)
        val openingStockWorth = calculateOpeningStockWorth(businessSlug)

        // 2. Opening Party Balances (sum of opening_balance from all parties)
        val openingCustomerBalance = partyDao.getTotalOpeningBalance(
            listOf(PartyType.CUSTOMER.type, PartyType.WALK_IN_CUSTOMER.type),
            businessSlug
        ) ?: 0.0
        val openingVendorBalance = partyDao.getTotalOpeningBalance(
            listOf(PartyType.VENDOR.type, PartyType.DEFAULT_VENDOR.type),
            businessSlug
        ) ?: 0.0
        val openingInvestorBalance = partyDao.getTotalOpeningBalance(
            listOf(PartyType.INVESTOR.type),
            businessSlug
        ) ?: 0.0
        val openingPartyBalances =
            openingCustomerBalance + openingVendorBalance + openingInvestorBalance

        // 3. Opening Payment Method amounts (sum of opening_amount)
        val openingPaymentAmounts = paymentMethodDao.getTotalOpeningAmount(businessSlug) ?: 0.0

        return openingStockWorth + openingPartyBalances + openingPaymentAmounts
    }

    private suspend fun calculateOpeningStockWorth(businessSlug: String): Double {
        // Get all product quantities for this business
        val quantities = productQuantitiesDao.getQuantitiesByBusiness(businessSlug)

        // Get all products for this business
        val products = productDao.getProductsByBusiness(businessSlug).first()

        // Create a map of product slug to opening_quantity_purchase_price for quick lookup
        val productPriceMap = products.associateBy(
            { it.slug ?: "" },
            { it.opening_quantity_purchase_price }
        )

        // Calculate total opening stock value: sum(opening_quantity * opening_quantity_purchase_price)
        var totalValue = 0.0
        quantities.forEach { quantity ->
            val productSlug = quantity.product_slug ?: return@forEach
            val openingPurchasePrice = productPriceMap[productSlug] ?: 0.0
            val openingQuantity = quantity.opening_quantity
            totalValue += openingQuantity * openingPurchasePrice
        }

        return totalValue
    }

    private suspend fun calculatePayables(businessSlug: String): Double {
        // Payables to customers (positive balance)
        val customerBalance = partyDao.getTotalBalance(
            listOf(PartyType.CUSTOMER.type, PartyType.WALK_IN_CUSTOMER.type),
            businessSlug
        ) ?: 0.0
        val customerPayables = if (customerBalance > 0) customerBalance else 0.0

        // Payables to vendors (positive balance)
        val vendorBalance = partyDao.getTotalBalance(
            listOf(PartyType.VENDOR.type, PartyType.DEFAULT_VENDOR.type),
            businessSlug
        ) ?: 0.0
        val vendorPayables = if (vendorBalance > 0) vendorBalance else 0.0

        // Payables to investors (positive balance)
        val investorBalance = partyDao.getTotalBalance(
            listOf(PartyType.INVESTOR.type),
            businessSlug
        ) ?: 0.0
        val investorPayables = if (investorBalance > 0) investorBalance else 0.0

        return customerPayables + vendorPayables + investorPayables
    }

    private suspend fun calculateCurrentProfitLoss(businessSlug: String): Double {
        // Get all transactions for this business
        val transactions = inventoryTransactionDao.getAllTransactionsByBusiness(businessSlug)

        // Get all transaction details
        val transactionSlugs = transactions.mapNotNull { it.slug }
        val allDetails = transactionDetailDao.getDetailsByTransactionSlugs(transactionSlugs)

        // Group details by transaction slug
        val detailsByTransaction = allDetails.groupBy { it.transaction_slug ?: "" }

        var totalSaleAmount = 0.0
        var costOfSoldProducts = 0.0
        var discountTaken = 0.0  // Discount during purchase
        var discountGiven = 0.0  // Discount during sale
        var totalExpenses = 0.0
        var totalExtraIncome = 0.0
        var additionalChargesReceived = 0.0
        var additionalChargesPaid = 0.0
        var taxPaid = 0.0
        var taxReceived = 0.0

        transactions.forEach { transaction ->
            val details = detailsByTransaction[transaction.slug ?: ""] ?: emptyList()

            when (transaction.transaction_type) {
                AllTransactionTypes.SALE.value -> {
                    // Sale amount = total_bill (subtotal) + additional_charges + tax - discount
                    val saleAmount =
                        transaction.total_bill + transaction.additional_charges + transaction.flat_tax - transaction.flat_discount
                    totalSaleAmount += saleAmount

                    // Cost of sold products (price * quantity - profit for sale transactions)
                    val cost = details.sumOf { (it.price * it.quantity) - it.profit }
                    costOfSoldProducts += cost

                    // Discount given during sale
                    discountGiven += transaction.flat_discount + details.sumOf { it.flat_discount }

                    // Additional charges received
                    additionalChargesReceived += transaction.additional_charges

                    // Tax received
                    taxReceived += transaction.flat_tax + details.sumOf { it.flat_tax }
                }

                AllTransactionTypes.PURCHASE.value -> {
                    // Discount taken during purchase
                    discountTaken += transaction.flat_discount + details.sumOf { it.flat_discount }

                    // Additional charges paid
                    additionalChargesPaid += transaction.additional_charges

                    // Tax paid
                    taxPaid += transaction.flat_tax + details.sumOf { it.flat_tax }
                }

                AllTransactionTypes.EXPENSE.value -> {
                    // Expense transactions sum
                    totalExpenses += transaction.total_bill + transaction.additional_charges
                }

                AllTransactionTypes.EXTRA_INCOME.value -> {
                    // Extra income transactions sum
                    totalExtraIncome += transaction.total_bill + transaction.additional_charges
                }
            }
        }

        // Current Profit/Loss = Total Sale - Cost of Sold Products + Discount Taken - Discount Given
        //                      - Expenses + Extra Income - Additional Charges Received + Additional Charges Paid
        //                      - Tax Paid + Tax Received
        val currentProfitLoss = totalSaleAmount
        -costOfSoldProducts
        +discountTaken
        -discountGiven
        -totalExpenses
        +totalExtraIncome
        +additionalChargesReceived
        -additionalChargesPaid
        -taxPaid
        +taxReceived

        return currentProfitLoss
    }
}
