package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReservedAmountEntity
import com.example.domain.Money
import com.example.ui.theme.ClayAccentLime
import com.example.ui.theme.ClayBorderHairline
import com.example.ui.theme.ClayInkDeep
import com.example.ui.theme.ClaySurface
import com.example.ui.theme.ClayTextPrimary
import com.example.ui.theme.ClayTextSecondary

@Composable
fun ReservedMoneyDialog(
    reservedAmounts: List<ReservedAmountEntity>,
    onAddReserve: (title: String, amountPaisa: Long, note: String) -> Unit,
    onToggleActive: (ReservedAmountEntity) -> Unit,
    onDelete: (ReservedAmountEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reserved Money",
                    style = MaterialTheme.typography.titleLarge,
                    color = ClayTextPrimary
                )
                if (!showAddForm) {
                    TextButton(onClick = { showAddForm = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("+ Reserve", color = ClayInkDeep)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Reserved money protects funds for upcoming commitments (e.g. rent or emergency). It lowers your Remaining Balance without recording an expense until you actually spend it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClayTextSecondary
                )

                if (showAddForm) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ClaySurface)
                            .border(1.dp, ClayBorderHairline, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "New Reservation",
                                style = MaterialTheme.typography.labelMedium,
                                color = ClayTextPrimary
                            )

                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Purpose * (e.g. Utility Bills)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("reserve_title_input")
                            )

                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                                        amountText = input
                                    }
                                },
                                label = { Text("Amount (PKR) *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("reserve_amount_input")
                            )

                            OutlinedTextField(
                                value = note,
                                onValueChange = { note = it },
                                label = { Text("Note (optional)") },
                                maxLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (errorText != null) {
                                Text(text = errorText ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showAddForm = false }) {
                                    Text("Cancel", color = ClayTextSecondary)
                                }
                                Button(
                                    onClick = {
                                        if (title.isBlank()) {
                                            errorText = "Title is required"
                                            return@Button
                                        }
                                        val paisa = Money.parsePaisa(amountText)
                                        if (paisa == null || paisa <= 0L) {
                                            errorText = "Enter a valid amount"
                                            return@Button
                                        }
                                        onAddReserve(title.trim(), paisa, note.trim())
                                        title = ""
                                        amountText = ""
                                        note = ""
                                        errorText = null
                                        showAddForm = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ClayInkDeep,
                                        contentColor = androidx.compose.ui.graphics.Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Save Reserve")
                                }
                            }
                        }
                    }
                }

                if (reservedAmounts.isEmpty() && !showAddForm) {
                    Text(
                        text = "No active reserves. Your Remaining Balance equals Total Balance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClayTextSecondary
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (showAddForm) 120.dp else 220.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(reservedAmounts, key = { it.id }) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ClaySurface)
                                    .border(1.dp, ClayBorderHairline, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (item.isActive) ClayTextPrimary else ClayTextSecondary
                                    )
                                    Text(
                                        text = Money.formatPkr(item.amountPaisa),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = ClayTextPrimary
                                    )
                                    if (item.note.isNotBlank()) {
                                        Text(text = item.note, style = MaterialTheme.typography.bodySmall, color = ClayTextSecondary)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = item.isActive,
                                        onCheckedChange = { onToggleActive(item) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = ClayInkDeep,
                                            checkedTrackColor = ClayAccentLime
                                        ),
                                        modifier = Modifier.size(36.dp)
                                    )

                                    IconButton(onClick = { onDelete(item) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ClayTextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ClayInkDeep,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Done")
            }
        },
        shape = RoundedCornerShape(18.dp)
    )
}
