# Hisaab Do Bhai --- Complete Android App Implementation & Polish Prompt

**Document purpose:** Ready-to-use implementation prompt for Google AI
Studio or an AI coding agent.\
**Product:** Hisaab Do Bhai\
**Platform:** Native Android\
**Currency:** Pakistani Rupees (PKR)\
**Storage:** Local-only, offline-first\
**Authentication:** No login\
**Design reference files:** `clay.html` and `clay-design.md`\
**Priority:** Financial correctness, excellent UX, real functionality,
maintainable code, and GitHub-ready documentation.

------------------------------------------------------------------------

# 1. Role and Mission

Act as a senior Android engineer, product designer, UX researcher,
accessibility specialist, financial-domain engineer, QA engineer, and
technical writer.

Build and refine **Hisaab Do Bhai**, a polished, trustworthy,
offline-first personal finance app for Android. The result must feel
like a carefully designed, production-minded product---not a generic
AI-generated template, a static mockup, or a demo populated with
fabricated data.

Inspect the existing repository before changing anything. Understand its
architecture, build configuration, database, navigation, screens,
assets, tests, and current behavior. Preserve working functionality and
make focused improvements. Do not replace the entire app simply because
a new implementation is easier to generate.

Every displayed balance, form, button, navigation destination, filter,
chart, debt action, reminder, export operation, and setting must be
connected to real functionality. Never claim a feature works unless it
is implemented and verified.

## Product principles

1.  Financial correctness comes before decorative design.
2.  All core features must work offline.
3.  No login, registration, remote database, cloud dependency, or
    required internet connection.
4.  No fake transactions, demo balances, dummy users, fabricated
    reports, or randomly generated content.
5.  Do not silently change or delete a user's financial records.
6.  Keep the app simple for a non-expert while allowing useful detail
    when needed.
7.  Use clear language and explain financial effects before
    consequential actions.
8.  Preserve existing user data during upgrades and migrations.
9.  Build a consistent, accessible, premium visual system from the
    supplied design references.
10. Do not automatically launch the app, emulator, or device. Wait for
    my explicit approval before running or launching the completed app.

------------------------------------------------------------------------

# 2. Inspect the Existing Project and Design References First

Before implementation:

-   Inspect the complete repository and identify the actual Android
    framework, language, build system, package name, app entry points,
    and existing features.
-   Inspect `clay.html` and `clay-design.md` directly. Treat them as the
    primary design references.
-   Inventory existing screens, components, icons, typography, colors,
    database entities, migrations, navigation, and tests.
-   Identify current bugs, incomplete features, duplicate
    implementations, hardcoded data, and accessibility issues.
-   Determine whether the app is new or already contains user data.
-   Write a concise implementation plan with risks, affected files, data
    migrations, and test strategy.
-   Preserve useful existing work and avoid gratuitous rewrites.

Do not assume a file, asset, icon, dependency, or feature exists without
checking the repository.

------------------------------------------------------------------------

# 3. Product and Platform Requirements

For a native Android project, prefer:

-   Kotlin.
-   Jetpack Compose.
-   Material 3 as a foundation, customized to match the provided design.
-   Room with SQLite for persistent local data.
-   Kotlin Coroutines and Flow for asynchronous data and observable UI
    state.
-   ViewModels and a repository/domain architecture.
-   WorkManager for deferrable scheduled work and reminders where
    appropriate.
-   Android Photo Picker and supported camera flows for receipt photos.
-   Android Sharesheet for user-initiated sharing and exports.

Follow the project's existing stack if it is already valid and
functional. Do not migrate technologies without a strong, documented
reason.

Requirements:

-   Currency: Pakistani Rupees (PKR).
-   Storage: local-only and offline-first.
-   Authentication: no login or account registration.
-   Core functionality must not require network access.
-   Multiple accounts are supported, but the default account is one
    unified **Cash Wallet**.
-   Receipt photos are supported and stored locally.
-   Recurring transactions must require explicit user confirmation
    before being recorded as completed financial transactions.
-   Karz supports both money lending/borrowing and non-cash goods or
    expenses provided/received on credit.
-   No fabricated financial data on first launch.

------------------------------------------------------------------------

# 4. Design System: Clay-Inspired, Premium, and Consistent

Use `clay.html` and `clay-design.md` as the source of truth for the
app's visual direction. Inspect the real tokens and components before
implementation. Do not merely describe the reference---translate it into
a consistent Android design system.

## Visual direction

Preserve the reference's defining characteristics:

-   Warm cream and ivory surfaces.
-   Strong, readable black typography.
-   Restrained lime-green accents.
-   Rounded cards and carefully controlled corner radii.
-   Thin, subtle borders.
-   Soft, purposeful shadows where appropriate.
-   Playful clay-inspired illustrations used sparingly, especially in
    empty states.
-   Generous whitespace and clear visual hierarchy.
-   Refined icons, consistent alignment, and deliberate spacing.
-   Friendly, approachable financial-product personality.

Use actual values from the reference when available. Approximate
fallbacks such as `#FEFDFB`, `#F4F3F0`, and `#EEF773` must not override
real design tokens found in the supplied files.

## Quality bar

-   Establish reusable color, typography, spacing, radius, icon,
    elevation, and motion tokens.
-   Use a consistent type scale and readable monetary figures.
-   Align currency amounts consistently and avoid clipped numbers.
-   Use the lime accent for primary actions and meaningful emphasis, not
    for every component.
