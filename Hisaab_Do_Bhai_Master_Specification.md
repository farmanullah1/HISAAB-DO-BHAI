# Hisaab Do Bhai (حساب دو بھائی)
## Product, Design, Architecture & Implementation Master Specification

**Document type:** Implementation source of truth for Google AI Studio  
**Version:** 3.0  
**Status:** Proposed implementation specification — inspect the existing repository before changing code  
**Platform:** Native Android  
**Preferred stack:** Kotlin, Jetpack Compose, Material 3, Room, Kotlin Coroutines/Flow, WorkManager where appropriate  
**Default locale:** English, with Urdu-ready architecture and right-to-left support  
**Currency:** Pakistani Rupees (PKR), stored as integer paisa  
**Privacy model:** Offline-first, local-only, no account/login, no cloud backend  
**Design direction:** Warm, tactile clay-inspired UI, restrained and accessible rather than decorative or skeuomorphic to the point of harming usability

---

# 0. Binding Instructions for Google AI Studio

Treat this document as the **single source of truth for the requested product direction**, but do not assume every described capability already exists in the repository. First inspect the actual project, identify what is implemented, and then implement the specification incrementally without destroying working functionality or existing user data.

## 0.1 Required workflow

1. **Inspect before editing.** Read the project structure, Gradle files, manifest, navigation, Compose screens, Room entities/DAOs/database version, repositories, ViewModels, resources, tests, and current Git status.
2. **Create an implementation audit.** For every requirement, mark it `Implemented`, `Partially implemented`, `Missing`, `Conflicting`, or `Needs decision`, citing the relevant file/class where possible.
3. **Resolve conflicts safely.** Prefer the existing working implementation when the spec does not require changing it. If this document conflicts with the current project or a platform limitation, explain the conflict and choose the safest compatible approach.
4. **Plan database changes before coding.** Never delete, recreate, or destructively reset the user's database to make a migration easier.
5. **Implement in small, testable phases.** Complete a phase, run the relevant available tests/build checks, inspect the diff, and fix regressions before moving on.
6. **Use real functionality only.** Do not add dummy transactions, fake balances, fabricated chart data, simulated successful backups, placeholder buttons, fake notifications, or claims that a feature works when it does not.
7. **Keep a work log.** Maintain `brain.md` at the project root. Record the audit, decisions, changed files, commands/tests actually run, known limitations, and next steps. Do not claim commands or tests were run unless they were.
8. **Use Git safely.** Inspect `git status` first. Never overwrite or discard the user's uncommitted changes. After a coherent implementation phase passes checks, create a descriptive Git commit if Git is available and the user/project workflow permits it. Never commit secrets or personal financial data.
9. **Report honestly.** Finish with implemented features, files changed, migration details, tests/build results, remaining work, and any manual steps required.

## 0.2 Non-negotiable rules

- No generated filler, fake data, demo records in the real database, or hard-coded financial totals.
- No network/cloud dependency for core app functionality.
- Do not add `android.permission.INTERNET` unless the owner explicitly changes the offline-only requirement and approves the implications. The default is **no Internet permission**.
- No login, signup, account creation, analytics SDK, advertising SDK, remote telemetry, or hidden data collection.
- Do not read SMS automatically by default. SMS access is sensitive and may be restricted by Google Play policy. Prefer manual paste/import of a bank notification. If a future implementation proposes SMS permissions, stop and document the privacy, platform, and distribution implications before implementing.
- Do not record a recurring payment as paid without the user's explicit confirmation.
- Do not silently infer a financial transaction from an SMS, receipt, reminder, or calendar event.
- Do not treat an item or service received on credit as cash income or increase a cash/bank balance unless actual money entered an account.
- No destructive database migration, backup overwrite, or transaction deletion without a clear, user-confirmed path.
- Buttons must work, be disabled when unavailable, or be clearly presented as unavailable with an explanation. Never leave deceptive UI.
- Do not make religious, legal, tax, or investment conclusions appear authoritative. Zakat/Nisab results must be configurable estimates with transparent assumptions and a user verification notice.

---

# 1. Product Vision and Audience

Hisaab Do Bhai is a private personal bookkeeping app for people who want to track everyday spending, income, accounts, money owed, money lent, item-based obligations, informal committee savings, recurring bills, charity, and financial due dates — especially in a Pakistani context.

The app should feel calm, fast, trustworthy, and understandable to a non-accountant. The user should be able to record a routine expense in a few seconds, inspect where a balance came from, and recover their data without relying on an online account.

## 1.1 Product principles

1. **Accuracy before decoration:** financial totals and transaction history must be correct before visual polish.
2. **User intent before automation:** automation can prepare a suggestion, but sensitive financial actions require confirmation.
3. **Local ownership:** the user's financial records stay on the device unless the user explicitly exports or shares a file.
4. **Explainable balances:** every displayed total should be traceable to real records and a clear formula.
5. **No hidden accounting:** transfers, debt creation, repayments, committee payments, and reversals must have explicit ledger effects.
6. **Accessible by default:** readable contrast, scalable text, screen-reader labels, touch targets, and reduced-motion support.
7. **Culturally relevant, not culturally stereotyped:** useful PKR formatting and locally familiar categories without cluttering every screen with decorative cultural motifs.
8. **Progressive disclosure:** simple actions first; advanced details remain available in transaction details or settings.
9. **Honest empty states:** show how to start when there are no records; never populate a new installation with sample financial activity.
10. **No false security promises:** explain what biometric locking, encryption, backups, and screenshot blocking do and do not protect.

