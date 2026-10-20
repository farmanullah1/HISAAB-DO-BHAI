package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.CategoryType
import com.example.data.model.DebtDirection
import com.example.data.model.DebtEntity
import com.example.data.model.DebtEventEntity
import com.example.data.model.DebtStatus
import com.example.data.model.NeedEntity
import com.example.data.model.OccurrenceStatus
import com.example.data.model.ReceiptAttachmentEntity
import com.example.data.model.RecurringOccurrenceEntity
import com.example.data.model.RecurringRuleEntity
import com.example.data.model.ReservedAmountEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

data class AccountBalanceRecord(
    val accountId: Long,
    val balancePaisa: Long
)

data class CategorySpentRecord(
    val categoryId: Long,
    val spentPaisa: Long
)

@Dao
interface HisaabDao {

    // --- Accounts ---
    @Query("SELECT * FROM accounts ORDER BY isArchived ASC, id ASC")
    fun getAllAccountsFlow(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE isArchived = 0 ORDER BY id ASC")
    fun getActiveAccountsFlow(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY id ASC")
    suspend fun getAllAccountsList(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Query("SELECT * FROM accounts WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getAccountByName(name: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccountById(id: Long)

    @Query("""
        SELECT COALESCE(openingBalancePaisa, 0) +
          COALESCE((SELECT SUM(amountPaisa) FROM transactions WHERE type IN ('INCOME', 'DEBT_BORROWED', 'DEBT_REPAYMENT_IN') AND toAccountId = :id), 0) -
          COALESCE((SELECT SUM(amountPaisa) FROM transactions WHERE type IN ('EXPENSE', 'DEBT_LENT', 'DEBT_REPAYMENT_OUT') AND fromAccountId = :id), 0) +
          COALESCE((SELECT SUM(amountPaisa) FROM transactions WHERE type = 'TRANSFER' AND toAccountId = :id), 0) -
          COALESCE((SELECT SUM(amountPaisa) FROM transactions WHERE type = 'TRANSFER' AND fromAccountId = :id), 0)
        FROM accounts WHERE id = :id
    """)
    fun getAccountBalanceFlow(id: Long): Flow<Long?>

    @Query("""
        SELECT 
          a.id AS accountId,
          COALESCE(a.openingBalancePaisa, 0) +
          COALESCE((SELECT SUM(t1.amountPaisa) FROM transactions t1 WHERE t1.type IN ('INCOME', 'DEBT_BORROWED', 'DEBT_REPAYMENT_IN') AND t1.toAccountId = a.id), 0) -
          COALESCE((SELECT SUM(t2.amountPaisa) FROM transactions t2 WHERE t2.type IN ('EXPENSE', 'DEBT_LENT', 'DEBT_REPAYMENT_OUT') AND t2.fromAccountId = a.id), 0) +
          COALESCE((SELECT SUM(t3.amountPaisa) FROM transactions t3 WHERE t3.type = 'TRANSFER' AND t3.toAccountId = a.id), 0) -
          COALESCE((SELECT SUM(t4.amountPaisa) FROM transactions t4 WHERE t4.type = 'TRANSFER' AND t4.fromAccountId = a.id), 0) AS balancePaisa
        FROM accounts a
    """)
    fun getAllAccountBalancesFlow(): Flow<List<AccountBalanceRecord>>

    @Query("""
        SELECT 
          COALESCE((SELECT SUM(openingBalancePaisa) FROM accounts WHERE isArchived = 0), 0) +
          COALESCE((SELECT SUM(amountPaisa) FROM transactions WHERE type IN ('INCOME', 'DEBT_BORROWED', 'DEBT_REPAYMENT_IN') AND toAccountId IN (SELECT id FROM accounts WHERE isArchived = 0)), 0) -
          COALESCE((SELECT SUM(amountPaisa) FROM transactions WHERE type IN ('EXPENSE', 'DEBT_LENT', 'DEBT_REPAYMENT_OUT') AND fromAccountId IN (SELECT id FROM accounts WHERE isArchived = 0)), 0)
    """)
    fun getTotalBalanceFlow(): Flow<Long?>

    // --- Reserved Money / Remaining Balance ---
    @Query("SELECT * FROM reserved_amounts ORDER BY id DESC")
    fun getAllReservedAmountsFlow(): Flow<List<ReservedAmountEntity>>

    @Query("SELECT * FROM reserved_amounts WHERE isActive = 1 ORDER BY id DESC")
    fun getActiveReservedAmountsFlow(): Flow<List<ReservedAmountEntity>>

    @Query("SELECT COALESCE(SUM(amountPaisa), 0) FROM reserved_amounts WHERE isActive = 1")
    fun getActiveReservedTotalFlow(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservedAmount(reserved: ReservedAmountEntity): Long

    @Update
    suspend fun updateReservedAmount(reserved: ReservedAmountEntity)

    @Delete
    suspend fun deleteReservedAmount(reserved: ReservedAmountEntity)

    // --- Categories ---
    @Query("SELECT * FROM categories WHERE type = :type ORDER BY id ASC")
    fun getCategoriesByTypeFlow(type: CategoryType): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY type ASC, id ASC")
    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    // Category Spending for Budgets
    @Query("""
        SELECT categoryId, COALESCE(SUM(amountPaisa), 0) AS spentPaisa
        FROM transactions
        WHERE type = 'EXPENSE' AND categoryId IS NOT NULL AND dateEpochMs >= :startEpochMs AND dateEpochMs <= :endEpochMs
        GROUP BY categoryId
    """)
    fun getCategorySpendingFlow(startEpochMs: Long, endEpochMs: Long): Flow<List<CategorySpentRecord>>

    // --- Transactions ---
    @Query("SELECT * FROM transactions ORDER BY dateEpochMs DESC, id DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    // Migration transaction re-pointing
    @Query("UPDATE transactions SET fromAccountId = :newAccountId WHERE fromAccountId = :oldAccountId")
    suspend fun repointTransactionsFromAccount(oldAccountId: Long, newAccountId: Long)

    @Query("UPDATE transactions SET toAccountId = :newAccountId WHERE toAccountId = :oldAccountId")
    suspend fun repointTransactionsToAccount(oldAccountId: Long, newAccountId: Long)

    @Query("DELETE FROM transactions WHERE type = 'TRANSFER' AND fromAccountId = :accountId AND toAccountId = :accountId")
    suspend fun removeSelfTransfers(accountId: Long)

    @Query("UPDATE recurring_rules SET accountId = :newAccountId WHERE accountId = :oldAccountId")
    suspend fun repointRecurringRulesAccount(oldAccountId: Long, newAccountId: Long)

    @Query("""
        SELECT COALESCE(SUM(amountPaisa), 0) 
        FROM transactions 
        WHERE type = 'INCOME' AND dateEpochMs >= :startEpochMs AND dateEpochMs <= :endEpochMs
    """)
    fun getPeriodIncomeFlow(startEpochMs: Long, endEpochMs: Long): Flow<Long?>

    @Query("""
        SELECT COALESCE(SUM(amountPaisa), 0) 
        FROM transactions 
        WHERE type = 'EXPENSE' AND dateEpochMs >= :startEpochMs AND dateEpochMs <= :endEpochMs
    """)
    fun getPeriodExpenseFlow(startEpochMs: Long, endEpochMs: Long): Flow<Long?>

    // --- Needs / Shopping Planner ---
    @Query("SELECT * FROM needs ORDER BY id ASC")
    fun getAllNeedsFlow(): Flow<List<NeedEntity>>

    @Query("SELECT * FROM needs WHERE isDone = 0 ORDER BY id DESC")
    fun getActiveNeedsFlow(): Flow<List<NeedEntity>>

    @Query("SELECT * FROM needs WHERE isDone = 1 ORDER BY doneAtEpochMs DESC, id DESC")
    fun getDoneNeedsFlow(): Flow<List<NeedEntity>>

    @Query("SELECT * FROM needs WHERE id = :id")
    suspend fun getNeedById(id: Long): NeedEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNeed(need: NeedEntity): Long

    @Update
    suspend fun updateNeed(need: NeedEntity)

    @Delete
    suspend fun deleteNeed(need: NeedEntity)

    @Query("DELETE FROM needs WHERE isDone = 1")
    suspend fun clearAllDoneNeeds()

    // --- Receipt Attachments ---
    @Query("SELECT * FROM receipt_attachments WHERE transactionId = :transactionId LIMIT 1")
    fun getReceiptForTransactionFlow(transactionId: Long): Flow<ReceiptAttachmentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(attachment: ReceiptAttachmentEntity): Long

    @Delete
    suspend fun deleteReceipt(attachment: ReceiptAttachmentEntity)

    // --- Recurring Rules & Occurrences ---
    @Query("SELECT * FROM recurring_rules ORDER BY id DESC")
    fun getAllRecurringRulesFlow(): Flow<List<RecurringRuleEntity>>

    @Query("SELECT * FROM recurring_rules WHERE isActive = 1")
    suspend fun getActiveRecurringRules(): List<RecurringRuleEntity>

    @Query("SELECT * FROM recurring_rules WHERE id = :id")
    suspend fun getRecurringRuleById(id: Long): RecurringRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringRule(rule: RecurringRuleEntity): Long

    @Update
    suspend fun updateRecurringRule(rule: RecurringRuleEntity)

    @Delete
    suspend fun deleteRecurringRule(rule: RecurringRuleEntity)

    @Query("SELECT * FROM recurring_occurrences WHERE status = 'PENDING' ORDER BY occurrenceDateEpochMs ASC")
    fun getPendingOccurrencesFlow(): Flow<List<RecurringOccurrenceEntity>>

    @Query("SELECT * FROM recurring_occurrences WHERE ruleId = :ruleId AND occurrenceDateEpochMs = :dateEpochMs LIMIT 1")
    suspend fun getOccurrenceByRuleAndDate(ruleId: Long, dateEpochMs: Long): RecurringOccurrenceEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOccurrence(occurrence: RecurringOccurrenceEntity): Long

    @Update
    suspend fun updateOccurrence(occurrence: RecurringOccurrenceEntity)

    // Counts for integrity check
    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getAccountsCount(): Int

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoriesCount(): Int

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionsCount(): Int

    // --- Karz (Debts & Loans) ---
    @Query("SELECT * FROM debts ORDER BY updatedAtEpochMs DESC, id DESC")
    fun getAllDebtsFlow(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE direction = :direction ORDER BY updatedAtEpochMs DESC, id DESC")
    fun getDebtsByDirectionFlow(direction: DebtDirection): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE id = :id")
    suspend fun getDebtById(id: Long): DebtEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Delete
    suspend fun deleteDebt(debt: DebtEntity)

    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun deleteDebtById(id: Long)

    @Query("SELECT * FROM debt_events ORDER BY dateEpochMs ASC, id ASC")
    fun getAllDebtEventsFlow(): Flow<List<DebtEventEntity>>

    @Query("SELECT * FROM debt_events WHERE debtId = :debtId ORDER BY dateEpochMs ASC, id ASC")
    fun getDebtEventsForDebtFlow(debtId: Long): Flow<List<DebtEventEntity>>

    @Query("SELECT * FROM debt_events WHERE debtId = :debtId ORDER BY dateEpochMs ASC, id ASC")
    suspend fun getDebtEventsForDebt(debtId: Long): List<DebtEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtEvent(event: DebtEventEntity): Long

    @Update
    suspend fun updateDebtEvent(event: DebtEventEntity)

    @Delete
    suspend fun deleteDebtEvent(event: DebtEventEntity)

    @Query("DELETE FROM debt_events WHERE debtId = :debtId")
    suspend fun deleteDebtEventsForDebt(debtId: Long)

    @Query("SELECT COUNT(*) FROM debts")
    suspend fun getDebtsCount(): Int

    @Query("SELECT COUNT(*) FROM debt_events")
    suspend fun getDebtEventsCount(): Int

    @Query("SELECT COUNT(*) FROM needs")
    suspend fun getNeedsCount(): Int

    @Query("SELECT COUNT(*) FROM recurring_rules")
    suspend fun getRecurringRulesCount(): Int

    // --- Kameti (Committee) ---
    @Query("SELECT * FROM kameti WHERE archived = 0 ORDER BY id DESC")
    fun getActiveKametisFlow(): Flow<List<com.example.data.model.KametiEntity>>

    @Query("SELECT * FROM kameti ORDER BY id DESC")
    fun getAllKametisFlow(): Flow<List<com.example.data.model.KametiEntity>>

    @Query("SELECT * FROM kameti WHERE id = :id")
    suspend fun getKametiById(id: Long): com.example.data.model.KametiEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKameti(kameti: com.example.data.model.KametiEntity): Long

    @Update
    suspend fun updateKameti(kameti: com.example.data.model.KametiEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContributions(contributions: List<com.example.data.model.KametiContributionEntity>)

    @Query("SELECT * FROM kameti_contribution WHERE kametiId = :kametiId ORDER BY installmentIndex ASC")
    fun getContributionsForKametiFlow(kametiId: Long): Flow<List<com.example.data.model.KametiContributionEntity>>

    @Query("SELECT * FROM kameti_contribution WHERE id = :id")
    suspend fun getContributionById(id: Long): com.example.data.model.KametiContributionEntity?

    @Update
    suspend fun updateContribution(contribution: com.example.data.model.KametiContributionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayout(payout: com.example.data.model.KametiPayoutEntity): Long

    @Query("SELECT * FROM kameti_payout WHERE kametiId = :kametiId LIMIT 1")
    fun getPayoutForKametiFlow(kametiId: Long): Flow<com.example.data.model.KametiPayoutEntity?>

    @Query("SELECT * FROM kameti_payout WHERE kametiId = :kametiId LIMIT 1")
    suspend fun getPayoutForKameti(kametiId: Long): com.example.data.model.KametiPayoutEntity?

    @Update
    suspend fun updatePayout(payout: com.example.data.model.KametiPayoutEntity)

    // --- Karz Installments ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstallments(installments: List<com.example.data.model.KarzInstallmentEntity>)

    @Query("SELECT * FROM karz_installment WHERE debtId = :debtId ORDER BY number ASC")
    fun getInstallmentsForDebtFlow(debtId: Long): Flow<List<com.example.data.model.KarzInstallmentEntity>>

    @Query("SELECT * FROM karz_installment WHERE dueDateEpochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY dueDateEpochDay ASC")
    fun getInstallmentsDueBetweenFlow(startEpochDay: Long, endEpochDay: Long): Flow<List<com.example.data.model.KarzInstallmentEntity>>

    @Update
    suspend fun updateInstallment(installment: com.example.data.model.KarzInstallmentEntity)

    // --- Charity & Zakat ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharityRecord(record: com.example.data.model.CharityRecordEntity): Long

    @Query("SELECT * FROM charity_record ORDER BY id DESC")
    fun getAllCharityRecordsFlow(): Flow<List<com.example.data.model.CharityRecordEntity>>

    @Update
    suspend fun updateCharityRecord(record: com.example.data.model.CharityRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZakatCalculation(calculation: com.example.data.model.ZakatCalculationEntity): Long

    @Query("SELECT * FROM zakat_calculation ORDER BY createdAtEpochMs DESC")
    fun getAllZakatCalculationsFlow(): Flow<List<com.example.data.model.ZakatCalculationEntity>>

    // --- Category Budgets ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryBudget(budget: com.example.data.model.CategoryBudgetEntity): Long

    @Query("SELECT * FROM category_budget ORDER BY id ASC")
    fun getAllCategoryBudgetsFlow(): Flow<List<com.example.data.model.CategoryBudgetEntity>>

    @Query("SELECT * FROM category_budget WHERE categoryId = :categoryId AND (monthKey = :monthKey OR monthKey = '*') LIMIT 1")
    suspend fun getBudgetForCategory(categoryId: Long, monthKey: String): com.example.data.model.CategoryBudgetEntity?

    // --- Audit Log ---
    @Insert
    suspend fun insertAuditLog(log: com.example.data.model.AuditLogEntity): Long

    @Query("SELECT * FROM audit_log ORDER BY createdAtEpochMs DESC LIMIT :limit")
    fun getRecentAuditLogsFlow(limit: Int = 100): Flow<List<com.example.data.model.AuditLogEntity>>

    // --- User Reminders ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: com.example.data.model.UserReminderEntity): Long

    @Query("SELECT * FROM user_reminder WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY dateEpochDay ASC")
    fun getRemindersDueBetweenFlow(startEpochDay: Long, endEpochDay: Long): Flow<List<com.example.data.model.UserReminderEntity>>

    @Update
    suspend fun updateReminder(reminder: com.example.data.model.UserReminderEntity)

    // --- Import Fingerprints ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFingerprint(fp: com.example.data.model.ImportFingerprintEntity): Long

    @Query("SELECT COUNT(*) > 0 FROM import_fingerprint WHERE fingerprint = :fingerprint")
    suspend fun hasFingerprint(fingerprint: String): Boolean
}

