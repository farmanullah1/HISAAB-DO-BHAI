package com.example.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.money.PkrFormatter
import com.example.data.model.AccountEntity
import com.example.data.model.ContributionStatus
import com.example.data.model.KametiCadence
import com.example.data.model.KametiContributionEntity
import com.example.data.model.KametiEntity
import com.example.data.model.PayoutStatus
import com.example.domain.Money
import com.example.ui.theme.ClayAccentLime
import com.example.ui.theme.ClayBorderHairline
import com.example.ui.theme.ClayInkDeep
import com.example.ui.theme.ClaySurface
import com.example.ui.theme.ClayTextPrimary
import com.example.ui.theme.ClayTextSecondary
import com.example.ui.theme.SemanticIncome
import com.example.ui.viewmodel.HisaabViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KametiScreen(
    viewModel: HisaabViewModel,
    accounts: List<AccountEntity>,
    privacyMode: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val kametis by viewModel.allKametis.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }
    var selectedKametiId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kameti Savings (کمیٹی)",
                        style = MaterialTheme.typography.titleMedium,
                        color = ClayTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ClayTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddSheet = true },
                        modifier = Modifier.testTag("add_kameti_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Committee",
                            tint = ClayInkDeep
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (kametis.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = ClayTextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Committees Registered",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ClayTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Track your monthly or weekly committee savings, payout schedules, and contribution records.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClayTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showAddSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ClayInkDeep),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Committee (کمیٹی بنائیں)")
                    }
                }
            }
        } else {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val isWideScreen = maxWidth >= 600.dp
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = if (isWideScreen) 24.dp else 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(kametis, key = { it.id }) { kameti ->
                        KametiCard(
                            kameti = kameti,
                            viewModel = viewModel,
                            accounts = accounts,
                            privacyMode = privacyMode,
                            isExpanded = selectedKametiId == kameti.id,
                            onToggleExpand = {
                                selectedKametiId = if (selectedKametiId == kameti.id) null else kameti.id
                            }
                        )
                    }
                }
            }
        }

        if (showAddSheet) {
            AddKametiSheet(
                onDismiss = { showAddSheet = false },
                onSave = { name, count, amount, cadence, startDay, total, myInstallment, payoutPaisa, note ->
                    viewModel.createKameti(
                        name = name,
                        memberCount = count,
                        contributionPaisa = amount,
                        cadence = cadence,
                        startDateEpochDay = startDay,
                        totalInstallments = total,
                        myPayoutInstallment = myInstallment,
                        expectedPayoutPaisa = payoutPaisa,
                        note = note
                    )
                    showAddSheet = false
                }
            )
        }
    }
}

