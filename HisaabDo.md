# Hisaab Do Bhai — Unified Cash Wallet & Premium UI/UX Refinement

## 1. Your Role

Act as a senior Android engineer, product designer, financial-app architect, accessibility specialist, and QA engineer.

Build and refine **Hisaab Do Bhai**, a polished, reliable, offline-first personal finance application for Android.

This is a real expense-management app, not a visual prototype. Every displayed balance, button, transaction, chart, filter, setting, and automation must work correctly.

Before changing an existing project, inspect its codebase, current screens, data model, and existing functionality. Preserve working features and improve them rather than unnecessarily rebuilding them.

## 2. Critical Product Change: Combine Cash and Wallet

Cash and Wallet must be represented as **one unified account**, not two separate account types.

### Default account

* Create one default account named `Cash Wallet`.
* It represents money the user manages as a single combined balance.
* Do not create separate default accounts named Cash and Wallet.
* Do not show Cash and Wallet as separate account cards, categories, balances, or account filters.
* Allow the user to rename Cash Wallet.
* Allow the user to edit its opening balance through a validated, clearly explained adjustment flow.
* Allow users to create additional accounts for genuinely separate financial sources, such as a bank account or another separately tracked account.
* Do not automatically create accounts the user has not requested.

### Account management

Each account must have:

* Unique ID.
* Name.
* Account type where relevant.
* Opening balance in integer paisa.
* Calculated current balance.
* Creation and modification timestamps.
* Active or archived status.
* Optional account color or icon.

The default Cash Wallet must be created exactly once using an idempotent database initialization or migration.

### Migration requirements

If an existing installation has separate Cash and Wallet accounts:

1. Inspect the actual stored accounts and their associated transactions.
2. Provide a safe, transactional migration that merges them into the unified Cash Wallet model.
3. Preserve every transaction, timestamp, category, note, receipt attachment, and recurring rule.
4. Do not simply add the two current balances if doing so would double-count transactions or opening balances.
5. Reconcile opening balances and transaction history using the existing ledger.
6. Detect duplicate or conflicting records and report them for safe resolution.
7. Preserve a recoverable backup before migration.
8. Make the migration idempotent so it cannot merge the same data twice.
9. Verify account balances and ledger totals before and after migration.
10. If a safe automatic migration is impossible, stop and request a clear user decision. Never silently discard or alter financial records.

For a fresh installation, initialize only the intended default Cash Wallet account. Never populate the app with fictional transactions or demo balances.

## 3. Product and Platform Requirements

* Platform: native Android.
* Preferred stack: Kotlin, Jetpack Compose, Material 3, Room with SQLite, Kotlin Coroutines and Flow.
* Architecture: maintainable MVVM with repository and domain layers.
* Currency: Pakistani Rupees (PKR).
* Storage: local-only and offline-first.
* Authentication: no login or account registration.
* Receipts: locally stored receipt photos.
* Accounts: one unified default Cash Wallet plus optional additional accounts.
* Recurring transactions: require explicit user confirmation before recording each scheduled financial transaction.
* Network access: do not require a server, cloud account, or internet connection for core features.

## 4. Design Direction

Use the supplied `clay.html` and `clay-design.md` as the primary design references. Inspect their actual contents and reuse the established visual language instead of inventing a competing design system.

Preserve the design's defining characteristics:

* Warm ivory and cream surfaces.
* Strong, readable black typography.
* Restrained lime-green accents.
* Rounded cards and carefully controlled corner radii.
* Thin, subtle borders.
* Soft, purposeful shadows where appropriate.
* Playful clay-inspired illustrations used sparingly.
* Generous whitespace and a clear visual hierarchy.
* Refined icons and consistent alignment.
* A friendly, approachable financial personality.

Use the reference's actual tokens when available. Treat approximate colors such as `#FEFDFB`, `#F4F3F0`, and `#EEF773` as provisional fallbacks, not replacements for the supplied design system.

### Premium-quality requirements

The interface must look deliberately designed, not automatically generated.

* Establish a consistent spacing scale, typography scale, icon size system, card radius system, and component hierarchy.
* Use one primary accent color consistently.
* Avoid excessive gradients, glassmorphism, random bright colors, excessive shadows, and unnecessary decorative elements.
* Avoid emojis as substitutes for proper interface icons.
* Use subtle animations for transitions, balance updates, expanding sections, and feedback.
* Respect Android system animation and reduced-motion preferences where possible.
* Keep animations fast, smooth, and useful.
* Never animate financial amounts in a way that makes the actual value ambiguous.
* Use consistent light and dark themes if both are implemented; do not ship an unfinished theme.
* Support narrow screens, larger text settings, and system display scaling.
* Use polished typography, legible financial figures, and consistent alignment of monetary amounts.
* Make every screen visually consistent with the supplied design references.

