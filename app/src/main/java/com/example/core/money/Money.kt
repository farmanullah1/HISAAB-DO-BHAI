package com.example.core.money

sealed interface AmountParseResult {
    data class Success(val paisa: Long) : AmountParseResult
    data class Failure(val error: AmountError) : AmountParseResult
}

enum class AmountError {
    EMPTY,
    MALFORMED,
    TOO_MANY_DECIMALS,
    NEGATIVE,
    ZERO_NOT_ALLOWED,
    TOO_LARGE
}

object Money {
    const val PAISA_PER_RUPEE = 100L
    const val MAX_PAISA = 99_999_999_999_999L // ₨ 999,999,999,999.99

    private val prefix = Regex("^(?:₨|Rs\\.?|PKR)\\s*", RegexOption.IGNORE_CASE)
    private val decimal = Regex("^\\d+(\\.\\d{1,2})?$")
    private val tooManyDecimals = Regex("^\\d+\\.\\d{3,}$")

    /**
     * Strictly parses user input into integer paisa.
     * Accepts: "1,250", "1250.5", "₨ 1,250.50", Urdu/Arabic-Indic digits.
     * Rejects: negative numbers, >2 decimal places, malformed formats, overflow.
     */
    fun parse(input: String, allowZero: Boolean = false): AmountParseResult {
        var s = input.trim()
        if (s.isEmpty()) return AmountParseResult.Failure(AmountError.EMPTY)
        s = prefix.replaceFirst(s, "")
        s = normalizeDigits(s).replace(",", "").replace(" ", "")
        if (s.startsWith("-")) return AmountParseResult.Failure(AmountError.NEGATIVE)
        if (tooManyDecimals.matches(s)) return AmountParseResult.Failure(AmountError.TOO_MANY_DECIMALS)
        if (!decimal.matches(s)) return AmountParseResult.Failure(AmountError.MALFORMED)

        val parts = s.split('.')
        val whole = parts[0].toLongOrNull() ?: return AmountParseResult.Failure(AmountError.TOO_LARGE)
        if (whole > MAX_PAISA / PAISA_PER_RUPEE) return AmountParseResult.Failure(AmountError.TOO_LARGE)

        val frac = if (parts.size == 2) parts[1].padEnd(2, '0').toLong() else 0L
        val paisa = whole * PAISA_PER_RUPEE + frac
        if (paisa > MAX_PAISA) return AmountParseResult.Failure(AmountError.TOO_LARGE)
        if (paisa == 0L && !allowZero) return AmountParseResult.Failure(AmountError.ZERO_NOT_ALLOWED)

        return AmountParseResult.Success(paisa)
    }

    fun normalizeDigits(s: String): String = buildString(s.length) {
        for (c in s) {
            append(if (Character.isDigit(c)) Character.forDigit(Character.digit(c, 10), 10) else c)
        }
    }

    fun addExact(a: Long, b: Long): Long = Math.addExact(a, b)
    fun subtractExact(a: Long, b: Long): Long = Math.subtractExact(a, b)
}
