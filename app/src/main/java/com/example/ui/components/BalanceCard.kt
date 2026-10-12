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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountWithBalance
import com.example.domain.Money
import com.example.ui.theme.ClayAccentLime
import com.example.ui.theme.ClayBorderHairline
import com.example.ui.theme.ClayInkDeep
import com.example.ui.theme.ClaySurface
import com.example.ui.theme.ClayTextPrimary
import com.example.ui.theme.ClayTextSecondary

@Composable
fun BalanceCard(
    totalBalancePaisa: Long,
    accounts: List<AccountWithBalance>,
    selectedAccountId: Long?,
    onAccountSelected: (Long?) -> Unit,
    privacyMode: Boolean,
    onTogglePrivacy: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

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
            // Header Row: Label & Privacy Button & Expand toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TOTAL AVAILABLE BALANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClayTextSecondary,
                        letterSpacing = 0.8.sp
                    )
                    IconButton(
                        onClick = onTogglePrivacy,
                        modifier = Modifier.size(28.dp).testTag("toggle_privacy_button")
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

            Spacer(modifier = Modifier.height(10.dp))

            // Display Amount
            val balanceDisplay = if (privacyMode) "Rs. ••••••" else Money.formatPkr(totalBalancePaisa)
            Text(
                text = balanceDisplay,
                style = MaterialTheme.typography.displayLarge,
                color = ClayTextPrimary,
                maxLines = 1,
                modifier = Modifier.testTag("total_balance_text")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Filter Chips (Pill shaped)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("filter_all_accounts")
                ) {
                    Text(
                        text = "All",
                        style = MaterialTheme.typography.labelMedium,
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
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("filter_account_${accWithBal.account.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(accountColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = accWithBal.account.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = ClayInkDeep
                        )
                    }
                }
            }

            // Expandable breakdown
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
                                .padding(vertical = 6.dp),
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
                                        text = item.account.type.name,
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
}
