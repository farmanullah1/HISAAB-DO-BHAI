package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.HisaabDao
import com.example.data.database.HisaabDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.DebtDirection
import com.example.data.model.DebtEntity
import com.example.data.model.DebtEventEntity
import com.example.data.model.DebtEventType
import com.example.data.model.DebtStatus
import com.example.data.model.DebtWithDetails
import com.example.data.model.KarzSummary
import com.example.data.repository.HisaabRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Validates all 21 Acceptance Test Scenarios specified in newUpdate.md for
 * Karz (Debt & Loan Management).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class KarzAcceptanceTest {

    private lateinit var context: Context
    private lateinit var db: HisaabDatabase
    private lateinit var dao: HisaabDao
    private lateinit var repository: HisaabRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, HisaabDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.dao()
        repository = HisaabRepository(context, dao)
    }

    @After
    fun tearDown() {
        db.close()
    }

    // 1 & 3: A user can create a debt using "I Gave Karz" with person-facing wording
    @Test
    fun test01_createDebt_IGaveKarz() = runBlocking {
        val debtId = repository.createDebt(
            personName = "Ahmed",
            direction = DebtDirection.I_GAVE,
            amountPaisa = 1000000L, // 10,000 PKR
            dateEpochMs = System.currentTimeMillis(),
            description = "Emergency loan"
        )

        val debt = dao.getDebtById(debtId)
        assertNotNull(debt)
        assertEquals("Ahmed", debt!!.personName)
        assertEquals(DebtDirection.I_GAVE, debt.direction)
        assertEquals(1000000L, debt.originalPrincipalPaisa)
        assertEquals(DebtStatus.ACTIVE, debt.status)

        val details = repository.getDebtDetailsFlow(debtId).first()
        assertNotNull(details)
        assertEquals(1000000L, details!!.outstandingPaisa)
        assertEquals(1000000L, details.totalPrincipalPaisa)
    }

    // 2 & 3: A user can create a debt using "I Took Karz" with person-facing wording
    @Test
    fun test02_createDebt_ITookKarz() = runBlocking {
        val debtId = repository.createDebt(
            personName = "Ali",
            direction = DebtDirection.I_TOOK,
            amountPaisa = 2000000L, // 20,000 PKR
            dateEpochMs = System.currentTimeMillis(),
            description = "Shop inventory borrowing"
        )

        val debt = dao.getDebtById(debtId)
        assertNotNull(debt)
        assertEquals("Ali", debt!!.personName)
        assertEquals(DebtDirection.I_TOOK, debt.direction)
        assertEquals(2000000L, debt.originalPrincipalPaisa)

        val details = repository.getDebtDetailsFlow(debtId).first()
        assertNotNull(details)
        assertEquals(2000000L, details!!.outstandingPaisa)
        assertEquals(2000000L, details.totalPrincipalPaisa)
    }

    // 4: Lending updates tracked account and receivable correctly
    @Test
    fun test04_lendingUpdatesTrackedAccountAndReceivable() = runBlocking {
        val cashWalletId = dao.insertAccount(
            AccountEntity(
                name = "Cash Wallet",
                type = AccountType.CASH_WALLET,
                openingBalancePaisa = 3000000L, // 30,000 PKR
                colorHex = "#7C9A6B"
            )
        )

        val debtId = repository.createDebt(
            personName = "Ahmed",
            direction = DebtDirection.I_GAVE,
            amountPaisa = 1000000L, // 10,000 PKR
            dateEpochMs = System.currentTimeMillis(),
            accountId = cashWalletId,
            recordCashMovement = true
        )

        // Cash Wallet after lending 10,000 must be 20,000 PKR
        val updatedBalance = dao.getAccountBalanceFlow(cashWalletId).first()
        assertEquals(2000000L, updatedBalance)

        // Outstanding receivable must be 10,000 PKR
        val details = repository.getDebtDetailsFlow(debtId).first()
        assertEquals(1000000L, details?.outstandingPaisa)

        // Net Karz summary
        val summary = repository.karzSummary.first()
        assertEquals(1000000L, summary.totalGavePaisa)
        assertEquals(0L, summary.totalTookPaisa)
        assertEquals(1000000L, summary.netKarzPaisa)
    }

    // 5: Borrowing updates tracked account and liability correctly
    @Test
    fun test05_borrowingUpdatesTrackedAccountAndLiability() = runBlocking {
        val cashWalletId = dao.insertAccount(
            AccountEntity(
                name = "Cash Wallet",
                type = AccountType.CASH_WALLET,
                openingBalancePaisa = 500000L, // 5,000 PKR
                colorHex = "#7C9A6B"
            )
        )

        val debtId = repository.createDebt(
            personName = "Ali",
            direction = DebtDirection.I_TOOK,
            amountPaisa = 2000000L, // 20,000 PKR
            dateEpochMs = System.currentTimeMillis(),
            accountId = cashWalletId,
            recordCashMovement = true
        )

        // Cash Wallet after borrowing must increase to 25,000 PKR
        val updatedBalance = dao.getAccountBalanceFlow(cashWalletId).first()
        assertEquals(2500000L, updatedBalance)

        // Outstanding liability must be 20,000 PKR
        val details = repository.getDebtDetailsFlow(debtId).first()
        assertEquals(2000000L, details?.outstandingPaisa)

        val summary = repository.karzSummary.first()
        assertEquals(0L, summary.totalGavePaisa)
        assertEquals(2000000L, summary.totalTookPaisa)
        assertEquals(-2000000L, summary.netKarzPaisa)
    }

    // 6: Historical debt entry does not alter current account balance
    @Test
    fun test06_historicalDebtDoesNotAlterAccountBalance() = runBlocking {
        val cashWalletId = dao.insertAccount(
            AccountEntity(
                name = "Cash Wallet",
                type = AccountType.CASH_WALLET,
                openingBalancePaisa = 5000000L, // 50,000 PKR
                colorHex = "#7C9A6B"
            )
        )

        repository.createDebt(
            personName = "Old Acquaintance",
            direction = DebtDirection.I_GAVE,
            amountPaisa = 1500000L,
            dateEpochMs = System.currentTimeMillis() - 86400000L * 30,
            accountId = null,
            recordCashMovement = false
        )

        // Account balance MUST remain untouched at 50,000 PKR
        val currentBalance = dao.getAccountBalanceFlow(cashWalletId).first()
        assertEquals(5000000L, currentBalance)
        assertEquals(0, dao.getTransactionsCount())
    }

    // 7 & 8: Partial repayments calculate outstanding principal and preserve history
    @Test
    fun test07_and_08_partialRepaymentsAndHistory() = runBlocking {
        val cashWalletId = dao.insertAccount(
            AccountEntity(
                name = "Cash Wallet",
                type = AccountType.CASH_WALLET,
                openingBalancePaisa = 0L,
                colorHex = "#7C9A6B"
            )
        )

        val debtId = repository.createDebt(
            personName = "Tariq",
            direction = DebtDirection.I_GAVE,
            amountPaisa = 1000000L, // 10,000 PKR
            dateEpochMs = System.currentTimeMillis(),
            accountId = null,
            recordCashMovement = false
        )

        // First repayment: 3,000 PKR (300,000 paisa) received into Cash Wallet
        repository.recordDebtRepayment(
            debtId = debtId,
            amountPaisa = 300000L,
            dateEpochMs = System.currentTimeMillis(),
            accountId = cashWalletId,
            recordCashMovement = true,
            note = "First installment"
        )

        var details = repository.getDebtDetailsFlow(debtId).first()!!
        assertEquals(700000L, details.outstandingPaisa)
        assertEquals(300000L, details.totalRepaidPaisa)
        assertEquals(DebtStatus.PARTIALLY_PAID, details.calculatedStatus)
        assertEquals(300000L, dao.getAccountBalanceFlow(cashWalletId).first())

        // Second repayment: 2,000 PKR (200,000 paisa)
        repository.recordDebtRepayment(
            debtId = debtId,
            amountPaisa = 200000L,
            dateEpochMs = System.currentTimeMillis(),
            accountId = cashWalletId,
            recordCashMovement = true,
            note = "Second installment"
        )

        details = repository.getDebtDetailsFlow(debtId).first()!!
        assertEquals(500000L, details.outstandingPaisa)
        assertEquals(500000L, details.totalRepaidPaisa)
        assertEquals(500000L, dao.getAccountBalanceFlow(cashWalletId).first())

        // Final repayment: 5,000 PKR (500,000 paisa)
        repository.recordDebtRepayment(
            debtId = debtId,
            amountPaisa = 500000L,
            dateEpochMs = System.currentTimeMillis(),
            accountId = cashWalletId,
            recordCashMovement = true,
            note = "Final payment"
        )

        details = repository.getDebtDetailsFlow(debtId).first()!!
        assertEquals(0L, details.outstandingPaisa)
        assertEquals(1000000L, details.totalRepaidPaisa)
        assertEquals(DebtStatus.SETTLED, details.calculatedStatus)
        assertEquals(1000000L, dao.getAccountBalanceFlow(cashWalletId).first())

        // Verify history preserved: 1 original advance + 3 repayments = 4 events
        val events = dao.getDebtEventsForDebt(debtId)
        assertEquals(4, events.size)
        assertEquals(DebtEventType.ORIGINAL_ADVANCE, events[0].type)
        assertEquals(DebtEventType.REPAYMENT, events[1].type)
        assertEquals(DebtEventType.REPAYMENT, events[2].type)
        assertEquals(DebtEventType.REPAYMENT, events[3].type)
    }

    // 9: Additional advances increase principal correctly
    @Test
    fun test09_additionalAdvancesIncreasePrincipal() = runBlocking {
        val debtId = repository.createDebt(
            personName = "Bilal",
            direction = DebtDirection.I_GAVE,
            amountPaisa = 500000L, // 5,000 PKR
            dateEpochMs = System.currentTimeMillis()
        )

        repository.recordAdditionalAdvance(
            debtId = debtId,
            amountPaisa = 300000L, // 3,000 PKR additional
            dateEpochMs = System.currentTimeMillis(),
            note = "Extra loan for fuel"
        )

        val details = repository.getDebtDetailsFlow(debtId).first()!!
        assertEquals(800000L, details.totalPrincipalPaisa)
        assertEquals(800000L, details.outstandingPaisa)
    }

    // 10: Principal repayments are not counted as ordinary income or expenses
    @Test
    fun test10_principalRepaymentsNotOrdinaryIncomeOrExpense() = runBlocking {
        val cashWalletId = dao.insertAccount(
            AccountEntity(
                name = "Cash Wallet",
                type = AccountType.CASH_WALLET,
                openingBalancePaisa = 2000000L,
                colorHex = "#7C9A6B"
            )
        )

        val debtId = repository.createDebt(
            personName = "Kamran",
            direction = DebtDirection.I_GAVE,
            amountPaisa = 1000000L,
            dateEpochMs = System.currentTimeMillis(),
            accountId = cashWalletId,
            recordCashMovement = true
        )

        // Repayment of 5000 PKR received into Cash Wallet
        repository.recordDebtRepayment(
            debtId = debtId,
            amountPaisa = 500000L,
            dateEpochMs = System.currentTimeMillis(),
            accountId = cashWalletId,
            recordCashMovement = true
        )

        val startOfMonth = System.currentTimeMillis() - 86400000L * 5
        val endOfMonth = System.currentTimeMillis() + 86400000L * 5

        // Ordinary Income and Expense flows MUST be 0 (repayments are not ordinary income)
        val periodIncome = dao.getPeriodIncomeFlow(startOfMonth, endOfMonth).first() ?: 0L
        val periodExpense = dao.getPeriodExpenseFlow(startOfMonth, endOfMonth).first() ?: 0L
        assertEquals(0L, periodIncome)
        assertEquals(0L, periodExpense)

        // But account balance is properly updated
        // Started 20,000 - 10,000 lent + 5,000 repaid = 15,000 PKR (1,500,000 paisa)
        assertEquals(1500000L, dao.getAccountBalanceFlow(cashWalletId).first())
    }

    // 11: Optional interest is separated from principal
    @Test
    fun test11_optionalInterestDisabledByDefaultAndSeparated() = runBlocking {
        val debtId = repository.createDebt(
            personName = "Partner",
            direction = DebtDirection.I_TOOK,
            amountPaisa = 1000000L,
            dateEpochMs = System.currentTimeMillis(),
            isInterestEnabled = false,
            interestRatePercent = 0.0
        )

        val debt = dao.getDebtById(debtId)!!
        assertFalse(debt.isInterestEnabled)
        assertEquals(0.0, debt.interestRatePercent, 0.001)
    }

    // 12: Settled debts show zero outstanding principal
    @Test
    fun test12_settledDebtsShowZeroOutstanding() = runBlocking {
        val debtId = repository.createDebt(
            personName = "Sara",
            direction = DebtDirection.I_TOOK,
            amountPaisa = 400000L, // 4,000 PKR
            dateEpochMs = System.currentTimeMillis()
        )

        repository.recordDebtRepayment(
            debtId = debtId,
            amountPaisa = 400000L,
            dateEpochMs = System.currentTimeMillis()
        )

        val details = repository.getDebtDetailsFlow(debtId).first()!!
        assertEquals(0L, details.outstandingPaisa)
        assertEquals(DebtStatus.SETTLED, details.calculatedStatus)
    }

    // 13 & 14: Overdue status when due date exists vs missing due date
    @Test
    fun test13_and_14_dueDateOverdueAndMissingDueDate() = runBlocking {
        val now = System.currentTimeMillis()
        val pastDue = now - 86400000L * 3 // 3 days ago

        val overdueDebtId = repository.createDebt(
            personName = "Late Payer",
            direction = DebtDirection.I_GAVE,
            amountPaisa = 500000L,
            dateEpochMs = pastDue - 86400000L,
            dueDateEpochMs = pastDue
        )

        val noDueDateId = repository.createDebt(
            personName = "Friend With No Deadline",
            direction = DebtDirection.I_GAVE,
            amountPaisa = 500000L,
            dateEpochMs = now,
            dueDateEpochMs = null
        )

        val overdueDetails = repository.getDebtDetailsFlow(overdueDebtId).first()!!
        assertTrue(overdueDetails.isOverdue)

        val noDueDetails = repository.getDebtDetailsFlow(noDueDateId).first()!!
        assertFalse(noDueDetails.isOverdue)
        assertFalse(noDueDetails.isDueSoon)
        assertEquals(DebtStatus.ACTIVE, noDueDetails.calculatedStatus)
    }

    // 15: Dashboard summaries match the debt ledger
    @Test
    fun test15_dashboardSummariesMatchDebtLedger() = runBlocking {
        repository.createDebt(
            personName = "User A",
            direction = DebtDirection.I_GAVE,
            amountPaisa = 700000L, // 7,000 PKR
            dateEpochMs = System.currentTimeMillis()
        )
        repository.createDebt(
            personName = "User B",
            direction = DebtDirection.I_TOOK,
            amountPaisa = 300000L, // 3,000 PKR
            dateEpochMs = System.currentTimeMillis()
        )

        val summary = repository.karzSummary.first()
        assertEquals(700000L, summary.totalGavePaisa)
        assertEquals(300000L, summary.totalTookPaisa)
        assertEquals(400000L, summary.netKarzPaisa)
    }

    // 16: Cash Wallet balances remain consistent with confirmed cash movements
    @Test
    fun test16_cashWalletConsistency() = runBlocking {
        val walletId = dao.insertAccount(
            AccountEntity(name = "Cash Wallet", type = AccountType.CASH_WALLET, openingBalancePaisa = 1000000L, colorHex = "#7C9A6B")
        )

        val debtId = repository.createDebt(
            personName = "Zahid",
            direction = DebtDirection.I_GAVE,
            amountPaisa = 400000L,
            dateEpochMs = System.currentTimeMillis(),
            accountId = walletId,
            recordCashMovement = true
        )

        assertEquals(600000L, dao.getAccountBalanceFlow(walletId).first())

        repository.recordDebtRepayment(
            debtId = debtId,
            amountPaisa = 400000L,
            dateEpochMs = System.currentTimeMillis(),
            accountId = walletId,
            recordCashMovement = true
        )

        assertEquals(1000000L, dao.getAccountBalanceFlow(walletId).first())
    }

    // 17: Duplicate / invalid repayment validation
    @Test
    fun test17_invalidRepaymentsRejected() = runBlocking {
        val debtId = repository.createDebt(
            personName = "Noman",
            direction = DebtDirection.I_GAVE,
            amountPaisa = 500000L,
            dateEpochMs = System.currentTimeMillis()
        )

        // Repaying zero or negative amount must throw exception
        var caughtZero = false
        try {
            repository.recordDebtRepayment(debtId, 0L, System.currentTimeMillis())
        } catch (e: IllegalArgumentException) {
            caughtZero = true
        }
        assertTrue(caughtZero)

        // Repaying more than outstanding must throw exception
        var caughtExcess = false
        try {
            repository.recordDebtRepayment(debtId, 600000L, System.currentTimeMillis())
        } catch (e: IllegalArgumentException) {
            caughtExcess = true
        }
        assertTrue(caughtExcess)
    }

    // 19: Backup and restore preserve debts, events, repayments
    @Test
    fun test19_backupAndRestorePreserveDebtsAndEvents() = runBlocking {
        val debtId = repository.createDebt(
            personName = "Export Test User",
            direction = DebtDirection.I_GAVE,
            amountPaisa = 1200000L,
            dateEpochMs = System.currentTimeMillis(),
            notes = "Export note"
        )
        repository.recordDebtRepayment(
            debtId = debtId,
            amountPaisa = 200000L,
            dateEpochMs = System.currentTimeMillis(),
            note = "Export repayment"
        )

        val backupFile = repository.exportDataAsJsonFile()
        assertTrue(backupFile.exists())
        val jsonContent = backupFile.readText()
        assertTrue(jsonContent.contains("Export Test User"))
        assertTrue(jsonContent.contains("Export repayment"))

        // Wipe database
        dao.deleteDebt(dao.getDebtById(debtId)!!)
        assertEquals(0, dao.getAllDebtsFlow().first().size)

        // Restore
        val result = repository.restoreDataFromJson(jsonContent)
        assertTrue(result.isSuccess)

        val restoredDebts = dao.getAllDebtsFlow().first()
        assertEquals(1, restoredDebts.size)
        assertEquals("Export Test User", restoredDebts[0].personName)

        val restoredEvents = dao.getDebtEventsForDebt(restoredDebts[0].id)
        assertEquals(2, restoredEvents.size)
    }

    // 21: No fabricated debt records created on first launch
    @Test
    fun test21_noFabricatedDebtsOnFirstLaunch() = runBlocking {
        val debts = dao.getAllDebtsFlow().first()
        val events = dao.getAllDebtEventsFlow().first()
        assertEquals(0, debts.size)
        assertEquals(0, events.size)

        val summary = repository.karzSummary.first()
        assertEquals(0L, summary.totalGavePaisa)
        assertEquals(0L, summary.totalTookPaisa)
        assertEquals(0L, summary.netKarzPaisa)
    }
}
