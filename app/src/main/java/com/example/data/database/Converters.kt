package com.example.data.database

import androidx.room.TypeConverter
import com.example.data.model.AccountType
import com.example.data.model.CategoryType
import com.example.data.model.OccurrenceStatus
import com.example.data.model.RecurringFrequency
import com.example.data.model.TransactionType

class Converters {

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType =
        runCatching { TransactionType.valueOf(value) }.getOrDefault(TransactionType.EXPENSE)

    @TypeConverter
    fun fromAccountType(value: AccountType): String = value.name

    @TypeConverter
    fun toAccountType(value: String): AccountType {
        return when (value.uppercase()) {
            "CASH", "WALLET", "CASH_WALLET" -> AccountType.CASH_WALLET
            "BANK" -> AccountType.BANK
            "SAVINGS" -> AccountType.SAVINGS
            else -> AccountType.OTHER
        }
    }

    @TypeConverter
    fun fromCategoryType(value: CategoryType): String = value.name

    @TypeConverter
    fun toCategoryType(value: String): CategoryType =
        runCatching { CategoryType.valueOf(value) }.getOrDefault(CategoryType.EXPENSE)

    @TypeConverter
    fun fromRecurringFrequency(value: RecurringFrequency): String = value.name

    @TypeConverter
    fun toRecurringFrequency(value: String): RecurringFrequency =
        runCatching { RecurringFrequency.valueOf(value) }.getOrDefault(RecurringFrequency.MONTHLY)

    @TypeConverter
    fun fromOccurrenceStatus(value: OccurrenceStatus): String = value.name

    @TypeConverter
    fun toOccurrenceStatus(value: String): OccurrenceStatus =
        runCatching { OccurrenceStatus.valueOf(value) }.getOrDefault(OccurrenceStatus.PENDING)

    @TypeConverter
    fun fromDebtDirection(value: com.example.data.model.DebtDirection): String = value.name

    @TypeConverter
    fun toDebtDirection(value: String): com.example.data.model.DebtDirection =
        runCatching { com.example.data.model.DebtDirection.valueOf(value) }
            .getOrDefault(com.example.data.model.DebtDirection.I_GAVE)

    @TypeConverter
    fun fromDebtStatus(value: com.example.data.model.DebtStatus): String = value.name

    @TypeConverter
    fun toDebtStatus(value: String): com.example.data.model.DebtStatus =
        runCatching { com.example.data.model.DebtStatus.valueOf(value) }
            .getOrDefault(com.example.data.model.DebtStatus.ACTIVE)

    @TypeConverter
    fun fromDebtEventType(value: com.example.data.model.DebtEventType): String = value.name

    @TypeConverter
    fun toDebtEventType(value: String): com.example.data.model.DebtEventType =
        runCatching { com.example.data.model.DebtEventType.valueOf(value) }
            .getOrDefault(com.example.data.model.DebtEventType.ORIGINAL_ADVANCE)
}
