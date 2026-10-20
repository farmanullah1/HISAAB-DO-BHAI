# Hisaab Do Bhai (حساب دو بھائی) — Project Architecture & Status (brain.md)

## 1. Project Baseline & Environment
- **Platform:** Native Android (Kotlin, Jetpack Compose, Material 3, Room, Coroutines/Flow)
- **Base Package:** `com.example`
- **Application ID:** `com.aistudio.hisaab.pjwnxq`
- **Compile SDK:** 36, **Target SDK:** 36, **Min SDK:** 24
- **Room Version:** 2.7.0 (Current Database Version: 4)
- **Git Status:** Server execution container does not initialize a local `.git` repository (documented limitation). All file tracking is strictly validated before modification.
- **Manifest Permissions:** `USE_BIOMETRIC`, `VIBRATE`. Zero network permissions (`android.permission.INTERNET` is strictly forbidden and absent).
- **Baseline Build:** `gradle :app:testDebugUnitTest` executed on 2026-10-18: **BUILD SUCCESSFUL** (33 actionable tasks, 0 test failures).

---

## 2. Phase 0: Master Specification v4.0 Audit Table

| Section & Requirement | Status | Existing Location / File | Action Plan / Resolution |
|---|---|---|---|
| **0. Rules & Offline Model** | Implemented | `AndroidManifest.xml` | Maintain zero internet permission, 100% offline, local-only. |
| **2. Decision Register (D-01 to D-13)** | Partial | `com.example.domain.Money` | Ensure all money remains `Long` paisa, date-only uses epochDay, derived balances only. |
| **3.1 Strict Money Parser (`Money.parse`)** | Partial | `domain/Money.kt` | Implement `AmountParseResult` (Success/Failure with `AmountError`), Urdu digit normalization, max bounds. |
| **3.2 Lakh/Crore Formatter (`PkrFormatter`)** | Missing | `domain/Money.kt` uses Western `#,##0.00` | Implement `PkrFormatter.kt` with South Asian `12,34,567.50` grouping, `₨` symbol, and comprehensive unit tests. |
| **3.3 Installment Splitter (`InstallmentSplitter`)** | Missing | N/A | Implement `InstallmentSplitter.kt` with deterministic remainder distribution and anchor-based date math. |
| **3.4 Nisab Calculator (`NisabCalculator`)** | Missing | N/A | Implement integer milligram Nisab calculations with `Math.multiplyExact` overflow safeguards. |
| **3.5 Safe CSV Writer (`CsvWriter`)** | Missing | N/A | Implement `CsvWriter.kt` with UTF-8 BOM, formula-injection sanitization (`=`, `+`, `-`, `@`), and escaping. |
| **4.1 Transaction Type Rules** | Partial | `data/model/Enums.kt` | Create `TransactionTypeRules.kt` with single source of truth for signs and reporting classes. |
| **4.2 Data Model Entities** | Partial | `data/model/` | Add `KametiEntity`, `KametiContributionEntity`, `KametiPayoutEntity`, `KarzInstallmentEntity`, `CharityRecordEntity`, `ZakatCalculationEntity`, `CategoryBudgetEntity`, `AuditLogEntity`, `UserReminderEntity`, `ImportFingerprintEntity`. |
| **4.4 Room Safe Migration (4 → 5)** | Needs Migration | `HisaabDatabase.kt` (v4) | Migrate `4 → 5` with explicit non-destructive SQL migrations, schema export, and remove `fallbackToDestructiveMigration()`. |
| **5. Accounting Invariants (I-1 to I-9)** | Partial | `data/database/HisaabDao.kt` | Derived balance queries already enforce I-1/I-2. Add unit tests for atomic operations and invariants. |
| **6. Kameti Tracker** | Missing | N/A | Implement `KametiRepository`, `AddKametiSheet`, contribution checklist with confirmation, and payout inflows. |
| **7.1-7.3 Karz: Money vs Item & Installments** | Partial | `data/model/DebtEntity.kt`, `DebtDetailScreen.kt` | Extend debt model with `kind` (MONEY vs ITEM_SERVICE) and installment schedule generator. |
| **7.4 Recurring Bills & Inexact Notifications** | Partial | `RecurringRuleEntity.kt`, `HisaabViewModel.kt` | Implement `RecurringScheduler` materialization and confirm-first payment flows. |
| **8. Financial Dues Calendar** | Missing | N/A | Implement monthly calendar grid with distinct vector shapes (▲, ▼, ■, ◆, ●) and agenda bottom sheet. |
| **9. Zakat, Nisab & Charity Ledger** | Missing | N/A | Implement 4-step Zakat wizard, immutable calculation snapshots, and Charity disbursement ledger. |
| **10. Backup, Restore & CSV Export** | Partial | `HisaabRepository.kt` (JSON only) | Add `.csv` file generator and encrypted `.hisaab` backup/restore with PBKDF2/AES-GCM verification. |
| **11. Navigation & Plans Hub** | Partial | `ui/navigation/NavScreen.kt` | Refactor primary navigation to: Home, Transactions, Karz, Plans (hub for Kameti/Calendar/Zakat/Budgets), Settings. |
| **12. Quick Presets & Bank Message Import** | Partial | `ExpressSpeedDialFAB.kt` | Retain express speed-dial; add on-device paste/share bank notification parser (`BankMessageParser`). |
| **13. Design Tokens & Biometric Vault** | Implemented | `ui/security/`, `ui/theme/` | Retain light clay theme, `BiometricPrompt`, 4-digit PIN fallback with shake physics, and `FLAG_SECURE`. Centralize tokens in `HisaabTokens.kt`. |

