package com.example.ui.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KarzSummary
import com.example.data.model.MonthlySummary
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AnalyticsWindow(val label: String) {
    THIS_MONTH("This Month"),
    LAST_30_DAYS("Last 30 Days"),
    ALL_TIME("All Time")
}

@Composable
fun ClayAnalyticsSection(
    transactions: List<TransactionItem>,
    monthlySummary: MonthlySummary,
    karzSummary: KarzSummary?,
    modifier: Modifier = Modifier
) {
    var selectedWindow by remember { mutableStateOf(AnalyticsWindow.THIS_MONTH) }

    val filteredTransactions = remember(transactions, selectedWindow) {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        when (selectedWindow) {
            AnalyticsWindow.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val startOfMonth = cal.timeInMillis
                transactions.filter { it.transaction.dateEpochMs >= startOfMonth }
            }
            AnalyticsWindow.LAST_30_DAYS -> {
                val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000L)
                transactions.filter { it.transaction.dateEpochMs >= thirtyDaysAgo }
            }
            AnalyticsWindow.ALL_TIME -> transactions
        }
    }

    // Category Spending Slices for Donut Chart
    val categorySlices = remember(filteredTransactions) {
        val expenseTx = filteredTransactions.filter { it.transaction.type == TransactionType.EXPENSE }
        val grouped = expenseTx.groupBy { it.categoryName ?: "Other" }

        val palette = listOf(
            Color(0xFFE07A5F), Color(0xFF81B29A), Color(0xFFF2CC8F),
            Color(0xFF3D5A80), Color(0xFF9B5DE5), Color(0xFFE76F51),
            Color(0xFF2A9D8F), Color(0xFFF4A261), Color(0xFF6BA88A)
        )

        grouped.entries.mapIndexed { index, entry ->
            val total = entry.value.sumOf { it.transaction.amountPaisa }
            val color = try {
                val hex = entry.value.firstOrNull()?.categoryColorHex
                if (hex != null && hex.startsWith("#")) Color(android.graphics.Color.parseColor(hex))
                else palette[index % palette.size]
            } catch (_: Exception) {
                palette[index % palette.size]
            }
            ChartSlice(
                label = entry.key,
                valuePaisa = total,
                color = color
            )
        }.sortedByDescending { it.valuePaisa }
    }

    val totalWindowExpense = remember(categorySlices) {
        categorySlices.sumOf { it.valuePaisa }
    }

    // Bar Groups (Weekly or Chronological breakdown)
    val barGroups = remember(filteredTransactions) {
        // Group into 4 periods/weeks
        val cal = Calendar.getInstance()
        val groups = mutableListOf<BarGroup>()
        val dateFormatter = SimpleDateFormat("dd MMM", Locale.US)

        for (i in 3 downTo 0) {
            val endDaysAgo = i * 7
            val startDaysAgo = (i + 1) * 7
            val nowMs = System.currentTimeMillis()
            val startMs = nowMs - (startDaysAgo * 24L * 60 * 60 * 1000L)
            val endMs = nowMs - (endDaysAgo * 24L * 60 * 60 * 1000L)

            val periodTxs = filteredTransactions.filter { it.transaction.dateEpochMs in startMs..endMs }
            val inc = periodTxs.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amountPaisa }
            val exp = periodTxs.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amountPaisa }

            val label = "W${4 - i} (${dateFormatter.format(Date(startMs))})"
            groups.add(BarGroup(label = label, incomePaisa = inc, expensePaisa = exp))
        }
        groups
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("clay_analytics_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Time Window Segmented Control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(ClaySurface)
                .border(1.dp, ClayBorderHairline, RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AnalyticsWindow.entries.forEach { window ->
                val isSelected = selectedWindow == window
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) ClayInkDeep else Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(),
                            onClick = { selectedWindow = window }
                        )
                        .padding(vertical = 8.dp)
                        .testTag("window_${window.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = window.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) ClayAccentLime else ClayTextSecondary
                    )
                }
            }
        }

        // 1. Spending Breakdown Donut Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, ClayBorderHairline, RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "CATEGORY EXPENSE DISTRIBUTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = ClayTextSecondary,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "اخراجات کی تفصیل",
                    style = MaterialTheme.typography.labelSmall,
                    color = ClayTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (categorySlices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No expenses recorded in this period",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ClayTextSecondary
                        )
                    }
                } else {
                    ClayDonutChart(
                        slices = categorySlices,
                        totalExpensePaisa = totalWindowExpense
                    )
                }
            }
        }

        // 2. Cash Flow Trends Bar Chart Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, ClayBorderHairline, RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "CASH FLOW TRENDS (INCOME VS EXPENSE)",
                    style = MaterialTheme.typography.labelSmall,
                    color = ClayTextSecondary,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "آمدن اور اخراجات کا رجحان",
                    style = MaterialTheme.typography.labelSmall,
                    color = ClayTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                ClayBarChart(barGroups = barGroups)
            }
        }

        // 3. Karz Liability Gauge Card
        KarzLiabilityGauge(karzSummary = karzSummary)
    }
}
