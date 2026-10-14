# Hisaab Do Bhai (حساب دو بھائی) — Full Implementation & Polish Plan

A production-grade, offline-first personal finance and debt management Android application built with Kotlin, Jetpack Compose, and Room. Designed around mathematical accuracy in Pakistani Rupees (PKR), a cohesive Clay-inspired aesthetic, and full compliance with `Hisaab_Do_Bhai_Fully_Updated_Implementation_Prompt.md`.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following user preferences were confirmed during Phase 1 interactive clarification:
> - **Primary Priority:** Full end-to-end implementation across all prompt sections (accounting integrity, Karz debt/credit-item ledger, Clay Design System polish, transaction detail views, receipt capture, recurring automation, and data safety).
> - **Navigation Hierarchy:** 5 bottom navigation tabs (`Home`, `Karz`, `Add`, `Needs`, `Settings`) for immediate discoverability of core modules.
> - **Design Aesthetic:** Signature Clay Warm Cream canvas (`#FEFDFB`), ivory elevation layers (`#F4F3F0`, `#FFFFFF`), crisp black typography (`#000000`), and restrained Lime accents (`#EEF773` / `#CBD810`).

---

## 1. Overview & Core Concept

- **What It Does**: A completely private, offline personal ledger and debt tracker for individuals, freelancers, and small shop/household managers in Pakistan. Tracks cash flow, unified Cash Wallet balances, shopping needs, and informal loans/credit items (both money and physical goods/groceries/utilities received on credit) without requiring accounts, logins, or cloud access.
- **Target Audience / Persona**: Everyday Pakistani users managing household budgets, shared family expenditures, and informal peer-to-peer credit (*Karz-e-Hasana*, shop credit, borrowed goods) who demand total privacy, zero fake data, and rock-solid balance integrity.
- **Key Value**: Guarantees zero arithmetic discrepancies by storing all amounts in integer paisa (`Long`). Differentiates money borrowed from items/food received on credit so cash accounts are never falsely inflated.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Dashboard & Balance Inspection**:
   - User opens the app to see their **Total Balance** (liquid cash across active accounts) and **Remaining Balance** (Total Balance minus active reserved funds).
   - Monthly summary cards breakdown verified income vs. expense, spending progress against set category budgets, and a compact Karz status card.
   - Five most recent entries display with quick actions and explicit "View Details" access.

2. **Bidirectional Karz Management (`KarzScreen` & `AddKarzScreen`)**:
   - Dedicated tab with toggleable filters: *Active*, *Due Soon*, *Overdue*, and *Settled*.
   - **`+ I Gave Karz`**: User records money lent, goods provided, or bills covered for another person. Explicitly prompts whether cash left Cash Wallet or is a historical/non-cash entry.
   - **`+ I Took Karz`**: Prompts the mandatory choice:
     1. *Money*: Borrowed cash deposited into an account (or recorded historically).
     2. *Item or expense on credit*: Food, groceries, bill paid by another person. **Default: No cash account movement.** Outstanding liability recorded without inflating Cash Wallet.
     3. *Other non-cash value*: Custom described good or service.
   - **Settlement Flow**: Detail screen features contextual actions: `Mark as Paid Back` (full settlement) and `Record Partial Repayment`, asking whether money was paid from Cash Wallet, another account, or settled non-cash/waived.

3. **Transaction Details & History Drilldown**:
   - Every transaction row on Home and History contains a clear, accessible `View Details` trigger opening a comprehensive detail sheet/dialog showing exact timestamps, category, account, merchant, note, and receipt attachment.
   - For Karz-linked events, a direct link navigates immediately to the underlying debt ledger.

4. **Needs & Shopping Planner (`NeedsScreen`)**:
   - Manage pending shopping needs with priority indicators and estimated PKR prices.
   - Marking a need as fulfilled opens a guided transaction or Karz creation flow; never silently mutates account balances.

