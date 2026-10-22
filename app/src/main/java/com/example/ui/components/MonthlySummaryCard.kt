package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.money.PkrFormatter
import com.example.data.model.MonthlySummary
import com.example.domain.Money
import com.example.ui.theme.ClayAccentLime
import com.example.ui.theme.ClayBorderHairline
import com.example.ui.theme.ClayInkDeep
import com.example.ui.theme.ClayPanelBlue
import com.example.ui.theme.ClayPanelCoral
import com.example.ui.theme.ClaySurface
import com.example.ui.theme.ClayTextPrimary
import com.example.ui.theme.ClayTextSecondary
import com.example.ui.theme.SemanticExpense
import com.example.ui.theme.SemanticIncome

@Composable
fun MonthlySummaryCard(
    summary: MonthlySummary,
    privacyMode: Boolean,
    modifier: Modifier = Modifier
) {
    val netCashflowPaisa = summary.incomePaisa - summary.expensePaisa
    val isSurplus = netCashflowPaisa >= 0
    val totalVolume = (summary.incomePaisa + summary.expensePaisa).coerceAtLeast(1L)
    val incomeRatio = (summary.incomePaisa.toFloat() / totalVolume).coerceIn(0f, 1f)

    val animatedIncomeRatio by animateFloatAsState(
        targetValue = incomeRatio,
        animationSpec = tween(durationMillis = 600),
        label = "income_ratio"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, ClayBorderHairline, RoundedCornerShape(20.dp))
            .testTag("monthly_summary_card"),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Title & Net Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ClayAccentLime)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LAST 30 DAYS CASHFLOW",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClayTextSecondary,
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Net cashflow badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (isSurplus) ClayPanelBlue else ClayPanelCoral)
                        .border(
                            1.dp,
                            if (isSurplus) SemanticIncome.copy(alpha = 0.3f) else SemanticExpense.copy(alpha = 0.3f),
                            RoundedCornerShape(9999.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isSurplus) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isSurplus) SemanticIncome else SemanticExpense,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (privacyMode) "Net ••••" else if (isSurplus) "+${PkrFormatter.format(netCashflowPaisa)}" else "-${PkrFormatter.format(-netCashflowPaisa)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSurplus) SemanticIncome else SemanticExpense
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Income and Expense Stat Blocks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Income column
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ClaySurface)
                        .border(1.dp, ClayBorderHairline, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SemanticIncome.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Income indicator",
                            tint = SemanticIncome,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Income",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClayTextSecondary
                        )
                        Text(
                            text = if (privacyMode) "₨ •••••" else Money.formatPkr(summary.incomePaisa),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SemanticIncome,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Expense column
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ClaySurface)
                        .border(1.dp, ClayBorderHairline, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SemanticExpense.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Expense indicator",
                            tint = SemanticExpense,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Expenses",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClayTextSecondary
                        )
                        Text(
                            text = if (privacyMode) "₨ •••••" else Money.formatPkr(summary.expensePaisa),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SemanticExpense,
                            maxLines = 1
                        )
                    }
                }
            }

            // Ratio progress bar
            if (summary.incomePaisa > 0 || summary.expensePaisa > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(ClaySurface)
                ) {
                    if (animatedIncomeRatio > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(animatedIncomeRatio.coerceAtLeast(0.02f))
                                .height(8.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                                .background(SemanticIncome)
                        )
                    }
                    if (1f - animatedIncomeRatio > 0f) {
                        Box(
                            modifier = Modifier
                                .weight((1f - animatedIncomeRatio).coerceAtLeast(0.02f))
                                .height(8.dp)
                                .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                                .background(SemanticExpense)
                        )
                    }
                }
            }
        }
    }
}