@Composable
fun KametiCard(
    kameti: KametiEntity,
    viewModel: HisaabViewModel,
    accounts: List<AccountEntity>,
    privacyMode: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val contributionsFlow = remember(kameti.id) { viewModel.getContributionsForKameti(kameti.id) }
    val contributions by contributionsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    val payoutFlow = remember(kameti.id) { viewModel.getPayoutForKameti(kameti.id) }
    val payout by payoutFlow.collectAsStateWithLifecycle(initialValue = null)

    val paidCount = contributions.count { it.status == ContributionStatus.PAID }
    val totalCount = kameti.totalInstallments
    val progress = if (totalCount > 0) paidCount.toFloat() / totalCount else 0f

    val totalExpectedPaisa = kameti.contributionPaisa * totalCount
    val totalRecordedPaisa = contributions.sumOf { it.paidPaisa }

    var payingContribution by remember { mutableStateOf<KametiContributionEntity?>(null) }
    var showClaimPayoutDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, ClayBorderHairline, RoundedCornerShape(16.dp))
            .clickable { onToggleExpand() },
        color = ClaySurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = kameti.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ClayTextPrimary
                    )
                    Text(
                        text = "${kameti.memberCount} members · ${kameti.cadence.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClayTextSecondary
                    )
                }

                Surface(
                    color = ClayAccentLime.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ClayAccentLime.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = if (privacyMode) "₨ ••••••" else PkrFormatter.format(kameti.contributionPaisa),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ClayInkDeep,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = ClayInkDeep,
                trackColor = ClayBorderHairline
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$paidCount of $totalCount recorded",
                    style = MaterialTheme.typography.labelSmall,
                    color = ClayTextSecondary
                )
                Text(
                    text = if (privacyMode) "Recorded ₨ ••• of expected ₨ •••"
                    else "Recorded ${PkrFormatter.format(totalRecordedPaisa)} of expected ${PkrFormatter.format(totalExpectedPaisa)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ClayTextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Payout notice
            val payoutStatusText = if (payout?.status == PayoutStatus.RECEIVED) {
                "Payout received ✓ (${PkrFormatter.format(payout?.receivedPaisa ?: 0L)})"
            } else {
                "Your payout: installment ${kameti.myPayoutInstallment} · expected ${PkrFormatter.format(kameti.expectedPayoutPaisa ?: (kameti.contributionPaisa * kameti.memberCount))}"
            }

            Text(
                text = payoutStatusText,
                style = MaterialTheme.typography.bodySmall,
                color = if (payout?.status == PayoutStatus.RECEIVED) SemanticIncome else ClayTextSecondary
            )

            if (payout?.status != PayoutStatus.RECEIVED) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showClaimPayoutDialog = true },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ClayBorderHairline)
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Claim Payout (وصول کریں)")
                }
            }

            // Expanded list of installments
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Contribution Schedule",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ClayTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                contributions.forEach { c ->
                    ContributionRow(
                        contribution = c,
                        privacyMode = privacyMode,
                        onPayClick = { payingContribution = c }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }

    // Pay Contribution Dialog
    if (payingContribution != null) {
        val target = payingContribution!!
        RecordContributionDialog(
            contribution = target,
            accounts = accounts,
            onDismiss = { payingContribution = null },
            onConfirm = { accountId, amountPaisa, dateEpochDay, note ->
                viewModel.recordKametiContribution(
                    contributionId = target.id,
                    accountId = accountId,
                    amountPaisa = amountPaisa,
                    dateEpochDay = dateEpochDay,
                    note = note
                )
                payingContribution = null
            }
        )
    }

    // Claim Payout Dialog
    if (showClaimPayoutDialog && payout != null) {
        ClaimPayoutDialog(
            kameti = kameti,
            accounts = accounts,
            onDismiss = { showClaimPayoutDialog = false },
            onConfirm = { accountId, receivedPaisa, dateEpochDay, note ->
                viewModel.claimKametiPayout(
                    payoutId = payout!!.id,
                    kametiId = kameti.id,
                    accountId = accountId,
                    receivedAmountPaisa = receivedPaisa,
                    dateEpochDay = dateEpochDay,
                    note = note
                )
                showClaimPayoutDialog = false
            }
        )
    }
}

