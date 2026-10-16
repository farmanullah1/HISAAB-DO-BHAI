package com.example.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import com.example.ui.karz.KarzSlipPreviewDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.DebtDirection
import com.example.data.model.DebtEventEntity
import com.example.data.model.DebtEventType
import com.example.data.model.DebtStatus
import com.example.data.model.DebtWithDetails
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtDetailScreen(
    debtDetails: DebtWithDetails?,
    accounts: List<AccountEntity>,
    privacyMode: Boolean,
    onRecordRepayment: (debtId: Long, amountPaisa: Long, dateEpochMs: Long, accountId: Long?, recordCashMovement: Boolean, note: String) -> Unit,
    onRecordAdvance: (debtId: Long, amountPaisa: Long, dateEpochMs: Long, accountId: Long?, recordCashMovement: Boolean, note: String) -> Unit,
    onSettleWithAdjustment: (debtId: Long, reason: String) -> Unit,
    onChangeDueDate: (debtId: Long, newDueDateEpochMs: Long?) -> Unit,
    onDeleteDebt: (debtId: Long) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }

    var showRepaymentDialog by remember { mutableStateOf(false) }
    var isFullSettlementRequested by remember { mutableStateOf(false) }
    var showAdvanceDialog by remember { mutableStateOf(false) }
    var showSettleDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSlipPreviewDialog by remember { mutableStateOf(false) }

    if (debtDetails == null) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("Debt Details") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ClayInkDeep)
            }
        }
        return
    }

    val debt = debtDetails.debt
    val isGave = debt.direction == DebtDirection.I_GAVE
    val progress = if (debtDetails.totalPrincipalPaisa > 0) {
        (debtDetails.totalRepaidPaisa.toFloat() / debtDetails.totalPrincipalPaisa.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val isSettled = debt.status == DebtStatus.SETTLED || debtDetails.outstandingPaisa == 0L
    val isWrittenOff = debt.status == DebtStatus.WRITTEN_OFF

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = debt.personName,
                        style = MaterialTheme.typography.titleMedium,
                        color = ClayTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("debt_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ClayTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSlipPreviewDialog = true },
                        modifier = Modifier.testTag("share_karz_top_bar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Slip",
                            tint = ClayInkDeep
                        )
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.testTag("delete_debt_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Debt",
                            tint = SemanticExpense
                        )
                    }
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
                .padding(innerPadding)
                .testTag("debt_detail_content"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Outstanding Hero Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, ClayBorderHairline, RoundedCornerShape(20.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Direction Badge & Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
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
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isGave) "I Gave Karz (قرض دیا)" else "I Took Karz (قرض لیا)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isGave) SemanticIncome else SemanticExpense,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Status Chip
                            val statusColor = when {
                                isSettled -> SemanticIncome
                                isWrittenOff -> ClayTextSecondary
                                debtDetails.isOverdue -> SemanticExpense
                                debtDetails.isDueSoon -> ClayInkDeep
                                else -> ClayTextSecondary
                            }
                            val statusText = when {
                                isSettled -> "Settled (چکتا)"
                                isWrittenOff -> "Written Off"
                                debtDetails.isOverdue -> "Overdue"
                                debtDetails.isDueSoon -> "Due Soon"
                                debtDetails.totalRepaidPaisa > 0 -> "Partially Paid"
                                else -> "Active"
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(statusColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = statusColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isGave) "Amount Ahmed/Person Owes You" else "Amount You Owe Person",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClayTextSecondary
                        )
                        Text(
                            text = if (privacyMode) "••••••" else Money.formatPkr(debtDetails.outstandingPaisa),
                            style = MaterialTheme.typography.headlineLarge,
                            color = if (isGave) SemanticIncome else SemanticExpense,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Repayment Progress Bar
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = if (isGave) SemanticIncome else SemanticExpense,
                            trackColor = ClaySurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Repaid: ${if (privacyMode) "••••" else Money.formatPkr(debtDetails.totalRepaidPaisa)} (${(progress * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = ClayTextSecondary
                            )
                            Text(
                                text = "Total: ${if (privacyMode) "••••" else Money.formatPkr(debtDetails.totalPrincipalPaisa)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ClayTextSecondary
                            )
                        }
                    }
                }
            }

            // Quick Actions: Record Repayment / Add Advance
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!isSettled && !isWrittenOff) {
                        Button(
                            onClick = {
                                isFullSettlementRequested = true
                                showRepaymentDialog = true
                            },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(48.dp)
                                .testTag("mark_paid_back_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ClayInkDeep,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = ClayAccentLime)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isGave) "Receive All" else "Mark as Paid Back",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                isFullSettlementRequested = false
                                showRepaymentDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("record_repayment_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ClayBorderHairline)
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = ClayInkDeep, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Partial",
                                style = MaterialTheme.typography.labelMedium,
                                color = ClayInkDeep
                            )
                        }

                        IconButton(
                            onClick = { showAdvanceDialog = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, ClayBorderHairline, RoundedCornerShape(12.dp))
                                .testTag("record_advance_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = if (isGave) "+ Add Advance" else "+ Borrow More", tint = ClayInkDeep)
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SemanticIncome.copy(alpha = 0.1f))
                                .padding(12.dp),
                            color = Color.Transparent
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SemanticIncome)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isSettled) "This debt has been completely settled!" else "This debt was written off.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SemanticIncome,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // WhatsApp Karz Slip & PDF Statement Share Banner Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.5.dp, ClayBorderHairline, RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = androidx.compose.material3.ripple(),
                            onClick = { showSlipPreviewDialog = true }
                        )
                        .testTag("open_karz_slip_preview_card"),
                    color = ClaySurface,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ClayInkDeep),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = ClayAccentLime,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "WhatsApp Karz Slip & PDF Statement",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ClayInkDeep
                                )
                                Text(
                                    text = "شائستہ یاد دہانی اور مکمل اسٹیٹمنٹ شیئر کریں",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ClayTextSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ClayAccentLime)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Share",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ClayInkDeep
                            )
                        }
                    }
                }
            }

            // Key Debt Information Grid Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, ClayBorderHairline, RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "DETAILS & TERMS",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClayTextSecondary,
                            letterSpacing = 0.8.sp
                        )

                        // Given/Taken Date
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Initial Date", style = MaterialTheme.typography.bodyMedium, color = ClayTextSecondary)
                            Text(text = dateFormatter.format(Date(debt.dateEpochMs)), style = MaterialTheme.typography.bodyMedium, color = ClayTextPrimary)
                        }

                        // Due Date
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Due Date", style = MaterialTheme.typography.bodyMedium, color = ClayTextSecondary)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        val cal = Calendar.getInstance().apply {
                                            timeInMillis = debt.dueDateEpochMs ?: System.currentTimeMillis()
                                        }
                                        DatePickerDialog(
                                            context,
                                            { _, y, m, d ->
                                                val newCal = Calendar.getInstance().apply { set(y, m, d) }
                                                onChangeDueDate(debt.id, newCal.timeInMillis)
                                            },
                                            cal.get(Calendar.YEAR),
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (debt.dueDateEpochMs != null) dateFormatter.format(Date(debt.dueDateEpochMs)) else "Set Date",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ClayInkDeep,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Edit, contentDescription = "Edit due date", modifier = Modifier.size(14.dp), tint = ClayTextSecondary)
                            }
                        }

                        // Contact phone
                        if (debt.phoneNumber.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Phone", style = MaterialTheme.typography.bodyMedium, color = ClayTextSecondary)
                                Text(text = debt.phoneNumber, style = MaterialTheme.typography.bodyMedium, color = ClayTextPrimary)
                            }
                        }

                        // Linked Account
                        if (debtDetails.linkedAccountName != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Linked Account", style = MaterialTheme.typography.bodyMedium, color = ClayTextSecondary)
                                Text(text = debtDetails.linkedAccountName, style = MaterialTheme.typography.bodyMedium, color = ClayTextPrimary)
                            }
                        }

                        // Notes
                        if (debt.notes.isNotBlank() || debt.description.isNotBlank()) {
                            Column {
                                Text(text = "Notes / Description", style = MaterialTheme.typography.bodyMedium, color = ClayTextSecondary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = listOf(debt.description, debt.notes).filter { it.isNotBlank() }.joinToString(" · "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ClayTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Settle / Write-off action button (if outstanding remains)
            if (!isSettled && !isWrittenOff) {
                item {
                    OutlinedButton(
                        onClick = { showSettleDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ClayBorderHairline)
                    ) {
                        Text(
                            text = "Settle / Write Off with Adjustment Reason",
                            style = MaterialTheme.typography.labelMedium,
                            color = ClayTextSecondary
                        )
                    }
                }
            }

            // Chronological Activity Timeline Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = ClayTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ACTIVITY HISTORY (${debtDetails.events.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClayTextSecondary,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }

            // Event items
            items(
                items = debtDetails.events.reversed(),
                key = { it.id }
            ) { event ->
                DebtEventRow(
                    event = event,
                    isGave = isGave,
                    privacyMode = privacyMode
                )
            }
        }
    }

    // Repayment Dialog
    if (showRepaymentDialog) {
        DebtRepaymentDialog(
            debt = debt,
            maxAmountPaisa = debtDetails.outstandingPaisa,
            initialFullAmount = isFullSettlementRequested,
            accounts = accounts,
            onDismiss = { showRepaymentDialog = false },
            onConfirm = { amountPaisa, dateEpoch, accountId, trackCash, note ->
                showRepaymentDialog = false
                onRecordRepayment(debt.id, amountPaisa, dateEpoch, accountId, trackCash, note)
            }
        )
    }

    // Additional Advance Dialog
    if (showAdvanceDialog) {
        DebtAdvanceDialog(
            debt = debt,
            accounts = accounts,
            onDismiss = { showAdvanceDialog = false },
            onConfirm = { amountPaisa, dateEpoch, accountId, trackCash, note ->
                showAdvanceDialog = false
                onRecordAdvance(debt.id, amountPaisa, dateEpoch, accountId, trackCash, note)
            }
        )
    }

    // Settle / Write Off Dialog
    if (showSettleDialog) {
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSettleDialog = false },
            title = { Text("Settle / Write Off Debt") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Outstanding balance: ${Money.formatPkr(debtDetails.outstandingPaisa)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Enter reason for balance adjustment or write-off (e.g. debt forgiven, mutual discount, cash resolved externally):",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClayTextSecondary
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        placeholder = { Text("Reason (required)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reason.isNotBlank()) {
                            showSettleDialog = false
                            onSettleWithAdjustment(debt.id, reason)
                        }
                    },
                    enabled = reason.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = ClayInkDeep)
                ) {
                    Text("Confirm Settlement")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettleDialog = false }) {
                    Text("Cancel", color = ClayTextSecondary)
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Debt Record?") },
            text = {
                Text("This will permanently remove this debt and its activity history. Any past cash movements in accounts will remain intact.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteDebt(debt.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticExpense)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = ClayTextSecondary)
                }
            }
        )
    }

    if (showSlipPreviewDialog && debtDetails != null) {
        KarzSlipPreviewDialog(
            debtDetails = debtDetails,
            onDismiss = { showSlipPreviewDialog = false }
        )
    }
}

