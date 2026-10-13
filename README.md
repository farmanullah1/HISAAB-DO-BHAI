# Hisaab Do Bhai (حساب دو بھائی)

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="Hisaab Do Bhai Logo" width="130" height="130" />
</p>

<p align="center">
  <strong>Production-Grade, 100% Offline-First Native Android Personal Finance & Karz Ledger</strong><br>
  Crafted with Kotlin 2.0, Jetpack Compose (Material 3), Room SQLite, and the Tactile Clay Design System.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Platform: Android" />
  <img src="https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin 2.0" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean-FF6F00?style=for-the-badge" alt="MVVM Clean Architecture" />
  <img src="https://img.shields.io/badge/Database-Room%20SQLite-009688?style=for-the-badge&logo=sqlite&logoColor=white" alt="Room SQLite" />
  <img src="https://img.shields.io/badge/Tests-35%2F35%20Passing-success?style=for-the-badge&logo=githubactions&logoColor=white" alt="35/35 Tests Passing" />
  <img src="https://img.shields.io/badge/Design-Clay%20Ivory%20%2B%20Lime-EEF773?style=for-the-badge&labelColor=171714&color=EEF773" alt="Clay Design" />
  <img src="https://img.shields.io/badge/Security-Zero%20Network%20Permissions-darkgreen?style=for-the-badge" alt="Zero Network" />
</p>

---

## 📖 Executive Summary

**Hisaab Do Bhai** (حساب دو بھائی) is a thoughtful, uncompromising personal finance companion designed for everyday life in Pakistan. Unlike typical expense trackers that treat money as abstract floats or upload sensitive bank records to third-party clouds, Hisaab Do Bhai operates on **pure integer paisa arithmetic**, stores **100% of data locally on-device**, and enforces strict **double-entry financial integrity**.

It unifies daily physical cash and immediate digital funds into a single, cohesive **Cash Wallet**, integrates a comprehensive **Karz (قرض - Debt & Loan)** ledger, protects planned savings through a **Two-Tier Balance Architecture** (Total Balance vs. Remaining Spendable Balance), and guarantees that recurring obligations are **never charged without your explicit confirmation**.

---

## 🎨 Clay Design Language

Hisaab Do Bhai features a calm, tactile aesthetic rooted in the **Clay Design System**:

```
┌─────────────────────────────────────────────────────────────┐
│                       CLAY PALETTE                          │
├───────────────────┬───────────┬─────────────────────────────┤
│ Token             │ Hex       │ Role & Personality          │
├───────────────────┼───────────┼─────────────────────────────┤
│ ClayCanvas        │ #FEFDFB   │ Warm ivory canvas surface   │
│ ClaySurface       │ #FFFFFF   │ Elevated ceramic cards      │
│ ClayBorderHairline│ #D1CDC7   │ Fine 1dp structural divider │
│ ClayInkDeep       │ #171714   │ High-contrast typography    │
│ ClayAccentLime    │ #EEF773   │ Chartreuse focal highlights │
│ ClayLimeDeep      │ #CBD810   │ Focused button borders      │
│ SemanticIncome    │ #2E7D32   │ Calibrated forest green     │
│ SemanticExpense   │ #C62828   │ Gentle brick red            │
└───────────────────┴───────────┴─────────────────────────────┘
```

- **Gentle Curves**: Generous corner radii (`18.dp` to `24.dp`) mimicking sculpted clay.
- **Privacy Shutter**: Instant balance masking (`••••••`) with one tap on the header eye icon when opening the app in public or transit.
- **Accessible Indicators**: Amounts receivable and payable are clearly distinguished through text labels, directional arrows, and semantic badges—never color alone.
- **Haptic Feedback & Spring Physics**: Subtle, tactile spring transitions for balance updates and modal dialogs.

---

## 📱 Visual Screen Architecture