-   Avoid excessive gradients, glassmorphism, random bright colors,
    heavy shadows, and unnecessary decoration.
-   Do not use emojis as substitutes for proper UI icons.
-   Use subtle, purposeful animations for transitions, state changes,
    confirmations, and expanding details.
-   Respect system animation and reduced-motion preferences where
    practical.
-   Keep animation short and non-blocking; never animate money in a way
    that obscures the actual value.
-   Ensure dialogs, sheets, forms, cards, lists, and navigation all
    belong to the same design system.
-   Include well-designed loading, empty, success, error, disabled, and
    confirmation states.
-   Support narrow screens, large font settings, and display scaling.
-   Maintain sufficient contrast and never use color alone to
    communicate debt direction, warnings, or success.
-   Implement a complete theme if light and dark modes are offered; do
    not expose unfinished themes.

The UI must look intentional and product-designed, not like a collection
of generated cards.

------------------------------------------------------------------------

# 5. Navigation and Information Architecture

The existing destinations are Home, Add, Needs, and Settings. Add a
dedicated **Karz** experience without overcrowding small screens.

Evaluate a five-destination bottom navigation or another clear pattern
based on the existing app and design reference. If five destinations do
not fit comfortably, keep Karz easily discoverable from Home and the
main navigation, and reorganize Needs only if it remains easy to find.
Do not hide core features behind confusing menus.

Suggested destinations:

1.  Home
2.  Add
3.  Karz
4.  Needs
5.  Settings

Use a clear selected state, accessible labels, consistent icons, and
correct back navigation. All destinations must lead to real screens.

------------------------------------------------------------------------

# 6. Unified Cash Wallet and Account Management

## Critical rule: Cash and Wallet are one account

Create a single default account named **Cash Wallet**. Do not create
separate default Cash and Wallet accounts or display them as separate
account cards, filters, or balances.

Cash Wallet represents money the user manages as one combined balance.
The user can rename it and create additional accounts when genuinely
separate financial sources need tracking, such as a bank account.

Account fields should include:

-   Stable unique ID.
-   Name.
-   Optional type, icon, and color.
-   Opening balance in integer paisa.
-   Active or archived state.
-   Creation and modification timestamps.

Create Cash Wallet exactly once using idempotent initialization. Never
create demo accounts or balances.

## Account operations

Allow users to:

-   View account balances.
-   Create and rename accounts.
-   Change icons or colors where supported.
-   Record an opening balance.
-   Archive accounts safely.
-   View transactions associated with an account.
-   Transfer money between their own accounts.

Opening-balance changes must be explicit and auditable. Archiving must
not delete financial history. Prevent transfers from an account to
itself and record transfers atomically.

## Existing-data migration

If the current database contains separate Cash and Wallet accounts:

1.  Back up the database and relevant receipt files before migration.
2.  Inspect account records, opening balances, transactions, transfers,
    and linked rules.
3.  Define a reconciliation strategy that avoids double-counting opening
    balances or ledger entries.
4.  Merge into the unified Cash Wallet model while preserving
    transaction IDs, dates, categories, notes, receipts, and
    relationships.
5.  Detect ambiguous or conflicting records and request user resolution
    rather than guessing.
6.  Make migration idempotent.
7.  Verify balances and record counts before and after migration.
8.  Preserve a recovery path.
9.  Never silently discard transactions or attachments.

For a new installation, create only the intended Cash Wallet account and
no fabricated activity.

------------------------------------------------------------------------

# 7. Home Dashboard

The first screen must immediately explain the user's financial position.

Show:

-   **Total Balance:** current balances across active accounts included
    in the total.
-   **Remaining Balance:** Total Balance minus valid, explicitly
    reserved amounts not already spent.
-   Current reporting month or period.
-   Income and expenses for the selected period.
-   A prominent Add Transaction action.
-   A compact spending breakdown based on actual records.
-   The five most recent entries.
-   A compact Karz summary.
-   A clear way to view all transactions and all Karz records.

Never show fake data to make the dashboard look populated.

## Financial definitions

-   Total Balance means money currently held in tracked accounts; it
    does not automatically include receivables or subtract liabilities.
-   Remaining Balance equals Total Balance minus valid reserved amounts,
    counted once.
-   Income is earned or received money classified as income, not
    borrowed principal.
-   Expenses are spending events, not principal repayments or transfers.
-   Transfers move money between the user's own accounts and are neither
    income nor expenses.
-   Karz receivables and payables must be shown separately from
    spendable cash.
-   Non-cash items acquired on credit must not increase Cash Wallet.

Explain the Remaining Balance calculation in an accessible information
sheet or tooltip. If no valid reserved amounts exist, Remaining Balance
equals Total Balance.

Include a concise Karz widget showing: - Money owed to me. - Money I owe
others. - Net Karz, with a clear explanation. - One or two upcoming or
overdue items. - View All Karz.

Do not overwhelm the Home screen. Use progressive disclosure for
detailed reports and histories.

------------------------------------------------------------------------

# 8. Transaction Entry and History

Support three ordinary transaction types:

-   Expense.
-   Income.
-   Transfer.

For income and expense, provide amount, category, account, date/time,
optional merchant or description, optional note, and optional receipt
photo. For transfers, require source account, destination account,
amount, and date/time.