@Composable
fun DebtEventRow(
    event: DebtEventEntity,
    isGave: Boolean,
    privacyMode: Boolean,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.US) }

    val (badgeColor, title) = when (event.type) {
        DebtEventType.ORIGINAL_ADVANCE -> Pair(ClayInkDeep, "Original Principal")
        DebtEventType.ADDITIONAL_ADVANCE -> Pair(
            if (isGave) SemanticExpense else SemanticIncome,
            if (isGave) "Additional Lent" else "Additional Borrowed"
        )
        DebtEventType.REPAYMENT -> Pair(
            SemanticIncome,
            "Repayment Recorded"
        )
        DebtEventType.ADJUSTMENT -> Pair(ClayAccentLime, "Adjustment")
        DebtEventType.WRITE_OFF -> Pair(ClayTextSecondary, "Written Off / Forgiven")
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, ClayBorderHairline, RoundedCornerShape(14.dp)),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ClaySurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (event.type) {
                        DebtEventType.REPAYMENT -> Icons.Default.Payments
                        DebtEventType.ADDITIONAL_ADVANCE -> Icons.Default.Add
                        DebtEventType.WRITE_OFF -> Icons.Default.CheckCircle
                        else -> Icons.Default.Handshake
                    },
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = ClayTextPrimary
                )
                if (event.note.isNotBlank()) {
                    Text(
                        text = event.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = ClayTextSecondary
                    )
                }
                Text(
                    text = dateFormatter.format(Date(event.dateEpochMs)),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = ClayTextSecondary.copy(alpha = 0.6f)
                )
            }

            Text(
                text = if (privacyMode) "••••••" else Money.formatPkr(event.amountPaisa),
                style = MaterialTheme.typography.titleMedium,
                color = badgeColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DebtRepaymentDialog(
    debt: com.example.data.model.DebtEntity,
    maxAmountPaisa: Long,
    accounts: List<AccountEntity>,
    initialFullAmount: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (amountPaisa: Long, dateEpochMs: Long, accountId: Long?, recordCash: Boolean, note: String) -> Unit
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }

    var amountInput by remember {
        mutableStateOf(if (initialFullAmount) Money.toDecimalString(maxAmountPaisa) else "")
    }
    var note by remember { mutableStateOf("") }
    var dateEpochMs by remember { mutableStateOf(System.currentTimeMillis()) }
    var recordCashMovement by remember { mutableStateOf(true) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id) }

    val amountPaisa = Money.parsePaisa(amountInput) ?: 0L
    val isValid = amountPaisa > 0L && amountPaisa <= maxAmountPaisa

    val dialogTitle = if (debt.direction == DebtDirection.I_TOOK) {
        if (initialFullAmount) "Mark as Paid Back (ادائیگی)" else "Record Partial Repayment"
    } else {
        if (initialFullAmount) "Mark as Received in Full (وصولی)" else "Record Partial Receipt"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Outstanding remaining: ${Money.formatPkr(maxAmountPaisa)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ClayTextSecondary
                )

                // Quick settlement chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (amountPaisa == maxAmountPaisa) ClayInkDeep else ClaySurface)
                            .clickable { amountInput = Money.toDecimalString(maxAmountPaisa) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Full (${Money.formatPkr(maxAmountPaisa)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (amountPaisa == maxAmountPaisa) Color.White else ClayTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (maxAmountPaisa > 10000L) {
                        val halfPaisa = maxAmountPaisa / 2
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (amountPaisa == halfPaisa) ClayInkDeep else ClaySurface)
                                .clickable { amountInput = Money.toDecimalString(halfPaisa) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "50% (${Money.formatPkr(halfPaisa)})",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (amountPaisa == halfPaisa) Color.White else ClayTextPrimary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Payment Amount (PKR) *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                // Date Picker Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, ClayBorderHairline, RoundedCornerShape(8.dp))
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = dateEpochMs }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val newCal = Calendar.getInstance().apply { set(y, m, d) }
                                    dateEpochMs = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                    color = ClaySurface
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Date: ${dateFormatter.format(Date(dateEpochMs))}", style = MaterialTheme.typography.bodySmall)
                    }
                }

                // Account Movement Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Track cash in account", style = MaterialTheme.typography.bodySmall)
                    Switch(
                        checked = recordCashMovement,
                        onCheckedChange = { recordCashMovement = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = ClayInkDeep, checkedTrackColor = ClayAccentLime)
                    )
                }

                if (recordCashMovement) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        accounts.forEach { acc ->
                            val sel = selectedAccountId == acc.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (sel) ClayInkDeep else ClaySurface)
                                    .clickable { selectedAccountId = acc.id }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = acc.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (sel) Color.White else ClayTextPrimary
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        amountPaisa,
                        dateEpochMs,
                        if (recordCashMovement) selectedAccountId else null,
                        recordCashMovement,
                        note.trim()
                    )
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = ClayInkDeep)
            ) {
                Text("Confirm Repayment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ClayTextSecondary)
            }
        }
    )
}