```
┌───────────────────────────────────────────────────────────────────────────┐
│                              HISAAB DO BHAI                               │
├───────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│   [ HOME ]           [ KARZ ]          [ + ADD ]    [ NEEDS ] [ SETTINGS] │
│      │                  │                  │            │         │       │
│      ├─ Balance Card    ├─ I Gave (Lent)   ├─ 12-Key    ├─ Item   ├─ Accs │
│      │  ├─ Total        ├─ I Took (Owed)   │  Keypad    │  Name   ├─ Cats │
│      │  └─ Remaining    ├─ Net Karz        ├─ Category  ├─ Target ├─ Rules│
│      ├─ Monthly Card    ├─ Due Soon & Over │  Pills     │  Price  ├─ Recon│
│      ├─ Karz Card       ├─ Filter Tabs     ├─ Account   ├─ Store  └─ JSON │
│      ├─ Budgets Card    ├─ Search by Name  │  Select    │  Link      Bkp  │
│      └─ Recent Ledger   └─ Debt Details    └─ Receipt   └─ Toggle         │
│                                               Photo        Done           │
└───────────────────────────────────────────────────────────────────────────┘
```

---

## 🚀 Key Functional Systems

### 1. 💼 Unified Cash Wallet & Multi-Account System
- **Unified Default Account**: On first run, a default `Cash Wallet` is created with `Rs. 0.00`. Physical pocket cash and instant electronic wallets are managed together to eliminate fragmented accounting.
- **Custom Accounts**: Seamlessly add Bank accounts (Meezan, HBL, SadaPay, NayaPay), Savings funds, or Other custom accounts.
- **Atomic Double-Entry Transfers**: Moving funds between accounts records both sides simultaneously within a Room database transaction, preserving overall net worth.

### 2. 🧮 Two-Tier Balance Architecture
```
┌─────────────────────────────────────────────────────────────────────────┐
│ TOTAL BALANCE = Σ (Opening Balance + Incomes - Expenses ± Transfers)   │
├─────────────────────────────────────────────────────────────────────────┤
│ REMAINING BALANCE = Total Balance - Active Reserved Funds               │
└─────────────────────────────────────────────────────────────────────────┘
```
- **Total Balance**: The definitive mathematical sum of all your cash and bank accounts.
- **Reserved Funds Manager**: Set aside funds for essential obligations (*Rent*, *Electricity Bill*, *Tuition Fees*, *Car Maintenance*) with target dates.
- **Remaining Balance**: Your safe-to-spend spendable balance. Prevents accidental overspending on daily expenses that would compromise reserved rent or bill money.

### 3. 🤝 Karz (قرض) — Comprehensive Debt & Loan Ledger
Karz is a fully integrated, bidirectional financial ledger supporting both directions of debt:
- **I Gave Karz (قرض دیا - Lent)**:
  - Records money lent to friends, relatives, or business contacts as a receivable asset.
  - Optional confirmed cash movement automatically decreases the selected cash/bank account.
  - Historical entry option allows tracking pre-existing loans without altering today's bank balance.
- **I Took Karz (قرض لیا - Borrowed)**:
  - Records money borrowed from others as a payable liability.
  - Optional confirmed cash movement automatically increases the selected account.
  - Clear accounting separation: borrowing is a liability, **not earned income**.
- **Partial Repayments & Advances**:
  - Record multiple installments over time.
  - Repayments atomically update the cash ledger and calculate the remaining balance down to the paisa.
  - Repayments are strictly excluded from ordinary monthly income and expense totals.
- **Full Activity Timeline**:
  - Every advance, repayment installment, due date modification, and write-off is preserved chronologically.
  - Settled state is reached automatically when remaining balance reaches zero.
  - Optional auditable write-off / forgiveness with mandatory recorded reason.

### 4. ⚡ Rapid Contextual Keypad & Receipts
- **12-Key Numeric Keypad**: Ergonomically tuned for one-handed thumb entry on mobile devices.
- **Live Categorization**: Quick-selection chips for 14 built-in categories (Food, Groceries, Fuel, Bills, Healthcare, etc.) or custom user categories with monthly budget limits.
- **Local Receipt Capture**: Android Photo Picker (`ActivityResultContracts.PickVisualMedia`) attaches receipt images safely stored inside private app storage (`context.filesDir/receipts`).
- **Duplicate Entry Warning**: Automatically alerts you if a transaction with the identical amount, account, and date was saved within the past 5 minutes.
- **5-Second Graceful Undo**: Accidentally deleted a transaction? Tap "Undo" on the bottom snackbar to restore it immediately with all relations intact.

### 5. 🛒 "What I Need" (Needs & Shopping Planner)
- Dedicated shopping wishlist to plan purchases before spending.
- Track expected price in PKR, shop location, web links, and notes.
- Mark items purchased or review your pending shopping budget without artificially altering your active ledger balances.

