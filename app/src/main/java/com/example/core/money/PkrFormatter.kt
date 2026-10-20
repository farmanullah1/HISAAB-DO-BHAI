package com.example.core.money

enum class CurrencyStyle(val prefix: String) {
    SYMBOL("₨ "),
    RS("Rs "),
    PKR("PKR ")
}

object PkrFormatter {

    /**
     * Formats integer paisa with South Asian lakh/crore grouping: e.g. 123456700 -> "₨ 12,34,567"
     */
    fun format(paisa: Long, style: CurrencyStyle = CurrencyStyle.SYMBOL, signed: Boolean = false): String {
        require(paisa != Long.MIN_VALUE) { "paisa cannot be Long.MIN_VALUE" }
        val negative = paisa < 0
        val abs = if (negative) -paisa else paisa
        val rupees = abs / 100
        val frac = (abs % 100).toInt()
        val body = group(rupees.toString()) + if (frac != 0) ".%02d".format(java.util.Locale.ROOT, frac) else ""
        val sign = when {
            negative -> "-"
            signed && paisa > 0 -> "+"
            else -> ""
        }
        return "$sign${style.prefix}$body"
    }

    /**
     * 1234567 -> 12,34,567
     * Groups last 3 digits, then chunks remaining preceding digits by 2.
     */
    internal fun group(digits: String): String {
        if (digits.length <= 3) return digits
        val last3 = digits.takeLast(3)
        val rest = digits.dropLast(3).reversed().chunked(2).joinToString(",").reversed()
        return "$rest,$last3"
    }
}
