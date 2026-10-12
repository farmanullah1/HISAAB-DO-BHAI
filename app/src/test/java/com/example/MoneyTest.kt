package com.example

import com.example.data.model.TransactionType
import com.example.domain.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {

    @Test
    fun testFormatPkrZero() {
        assertEquals("Rs. 0.00", Money.formatPkr(0L))
    }

    @Test
    fun testFormatPkrSimple() {
        assertEquals("Rs. 1,234.56", Money.formatPkr(123456L))
        assertEquals("Rs. 100.00", Money.formatPkr(10000L))
        assertEquals("Rs. 0.50", Money.formatPkr(50L))
        assertEquals("Rs. 0.05", Money.formatPkr(5L))
    }

    @Test
    fun testFormatPkrLarge() {
        assertEquals("Rs. 1,000,000.00", Money.formatPkr(100000000L))
        assertEquals("Rs. 12,345,678.90", Money.formatPkr(1234567890L))
    }

    @Test
    fun testFormatSigned() {
        assertEquals("+Rs. 500.00", Money.formatSigned(50000L, TransactionType.INCOME))
        assertEquals("-Rs. 250.00", Money.formatSigned(25000L, TransactionType.EXPENSE))
        assertEquals("Rs. 300.00", Money.formatSigned(30000L, TransactionType.TRANSFER))
    }

    @Test
    fun testParseValidInputs() {
        assertEquals(0L, Money.parsePaisa("0"))
        assertEquals(0L, Money.parsePaisa("0.00"))
        assertEquals(10000L, Money.parsePaisa("100"))
        assertEquals(10050L, Money.parsePaisa("100.5"))
        assertEquals(10050L, Money.parsePaisa("100.50"))
        assertEquals(123456L, Money.parsePaisa("1234.56"))
        assertEquals(50L, Money.parsePaisa("0.5"))
        assertEquals(5L, Money.parsePaisa("0.05"))
    }

    @Test
    fun testParseRejectsInvalidPatterns() {
        assertNull(Money.parsePaisa(""))
        assertNull(Money.parsePaisa("   "))
        assertNull(Money.parsePaisa("-100"))
        assertNull(Money.parsePaisa("+100"))
        assertNull(Money.parsePaisa("12.345")) // More than 2 decimals
        assertNull(Money.parsePaisa("12.34.56")) // Multiple dots
        assertNull(Money.parsePaisa("abc"))
        assertNull(Money.parsePaisa("12a"))
        assertNull(Money.parsePaisa(".50")) // Missing whole part
    }

    @Test
    fun testKeypadBufferToPaisa() {
        assertEquals(0L, Money.keypadBufferToPaisa(""))
        assertEquals(0L, Money.keypadBufferToPaisa("."))
        assertEquals(150000L, Money.keypadBufferToPaisa("1500"))
        assertEquals(150050L, Money.keypadBufferToPaisa("1500.5"))
    }

    @Test
    fun testRoundTripParseAndFormat() {
        val originalPaisa = 459950L // Rs. 4,599.50
        val formatted = Money.formatPkr(originalPaisa) // "Rs. 4,599.50"
        val rawNumberString = formatted.removePrefix("Rs. ").replace(",", "") // "4599.50"
        val parsed = Money.parsePaisa(rawNumberString)
        assertEquals(originalPaisa, parsed)
    }
}
