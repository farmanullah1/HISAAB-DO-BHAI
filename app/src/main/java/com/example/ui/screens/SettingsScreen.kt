package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.AccountWithBalance
import com.example.data.model.CategoryEntity
import com.example.data.model.CategoryType
import com.example.domain.Money
import com.example.ui.components.AddAccountDialog
import com.example.ui.components.AddCategoryDialog
import com.example.ui.components.CategoryIconHelper
import com.example.ui.components.ReconciliationDialog
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SettingsScreen(
    accounts: List<AccountWithBalance>,
    categories: List<CategoryEntity>,
    onAddAccount: (name: String, type: AccountType, openingPaisa: Long, colorHex: String) -> Unit,
    onUpdateAccount: (AccountEntity) -> Unit,
    onAddCategory: (name: String, type: CategoryType, iconKey: String, colorHex: String, limitPaisa: Long?) -> Unit,
    onUpdateCategory: (CategoryEntity) -> Unit,
    onExportJson: suspend () -> File,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddAccountDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<AccountEntity?>(null) }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }

    var showReconciliationDialog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("settings_screen"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Screen Header
            item {
                Column {
                    Text(
                        text = "Settings & Data",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Manage your accounts, categories, and ledger export",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Accounts Section
            item {
                SectionCard(
                    title = "ACCOUNTS & BALANCES",
                    actionTitle = "+ Add Account",
                    onAction = { showAddAccountDialog = true }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        accounts.forEach { item ->
                            val color = runCatching {
                                Color(android.graphics.Color.parseColor(item.account.colorHex))
                            }.getOrDefault(MaterialTheme.colorScheme.primary)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { editingAccount = item.account }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = item.account.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = item.account.type.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }

                                Text(
                                    text = Money.formatPkr(item.balancePaisa),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Categories Section
            item {
                SectionCard(
                    title = "CATEGORIES & BUDGET LIMITS",
                    actionTitle = "+ Add Category",
                    onAction = { showAddCategoryDialog = true }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.forEach { cat ->
                            val color = runCatching {
                                Color(android.graphics.Color.parseColor(cat.colorHex))
                            }.getOrDefault(MaterialTheme.colorScheme.primary)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { editingCategory = cat }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(color.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = CategoryIconHelper.getIcon(cat.iconKey),
                                            contentDescription = null,
                                            tint = color,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = cat.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = cat.type.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }

                                cat.monthlyLimitPaisa?.let { limit ->
                                    Text(
                                        text = "Limit: ${Money.formatPkr(limit)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Financial Integrity & Reconciliation
            item {
                SectionCard(title = "INTEGRITY & TOOLS") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { showReconciliationDialog = true }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Balance,
                                contentDescription = "Reconciliation",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Account Reconciliation Tool",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Compare physical wallet/statement with Hisaab balance",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }

            // Data Export
            item {
                SectionCard(title = "DATA & EXPORT") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Export your entire ledger (accounts, categories, transactions, and needs) as a standard JSON archive.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                if (isExporting) return@Button
                                isExporting = true
                                scope.launch {
                                    try {
                                        val file = onExportJson()
                                        isExporting = false
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/json"
                                            putExtra(Intent.EXTRA_SUBJECT, "Hisaab Backup JSON")
                                            putExtra(Intent.EXTRA_TEXT, "Hisaab JSON Backup file created on ${file.name}")
                                        }
                                        context.startActivity(
                                            Intent.createChooser(shareIntent, "Save or share backup")
                                        )
                                        snackbarHostState.showSnackbar("Backup created: ${file.name}")
                                    } catch (e: Exception) {
                                        isExporting = false
                                        snackbarHostState.showSnackbar("Export failed: ${e.message}")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("export_json_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isExporting) "Exporting..." else "Export as JSON")
                        }
                    }
                }
            }

            // About Hisaab Section
            item {
                SectionCard(title = "ABOUT HISAAB") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "100% Offline & Local Ledger",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "Hisaab (Urdu for 'Account') operates entirely offline with zero cloud telemetry, no logins, and no trackers. All financial figures are stored strictly on your device using exact integer minor units.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Version 1.0 · Pakistani Rupee (PKR)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    if (showAddAccountDialog) {
        AddAccountDialog(
            initialAccount = null,
            onDismiss = { showAddAccountDialog = false },
            onSave = { name, type, opening, colorHex ->
                onAddAccount(name, type, opening, colorHex)
                showAddAccountDialog = false
            }
        )
    }

    editingAccount?.let { acc ->
        AddAccountDialog(
            initialAccount = acc,
            onDismiss = { editingAccount = null },
            onSave = { name, type, opening, colorHex ->
                onUpdateAccount(
                    acc.copy(
                        name = name,
                        type = type,
                        openingBalancePaisa = opening,
                        colorHex = colorHex
                    )
                )
                editingAccount = null
            }
        )
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            initialCategory = null,
            onDismiss = { showAddCategoryDialog = false },
            onSave = { name, type, iconKey, colorHex, limit ->
                onAddCategory(name, type, iconKey, colorHex, limit)
                showAddCategoryDialog = false
            }
        )
    }

    editingCategory?.let { cat ->
        AddCategoryDialog(
            initialCategory = cat,
            onDismiss = { editingCategory = null },
            onSave = { name, type, iconKey, colorHex, limit ->
                onUpdateCategory(
                    cat.copy(
                        name = name,
                        type = type,
                        iconKey = iconKey,
                        colorHex = colorHex,
                        monthlyLimitPaisa = limit
                    )
                )
                editingCategory = null
            }
        )
    }

    if (showReconciliationDialog) {
        ReconciliationDialog(
            accounts = accounts,
            onDismiss = { showReconciliationDialog = false }
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    actionTitle: String? = null,
    onAction: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 0.8.sp
                )

                if (actionTitle != null && onAction != null) {
                    Text(
                        text = actionTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onAction() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}
