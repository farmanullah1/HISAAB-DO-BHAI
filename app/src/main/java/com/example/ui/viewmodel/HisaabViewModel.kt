package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.AccountWithBalance
import com.example.data.model.BudgetProgressItem
import com.example.data.model.CategoryEntity
import com.example.data.model.CategoryType
import com.example.data.model.MonthlySummary
import com.example.data.model.NeedEntity
import com.example.data.model.PendingRecurringItem
import com.example.data.model.RecurringFrequency
import com.example.data.model.RecurringRuleEntity
import com.example.data.model.ReservedAmountEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import com.example.data.repository.HisaabRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class HisaabViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HisaabRepository.getInstance(application)

    private var lastDeletedTransaction: TransactionEntity? = null
    private var lastDeletedNeed: NeedEntity? = null

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }

    val privacyMode: StateFlow<Boolean> = repository.privacyMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setPrivacyMode(enabled: Boolean) {
        repository.setPrivacyMode(enabled)
    }

    val totalBalancePaisa: StateFlow<Long> = repository.totalBalancePaisa
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val remainingBalancePaisa: StateFlow<Long> = repository.remainingBalancePaisa
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val activeReservedTotalPaisa: StateFlow<Long> = repository.activeReservedTotalPaisa
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val reservedAmounts: StateFlow<List<ReservedAmountEntity>> = repository.reservedAmounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accountsWithBalances: StateFlow<List<AccountWithBalance>> = repository.accountsWithBalances
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAccounts: StateFlow<List<AccountEntity>> = repository.accountsWithBalances
        .map { list -> list.map { it.account } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseCategories: StateFlow<List<CategoryEntity>> = repository.getCategoriesByType(CategoryType.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeCategories: StateFlow<List<CategoryEntity>> = repository.getCategoriesByType(CategoryType.INCOME)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionItem>> = repository.transactionsWithDetails
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlySummary: StateFlow<MonthlySummary> = repository.monthlySummary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthlySummary(0L, 0L))

    val activeNeeds: StateFlow<List<NeedEntity>> = repository.activeNeeds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val doneNeeds: StateFlow<List<NeedEntity>> = repository.doneNeeds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgetProgressList: StateFlow<List<BudgetProgressItem>> = repository.budgetProgressList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingRecurringItems: StateFlow<List<PendingRecurringItem>> = repository.pendingRecurringItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurringRules: StateFlow<List<RecurringRuleEntity>> = repository.recurringRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun recordExpense(
        amountPaisa: Long,
        fromAccountId: Long,
        categoryId: Long,
        note: String,
        dateEpochMs: Long,
        receiptPath: String? = null
    ) {
        viewModelScope.launch {
            repository.recordExpense(amountPaisa, fromAccountId, categoryId, note, dateEpochMs, receiptPath)
        }
    }

    fun recordIncome(
        amountPaisa: Long,
        toAccountId: Long,
        categoryId: Long,
        note: String,
        dateEpochMs: Long,
        receiptPath: String? = null
    ) {
        viewModelScope.launch {
            repository.recordIncome(amountPaisa, toAccountId, categoryId, note, dateEpochMs, receiptPath)
        }
    }

    fun recordTransfer(amountPaisa: Long, fromAccountId: Long, toAccountId: Long, note: String, dateEpochMs: Long) {
        viewModelScope.launch {
            repository.recordTransfer(amountPaisa, fromAccountId, toAccountId, note, dateEpochMs)
        }
    }

    fun deleteTransaction(txId: Long) {
        viewModelScope.launch {
            val deleted = repository.deleteTransaction(txId)
            lastDeletedTransaction = deleted
        }
    }

    fun undoDeleteTransaction() {
        val tx = lastDeletedTransaction ?: return
        viewModelScope.launch {
            repository.restoreTransaction(tx)
            lastDeletedTransaction = null
        }
    }

    suspend fun saveReceiptImage(uri: Uri): String? {
        return repository.saveReceiptImage(uri)
    }

    fun confirmRecurringPayment(occurrenceId: Long) {
        viewModelScope.launch {
            repository.confirmRecurringPayment(occurrenceId)
        }
    }

    fun skipRecurringPayment(occurrenceId: Long) {
        viewModelScope.launch {
            repository.skipRecurringPayment(occurrenceId)
        }
    }

    fun addRecurringRule(
        title: String,
        type: TransactionType,
        amountPaisa: Long,
        accountId: Long,
        categoryId: Long,
        frequency: RecurringFrequency,
        startDateEpochMs: Long,
        note: String
    ) {
        viewModelScope.launch {
            repository.addRecurringRule(title, type, amountPaisa, accountId, categoryId, frequency, startDateEpochMs, note)
        }
    }

    fun deleteRecurringRule(rule: RecurringRuleEntity) {
        viewModelScope.launch {
            repository.deleteRecurringRule(rule)
        }
    }

    fun addNeed(name: String, pricePaisa: Long?, where: String, externalUrl: String, note: String) {
        viewModelScope.launch {
            repository.addNeed(name, pricePaisa, where, externalUrl, note)
        }
    }

    fun updateNeed(need: NeedEntity) {
        viewModelScope.launch {
            repository.updateNeed(need)
        }
    }

    fun toggleNeedDone(need: NeedEntity) {
        viewModelScope.launch {
            repository.toggleNeedDone(need)
        }
    }

    fun deleteNeed(needId: Long) {
        viewModelScope.launch {
            val deleted = repository.deleteNeed(needId)
            lastDeletedNeed = deleted
        }
    }

    fun undoDeleteNeed() {
        val need = lastDeletedNeed ?: return
        viewModelScope.launch {
            repository.restoreNeed(need)
            lastDeletedNeed = null
        }
    }

    fun clearAllDoneNeeds() {
        viewModelScope.launch {
            repository.clearAllDoneNeeds()
        }
    }

    fun addAccount(name: String, type: AccountType, openingBalancePaisa: Long, colorHex: String) {
        viewModelScope.launch {
            repository.addAccount(name, type, openingBalancePaisa, colorHex)
        }
    }

    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun addCategory(name: String, type: CategoryType, iconKey: String, colorHex: String, limitPaisa: Long?) {
        viewModelScope.launch {
            repository.addCategory(name, type, iconKey, colorHex, limitPaisa)
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun addReservedAmount(title: String, amountPaisa: Long, targetDateEpochMs: Long?, note: String) {
        viewModelScope.launch {
            repository.addReservedAmount(title, amountPaisa, targetDateEpochMs, note)
        }
    }

    fun toggleReservedAmountActive(reserved: ReservedAmountEntity) {
        viewModelScope.launch {
            repository.toggleReservedAmountActive(reserved)
        }
    }

    fun deleteReservedAmount(reserved: ReservedAmountEntity) {
        viewModelScope.launch {
            repository.deleteReservedAmount(reserved)
        }
    }

    suspend fun exportJsonFile(): File {
        return repository.exportDataAsJsonFile()
    }

    suspend fun restoreDataFromJson(jsonString: String): Result<String> {
        return repository.restoreDataFromJson(jsonString)
    }
}
