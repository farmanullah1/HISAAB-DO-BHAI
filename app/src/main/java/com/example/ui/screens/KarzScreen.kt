package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DebtDirection
import com.example.data.model.DebtKind
import com.example.data.model.DebtStatus
import com.example.data.model.DebtWithDetails
import com.example.data.model.KarzSummary
import com.example.domain.Money
import com.example.ui.components.ClayEmptyState
import com.example.ui.components.KarzSummaryCard
import com.example.ui.theme.ClayAccentLime
import com.example.ui.theme.ClayBorderHairline
import com.example.ui.theme.ClayInkDeep
import com.example.ui.theme.ClaySurface
import com.example.ui.theme.ClayTextPrimary
import com.example.ui.theme.ClayTextSecondary
import com.example.ui.theme.SemanticExpense
import com.example.ui.theme.SemanticIncome
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class KarzFilterTab(val label: String) {
    ACTIVE("Active"),
    DUE_SOON("Due Soon"),
    OVERDUE("Overdue"),
    SETTLED("Settled"),
    ALL("All")
}

@Composable
fun KarzScreen(
    summary: KarzSummary,
    debts: List<DebtWithDetails>,
    privacyMode: Boolean,
    onNavigateToAddGave: () -> Unit,
    onNavigateToAddTook: () -> Unit,
    onDebtSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(KarzFilterTab.ACTIVE) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }

    val activeCount = remember(debts) { debts.count { it.outstandingPaisa > 0 && !it.isOverdue } }
    val dueSoonCount = remember(debts) { debts.count { it.isDueSoon } }
    val overdueCount = remember(debts) { debts.count { it.isOverdue } }
    val settledCount = remember(debts) { debts.count { it.debt.status == DebtStatus.SETTLED || it.debt.status == DebtStatus.WRITTEN_OFF } }
    val allCount = remember(debts) { debts.size }

    val filteredDebts = remember(debts, selectedTab, searchQuery) {
        debts.filter { item ->
            val matchesTab = when (selectedTab) {
                KarzFilterTab.ACTIVE -> item.outstandingPaisa > 0 && !item.isOverdue
                KarzFilterTab.DUE_SOON -> item.isDueSoon
                KarzFilterTab.OVERDUE -> item.isOverdue
                KarzFilterTab.SETTLED -> item.debt.status == DebtStatus.SETTLED || item.debt.status == DebtStatus.WRITTEN_OFF
                KarzFilterTab.ALL -> true
            }

            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                item.debt.personName.lowercase().contains(q) ||
                        item.debt.description.lowercase().contains(q) ||
                        item.debt.phoneNumber.contains(q)
            }

            matchesTab && matchesSearch
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 600.dp
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("karz_screen_list"),
                contentPadding = PaddingValues(horizontal = if (isWideScreen) 24.dp else 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ClayAccentLime),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Handshake,
                                contentDescription = null,
                                tint = ClayInkDeep,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Karz Management",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = ClayTextPrimary
                            )
                            Text(
                                text = "Lend & Borrow · Double-Entry Integrity",
                                style = MaterialTheme.typography.labelSmall,
                                color = ClayTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = { isSearchVisible = !isSearchVisible },
                        modifier = Modifier.testTag("karz_search_button")
                    ) {
                        Icon(
                            imageVector = if (isSearchVisible) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search karz",
                            tint = ClayTextSecondary
                        )
                    }
                }
            }

            // Search Bar
            if (isSearchVisible) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by person name or notes...") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = ClayTextSecondary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = ClayTextSecondary)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClayInkDeep,
                            unfocusedBorderColor = ClayBorderHairline,
                            focusedContainerColor = ClaySurface,
                            unfocusedContainerColor = ClaySurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("karz_search_input")
                    )
                }
            }

            // Summary Card
            item {
                KarzSummaryCard(
                    summary = summary,
                    privacyMode = privacyMode,
                    onViewAll = { selectedTab = KarzFilterTab.ALL }
                )
            }

            // Two Prominent Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onNavigateToAddGave,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("action_i_gave_karz"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ClayInkDeep,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CallMade,
                            contentDescription = null,
                            tint = ClayAccentLime,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ I Gave Karz",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = onNavigateToAddTook,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("action_i_took_karz"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ClayInkDeep
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ClayInkDeep)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CallReceived,
                            contentDescription = null,
                            tint = ClayInkDeep,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ I Took Karz",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Filter Tabs with counter badges
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(KarzFilterTab.values()) { tab ->
                        val isSelected = selectedTab == tab
                        val count = when (tab) {
                            KarzFilterTab.ACTIVE -> activeCount
                            KarzFilterTab.DUE_SOON -> dueSoonCount
                            KarzFilterTab.OVERDUE -> overdueCount
                            KarzFilterTab.SETTLED -> settledCount
                            KarzFilterTab.ALL -> allCount
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            label = {
                                Text(
                                    text = "${tab.label} ($count)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ClayAccentLime,
                                selectedLabelColor = ClayInkDeep,
                                containerColor = ClaySurface,
                                labelColor = ClayTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) ClayAccentLime else ClayBorderHairline,
                                selectedBorderColor = ClayAccentLime
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }

            // Debt Records List
            if (filteredDebts.isEmpty()) {
                item {
                    ClayEmptyState(
                        icon = Icons.Default.Handshake,
                        title = when (selectedTab) {
                            KarzFilterTab.ACTIVE -> "No active debts"
                            KarzFilterTab.DUE_SOON -> "No debts due soon"
                            KarzFilterTab.OVERDUE -> "No overdue debts"
                            KarzFilterTab.SETTLED -> "No settled debts yet"
                            KarzFilterTab.ALL -> "No debts recorded yet"
                        },
                        description = "Record money you lent to someone or borrowed from someone. Every repayment and advance maintains full audit history.",
                        actionTitle = "+ I Gave Karz",
                        onAction = onNavigateToAddGave,
                        illustrationRes = if (searchQuery.isEmpty()) com.example.R.drawable.img_clay_karz_art else null
                    )
                }
            } else {
                items(
                    items = filteredDebts,
                    key = { it.debt.id }
                ) { item ->
                    DebtCard(
                        item = item,
                        privacyMode = privacyMode,
                        onClick = { onDebtSelected(item.debt.id) }
                    )
                }
            }
        }
    }
}
}

