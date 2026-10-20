package com.example

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.MIGRATION_4_5
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DatabaseMigrationTest {

    @Test
    fun migration_4_to_5_preservesDataAndAddsNewTables() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name("test_migration.db")
            .callback(object : SupportSQLiteOpenHelper.Callback(4) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    // Create minimal v4 schema
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS accounts (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            name TEXT NOT NULL,
                            type TEXT NOT NULL,
                            openingBalancePaisa INTEGER NOT NULL,
                            colorHex TEXT NOT NULL,
                            isArchived INTEGER NOT NULL,
                            createdAtEpochMs INTEGER NOT NULL
                        )
                    """.trimIndent())

                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS debts (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            personName TEXT NOT NULL,
                            direction TEXT NOT NULL,
                            originalPrincipalPaisa INTEGER NOT NULL,
                            dateEpochMs INTEGER NOT NULL,
                            dueDateEpochMs INTEGER,
                            description TEXT NOT NULL,
                            phoneNumber TEXT NOT NULL,
                            notes TEXT NOT NULL,
                            status TEXT NOT NULL,
                            linkedAccountId INTEGER,
                            isInterestEnabled INTEGER NOT NULL,
                            interestRatePercent REAL NOT NULL,
                            settledAtEpochMs INTEGER,
                            writtenOffReason TEXT,
                            createdAtEpochMs INTEGER NOT NULL,
                            updatedAtEpochMs INTEGER NOT NULL
                        )
                    """.trimIndent())

                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS transactions (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            type TEXT NOT NULL,
                            amountPaisa INTEGER NOT NULL,
                            fromAccountId INTEGER,
                            toAccountId INTEGER,
                            categoryId INTEGER,
                            note TEXT NOT NULL,
                            receiptPath TEXT,
                            recurringOccurrenceId INTEGER,
                            debtId INTEGER,
                            dateEpochMs INTEGER NOT NULL,
                            createdAtEpochMs INTEGER NOT NULL
                        )
                    """.trimIndent())

                    // Insert representative v4 rows
                    db.execSQL("INSERT INTO accounts (id, name, type, openingBalancePaisa, colorHex, isArchived, createdAtEpochMs) VALUES (1, 'Cash Wallet', 'CASH_WALLET', 500000, '#2A2826', 0, 1000)")
                    db.execSQL("INSERT INTO debts (id, personName, direction, originalPrincipalPaisa, dateEpochMs, description, phoneNumber, notes, status, isInterestEnabled, interestRatePercent, createdAtEpochMs, updatedAtEpochMs) VALUES (1, 'Ahmed', 'I_GAVE', 100000, 2000, 'Loan', '03001234567', '', 'ACTIVE', 0, 0.0, 2000, 2000)")
                    db.execSQL("INSERT INTO transactions (id, type, amountPaisa, fromAccountId, toAccountId, note, dateEpochMs, createdAtEpochMs) VALUES (1, 'EXPENSE', 25000, 1, NULL, 'Chai', 3000, 3000)")
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        val db = helper.writableDatabase

        // Execute MIGRATION_4_5
        MIGRATION_4_5.migrate(db)

        // 1. Verify debts columns added and existing row has defaults
        val debtCursor = db.query("SELECT personName, kind, itemDescription FROM debts WHERE id = 1")
        assertTrue(debtCursor.moveToFirst())
        assertEquals("Ahmed", debtCursor.getString(0))
        assertEquals("MONEY", debtCursor.getString(1))
        assertEquals("", debtCursor.getString(2))
        debtCursor.close()

        // 2. Verify transactions columns added
        val txCursor = db.query("SELECT id, amountPaisa, sourceType FROM transactions WHERE id = 1")
        assertTrue(txCursor.moveToFirst())
        assertEquals(25000L, txCursor.getLong(1))
        assertTrue(txCursor.isNull(2))
        txCursor.close()

        // 3. Verify new tables exist and can be queried
        val kametiCursor = db.query("SELECT COUNT(*) FROM kameti")
        assertTrue(kametiCursor.moveToFirst())
        assertEquals(0, kametiCursor.getInt(0))
        kametiCursor.close()

        val budgetCursor = db.query("SELECT COUNT(*) FROM category_budget")
        assertTrue(budgetCursor.moveToFirst())
        assertEquals(0, budgetCursor.getInt(0))
        budgetCursor.close()

        val zakatCursor = db.query("SELECT COUNT(*) FROM zakat_calculation")
        assertTrue(zakatCursor.moveToFirst())
        assertEquals(0, zakatCursor.getInt(0))
        zakatCursor.close()

        db.close()
    }
}