## 5. Navigation and Main Screens

Use a simple bottom navigation system with four destinations:

1. Home
2. Add
3. Needs
4. Settings

Use clear icons and text labels. Highlight the active destination with the lime accent without making inactive destinations difficult to read.

Do not introduce unnecessary navigation destinations simply to make the app appear feature-rich.

### Home dashboard

The dashboard should answer three questions immediately:

1. How much money do I currently have?
2. How much money is available after my reserved amounts?
3. Where has my money gone recently?

Include:

* Total Balance.
* Remaining Balance.
* The selected month or reporting period.
* Income and expenses for the current month.
* A clear primary Add Transaction action.
* A compact spending breakdown.
* The five most recent transactions.
* A clear route to account management and transaction history.

Display Cash Wallet as the unified default account wherever account information is shown.

Do not show a Cash card next to a separate Wallet card.

### Financial terminology

Use consistent, understandable labels.

* **Total Balance:** the sum of current balances across all active, included accounts.
* **Remaining Balance:** Total Balance minus explicitly reserved amounts that are still relevant and not already spent.
* **Income:** recorded incoming money.
* **Expenses:** recorded outgoing spending.
* **Transfers:** movement between tracked accounts, not income or expenses.

Prevent double subtraction of reserved amounts. Explain the Remaining Balance calculation in a short tooltip or information sheet.

If no reserved amounts exist, Remaining Balance should equal Total Balance.

Never invent balances to make the dashboard appear populated. A new installation should show a genuine empty state and accurate zero values.

## 6. Transaction Entry

Make adding a transaction quick and intuitive.

Transaction types:

* Expense.
* Income.
* Transfer.

Required fields should be minimal. Show additional fields only when relevant.

### Expense and income fields

* Amount.
* Transaction type.
* Category.
* Account.
* Date and time.
* Optional description or merchant.
* Optional receipt photo.

### Transfer fields

* Source account.
* Destination account.
* Amount.
* Date and time.
* Optional note.

A transfer must not increase total money or appear as an expense. Validate that source and destination accounts are different and that the source has sufficient funds unless the user has explicitly enabled a supported overdraft policy.

### Form quality

* Use an easy-to-use amount keypad or numeric input.
* Format amounts consistently in PKR.
* Validate invalid, empty, zero, negative, or excessively large amounts.
* Preserve the form when validation fails.
* Prevent duplicate submissions caused by repeated taps.
* Show the selected account clearly.
* Use contextual fields rather than displaying every possible field at once.
* Ask for confirmation before destructive operations.
* Provide a clear success state only after the database operation succeeds.

## 7. Money and Ledger Integrity

Store money as integer paisa using `Long`. Never use floating-point arithmetic for financial calculations.

Centralize all balance calculations in the domain layer.

Calculate balances from the opening balance and relevant ledger entries rather than maintaining unrelated balance values that can drift apart.

Requirements:

* Validate all money inputs.
* Use database transactions for multi-record operations.
* Record transfers atomically.
* Prevent duplicate transaction creation.
* Maintain stable transaction IDs.
* Preserve original timestamps when editing or migrating records.
* Ensure editing or deleting a transaction correctly updates every affected balance and report.
* Record opening-balance changes as auditable adjustments rather than silently overwriting financial history.
* Recalculate or verify derived balances after migrations and recovery.
* Use consistent rounding and formatting rules for PKR.

Every dashboard balance, account balance, report, and spending chart must use the same underlying ledger rules.

## 8. Transaction History and Management

Provide a polished transaction history screen with:

* Search by description, merchant, or note.
* Date-range filters.
* Account filters.
* Category filters.
* Expense, income, and transfer filters.
* Sorting by newest, oldest, amount, and other useful criteria.
* Clear empty and no-results states.
* Transaction detail screens.
* Edit, delete, and undo where safe.

The account filter must display Cash Wallet as one account, not separate Cash and Wallet entries.

Use pagination or efficient database queries for large histories.

## 9. Categories and Budgets

Provide sensible, editable expense and income categories.

Categories should have consistent icons and restrained colors that work with the design system.

Users can:

* Create categories.
* Rename categories.
* Change category icons.
* Archive unused categories.
* Set monthly category budgets.
* View actual spending versus budget.
* See clear warnings when approaching or exceeding a budget.

Do not create fictional historical spending to populate charts.