@Composable
fun DebtCard(
    item: DebtWithDetails,
    privacyMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val debt = item.debt
    val isGave = debt.direction == DebtDirection.I_GAVE
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }

    val statusBadgeColor = when {
        debt.status == DebtStatus.SETTLED -> SemanticIncome
        debt.status == DebtStatus.WRITTEN_OFF -> ClayTextSecondary
        item.isOverdue -> SemanticExpense
        item.isDueSoon -> ClayInkDeep
        else -> ClayTextSecondary
    }

    val statusBadgeText = when {
        debt.status == DebtStatus.SETTLED -> "Settled (چکتا)"
        debt.status == DebtStatus.WRITTEN_OFF -> "Written Off"
        item.isOverdue -> "Overdue"
        item.isDueSoon -> "Due Soon"
        item.totalRepaidPaisa > 0 -> "Partially Paid"
        else -> "Active"
    }

    val progress = if (item.totalPrincipalPaisa > 0) {
        (item.totalRepaidPaisa.toFloat() / item.totalPrincipalPaisa.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, ClayBorderHairline, RoundedCornerShape(18.dp))
            .testTag("debt_card_${debt.id}"),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Top Row: Avatar + Person Name + Direction Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isGave) SemanticIncome.copy(alpha = 0.12f)
                                else SemanticExpense.copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = debt.personName.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isGave) SemanticIncome else SemanticExpense
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = debt.personName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ClayTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (debt.phoneNumber.isNotBlank()) {
                            Text(
                                text = debt.phoneNumber,
                                style = MaterialTheme.typography.labelSmall,
                                color = ClayTextSecondary
                            )
                        } else if (debt.description.isNotBlank()) {
                            Text(
                                text = debt.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = ClayTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Direction Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(
                            if (isGave) SemanticIncome.copy(alpha = 0.12f)
                            else SemanticExpense.copy(alpha = 0.12f)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isGave) Icons.AutoMirrored.Filled.CallMade else Icons.AutoMirrored.Filled.CallReceived,
                            contentDescription = null,
                            tint = if (isGave) SemanticIncome else SemanticExpense,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isGave) "I Gave" else "I Took",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isGave) SemanticIncome else SemanticExpense,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Outstanding Amount Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = if (debt.kind == DebtKind.ITEM_SERVICE) "Item / Service Loan" else "Outstanding Balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClayTextSecondary
                    )
                    Text(
                        text = if (privacyMode) "₨ ••••••" else if (debt.kind == DebtKind.ITEM_SERVICE) {
                            if (debt.itemQuantity != null) "${debt.itemQuantity} ${debt.itemUnit} ${debt.itemDescription}".trim()
                            else debt.itemDescription.ifBlank { "Item Loan" }
                        } else Money.formatPkr(item.outstandingPaisa),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isGave) SemanticIncome else SemanticExpense,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusBadgeColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = statusBadgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusBadgeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (debt.dueDateEpochMs != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Due: ${dateFormatter.format(Date(debt.dueDateEpochMs))}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = ClayTextSecondary
                        )
                    }
                }
            }

            if (debt.kind == DebtKind.MONEY) {
                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = if (isGave) SemanticIncome else SemanticExpense,
                    trackColor = ClaySurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Repaid vs Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (privacyMode) "Repaid: ••••••" else "Repaid: ${Money.formatPkr(item.totalRepaidPaisa)} (${(progress * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = ClayTextSecondary
                    )
                    Text(
                        text = if (privacyMode) "Total: ••••••" else "Total: ${Money.formatPkr(item.totalPrincipalPaisa)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = ClayTextSecondary
                    )
                }
            }
        }
    }
}
