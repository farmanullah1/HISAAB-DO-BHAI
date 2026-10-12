package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.AccountWithBalance
import com.example.domain.Money
import com.example.ui.theme.ExpenseTerracotta
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReconciliationDialog(
    accounts: List<AccountWithBalance>,
    onDismiss: () -> Unit,
    onRecordAdjustment: ((accountId: Long, diffPaisa: Long, note: String) -> Unit)? = null
) {
    var selectedAccountIndex by remember { mutableStateOf(0) }
    var expandedDropdown by remember { mutableStateOf(false) }
    var enteredActualText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val currentAccount = accounts.getOrNull(selectedAccountIndex)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Account Reconciliation",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Compare Hisaab's calculated balance with your actual cash count or bank statement without automated distortion.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )

                if (accounts.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown }
                    ) {
                        OutlinedTextField(
                            value = currentAccount?.account?.name ?: "Select Account",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Account") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            accounts.forEachIndexed { index, item ->
                                DropMenuItemAccount(
                                    name = item.account.name,
                                    balanceText = Money.formatPkr(item.balancePaisa),
                                    onClick = {
                                        selectedAccountIndex = index
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                currentAccount?.let { acc ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Hisaab Calculated Balance",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = Money.formatPkr(acc.balancePaisa),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    OutlinedTextField(
                        value = enteredActualText,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                                enteredActualText = input
                            }
                        },
                        label = { Text("Actual Statement / Cash (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reconciliation_actual_input")
                    )

                    val actualPaisa = Money.parsePaisa(enteredActualText)
                    if (actualPaisa != null) {
                        val difference = actualPaisa - acc.balancePaisa
                        val diffColor = when {
                            difference == 0L -> IncomeGreen
                            difference > 0L -> WarningAmber
                            else -> ExpenseTerracotta
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, diffColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .background(diffColor.copy(alpha = 0.1f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (difference == 0L) "Perfect Match" else "Discrepancy (Actual - Hisaab)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = diffColor
                                )
                                Text(
                                    text = (if (difference > 0) "+" else "") + Money.formatPkr(difference),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = diffColor
                                )
                                if (difference != 0L) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tip: Review your recent receipts or record a manual adjustment note.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Reconciliation Note (optional)") },
                        maxLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reconciliation_note_input")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Close", color = MaterialTheme.colorScheme.secondary)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun DropMenuItemAccount(
    name: String,
    balanceText: String,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = name, style = MaterialTheme.typography.bodyMedium)
                Text(text = balanceText, style = MaterialTheme.typography.labelMedium)
            }
        },
        onClick = onClick
    )
}
