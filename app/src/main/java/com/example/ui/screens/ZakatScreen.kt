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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.money.PkrFormatter
import com.example.core.zakat.NisabCalculator
import com.example.data.model.AccountEntity
import com.example.data.model.CharityCategory
import com.example.data.model.CharityRecordEntity
import com.example.data.model.ZakatCalculationEntity
import com.example.domain.Money
import com.example.ui.theme.ClayAccentLime
import com.example.ui.theme.ClayBorderHairline
import com.example.ui.theme.ClayInkDeep
import com.example.ui.theme.ClaySurface
import com.example.ui.theme.ClayTextPrimary
import com.example.ui.theme.ClayTextSecondary
import com.example.ui.theme.SemanticExpense
import com.example.ui.theme.SemanticIncome
import com.example.ui.viewmodel.HisaabViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class ZakatTab(val label: String) {
    CALCULATOR("Zakat Calculator"),
    CHARITY_LOG("Charity & Sadqah")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZakatScreen(
    viewModel: HisaabViewModel,
    accounts: List<AccountEntity>,
    privacyMode: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ZakatTab.CALCULATOR) }
    val charityRecords by viewModel.charityRecords.collectAsStateWithLifecycle()
    val totalBalancePaisa by viewModel.totalBalancePaisa.collectAsStateWithLifecycle()
    val karzSummary by viewModel.karzSummary.collectAsStateWithLifecycle()

    var showCharityDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Zakat & Charity (زکوٰۃ و صدقات)",
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
                    if (selectedTab == ZakatTab.CHARITY_LOG) {
                        IconButton(onClick = { showCharityDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Charity", tint = ClayInkDeep)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = ClayInkDeep
            ) {
                ZakatTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = tab.label,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                ZakatTab.CALCULATOR -> {
                    ZakatCalculatorContent(
                        totalCashPaisa = totalBalancePaisa,
                        receivablesPaisa = karzSummary.totalGavePaisa,
                        payablesPaisa = karzSummary.totalTookPaisa,
                        privacyMode = privacyMode,
                        onSaveSnapshot = { calculation ->
                            viewModel.saveZakatCalculation(calculation)
                        }
                    )
                }
                ZakatTab.CHARITY_LOG -> {
                    CharityLogContent(
                        records = charityRecords,
                        privacyMode = privacyMode,
                        onAddClick = { showCharityDialog = true }
                    )
                }
            }
        }

        if (showCharityDialog) {
            AddCharityPaymentDialog(
                accounts = accounts,
                onDismiss = { showCharityDialog = false },
                onConfirm = { cat, recipient, amount, dateEpochDay, accId, trackCash, note ->
                    viewModel.recordCharityPayment(cat, recipient, amount, dateEpochDay, accId, trackCash, note)
                    showCharityDialog = false
                }
            )
        }
    }
}