## 1.2 Success criteria

The implementation is successful when:

- A new installation opens with a useful empty dashboard and no invented transactions.
- Users can create multiple accounts/wallets and record income, expenses, and transfers.
- Account balances and the dashboard reconcile with the ledger.
- Money debts and item/service obligations are represented separately and correctly.
- Partial and full repayments update the relevant debt and account records atomically.
- Recurring transactions generate due reminders/suggestions but are not recorded as paid without confirmation.
- Local backup/restore is testable and reports success only after verifying the result.
- Core features work without network access.
- Existing user data survives every supported database migration.
- Financial calculations use integer paisa and have unit tests for edge cases.
- Every important action has loading, success, failure, empty, and confirmation states as appropriate.

---

# 2. Repository Audit and Scope Control

Before implementation, inspect the existing project and produce a compact audit table:

| Area | Inspect | Required output |
|---|---|---|
| Platform/build | Gradle, Kotlin, Compose, SDK levels | Actual versions and build health |
| Navigation | Routes, back stack, deep links | Existing navigation map |
| Data | Room entities, DAOs, migrations | Schema diagram and current version |
| Finance logic | Repositories, use cases, ViewModels | Current source of balance calculations |
| UI | Screens, design tokens, reusable components | Screen inventory and visual inconsistencies |
| Security | Manifest permissions, backup rules, lock flow | Permission/security gap list |
| Storage | Receipt paths, export/backup paths | File lifecycle and deletion behavior |
| Tests | Unit, DAO, migration, UI tests | Existing coverage and missing tests |
| Build/run | Current build/test commands | Commands actually verified |
| Git | Current branch/status/diff | Changes that must be preserved |

Do not replace the app wholesale merely because a rewrite looks simpler. Prefer targeted refactoring and backward-compatible improvements.

## 2.1 In-scope features

- Dashboard and transaction ledger
- Multiple accounts/wallets
- Income, expense, transfers, categories, notes, receipt photos
- Karz: money lent, money borrowed, item/service obligations, partial/full settlement
- Recurring transaction reminders with explicit confirmation
- Search, filters, transaction detail, edit, archive/delete with safeguards
- Kameti/committee tracker
- Zakat estimate and charity ledger
- Installment plans and local dues calendar
- CSV export, PDF/share documents where implemented safely
- User-controlled local backups and restore
- App lock with Android biometric support and secure fallback
- Adaptive layouts for phones, tablets, and foldables
- Accessibility, localization readiness, automated tests

## 2.2 Out of scope unless explicitly approved

- Cloud synchronization, remote account, online dashboard
- Bank login or direct bank integration
- Automatic SMS reading or notification scraping
- AI-generated financial advice or automatic transaction posting
- Cryptocurrency, trading, investment advice, or tax filing
- Fake sample dashboards presented as real user data
- Automatic money movement or payment execution
- Background actions that change financial records without user confirmation

---

# 3. Information Architecture and Navigation

Keep navigation predictable. Avoid cramming every advanced feature into the primary navigation.

## 3.1 Primary destinations

**Phone / compact width**
- Home
- Transactions
- Karz
- Plans
- Settings

“Plans” can contain Kameti, installments, recurring bills, and the dues calendar. Charity/Zakat may be reached from a clearly labeled “Tools” section on Home or from Plans, depending on the existing navigation. Do not use an unclear label such as “Needs” unless the app already has a defined meaning for it.

**Tablet / expanded width**
- Use a navigation rail or adaptive navigation drawer.
- Use a master-detail layout where it genuinely improves navigation: list on the left, selected record detail on the right.
- Do not force a two-pane layout on small or medium windows.
- Use current supported Compose Material 3 adaptive APIs if available in the project's dependency versions; do not invent APIs.

## 3.2 Dashboard content order

1. Greeting/date area with a clear app title.
2. Total Balance across selected accounts, with a hide/show privacy control.
3. Remaining Balance only if its meaning is clearly defined by the app's accounting model. Prefer “Available after planned expenses” or a similarly explicit label if it subtracts upcoming commitments. Never present two ambiguous balance cards.
4. Account/wallet selector or compact account summary.
5. Primary quick actions: Add Expense, Add Income, Transfer, Add Karz.
6. Monthly income/expense summary with selectable period.
7. Upcoming dues and pending recurring transactions.
8. Five most recent transactions, with “View all”.
9. Optional category chart only when real records exist and the chart meaning is clear.

All totals must be derived from the database. If there are no records, display a zero/empty state with an action to add the first account or transaction. Never add demo activity.

## 3.3 Empty, loading, and error states

Every major screen needs:
- A first-use empty state with a useful next action.
- A loading state only where actual asynchronous work occurs.
- A clear error state with retry or recovery guidance.
- A success state after a database/file operation really succeeds.
- A distinction between “no records” and “failed to load records”.

---

# 4. Design System: Tactile Clay, Refined

Use a warm clay-inspired design, but keep it subtle. Clay surfaces should help establish hierarchy rather than make every control look inflated.

## 4.1 Visual direction

- Background: warm cream near `#F1EBDF`.
- Main text: deep charcoal/brown with accessible contrast.
- Primary accent: restrained forest/mint green for positive actions and healthy balances.
- Secondary accent: muted terracotta for spending and attention.
- Warning/error: semantic amber/red, never color alone.
- Surfaces: warm off-white, slightly elevated cards, restrained shadows and highlights.
- Use gradients sparingly; avoid excessive glassmorphism, neon gradients, random glow, excessive rounded cards, and decorative blobs.
- Do not use emojis as the only iconography for important actions.
- Use consistent vector icons with text labels for critical finance actions.
- Use the rupee sign `₨` or a consistent PKR formatter. Avoid mixing `Rs`, `PKR`, and `₨` randomly.

