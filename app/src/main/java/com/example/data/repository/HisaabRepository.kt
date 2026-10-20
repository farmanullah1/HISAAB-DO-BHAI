package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.data.database.HisaabDao
import com.example.data.database.HisaabDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.AccountWithBalance
import com.example.data.model.BudgetProgressItem
import com.example.data.model.CategoryEntity
import com.example.data.model.CategoryType
import com.example.data.model.DebtDirection
import com.example.data.model.DebtEntity
import com.example.data.model.DebtEventEntity
import com.example.data.model.DebtEventType
import com.example.data.model.DebtStatus
import com.example.data.model.DebtWithDetails
import com.example.data.model.KarzSummary
import com.example.data.model.MonthlySummary
import com.example.data.model.NeedEntity
import com.example.data.model.OccurrenceStatus
import com.example.data.model.PendingRecurringItem
import com.example.data.model.ReceiptAttachmentEntity
import com.example.data.model.RecurringFrequency
import com.example.data.model.RecurringOccurrenceEntity
import com.example.data.model.RecurringRuleEntity
import com.example.data.model.ReservedAmountEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.util.Calendar
import com.example.core.money.InstallmentSplitter
import com.example.data.model.AuditLogEntity
import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.CharityCategory
import com.example.data.model.CharityRecordEntity
import com.example.data.model.CharityStatus
import com.example.data.model.ContributionStatus
import com.example.data.model.InstallmentStatus
import com.example.data.model.KametiCadence
import com.example.data.model.KametiContributionEntity
import com.example.data.model.KametiEntity
import com.example.data.model.KametiPayoutEntity
import com.example.data.model.KametiStatus
import com.example.data.model.KarzInstallmentEntity
import com.example.data.model.PayoutStatus
import com.example.data.model.ZakatCalculationEntity

