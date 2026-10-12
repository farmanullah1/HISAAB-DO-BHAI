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