For a new user, show a designed empty state explaining how to start tracking spending.

## 10. Remaining Balance and Reserved Money

Allow users to explicitly reserve money for a goal or upcoming obligation.

For example, a user may reserve PKR 5,000 from their current balance.

Reserved amounts must:

* Be clearly identified.
* Have an optional purpose and target date.
* Be editable and releasable.
* Be excluded from available money only while genuinely reserved.
* Never be recorded as an expense merely because the user reserved them.
* Never be deducted twice after the actual expense is recorded.

Show the calculation clearly and make it easy to understand.

## 11. Needs and Shopping Planner

Create a useful Needs screen for items the user intends to buy.

Support:

* Item name.
* Estimated price.
* Priority.
* Optional category.
* Optional target date.
* Purchased or not purchased status.
* Editing and deletion.
* Estimated total of outstanding needs.
* Optional explicit reservation of money for selected needs.

Adding an item to Needs must not automatically create an expense.

Marking an item as purchased must not silently record a payment. Offer a transaction creation flow and require the user to confirm the amount, account, and date.

## 12. Recurring Transactions and Local Automation

Support recurring income and expense rules, such as rent, bills, subscriptions, and salary.

Provide schedules for daily, weekly, monthly, and yearly recurrence where appropriate.

The app must:

* Use reliable Android scheduling mechanisms.
* Handle device restarts and time-zone changes carefully.
* Avoid duplicate occurrences.
* Show upcoming and overdue occurrences.
* Notify users when an occurrence is due, subject to notification permissions.
* Allow users to pause, edit, or cancel rules.
* Respect recurrence end dates and limits.
* Track each scheduled occurrence using a stable identifier.

**Critical financial rule:** scheduled events may generate reminders or pending items, but must never automatically create a completed financial transaction or deduct money without explicit user confirmation.

On confirmation, create the transaction once and link it to the relevant occurrence.

If a notification is missed, the app must recover pending reminders without recording fictional or duplicate payments.

## 13. Receipt Photos

Allow users to attach receipt photos to transactions.

* Use Android's system photo picker or camera functionality where appropriate.
* Store receipt files locally in app-private storage.
* Keep file references in the database rather than storing large image blobs unnecessarily.
* Generate thumbnails for transaction lists.
* Handle permissions and cancellation gracefully.
* Allow viewing, replacing, and deleting receipt attachments.
* Avoid orphaned files when transactions are deleted.
* Never upload receipts to a server.
* Exclude receipt files from logs and diagnostic output.
* Clearly explain export and backup behavior for receipt attachments.

## 14. Local Data Protection and Recovery

No login does not mean no security.

* Keep financial data in app-private storage.
* Avoid unnecessary permissions.
* Do not transmit financial records or receipts over the network.
* Avoid logging transaction details or sensitive receipt paths.
* Validate imported files before restoring them.
* Provide a complete local export and import workflow.
* Use versioned backups and a recoverable import process.
* Detect corrupted, incomplete, incompatible, or malicious import files.
* Never overwrite existing records without an explicit, clearly explained confirmation.
* Protect against duplicate imports.
* Keep database migrations versioned and tested.
* Explain the limits of uninstall recovery and Android device backups.

If encryption is implemented, use an established Android-supported cryptographic approach and proper key management. Do not invent custom cryptography or claim protection that has not been implemented and tested.

## 15. Settings and Account Management

Provide clean settings sections for:

* Accounts.
* Categories.
* Budgets.
* Reserved amounts.
* Recurring rules.
* Notifications.
* Theme and appearance.
* Export and import.
* Data integrity and recovery.
* Privacy information.
* About the app.

Account management should clearly distinguish Cash Wallet from optional additional accounts.

Before archiving or removing an account, explain what happens to its transaction history and linked recurring rules. Never delete financial history silently.

If an account contains transactions, prefer archiving or a safe migration flow over destructive deletion.

## 16. UX States and Interaction Quality

Every screen and operation must handle:

* First launch.
* Empty data.
* Loading.
* Successful completion.
* Validation errors.
* Database errors.
* Cancelled photo selection.
* Missing receipt files.
* Permission denial.
* Interrupted imports.
* Duplicate actions.
* Background scheduling failures.
* Migration failures.
* Large transaction histories.

Use clear language that tells the user what happened and what to do next.

Avoid generic error messages, fake progress indicators, decorative controls that do nothing, and success messages shown before an operation completes.

Use confirmation dialogs for destructive or financially significant actions, but do not make routine actions unnecessarily slow.

## 17. Accessibility and Localization

