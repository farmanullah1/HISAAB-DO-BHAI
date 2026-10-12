package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.CategoryType
import com.example.data.model.TransactionType
import com.example.domain.Money
import com.example.ui.components.CategoryIconHelper
import com.example.ui.components.NumericKeypad
import com.example.ui.theme.ExpenseTerracotta
import com.example.ui.theme.IncomeGreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddTransactionScreen(
    accounts: List<AccountEntity>,
    expenseCategories: List<CategoryEntity>,
    incomeCategories: List<CategoryEntity>,
    onSaveExpense: (amountPaisa: Long, fromAccountId: Long, categoryId: Long, note: String, dateEpochMs: Long) -> Unit,
    onSaveIncome: (amountPaisa: Long, toAccountId: Long, categoryId: Long, note: String, dateEpochMs: Long) -> Unit,
    onSaveTransfer: (amountPaisa: Long, fromAccountId: Long, toAccountId: Long, note: String, dateEpochMs: Long) -> Unit,
    onTransactionSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var keypadBuffer by remember { mutableStateOf("") }
    var selectedFromAccountId by remember(accounts) {
        mutableStateOf(accounts.firstOrNull()?.id)
    }
    var selectedToAccountId by remember(accounts) {
        mutableStateOf(accounts.getOrNull(1)?.id ?: accounts.firstOrNull()?.id)
    }
    var selectedCategoryId by remember(selectedType, expenseCategories, incomeCategories) {
        val initialCat = if (selectedType == TransactionType.EXPENSE) {
            expenseCategories.firstOrNull()?.id
        } else {
            incomeCategories.firstOrNull()?.id
        }
        mutableStateOf(initialCat)
    }
    var note by remember { mutableStateOf("") }
    var selectedDateEpochMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val amountPaisa by remember {
        derivedStateOf { Money.keypadBufferToPaisa(keypadBuffer) }
    }

    val isValid by remember {
        derivedStateOf {
            if (amountPaisa <= 0L) return@derivedStateOf false
            when (selectedType) {
                TransactionType.EXPENSE -> selectedFromAccountId != null && selectedCategoryId != null
                TransactionType.INCOME -> selectedToAccountId != null && selectedCategoryId != null
                TransactionType.TRANSFER -> selectedFromAccountId != null &&
                        selectedToAccountId != null &&
                        selectedFromAccountId != selectedToAccountId
            }
        }
    }

    val dateFormatter = remember { SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.US) }

    fun showDatePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateEpochMs }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                selectedDateEpochMs = newCal.timeInMillis
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Segmented Control (Expense | Income | Transfer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TransactionType.values().forEach { type ->
                    val isSelected = selectedType == type
                    val label = when (type) {
                        TransactionType.EXPENSE -> "Expense"
                        TransactionType.INCOME -> "Income"
                        TransactionType.TRANSFER -> "Transfer"
                    }
                    val selectedBg = when (type) {
                        TransactionType.EXPENSE -> ExpenseTerracotta.copy(alpha = 0.25f)
                        TransactionType.INCOME -> IncomeGreen.copy(alpha = 0.25f)
                        TransactionType.TRANSFER -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    }
                    val selectedText = when (type) {
                        TransactionType.EXPENSE -> ExpenseTerracotta
                        TransactionType.INCOME -> IncomeGreen
                        TransactionType.TRANSFER -> MaterialTheme.colorScheme.primary
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) selectedBg else Color.Transparent)
                            .clickable {
                                selectedType = type
                                if (type == TransactionType.EXPENSE) {
                                    selectedCategoryId = expenseCategories.firstOrNull()?.id
                                } else if (type == TransactionType.INCOME) {
                                    selectedCategoryId = incomeCategories.firstOrNull()?.id
                                }
                            }
                            .padding(vertical = 10.dp)
                            .testTag("tab_${type.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) selectedText else MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Amount Display
            Text(
                text = "AMOUNT (PKR)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            val displayAmountText = if (keypadBuffer.isEmpty()) "0.00" else keypadBuffer
            Text(
                text = "Rs. $displayAmountText",
                style = MaterialTheme.typography.displayMedium,
                color = when (selectedType) {
                    TransactionType.EXPENSE -> ExpenseTerracotta
                    TransactionType.INCOME -> IncomeGreen
                    TransactionType.TRANSFER -> MaterialTheme.colorScheme.onBackground
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("live_amount_display")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Account selection
            when (selectedType) {
                TransactionType.EXPENSE -> {
                    AccountChipGroup(
                        title = "Pay From Account",
                        accounts = accounts,
                        selectedAccountId = selectedFromAccountId,
                        onAccountSelected = { selectedFromAccountId = it }
                    )
                }
                TransactionType.INCOME -> {
                    AccountChipGroup(
                        title = "Deposit To Account",
                        accounts = accounts,
                        selectedAccountId = selectedToAccountId,
                        onAccountSelected = { selectedToAccountId = it }
                    )
                }
                TransactionType.TRANSFER -> {
                    AccountChipGroup(
                        title = "From Account",
                        accounts = accounts,
                        selectedAccountId = selectedFromAccountId,
                        onAccountSelected = { selectedFromAccountId = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AccountChipGroup(
                        title = "To Account (Must be different)",
                        accounts = accounts,
                        selectedAccountId = selectedToAccountId,
                        onAccountSelected = { selectedToAccountId = it }
                    )
                }
            }

            // Category Chips (hidden for Transfer)
            if (selectedType != TransactionType.TRANSFER) {
                Spacer(modifier = Modifier.height(16.dp))
                val categories = if (selectedType == TransactionType.EXPENSE) expenseCategories else incomeCategories

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategoryId == cat.id
                            val catColor = runCatching {
                                Color(android.graphics.Color.parseColor(cat.colorHex))
                            }.getOrDefault(MaterialTheme.colorScheme.primary)

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedCategoryId = cat.id }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("category_chip_${cat.id}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = CategoryIconHelper.getIcon(cat.iconKey),
                                    contentDescription = cat.name,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else catColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date Picker Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showDatePicker() }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .testTag("date_picker_row"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Calendar icon",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = dateFormatter.format(Date(selectedDateEpochMs)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Change",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Note Text Field
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional, e.g. Lunch with team)") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_note_input")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Custom Numeric Keypad
            NumericKeypad(
                onDigit = { digit ->
                    if (keypadBuffer == "0") {
                        keypadBuffer = digit.toString()
                    } else {
                        // Check decimal limit: reject if already has '.' and 2 decimal places
                        if (keypadBuffer.contains('.')) {
                            val decimals = keypadBuffer.substringAfter('.')
                            if (decimals.length < 2) {
                                keypadBuffer += digit
                            }
                        } else {
                            if (keypadBuffer.length < 9) {
                                keypadBuffer += digit
                            }
                        }
                    }
                },
                onDot = {
                    if (!keypadBuffer.contains('.')) {
                        keypadBuffer = if (keypadBuffer.isEmpty()) "0." else "$keypadBuffer."
                    }
                },
                onBackspace = {
                    if (keypadBuffer.isNotEmpty()) {
                        keypadBuffer = keypadBuffer.dropLast(1)
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Save Button
            Button(
                onClick = {
                    if (!isValid) return@Button
                    val finalAmount = amountPaisa

                    when (selectedType) {
                        TransactionType.EXPENSE -> {
                            onSaveExpense(
                                finalAmount,
                                selectedFromAccountId!!,
                                selectedCategoryId!!,
                                note,
                                selectedDateEpochMs
                            )
                        }
                        TransactionType.INCOME -> {
                            onSaveIncome(
                                finalAmount,
                                selectedToAccountId!!,
                                selectedCategoryId!!,
                                note,
                                selectedDateEpochMs
                            )
                        }
                        TransactionType.TRANSFER -> {
                            onSaveTransfer(
                                finalAmount,
                                selectedFromAccountId!!,
                                selectedToAccountId!!,
                                note,
                                selectedDateEpochMs
                            )
                        }
                    }
                    onTransactionSaved()
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_transaction_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Transaction",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun AccountChipGroup(
    title: String,
    accounts: List<AccountEntity>,
    selectedAccountId: Long?,
    onAccountSelected: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            accounts.forEach { account ->
                val isSelected = selectedAccountId == account.id
                val accountColor = runCatching {
                    Color(android.graphics.Color.parseColor(account.colorHex))
                }.getOrDefault(MaterialTheme.colorScheme.primary)

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onAccountSelected(account.id) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("account_chip_${account.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accountColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
