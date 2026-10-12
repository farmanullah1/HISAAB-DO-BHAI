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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PendingRecurringItem
import com.example.data.model.TransactionType
import com.example.domain.Money
import com.example.ui.theme.ClayAccentLime
import com.example.ui.theme.ClayBorderHairline
import com.example.ui.theme.ClayInkDeep
import com.example.ui.theme.ClayPanelBlue
import com.example.ui.theme.ClayTextPrimary
import com.example.ui.theme.ClayTextSecondary

@Composable
fun PendingRecurringCard(
    item: PendingRecurringItem,
    onConfirm: (Long) -> Unit,
    onSkip: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val occ = item.occurrence
    val rule = item.rule

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ClayPanelBlue)
            .border(1.dp, ClayBorderHairline, RoundedCornerShape(18.dp))
            .padding(18.dp)
            .testTag("pending_recurring_${occ.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ClayAccentLime),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = ClayInkDeep,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "PAYMENT DUE · CONFIRMATION REQUIRED",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClayTextSecondary,
                            letterSpacing = 0.6.sp
                        )
                        Text(
                            text = rule.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = ClayTextPrimary
                        )
                    }
                }

                Text(
                    text = Money.formatSigned(rule.amountPaisa, rule.type),
                    style = MaterialTheme.typography.titleLarge,
                    color = ClayTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val contextText = buildString {
                append(if (rule.type == TransactionType.EXPENSE) "From " else "To ")
                append(item.accountName ?: "Account")
                if (!item.categoryName.isNullOrBlank()) {
                    append(" · Category: ${item.categoryName}")
                }
                append(" (${rule.frequency.name.lowercase().replaceFirstChar { it.uppercase() }})")
            }

            Text(
                text = contextText,
                style = MaterialTheme.typography.bodySmall,
                color = ClayTextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Confirm or Skip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onConfirm(occ.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ClayAccentLime,
                        contentColor = ClayInkDeep
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("confirm_recurring_${occ.id}")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Confirm & Record", style = MaterialTheme.typography.labelLarge)
                }

                OutlinedButton(
                    onClick = { onSkip(occ.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ClayTextSecondary
                    ),
                    modifier = Modifier.testTag("skip_recurring_${occ.id}")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Skip", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
