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
    fun toAccountType(value: String): AccountType =
        runCatching { AccountType.valueOf(value) }.getOrDefault(AccountType.CASH)

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
}
