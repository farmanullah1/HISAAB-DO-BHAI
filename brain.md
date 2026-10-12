# Hisaab Do Bhai (حساب دو بھائی) — Project Architecture & Status (brain.md)

## 1. Product Summary & Philosophy
**Hisaab Do Bhai** is a production-grade, native Android personal finance and shopping planner built with Kotlin and Jetpack Compose.
It strictly implements the **Clay design system** (`clay.html`, `clay-design.md`):
- Light-mode default on a warm off-white canvas (`#FEFDFB`), flat cards (`#FFFFFF`) with fine hairline borders (`#D1CDC7`), pill controls, and rationed chartreuse-lime accents (`#EEF773` / `#CBD810`).
- 100% offline-first with zero cloud dependencies, no logins, no telemetry, and private app storage.
- Strict derived financial balances in Room/SQLite with exact integer minor units (`Long` paisa, 1 PKR = 100 paisa) — no floating point calculations.
- Mandatory user confirmation for recurring payments — never automatically deducted.

## 2. Architecture & Data Layers
- **Domain Layer (`domain/Money.kt`)**:
  - `formatPkr(paisa: Long): String` — formats exact PKR notation (e.g. `Rs. 12,345.00`).
  - `formatSigned(paisa: Long, type: TransactionType): String` — signed variants (`+Rs. 500.00`, `-Rs. 250.00`).
  - `parsePaisa(input: String): Long?` — strictly rejects negatives, >2 decimal places, and non-numeric inputs.
- **Database Layer (`data/database/`, `data/model/`)**:
  - `accounts`: ID, name, type (CASH, BANK, WALLET, SAVINGS), openingBalancePaisa, colorHex, isArchived.
  - `transactions`: ID, type (INCOME, EXPENSE, TRANSFER), amountPaisa, fromAccountId, toAccountId, categoryId, note, receiptPath, recurringOccurrenceId, dateEpochMs.
  - `categories`: ID, name, type (EXPENSE, INCOME), iconKey, colorHex, monthlyLimitPaisa (for budget tracking).
  - `needs`: ID, name, expectedPricePaisa, whereToGet, externalUrl, note, isDone, doneAtEpochMs.
  - `receipt_attachments`: ID, transactionId, localRelativePath, mimeType, fileSizeBytes.
  - `recurring_rules`: ID, title, type, amountPaisa, accountId, categoryId, frequency (DAILY, WEEKLY, MONTHLY, YEARLY), startDateEpochMs, nextDueEpochMs, isActive.
  - `recurring_occurrences`: ID, ruleId, occurrenceDateEpochMs, status (PENDING, CONFIRMED, SKIPPED, CANCELLED), transactionId.
- **Repository (`data/repository/HisaabRepository.kt`)**:
  - Pure SQL derived balance queries: `openingBalance + income - expenses + incoming transfers - outgoing transfers`.
  - First-launch seeding of default accounts (Cash, Bank, Wallet with 0 opening balance) and 14 standard categories. Zero artificial transactions.
  - Local receipt photo handling (saving bitmaps safely into `filesDir/receipts/`).
  - Recurring payment queue and verification.
  - Full versioned JSON backup export and safe validation restore.
- **Presentation Layer (`ui/`)**:
  - Theme: Clay light mode as default, coordinated dark mode.
  - `HomeScreen`: Total Balance card with Privacy Mode toggle, account breakdown, pending recurring payment banner (Confirm / Skip), 30-day summary, budget limit progress, search bar, and chronological ledger with 5-second undo snackbar.
  - `AddTransactionScreen`: Segmented type control (Expense, Income, Transfer), live numeric keypad, account chips, category chips, date picker, note field, and photo picker for receipt attachment.
  - `NeedsScreen`: Independent shopping planner, active items with expected price subtotal, URL browser links, completion toggles, and collapsed completed section.
  - `SettingsScreen`: Account & wallet manager, categories with budget limits, recurring rules manager, financial reconciliation tool, JSON export & restore.

## 3. Verification & Test Status
- `MoneyTest.kt`: Unit tests for zero, simple amounts, large amounts, signed formatting, input validation, and round-trip parse/formatting.
- `ExampleRobolectricTest.kt`: Verified app resource string matches "Hisaab Do Bhai".
- Compilation: Validated with `compile_applet`.
- Tests: Validated with `gradle :app:testDebugUnitTest`.

## 4. Key Rules Enforced
1. Financial correctness: All balances derived from transactions; no independently mutable stored balance.
2. Mandatory recurring confirmation: Recurring schedules are never recorded without the user tapping "Confirm & Record".
3. Shopping planner independence: "What I Need" estimates never alter ledger balances.
4. Privacy: 100% offline, zero network requests, local receipt storage.