## 4.2 Design tokens

Create a central theme/token layer rather than scattering hex values through composables.

Define tokens for:
- `colorBackground`, `colorSurface`, `colorSurfaceRaised`
- `colorTextPrimary`, `colorTextSecondary`, `colorOutline`
- `colorIncome`, `colorExpense`, `colorTransfer`, `colorDebt`, `colorWarning`, `colorError`
- spacing scale (for example 4, 8, 12, 16, 24, 32 dp)
- corner radii (small controls, cards, large sheets)
- elevations/shadows
- typography styles
- minimum touch target sizes
- animation durations and reduced-motion alternatives

The exact token values may be tuned after inspecting the existing app, but maintain consistent semantics.

## 4.3 Motion and tactile feedback

- Keep transitions short and purposeful.
- Use animation to show state changes, expand details, or confirm an action — not to delay common workflows.
- Respect Android's system animation/reduced-motion settings where possible.
- Haptics and sound are optional enhancements; default to subtle haptics and no unsolicited sounds.
- Provide a setting to disable haptics/sounds.
- Do not play a sound for every tap.
- Never use animation as the only indication that a save succeeded.

## 4.4 Accessibility

- Support font scaling and avoid fixed heights that clip large text.
- Provide meaningful content descriptions for icons and charts.
- Ensure touch targets are approximately 48 dp or larger where practical.
- Keep text/background contrast accessible.
- Do not encode income/expense/debt solely by color; include labels, signs, and icons.
- Ensure keyboard navigation and focus order are sensible on large screens.
- Test TalkBack labels and modal focus behavior.
- Charts must have an accessible textual summary or equivalent data list.

## 4.5 App icon

Use a clean, recognizable clay-inspired booklet/ledger motif with a rupee mark. Avoid tiny detail that disappears at launcher size. Create proper Android adaptive icon foreground/background assets and verify safe zones. Do not claim a biometric shield is a security certification.

---

# 5. Core Finance Model and Accounting Rules

This section is critical. UI and database changes must preserve these rules.

## 5.1 Currency and arithmetic

- Store all monetary amounts as integer paisa (`Long`).
- One rupee equals 100 paisa.
- Never use `Float` or `Double` for stored monetary arithmetic.
- Parse user input with strict validation; reject malformed, negative where not allowed, overflow, NaN-like, or unsupported precision values.
- Format amounts for display using locale-aware formatting with PKR and at most two decimal places when needed.
- Define rounding rules explicitly for percentage calculations and installment splits.
- Add boundary tests for zero, maximum supported values, and fractional-rupee input.

## 5.2 Accounts and balances

An account can represent Cash, Bank, Savings, or a custom wallet. “Cash” and “wallet” should not create duplicate concepts if the existing product model treats them as one unified cash account.

Each account should have:
- Stable ID
- Name
- Account type
- Opening balance in paisa
- Created/updated timestamps
- Optional color/icon
- Active/archived state
- Optional notes

Balance must be calculated from opening balance plus ledger effects, or maintained through a rigorously tested ledger invariant. Do not mix two conflicting sources of truth.

Transfers between accounts:
- Decrease the source account and increase the destination account by the same amount.
- Do not count as income or expense in overall cash-flow summaries.
- Be saved atomically as a transfer record or linked debit/credit pair.
- Preserve a shared transfer ID so both sides can be audited.
- Never allow a transfer to create or destroy money because one side failed to save.

## 5.3 Transaction types

At minimum:
- Income
- Expense
- Transfer
- Debt disbursement/receipt and repayment/collection, linked to a debt record
- Committee installment/payout
- Charity payment
- Reversal/adjustment where required

A transaction should support:
- ID
- Type
- Amount in paisa
- Account ID where relevant
- Category ID
- Title/description
- Transaction date/time
- Created/updated timestamps
- Optional note
- Optional person/payee/payer
- Optional receipt reference
- Optional debt/committee/installment link
- Status where needed
- Soft-delete/archive/reversal metadata where appropriate

Use database foreign keys and indexes thoughtfully. Add constraints and validation in the domain layer; database constraints alone are not sufficient.

## 5.4 Remaining Balance definition

Do not calculate “Remaining Balance” by subtracting expenses twice or mixing future commitments with actual cash.

Use clearly named metrics:
- **Current balance:** sum of actual account balances included in the selected scope.
- **Period income:** confirmed income during the chosen period.
- **Period expenses:** confirmed expenses during the chosen period.
- **Net cash flow:** period income minus period expenses, excluding transfers.
- **Committed amount:** unpaid scheduled obligations the user has chosen to include.
- **Estimated available after commitments:** current balance minus selected upcoming commitments, clearly labeled as an estimate.

If the existing app's “Remaining Balance” has a different definition, document it and migrate the label/logic carefully rather than silently changing user expectations.

## 5.5 Editing, deleting, and auditability

- Editing a transaction must recalculate all dependent summaries.
- Deleting a linked debt repayment, committee payment, or transfer must not leave orphaned balances.
- Prefer reversible void/reversal records for financial actions that already affected other records.
- For a simple transaction, offer a clear confirmation and explain the effect.
- Keep a useful local audit trail for settlement adjustments and important financial record changes.
- Avoid storing unnecessary personal data in audit logs.

