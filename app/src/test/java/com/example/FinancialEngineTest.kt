package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.HisaabDao
import com.example.data.database.HisaabDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.CategoryEntity
import com.example.data.model.CategoryType
import com.example.data.model.NeedEntity
import com.example.data.model.OccurrenceStatus
import com.example.data.model.RecurringFrequency
import com.example.data.model.RecurringOccurrenceEntity
import com.example.data.model.RecurringRuleEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.domain.Money
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FinancialEngineTest {

    private lateinit var db: HisaabDatabase
    private lateinit var dao: HisaabDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, HisaabDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.dao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun scenarioG_freshInstallation_zeroBalancesAndNoTransactions() = runBlocking {
        // Fresh state
        val total = dao.getTotalBalanceFlow().first() ?: 0L
        assertEquals(0L, total)
        assertEquals(0, dao.getTransactionsCount())
        assertEquals(0, dao.getNeedsCount())
    }

    @Test
    fun scenarioA_expenseReducesAccountAndUpdatesLedger() = runBlocking {
        // Account with 1000.00 PKR (100,000 paisa)
        val cashId = dao.insertAccount(
            AccountEntity(
                name = "Cash",
                type = AccountType.CASH_WALLET,
                openingBalancePaisa = 100000L,
                colorHex = "#7C9A6B"
            )
        )
        val foodCatId = dao.insertCategory(
            CategoryEntity(
                name = "Food",
                type = CategoryType.EXPENSE,
                iconKey = "restaurant",
                colorHex = "#E07A5F"
            )
        )

        // Initial cash balance
        assertEquals(100000L, dao.getAccountBalanceFlow(cashId).first())

        // Record 250.00 PKR (25,000 paisa) expense
        val now = System.currentTimeMillis()
        dao.insertTransaction(
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amountPaisa = 25000L,
                fromAccountId = cashId,
                categoryId = foodCatId,
                note = "Lunch",
                dateEpochMs = now
            )
        )

        // Cash balance must now be exactly 750.00 PKR (75,000 paisa)
        val updatedCashBalance = dao.getAccountBalanceFlow(cashId).first()
        assertEquals(75000L, updatedCashBalance)

        // Total balance reflects the expense
        val total = dao.getTotalBalanceFlow().first()
        assertEquals(75000L, total)

        // Period expense reflects the expense
        val periodExpense = dao.getPeriodExpenseFlow(now - 1000, now + 1000).first()
        assertEquals(25000L, periodExpense)
    }

    @Test
    fun scenarioB_incomeIncreasesAccountAndPeriodIncome() = runBlocking {
        val bankId = dao.insertAccount(
            AccountEntity(
                name = "Bank",
                type = AccountType.BANK,
                openingBalancePaisa = 0L,
                colorHex = "#5B8A8A"
            )
        )
        val salaryCatId = dao.insertCategory(
            CategoryEntity(
                name = "Salary",
                type = CategoryType.INCOME,
                iconKey = "payments",
                colorHex = "#6BA88A"
            )
        )

        val now = System.currentTimeMillis()
        // Record 50,000.00 PKR (5,000,000 paisa)
        dao.insertTransaction(
            TransactionEntity(
                type = TransactionType.INCOME,
                amountPaisa = 5000000L,
                toAccountId = bankId,
                categoryId = salaryCatId,
                note = "Monthly Salary",
                dateEpochMs = now
            )
        )

        assertEquals(5000000L, dao.getAccountBalanceFlow(bankId).first())
        assertEquals(5000000L, dao.getTotalBalanceFlow().first())
        assertEquals(5000000L, dao.getPeriodIncomeFlow(now - 1000, now + 1000).first())
    }

    @Test
    fun scenarioC_transferAdjustsAccountsWithoutAlteringTotalBalance() = runBlocking {
        val bankId = dao.insertAccount(
            AccountEntity(name = "Bank", type = AccountType.BANK, openingBalancePaisa = 1000000L, colorHex = "#5B8A8A")
        )
        val cashId = dao.insertAccount(
            AccountEntity(name = "Cash", type = AccountType.CASH_WALLET, openingBalancePaisa = 200000L, colorHex = "#7C9A6B")
        )

        // Combined initial: 12,000.00 PKR (1,200,000 paisa)
        assertEquals(1200000L, dao.getTotalBalanceFlow().first())

        // Transfer 5,000.00 PKR (500,000 paisa) from Bank to Cash
        val now = System.currentTimeMillis()
        dao.insertTransaction(
            TransactionEntity(
                type = TransactionType.TRANSFER,
                amountPaisa = 500000L,
                fromAccountId = bankId,
                toAccountId = cashId,
                dateEpochMs = now
            )
        )

        // Bank should decrease to 5,000.00 PKR (500,000 paisa)
        assertEquals(500000L, dao.getAccountBalanceFlow(bankId).first())

        // Cash should increase to 7,000.00 PKR (700,000 paisa)
        assertEquals(700000L, dao.getAccountBalanceFlow(cashId).first())

        // Total available balance across all accounts MUST remain exactly 12,000.00 PKR!
        assertEquals(1200000L, dao.getTotalBalanceFlow().first())

        // Transfer must NOT appear in Income or Expense totals
        val periodIncome = dao.getPeriodIncomeFlow(now - 1000, now + 1000).first() ?: 0L
        val periodExpense = dao.getPeriodExpenseFlow(now - 1000, now + 1000).first() ?: 0L
        assertEquals(0L, periodIncome)
        assertEquals(0L, periodExpense)
    }

    @Test
    fun scenarioD_wishlistNeverAffectsLedgerBalances() = runBlocking {
        val cashId = dao.insertAccount(
            AccountEntity(name = "Cash", type = AccountType.CASH_WALLET, openingBalancePaisa = 500000L, colorHex = "#7C9A6B")
        )

        // Add a planned shopping item with 3,500.00 PKR expected price
        dao.insertNeed(
            NeedEntity(
                name = "Noise Cancelling Headphones",
                expectedPricePaisa = 350000L,
                whereToGet = "Daraz"
            )
        )

        // Cash and total balance must remain completely untouched!
        assertEquals(500000L, dao.getAccountBalanceFlow(cashId).first())
        assertEquals(500000L, dao.getTotalBalanceFlow().first())
        assertEquals(0, dao.getTransactionsCount())
    }

    @Test
    fun scenarioE_recurringPaymentRequiresConfirmationBeforeAffectingBalance() = runBlocking {
        val bankId = dao.insertAccount(
            AccountEntity(name = "Bank", type = AccountType.BANK, openingBalancePaisa = 10000000L, colorHex = "#5B8A8A")
        )
        val rentCatId = dao.insertCategory(
            CategoryEntity(name = "Rent", type = CategoryType.EXPENSE, iconKey = "home", colorHex = "#9B5DE5")
        )

        val ruleId = dao.insertRecurringRule(
            RecurringRuleEntity(
                title = "Monthly House Rent",
                type = TransactionType.EXPENSE,
                amountPaisa = 2000000L, // 20,000.00 PKR
                accountId = bankId,
                categoryId = rentCatId,
                frequency = RecurringFrequency.MONTHLY,
                startDateEpochMs = System.currentTimeMillis(),
                nextDueEpochMs = System.currentTimeMillis()
            )
        )

        // Pending occurrence created
        val occId = dao.insertOccurrence(
            RecurringOccurrenceEntity(
                ruleId = ruleId,
                occurrenceDateEpochMs = System.currentTimeMillis(),
                status = OccurrenceStatus.PENDING
            )
        )

        // Balance MUST NOT be deducted yet!
        assertEquals(10000000L, dao.getAccountBalanceFlow(bankId).first())
        assertEquals(0, dao.getTransactionsCount())

        // User explicitly confirms:
        val txId = dao.insertTransaction(
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amountPaisa = 2000000L,
                fromAccountId = bankId,
                categoryId = rentCatId,
                note = "Monthly House Rent",
                recurringOccurrenceId = occId
            )
        )
        dao.updateOccurrence(
            RecurringOccurrenceEntity(
                id = occId,
                ruleId = ruleId,
                occurrenceDateEpochMs = System.currentTimeMillis(),
                status = OccurrenceStatus.CONFIRMED,
                transactionId = txId
            )
        )

        // Balance now reflects exactly ONE confirmed expense
        assertEquals(8000000L, dao.getAccountBalanceFlow(bankId).first())
        assertEquals(1, dao.getTransactionsCount())
    }

    @Test
    fun scenarioKarz_lendingReducesAccountBalanceAndRepaymentsRestoreIt() = runBlocking {
        // Initial Cash Wallet: PKR 30,000 (3,000,000 paisa)
        val cashWalletId = dao.insertAccount(
            AccountEntity(
                name = "Cash Wallet",
                type = AccountType.CASH_WALLET,
                openingBalancePaisa = 3000000L,
                colorHex = "#7C9A6B"
            )
        )
        assertEquals(3000000L, dao.getAccountBalanceFlow(cashWalletId).first())

        // 1. Lent PKR 10,000 (1,000,000 paisa) to Ahmed
        val debtId = dao.insertDebt(
            com.example.data.model.DebtEntity(
                personName = "Ahmed",
                direction = com.example.data.model.DebtDirection.I_GAVE,
                originalPrincipalPaisa = 1000000L,
                linkedAccountId = cashWalletId
            )
        )
        dao.insertTransaction(
            TransactionEntity(
                type = TransactionType.DEBT_LENT,
                amountPaisa = 1000000L,
                fromAccountId = cashWalletId,
                note = "Karz lent to Ahmed",
                debtId = debtId,
                dateEpochMs = System.currentTimeMillis()
            )
        )
        dao.insertDebtEvent(
            com.example.data.model.DebtEventEntity(
                debtId = debtId,
                type = com.example.data.model.DebtEventType.ORIGINAL_ADVANCE,
                amountPaisa = 1000000L,
                accountId = cashWalletId
            )
        )

        // Cash Wallet after lending must be PKR 20,000 (2,000,000 paisa)
        assertEquals(2000000L, dao.getAccountBalanceFlow(cashWalletId).first())

        // 2. Partial repayment 1: PKR 3,000 (300,000 paisa) received
        dao.insertTransaction(
            TransactionEntity(
                type = TransactionType.DEBT_REPAYMENT_IN,
                amountPaisa = 300000L,
                toAccountId = cashWalletId,
                note = "Repayment from Ahmed",
                debtId = debtId,
                dateEpochMs = System.currentTimeMillis()
            )
        )
        dao.insertDebtEvent(
            com.example.data.model.DebtEventEntity(
                debtId = debtId,
                type = com.example.data.model.DebtEventType.REPAYMENT,
                amountPaisa = 300000L,
                accountId = cashWalletId
            )
        )

        // Cash Wallet increases to PKR 23,000 (2,300,000 paisa)
        assertEquals(2300000L, dao.getAccountBalanceFlow(cashWalletId).first())

        // Check debt outstanding
        val events = dao.getDebtEventsForDebt(debtId)
        val totalRepaid = events.filter { it.type == com.example.data.model.DebtEventType.REPAYMENT }.sumOf { it.amountPaisa }
        assertEquals(300000L, totalRepaid)
        val outstanding = 1000000L - totalRepaid
        assertEquals(700000L, outstanding) // PKR 7,000 remaining

        // 3. Final repayment: PKR 7,000 (700,000 paisa) received
        dao.insertTransaction(
            TransactionEntity(
                type = TransactionType.DEBT_REPAYMENT_IN,
                amountPaisa = 700000L,
                toAccountId = cashWalletId,
                note = "Final repayment from Ahmed",
                debtId = debtId,
                dateEpochMs = System.currentTimeMillis()
            )
        )
        dao.insertDebtEvent(
            com.example.data.model.DebtEventEntity(
                debtId = debtId,
                type = com.example.data.model.DebtEventType.REPAYMENT,
                amountPaisa = 700000L,
                accountId = cashWalletId
            )
        )

        // Cash Wallet is fully restored to original PKR 30,000 (3,000,000 paisa)
        assertEquals(3000000L, dao.getAccountBalanceFlow(cashWalletId).first())
        val finalEvents = dao.getDebtEventsForDebt(debtId)
        val finalRepaid = finalEvents.filter { it.type == com.example.data.model.DebtEventType.REPAYMENT }.sumOf { it.amountPaisa }
        assertEquals(1000000L, finalRepaid)
        assertEquals(0L, 1000000L - finalRepaid) // Fully settled
    }

    @Test
    fun scenarioKarz_borrowingIncreasesAccountBalanceAndRepaymentDeductsIt() = runBlocking {
        // Initial Cash Wallet: PKR 5,000 (500,000 paisa)
        val cashWalletId = dao.insertAccount(
            AccountEntity(
                name = "Cash Wallet",
                type = AccountType.CASH_WALLET,
                openingBalancePaisa = 500000L,
                colorHex = "#7C9A6B"
            )
        )
        assertEquals(500000L, dao.getAccountBalanceFlow(cashWalletId).first())

        // Borrowed PKR 20,000 (2,000,000 paisa) from Ali into Cash Wallet
        val debtId = dao.insertDebt(
            com.example.data.model.DebtEntity(
                personName = "Ali",
                direction = com.example.data.model.DebtDirection.I_TOOK,
                originalPrincipalPaisa = 2000000L,
                linkedAccountId = cashWalletId
            )
        )
        dao.insertTransaction(
            TransactionEntity(
                type = TransactionType.DEBT_BORROWED,
                amountPaisa = 2000000L,
                toAccountId = cashWalletId,
                note = "Karz borrowed from Ali",
                debtId = debtId,
                dateEpochMs = System.currentTimeMillis()
            )
        )

        // Cash Wallet after borrowing must be PKR 25,000 (2,500,000 paisa)
        assertEquals(2500000L, dao.getAccountBalanceFlow(cashWalletId).first())

        // Record repayment of PKR 5,000 (500,000 paisa) paid back to Ali
        dao.insertTransaction(
            TransactionEntity(
                type = TransactionType.DEBT_REPAYMENT_OUT,
                amountPaisa = 500000L,
                fromAccountId = cashWalletId,
                note = "Repayment paid to Ali",
                debtId = debtId,
                dateEpochMs = System.currentTimeMillis()
            )
        )

        // Cash Wallet decreases back to PKR 20,000 (2,000,000 paisa)
        assertEquals(2000000L, dao.getAccountBalanceFlow(cashWalletId).first())
    }

    @Test
    fun scenarioKarz_historicalDebtDoesNotChangeAccountBalance() = runBlocking {
        val cashWalletId = dao.insertAccount(
            AccountEntity(
                name = "Cash Wallet",
                type = AccountType.CASH_WALLET,
                openingBalancePaisa = 5000000L, // 50,000 PKR
                colorHex = "#7C9A6B"
            )
        )
        assertEquals(5000000L, dao.getAccountBalanceFlow(cashWalletId).first())

        // Insert historical debt without transactions
        val debtId = dao.insertDebt(
            com.example.data.model.DebtEntity(
                personName = "Old Friend",
                direction = com.example.data.model.DebtDirection.I_GAVE,
                originalPrincipalPaisa = 2500000L, // 25,000 PKR
                linkedAccountId = null // No account linked
            )
        )
        dao.insertDebtEvent(
            com.example.data.model.DebtEventEntity(
                debtId = debtId,
                type = com.example.data.model.DebtEventType.ORIGINAL_ADVANCE,
                amountPaisa = 2500000L,
                accountId = null
            )
        )

        // Account balance MUST remain untouched!
        assertEquals(5000000L, dao.getAccountBalanceFlow(cashWalletId).first())
        assertEquals(0, dao.getTransactionsCount())
    }
}

