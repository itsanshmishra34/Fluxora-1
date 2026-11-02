# FLUXORA DESIGN MAP (Section 15 — 1:1 `Fluxora Finance App Showcase.png` Alignment)

## 1. Visual Precedence & Source of Truth
Per Section 15 (*"Provided design"*) of `Fluxora Master Engineering Prompt v2`, the uploaded **`Fluxora Finance App Showcase.png`** serves as the primary visual source of truth while preserving all Section 3 security, integer paise (`Money`), and essential-first rules.

---

## 2. Official Showcase Color Swatches (`Color.kt` & `Theme.kt`)

| Swatch Label in Showcase | Hex Value | Kotlin Token (`Color.kt`) | Usage |
|---|---|---|---|
| **Primary Emerald** | `#10B981` | `ShowcasePrimaryEmerald` | Brand `ora` wordmark, active nav tab, reactor core glow, bar charts, `+` FAB rim |
| **Accent Cyan** | `#22D3EE` | `ShowcaseAccentCyan` | Double-wing `F` gradient tip, orbital ring accent, splash progress bar |
| **Deep Background** | `#0B1220` | `ShowcaseDeepBackground` | Primary dark app canvas & Splash screen base |
| **Surface Dark** | `#121826` | `ShowcaseSurfaceDark` | Month selector pill, top icon buttons, bottom navigation bar, dark cards |
| **Surface Elevation** | `#1F2937` | `ShowcaseSurfaceElevation` | Elevated glass borders, dividers, and pill tracks |
| **Text Primary** | `#E2E8F0` | `ShowcaseTextPrimary` | Primary headings and telemetry figures on dark surfaces |
| **Text Secondary** | `#94A3B8` | `ShowcaseTextSecondary` | Subtitles, unselected nav items, and secondary labels |

---

## 3. Brand Identity & Adaptive Icon Mapping (Top Row of Showcase)

| Showcase Element | Implementation File(s) | Details |
|---|---|---|
| **App Icon (Launcher) (1024×1024)** | `res/mipmap-*/ic_launcher.png`, `res/mipmap-*/ic_launcher_round.png`, `res/drawable-nodpi/img_fluxora_launcher_icon.png` | Obsidian stone & glowing emerald-cyan double-wing `F` emblem |
| **Adaptive Icon Foreground (432×432)** | `res/drawable/ic_launcher_foreground.xml`, `FluxoraWingLogoIcon` (`FinancialHero3DScene.kt`) | Vector double-wing `F` with `#059669 -> #10B981 -> #22D3EE -> #CCFBF1` linear gradients and specular rim highlights |
| **Adaptive Icon Background (432×432)** | `res/drawable/ic_launcher_background.xml` | Midnight-to-emerald sky gradient (`#071018 -> #164E43`), layered mountain silhouettes, and glowing moon |
| **App Logo (Horizontal)** | `FluxoraBrandHeader` (`FinancialHero3DScene.kt`) | Double-wing `F` emblem + **`Fluxora`** (`Flux` in crisp white/dark ink, `ora` in `#10B981 -> #22D3EE` gradient) + **`FLOW • TRACK • GROW`** tracked subtitle |

---

## 4. Screen-by-Screen Mapping (`Fluxora Finance App Showcase.png`)

### A. Left Phone — Splash Screen (`Minimal • Calm • Premium`)
- **Composable**: `FluxoraSplashScreen` (`FluxoraAuthAndCoreScreens.kt`)
- **Visual Assets & Elements**:
  - Full-bleed atmospheric twilight mountain-lake & glowing eclipse artwork (`R.drawable.img_splash_mountain_lake`) with centered 3D double-wing `F` emblem and **`Fluxora` / `FLOW • TRACK • GROW`**.
  - Animated glowing emerald-to-cyan pill progress bar (`#10B981 -> #22D3EE`) at bottom.
  - Caption: **"Building a smarter financial future..."** in `#E2E8F0`.