@Composable
fun ContributionRow(
    contribution: KametiContributionEntity,
    privacyMode: Boolean,
    onPayClick: () -> Unit
) {
    val dateText = remember(contribution.dueDateEpochDay) {
        LocalDate.ofEpochDay(contribution.dueDateEpochDay).format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    }
    val isPaid = contribution.status == ContributionStatus.PAID

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, ClayBorderHairline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isPaid) Icons.Default.CheckCircle else Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = if (isPaid) SemanticIncome else ClayTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Installment ${contribution.installmentIndex}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = ClayTextPrimary
                    )
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.labelSmall,
                        color = ClayTextSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (privacyMode) "₨ •••••" else PkrFormatter.format(contribution.expectedPaisa),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = ClayTextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (!isPaid) {
                    Button(
                        onClick = onPayClick,
                        colors = ButtonDefaults.buttonColors(containerColor = ClayInkDeep),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Pay", fontSize = 12.sp)
                    }
                } else {
                    Surface(
                        color = SemanticIncome.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "PAID",
                            color = SemanticIncome,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddKametiSheet(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        memberCount: Int,
        contributionPaisa: Long,
        cadence: KametiCadence,
        startDateEpochDay: Long,
        totalInstallments: Int,
        myPayoutInstallment: Int,
        expectedPayoutPaisa: Long?,
        note: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var memberCountText by remember { mutableStateOf("10") }
    var contributionText by remember { mutableStateOf("") }
    var cadence by remember { mutableStateOf(KametiCadence.MONTHLY) }
    var startDateEpochDay by remember { mutableStateOf(LocalDate.now().toEpochDay()) }
    var myPayoutInstallmentText by remember { mutableStateOf("1") }
    var note by remember { mutableStateOf("") }

    val members = memberCountText.toIntOrNull() ?: 0
    val contributionPaisa = Money.parsePaisa(contributionText) ?: 0L
    val myInstallment = myPayoutInstallmentText.toIntOrNull() ?: 1
    val isValid = name.isNotBlank() && members >= 2 && contributionPaisa > 0L && myInstallment in 1..members

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "New Committee (نئی کمیٹی)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ClayTextPrimary
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Committee Name *") },
                placeholder = { Text("e.g. Mohalla Committee, Office BC") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = memberCountText,
                    onValueChange = { memberCountText = it },
                    label = { Text("Members (Count) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = contributionText,
                    onValueChange = { contributionText = it },
                    label = { Text("Per Installment (PKR) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = myPayoutInstallmentText,
                    onValueChange = { myPayoutInstallmentText = it },
                    label = { Text("My Payout Turn (No.) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, ClayBorderHairline, RoundedCornerShape(12.dp))
                        .clickable {
                            val cur = LocalDate.ofEpochDay(startDateEpochDay)
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    startDateEpochDay = LocalDate.of(y, m + 1, d).toEpochDay()
                                },
                                cur.year,
                                cur.monthValue - 1,
                                cur.dayOfMonth
                            ).show()
                        }
                        .padding(14.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        Text("Start Date", style = MaterialTheme.typography.labelSmall, color = ClayTextSecondary)
                        Text(
                            text = LocalDate.ofEpochDay(startDateEpochDay).format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = ClayTextPrimary
                        )
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onSave(
                        name,
                        members,
                        contributionPaisa,
                        cadence,
                        startDateEpochDay,
                        members,
                        myInstallment,
                        contributionPaisa * members,
                        note
                    )
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ClayInkDeep)
            ) {
                Text("Create Committee (کمیٹی محفوظ کریں)")
            }
        }
    }
}

@Composable
fun RecordContributionDialog(
    contribution: KametiContributionEntity,
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (accountId: Long, amountPaisa: Long, dateEpochDay: Long, note: String) -> Unit
) {
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 0L) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 500.dp),
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Record Contribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = ClayTextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Pay installment ${contribution.installmentIndex} for ${PkrFormatter.format(contribution.expectedPaisa)} from your cash or bank account.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClayTextPrimary
                )
                if (accounts.isNotEmpty()) {
                    Text("Select Account:", style = MaterialTheme.typography.labelSmall, color = ClayTextSecondary)
                    accounts.forEach { acc ->
                        val isSelected = acc.id == selectedAccountId
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ClayAccentLime.copy(alpha = 0.2f) else ClaySurface)
                                .border(1.dp, if (isSelected) ClayAccentLime else ClayBorderHairline, RoundedCornerShape(10.dp))
                                .clickable { selectedAccountId = acc.id }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = acc.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                                color = ClayTextPrimary
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Payment Note (Optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedAccountId, contribution.expectedPaisa, LocalDate.now().toEpochDay(), note)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ClayInkDeep, contentColor = androidx.compose.ui.graphics.Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.heightIn(min = 44.dp)
            ) {
                Text("Confirm Payment")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 44.dp)
            ) {
                Text("Cancel", color = ClayTextSecondary)
            }
        }
    )
}

@Composable
fun ClaimPayoutDialog(
    kameti: KametiEntity,
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (accountId: Long, receivedPaisa: Long, dateEpochDay: Long, note: String) -> Unit
) {
    val defaultAmount = kameti.expectedPayoutPaisa ?: (kameti.contributionPaisa * kameti.memberCount)
    var amountText by remember { mutableStateOf((defaultAmount / 100.0).toString()) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 0L) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 500.dp),
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Claim Committee Payout",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = ClayTextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Record committee payout into your account. Note: Committee payout is non-income capital repayment.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClayTextPrimary
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Received Amount (PKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (accounts.isNotEmpty()) {
                    Text("Deposit Into Account:", style = MaterialTheme.typography.labelSmall, color = ClayTextSecondary)
                    accounts.forEach { acc ->
                        val isSelected = acc.id == selectedAccountId
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ClayAccentLime.copy(alpha = 0.2f) else ClaySurface)
                                .border(1.dp, if (isSelected) ClayAccentLime else ClayBorderHairline, RoundedCornerShape(10.dp))
                                .clickable { selectedAccountId = acc.id }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = acc.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                                color = ClayTextPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = Money.parsePaisa(amountText) ?: defaultAmount
                    onConfirm(selectedAccountId, parsed, LocalDate.now().toEpochDay(), note)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ClayInkDeep, contentColor = androidx.compose.ui.graphics.Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.heightIn(min = 44.dp)
            ) {
                Text("Claim Payout")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 44.dp)
            ) {
                Text("Cancel", color = ClayTextSecondary)
            }
        }
    )
}
