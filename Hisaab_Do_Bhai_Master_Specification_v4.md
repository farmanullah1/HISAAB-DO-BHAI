# Hisaab Do Bhai (حساب دو بھائی) — Master Specification v4.0

**Document type:** Implementation source of truth for Google AI Studio (supersedes Roadmap v2.0 and Spec v3.0)
**Date:** 2026-10-18
**Platform:** Native Android — Kotlin, Jetpack Compose, Material 3, Room, Coroutines/Flow, WorkManager (only where stated)
**Currency:** PKR, stored as integer paisa (`Long`)
**Privacy model:** Offline-first, local-only, no account, no cloud, no `INTERNET` permission
**Design:** Warm, tactile clay UI that stays readable and accessible

> **How to read this file.** Sections 0–2 are the rules. Sections 3–5 define the data and money primitives (with reference code). Sections 6–16 define features. Section 17 is the test matrix. Section 18 is the phase plan with acceptance gates. Section 19 is the prompt to paste into AI Studio.
> Code blocks are **reference implementations**. Adapt names/packages to the real repository. Never paste a block that conflicts with existing working code without reconciling it first.

---

## 0. Binding Operating Rules for the Agent

1. **Inspect before editing.** Read `settings.gradle*`, `build.gradle*`, `libs.versions.toml`, `AndroidManifest.xml`, navigation, Room `@Database` class, every entity/DAO/migration, repositories, ViewModels, `res/`, tests, and `git status`.
2. **Never guess identifiers.** Discover the real application package, `applicationId`, existing entity/DAO names, and the real Room version. In this spec `<pkg>` means the discovered base package.
3. **Audit first.** Produce an audit table in `brain.md` marking every requirement in this file as `Implemented / Partial / Missing / Conflicting / Needs decision`, citing file paths.
4. **No destructive actions.** No `fallbackToDestructiveMigration()`, no dropping tables holding user data, no overwriting uncommitted changes, no deleting the user's DB to "simplify" a migration.
5. **No fake anything.** No seeded transactions, no demo balances, no invented chart data, no fake "backup successful", no placeholder buttons. A button either works, is disabled with a reason, or is absent.
6. **No stubs / no TODO bodies.** Every function you add must be fully implemented and called from real UI or a test. If a feature cannot be completed in this environment, do not add its UI; document it in `brain.md` under *Blocked*.
7. **Small phases.** After each phase: build, run tests, inspect the diff, fix regressions, update `brain.md`, then commit (if Git is available).
8. **Honest reporting.** Only report a command as run if you ran it and saw its output. If the Android build cannot run in your environment, say so and list the exact commands for the owner.
9. **Offline guarantee.** Do not add `INTERNET`, analytics, ads, crash-upload SDKs, remote fonts, remote images, or cloud APIs. Check transitive dependencies in the merged manifest (`./gradlew :app:processDebugMainManifest` or equivalent) after dependency changes.
10. **Keep the app buildable** at the end of every phase.

**Priority order when rules conflict:**
Correct accounting → preserve user data → privacy/security → usable workflows → accessibility → visual polish → optional automation.

---

## 1. Review of Roadmap v2.0 — Defects This Version Fixes

| # | v2.0 statement | Problem | v4.0 resolution |
|---|---|---|---|
| R1 | "Database version 3 → 4" | Version number is a claim, not a fact. | Section 4.4: read the real version; migrate `N → N+1`. |
| R2 | Kameti "automatically synchronizes" with wallet; payout "logs income" | Violates confirm-first rule; payout counted as earned income inflates income reports. | Sections 6, 5.3: confirm sheet required; payout is a *committee payout* inflow, **not** income. |
| R3 | `KametiEntity` has no member count, uses `Boolean payoutReceived`, `Long` epoch for month | Cannot model late/partial/missed; month-index/epoch ambiguity. | Section 4.2: separate contribution + payout tables, status enums, `LocalDate` epoch-day storage. |
| R4 | Zakat "real-time Nisab valuation" | App is offline; nothing is real-time. | Section 9: user-entered rates with date + "User-entered rate" label; weights stored as integer milligrams. |
| R5 | Zakat formula subtracts all "Took Karz" and adds all "Gave Karz" | Religiously/legally contestable; ignores item-karz; assumes all receivables are collectible. | Section 9.2: per-component include/exclude toggles; item-karz never counted; every assumption stored with the calculation. |
| R6 | `ZakatRecordEntity` has no assumptions snapshot, no hawl note | Historical calculations silently change meaning. | Section 4.2: immutable snapshot incl. `assumptionsJson`. |
| R7 | Karz installments: `isPaid: Boolean` | Can't represent partial, missed, rescheduled. | Section 4.2/7: `paidPaisa` + status enum. |
| R8 | "Local SMS Expense Parser … scanner" | SMS permissions are sensitive, Play-policy risky, contradict "no hidden inference". | Section 12: paste/share-target import with draft + confirm. **No `READ_SMS`.** |
| R9 | Scheduled backup "encrypted or plain" with no key strategy | A background worker can't prompt for a passphrase; unclear restore path. | Section 10.3: passphrase-derived key, Keystore-wrapped for the worker, self-describing header for restore. |
| R10 | Quick Settings tile / shortcuts "from anywhere" | Could bypass the vault lock. | Section 14: every entry point goes through the lock gate. |
| R11 | Calendar uses 🔴🟢🔵🟡 dots | Color-only; emoji icons; inaccessible. | Section 8: color **plus** shape + text label. |
| R12 | Bottom nav "Home, Karz, Kameti, Needs, Settings" | "Needs" is undefined; Kameti deserves a hub, not a primary slot. | Section 11.1: Home, Transactions, Karz, Plans, Settings. |
| R13 | Recurring bills appear in the calendar, but no schema | Calendar can't be built truthfully. | Section 4.2/7.3: `recurring_rule` + `recurring_occurrence`. |
| R14 | `FLAG_SECURE` always on | Blocks legitimate screenshots/screen-sharing support; user can't choose. | Section 13.3: setting, default **on**, with explanation. |
| R15 | Money formatting not specified | Pakistani grouping is `12,34,567`, not `1,234,567`. | Section 3.2: lakh/crore grouping formatter + tests. |

---

## 2. Decisions Register (defaults the agent must follow unless the repo proves otherwise)

| ID | Decision |
|---|---|
| D-01 | All money is `Long` paisa. `Double/Float` are forbidden for stored or intermediate monetary arithmetic. Max supported value: `99_999_999_999_999` paisa (₨ 999,999,999,999.99). |
| D-02 | **Date-only** values (due dates, installment dates, committee dates) are stored as `Long` = `LocalDate.toEpochDay()`. **Instants** (created/updated/transaction time) are stored as epoch milliseconds. Never store a date-only value as a UTC midnight instant. |
| D-03 | Account balance = `openingBalancePaisa + Σ signed effects of non-voided transactions`. Derived via query. Do **not** also keep a mutable `balance` column. If the existing app has one, replace its readers with the derived query in Phase 1 and keep the column only if removing it is migration-unsafe (then document and keep it in sync inside the same Room transaction with an invariant test). |
| D-04 | Committee (Kameti) contributions are *outflows* categorised **Committee**; payouts are *inflows* categorised **Committee payout**. Both are excluded from "Income"/"Expense" totals by default and shown on a separate **Committee** line. A setting "Count committee in spending" (default off) may include contributions in expenses. Net balance change always includes them. |
| D-05 | Karz **money** obligations may touch an account only through an explicit linked transaction the user confirms. Karz **item/service** obligations never touch an account. |
| D-06 | Recurring rules, Kameti dates, installment dates and reminders create **pending occurrences**, never transactions. |
| D-07 | Reversal over deletion for any financial record that already affected another record. Plain unlinked income/expense may be hard-deleted after confirmation (user-facing wording: "Delete"), implemented as soft-delete (`voidedAt`) so it is auditable. |
| D-08 | Pakistani digit grouping (`12,34,567.50`) with `₨` prefix by default. A setting may switch to `Rs`/`PKR`. Stored data is unaffected. |
| D-09 | Receipts live in app-private storage; DB stores a relative file name, not an absolute path. |
| D-10 | No SMS permissions. Bank-message import is paste or share-sheet text only. |
| D-11 | Backups default to **encrypted** (`AES-256-GCM`, `PBKDF2WithHmacSHA256`). Plain export requires an explicit confirmation naming it "unencrypted". |
| D-12 | Primary nav: Home, Transactions, Karz, Plans, Settings. |
| D-13 | Minimum SDK stays whatever the project already declares. Use `java.time` (desugaring if `minSdk < 26`). |