Use an intuitive numeric amount input. Validate all monetary input and
prevent zero, negative, malformed, or unsupported values. Preserve
user-entered fields when validation fails. Prevent duplicate
submissions.

Store money as integer paisa using `Long`. Never use `Float` or `Double`
for financial arithmetic.

## Entries section: mandatory View Details option

Every entry row or card in the Entries/transaction list must have a
clear, accessible way to **View Details**. This can be a row tap, a
trailing details icon, or an explicit `View details` action, but the
interaction must be discoverable and consistent.

The details screen must show the actual saved record, including where
relevant:

-   Entry type.
-   Amount.
-   Category.
-   Account.
-   Date and time.
-   Description or merchant.
-   Notes.
-   Receipt image and attachment actions.
-   Related debt or repayment, if applicable.
-   Creation/update information where useful.
-   Edit and delete actions with appropriate confirmation.

For debt-related entries, open the linked Karz detail screen or show a
clearly linked debt-event detail screen. Do not show a generic details
page with missing or hardcoded fields.

## History tools

Support:

-   Search by merchant, description, note, or person.
-   Date-range filters.
-   Account and category filters.
-   Expense, income, transfer, and debt-event filters.
-   Sort by newest, oldest, amount, and other useful criteria.
-   Empty and no-results states.
-   Editing, safe deletion, and undo where possible.
-   Efficient queries or pagination for large histories.

Ensure the account filter shows Cash Wallet as one account, not separate
Cash and Wallet entries.

------------------------------------------------------------------------

# 9. Categories, Budgets, Reserved Money, and Needs

## Categories and budgets

Provide editable income and expense categories with consistent icons and
restrained colors. Allow creating, renaming, archiving, and setting
monthly budgets. Show actual spending versus budget and clear warnings
as limits approach or are exceeded.

Do not generate fictional history to populate charts. Empty charts
should explain how real activity will appear.

## Reserved money

Let users explicitly reserve funds for a goal or upcoming obligation. A
reserved amount has a purpose, amount, optional due date, and
editable/releasable state.

-   Reserving money is not an expense.
-   Do not deduct a reservation twice.
-   Do not subtract money that has already been spent.
-   Explain how reservations affect Remaining Balance.

## Needs / shopping planner

Allow item name, estimated price, priority, optional category, target
date, and purchased state. Show the total of outstanding needs. Adding a
need must not create an expense or reserve funds automatically.

Marking an item purchased must not silently create a transaction. Offer
a transaction flow and require the user to confirm amount, account, and
date. If the purchase was acquired on credit and should be tracked as
Karz, offer that path explicitly.

------------------------------------------------------------------------

# 10. Karz --- Complete Debt, Borrowing, and Credit-Item Management

Karz is a first-class feature, not a disconnected list. It must track
both money lent/borrowed and goods or services provided/received on
credit.

## 10.1 Karz overview

Provide clear summary figures based on actual records:

-   **I Gave:** outstanding money or recoverable value owed to me.
-   **I Took:** outstanding money or value I owe others.
-   **Net Karz:** receivables minus payables, with the calculation
    explained.
-   **Due Soon:** outstanding items with upcoming due dates.
-   Overdue items.

Provide two primary actions: - `+ I Gave Karz` - `+ I Took Karz`

Show active, partially settled, overdue, and settled records with clear
filters. Use text and icons as well as color to distinguish receivables
from payables.

## 10.2 I Gave Karz

This direction means another person owes the user. Support:

-   Money lent to someone.
-   An item or goods the user provided on credit.
-   Food, groceries, supplies, or another item/service provided for
    which the other person will pay later, if the user explicitly
    records it as an amount owed.

The user must be able to record the person, event date, amount or item
valuation, optional due date, description, notes, account movement if
cash was actually given, and optional locally stored receipt/photo.

If the user gave actual money from Cash Wallet, reduce that account only
after confirming the real cash movement. This creates a receivable; it
is not an ordinary expense.

If the user provided goods or food on credit, do not create a cash
outflow unless a real cash movement also occurred. Record the
recoverable amount as a receivable. Do not imply the user handed over
money when they handed over an item.

## 10.3 I Took Karz --- choose what was received

When the user selects `+ I Took Karz`, the form must begin with a clear
required choice:

**What did you receive from this person?**

Options: 1. **Money** --- I borrowed money and received cash or funds.
2. **Item or expense on credit** --- the person gave me an item, food,
groceries, paid a bill for me, or covered a cost that I must repay. 3.
**Other non-cash value** --- a clearly described good or service with an
agreed amount to repay.

Use clear explanatory subtitles and icons. The user must choose the
actual situation; do not guess based on a note or amount.

### Option A: Money received

Required/optional fields: - Lender/person name (required). - Amount in
PKR (required). - Date received (required). - Destination account
(required if the user wants to record the actual cash movement). - Due
date (optional). - Description and note (optional). - Receipt or
supporting photo (optional).

Ask whether the borrowed money was actually received into a tracked
account or whether the user is recording an existing/historical debt
only.

If the money was received into Cash Wallet, increase Cash Wallet by the
confirmed amount and create a payable liability. Borrowing is not
income.

If the money was received into another tracked account, increase that
account instead.

If the user is recording an old debt without a current cash movement,
create the debt record without changing today's account balance. Make
this choice explicit.

### Option B: Item, food, bill, or expense received on credit

