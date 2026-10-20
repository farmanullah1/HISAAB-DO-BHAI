package com.example.core.money

import java.time.LocalDate

object InstallmentSplitter {

    /**
     * Splits principal into n parts that sum EXACTLY to principal.
     * The first (principal % n) parts receive +1 paisa remainder distribution.
     */
    fun split(principalPaisa: Long, count: Int): List<Long> {
        require(principalPaisa >= 0) { "principal must be >= 0" }
        require(count in 1..600) { "count out of range (must be 1..600)" }
        val base = principalPaisa / count
        val extra = (principalPaisa % count).toInt()
        return List(count) { i -> if (i < extra) base + 1 else base }
    }

    /**
     * Monthly dates computed directly from the anchor (not iteratively)
     * ensuring Jan 31 -> Feb 28/29 -> Mar 31 is handled cleanly by java.time.
     */
    fun monthlyDates(anchor: LocalDate, count: Int): List<LocalDate> =
        List(count) { anchor.plusMonths(it.toLong()) }

    /**
     * Weekly dates computed directly from the anchor.
     */
    fun weeklyDates(anchor: LocalDate, count: Int): List<LocalDate> =
        List(count) { anchor.plusWeeks(it.toLong()) }
}