---

## 3. Core Primitives (implement first, test first)

### 3.1 `Money.kt` — strict parsing

```kotlin
package <pkg>.core.money

sealed interface AmountParseResult {
    data class Success(val paisa: Long) : AmountParseResult
    data class Failure(val error: AmountError) : AmountParseResult
}

enum class AmountError { EMPTY, MALFORMED, TOO_MANY_DECIMALS, NEGATIVE, ZERO_NOT_ALLOWED, TOO_LARGE }

object Money {
    const val PAISA_PER_RUPEE = 100L
    const val MAX_PAISA = 99_999_999_999_999L

    private val prefix = Regex("^(?:₨|Rs\\.?|PKR)\\s*", RegexOption.IGNORE_CASE)
    private val decimal = Regex("^\\d+(\\.\\d{1,2})?$")
    private val tooManyDecimals = Regex("^\\d+\\.\\d{3,}$")

    /** Accepts "1,250", "1250.5", "₨ 1,250.50", Urdu/Arabic-Indic digits. Rejects negatives. */
    fun parse(input: String, allowZero: Boolean = false): AmountParseResult {
        var s = input.trim()
        if (s.isEmpty()) return AmountParseResult.Failure(AmountError.EMPTY)
        s = prefix.replaceFirst(s, "")
        s = normalizeDigits(s).replace(",", "").replace(" ", "")
        if (s.startsWith("-")) return AmountParseResult.Failure(AmountError.NEGATIVE)
        if (tooManyDecimals.matches(s)) return AmountParseResult.Failure(AmountError.TOO_MANY_DECIMALS)
        if (!decimal.matches(s)) return AmountParseResult.Failure(AmountError.MALFORMED)
        val parts = s.split('.')
        val whole = parts[0].toLongOrNull() ?: return AmountParseResult.Failure(AmountError.TOO_LARGE)
        if (whole > MAX_PAISA / PAISA_PER_RUPEE) return AmountParseResult.Failure(AmountError.TOO_LARGE)
        val frac = if (parts.size == 2) parts[1].padEnd(2, '0').toLong() else 0L
        val paisa = whole * PAISA_PER_RUPEE + frac
        if (paisa > MAX_PAISA) return AmountParseResult.Failure(AmountError.TOO_LARGE)
        if (paisa == 0L && !allowZero) return AmountParseResult.Failure(AmountError.ZERO_NOT_ALLOWED)
        return AmountParseResult.Success(paisa)
    }

    private fun normalizeDigits(s: String): String = buildString(s.length) {
        for (c in s) append(if (Character.isDigit(c)) Character.forDigit(Character.digit(c, 10), 10) else c)
    }

    fun addExact(a: Long, b: Long): Long = Math.addExact(a, b)
    fun subtractExact(a: Long, b: Long): Long = Math.subtractExact(a, b)
}
```

### 3.2 `PkrFormatter.kt` — lakh/crore grouping

```kotlin
package <pkg>.core.money

enum class CurrencyStyle(val prefix: String) { SYMBOL("₨ "), RS("Rs "), PKR("PKR ") }

object PkrFormatter {
    fun format(paisa: Long, style: CurrencyStyle = CurrencyStyle.SYMBOL, signed: Boolean = false): String {
        require(paisa != Long.MIN_VALUE)
        val negative = paisa < 0
        val abs = if (negative) -paisa else paisa
        val rupees = abs / 100
        val frac = (abs % 100).toInt()
        val body = group(rupees.toString()) + if (frac != 0) ".%02d".format(java.util.Locale.ROOT, frac) else ""
        val sign = when { negative -> "-"; signed && paisa > 0 -> "+"; else -> "" }
        return "$sign${style.prefix}$body"
    }

    /** 1234567 -> 12,34,567 */
    internal fun group(digits: String): String {
        if (digits.length <= 3) return digits
        val last3 = digits.takeLast(3)
        val rest = digits.dropLast(3).reversed().chunked(2).joinToString(",").reversed()
        return "$rest,$last3"
    }
}
```

Required tests: `0 → "₨ 0"`, `100 → "₨ 1"`, `150 → "₨ 1.50"`, `123456700 → "₨ 12,34,567"`, `-5000 → "-₨ 50"`, max value, parse→format round trip.

### 3.3 `InstallmentSplitter.kt` — deterministic rounding

```kotlin
package <pkg>.core.money

import java.time.LocalDate

object InstallmentSplitter {
    /** Splits principal into n parts that sum EXACTLY to principal. The first (principal % n) parts get +1 paisa. */
    fun split(principalPaisa: Long, count: Int): List<Long> {
        require(principalPaisa >= 0) { "principal must be >= 0" }
        require(count in 1..600) { "count out of range" }
        val base = principalPaisa / count
        val extra = (principalPaisa % count).toInt()
        return List(count) { i -> if (i < extra) base + 1 else base }
    }

    /** Monthly dates computed from the anchor (not iteratively) so Jan 31 -> Feb 28/29 -> Mar 31. */
    fun monthlyDates(anchor: LocalDate, count: Int): List<LocalDate> =
        List(count) { anchor.plusMonths(it.toLong()) }

    fun weeklyDates(anchor: LocalDate, count: Int): List<LocalDate> =
        List(count) { anchor.plusWeeks(it.toLong()) }
}
```

### 3.4 `NisabCalculator.kt` — integer-only Zakat estimate

Weights are stored in **milligrams** (gold 87.48 g = `87_480`, silver 612.36 g = `612_360`) and are user-editable; rates are **paisa per gram**.

```kotlin
package <pkg>.core.zakat

object NisabCalculator {
    const val DEFAULT_GOLD_NISAB_MG = 87_480L
    const val DEFAULT_SILVER_NISAB_MG = 612_360L

    /** Half-up rounded: weightMg * ratePerGramPaisa / 1000. Uses Math.multiplyExact to surface overflow. */
    fun nisabThresholdPaisa(weightMg: Long, ratePerGramPaisa: Long): Long {
        require(weightMg > 0 && ratePerGramPaisa > 0)
        val product = Math.multiplyExact(weightMg, ratePerGramPaisa)
        return (product + 500L) / 1000L
    }

    /** 2.5% (= 1/40), half-up. */
    fun zakatPayablePaisa(eligibleWealthPaisa: Long): Long {
        require(eligibleWealthPaisa >= 0)
        return (eligibleWealthPaisa + 20L) / 40L
    }

    fun isNisabReached(eligibleWealthPaisa: Long, thresholdPaisa: Long) = eligibleWealthPaisa >= thresholdPaisa
}
```

Tests: threshold with `ratePerGram = 30_000` (₨300/g) and silver weight; `eligible=100 → payable 3`; `eligible=19 → 0`; `eligible=20 → 1`; overflow test throws `ArithmeticException` and the UI shows a validation error, not a crash.

### 3.5 `CsvWriter.kt` — safe export

```kotlin
package <pkg>.core.export

import java.io.Writer

object CsvWriter {
    private val formulaStarts = setOf('=', '+', '-', '@', '\t', '\r')

    /** Text fields only. Never pass numeric amount columns through here. */
    fun safeText(value: String): String {
        val v = value.replace("\u0000", "")
        return if (v.isNotEmpty() && v[0] in formulaStarts) "'$v" else v
    }

    fun escape(field: String): String =
        if (field.any { it == ',' || it == '"' || it == '\n' || it == '\r' })
            "\"" + field.replace("\"", "\"\"") + "\"" else field

    fun writeRow(out: Writer, fields: List<String>) {
        out.write(fields.joinToString(",") { escape(it) })
        out.write("\r\n")
    }

    /** Excel opens UTF-8 correctly (including Urdu) only with a BOM. */
    fun writeBom(out: Writer) = out.write("\uFEFF")

    /** Amounts as plain decimal, no grouping, with a separate currency column. 150 -> "1.50" */
    fun amount(paisa: Long): String {
        val neg = paisa < 0
        val abs = if (neg) -paisa else paisa
        return (if (neg) "-" else "") + "${abs / 100}.%02d".format(java.util.Locale.ROOT, (abs % 100).toInt())
    }
}
```

