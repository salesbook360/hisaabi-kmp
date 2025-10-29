package com.hisaabi.hisaabi_kmp.receipt

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaabi.hisaabi_kmp.settings.domain.model.ReceiptConfig
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.utils.formatTransactionDate
import kotlin.math.abs

/**
 * Composable receipt that can be rendered as an image/PDF
 */
@Composable
fun ReceiptContent(
    transaction: Transaction,
    config: ReceiptConfig,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Top border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color(0xFFB71C1C))
        )
        
        Spacer(Modifier.height(20.dp))
        
        // Header - RECEIPT
        Text(
            text = "RECEIPT",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(20.dp))
        
        // Business Information
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (config.showBusinessName && config.businessName.isNotEmpty()) {
                Text(
                    text = config.businessName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
            }
            
            if (config.showBusinessAddress && config.businessAddress.isNotEmpty()) {
                Text(
                    text = config.businessAddress,
                    fontSize = 11.sp,
                    color = Color(0xFF757575),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(2.dp))
            }
            
            if (config.showBusinessPhone && config.businessPhone.isNotEmpty()) {
                Text(
                    text = config.businessPhone,
                    fontSize = 11.sp,
                    color = Color(0xFF757575),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(2.dp))
            }
            
            if (config.showBusinessEmail && config.businessEmail.isNotEmpty()) {
                Text(
                    text = config.businessEmail,
                    fontSize = 11.sp,
                    color = Color(0xFF757575),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFE0E0E0))
        )
        
        Spacer(Modifier.height(16.dp))
        
        // Transaction Info Row (Date, Receipt No, Type, Payment Method)
        val showDate = config.showTransactionDate && transaction.timestamp != null
        val showReceipt = config.showOrderNo && transaction.slug != null
        val showType = config.showTransactionType
        val showPayment = config.showPaymentMethod && (transaction.paymentMethodTo != null || transaction.paymentMethodFrom != null)
        
        if (showDate || showReceipt || showType || showPayment) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(12.dp)
            ) {
                // First Row: Date and Receipt No
                if (showDate || showReceipt) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (showDate) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "PAYMENT DATE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF757575),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = formatTransactionDate(transaction.timestamp!!),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF212121)
                                )
                            }
                        }
                        
                        if (showReceipt) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = if (showDate) Alignment.End else Alignment.Start
                            ) {
                                Text(
                                    text = "RECEIPT NO.",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF757575),
                                    letterSpacing = 0.5.sp,
                                    textAlign = if (showDate) TextAlign.End else TextAlign.Start
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = transaction.slug!!,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF212121),
                                    textAlign = if (showDate) TextAlign.End else TextAlign.Start
                                )
                            }
                        }
                    }
                }
                
                // Second Row: Transaction Type and Payment Method
                if (showType || showPayment) {
                    if (showDate || showReceipt) {
                        Spacer(Modifier.height(12.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (showType) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "TRANSACTION TYPE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF757575),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = transaction.getTransactionTypeName(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF212121)
                                )
                            }
                        }
                        
                        if (showPayment) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = if (showType) Alignment.End else Alignment.Start
                            ) {
                                Text(
                                    text = "PAYMENT METHOD",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF757575),
                                    letterSpacing = 0.5.sp,
                                    textAlign = if (showType) TextAlign.End else TextAlign.Start
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = (transaction.paymentMethodTo?.title ?: transaction.paymentMethodFrom?.title) ?: "N/A",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF212121),
                                    textAlign = if (showType) TextAlign.End else TextAlign.Start
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(20.dp))
        }
        
        // Bill To and Ship To
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bill To
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFFAFAFA))
                    .padding(12.dp)
            ) {
                Text(
                    text = "BILL TO",
                    fontSize = 9.sp,
                    color = Color(0xFF757575),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(8.dp))
                
                transaction.party?.let { party ->
                    if (config.showCustomerName) {
                        Text(
                            text = party.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    
                    if (config.showCustomerPhone && party.phone != null) {
                        Text(
                            text = party.phone,
                            fontSize = 11.sp,
                            color = Color(0xFF616161)
                        )
                        Spacer(Modifier.height(2.dp))
                    }
                    
                    if (config.showCustomerAddress && party.address != null) {
                        Text(
                            text = party.address,
                            fontSize = 11.sp,
                            color = Color(0xFF616161)
                        )
                    }
                }
            }
            
            // Ship To
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFFAFAFA))
                    .padding(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "SHIP TO",
                    fontSize = 9.sp,
                    color = Color(0xFF757575),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(8.dp))
                
                transaction.party?.let { party ->
                    if (config.showCustomerName) {
                        Text(
                            text = party.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121),
                            textAlign = TextAlign.End
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    
                    if (config.showCustomerAddress && party.address != null) {
                        Text(
                            text = party.address,
                            fontSize = 11.sp,
                            color = Color(0xFF616161),
                            textAlign = TextAlign.End
                        )
                        Spacer(Modifier.height(2.dp))
                    }
                    
                    if (config.showCustomerPhone && party.phone != null) {
                        Text(
                            text = party.phone,
                            fontSize = 11.sp,
                            color = Color(0xFF616161),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
        // Items Table
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            // Table Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFB71C1C))
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "DESCRIPTION",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    text = "QTY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.weight(0.6f)
                )
                Text(
                    text = "PRICE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "TOTAL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Items
            transaction.transactionDetails.forEachIndexed { index, detail ->
                val backgroundColor = if (index % 2 == 0) Color(0xFFFAFAFA) else Color.White
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor)
                        .padding(vertical = 12.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = detail.product?.title ?: "Unknown",
                        fontSize = 11.sp,
                        color = Color(0xFF212121),
                        modifier = Modifier.weight(2f)
                    )
                    Text(
                        text = String.format("%.1f", detail.quantity),
                        fontSize = 11.sp,
                        color = Color(0xFF616161),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(0.6f)
                    )
                    Text(
                        text = "₨${String.format("%.2f", detail.price)}",
                        fontSize = 11.sp,
                        color = Color(0xFF616161),
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "₨${String.format("%.2f", detail.calculateSubtotal())}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF212121),
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Show discount/tax if any
                if (detail.flatDiscount > 0 || detail.flatTax > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(backgroundColor)
                            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (detail.flatDiscount > 0) {
                            Text(
                                text = "Discount: -₨${String.format("%.2f", detail.flatDiscount)}",
                                fontSize = 9.sp,
                                color = Color(0xFFE53935),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                        if (detail.flatTax > 0) {
                            Text(
                                text = "Tax: +₨${String.format("%.2f", detail.flatTax)}",
                                fontSize = 9.sp,
                                color = Color(0xFF43A047)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
        // Summary Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5))
                .padding(16.dp)
        ) {
            // Subtotal
            SummaryRow("Subtotal", transaction.calculateSubtotal())
            
            // Product level totals
            val productsDiscount = transaction.transactionDetails.sumOf { it.flatDiscount }
            val productsTax = transaction.transactionDetails.sumOf { it.flatTax }
            
            if (productsDiscount > 0) {
                SummaryRow("Products Discount", productsDiscount, isNegative = true)
            }
            
            if (productsTax > 0) {
                SummaryRow("Products Tax", productsTax)
            }
            
            // Transaction Discount
            if (config.showDiscount && transaction.flatDiscount > 0) {
                SummaryRow("Transaction Discount", transaction.flatDiscount, isNegative = true)
            }
            
            // Transaction Tax
            if (config.showTax && transaction.flatTax > 0) {
                SummaryRow("Transaction Tax", transaction.flatTax)
            }
            
            // Shipping/Handling
            if (config.showAdditionalCharges && transaction.additionalCharges > 0) {
                SummaryRow("Shipping/Handling", transaction.additionalCharges)
            }
            
            Spacer(Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color(0xFFB71C1C))
            )
            
            Spacer(Modifier.height(12.dp))
            
            // Total Items
            if (config.showTotalItems) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Items",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF616161)
                    )
                    Text(
                        text = "${transaction.transactionDetails.size} items (${String.format("%.1f", transaction.calculateTotalQuantity())} qty)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF212121)
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
            
            // Grand Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GRAND TOTAL",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "₨${String.format("%.2f", transaction.calculateGrandTotal())}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB71C1C)
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Previous Balance
            if (config.showPreviousBalance && transaction.party != null && transaction.party.balance != 0.0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Previous Balance",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF616161)
                    )
                    Text(
                        text = "₨${String.format("%.2f", abs(transaction.party.balance))}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (transaction.party.balance > 0) Color(0xFF43A047) else Color(0xFFE53935)
                    )
                }
                Spacer(Modifier.height(4.dp))
            }
            
            // Paid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Paid",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF616161)
                )
                Text(
                    text = "₨${String.format("%.2f", transaction.totalPaid)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF43A047)
                )
            }
            
            // Payable Amount or Balance Due
            if (config.showPayableAmount || config.showCurrentBalance) {
                val balance = transaction.calculatePayable()
                if (balance != 0.0 || config.showPayableAmount) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (balance > 0) {
                                if (config.showCurrentBalance) "Current Balance" else "Balance Due"
                            } else {
                                "Change"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF616161)
                        )
                        Text(
                            text = "₨${String.format("%.2f", abs(balance))}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (balance > 0) Color(0xFFE53935) else Color(0xFF43A047)
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
        // Remarks
        if (transaction.description?.isNotEmpty() == true) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFAFAFA))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Remarks",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF757575),
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = transaction.description,
                    fontSize = 11.sp,
                    color = Color(0xFF616161)
                )
            }
            
            Spacer(Modifier.height(20.dp))
        }
        
        // Invoice Terms
        if (config.showInvoiceTerms && config.invoiceTerms.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFE0E0E0))
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = config.invoiceTerms,
                fontSize = 10.sp,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Regards Message
        if (config.showRegardsMessage && config.regardsMessage != null) {
            if (!config.showInvoiceTerms || config.invoiceTerms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFE0E0E0))
                )
                Spacer(Modifier.height(16.dp))
            } else {
                Spacer(Modifier.height(8.dp))
            }
            Text(
                text = config.regardsMessage,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1976D2),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(Modifier.height(20.dp))
        
        // Bottom border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color(0xFFB71C1C))
        )
    }
}

@Composable
private fun SummaryRow(label: String, amount: Double, isNegative: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF616161)
        )
        Text(
            text = "${if (isNegative) "-" else ""}₨${String.format("%.2f", amount)}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isNegative) Color(0xFFE53935) else Color(0xFF212121)
        )
    }
}

