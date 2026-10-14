package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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

enum class KarzConsiderationType(val label: String, val subtitle: String) {
    MONEY("Money (نقدی)", "Cash or bank funds"),
    ITEM_ON_CREDIT("Item / Bill on Credit", "Groceries, food, or paid bill"),
    NON_CASH_VALUE("Other Value", "Agreed goods or services")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddKarzScreen(
    initialDirection: DebtDirection,
    accounts: List<AccountEntity>,
    onSaveDebt: (
        personName: String,
        direction: DebtDirection,
        amountPaisa: Long,
        dateEpochMs: Long,
        dueDateEpochMs: Long?,
        description: String,
        phoneNumber: String,
        notes: String,
        accountId: Long?,
        recordCashMovement: Boolean
    ) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }

    var direction by remember { mutableStateOf(initialDirection) }
    var considerationType by remember { mutableStateOf(KarzConsiderationType.MONEY) }
    var personName by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var dateEpochMs by remember { mutableStateOf(System.currentTimeMillis()) }
    var hasDueDate by remember { mutableStateOf(false) }
    var dueDateEpochMs by remember { mutableStateOf<Long?>(null) }
    var description by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Cash movement options
    var recordCashMovement by remember { mutableStateOf(true) }
    var selectedAccountId by remember {
        mutableStateOf(accounts.firstOrNull()?.id)
    }

    // Review dialog state
    var showReviewDialog by remember { mutableStateOf(false) }

    // Validation
    val amountPaisa = Money.parsePaisa(amountInput) ?: 0L
    val isFormValid = personName.isNotBlank() && amountPaisa > 0L