---

## 4. Data Model

### 4.1 Transaction type rules (single source of truth)

Create `TransactionTypeRules.kt` with an enum `TransactionType`, a function `signFor(type, side)`, and a function `reportingClass(type)`. All SQL queries and Kotlin summaries must use these constants (generate the SQL `IN (...)` lists from the enum so they cannot drift).

| Type | Account effect | Reporting class | Notes |
|---|---|---|---|
| `INCOME` | + | INCOME | |
| `EXPENSE` | − | EXPENSE | |
| `TRANSFER_OUT` / `TRANSFER_IN` | − / + | NEUTRAL | share `transferId`; always saved as a pair |
| `DEBT_RECEIPT` (borrowed money received) | + | NEUTRAL | linked to debt |
| `DEBT_DISBURSEMENT` (lent money handed over) | − | NEUTRAL | linked to debt |
| `DEBT_REPAYMENT_OUT` (I repaid) | − | NEUTRAL | linked to settlement |
| `DEBT_COLLECTION_IN` (they repaid me) | + | NEUTRAL | linked to settlement |
| `COMMITTEE_CONTRIBUTION` | − | COMMITTEE | see D-04 |
| `COMMITTEE_PAYOUT` | + | COMMITTEE | |
| `CHARITY_PAYMENT` | − | EXPENSE | category Charity; linked to `charity_record` |
| `ADJUSTMENT_UP` / `ADJUSTMENT_DOWN` | + / − | NEUTRAL | from "Reconcile balance" |
| `REVERSAL` | opposite of the reversed row | same class, negated | `reversalOfId` set |

Add to the existing transaction table (via migration, nullable/defaulted): `sourceType TEXT NULL`, `sourceId INTEGER NULL`, `transferId TEXT NULL`, `reversalOfId INTEGER NULL`, `voidedAtEpochMs INTEGER NULL`, `receiptFileName TEXT NULL` (if absent), `createdAtEpochMs`, `updatedAtEpochMs` (if absent). Reuse existing columns where they already express the same thing.

### 4.2 New / changed Room entities (target shape — reconcile with existing names)

All amounts `Long` paisa. All `*EpochDay` columns are `LocalDate.toEpochDay()`. Enums are stored as `TEXT` via `TypeConverter`s using `name`.

```kotlin
// --- Committee (Kameti) ---
enum class KametiCadence { WEEKLY, MONTHLY }
enum class KametiStatus { ACTIVE, PAUSED, COMPLETED }
enum class ContributionStatus { PENDING, PAID, PARTIAL, MISSED, SKIPPED }
enum class PayoutStatus { EXPECTED, RECEIVED, NOT_RECEIVED }

@Entity(tableName = "kameti")
data class KametiEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val memberCount: Int,
    val contributionPaisa: Long,
    val cadence: KametiCadence,
    val startDateEpochDay: Long,
    val totalInstallments: Int,
    val myPayoutInstallment: Int,          // 1..totalInstallments
    val expectedPayoutPaisa: Long?,        // user-entered; may differ from members*contribution
    val status: KametiStatus = KametiStatus.ACTIVE,
    val note: String = "",
    val archived: Boolean = false,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

@Entity(
    tableName = "kameti_contribution",
    foreignKeys = [ForeignKey(KametiEntity::class, ["id"], ["kametiId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("kametiId"), Index(value = ["kametiId", "installmentIndex"], unique = true), Index("dueDateEpochDay")]
)
data class KametiContributionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kametiId: Long,
    val installmentIndex: Int,
    val dueDateEpochDay: Long,
    val expectedPaisa: Long,
    val paidPaisa: Long = 0,
    val status: ContributionStatus = ContributionStatus.PENDING,
    val lastPaidDateEpochDay: Long? = null,
    val note: String = ""
)   // actual money movement lives in transactions with sourceType="KAMETI_CONTRIBUTION", sourceId=this.id

@Entity(
    tableName = "kameti_payout",
    foreignKeys = [ForeignKey(KametiEntity::class, ["id"], ["kametiId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("kametiId", unique = true)]
)
data class KametiPayoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kametiId: Long,
    val expectedDateEpochDay: Long,
    val expectedPaisa: Long?,
    val receivedPaisa: Long? = null,
    val receivedDateEpochDay: Long? = null,
    val status: PayoutStatus = PayoutStatus.EXPECTED,
    val transactionId: Long? = null
)

// --- Karz installments ---
enum class InstallmentStatus { SCHEDULED, PAID, PARTIAL, MISSED, RESCHEDULED, CANCELLED }

@Entity(
    tableName = "karz_installment",
    foreignKeys = [ForeignKey(DebtEntity::class, ["id"], ["debtId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("debtId"), Index(value = ["debtId", "number"], unique = true), Index("dueDateEpochDay")]
)
data class KarzInstallmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val debtId: Long,
    val number: Int,
    val totalCount: Int,
    val dueDateEpochDay: Long,
    val scheduledPaisa: Long,
    val paidPaisa: Long = 0,
    val status: InstallmentStatus = InstallmentStatus.SCHEDULED,
    val note: String = ""
)   // payments are DebtSettlement rows linked by installmentId (add nullable column to the settlement table)

// --- Recurring ---
enum class RecurrenceFrequency { WEEKLY, MONTHLY, YEARLY }
enum class OccurrenceStatus { PENDING, RECORDED, SKIPPED }

@Entity(tableName = "recurring_rule", indices = [Index("active")])
data class RecurringRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val expectedPaisa: Long,
    val accountId: Long?,
    val categoryId: Long?,
    val frequency: RecurrenceFrequency,
    val anchorDateEpochDay: Long,
    val endDateEpochDay: Long? = null,
    val active: Boolean = true,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

@Entity(
    tableName = "recurring_occurrence",
    foreignKeys = [ForeignKey(RecurringRuleEntity::class, ["id"], ["ruleId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("ruleId"), Index(value = ["ruleId", "dateEpochDay"], unique = true), Index("dateEpochDay")]
)
data class RecurringOccurrenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ruleId: Long,
    val dateEpochDay: Long,
    val expectedPaisa: Long,
    val status: OccurrenceStatus = OccurrenceStatus.PENDING,
    val transactionId: Long? = null,
    val snoozedUntilEpochDay: Long? = null
)

// --- Charity & Zakat ---
enum class CharityCategory { ZAKAT, SADQAH, FITRANA, KHAIRAT, CUSTOM }
enum class CharityStatus { PLANNED, PAID }

@Entity(tableName = "charity_record", indices = [Index("paidDateEpochDay"), Index("zakatCalculationId")])
data class CharityRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: CharityCategory,
    val customCategoryName: String? = null,
    val recipientName: String = "",
    val amountPaisa: Long,
    val status: CharityStatus = CharityStatus.PLANNED,
    val plannedDateEpochDay: Long? = null,
    val paidDateEpochDay: Long? = null,
    val accountId: Long? = null,
    val transactionId: Long? = null,
    val zakatCalculationId: Long? = null,
    val note: String = ""
)

@Entity(tableName = "zakat_calculation")
data class ZakatCalculationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAtEpochMs: Long,
    val label: String,                         // e.g. "Ramadan 1448"
    val standard: String,                      // "SILVER" | "GOLD"
    val weightMgUsed: Long,
    val ratePerGramPaisa: Long,
    val rateEnteredDateEpochDay: Long,
    val rateSource: String = "USER_ENTERED",
    val nisabThresholdPaisa: Long,
    val eligibleWealthPaisa: Long,
    val zakatPayablePaisa: Long,
    val nisabReached: Boolean,
    val hawlConfirmedByUser: Boolean,
    val assumptionsJson: String,               // immutable snapshot of every included/excluded line (see 9.2)
    val note: String = ""
)

// --- Budgets ---
@Entity(tableName = "category_budget", indices = [Index(value = ["categoryId", "monthKey"], unique = true)])
data class CategoryBudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val monthKey: String,          // "2026-10", or "*" for the default every month
    val limitPaisa: Long
)

// --- Audit log (no personal text) ---
@Entity(tableName = "audit_log", indices = [Index("createdAtEpochMs")])
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAtEpochMs: Long,
    val entityType: String,
    val entityId: Long,
    val action: String,            // CREATED, SETTLED, REVERSED, VOIDED, RESCHEDULED, RESTORED ...
    val amountPaisa: Long? = null
)

// --- User reminders & import fingerprints ---
@Entity(tableName = "user_reminder", indices = [Index("dateEpochDay")])
data class UserReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dateEpochDay: Long,
    val done: Boolean = false
)

@Entity(tableName = "import_fingerprint", indices = [Index(value = ["fingerprint"], unique = true)])
data class ImportFingerprintEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fingerprint: String,
    val transactionId: Long,
    val createdAtEpochMs: Long
)
```

