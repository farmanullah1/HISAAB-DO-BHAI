package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.Money
import com.example.ui.theme.ClayAccentLime
import com.example.ui.theme.ClayBorderHairline
import com.example.ui.theme.ClayInkDeep
import com.example.ui.theme.ClaySurface
import com.example.ui.theme.ClayTextPrimary
import com.example.ui.theme.ClayTextSecondary

data class SpeedDialPreset(
    val id: String,
    val title: String,
    val urduTitle: String,
    val amountPaisa: Long,
    val categoryName: String,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
fun ExpressSpeedDialFAB(
    onQuickLog: (title: String, amountPaisa: Long, categoryName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showCustomQuickDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 135f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "fab_rotation"
    )

    val presets = remember {
        listOf(
            SpeedDialPreset(
                id = "chai",
                title = "Chai / Tea",
                urduTitle = "چائے",
                amountPaisa = 100_00L, // PKR 100
                categoryName = "Food & Dining",
                icon = Icons.Default.LocalCafe,
                accentColor = Color(0xFFD4A373)
            ),
            SpeedDialPreset(
                id = "fuel",
                title = "Petrol / Fuel",
                urduTitle = "پٹرول",
                amountPaisa = 1000_00L, // PKR 1,000
                categoryName = "Fuel & Transport",
                icon = Icons.Default.DirectionsCar,
                accentColor = Color(0xFFE07A5F)
            ),
            SpeedDialPreset(
                id = "sauda",
                title = "Daily Sauda",
                urduTitle = "سودا سلف",
                amountPaisa = 500_00L, // PKR 500
                categoryName = "Groceries",
                icon = Icons.Default.ShoppingCart,
                accentColor = Color(0xFF81B29A)
            ),
            SpeedDialPreset(
                id = "khana",
                title = "Lunch / Khana",
                urduTitle = "کھانا",
                amountPaisa = 400_00L, // PKR 400
                categoryName = "Food & Dining",
                icon = Icons.Default.Fastfood,
                accentColor = Color(0xFFF2CC8F)
            ),
            SpeedDialPreset(
                id = "load",
                title = "Mobile Load",
                urduTitle = "موبائل لوڈ",
                amountPaisa = 250_00L, // PKR 250
                categoryName = "Bills & Utilities",
                icon = Icons.Default.PhoneAndroid,
                accentColor = Color(0xFF3D5A80)
            ),
            SpeedDialPreset(
                id = "dawa",
                title = "Medicine / Dawa",
                urduTitle = "دوا",
                amountPaisa = 300_00L, // PKR 300
                categoryName = "Healthcare",
                icon = Icons.Default.MedicalServices,
                accentColor = Color(0xFFE76F51)
            )
        )
    }

    Column(
        modifier = modifier.padding(bottom = 8.dp, end = 8.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Expanded Preset Items
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + scaleIn(transformOrigin = androidx.compose.ui.graphics.TransformOrigin(1f, 1f)),
            exit = fadeOut() + scaleOut(transformOrigin = androidx.compose.ui.graphics.TransformOrigin(1f, 1f))
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ClayInkDeep)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "⚡ 1-TAP QUICK EXPENSES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ClayAccentLime,
                        fontSize = 10.sp
                    )
                }

                // Preset Pills
                presets.forEach { preset ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.5.dp, ClayBorderHairline, RoundedCornerShape(24.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(),
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onQuickLog(preset.title, preset.amountPaisa, preset.categoryName)
                                    expanded = false
                                }
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("speed_dial_${preset.id}")
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${preset.title} • ${preset.urduTitle}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ClayTextPrimary
                            )
                            Text(
                                text = Money.formatPkr(preset.amountPaisa),
                                style = MaterialTheme.typography.labelSmall,
                                color = ClayInkDeep,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(preset.accentColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = preset.icon,
                                contentDescription = null,
                                tint = preset.accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Custom Fast Amount Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(ClaySurface)
                        .border(1.5.dp, ClayBorderHairline, RoundedCornerShape(24.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(),
                            onClick = {
                                expanded = false
                                showCustomQuickDialog = true
                            }
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("speed_dial_custom")
                ) {
                    Text(
                        text = "Custom Fast Log...",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ClayInkDeep
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ClayAccentLime),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = ClayInkDeep,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Refined Tactile Clay FAB Button
        Box(
            modifier = Modifier
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(ClayInkDeep)
                .border(1.5.dp, ClayAccentLime.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = ClayAccentLime),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        expanded = !expanded
                    }
                )
                .padding(horizontal = if (expanded) 16.dp else 18.dp, vertical = 12.dp)
                .testTag("express_speed_dial_fab"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Express Speed Dial",
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotation),
                    tint = ClayAccentLime
                )

                if (!expanded) {
                    Text(
                        text = "⚡ Express",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Close",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = ClayAccentLime
                    )
                }
            }
        }
    }

    if (showCustomQuickDialog) {
        var quickAmountText by remember { mutableStateOf("") }
        var quickNote by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCustomQuickDialog = false },
            title = {
                Text(
                    text = "Fast 5-Second Expense",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ClayTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Instantly logs into Cash Wallet under Food/Miscellaneous.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClayTextSecondary
                    )

                    // Quick Increment Buttons in Custom Dialog
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(100L, 500L, 1000L).forEach { inc ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ClaySurface)
                                    .border(1.dp, ClayBorderHairline, RoundedCornerShape(10.dp))
                                    .clickable {
                                        val cur = quickAmountText.toLongOrNull() ?: 0L
                                        quickAmountText = (cur + inc).toString()
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+$inc",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ClayInkDeep
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = quickAmountText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) quickAmountText = it },
                        label = { Text("Amount (PKR)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("quick_amount_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClayInkDeep,
                            unfocusedBorderColor = ClayBorderHairline
                        )
                    )
                    OutlinedTextField(
                        value = quickNote,
                        onValueChange = { quickNote = it },
                        label = { Text("Quick Note (Optional, e.g. Bakery, Rickshaw)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("quick_note_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClayInkDeep,
                            unfocusedBorderColor = ClayBorderHairline
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountPaisa = (quickAmountText.toLongOrNull() ?: 0L) * 100L
                        if (amountPaisa > 0) {
                            val note = quickNote.ifBlank { "Quick Expense" }
                            onQuickLog(note, amountPaisa, "Food & Dining")
                            showCustomQuickDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ClayInkDeep, contentColor = Color.White),
                    modifier = Modifier.testTag("save_quick_custom_button")
                ) {
                    Text("Log Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomQuickDialog = false }) {
                    Text("Cancel", color = ClayTextSecondary)
                }
            }
        )
    }
}
