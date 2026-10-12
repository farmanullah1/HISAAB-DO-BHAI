# Hisaab Do Bhai (حساب دو بھائی)

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="Hisaab Do Bhai Logo" width="120" height="120" />
</p>

<p align="center">
  <strong>Production-Grade, 100% Offline-First Native Android Personal Finance & Needs Planner</strong><br>
  Built with Kotlin, Jetpack Compose (Material 3), Room SQLite, and the Clay Design System.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Platform: Android" />
  <img src="https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Design%20System-Clay%20(Warm%20Ivory%20%2B%20Lime)-EEF773?style=flat-square&labelColor=171714&color=EEF773" alt="Clay Design" />
  <img src="https://img.shields.io/badge/Currency-PKR%20(Exact%20Paisa)-2E7D32?style=flat-square" alt="PKR Paisa" />
  <img src="https://img.shields.io/badge/Privacy-100%25%20Offline-000000?style=flat-square" alt="Offline-First" />
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue?style=flat-square" alt="License" />
</p>

---

## 🌟 Overview

**Hisaab Do Bhai** (حساب دو بھائی) is an everyday personal finance companion built to be dependable, straightforward, and strictly honest about money. It handles real personal finances: income, daily expenses, inter-account transfers, reserved funds, monthly budgets, and planned purchases.

### Core Philosophy
1. **Zero Artificial Data**: A brand new installation starts with exact zero balances (`Rs. 0.00`) and clear onboarding prompts. We never seed dummy transactions or fake metrics.
2. **Unified Cash Wallet**: Daily physical cash and immediate digital balances are managed under a single, unified **Cash Wallet** account. No confusing fragmented cash vs. wallet accounts.
3. **Exact Integer Arithmetic**: Every monetary calculation is performed in integer minor units (`Long` paisa, where 1 PKR = 100 paisa). Floating-point inaccuracies (`Double`/`Float`) are strictly prohibited in the financial domain.
4. **Mandatory User Confirmation for Recurring Events**: Scheduled salaries, bills, and subscriptions are treated as reminders. Money is **never** deducted or added automatically without your explicit tap to record.
5. **100% Offline & Private**: Zero network permissions, no external cloud syncing, no background trackers, and private local storage for database and receipt snapshots.

---

## 🎨 Clay Design Language

Hisaab Do Bhai features a tactile, warm aesthetic rooted in the **Clay Design System**:

| Design Token | Value | Purpose |
|:---|:---|:---|
| **Canvas Background** | `#FEFDFB` | Warm off-white, easy on the eyes in all lighting |
| **Surface Cards** | `#FFFFFF` | Crisp elevated surfaces with hairline borders |
| **Hairline Borders** | `#D1CDC7` | Subtle 1dp structural definition |
| **Deep Ink** | `#171714` | High-contrast, legible typography |
| **Accent Lime** | `#EEF773` | Restrained chartreuse accent for active states & highlights |
| **Lime Deep** | `#CBD810` | High-contrast variant for borders and focus outlines |
| **Income Semantic** | `#2E7D32` | Calibrated forest green for incoming funds |
| **Expense Semantic** | `#C62828` | Gentle ruby red for outgoing expenditures |

- **Subtle Corner Radii**: Softly rounded cards (16dp to 24dp) and pill badges (9999dp) matching clay pottery ergonomics.
- **Privacy Mode**: Tap the eye icon to instantly mask your active balances with `••••••` whenever you're in public.
- **Responsive Animations**: Fast, spring-based transitions for balance updates and dialog transitions.

---

## 🚀 Key Features

### 1. 💼 Unified Cash Wallet & Multi-Account System
- **Default Unified Cash Wallet**: Created automatically on first run with 0 opening balance. Edit the opening balance anytime with an auditable adjustment flow.
- **Support for Multiple Accounts**: Add Bank accounts (HBL, Meezan, Nayapay, SadaPay, etc.), Savings accounts, or Other custom accounts.
- **Atomic Transfers**: Moving money between accounts updates both balances simultaneously via atomic database transactions without changing your total net worth.

### 2. 🧮 Two-Tier Balance Architecture
- **Total Balance**: Authoritative sum of all active accounts (`Opening Balance + Income - Expenses + Transfers In - Transfers Out`).
- **Remaining Balance**: `Total Balance - Active Reserved Money`. Gives you the true amount safe to spend today without jeopardizing reserved rent, bills, or fees.
- **Reserved Money Manager**: Set aside specific amounts (e.g., *House Rent*, *Electricity Bill*, *Car Service*) with target dates and active toggles.

### 3. ⚡ Quick Transaction Capture
- **Contextual Keypad**: Dedicated on-screen keypad designed for rapid single-handed entry.
- **Instant Categorization**: Quick pill chips for 14 built-in categories (Food, Groceries, Fuel, Bills, Healthcare, etc.) or custom user categories.
- **Local Receipt Capture**: Attach receipt images directly using the Android Photo Picker (`ActivityResultContracts.PickVisualMedia`). Stored safely in app-private storage.
- **Duplicate Detection**: Warns you if an identical amount, account, and date match an existing entry within 5 minutes.
- **5-Second Undo**: Accidentally deleted a transaction? Tap "Undo" on the bottom snackbar to restore it immediately with all relations intact.

### 4. 🛒 "What I Need" (Needs & Shopping Planner)
- Separate dedicated tab to plan purchases before spending.
- Track items, estimated prices, store locations, and web URLs.
- Filter by active items or review completed purchases.
- Calculates your total planned wishlist cost without affecting your ledger balances.