Required/optional fields: - Person/lender name (required). - Item or
expense description (required). - Agreed amount owed in PKR
(required). - Date received (required). - Due date (optional). -
Optional quantity, unit, or item details where relevant. - Optional
notes. - Optional receipt/photo. - Optional repayment plan.

Examples: - Ali gave me food worth PKR 800 and I will pay him later. - A
friend bought groceries worth PKR 3,500 for me. - Someone paid my
electricity bill of PKR 4,000 and I owe them that amount. - I received a
product on credit for PKR 12,000.

**Critical accounting rule:** the user did not receive cash. Do not
increase Cash Wallet, any other cash account, Total Balance, or ordinary
income because of this record. Create a debt payable for the agreed
amount and preserve the item/expense description.

Do not create a cash transaction merely to make the account balance
match the debt. Do not categorize borrowed food or goods as cash
received. If the user wants to record the related consumption or expense
for reporting purposes, make that a separate, clearly linked
classification and ensure it does not fabricate a cash movement or
double-count the liability.

The form and confirmation screen must explicitly state: - What was
received. - Who provided it. - Amount owed. - Whether any money account
will change. - The resulting outstanding liability.

For this non-cash option, the default must be **no account balance
change**.

### Option C: Other non-cash value

Allow a user-defined description and agreed payable value. Apply the
same no-cash-movement default unless a real account movement is
explicitly confirmed.

## 10.4 Debt record data model

Each Karz record should store, at minimum:

-   Stable ID.
-   Direction: receivable (`I Gave`) or payable (`I Took`).
-   Consideration type: money, item/food/goods, bill/expense paid by
    another person, or other non-cash value.
-   Person/lender/borrower name.
-   Item/service description where applicable.
-   Original principal or agreed value in integer paisa.
-   Currency (PKR).
-   Date created and event date.
-   Optional due date.
-   Optional account reference only when a real account movement exists.
-   Explicit cash-movement state.
-   Outstanding principal calculated from events.
-   Optional notes and attachments.
-   Status.
-   Creation/update timestamps.
-   Optional interest settings, disabled by default.

Never rely on a manually editable outstanding amount as the only source
of truth. Calculate outstanding amounts from the original value,
additional advances, repayments, and explicit corrections.

## 10.5 Repayments and the "Paid Back" action

On every `I Took Karz` detail screen, show a prominent action whose
label depends on the debt's state:

-   **`Mark as Paid Back`** when the full outstanding amount can be paid
    in one action.
-   **`Record Partial Repayment`** when the user wants to pay only part.
-   After settlement, display **`Paid Back`** / **`Settled`** as a
    status, not as an enabled action that can accidentally charge the
    user again.

For clarity, the main full-settlement button should be labeled
**`Mark as Paid Back`**, with supporting text such as "Record that you
have repaid the outstanding amount."

When the user taps it: 1. Open a confirmation/review sheet, not an
immediate database mutation. 2. Show the person, what was
borrowed/received, outstanding amount, repayment date, and selected
payment method/account if applicable. 3. Ask whether the repayment is
full or partial if the user entered through a generic repayment action.
4. Ask how the obligation was settled: - Paid money from Cash Wallet. -
Paid money from another tracked account. - Settled without a tracked
cash movement, for a valid non-cash settlement or historical correction.
5. For a normal cash repayment, require the account and amount, then
reduce that account by the amount actually paid. 6. For a non-cash or
waived settlement, require a clear reason and explicit confirmation; do
not pretend cash was paid. 7. Save the repayment event and any account
ledger entry atomically. 8. Update outstanding principal and status only
after the database operation succeeds. 9. Prevent repeated taps or
duplicate submission. 10. Show a truthful success message only after
persistence succeeds.

For a debt of PKR 4,000: - Full repayment of PKR 4,000 leaves PKR 0
outstanding and marks the debt Settled. - Partial repayment of PKR 1,000
leaves PKR 3,000 outstanding and keeps the debt active/partially paid.

The action must not mark a debt as paid back without recording how the
obligation was actually settled. A non-cash item received on credit does
not change Cash Wallet when created, but paying it back in cash should
reduce the selected account at repayment time.

## 10.6 Repayment rules

Each debt supports multiple repayment events. A repayment stores amount,
date/time, payment method/settlement type, account reference if
applicable, note, optional receipt, and linked debt ID.

-   Repayment amount must be greater than zero.
-   A normal principal repayment cannot exceed the outstanding
    principal.
-   Principal repayment is not ordinary income or expense.
-   Paying a payable reduces cash only when an actual cash payment is
    confirmed.
-   Receiving a receivable repayment increases cash only when money is
    actually received into a tracked account.
-   A non-cash settlement or correction must be explicitly identified
    and explained.
-   Preserve all prior repayments.
-   Prevent duplicate submissions.
-   Commit debt updates and associated ledger entries atomically.
-   Recalculate and display outstanding amount immediately from saved
    data.

## 10.7 I Gave Karz detail and repayment

For money lent, show `Record Money Received` and a full/partial
repayment flow. Receiving principal repayment increases the selected
account only if the user confirms that money was actually received. It
reduces the receivable, not normal income.

For goods or food provided on credit, the amount owed is a receivable
for the agreed value. It must not reduce Cash Wallet unless the user
actually paid cash as part of the event.

## 10.8 Debt detail screens and Entries details

Every Karz item must have a dedicated detail screen. Show:

-   Person.
-   Direction: I Gave or I Took.
-   What was given/received: money, item, food, bill, service, or other
    value.
-   Original amount/value.
-   Additional advances, if any.
-   Total repayments.
-   Outstanding principal.
-   Due date and status.
-   Event date and notes.
-   Supporting attachments.
-   Complete chronological event/repayment history.
-   Whether a cash account was changed at creation or repayment.
-   Edit and safe correction actions.
-   Full/partial repayment actions.
-   Reminder settings.

In the general **Entries** list, each entry must have an explicit,
discoverable **`View Details`** action or an equally clear row-tap
interaction. Tapping a debt-related entry must open the correct linked
Karz record or event details---not a generic, empty, or fake screen.

Details must be driven by the saved record and include all available
fields. Hide fields that genuinely do not apply rather than displaying
placeholder values.

## 10.9 Additional borrowing, corrections, interest, and write-offs

Support additional advances as separate dated events. Never silently
overwrite original principal.

Allow corrections only with confirmation and an audit trail.
Distinguish: - New principal/value. - Principal repayment. - Interest,
if explicitly enabled. - Explicit balance correction. -
Forgiveness/write-off. - Non-cash settlement.

Interest must be disabled by default. Never automatically add interest,
penalties, or late fees. If enabled, explain the method and keep
interest separate from principal.

A written-off receivable or forgiven payable requires a reason and
explicit confirmation. Never mark a debt settled simply because its due
date passed.

## 10.10 Debt statuses and reminders

Support: - Active. - Partially Paid. - Due Soon. - Overdue. - Settled. -
Written Off.

Due Soon and Overdue may be derived from due date and outstanding
amount. A missing due date is not overdue. Settled and Written Off are
explicit recorded outcomes.

Allow optional local reminders. Ask for notification permission when
needed, allow reminders to be disabled, reschedule after due-date
changes, avoid duplicates, and recover after restart where supported. A
reminder never records a payment automatically.

## 10.11 Karz overview, search, filters, and reports

Support: - Search by person's name or item description. - Filters for I
Gave, I Took, consideration type, active, partial, overdue, settled, and
written off. - Sorting by due date, amount, name, and last activity. -
Person-specific statement export with user preview. - Monthly
principal/value lent and borrowed. - Monthly repayments received and
made. - Separate principal and interest totals. - Outstanding balance
summaries. - Linked entry and repayment details.

Reports must not classify borrowed principal as income, principal
repayment as ordinary expense, or non-cash items as cash received.

## 10.12 Karz acceptance tests

At minimum, test: 1. Create `I Took Karz` for money actually received
into Cash Wallet; cash increases and liability is created. 2. Create
`I Took Karz` for food/item on credit; liability is created but Cash
Wallet and income do not increase. 3. Create `I Took Karz` for a bill
paid by another person; no cash is added to the user's account. 4.
Create a non-cash debt with an optional due date and correct status. 5.
Mark a money debt fully paid back; the chosen account decreases by the
actual payment and the debt becomes settled. 6. Mark an item/food debt
fully paid back; the account decreases only if a real cash repayment is
confirmed. 7. Record a partial repayment and verify the outstanding
amount. 8. Attempt duplicate submission and verify no duplicate records.
9. Attempt repayment greater than outstanding amount and verify
rejection or a separate explicit adjustment flow. 10. Record a
historical debt without changing today's account balance. 11. Confirm
principal repayments are not ordinary income or expense. 12. Confirm
debt detail and Entries \> View Details show the correct saved record.
13. Confirm reports and dashboard use real ledger data. 14. Confirm due
date absence never causes false overdue status. 15. Confirm backups
preserve debt events, repayments, and attachments. 16. Confirm a
database failure cannot leave a repayment without its required linked
ledger event. 17. Confirm no sample debts are created on fresh install.

------------------------------------------------------------------------

# 11. Recurring Transactions and Automation

Support daily, weekly, monthly, and yearly recurring rules where
relevant.

-   Show upcoming and overdue occurrences.
-   Allow pause, edit, and cancel.
-   Handle device restart and time-zone changes carefully.
-   Avoid duplicate occurrences.
-   Use local notifications only, subject to Android permission
    requirements.
-   Never create a completed financial transaction automatically.
-   A scheduled event can create a reminder or pending item; recording
    the actual transaction requires explicit user confirmation.
-   Confirm a recurring occurrence only once using a stable occurrence
    ID.

Automation must assist the user, not invent financial activity.

------------------------------------------------------------------------

# 12. Receipt and Attachment Handling

Use Android Photo Picker and appropriate camera integration. Store
receipt and debt attachments in app-private local storage.

-   Keep database references rather than storing large images in
    database rows unnecessarily.
-   Generate thumbnails where useful.
-   Allow viewing, replacing, and deleting attachments.
-   Handle cancelled selection and missing files.
-   Avoid orphaned files when records are removed.
-   Do not upload attachments.
-   Do not log receipt contents or sensitive file paths.
-   Include attachments in backup/export only with clear user consent
    and documented behavior.

------------------------------------------------------------------------

# 13. Data Protection, Export, Import, and Recovery

-   Keep financial data in app-private storage.
-   Do not transmit data over the network.
-   Avoid unnecessary Android permissions.
-   Provide a local export and import workflow.
-   Version backup formats.
-   Validate imported files before applying them.
-   Detect malformed, corrupted, incompatible, or duplicate data.
-   Preview import consequences and ask for explicit confirmation.
-   Never overwrite current records silently.
-   Preserve accounts, transactions, debts, debt events, repayments,
    categories, budgets, needs, settings, and attachments as supported.