### 6. 📅 Scheduled Recurring Obligations
- Schedule daily, weekly, monthly, or yearly recurring obligations (salary, house rent, internet bill).
- When an obligation becomes due, it appears in the **Pending Confirmations** section of the Home screen.
- Money is **never** deducted or added automatically. Tap **Confirm & Record** to verify the account and amount, or **Skip** to bypass.

### 7. ⚖️ Physical Ledger Reconciliation
- Reconcile Hisaab's digital balance against your actual physical cash wallet count or bank statement.
- Calculates exact discrepancies down to the paisa.
- Choose between recording an auditable balancing adjustment, correcting an existing transaction, or leaving the discrepancy pending.

### 8. 🔒 Local Backup, Restore & Migration
- Export your entire financial universe into a standardized, human-readable JSON backup file:
  `hisaab_backup_<timestamp>.json`
- Includes all accounts, categories, transactions, needs, reserved funds, recurring rules, **and all Karz debts and events**.
- Pre-restore inspector verifies database integrity and record counts before applying changes.
- Works 100% offline via the native Android system share sheet.

---

## 📐 Accounting Integrity Matrix

Hisaab Do Bhai enforces 12 immutable financial accounting rules:

| # | Action | Account Balance Effect | Net Worth Effect | Ordinary Income/Expense Effect |
|:--|:---|:---|:---|:---|
| **1** | Record Income | Increases `toAccount` | Increases Net Worth | Counted as Monthly Income |
| **2** | Record Expense | Decreases `fromAccount` | Decreases Net Worth | Counted as Monthly Expense |
| **3** | Account Transfer | Decreases `fromAccount`, increases `toAccount` | Neutral (Unchanged) | Excluded from Income/Expense |
| **4** | Lent Money (Tracked) | Decreases `fromAccount` | Neutral (Asset converted to Receivable) | Excluded from Income/Expense |
| **5** | Lent Money (Historical) | Unchanged | Neutral (Receivable created) | Excluded from Income/Expense |
| **6** | Borrowed Money (Tracked) | Increases `toAccount` | Neutral (Cash offset by Liability) | Excluded from Income/Expense |
| **7** | Borrowed Money (Historical)| Unchanged | Neutral (Liability created) | Excluded from Income/Expense |
| **8** | Repayment Received (Lent) | Increases `toAccount` | Neutral (Receivable converted to Cash) | Excluded from Income/Expense |
| **9** | Repayment Paid (Borrowed) | Decreases `fromAccount` | Neutral (Cash used to extinguish Debt) | Excluded from Income/Expense |
| **10**| Additional Advance | Updates account if tracked | Increases Principal | Excluded from Income/Expense |
| **11**| Debt Write-Off | Unchanged | Decreases Receivable / Extinguishes Debt | Recorded with explicit audit reason |
| **12**| Reserve Money | Unchanged (Deducted from *Remaining* only)| Neutral (Earmarked within Total) | Excluded until spent |

---

## 🏛 Architecture & Code Structure

The project strictly follows modern Android Architecture (MVVM + Room Repository + Clean Domain):

