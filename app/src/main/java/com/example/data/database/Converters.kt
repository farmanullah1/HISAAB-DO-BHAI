package com.example.data.database

import androidx.room.TypeConverter
import com.example.data.model.AccountType
import com.example.data.model.CategoryType
import com.example.data.model.CharityCategory
import com.example.data.model.CharityStatus
import com.example.data.model.ContributionStatus
import com.example.data.model.DebtDirection
import com.example.data.model.DebtEventType
import com.example.data.model.DebtKind
import com.example.data.model.DebtStatus
import com.example.data.model.InstallmentStatus
import com.example.data.model.KametiCadence
import com.example.data.model.KametiStatus
import com.example.data.model.OccurrenceStatus
import com.example.data.model.PayoutStatus
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
    fun fromDebtDirection(value: DebtDirection): String = value.name

    @TypeConverter
    fun toDebtDirection(value: String): DebtDirection =
        runCatching { DebtDirection.valueOf(value) }.getOrDefault(DebtDirection.I_GAVE)

    @TypeConverter
    fun fromDebtStatus(value: DebtStatus): String = value.name

    @TypeConverter
    fun toDebtStatus(value: String): DebtStatus =
        runCatching { DebtStatus.valueOf(value) }.getOrDefault(DebtStatus.ACTIVE)

    @TypeConverter
    fun fromDebtEventType(value: DebtEventType): String = value.name

    @TypeConverter
    fun toDebtEventType(value: String): DebtEventType =
        runCatching { DebtEventType.valueOf(value) }.getOrDefault(DebtEventType.ORIGINAL_ADVANCE)

    @TypeConverter
    fun fromDebtKind(value: DebtKind): String = value.name

    @TypeConverter
    fun toDebtKind(value: String): DebtKind =
        runCatching { DebtKind.valueOf(value) }.getOrDefault(DebtKind.MONEY)

    @TypeConverter
    fun fromKametiCadence(value: KametiCadence): String = value.name

    @TypeConverter
    fun toKametiCadence(value: String): KametiCadence =
        runCatching { KametiCadence.valueOf(value) }.getOrDefault(KametiCadence.MONTHLY)

    @TypeConverter
    fun fromKametiStatus(value: KametiStatus): String = value.name

    @TypeConverter
    fun toKametiStatus(value: String): KametiStatus =
        runCatching { KametiStatus.valueOf(value) }.getOrDefault(KametiStatus.ACTIVE)

    @TypeConverter
    fun fromContributionStatus(value: ContributionStatus): String = value.name

    @TypeConverter
    fun toContributionStatus(value: String): ContributionStatus =
        runCatching { ContributionStatus.valueOf(value) }.getOrDefault(ContributionStatus.PENDING)

    @TypeConverter
    fun fromPayoutStatus(value: PayoutStatus): String = value.name

    @TypeConverter
    fun toPayoutStatus(value: String): PayoutStatus =
        runCatching { PayoutStatus.valueOf(value) }.getOrDefault(PayoutStatus.EXPECTED)

    @TypeConverter
    fun fromInstallmentStatus(value: InstallmentStatus): String = value.name

    @TypeConverter
    fun toInstallmentStatus(value: String): InstallmentStatus =
        runCatching { InstallmentStatus.valueOf(value) }.getOrDefault(InstallmentStatus.SCHEDULED)

    @TypeConverter
    fun fromCharityCategory(value: CharityCategory): String = value.name

    @TypeConverter
    fun toCharityCategory(value: String): CharityCategory =
        runCatching { CharityCategory.valueOf(value) }.getOrDefault(CharityCategory.SADQAH)

    @TypeConverter
    fun fromCharityStatus(value: CharityStatus): String = value.name

    @TypeConverter
    fun toCharityStatus(value: String): CharityStatus =
        runCatching { CharityStatus.valueOf(value) }.getOrDefault(CharityStatus.PLANNED)
}
