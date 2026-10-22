package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import com.example.ui.theme.SemanticExpense
import com.example.ui.theme.SemanticIncome
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.data.model.AccountEntity
import com.example.data.model.AccountWithBalance
import com.example.data.model.BudgetProgressItem
import com.example.data.model.KarzSummary
import com.example.data.model.MonthlySummary
import com.example.data.model.PendingRecurringItem
import com.example.data.model.ReservedAmountEntity
import com.example.data.model.TransactionItem
import com.example.ui.components.AddAccountDialog
import com.example.ui.components.BalanceCard
import com.example.ui.components.BudgetSectionCard
import com.example.ui.components.ClayEmptyState
import com.example.ui.components.KarzSummaryCard
import com.example.ui.components.MonthlySummaryCard
import com.example.ui.components.PendingRecurringCard
import com.example.ui.components.ReservedMoneyDialog
import com.example.ui.components.TransactionRow
import com.example.ui.charts.ClayAnalyticsSection
import com.example.ui.components.ExpressSpeedDialFAB
import com.example.ui.theme.ClayAccentLime
import com.example.ui.theme.ClayBorderHairline
import com.example.ui.theme.ClayInkDeep
import com.example.ui.theme.ClaySurface
import com.example.ui.theme.ClayTextPrimary
import com.example.ui.theme.ClayTextSecondary

enum class HomeTab {
    TRANSACTIONS,
    ANALYTICS
}

