# Hisaab Do Bhai (حساب دو بھائی) — Master Roadmap & Next-Gen Implementation Spec

**Document Version:** 2.0 (Unified Cash Wallet, Karz, Biometric Vault & Next-Gen Expansion)  
**Target Platform:** Native Android (Kotlin, Jetpack Compose, Material 3, Room SQLite)  
**Product Philosophy:** 100% Offline-First, Zero Cloud Dependencies, Integer Minor Units (Paisa), Tactile Clay Design Language.

---

## 1. Project Review & Architectural Baseline

### 1.1 Existing Core Capabilities
1. **Unified Cash Wallet Model:**
   - Single combined default cash account with exact integer paisa arithmetic (`Long`), eliminating floating-point rounding errors.
   - Multi-account flexibility (Bank accounts, Savings, Custom Wallets) with transactional transfers.
2. **Comprehensive Karz (Debt & Loan) Ledger:**
   - **I Gave Karz (Receivables)** and **I Took Karz (Payables)** with optional cash account synchronization.
   - Due dates, settlement adjustments, partial repayments, and full audit logs.
   - Bilingual WhatsApp reminder slips (Polite, Formal, Itemized) and vector-rendered A4 PDF statements.
3. **Private Financial Vault & Security:**
   - Android native `BiometricPrompt` with hardware fingerprint authentication.
   - In-app 4-digit Vault PIN fallback with tactile clay numeric keypad and spring shake physics.
   - `FLAG_SECURE` window protection against screenshots and recent app previews.
   - Auto-lock after 15 seconds in background.
4. **Clay Visual Analytics & Fast Logging:**
   - `ClayDonutChart` for interactive touch-to-inspect category breakdowns.
   - `ClayBarChart` for monthly cash flow comparisons.
   - `KarzLiabilityGauge` for liquidity risk visualization.
   - `ExpressSpeedDialFAB` for 1-tap rapid logging of South Asian daily presets (Chai, Fuel, Groceries, Load, Bills).
5. **Light Claymorphic Adaptive App Icon:**
   - Custom 3D sculpted clay booklet with embossed rupee symbol (`₨`) and mint biometric shield on warm cream background (`#F1EBDF`).

---

## 2. Recommended Next-Gen Improvements

### A. Design & Tactile Micro-Interactions
1. **Adaptive Two-Pane Canonical Layout for Foldables & Tablets:**
   - Implement `WindowWidthSizeClass` (Compact, Medium, Expanded).
   - On wide screens, show a master-detail split layout (Ledger list on the left pane, deep transaction/debt details on the right pane).
   - Replace bottom navigation with an ergonomic Clay Navigation Rail on large displays.
2. **Micro-Haptic & Acoustic Layer:**
   - Subtle tactile feedback profiles: gentle tick on keypad touches, firm click on biometrics, and a satisfying soft snap on saving transactions.
3. **Localized Clay Category Iconography:**
   - Authentic South Asian vector glyphs: Dhaba chai cup, Rickshaw, K-Electric/SNGPL meters, Sadqah box, Kiryana grocery sack.

### B. Cultural & Financial High-Value Features
1. **Kameti / Committee (ROSCA / Chit Fund) Tracker:**
   - Pakistan's widely used informal community savings system.
   - Track committee name, monthly installment (Kist), total members, payout month, collected pool, and status (Paid / Received).
   - Automatically synchronizes with the Cash Wallet when installments are paid or the lump sum is received.
2. **Zakat & Sadqah / Charity Calculator & Ledger:**
   - Real-time Nisab valuation (Gold 87.48g / Silver 612.36g) based on user-entered local spot rates.
   - Automatic net eligible zakat asset tally: `(Cash + Bank Balances + Receivables (Gave Karz) - Short-term Debts (Took Karz)) * 2.5%`.
   - Dedicated Sadqah / Khairat log to track charitable giving throughout Ramadan and the year.
3. **Debt Installment Plans (Kist Schedule for Karz):**
   - Ability to split large Karz into equal monthly or weekly installments (Kist).
   - Automatic reminder schedule and WhatsApp slip indicating installment number (e.g., *Installment 3 of 6*).
4. **Interactive Financial Dues Calendar:**
   - Month-view calendar displaying upcoming Karz due dates, pending recurring bills (rent, electricity, internet), and Kameti payout dates.

### C. Core Functionality & Engineering Polish
1. **Automated Scheduled Local Backups via Android Storage Access Framework (SAF):**
   - Enable users to pick a local folder (e.g., `Documents/HisaabBackups/` or SD card).
   - WorkManager periodically writes timestamped, encrypted or plain JSON snapshots without manual intervention.
2. **CSV / Excel Statement Export:**
   - Export filtered transactions, monthly summaries, and Karz ledgers to standard `.csv` files readable by Microsoft Excel, Google Sheets, or LibreOffice.
3. **Local SMS Expense Parser (Zero-Network On-Device):**
   - Optional on-device scanner for Pakistani bank & fintech notification SMS (JazzCash, EasyPaisa, Meezan Bank, HBL, Sadapay, Nayapay).
   - Extracts amount, merchant, and timestamp into a 1-tap "Confirm Expense" queue with zero network permissions.
4. **Quick Settings Tile & Android App Shortcuts:**
   - App Shortcuts on launcher long-press: "Quick Expense", "Add Karz", "Lock Vault".
   - System Quick Settings Tile for instant expense logging from anywhere.

---

## 3. Detailed Technical Architecture & Database Schema Additions

### 3.1 New Room Database Entities

