package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.data.model.AccountWithBalance
import com.example.data.model.BudgetProgressItem
import com.example.data.model.MonthlySummary
import com.example.data.model.PendingRecurringItem
import com.example.data.model.TransactionItem
import com.example.ui.components.BalanceCard
import com.example.ui.components.BudgetSectionCard
import com.example.ui.components.ClayEmptyState
import com.example.ui.components.MonthlySummaryCard
import com.example.ui.components.PendingRecurringCard
import com.example.ui.components.TransactionRow
import com.example.ui.theme.ClayAccentLime
import com.example.ui.theme.ClayBorderHairline
import com.example.ui.theme.ClayInkDeep
import com.example.ui.theme.ClaySurface
import com.example.ui.theme.ClayTextPrimary
import com.example.ui.theme.ClayTextSecondary

@Composable
fun HomeScreen(
    totalBalancePaisa: Long,
    accounts: List<AccountWithBalance>,
    monthlySummary: MonthlySummary,
    transactions: List<TransactionItem>,
    pendingRecurring: List<PendingRecurringItem>,
    budgets: List<BudgetProgressItem>,
    privacyMode: Boolean,
    onTogglePrivacy: () -> Unit,
    onConfirmRecurring: (Long) -> Unit,
    onSkipRecurring: (Long) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onUndoDelete: () -> Unit,
    onNavigateToAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }

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
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("home_ledger_list"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
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
                    accounts = accounts,
                    selectedAccountId = selectedAccountId,
                    onAccountSelected = { selectedAccountId = it },
                    privacyMode = privacyMode,
                    onTogglePrivacy = onTogglePrivacy
                )
            }

            // 30 Days Activity Summary Card
            item {
                MonthlySummaryCard(
                    summary = monthlySummary,
                    privacyMode = privacyMode
                )
            }

            // Budget Progress Section
            if (budgets.isNotEmpty()) {
                item {
                    BudgetSectionCard(budgets = budgets)
                }
            }

            // Ledger Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedAccountId == null) "LEDGER ENTRIES" else "FILTERED ENTRIES",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClayTextSecondary,
                        letterSpacing = 0.8.sp
                    )

                    Text(
                        text = "${filteredTransactions.size} records",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClayTextSecondary
                    )
                }
            }

            // Empty State or Transactions List
            if (filteredTransactions.isEmpty()) {
                item {
                    ClayEmptyState(
                        icon = Icons.Default.MenuBook,
                        title = if (searchQuery.isNotEmpty()) "No matching entries" else "No entries yet",
                        description = if (searchQuery.isNotEmpty()) "Try adjusting your search query or filters." else "Tap + below to record your first expense, income, or transfer. Your ledger is 100% offline.",
                        actionTitle = if (searchQuery.isEmpty()) "+ Record Hisaab" else null,
                        onAction = onNavigateToAdd
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

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