---

# 6. Karz: Money and Non-Money Obligations

Karz must clearly distinguish money that changes account balances from items/services owed.

## 6.1 Debt direction

Support:
- **I gave Karz** — another person owes the user.
- **I took Karz** — the user owes another person.

Each debt record should include person/name, direction, creation date, due date (optional), note, status, and linked transactions/settlements.

## 6.2 Obligation kind

Provide an explicit kind:
1. **Money**
2. **Item or service**

For item/service obligations, capture a description (for example, a meal, groceries, borrowed item, or service), quantity/unit where useful, and an optional estimated value for reference. The estimated value must not be treated as money received or as an account balance change.

Examples:
- User borrowed ₨5,000: record a liability. If that money was actually received into a selected account, increase that account once through a linked receipt transaction.
- User received food on credit: record an item/service obligation. Do **not** increase cash or bank balance.
- User lent ₨2,000: record a receivable. Decrease the selected account only if the user confirms that actual money was handed over.
- User lent an item: record an item/service receivable without reducing a cash balance unless a real cash payment occurred.

## 6.3 Settlements and repayment

- Support partial and full settlements.
- For money debts, ask how much was settled and which account was used/received, when relevant.
- A full-settlement action should be clearly labeled, such as **Mark as fully repaid** or **Mark as fully collected**, after showing the remaining amount.
- Never mark an obligation paid without user confirmation.
- For item/service obligations, settlement can be marked complete without a fake cash transaction. If money compensation is involved, record it as a separate, explicit money settlement.
- Prevent overpayment beyond the outstanding balance unless the user intentionally records an adjustment with a clear explanation.
- Record settlement date, amount/type, optional note, and linked transaction ID.
- Recalculate outstanding amounts from settlement records or enforce a tested invariant.
- Make direction and status understandable in list rows: “You owe” versus “Owes you”.

## 6.4 Debt detail screen

Show:
- Person and debt direction
- Money versus item/service type
- Original amount or item description
- Settled amount and outstanding balance/status
- Due date and overdue status
- Installment plan if present
- Settlement history
- Linked account transactions, if any
- Actions: record partial payment, mark fully settled, edit, share reminder, archive
- A confirmation step for full settlement and destructive actions

WhatsApp reminders should use Android's share intent with a preview the user can edit. Sharing must be user-initiated; the app must not send messages automatically. Support English and Urdu-ready templates without requiring network permission in Hisaab itself.

---

# 7. Recurring Transactions and Reminders

Recurring entries represent a schedule, not proof that money moved.

## 7.1 Required behavior

- Support frequency options such as weekly, monthly, and yearly where useful.
- Store next due date, schedule, expected amount, account/category defaults, title, and active/paused state.
- At due time, show a local notification or in-app reminder only if notification permission and scheduling are supported and the user has enabled reminders.
- A due reminder may offer **Record payment**, **Skip this occurrence**, or **Remind me later**.
- The app must not create a completed expense or change balances until the user confirms **Record payment**.
- If a reminder is missed while the device is off, recover gracefully without creating duplicate payments.
- Store an occurrence identifier so one scheduled occurrence cannot be recorded twice.
- Editing a recurring template should ask whether changes apply to future occurrences only or also to a specific pending occurrence.
- Pausing or deleting a schedule must not delete already-recorded transactions.

## 7.2 Notifications

- Ask for notification permission only when the user enables reminders and the platform requires it.
- Notification content should avoid exposing sensitive amounts/person names on a locked screen unless the user opts in.
- Tapping a notification must open the relevant record safely.
- Do not claim exact delivery time when Android battery optimization can delay background work.
- WorkManager is suitable for deferrable work, not exact alarms. Do not request exact-alarm permission without a justified product need.

---

# 8. Kameti / Committee Savings Tracker

The Kameti tracker records an informal rotating savings arrangement. It must distinguish the user's contributions from a payout received.

## 8.1 Data and workflow

A committee can include:
- Name
- Total members
- Contribution amount and cadence
- Start date/month
- Expected number of installments
- User's expected payout order/date/month
- Payout amount if known
- Notes
- Active/completed/paused state

Each contribution should record amount, date, status, and optional linked account transaction. Avoid assuming every month has the same calendar-day due date.

## 8.2 Accounting behavior

- Recording a contribution should create an expense/outflow linked to the selected account only after user confirmation.
- Receiving a payout should create an inflow to the selected account only after user confirmation.
- Do not count a payout as ordinary earned income in reports by default; categorize it as a committee payout/transfer-like inflow and show the accounting treatment transparently.
- Avoid double counting contributions, payout, or the committee pool as personal wealth.
- Support missed, late, partially paid, and corrected contributions.
- Do not automatically mark a contribution paid just because its date has arrived.
- The “Claim payout” action must show the expected amount and target account before confirming.
- A payout is not verified merely because the expected payout date arrived.

## 8.3 Committee overview

Show active committees, next due contribution, total contributions recorded, payout schedule, and status. Label “recorded contributions” separately from “expected contributions”. Do not call expected values savings already held by the user.

---

# 9. Zakat, Nisab, Sadqah and Charity

This module is a configurable recordkeeping and estimation tool, not a religious ruling.

## 9.1 Transparent calculation

