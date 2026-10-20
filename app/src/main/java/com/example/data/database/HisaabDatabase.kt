package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.CharityRecordEntity
import com.example.data.model.DebtEntity
import com.example.data.model.DebtEventEntity
import com.example.data.model.ImportFingerprintEntity
import com.example.data.model.KametiContributionEntity
import com.example.data.model.KametiEntity
import com.example.data.model.KametiPayoutEntity
import com.example.data.model.KarzInstallmentEntity
import com.example.data.model.NeedEntity
import com.example.data.model.ReceiptAttachmentEntity
import com.example.data.model.RecurringOccurrenceEntity
import com.example.data.model.RecurringRuleEntity
import com.example.data.model.ReservedAmountEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserReminderEntity
import com.example.data.model.ZakatCalculationEntity

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Extend debts table
        db.execSQL("ALTER TABLE debts ADD COLUMN kind TEXT NOT NULL DEFAULT 'MONEY'")
        db.execSQL("ALTER TABLE debts ADD COLUMN itemDescription TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE debts ADD COLUMN itemQuantity REAL DEFAULT NULL")
        db.execSQL("ALTER TABLE debts ADD COLUMN itemUnit TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE debts ADD COLUMN estimatedValuePaisa INTEGER DEFAULT NULL")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_debts_kind ON debts (kind)")

        // 2. Extend transactions table
        db.execSQL("ALTER TABLE transactions ADD COLUMN sourceType TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE transactions ADD COLUMN sourceId INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE transactions ADD COLUMN transferId TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE transactions ADD COLUMN reversalOfId INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE transactions ADD COLUMN voidedAtEpochMs INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE transactions ADD COLUMN updatedAtEpochMs INTEGER NOT NULL DEFAULT 0")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_transferId ON transactions (transferId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_sourceType ON transactions (sourceType)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_voidedAtEpochMs ON transactions (voidedAtEpochMs)")

        // 3. Create kameti tables
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS kameti (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                memberCount INTEGER NOT NULL,
                contributionPaisa INTEGER NOT NULL,
                cadence TEXT NOT NULL,
                startDateEpochDay INTEGER NOT NULL,
                totalInstallments INTEGER NOT NULL,
                myPayoutInstallment INTEGER NOT NULL,
                expectedPayoutPaisa INTEGER,
                status TEXT NOT NULL,
                note TEXT NOT NULL,
                archived INTEGER NOT NULL,
                createdAtEpochMs INTEGER NOT NULL,
                updatedAtEpochMs INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_kameti_status ON kameti (status)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_kameti_archived ON kameti (archived)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS kameti_contribution (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                kametiId INTEGER NOT NULL,
                installmentIndex INTEGER NOT NULL,
                dueDateEpochDay INTEGER NOT NULL,
                expectedPaisa INTEGER NOT NULL,
                paidPaisa INTEGER NOT NULL,
                status TEXT NOT NULL,
                lastPaidDateEpochDay INTEGER,
                note TEXT NOT NULL,
                FOREIGN KEY (kametiId) REFERENCES kameti(id) ON DELETE RESTRICT
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_kameti_contribution_kametiId ON kameti_contribution (kametiId)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_kameti_contribution_kametiId_installmentIndex ON kameti_contribution (kametiId, installmentIndex)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_kameti_contribution_dueDateEpochDay ON kameti_contribution (dueDateEpochDay)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS kameti_payout (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                kametiId INTEGER NOT NULL,
                expectedDateEpochDay INTEGER NOT NULL,
                expectedPaisa INTEGER,
                receivedPaisa INTEGER,
                receivedDateEpochDay INTEGER,
                status TEXT NOT NULL,
                transactionId INTEGER,
                FOREIGN KEY (kametiId) REFERENCES kameti(id) ON DELETE RESTRICT
            )
        """.trimIndent())
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_kameti_payout_kametiId ON kameti_payout (kametiId)")

        // 4. Create karz_installment table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS karz_installment (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                debtId INTEGER NOT NULL,
                number INTEGER NOT NULL,
                totalCount INTEGER NOT NULL,
                dueDateEpochDay INTEGER NOT NULL,
                scheduledPaisa INTEGER NOT NULL,
                paidPaisa INTEGER NOT NULL,
                status TEXT NOT NULL,
                note TEXT NOT NULL,
                FOREIGN KEY (debtId) REFERENCES debts(id) ON DELETE RESTRICT
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_karz_installment_debtId ON karz_installment (debtId)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_karz_installment_debtId_number ON karz_installment (debtId, number)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_karz_installment_dueDateEpochDay ON karz_installment (dueDateEpochDay)")

        // 5. Create charity_record and zakat_calculation tables
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS charity_record (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                category TEXT NOT NULL,
                customCategoryName TEXT,
                recipientName TEXT NOT NULL,
                amountPaisa INTEGER NOT NULL,
                status TEXT NOT NULL,
                plannedDateEpochDay INTEGER,
                paidDateEpochDay INTEGER,
                accountId INTEGER,
                transactionId INTEGER,
                zakatCalculationId INTEGER,
                note TEXT NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_charity_record_paidDateEpochDay ON charity_record (paidDateEpochDay)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_charity_record_zakatCalculationId ON charity_record (zakatCalculationId)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS zakat_calculation (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                createdAtEpochMs INTEGER NOT NULL,
                label TEXT NOT NULL,
                standard TEXT NOT NULL,
                weightMgUsed INTEGER NOT NULL,
                ratePerGramPaisa INTEGER NOT NULL,
                rateEnteredDateEpochDay INTEGER NOT NULL,
                rateSource TEXT NOT NULL,
                nisabThresholdPaisa INTEGER NOT NULL,
                eligibleWealthPaisa INTEGER NOT NULL,
                zakatPayablePaisa INTEGER NOT NULL,
                nisabReached INTEGER NOT NULL,
                hawlConfirmedByUser INTEGER NOT NULL,
                assumptionsJson TEXT NOT NULL,
                note TEXT NOT NULL
            )
        """.trimIndent())

        // 6. Create category_budget table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS category_budget (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                categoryId INTEGER NOT NULL,
                monthKey TEXT NOT NULL,
                limitPaisa INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_category_budget_categoryId_monthKey ON category_budget (categoryId, monthKey)")

        // 7. Create audit_log table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS audit_log (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                createdAtEpochMs INTEGER NOT NULL,
                entityType TEXT NOT NULL,
                entityId INTEGER NOT NULL,
                action TEXT NOT NULL,
                amountPaisa INTEGER
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_audit_log_createdAtEpochMs ON audit_log (createdAtEpochMs)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_audit_log_entityType ON audit_log (entityType)")

        // 8. Create user_reminder table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS user_reminder (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                dateEpochDay INTEGER NOT NULL,
                done INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_user_reminder_dateEpochDay ON user_reminder (dateEpochDay)")

        // 9. Create import_fingerprint table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS import_fingerprint (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                fingerprint TEXT NOT NULL,
                transactionId INTEGER NOT NULL,
                createdAtEpochMs INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_import_fingerprint_fingerprint ON import_fingerprint (fingerprint)")
    }
}

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        NeedEntity::class,
        ReceiptAttachmentEntity::class,
        RecurringRuleEntity::class,
        RecurringOccurrenceEntity::class,
        ReservedAmountEntity::class,
        DebtEntity::class,
        DebtEventEntity::class,
        KametiEntity::class,
        KametiContributionEntity::class,
        KametiPayoutEntity::class,
        KarzInstallmentEntity::class,
        CharityRecordEntity::class,
        ZakatCalculationEntity::class,
        CategoryBudgetEntity::class,
        AuditLogEntity::class,
        UserReminderEntity::class,
        ImportFingerprintEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HisaabDatabase : RoomDatabase() {

    abstract fun dao(): HisaabDao

    companion object {
        @Volatile
        private var INSTANCE: HisaabDatabase? = null

        fun getInstance(context: Context): HisaabDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HisaabDatabase::class.java,
                    "hisaab_database.db"
                )
                .addMigrations(MIGRATION_4_5)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