* Support Android accessibility services.
* Provide semantic labels for icons and controls.
* Maintain readable contrast.
* Support larger font sizes.
* Use touch targets suitable for mobile interaction.
* Avoid conveying financial meaning through color alone.
* Format dates and currency consistently.
* Use `PKR` or a consistent Pakistani Rupee presentation throughout the app.
* Handle large monetary amounts without clipping.
* Keep important amounts readable at common screen widths.

## 18. Architecture and Data Model

Use a maintainable structure separating:

* UI and Compose screens.
* ViewModels and UI state.
* Domain models and business rules.
* Repositories.
* Room entities and DAOs.
* Database migrations.
* Scheduling and notification services.
* Receipt file storage.
* Export and import services.
* Validation and money-formatting utilities.

Suggested persistent entities:

* Account.
* Transaction.
* Category.
* Budget.
* ReservedAmount.
* NeedItem.
* ReceiptAttachment.
* RecurringRule.
* RecurringOccurrence.
* AppSettings.

Use stable IDs, explicit relationships, suitable database indexes, and foreign-key rules.

Represent account archives and category archives safely. Keep derived financial values consistent with the ledger.

Avoid one giant ViewModel, duplicated business logic, direct database access from composables, and unnecessary dependencies.

## 19. Automated Testing and Acceptance Criteria

Implement and run relevant tests for:

1. A fresh installation creates exactly one default Cash Wallet.
2. No separate default Cash and Wallet accounts are created.
3. The user can create additional distinct accounts.
4. Existing Cash and Wallet data is migrated safely.
5. Re-running a migration does not duplicate or alter records.
6. The total balance is correct across active accounts.
7. Remaining Balance subtracts valid reserved amounts exactly once.
8. Expenses decrease the appropriate account balance.
9. Income increases the appropriate account balance.
10. Transfers preserve the total balance.
11. Transfers update both accounts atomically.
12. Editing and deleting transactions produce correct balances.
13. Invalid amounts are rejected.
14. Recurring transactions are never recorded before confirmation.
15. Confirming a recurring occurrence twice does not duplicate the transaction.
16. Needs items never create expenses without confirmation.
17. Receipt attachments remain linked correctly.
18. Export and import preserve records and relationships.
19. A new installation contains no fabricated transactions.
20. Dashboard totals, account details, and reports agree.
21. Offline operation works without a network connection.
22. Large histories remain responsive.
23. Accessibility labels and screen layouts remain usable at larger text sizes.

Use unit tests, Room/database tests, migration tests, ViewModel tests, and Compose UI tests where appropriate.

Do not claim tests passed unless they were actually executed. Report failures and unresolved issues honestly.

## 20. Anti-AI-Slop Requirements

Do not:

* Insert sample transactions, fake users, dummy balances, or fabricated reports.
* Add nonfunctional buttons or fake integrations.
* Generate random data to make the app look complete.
* Use placeholder screens as final implementations.
* Hide calculation errors behind attractive visuals.
* Duplicate Cash and Wallet under different labels.
* Automatically record scheduled payments.
* Silently delete financial records.
* Claim that an untested feature works.
* Replace real functionality with hardcoded demo values.
* Add unnecessary features solely to make the app appear more sophisticated.

Use realistic empty states, genuine database-driven content, clear validation, and predictable behavior.

## 21. Implementation Workflow

1. Inspect all existing project files and the supplied Clay design references.
2. Document the current architecture, data model, screens, and problems.
3. Identify the smallest safe changes needed to unify Cash and Wallet.
4. Define the database migration and financial reconciliation strategy.
5. Refine the design system and core reusable components.
6. Improve the dashboard, transaction entry, history, and account management first.
7. Implement and verify the remaining features without breaking existing functionality.
8. Run available tests, static analysis, and build checks.
9. Fix discovered issues and document anything that remains unresolved.
10. Maintain `brain.md` with architecture decisions, completed work, tests, known issues, and next steps.
11. Make focused Git commits after completed and verified implementation milestones, when Git is available.
12. Provide a final implementation report listing changed files, migrations, tests executed, build status, and remaining limitations.

Do not automatically launch the app, emulator, or device, or perform unrelated destructive actions. Wait for my explicit approval before running or launching the completed app.

## Definition of Done

Hisaab Do Bhai is complete when Cash and Wallet are represented as one unified Cash Wallet account, existing financial records are preserved, balances reconcile correctly, the shared Clay-inspired design is applied consistently, essential screens are polished, core functionality works offline, automated tests provide meaningful coverage, and there are no knowingly fake features or fabricated financial records.