class HisaabRepository(
    private val context: Context,
    private val dao: HisaabDao
) {

    private val prefs = context.getSharedPreferences("hisaab_prefs", Context.MODE_PRIVATE)

    private val _privacyMode = MutableStateFlow(prefs.getBoolean("privacy_mode", false))
    val privacyMode: Flow<Boolean> = _privacyMode

    fun setPrivacyMode(enabled: Boolean) {
        prefs.edit().putBoolean("privacy_mode", enabled).apply()
        _privacyMode.value = enabled
    }

    suspend fun seedInitialDataIfNeeded() = withContext(Dispatchers.IO) {
        val alreadySeeded = prefs.getBoolean("hisaab_seeded_v2", false)
        if (alreadySeeded) {
            checkAndGenerateRecurringOccurrences()
            return@withContext
        }

        // Perform safe migration if legacy accounts (Cash & Wallet) exist
        migrateCashAndWalletIfNeeded()

        // Fresh installation: Exactly one default account named "Cash Wallet"
        if (dao.getAccountsCount() == 0) {
            val defaultAccount = AccountEntity(
                name = "Cash Wallet",
                type = AccountType.CASH_WALLET,
                openingBalancePaisa = 0L,
                colorHex = "#7C9A6B"
            )
            dao.insertAccount(defaultAccount)
        }

        if (dao.getCategoriesCount() == 0) {
            val defaultCategories = listOf(
                CategoryEntity(name = "Food & Dining", type = CategoryType.EXPENSE, iconKey = "restaurant", colorHex = "#E07A5F"),
                CategoryEntity(name = "Groceries", type = CategoryType.EXPENSE, iconKey = "shopping_cart", colorHex = "#81B29A"),
                CategoryEntity(name = "Fuel & Transport", type = CategoryType.EXPENSE, iconKey = "directions_car", colorHex = "#F2CC8F"),
                CategoryEntity(name = "Bills & Utilities", type = CategoryType.EXPENSE, iconKey = "receipt_long", colorHex = "#3D5A80"),
                CategoryEntity(name = "Rent & Housing", type = CategoryType.EXPENSE, iconKey = "home", colorHex = "#9B5DE5"),
                CategoryEntity(name = "Healthcare", type = CategoryType.EXPENSE, iconKey = "medical_services", colorHex = "#E76F51"),
                CategoryEntity(name = "Shopping", type = CategoryType.EXPENSE, iconKey = "shopping_bag", colorHex = "#F4A261"),
                CategoryEntity(name = "Education", type = CategoryType.EXPENSE, iconKey = "school", colorHex = "#2A9D8F"),
                CategoryEntity(name = "Other Expense", type = CategoryType.EXPENSE, iconKey = "more_horiz", colorHex = "#8A8F99"),
                CategoryEntity(name = "Salary", type = CategoryType.INCOME, iconKey = "payments", colorHex = "#6BA88A"),
                CategoryEntity(name = "Freelance", type = CategoryType.INCOME, iconKey = "laptop", colorHex = "#457B9D"),
                CategoryEntity(name = "Gift", type = CategoryType.INCOME, iconKey = "redeem", colorHex = "#E9C46A"),
                CategoryEntity(name = "Refund", type = CategoryType.INCOME, iconKey = "replay", colorHex = "#A8DADC"),
                CategoryEntity(name = "Other Income", type = CategoryType.INCOME, iconKey = "more_horiz", colorHex = "#8A8F99")
            )
            dao.insertCategories(defaultCategories)
        }

        prefs.edit().putBoolean("hisaab_seeded_v2", true).apply()
        checkAndGenerateRecurringOccurrences()
    }

    /**
     * Idempotent migration to safely merge separate "Cash" and "Wallet" into "Cash Wallet".
     */
    suspend fun migrateCashAndWalletIfNeeded() = withContext(Dispatchers.IO) {
        val accounts = dao.getAllAccountsList()
        val cashAcc = accounts.firstOrNull { it.name.equals("Cash", ignoreCase = true) }
        val walletAcc = accounts.firstOrNull { it.name.equals("Wallet", ignoreCase = true) }

        if (cashAcc != null && walletAcc != null) {
            // Merge Wallet into Cash, and rename Cash to "Cash Wallet"
            val mergedOpeningPaisa = cashAcc.openingBalancePaisa + walletAcc.openingBalancePaisa
            val unifiedCashWallet = cashAcc.copy(
                name = "Cash Wallet",
                type = AccountType.CASH_WALLET,
                openingBalancePaisa = mergedOpeningPaisa
            )
            dao.updateAccount(unifiedCashWallet)

            // Re-point transactions
            dao.repointTransactionsFromAccount(walletAcc.id, unifiedCashWallet.id)
            dao.repointTransactionsToAccount(walletAcc.id, unifiedCashWallet.id)
            dao.removeSelfTransfers(unifiedCashWallet.id)
            dao.repointRecurringRulesAccount(walletAcc.id, unifiedCashWallet.id)

            // Delete redundant wallet account
            dao.deleteAccountById(walletAcc.id)
        } else if (cashAcc != null && !cashAcc.name.equals("Cash Wallet", ignoreCase = true)) {
            // Rename standalone "Cash" to "Cash Wallet"
            dao.updateAccount(cashAcc.copy(name = "Cash Wallet", type = AccountType.CASH_WALLET))
        } else if (walletAcc != null && !walletAcc.name.equals("Cash Wallet", ignoreCase = true)) {
            // Rename standalone "Wallet" to "Cash Wallet"
            dao.updateAccount(walletAcc.copy(name = "Cash Wallet", type = AccountType.CASH_WALLET))
        }
    }

    val totalBalancePaisa: Flow<Long> = dao.getTotalBalanceFlow().map { it ?: 0L }

    val activeReservedTotalPaisa: Flow<Long> = dao.getActiveReservedTotalFlow().map { it ?: 0L }

    val remainingBalancePaisa: Flow<Long> = combine(
        totalBalancePaisa,
        activeReservedTotalPaisa
    ) { total, reserved ->
        total - reserved
    }.flowOn(Dispatchers.IO)

    val reservedAmounts: Flow<List<ReservedAmountEntity>> = dao.getAllReservedAmountsFlow()

    val accountsWithBalances: Flow<List<AccountWithBalance>> = combine(
        dao.getActiveAccountsFlow(),
        dao.getAllAccountBalancesFlow()
    ) { accounts, balances ->
        val balanceMap = balances.associate { it.accountId to it.balancePaisa }
        accounts.map { account ->
            AccountWithBalance(
                account = account,
                balancePaisa = balanceMap[account.id] ?: account.openingBalancePaisa
            )
        }
    }.flowOn(Dispatchers.IO)

    val allAccounts: Flow<List<AccountEntity>> = dao.getAllAccountsFlow()

    fun getCategoriesByType(type: CategoryType): Flow<List<CategoryEntity>> =
        dao.getCategoriesByTypeFlow(type)

    val allCategories: Flow<List<CategoryEntity>> = dao.getAllCategoriesFlow()

    val transactionsWithDetails: Flow<List<TransactionItem>> = combine(
        dao.getAllTransactionsFlow(),
        dao.getAllCategoriesFlow(),
        dao.getAllAccountsFlow()
    ) { transactions, categories, accounts ->
        val catMap = categories.associateBy { it.id }
        val accMap = accounts.associateBy { it.id }

        transactions.map { tx ->
            val cat = tx.categoryId?.let { catMap[it] }
            val fromAcc = tx.fromAccountId?.let { accMap[it] }
            val toAcc = tx.toAccountId?.let { accMap[it] }

            TransactionItem(
                transaction = tx,
                categoryName = cat?.name,
                categoryIconKey = cat?.iconKey,
                categoryColorHex = cat?.colorHex,
                fromAccountName = fromAcc?.name,
                toAccountName = toAcc?.name
            )
        }
    }.flowOn(Dispatchers.IO)

    val monthlySummary: Flow<MonthlySummary> = run {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, -30)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startEpoch = calendar.timeInMillis

        combine(
            dao.getPeriodIncomeFlow(startEpoch, now),
            dao.getPeriodExpenseFlow(startEpoch, now)
        ) { income, expense ->
            MonthlySummary(
                incomePaisa = income ?: 0L,
                expensePaisa = expense ?: 0L
            )
        }.flowOn(Dispatchers.IO)
    }

    val activeNeeds: Flow<List<NeedEntity>> = dao.getActiveNeedsFlow()
    val doneNeeds: Flow<List<NeedEntity>> = dao.getDoneNeedsFlow()

    // --- Budgets ---
    val budgetProgressList: Flow<List<BudgetProgressItem>> = run {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfMonth = cal.timeInMillis
        val now = System.currentTimeMillis()

        combine(
            dao.getCategoriesByTypeFlow(CategoryType.EXPENSE),
            dao.getCategorySpendingFlow(startOfMonth, now)
        ) { categories, spendings ->
            val spentMap = spendings.associate { it.categoryId to it.spentPaisa }
            categories.filter { it.monthlyLimitPaisa != null && it.monthlyLimitPaisa > 0L }.map { cat ->
                val spent = spentMap[cat.id] ?: 0L
                BudgetProgressItem(
                    category = cat,
                    spentPaisa = spent,
                    limitPaisa = cat.monthlyLimitPaisa ?: 0L
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    // --- Recurring Rules & Occurrences ---
    val recurringRules: Flow<List<RecurringRuleEntity>> = dao.getAllRecurringRulesFlow()

    val pendingRecurringItems: Flow<List<PendingRecurringItem>> = combine(
        dao.getPendingOccurrencesFlow(),
        dao.getAllRecurringRulesFlow(),
        dao.getAllCategoriesFlow(),
        dao.getAllAccountsFlow()
    ) { occurrences, rules, categories, accounts ->
        val ruleMap = rules.associateBy { it.id }
        val catMap = categories.associateBy { it.id }
        val accMap = accounts.associateBy { it.id }

        occurrences.mapNotNull { occ ->
            val rule = ruleMap[occ.ruleId] ?: return@mapNotNull null
            val cat = catMap[rule.categoryId]
            val acc = accMap[rule.accountId]
            PendingRecurringItem(
                occurrence = occ,
                rule = rule,
                categoryName = cat?.name,
                categoryIconKey = cat?.iconKey,
                accountName = acc?.name
            )
        }
    }.flowOn(Dispatchers.IO)

    // --- Karz (Debt & Loan Management) Flows ---
    val allDebtsWithDetails: Flow<List<DebtWithDetails>> = combine(
        dao.getAllDebtsFlow(),
        dao.getAllDebtEventsFlow(),
        dao.getAllAccountsFlow()
    ) { debts, events, accounts ->
        val eventsMap = events.groupBy { it.debtId }
        val accountsMap = accounts.associateBy { it.id }
        debts.map { debt ->
            val debtEvents = eventsMap[debt.id] ?: emptyList()
            val accName = debt.linkedAccountId?.let { accountsMap[it]?.name }
            DebtWithDetails(
                debt = debt,
                events = debtEvents,
                linkedAccountName = accName
            )
        }
    }.flowOn(Dispatchers.IO)

    val karzSummary: Flow<KarzSummary> = allDebtsWithDetails.map { debtsWithDetails ->
        var totalGave = 0L
        var totalTook = 0L
        var dueSoonCount = 0
        var dueSoonPaisa = 0L
        var overdueCount = 0
        var overduePaisa = 0L

        for (item in debtsWithDetails) {
            val remaining = item.outstandingPaisa
            if (item.debt.direction == DebtDirection.I_GAVE) {
                totalGave += remaining
            } else {
                totalTook += remaining
            }

            if (item.isDueSoon) {
                dueSoonCount++
                dueSoonPaisa += remaining
            }
            if (item.isOverdue) {
                overdueCount++
                overduePaisa += remaining
            }
        }

        KarzSummary(
            totalGavePaisa = totalGave,
            totalTookPaisa = totalTook,
            dueSoonCount = dueSoonCount,
            dueSoonPaisa = dueSoonPaisa,
            overdueCount = overdueCount,
            overduePaisa = overduePaisa
        )
    }.flowOn(Dispatchers.IO)

    fun getDebtDetailsFlow(debtId: Long): Flow<DebtWithDetails?> = combine(
        dao.getAllDebtsFlow(),
        dao.getDebtEventsForDebtFlow(debtId),
        dao.getAllAccountsFlow()
    ) { debts, events, accounts ->
        val debt = debts.find { it.id == debtId } ?: return@combine null
        val accName = debt.linkedAccountId?.let { id -> accounts.find { it.id == id }?.name }
        DebtWithDetails(
            debt = debt,
            events = events,
            linkedAccountName = accName
        )
    }.flowOn(Dispatchers.IO)

    suspend fun checkAndGenerateRecurringOccurrences() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val activeRules = dao.getActiveRecurringRules()
        for (rule in activeRules) {
            if (rule.nextDueEpochMs <= now) {
                val existing = dao.getOccurrenceByRuleAndDate(rule.id, rule.nextDueEpochMs)
                if (existing == null) {
                    val occ = RecurringOccurrenceEntity(
                        ruleId = rule.id,
                        occurrenceDateEpochMs = rule.nextDueEpochMs,
                        status = OccurrenceStatus.PENDING
                    )
                    dao.insertOccurrence(occ)
                }
            }
        }
    }

    suspend fun confirmRecurringPayment(occurrenceId: Long): Long = withContext(Dispatchers.IO) {
        val occurrences = HisaabDatabase.getInstance(context).query(
            "SELECT * FROM recurring_occurrences WHERE id = ?",
            arrayOf(occurrenceId)
        )
        var ruleId: Long? = null
        var occDate: Long = System.currentTimeMillis()
        occurrences.use { c ->
            if (c.moveToNext()) {
                ruleId = c.getLong(c.getColumnIndexOrThrow("ruleId"))
                occDate = c.getLong(c.getColumnIndexOrThrow("occurrenceDateEpochMs"))
            }
        }
        val targetRuleId = ruleId ?: throw IllegalArgumentException("Occurrence not found")
        val rule = dao.getRecurringRuleById(targetRuleId) ?: throw IllegalArgumentException("Rule not found")

        val txId = if (rule.type == TransactionType.EXPENSE) {
            recordExpense(
                amountPaisa = rule.amountPaisa,
                fromAccountId = rule.accountId,
                categoryId = rule.categoryId,
                note = rule.title + (if (rule.note.isNotBlank()) " · ${rule.note}" else ""),
                dateEpochMs = occDate,
                recurringOccurrenceId = occurrenceId
            )
        } else {
            recordIncome(
                amountPaisa = rule.amountPaisa,
                toAccountId = rule.accountId,
                categoryId = rule.categoryId,
                note = rule.title + (if (rule.note.isNotBlank()) " · ${rule.note}" else ""),
                dateEpochMs = occDate,
                recurringOccurrenceId = occurrenceId
            )
        }

        val updatedOcc = RecurringOccurrenceEntity(
            id = occurrenceId,
            ruleId = rule.id,
            occurrenceDateEpochMs = occDate,
            status = OccurrenceStatus.CONFIRMED,
            transactionId = txId
        )
        dao.updateOccurrence(updatedOcc)

        val nextDue = calculateNextDue(rule.nextDueEpochMs, rule.frequency)
        dao.updateRecurringRule(rule.copy(nextDueEpochMs = nextDue))

        txId
    }

    suspend fun skipRecurringPayment(occurrenceId: Long) = withContext(Dispatchers.IO) {
        val occurrences = HisaabDatabase.getInstance(context).query(
            "SELECT * FROM recurring_occurrences WHERE id = ?",
            arrayOf(occurrenceId)
        )
        var ruleId: Long? = null
        var occDate: Long = System.currentTimeMillis()
        occurrences.use { c ->
            if (c.moveToNext()) {
                ruleId = c.getLong(c.getColumnIndexOrThrow("ruleId"))
                occDate = c.getLong(c.getColumnIndexOrThrow("occurrenceDateEpochMs"))
            }
        }
        val targetRuleId = ruleId ?: return@withContext
        val rule = dao.getRecurringRuleById(targetRuleId) ?: return@withContext

        dao.updateOccurrence(
            RecurringOccurrenceEntity(
                id = occurrenceId,
                ruleId = rule.id,
                occurrenceDateEpochMs = occDate,
                status = OccurrenceStatus.SKIPPED
            )
        )

        val nextDue = calculateNextDue(rule.nextDueEpochMs, rule.frequency)
        dao.updateRecurringRule(rule.copy(nextDueEpochMs = nextDue))
    }

    private fun calculateNextDue(currentEpochMs: Long, frequency: RecurringFrequency): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = currentEpochMs }
        when (frequency) {
            RecurringFrequency.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
            RecurringFrequency.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            RecurringFrequency.MONTHLY -> cal.add(Calendar.MONTH, 1)
            RecurringFrequency.YEARLY -> cal.add(Calendar.YEAR, 1)
        }
        return cal.timeInMillis
    }

    suspend fun addRecurringRule(
        title: String,
        type: TransactionType,
        amountPaisa: Long,
        accountId: Long,
        categoryId: Long,
        frequency: RecurringFrequency,
        startDateEpochMs: Long,
        note: String
    ): Long = withContext(Dispatchers.IO) {
        require(title.isNotBlank()) { "Title cannot be blank" }
        require(amountPaisa > 0) { "Amount must be positive" }
        val rule = RecurringRuleEntity(
            title = title.trim(),
            type = type,
            amountPaisa = amountPaisa,
            accountId = accountId,
            categoryId = categoryId,
            frequency = frequency,
            startDateEpochMs = startDateEpochMs,
            nextDueEpochMs = startDateEpochMs,
            note = note.trim()
        )
        val id = dao.insertRecurringRule(rule)
        checkAndGenerateRecurringOccurrences()
        id
    }

    suspend fun deleteRecurringRule(rule: RecurringRuleEntity) = withContext(Dispatchers.IO) {
        dao.deleteRecurringRule(rule)
    }

    // --- Actions ---

    suspend fun recordExpense(
        amountPaisa: Long,
        fromAccountId: Long,
        categoryId: Long,
        note: String,
        dateEpochMs: Long,
        receiptPath: String? = null,
        recurringOccurrenceId: Long? = null
    ): Long = withContext(Dispatchers.IO) {
        require(amountPaisa > 0) { "Amount must be greater than zero" }
        val tx = TransactionEntity(
            type = TransactionType.EXPENSE,
            amountPaisa = amountPaisa,
            fromAccountId = fromAccountId,
            toAccountId = null,
            categoryId = categoryId,
            note = note.trim(),
            receiptPath = receiptPath,
            recurringOccurrenceId = recurringOccurrenceId,
            dateEpochMs = dateEpochMs
        )
        val txId = dao.insertTransaction(tx)
        if (receiptPath != null) {
            dao.insertReceipt(
                ReceiptAttachmentEntity(
                    transactionId = txId,
                    localRelativePath = receiptPath
                )
            )
        }
        txId
    }

    suspend fun recordIncome(
        amountPaisa: Long,
        toAccountId: Long,
        categoryId: Long,
        note: String,
        dateEpochMs: Long,
        receiptPath: String? = null,
        recurringOccurrenceId: Long? = null
    ): Long = withContext(Dispatchers.IO) {
        require(amountPaisa > 0) { "Amount must be greater than zero" }
        val tx = TransactionEntity(
            type = TransactionType.INCOME,
            amountPaisa = amountPaisa,
            fromAccountId = null,
            toAccountId = toAccountId,
            categoryId = categoryId,
            note = note.trim(),
            receiptPath = receiptPath,
            recurringOccurrenceId = recurringOccurrenceId,
            dateEpochMs = dateEpochMs
        )
        val txId = dao.insertTransaction(tx)
        if (receiptPath != null) {
            dao.insertReceipt(
                ReceiptAttachmentEntity(
                    transactionId = txId,
                    localRelativePath = receiptPath
                )
            )
        }
        txId
    }

    suspend fun recordTransfer(
        amountPaisa: Long,
        fromAccountId: Long,
        toAccountId: Long,
        note: String,
        dateEpochMs: Long
    ): Long = withContext(Dispatchers.IO) {
        require(amountPaisa > 0) { "Amount must be greater than zero" }
        require(fromAccountId != toAccountId) { "Source and destination accounts must differ" }
        val tx = TransactionEntity(
            type = TransactionType.TRANSFER,
            amountPaisa = amountPaisa,
            fromAccountId = fromAccountId,
            toAccountId = toAccountId,
            categoryId = null,
            note = note.trim(),
            dateEpochMs = dateEpochMs
        )
        dao.insertTransaction(tx)
    }

    suspend fun deleteTransaction(txId: Long): TransactionEntity? = withContext(Dispatchers.IO) {
        val tx = dao.getTransactionById(txId) ?: return@withContext null
        dao.deleteTransaction(tx)
        tx
    }

    suspend fun restoreTransaction(tx: TransactionEntity) = withContext(Dispatchers.IO) {
        dao.insertTransaction(tx)
    }

    // Receipt image save helper
    suspend fun saveReceiptImage(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val receiptsDir = File(context.filesDir, "receipts").apply { mkdirs() }
            val fileName = "receipt_${System.currentTimeMillis()}.jpg"
            val targetFile = File(receiptsDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                val bitmap = BitmapFactory.decodeStream(input)
                FileOutputStream(targetFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
            }
            targetFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    // --- Reserved Money ---
    suspend fun addReservedAmount(
        title: String,
        amountPaisa: Long,
        targetDateEpochMs: Long?,
        note: String
    ): Long = withContext(Dispatchers.IO) {
        require(title.isNotBlank()) { "Reservation title cannot be blank" }
        require(amountPaisa > 0) { "Reserved amount must be greater than zero" }
        val reserved = ReservedAmountEntity(
            title = title.trim(),
            amountPaisa = amountPaisa,
            targetDateEpochMs = targetDateEpochMs,
            note = note.trim()
        )
        dao.insertReservedAmount(reserved)
    }

    suspend fun toggleReservedAmountActive(reserved: ReservedAmountEntity) = withContext(Dispatchers.IO) {
        dao.updateReservedAmount(reserved.copy(isActive = !reserved.isActive))
    }

    suspend fun deleteReservedAmount(reserved: ReservedAmountEntity) = withContext(Dispatchers.IO) {
        dao.deleteReservedAmount(reserved)
    }

    suspend fun addNeed(
        name: String,
        expectedPricePaisa: Long?,
        whereToGet: String,
        externalUrl: String,
        note: String
    ): Long = withContext(Dispatchers.IO) {
        require(name.isNotBlank()) { "Need name cannot be blank" }
        val need = NeedEntity(
            name = name.trim(),
            expectedPricePaisa = expectedPricePaisa,
            whereToGet = whereToGet.trim(),
            externalUrl = externalUrl.trim(),
            note = note.trim()
        )
        dao.insertNeed(need)
    }

    suspend fun updateNeed(need: NeedEntity) = withContext(Dispatchers.IO) {
        dao.updateNeed(need)
    }

    suspend fun toggleNeedDone(need: NeedEntity) = withContext(Dispatchers.IO) {
        val updated = if (need.isDone) {
            need.copy(isDone = false, doneAtEpochMs = null)
        } else {
            need.copy(isDone = true, doneAtEpochMs = System.currentTimeMillis())
        }
        dao.updateNeed(updated)
    }

    suspend fun deleteNeed(needId: Long): NeedEntity? = withContext(Dispatchers.IO) {
        val need = dao.getNeedById(needId) ?: return@withContext null
        dao.deleteNeed(need)
        need
    }

    suspend fun restoreNeed(need: NeedEntity) = withContext(Dispatchers.IO) {
        dao.insertNeed(need)
    }

    suspend fun clearAllDoneNeeds() = withContext(Dispatchers.IO) {
        dao.clearAllDoneNeeds()
    }

    suspend fun addAccount(
        name: String,
        type: AccountType,
        openingBalancePaisa: Long,
        colorHex: String
    ): Long = withContext(Dispatchers.IO) {
        require(name.isNotBlank()) { "Account name cannot be blank" }
        val account = AccountEntity(
            name = name.trim(),
            type = type,
            openingBalancePaisa = openingBalancePaisa,
            colorHex = colorHex
        )
        dao.insertAccount(account)
    }

    suspend fun updateAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
        dao.updateAccount(account)
    }

    suspend fun addCategory(
        name: String,
        type: CategoryType,
        iconKey: String,
        colorHex: String,
        monthlyLimitPaisa: Long? = null
    ): Long = withContext(Dispatchers.IO) {
        require(name.isNotBlank()) { "Category name cannot be blank" }
        val category = CategoryEntity(
            name = name.trim(),
            type = type,
            iconKey = iconKey,
            colorHex = colorHex,
            monthlyLimitPaisa = monthlyLimitPaisa
        )
        dao.insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        dao.updateCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        dao.deleteCategory(category)
    }

    // --- Karz Operations (Debt & Loan Management) ---
    suspend fun createDebt(
        personName: String,
        direction: DebtDirection,
        amountPaisa: Long,
        dateEpochMs: Long,
        dueDateEpochMs: Long? = null,
        description: String = "",
        phoneNumber: String = "",
        notes: String = "",
        accountId: Long? = null,
        recordCashMovement: Boolean = false,
        isInterestEnabled: Boolean = false,
        interestRatePercent: Double = 0.0,
        kind: com.example.data.model.DebtKind = com.example.data.model.DebtKind.MONEY,
        itemDescription: String = "",
        itemQuantity: Double? = null,
        itemUnit: String = "",
        estimatedValuePaisa: Long? = null
    ): Long = withContext(Dispatchers.IO) {
        require(personName.isNotBlank()) { "Person name is required" }
        require(amountPaisa > 0L) { "Amount must be greater than zero" }

        // Invariant I-4: Item/service debts NEVER touch accounts
        val effectiveRecordCash = recordCashMovement && (kind == com.example.data.model.DebtKind.MONEY)

        val debt = DebtEntity(
            personName = personName.trim(),
            direction = direction,
            originalPrincipalPaisa = amountPaisa,
            kind = kind,
            itemDescription = itemDescription.trim(),
            itemQuantity = itemQuantity,
            itemUnit = itemUnit.trim(),
            estimatedValuePaisa = estimatedValuePaisa,
            dateEpochMs = dateEpochMs,
            dueDateEpochMs = dueDateEpochMs,
            description = description.trim(),
            phoneNumber = phoneNumber.trim(),
            notes = notes.trim(),
            status = DebtStatus.ACTIVE,
            linkedAccountId = if (effectiveRecordCash) accountId else null,
            isInterestEnabled = isInterestEnabled,
            interestRatePercent = interestRatePercent,
            createdAtEpochMs = System.currentTimeMillis(),
            updatedAtEpochMs = System.currentTimeMillis()
        )
        val debtId = dao.insertDebt(debt)

        var createdTxId: Long? = null
        if (effectiveRecordCash && accountId != null) {
            val txType = if (direction == DebtDirection.I_GAVE) {
                TransactionType.DEBT_LENT
            } else {
                TransactionType.DEBT_BORROWED
            }
            val txNote = if (direction == DebtDirection.I_GAVE) {
                "Karz Lent to ${personName.trim()}" + if (description.isNotBlank()) " · $description" else ""
            } else {
                "Karz Borrowed from ${personName.trim()}" + if (description.isNotBlank()) " · $description" else ""
            }
            val tx = TransactionEntity(
                type = txType,
                amountPaisa = amountPaisa,
                fromAccountId = if (direction == DebtDirection.I_GAVE) accountId else null,
                toAccountId = if (direction == DebtDirection.I_TOOK) accountId else null,
                categoryId = null,
                note = txNote,
                debtId = debtId,
                dateEpochMs = dateEpochMs
            )
            createdTxId = dao.insertTransaction(tx)
        }

        val event = DebtEventEntity(
            debtId = debtId,
            type = DebtEventType.ORIGINAL_ADVANCE,
            amountPaisa = amountPaisa,
            dateEpochMs = dateEpochMs,
            note = "Original amount" + if (description.isNotBlank()) ": $description" else "",
            accountId = if (recordCashMovement) accountId else null,
            transactionId = createdTxId
        )
        dao.insertDebtEvent(event)
        debtId
    }

    suspend fun recordDebtRepayment(
        debtId: Long,
        amountPaisa: Long,
        dateEpochMs: Long,
        accountId: Long? = null,
        recordCashMovement: Boolean = false,
        note: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val debt = dao.getDebtById(debtId) ?: throw IllegalArgumentException("Debt not found")
        val events = dao.getDebtEventsForDebt(debtId)
        val totalPrincipal = debt.originalPrincipalPaisa + events.filter { it.type == DebtEventType.ADDITIONAL_ADVANCE }.sumOf { it.amountPaisa }
        val currentRepaid = events.filter { it.type == DebtEventType.REPAYMENT }.sumOf { it.amountPaisa }
        val currentAdjustments = events.filter { it.type == DebtEventType.ADJUSTMENT }.sumOf { it.amountPaisa }
        val currentOutstanding = (totalPrincipal - currentRepaid + currentAdjustments).coerceAtLeast(0L)

        require(amountPaisa > 0L) { "Repayment amount must be positive" }
        require(amountPaisa <= currentOutstanding) { "Repayment cannot exceed outstanding balance" }

        var createdTxId: Long? = null
        if (recordCashMovement && accountId != null) {
            val txType = if (debt.direction == DebtDirection.I_GAVE) {
                TransactionType.DEBT_REPAYMENT_IN
            } else {
                TransactionType.DEBT_REPAYMENT_OUT
            }
            val txNote = if (debt.direction == DebtDirection.I_GAVE) {
                "Karz Repayment received from ${debt.personName}" + if (note.isNotBlank()) " · $note" else ""
            } else {
                "Karz Repayment paid to ${debt.personName}" + if (note.isNotBlank()) " · $note" else ""
            }
            val tx = TransactionEntity(
                type = txType,
                amountPaisa = amountPaisa,
                fromAccountId = if (debt.direction == DebtDirection.I_TOOK) accountId else null,
                toAccountId = if (debt.direction == DebtDirection.I_GAVE) accountId else null,
                categoryId = null,
                note = txNote,
                debtId = debtId,
                dateEpochMs = dateEpochMs
            )
            createdTxId = dao.insertTransaction(tx)
        }

        val event = DebtEventEntity(
            debtId = debtId,
            type = DebtEventType.REPAYMENT,
            amountPaisa = amountPaisa,
            dateEpochMs = dateEpochMs,
            note = note.trim().ifEmpty { "Repayment" },
            accountId = if (recordCashMovement) accountId else null,
            transactionId = createdTxId
        )
        val eventId = dao.insertDebtEvent(event)

        val newRepaid = currentRepaid + amountPaisa
        val newOutstanding = (totalPrincipal - newRepaid + currentAdjustments).coerceAtLeast(0L)
        val newStatus = if (newOutstanding == 0L) {
            DebtStatus.SETTLED
        } else {
            DebtStatus.PARTIALLY_PAID
        }

        dao.updateDebt(
            debt.copy(
                status = newStatus,
                settledAtEpochMs = if (newStatus == DebtStatus.SETTLED) System.currentTimeMillis() else null,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )
        eventId
    }

    suspend fun recordAdditionalAdvance(
        debtId: Long,
        amountPaisa: Long,
        dateEpochMs: Long,
        accountId: Long? = null,
        recordCashMovement: Boolean = false,
        note: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val debt = dao.getDebtById(debtId) ?: throw IllegalArgumentException("Debt not found")
        require(amountPaisa > 0L) { "Advance amount must be greater than zero" }

        var createdTxId: Long? = null
        if (recordCashMovement && accountId != null) {
            val txType = if (debt.direction == DebtDirection.I_GAVE) {
                TransactionType.DEBT_LENT
            } else {
                TransactionType.DEBT_BORROWED
            }
            val txNote = if (debt.direction == DebtDirection.I_GAVE) {
                "Additional Karz to ${debt.personName}" + if (note.isNotBlank()) " · $note" else ""
            } else {
                "Additional Karz from ${debt.personName}" + if (note.isNotBlank()) " · $note" else ""
            }
            val tx = TransactionEntity(
                type = txType,
                amountPaisa = amountPaisa,
                fromAccountId = if (debt.direction == DebtDirection.I_GAVE) accountId else null,
                toAccountId = if (debt.direction == DebtDirection.I_TOOK) accountId else null,
                categoryId = null,
                note = txNote,
                debtId = debtId,
                dateEpochMs = dateEpochMs
            )
            createdTxId = dao.insertTransaction(tx)
        }

        val event = DebtEventEntity(
            debtId = debtId,
            type = DebtEventType.ADDITIONAL_ADVANCE,
            amountPaisa = amountPaisa,
            dateEpochMs = dateEpochMs,
            note = note.trim().ifEmpty { "Additional Advance" },
            accountId = if (recordCashMovement) accountId else null,
            transactionId = createdTxId
        )
        val eventId = dao.insertDebtEvent(event)

        val newStatus = if (debt.status == DebtStatus.SETTLED) DebtStatus.ACTIVE else debt.status
        dao.updateDebt(
            debt.copy(
                status = newStatus,
                settledAtEpochMs = null,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )
        eventId
    }

    suspend fun settleDebtWithAdjustment(
        debtId: Long,
        reason: String
    ) = withContext(Dispatchers.IO) {
        val debt = dao.getDebtById(debtId) ?: throw IllegalArgumentException("Debt not found")
        require(reason.isNotBlank()) { "Settlement / write-off reason is required" }
        val events = dao.getDebtEventsForDebt(debtId)
        val totalPrincipal = debt.originalPrincipalPaisa + events.filter { it.type == DebtEventType.ADDITIONAL_ADVANCE }.sumOf { it.amountPaisa }
        val currentRepaid = events.filter { it.type == DebtEventType.REPAYMENT }.sumOf { it.amountPaisa }
        val currentAdjustments = events.filter { it.type == DebtEventType.ADJUSTMENT }.sumOf { it.amountPaisa }
        val currentOutstanding = (totalPrincipal - currentRepaid + currentAdjustments).coerceAtLeast(0L)

        if (currentOutstanding > 0L) {
            val event = DebtEventEntity(
                debtId = debtId,
                type = DebtEventType.WRITE_OFF,
                amountPaisa = currentOutstanding,
                dateEpochMs = System.currentTimeMillis(),
                note = "Written off: ${reason.trim()}"
            )
            dao.insertDebtEvent(event)
        }

        dao.updateDebt(
            debt.copy(
                status = DebtStatus.WRITTEN_OFF,
                writtenOffReason = reason.trim(),
                settledAtEpochMs = System.currentTimeMillis(),
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateDebtDueDate(debtId: Long, newDueDateEpochMs: Long?) = withContext(Dispatchers.IO) {
        val debt = dao.getDebtById(debtId) ?: return@withContext
        dao.updateDebt(debt.copy(dueDateEpochMs = newDueDateEpochMs, updatedAtEpochMs = System.currentTimeMillis()))
    }

    suspend fun updateDebtDetails(
        debtId: Long,
        personName: String,
        notes: String,
        phoneNumber: String
    ) = withContext(Dispatchers.IO) {
        val debt = dao.getDebtById(debtId) ?: return@withContext
        dao.updateDebt(
            debt.copy(
                personName = personName.trim(),
                notes = notes.trim(),
                phoneNumber = phoneNumber.trim(),
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteDebt(debtId: Long) = withContext(Dispatchers.IO) {
        dao.deleteDebtEventsForDebt(debtId)
        dao.deleteDebtById(debtId)
    }

    // --- Karz Installments ---
    fun getInstallmentsForDebtFlow(debtId: Long): Flow<List<KarzInstallmentEntity>> =
        dao.getInstallmentsForDebtFlow(debtId)

    suspend fun splitDebtIntoInstallments(
        debtId: Long,
        count: Int,
        isWeekly: Boolean,
        firstDueDateEpochDay: Long
    ) = withContext(Dispatchers.IO) {
        val debt = dao.getDebtById(debtId) ?: throw IllegalArgumentException("Debt not found")
        require(debt.kind == com.example.data.model.DebtKind.MONEY) { "Installments only available for money debts" }
        require(count in 2..120) { "Installment count must be between 2 and 120" }

        val events = dao.getDebtEventsForDebt(debtId)
        val totalPrincipal = debt.originalPrincipalPaisa + events.filter { it.type == DebtEventType.ADDITIONAL_ADVANCE }.sumOf { it.amountPaisa }
        val currentRepaid = events.filter { it.type == DebtEventType.REPAYMENT }.sumOf { it.amountPaisa }
        val currentAdjustments = events.filter { it.type == DebtEventType.ADJUSTMENT }.sumOf { it.amountPaisa }
        val outstandingPaisa = (totalPrincipal - currentRepaid + currentAdjustments).coerceAtLeast(0L)
        require(outstandingPaisa > 0L) { "Debt is already settled or has no outstanding balance" }

        val splitAmounts = InstallmentSplitter.split(outstandingPaisa, count)
        val anchorDate = LocalDate.ofEpochDay(firstDueDateEpochDay)
        val dueDates = if (isWeekly) {
            InstallmentSplitter.weeklyDates(anchorDate, count)
        } else {
            InstallmentSplitter.monthlyDates(anchorDate, count)
        }

        val installments = splitAmounts.mapIndexed { index, amount ->
            KarzInstallmentEntity(
                debtId = debtId,
                number = index + 1,
                totalCount = count,
                dueDateEpochDay = dueDates[index].toEpochDay(),
                scheduledPaisa = amount,
                paidPaisa = 0L,
                status = InstallmentStatus.SCHEDULED,
                note = "Installment ${index + 1} of $count"
            )
        }
        dao.insertInstallments(installments)
        dao.insertAuditLog(
            AuditLogEntity(
                createdAtEpochMs = System.currentTimeMillis(),
                entityType = "KARZ_INSTALLMENTS",
                entityId = debtId,
                action = "SPLIT",
                amountPaisa = outstandingPaisa
            )
        )
    }

    suspend fun recordKarzInstallmentPayment(
        installment: KarzInstallmentEntity,
        amountPaisa: Long,
        accountId: Long?,
        recordCashMovement: Boolean,
        dateEpochMs: Long,
        note: String
    ): Long = withContext(Dispatchers.IO) {
        require(amountPaisa > 0L) { "Payment amount must be positive" }
        val newPaid = installment.paidPaisa + amountPaisa
        require(newPaid <= installment.scheduledPaisa) { "Payment cannot exceed installment amount" }

        val newStatus = if (newPaid >= installment.scheduledPaisa) InstallmentStatus.PAID else InstallmentStatus.PARTIAL
        dao.updateInstallment(
            installment.copy(
                paidPaisa = newPaid,
                status = newStatus,
                note = if (note.isNotBlank()) note else installment.note
            )
        )

        // Record repayment on parent debt
        val eventId = recordDebtRepayment(
            debtId = installment.debtId,
            amountPaisa = amountPaisa,
            dateEpochMs = dateEpochMs,
            accountId = accountId,
            recordCashMovement = recordCashMovement,
            note = "Installment ${installment.number}/${installment.totalCount}" + if (note.isNotBlank()) " · $note" else ""
        )

        dao.insertAuditLog(
            AuditLogEntity(
                createdAtEpochMs = System.currentTimeMillis(),
                entityType = "KARZ_INSTALLMENT",
                entityId = installment.id,
                action = "PAID",
                amountPaisa = amountPaisa
            )
        )
        eventId
    }

    suspend fun rescheduleInstallment(
        installment: KarzInstallmentEntity,
        newDueDateEpochDay: Long
    ) = withContext(Dispatchers.IO) {
        dao.updateInstallment(
            installment.copy(
                dueDateEpochDay = newDueDateEpochDay,
                status = InstallmentStatus.RESCHEDULED
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                createdAtEpochMs = System.currentTimeMillis(),
                entityType = "KARZ_INSTALLMENT",
                entityId = installment.id,
                action = "RESCHEDULED"
            )
        )
    }

    // --- Kameti (Committee Savings) ---
    fun getAllKametisFlow(): Flow<List<KametiEntity>> = dao.getAllKametisFlow()

    suspend fun getKametiById(id: Long): KametiEntity? = dao.getKametiById(id)

    fun getContributionsForKametiFlow(kametiId: Long): Flow<List<KametiContributionEntity>> =
        dao.getContributionsForKametiFlow(kametiId)

    fun getPayoutForKametiFlow(kametiId: Long): Flow<KametiPayoutEntity?> =
        dao.getPayoutForKametiFlow(kametiId)

    suspend fun createKameti(
        name: String,
        memberCount: Int,
        contributionPaisa: Long,
        cadence: KametiCadence,
        startDateEpochDay: Long,
        totalInstallments: Int,
        myPayoutInstallment: Int,
        expectedPayoutPaisa: Long?,
        note: String
    ): Long = withContext(Dispatchers.IO) {
        require(name.isNotBlank()) { "Committee name is required" }
        require(memberCount >= 2) { "Member count must be at least 2" }
        require(contributionPaisa > 0L) { "Contribution amount must be greater than zero" }
        require(myPayoutInstallment in 1..totalInstallments) { "Payout installment must be between 1 and $totalInstallments" }

        val kameti = KametiEntity(
            name = name.trim(),
            memberCount = memberCount,
            contributionPaisa = contributionPaisa,
            cadence = cadence,
            startDateEpochDay = startDateEpochDay,
            totalInstallments = totalInstallments,
            myPayoutInstallment = myPayoutInstallment,
            expectedPayoutPaisa = expectedPayoutPaisa ?: (contributionPaisa * memberCount),
            status = KametiStatus.ACTIVE,
            note = note.trim()
        )
        val kametiId = dao.insertKameti(kameti)

        // Generate contribution schedule using InstallmentSplitter dates
        val anchorDate = LocalDate.ofEpochDay(startDateEpochDay)
        val dueDates = if (cadence == KametiCadence.WEEKLY) {
            InstallmentSplitter.weeklyDates(anchorDate, totalInstallments)
        } else {
            InstallmentSplitter.monthlyDates(anchorDate, totalInstallments)
        }

        val contributions = dueDates.mapIndexed { index, date ->
            KametiContributionEntity(
                kametiId = kametiId,
                installmentIndex = index + 1,
                dueDateEpochDay = date.toEpochDay(),
                expectedPaisa = contributionPaisa,
                paidPaisa = 0L,
                status = ContributionStatus.PENDING
            )
        }
        dao.insertContributions(contributions)

        // Insert payout record
        val payoutDueDate = dueDates.getOrNull(myPayoutInstallment - 1)?.toEpochDay() ?: startDateEpochDay
        val payout = KametiPayoutEntity(
            kametiId = kametiId,
            expectedDateEpochDay = payoutDueDate,
            expectedPaisa = expectedPayoutPaisa ?: (contributionPaisa * memberCount),
            status = PayoutStatus.EXPECTED
        )
        dao.insertPayout(payout)

        dao.insertAuditLog(
            AuditLogEntity(
                createdAtEpochMs = System.currentTimeMillis(),
                entityType = "KAMETI",
                entityId = kametiId,
                action = "CREATED",
                amountPaisa = contributionPaisa
            )
        )
        kametiId
    }

    suspend fun recordKametiContribution(
        contributionId: Long,
        accountId: Long,
        amountPaisa: Long,
        dateEpochDay: Long,
        note: String
    ): Long = withContext(Dispatchers.IO) {
        val contribution = dao.getContributionById(contributionId) ?: throw IllegalArgumentException("Contribution not found")
        val kameti = dao.getKametiById(contribution.kametiId) ?: throw IllegalArgumentException("Kameti not found")
        require(amountPaisa > 0L) { "Payment amount must be greater than zero" }

        val remaining = contribution.expectedPaisa - contribution.paidPaisa
        require(amountPaisa <= remaining) { "Amount exceeds remaining installment balance" }

        val now = System.currentTimeMillis()
        val tx = TransactionEntity(
            type = TransactionType.EXPENSE,
            amountPaisa = amountPaisa,
            fromAccountId = accountId,
            toAccountId = null,
            categoryId = null,
            sourceType = "KAMETI_CONTRIBUTION",
            sourceId = contributionId,
            note = "Kameti contribution: ${kameti.name} (Installment ${contribution.installmentIndex})" + if (note.isNotBlank()) " · $note" else "",
            dateEpochMs = LocalDate.ofEpochDay(dateEpochDay).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        val txId = dao.insertTransaction(tx)

        val newPaid = contribution.paidPaisa + amountPaisa
        dao.updateContribution(
            contribution.copy(
                paidPaisa = newPaid,
                status = if (newPaid >= contribution.expectedPaisa) ContributionStatus.PAID else ContributionStatus.PARTIAL,
                lastPaidDateEpochDay = dateEpochDay,
                note = if (note.isNotBlank()) note else contribution.note
            )
        )

        dao.insertAuditLog(
            AuditLogEntity(
                createdAtEpochMs = now,
                entityType = "KAMETI_CONTRIBUTION",
                entityId = contributionId,
                action = "PAID",
                amountPaisa = amountPaisa
            )
        )
        txId
    }

    suspend fun claimKametiPayout(
        payoutId: Long,
        kametiId: Long,
        accountId: Long,
        receivedAmountPaisa: Long,
        dateEpochDay: Long,
        note: String
    ): Long = withContext(Dispatchers.IO) {
        val kameti = dao.getKametiById(kametiId) ?: throw IllegalArgumentException("Kameti not found")
        val payout = dao.getPayoutForKameti(kametiId) ?: throw IllegalArgumentException("Payout not found")
        require(receivedAmountPaisa > 0L) { "Payout amount must be greater than zero" }

        val now = System.currentTimeMillis()
        val tx = TransactionEntity(
            type = TransactionType.INCOME,
            amountPaisa = receivedAmountPaisa,
            fromAccountId = null,
            toAccountId = accountId,
            categoryId = null,
            sourceType = "KAMETI_PAYOUT",
            sourceId = payout.id,
            note = "Kameti Payout Received: ${kameti.name}" + if (note.isNotBlank()) " · $note" else "",
            dateEpochMs = LocalDate.ofEpochDay(dateEpochDay).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        val txId = dao.insertTransaction(tx)

        dao.updatePayout(
            payout.copy(
                receivedPaisa = receivedAmountPaisa,
                receivedDateEpochDay = dateEpochDay,
                status = PayoutStatus.RECEIVED,
                transactionId = txId
            )
        )

        dao.insertAuditLog(
            AuditLogEntity(
                createdAtEpochMs = now,
                entityType = "KAMETI_PAYOUT",
                entityId = payout.id,
                action = "RECEIVED",
                amountPaisa = receivedAmountPaisa
            )
        )
        txId
    }

    suspend fun archiveKameti(kametiId: Long) = withContext(Dispatchers.IO) {
        val kameti = dao.getKametiById(kametiId) ?: return@withContext
        dao.updateKameti(kameti.copy(archived = true, status = KametiStatus.COMPLETED, updatedAtEpochMs = System.currentTimeMillis()))
    }

    // --- Zakat & Charity ---
    fun getAllCharityRecordsFlow(): Flow<List<CharityRecordEntity>> = dao.getAllCharityRecordsFlow()

    fun getAllZakatCalculationsFlow(): Flow<List<ZakatCalculationEntity>> = dao.getAllZakatCalculationsFlow()

    suspend fun saveZakatCalculation(calculation: ZakatCalculationEntity): Long = withContext(Dispatchers.IO) {
        dao.insertZakatCalculation(calculation)
    }

    suspend fun recordCharityPayment(
        category: CharityCategory,
        recipientName: String,
        amountPaisa: Long,
        dateEpochDay: Long,
        accountId: Long?,
        recordCashMovement: Boolean,
        note: String
    ): Long = withContext(Dispatchers.IO) {
        require(amountPaisa > 0L) { "Charity amount must be positive" }

        var txId: Long? = null
        if (recordCashMovement && accountId != null) {
            val tx = TransactionEntity(
                type = TransactionType.EXPENSE,
                amountPaisa = amountPaisa,
                fromAccountId = accountId,
                toAccountId = null,
                categoryId = null,
                note = "${category.name}: ${recipientName.trim()}" + if (note.isNotBlank()) " · $note" else "",
                dateEpochMs = LocalDate.ofEpochDay(dateEpochDay).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            txId = dao.insertTransaction(tx)
        }

        val record = CharityRecordEntity(
            category = category,
            recipientName = recipientName.trim(),
            amountPaisa = amountPaisa,
            status = CharityStatus.PAID,
            paidDateEpochDay = dateEpochDay,
            accountId = if (recordCashMovement) accountId else null,
            transactionId = txId,
            note = note.trim()
        )
        dao.insertCharityRecord(record)
    }

    // --- Category Budgets ---
    fun getAllCategoryBudgetsFlow(): Flow<List<CategoryBudgetEntity>> = dao.getAllCategoryBudgetsFlow()

    suspend fun saveCategoryBudget(categoryId: Long, monthKey: String, limitPaisa: Long): Long = withContext(Dispatchers.IO) {
        val budget = CategoryBudgetEntity(
            categoryId = categoryId,
            monthKey = monthKey,
            limitPaisa = limitPaisa
        )
        dao.insertCategoryBudget(budget)
    }

    suspend fun exportDataAsJsonFile(): File = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "Hisaab Do Bhai")
        root.put("version", "2.1")
        root.put("exportedAtEpochMs", System.currentTimeMillis())

        // Accounts
        val accountsArray = JSONArray()
        dao.getAllAccountsFlow().first().forEach { acc ->
            val obj = JSONObject().apply {
                put("id", acc.id)
                put("name", acc.name)
                put("type", acc.type.name)
                put("openingBalancePaisa", acc.openingBalancePaisa)
                put("colorHex", acc.colorHex)
                put("isArchived", acc.isArchived)
                put("createdAtEpochMs", acc.createdAtEpochMs)
            }
            accountsArray.put(obj)
        }
        root.put("accounts", accountsArray)

        // Categories
        val categoriesArray = JSONArray()
        dao.getAllCategoriesFlow().first().forEach { cat ->
            val obj = JSONObject().apply {
                put("id", cat.id)
                put("name", cat.name)
                put("type", cat.type.name)
                put("iconKey", cat.iconKey)
                put("colorHex", cat.colorHex)
                cat.monthlyLimitPaisa?.let { put("monthlyLimitPaisa", it) }
                put("createdAtEpochMs", cat.createdAtEpochMs)
            }
            categoriesArray.put(obj)
        }
        root.put("categories", categoriesArray)

        // Transactions
        val txArray = JSONArray()
        dao.getAllTransactionsFlow().first().forEach { tx ->
            val obj = JSONObject().apply {
                put("id", tx.id)
                put("type", tx.type.name)
                put("amountPaisa", tx.amountPaisa)
                tx.fromAccountId?.let { put("fromAccountId", it) }
                tx.toAccountId?.let { put("toAccountId", it) }
                tx.categoryId?.let { put("categoryId", it) }
                put("note", tx.note)
                put("dateEpochMs", tx.dateEpochMs)
                tx.debtId?.let { put("debtId", it) }
                put("createdAtEpochMs", tx.createdAtEpochMs)
            }
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        // Needs
        val needsArray = JSONArray()
        dao.getAllNeedsFlow().first().forEach { need ->
            val obj = JSONObject().apply {
                put("id", need.id)
                put("name", need.name)
                need.expectedPricePaisa?.let { put("expectedPricePaisa", it) }
                put("whereToGet", need.whereToGet)
                put("externalUrl", need.externalUrl)
                put("note", need.note)
                put("isDone", need.isDone)
                put("createdAtEpochMs", need.createdAtEpochMs)
            }
            needsArray.put(obj)
        }
        root.put("needs", needsArray)

        // Reserved amounts
        val reservedArray = JSONArray()
        dao.getAllReservedAmountsFlow().first().forEach { res ->
            val obj = JSONObject().apply {
                put("id", res.id)
                put("title", res.title)
                put("amountPaisa", res.amountPaisa)
                res.targetDateEpochMs?.let { put("targetDateEpochMs", it) }
                put("note", res.note)
                put("isActive", res.isActive)
            }
            reservedArray.put(obj)
        }
        root.put("reservedAmounts", reservedArray)

        // Debts
        val debtsArray = JSONArray()
        dao.getAllDebtsFlow().first().forEach { debt ->
            val obj = JSONObject().apply {
                put("id", debt.id)
                put("personName", debt.personName)
                put("direction", debt.direction.name)
                put("originalPrincipalPaisa", debt.originalPrincipalPaisa)
                put("dateEpochMs", debt.dateEpochMs)
                debt.dueDateEpochMs?.let { put("dueDateEpochMs", it) }
                put("description", debt.description)
                put("phoneNumber", debt.phoneNumber)
                put("notes", debt.notes)
                put("status", debt.status.name)
                debt.linkedAccountId?.let { put("linkedAccountId", it) }
                put("createdAtEpochMs", debt.createdAtEpochMs)
                put("updatedAtEpochMs", debt.updatedAtEpochMs)
            }
            debtsArray.put(obj)
        }
        root.put("debts", debtsArray)

        // Debt Events
        val debtEventsArray = JSONArray()
        dao.getAllDebtEventsFlow().first().forEach { ev ->
            val obj = JSONObject().apply {
                put("id", ev.id)
                put("debtId", ev.debtId)
                put("type", ev.type.name)
                put("amountPaisa", ev.amountPaisa)
                put("dateEpochMs", ev.dateEpochMs)
                put("note", ev.note)
                ev.accountId?.let { put("accountId", it) }
                ev.transactionId?.let { put("transactionId", it) }
                put("createdAtEpochMs", ev.createdAtEpochMs)
            }
            debtEventsArray.put(obj)
        }
        root.put("debtEvents", debtEventsArray)

        val exportDir = File(context.filesDir, "exports").apply { mkdirs() }
        val exportFile = File(exportDir, "hisaab_backup_${System.currentTimeMillis()}.json")
        exportFile.writeText(root.toString(2))
        exportFile
    }

    suspend fun restoreDataFromJson(jsonString: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("accounts") || !root.has("categories")) {
                return@withContext Result.failure(IllegalArgumentException("Invalid Hisaab backup: missing accounts or categories"))
            }

            val accountsArray = root.getJSONArray("accounts")
            val categoriesArray = root.getJSONArray("categories")
            val txArray = root.optJSONArray("transactions") ?: JSONArray()
            val needsArray = root.optJSONArray("needs") ?: JSONArray()
            val debtsArray = root.optJSONArray("debts") ?: JSONArray()
            val debtEventsArray = root.optJSONArray("debtEvents") ?: JSONArray()

            // Restore Accounts
            val accounts = mutableListOf<AccountEntity>()
            for (i in 0 until accountsArray.length()) {
                val obj = accountsArray.getJSONObject(i)
                val typeStr = obj.getString("type")
                val accType = when (typeStr.uppercase()) {
                    "CASH", "WALLET", "CASH_WALLET" -> AccountType.CASH_WALLET
                    "BANK" -> AccountType.BANK
                    "SAVINGS" -> AccountType.SAVINGS
                    else -> AccountType.OTHER
                }
                accounts.add(
                    AccountEntity(
                        id = obj.optLong("id", 0),
                        name = obj.getString("name"),
                        type = accType,
                        openingBalancePaisa = obj.optLong("openingBalancePaisa", 0L),
                        colorHex = obj.optString("colorHex", "#7C9A6B"),
                        isArchived = obj.optBoolean("isArchived", false)
                    )
                )
            }
            dao.insertAccounts(accounts)

            // Restore Categories
            val categories = mutableListOf<CategoryEntity>()
            for (i in 0 until categoriesArray.length()) {
                val obj = categoriesArray.getJSONObject(i)
                categories.add(
                    CategoryEntity(
                        id = obj.optLong("id", 0),
                        name = obj.getString("name"),
                        type = CategoryType.valueOf(obj.getString("type")),
                        iconKey = obj.optString("iconKey", "category"),
                        colorHex = obj.optString("colorHex", "#8A8F99"),
                        monthlyLimitPaisa = if (obj.has("monthlyLimitPaisa")) obj.getLong("monthlyLimitPaisa") else null
                    )
                )
            }
            dao.insertCategories(categories)

            // Restore Transactions
            for (i in 0 until txArray.length()) {
                val obj = txArray.getJSONObject(i)
                dao.insertTransaction(
                    TransactionEntity(
                        id = obj.optLong("id", 0),
                        type = TransactionType.valueOf(obj.getString("type")),
                        amountPaisa = obj.getLong("amountPaisa"),
                        fromAccountId = if (obj.has("fromAccountId")) obj.getLong("fromAccountId") else null,
                        toAccountId = if (obj.has("toAccountId")) obj.getLong("toAccountId") else null,
                        categoryId = if (obj.has("categoryId")) obj.getLong("categoryId") else null,
                        note = obj.optString("note", ""),
                        dateEpochMs = obj.optLong("dateEpochMs", System.currentTimeMillis()),
                        debtId = if (obj.has("debtId")) obj.getLong("debtId") else null
                    )
                )
            }

            // Restore Needs
            for (i in 0 until needsArray.length()) {
                val obj = needsArray.getJSONObject(i)
                dao.insertNeed(
                    NeedEntity(
                        id = obj.optLong("id", 0),
                        name = obj.getString("name"),
                        expectedPricePaisa = if (obj.has("expectedPricePaisa")) obj.getLong("expectedPricePaisa") else null,
                        whereToGet = obj.optString("whereToGet", ""),
                        externalUrl = obj.optString("externalUrl", ""),
                        note = obj.optString("note", ""),
                        isDone = obj.optBoolean("isDone", false)
                    )
                )
            }

            // Restore Debts
            for (i in 0 until debtsArray.length()) {
                val obj = debtsArray.getJSONObject(i)
                dao.insertDebt(
                    DebtEntity(
                        id = obj.optLong("id", 0),
                        personName = obj.getString("personName"),
                        direction = DebtDirection.valueOf(obj.getString("direction")),
                        originalPrincipalPaisa = obj.getLong("originalPrincipalPaisa"),
                        dateEpochMs = obj.optLong("dateEpochMs", System.currentTimeMillis()),
                        dueDateEpochMs = if (obj.has("dueDateEpochMs")) obj.getLong("dueDateEpochMs") else null,
                        description = obj.optString("description", ""),
                        phoneNumber = obj.optString("phoneNumber", ""),
                        notes = obj.optString("notes", ""),
                        status = DebtStatus.valueOf(obj.optString("status", "ACTIVE")),
                        linkedAccountId = if (obj.has("linkedAccountId")) obj.getLong("linkedAccountId") else null,
                        createdAtEpochMs = obj.optLong("createdAtEpochMs", System.currentTimeMillis()),
                        updatedAtEpochMs = obj.optLong("updatedAtEpochMs", System.currentTimeMillis())
                    )
                )
            }

            // Restore Debt Events
            for (i in 0 until debtEventsArray.length()) {
                val obj = debtEventsArray.getJSONObject(i)
                dao.insertDebtEvent(
                    DebtEventEntity(
                        id = obj.optLong("id", 0),
                        debtId = obj.getLong("debtId"),
                        type = DebtEventType.valueOf(obj.getString("type")),
                        amountPaisa = obj.getLong("amountPaisa"),
                        dateEpochMs = obj.optLong("dateEpochMs", System.currentTimeMillis()),
                        note = obj.optString("note", ""),
                        accountId = if (obj.has("accountId")) obj.getLong("accountId") else null,
                        transactionId = if (obj.has("transactionId")) obj.getLong("transactionId") else null,
                        createdAtEpochMs = obj.optLong("createdAtEpochMs", System.currentTimeMillis())
                    )
                )
            }

            Result.success("Restored ${accounts.size} accounts, ${categories.size} categories, ${txArray.length()} transactions, ${needsArray.length()} needs, and ${debtsArray.length()} debts.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: HisaabRepository? = null

        fun getInstance(context: Context): HisaabRepository {
            return INSTANCE ?: synchronized(this) {
                val db = HisaabDatabase.getInstance(context)
                val repo = HisaabRepository(context.applicationContext, db.dao())
                INSTANCE = repo
                repo
            }
        }
    }
}
