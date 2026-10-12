package com.example.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.NavScreen
import com.example.ui.screens.AddTransactionScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NeedsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ClayAccentLime
import com.example.ui.theme.ClayInkDeep
import com.example.ui.theme.ClayTextSecondary
import com.example.ui.viewmodel.HisaabViewModel

@Composable
fun HisaabApp(
    viewModel: HisaabViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val totalBalancePaisa by viewModel.totalBalancePaisa.collectAsStateWithLifecycle()
    val accountsWithBalances by viewModel.accountsWithBalances.collectAsStateWithLifecycle()
    val activeAccounts by viewModel.activeAccounts.collectAsStateWithLifecycle()
    val expenseCategories by viewModel.expenseCategories.collectAsStateWithLifecycle()
    val incomeCategories by viewModel.incomeCategories.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val monthlySummary by viewModel.monthlySummary.collectAsStateWithLifecycle()
    val activeNeeds by viewModel.activeNeeds.collectAsStateWithLifecycle()
    val doneNeeds by viewModel.doneNeeds.collectAsStateWithLifecycle()
    val pendingRecurring by viewModel.pendingRecurringItems.collectAsStateWithLifecycle()
    val budgetProgressList by viewModel.budgetProgressList.collectAsStateWithLifecycle()
    val recurringRules by viewModel.recurringRules.collectAsStateWithLifecycle()
    val privacyMode by viewModel.privacyMode.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .testTag("bottom_nav_bar")
            ) {
                NavScreen.items.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ClayInkDeep,
                            selectedTextColor = ClayInkDeep,
                            indicatorColor = ClayAccentLime,
                            unselectedIconColor = ClayTextSecondary,
                            unselectedTextColor = ClayTextSecondary
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavScreen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavScreen.Home.route) {
                HomeScreen(
                    totalBalancePaisa = totalBalancePaisa,
                    accounts = accountsWithBalances,
                    monthlySummary = monthlySummary,
                    transactions = transactions,
                    pendingRecurring = pendingRecurring,
                    budgets = budgetProgressList,
                    privacyMode = privacyMode,
                    onTogglePrivacy = { viewModel.setPrivacyMode(!privacyMode) },
                    onConfirmRecurring = { viewModel.confirmRecurringPayment(it) },
                    onSkipRecurring = { viewModel.skipRecurringPayment(it) },
                    onDeleteTransaction = { viewModel.deleteTransaction(it) },
                    onUndoDelete = { viewModel.undoDeleteTransaction() },
                    onNavigateToAdd = {
                        navController.navigate(NavScreen.Add.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(NavScreen.Add.route) {
                AddTransactionScreen(
                    accounts = activeAccounts,
                    expenseCategories = expenseCategories,
                    incomeCategories = incomeCategories,
                    onSaveExpense = { amount, fromAcc, cat, note, date, receipt ->
                        viewModel.recordExpense(amount, fromAcc, cat, note, date, receipt)
                    },
                    onSaveIncome = { amount, toAcc, cat, note, date, receipt ->
                        viewModel.recordIncome(amount, toAcc, cat, note, date, receipt)
                    },
                    onSaveTransfer = { amount, fromAcc, toAcc, note, date ->
                        viewModel.recordTransfer(amount, fromAcc, toAcc, note, date)
                    },
                    onSaveReceiptImage = { uri ->
                        viewModel.saveReceiptImage(uri)
                    },
                    onTransactionSaved = {
                        navController.navigate(NavScreen.Home.route) {
                            popUpTo(NavScreen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavScreen.Needs.route) {
                NeedsScreen(
                    activeNeeds = activeNeeds,
                    doneNeeds = doneNeeds,
                    onAddNeed = { name, price, where, url, note ->
                        viewModel.addNeed(name, price, where, url, note)
                    },
                    onUpdateNeed = { viewModel.updateNeed(it) },
                    onToggleDone = { viewModel.toggleNeedDone(it) },
                    onDeleteNeed = { viewModel.deleteNeed(it) },
                    onUndoDeleteNeed = { viewModel.undoDeleteNeed() },
                    onClearAllDone = { viewModel.clearAllDoneNeeds() }
                )
            }

            composable(NavScreen.Settings.route) {
                SettingsScreen(
                    accounts = accountsWithBalances,
                    categories = allCategories,
                    recurringRules = recurringRules,
                    onAddAccount = { name, type, opening, color ->
                        viewModel.addAccount(name, type, opening, color)
                    },
                    onUpdateAccount = { viewModel.updateAccount(it) },
                    onAddCategory = { name, type, icon, color, limit ->
                        viewModel.addCategory(name, type, icon, color, limit)
                    },
                    onUpdateCategory = { viewModel.updateCategory(it) },
                    onAddRecurringRule = { title, type, amount, accId, catId, freq, start, note ->
                        viewModel.addRecurringRule(title, type, amount, accId, catId, freq, start, note)
                    },
                    onDeleteRecurringRule = { viewModel.deleteRecurringRule(it) },
                    onExportJson = { viewModel.exportJsonFile() },
                    onRestoreJson = { viewModel.restoreDataFromJson(it) }
                )
            }
        }
    }
}