    val isGave = direction == DebtDirection.I_GAVE

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isGave) "Record Karz Given (قرض دیا)" else "Record Karz Taken (قرض لیا)",
                        style = MaterialTheme.typography.titleMedium,
                        color = ClayTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("add_karz_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ClayTextPrimary
                        )
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Direction Toggle Selector
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, ClayBorderHairline, RoundedCornerShape(14.dp)),
                color = ClaySurface,
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    // I Gave Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isGave) ClayInkDeep else Color.Transparent)
                            .clickable { direction = DebtDirection.I_GAVE }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.CallMade,
                                contentDescription = null,
                                tint = if (isGave) ClayAccentLime else ClayTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "I Gave (Lent)",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isGave) Color.White else ClayTextSecondary,
                                fontWeight = if (isGave) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    // I Took Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isGave) ClayInkDeep else Color.Transparent)
                            .clickable { direction = DebtDirection.I_TOOK }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.CallReceived,
                                contentDescription = null,
                                tint = if (!isGave) ClayAccentLime else ClayTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "I Took (Borrowed)",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (!isGave) Color.White else ClayTextSecondary,
                                fontWeight = if (!isGave) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Consideration Type Selector (Money vs Item/Food on credit vs Non-cash)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isGave) "What are you providing to this person? *" else "What did you receive from this person? *",
                    style = MaterialTheme.typography.labelMedium,
                    color = ClayTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KarzConsiderationType.values().forEach { type ->
                        val isSelected = considerationType == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) ClayInkDeep else ClayBorderHairline,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    considerationType = type
                                    if (type == KarzConsiderationType.ITEM_ON_CREDIT || type == KarzConsiderationType.NON_CASH_VALUE) {
                                        recordCashMovement = false
                                    } else {
                                        recordCashMovement = true
                                    }
                                }
                                .testTag("consideration_${type.name.lowercase()}"),
                            color = if (isSelected) ClayInkDeep else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = type.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else ClayTextPrimary,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = type.subtitle,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = if (isSelected) ClayAccentLime else ClayTextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                if (considerationType != KarzConsiderationType.MONEY) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ClayAccentLime.copy(alpha = 0.35f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        color = Color.Transparent
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = ClayInkDeep,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Non-cash transaction: Records payable/receivable liability without falsely altering your liquid cash balance.",
                                style = MaterialTheme.typography.labelSmall,
                                color = ClayInkDeep
                            )
                        }
                    }
                }
            }

            // Person Name Field
            OutlinedTextField(
                value = personName,
                onValueChange = { personName = it },
                label = { Text("Person's Name *") },
                placeholder = { Text("e.g. Ahmed, Tariq, Uncle Aslam") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("debt_person_name_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ClayInkDeep,
                    unfocusedBorderColor = ClayBorderHairline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Principal Amount Field
            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                label = { Text("Amount (PKR) *") },
                placeholder = { Text("0.00") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = {
                    Text(
                        text = "Rs.",
                        style = MaterialTheme.typography.titleMedium,
                        color = ClayTextSecondary,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("debt_amount_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ClayInkDeep,
                    unfocusedBorderColor = ClayBorderHairline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Date Pickers Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Given / Taken Date
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, ClayBorderHairline, RoundedCornerShape(12.dp))
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = dateEpochMs }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(y, m, d)
                                    }
                                    dateEpochMs = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = ClayTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isGave) "Given Date" else "Received Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = ClayTextSecondary
                            )
                            Text(
                                text = dateFormatter.format(Date(dateEpochMs)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = ClayTextPrimary
                            )
                        }
                    }
                }

                // Due Date Toggle / Picker
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, ClayBorderHairline, RoundedCornerShape(12.dp))
                        .clickable {
                            val cal = Calendar.getInstance().apply {
                                timeInMillis = dueDateEpochMs ?: (System.currentTimeMillis() + 14 * 86400000L)
                            }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(y, m, d)
                                    }
                                    dueDateEpochMs = newCal.timeInMillis
                                    hasDueDate = true
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = if (hasDueDate) ClayInkDeep else ClayTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Due Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = ClayTextSecondary
                            )
                            Text(
                                text = if (hasDueDate && dueDateEpochMs != null)
                                    dateFormatter.format(Date(dueDateEpochMs!!))
                                else "Optional (None)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (hasDueDate) ClayTextPrimary else ClayTextSecondary
                            )
                        }
                    }
                }
            }

            // Reason / Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Reason or Purpose (Optional)") },
                placeholder = { Text("e.g. Emergency medical, rent help, business loan") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("debt_description_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ClayInkDeep,
                    unfocusedBorderColor = ClayBorderHairline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Phone Number
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Contact Phone (Optional)") },
                placeholder = { Text("e.g. 0300-1234567") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("debt_phone_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ClayInkDeep,
                    unfocusedBorderColor = ClayBorderHairline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Additional Notes (Optional)") },
                placeholder = { Text("e.g. Promissory agreement, installments agreed") },
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("debt_notes_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ClayInkDeep,
                    unfocusedBorderColor = ClayBorderHairline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Accounting Behavior / Track Account Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, ClayBorderHairline, RoundedCornerShape(14.dp)),
                color = ClaySurface,
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Track Cash Movement in Account",
                                style = MaterialTheme.typography.titleSmall,
                                color = ClayTextPrimary
                            )
                            Text(
                                text = if (recordCashMovement)
                                    (if (isGave) "Deducts money from selected account as a loan asset"
                                    else "Adds borrowed money to selected account as a liability")
                                else
                                    "Historical debt: tracks balance without altering cash ledger",
                                style = MaterialTheme.typography.labelSmall,
                                color = ClayTextSecondary
                            )
                        }

                        Switch(
                            checked = recordCashMovement,
                            onCheckedChange = { recordCashMovement = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ClayInkDeep,
                                checkedTrackColor = ClayAccentLime
                            ),
                            modifier = Modifier.testTag("debt_record_cash_toggle")
                        )
                    }

                    if (recordCashMovement) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isGave) "Paid from account:" else "Deposited into account:",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClayTextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            accounts.forEach { acc ->
                                val isSelected = selectedAccountId == acc.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) ClayInkDeep else MaterialTheme.colorScheme.surface)
                                        .border(
                                            1.dp,
                                            if (isSelected) ClayInkDeep else ClayBorderHairline,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedAccountId = acc.id }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = acc.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) Color.White else ClayTextPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save / Review Button
            Button(
                onClick = { showReviewDialog = true },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("debt_review_and_save_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ClayInkDeep,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Review & Save Karz",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Confirmation Review Dialog
    if (showReviewDialog) {
        val selectedAccName = accounts.find { it.id == selectedAccountId }?.name ?: "None"
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = {
                Text(
                    text = "Confirm Karz Record",
                    style = MaterialTheme.typography.titleMedium,
                    color = ClayTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isGave) {
                            if (considerationType == KarzConsiderationType.MONEY)
                                "You are recording that you gave ${Money.formatPkr(amountPaisa)} to $personName."
                            else
                                "You are recording that you provided ${Money.formatPkr(amountPaisa)} worth of goods/credit to $personName."
                        } else {
                            if (considerationType == KarzConsiderationType.MONEY)
                                "You are recording that you borrowed ${Money.formatPkr(amountPaisa)} from $personName."
                            else
                                "You are recording that you received ${Money.formatPkr(amountPaisa)} of items/bill on credit from $personName."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClayTextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        color = ClaySurface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Accounting Summary:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ClayTextPrimary
                            )
                            if (recordCashMovement) {
                                Text(
                                    text = if (isGave)
                                        "• $selectedAccName balance will decrease by ${Money.formatPkr(amountPaisa)}.\n• Receivable asset of ${Money.formatPkr(amountPaisa)} will be created."
                                    else
                                        "• $selectedAccName balance will increase by ${Money.formatPkr(amountPaisa)}.\n• Payable liability of ${Money.formatPkr(amountPaisa)} will be created.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ClayTextSecondary
                                )
                            } else {
                                Text(
                                    text = if (considerationType != KarzConsiderationType.MONEY)
                                        "• Non-cash credit item: Cash Wallet balance will NOT change.\n• Pure liability/receivable ledger entry of ${Money.formatPkr(amountPaisa)}."
                                    else
                                        "• Historical debt: Account balances will NOT change.\n• Only the debt tracking ledger will be updated.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ClayTextSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReviewDialog = false
                        val finalDesc = description.trim().ifBlank {
                            if (considerationType == KarzConsiderationType.ITEM_ON_CREDIT) "Item/Expense on credit"
                            else if (considerationType == KarzConsiderationType.NON_CASH_VALUE) "Non-cash value on credit"
                            else ""
                        }
                        onSaveDebt(
                            personName.trim(),
                            direction,
                            amountPaisa,
                            dateEpochMs,
                            if (hasDueDate) dueDateEpochMs else null,
                            finalDesc,
                            phoneNumber.trim(),
                            notes.trim(),
                            if (recordCashMovement) selectedAccountId else null,
                            recordCashMovement
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ClayInkDeep,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("confirm_save_debt_button")
                ) {
                    Text("Confirm & Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showReviewDialog = false }
                ) {
                    Text("Cancel", color = ClayTextSecondary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
