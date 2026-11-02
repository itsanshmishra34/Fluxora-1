# Implementation Plan - Fluxora Master Engineering Specification v2

## User Selected Preferences & Architecture Decisions
- **Default Launch Theme**: Midnight Emerald (Dark Mode) with live 8-theme switching.
- **Onboarding Flow**: 3-Screen Progressive Onboarding (Contextual permissions for notifications, biometrics, and features).
- **Currency Format**: Primary Symbol Style (`₹39,050.00`) across all KPI cards, with ISO prefix option (`INR`) for exports.
- **Identity & Application ID**:
  - `applicationId = com.aistudio.financetrack.qwrxnv`
  - `namespace = com.example`
  - App Name: `Fluxora`

---

## Technical Architecture & Core Modules

### 1. Money & Financial Arithmetic Engine (`:core:money`)
- **Integer Paise Precision**: Value class `Money(val amountMinor: Long)` ensuring exact paise arithmetic (1 INR = 100 paise) to prevent floating-point rounding errors.
- **Grouping & Formatting**: Indian number grouping (`INR 1,23,456.78` / `₹1,23,456.78`) with optional sign prefixing and privacy masking (`₹••••••`).
- **Date Validation**: `DateValidator.validateTransactionDate` allowing past/today dates and rejecting future-dated records in the default `Asia/Kolkata` time zone.

### 2. Design System & Glass UI (`:core:design`)
- **8 Signature Themes**: Midnight Emerald (default), Cyber Violet, Solar Gold, Arctic Cyan, Crimson Pulse, Obsidian Rose, Aurora Borealis, and Monochrome Slate.
- **3D Cash-Flow Reactor (`FinancialHero3DScene`)**: Interactive Canvas 3D orbital core with touch-drag rotation, animated particle streams, income/expense rings, and real-time net position telemetry.
- **Dynamic Island Capsules**: Top floating status capsule surfacing live cash-flow health, active budget warnings (80% / 100%), and quick navigation shortcuts.
- **Privacy & Masking**: Global `LocalHideSensitiveAmounts` CompositionLocal backed by DataStore to mask figures across all screens.

### 3. Authentication & Security Layer (`:core:security`)
- **Multi-Modal Auth**: Google Sign-In via Jetpack `CredentialManager`, Email/Password, and Firebase Phone Auth OTP.
- **Biometric App Lock & Inactivity Monitor**: Hardware biometric gate with configurable inactivity timeout and session cleanup.
- **Per-User Phone Ownership Scoping**: Verified E.164 phone numbers scoped per user UID (`isPhoneVerifiedFlow`, `getVerifiedPhoneFlow`).

### 4. Cloud Data Layer & Repositories (`:data`)
- **Firestore Integration**: Reactive StateFlows with zero-trust Firestore security rules.
- **Core Entities**: Transactions, Categories (12 default + custom), Category Budgets, Savings Goals with 25/50/75/100% milestone notifications, Atomic Recurring Bills, Household Members, Shared Expenses, Roommate Connection Requests, and Debts.

### 5. Roommate Split Ledger & UPI Settlement
- **4 Split Methods**: `EQUAL`, `EXACT`, `PERCENTAGE`, and `SHARES` with deterministic paise remainder allocation.
- **UPI Deep-Link Handoff**: `upi://pay` intent generation with VPA validation and dual-confirmation settlement states (`CONFIRMED_BY_RECEIVER`).

### 6. Debt Payoff Calculator (Snowball & Avalanche)
- **Debt Snowball ⚡**: Ranks debts by lowest balance first for rapid momentum.
- **Debt Avalanche 🏔️**: Ranks debts by highest APR % first for maximum interest savings.
- **Extra Payment Simulator**: Real-time schedule recalculation and comparison banners.

### 7. AI Financial Advisor & Supporting Features
- **AI Advisor**: Firebase AI Logic (Gemini Developer API) with daily saving insights, fallback rule engine, and interactive chat (`GeminiChatDialog`).
- **Data Portability & CSV**: UTF-8 CSV Export with formula injection defense (prefixing `=`, `+`, `-`, `@`) and CSV Import preview with deduplication.
- **Legal & Authors**: DPDP Act compliant 8-accordion Privacy Policy & Terms in `LegalScreens.kt` and `AuthorsScreen.kt` featuring Ayush Mishra, Sunaina Bansal, and Rohan Kumar.

---

## Verification & Execution Plan
1. Validate `applicationId` and `strings.xml` resource branding.
2. Verify all Kotlin source code compilation via `compile_applet`.
3. Confirm zero color token violations and accessibility contrast checks.
4. Deliver short, scannable status summary to user upon completion.