- Let the user choose the Nisab standard used (gold or silver) and enter/update local rates manually.
- Store the rate, date, unit, standard, excluded assets/liabilities, eligible asset categories, and assumptions used for each saved calculation.
- Do not call rates “real-time” when no network connection or data source exists.
- Show the source as **User-entered rate** unless a future, explicitly approved source is implemented.
- Make the commonly referenced weights configurable and explain that standards/calculations can vary; the app must not present a single method as universally authoritative.
- Use integer paisa for money arithmetic and define rounding rules.
- Display the calculation breakdown so the user can verify every included value.

## 9.2 Eligibility and assumptions

The suggested baseline may consider selected cash/bank balances and eligible receivables minus selected short-term liabilities, but:
- Let users include/exclude categories explicitly.
- Do not automatically assume every receivable will be collected.
- Do not subtract all liabilities indiscriminately; let the user choose applicable liabilities and document the method.
- Do not treat item/service debt as cash.
- Do not count the same asset in multiple categories.
- Do not infer gold/silver holdings if the user has not entered them.
- Do not imply the app determines religious eligibility, nisab date, hawl, or special cases without user verification.

Display a clear notice: **“This is an estimate based on the method and values you selected. Please verify the calculation with a trusted qualified scholar or adviser for your circumstances.”**

## 9.3 Charity ledger

Support categories such as Zakat, Sadqah, Fitrana, Khairat, and custom categories. A confirmed payment may create an expense in a selected account, linked to the charity record. A planned charity entry must not change balances until paid/confirmed.

Saved calculation records should preserve the values and assumptions used at the time, rather than silently changing historical results when the user edits current rates.

---

# 10. Installment Plans and Financial Dues Calendar

## 10.1 Installment plans

- Support weekly or monthly schedules where appropriate.
- Store installment number, due date, expected amount, status, actual paid amount/date, and linked settlement/transaction.
- Make rounding distribution deterministic so installment amounts sum exactly to the principal where the plan is intended to do so.
- Support partial payment, missed installment, rescheduling, and early settlement.
- Do not create payments automatically when a due date arrives.
- Clearly distinguish scheduled amount from amount actually paid.
- Do not assume an interest rate or add fees unless the user explicitly enters them.

## 10.2 Calendar

Show relevant due items:
- Karz payable due dates
- Karz receivable expected dates
- Recurring bill occurrences
- Kameti contribution dates and expected payout dates
- Installment due dates
- Optional user-created reminders

Use both color and text/icon labels. Tapping a date opens an agenda with the underlying records and safe actions. “Skip” should affect only the relevant scheduled occurrence and require confirmation where it changes a plan.

Use the device's local timezone and robust date handling. Avoid storing ambiguous date-only values as UTC timestamps if that can shift a due date across calendar days. Define how dates are stored and tested.

---

# 11. Receipts, Documents, Sharing and Exports

## 11.1 Receipt photos

- Let the user attach a photo using Android's photo picker or another least-privilege system picker.
- Request broad storage access only if absolutely required; prefer scoped pickers and app-private storage.
- Store an internal reference, not a fragile absolute path.
- Handle missing, moved, or deleted receipt files gracefully.
- Allow replacing/removing a receipt with confirmation.
- Avoid placing receipt images in public folders automatically.
- Consider image size limits and compression, but preserve legibility.
- Exclude receipt files from system/cloud backup if the app's backup policy cannot protect them; document the chosen behavior.

## 11.2 CSV export

Provide export of:
- Filtered transactions
- Monthly summaries
- Account ledger
- Karz ledger and settlements
- Kameti contributions/payouts
- Charity records

Requirements:
- Use Android Storage Access Framework or a safe share flow.
- Let the user review the export scope/date range before export.
- Use stable headers and ISO-like dates.
- Correctly escape commas, quotes, and newlines.
- Prevent spreadsheet formula injection for untrusted values beginning with formula characters such as `=`, `+`, `-`, or `@` where appropriate.
- Include currency and amount units clearly.
- Do not silently export unrelated records.
- Report success only after the file has been written and closed successfully.

## 11.3 PDF/share output

- Generate PDFs on-device if the feature exists.
- Use vector/text output for crisp printing and accessible document structure where possible.
- Preview the document before sharing.
- Share through Android's chooser and temporary content URI grants.
- Use FileProvider or a suitable content URI implementation; never expose private filesystem paths.
- Delete temporary share files when safe.
- Never claim that a WhatsApp message was delivered; Hisaab only opens the share target and the user completes sending.

## 11.4 Local backup and restore

- Backups are user-controlled and local. No cloud sync is implied.
- Prefer an encrypted backup format by default. If encryption is implemented, use a well-reviewed platform cryptographic API, a user passphrase or secure key strategy, authenticated encryption, and a versioned format. Never invent custom cryptography.
- If plain JSON export is offered, label it clearly as unencrypted and require explicit confirmation.
- Use SAF to let the user select a destination folder/file. Do not assume a hard-coded Documents path is writable.
- Scheduled backup to a user-selected folder must be opt-in and must handle revoked URI permissions, unavailable removable storage, low disk space, and device restart.
- Do not overwrite the only existing backup silently. Use timestamped names or a confirmation/retention policy.
- Include schema version, app version, creation timestamp, and integrity checks in the backup manifest.
- Do not include secrets or unnecessary device identifiers.
- Restore must validate the file, version, integrity, and schema before changing current data.
- Show a restore preview (record counts and conflicts) and require confirmation.
- Use a transaction/staging strategy so failed restore does not leave a half-restored database.
- Clearly explain whether restore replaces current data, merges it, or creates a separate copy. Default to a safe, explicit choice.
- Test backup and restore with a real temporary database; do not fake success.

---

