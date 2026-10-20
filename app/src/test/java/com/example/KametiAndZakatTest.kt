package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.HisaabDao
import com.example.data.database.HisaabDatabase
import com.example.data.model.AccountType
import com.example.data.model.CharityCategory
import com.example.data.model.ContributionStatus
import com.example.data.model.DebtDirection
import com.example.data.model.DebtKind
import com.example.data.model.InstallmentStatus
import com.example.data.model.KametiCadence
import com.example.data.model.PayoutStatus
import com.example.data.model.ZakatCalculationEntity
import com.example.data.repository.HisaabRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class KametiAndZakatTest {

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
    fun teardown() {
        db.close()
    }

    @Test
    fun testKameti_creation_and_contributions_schedule() = runBlocking {
        val todayEpochDay = LocalDate.of(2026, 10, 1).toEpochDay()
        val kametiId = repository.createKameti(
            name = "Office BC",
            memberCount = 5,
            contributionPaisa = 10_000_00L, // 10,000 PKR
            cadence = KametiCadence.MONTHLY,
            startDateEpochDay = todayEpochDay,
            totalInstallments = 5,
            myPayoutInstallment = 3,
            expectedPayoutPaisa = 50_000_00L,
            note = "5 members monthly"
        )

        val kameti = repository.getKametiById(kametiId)
        assertNotNull(kameti)
        assertEquals("Office BC", kameti!!.name)
        assertEquals(5, kameti.memberCount)

        // Verify 5 contribution rows generated
        val contributions = repository.getContributionsForKametiFlow(kametiId).first()
        assertEquals(5, contributions.size)
        assertEquals(1, contributions[0].installmentIndex)
        assertEquals(10_000_00L, contributions[0].expectedPaisa)
        assertEquals(ContributionStatus.PENDING, contributions[0].status)

        // Verify payout row
        val payout = repository.getPayoutForKametiFlow(kametiId).first()
        assertNotNull(payout)
        assertEquals(50_000_00L, payout!!.expectedPaisa)
        assertEquals(PayoutStatus.EXPECTED, payout.status)
    }

    @Test
    fun testKameti_recordContribution_and_claimPayout() = runBlocking {
        val accId = repository.addAccount("Cash Wallet", AccountType.CASH_WALLET, 100_000_00L, "#2A2826")
        val startDay = LocalDate.of(2026, 10, 1).toEpochDay()
        val kametiId = repository.createKameti(
            name = "Neighborhood BC",
            memberCount = 4,
            contributionPaisa = 5_000_00L,
            cadence = KametiCadence.MONTHLY,
            startDateEpochDay = startDay,
            totalInstallments = 4,
            myPayoutInstallment = 2,
            expectedPayoutPaisa = 20_000_00L,
            note = ""
        )

        val contributions = repository.getContributionsForKametiFlow(kametiId).first()
        val firstContribution = contributions.first()

        // Pay installment 1
        repository.recordKametiContribution(
            contributionId = firstContribution.id,
            accountId = accId,
            amountPaisa = 5_000_00L,
            dateEpochDay = startDay,
            note = "October installment"
        )

        // Verify contribution updated
        val updatedContributions = repository.getContributionsForKametiFlow(kametiId).first()
        assertEquals(ContributionStatus.PAID, updatedContributions[0].status)
        assertEquals(5_000_00L, updatedContributions[0].paidPaisa)

        // Verify account balance decreased by 5,000 PKR
        val postBalance = dao.getAccountBalanceFlow(accId).first() ?: 0L
        assertEquals(95_000_00L, postBalance)

        // Claim payout of 20,000 PKR
        val payout = repository.getPayoutForKametiFlow(kametiId).first()!!
        repository.claimKametiPayout(
            payoutId = payout.id,
            kametiId = kametiId,
            accountId = accId,
            receivedAmountPaisa = 20_000_00L,
            dateEpochDay = startDay + 30,
            note = "Payout collected"
        )

        // Verify payout updated to RECEIVED
        val updatedPayout = repository.getPayoutForKametiFlow(kametiId).first()!!
        assertEquals(PayoutStatus.RECEIVED, updatedPayout.status)
        assertEquals(20_000_00L, updatedPayout.receivedPaisa)

        // Verify balance increased by 20,000 PKR -> 115,000 PKR
        val finalBalance = dao.getAccountBalanceFlow(accId).first() ?: 0L
        assertEquals(115_000_00L, finalBalance)
    }

    @Test
    fun testKarzInstallments_split_and_repayment() = runBlocking {
        val debtId = repository.createDebt(
            personName = "Kashif",
            direction = DebtDirection.I_GAVE,
            amountPaisa = 15_000_00L, // 15,000 PKR
            dateEpochMs = System.currentTimeMillis(),
            kind = DebtKind.MONEY
        )

        val startDay = LocalDate.of(2026, 11, 1).toEpochDay()
        repository.splitDebtIntoInstallments(
            debtId = debtId,
            count = 3,
            isWeekly = false,
            firstDueDateEpochDay = startDay
        )

        val installments = repository.getInstallmentsForDebtFlow(debtId).first()
        assertEquals(3, installments.size)
        assertEquals(5_000_00L, installments[0].scheduledPaisa)
        assertEquals(5_000_00L, installments[1].scheduledPaisa)
        assertEquals(5_000_00L, installments[2].scheduledPaisa)
        assertEquals(InstallmentStatus.SCHEDULED, installments[0].status)

        // Pay installment 1
        repository.recordKarzInstallmentPayment(
            installment = installments[0],
            amountPaisa = 5_000_00L,
            accountId = null,
            recordCashMovement = false,
            dateEpochMs = System.currentTimeMillis(),
            note = "First installment"
        )

        val postInstallments = repository.getInstallmentsForDebtFlow(debtId).first()
        assertEquals(InstallmentStatus.PAID, postInstallments[0].status)
        assertEquals(5_000_00L, postInstallments[0].paidPaisa)
    }

    @Test
    fun testZakat_and_charity_records() = runBlocking {
        val snapshot = ZakatCalculationEntity(
            createdAtEpochMs = System.currentTimeMillis(),
            label = "Ramadan 1448",
            standard = "SILVER",
            weightMgUsed = 612_360L,
            ratePerGramPaisa = 300_00L, // PKR 300/g
            rateEnteredDateEpochDay = LocalDate.of(2026, 10, 1).toEpochDay(),
            nisabThresholdPaisa = 183_708_00L,
            eligibleWealthPaisa = 500_000_00L,
            zakatPayablePaisa = 12_500_00L,
            nisabReached = true,
            hawlConfirmedByUser = true,
            assumptionsJson = "{}",
            note = "Verified by user"
        )
        val zakatId = repository.saveZakatCalculation(snapshot)
        assertTrue(zakatId > 0)

        val records = repository.getAllZakatCalculationsFlow().first()
        assertEquals(1, records.size)
        assertEquals("Ramadan 1448", records[0].label)

        // Record charity payment
        val charityId = repository.recordCharityPayment(
            category = CharityCategory.ZAKAT,
            recipientName = "Edhi Foundation",
            amountPaisa = 12_500_00L,
            dateEpochDay = LocalDate.of(2026, 10, 5).toEpochDay(),
            accountId = null,
            recordCashMovement = false,
            note = "Full annual Zakat disbursement"
        )
        assertTrue(charityId > 0)

        val charityList = repository.getAllCharityRecordsFlow().first()
        assertEquals(1, charityList.size)
        assertEquals("Edhi Foundation", charityList[0].recipientName)
        assertEquals(12_500_00L, charityList[0].amountPaisa)
    }
}