### Visual Identity & Clay Design Tokens

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Clay Design System Palette                      │
├────────────────────────────────────────────────────────────────────────┤
│  Background Canvas    :  ClayBackground        (#FEFDFB)               │
│  Secondary Surface    :  ClaySurface           (#F4F3F0)               │
│  Card Elevation Fill  :  ClaySurfaceElevated   (#FFFFFF)               │
│  Primary Ink          :  ClayTextPrimary       (#000000)               │
│  Secondary Copy       :  ClayTextSecondary     (#55534E)               │
│  Border Hairline      :  ClayBorderHairline    (#D1CDC7 - 1dp stroke)  │
│  Active Accent        :  ClayAccentLime        (#EEF773)               │
│  Accent Button Fill   :  ClayAccentLimeDeep    (#CBD810)               │
│  Solid Button Ink     :  ClayInkDeep           (#1B1A18)               │
│  Card Corner Radii    :  16.dp - 24.dp pill/squircle                   │
└────────────────────────────────────────────────────────────────────────┘
```

- **Typography**: High-contrast, bold display numbers with tabular figure styling for monetary values. Clear distinction between primary captions and muted metadata (`sp` scaling throughout).
- **Surface Atmosphere**: Tactile, layered cards with subtle hairlines (`1.dp` border) and soft elevation. Avoids heavy drop-shadows and garish gradients.
- **Micro-Interactions**: M3 ripple effects on all clickable surfaces (`minimumInteractiveComponentSize(48.dp)`), animated filter chips, smooth expand/collapse accordions for debt event histories, and tactile bottom sheets.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Integer Paisa Arithmetic (`Long`)**
  - *Approach*: All currency values are stored as `Long` paisa (1 PKR = 100 paisa) throughout the database and repository. Division/multiplication rounding errors inherent to IEEE 754 floating-point numbers are mathematically eliminated.
  - *Trade-Off*: Requires explicit format conversions (`toPaisa()`, `toRupeesString()`), but guarantees 100% financial precision.

- **Decision 2: Separation of Debt Consideration Types (Money vs. Non-Cash Goods/Bills)**
  - *Approach*: When a user takes credit for groceries, food, or a utility bill paid by someone else, the app records an accounts-payable liability **without** adding money to Cash Wallet.
  - *Why*: Borrowing an item or having someone buy dinner does not put cash in the user's pocket. Conflating non-cash debt with cash income creates phantom bank balances and causes real-world overdrafts.

- **Decision 3: Single Unified Cash Wallet Account**
  - *Approach*: The database initializes exactly one default liquid account named "Cash Wallet". Users can create additional accounts (e.g., "Meezan Bank", "Easypaisa") or rename Cash Wallet, but "Cash" and "Wallet" are never bifurcated into separate default accounts.
  - *Why*: Directly aligns with user reality where physical cash in hand is a single pool of funds.

- **Decision 4: Explicit Confirmation for Automation and Purchases**
  - *Approach*: Recurring transactions and fulfilled Needs items require explicit user confirmation before recording financial ledger movements. Automated background tasks generate reminder notifications, but never silently create financial transactions without human confirmation.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                               Presentation Layer                                 │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌─────────────┐ ┌──────────┐ │
│  │  HomeScreen  │ │  KarzScreen  │ │   AddScreen  │ │ NeedsScreen │ │ Settings │ │
│  └──────┬───────┘ └──────┬───────┘ └──────┬───────┘ └──────┬──────┘ └─────┬────┘ │
│         │                │                │                │              │      │
│         └────────────────┼────────────────┼────────────────┼──────────────┘      │
│                          ▼                ▼                ▼                     │
│                   ┌──────────────────────────────────────────────┐               │
│                   │               HisaabViewModel                │               │
│                   │  (StateFlow: Balances, Debts, History, Needs)│               │
│                   └──────────────────────┬───────────────────────┘               │
├──────────────────────────────────────────┼───────────────────────────────────────┤
│                               Domain / Repository Layer                          │
│                   ┌──────────────────────▼───────────────────────┐               │
│                   │               HisaabRepository               │               │
│                   │  - Idempotent Cash Wallet Setup              │               │
│                   │  - Atomic Debt & Repayment Engine            │               │
│                   │  - JSON Backup/Restore & CSV Statement       │               │
│                   │  - Image File Store for Receipts             │               │
│                   └──────────────────────┬───────────────────────┘               │
├──────────────────────────────────────────┼───────────────────────────────────────┤
│                               Persistence Layer                                  │
│                   ┌──────────────────────▼───────────────────────┐               │
│                   │             Room SQLite Database             │               │
│                   │  ┌──────────────┐ ┌────────────────────────┐ │               │
│                   │  │   accounts   │ │      transactions      │ │               │
│                   │  ├──────────────┤ ├────────────────────────┤ │               │
│                   │  │    debts     │ │      debt_events       │ │               │
│                   │  ├──────────────┤ ├────────────────────────┤ │               │
│                   │  │    needs     │ │   recurring_rules      │ │               │
│                   │  └──────────────┘ └────────────────────────┘ │               │
│                   └──────────────────────────────────────────────┘               │
└──────────────────────────────────────────────────────────────────────────────────┘
```

### Core Entities & Database Schema

1. **`accounts`**: `id`, `name`, `type`, `colorHex`, `iconName`, `openingBalancePaisa`, `isArchived`, `createdAt`, `updatedAt`. Default seeded: single `Cash Wallet`.
2. **`transactions`**: `id`, `type` (`EXPENSE`, `INCOME`, `TRANSFER`), `amountPaisa`, `accountId`, `toAccountId`, `categoryId`, `description`, `note`, `receiptPath`, `timestampEpochMs`, `linkedDebtId`, `linkedDebtEventId`.
3. **`debts`**: `id`, `direction` (`GAVE`, `TOOK`), `considerationType` (`MONEY`, `ITEM_GOODS`, `BILL_EXPENSE`, `OTHER_NON_CASH`), `personName`, `itemDescription`, `originalPrincipalPaisa`, `dueDateEpochMs`, `status` (`ACTIVE`, `PARTIALLY_PAID`, `SETTLED`, `WRITTEN_OFF`), `accountId`, `notes`, `receiptPath`, `createdAtEpochMs`.
4. **`debt_events`**: `id`, `debtId`, `eventType` (`ADVANCE`, `REPAYMENT`, `CORRECTION`, `FORGIVENESS`), `amountPaisa`, `timestampEpochMs`, `settlementType` (`CASH_WALLET`, `OTHER_ACCOUNT`, `NON_CASH_SETTLED`, `WAIVED`), `accountId`, `note`, `receiptPath`.
5. **`needs`**: `id`, `name`, `estimatedPricePaisa`, `priority`, `category`, `isDone`, `doneAtEpochMs`, `targetDateEpochMs`.
6. **`recurring_rules`**: `id`, `title`, `amountPaisa`, `type`, `categoryId`, `accountId`, `frequency`, `nextDueEpochMs`, `isPaused`.

### Implementation Sequence

1. **Audit & Navigation Alignment**:
   - Verify `NavScreen.kt` and `HisaabApp.kt` bottom navigation bar correctly binds all 5 destinations (`Home`, `Karz`, `Add`, `Needs`, `Settings`) with proper insets (`navigationBarsPadding()`), active lime pill styling, and back navigation.
2. **Transaction Entry & Explicit View Details**:
   - Ensure `TransactionRow` and transaction lists across `HomeScreen` and History offer an explicit, discoverable `View Details` modal/dialog with full record introspection, receipt zoom, edit, and safe delete.
   - For transactions linked to debts (`linkedDebtId`), link directly to the target debt record.
3. **Karz Ledger & Non-Cash Debt Fidelity**:
   - Confirm `AddKarzScreen` prominently presents the three consideration types for `I Took Karz` with explanatory cards and icons.
   - Guarantee non-cash debts leave Cash Wallet unchanged at creation.
   - Confirm `DebtDetailScreen` enables `Mark as Paid Back`, `Record Partial Repayment`, and detailed chronological event tracking.
4. **Receipt Capture & Local Image Storage**:
   - Verify zero-permission Android Photo Picker (`ActivityResultContracts.PickVisualMedia`) and Camera flows save images locally into app-private internal storage, with zoomable previews and safe cleanup.
5. **Recurring Automation & Notification Permission**:
   - Confirm pending recurring items show review cards on `HomeScreen` requiring user tap to confirm or skip.
   - Handle Android 13+ `POST_NOTIFICATIONS` runtime permission with informative rationale dialog.
6. **Data Safety, Backup, & Export**:
   - Verify JSON backup/restore preserves all accounts, transactions, debts, events, and needs.
   - Confirm person-specific Karz statement export generates clean, human-readable summary text and CSV files via Android Sharesheet.
7. **Verification & Testing**:
   - Run `gradle :app:testDebugUnitTest` to verify all 35+ Robolectric and unit tests pass without errors.
   - Run `compile_applet` to guarantee a clean build.
