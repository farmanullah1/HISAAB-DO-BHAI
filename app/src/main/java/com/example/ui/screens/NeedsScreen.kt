package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NeedEntity
import com.example.domain.Money
import com.example.ui.components.AddNeedDialog
import com.example.ui.components.ClayEmptyState
import com.example.ui.theme.ClayAccentLime
import com.example.ui.theme.ClayBorderHairline
import com.example.ui.theme.ClayInkDeep
import com.example.ui.theme.ClaySurface
import com.example.ui.theme.ClayTextPrimary
import com.example.ui.theme.ClayTextSecondary
import kotlinx.coroutines.launch

@Composable
fun NeedsScreen(
    activeNeeds: List<NeedEntity>,
    doneNeeds: List<NeedEntity>,
    onAddNeed: (name: String, pricePaisa: Long?, where: String, externalUrl: String, note: String) -> Unit,
    onUpdateNeed: (NeedEntity) -> Unit,
    onToggleDone: (NeedEntity) -> Unit,
    onDeleteNeed: (Long) -> Unit,
    onUndoDeleteNeed: () -> Unit,
    onClearAllDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingNeed by remember { mutableStateOf<NeedEntity?>(null) }
    var doneSectionExpanded by remember { mutableStateOf(false) }

    val totalExpectedPaisa = activeNeeds.mapNotNull { it.expectedPricePaisa }.sum()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ClayAccentLime,
                contentColor = ClayInkDeep,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_need_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add need",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("needs_list"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, ClayBorderHairline, RoundedCornerShape(18.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "WHAT I NEED · SHOPPING PLANNER",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClayTextSecondary,
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = Money.formatPkr(totalExpectedPaisa),
                            style = MaterialTheme.typography.displayLarge,
                            color = ClayTextPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Estimated total for ${activeNeeds.size} active items (independent of ledger balances)",
                            style = MaterialTheme.typography.bodySmall,
                            color = ClayTextSecondary
                        )
                    }
                }
            }

            // Section Label
            item {
                Text(
                    text = "ACTIVE ITEMS",
                    style = MaterialTheme.typography.labelSmall,
                    color = ClayTextSecondary,
                    letterSpacing = 0.8.sp
                )
            }

            // Empty state for active needs
            if (activeNeeds.isEmpty()) {
                item {
                    ClayEmptyState(
                        icon = Icons.Default.Checklist,
                        title = "Nothing on your list yet",
                        description = "Add things you're planning to buy. What I Need tracks items and estimated costs without affecting your real account balances.",
                        actionTitle = "+ Add Item",
                        onAction = { showAddDialog = true }
                    )
                }
            } else {
                items(
                    items = activeNeeds,
                    key = { it.id }
                ) { need ->
                    NeedItemRow(
                        need = need,
                        onToggle = { onToggleDone(need) },
                        onEdit = { editingNeed = need },
                        onOpenUrl = { url ->
                            runCatching {
                                val fixedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fixedUrl))
                                context.startActivity(intent)
                            }
                        },
                        onDelete = {
                            val needId = need.id
                            onDeleteNeed(needId)
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                val result = snackbarHostState.showSnackbar(
                                    message = "Item removed",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    onUndoDeleteNeed()
                                }
                            }
                        }
                    )
                }
            }

            // Done section (collapsible)
            if (doneNeeds.isNotEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, ClayBorderHairline, RoundedCornerShape(14.dp)),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { doneSectionExpanded = !doneSectionExpanded },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Purchased / Completed (${doneNeeds.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = ClayTextSecondary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = if (doneSectionExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Toggle done items",
                                        tint = ClayTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                TextButton(onClick = onClearAllDone) {
                                    Text(
                                        text = "Clear all",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            AnimatedVisibility(visible = doneSectionExpanded) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    doneNeeds.forEach { item ->
                                        NeedItemRow(
                                            need = item,
                                            onToggle = { onToggleDone(item) },
                                            onEdit = { editingNeed = item },
                                            onOpenUrl = null,
                                            onDelete = { onDeleteNeed(item.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    if (showAddDialog) {
        AddNeedDialog(
            initialNeed = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, price, where, url, note ->
                onAddNeed(name, price, where, url, note)
                showAddDialog = false
            }
        )
    }

    editingNeed?.let { need ->
        AddNeedDialog(
            initialNeed = need,
            onDismiss = { editingNeed = null },
            onSave = { name, price, where, url, note ->
                onUpdateNeed(
                    need.copy(
                        name = name,
                        expectedPricePaisa = price,
                        whereToGet = where,
                        externalUrl = url,
                        note = note
                    )
                )
                editingNeed = null
            }
        )
    }
}

@Composable
private fun NeedItemRow(
    need: NeedEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onOpenUrl: ((String) -> Unit)? = null,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, ClayBorderHairline, RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = need.isDone,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = ClayAccentLime,
                    checkmarkColor = ClayInkDeep,
                    uncheckedColor = ClayTextSecondary
                ),
                modifier = Modifier.testTag("need_checkbox_${need.id}")
            )

            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 6.dp)
            ) {
                Text(
                    text = need.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (need.isDone) ClayTextSecondary else ClayTextPrimary,
                    textDecoration = if (need.isDone) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (need.whereToGet.isNotBlank() || need.note.isNotBlank() || need.externalUrl.isNotBlank()) {
                    val sub = buildString {
                        if (need.whereToGet.isNotBlank()) append("At: ${need.whereToGet} ")
                        if (need.note.isNotBlank()) append("· ${need.note}")
                    }
                    Text(
                        text = sub,
                        style = MaterialTheme.typography.bodySmall,
                        color = ClayTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (need.expectedPricePaisa != null) {
                Text(
                    text = Money.formatPkr(need.expectedPricePaisa),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (need.isDone) ClayTextSecondary else ClayTextPrimary
                )
            }

            if (need.externalUrl.isNotBlank() && onOpenUrl != null) {
                IconButton(onClick = { onOpenUrl(need.externalUrl) }) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Open shop link",
                        tint = ClayTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit need",
                    tint = ClayTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete need",
                    tint = ClayTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
