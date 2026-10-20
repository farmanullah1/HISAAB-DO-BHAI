package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.money.PkrFormatter
import com.example.data.model.AccountEntity
import com.example.data.model.AccountWithBalance
import com.example.domain.Money
import com.example.ui.theme.ClayAccentLime
import com.example.ui.theme.ClayBorderHairline
import com.example.ui.theme.ClayInkDeep
import com.example.ui.theme.ClayPanelBlue
import com.example.ui.theme.ClaySurface
import com.example.ui.theme.ClayTextPrimary
import com.example.ui.theme.ClayTextSecondary

@Composable
fun BalanceCard(
    totalBalancePaisa: Long,
    remainingBalancePaisa: Long,
    reservedPaisa: Long,
    accounts: List<AccountWithBalance>,
    selectedAccountId: Long?,
    onAccountSelected: (Long?) -> Unit,
    onEditAccount: (AccountEntity) -> Unit,
    onManageReserves: () -> Unit,
    privacyMode: Boolean,
    onTogglePrivacy: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, ClayBorderHairline, RoundedCornerShape(18.dp)),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Label, Info, Privacy Button & Expand toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TOTAL BALANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClayTextSecondary,
                        letterSpacing = 0.8.sp
                    )
                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Balance explanation",
                            tint = ClayTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onTogglePrivacy,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(36.dp)
                            .testTag("toggle_privacy_button")
                    ) {
                        Icon(
                            imageVector = if (privacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (privacyMode) "Reveal balances" else "Hide balances",
                            tint = ClayTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(ClaySurface)
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${accounts.size} Accounts",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClayTextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle accounts list",
                        modifier = Modifier.size(16.dp),
                        tint = ClayTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Display Total Balance
            val totalDisplay = if (privacyMode) "₨ ••••••" else PkrFormatter.format(totalBalancePaisa)
            Text(
                text = totalDisplay,
                style = MaterialTheme.typography.displayLarge,
                color = ClayTextPrimary,
                maxLines = 1,
                modifier = Modifier.testTag("total_balance_text")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Remaining Balance Banner (Total - Reserved)
            val remainingDisplay = if (privacyMode) "₨ ••••••" else PkrFormatter.format(remainingBalancePaisa)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ClayPanelBlue)
                    .border(1.dp, ClayBorderHairline, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "REMAINING BALANCE (AVAILABLE)",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClayTextSecondary,
                            letterSpacing = 0.6.sp
                        )
                        Text(
                            text = remainingDisplay,
                            style = MaterialTheme.typography.titleLarge,
                            color = ClayTextPrimary
                        )
                    }

                    if (reservedPaisa > 0L) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(ClayAccentLime)
                                .clickable { onManageReserves() }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (privacyMode) "Reserved" else "${PkrFormatter.format(reservedPaisa)} Reserved",
                                style = MaterialTheme.typography.labelSmall,
                                color = ClayInkDeep
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(ClaySurface)
                                .clickable { onManageReserves() }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "+ Reserve Funds",
                                style = MaterialTheme.typography.labelSmall,
                                color = ClayTextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Chips (Pill shaped: All, Cash Wallet, and other user accounts)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isAllSelected = selectedAccountId == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (isAllSelected) ClayAccentLime else ClaySurface)
                        .border(
                            width = 1.dp,
                            color = if (isAllSelected) ClayInkDeep.copy(alpha = 0.3f) else Color.Transparent,
                            shape = RoundedCornerShape(9999.dp)
                        )
                        .clickable { onAccountSelected(null) }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("filter_all_accounts")
                ) {
                    Text(
                        text = "All",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                        color = ClayInkDeep
                    )
                }

                accounts.forEach { accWithBal ->
                    val isSelected = selectedAccountId == accWithBal.account.id
                    val accountColor = runCatching {
                        Color(android.graphics.Color.parseColor(accWithBal.account.colorHex))
                    }.getOrDefault(ClayInkDeep)

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(if (isSelected) ClayAccentLime else ClaySurface)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) ClayInkDeep.copy(alpha = 0.3f) else Color.Transparent,
                                shape = RoundedCornerShape(9999.dp)
                            )
                            .clickable { onAccountSelected(if (isSelected) null else accWithBal.account.id) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("filter_account_${accWithBal.account.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(accountColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (privacyMode) accWithBal.account.name else "${accWithBal.account.name} · ${PkrFormatter.format(accWithBal.currentBalancePaisa)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = ClayInkDeep
                        )
                    }
                }
            }

            // Expandable breakdown with tap-to-edit opening balance
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    accounts.forEach { item ->
                        val color = runCatching {
                            Color(android.graphics.Color.parseColor(item.account.colorHex))
                        }.getOrDefault(ClayInkDeep)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onEditAccount(item.account) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
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
                                    Text(
                                        text = "${item.account.type.name} · Tap to edit",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ClayTextSecondary
                                    )
                                }
                            }

                            Text(
                                text = if (privacyMode) "••••••" else Money.formatPkr(item.balancePaisa),
                                style = MaterialTheme.typography.titleMedium,
                                color = ClayTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(text = "Balance Information", style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "• Total Balance is the sum of all current money in your active accounts derived directly from your transactions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClayTextPrimary
                    )
                    Text(
                        text = "• Remaining Balance is your Total Balance minus funds you have explicitly reserved for upcoming obligations.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClayTextPrimary
                    )
                    Text(
                        text = "• Cash Wallet represents all physical cash and mobile wallet money as a single unified balance.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClayTextPrimary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ClayInkDeep),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Understood")
                }
            },
            shape = RoundedCornerShape(18.dp)
        )
    }
}
