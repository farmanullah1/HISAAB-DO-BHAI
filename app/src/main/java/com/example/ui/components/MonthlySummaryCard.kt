package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MonthlySummary
import com.example.domain.Money
import com.example.ui.theme.ClayBorderHairline
import com.example.ui.theme.ClayTextSecondary
import com.example.ui.theme.SemanticExpense
import com.example.ui.theme.SemanticIncome

@Composable
fun MonthlySummaryCard(
    summary: MonthlySummary,
    privacyMode: Boolean,
    modifier: Modifier = Modifier
) {
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
                text = "PAST 30 DAYS ACTIVITY",
                style = MaterialTheme.typography.labelSmall,
                color = ClayTextSecondary,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Income column
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Income indicator",
                        tint = SemanticIncome,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column {
                        Text(
                            text = "Income",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClayTextSecondary
                        )
                        Text(
                            text = if (privacyMode) "••••••" else Money.formatPkr(summary.incomePaisa),
                            style = MaterialTheme.typography.titleMedium,
                            color = SemanticIncome
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Expense column
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Expense indicator",
                        tint = SemanticExpense,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column {
                        Text(
                            text = "Expenses",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClayTextSecondary
                        )
                        Text(
                            text = if (privacyMode) "••••••" else Money.formatPkr(summary.expensePaisa),
                            style = MaterialTheme.typography.titleMedium,
                            color = SemanticExpense
                        )
                    }
                }
            }
        }
    }
}