**Debt model additions (reconcile with the existing `DebtEntity`):** `kind` (`MONEY` | `ITEM_SERVICE`), `direction` (`GAVE` | `TOOK`), `itemDescription`, `itemQuantity`, `itemUnit`, `estimatedValuePaisa` (informational only), `principalPaisa` (money kind), `status` (`OPEN`, `PARTIAL`, `SETTLED`, `ARCHIVED`), `dueDateEpochDay`. Outstanding = `principalPaisa − Σ settlements` (derived).

### 4.3 DAO requirements

* Every DAO read used by the UI returns `Flow`. One-shot reads are `suspend`.
* Aggregations return `Long?` coalesced in Kotlin; never `Double`.
* Required queries: account balances (derived), period totals by reporting class with date bounds and optional account filter, category totals, per-debt outstanding, per-kameti recorded-vs-expected, due items in a date range (union via Kotlin merge of the separate DAOs, not a giant SQL union), pending recurring occurrences ≤ date.

### 4.4 Migration (mandatory procedure)

1. Open the `@Database` class; read `version = N` and the existing migrations. **Do not assume N = 3.**
2. Ensure `exportSchema = true` and the Room `schemaLocation` KSP/kapt arg is set; commit `schemas/<pkg>.AppDatabase/N.json`.
3. Bump to `N+1`, build once so Room emits `N+1.json`, then write `MIGRATION_N_N+1` using SQL **copied from the generated schema JSON** (`createSql` entries) so the identity hash matches exactly.
4. For changed existing tables, use `ALTER TABLE … ADD COLUMN … DEFAULT …` (nullable or defaulted) wherever possible. If a table must be rebuilt (e.g., to add a foreign key), use the create-new / copy / drop-old / rename pattern **inside the migration's transaction** and run `PRAGMA foreign_key_check` at the end.
5. Backfill: existing debts → `kind='MONEY'`; existing timestamps copied; `createdAt/updatedAt` = existing value or migration time.
6. **Backfill transaction types carefully**: map the existing app's transaction/debt-related rows onto the type table in 4.1 without changing any account's derived balance. Prove this with a migration test comparing every account's pre- and post-migration balance.
7. Add the migration to `Room.databaseBuilder(...).addMigrations(...)`. Never call `fallbackToDestructiveMigration*`.
8. Write `MigrationTestHelper` tests (androidTest) that create the old-version DB with representative rows (accounts, income, expense, transfer, money debt with partial repayment, receipt reference, settings) → migrate → assert counts, balances, nullability, indexes, `foreign_key_check` empty.

If the migration cannot be made safe, **stop and report the blocker**.

---

## 5. Accounting Invariants (must be unit-tested)

1. **I-1 Balance reconciliation:** Σ account balances = Σ opening balances + Σ non-voided signed effects.
2. **I-2 Transfer neutrality:** a transfer changes two accounts by equal and opposite amounts and changes Income/Expense totals by zero. Both rows are written in one `withTransaction`.
3. **I-3 Debt outstanding:** `outstanding = principal − Σ settlements ≥ 0`. Overpayment is rejected unless the user records an explicit, labelled `ADJUSTMENT` with a note.
4. **I-4 Item/service neutrality:** creating, settling, or reversing an item/service debt changes **no** account balance.
5. **I-5 Money debt single-effect:** borrowed ₨5,000 received into Account A raises A exactly once (one `DEBT_RECEIPT`); toggling "money actually received/handed over" off creates no transaction.
6. **I-6 Occurrence uniqueness:** a recurring occurrence / installment / contribution can produce at most one *active* (non-voided, non-reversed) transaction for a given amount-entry; double taps and replays are idempotent.
7. **I-7 Reversal exactness:** reversing a record restores every dependent total exactly and leaves an audit row.
8. **I-8 Committee neutrality in reports:** committee flows never appear in Income/Expense by default (D-04).
9. **I-9 No negative creation:** no operation may create or destroy money through a partial failure (all multi-row writes are single Room transactions).

### 5.1 Reference pattern for an atomic operation

```kotlin
class RecordKametiContribution(
    private val db: AppDatabase,
    private val clock: java.time.Clock
) {
    sealed interface Result {
        data class Success(val transactionId: Long) : Result
        data class Rejected(val reason: String) : Result
    }

    suspend operator fun invoke(contributionId: Long, accountId: Long, amountPaisa: Long, dateEpochDay: Long, note: String): Result =
        db.withTransaction {
            if (amountPaisa <= 0 || amountPaisa > Money.MAX_PAISA) return@withTransaction Result.Rejected("Invalid amount")
            val c = db.kametiDao().getContribution(contributionId) ?: return@withTransaction Result.Rejected("Contribution not found")
            val remaining = c.expectedPaisa - c.paidPaisa
            if (amountPaisa > remaining) return@withTransaction Result.Rejected("Amount exceeds remaining ₨ ${PkrFormatter.format(remaining)}")
            val now = clock.millis()
            val txId = db.transactionDao().insert(
                TransactionEntity(/* type = COMMITTEE_CONTRIBUTION, accountId, amountPaisa, dateEpochMs from dateEpochDay at local noon,
                                     sourceType = "KAMETI_CONTRIBUTION", sourceId = contributionId, createdAt/updatedAt = now */)
            )
            val newPaid = c.paidPaisa + amountPaisa
            db.kametiDao().update(
                c.copy(
                    paidPaisa = newPaid,
                    status = if (newPaid == c.expectedPaisa) ContributionStatus.PAID else ContributionStatus.PARTIAL,
                    lastPaidDateEpochDay = dateEpochDay
                )
            )
            db.auditDao().insert(AuditLogEntity(createdAtEpochMs = now, entityType = "KAMETI_CONTRIBUTION", entityId = contributionId, action = "PAID", amountPaisa = amountPaisa))
            Result.Success(txId)
        }
}
```

Use the same pattern (validate → write rows → update aggregate/status → audit → return result inside one transaction) for: `createIncome`, `createExpense`, `transferBetweenAccounts`, `createMoneyDebt`, `createItemDebt`, `recordDebtSettlement`, `settleDebtInFull`, `recordKarzInstallmentPayment`, `recordRecurringOccurrence`, `skipOccurrence`, `claimKametiPayout`, `recordCharityPayment`, `reconcileAccount`, `reverseTransaction`.

---

## 6. Kameti (Committee) Tracker

### 6.1 Create flow (`AddKametiSheet`)
Fields: name, member count, contribution per installment, cadence (weekly/monthly), start date, total installments (default = member count), **my payout installment** (1..total), expected payout amount (prefill `members × contribution`, user-editable, helper text: "Some committees pay a different amount"), note.
Validation: all amounts via `Money.parse`; `myPayoutInstallment ≤ totalInstallments`; `memberCount ≥ 2`.
On save (one transaction): insert `kameti`, generate `totalInstallments` `kameti_contribution` rows (`InstallmentSplitter.monthlyDates/weeklyDates` from start date; `expectedPaisa = contributionPaisa`), insert one `kameti_payout` row dated at installment `myPayoutInstallment`, write audit row. **No transaction rows are created.**