### B. Center Phone — Login Screen (`Clean • Minimal • Elegant`)
- **Composable**: `FluxoraAuthScreen` (`FluxoraAuthAndCoreScreens.kt`) + `GoogleSignInAuthComponent` (`GoogleSignInAuthComponent.kt`)
- **Visual Assets & Elements**:
  - Frosted light botanical/architectural S-curve sheet (`ShowcaseLoginCardFrost`) over tropical leaf accents (`ShowcaseLeafDark`, `ShowcaseLeafMid`).
  - Top-left **`Fluxora`** (`FLOW • TRACK • GROW`) horizontal wordmark + `"Splash Art"` preview toggle pill.
  - Headline: **"Welcome Back"** + subtitle **"Continue your financial journey"**.
  - Frosted rounded-pill input fields (`ShowcaseLoginFieldBg`) for **Email address** (leading mail icon) and **Password** (leading lock icon + trailing eye visibility toggle).
  - Right-aligned **"Forgot Password?"** link (`ShowcaseLoginLinkTeal`) connected to `AuthManager.sendPasswordReset`.
  - Primary dark emerald glowing pill CTA: **"Sign In  →"** (`ShowcaseSignInBtnStart -> ShowcaseSignInBtnEnd` with glowing `#10B981` border, connected to `AuthManager.signInOrRegisterWithEmail`).
  - **`─── OR ───`** divider.
  - White elevated pill button: **`Continue with Google`** (`GoogleMulticolorLogoIcon` + `"Continue with Google"`, `google_sign_in_button` test tag, connected to Jetpack Credential Manager + Firebase Auth).
  - Footer: **"New here? Create an account"** mode toggle.

### C. Right Phone — Dashboard (Overview) (`Modern • Minimal • Focused`)
- **Composables**: `OverviewDashboardScreen` (`FluxoraAuthAndCoreScreens.kt`), `FinancialHero3DScene` (`FinancialHero3DScene.kt`), `FluxoraRootContent` (`MainActivity.kt`)
- **Visual Assets & Elements**:
  1. **Top Header Bar**: `FluxoraBrandHeader` on the left; circular dark glass **Notification Bell** button (with red unread dot `ShowcaseNotificationDot`) and **User Profile** button (with emerald rim) on the right.
  2. **Net Spend Frosted Light Mint Card (`available_to_spend_card`)**:
     - Left circular mint badge (`ShowcaseNetSpendIconBg`) with 4 vertical emerald bar columns.
     - Center **"Net Spend"** label + bold **`₹12,450`** figure (`formatShowcaseRupee`, respecting `LocalHideSensitiveAmounts`) + **`safe_to_spend_today_badge`**.
     - Right trend pill badge: **`↗ 12%`** (`ShowcaseTrendBadgeBg` / `ShowcaseTrendBadgeText`).
     - Right-half soft mint area sparkline wave drawn in Canvas.
  3. **Month Selector Pill Bar (`dashboard_month_selector_pill`)**:
     - Dark glass pill (`#121826`, border `#1F2937`): `<` chevron left, emerald calendar icon + **`October 2026`**, `>` chevron right.
  4. **Cash-Flow Reactor Card (`FinancialHero3DScene`)**:
     - Header: **"Cash-Flow Reactor"** + **"Visualize your money flow"** + **`● LIVE`** emerald pill badge.
     - Center viewport (`reactor_3d_canvas`): Stacked glowing zen stones artwork (`R.drawable.img_reactor_zen_stones`) + interactive touch-drag 3D gyroscopic orbital rings & energy particles + right-side stacked circular buttons (`[⛶]` reset orbit and `[🧊]` 3D mode).
     - Bottom 3 glass metric sub-cards: **Income (`₹28,500 ↑`)** in `#10B981`, **Net Flow (`₹12,450`)** in `#FFFFFF`, and **Expenses (`↑ ₹16,050`)** in `#EF4444`.
  5. **Monthly Cash Flow Card (`monthly_cash_flow_card`)**:
     - Frosted light card (`ShowcaseMonthlyCardBg`) with **"Monthly Cash Flow"** title + **"This Month ⌄"** pill dropdown.
     - 6 vertical pill-track bar columns for **`May`**, **`Jun`**, **`Jul`**, **`Aug`**, **`Sep`**, **`Oct`** with `#10B981 -> #6EE7B7` vertical gradient fills.
  6. **Floating Action Button (`+`) & Bottom Navigation Bar**:
     - Circular dark emerald-glass `+` FAB (`ShowcaseFabBg`) with bright glowing `#10B981` border at bottom-right.
     - 5-tab dark glass bottom navigation bar: **Overview**, **Transactions**, **Budgets**, **Roommates**, **More**.