---

## 3. Phased Execution Roadmap

- **Phase 1: Core Primitives & Accounting Primitives [COMPLETED & VERIFIED]**
  - Implemented `core/money/Money.kt` (strict parsing, AmountParseResult, AmountError).
  - Implemented `core/money/PkrFormatter.kt` (Pakistani lakh/crore grouping `12,34,567.50`, currency styles `₨`, `Rs`, `PKR`).
  - Implemented `core/money/InstallmentSplitter.kt` (exact paisa allocation, anchor-based monthly/weekly dates).
  - Implemented `core/zakat/NisabCalculator.kt` (integer milligram calculations, 2.5% half-up, `Math.multiplyExact` overflow safeguards).
  - Implemented `core/export/CsvWriter.kt` (formula injection escaping, UTF-8 BOM, plain decimal amounts).
  - Implemented `core/accounting/TransactionTypeRules.kt` (single source of truth for reporting classes and signs).
  - Added comprehensive test suites: `MoneyCoreTest`, `PkrFormatterTest`, `InstallmentSplitterTest`, `NisabCalculatorTest`, `CsvWriterTest`.
  - Gate verification: `gradle :app:testDebugUnitTest` passed cleanly (33 actionable tasks executed/cached, 0 failures).

- **Phase 2: Database Schema & Migration (4 → 5) [COMPLETED & VERIFIED]**
  - Added new entities: `KametiEntity`, `KametiContributionEntity`, `KametiPayoutEntity`, `KarzInstallmentEntity`, `CharityRecordEntity`, `ZakatCalculationEntity`, `CategoryBudgetEntity`, `AuditLogEntity`, `UserReminderEntity`, `ImportFingerprintEntity`.
  - Updated `DebtEntity` with `kind` (`DebtKind.MONEY` vs `DebtKind.ITEM_SERVICE`), `itemDescription`, `itemQuantity`, `itemUnit`, `estimatedValuePaisa`.
  - Updated `TransactionEntity` with `sourceType`, `sourceId`, `transferId`, `reversalOfId`, `voidedAtEpochMs`, `updatedAtEpochMs`.
  - Added TypeConverters for all new enums in `Converters.kt`.
  - Extended `HisaabDao.kt` with full flow and suspend queries for all new entities.
  - Implemented non-destructive `MIGRATION_4_5` in `HisaabDatabase.kt` and removed deprecated destructive migration fallback.
  - Wrote and passed `DatabaseMigrationTest`: Verified 4 → 5 migration preserves all existing rows, backfills new columns with correct defaults, and successfully initializes all new tables.
  - Gate verification: `gradle :app:testDebugUnitTest` passed cleanly.

- **Phase 3: Karz Money vs Item & Installments [COMPLETED & VERIFIED]**
  - Extended Karz domain and repository with `splitDebtIntoInstallments`, `recordKarzInstallmentPayment`, and `rescheduleInstallment`.
  - Added Karz Invariant tests in `KarzMoneyVsItemTest` proving Invariant I-4 (Item/Service debts never alter bank/cash balances) and Invariant I-5 (Money disbursements and repayments affect accounts atomically).
  - Verified gate: Invariant tests passed without error.

- **Phase 4: Kameti (Committee) Tracker [COMPLETED & VERIFIED]**
  - Added full committee repository methods in `HisaabRepository`: `createKameti`, `recordKametiContribution`, `claimKametiPayout`, `archiveKameti`.
  - Implemented `KametiScreen.kt` with tactile Clay overview cards, progress bars, visible payment buttons with account confirmations, and claim payout flows.
  - Implemented `AddKametiSheet` with member count validation, weekly/monthly cadence, and payout installment index.
  - Verified gate: `KametiAndZakatTest` unit tests passed cleanly (contribution schedules, payments, and payouts verified).

- **Phase 5: Financial Dues Calendar & Recurring Materializer [IN PROGRESS / NEXT]**
  - Calendar agenda integration and due reminders query aggregation across debts, kameti, and recurring bills.

- **Phase 6: Zakat, Nisab & Charity Ledger [COMPLETED & VERIFIED]**
  - Built `ZakatScreen.kt` featuring Silver standard (612.36g) and Gold standard (87.48g) Nisab calculations via `NisabCalculator`.
  - Implemented dynamic wealth breakdown (Cash + Bank balances + Karz Receivables - Karz Payables + Additional assets).
  - Added immutable calculation snapshot generator and private Charity & Sadqah payment recording with category chips (ZAKAT, SADQAH, FITRANA, KHAIRAT).
  - Verified gate: `KametiAndZakatTest` verified snapshot creation and charity disbursement logs.

- **Phase 7: CSV Statement Export & Backup Enhancements [READY]**
  - `CsvWriter` primitive ready for transactions, Karz, and Kameti statements.

- **Phase 8: Plans Hub & Navigation [COMPLETED & VERIFIED]**
  - Updated primary navigation in `NavScreen.kt` to include `Plans` hub connecting Kameti, Zakat, and Needs.
  - Built `PlansHubScreen.kt` providing access to savings committees, religious tithes, purchase goals, and recurring rules.
  - Wired routes in `HisaabApp.kt` and verified full test suite.
  - Gate verification: `gradle :app:testDebugUnitTest` executed on 2026-10-19: **BUILD SUCCESSFUL** (33 actionable tasks, 0 failures).