### 6.2 Overview card
Show: name, status chip, `Recorded ₨X of expected ₨Y` (always labelled *recorded* vs *expected*), next due contribution with date and a **text** state ("Due in 3 days", "Overdue 2 days"), payout line ("Your payout: installment 4 · Jul 2027 · expected ₨…"), progress bar with textual `4 of 10 recorded`.
Never label expected values as savings.

### 6.3 Contribution actions
Each row has a visible **Record payment** button (no hidden swipe-only action). Opens a confirm sheet: amount (default remaining), account (default = last used), date (default today), note. Buttons: **Confirm payment**, Cancel. On confirm call `RecordKametiContribution`. Also: **Mark missed** (status only, no money), **Skip** (confirm), **Correct payment** (creates reversal then new payment, with confirmation).
Late/partial states are derived: `PENDING` + `due < today` renders "Overdue".

### 6.4 Payout
**Claim payout** button appears only when the payout installment's due date ≤ today **or** the user taps "Mark as received early" (confirm). It opens a sheet showing the expected amount, an editable received amount, and the target account. Confirm → one transaction `COMMITTEE_PAYOUT` and payout row `RECEIVED`. Show the accounting note: *"Committee payout is not counted as income."*

### 6.5 Edit/delete
Editing future installment amounts changes only `PENDING` rows. Deleting a committee with recorded transactions is not allowed; offer **Archive** (and optionally **Reverse all payments**, separate confirmation listing the total). Kameti that is `COMPLETED` automatically when all contributions are `PAID/SKIPPED` and the payout is `RECEIVED`/`NOT_RECEIVED` (computed, user can reopen).

---

## 7. Karz: Money vs Item, Settlements, Installments, Recurring

### 7.1 Create Karz (`AddKarzSheet`)
Step 1: direction (**I gave** / **I took**). Step 2: kind (**Money** / **Item or service**).
* **Money:** person, amount, date, optional due date, note, and a switch **"Money actually moved through one of my accounts"** (default on) + account picker. If on → write `DEBT_DISBURSEMENT` (gave) or `DEBT_RECEIPT` (took) in the same transaction as the debt row. If off → debt only.
* **Item/service:** person, description (required), quantity/unit (optional), estimated value (optional, label "Reference only — does not change your balance"), due date, note. No account fields are shown.

### 7.2 Settlements
* **Money:** `Record payment` (amount ≤ outstanding, account, date, note) → settlement row + `DEBT_REPAYMENT_OUT`/`DEBT_COLLECTION_IN`. `Mark fully repaid/collected` shows the remaining amount and requires confirmation, then records the remaining amount (not zero-amount "magic").
* **Item/service:** `Mark returned/delivered` (confirm). If compensated with money, a separate **Add money settlement** records it explicitly.
* List rows show direction text: **"You owe"** / **"Owes you"**, kind badge (Money / Item), outstanding, overdue state as text+icon.
* Detail screen shows settlement history, linked transactions, installment table, actions (Record payment, Mark fully settled, Edit, Share reminder, Archive), each destructive/finalising action confirmed.

### 7.3 Karz installment plans
* Offer **Split into installments** only for money debts. Inputs: count (2–120), cadence (weekly/monthly), first due date. Preview table (number, date, amount) **before** saving; amounts from `InstallmentSplitter.split(outstanding, count)`; sum shown and equal to outstanding.
* Paying an installment: `RecordKarzInstallmentPayment` (settlement + transaction + installment `paidPaisa/status`), all atomic. Partial → `PARTIAL`. Early settlement of the whole debt cancels future `SCHEDULED` installments (`CANCELLED`) after confirmation.
* **Reschedule** one installment: changes its due date, status `RESCHEDULED` → back to `SCHEDULED` with a new date; audit row.
* WhatsApp text includes `Installment N of M` (see 7.5). No interest or fees unless the user types them as a separate explicit adjustment.

### 7.4 Recurring bills
* Rule form: title, expected amount, frequency (weekly/monthly/yearly), first date, optional end date, default account/category, active toggle.
* **Materialisation** (`RecurringScheduler.materialise(todayEpochDay)`): for each active rule, insert missing `recurring_occurrence` rows with `INSERT OR IGNORE` (unique `(ruleId, dateEpochDay)`) for dates ≤ today + 35 days and ≥ the last recorded date; cap catch-up at 36 occurrences per rule per run. Run on app start, on resume, after a rule edit, and from a daily `WorkManager` job.
* A pending occurrence offers **Record payment** (confirm sheet with amount editable/account/date), **Skip this occurrence** (confirm), **Remind me later** (sets `snoozedUntilEpochDay`).
* Editing a rule: ask "Apply to future occurrences only" or "Also update the pending occurrence on <date>". Pausing/deleting a rule never touches recorded transactions (deleting is archive: `active=false`).
* **Notifications** (opt-in; request `POST_NOTIFICATIONS` only when the user enables reminders on API 33+). Default text is generic ("A bill is due today"); amounts/names only if the user enables "Show details on lock screen". Use `WorkManager` (inexact). Do **not** request exact-alarm permissions. Settings copy: "Reminders may arrive a little late when battery saver is on."
* Tapping a notification deep-links to the occurrence **after** the lock gate.

### 7.5 WhatsApp / share reminders
Android share intent (`ACTION_SEND`, `text/plain`) — user-initiated, editable preview dialog first, never auto-send, never claim delivery. Templates: Polite, Formal, Itemized; English now, Urdu strings in `values-ur`. Placeholders: person, outstanding, due date, installment `N of M`. PDF statements (if the existing feature exists) are written to `cacheDir/shared/` and shared via `FileProvider` with `FLAG_GRANT_READ_URI_PERMISSION`; delete files older than 24 h on app start.

---

## 8. Financial Dues Calendar

* Month grid (swipe or arrows). Today highlighted by outline + text, not color only.
* Each due item type has **color + distinct shape + short label**:

| Kind | Shape | Label | Source |
|---|---|---|---|
| Karz payable | ▲ filled | "You owe" | `karz_installment` / `debt.dueDate` where direction = TOOK |
| Karz receivable | ▼ filled | "Owes you" | same where direction = GAVE |
| Recurring bill | ■ | "Bill" | `recurring_occurrence` PENDING |
| Kameti | ◆ | "Committee" | `kameti_contribution` PENDING/PARTIAL, `kameti_payout` EXPECTED |
| Reminder | ● hollow | "Reminder" | `user_reminder` |

Draw shapes with Compose `Canvas` or vector icons (not emoji). Day cell `contentDescription`: "Tuesday 21 October, 2 items: 1 bill, 1 committee payment".
* Tap a day → bottom-sheet agenda listing records with safe actions: **Record payment**, **Remind via WhatsApp** (Karz receivables only), **Skip** (confirm; affects only that occurrence), **Open**.
* Data: a `DueItemsRepository.observeRange(start, end): Flow<List<DueItem>>` that merges DAO flows with `combine`. Overdue items appear in a separate "Overdue" section above the grid agenda.
* Dates are `LocalDate` in `ZoneId.systemDefault()`. Add tests for month boundaries, leap day, Jan 31 monthly schedules, and timezone change.

---

## 9. Zakat, Nisab, Charity Ledger (estimate tool — not a ruling)

### 9.1 Wizard (`ZakatScreen`, 4 steps)
1. **Method:** Nisab standard (Silver default / Gold), weight in grams (prefilled 612.36 / 87.48, editable), rate per gram **entered by the user** with the date entered. Label: *"User-entered rate"*. Never say "live" or "real-time".
2. **Assets:** table of selectable lines, each with amount, include checkbox, and source note. Auto-suggested lines (user can uncheck every one): account balances (one line per account), money receivables ("Karz owed to you — money only"; **unchecked by default**, helper: "Only include what you expect to collect"), plus **manual lines** the user types (gold/silver held, business stock, other). Item/service karz are never listed.
3. **Deductions:** user-selected short-term liabilities only: money payables ("Karz you owe — money only") each individually selectable, plus manual lines. Default unchecked.
4. **Result:** breakdown table (every included/excluded line), eligible wealth, Nisab threshold, "Nisab reached" yes/no text, Zakat payable (`NisabCalculator`), a **"I have verified hawl (one lunar year)"** checkbox (stored, never inferred), the notice:
   > "This is an estimate based on the method and values you selected. Please verify the calculation with a trusted qualified scholar or adviser for your circumstances."
   Buttons: **Save calculation**, **Create planned Zakat payment**.
