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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
fun TransactionRow(
    item: TransactionItem,
    privacyMode: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToDebtDetail: ((Long) -> Unit)? = null
) {
    val tx = item.transaction
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }
    val formattedDate = dateFormatter.format(Date(tx.dateEpochMs))

    var showDetailDialog by remember { mutableStateOf(false) }
    var showReceiptDialog by remember { mutableStateOf(false) }

    val (tintBg, amountColor, title) = when (tx.type) {
        TransactionType.INCOME -> Triple(
            SemanticIncome.copy(alpha = 0.12f),
            SemanticIncome,
            item.categoryName ?: "Income"
        )
        TransactionType.EXPENSE -> Triple(
            SemanticExpense.copy(alpha = 0.12f),
            SemanticExpense,
            item.categoryName ?: "Expense"
        )
        TransactionType.TRANSFER -> Triple(
            ClayAccentLime.copy(alpha = 0.5f),
            ClayTextPrimary,
            "Transfer"
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
            "Karz Repaid to Me"
        )
        TransactionType.DEBT_REPAYMENT_OUT -> Triple(
            SemanticExpense.copy(alpha = 0.12f),
            SemanticExpense,
            "Karz Repaid by Me"
        )
    }

    val subtitle = when (tx.type) {
        TransactionType.EXPENSE,
        TransactionType.DEBT_LENT,
        TransactionType.DEBT_REPAYMENT_OUT -> "From ${item.fromAccountName ?: "Account"}"

        TransactionType.INCOME,
        TransactionType.DEBT_BORROWED,
        TransactionType.DEBT_REPAYMENT_IN -> "To ${item.toAccountName ?: "Account"}"

        TransactionType.TRANSFER -> "${item.fromAccountName ?: "Account"} → ${item.toAccountName ?: "Account"}"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, ClayBorderHairline, RoundedCornerShape(14.dp))
            .clickable { showDetailDialog = true }
            .testTag("transaction_row_${tx.id}"),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tintBg),
                contentAlignment = Alignment.Center
            ) {
                if (tx.type == TransactionType.TRANSFER) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Transfer",
                        tint = ClayInkDeep,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (tx.type == TransactionType.DEBT_LENT ||
                    tx.type == TransactionType.DEBT_BORROWED ||
                    tx.type == TransactionType.DEBT_REPAYMENT_IN ||
                    tx.type == TransactionType.DEBT_REPAYMENT_OUT
                ) {
                    Icon(
                        imageVector = Icons.Default.Handshake,
                        contentDescription = "Karz",
                        tint = amountColor,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = CategoryIconHelper.getIcon(item.categoryIconKey),
                        contentDescription = title,
                        tint = amountColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = ClayTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (tx.receiptPath != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ClaySurface)
                                .clickable { showReceiptDialog = true }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = "View receipt",
                                    tint = ClayTextSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Receipt",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ClayTextSecondary
                                )
                            }
                        }
                    }
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ClayTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (tx.note.isNotBlank()) {
                    Text(
                        text = tx.note,
                        style = MaterialTheme.typography.labelSmall,
                        color = ClayTextSecondary.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = ClayTextSecondary.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount and delete button
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (privacyMode) "••••••" else Money.formatSigned(tx.amountPaisa, tx.type),
                    style = MaterialTheme.typography.titleLarge,
                    color = amountColor
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_transaction_${tx.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete transaction",
                        tint = ClayTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (showReceiptDialog && tx.receiptPath != null) {
        ReceiptViewerDialog(
            filePath = tx.receiptPath,
            onDismiss = { showReceiptDialog = false }
        )
    }

    if (showDetailDialog) {
        TransactionDetailDialog(
            item = item,
            privacyMode = privacyMode,
            onDelete = onDelete,
            onNavigateToDebtDetail = onNavigateToDebtDetail,
            onDismiss = { showDetailDialog = false }
        )
    }
}
