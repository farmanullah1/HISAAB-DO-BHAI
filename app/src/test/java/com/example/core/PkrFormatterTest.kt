package com.example.core

import com.example.core.money.CurrencyStyle
import com.example.core.money.PkrFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class PkrFormatterTest {

    @Test
    fun format_specRequiredCases() {
        assertEquals("₨ 0", PkrFormatter.format(0L))
        assertEquals("₨ 1", PkrFormatter.format(100L))
        assertEquals("₨ 1.50", PkrFormatter.format(150L))
        assertEquals("₨ 12,34,567", PkrFormatter.format(123456700L))
        assertEquals("-₨ 50", PkrFormatter.format(-5000L))
    }

    @Test
    fun format_signedFlag() {
        assertEquals("+₨ 50", PkrFormatter.format(5000L, signed = true))
        assertEquals("-₨ 50", PkrFormatter.format(-5000L, signed = true))
        assertEquals("₨ 0", PkrFormatter.format(0L, signed = true))
    }

    @Test
    fun format_styles() {
        assertEquals("Rs 1,000", PkrFormatter.format(100000L, style = CurrencyStyle.RS))
        assertEquals("PKR 1,000", PkrFormatter.format(100000L, style = CurrencyStyle.PKR))
        assertEquals("₨ 1,000", PkrFormatter.format(100000L, style = CurrencyStyle.SYMBOL))
    }

    @Test
    fun group_lakhCrore() {
        assertEquals("12", PkrFormatter.group("12"))
        assertEquals("123", PkrFormatter.group("123"))
        assertEquals("1,234", PkrFormatter.group("1234"))
        assertEquals("12,345", PkrFormatter.group("12345"))
        assertEquals("1,23,456", PkrFormatter.group("123456"))
        assertEquals("12,34,567", PkrFormatter.group("1234567"))
        assertEquals("1,23,45,678", PkrFormatter.group("12345678"))
    }
}
