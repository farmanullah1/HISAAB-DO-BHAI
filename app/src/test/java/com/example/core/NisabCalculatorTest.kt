package com.example.core

import com.example.core.zakat.NisabCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NisabCalculatorTest {

    @Test
    fun nisabThresholdPaisa_silverStandard() {
        // Silver weight: 612,360 mg (612.36g)
        // Rate: ₨ 300 per gram -> 30,000 paisa per gram
        // Expected = (612360 * 30000 + 500) / 1000 = 18,370,800 paisa (₨ 183,708.00)
        val threshold = NisabCalculator.nisabThresholdPaisa(
            NisabCalculator.DEFAULT_SILVER_NISAB_MG,
            30_000L
        )
        assertEquals(18_370_800L, threshold)
    }

    @Test
    fun zakatPayablePaisa_halfUpRoundingCases() {
        // eligible = 100 -> payable 3 ((100+20)/40 = 3)
        assertEquals(3L, NisabCalculator.zakatPayablePaisa(100L))

        // eligible = 19 -> payable 0 ((19+20)/40 = 0)
        assertEquals(0L, NisabCalculator.zakatPayablePaisa(19L))

        // eligible = 20 -> payable 1 ((20+20)/40 = 1)
        assertEquals(1L, NisabCalculator.zakatPayablePaisa(20L))

        // eligible = 100,000 paisa (₨ 1,000) -> payable 2,500 paisa (₨ 25)
        assertEquals(2500L, NisabCalculator.zakatPayablePaisa(100_000L))
    }

    @Test
    fun isNisabReached_comparison() {
        val threshold = 18_370_800L
        assertFalse(NisabCalculator.isNisabReached(18_370_799L, threshold))
        assertTrue(NisabCalculator.isNisabReached(18_370_800L, threshold))
        assertTrue(NisabCalculator.isNisabReached(20_000_000L, threshold))
    }

    @Test(expected = ArithmeticException::class)
    fun threshold_overflowThrowsArithmeticException() {
        NisabCalculator.nisabThresholdPaisa(Long.MAX_VALUE / 2, 100L)
    }
}