@Composable
fun ZakatCalculatorContent(
    totalCashPaisa: Long,
    receivablesPaisa: Long,
    payablesPaisa: Long,
    privacyMode: Boolean,
    onSaveSnapshot: (ZakatCalculationEntity) -> Unit
) {
    var isSilverStandard by remember { mutableStateOf(true) }
    var silverRateGramInput by remember { mutableStateOf("320") } // e.g. PKR 320/gram default reference
    var goldRateGramInput by remember { mutableStateOf("26000") } // e.g. PKR 26,000/gram default reference
    var additionalGoldSilverPaisaInput by remember { mutableStateOf("") }
    var snapshotSavedMessage by remember { mutableStateOf<String?>(null) }

    val rateGramPaisa = remember(isSilverStandard, silverRateGramInput, goldRateGramInput) {
        val input = if (isSilverStandard) silverRateGramInput else goldRateGramInput
        (Money.parsePaisa(input) ?: 0L)
    }

    val weightMg = if (isSilverStandard) NisabCalculator.DEFAULT_SILVER_NISAB_MG else NisabCalculator.DEFAULT_GOLD_NISAB_MG
    val nisabThresholdPaisa = remember(weightMg, rateGramPaisa) {
        if (rateGramPaisa > 0L) NisabCalculator.nisabThresholdPaisa(weightMg, rateGramPaisa) else 0L
    }

    val additionalPaisa = Money.parsePaisa(additionalGoldSilverPaisaInput) ?: 0L
    val netEligibleWealth = (totalCashPaisa + receivablesPaisa - payablesPaisa + additionalPaisa).coerceAtLeast(0L)
    val isNisabReached = NisabCalculator.isNisabReached(netEligibleWealth, nisabThresholdPaisa)
    val zakatPayablePaisa = if (isNisabReached) NisabCalculator.zakatPayablePaisa(netEligibleWealth) else 0L

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isWideScreen = maxWidth >= 600.dp
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = if (isWideScreen) 24.dp else 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        // Standard Selector: Silver vs Gold
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, ClayBorderHairline, RoundedCornerShape(12.dp)),
                color = ClaySurface
            ) {
                Row(modifier = Modifier.padding(6.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSilverStandard) ClayInkDeep else Color.Transparent)
                            .clickable { isSilverStandard = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Silver Standard (612.36g)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSilverStandard) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSilverStandard) Color.White else ClayTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isSilverStandard) ClayInkDeep else Color.Transparent)
                            .clickable { isSilverStandard = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Gold Standard (87.48g)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (!isSilverStandard) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isSilverStandard) Color.White else ClayTextSecondary
                        )
                    }
                }
            }
        }

        // Metal rate input
        item {
            OutlinedTextField(
                value = if (isSilverStandard) silverRateGramInput else goldRateGramInput,
                onValueChange = {
                    if (isSilverStandard) silverRateGramInput = it else goldRateGramInput = it
                },
                label = { Text(if (isSilverStandard) "Silver Rate per Gram (PKR)" else "Gold Rate per Gram (PKR)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Additional Gold / Silver Ornaments
        item {
            OutlinedTextField(
                value = additionalGoldSilverPaisaInput,
                onValueChange = { additionalGoldSilverPaisaInput = it },
                label = { Text("Gold / Jewelry / Merchandise Value (Optional)") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Summary Breakdown Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, ClayBorderHairline, RoundedCornerShape(16.dp)),
                color = ClaySurface
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Wealth Breakdown",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ClayTextPrimary
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Bank & Cash Balances", style = MaterialTheme.typography.bodySmall, color = ClayTextSecondary)
                        Text(if (privacyMode) "₨ •••••" else PkrFormatter.format(totalCashPaisa), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("(+) Karz Receivables (Owed to you)", style = MaterialTheme.typography.bodySmall, color = SemanticIncome)
                        Text(if (privacyMode) "₨ •••••" else PkrFormatter.format(receivablesPaisa), style = MaterialTheme.typography.bodySmall, color = SemanticIncome)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("(-) Karz Payables (You owe others)", style = MaterialTheme.typography.bodySmall, color = SemanticExpense)
                        Text(if (privacyMode) "₨ •••••" else "- ${PkrFormatter.format(payablesPaisa)}", style = MaterialTheme.typography.bodySmall, color = SemanticExpense)
                    }

                    if (additionalPaisa > 0L) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("(+) Additional Assets", style = MaterialTheme.typography.bodySmall, color = ClayTextSecondary)
                            Text(PkrFormatter.format(additionalPaisa), style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ClayBorderHairline))
                    Spacer(modifier = Modifier.height(2.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Net Eligible Wealth", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ClayTextPrimary)
                        Text(if (privacyMode) "₨ ••••••" else PkrFormatter.format(netEligibleWealth), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ClayTextPrimary)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Nisab Threshold (${if (isSilverStandard) "Silver" else "Gold"})", style = MaterialTheme.typography.bodySmall, color = ClayTextSecondary)
                        Text(if (privacyMode) "₨ ••••••" else PkrFormatter.format(nisabThresholdPaisa), style = MaterialTheme.typography.bodySmall, color = ClayTextSecondary)
                    }
                }
            }
        }

        // Zakat Obligation Result Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, if (isNisabReached) ClayAccentLime else ClayBorderHairline, RoundedCornerShape(16.dp)),
                color = if (isNisabReached) ClayAccentLime.copy(alpha = 0.12f) else ClaySurface
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isNisabReached) "Zakat is Applicable (فرض ہے)" else "Below Nisab Threshold",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isNisabReached) ClayInkDeep else ClayTextSecondary
                    )

                    if (isNisabReached) {
                        Text(
                            text = "2.5% of net wealth over Hawl (one full lunar year).",
                            style = MaterialTheme.typography.bodySmall,
                            color = ClayTextSecondary
                        )
                        Text(
                            text = if (privacyMode) "₨ ••••••" else PkrFormatter.format(zakatPayablePaisa),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = ClayInkDeep
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                val snapshot = ZakatCalculationEntity(
                                    createdAtEpochMs = System.currentTimeMillis(),
                                    label = "Zakat Calculation (${LocalDate.now().year})",
                                    standard = if (isSilverStandard) "SILVER" else "GOLD",
                                    weightMgUsed = weightMg,
                                    ratePerGramPaisa = rateGramPaisa,
                                    rateEnteredDateEpochDay = LocalDate.now().toEpochDay(),
                                    nisabThresholdPaisa = nisabThresholdPaisa,
                                    eligibleWealthPaisa = netEligibleWealth,
                                    zakatPayablePaisa = zakatPayablePaisa,
                                    nisabReached = true,
                                    hawlConfirmedByUser = true,
                                    assumptionsJson = "{}",
                                    note = "Eligible: ${PkrFormatter.format(netEligibleWealth)}, Rate: ${rateGramPaisa/100}/g"
                                )
                                onSaveSnapshot(snapshot)
                                snapshotSavedMessage = "Snapshot saved to historical ledger ✓"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ClayInkDeep),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 44.dp)
                        ) {
                            Text("Save Calculation Snapshot")
                        }

                        if (snapshotSavedMessage != null) {
                            Text(
                                text = snapshotSavedMessage!!,
                                style = MaterialTheme.typography.labelSmall,
                                color = SemanticIncome,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = "Your net wealth has not reached the threshold (${PkrFormatter.format(nisabThresholdPaisa)}). No Zakat is mandatory at this time.",
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
fun CharityLogContent(
    records: List<CharityRecordEntity>,
    privacyMode: Boolean,
    onAddClick: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp
        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (isWideScreen) 32.dp else 24.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.widthIn(max = 520.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = null,
                        tint = ClayTextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Charity or Sadqah Recorded",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ClayTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Record and keep private audit logs of your Zakat, Sadqah, Fitrana, and Khairat disbursements.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClayTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onAddClick,
                        colors = ButtonDefaults.buttonColors(containerColor = ClayInkDeep),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Record Charity Payment (صدقہ / زکوٰۃ درج کریں)")
                    }
                }
            }
        } else {
            val totalCharity = records.sumOf { it.amountPaisa }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = if (isWideScreen) 24.dp else 16.dp,
                    vertical = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, ClayBorderHairline, RoundedCornerShape(14.dp)),
                        color = ClaySurface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total Recorded Charity", style = MaterialTheme.typography.labelSmall, color = ClayTextSecondary)
                                Text(
                                    text = if (privacyMode) "₨ ••••••" else PkrFormatter.format(totalCharity),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ClayInkDeep
                                )
                            }
                            Button(
                                onClick = onAddClick,
                                colors = ButtonDefaults.buttonColors(containerColor = ClayInkDeep),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.heightIn(min = 44.dp)
                            ) {
                                Text("Record")
                            }
                        }
                    }
                }

                items(records, key = { it.id }) { record ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, ClayBorderHairline, RoundedCornerShape(12.dp)),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = record.recipientName.ifEmpty { record.category.name },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ClayTextPrimary
                                )
                                Text(
                                    text = "${record.category.name}" + if (record.note.isNotBlank()) " · ${record.note}" else "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ClayTextSecondary
                                )
                            }
                            Text(
                                text = if (privacyMode) "₨ •••••" else PkrFormatter.format(record.amountPaisa),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = SemanticIncome
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddCharityPaymentDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        category: CharityCategory,
        recipientName: String,
        amountPaisa: Long,
        dateEpochDay: Long,
        accountId: Long?,
        recordCashMovement: Boolean,
        note: String
    ) -> Unit
) {
    var category by remember { mutableStateOf(CharityCategory.SADQAH) }
    var recipient by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id) }
    var recordCashMovement by remember { mutableStateOf(true) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 500.dp),
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Record Charity Disbursement",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ClayTextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CharityCategory.entries.take(4).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.name, fontSize = 11.sp) },
                            shape = RoundedCornerShape(10.dp),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = category == cat,
                                borderColor = if (category == cat) ClayInkDeep else ClayBorderHairline,
                                selectedBorderColor = ClayInkDeep
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (PKR) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = recipient,
                    onValueChange = { recipient = it },
                    label = { Text("Recipient or Organization (Optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (accounts.isNotEmpty()) {
                    Text("Deduct from Account:", style = MaterialTheme.typography.labelSmall, color = ClayTextSecondary)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        accounts.forEach { acc ->
                            val isSelected = acc.id == selectedAccountId
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        1.dp,
                                        if (isSelected) ClayInkDeep else ClayBorderHairline,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedAccountId = acc.id },
                                color = if (isSelected) ClaySurface else Color.Transparent
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = if (isSelected) "●" else "○",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) ClayInkDeep else ClayTextSecondary,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = acc.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ClayTextPrimary,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Notes (Optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = Money.parsePaisa(amountText) ?: 0L
                    if (amount > 0L) {
                        onConfirm(
                            category,
                            recipient,
                            amount,
                            LocalDate.now().toEpochDay(),
                            selectedAccountId,
                            recordCashMovement,
                            note
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ClayInkDeep),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.heightIn(min = 44.dp)
            ) {
                Text("Confirm & Record")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.heightIn(min = 44.dp)
            ) {
                Text("Cancel", color = ClayTextSecondary)
            }
        }
    )
}
