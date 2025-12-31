package com.hisaabi.hisaabi_kmp.receipt

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import com.hisaabi.hisaabi_kmp.receipt.PdfPrintHelper
import com.hisaabi.hisaabi_kmp.settings.domain.model.ReceiptConfig
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.utils.formatTransactionDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Helper class for generating receipts from HTML templates
 */
object ReceiptHtmlHelper {
    
    /**
     * Read HTML template from assets
     */
    fun readHtmlFromAssets(context: Context, fileName: String = "pdfInvoice.html"): String {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            // If file not found in composeApp assets, try to read from HisaabiAndroidNative
            // For now, return empty string - we'll need to copy the file or access it differently
            ""
        }
    }
    
    /**
     * Convert Transaction to JSON format expected by HTML template
     */
    fun transactionToInvoiceJson(
        transaction: Transaction,
        config: ReceiptConfig,
        currencySymbol: String
    ): String {
        val isPaymentTransaction = AllTransactionTypes.isPayGetCash(transaction.transactionType)
        
        val businessInfo = buildBusinessInfoJson(config)
        val beneficiaryInfo = buildBeneficiaryInfoJson(transaction, config)
        val invoiceDetail = if (isPaymentTransaction) {
            buildPaymentInvoiceDetailJson(transaction, config, currencySymbol)
        } else {
            buildRegularInvoiceDetailJson(transaction, config, currencySymbol)
        }
        
        return """{"businessInfo":$businessInfo,"beneficiaryInfo":$beneficiaryInfo,"invoiceDetail":$invoiceDetail}"""
    }
    
    private fun buildBusinessInfoJson(config: ReceiptConfig): String {
        val parts = mutableListOf<String>()
        
        if (config.showBusinessName && config.businessName.isNotEmpty()) {
            parts.add(""""name":{"key":"Business name","value":${escapeJson(config.businessName)}}""")
        }
        if (config.showBusinessEmail && config.businessEmail.isNotEmpty()) {
            parts.add(""""email":{"key":"email","value":${escapeJson(config.businessEmail)}}""")
        }
        if (config.showBusinessPhone && config.businessPhone.isNotEmpty()) {
            parts.add(""""phone":{"key":"Phone","value":${escapeJson(config.businessPhone)}}""")
        }
        if (config.showBusinessAddress && config.businessAddress.isNotEmpty()) {
            parts.add(""""address":{"key":"Address","value":${escapeJson(config.businessAddress)}}""")
        }
        if (config.logoUrl != null) {
            parts.add(""""logoUrl":{"key":"Logo","value":${escapeJson(config.logoUrl)}}""")
        }
        
        return "{${parts.joinToString(",")}}"
    }
    
    private fun buildBeneficiaryInfoJson(transaction: Transaction, config: ReceiptConfig): String {
        val party = transaction.party ?: return "{}"
        val parts = mutableListOf<String>()
        
        if (config.showCustomerName) {
            parts.add(""""name":{"key":"Customer","value":${escapeJson(party.name)}}""")
        }
        if (party.phone != null && config.showCustomerPhone) {
            parts.add(""""phone":{"key":"Phone","value":${escapeJson(party.phone)}}""")
        }
        if (party.address != null && config.showCustomerAddress) {
            parts.add(""""address":{"key":"Address","value":${escapeJson(party.address)}}""")
        }
        
        return "{${parts.joinToString(",")}}"
    }
    
    private fun buildPaymentInvoiceDetailJson(
        transaction: Transaction,
        config: ReceiptConfig,
        currencySymbol: String
    ): String {
        val parts = mutableListOf<String>()
        
        if (config.showOrderNo && transaction.slug != null) {
            parts.add(""""id":{"key":"Sr. no","value":${escapeJson("#${transaction.slug}")}}""")
        }
        
        if (config.showTransactionDate && transaction.timestamp != null) {
            parts.add(""""date":{"key":"Date","value":${escapeJson(formatTransactionDate(transaction.timestamp!!))}}""")
        }
        
        if (config.showTransactionType) {
            parts.add(""""transactionType":{"key":"Tr.type","value":${escapeJson(transaction.getTransactionTypeName())}}""")
        }
        
        if (config.showPaymentMethod && (transaction.paymentMethodTo != null || transaction.paymentMethodFrom != null)) {
            val paymentMethod = transaction.paymentMethodTo?.title ?: transaction.paymentMethodFrom?.title ?: "N/A"
            parts.add(""""paymentMethod":{"key":"Payment method","value":${escapeJson(paymentMethod)}}""")
        }
        
        parts.add(""""paidNow":{"key":"Paid now","value":${escapeJson("$currencySymbol${"%.2f".format(transaction.totalPaid)}")}}""")
        
        if (transaction.description?.isNotEmpty() == true) {
            parts.add(""""remarks":{"key":"Remarks","value":${escapeJson(transaction.description)}}""")
        }
        
        if (config.showInvoiceTerms && config.invoiceTerms.isNotEmpty()) {
            parts.add(""""terms":{"key":"Terms","value":${escapeJson(config.invoiceTerms)}}""")
        }
        
        if (config.showRegardsMessage && config.regardsMessage != null) {
            parts.add(""""regardsMessage":{"key":"Regards","value":${escapeJson(config.regardsMessage)}}""")
        }
        
        parts.add(""""transactionDescription":[]""")
        
        return "{${parts.joinToString(",")}}"
    }
    
    private fun buildRegularInvoiceDetailJson(
        transaction: Transaction,
        config: ReceiptConfig,
        currencySymbol: String
    ): String {
        val parts = mutableListOf<String>()
        
        if (config.showOrderNo && transaction.slug != null) {
            parts.add(""""id":{"key":"Sr. no","value":${escapeJson("#${transaction.slug}")}}""")
        }
        
        if (config.showTransactionDate && transaction.timestamp != null) {
            parts.add(""""date":{"key":"Date","value":${escapeJson(formatTransactionDate(transaction.timestamp!!))}}""")
        }
        
        if (config.showTransactionType) {
            parts.add(""""transactionType":{"key":"Tr.type","value":${escapeJson(transaction.getTransactionTypeName())}}""")
        }
        
        // Transaction description (products)
        val transactionDescription = transaction.transactionDetails.joinToString(",") { detail ->
            """{"productTitle":${escapeJson(detail.product?.title ?: "Unknown")},"description":${escapeJson(detail.product?.description ?: "")},"quantity":${escapeJson("%.1f".format(detail.quantity))},"quantityUnit":"Piece","unitPrice":${escapeJson("%.2f".format(detail.price))},"discount":${escapeJson("%.2f".format(detail.flatDiscount))},"tax":${escapeJson("%.2f".format(detail.flatTax))},"amount":${escapeJson("%.2f".format(detail.calculateSubtotal()))}}"""
        }
        parts.add(""""transactionDescription":[$transactionDescription]""")
        
        // Totals
        val subtotal = transaction.calculateSubtotal()
        parts.add(""""subTotal":{"key":"Sub total","value":${escapeJson("$currencySymbol${"%.2f".format(subtotal)}")}}""")
        
        if (config.showDiscount && transaction.flatDiscount > 0) {
            parts.add(""""discount":{"key":"Discount","value":${escapeJson("$currencySymbol${"%.2f".format(transaction.flatDiscount)}")}}""")
        }
        
        if (config.showTax && transaction.flatTax > 0) {
            parts.add(""""tax":{"key":"Tax","value":${escapeJson("$currencySymbol${"%.2f".format(transaction.flatTax)}")}}""")
        }
        
        if (config.showAdditionalCharges && transaction.additionalCharges > 0) {
            parts.add(""""additionalCharges":{"key":"Extra charges","value":${escapeJson("$currencySymbol${"%.2f".format(transaction.additionalCharges)}")}}""")
        }
        
        val grandTotal = transaction.calculateGrandTotal()
        parts.add(""""totalPayable":{"key":"Total payable","value":${escapeJson("$currencySymbol${"%.2f".format(grandTotal)}")}}""")
        
        if (config.showTotalItems) {
            val totalQty = transaction.calculateTotalQuantity()
            parts.add(""""totalItems":{"key":"Total Items","value":${escapeJson("${transaction.transactionDetails.size} items (${"%.1f".format(totalQty)} qty)")}}""")
        }
        
        if (config.showPreviousBalance && transaction.party != null && transaction.party.balance != 0.0) {
            parts.add(""""previousDue":{"key":"Previous Balance","value":${escapeJson("$currencySymbol${"%.2f".format(kotlin.math.abs(transaction.party.balance))}")}}""")
        }
        
        parts.add(""""paidNow":{"key":"Paid now","value":${escapeJson("$currencySymbol${"%.2f".format(transaction.totalPaid)}")}}""")
        
        val balance = transaction.calculatePayable()
        if (balance != 0.0 || config.showPayableAmount) {
            parts.add(""""balance":{"key":"Balance:","value":${escapeJson("$currencySymbol${"%.2f".format(balance)}")}}""")
        }
        
        if (config.showPaymentMethod && (transaction.paymentMethodTo != null || transaction.paymentMethodFrom != null)) {
            val paymentMethod = transaction.paymentMethodTo?.title ?: transaction.paymentMethodFrom?.title ?: "N/A"
            parts.add(""""paymentMethod":{"key":"Payment method","value":${escapeJson(paymentMethod)}}""")
        }
        
        if (transaction.description?.isNotEmpty() == true) {
            parts.add(""""remarks":{"key":"Remarks","value":${escapeJson(transaction.description)}}""")
        }
        
        if (config.showInvoiceTerms && config.invoiceTerms.isNotEmpty()) {
            parts.add(""""terms":{"key":"Terms","value":${escapeJson(config.invoiceTerms)}}""")
        }
        
        if (config.showRegardsMessage && config.regardsMessage != null) {
            parts.add(""""regardsMessage":{"key":"Regards","value":${escapeJson(config.regardsMessage)}}""")
        }
        
        return "{${parts.joinToString(",")}}"
    }
    
    private fun escapeJson(value: String): String {
        return "\"${value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")}\""
    }
    
    /**
     * Generate PDF from HTML using WebView
     */
    suspend fun generatePdfFromHtml(
        context: Context,
        htmlContent: String,
        invoiceJson: String,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            val webView = WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.loadsImagesAutomatically = true
            }
            
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // Inject JSON data into HTML
                    webView.evaluateJavascript("setInvoiceDetail('${escapeJsonForJs(invoiceJson)}')", null)
                    
                    // Wait a bit for JavaScript to execute, then generate PDF
                    webView.postDelayed({
                        generatePdfFromWebView(webView, outputFile, continuation)
                    }, 500)
                }
            }
            
            // Load HTML content
            webView.loadData(htmlContent.replace("#", "%23"), "text/HTML", "UTF-8")
            
            continuation.invokeOnCancellation {
                webView.destroy()
            }
        }
    }
    
    private fun escapeJsonForJs(json: String): String {
        return json
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
    
    private fun generatePdfFromWebView(
        webView: WebView,
        outputFile: File,
        continuation: kotlin.coroutines.Continuation<Result<File>>
    ) {
        PdfPrintHelper.generatePdfFromWebView(
            webView,
            outputFile,
            object : PdfPrintHelper.PdfGenerationCallback {
                override fun onSuccess(file: File) {
                    continuation.resume(Result.success(file))
                }
                
                override fun onError(exception: Exception) {
                    continuation.resumeWithException(exception)
                }
            }
        )
    }
}

