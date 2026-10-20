package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.HisaabDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.DebtDirection
import com.example.data.model.DebtKind
import com.example.data.repository.HisaabRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class KarzMoneyVsItemTest {

    private lateinit var db: HisaabDatabase
    private lateinit var repository: HisaabRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, HisaabDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = HisaabRepository(context, db.dao())
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun itemDebt_doesNotChangeAccountBalance_Invariant_I4() = runBlocking {
        // 1. Create Cash Wallet with opening balance ₨ 5,000.00 (500,000 paisa)
        val accId = repository.addAccount("Cash Wallet", AccountType.CASH_WALLET, 500_000L, "#2A2826")
        val initialBalance = db.dao().getAccountBalanceFlow(accId).first() ?: 0L
        assertEquals(500_000L, initialBalance)

        // 2. Create Item/Service Debt (e.g. borrowed 2 sacks of wheat valued at ₨ 1,200.00)
        val debtId = repository.createDebt(
            personName = "Rashid",
            direction = DebtDirection.I_TOOK,
            amountPaisa = 120_000L,
            dateEpochMs = System.currentTimeMillis(),
            kind = DebtKind.ITEM_SERVICE,
            itemDescription = "Wheat sacks",
            itemQuantity = 2.0,
            itemUnit = "sacks",
            estimatedValuePaisa = 120_000L,
            accountId = accId,
            recordCashMovement = true // Even if user requested cash movement, Item debts MUST ignore it
        )

        // 3. Verify debt record created with ITEM_SERVICE kind
        val debt = db.dao().getDebtById(debtId)
        assertNotNull(debt)
        assertEquals(DebtKind.ITEM_SERVICE, debt!!.kind)
        assertEquals("Wheat sacks", debt.itemDescription)

        // 4. Invariant I-4: Account balance MUST remain EXACTLY ₨ 5,000.00 (500,000 paisa)
        val postBalance = db.dao().getAccountBalanceFlow(accId).first() ?: 0L
        assertEquals(500_000L, postBalance)
    }

    @Test
    fun moneyDebt_withCashMovement_updatesAccountBalance_Invariant_I5() = runBlocking {
        // 1. Create Cash Wallet with opening balance ₨ 5,000.00 (500,000 paisa)
        val accId = repository.addAccount("Cash Wallet", AccountType.CASH_WALLET, 500_000L, "#2A2826")

        // 2. Lent ₨ 1,000.00 (100,000 paisa) from Cash Wallet
        val debtId = repository.createDebt(
            personName = "Tariq",
            direction = DebtDirection.I_GAVE,
            amountPaisa = 100_000L,
            dateEpochMs = System.currentTimeMillis(),
            kind = DebtKind.MONEY,
            accountId = accId,
            recordCashMovement = true
        )

        // 3. Balance must decrease to ₨ 4,000.00 (400,000 paisa)
        val postBalance = db.dao().getAccountBalanceFlow(accId).first() ?: 0L
        assertEquals(400_000L, postBalance)
    }
}