* Saving writes `ZakatCalculationEntity` with `assumptionsJson` (list of `{label, amountPaisa, included, sourceType}` + rates + weights). Past calculations are read-only; "Duplicate as new" creates a new one. Editing today's rate never changes saved history.
* All arithmetic: `Long` + `Math.addExact`/`multiplyExact`; validation errors shown inline.

### 9.2 Charity ledger tab
List and add `charity_record`s (Zakat, Sadqah, Fitrana, Khairat, Custom). Status **Planned** (no balance effect) → **Mark paid** (confirm sheet: amount, account, date) → creates `CHARITY_PAYMENT` and links the record; if `zakatCalculationId` is set, show `Paid ₨X of ₨Y` for that calculation. Yearly total and category totals computed from paid records only. Recipient names are optional and excluded from audit logs.

---

## 10. Backup, Restore, Export

### 10.1 CSV export (`ExportScreen`)
Exports: filtered transactions, monthly summary, account ledger, Karz ledger + settlements, Kameti contributions/payouts, charity records, Zakat calculations. Step UI: choose dataset → choose date range/account → **review scope** (row count + range) → pick destination via `ActivityResultContracts.CreateDocument("text/csv")` → write on `Dispatchers.IO` → success message **only after** the stream closed without exception. Columns: stable English headers; `date` ISO `yyyy-MM-dd`; `amount` via `CsvWriter.amount`; separate `currency` column (`PKR`); text through `CsvWriter.safeText` then `escape`. Include UTF-8 BOM.

### 10.2 Backup file format (`.hisaab`)
Single file, streamed:
```
magic "HSBK" (4 bytes) | formatVersion (1 byte = 1) | kdfId (1 byte: 1 = PBKDF2-HMAC-SHA256) |
iterations (4 bytes, big-endian, ≥ 310000) | salt (16 bytes) | iv (12 bytes) |
AES-256-GCM( zip( manifest.json, data.json, receipts/<file>... ) ) with the header bytes as AAD
```
`manifest.json`: `formatVersion`, `appVersionName`, `dbSchemaVersion`, `createdAtEpochMs`, per-table row counts, receipt count, SHA-256 of `data.json`. `data.json`: a versioned DTO set (one array per table). Plain mode (explicit confirmation naming it "unencrypted") writes the same zip with extension `.hisaab.zip` and no header.
Crypto: `javax.crypto` only (`SecretKeyFactory "PBKDF2WithHmacSHA256"`, `Cipher "AES/GCM/NoPadding"`, 128-bit tag, `SecureRandom` IV never reused with the same key). No custom primitives. Never log passphrases, keys, or file contents.

### 10.3 Manual and scheduled backups
* **Manual:** passphrase + confirm → `CreateDocument` destination → write → **verify** by re-opening the written file, decrypting, validating manifest + hash → show "Backup verified (N records)". If verification fails, show failure and offer to retry; never show success.
* **Scheduled (opt-in):** user picks a folder with `OpenDocumentTree`, app calls `takePersistableUriPermission`. A worker cannot prompt for a passphrase, so at opt-in time the app derives the key once from the passphrase+salt, **wraps the derived key with an Android Keystore AES key** (non-exportable, `setUserAuthenticationRequired(false)`), and stores only the wrapped blob in app-private storage. Each run writes a new file `HisaabBackup_yyyyMMdd_HHmm.hisaab` with its own random IV and the stored salt/iterations in the header, so **restore only needs the passphrase** (re-derives the key). Explain in UI: "If you forget the passphrase, backups cannot be recovered."
* Frequency: daily/weekly/monthly via `PeriodicWorkRequest` with `setRequiresStorageNotLow(true)`. Handle: revoked URI permission (mark schedule "needs attention", surface a banner), unavailable folder, low space, restart. Retention: keep the last N (default 7) **only files whose names match the app pattern**; never delete other files. Never overwrite an existing file (timestamped names; on collision append a counter). Show "Last successful backup: <time>" from a persisted success record.

### 10.4 Restore
1. User picks a file (`OpenDocument`). 2. Ask passphrase (if encrypted). 3. Decrypt to a cache temp file; validate magic/version/GCM tag; unzip; validate manifest, SHA-256, `dbSchemaVersion ≤ current`; reject newer. 4. Show **preview**: record counts per table vs current counts, backup date, app version. 5. User chooses **Replace current data** (default) — a second confirm lists the data that will be replaced, and the app first writes an automatic pre-restore safety copy to cache/app-private storage and offers to keep it. (Merge is **not** offered unless fully implemented and tested.) 6. Execute inside **one Room transaction**: clear tables in FK-safe order, insert DTOs, restore receipts into app-private storage under temp names then rename, run `PRAGMA foreign_key_check`. On any exception roll back and keep the old data. 7. Success is shown only after commit + a re-read of counts.
Tests use a real temporary Room database (instrumented or Robolectric with in-memory/real file DB), including: wrong passphrase, tampered byte (GCM fails), truncated file, newer schema, missing receipt, and round-trip equality of all balances.

---

## 11. Navigation, Layout, Dashboard

