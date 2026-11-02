# FLUXORA TEST MATRIX (v2.2 + 1:1 Showcase Verification)

## Summary
- **Total Automated Tests**: **122**
  - `ExampleUnitTest` (Money value class, Safe-to-Spend Today, Smart Quick-Add EN/HI/Hinglish, Splits, CINEMATIC v2.0 WCAG contrast & Liquid Glass tiers): **73 passed**
  - `ExampleRobolectricTest` (Compose UI screens, 1:1 Showcase Dashboard/Login/Splash, DataStore scoping, Notification deduplication, Gemini ViewModel, CINEMATIC v2.0 Inspector): **19 passed**
  - `RoommatePaymentAndSecurityTest` (UPI deep links, phone verification E.164, AppLockMonitor, BiometricAuthManager, Legal & Authors registry): **27 passed**
  - `FinanceRepositoryRuleTest` (Live Firebase Firestore + Auth Emulator zero-trust security rules): **3 passed**
- **Color & Token Governance**:
  - `node scripts/validate-tokens.mjs`: **16/16 WCAG AA/AAA checks passed (`0` failures)**
  - `node scripts/audit-colors.mjs`: **0 unauthorized raw color violations**

---

## Test Suite Breakdown

| Test Class | Category | Tests | Status |
|---|---|---|---|
| `com.example.ExampleUnitTest` | `Money` value class, `FinancialEngine`, `SmartQuickAddParser` (EN/HI/Hinglish), `RoommateExpenseEngine` (EQUAL/EXACT/PERCENTAGE/SHARES + greedy min-transfer), CSV round-trip, `CinematicContrastEngine` WCAG 2.1, `CinematicFluidScale`, `GlassTierResolver` | 73 | PASS |
| `com.example.ExampleRobolectricTest` | `FluxoraAuthScreen`, `OverviewDashboardScreen` (1:1 Showcase Net Spend, Month Selector, Cash-Flow Reactor, Monthly Cash Flow), `LedgerTransactionsScreen`, `PlanningScreen`, `RoommateSplitScreen`, `SettingsAndGovernanceScreen`, `CinematicDesignSystemShowcaseScreen`, `PreferencesManager`, `NotificationManager`, `GeminiChatViewModel` | 19 | PASS |
| `com.example.RoommatePaymentAndSecurityTest` | `UpiPaymentIntentBuilder`, `PhoneValidation`, `AppLockMonitor`, `AppCheckConfiguration`, `AuthorRegistry`, `LegalPrivacyContent` | 27 | PASS |
| `com.example.FinanceRepositoryRuleTest` | Live Firebase Firestore & Auth Emulator rules (`127.0.0.1:8085` / `127.0.0.1:9099`): owner isolation, unauthenticated rejection, cross-user roommate handshake & `phone_claims` uniqueness | 3 | PASS |