# 12. Privacy, App Lock and Security

## 12.1 Offline guarantee

- Core screens, calculations, Room access, receipt viewing, local reminders, exports, and backups must work without network connectivity.
- By default, the manifest must not request `android.permission.INTERNET`.
- Inspect transitive dependencies and SDKs for network-related behavior; do not include analytics, ads, crash reporting uploads, remote fonts, or remote APIs.
- Do not confuse “no Internet permission” with complete device security. The OS, rooted devices, backups, and user-shared exports are separate considerations.
- Avoid unnecessary permissions. Document every permission and the feature that needs it.

## 12.2 App lock

- Use AndroidX Biometric / `BiometricPrompt` where compatible with the existing stack.
- Biometrics should use the system prompt; do not build a fake fingerprint scanner UI.
- If a PIN fallback exists, store a salted, suitably derived verifier rather than a plain-text PIN. Use platform-supported cryptographic primitives and rate-limit failed attempts.
- A 4-digit PIN is convenient but weak; explain the tradeoff and consider a longer PIN/passcode option.
- Do not store biometric templates.
- Auto-lock after a configurable short background interval, default 15 seconds if consistent with the current product.
- Ensure lock state cannot be bypassed by opening a deep link, notification, app shortcut, or returning from another activity.
- Avoid leaking private amounts in recent-app previews. `FLAG_SECURE` can be offered as a privacy setting or applied to sensitive screens, but explain that it does not prevent every possible capture method.
- Avoid collecting PINs or financial details in logs.

## 12.3 Data protection

- Review Android Auto Backup and device-transfer rules. Because the app is local-only, decide explicitly whether the Room database and receipt files are excluded from system backup to avoid an unexpected cloud copy. Explain the choice in settings/help.
- Use app-private storage for internal data.
- Do not log full transaction descriptions, person names, balances, receipt paths, PINs, or backup passphrases.
- Avoid clipboard use for sensitive data except after a user action.
- Require user confirmation before destructive reset or all-data deletion.
- Provide a clear “Export my data” and “Delete all local data” flow with warnings and confirmation.
- No hidden telemetry or analytics.

---

# 13. Local SMS Import: Safe Alternative First

Automatic SMS scanning is not part of the default scope. SMS contains highly sensitive information and SMS permissions can trigger store-policy and privacy obligations.

Preferred workflow:
1. User copies a bank/fintech notification and opens **Import bank message**.
2. The app parses the pasted text entirely on-device.
3. The app shows extracted amount, merchant, date, account/source, and confidence/unknown fields.
4. User edits the draft and confirms it.
5. Only after confirmation is a transaction saved.
6. Duplicate detection checks a stable fingerprint of normalized message fields and allows the user to resolve uncertain matches.

Support provider-specific parsing only with tests using synthetic fixtures that contain no real personal data. Do not add fabricated examples to the production database. If automatic SMS access is later requested, stop and present a separate privacy/platform review before implementation.

---

# 14. Database and Domain Architecture

## 14.1 Architecture principles

Use a maintainable, testable structure compatible with the current repository. Prefer:
- Compose UI
- ViewModels with state exposed through `StateFlow`
- Repositories as the boundary between UI and Room
- Domain/use-case classes for nontrivial finance operations
- Room entities/DAOs for persistence
- Kotlin Coroutines/Flow for asynchronous operations
- WorkManager for deferrable reminders/backups only where justified
- Dependency injection only if already present or it materially improves the project; do not add a complex framework for its own sake

The UI must not directly execute multi-step accounting operations against multiple DAOs. Put atomic finance operations in a repository/use case using Room transactions.

## 14.2 Data entities to audit/add as needed

Names are suggestions; adapt to the existing schema rather than creating duplicate entities.

- `AccountEntity`
- `CategoryEntity`
- `TransactionEntity`
- `TransferEntity` or explicit linked transfer metadata
- `DebtEntity`
- `DebtSettlementEntity`
- `DebtInstallmentEntity`
- `RecurringRuleEntity`
- `RecurringOccurrenceEntity`
- `KametiEntity`
- `KametiPaymentEntity`
- `CharityRecordEntity`
- `ZakatCalculationEntity`
- `ReminderEntity` if needed
- Receipt metadata or receipt URI references
- Settings/preferences model

Each entity should define:
- Stable primary key
- Required fields and sensible defaults
- Foreign keys where appropriate
- Indexes for common filters and joins
- Creation/update timestamps where useful
- Archive/delete/reversal semantics
- Migration implications

Avoid storing derived values as independent sources of truth unless there is a documented consistency strategy.

## 14.3 Migration safety

The supplied project document mentions Room database migration from version 3 to 4. Treat that version number as a claim to verify against the actual repository. Do not assume version 3 is current.

Before changing the schema:
1. Read the real database version and existing migration chain.
2. Write a migration from the actual current version to the next version.
3. Preserve all existing accounts, transactions, debts, categories, settings, and receipt references.
4. Backfill new fields with safe, documented defaults.
5. Add migration tests that create an old-version database, insert representative records, migrate, and verify the result.
6. Test foreign keys, indexes, and nullability after migration.
7. Do not use destructive migration fallbacks in production.
8. If a migration cannot be made safe, stop and report the blocker instead of deleting user data.

## 14.4 Suggested domain operations

Implement and test operations such as:
- `createIncome(...)`
- `createExpense(...)`
- `transferBetweenAccounts(...)`
- `createDebt(...)`
- `recordDebtSettlement(...)`
- `settleDebtInFull(...)`
- `recordRecurringOccurrence(...)`
- `recordKametiContribution(...)`
- `recordKametiPayout(...)`
- `recordCharityPayment(...)`
- `exportLedger(...)`
- `validateAndRestoreBackup(...)`

