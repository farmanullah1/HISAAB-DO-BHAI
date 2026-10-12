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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BudgetProgressItem
import com.example.domain.Money
import com.example.ui.theme.ClayAccentLimeDeep
import com.example.ui.theme.ClayBorderHairline
import com.example.ui.theme.ClayPanelCoral
import com.example.ui.theme.ClaySurface
import com.example.ui.theme.ClayTextPrimary
import com.example.ui.theme.ClayTextSecondary
import com.example.ui.theme.SemanticExpense
import com.example.ui.theme.SemanticWarning

@Composable
fun BudgetSectionCard(
    budgets: List<BudgetProgressItem>,
    modifier: Modifier = Modifier
) {
    if (budgets.isEmpty()) return

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
                .padding(18.dp)
        ) {
            Text(
                text = "MONTHLY BUDGET PROGRESS",
                style = MaterialTheme.typography.labelSmall,
                color = ClayTextSecondary,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                budgets.forEach { item ->
                    val isExceeded = item.isOverBudget
                    val isWarning = item.isWarningThreshold

                    val cardBackground = when {
                        isExceeded -> ClayPanelCoral
                        isWarning -> ClayPanelCoral.copy(alpha = 0.5f)
                        else -> ClaySurface.copy(alpha = 0.4f)
                    }

                    val progressColor = when {
                        isExceeded -> SemanticExpense
                        isWarning -> SemanticWarning
                        else -> ClayAccentLimeDeep
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardBackground)
                            .border(
                                width = 1.dp,
                                color = if (isExceeded) SemanticExpense.copy(alpha = 0.4f) else ClayBorderHairline,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = CategoryIconHelper.getIcon(item.category.iconKey),
                                        contentDescription = null,
                                        tint = progressColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.category.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = ClayTextPrimary
                                    )
                                }

                                if (isExceeded) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Over budget",
                                            tint = SemanticExpense,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Exceeded",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SemanticExpense
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "${(item.percentageUsed * 100).toInt()}% used",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ClayTextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { (item.percentageUsed).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(9999.dp)),
                                color = progressColor,
                                trackColor = ClayBorderHairline.copy(alpha = 0.4f),
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Spent: ${Money.formatPkr(item.spentPaisa)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ClayTextSecondary
                                )
                                Text(
                                    text = "Limit: ${Money.formatPkr(item.limitPaisa)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ClayTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
