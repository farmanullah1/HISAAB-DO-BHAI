package com.example.domain

import com.example.data.model.TransactionType
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Pure domain functions for Pakistani Rupee (PKR) money representation.
 * All amounts are stored as Long paisa (1 PKR = 100 paisa).
 * Floating-point numbers are strictly forbidden for monetary calculations.
 */
object Money {

    private val pkrFormat: DecimalFormat = run {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
            decimalSeparator = '.'
        }
        DecimalFormat("#,##0.00", symbols)
    }

    /**
     * Formats paisa as standard PKR string: e.g. 123456L -> "Rs. 1,234.56"
     */
    fun formatPkr(paisa: Long): String {
        val isNegative = paisa < 0
        val absPaisa = kotlin.math.abs(paisa)
        val rupees = absPaisa / 100
        val remainingPaisa = absPaisa % 100
        val formatted = "${pkrFormat.format(rupees).substringBefore('.')}.${String.format(Locale.US, "%02d", remainingPaisa)}"
        return if (isNegative) "-Rs. $formatted" else "Rs. $formatted"
    }

    /**
     * Formats paisa with transaction type sign:
     * INCOME -> "+Rs. 500.00"
     * EXPENSE -> "-Rs. 250.00"
     * TRANSFER -> "Rs. 300.00"
     */
    fun formatSigned(paisa: Long, type: TransactionType): String {
        val absPaisa = kotlin.math.abs(paisa)
        val base = formatPkr(absPaisa)
        return when (type) {
            TransactionType.INCOME -> "+$base"
            TransactionType.EXPENSE -> "-$base"
            TransactionType.TRANSFER -> base
        }
    }

    /**
     * Parses a user input string into paisa (Long).
     * Accept: "0", "100", "100.5", "1234.56"
     * Reject: negative, >2 decimals, blank, non-numeric, multiple dots.
     * Returns null on invalid (never throws).
     */
    fun parsePaisa(input: String): Long? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null
        if (trimmed.startsWith("-") || trimmed.startsWith("+")) return null

        val parts = trimmed.split('.')
        if (parts.size > 2) return null

        val wholePartStr = parts[0]
        if (wholePartStr.isEmpty()) return null
        if (!wholePartStr.all { it.isDigit() }) return null

        val wholeRupees = wholePartStr.toLongOrNull() ?: return null

        val fractionPaisa = if (parts.size == 2) {
            val fractionStr = parts[1]
            if (!fractionStr.all { it.isDigit() }) return null
            when (fractionStr.length) {
                0 -> 0L
                1 -> (fractionStr + "0").toLong()
                2 -> fractionStr.toLong()
                else -> return null // More than 2 decimal places rejected
            }
        } else {
            0L
        }

        // Check for potential overflow
        if (wholeRupees > Long.MAX_VALUE / 100 - fractionPaisa) return null

        return wholeRupees * 100 + fractionPaisa
    }

    /**
     * Converts raw keypad string buffer (e.g. "1250", "45.5") into paisa if valid.
     */
    fun keypadBufferToPaisa(buffer: String): Long {
        if (buffer.isEmpty() || buffer == ".") return 0L
        return parsePaisa(buffer) ?: 0L
    }
}
