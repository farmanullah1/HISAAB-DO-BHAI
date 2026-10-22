package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import com.example.domain.Money
import com.example.ui.theme.ClayAccentLime
import com.example.ui.theme.ClayBorderHairline
import com.example.ui.theme.ClayInkDeep
import com.example.ui.theme.ClaySurface
import com.example.ui.theme.ClayTextPrimary
import com.example.ui.theme.ClayTextSecondary
import com.example.ui.theme.SemanticExpense
import com.example.ui.theme.SemanticIncome
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionDetailDialog(
    item: TransactionItem,
    privacyMode: Boolean,
    onDelete: () -> Unit,
    onNavigateToDebtDetail: ((Long) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val tx = item.transaction
    val fullDateFormatter = remember { SimpleDateFormat("EEEE, MMM dd, yyyy · hh:mm a", Locale.US) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showReceiptViewer by remember { mutableStateOf(false) }

    val (badgeBg, badgeTextColor, typeTitle) = when (tx.type) {
        TransactionType.INCOME -> Triple(
            SemanticIncome.copy(alpha = 0.12f),
            SemanticIncome,
            "Income (آمدنی)"
        )
        TransactionType.EXPENSE -> Triple(
            SemanticExpense.copy(alpha = 0.12f),
            SemanticExpense,
            "Expense (خرچہ)"
        )
        TransactionType.TRANSFER -> Triple(
            ClayAccentLime.copy(alpha = 0.4f),
            ClayInkDeep,
            "Account Transfer (منتقلی)"
        )
        TransactionType.DEBT_LENT -> Triple(
            SemanticExpense.copy(alpha = 0.12f),
            SemanticExpense,
            "Karz Lent (قرض دیا)"
        )
        TransactionType.DEBT_BORROWED -> Triple(
            SemanticIncome.copy(alpha = 0.12f),
            SemanticIncome,
            "Karz Borrowed (قرض لیا)"
        )
        TransactionType.DEBT_REPAYMENT_IN -> Triple(
            SemanticIncome.copy(alpha = 0.12f),
            SemanticIncome,
            "Karz Repayment In (وصولی)"
        )
        TransactionType.DEBT_REPAYMENT_OUT -> Triple(
            SemanticExpense.copy(alpha = 0.12f),
            SemanticExpense,
            "Karz Repaid by Me (ادائیگی)"
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 520.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, ClayBorderHairline, RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeBg)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = typeTitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = badgeTextColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("close_transaction_detail_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ClayTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Big Amount Display
                Text(
                    text = if (privacyMode) "••••••" else Money.formatSigned(tx.amountPaisa, tx.type),
                    style = MaterialTheme.typography.headlineLarge,
                    color = badgeTextColor,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Details Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, ClayBorderHairline, RoundedCornerShape(16.dp)),
                    color = ClaySurface,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Date & Time
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = ClayTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Date & Time",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ClayTextSecondary
                                )
                                Text(
                                    text = fullDateFormatter.format(Date(tx.dateEpochMs)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ClayTextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Account
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Wallet,
                                contentDescription = null,
                                tint = ClayTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (tx.type == TransactionType.TRANSFER) "Accounts Involved" else "Account",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ClayTextSecondary
                                )
                                Text(
                                    text = when (tx.type) {
                                        TransactionType.TRANSFER ->
                                            "${item.fromAccountName ?: "Account"} → ${item.toAccountName ?: "Account"}"
                                        TransactionType.EXPENSE,
                                        TransactionType.DEBT_LENT,
                                        TransactionType.DEBT_REPAYMENT_OUT ->
                                            item.fromAccountName ?: "Cash Wallet"
                                        TransactionType.INCOME,
                                        TransactionType.DEBT_BORROWED,
                                        TransactionType.DEBT_REPAYMENT_IN ->
                                            item.toAccountName ?: "Cash Wallet"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ClayTextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Category (if not transfer)
                        if (tx.type != TransactionType.TRANSFER && item.categoryName != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = CategoryIconHelper.getIcon(item.categoryIconKey),
                                    contentDescription = null,
                                    tint = ClayTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Category",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ClayTextSecondary
                                    )
                                    Text(
                                        text = item.categoryName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ClayTextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Notes / Description
                        if (tx.note.isNotBlank()) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = ClayTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Note / Description",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ClayTextSecondary
                                    )
                                    Text(
                                        text = tx.note,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ClayTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Receipt Attachment Section
                if (tx.receiptPath != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, ClayBorderHairline, RoundedCornerShape(14.dp))
                            .clickable { showReceiptViewer = true },
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ClayAccentLime.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = "Receipt",
                                    tint = ClayInkDeep,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Receipt Photo Attached",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ClayTextPrimary
                                )
                                Text(
                                    text = "Tap to view full receipt",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ClayTextSecondary
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = ClayTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Linked Karz Record Card
                if (tx.debtId != null && onNavigateToDebtDetail != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, ClayInkDeep, RoundedCornerShape(14.dp))
                            .clickable {
                                onDismiss()
                                onNavigateToDebtDetail(tx.debtId)
                            },
                        color = ClayInkDeep,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Handshake,
                                contentDescription = null,
                                tint = ClayAccentLime,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Linked to Karz Ledger",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White
                                )
                                Text(
                                    text = "Tap to view person's full debt file",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ClayAccentLime
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("dialog_delete_transaction_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SemanticExpense
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Entry")
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("dialog_dismiss_transaction_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ClayInkDeep,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Transaction?") },
            text = {
                Text(
                    "Are you sure you want to delete this ${typeTitle.lowercase()} entry of ${Money.formatPkr(tx.amountPaisa)}? Account balances will be adjusted automatically."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDismiss()
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SemanticExpense,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showReceiptViewer && tx.receiptPath != null) {
        ReceiptViewerDialog(
            filePath = tx.receiptPath,
            onDismiss = { showReceiptViewer = false }
        )
    }
}
