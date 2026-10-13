package com.example.data.model

enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER,
    DEBT_LENT,
    DEBT_BORROWED,
    DEBT_REPAYMENT_IN,
    DEBT_REPAYMENT_OUT
}

enum class AccountType {
    CASH_WALLET,
    BANK,
    SAVINGS,
    OTHER
}

enum class CategoryType {
    EXPENSE,
    INCOME
}

enum class RecurringFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

enum class OccurrenceStatus {
    PENDING,
    CONFIRMED,
    SKIPPED,
    CANCELLED
}
