package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.security.SecurityManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.AccountWithBalance
import com.example.data.model.CategoryEntity
import com.example.data.model.CategoryType
import com.example.data.model.RecurringFrequency
import com.example.data.model.RecurringRuleEntity
import com.example.data.model.TransactionType
import com.example.domain.Money
import com.example.ui.components.AddAccountDialog
import com.example.ui.components.AddCategoryDialog
import com.example.ui.components.AddRecurringRuleDialog
import com.example.ui.components.CategoryIconHelper
import com.example.ui.components.ReconciliationDialog
import com.example.ui.components.RestoreDataDialog
import com.example.ui.theme.ClayAccentLime
import com.example.ui.theme.ClayBorderHairline
import com.example.ui.theme.ClayInkDeep
import com.example.ui.theme.ClaySurface
import com.example.ui.theme.ClayTextPrimary
import com.example.ui.theme.ClayTextSecondary
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SettingsScreen(
    accounts: List<AccountWithBalance>,
    categories: List<CategoryEntity>,
    recurringRules: List<RecurringRuleEntity>,
    onAddAccount: (name: String, type: AccountType, openingPaisa: Long, colorHex: String) -> Unit,
    onUpdateAccount: (AccountEntity) -> Unit,
    onAddCategory: (name: String, type: CategoryType, iconKey: String, colorHex: String, limitPaisa: Long?) -> Unit,
    onUpdateCategory: (CategoryEntity) -> Unit,
    onAddRecurringRule: (
        title: String,
        type: TransactionType,
        amountPaisa: Long,
        accountId: Long,
        categoryId: Long,
        frequency: RecurringFrequency,
        startDateEpochMs: Long,
        note: String
    ) -> Unit,
    onDeleteRecurringRule: (RecurringRuleEntity) -> Unit,
    onExportJson: suspend () -> File,
    onRestoreJson: suspend (String) -> Result<String>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val securityManager = remember { SecurityManager.getInstance(context) }
    val isLockConfigured by securityManager.isLockConfigured.collectAsStateWithLifecycle()
    val isBiometricEnabled by securityManager.isBiometricEnabled.collectAsStateWithLifecycle()
    val isFlagSecureEnabled by securityManager.isFlagSecureEnabled.collectAsStateWithLifecycle()
    var showSetPinDialog by remember { mutableStateOf(false) }

    var showAddAccountDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<AccountEntity?>(null) }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }

    var showAddRecurringDialog by remember { mutableStateOf(false) }
    var showReconciliationDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    .testTag("settings_screen"),
                contentPadding = PaddingValues(horizontal = if (isWideScreen) 24.dp else 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
            // Screen Header
            item {
                Column {
                    Text(
                        text = "Settings & Ledger Management",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ClayTextPrimary
                    )
                    Text(
                        text = "Accounts, categories, recurring payments, and backup",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClayTextSecondary
                    )
                }
            }

            // Accounts Section
            item {
                ClaySectionCard(
                    title = "ACCOUNTS & WALLETS",
                    actionTitle = "+ Add Account",
                    onAction = { showAddAccountDialog = true }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        accounts.forEach { item ->
                            val color = runCatching {
                                Color(android.graphics.Color.parseColor(item.account.colorHex))
                            }.getOrDefault(ClayInkDeep)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ClaySurface)
                                    .border(1.dp, ClayBorderHairline, RoundedCornerShape(12.dp))
                                    .clickable { editingAccount = item.account }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
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
                                            color = ClayTextPrimary
                                        )
                                        val typeLabel = when (item.account.type) {
                                            AccountType.CASH_WALLET -> "Cash Wallet"
                                            AccountType.BANK -> "Bank Account"
                                            AccountType.SAVINGS -> "Savings"
                                            AccountType.OTHER -> "Other"
                                        }
                                        Text(
                                            text = typeLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ClayTextSecondary
                                        )
                                    }
                                }

                                Text(
                                    text = Money.formatPkr(item.balancePaisa),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ClayTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Categories Section
            item {
                ClaySectionCard(
                    title = "CATEGORIES & BUDGET LIMITS",
                    actionTitle = "+ Add Category",
                    onAction = { showAddCategoryDialog = true }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.forEach { cat ->
                            val color = runCatching {
                                Color(android.graphics.Color.parseColor(cat.colorHex))
                            }.getOrDefault(ClayInkDeep)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ClaySurface)
                                    .border(1.dp, ClayBorderHairline, RoundedCornerShape(12.dp))
                                    .clickable { editingCategory = cat }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ClayAccentLime.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = CategoryIconHelper.getIcon(cat.iconKey),
                                            contentDescription = null,
                                            tint = ClayInkDeep,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = cat.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = ClayTextPrimary
                                        )
                                        Text(
                                            text = cat.type.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ClayTextSecondary
                                        )
                                    }
                                }

                                cat.monthlyLimitPaisa?.let { limit ->
                                    Text(
                                        text = "Limit: ${Money.formatPkr(limit)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ClayTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recurring Rules Section
            item {
                ClaySectionCard(
                    title = "RECURRING RULES & BILLS",
                    actionTitle = "+ New Rule",
                    onAction = { showAddRecurringDialog = true }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (recurringRules.isEmpty()) {
                            Text(
                                text = "No recurring rules set. Add rent, salary, or utility schedules for automated due reminders.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ClayTextSecondary
                            )
                        } else {
                            recurringRules.forEach { rule ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ClaySurface)
                                        .border(1.dp, ClayBorderHairline, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Alarm,
                                            contentDescription = null,
                                            tint = ClayInkDeep,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = rule.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = ClayTextPrimary
                                            )
                                            Text(
                                                text = "${rule.frequency.name.lowercase().replaceFirstChar { it.uppercase() }} · ${Money.formatPkr(rule.amountPaisa)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ClayTextSecondary
                                            )
                                        }
                                    }

                                    IconButton(onClick = { onDeleteRecurringRule(rule) }) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete rule",
                                            tint = ClayTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Financial Integrity & Reconciliation
            item {
                ClaySectionCard(title = "FINANCIAL INTEGRITY TOOLS") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ClaySurface)
                                .border(1.dp, ClayBorderHairline, RoundedCornerShape(12.dp))
                                .clickable { showReconciliationDialog = true }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Balance,
                                contentDescription = "Reconciliation",
                                tint = ClayInkDeep,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Account Reconciliation Tool",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ClayTextPrimary
                                )
                                Text(
                                    text = "Compare physical wallet/statement with Hisaab balance without automatic distortion",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ClayTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Biometric & Financial Vault Security Section
            item {
                ClaySectionCard(title = "BIOMETRIC LOCK & FINANCIAL VAULT") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Master App Lock Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ClaySurface)
                                .border(1.dp, ClayBorderHairline, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = ClayInkDeep,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Require Lock to Open App",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = ClayTextPrimary
                                    )
                                    Text(
                                        text = "Locks app after 15s in background or on launch",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ClayTextSecondary
                                    )
                                }
                            }
                            Switch(
                                checked = isLockConfigured,
                                onCheckedChange = { enabled ->
                                    if (enabled && !securityManager.hasVaultPin()) {
                                        showSetPinDialog = true
                                    } else {
                                        securityManager.setLockConfigured(enabled)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ClayInkDeep,
                                    checkedTrackColor = ClayAccentLime,
                                    uncheckedThumbColor = ClayTextSecondary,
                                    uncheckedTrackColor = ClaySurface
                                ),
                                modifier = Modifier.testTag("app_lock_switch")
                            )
                        }

                        // Biometric (Fingerprint/Face) Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ClaySurface)
                                .border(1.dp, ClayBorderHairline, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = ClayInkDeep,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Enable Fingerprint Biometrics",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = ClayTextPrimary
                                    )
                                    Text(
                                        text = "Fast biometric unlock with device sensor",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ClayTextSecondary
                                    )
                                }
                            }
                            Switch(
                                checked = isBiometricEnabled,
                                onCheckedChange = { securityManager.setBiometricEnabled(it) },
                                enabled = isLockConfigured,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ClayInkDeep,
                                    checkedTrackColor = ClayAccentLime
                                ),
                                modifier = Modifier.testTag("biometric_switch")
                            )
                        }

                        // Change 4-digit Vault PIN Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ClaySurface)
                                .border(1.dp, ClayBorderHairline, RoundedCornerShape(12.dp))
                                .clickable { showSetPinDialog = true }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Password,
                                    contentDescription = null,
                                    tint = ClayInkDeep,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Change 4-Digit Vault PIN",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = ClayTextPrimary
                                    )
                                    Text(
                                        text = if (securityManager.hasVaultPin()) "Custom PIN configured" else "Default PIN: 1234",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ClayTextSecondary
                                    )
                                }
                            }
                            OutlinedButton(
                                onClick = { showSetPinDialog = true },
                                modifier = Modifier.heightIn(min = 40.dp).testTag("change_pin_button")
                            ) {
                                Text("Edit PIN", style = MaterialTheme.typography.labelSmall, color = ClayInkDeep)
                            }
                        }

                        // Window FLAG_SECURE Toggle (Privacy Shield in Recents)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ClaySurface)
                                .border(1.dp, ClayBorderHairline, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = ClayInkDeep,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Recent Apps Privacy Shield",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = ClayTextPrimary
                                    )
                                    Text(
                                        text = "Blocks system screenshots in app switcher",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ClayTextSecondary
                                    )
                                }
                            }
                            Switch(
                                checked = isFlagSecureEnabled,
                                onCheckedChange = { enabled ->
                                    val activity = context as? android.app.Activity
                                    securityManager.setFlagSecureEnabled(activity, enabled)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ClayInkDeep,
                                    checkedTrackColor = ClayAccentLime
                                ),
                                modifier = Modifier.testTag("flag_secure_switch")
                            )
                        }
                    }
                }
            }

            // Data Export & Restore Section
            item {
                ClaySectionCard(title = "DATA BACKUP & RESTORE") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Export your entire ledger (accounts, categories, transactions, needs, rules) as a validated JSON file, or restore from an existing backup.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ClayTextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
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
                                    containerColor = ClayInkDeep,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("export_json_button")
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isExporting) "Exporting..." else "Export JSON")
                            }

                            OutlinedButton(
                                onClick = { showRestoreDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("restore_json_button")
                            ) {
                                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restore JSON")
                            }
                        }
                    }
                }
            }

            // About Hisaab Do Bhai
            item {
                ClaySectionCard(title = "ABOUT HISAAB DO BHAI") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = ClayInkDeep,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "100% Offline & Local Privacy",
                                style = MaterialTheme.typography.titleMedium,
                                color = ClayTextPrimary
                            )
                        }

                        Text(
                            text = "Hisaab Do Bhai operates entirely on your device with no accounts, no telemetry, and no external AI dependencies. All monetary math uses exact integer paisa arithmetic, and account balances are derived deterministically from the ledger.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ClayTextSecondary
                        )

                        Text(
                            text = "Version 2.0 (Clay Edition) · Pakistani Rupee (PKR)",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClayTextSecondary.copy(alpha = 0.7f)
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

    if (showAddRecurringDialog) {
        AddRecurringRuleDialog(
            accounts = accounts.map { it.account },
            categories = categories,
            onDismiss = { showAddRecurringDialog = false },
            onSave = { title, type, amount, accId, catId, freq, start, note ->
                onAddRecurringRule(title, type, amount, accId, catId, freq, start, note)
                showAddRecurringDialog = false
            }
        )
    }

    if (showReconciliationDialog) {
        ReconciliationDialog(
            accounts = accounts,
            onDismiss = { showReconciliationDialog = false }
        )
    }

    if (showRestoreDialog) {
        RestoreDataDialog(
            onDismiss = { showRestoreDialog = false },
            onRestore = { json ->
                scope.launch {
                    val result = onRestoreJson(json)
                    showRestoreDialog = false
                    if (result.isSuccess) {
                        snackbarHostState.showSnackbar("Restore successful: ${result.getOrNull()}")
                    } else {
                        snackbarHostState.showSnackbar("Restore error: ${result.exceptionOrNull()?.message}")
                    }
                }
            }
        )
    }

    if (showSetPinDialog) {
        var newPin by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showSetPinDialog = false },
            modifier = Modifier.widthIn(max = 500.dp),
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Configure 4-Digit Vault PIN",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = ClayTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Enter a 4-digit numeric code to unlock your private ledger if fingerprint is unavailable.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClayTextSecondary
                    )
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = {
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                newPin = it
                                pinError = ""
                            }
                        },
                        label = { Text("4-Digit PIN") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("vault_pin_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClayInkDeep,
                            unfocusedBorderColor = ClayBorderHairline
                        )
                    )
                    if (pinError.isNotEmpty()) {
                        Text(
                            text = pinError,
                            style = MaterialTheme.typography.labelSmall,
                            color = com.example.ui.theme.SemanticExpense
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length == 4) {
                            securityManager.setVaultPin(newPin)
                            securityManager.setLockConfigured(true)
                            showSetPinDialog = false
                            Toast.makeText(context, "Vault PIN updated & lock enabled!", Toast.LENGTH_SHORT).show()
                        } else {
                            pinError = "PIN must be exactly 4 digits"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ClayInkDeep, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.heightIn(min = 44.dp).testTag("save_pin_button")
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSetPinDialog = false },
                    modifier = Modifier.heightIn(min = 44.dp)
                ) {
                    Text("Cancel", color = ClayTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun ClaySectionCard(
    title: String,
    actionTitle: String? = null,
    onAction: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, ClayBorderHairline, RoundedCornerShape(18.dp)),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = ClayTextSecondary,
                    letterSpacing = 0.8.sp
                )

                if (actionTitle != null && onAction != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(ClayAccentLime.copy(alpha = 0.25f))
                            .border(1.dp, ClayAccentLime.copy(alpha = 0.6f), RoundedCornerShape(9999.dp))
                            .clickable { onAction() }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = actionTitle,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = ClayInkDeep
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            content()
        }
    }
}
