package com.example.ui.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.Money
import com.example.ui.theme.ClayBorderHairline
import com.example.ui.theme.ClayInkDeep
import com.example.ui.theme.ClaySurface
import com.example.ui.theme.ClayTextPrimary
import com.example.ui.theme.ClayTextSecondary
import com.example.ui.theme.SemanticExpense
import com.example.ui.theme.SemanticIncome

data class BarGroup(
    val label: String,
    val incomePaisa: Long,
    val expensePaisa: Long
)

@Composable
fun ClayBarChart(
    barGroups: List<BarGroup>,
    modifier: Modifier = Modifier
) {
    var selectedGroup by remember { mutableStateOf<BarGroup?>(null) }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(barGroups) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, tween(800))
    }

    val maxVal = remember(barGroups) {
        barGroups.maxOfOrNull { maxOf(it.incomePaisa, it.expensePaisa) }?.coerceAtLeast(10_000L) ?: 10_000L
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("clay_bar_chart")
    ) {
        // Legend & Tooltip Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(SemanticIncome))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Income", style = MaterialTheme.typography.labelSmall, color = ClayTextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(SemanticExpense))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Expense", style = MaterialTheme.typography.labelSmall, color = ClayTextSecondary)
                }
            }

            if (selectedGroup != null) {
                Text(
                    text = "${selectedGroup?.label}: In: ${Money.formatPkr(selectedGroup!!.incomePaisa)} | Ex: ${Money.formatPkr(selectedGroup!!.expensePaisa)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ClayInkDeep
                )
            } else {
                Text(
                    text = "Tap bar to inspect",
                    style = MaterialTheme.typography.labelSmall,
                    color = ClayTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bar Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .pointerInput(barGroups, maxVal) {
                    detectTapGestures { tapOffset ->
                        val groupWidth = size.width / barGroups.size.coerceAtLeast(1)
                        val groupIndex = (tapOffset.x / groupWidth).toInt()
                        if (groupIndex in barGroups.indices) {
                            selectedGroup = if (selectedGroup == barGroups[groupIndex]) null else barGroups[groupIndex]
                        }
                    }
                }
        ) {
            val chartHeight = size.height - 30.dp.toPx()
            val numGroups = barGroups.size.coerceAtLeast(1)
            val groupWidth = size.width / numGroups
            val barWidth = (groupWidth * 0.28f).coerceIn(12f, 32.dp.toPx())
            val cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)

            // Horizontal Grid Lines
            val gridPaintColor = Color(0xFFECEAE4)
            for (i in 1..3) {
                val y = chartHeight * (i / 4f)
                drawLine(
                    color = gridPaintColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }

            // Baseline
            drawLine(
                color = Color(0xFFD4D0C7),
                start = Offset(0f, chartHeight),
                end = Offset(size.width, chartHeight),
                strokeWidth = 2f
            )

            barGroups.forEachIndexed { index, group ->
                val groupCenterX = (index * groupWidth) + (groupWidth / 2f)

                val incomeHeight = (group.incomePaisa.toFloat() / maxVal.toFloat()) * chartHeight * animationProgress.value
                val expenseHeight = (group.expensePaisa.toFloat() / maxVal.toFloat()) * chartHeight * animationProgress.value

                val incomeLeft = groupCenterX - barWidth - 4.dp.toPx()
                val expenseLeft = groupCenterX + 4.dp.toPx()

                // Income Bar
                if (incomeHeight > 0) {
                    drawRoundRect(
                        color = SemanticIncome,
                        topLeft = Offset(incomeLeft, chartHeight - incomeHeight),
                        size = Size(barWidth, incomeHeight),
                        cornerRadius = cornerRadius
                    )
                }

                // Expense Bar
                if (expenseHeight > 0) {
                    drawRoundRect(
                        color = SemanticExpense,
                        topLeft = Offset(expenseLeft, chartHeight - expenseHeight),
                        size = Size(barWidth, expenseHeight),
                        cornerRadius = cornerRadius
                    )
                }
            }
        }

        // Labels Row underneath
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            barGroups.forEach { group ->
                val isSelected = selectedGroup == group
                Text(
                    text = group.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) ClayInkDeep else ClayTextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.sp
                )
            }
        }
    }
}
