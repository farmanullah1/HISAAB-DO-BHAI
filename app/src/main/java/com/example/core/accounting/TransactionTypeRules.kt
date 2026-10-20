package com.example.core.accounting

enum class ReportingClass {
    INCOME,
    EXPENSE,
    NEUTRAL,
    COMMITTEE
}

enum class CoreTransactionType(
    val reportingClass: ReportingClass,
    val defaultSign: Int // +1 for credit/inflow, -1 for debit/outflow, 0 for neutral
) {
    INCOME(ReportingClass.INCOME, 1),
    EXPENSE(ReportingClass.EXPENSE, -1),
    TRANSFER_OUT(ReportingClass.NEUTRAL, -1),
    TRANSFER_IN(ReportingClass.NEUTRAL, 1),
    DEBT_RECEIPT(ReportingClass.NEUTRAL, 1),       // Borrowed money received
    DEBT_DISBURSEMENT(ReportingClass.NEUTRAL, -1), // Lent money handed over
    DEBT_REPAYMENT_OUT(ReportingClass.NEUTRAL, -1),// I repaid someone
    DEBT_COLLECTION_IN(ReportingClass.NEUTRAL, 1), // Someone repaid me
    COMMITTEE_CONTRIBUTION(ReportingClass.COMMITTEE, -1),
    COMMITTEE_PAYOUT(ReportingClass.COMMITTEE, 1),
    CHARITY_PAYMENT(ReportingClass.EXPENSE, -1),
    ADJUSTMENT_UP(ReportingClass.NEUTRAL, 1),
    ADJUSTMENT_DOWN(ReportingClass.NEUTRAL, -1),
    REVERSAL(ReportingClass.NEUTRAL, 0);

    companion object {
        fun incomeTypes(): List<CoreTransactionType> =
            entries.filter { it.reportingClass == ReportingClass.INCOME }

        fun expenseTypes(): List<CoreTransactionType> =
            entries.filter { it.reportingClass == ReportingClass.EXPENSE }

        fun committeeTypes(): List<CoreTransactionType> =
            entries.filter { it.reportingClass == ReportingClass.COMMITTEE }
    }
}
