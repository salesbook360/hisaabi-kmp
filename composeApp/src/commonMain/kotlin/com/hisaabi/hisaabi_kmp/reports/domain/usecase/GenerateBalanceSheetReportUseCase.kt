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
        val receivableBreakdown = calculateReceivableBreakdown(businessSlug)

        // 2. Available Stock (current quantity * avg_purchase_price)
        val availableStock = calculateAvailableStock(businessSlug)
        val availableStockBreakdown = com.hisaabi.hisaabi_kmp.reports.domain.model.AvailableStockBreakdown(
            totalAvailableStock = availableStock
        )

        // 3. Cash in Hand (total amount from payment methods)
        val cashInHand = paymentMethodDao.getTotalCashInHand(businessSlug) ?: 0.0
        val cashInHandBreakdown = calculateCashInHandBreakdown(businessSlug)

        val totalAssets = receivables + availableStock + cashInHand

        // ========== LIABILITIES CALCULATION ==========

        // 1. Capital Investment
        val capitalInvestment = calculateCapitalInvestment(businessSlug)
        val capitalInvestmentBreakdown = calculateCapitalInvestmentBreakdown(businessSlug)

        // 2. Payables (parties with positive balance)
        val payables = calculatePayables(businessSlug)
        val payablesBreakdown = calculatePayablesBreakdown(businessSlug)

        // 3. Current Profit/Loss
        val currentProfitLoss = calculateCurrentProfitLoss(businessSlug)
        val profitLossBreakdown = calculateProfitLossBreakdown(businessSlug)

        val totalLiabilities = capitalInvestment + payables + currentProfitLoss

        // ========== GENERATE REPORT ROWS ==========
        // 4 columns: Assets Label | Assets Value | Liabilities Label | Liabilities Value
        val columns = listOf("Assets", "", "Liabilities", "")
        val rows = mutableListOf<ReportRow>()
        
        // Assets items
        val assetsItems = listOf(
            "Receivable" to receivables,
            "Available Stock" to availableStock,
            "Cash in Hand" to cashInHand,
            "Total Assets" to totalAssets
        )
        
        // Liabilities items
        val liabilitiesItems = listOf(
            "Capital Investment" to capitalInvestment,
            "Payables" to payables,
            "Current Profit/Loss" to currentProfitLoss,
            "Total Liabilities" to totalLiabilities
        )
        
        // Combine into rows - pad the shorter list
        val maxRows = maxOf(assetsItems.size, liabilitiesItems.size)
        for (i in 0 until maxRows) {
            val assetItem = if (i < assetsItems.size) assetsItems[i] else null
            val liabilityItem = if (i < liabilitiesItems.size) liabilitiesItems[i] else null
            
            val assetLabel = assetItem?.first ?: ""
            val assetValue = assetItem?.let { 
                "$currencySymbol ${String.format("%,.0f", it.second)}" 
            } ?: ""
            
            val liabilityLabel = liabilityItem?.first ?: ""
            val liabilityValue = liabilityItem?.let { 
                "$currencySymbol ${String.format("%,.0f", it.second)}" 
            } ?: ""
            
            rows.add(ReportRow("row_$i", listOf(assetLabel, assetValue, liabilityLabel, liabilityValue)))
        }

        return ReportResult(
            reportType = ReportType.BALANCE_SHEET,
            filters = defaultFilters, // Use default filters (no filtering)
            columns = columns,
            rows = rows,
            summary = null, // No summary for balance sheet
            profitLossBreakdown = profitLossBreakdown,
            receivableBreakdown = receivableBreakdown,
            payablesBreakdown = payablesBreakdown,
            cashInHandBreakdown = cashInHandBreakdown,
            capitalInvestmentBreakdown = capitalInvestmentBreakdown,
            availableStockBreakdown = availableStockBreakdown
        )
    }

    private suspend fun calculateReceivables(businessSlug: String): Double {
        val breakdown = calculateReceivableBreakdown(businessSlug)
        return breakdown.totalReceivables
    }

    private suspend fun calculateReceivableBreakdown(businessSlug: String): com.hisaabi.hisaabi_kmp.reports.domain.model.ReceivableBreakdown {
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

        val totalReceivables = customerReceivables + vendorReceivables + investorReceivables

        return com.hisaabi.hisaabi_kmp.reports.domain.model.ReceivableBreakdown(
            customerReceivables = customerReceivables,
            vendorReceivables = vendorReceivables,
            investorReceivables = investorReceivables,
            totalReceivables = totalReceivables
        )
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
        val breakdown = calculateCapitalInvestmentBreakdown(businessSlug)
        return breakdown.totalCapitalInvestment
    }

    private suspend fun calculateCapitalInvestmentBreakdown(businessSlug: String): com.hisaabi.hisaabi_kmp.reports.domain.model.CapitalInvestmentBreakdown {
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

        val totalCapitalInvestment = openingStockWorth + openingPartyBalances + openingPaymentAmounts

        return com.hisaabi.hisaabi_kmp.reports.domain.model.CapitalInvestmentBreakdown(
            openingStockWorth = openingStockWorth,
            openingPartyBalances = openingPartyBalances,
            openingPaymentAmounts = openingPaymentAmounts,
            totalCapitalInvestment = totalCapitalInvestment
        )
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
        val breakdown = calculatePayablesBreakdown(businessSlug)
        return breakdown.totalPayables
    }

    private suspend fun calculatePayablesBreakdown(businessSlug: String): com.hisaabi.hisaabi_kmp.reports.domain.model.PayableBreakdown {
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

        val totalPayables = customerPayables + vendorPayables + investorPayables

        return com.hisaabi.hisaabi_kmp.reports.domain.model.PayableBreakdown(
            customerPayables = customerPayables,
            vendorPayables = vendorPayables,
            investorPayables = investorPayables,
            totalPayables = totalPayables
        )
    }

    private suspend fun calculateCurrentProfitLoss(businessSlug: String): Double {
        val breakdown = calculateProfitLossBreakdown(businessSlug)
        return breakdown.totalProfitLoss
    }

    private suspend fun calculateProfitLossBreakdown(businessSlug: String): com.hisaabi.hisaabi_kmp.reports.domain.model.ProfitLossBreakdown {
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

        // Profit or Loss (Sale amount - cost of sold products)
        val profitOrLoss = totalSaleAmount - costOfSoldProducts

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

        return com.hisaabi.hisaabi_kmp.reports.domain.model.ProfitLossBreakdown(
            saleAmount = totalSaleAmount,
            costOfSoldProducts = costOfSoldProducts,
            profitOrLoss = profitOrLoss,
            discountTaken = discountTaken,
            discountGiven = discountGiven,
            totalExpenses = totalExpenses,
            totalIncome = totalExtraIncome,
            additionalChargesReceived = additionalChargesReceived,
            additionalChargesPaid = additionalChargesPaid,
            taxPaid = taxPaid,
            taxReceived = taxReceived,
            totalProfitLoss = currentProfitLoss
        )
    }

    private suspend fun calculateCashInHandBreakdown(businessSlug: String): com.hisaabi.hisaabi_kmp.reports.domain.model.CashInHandBreakdown {
        // Get all payment methods for this business
        val paymentMethods = paymentMethodDao.getPaymentMethodsByBusiness(businessSlug).first()
        
        // Create a map of payment method name to amount (only active ones)
        val breakdownMap = paymentMethods
            .filter { it.status_id != 2 } // Exclude deleted
            .associate { it.title to it.amount }
        
        val totalCashInHand = breakdownMap.values.sum()

        return com.hisaabi.hisaabi_kmp.reports.domain.model.CashInHandBreakdown(
            paymentMethodBreakdowns = breakdownMap,
            totalCashInHand = totalCashInHand
        )
    }
}
