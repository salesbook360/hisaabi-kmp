package com.hisaabi.hisaabi_kmp.settings.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Currency(
    val code: String,
    val name: String,
    val symbol: String,
    val flag: String  // Unicode flag emoji
) {
    fun getDisplayText(): String = "$name ($symbol)"
    
    companion object {
        // Default currency
        val PKR = Currency("PKR", "Pakistan Rupee", "₨", "🇵🇰")
        
        // All available currencies with their flags
        val ALL_CURRENCIES = listOf(
            Currency("EUR", "Euro", "€", "🇪🇺"),
            Currency("USD", "United States Dollar", "$", "🇺🇸"),
            Currency("GBP", "British Pound", "£", "🇬🇧"),
            Currency("CZK", "Czech Koruna", "Kč", "🇨🇿"),
            Currency("TRY", "Turkish Lira", "₺", "🇹🇷"),
            Currency("AED", "Emirati Dirham", "د.إ", "🇦🇪"),
            Currency("AFN", "Afghanistan Afghani", "؋", "🇦🇫"),
            Currency("ARS", "Argentine Peso", "$", "🇦🇷"),
            Currency("AUD", "Australian Dollar", "$", "🇦🇺"),
            Currency("BBD", "Barbados Dollar", "$", "🇧🇧"),
            Currency("BDT", "Bangladeshi Taka", "Tk", "🇧🇩"),
            Currency("BGN", "Bulgarian Lev", "лв", "🇧🇬"),
            Currency("BHD", "Bahraini Dinar", "BD", "🇧🇭"),
            Currency("BMD", "Bermuda Dollar", "$", "🇧🇲"),
            Currency("BND", "Brunei Dollar", "$", "🇧🇳"),
            Currency("BOB", "Bolivia Bolíviano", "\$b", "🇧🇴"),
            Currency("BRL", "Brazil Real", "R$", "🇧🇷"),
            Currency("BWP", "Botswana Pula", "P", "🇧🇼"),
            Currency("BTN", "Bhutanese Ngultrum", "Nu.", "🇧🇹"),
            Currency("BZD", "Belize Dollar", "BZ$", "🇧🇿"),
            Currency("CAD", "Canada Dollar", "$", "🇨🇦"),
            Currency("CHF", "Switzerland Franc", "CHF", "🇨🇭"),
            Currency("CLP", "Chile Peso", "$", "🇨🇱"),
            Currency("CNY", "China Yuan Renminbi", "¥", "🇨🇳"),
            Currency("COP", "Colombia Peso", "$", "🇨🇴"),
            Currency("CRC", "Costa Rica Colon", "₡", "🇨🇷"),
            Currency("DKK", "Denmark Krone", "kr", "🇩🇰"),
            Currency("DOP", "Dominican Republic Peso", "RD$", "🇩🇴"),
            Currency("DZD", "Algerian Dinar", "DA", "🇩🇿"),
            Currency("EGP", "Egypt Pound", "£", "🇪🇬"),
            Currency("ETB", "Ethiopian Birr", "Br", "🇪🇹"),
            Currency("GEL", "Georgian Lari", "₾", "🇬🇪"),
            Currency("GHS", "Ghana Cedi", "¢", "🇬🇭"),
            Currency("GMD", "Gambian Dalasi", "D", "🇬🇲"),
            Currency("GYD", "Guyana Dollar", "$", "🇬🇾"),
            Currency("HKD", "Hong Kong Dollar", "$", "🇭🇰"),
            Currency("HRK", "Croatia Kuna", "kn", "🇭🇷"),
            Currency("HUF", "Hungary Forint", "Ft", "🇭🇺"),
            Currency("IDR", "Indonesia Rupiah", "Rp", "🇮🇩"),
            Currency("ILS", "Israel Shekel", "₪", "🇮🇱"),
            Currency("INR", "Indian Rupee", "₹", "🇮🇳"),
            Currency("ISK", "Iceland Krona", "kr", "🇮🇸"),
            Currency("JMD", "Jamaica Dollar", "J$", "🇯🇲"),
            Currency("JPY", "Japanese Yen", "¥", "🇯🇵"),
            Currency("KES", "Kenyan Shilling", "KSh", "🇰🇪"),
            Currency("KRW", "Korea (South) Won", "₩", "🇰🇷"),
            Currency("KWD", "Kuwaiti Dinar", "د.ك", "🇰🇼"),
            Currency("KGS", "Kyrgystani Som", "KGS", "🇰🇬"),
            Currency("KYD", "Cayman Islands Dollar", "$", "🇰🇾"),
            Currency("KZT", "Kazakhstan Tenge", "₸", "🇰🇿"),
            Currency("LAK", "Laos Kip", "₭", "🇱🇦"),
            Currency("LKR", "Sri Lanka Rupee", "₨", "🇱🇰"),
            Currency("LRD", "Liberia Dollar", "$", "🇱🇷"),
            Currency("MAD", "Moroccan Dirham", "MAD", "🇲🇦"),
            Currency("MDL", "Moldovan Leu", "MDL", "🇲🇩"),
            Currency("MKD", "Macedonia Denar", "ден", "🇲🇰"),
            Currency("MMK", "Myanmar Kyat", "Ks", "🇲🇲"),
            Currency("MNT", "Mongolia Tughrik", "₮", "🇲🇳"),
            Currency("MUR", "Mauritius Rupee", "₨", "🇲🇺"),
            Currency("MWK", "Malawian Kwacha", "MK", "🇲🇼"),
            Currency("MVR", "Maldivian Rufiyaa", "Rf", "🇲🇻"),
            Currency("MXN", "Mexico Peso", "$", "🇲🇽"),
            Currency("MYR", "Malaysia Ringgit", "RM", "🇲🇾"),
            Currency("MZN", "Mozambique Metical", "MT", "🇲🇿"),
            Currency("NAD", "Namibia Dollar", "$", "🇳🇦"),
            Currency("NGN", "Nigeria Naira", "₦", "🇳🇬"),
            Currency("NIO", "Nicaragua Cordoba", "C$", "🇳🇮"),
            Currency("NOK", "Norway Krone", "kr", "🇳🇴"),
            Currency("NPR", "Nepal Rupee", "₨", "🇳🇵"),
            Currency("NZD", "New Zealand Dollar", "$", "🇳🇿"),
            Currency("OMR", "Oman Rial", "﷼", "🇴🇲"),
            Currency("PEN", "Peru Sol", "S/.", "🇵🇪"),
            Currency("PGK", "Papua New Guinean Kina", "K", "🇵🇬"),
            Currency("PHP", "Philippines Peso", "₱", "🇵🇭"),
            PKR,  // Pakistan Rupee
            Currency("PLN", "Poland Zloty", "zł", "🇵🇱"),
            Currency("PYG", "Paraguay Guarani", "Gs", "🇵🇾"),
            Currency("QAR", "Qatar Riyal", "﷼", "🇶🇦"),
            Currency("RON", "Romania Leu", "lei", "🇷🇴"),
            Currency("RSD", "Serbia Dinar", "Дин.", "🇷🇸"),
            Currency("RUB", "Russia Ruble", "₽", "🇷🇺"),
            Currency("SAR", "Saudi Arabia Riyal", "﷼", "🇸🇦"),
            Currency("SEK", "Sweden Krona", "kr", "🇸🇪"),
            Currency("SGD", "Singapore Dollar", "$", "🇸🇬"),
            Currency("SOS", "Somalia Shilling", "S", "🇸🇴"),
            Currency("SRD", "Suriname Dollar", "$", "🇸🇷"),
            Currency("SLL", "Sierra Leonean Leone", "Le", "🇸🇱"),
            Currency("SSP", "South Sudanese Pound", "£", "🇸🇸"),
            Currency("THB", "Thailand Baht", "฿", "🇹🇭"),
            Currency("TND", "Tunisian Dinar", "د.ت", "🇹🇳"),
            Currency("TTD", "Trinidad and Tobago Dollar", "TT$", "🇹🇹"),
            Currency("TWD", "Taiwan New Dollar", "NT$", "🇹🇼"),
            Currency("TZS", "Tanzanian Shilling", "TSh", "🇹🇿"),
            Currency("UAH", "Ukraine Hryvnia", "₴", "🇺🇦"),
            Currency("UGX", "Ugandan Shilling", "USh", "🇺🇬"),
            Currency("UYU", "Uruguay Peso", "\$U", "🇺🇾"),
            Currency("VEF", "Venezuela Bolívar", "Bs", "🇻🇪"),
            Currency("VND", "Viet Nam Dong", "₫", "🇻🇳"),
            Currency("XAF", "Central African CFA Franc", "FCFA", "🇨🇲"),
            Currency("XOF", "West African CFA Franc", "CFA", "🇸🇳"),
            Currency("YER", "Yemen Rial", "﷼", "🇾🇪"),
            Currency("ZAR", "South Africa Rand", "R", "🇿🇦"),
            Currency("ZMK", "Zambian Kwacha", "ZK", "🇿🇲"),
            Currency("ZWL", "Zimbabwean Dollar", "Z$", "🇿🇼")
        )
        
        fun findByCode(code: String): Currency? {
            return ALL_CURRENCIES.find { it.code.equals(code, ignoreCase = true) }
        }
    }
}