### 5. 📅 Recurring Payment Reminders
- Schedule daily, weekly, monthly, or yearly recurring obligations (rent, internet bill, subscriptions, salary).
- Appears prominently on the Home dashboard when due.
- Provides two clear actions: **"Confirm & Record"** (prompts you to verify amount and account before saving to ledger) or **"Skip"**.

### 6. ⚖️ Physical Ledger Reconciliation
- Compare Hisaab's calculated balance against your actual physical cash count or bank statement.
- Calculates the exact discrepancy down to the paisa.
- Offers three choices:
  - Record an auditable balancing adjustment.
  - Correct an existing transaction.
  - Leave the discrepancy pending.

### 7. 🔒 Full Local Backup & Restore
- Export your complete financial history to a versioned JSON backup file (`hisaab_backup_<timestamp>.json`).
- Includes accounts, transactions, categories, needs, recurring rules, and reserved balances.
- Pre-restore preview inspects the backup and confirms record counts before restoring.
- Share your backup to your preferred secure local folder or storage device using Android's native share sheet.

---

## 🏛 Architecture & Code Structure

The project follows modern Android architecture (MVVM + Repository + Clean Domain):

```
app/src/main/java/com/example/
├── data/
│   ├── database/
│   │   ├── HisaabDatabase.kt       # Room database with SQLite schema & pre-population
│   │   └── HisaabDao.kt            # Room DAO with reactive Flow queries & atomic transactions
│   ├── model/
│   │   ├── AccountEntity.kt        # Accounts (CASH_WALLET, BANK, SAVINGS, OTHER)
│   │   ├── TransactionEntity.kt    # Transactions (EXPENSE, INCOME, TRANSFER)
│   │   ├── CategoryEntity.kt       # Categories with monthly budget limits
│   │   ├── NeedEntity.kt           # Shopping planner wish list items
│   │   ├── ReservedAmountEntity.kt # Reserved funds (Rent, Fees, Bills)
│   │   ├── RecurringRuleEntity.kt  # Recurring schedule definitions
│   │   ├── ReceiptAttachment.kt    # Local receipt metadata
│   │   ├── Enums.kt                # Type-safe enum definitions
│   │   └── Models.kt               # View models and aggregated UI state models
│   └── repository/
│       └── HisaabRepository.kt     # Repository with derived balance logic & JSON backup/restore
├── domain/
│   └── Money.kt                    # Pure functional Money utilities: formatPkr, parsePaisa, formatSigned
└── ui/
    ├── components/
    │   ├── BalanceCard.kt          # Total & Remaining balance card with account pills
    │   ├── NumericKeypad.kt        # Rapid-entry 12-key numeric keypad
    │   ├── AddAccountDialog.kt     # Account creation and edit modal
    │   ├── AddCategoryDialog.kt    # Category creator with budget limit setup
    │   ├── AddRecurringDialog.kt   # Recurring rule scheduler
    │   ├── ReservedMoneyDialog.kt  # Reserved funds management dialog
    │   ├── ReconciliationDialog.kt # Ledger reconciliation modal
    │   └── RestoreDataDialog.kt    # JSON backup restore validator
    ├── screens/
    │   ├── HomeScreen.kt           # Primary dashboard, balance overview, ledger, and budget tracker
    │   ├── AddTransactionScreen.kt # Full-screen transaction entry with live keypad
    │   ├── NeedsScreen.kt          # What I Need wishlist & shopping planner
    │   └── SettingsScreen.kt       # Accounts, categories, reconciliation, and backup/restore
    ├── theme/
    │   ├── Color.kt                # Clay design palette tokens
    │   ├── Type.kt                 # Material 3 typography with clean numeric weights
    │   └── Theme.kt                # Theme provider with edge-to-edge support
    ├── viewmodel/
    │   └── HisaabViewModel.kt      # Reactive state management with Kotlin Coroutines & StateFlow
    └── HisaabApp.kt                # Edge-to-edge Scaffold and Navigation Compose controller
```

---

## 🧪 Testing & Data Integrity

Financial calculations are thoroughly covered by local JVM unit tests:

| Test Class | Focus |
|:---|:---|
| **`MoneyTest.kt`** | Parsing string inputs to integer paisa, rejecting malformed decimals/negatives, exact formatting in PKR notation (`Rs. 1,234.50`), and round-trip conversion. |
| **`FinancialEngineTest.kt`** | Robolectric SQLite tests validating: fresh installs have 0 balance, expenses decrease account balances, transfers preserve total funds, recurring payments require user action, and reserved funds compute exact remaining balances. |

### Running Tests
To run all unit tests locally:
```bash
gradle :app:testDebugUnitTest
```

---

## 🛠 Building & Setup Instructions

### Prerequisites
- **Android Studio Ladybug (2024.2.1)** or newer
- **JDK 17** or **JDK 21**
- **Android SDK Platform 36** (minSdk: 26, targetSdk: 36)

### Build Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/example/hisaab-do-bhai.git
   cd hisaab-do-bhai
   ```
2. Build the debug APK:
   ```bash
   gradle :app:assembleDebug
   ```
3. Locate the generated APK at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 🔐 Security & Permissions

- **`INTERNET`**: **Not requested**. Hisaab Do Bhai makes no network connections whatsoever.
- **Storage**: Uses zero-permission Android Photo Picker (`ActivityResultContracts.PickVisualMedia`) for receipt photos. Receipt images are saved directly in app-private storage (`context.filesDir/receipts`).
- **No Analytics / Telemetry**: No tracking SDKs, no ads, no third-party telemetry.

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