-   Use a recoverable import strategy and test interrupted imports.
-   Explain uninstall and Android backup limitations honestly.
-   If encryption is implemented, use established Android cryptography
    and sound key management. Never invent custom cryptography or claim
    unimplemented security.

------------------------------------------------------------------------

# 14. Favicon, App Icon, and Branding

Update the **favicon** and all relevant app branding assets to match
Hisaab Do Bhai.

First inspect the repository to determine whether it contains a web
front end, a website/PWA, a documentation site, or only a native Android
app.

-   If a web interface exists, replace the old favicon with a clean,
    custom Hisaab Do Bhai favicon consistent with the Clay-inspired
    brand.
-   If this is a native Android app, update the Android launcher icon
    and adaptive icon assets; a favicon alone is not sufficient for
    native Android.
-   If the repository contains both web and Android assets, update both.
-   Keep branding consistent across the app icon, splash/startup
    branding where present, README screenshots, and documentation.
-   Use a simple, recognizable mark that remains legible at small sizes;
    avoid tiny text and overly detailed artwork.
-   Generate the required sizes and formats from one coherent source
    asset where possible.
-   Ensure all manifest, HTML, and resource references point to valid
    assets.
-   Remove obsolete icon references only after verifying they are no
    longer used.
-   Do not use a random third-party icon or a copyrighted logo.
-   Test that the favicon and launcher icon actually appear in their
    intended contexts.

Do not add a web favicon to a native-only project where it serves no
purpose; instead, update the real Android icon assets.

------------------------------------------------------------------------

# 15. GitHub-Ready Repository, README, and Supporting Files

The finished repository will be uploaded to GitHub. Prepare it as a
professional, understandable, reproducible project.

## 15.1 Detailed README.md

Create or fully update `README.md`. It must describe the actual
implementation, not planned features that do not exist.

Include:

1.  Project title and concise description.
2.  App logo or branding image, if a valid repository asset exists.
3.  A small set of meaningful badges only when accurate; do not add fake
    CI, coverage, release, or version badges.
4.  Features that are actually implemented.
5.  Screenshots or GIFs using real app captures only; do not fabricate
    screenshots.
6.  Tech stack and architecture overview.
7.  Design philosophy and design-reference attribution/notes where
    appropriate.
8.  Requirements and prerequisites.
9.  Step-by-step setup and build instructions for Windows, macOS, and
    Linux where the project tooling supports them.
10. How to open the project in Android Studio.
11. How to configure SDK/JDK/Gradle versions based on the actual
    project.
12. How to build a debug APK and where the generated output is located.
13. How to run tests and static analysis.
14. How to use the app's core features.
15. Clear explanation of Cash Wallet unification.
16. Detailed Karz guide covering:
    -   I Gave Karz.
    -   I Took Karz: money received.
    -   I Took Karz: item/food/bill received on credit.
    -   Partial repayment.
    -   `Mark as Paid Back`.
    -   Why non-cash items do not increase Cash Wallet.
    -   Historical debt entry and account movement choices.
17. Explanation of balances, transfers, debt, and principal repayments.
18. Local storage, offline behavior, privacy, backup, and recovery
    limitations.
19. Database schema and migration notes.
20. Accessibility and localization notes.
21. Known limitations, clearly labeled.
22. Troubleshooting for common Gradle, SDK, emulator, build, and
    migration problems.
23. Contribution guidelines or a link to `CONTRIBUTING.md`.
24. Security and vulnerability reporting guidance or a link to
    `SECURITY.md`.
25. License information only after the actual license has been chosen
    and added.
26. Changelog/release notes location.
27. Maintainer contact only if intentionally supplied by the project
    owner.

Do not claim that the app is published, production-certified, fully
secure, or tested on platforms that were not actually tested.

## 15.2 Recommended repository files

Create or update the following where applicable:

-   `README.md` --- complete setup, feature, architecture, usage, and
    troubleshooting guide.
-   `LICENSE` --- only after the owner chooses a license; do not
    silently assume a license.
-   `CONTRIBUTING.md` --- development setup, coding conventions,
    testing, pull request expectations.
-   `SECURITY.md` --- safe vulnerability-reporting instructions without
    exposing private data.
-   `CHANGELOG.md` --- versioned, truthful changes.
-   `.gitignore` --- Android Studio, Gradle, local SDK configuration,
    build outputs, IDE state, signing keys, local secrets, generated
    files, and machine-specific paths; do not ignore source assets or
    required Gradle wrapper files.
-   `.editorconfig` --- consistent text and indentation settings.
-   `docs/ARCHITECTURE.md` --- architecture, modules, data flow,
    database entities, and financial invariants.
-   `docs/DATA_MODEL.md` --- entity relationships and accounting rules.
-   `docs/TESTING.md` --- test strategy and commands.
-   `docs/PRIVACY.md` --- what is stored locally, permissions, export,
    and data handling.
-   `docs/RELEASE.md` --- repeatable release/build instructions and
    signing cautions.
-   `docs/DESIGN_SYSTEM.md` --- design tokens, typography, spacing,
    component conventions, and icon rules.
