# NEW MAJOR FEATURE: KARZ (DEBT & LOAN MANAGEMENT)

## 1. Objective

Implement a complete, reliable Karz (debt management) system inside Hisaab Do Bhai.

The feature must support both directions of debt:

1. **I Gave Karz** — money I lent to another person. That person owes me money.
2. **I Took Karz** — money I borrowed from another person. I owe that person money.

Use the term **Karz** as the main feature name. The interface may use English descriptions alongside it so the feature remains easy to understand.

This must be a functional financial ledger integrated with the existing Cash Wallet, accounts, dashboard, transaction history, local storage, and reports. It must not be a disconnected list of names and amounts.

All debt information must be stored locally. No login, cloud synchronization, or internet connection is required.

## 2. Karz Navigation and Overview

Add a Karz destination to the app's navigation. Since the existing app uses Home, Add, Needs, and Settings, redesign the navigation carefully to accommodate Karz without overcrowding the bottom bar. Consider a fifth destination if it remains usable on small screens, or move Needs into Home while retaining a clear, dedicated Karz entry.

The Karz overview screen should display four clear summary figures:

* **I Gave:** total outstanding money owed to me.
* **I Took:** total outstanding money I owe others.
* **Net Karz:** money owed to me minus money I owe.
* **Due Soon:** outstanding debts with upcoming due dates.

All figures must be calculated from actual saved debt records and repayments. Never use hardcoded example values.

Clearly distinguish amounts receivable from amounts payable through text, icons, and accessible visual indicators—not color alone.

Provide two prominent actions:

* `+ I Gave Karz`
* `+ I Took Karz`

Below the summary, display active debts, overdue debts, recently updated debts, and settled debts through clear filters or tabs.

Include a polished empty state explaining how to record the first debt.

## 3. Create a Karz Record

When the user selects either action, open a dedicated form with the correct direction preselected.

### Shared fields

* Person's full name or recognizable name.
* Original principal amount in PKR.
* Date the money was given or received.
* Optional due date.
* Optional description or reason.
* Optional contact information.
* Optional notes.
* Optional receipt or supporting photo, stored locally.
* Optional account used for the original money movement.
* Optional agreed repayment schedule.
* Optional interest terms, only if the user explicitly enables them.

The person and principal amount are required. Other fields should be optional unless required by a selected feature.

Validate the amount, date, and all user-entered values. Prevent zero or negative principal amounts and duplicate submissions.

Display a review screen or clear confirmation before saving a new debt.

## 4. I Gave Karz — Money I Lent

When the user chooses `I Gave Karz`, create a debt receivable.

Display wording such as:

* You gave PKR 10,000 to Ahmed.
* Ahmed owes you PKR 10,000.
* Outstanding amount: PKR 10,000.

The user must be able to:

* View the debt details.
* Record a full or partial repayment received.
* View repayment history.
* Edit permitted details.
* Add notes or attachments.
* Set or change a due date.
* Mark the debt settled only when the outstanding amount is zero, or through a carefully confirmed adjustment.
* Reopen a settled debt only through an explicit, auditable correction flow.

### Accounting behavior

If the user confirms that the loan was paid from Cash Wallet or another tracked account, decrease that account's balance by the amount actually lent.

The initial lending is a movement of the user's existing money into a receivable, not a normal household expense.

For example:

* Cash Wallet before lending: PKR 30,000.
* Money lent to Ahmed: PKR 10,000.
* Cash Wallet after lending: PKR 20,000.
* Outstanding receivable: PKR 10,000.
* Total net worth across tracked cash and receivables remains PKR 30,000, ignoring other assets and liabilities.

If the user is recording an old debt for historical tracking and does not want to change the current account balance, provide an explicit **Record existing debt without changing account balance** option. Explain its accounting consequences and do not silently create a cash movement.

## 5. I Took Karz — Money I Borrowed

When the user chooses `I Took Karz`, create a debt payable.

Display wording such as:

* You borrowed PKR 20,000 from Ali.
* You owe Ali PKR 20,000.
* Outstanding amount: PKR 20,000.

The user must be able to:

* View the lender's details.
* Record a full or partial repayment.
* Record additional borrowing as a separate, linked event.
* View all borrowing and repayment history.
* Add notes and supporting photos.
* Set a due date.
* Review overdue amounts.
* Mark the debt settled when the outstanding amount is zero, or use a confirmed correction flow when a genuine balance adjustment is required.

### Accounting behavior

If the user confirms that borrowed money was received into Cash Wallet or another tracked account, increase that account's balance by the amount actually received.

