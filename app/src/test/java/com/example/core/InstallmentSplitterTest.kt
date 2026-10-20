package com.example.core

import com.example.core.money.InstallmentSplitter
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class InstallmentSplitterTest {

    @Test
    fun split_exactSumForVariousCounts() {
        for (count in 1..100) {
            val principal = 100000L // ₨ 1,000.00
            val parts = InstallmentSplitter.split(principal, count)
            assertEquals(count, parts.size)
            assertEquals(principal, parts.sum())
        }
    }

    @Test
    fun split_remainderDistributedToFirstParts() {
        // 10 paisa split into 3 parts: 4, 3, 3
        val parts = InstallmentSplitter.split(10L, 3)
        assertEquals(listOf(4L, 3L, 3L), parts)
        assertEquals(10L, parts.sum())
    }

    @Test
    fun monthlyDates_anchoredPreservesMonthEnd() {
        // Jan 31 -> Feb 28 (non-leap) -> Mar 31 -> Apr 30
        val anchor = LocalDate.of(2025, 1, 31)
        val dates = InstallmentSplitter.monthlyDates(anchor, 4)
        assertEquals(LocalDate.of(2025, 1, 31), dates[0])
        assertEquals(LocalDate.of(2025, 2, 28), dates[1])
        assertEquals(LocalDate.of(2025, 3, 31), dates[2])
        assertEquals(LocalDate.of(2025, 4, 30), dates[3])
    }

    @Test
    fun weeklyDates_regularIntervals() {
        val anchor = LocalDate.of(2025, 5, 1)
        val dates = InstallmentSplitter.weeklyDates(anchor, 3)
        assertEquals(LocalDate.of(2025, 5, 1), dates[0])
        assertEquals(LocalDate.of(2025, 5, 8), dates[1])
        assertEquals(LocalDate.of(2025, 5, 15), dates[2])
    }
}
