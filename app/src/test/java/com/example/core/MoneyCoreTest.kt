package com.example.core

import com.example.core.money.AmountError
import com.example.core.money.AmountParseResult
import com.example.core.money.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyCoreTest {

    @Test
    fun parse_validAmounts() {
        val r1 = Money.parse("100")
        assertTrue(r1 is AmountParseResult.Success)
        assertEquals(10000L, (r1 as AmountParseResult.Success).paisa)

        val r2 = Money.parse("1,250")
        assertTrue(r2 is AmountParseResult.Success)
        assertEquals(125000L, (r2 as AmountParseResult.Success).paisa)

        val r3 = Money.parse("1250.5")
        assertTrue(r3 is AmountParseResult.Success)
        assertEquals(125050L, (r3 as AmountParseResult.Success).paisa)

        val r4 = Money.parse("₨ 1,250.50")
        assertTrue(r4 is AmountParseResult.Success)
        assertEquals(125050L, (r4 as AmountParseResult.Success).paisa)

        val r5 = Money.parse("Rs. 500")
        assertTrue(r5 is AmountParseResult.Success)
        assertEquals(50000L, (r5 as AmountParseResult.Success).paisa)

        val r6 = Money.parse("PKR 99.99")
        assertTrue(r6 is AmountParseResult.Success)
        assertEquals(9999L, (r6 as AmountParseResult.Success).paisa)
    }

    @Test
    fun parse_zeroHandling() {
        val disallowed = Money.parse("0", allowZero = false)
        assertTrue(disallowed is AmountParseResult.Failure)
        assertEquals(AmountError.ZERO_NOT_ALLOWED, (disallowed as AmountParseResult.Failure).error)

        val allowed = Money.parse("0", allowZero = true)
        assertTrue(allowed is AmountParseResult.Success)
        assertEquals(0L, (allowed as AmountParseResult.Success).paisa)
    }

    @Test
    fun parse_rejections() {
        val empty = Money.parse("   ")
        assertTrue(empty is AmountParseResult.Failure)
        assertEquals(AmountError.EMPTY, (empty as AmountParseResult.Failure).error)

        val negative = Money.parse("-50")
        assertTrue(negative is AmountParseResult.Failure)
        assertEquals(AmountError.NEGATIVE, (negative as AmountParseResult.Failure).error)

        val tooManyDecimals = Money.parse("12.345")
        assertTrue(tooManyDecimals is AmountParseResult.Failure)
        assertEquals(AmountError.TOO_MANY_DECIMALS, (tooManyDecimals as AmountParseResult.Failure).error)

        val malformed = Money.parse("abc")
        assertTrue(malformed is AmountParseResult.Failure)
        assertEquals(AmountError.MALFORMED, (malformed as AmountParseResult.Failure).error)
    }

    @Test
    fun parse_urduArabicIndicDigits() {
        // Urdu/Eastern Arabic digits: ۱۲۵۰ (1250)
        val urduStr = "\u06F1\u06F2\u06F5\u06F0"
        val res = Money.parse(urduStr)
        assertTrue(res is AmountParseResult.Success)
        assertEquals(125000L, (res as AmountParseResult.Success).paisa)
    }

    @Test
    fun addAndSubtractExact_noOverflow() {
        assertEquals(1500L, Money.addExact(1000L, 500L))
        assertEquals(500L, Money.subtractExact(1000L, 500L))
    }
}
