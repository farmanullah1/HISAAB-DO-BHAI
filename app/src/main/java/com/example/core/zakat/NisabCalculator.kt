package com.example.core.zakat

object NisabCalculator {
    const val DEFAULT_GOLD_NISAB_MG = 87_480L    // 87.48 grams in milligrams
    const val DEFAULT_SILVER_NISAB_MG = 612_360L // 612.36 grams in milligrams

    /**
     * Calculates Nisab threshold in integer paisa using half-up rounding:
     * (weightMg * ratePerGramPaisa + 500L) / 1000L.
     * Uses Math.multiplyExact to surface arithmetic overflow.
     */
    fun nisabThresholdPaisa(weightMg: Long, ratePerGramPaisa: Long): Long {
        require(weightMg > 0 && ratePerGramPaisa > 0) { "weightMg and ratePerGramPaisa must be positive" }
        val product = Math.multiplyExact(weightMg, ratePerGramPaisa)
        return (product + 500L) / 1000L
    }

    /**
     * Calculates 2.5% Zakat obligation (= 1/40) with half-up rounding:
     * (eligibleWealthPaisa + 20L) / 40L.
     */
    fun zakatPayablePaisa(eligibleWealthPaisa: Long): Long {
        require(eligibleWealthPaisa >= 0) { "eligibleWealthPaisa cannot be negative" }
        return (eligibleWealthPaisa + 20L) / 40L
    }

    fun isNisabReached(eligibleWealthPaisa: Long, thresholdPaisa: Long): Boolean =
        eligibleWealthPaisa >= thresholdPaisa
}
