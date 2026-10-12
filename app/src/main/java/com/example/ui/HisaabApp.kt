package com.example.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.AccentSage
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
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
                            selectedIconColor = AccentSage,
                            selectedTextColor = AccentSage,
                            indicatorColor = AccentSage.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.secondary,
                            unselectedTextColor = MaterialTheme.colorScheme.secondary
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
                    onSaveExpense = { amount, fromAcc, cat, note, date ->
                        viewModel.recordExpense(amount, fromAcc, cat, note, date)
                    },
                    onSaveIncome = { amount, toAcc, cat, note, date ->
                        viewModel.recordIncome(amount, toAcc, cat, note, date)
                    },
                    onSaveTransfer = { amount, fromAcc, toAcc, note, date ->
                        viewModel.recordTransfer(amount, fromAcc, toAcc, note, date)
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
                    onAddNeed = { name, price, where, note ->
                        viewModel.addNeed(name, price, where, note)
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
                    onAddAccount = { name, type, opening, color ->
                        viewModel.addAccount(name, type, opening, color)
                    },
                    onUpdateAccount = { viewModel.updateAccount(it) },
                    onAddCategory = { name, type, icon, color, limit ->
                        viewModel.addCategory(name, type, icon, color, limit)
                    },
                    onUpdateCategory = { viewModel.updateCategory(it) },
                    onExportJson = { viewModel.exportJsonFile() }
                )
            }
        }
    }
}