These names are illustrative. Reuse the repository's naming conventions where possible.

---

# 15. Dashboard Analytics and Charts

- All charts must be based on real database query results for the selected date range.
- Never generate placeholder chart values to make an empty screen look populated.
- When no data exists, show a helpful empty state instead of an empty-looking chart with fabricated bars.
- Make chart period and filters visible.
- Exclude transfers from income/expense totals unless a chart explicitly represents account movement.
- Keep refunds/reversals consistent and documented.
- Use accessible summaries such as “Expenses this month: ₨X; largest category: Y”.
- A donut chart must show category names/values on tap and have a textual equivalent.
- A monthly bar chart must label whether it compares income, expenses, or net cash flow.
- A debt/liquidity gauge must not imply investment or credit advice. Show the formula and inputs behind the indicator.

---

# 16. Search, Filters and Transaction Details

## 16.1 Search and filtering

Support useful filters:
- Date range
- Account
- Transaction type
- Category
- Amount range
- Person/payee
- Debt/committee link
- Pending/confirmed status where relevant

Search should be local, responsive, and not send queries anywhere. Preserve filters when returning from details where practical.

## 16.2 Transaction detail

Show:
- Full amount and type
- Date/time
- Account
- Category
- Title/description and note
- Receipt if attached
- Linked debt/committee/recurring record
- Created/updated details where useful
- Edit, share/export, archive/delete/reverse actions appropriate to that record

Every list item that opens details should have a visible and accessible way to view details. Do not rely on a hidden gesture alone.

---

# 17. Settings and User Control

Include clear settings for:
- Currency display (default PKR; do not change stored amounts when display format changes)
- Language (English now, Urdu-ready; only expose Urdu selection when translations are sufficiently complete)
- Theme (system/light/dark if all screens support it)
- Biometric/PIN lock and timeout
- Privacy options for previews and notification content
- Haptics
- Reminders
- Backup destination/schedule/encryption
- Export data
- Manage categories and accounts
- About, privacy explanation, permissions, and app version
- Delete all local data with explicit confirmation

Settings should accurately describe actual behavior. Do not display toggles for unimplemented features.

---

# 18. Performance, Reliability and Offline Behavior

- Avoid blocking the main thread with database queries, PDF generation, image processing, or backup serialization.
- Use paging or bounded queries for large transaction histories where needed.
- Use stable Compose keys for lists.
- Avoid unnecessary recomposition and expensive work inside composable functions.
- Handle empty database, large database, disk full, corrupted backup, missing receipt, revoked URI permission, and process death.
- Make save operations idempotent where possible to prevent duplicate records on repeated taps.
- Disable duplicate submit actions while a save is in progress.
- Use explicit error handling; do not swallow exceptions or show success after failure.
- Ensure the app remains useful in airplane mode and after a device restart.
- Avoid overengineering premature caching or introducing extra libraries without need.

---

# 19. Testing and Acceptance Criteria

Implement tests that fit the current project and actually run them.

## 19.1 Unit tests

- PKR/paisa parsing and formatting
- Amount validation and overflow boundaries
- Balance calculations
- Period totals and transfer exclusion
- Debt outstanding balance
- Money debt versus item/service obligation effects
- Partial/full settlement
- Installment rounding and schedule dates
- Recurring occurrence duplicate prevention
- Kameti contribution and payout accounting
- Zakat estimate formulas and rounding
- CSV escaping and formula-injection handling
- Backup schema/integrity validation

## 19.2 Room/DAO tests

- Insert/update/delete and foreign-key behavior
- Account/transaction relationships
- Atomic transfer operation
- Atomic debt settlement
- Migration from every supported old schema version
- Migration preserves existing data
- Queries return correct results for empty and populated databases

## 19.3 UI/integration tests

- First launch contains no fake transactions.
- User can create an account and add an expense.
- Dashboard updates from persisted records after app restart.
- A transfer changes account balances but not overall income/expense totals.
- Borrowed money received into an account changes that account once.
- An item/service borrowed on credit does not change cash/bank balances.
- Recurring due reminder does not post a transaction until confirmation.
- Full debt settlement requires confirmation.
- Backup/restore has visible validation and failure handling.
- App lock behaves correctly after backgrounding and returning.
- Core navigation and critical actions work with TalkBack/large text where feasible.

## 19.4 Build and regression checks

- Run the project's actual Gradle test/build tasks after inspection.
- Do not invent task names or claim success without tool output.
- Record build/test output summaries in `brain.md`.
- If the environment cannot run Android builds, state that limitation and provide the exact commands the project owner should run.

## 19.5 Release checklist

- No Internet permission unless explicitly approved.
- No demo/fake financial records.
- No placeholder buttons or fake success messages.
- No destructive migration fallback.
- No secrets in source control.
- No unnecessary permissions.
- App icon and adaptive layouts verified.
- Backup and restore tested.
- Exported files verified.
- Accessibility and text scaling reviewed.
- Privacy disclosures match actual behavior.

---

# 20. Phased Implementation Plan

Do not attempt every feature in one unreviewed giant code generation pass. Follow this order and keep the project buildable after each phase.

## Phase 0 — Audit and baseline
- Inspect repository and Git status.
- Identify actual implemented features, database version, manifest permissions, and tests.
- Build the current project if possible.
- Create/update `brain.md` with audit and decisions.
- Deliver the audit before making risky changes.

