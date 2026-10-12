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
import com.example.data.model.MonthlySummary
import com.example.data.model.NeedEntity
import com.example.data.model.OccurrenceStatus
import com.example.data.model.PendingRecurringItem
import com.example.data.model.ReceiptAttachmentEntity
import com.example.data.model.RecurringFrequency
import com.example.data.model.RecurringOccurrenceEntity
import com.example.data.model.RecurringRuleEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

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
        val alreadySeeded = prefs.getBoolean("hisaab_seeded_v1", false)
        if (alreadySeeded) {
            checkAndGenerateRecurringOccurrences()
            return@withContext
        }

        if (dao.getAccountsCount() == 0) {
            val defaultAccounts = listOf(
                AccountEntity(
                    name = "Cash",
                    type = AccountType.CASH,
                    openingBalancePaisa = 0L,
                    colorHex = "#7C9A6B"
                ),
                AccountEntity(
                    name = "Bank",
                    type = AccountType.BANK,
                    openingBalancePaisa = 0L,
                    colorHex = "#5B8A8A"
                ),
                AccountEntity(
                    name = "Wallet",
                    type = AccountType.WALLET,
                    openingBalancePaisa = 0L,
                    colorHex = "#C67B7B"
                )
            )
            dao.insertAccounts(defaultAccounts)
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

        prefs.edit().putBoolean("hisaab_seeded_v1", true).apply()
        checkAndGenerateRecurringOccurrences()
    }

    val totalBalancePaisa: Flow<Long> = dao.getTotalBalanceFlow().map { it ?: 0L }

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

    suspend fun exportDataAsJsonFile(): File = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "Hisaab Do Bhai")
        root.put("version", "2.0")
        root.put("exportedAtEpochMs", System.currentTimeMillis())

        val db = HisaabDatabase.getInstance(context)

        // Accounts
        val accountsArray = JSONArray()
        db.query("SELECT * FROM accounts", null).use { c ->
            while (c.moveToNext()) {
                val obj = JSONObject().apply {
                    put("id", c.getLong(c.getColumnIndexOrThrow("id")))
                    put("name", c.getString(c.getColumnIndexOrThrow("name")))
                    put("type", c.getString(c.getColumnIndexOrThrow("type")))
                    put("openingBalancePaisa", c.getLong(c.getColumnIndexOrThrow("openingBalancePaisa")))
                    put("colorHex", c.getString(c.getColumnIndexOrThrow("colorHex")))
                    put("isArchived", c.getInt(c.getColumnIndexOrThrow("isArchived")) == 1)
                    put("createdAtEpochMs", c.getLong(c.getColumnIndexOrThrow("createdAtEpochMs")))
                }
                accountsArray.put(obj)
            }
        }
        root.put("accounts", accountsArray)

        // Categories
        val categoriesArray = JSONArray()
        db.query("SELECT * FROM categories", null).use { c ->
            while (c.moveToNext()) {
                val obj = JSONObject().apply {
                    put("id", c.getLong(c.getColumnIndexOrThrow("id")))
                    put("name", c.getString(c.getColumnIndexOrThrow("name")))
                    put("type", c.getString(c.getColumnIndexOrThrow("type")))
                    put("iconKey", c.getString(c.getColumnIndexOrThrow("iconKey")))
                    put("colorHex", c.getString(c.getColumnIndexOrThrow("colorHex")))
                    val limitIdx = c.getColumnIndexOrThrow("monthlyLimitPaisa")
                    if (!c.isNull(limitIdx)) put("monthlyLimitPaisa", c.getLong(limitIdx))
                    put("createdAtEpochMs", c.getLong(c.getColumnIndexOrThrow("createdAtEpochMs")))
                }
                categoriesArray.put(obj)
            }
        }
        root.put("categories", categoriesArray)

        // Transactions
        val txArray = JSONArray()
        db.query("SELECT * FROM transactions", null).use { c ->
            while (c.moveToNext()) {
                val obj = JSONObject().apply {
                    put("id", c.getLong(c.getColumnIndexOrThrow("id")))
                    put("type", c.getString(c.getColumnIndexOrThrow("type")))
                    put("amountPaisa", c.getLong(c.getColumnIndexOrThrow("amountPaisa")))
                    val fromIdx = c.getColumnIndexOrThrow("fromAccountId")
                    if (!c.isNull(fromIdx)) put("fromAccountId", c.getLong(fromIdx))
                    val toIdx = c.getColumnIndexOrThrow("toAccountId")
                    if (!c.isNull(toIdx)) put("toAccountId", c.getLong(toIdx))
                    val catIdx = c.getColumnIndexOrThrow("categoryId")
                    if (!c.isNull(catIdx)) put("categoryId", c.getLong(catIdx))
                    put("note", c.getString(c.getColumnIndexOrThrow("note")))
                    put("dateEpochMs", c.getLong(c.getColumnIndexOrThrow("dateEpochMs")))
                    put("createdAtEpochMs", c.getLong(c.getColumnIndexOrThrow("createdAtEpochMs")))
                }
                txArray.put(obj)
            }
        }
        root.put("transactions", txArray)

        // Needs
        val needsArray = JSONArray()
        db.query("SELECT * FROM needs", null).use { c ->
            while (c.moveToNext()) {
                val obj = JSONObject().apply {
                    put("id", c.getLong(c.getColumnIndexOrThrow("id")))
                    put("name", c.getString(c.getColumnIndexOrThrow("name")))
                    val priceIdx = c.getColumnIndexOrThrow("expectedPricePaisa")
                    if (!c.isNull(priceIdx)) put("expectedPricePaisa", c.getLong(priceIdx))
                    put("whereToGet", c.getString(c.getColumnIndexOrThrow("whereToGet")))
                    put("externalUrl", c.getString(c.getColumnIndexOrThrow("externalUrl")))
                    put("note", c.getString(c.getColumnIndexOrThrow("note")))
                    put("isDone", c.getInt(c.getColumnIndexOrThrow("isDone")) == 1)
                    put("createdAtEpochMs", c.getLong(c.getColumnIndexOrThrow("createdAtEpochMs")))
                }
                needsArray.put(obj)
            }
        }
        root.put("needs", needsArray)

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

            // Restore Accounts
            val accounts = mutableListOf<AccountEntity>()
            for (i in 0 until accountsArray.length()) {
                val obj = accountsArray.getJSONObject(i)
                accounts.add(
                    AccountEntity(
                        id = obj.optLong("id", 0),
                        name = obj.getString("name"),
                        type = AccountType.valueOf(obj.getString("type")),
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
                        dateEpochMs = obj.optLong("dateEpochMs", System.currentTimeMillis())
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

            Result.success("Restored ${accounts.size} accounts, ${categories.size} categories, ${txArray.length()} transactions, and ${needsArray.length()} needs.")
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
