package com.example.ui.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class ChartSlice(
    val label: String,
    val valuePaisa: Long,
    val color: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClayDonutChart(
    slices: List<ChartSlice>,
    totalExpensePaisa: Long,
    modifier: Modifier = Modifier
) {
    var selectedSliceIndex by remember { mutableIntStateOf(-1) }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(slices) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    val totalValue = remember(slices) {
        slices.sumOf { it.valuePaisa }.coerceAtLeast(1L)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("clay_donut_chart"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(230.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .pointerInput(slices, totalValue) {
                        detectTapGestures { tapOffset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = tapOffset.x - center.x
                            val dy = tapOffset.y - center.y
                            val dist = sqrt((dx * dx + dy * dy).toDouble())
                            val outerRadius = size.width / 2f
                            val innerRadius = outerRadius - 36.dp.toPx()

                            if (dist in innerRadius..outerRadius) {
                                var angle = (atan2(dy.toDouble(), dx.toDouble()) * (180.0 / PI)).toFloat()
                                if (angle < 0f) angle += 360f

                                var currentAngle = 0f
                                slices.forEachIndexed { index, slice ->
                                    val sweep = (slice.valuePaisa.toFloat() / totalValue.toFloat()) * 360f
                                    if (angle >= currentAngle && angle <= currentAngle + sweep) {
                                        selectedSliceIndex = if (selectedSliceIndex == index) -1 else index
                                        return@detectTapGestures
                                    }
                                    currentAngle += sweep
                                }
                            } else {
                                selectedSliceIndex = -1
                            }
                        }
                    }
            ) {
                val strokeWidth = 32.dp.toPx()
                val radius = (size.width - strokeWidth) / 2f
                val topLeft = Offset((size.width - radius * 2f) / 2f, (size.height - radius * 2f) / 2f)
                val arcSize = Size(radius * 2f, radius * 2f)

                // Background track
                drawArc(
                    color = Color(0xFFEBE8E1),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )

                var startAngle = -90f
                slices.forEachIndexed { index, slice ->
                    val sweepAngle = (slice.valuePaisa.toFloat() / totalValue.toFloat()) * 360f * animationProgress.value
                    val isSelected = selectedSliceIndex == index
                    val currentStrokeWidth = if (isSelected) strokeWidth + 8.dp.toPx() else strokeWidth

                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle.coerceAtLeast(0.5f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = currentStrokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += sweepAngle
                }
            }

            // Center Callout Display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                if (selectedSliceIndex in slices.indices) {
                    val sel = slices[selectedSliceIndex]
                    val pct = ((sel.valuePaisa.toFloat() / totalValue.toFloat()) * 100).toInt()
                    Text(
                        text = sel.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = ClayTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = Money.formatPkr(sel.valuePaisa),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ClayInkDeep
                    )
                    Text(
                        text = "$pct% of expenses",
                        style = MaterialTheme.typography.labelSmall,
                        color = sel.color,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Total Spending",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClayTextSecondary
                    )
                    Text(
                        text = Money.formatPkr(totalExpensePaisa),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ClayInkDeep
                    )
                    Text(
                        text = "${slices.size} Categories",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClayTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Legend Chips
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            slices.forEachIndexed { index, slice ->
                val isSelected = selectedSliceIndex == index
                val pct = ((slice.valuePaisa.toFloat() / totalValue.toFloat()) * 100).toInt()

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) ClayInkDeep else ClaySurface)
                        .border(1.dp, if (isSelected) ClayInkDeep else ClayBorderHairline, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(slice.color)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${slice.label} ($pct%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color.White else ClayTextPrimary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