## Phase 1 — Accounting correctness
- Verify integer paisa handling.
- Verify balance formula and transfer accounting.
- Verify income/expense summaries exclude transfers.
- Audit debt direction and settlement behavior.
- Add unit tests for core calculations.
- Fix correctness issues before adding visual features.

## Phase 2 — Data integrity and migration
- Design the required Room schema changes.
- Implement safe migrations from the actual current version.
- Add migration tests.
- Confirm existing user data remains intact.

## Phase 3 — Karz and recurring workflows
- Separate money obligations from item/service obligations.
- Implement safe partial/full settlement.
- Ensure item/service credit does not change cash balance.
- Add recurring schedules and confirmation-first payment recording.
- Add tests for duplicate prevention and accounting effects.

## Phase 4 — UI/UX foundation
- Centralize design tokens and reusable components.
- Improve dashboard hierarchy and empty states.
- Add transaction details, clear actions, search, and filters.
- Polish motion/haptics with accessibility controls.
- Check large text and contrast.

## Phase 5 — Backup, restore, and export
- Implement and test user-controlled backup/restore.
- Prefer encrypted backups; document any unencrypted option.
- Implement CSV export with correct escaping and safe filenames.
- Add PDF/share improvements only if dependencies and current code support them.

## Phase 6 — Kameti, installments, calendar, charity
- Implement committee accounting with explicit confirmation.
- Implement installment plans and dues calendar.
- Implement transparent, configurable Zakat estimate and charity ledger.
- Add tests for all financial side effects.

## Phase 7 — Adaptive layouts and optional enhancements
- Add tablet/foldable layouts using supported APIs.
- Add app shortcuts/Quick Settings tile only if each flow can be completed safely.
- Consider manual bank-message import; do not enable automatic SMS reading by default.
- Validate performance and accessibility.

## Phase 8 — Final verification
- Run build and tests.
- Inspect Git diff and changed files.
- Verify no fake data, network permission, accidental data loss, or false success states.
- Update `brain.md`.
- Provide an honest implementation report and next steps.

---

# 21. Required Deliverables

Google AI Studio should produce or update, as appropriate to the existing repository:

1. Working source code for the approved phase(s).
2. Room entities, DAOs, repositories/use cases, and safe migrations where required.
3. Compose screens and reusable UI components.
4. Design tokens/theme and icon assets where needed.
5. Unit, database, migration, and UI tests for critical behavior.
6. Updated manifest and backup/security configuration, with no unnecessary permissions.
7. CSV/backup/restore implementation and validation where included in the completed phase.
8. `brain.md` documenting audit, decisions, changed files, test results, limitations, and next steps.
9. A README update covering setup/build/test, offline/privacy model, backup behavior, and known limitations.
10. A concise final report distinguishing completed, partial, blocked, and not-yet-started features.

Do not create unrelated files or duplicate architecture layers merely to satisfy a checklist. Deliverables must be integrated into the actual project.

---

# 22. Copy/Paste Execution Prompt for Google AI Studio

**Use the following prompt when asking Google AI Studio to act on this specification:**

> Read `Hisaab_Do_Bhai_Master_Specification.md` completely and treat it as the product and engineering source of truth for this project. First inspect the existing repository, current Git changes, architecture, manifest, Room database version/migrations, screens, and tests. Do not assume the document describes features that already exist.
>
> Start with an implementation audit and a phased plan. Preserve existing user data and all uncommitted changes. Do not perform a destructive rewrite. Implement the phases in the order specified, prioritizing accounting correctness, database safety, and the money-versus-item Karz distinction before adding optional features or visual effects.
>
> Follow the design system while keeping the app accessible, polished, responsive, and practical. Use real database-backed data only. Never seed fake transactions or balances, fabricate chart values, add dummy interactions, or show a success message when an operation failed. Keep the core app local-only and offline; do not add Internet permission, cloud services, analytics, ads, or automatic SMS reading.
>
> Recurring payments, Karz settlements, Kameti payments/payouts, charity payments, backup restores, and destructive actions must be confirmed by the user when specified. Item/service obligations must not change account balances unless an actual money movement is explicitly recorded.
>
> Before schema changes, verify the actual Room database version and implement safe, tested migrations from the current schema. Never use destructive migration as a shortcut. Use integer paisa for monetary values. Make multi-record accounting changes atomic and test the accounting effects.
>
> Work in small phases. After each phase, run the relevant tests/build checks available in the environment, inspect the diff, fix regressions, and update `brain.md` with what was actually changed and tested. Never claim that a test, build, backup, restore, export, or feature succeeded unless it really did.
>
> If a requirement is unsafe, conflicts with the repository, requires unavailable APIs, or cannot be completed in the current environment, do not silently fake it. Explain the issue and propose the safest compatible alternative. Finish with a concise report of completed work, files changed, migration safety, tests/build results, known limitations, and the next phase.
>
> Begin with **Phase 0 — Audit and baseline**. Do not skip the audit and do not start by generating a replacement app from scratch.

---

# 23. Final Product Rule

The priority order is:

**Correct accounting → preserve user data → privacy/security → usable workflows → accessibility → visual polish → optional automation.**

A beautiful screen with incorrect balances is a failed implementation. A clever automation that records a payment without the user's confirmation is a failed implementation. A backup feature that cannot reliably restore data is a failed implementation. When in doubt, choose the behavior that is transparent, reversible, testable, and under the user's control.