-   `docs/SCREENSHOTS.md` or an equivalent screenshot directory with
    genuine screenshots if available.
-   `brain.md` --- working project handoff file tracking architecture
    decisions, completed work, tests, known issues, and next steps. Keep
    it useful; do not replace public documentation with it.
-   `.github/ISSUE_TEMPLATE/bug_report.yml` or a Markdown bug template.
-   `.github/ISSUE_TEMPLATE/feature_request.yml` or a Markdown feature
    template.
-   `.github/pull_request_template.md`.
-   `.github/workflows/android-ci.yml` --- only if CI can be configured
    correctly for the actual project; run or validate the workflow
    configuration and do not claim a passing workflow without a run.
-   `gradlew`, `gradlew.bat`, and Gradle wrapper files if missing and
    appropriate for the project; do not replace valid existing wrapper
    versions casually.

Do not create meaningless boilerplate files solely to increase file
count. Each file must be accurate and useful.

## 15.3 Repository hygiene and secrets

-   Never commit signing keys, keystores, passwords, tokens, API
    secrets, personal financial records, receipt photos, local
    databases, or machine-specific local configuration.
-   Add appropriate ignore rules for private local data and build
    outputs.
-   Use placeholders only for configuration examples, never fake
    credentials that might be mistaken for real secrets.
-   Review the staged Git diff before commits.
-   Do not rewrite Git history, force-push, publish a release, or push
    to GitHub without explicit authorization.
-   Do not create an unchosen license.
-   Keep documentation synchronized with implemented behavior.

## 15.4 GitHub presentation quality

The repository should be easy for another developer to understand and
build:

-   Clear folder structure.
-   Descriptive filenames.
-   Consistent code style.
-   No committed generated build folders.
-   No accidental local databases or personal data.
-   No dead links in README.
-   No screenshots that misrepresent current functionality.
-   No inaccurate badges or claims.
-   Useful issue and pull-request templates.
-   A concise, professional project description and topic suggestions in
    documentation, without claiming GitHub repository settings have been
    changed unless actually done.

------------------------------------------------------------------------

# 16. Architecture and Suggested Data Model

Use a maintainable separation between:

-   Compose UI and reusable components.
-   ViewModels and UI state.
-   Domain models and business rules.
-   Repositories.
-   Room entities and DAOs.
-   Database migrations.
-   Local scheduler and notifications.
-   Receipt/attachment file storage.
-   Import/export services.
-   Validation and money-formatting utilities.

Suggested entities, adapted to the existing project:

-   Account
-   Transaction
-   Category
-   Budget
-   ReservedAmount
-   NeedItem
-   ReceiptAttachment
-   Debt
-   DebtEvent
-   DebtRepayment
-   DebtAttachment
-   RecurringRule
-   RecurringOccurrence
-   AppSettings

Use stable IDs, foreign keys, indexes, and appropriate delete/update
rules. Keep a debt activity trail and link account ledger events to debt
events when an actual cash movement occurs.

Do not duplicate the source of truth across unrelated balance fields.
Derive balances from opening balances and ledger events or use a
rigorously tested ledger approach.

## Financial integrity rules

1.  Lending money reduces the selected cash account only if an actual
    tracked cash movement is confirmed.
2.  Lending creates a receivable.
3.  Borrowing money increases the selected cash account only if money
    was actually received into that account.
4.  Borrowed principal creates a liability, not income.
5.  Receiving an item or food on credit creates a payable but does not
    increase cash.
6.  Receiving a bill or expense paid by someone else creates a payable
    but does not increase cash.
7.  Paying a payable reduces cash only when a real payment is confirmed.
8.  Receiving repayment of a receivable increases cash only when money
    is actually received.
9.  Principal repayments are not ordinary income or expense.
10. Interest is separate and disabled by default.
11. Transfers between own accounts are neither income nor expenses.
12. Historical records do not silently alter today's account balance.
13. Related account and debt events are saved atomically.
14. Repeated actions cannot create duplicate transactions or repayments.
15. Money is stored as integer paisa (`Long`) and never calculated with
    floating-point types.

------------------------------------------------------------------------

# 17. Accessibility, Localization, and Performance

-   Provide semantic descriptions for controls and icons.
-   Support screen readers and keyboard navigation where applicable.
-   Maintain readable contrast.
-   Support larger font sizes and display scaling.
-   Use touch targets suitable for mobile use.
-   Do not communicate meaning with color alone.
-   Format PKR consistently and prevent clipping of large amounts.
-   Use the appropriate locale-aware date presentation without altering
    stored event timestamps.
-   Keep scrolling smooth and database operations off the main thread.
-   Use stable list keys and efficient Room queries.
-   Avoid unnecessary recompositions and oversized images.
-   Handle empty, loading, success, and error states consistently.
-   Keep the app usable on modest Android devices.

------------------------------------------------------------------------

# 18. Testing and Acceptance Criteria

Implement and run meaningful tests appropriate to the existing project.

## Financial and database tests

-   Fresh installation creates exactly one Cash Wallet.
-   No separate default Cash and Wallet accounts are created.
-   Additional accounts can be created safely.
-   Existing account migration preserves all valid records and does not
    double-count balances.
-   Income, expenses, transfers, and edits produce correct balances.
-   Transfers preserve total money across tracked accounts.
-   Reserved amounts affect Remaining Balance exactly once.
-   Money inputs reject invalid values.
-   Debt records calculate outstanding values from events.
-   Borrowed cash changes the selected account only when actually
    received.
