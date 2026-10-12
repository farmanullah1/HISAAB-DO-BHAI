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
import com.example.data.model.NeedEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

data class AccountBalanceRecord(
    val accountId: Long,
    val balancePaisa: Long
)

@Dao
interface HisaabDao {

    // --- Accounts ---
    @Query("SELECT * FROM accounts ORDER BY isArchived ASC, id ASC")
    fun getAllAccountsFlow(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE isArchived = 0 ORDER BY id ASC")
    fun getActiveAccountsFlow(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("""
        SELECT COALESCE(openingBalancePaisa, 0) +
          COALESCE((SELECT SUM(amountPaisa) FROM transactions WHERE type = 'INCOME' AND toAccountId = :id), 0) -
          COALESCE((SELECT SUM(amountPaisa) FROM transactions WHERE type = 'EXPENSE' AND fromAccountId = :id), 0) +
          COALESCE((SELECT SUM(amountPaisa) FROM transactions WHERE type = 'TRANSFER' AND toAccountId = :id), 0) -
          COALESCE((SELECT SUM(amountPaisa) FROM transactions WHERE type = 'TRANSFER' AND fromAccountId = :id), 0)
        FROM accounts WHERE id = :id
    """)
    fun getAccountBalanceFlow(id: Long): Flow<Long?>

    @Query("""
        SELECT 
          a.id AS accountId,
          COALESCE(a.openingBalancePaisa, 0) +
          COALESCE((SELECT SUM(t1.amountPaisa) FROM transactions t1 WHERE t1.type = 'INCOME' AND t1.toAccountId = a.id), 0) -
          COALESCE((SELECT SUM(t2.amountPaisa) FROM transactions t2 WHERE t2.type = 'EXPENSE' AND t2.fromAccountId = a.id), 0) +
          COALESCE((SELECT SUM(t3.amountPaisa) FROM transactions t3 WHERE t3.type = 'TRANSFER' AND t3.toAccountId = a.id), 0) -
          COALESCE((SELECT SUM(t4.amountPaisa) FROM transactions t4 WHERE t4.type = 'TRANSFER' AND t4.fromAccountId = a.id), 0) AS balancePaisa
        FROM accounts a
    """)
    fun getAllAccountBalancesFlow(): Flow<List<AccountBalanceRecord>>

    @Query("""
        SELECT 
          COALESCE((SELECT SUM(openingBalancePaisa) FROM accounts WHERE isArchived = 0), 0) +
          COALESCE((SELECT SUM(amountPaisa) FROM transactions WHERE type = 'INCOME' AND toAccountId IN (SELECT id FROM accounts WHERE isArchived = 0)), 0) -
          COALESCE((SELECT SUM(amountPaisa) FROM transactions WHERE type = 'EXPENSE' AND fromAccountId IN (SELECT id FROM accounts WHERE isArchived = 0)), 0)
    """)
    fun getTotalBalanceFlow(): Flow<Long?>

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

    // --- Transactions ---
    @Query("SELECT * FROM transactions ORDER BY dateEpochMs DESC, id DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE fromAccountId = :accountId OR toAccountId = :accountId ORDER BY dateEpochMs DESC, id DESC")
    fun getTransactionsForAccountFlow(accountId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

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

    // --- Needs / Wishlist ---
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

    // Counts for integrity check
    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getAccountsCount(): Int

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoriesCount(): Int

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionsCount(): Int

    @Query("SELECT COUNT(*) FROM needs")
    suspend fun getNeedsCount(): Int
}