### 11.1 Navigation
* **Compact (<600dp):** bottom bar — Home, Transactions, Karz, Plans, Settings.
* **Plans hub** (tabs/cards): Kameti, Installments, Recurring bills, Dues calendar, Zakat & Charity, Budgets.
* **Medium/Expanded:** `NavigationRail` (or the project's supported adaptive navigation suite). Two-pane list+detail only on **Expanded** (≥840dp) for Transactions and Karz; Medium uses single pane with a rail.
* Use `currentWindowAdaptiveInfo()` / `WindowSizeClass` APIs **only if present in the project's dependency versions**; otherwise compute `WindowSizeClass` from `LocalConfiguration`. Do not invent APIs. Preserve selected item across rotation and fold/unfold (`rememberSaveable`).
* Every list row exposes a visible way into details (chevron or "Details" action), not a hidden gesture.

### 11.2 Dashboard order
1. App title + date. 2. **Current balance** (hide/show eye toggle, persisted; hidden state also masks amounts in recent-app preview if `FLAG_SECURE` is on). 3. Account chips / compact account list. 4. Quick actions: Add Expense, Add Income, Transfer, Add Karz. 5. Period summary (This month / Last month / Custom): **Income, Expenses, Net cash flow** (transfers + committee excluded; footnote line shows committee flows separately). 6. **Upcoming & pending** card (next 7 days of due items + pending recurring/installments with Record/Skip). 7. Budget progress (only if budgets exist). 8. Last 5 transactions + "View all". 9. Category donut only with real data, with a text list equivalent.
* **Balance labels:** "Current balance", "Net cash flow (this month)", "Estimated available after commitments" (= current balance − the user-selected upcoming commitments; shows its formula on tap). Remove any ambiguous "Remaining Balance" card unless it is migrated to one of these labels with release-note text in Settings → About.
* Empty states: no accounts → "Create your first account"; no transactions → "Add your first transaction". Distinguish *empty* from *failed to load* (error state with Retry).

---

## 12. Quick Logging, Search, Import, Budgets, Reconcile

### 12.1 Speed-dial / presets
Keep the existing `ExpressSpeedDialFAB`. Presets (Chai, Fuel, Groceries/Kiryana, Mobile load, Bills, Rickshaw/Transport, Sadqah) are **editable user templates** stored in DB/DataStore (name, category, optional default amount); they are not transactions. Tapping a preset opens a pre-filled sheet with **Save** (one tap) — a transaction is created only on Save.

### 12.2 Search & filters (Transactions)
Local search over title/note/person; filters: date range, account, type, category, amount range, person, linked record (Karz/Kameti/Recurring), status. Filters persist across navigation to detail and back (`SavedStateHandle`). Use `Paging 3` **only if** the project already includes it or the list exceeds ~2,000 rows in tests; otherwise bounded queries.

### 12.3 Transaction detail
Amount/type (text + icon), date/time, account, category, title/note, receipt (graceful "file missing" state), linked record, audit info, actions: Edit, Share, Reverse/Delete (per D-07 with explanatory confirm). After delete/void show an **Undo** snackbar (reverts the void within 10 s).

### 12.4 Reconcile balance (new)
Account menu → **Reconcile**: user enters the real balance; app shows the difference and creates an `ADJUSTMENT_UP/DOWN` transaction (category "Adjustment", note required) only after confirmation. Excluded from income/expense.

### 12.5 Category budgets (new, optional)
Monthly limit per category (`category_budget`), default `monthKey="*"` plus per-month overrides. Dashboard/Plans show `spent / limit` with text ("₨3,200 left", "₨500 over") and a progress bar that is never color-only. Optional local notification at 80%/100% (opt-in). Spending counts EXPENSE class only.

### 12.6 Manual bank-message import (replaces SMS scanner)
* Entry points: **Import bank message** (paste field) and an `ACTION_SEND` `text/plain` intent filter so the user can *share* a message into Hisaab. Never `READ_SMS`/notification listener.
* `BankMessageParser` (pure Kotlin, on-device): extracts amount (`Rs.`/`PKR`/`₨` patterns, `Money.parse`), direction keywords (debited/credited/paid/received/sent), date/time, last-4 of account/card, merchant/counterparty guess. Returns a **draft** with a per-field confidence (`FOUND`, `GUESSED`, `UNKNOWN`). Unknown fields are left blank for the user.
* User reviews/edits → **Confirm** → save. Duplicate guard: `fingerprint = SHA-256(normalised amount + direction + last4 + date-to-minute + merchantNormalised)` checked against `import_fingerprint`; on match show "Looks like a duplicate of <transaction> — save anyway?".
* Provider-specific parsers (JazzCash, EasyPaisa, Meezan, HBL, SadaPay, NayaPay) only with **synthetic** test fixtures that contain no real personal data. Do not insert fixtures into the production DB.

---

## 13. Design System, Motion, Accessibility, Privacy

### 13.1 Tokens (`ui/theme/HisaabTokens.kt`, one central file; no hex values elsewhere)
Light defaults (tune after contrast tests): `background #F1EBDF`, `surface #F8F3E9`, `surfaceRaised #FFFBF2`, `textPrimary #2B2118`, `textSecondary #6B5E50`, `outline #CDBFA9`, `income #2F6B4F`, `expense #A5482B`, `transfer #3D5A80`, `debt #7A5C1E`, `committee #5B4B8A`, `warning #8A5A00`, `error #B3261E`. Provide a dark scheme with the same semantic names (never pure black/white). Spacing 4/8/12/16/24/32; radii small 12, card 20, sheet 28; min touch target 48dp; durations 120/200/300 ms; elevation tokens (restrained, ≤ 6dp).
Add a unit test computing WCAG contrast (`(L1+0.05)/(L2+0.05)`) for every text/background pair: body text ≥ 4.5:1, large text/icons ≥ 3:1. Adjust tokens until it passes.

### 13.2 Clay look (restrained)
Raised cards: subtle top-left highlight + soft bottom shadow via `Modifier.shadow` + 1px inner outline; pressed state flattens slightly. No neon, no blobs, no glass. Money amounts use a tabular/lining numeral font feature (`fontFeatureSettings = "tnum"`) so columns align.

### 13.3 Privacy & lock
* Biometric: `BiometricPrompt` with `BIOMETRIC_STRONG | DEVICE_CREDENTIAL` where supported; otherwise the in-app PIN. No fake scanner UI.
* PIN: ≥ 4 digits (offer 6), salted `PBKDF2WithHmacSHA256` verifier in app-private storage (not plain text, not `SharedPreferences` raw digits), constant-time compare, rate limit (5 failures → exponential delay, persisted across process death). Explain that a short PIN is weak.
* Auto-lock after a configurable background interval (default 15 s). **One lock gate** at the activity/nav-host level covers: cold start, deep links, notification taps, app shortcuts, Quick Settings tile launches, `ACTION_SEND` import, and return from another activity. Add a test that every entry intent is blocked until unlock.
* `FLAG_SECURE`: Setting "Hide app in recent apps & block screenshots" (default on). Copy explains it does not stop a camera or every capture method.
* Auto Backup: set `android:allowBackup="false"` (or explicit `dataExtractionRules`/`fullBackupContent` excluding DB, receipts, backup keys) and say so in Settings → Privacy.
* No logging of amounts, names, notes, PINs, passphrases, receipt paths (strip `Log.*` or use a debug-only logger).
* **Delete all local data:** type-to-confirm ("DELETE"), then wipes DB, receipts, preferences, Keystore keys, scheduled work; shows a result after verifying emptiness.

### 13.4 Motion & haptics
Haptics via `HapticFeedback`/`View.performHapticFeedback` (tick on keypad, confirm on save, reject on error). Setting to disable. No sounds by default. Respect `Settings.Global.ANIMATOR_DURATION_SCALE == 0` (skip animations). Success is always also shown as text (snackbar/inline), never animation only.

### 13.5 Accessibility
`contentDescription` on every icon-only control; charts expose a text summary and a data list; focus order verified on tablet/keyboard; modal sheets trap and restore focus; font scale up to 200% with no clipped text (no fixed heights on text containers); semantics roles for toggles/tabs; test with TalkBack and add Compose UI tests using `assertContentDescriptionEquals` for key controls.

### 13.6 Localization
`strings.xml` is the only source of UI text (no hard-coded literals). Add `values-ur/strings.xml` with the full set; expose "Urdu" in Settings **only when 100% of strings are translated**. Set `android:supportsRtl="true"`; use `start/end` not `left/right`. Support per-app language (`AppCompatDelegate.setApplicationLocales` or `LocaleManager`). Amounts remain left-to-right isolated (`\u2066…\u2069`) inside RTL text. Test large Urdu text for clipping (Nastaliq needs extra line height).

### 13.7 Category icons
Author original vector drawables (24dp, single path set, `currentColor` tint): chai cup, rickshaw/transport, electricity meter (generic bolt), gas flame, water drop, kiryana grocery bag, sadqah box, mobile load, rent/home, medical, education. **No third-party brand logos** (K-Electric, SNGPL, banks). Icons accompany, not replace, text labels.

### 13.8 Adaptive app icon
Keep the clay booklet + rupee design. Provide `ic_launcher_foreground/background` (+ monochrome layer for themed icons), keep the mark inside the 66dp safe zone of the 108dp canvas, verify at 48dp.

---

## 14. App Shortcuts & Quick Settings Tile

* **Static shortcuts** (`res/xml/shortcuts.xml`): *Quick expense*, *Add Karz*, *Lock vault*. Each launches `MainActivity` with a custom action; the lock gate runs first, then the target sheet opens.
* **Quick Settings tile** (`TileService`): only if it can open the Quick-Expense sheet through the lock gate (use `startActivityAndCollapse` with a `PendingIntent` on API 34+). It must never write a transaction without the app UI. If this can't be done safely in the project's SDK range, omit it and record why in `brain.md`.
* "Lock vault" shortcut sets locked state and finishes the task.

---

## 15. Permissions Policy

Allowed by default: none beyond normal. Conditional: `POST_NOTIFICATIONS` (only when user enables reminders), `USE_BIOMETRIC` (normal). Receipts via `PickVisualMedia` (no storage permission). Camera capture, if offered, via `ActivityResultContracts.TakePicture` into app-private `FileProvider` URI (no `CAMERA` permission needed when using the system camera app). **Forbidden:** `INTERNET`, `READ_SMS`/`RECEIVE_SMS`, `READ_EXTERNAL_STORAGE`/`MANAGE_EXTERNAL_STORAGE`, `SCHEDULE_EXACT_ALARM`/`USE_EXACT_ALARM`, notification-listener access, contacts. Add a Gradle/CI check or unit test that parses the merged manifest and fails if a forbidden permission appears. Document every permission in `README.md` and Settings → About → Permissions.

---

## 16. Performance & Reliability

* No DB, file, PDF, image, crypto, or serialization work on the main thread. Use `Dispatchers.IO`/`Default` via injected dispatchers for testability.
* `StateFlow` UI state with sealed `Loading/Content/Empty/Error`; use `collectAsStateWithLifecycle`.
* Stable keys in `LazyColumn`. Heavy lists use bounded queries.
* Disable submit buttons while saving; make every create operation idempotent against double taps (ViewModel `isSaving` guard + repository unique constraints where applicable).
* Handle process death (`SavedStateHandle` for in-progress forms), disk full, corrupted/foreign backup file, missing receipt, revoked URI.
* Receipt images: downscale to max 1600 px long edge, JPEG quality ~85, strip location EXIF, store under `filesDir/receipts/<uuid>.jpg`; delete the file only after the DB reference is removed and confirmed.

---

## 17. Test Matrix (write and run)

**Unit (JVM):** `Money.parse` (valid, commas, `Rs`/`₨`/`PKR` prefixes, Urdu digits, `1.5`, `1.505` rejected, `-1` rejected, overflow, zero); `PkrFormatter` cases from 3.2; `InstallmentSplitter` (sum equals principal for 1…600 counts, deterministic distribution, Jan 31 monthly, leap day); `NisabCalculator`; `CsvWriter` (commas, quotes, newlines, formula prefixes, negative amount not prefixed); contrast test; invariants I-1…I-9 via in-memory Room (Robolectric or instrumented); recurring materialisation idempotency and catch-up cap; `BankMessageParser` (synthetic fixtures); backup crypto round-trip, wrong passphrase, tamper detection.

**Room/instrumented:** DAO CRUD + FK RESTRICT behaviour; atomic transfer failure rollback (simulate exception on 2nd insert → both rows absent); atomic settlement; migration `N → N+1` per 4.4; empty vs populated queries.

**Compose UI:** first launch has zero transactions and empty-state CTAs; create account → add expense → balance updates → survives process restart; transfer leaves income/expense unchanged; borrowed-money-received affects the account once; item karz leaves balances unchanged; recurring due item doesn't post until Confirm; full settlement requires confirm; Kameti contribution/payout confirm flows and totals; Zakat wizard saves immutable snapshot; backup → wipe → restore equals original; app lock after background and via deep link/shortcut; 200% font scale smoke test on Home, Karz detail, Kameti; Expanded-width two-pane renders and keeps selection on rotation.

**Build/regression:** run the project's real Gradle tasks (discover them; typically `./gradlew testDebugUnitTest`, `./gradlew connectedDebugAndroidTest` if a device/emulator exists, `./gradlew lintDebug`, `./gradlew assembleDebug`). Record outputs in `brain.md`. If an emulator is unavailable, say so and list the owner's commands.

**Release checklist:** no `INTERNET`; no seeded data (grep for demo/sample inserts); no `fallbackToDestructiveMigration`; no placeholder buttons (grep `TODO`, `onClick = {}`); no hard-coded UI strings; Urdu hidden unless complete; privacy text matches behaviour; backup/restore verified; CSV opened-and-checked; icon verified.

---

## 18. Phased Plan (continue automatically when a gate passes; stop only on a blocker)

| Phase | Work | Gate |
|---|---|---|
| **0 Audit** | Inspect repo, Git status, DB version, permissions, tests; build baseline; write audit + plan to `brain.md`. | Audit table complete; baseline build result recorded. |
| **1 Primitives & accounting** | 3.1–3.5, 4.1 type rules, invariants I-1…I-3, I-9; fix any float/double money; derived balances; transfer neutrality; unit tests. | All unit tests green; no `Double` on money paths (grep). |
| **2 Schema & migration** | 4.2 entities, DAOs, `N→N+1` migration, schema JSON committed, migration + DAO tests, type backfill. | Migration test proves unchanged balances and data counts. |
| **3 Karz v2** | 7.1–7.3 money vs item, settlements, installments, I-4/I-5; UI. | Invariant tests + UI tests green. |
| **4 Recurring & reminders** | 7.4, occurrence materialiser, confirm-first flows, opt-in notifications. | Duplicate-prevention tests green; no transaction without confirm. |
| **5 UI foundation** | Tokens, contrast test, dashboard 11.2, nav 11.1, empty/error states, transaction detail/search/filters, undo, reconcile, a11y, haptics settings. | Contrast + large-text tests pass. |
| **6 Backup/restore/export** | Section 10 complete incl. scheduled backups and verification. | Round-trip, tamper, wrong-passphrase tests green. |
| **7 Kameti, Zakat, Charity, Calendar, Budgets** | Sections 6, 8, 9, 12.5. | Accounting + UI tests green. |
| **8 Adaptive, shortcuts, import, localization** | 11.1 two-pane, 14, 12.6, 13.6. | Lock-gate entry-point tests pass. |
| **9 Final verification** | Full test/lint/build; diff review; release checklist; README; `brain.md` final. | All checklist items true or listed as known limitations. |

After every phase: update `brain.md` (audit status, decisions, changed files, commands actually run + result summaries, limitations, next step) and commit with a descriptive message (e.g. `feat(accounting): paisa primitives, derived balances, invariants`). Never commit secrets, backups, keystores, or personal data.

### 18.1 Required deliverables
Working code for completed phases; entities/DAOs/repositories/use-cases; safe migration + committed schema JSON; Compose screens and reusable components; tokens/theme; tests per section 17; updated manifest + backup rules; `brain.md`; `README.md` (setup, build/test commands, offline/privacy model, permissions table, backup/restore guide, known limitations); a final report separating **Completed / Partial / Blocked / Not started**.

---

## 19. Execution Prompt for Google AI Studio (copy/paste)

```
Read Hisaab_Do_Bhai_Master_Specification_v4.md completely. It supersedes the earlier roadmap (v2.0) and spec (v3.0) and is the product and engineering source of truth. Do not assume it describes features that already exist.

Start with Phase 0: inspect the repository, Git status (preserve all uncommitted changes), Gradle/dependency versions, manifest permissions, navigation, Room @Database version and migration chain, entities/DAOs, repositories, ViewModels, screens, resources, and tests. Build the project and record the real result. Write the implementation audit (Implemented / Partial / Missing / Conflicting / Needs decision, with file paths) and a phased plan into brain.md at the project root.

Then implement phases 1–9 in order. Do not rewrite the app from scratch. Do not guess package names, DAO names, or the Room version — discover them. Money is integer paisa (Long) only. Use the reference code in Sections 3–5 after reconciling it with the existing code. All multi-record accounting changes must be single Room transactions with tests. Money-vs-item Karz, recurring payments, Kameti contributions/payouts, charity payments, Karz settlements, backup restores, and destructive actions must require explicit user confirmation, and item/service obligations must never change account balances.

Before any schema change, read the real database version N, enable schema export, write a non-destructive migration N→N+1 using SQL copied from the generated schema JSON, and add migration tests proving existing accounts, transactions, debts, settings and balances are unchanged. Never use destructive migration.

Keep the app 100% offline: do not add the INTERNET permission, analytics, ads, cloud SDKs, remote assets, or SMS/notification-reading permissions. Bank-message import is paste/share-sheet only. Backups are user-controlled, encrypted by default, verified after writing, and restored via a validated preview-and-confirm flow in one transaction.

Use only real database-backed data: no seeded transactions, fake balances, fabricated chart values, dummy buttons, stubs, TODO bodies, or false success messages. If a requirement is unsafe, conflicts with the repo, needs an unavailable API, or cannot be completed here, do not fake it — explain it in brain.md, propose the safest alternative, and continue with the rest.

After each phase: run the real Gradle test/lint/build tasks available in your environment, inspect the diff, fix regressions, update brain.md with only what was actually done and actually run, and make a descriptive Git commit. Continue to the next phase when the gate in Section 18 passes; stop only for a genuine blocker. Finish with README updates and a concise final report: completed, partial, blocked, not started, files changed, migration details, test/build results, known limitations, and manual steps for me.

Begin now with Phase 0. Do not skip the audit.
```

---

## 20. Final Product Rule

A beautiful screen with a wrong balance is a failed implementation. A reminder that posts a payment without confirmation is a failed implementation. A backup that can't be restored is a failed implementation. When in doubt, choose the behaviour that is **transparent, reversible, testable, and under the user's control**.
