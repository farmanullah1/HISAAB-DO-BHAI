package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.ClayAccentLime
import com.example.ui.theme.ClayBorderHairline
import com.example.ui.theme.ClayInkDeep
import com.example.ui.theme.ClayPanelBlue
import com.example.ui.theme.ClaySurface
import com.example.ui.theme.ClayTextPrimary
import com.example.ui.theme.ClayTextSecondary
import com.example.ui.theme.SemanticIncome
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
    val recurringRules by viewModel.recurringRules.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Financial Plans",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ClayTextPrimary
                        )
                        Text(
                            text = "منصوبہ بندی · Commitments & Goals",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClayTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 600.dp
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = if (isWideScreen) 24.dp else 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Header Hero Banner
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, ClayBorderHairline, RoundedCornerShape(20.dp)),
                    color = ClayInkDeep
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(ClayAccentLime)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "COMMITMENTS & GOALS",
                                style = MaterialTheme.typography.labelSmall,
                                color = ClayAccentLime,
                                letterSpacing = 0.8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Plan Your Savings & Tithes",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Manage committee circles (Kameti), calculate religious dues (Zakat & Nisab), and prioritize future shopping needs offline.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            if (isWideScreen) {
                // Wide Screen: Row 1 (Kameti & Zakat)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            PlanModuleCard(
                                title = "Kameti Savings (کمیٹی)",
                                subtitle = if (kametis.isEmpty()) "Start a committee pool or track contributions" else "${kametis.size} Active Circles · Payouts & contributions",
                                badge = if (kametis.isNotEmpty()) "${kametis.size} Active" else null,
                                icon = Icons.Default.Savings,
                                iconTint = SemanticIncome,
                                iconBg = SemanticIncome.copy(alpha = 0.12f),
                                tag = "kameti_module_card",
                                onClick = onNavigateToKameti
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            PlanModuleCard(
                                title = "Zakat & Charity (زکوٰۃ و صدقات)",
                                subtitle = "Gold/Silver Nisab calculator, 2.5% calculation & donation history",
                                badge = "2.5% Nisab",
                                icon = Icons.Default.VolunteerActivism,
                                iconTint = Color(0xFFB45309),
                                iconBg = Color(0xFFFDE68A).copy(alpha = 0.45f),
                                tag = "zakat_module_card",
                                onClick = onNavigateToZakat
                            )
                        }
                    }
                }

                // Wide Screen: Row 2 (Needs & Recurring)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            PlanModuleCard(
                                title = "Needs & Wishlist (ضروریات)",
                                subtitle = if (activeNeeds.isEmpty()) "Organize planned purchases by priority" else "${activeNeeds.size} Pending purchase goals and priorities",
                                badge = if (activeNeeds.isNotEmpty()) "${activeNeeds.size} Items" else null,
                                icon = Icons.Default.Checklist,
                                iconTint = ClayInkDeep,
                                iconBg = ClayAccentLime.copy(alpha = 0.35f),
                                tag = "needs_module_card",
                                onClick = onNavigateToNeeds
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .border(1.dp, ClayBorderHairline, RoundedCornerShape(18.dp)),
                                color = ClaySurface,
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(ClayPanelBlue),
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
                                            text = "Recurring Bills",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ClayTextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${recurringRules.size} recurring rules scheduled",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ClayTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Phone single column
                item {
                    PlanModuleCard(
                        title = "Kameti Savings (کمیٹی)",
                        subtitle = if (kametis.isEmpty()) "Start a committee pool or track contributions" else "${kametis.size} Active Circles · Payouts & contributions",
                        badge = if (kametis.isNotEmpty()) "${kametis.size} Active" else null,
                        icon = Icons.Default.Savings,
                        iconTint = SemanticIncome,
                        iconBg = SemanticIncome.copy(alpha = 0.12f),
                        tag = "kameti_module_card",
                        onClick = onNavigateToKameti
                    )
                }

                item {
                    PlanModuleCard(
                        title = "Zakat & Charity (زکوٰۃ و صدقات)",
                        subtitle = "Gold/Silver Nisab calculator, 2.5% calculation & donation history",
                        badge = "2.5% Nisab",
                        icon = Icons.Default.VolunteerActivism,
                        iconTint = Color(0xFFB45309),
                        iconBg = Color(0xFFFDE68A).copy(alpha = 0.45f),
                        tag = "zakat_module_card",
                        onClick = onNavigateToZakat
                    )
                }

                item {
                    PlanModuleCard(
                        title = "Needs & Wishlist (ضروریات)",
                        subtitle = if (activeNeeds.isEmpty()) "Organize planned purchases by priority" else "${activeNeeds.size} Pending purchase goals and priorities",
                        badge = if (activeNeeds.isNotEmpty()) "${activeNeeds.size} Items" else null,
                        icon = Icons.Default.Checklist,
                        iconTint = ClayInkDeep,
                        iconBg = ClayAccentLime.copy(alpha = 0.35f),
                        tag = "needs_module_card",
                        onClick = onNavigateToNeeds
                    )
                }

                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.dp, ClayBorderHairline, RoundedCornerShape(18.dp)),
                        color = ClaySurface,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(ClayPanelBlue),
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
                                    text = "Automated Recurring Bills",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ClayTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${recurringRules.size} recurring payment rules currently scheduled",
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
}

@Composable
fun PlanModuleCard(
    title: String,
    subtitle: String,
    badge: String? = null,
    icon: ImageVector,
    iconTint: Color = ClayInkDeep,
    iconBg: Color = ClaySurface,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, ClayBorderHairline, RoundedCornerShape(18.dp))
            .testTag(tag),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ClayTextPrimary,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(ClayAccentLime)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ClayInkDeep
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ClayTextSecondary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(ClaySurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open",
                    tint = ClayTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