-   Non-cash goods, food, and bills on credit do not increase cash or
    ordinary income.
-   Full and partial repayments update debt and account balances
    correctly.
-   `Mark as Paid Back` cannot create duplicate payments.
-   Historical debt entries do not change current balances unless
    explicitly chosen.
-   Debt principal repayments are excluded from ordinary income/expense
    totals.
-   Debt details and Entries \> View Details show the correct saved
    record.
-   Optional interest remains separate from principal.
-   Due Soon and Overdue states are correct.
-   Missing due dates do not become overdue.
-   Backup/restore preserves records and attachments.
-   Database failures do not leave linked records partially committed.
-   No fabricated records are created on first launch.

## UI tests

-   All navigation destinations open the correct screens.
-   Every entry provides a discoverable View Details interaction.
-   Every form has clear validation and cancellation behavior.
-   The non-cash Karz path clearly explains that cash will not change.
-   The full repayment confirmation shows the correct outstanding amount
    and account effect.
-   Empty states and error states are readable and useful.
-   Large font settings do not hide important actions.
-   Favicon or launcher icon references resolve to valid assets.

## Build and quality checks

-   Run unit, database, migration, and UI tests available in the
    project.
-   Run lint and static analysis available in the project.
-   Build the project using its wrapper and documented environment.
-   Review dependency versions and remove unused dependencies only when
    safe.
-   Inspect logs for warnings that indicate real problems.
-   Report commands run, pass/fail status, and tests not run.

Never state that tests passed if they were not executed. Do not conceal
failing tests.

------------------------------------------------------------------------

# 19. Anti-AI-Slop and Reliability Rules

Never:

-   Add fake transactions, people, debt records, balances, or reports.
-   Populate charts with random data.
-   Add decorative buttons that do nothing.
-   Use placeholder screens as final functionality.
-   Hardcode financial totals.
-   Add a borrowed item to Cash Wallet as though it were money received.
-   Mark a debt paid without recording a valid settlement event.
-   Automatically record recurring payments.
-   Silently erase, rewrite, or merge financial history.
-   Hide errors behind a success animation.
-   Claim untested functionality is complete.
-   Add unnecessary animations, gradients, cards, or features without
    user value.
-   Add a license without owner approval.
-   Add fake CI, test coverage, release, or version badges.
-   Include private data, signing keys, local databases, or receipts in
    Git.
-   launch the app, emulator, or device before explicit approval.

Use real local data, transparent calculations, meaningful empty states,
accurate documentation, and explicit confirmations for consequential
actions.

------------------------------------------------------------------------

# 20. Implementation Workflow and Deliverables

Follow this sequence:

1.  Inspect the repository, `clay.html`, and `clay-design.md`.
2.  Summarize current architecture, implemented features, gaps, and
    risks.
3.  Produce a focused implementation plan and migration strategy.
4.  Implement the unified Cash Wallet model and safe migrations.
5.  Refine the design tokens, typography, navigation, icons, and
    reusable components.
6.  Improve Home, transaction entry, Entries, View Details, Needs, and
    Settings.
7.  Implement Karz for cash borrowing/lending and non-cash items or
    expenses received/provided on credit.
8.  Implement full/partial repayment, `Mark as Paid Back`, event
    history, due dates, and local reminders.
9.  Integrate all balance calculations and reports with the actual
    ledger.
10. Update favicon and/or native launcher icons based on the
    repository's platforms.
11. Prepare the README and relevant GitHub documentation/configuration
    files.
12. Implement and run tests, lint, static analysis, and build checks.
13. Fix discovered issues and inspect the final diff.
14. Update `brain.md` with decisions, changed files, test results, known
    limitations, and next steps.
15. Make focused Git commits after completed and verified milestones
    when Git is available and safe. Do not push or publish without
    explicit approval.
16. Provide a final report of implemented features, changed files,
    migrations, commands/tests run, build status, known issues, and any
    remaining manual steps.

Do not claim work is complete if a required feature remains a stub. If a
feature cannot be completed in the current environment, document the
exact limitation and the remaining implementation steps.

## Required deliverables

-   A working, maintainable Android project with no fabricated financial
    records.
-   Unified Cash Wallet behavior and safe existing-data migration where
    needed.
-   Complete Karz support for money and non-cash credit items/expenses.
-   A prominent, correctly implemented `Mark as Paid Back` flow.
-   Working Entries \> View Details navigation.
-   Polished, consistent Clay-inspired UI/UX.
-   Correct favicon and/or Android launcher icon assets.
-   Detailed, truthful `README.md`.
-   Useful GitHub documentation and repository hygiene files appropriate
    to the project.
-   Automated tests and an honest test/build report.
-   Updated `brain.md` project handoff notes.
-   No automatic app launch until I explicitly approve it.

## Definition of Done

Hisaab Do Bhai is complete when its real local data drives every screen;
Cash and Wallet are one unified default account; debt can represent
money or non-cash goods/services received or provided on credit;
non-cash items never fabricate cash inflows; full and partial repayments
preserve correct balances and history; the `Mark as Paid Back` action is
safe and auditable; every entry has a working details path; the design
is consistent and polished; branding assets are correct; GitHub
documentation is detailed and accurate; migrations and financial
invariants are tested; and no fake data or nonfunctional features
remain.
