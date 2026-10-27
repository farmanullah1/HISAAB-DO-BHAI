# Up Until Now — Hisaab Do Bhai (حساب دو بھائی)
### Comprehensive Architectural, Visual, and Feature Status Report
*Generated on October 26, 2026*

---

## 1. Executive Summary & Core Philosophy

**Hisaab Do Bhai** is a modern, privacy-first, 100% offline personal finance manager and planning companion designed specifically for Pakistani cultural and financial workflows.

### Fundamental Principles
- **Quiet Ledger Philosophy:** Zero ads, zero analytics, zero external API tracking, and zero cloud storage leaks. All data is kept strictly inside the user's device in an encrypted/private SQLite database managed by Android Room.
- **Paisa Precision (Zero Floating-Point Drift):** Currency values are stored and calculated strictly as 64-bit integer `Long` representations of **Paisa** (`1 PKR = 100 Paisa`). There are zero floating-point arithmetic rounding errors.
- **Cultural First-Class Concepts:** Native workflows for **Karz** (قرض - bilateral debts/credit ledger with consideration types), **Kameti** (کمیٹی - ROSCA rotating savings), **Zakat & Charity** (زکوٰۃ و صدقات - lunar Hawl Nisab calculations based on gold/silver rates), and **Reserved Money** (محفوظ رقم - earmarked emergency funds).
- **Clay Aesthetics:** A bespoke, tactile, and restrained design system blending high-contrast readability, soft hairline borders, warm off-white canvas tones, and lime accents.

---

## 2. Visual Design & Aesthetic Status (How the App Looks Now)