#### `KametiEntity` (Committee Savings System)
```kotlin
@Entity(tableName = "kametis")
data class KametiEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val totalAmountPaisa: Long,
    val monthlyPaymentPaisa: Long,
    val totalMonths: Int,
    val startMonthEpochMs: Long,
    val myPayoutMonthIndex: Int, // 1 to totalMonths
    val payoutReceived: Boolean = false,
    val payoutDateEpochMs: Long? = null,
    val note: String = "",
    val isArchived: Boolean = false
)
```

#### `KametiPaymentEntity` (Monthly Installment Records)
```kotlin
@Entity(
    tableName = "kameti_payments",
    foreignKeys = [
        ForeignKey(
            entity = KametiEntity::class,
            parentColumns = ["id"],
            childColumns = ["kametiId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("kametiId")]
)
data class KametiPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kametiId: Long,
    val monthIndex: Int,
    val amountPaidPaisa: Long,
    val paymentDateEpochMs: Long,
    val linkedTransactionId: Long? = null,
    val note: String = ""
)
```

#### `KarzInstallmentEntity` (Kist Breakdown for Debts)
```kotlin
@Entity(
    tableName = "karz_installments",
    foreignKeys = [
        ForeignKey(
            entity = DebtEntity::class,
            parentColumns = ["id"],
            childColumns = ["debtId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("debtId")]
)
data class KarzInstallmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val debtId: Long,
    val installmentNumber: Int,
    val totalInstallments: Int,
    val dueAmountPaisa: Long,
    val dueDateEpochMs: Long,
    val isPaid: Boolean = false,
    val paidDateEpochMs: Long? = null,
    val linkedRepaymentId: Long? = null
)
```

#### `ZakatRecordEntity` (Zakat & Charity Ledger)
```kotlin
@Entity(tableName = "zakat_records")
data class ZakatRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val calculationYear: Int,
    val calculatedDateEpochMs: Long,
    val totalEligibleWealthPaisa: Long,
    val goldRatePerGramPaisa: Long,
    val silverRatePerGramPaisa: Long,
    val nisabStandardUsed: String, // "SILVER" or "GOLD"
    val nisabThresholdPaisa: Long,
    val isNisabReached: Boolean,
    val zakatPayablePaisa: Long,
    val zakatPaidSoFarPaisa: Long = 0L,
    val note: String = ""
)
```

---

## 4. UI/UX Specifications

### 4.1 Kameti (Committee) Management Screen
- **Visual Overview:**
  - Card displaying active committees, upcoming monthly payment, total savings pool, and payout countdown badge (e.g. *"Payout in Month 4 (July 2026)"*).
  - Monthly payment tracker with interactive tick checkboxes.
  - One-tap "Record Payment from Cash Wallet" which automatically creates a ledger transaction.
- **Payout Celebration:**
  - When payout month arrives, prominent "Claim Lump Sum" button which credits Cash Wallet and logs income with a single tap.

### 4.2 Zakat & Sadqah Hub Screen
- **Real-Time Wealth Audit:**
  - Auto-aggregates liquid balances: Cash Wallet + Bank Accounts + Receivables (`I Gave Karz`) minus immediate liabilities (`I Took Karz`).
  - Allows editing gold/silver spot rates or customizing excluded assets.
  - Computes exact 2.5% zakat obligation in integer paisa.
- **Zakat & Sadqah Payment Ledger:**
  - Separate tab to log individual charity disbursements with recipient name/institution and category (Zakat, Sadqah, Fitrana, Khairat).

### 4.3 Financial Dues Calendar Screen
- **Monthly Grid View:**
  - Day cells marked with indicator dots:
    - 🔴 Red dot: Karz payable due date.
    - 🟢 Green dot: Karz receivable expected date.
    - 🔵 Blue dot: Recurring bill payment (rent/utility).
    - 🟡 Yellow dot: Kameti installment due.
  - Tapping a day shows the day's itemized agenda with instant action buttons (Pay, Remind via WhatsApp, Skip).

### 4.4 Adaptive Layout Guidelines
- **Compact Screens (<600dp):** Standard bottom navigation bar with 5 items: Home, Karz, Kameti, Needs, Settings.
- **Expanded Screens (≥600dp):** Left Navigation Rail, two-pane list/detail layout with permanent side inspector.

---

## 5. Security & Offline Guarantee
1. **Zero Network Permissions in Manifest:**
   - The app must NOT declare `android.permission.INTERNET`.
   - All PDF creation, backup serialization, biometric checks, and calculations remain 100% on-device.
2. **File Provider Integrity:**
   - PDF statements and WhatsApp reminder images are saved strictly in app cache (`context.cacheDir`) with temporary read grants.
3. **Database Migration Safety:**
   - Database version bump from 3 to 4 with automated SQLite migrations preserving all existing accounts, transactions, debts, needs, and settings.

---

## 6. Implementation Phases

| Phase | Milestone | Deliverables |
|---|---|---|
| **Phase 1** | **Database & Repository Expansion** | Implement `KametiEntity`, `KametiPaymentEntity`, `KarzInstallmentEntity`, and `ZakatRecordEntity` with Room Migration 3 -> 4. |
| **Phase 2** | **Kameti & Installment Ledger** | Build `KametiScreen`, `AddKametiDialog`, and installment scheduler with Cash Wallet integration. |
| **Phase 3** | **Zakat & Sadqah Calculator Hub** | Build `ZakatScreen`, Nisab threshold logic, and charity disbursement ledger. |
| **Phase 4** | **Financial Dues Calendar** | Build monthly calendar view for upcoming debts, recurring bills, and committee dues. |
| **Phase 5** | **Export & Adaptive UI Polish** | Add CSV statement generator, Android App Shortcuts, and responsive two-pane layout for tablets. |

---

*This document serves as the single source of truth for the ongoing development and expansion of Hisaab Do Bhai.*