@Composable
fun DebtAdvanceDialog(
    debt: com.example.data.model.DebtEntity,
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (amountPaisa: Long, dateEpochMs: Long, accountId: Long?, recordCash: Boolean, note: String) -> Unit
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }

    var amountInput by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var dateEpochMs by remember { mutableStateOf(System.currentTimeMillis()) }
    var recordCashMovement by remember { mutableStateOf(true) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id) }

    val amountPaisa = Money.parsePaisa(amountInput) ?: 0L
    val isValid = amountPaisa > 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (debt.direction == DebtDirection.I_GAVE) "Give Additional Money" else "Borrow Additional Money") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Advance Amount (PKR) *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, ClayBorderHairline, RoundedCornerShape(8.dp))
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = dateEpochMs }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val newCal = Calendar.getInstance().apply { set(y, m, d) }
                                    dateEpochMs = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                    color = ClaySurface
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Date: ${dateFormatter.format(Date(dateEpochMs))}", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Track cash in account", style = MaterialTheme.typography.bodySmall)
                    Switch(
                        checked = recordCashMovement,
                        onCheckedChange = { recordCashMovement = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = ClayInkDeep, checkedTrackColor = ClayAccentLime)
                    )
                }

                if (recordCashMovement) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        accounts.forEach { acc ->
                            val sel = selectedAccountId == acc.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (sel) ClayInkDeep else ClaySurface)
                                    .clickable { selectedAccountId = acc.id }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = acc.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (sel) Color.White else ClayTextPrimary
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        amountPaisa,
                        dateEpochMs,
                        if (recordCashMovement) selectedAccountId else null,
                        recordCashMovement,
                        note.trim()
                    )
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = ClayInkDeep)
            ) {
                Text("Confirm Advance")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ClayTextSecondary)
            }
        }
    )
}