### 2.1 The Clay UI Design System
The entire application has been styled under a unified, tactile design language inspired by modern Material 3 and Clay UI principles:
- **Canvas (`ClayBackground` #FEFDFB):** A warm, soft off-white surface that eliminates blinding pure whites while maintaining superior contrast.
- **Card Surfaces (`ClaySurfaceElevated` #FFFFFF / `ClaySurface` #F4F3F0):** Nested cards and segmented insets with soft depth and crisp separation.
- **Hairline Borders (`ClayBorderHairline` #D1CDC7):** Every card, container, chip, modal dialog, and input field is framed with a delicate, razor-sharp `1.dp` border.
- **Primary Ink (`ClayInkDeep` #1B1A18):** High-contrast, near-black ink for buttons, prominent headers, and active states.
- **Vibrant Accent (`ClayAccentLime` #EEF773 & `ClayAccentLimeDeep` #CBD810):** Used strategically for active filter chips, toggle highlights, and primary key accents.
- **Semantic Financial Accents:** 
  - Income: `SemanticIncome` (#2E7D32 - deep readable green)
  - Expense: `SemanticExpense` (#B0472A - warm terracotta red)
  - Warning/Attention: `SemanticWarning` (#D97706 - restrained amber)

### 2.2 Padding, Margins & Touch Ergonomics
- **8.dp Grid Alignment:** Consistent padding scales (`8.dp`, `12.dp`, `16.dp`, `20.dp`, `24.dp`, `32.dp`) throughout every screen.
- **Touch Targets:** All interactive components (buttons, filter chips, dropdown anchors, keypad cells) strictly satisfy the minimum 48dp Android Accessibility recommendation (and min 44dp inside dialogs).
- **Rounded Corner Geometry:**
  - Screen cards: `14.dp` to `20.dp`
  - Dialogs: `20.dp`
  - Input fields: `12.dp`
  - Action chips and item pills: `8.dp` to `10.dp`
  - Primary segmented selectors: Fully rounded pill shape (`9999.dp`).

### 2.3 Comprehensive Responsiveness
- **Adaptive Centering:** The root application scaffold (`HisaabApp.kt`) enforces `.widthIn(max = 840.dp)` centered horizontally (`Alignment.TopCenter`), preventing visual stretching on foldables, tablets, and desktop ChromeOS.
- **Screen-Level `BoxWithConstraints`:** Screens measure viewport width (`maxWidth >= 600.dp`). Tablet mode automatically widens horizontal padding to `24.dp`–`32.dp`, constrains forms to max `680.dp`, and scales layout grids.
- **Dialog Constraints:** All 15+ interactive dialogs are capped at `.widthIn(max = 500.dp)` with top-tier rounded corners (`20.dp`) and clean background container colors.

---

## 3. Current Feature Set (What Features the App Has Up Until Now)

### 3.1 Home / Financial Overview
- **Net Balance & Remaining Available Funds:** Displays total liquid wealth alongside "Remaining Available" cash (after subtracting active Reserved/Earmarked amounts).
- **Global Privacy Mode:** One-tap toggle in the top bar transforms all sensitive monetary figures into `₨ ••••••` for discreet usage in public spaces.
- **Multi-Account Carousel & Filter:** Displays accounts with live balances, opening balances, and type badges. Tapping any account filters the entire dashboard's transaction feed.
- **Monthly Summary Stat Cards:** Side-by-side bordered cards tracking total monthly income and expense with percentage differentials.
- **Category Budget Progress:** Real-time progress bars comparing actual monthly category expenditure against user-defined monthly limits with overbudget warning banners.
- **Pending Recurring Rules Notice:** Proactively highlights recurring commitments (rent, utilities, salaries) due for confirmation.
- **Karz Snapshot Card:** Live overview of net receivables vs. net payables with instant navigation to debt details.
- **Express Speed Dial Floating Action Button:** Expandable high-tactile FAB providing instant shortcuts to Add Expense, Add Income, Transfer Cash, and Record Karz.

### 3.2 Transaction Management & Custom Keypad
- **Three Transaction Types:** Full support for Expenses, Income, and Account-to-Account Transfers with balance verification.
- **Custom Tactile Numeric Keypad:** Built-in integer & decimal keypad with quick-add buttons (`+100`, `+500`, `+1,000`) for rapid single-handed entry.
- **Zero-Permission Receipt Attachments:** Uses Android's modern Photo Picker (`ActivityResultContracts.PickVisualMedia`) to safely attach and store local image files without requesting broad storage access.
- **Transaction Detail Modal:** View full transaction metadata, account origins, categories, timestamp, notes, receipt photo preview, and 1-tap **Void/Delete** with automatic balance reversal.

### 3.3 Karz (Debts & Credit Ledger)
- **Bilateral Tracking:** Complete tracking for both directions:
  - *I Gave (قرض دیا - Receivable / Asset)*
  - *I Took (قرض لیا - Payable / Liability)*
- **Three Consideration Types:**
  1. *Money (نقدی):* Direct cash loans linked to account balance movement.
  2. *Items on Credit (ادھار سودا):* Grocery/store credit without moving cash balances.
  3. *Non-Cash Value (غیر نقدی قدر):* Gold, livestock, equipment, or service debts.
- **Installment Splitting (اقساط):** Divides any outstanding balance into 2 to 120 scheduled parts across Weekly or Monthly cadences.
- **Repayments & Advances:** Record full or partial repayments, give/take additional advances, or execute balance write-offs with reason notes.
- **Printable / Sharable Bilingual Karz Slip:** Generates formatted Urdu/English ledger slips (`KarzSlipGenerator`) ready to share with counter-parties via WhatsApp or SMS.

### 3.4 Kameti (ROSCAs / کمیٹی)
- **Rotating Committee Management:** Track traditional Pakistani community savings groups.
- **Custom Cadences:** Monthly, Bi-weekly, or Weekly rounds.
- **Installment Tracking:** Automatically generates all installment slots, tracks who has paid, marks overdue installments, and records payout collection directly into a chosen account.

### 3.5 Zakat & Charity (زکوٰۃ و صدقات)
- **Live Nisab Calculator:** Calculate religious obligation according to Silver (612.36g / 52.5 Tola) or Gold (87.48g / 7.5 Tola) standards with custom current gram market rates.
- **Asset Aggregation:** Automatically pools net liquid cash and receivables, subtracts active liabilities, factors in additional gold/silver entries, and verifies the 1-year lunar *Hawl*.
- **Snapshot Ledger:** Save calculation snapshots with historical date and rate audit trails.
- **Charity Log:** Private, audit-proof log for disbursements across *Zakat, Sadqah, Fitrana, Khairat*, and custom causes.

### 3.6 What I Need (Khwahishat / خواہشات)
- **Mindful Shopping & Wishlist Planner:** Catalog needed purchases with expected price, shop/seller, external product link, and notes.
- **1-Tap Purchase Conversion:** When an item is bought, converting it instantly generates a pre-filled Expense transaction, marks the item fulfilled, and updates account balances.

### 3.7 Security & Settings
- **Biometric & PIN Lock:** Integrated Android KeyStore and `BiometricPrompt` security gate to lock the application on launch or return from background.
- **Account Management:** Create, edit, and color-code Bank, Cash Wallet, Savings, and Other accounts.
- **Category Customization:** Create custom Expense and Income categories with 13+ Material icons, custom color palettes, and budget limits.
- **Account Reconciliation Tool:** Compare calculated app balance against actual physical cash counts or bank statements with live discrepancy indicators and adjustment logging.
- **Offline Data Backup & Restore:**
  - Export full ledger as an encrypted/validated JSON archive.
  - Export transactions as clean CSV files for spreadsheet review.
  - Import and validate JSON backup archives with duplicate-detection fingerprints.
- **Database Self-Check & Repair:** Built-in integrity audit verifying balance invariants and orphaned records.

---

## 4. How the Code Looks (Code Architecture & Quality)

The codebase strictly adheres to **Clean Architecture** and **MVVM** principles using 100% modern Kotlin and Jetpack Compose:

```
app/src/main/java/com/example/
├── MainActivity.kt                  // Entry activity with edge-to-edge & biometric lifecycle
├── core/
│   ├── accounting/                  // Financial rules & transaction validation logic
│   ├── export/                      // CSV and JSON serializers & backup exporters
│   ├── money/                       // Money domain class, Paisa converters, and PkrFormatter
│   └── zakat/                       // Nisab threshold mathematical calculators
├── data/
│   ├── database/                    // Room Database (HisaabDatabase, Converters, HisaabDao)
│   ├── model/                       // Room Entities, Enums, and domain data wrappers
│   └── repository/                  // HisaabRepository single source of truth
├── domain/                          // Domain invariants and currency calculation models
└── ui/
    ├── HisaabApp.kt                 // Root responsive Scaffold, Navigation bar, and App Bar
    ├── charts/                      // Custom Canvas-drawn bar charts, donut charts, gauges
    ├── components/                  // 19+ reusable Clay UI dialogs and cards
    ├── karz/                        // Karz receipt generators and share dialogs
    ├── navigation/                  // Type-safe sealed screen routes (NavScreen)
    ├── screens/                     // 10 top-level responsive screen Composables
    ├── security/                    // BiometricLockScreen & SecurityManager
    ├── theme/                       // Clay Design System Color, Type, and Theme definitions
    └── viewmodel/                   // HisaabViewModel central StateFlow pipeline
```

### Architectural Highlights
- **Reactive Flow:** Database queries return Kotlin Coroutine `Flow` streams that are combined in `HisaabRepository` and surfaced as `StateFlow` via `stateIn(viewModelScope)` in `HisaabViewModel`.
- **UI State Collection:** Composables consume state exclusively using lifecycle-aware `collectAsStateWithLifecycle()`.
- **Zero Mock Data:** All screens, statistics, charts, and dialogues read from and write to the real Room database.
- **Test Coverage:** Automated Robolectric unit test suites run against the JVM without requiring physical Android emulators.

---

## 5. How the Database Looks (Room Schema & Entity Architecture)

The app's persistence layer is powered by **Room Database Version 5** (`hisaab_database.db`), featuring robust SQLite foreign key constraints, indexes on query paths, and automated migrations (`MIGRATION_4_5`).

### 5.1 Entity Overview (20 Tables)

| # | Table Name | Entity Class | Purpose |
|---|------------|--------------|---------|
| 1 | `accounts` | `AccountEntity` | Cash Wallets, Banks, Savings accounts with opening balances and color hexes. |
| 2 | `categories` | `CategoryEntity` | Expense & Income taxonomy with icon keys, color codes, and monthly limits. |
| 3 | `transactions` | `TransactionEntity` | Core financial ledger entries (amount, timestamp, account, category, notes). |
| 4 | `category_budget` | `CategoryBudgetEntity` | Monthly category spending caps indexed by `(categoryId, monthKey)`. |
| 5 | `needs` | `NeedEntity` | Wishlist items with expected cost, store source, external URL, and bought status. |
| 6 | `receipt_attachments`| `ReceiptAttachmentEntity` | Local image file paths linked to parent transaction IDs. |
| 7 | `recurring_rules` | `RecurringRuleEntity` | Scheduled automated recurring income and expense rules. |
| 8 | `recurring_occurrences`| `RecurringOccurrenceEntity`| Historical log of executed recurring rule occurrences. |
| 9 | `reserved_amounts` | `ReservedAmountEntity` | Earmarked funds deducted from visible available spend. |
| 10 | `debts` | `DebtEntity` | Karz contracts (counterparty, direction, kind, amount, due date, status). |
| 11 | `debt_events` | `DebtEventEntity` | Karz audit trail (repayments, advances, write-offs, notes). |
| 12 | `karz_installment` | `KarzInstallmentEntity` | Split installment schedule parts (due date, scheduled paisa, paid paisa). |
| 13 | `kameti` | `KametiEntity` | Committee contracts (members, installment value, cadence, expected payout). |
| 14 | `kameti_contribution`| `KametiContributionEntity` | Individual member round payment statuses and dates. |
| 15 | `kameti_payout` | `KametiPayoutEntity` | Payout schedule, received amount, and linked account deposit transaction. |
| 16 | `charity_record` | `CharityRecordEntity` | Audit log of Zakat, Sadqah, Fitrana, and Khairat disbursements. |
| 17 | `zakat_calculation`| `ZakatCalculationEntity` | Historical Hawl snapshots, gold/silver rates, and net eligible wealth calculations. |
| 18 | `audit_log` | `AuditLogEntity` | Immutable timestamped audit log of all create, update, and delete actions. |
| 19 | `user_reminder` | `UserReminderEntity` | Scheduled ledger reminders and task notifications. |
| 20 | `import_fingerprint`| `ImportFingerprintEntity` | SHA hashes of imported records to prevent duplicate backup restorations. |

---

## 6. Current Compilation & Quality Health

- **Gradle Build State:** `BUILD SUCCESSFUL` (Clean compilation via Kotlin 2.0+ & AGP).
- **Unit & Robolectric Testing:** `BUILD SUCCESSFUL` (`:app:testDebugUnitTest` executed 10 tasks, 23 up-to-date, zero failures).
- **Static Analysis:** Zero fatal lint errors, proper WindowInsets handling with `enableEdgeToEdge()`.
- **Security & Permissions:** Strictly least-privilege manifest declaration (`BIOMETRIC`, `USE_BIOMETRIC`); zero broad external storage permissions.

---

*This document summarizes the exact state and capabilities of the Hisaab Do Bhai application up to the present moment.*
