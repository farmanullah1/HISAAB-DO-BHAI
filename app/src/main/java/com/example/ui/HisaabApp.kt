package com.example.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.remember
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.DebtDirection
import com.example.ui.navigation.NavScreen
import com.example.ui.screens.AddKarzScreen
import com.example.ui.screens.AddTransactionScreen
import com.example.ui.screens.DebtDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.KarzScreen
import com.example.ui.screens.NeedsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.PlansHubScreen
import com.example.ui.screens.KametiScreen
import com.example.ui.screens.ZakatScreen
import androidx.compose.foundation.border
import com.example.ui.security.BiometricLockScreen
import com.example.ui.theme.ClayAccentLime
import com.example.ui.theme.ClayBorderHairline
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
    val remainingBalancePaisa by viewModel.remainingBalancePaisa.collectAsStateWithLifecycle()
    val activeReservedTotalPaisa by viewModel.activeReservedTotalPaisa.collectAsStateWithLifecycle()
    val reservedAmounts by viewModel.reservedAmounts.collectAsStateWithLifecycle()
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
    val debts by viewModel.debts.collectAsStateWithLifecycle()
    val karzSummary by viewModel.karzSummary.collectAsStateWithLifecycle()
    val privacyMode by viewModel.privacyMode.collectAsStateWithLifecycle()
    val isLocked by viewModel.securityManager.isLocked.collectAsStateWithLifecycle()

    if (isLocked) {
        BiometricLockScreen(
            securityManager = viewModel.securityManager,
            onUnlocked = { viewModel.securityManager.unlock() }
        )
    } else {
        Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 840.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .border(1.dp, ClayBorderHairline, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
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
                            if (screen == NavScreen.Add) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) ClayAccentLime else ClayInkDeep),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = screen.title,
                                        tint = if (isSelected) ClayInkDeep else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            }
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
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 840.dp)
                    .fillMaxWidth()
            ) {
                NavHost(
                    navController = navController,
                    startDestination = NavScreen.Home.route,
                    modifier = Modifier.fillMaxSize()
                ) {
            composable(NavScreen.Home.route) {
                HomeScreen(
                    totalBalancePaisa = totalBalancePaisa,
                    remainingBalancePaisa = remainingBalancePaisa,
                    reservedPaisa = activeReservedTotalPaisa,
                    reservedAmounts = reservedAmounts,
                    accounts = accountsWithBalances,
                    monthlySummary = monthlySummary,
                    transactions = transactions,
                    pendingRecurring = pendingRecurring,
                    budgets = budgetProgressList,
                    privacyMode = privacyMode,
                    karzSummary = karzSummary,
                    onNavigateToKarz = {
                        navController.navigate(NavScreen.Karz.route) {
                            launchSingleTop = true
                        }
                    },
                    onTogglePrivacy = { viewModel.setPrivacyMode(!privacyMode) },
                    onConfirmRecurring = { viewModel.confirmRecurringPayment(it) },
                    onSkipRecurring = { viewModel.skipRecurringPayment(it) },
                    onDeleteTransaction = { viewModel.deleteTransaction(it) },
                    onUndoDelete = { viewModel.undoDeleteTransaction() },
                    onNavigateToAdd = {
                        navController.navigate(NavScreen.Add.route) {
                            launchSingleTop = true
                        }
                    },
                    onUpdateAccount = { viewModel.updateAccount(it) },
                    onAddReserve = { title, amount, note ->
                        viewModel.addReservedAmount(title, amount, null, note)
                    },
                    onToggleReserveActive = { viewModel.toggleReservedAmountActive(it) },
                    onDeleteReserve = { viewModel.deleteReservedAmount(it) },
                    onNavigateToDebtDetail = { debtId ->
                        navController.navigate("debt_detail/$debtId")
                    },
                    onQuickLog = { title, amountPaisa, categoryName ->
                        viewModel.recordQuickExpense(title, amountPaisa, categoryName)
                    }
                )
            }

            composable(NavScreen.Karz.route) {
                KarzScreen(
                    summary = karzSummary,
                    debts = debts,
                    privacyMode = privacyMode,
                    onNavigateToAddGave = {
                        navController.navigate("add_karz/I_GAVE")
                    },
                    onNavigateToAddTook = {
                        navController.navigate("add_karz/I_TOOK")
                    },
                    onDebtSelected = { debtId ->
                        navController.navigate("debt_detail/$debtId")
                    }
                )
            }

            composable("add_karz/{direction}") { backStackEntry ->
                val directionStr = backStackEntry.arguments?.getString("direction") ?: "I_GAVE"
                val dir = if (directionStr == "I_TOOK") DebtDirection.I_TOOK else DebtDirection.I_GAVE
                AddKarzScreen(
                    initialDirection = dir,
                    accounts = activeAccounts,
                    onSaveDebt = { person, direction, amount, date, due, desc, phone, notes, accId, trackCash ->
                        viewModel.createDebt(person, direction, amount, date, due, desc, phone, notes, accId, trackCash) {
                            navController.popBackStack()
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("debt_detail/{debtId}") { backStackEntry ->
                val debtId = backStackEntry.arguments?.getString("debtId")?.toLongOrNull() ?: 0L
                val debtDetailsFlow = remember(debtId) { viewModel.getDebtDetails(debtId) }
                val debtDetails by debtDetailsFlow.collectAsStateWithLifecycle(initialValue = null)
                val installmentsFlow = remember(debtId) { viewModel.getInstallmentsForDebt(debtId) }
                val installments by installmentsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

                DebtDetailScreen(
                    debtDetails = debtDetails,
                    accounts = activeAccounts,
                    privacyMode = privacyMode,
                    installments = installments,
                    onSplitInstallments = { id, count, isWeekly, firstDue ->
                        viewModel.splitDebtIntoInstallments(id, count, isWeekly, firstDue)
                    },
                    onPayInstallment = { inst, amount, accId, trackCash, note ->
                        viewModel.recordKarzInstallmentPayment(inst, amount, accId, trackCash, System.currentTimeMillis(), note)
                    },
                    onRecordRepayment = { id, amount, date, accId, trackCash, note ->
                        viewModel.recordDebtRepayment(id, amount, date, accId, trackCash, note)
                    },
                    onRecordAdvance = { id, amount, date, accId, trackCash, note ->
                        viewModel.recordAdditionalAdvance(id, amount, date, accId, trackCash, note)
                    },
                    onSettleWithAdjustment = { id, reason ->
                        viewModel.settleDebtWithAdjustment(id, reason)
                    },
                    onChangeDueDate = { id, newDue ->
                        viewModel.updateDebtDueDate(id, newDue)
                    },
                    onDeleteDebt = { id ->
                        viewModel.deleteDebt(id) {
                            navController.popBackStack()
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
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

            composable(NavScreen.Plans.route) {
                PlansHubScreen(
                    viewModel = viewModel,
                    privacyMode = privacyMode,
                    onNavigateToKameti = { navController.navigate(NavScreen.Kameti.route) },
                    onNavigateToZakat = { navController.navigate(NavScreen.Zakat.route) },
                    onNavigateToNeeds = { navController.navigate(NavScreen.Needs.route) }
                )
            }

            composable(NavScreen.Kameti.route) {
                KametiScreen(
                    viewModel = viewModel,
                    accounts = activeAccounts,
                    privacyMode = privacyMode,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavScreen.Zakat.route) {
                ZakatScreen(
                    viewModel = viewModel,
                    accounts = activeAccounts,
                    privacyMode = privacyMode,
                    onNavigateBack = { navController.popBackStack() }
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
    }
}
}