Borrowing is a liability, not income earned.

For example:

* Cash Wallet before borrowing: PKR 5,000.
* Money borrowed from Ali: PKR 20,000.
* Cash Wallet after borrowing: PKR 25,000.
* Outstanding payable: PKR 20,000.
* Net worth remains PKR 5,000, ignoring other assets and liabilities.

If the debt existed before the user started using the app, allow historical entry without automatically increasing the current account balance.

## 6. Repayments and Partial Payments

Every debt must support multiple repayments.

When the user records a repayment, show a form containing:

* Payment amount.
* Payment date.
* Account involved.
* Optional note.
* Optional receipt photo.
* Optional confirmation reference.

For `I Gave Karz`, a repayment is money received by the user.

For `I Took Karz`, a repayment is money paid by the user.

The repayment amount must be greater than zero and cannot exceed the outstanding principal unless a separately supported interest or adjustment rule explains the difference.

After confirmation:

* Save a permanent repayment record.
* Update the outstanding debt amount.
* Update the selected account balance if the user confirms that the repayment actually moved money through that account.
* Refresh dashboard summaries and related reports.
* Update the debt status to Partially Paid or Settled as appropriate.
* Prevent duplicate submissions.
* Keep the complete repayment history.

Example:

Original debt: PKR 10,000.

First repayment: PKR 3,000.

Remaining debt: PKR 7,000.

Second repayment: PKR 2,000.

Remaining debt: PKR 5,000.

Final repayment: PKR 5,000.

Remaining debt: PKR 0; status: Settled.

Never delete earlier repayments when a new repayment is recorded.

## 7. Additional Borrowing and Debt Adjustments

Support recording additional money given to the same person or additional money borrowed from the same lender.

Do not silently overwrite the original principal. Record every additional advance as a separate, dated event linked to the debt, or create a separate debt when the user chooses that option.

Support corrections for mistakes, but require a reason and preserve an audit trail.

Distinguish between:

* New principal.
* Principal repayment.
* Explicit balance adjustment.
* Optional interest charged or accrued.
* Optional interest paid or received.
* Debt forgiveness or write-off.

Interest must be disabled by default. Never automatically add interest, penalties, fees, or late charges.

If interest is enabled, explain the chosen calculation method and display principal and interest separately. Do not count principal repayments as interest expenses or income.

## 8. Debt Details Screen

Each debt must have a dedicated detail screen with:

* Person's name.
* Direction: I Gave or I Took.
* Original principal.
* Total additional borrowing or lending.
* Total principal repaid.
* Outstanding principal.
* Interest outstanding, if enabled.
* Due date.
* Current status.
* Creation date.
* Notes and attachments.
* Complete chronological activity history.

Provide contextual actions:

* Record repayment.
* Record additional borrowing or lending.
* Edit details.
* Change due date.
* View payment history.
* Export or share a user-approved summary.
* Settle or correct a debt through an explicit confirmation flow.

Display amounts prominently and keep the action buttons clear and consistent with the app's Clay-inspired design.

## 9. Debt Statuses

Use well-defined states:

* Active — outstanding balance exists and is not overdue.
* Partially Paid — some principal has been repaid.
* Due Soon — outstanding debt is approaching its due date.
* Overdue — the due date has passed and money remains outstanding.
* Settled — outstanding balance is zero.
* Written Off — the user explicitly records an unrecoverable receivable or forgiven payable, with a reason.

Keep status calculations consistent. Due Soon and Overdue can be derived display states, while Settled and Written Off should be explicit recorded states.

A missing due date must not be treated as overdue.

Never mark a debt settled simply because its due date has passed.

## 10. Dashboard Integration

Integrate Karz into the Home dashboard without overwhelming the main screen.

Include a compact Karz summary:

* Money owed to me.
* Money I owe others.
* Net Karz.
* One or two upcoming or overdue items.
* A `View All Karz` action.

Show the amounts separately from Total Balance and Remaining Balance.

Do not add receivables to Cash Wallet's spendable balance. Money someone owes me is not cash I can necessarily spend today.

Do not subtract borrowed money from Cash Wallet just because I owe it. It becomes a cash outflow only when an actual repayment is recorded.

Use a clear explanation of the difference between cash available and net worth.

## 11. Main Finance Accounting Rules

Use these rules consistently:

1. Lending money decreases the selected cash account only when an actual tracked cash movement is confirmed.
2. Lending creates or increases a receivable asset.
3. Receiving borrowed money increases the selected cash account only when an actual tracked cash movement is confirmed.
4. Borrowing creates or increases a liability; it is not earned income.
5. Receiving principal repayment on money lent increases the selected account balance when received.
6. Paying principal on borrowed money decreases the selected account balance when paid.
7. Principal repayment is not ordinary income or an ordinary expense.
8. If interest is explicitly enabled, interest received and paid must be tracked separately from principal.
9. A historical debt entry must not silently alter today's cash balance.
10. A repayment must never update a debt without the corresponding linked account movement when account tracking was explicitly selected.
11. A confirmed cash movement and its debt event must be committed atomically.
12. Transfers between the user's own accounts must not be misclassified as lending or borrowing.

Use integer paisa and `Long` for all monetary storage. Never use `Float` or `Double` for money calculations.

## 12. Local Reminders

Allow the user to opt into local reminders for upcoming repayments and overdue debts.

* Ask for notification permission where required.
* Allow reminders to be disabled.
* Support due-date changes.
* Avoid duplicate reminders.
* Recover schedules after device restart.
* Respect the user's local time zone.
* Never send reminders or debt details to a remote server.
* Do not automatically record a repayment because a reminder fires.

A reminder should open the relevant debt record so the user can review and confirm what actually happened.

## 13. Database and Data Integrity

Add suitable local database entities or equivalent domain records for:

* Debt.
* DebtActivity or DebtEvent.
* DebtRepayment.
* DebtAttachment.
* DebtReminderSettings.

Use stable IDs, foreign keys, indexes, timestamps, and explicit debt direction.

Each debt should retain a complete activity trail. Do not store only a manually editable remaining balance without preserving the events that produced it.

Calculate outstanding balances from the original principal, additional advances, repayments, and explicit adjustments according to the selected accounting rules.

Use database transactions when saving linked debt events, repayments, and account ledger entries.

Support database migrations from existing Hisaab versions without losing transactions, receipts, accounts, or other data.

## 14. Search, Filters, and Reports

Support:

* Search by person's name.
* Filter by I Gave, I Took, active, partially paid, settled, and overdue.
* Sort by amount, due date, name, and last activity.
* View repayments by date.
* View monthly principal lent and borrowed.
* View monthly principal repaid and received.
* Export a debt statement for a selected person.
* Include a date range and a clear outstanding-balance summary in reports.

Distinguish original principal, additional advances, principal repayments, and interest. Never treat the total of all borrowing events as the outstanding debt without subtracting repayments and applying explicit adjustments.

## 15. Privacy and Confirmation

Debt information can be sensitive.

* Store debt data locally in app-private storage.
* Never upload names, phone numbers, notes, receipts, or debt amounts.
* Avoid logging personal debt details.
* Ask for confirmation before recording a repayment or additional advance.
* Ask for confirmation before deleting, writing off, or correcting a debt.
* Explain financial consequences before confirming significant adjustments.
* Preserve records and relationships during backup and restore.
* Do not send messages or payment requests to contacts automatically.
* Sharing a debt statement must be a deliberate user action with a preview.

## 16. Acceptance Tests

The feature is not complete until tests verify:

1. A user can create a debt using `I Gave Karz`.
2. A user can create a debt using `I Took Karz`.
3. Both directions show the correct person-facing wording.
4. Lending updates the tracked account and receivable correctly.
5. Borrowing updates the tracked account and liability correctly.
6. Historical debt entry does not silently alter the current account balance.
7. Partial repayments calculate outstanding principal correctly.
8. Multiple repayments preserve their history.
9. Additional advances increase principal correctly.
10. Principal repayments are not counted as ordinary income or expenses.
11. Optional interest is separated from principal.
12. Settled debts show zero outstanding principal.
13. Overdue status is correct when a due date exists.
14. Missing due dates do not produce false overdue warnings.
15. Dashboard summaries match the debt ledger.
16. Cash Wallet balances remain consistent with confirmed cash movements.
17. Duplicate repayment submissions do not create duplicate records.
18. Database failures do not leave a repayment without its corresponding account entry.
19. Backup and restore preserve debts, events, repayments, and attachments.
20. Search, filters, reminders, and reports use actual saved data.
21. No fabricated debt records are created on first launch.

## Definition of Done

Karz is complete when users can reliably record money they lend and borrow, track partial repayments and outstanding balances, see correct due-date statuses, review complete history, receive optional local reminders, and understand how each event affects Cash Wallet and their overall financial position.

The feature must follow the existing warm cream, black typography, lime-green accent, rounded-card design system and work entirely with real local data.
