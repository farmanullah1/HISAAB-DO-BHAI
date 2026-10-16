package com.example.ui.karz

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DebtWithDetails
import com.example.domain.Money
import com.example.ui.theme.ClayAccentLime
import com.example.ui.theme.ClayBorderHairline
import com.example.ui.theme.ClayInkDeep
import com.example.ui.theme.ClaySurface
import com.example.ui.theme.ClayTextPrimary
import com.example.ui.theme.ClayTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun KarzSlipPreviewDialog(
    debtDetails: DebtWithDetails,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTone by remember { mutableStateOf(KarzSlipGenerator.ReminderTone.POLITE_BILINGUAL) }
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var isSharingSlip by remember { mutableStateOf(false) }

    val reminderText = remember(debtDetails, selectedTone) {
        KarzSlipGenerator.generateReminderText(debtDetails, selectedTone)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, ClayBorderHairline, RoundedCornerShape(24.dp))
                .testTag("karz_slip_preview_dialog"),
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WhatsApp Karz Slip & Statement",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ClayTextPrimary
                        )
                        Text(
                            text = "شائستہ یاد دہانی برائے ${debtDetails.debt.personName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClayTextSecondary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_slip_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ClayTextSecondary)
                    }
                }

                // Reminder Tone Selector Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ClaySurface)
                        .border(1.dp, ClayBorderHairline, RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tones = listOf(
                        Triple(KarzSlipGenerator.ReminderTone.POLITE_BILINGUAL, "Polite / Urdu", "tone_polite"),
                        Triple(KarzSlipGenerator.ReminderTone.FORMAL_BUSINESS, "Formal", "tone_formal"),
                        Triple(KarzSlipGenerator.ReminderTone.DETAILED_BREAKDOWN, "Detailed", "tone_detailed")
                    )

                    tones.forEach { (tone, label, tag) ->
                        val isSelected = selectedTone == tone
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ClayInkDeep else Color.Transparent)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(),
                                    onClick = { selectedTone = tone }
                                )
                                .padding(vertical = 8.dp)
                                .testTag(tag),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ClayAccentLime else ClayTextSecondary
                            )
                        }
                    }
                }

                // Message Text Preview Box with Copy
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ClaySurface)
                        .border(1.dp, ClayBorderHairline, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MESSAGE PREVIEW",
                                style = MaterialTheme.typography.labelSmall,
                                color = ClayTextSecondary,
                                letterSpacing = 0.8.sp
                            )
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Karz Reminder", reminderText))
                                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("copy_reminder_text_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy text",
                                    tint = ClayInkDeep,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = reminderText,
                            style = MaterialTheme.typography.bodySmall,
                            color = ClayTextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Digital Slip Summary Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ClaySurface.copy(alpha = 0.5f))
                        .border(1.dp, ClayBorderHairline, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Outstanding Balance",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClayTextSecondary
                        )
                        Text(
                            text = Money.formatPkr(debtDetails.outstandingPaisa),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ClayInkDeep
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ClayAccentLime)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "PNG Slip Attached",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ClayInkDeep
                        )
                    }
                }

                // Actions Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Main WhatsApp Action Button
                    Button(
                        onClick = {
                            isSharingSlip = true
                            coroutineScope.launch {
                                try {
                                    val slipUri = withContext(Dispatchers.IO) {
                                        KarzSlipGenerator.saveSlipToCache(context, debtDetails)
                                    }
                                    KarzSlipGenerator.shareViaWhatsApp(context, reminderText, slipUri)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Sharing failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isSharingSlip = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("share_whatsapp_slip_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ClayInkDeep,
                            contentColor = Color.White
                        ),
                        enabled = !isSharingSlip && !isGeneratingPdf
                    ) {
                        if (isSharingSlip) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = ClayAccentLime,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = ClayAccentLime,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Share via WhatsApp (Slip + Text)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // PDF Statement Export Button
                    OutlinedButton(
                        onClick = {
                            isGeneratingPdf = true
                            coroutineScope.launch {
                                try {
                                    val pdfUri = withContext(Dispatchers.IO) {
                                        KarzSlipGenerator.generatePdfStatement(context, debtDetails)
                                    }
                                    KarzSlipGenerator.sharePdf(context, pdfUri, debtDetails.debt.personName)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "PDF generation failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isGeneratingPdf = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("export_pdf_statement_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ClayBorderHairline),
                        enabled = !isSharingSlip && !isGeneratingPdf
                    ) {
                        if (isGeneratingPdf) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = ClayInkDeep,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = ClayInkDeep,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Export A4 PDF Statement",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = ClayInkDeep
                            )
                        }
                    }
                }
            }
        }
    }
}
