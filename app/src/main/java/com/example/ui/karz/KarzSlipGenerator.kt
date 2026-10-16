package com.example.ui.karz

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.DebtDirection
import com.example.data.model.DebtEventType
import com.example.data.model.DebtWithDetails
import com.example.domain.Money
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object KarzSlipGenerator {

    enum class ReminderTone {
        POLITE_BILINGUAL,
        FORMAL_BUSINESS,
        DETAILED_BREAKDOWN
    }

    fun generateReminderText(
        debtDetails: DebtWithDetails,
        tone: ReminderTone
    ): String {
        val debt = debtDetails.debt
        val personName = debt.personName
        val outstandingPkr = Money.formatPkr(debtDetails.outstandingPaisa)
        val totalPkr = Money.formatPkr(debtDetails.totalPrincipalPaisa)
        val repaidPkr = Money.formatPkr(debtDetails.totalRepaidPaisa)
        val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val dueDateStr = debt.dueDateEpochMs?.let { dateFormatter.format(Date(it)) } ?: "Open date"
        val isGave = debt.direction == DebtDirection.I_GAVE

        return when (tone) {
            ReminderTone.POLITE_BILINGUAL -> {
                if (isGave) {
                    """
السلام علیکم $personName بھائی! امید ہے آپ خیریت سے ہوں گے۔
ایک شائستہ یاد دہانی برائے حساب:
بقایا رقم: $outstandingPkr
(کل رقم: $totalPkr • ادا شدہ: $repaidPkr)
تاریخ واپسی: $dueDateStr

براہ کرم سہولت کے مطابق مطلع فرمائیں۔ جزاک اللہ خیر!
—
Assalam-o-Alaikum $personName bhai, just a gentle reminder regarding our ledger balance of $outstandingPkr. Thank you!
                    """.trimIndent()
                } else {
                    """
السلام علیکم $personName بھائی! 
ہمارے باہمی حساب کے مطابق، مجھے آپ کے $outstandingPkr ادا کرنے ہیں۔
(کل رقم: $totalPkr • ادا شدہ: $repaidPkr)

انشاء اللہ میں جلد ادائیگی مکمل کروں گا۔ جزاک اللہ خیر!
—
Assalam-o-Alaikum $personName bhai, confirmation of outstanding payable balance of $outstandingPkr on our ledger.
                    """.trimIndent()
                }
            }

            ReminderTone.FORMAL_BUSINESS -> {
                """
*HISAAB DO BHAI — STATEMENT SUMMARY*
Client / Person: $personName
Type: ${if (isGave) "Karz Receivable (I Gave)" else "Karz Payable (I Took)"}
Due Date: $dueDateStr
----------------------------------
Total Principal: $totalPkr
Total Repaid:    $repaidPkr
NET BALANCE:     $outstandingPkr
----------------------------------
Status: ${if (debtDetails.outstandingPaisa == 0L) "FULLY SETTLED" else "PENDING"}
Date Generated: ${dateFormatter.format(Date())}
                """.trimIndent()
            }

            ReminderTone.DETAILED_BREAKDOWN -> {
                val sb = StringBuilder()
                sb.append("📋 *HISAAB DO BHAI — ITEMIZED LEDGER*\n")
                sb.append("Person: $personName\n")
                sb.append("Account Direction: ${if (isGave) "Receivable" else "Payable"}\n")
                sb.append("Due Date: $dueDateStr\n\n")
                sb.append("📜 *HISTORY & REPAYMENTS:*\n")
                sb.append("• Initial Debt (${dateFormatter.format(Date(debt.dateEpochMs))}): $totalPkr\n")

                debtDetails.events.forEach { event ->
                    val date = dateFormatter.format(Date(event.dateEpochMs))
                    val amt = Money.formatPkr(event.amountPaisa)
                    when (event.type) {
                        DebtEventType.REPAYMENT -> sb.append("• Repayment ($date): -$amt (${event.note.ifBlank { "Cash received" }})\n")
                        DebtEventType.ADDITIONAL_ADVANCE -> sb.append("• Additional Advance ($date): +$amt (${event.note.ifBlank { "Advance" }})\n")
                        DebtEventType.ADJUSTMENT -> sb.append("• Adjustment ($date): -$amt (${event.note})\n")
                        DebtEventType.ORIGINAL_ADVANCE -> sb.append("• Opening Advance ($date): +$amt (${event.note.ifBlank { "Opening" }})\n")
                        DebtEventType.WRITE_OFF -> sb.append("• Written Off ($date): -$amt (${event.note.ifBlank { "Forgiven" }})\n")
                    }
                }

                sb.append("\n----------------------------------\n")
                sb.append("*CURRENT OUTSTANDING: $outstandingPkr*\n")
                sb.append("Generated with Hisaab Do Bhai (Quiet Ledger)")
                sb.toString()
            }
        }
    }

    /**
     * Renders a high-resolution visual Clay-styled Karz slip card as a PNG Bitmap.
     */
    fun createSlipBitmap(context: Context, debtDetails: DebtWithDetails): Bitmap {
        val width = 900
        val height = 1200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val debt = debtDetails.debt
        val isGave = debt.direction == DebtDirection.I_GAVE
        val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.US)

        // Background Warm Cream
        val bgPaint = Paint().apply {
            color = 0xFFF7F5F0.toInt()
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Card Container with Clay drop shadow look
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            style = Paint.Style.FILL
            setShadowLayer(24f, 0f, 12f, 0x1A222222)
        }
        val cardRect = RectF(50f, 50f, width - 50f, height - 50f)
        canvas.drawRoundRect(cardRect, 32f, 32f, cardPaint)

        // Card Border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE0DDD5.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(cardRect, 32f, 32f, borderPaint)

        // Header Top Bar
        val topBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isGave) 0xFF6BA88A.toInt() else 0xFFE07A5F.toInt()
            style = Paint.Style.FILL
        }
        val topBarRect = RectF(50f, 50f, width - 50f, 170f)
        canvas.drawRoundRect(topBarRect, 32f, 32f, topBarPaint)
        // Square off bottom corners of top bar
        canvas.drawRect(50f, 120f, width - 50f, 170f, topBarPaint)

        // App Title in Header
        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("HISAAB DO BHAI • حساب دو بھائی", 90f, 125f, headerTextPaint)

        // Direction Badge
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isGave) 0x1A6BA88A else 0x1AE07A5F
            style = Paint.Style.FILL
        }
        val badgeRect = RectF(90f, 210f, 480f, 270f)
        canvas.drawRoundRect(badgeRect, 16f, 16f, badgePaint)

        val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isGave) 0xFF356950.toInt() else 0xFFB34A30.toInt()
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(
            if (isGave) "✓ KARZ GIVEN (RECEIVABLE)" else "⚠ KARZ TAKEN (PAYABLE)",
            110f,
            250f,
            badgeTextPaint
        )

        // Person Name
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF2A2826.toInt()
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(debt.personName, 90f, 350f, namePaint)

        // Phone or note
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF7A7670.toInt()
            textSize = 28f
        }
        val subInfo = if (debt.phoneNumber?.isNotBlank() == true) "Phone: ${debt.phoneNumber}" else "Direct Ledger Contact"
        canvas.drawText(subInfo, 90f, 395f, subPaint)

        // Divider
        val dividerPaint = Paint().apply {
            color = 0xFFEAE7E0.toInt()
            strokeWidth = 2f
        }
        canvas.drawLine(90f, 430f, width - 90f, 430f, dividerPaint)

        // Big Outstanding Balance Card
        val balanceBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFAF8F5.toInt()
            style = Paint.Style.FILL
        }
        val balanceBoxRect = RectF(90f, 470f, width - 90f, 650f)
        canvas.drawRoundRect(balanceBoxRect, 24f, 24f, balanceBoxPaint)
        canvas.drawRoundRect(balanceBoxRect, 24f, 24f, borderPaint)

        val balanceLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF7A7670.toInt()
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("CURRENT OUTSTANDING BALANCE", 130f, 525f, balanceLabelPaint)

        val balanceAmountPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isGave) 0xFF2E634A.toInt() else 0xFFC04E32.toInt()
            textSize = 66f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(Money.formatPkr(debtDetails.outstandingPaisa), 130f, 605f, balanceAmountPaint)

        // Summary Statistics Row
        val statLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF8A8680.toInt()
            textSize = 24f
        }
        val statValPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF2A2826.toInt()
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Total Principal
        canvas.drawText("Total Principal:", 90f, 720f, statLabelPaint)
        canvas.drawText(Money.formatPkr(debtDetails.totalPrincipalPaisa), 90f, 760f, statValPaint)

        // Total Repaid
        canvas.drawText("Total Repaid:", 380f, 720f, statLabelPaint)
        canvas.drawText(Money.formatPkr(debtDetails.totalRepaidPaisa), 380f, 760f, statValPaint)

        // Due Date
        canvas.drawText("Due Date:", 650f, 720f, statLabelPaint)
        val dueDateText = debt.dueDateEpochMs?.let { dateFormatter.format(Date(it)) } ?: "Open"
        canvas.drawText(dueDateText, 650f, 760f, statValPaint)

        // Repayment Progress Bar
        val progressBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFEBE8E1.toInt()
            style = Paint.Style.FILL
        }
        val progressRect = RectF(90f, 805f, width - 90f, 825f)
        canvas.drawRoundRect(progressRect, 10f, 10f, progressBgPaint)

        val progress = if (debtDetails.totalPrincipalPaisa > 0) {
            (debtDetails.totalRepaidPaisa.toFloat() / debtDetails.totalPrincipalPaisa.toFloat()).coerceIn(0f, 1f)
        } else 0f

        val progressFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isGave) 0xFF6BA88A.toInt() else 0xFFE07A5F.toInt()
            style = Paint.Style.FILL
        }
        val fillWidth = 90f + ((width - 180f) * progress)
        val fillRect = RectF(90f, 805f, fillWidth, 825f)
        canvas.drawRoundRect(fillRect, 10f, 10f, progressFillPaint)

        // Progress text
        val pctText = "${(progress * 100).toInt()}% Repaid"
        canvas.drawText(pctText, 90f, 865f, statLabelPaint)

        // Notes & Date
        if (debt.description.isNotBlank()) {
            canvas.drawText("Item / Purpose: ${debt.description}", 90f, 930f, statLabelPaint)
        }
        val genDate = "Slip issued: ${dateFormatter.format(Date())} • Hisaab Do Bhai Ledger"
        canvas.drawText(genDate, 90f, 980f, statLabelPaint)

        // Stamp Footer
        val stampPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFD8D4CA.toInt()
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }
        canvas.drawText("Verified Offline Digital Slip • 100% Private Record", 90f, 1110f, stampPaint)

        return bitmap
    }

    /**
     * Saves the rendered slip bitmap to cache and returns its FileProvider content Uri.
     */
    fun saveSlipToCache(context: Context, debtDetails: DebtWithDetails): Uri {
        val bitmap = createSlipBitmap(context, debtDetails)
        val cacheFile = File(context.cacheDir, "hisaab_slip_${debtDetails.debt.id}.png")
        FileOutputStream(cacheFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cacheFile
        )
    }

    /**
     * Generates a multi-page/A4 PDF Ledger Statement using Android's native PdfDocument.
     */
    fun generatePdfStatement(context: Context, debtDetails: DebtWithDetails): Uri {
        val debt = debtDetails.debt
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (595x842 pt)
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val dateFormatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
        val shortDate = SimpleDateFormat("dd MMM yyyy", Locale.US)

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF2A2826.toInt()
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("HISAAB DO BHAI (حساب دو بھائی)", 40f, 50f, headerPaint)

        val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF7A7670.toInt()
            textSize = 10f
        }
        canvas.drawText("OFFICIAL KARZ LEDGER STATEMENT • 100% OFFLINE SECURE", 40f, 65f, subHeaderPaint)

        val linePaint = Paint().apply {
            color = 0xFFCCCCCC.toInt()
            strokeWidth = 1f
        }
        canvas.drawLine(40f, 75f, 555f, 75f, linePaint)

        // Details Grid
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF333333.toInt()
            textSize = 11f
        }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF111111.toInt()
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        canvas.drawText("Person Name:", 40f, 100f, textPaint)
        canvas.drawText(debt.personName, 130f, 100f, boldPaint)

        canvas.drawText("Account Direction:", 320f, 100f, textPaint)
        val dirStr = if (debt.direction == DebtDirection.I_GAVE) "Karz Given (Receivable)" else "Karz Taken (Payable)"
        canvas.drawText(dirStr, 430f, 100f, boldPaint)

        canvas.drawText("Contact Phone:", 40f, 120f, textPaint)
        canvas.drawText(debt.phoneNumber?.ifBlank { "N/A" } ?: "N/A", 130f, 120f, textPaint)

        canvas.drawText("Due Date:", 320f, 120f, textPaint)
        val dueStr = debt.dueDateEpochMs?.let { shortDate.format(Date(it)) } ?: "Open date"
        canvas.drawText(dueStr, 430f, 120f, textPaint)

        canvas.drawText("Statement Date:", 40f, 140f, textPaint)
        canvas.drawText(dateFormatter.format(Date()), 130f, 140f, textPaint)

        // Summary Box
        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF7F5F0.toInt()
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(40f, 160f, 555f, 220f), 8f, 8f, boxPaint)

        val statTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF666666.toInt()
            textSize = 9f
        }
        val statValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF222222.toInt()
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val balanceValPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (debt.direction == DebtDirection.I_GAVE) 0xFF2E634A.toInt() else 0xFFC04E32.toInt()
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        canvas.drawText("TOTAL ADVANCE", 60f, 180f, statTitlePaint)
        canvas.drawText(Money.formatPkr(debtDetails.totalPrincipalPaisa), 60f, 202f, statValuePaint)

        canvas.drawText("TOTAL REPAID", 220f, 180f, statTitlePaint)
        canvas.drawText(Money.formatPkr(debtDetails.totalRepaidPaisa), 220f, 202f, statValuePaint)

        canvas.drawText("NET OUTSTANDING", 380f, 180f, statTitlePaint)
        canvas.drawText(Money.formatPkr(debtDetails.outstandingPaisa), 380f, 202f, balanceValPaint)

        // Table Header
        canvas.drawText("TRANSACTION & REPAYMENT HISTORY", 40f, 255f, boldPaint)
        val tableHeadY = 270f
        val tableHeadPaint = Paint().apply {
            color = 0xFFECEAE4.toInt()
            style = Paint.Style.FILL
        }
        canvas.drawRect(40f, tableHeadY, 555f, tableHeadY + 22f, tableHeadPaint)

        val thPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF333333.toInt()
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("Date", 50f, tableHeadY + 15f, thPaint)
        canvas.drawText("Event Type", 140f, tableHeadY + 15f, thPaint)
        canvas.drawText("Note / Description", 260f, tableHeadY + 15f, thPaint)
        canvas.drawText("Amount", 480f, tableHeadY + 15f, thPaint)

        var curY = tableHeadY + 38f
        // Row 1: Initial creation
        canvas.drawText(shortDate.format(Date(debt.dateEpochMs)), 50f, curY, textPaint)
        canvas.drawText("Initial Debt", 140f, curY, boldPaint)
        canvas.drawText(debt.description.ifBlank { "Opening ledger balance" }, 260f, curY, textPaint)
        canvas.drawText(Money.formatPkr(debt.originalPrincipalPaisa), 480f, curY, boldPaint)
        curY += 22f

        // Subsequent Events
        debtDetails.events.forEach { event ->
            if (curY < 750f) {
                canvas.drawLine(40f, curY - 14f, 555f, curY - 14f, linePaint)
                canvas.drawText(shortDate.format(Date(event.dateEpochMs)), 50f, curY, textPaint)
                val typeName = when (event.type) {
                    DebtEventType.REPAYMENT -> "Repayment"
                    DebtEventType.ADDITIONAL_ADVANCE -> "Additional Advance"
                    DebtEventType.ADJUSTMENT -> "Adjustment"
                    DebtEventType.ORIGINAL_ADVANCE -> "Opening Advance"
                    DebtEventType.WRITE_OFF -> "Written Off"
                }
                canvas.drawText(typeName, 140f, curY, textPaint)
                canvas.drawText(event.note.ifBlank { "—" }, 260f, curY, textPaint)
                val isMinus = event.type == DebtEventType.REPAYMENT || event.type == DebtEventType.ADJUSTMENT || event.type == DebtEventType.WRITE_OFF
                val amtStr = (if (isMinus) "- " else "+ ") + Money.formatPkr(event.amountPaisa)
                canvas.drawText(amtStr, 480f, curY, textPaint)
                curY += 22f
            }
        }

        // Footer note
        canvas.drawLine(40f, 770f, 555f, 770f, linePaint)
        canvas.drawText("This statement was automatically generated from Hisaab Do Bhai. Keep for your mutual financial records.", 40f, 790f, subHeaderPaint)
        canvas.drawText("Authorized Sign / تصدیق دستخط: ___________________________", 320f, 810f, subHeaderPaint)

        document.finishPage(page)

        val pdfFile = File(context.cacheDir, "hisaab_statement_${debt.id}.pdf")
        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
    }

    /**
     * Dispatches intent to WhatsApp directly or opens universal share sheet.
     */
    fun shareViaWhatsApp(context: Context, text: String, imageUri: Uri? = null) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = if (imageUri != null) "image/png" else "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            if (imageUri != null) {
                putExtra(Intent.EXTRA_STREAM, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            setPackage("com.whatsapp")
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // If regular WhatsApp not found, try WhatsApp Business or generic share chooser
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = if (imageUri != null) "image/png" else "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                if (imageUri != null) {
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
            context.startActivity(Intent.createChooser(fallbackIntent, "Share Karz Slip via"))
        }
    }

    /**
     * Shares generated PDF statement via universal chooser.
     */
    fun sharePdf(context: Context, pdfUri: Uri, personName: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            putExtra(Intent.EXTRA_SUBJECT, "Hisaab Statement - $personName")
            putExtra(Intent.EXTRA_TEXT, "Detailed financial statement for $personName generated by Hisaab Do Bhai.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share PDF Statement via"))
    }
}
