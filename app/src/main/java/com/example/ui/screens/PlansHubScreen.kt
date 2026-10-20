package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.money.PkrFormatter
import com.example.ui.theme.ClayAccentLime
import com.example.ui.theme.ClayBorderHairline
import com.example.ui.theme.ClayInkDeep
import com.example.ui.theme.ClaySurface
import com.example.ui.theme.ClayTextPrimary
import com.example.ui.theme.ClayTextSecondary
import com.example.ui.viewmodel.HisaabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansHubScreen(
    viewModel: HisaabViewModel,
    privacyMode: Boolean,
    onNavigateToKameti: () -> Unit,
    onNavigateToZakat: () -> Unit,
    onNavigateToNeeds: () -> Unit,
    modifier: Modifier = Modifier
) {
    val kametis by viewModel.allKametis.collectAsStateWithLifecycle()
    val activeNeeds by viewModel.activeNeeds.collectAsStateWithLifecycle()
    val charityRecords by viewModel.charityRecords.collectAsStateWithLifecycle()
    val recurringRules by viewModel.recurringRules.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Financial Plans (منصوبہ بندی)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ClayTextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Hero Banner
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, ClayBorderHairline, RoundedCornerShape(18.dp)),
                    color = ClayInkDeep
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Future Commitments & Goals",
                            style = MaterialTheme.typography.titleSmall,
                            color = ClayAccentLime,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Track your long-term obligations, committee savings, religious tithes, and personal purchase planning.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // 1. Kameti Savings Module
            item {
                PlanModuleCard(
                    title = "Kameti Savings (کمیٹی)",
                    subtitle = "${kametis.size} Active Committees · Next payout scheduled",
                    icon = Icons.Default.Savings,
                    tag = "kameti_module_card",
                    onClick = onNavigateToKameti
                )
            }

            // 2. Zakat & Charity Module
            item {
                PlanModuleCard(
                    title = "Zakat & Charity (زکوٰۃ و صدقات)",
                    subtitle = "Gold/Silver Nisab calculator, 2.5% calculation & donation history",
                    icon = Icons.Default.VolunteerActivism,
                    tag = "zakat_module_card",
                    onClick = onNavigateToZakat
                )
            }

            // 3. Needs & Priorities Module
            item {
                PlanModuleCard(
                    title = "Needs & Wishlist (ضروریات)",
                    subtitle = "${activeNeeds.size} Pending purchase goals and priorities",
                    icon = Icons.Default.Checklist,
                    tag = "needs_module_card",
                    onClick = onNavigateToNeeds
                )
            }

            // 4. Recurring Bills summary item
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, ClayBorderHairline, RoundedCornerShape(16.dp)),
                    color = ClaySurface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(ClayAccentLime.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = ClayInkDeep,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Recurring Rules & Bills",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ClayTextPrimary
                            )
                            Text(
                                text = "${recurringRules.size} Automated recurring expense rules active",
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

@Composable
fun PlanModuleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, ClayBorderHairline, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(tag),
        color = ClaySurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ClayInkDeep.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ClayInkDeep,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ClayTextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ClayTextSecondary
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = ClayTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