```
app/src/main/java/com/example/
├── data/
│   ├── database/
│   │   ├── HisaabDatabase.kt       # Room SQLite database with safe migrations
│   │   └── HisaabDao.kt            # Reactive Flow DAO queries & atomic transactions
│   ├── model/
│   │   ├── AccountEntity.kt        # Accounts (CASH_WALLET, BANK, SAVINGS, OTHER)
│   │   ├── TransactionEntity.kt    # Transactions (EXPENSE, INCOME, TRANSFER, DEBT_*)
│   │   ├── DebtEntity.kt           # Karz debts (I_GAVE, I_TOOK, statuses)
│   │   ├── DebtEventEntity.kt      # Advances, repayments, adjustments history
│   │   ├── DebtWithDetails.kt      # Computed domain model & KarzSummary
│   │   ├── CategoryEntity.kt       # Categories with monthly budget limits
│   │   ├── NeedEntity.kt           # Shopping planner items
│   │   ├── ReservedAmountEntity.kt # Earmarked funds (Rent, Bills)
│   │   ├── RecurringRuleEntity.kt  # Recurring schedules
│   │   ├── ReceiptAttachment.kt    # Receipt metadata
│   │   ├── Enums.kt                # Type-safe enum definitions
│   │   └── Models.kt               # View state models & summary items
│   └── repository/
│       └── HisaabRepository.kt     # Repository logic, ledger queries & JSON backup/restore
├── domain/
│   └── Money.kt                    # Pure functional integer Paisa parsing & PKR formatting
└── ui/
    ├── components/
    │   ├── BalanceCard.kt          # Total & Remaining spendable balance card
    │   ├── KarzSummaryCard.kt      # Dashboard Karz receivables & payables card
    │   ├── NumericKeypad.kt        # Rapid-entry 12-key numeric keypad
    │   ├── AddAccountDialog.kt     # Account creation and edit modal
    │   ├── AddCategoryDialog.kt    # Category creator with budget limit setup
    │   ├── AddRecurringDialog.kt   # Recurring rule scheduler
    │   ├── ReservedMoneyDialog.kt  # Reserved funds management dialog
    │   ├── ReconciliationDialog.kt # Ledger reconciliation modal
    │   └── RestoreDataDialog.kt    # JSON backup restore validator
    ├── screens/
    │   ├── HomeScreen.kt           # Primary dashboard, balance overview, and ledger
    │   ├── KarzScreen.kt           # Debt overview, filter tabs, search, and debt list
    │   ├── AddKarzScreen.kt        # Karz entry form with account tracking & review
    │   ├── DebtDetailScreen.kt     # Debt detail screen with timeline & repayments
    │   ├── AddTransactionScreen.kt # Full-screen transaction entry with live keypad
    │   ├── NeedsScreen.kt          # "What I Need" wishlist & shopping planner
    │   └── SettingsScreen.kt       # Accounts, categories, reconciliation, backup/restore
    ├── theme/
    │   ├── Color.kt                # Clay design palette tokens
    │   ├── Type.kt                 # Material 3 typography scale
    │   └── Theme.kt                # Centralized Theme provider with edge-to-edge support
    ├── viewmodel/
    │   └── HisaabViewModel.kt      # Reactive state management with Kotlin Coroutines & StateFlow
    └── HisaabApp.kt                # Edge-to-edge Scaffold & Navigation Compose router
```

---

## 🧪 Comprehensive Test Suite (35 Tests Passing)

The test suite validates financial correctness, data integrity, and acceptance criteria across three specialized test classes:

| Test Class | Target Coverage | Status |
|:---|:---|:---:|
| **`MoneyTest.kt`** | Exact string-to-paisa parsing, rejection of malformed decimal inputs, edge case zeros, high-value millions, and PKR formatting (`Rs. 1,234.50`). | **8 / 8 PASS** |
| **`FinancialEngineTest.kt`** | Clean initial state validation (zero balances, zero transactions), expense ledger deduction, income increment, account transfers preserving net worth, recurring obligations requiring manual confirmation, and reserved funds computing exact remaining balance. | **9 / 9 PASS** |
| **`KarzAcceptanceTest.kt`** | Validates all 21 acceptance tests defined in `newUpdate.md`: `I Gave` lent receivables, `I Took` borrowed liabilities, historical balance preservation, multi-stage partial repayments, additional advances, exclusion from ordinary income/expense, overdue date evaluation, atomic cash balance synchronization, and full JSON backup/restore round-trip. | **16 / 16 PASS** |

### Running Unit Tests
To run all tests locally using the Gradle build tool:
```bash
gradle :app:testDebugUnitTest
```

---

## 🛠 Building & Setup Instructions

### Prerequisites
- **Android Studio Ladybug (2024.2.1)** or newer
- **JDK 17** or **JDK 21**
- **Android SDK Platform 36** (`minSdk = 26`, `targetSdk = 36`)

### Build Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/example/hisaab-do-bhai.git
   cd hisaab-do-bhai
   ```
2. Verify code and compile:
   ```bash
   gradle :app:assembleDebug
   ```
3. Locate the generated APK at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 🔐 Privacy & Security Guarantees

- **Zero Network Access**: `android.permission.INTERNET` is **never** requested. Hisaab Do Bhai is physically incapable of transmitting financial figures, contact names, notes, or receipts over the internet.
- **Zero Third-Party Trackers**: No Firebase analytics, no advertising SDKs, no external crash reporters.
- **App-Private Storage**: SQLite databases, exports, and receipt photos reside exclusively in private app-internal sandbox storage.

---

## 📄 License

```
Copyright 2026 Hisaab Do Bhai Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
