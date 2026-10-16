package com.example.ui.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KarzSummary
import com.example.domain.Money
import com.example.ui.theme.ClayAccentLime
import com.example.ui.theme.ClayBorderHairline
import com.example.ui.theme.ClayInkDeep
import com.example.ui.theme.ClaySurface
import com.example.ui.theme.ClayTextPrimary
import com.example.ui.theme.ClayTextSecondary
import com.example.ui.theme.SemanticExpense
import com.example.ui.theme.SemanticIncome

@Composable
fun KarzLiabilityGauge(
    karzSummary: KarzSummary?,
    modifier: Modifier = Modifier
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(karzSummary) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, tween(800))
    }

    val receivables = karzSummary?.totalGavePaisa ?: 0L
    val payables = karzSummary?.totalTookPaisa ?: 0L
    val netBalance = karzSummary?.netKarzPaisa ?: 0L
    val totalVolume = (receivables + payables).coerceAtLeast(1L)

    val receivableRatio = (receivables.toFloat() / totalVolume.toFloat()).coerceIn(0f, 1f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, ClayBorderHairline, RoundedCornerShape(20.dp))
            .testTag("karz_liability_gauge"),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "KARZ RATIO & LIQUIDITY GAUGE",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClayTextSecondary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "قرض تناسب و پوزیشن",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClayTextSecondary
                    )
                }

                // Health Badge
                val (badgeText, badgeBg, badgeColor) = when {
                    receivables > payables * 1.5 -> Triple("Strong Asset", SemanticIncome.copy(alpha = 0.15f), SemanticIncome)
                    receivables >= payables -> Triple("Balanced Position", ClayAccentLime, ClayInkDeep)
                    else -> Triple("Heavy Payable", SemanticExpense.copy(alpha = 0.15f), SemanticExpense)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Semicircular Dual-Arc Gauge
            Box(
                modifier = Modifier
                    .size(width = 220.dp, height = 120.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Canvas(
                    modifier = Modifier
                        .size(width = 220.dp, height = 120.dp)
                ) {
                    val strokeWidth = 24.dp.toPx()
                    val arcSize = Size(size.width - strokeWidth, (size.width - strokeWidth))
                    val topLeft = Offset(strokeWidth / 2f, 0f)

                    // Background track (180 degree semi-circle)
                    drawArc(
                        color = Color(0xFFEBE8E1),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Receivables Arc (Starts at 180 deg, sweeps clockwise)
                    val recSweep = 180f * receivableRatio * animationProgress.value
                    if (recSweep > 0) {
                        drawArc(
                            color = SemanticIncome,
                            startAngle = 180f,
                            sweepAngle = recSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Payables Arc (Starts at 0 deg, sweeps counter-clockwise)
                    val paySweep = 180f * (1f - receivableRatio) * animationProgress.value
                    if (paySweep > 0) {
                        drawArc(
                            color = SemanticExpense,
                            startAngle = 0f,
                            sweepAngle = -paySweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Center Indicator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = if (netBalance >= 0) "Net Receivable" else "Net Payable",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClayTextSecondary
                    )
                    Text(
                        text = Money.formatPkr(kotlin.math.abs(netBalance)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (netBalance >= 0) SemanticIncome else SemanticExpense
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ClaySurface)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.CallReceived,
                        contentDescription = null,
                        tint = SemanticIncome,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("I Gave (Receivable)", style = MaterialTheme.typography.labelSmall, color = ClayTextSecondary)
                        Text(Money.formatPkr(receivables), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ClayInkDeep)
                    }
                }

                Box(modifier = Modifier.width(1.dp).height(24.dp).background(ClayBorderHairline))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.CallMade,
                        contentDescription = null,
                        tint = SemanticExpense,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("I Took (Payable)", style = MaterialTheme.typography.labelSmall, color = ClayTextSecondary)
                        Text(Money.formatPkr(payables), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ClayInkDeep)
                    }
                }
            }
        }
    }
}