@Composable
fun HomeScreen(
    totalBalancePaisa: Long,
    remainingBalancePaisa: Long,
    reservedPaisa: Long,
    reservedAmounts: List<ReservedAmountEntity> = emptyList(),
    accounts: List<AccountWithBalance>,
    monthlySummary: MonthlySummary,
    transactions: List<TransactionItem>,
    pendingRecurring: List<PendingRecurringItem>,
    budgets: List<BudgetProgressItem>,
    privacyMode: Boolean,
    karzSummary: KarzSummary? = null,
    onNavigateToKarz: () -> Unit = {},
    onTogglePrivacy: () -> Unit,
    onConfirmRecurring: (Long) -> Unit,
    onSkipRecurring: (Long) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onUndoDelete: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onUpdateAccount: (AccountEntity) -> Unit = {},
    onAddReserve: (title: String, amountPaisa: Long, note: String) -> Unit = { _, _, _ -> },
    onToggleReserveActive: (ReservedAmountEntity) -> Unit = {},
    onDeleteReserve: (ReservedAmountEntity) -> Unit = {},
    onNavigateToDebtDetail: (Long) -> Unit = {},
    onQuickLog: (title: String, amountPaisa: Long, categoryName: String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<AccountEntity?>(null) }
    var showManageReservesDialog by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(HomeTab.TRANSACTIONS) }

    val filteredTransactions = remember(transactions, selectedAccountId, searchQuery) {
        transactions.filter { item ->
            val matchesAccount = selectedAccountId == null ||
                    item.transaction.fromAccountId == selectedAccountId ||
                    item.transaction.toAccountId == selectedAccountId

            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                (item.categoryName?.lowercase()?.contains(q) == true) ||
                        (item.transaction.note.lowercase().contains(q)) ||
                        (item.fromAccountName?.lowercase()?.contains(q) == true) ||
                        (item.toAccountName?.lowercase()?.contains(q) == true)
            }
            matchesAccount && matchesSearch
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExpressSpeedDialFAB(
                onQuickLog = { title, amountPaisa, categoryName ->
                    onQuickLog(title, amountPaisa, categoryName)
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        val result = snackbarHostState.showSnackbar(
                            message = "Logged ${com.example.domain.Money.formatPkr(amountPaisa)} for $title",
                            actionLabel = "Undo",
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            onUndoDelete()
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 600.dp
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home_ledger_list"),
                contentPadding = PaddingValues(horizontal = if (isWideScreen) 24.dp else 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
            // Header: Wordmark & Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(ClayAccentLime),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(ClayInkDeep)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Hisaab Do Bhai",
                                style = MaterialTheme.typography.headlineMedium,
                                color = ClayTextPrimary
                            )
                            Text(
                                text = "Quiet Ledger · 100% Offline",
                                style = MaterialTheme.typography.labelSmall,
                                color = ClayTextSecondary
                            )
                        }
                    }

                    Row {
                        IconButton(
                            onClick = { isSearchVisible = !isSearchVisible },
                            modifier = Modifier.testTag("search_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isSearchVisible) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Search ledger",
                                tint = ClayTextSecondary
                            )
                        }
                    }
                }
            }

            // Search bar (if expanded)
            if (isSearchVisible) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by category, note, or account...") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = ClayTextSecondary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = ClayTextSecondary)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClayInkDeep,
                            unfocusedBorderColor = ClayBorderHairline,
                            focusedContainerColor = ClaySurface,
                            unfocusedContainerColor = ClaySurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_text_input")
                    )
                }
            }

            // Pending Recurring Payments Banner
            if (pendingRecurring.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        pendingRecurring.forEach { item ->
                            PendingRecurringCard(
                                item = item,
                                onConfirm = onConfirmRecurring,
                                onSkip = onSkipRecurring
                            )
                        }
                    }
                }
            }

            // Total Balance Card
            item {
                BalanceCard(
                    totalBalancePaisa = totalBalancePaisa,
                    remainingBalancePaisa = remainingBalancePaisa,
                    reservedPaisa = reservedPaisa,
                    accounts = accounts,
                    selectedAccountId = selectedAccountId,
                    onAccountSelected = { selectedAccountId = it },
                    onEditAccount = { accountToEdit = it },
                    onManageReserves = { showManageReservesDialog = true },
                    privacyMode = privacyMode,
                    onTogglePrivacy = onTogglePrivacy
                )
            }

            // Quick Action Strip (Expense, Income, Transfer, Karz)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick Action: Add Expense
                    Surface(
                        onClick = { onNavigateToAdd() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_action_expense"),
                        color = ClaySurface,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ClayBorderHairline)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SemanticExpense.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.CallMade,
                                    contentDescription = "Expense",
                                    tint = SemanticExpense,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Expense",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ClayTextPrimary
                            )
                        }
                    }

                    // Quick Action: Add Income
                    Surface(
                        onClick = { onNavigateToAdd() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_action_income"),
                        color = ClaySurface,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ClayBorderHairline)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SemanticIncome.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.CallReceived,
                                    contentDescription = "Income",
                                    tint = SemanticIncome,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Income",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ClayTextPrimary
                            )
                        }
                    }

                    // Quick Action: Transfer
                    Surface(
                        onClick = { onNavigateToAdd() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_action_transfer"),
                        color = ClaySurface,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ClayBorderHairline)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(ClayAccentLime.copy(alpha = 0.45f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Transfer",
                                    tint = ClayInkDeep,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Transfer",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ClayTextPrimary
                            )
                        }
                    }

                    // Quick Action: Karz
                    Surface(
                        onClick = { onNavigateToKarz() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_action_karz"),
                        color = ClaySurface,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ClayBorderHairline)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(ClayInkDeep.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Handshake,
                                    contentDescription = "Karz",
                                    tint = ClayInkDeep,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Karz",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ClayTextPrimary
                            )
                        }
                    }
                }
            }

            // 30 Days Activity Summary & Karz Cards
            if (isWideScreen && karzSummary != null) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            MonthlySummaryCard(
                                summary = monthlySummary,
                                privacyMode = privacyMode
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            KarzSummaryCard(
                                summary = karzSummary,
                                privacyMode = privacyMode,
                                onViewAll = onNavigateToKarz
                            )
                        }
                    }
                }
            } else {
                item {
                    MonthlySummaryCard(
                        summary = monthlySummary,
                        privacyMode = privacyMode
                    )
                }

                if (karzSummary != null) {
                    item {
                        KarzSummaryCard(
                            summary = karzSummary,
                            privacyMode = privacyMode,
                            onViewAll = onNavigateToKarz
                        )
                    }
                }
            }

            // Budget Progress Section
            if (budgets.isNotEmpty()) {
                item {
                    BudgetSectionCard(budgets = budgets)
                }
            }

            // Tab Switcher between Ledger and Visual Analytics
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ClaySurface)
                        .border(1.dp, ClayBorderHairline, RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (currentTab == HomeTab.TRANSACTIONS) ClayInkDeep else androidx.compose.ui.graphics.Color.Transparent)
                            .clickable { currentTab = HomeTab.TRANSACTIONS }
                            .padding(vertical = 8.dp)
                            .testTag("tab_transactions"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📑 Ledger (${filteredTransactions.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (currentTab == HomeTab.TRANSACTIONS) FontWeight.Bold else FontWeight.Normal,
                            color = if (currentTab == HomeTab.TRANSACTIONS) ClayAccentLime else ClayTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (currentTab == HomeTab.ANALYTICS) ClayInkDeep else androidx.compose.ui.graphics.Color.Transparent)
                            .clickable { currentTab = HomeTab.ANALYTICS }
                            .padding(vertical = 8.dp)
                            .testTag("tab_analytics"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📊 Visual Charts & Analytics",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (currentTab == HomeTab.ANALYTICS) FontWeight.Bold else FontWeight.Normal,
                            color = if (currentTab == HomeTab.ANALYTICS) ClayAccentLime else ClayTextSecondary
                        )
                    }
                }
            }

            if (currentTab == HomeTab.ANALYTICS) {
                item {
                    ClayAnalyticsSection(
                        transactions = transactions,
                        monthlySummary = monthlySummary,
                        karzSummary = karzSummary
                    )
                }
            } else {
                // Empty State or Transactions List
                if (filteredTransactions.isEmpty()) {
                item {
                    ClayEmptyState(
                        icon = Icons.Default.MenuBook,
                        title = if (searchQuery.isNotEmpty()) "No matching entries" else "No entries yet",
                        description = if (searchQuery.isNotEmpty()) "Try adjusting your search query or filters." else "Tap + below to record your first expense, income, or transfer. Your ledger is 100% offline.",
                        actionTitle = if (searchQuery.isEmpty()) "+ Record Hisaab" else null,
                        onAction = onNavigateToAdd,
                        illustrationRes = if (searchQuery.isEmpty()) com.example.R.drawable.img_clay_ledger_hero else null
                    )
                }
            } else {
                items(
                    items = filteredTransactions,
                    key = { it.transaction.id }
                ) { item ->
                    TransactionRow(
                        item = item,
                        privacyMode = privacyMode,
                        onNavigateToDebtDetail = onNavigateToDebtDetail,
                        onDelete = {
                            val txId = item.transaction.id
                            onDeleteTransaction(txId)
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                val result = snackbarHostState.showSnackbar(
                                    message = "Transaction deleted",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    onUndoDelete()
                                }
                            }
                        }
                    )
                }
            }
        }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
        }
    }

    if (accountToEdit != null) {
        AddAccountDialog(
            initialAccount = accountToEdit,
            onDismiss = { accountToEdit = null },
            onSave = { name, type, openingBalance, colorHex ->
                accountToEdit?.let { current ->
                    onUpdateAccount(
                        current.copy(
                            name = name,
                            type = type,
                            openingBalancePaisa = openingBalance,
                            colorHex = colorHex
                        )
                    )
                }
                accountToEdit = null
            }
        )
    }

    if (showManageReservesDialog) {
        ReservedMoneyDialog(
            reservedAmounts = reservedAmounts,
            onAddReserve = onAddReserve,
            onToggleActive = onToggleReserveActive,
            onDelete = onDeleteReserve,
            onDismiss = { showManageReservesDialog = false }
        )
    }
}
