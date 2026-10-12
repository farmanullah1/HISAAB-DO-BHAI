package com.example.data.repository

import android.content.Context
import com.example.data.database.HisaabDao
import com.example.data.database.HisaabDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.AccountWithBalance
import com.example.data.model.CategoryEntity
import com.example.data.model.CategoryType
import com.example.data.model.MonthlySummary
import com.example.data.model.NeedEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Calendar

class HisaabRepository(
    private val context: Context,
    private val dao: HisaabDao
) {

    private val prefs = context.getSharedPreferences("hisaab_prefs", Context.MODE_PRIVATE)

    suspend fun seedInitialDataIfNeeded() = withContext(Dispatchers.IO) {
        val alreadySeeded = prefs.getBoolean("hisaab_seeded_v1", false)
        if (alreadySeeded) return@withContext

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
                // Expense categories
                CategoryEntity(name = "Food & Dining", type = CategoryType.EXPENSE, iconKey = "restaurant", colorHex = "#E07A5F"),
                CategoryEntity(name = "Groceries", type = CategoryType.EXPENSE, iconKey = "shopping_cart", colorHex = "#81B29A"),
                CategoryEntity(name = "Fuel & Transport", type = CategoryType.EXPENSE, iconKey = "directions_car", colorHex = "#F2CC8F"),
                CategoryEntity(name = "Bills & Utilities", type = CategoryType.EXPENSE, iconKey = "receipt_long", colorHex = "#3D5A80"),
                CategoryEntity(name = "Rent & Housing", type = CategoryType.EXPENSE, iconKey = "home", colorHex = "#9B5DE5"),
                CategoryEntity(name = "Healthcare", type = CategoryType.EXPENSE, iconKey = "medical_services", colorHex = "#E76F51"),
                CategoryEntity(name = "Shopping", type = CategoryType.EXPENSE, iconKey = "shopping_bag", colorHex = "#F4A261"),
                CategoryEntity(name = "Education", type = CategoryType.EXPENSE, iconKey = "school", colorHex = "#2A9D8F"),
                CategoryEntity(name = "Other Expense", type = CategoryType.EXPENSE, iconKey = "more_horiz", colorHex = "#8A8F99"),
                // Income categories
                CategoryEntity(name = "Salary", type = CategoryType.INCOME, iconKey = "payments", colorHex = "#6BA88A"),
                CategoryEntity(name = "Freelance", type = CategoryType.INCOME, iconKey = "laptop", colorHex = "#457B9D"),
                CategoryEntity(name = "Gift", type = CategoryType.INCOME, iconKey = "redeem", colorHex = "#E9C46A"),
                CategoryEntity(name = "Refund", type = CategoryType.INCOME, iconKey = "replay", colorHex = "#A8DADC"),
                CategoryEntity(name = "Other Income", type = CategoryType.INCOME, iconKey = "more_horiz", colorHex = "#8A8F99")
            )
            dao.insertCategories(defaultCategories)
        }

        // Never seed transactions or fake balances!
        prefs.edit().putBoolean("hisaab_seeded_v1", true).apply()
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
        // Last 30 days window
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

    // --- Actions ---

    suspend fun recordExpense(
        amountPaisa: Long,
        fromAccountId: Long,
        categoryId: Long,
        note: String,
        dateEpochMs: Long
    ): Long = withContext(Dispatchers.IO) {
        require(amountPaisa > 0) { "Amount must be greater than zero" }
        val tx = TransactionEntity(
            type = TransactionType.EXPENSE,
            amountPaisa = amountPaisa,
            fromAccountId = fromAccountId,
            toAccountId = null,
            categoryId = categoryId,
            note = note.trim(),
            dateEpochMs = dateEpochMs
        )
        dao.insertTransaction(tx)
    }

    suspend fun recordIncome(
        amountPaisa: Long,
        toAccountId: Long,
        categoryId: Long,
        note: String,
        dateEpochMs: Long
    ): Long = withContext(Dispatchers.IO) {
        require(amountPaisa > 0) { "Amount must be greater than zero" }
        val tx = TransactionEntity(
            type = TransactionType.INCOME,
            amountPaisa = amountPaisa,
            fromAccountId = null,
            toAccountId = toAccountId,
            categoryId = categoryId,
            note = note.trim(),
            dateEpochMs = dateEpochMs
        )
        dao.insertTransaction(tx)
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

    suspend fun addNeed(
        name: String,
        expectedPricePaisa: Long?,
        whereToGet: String,
        note: String
    ): Long = withContext(Dispatchers.IO) {
        require(name.isNotBlank()) { "Need name cannot be blank" }
        val need = NeedEntity(
            name = name.trim(),
            expectedPricePaisa = expectedPricePaisa,
            whereToGet = whereToGet.trim(),
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

    /**
     * Exports full account, category, transaction, and needs data as validated JSON.
     * Writes to app files directory and returns the absolute file path.
     */
    suspend fun exportDataAsJsonFile(): File = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "Hisaab")
        root.put("version", "1.0")
        root.put("exportedAtEpochMs", System.currentTimeMillis())

        val accountsArray = JSONArray()
        // Wait for first list
        dao.getAllAccountsFlow()
        val accounts = dao.getAccountsCount()
        // We can query database directly
        val db = HisaabDatabase.getInstance(context)
        val allAccountsList = withContext(Dispatchers.IO) {
            val cursor = db.query("SELECT * FROM accounts", null)
            val list = mutableListOf<JSONObject>()
            cursor.use { c ->
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
                    list.add(obj)
                }
            }
            list
        }
        allAccountsList.forEach { accountsArray.put(it) }
        root.put("accounts", accountsArray)

        val categoriesArray = JSONArray()
        val allCategoriesList = withContext(Dispatchers.IO) {
            val cursor = db.query("SELECT * FROM categories", null)
            val list = mutableListOf<JSONObject>()
            cursor.use { c ->
                while (c.moveToNext()) {
                    val obj = JSONObject().apply {
                        put("id", c.getLong(c.getColumnIndexOrThrow("id")))
                        put("name", c.getString(c.getColumnIndexOrThrow("name")))
                        put("type", c.getString(c.getColumnIndexOrThrow("type")))
                        put("iconKey", c.getString(c.getColumnIndexOrThrow("iconKey")))
                        put("colorHex", c.getString(c.getColumnIndexOrThrow("colorHex")))
                        val limitIdx = c.getColumnIndexOrThrow("monthlyLimitPaisa")
                        if (!c.isNull(limitIdx)) {
                            put("monthlyLimitPaisa", c.getLong(limitIdx))
                        }
                        put("createdAtEpochMs", c.getLong(c.getColumnIndexOrThrow("createdAtEpochMs")))
                    }
                    list.add(obj)
                }
            }
            list
        }
        allCategoriesList.forEach { categoriesArray.put(it) }
        root.put("categories", categoriesArray)

        val txArray = JSONArray()
        val allTxList = withContext(Dispatchers.IO) {
            val cursor = db.query("SELECT * FROM transactions", null)
            val list = mutableListOf<JSONObject>()
            cursor.use { c ->
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
                    list.add(obj)
                }
            }
            list
        }
        allTxList.forEach { txArray.put(it) }
        root.put("transactions", txArray)

        val needsArray = JSONArray()
        val allNeedsList = withContext(Dispatchers.IO) {
            val cursor = db.query("SELECT * FROM needs", null)
            val list = mutableListOf<JSONObject>()
            cursor.use { c ->
                while (c.moveToNext()) {
                    val obj = JSONObject().apply {
                        put("id", c.getLong(c.getColumnIndexOrThrow("id")))
                        put("name", c.getString(c.getColumnIndexOrThrow("name")))
                        val priceIdx = c.getColumnIndexOrThrow("expectedPricePaisa")
                        if (!c.isNull(priceIdx)) put("expectedPricePaisa", c.getLong(priceIdx))
                        put("whereToGet", c.getString(c.getColumnIndexOrThrow("whereToGet")))
                        put("note", c.getString(c.getColumnIndexOrThrow("note")))
                        put("isDone", c.getInt(c.getColumnIndexOrThrow("isDone")) == 1)
                        put("createdAtEpochMs", c.getLong(c.getColumnIndexOrThrow("createdAtEpochMs")))
                    }
                    list.add(obj)
                }
            }
            list
        }
        allNeedsList.forEach { needsArray.put(it) }
        root.put("needs", needsArray)

        val exportDir = File(context.filesDir, "exports").apply { mkdirs() }
        val exportFile = File(exportDir, "hisaab_backup_${System.currentTimeMillis()}.json")
        exportFile.writeText(root.toString(2))
        exportFile
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
