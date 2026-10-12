package com.example.data.model

data class AccountWithBalance(
    val account: AccountEntity,
    val balancePaisa: Long
)

data class TransactionItem(
    val transaction: TransactionEntity,
    val categoryName: String?,
    val categoryIconKey: String?,
    val categoryColorHex: String?,
    val fromAccountName: String?,
    val toAccountName: String?
)

data class MonthlySummary(
    val incomePaisa: Long,
    val expensePaisa: Long
) {
    val netPaisa: Long get() = incomePaisa - expensePaisa
}

data class PendingRecurringItem(
    val occurrence: RecurringOccurrenceEntity,
    val rule: RecurringRuleEntity,
    val categoryName: String?,
    val categoryIconKey: String?,
    val accountName: String?
)

data class BudgetProgressItem(
    val category: CategoryEntity,
    val spentPaisa: Long,
    val limitPaisa: Long
) {
    val remainingPaisa: Long get() = limitPaisa - spentPaisa
    val percentageUsed: Float get() = if (limitPaisa > 0) (spentPaisa.toFloat() / limitPaisa.toFloat()).coerceIn(0f, 2f) else 0f
    val isOverBudget: Boolean get() = spentPaisa > limitPaisa
    val isWarningThreshold: Boolean get() = percentageUsed >= 0.8f && !isOverBudget
}
