package com.hisaabi.hisaabi_kmp.templates.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MessageTemplate(
    val id: String = "",
    val title: String = "",
    val template: String = "",
    val composed: String? = null
) {
    companion object {
        fun getDefaultTemplates(): List<MessageTemplate> {
            return listOf(
                MessageTemplate(
                    id = "1",
                    title = "Cash Reminder",
                    template = "Dear [CUSTOMER_NAME], this is a gentle reminder that [PROMISED_AMOUNT] is due on [REMINDER_DATE] for [BUSINESS_NAME]. Please arrange payment at your earliest convenience. Thank you!\n\nRegards,\n[YOUR_NAME]\n[BUSINESS_NAME]\n[CONTACT_NUMBER]"
                ),
                MessageTemplate(
                    id = "2",
                    title = "Outstanding Balance",
                    template = "Dear [CUSTOMER_NAME], your current outstanding balance with [BUSINESS_NAME] is [CURRENT_BALANCE]. Please contact us to settle this amount.\n\nThank you,\n[YOUR_NAME]\n[BUSINESS_NAME]\n[CONTACT_NUMBER]"
                ),
                MessageTemplate(
                    id = "3",
                    title = "Payment Received",
                    template = "Dear [CUSTOMER_NAME], we have received your payment of [PROMISED_AMOUNT]. Thank you for your business!\n\nRegards,\n[YOUR_NAME]\n[BUSINESS_NAME]"
                ),
                MessageTemplate(
                    id = "4",
                    title = "Thank You Message",
                    template = "Dear [CUSTOMER_NAME], thank you for choosing [BUSINESS_NAME]. We appreciate your business and look forward to serving you again!\n\nBest regards,\n[YOUR_NAME]\n[CONTACT_NUMBER]"
                )
            )
        }
    }
}

/**
 * Enum for available placeholders in message templates
 */
enum class TemplatePlaceholder(val key: String, val description: String) {
    CUSTOMER_NAME("[CUSTOMER_NAME]", "Customer's name"),
    PROMISED_AMOUNT("[PROMISED_AMOUNT]", "Promised payment amount"),
    REMINDER_DATE("[REMINDER_DATE]", "Payment reminder date"),
    CURRENT_BALANCE("[CURRENT_BALANCE]", "Current outstanding balance"),
    YOUR_NAME("[YOUR_NAME]", "Your name"),
    CONTACT_NUMBER("[CONTACT_NUMBER]", "Your contact number"),
    BUSINESS_NAME("[BUSINESS_NAME]", "Your business name");

    companion object {
        fun getAllPlaceholders(): List<TemplatePlaceholder> = values().toList()
        
        fun getGuidelineText(): String {
            return buildString {
                appendLine("Available Placeholders:")
                appendLine()
                getAllPlaceholders().forEach { placeholder ->
                    appendLine("${placeholder.key} : ${placeholder.description}")
                }
            }
        }
    }
}

