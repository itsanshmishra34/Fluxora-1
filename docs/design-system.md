# CINEMATIC v2.0 + Fluxora Liquid Glass Design System

## 1. Visual Direction & Philosophy
**CINEMATIC v2.0** establishes a restrained, high-craft financial interface built around **Moonlight Silver (`#B9C9D8`)**, **Glacial Blue (`#6D91B2`)**, and **Deep Midnight (`#070B12`..`#26394D`)** in Dark mode, paired with **Cloud (`#F3F6F9`..`#D0DCE7`)**, **Snow (`#FFFFFF`)**, and **Midnight Ink (`#172A3D`)** in Light mode.

Combined with the **Fluxora Liquid Glass (`:core:glass`)** 3-tier adaptive surface system (`FULL` on API 31+, `SOFT` on API 26–30, `FROST` on API <26 / Low-RAM / Battery Saver), every screen maintains crisp optical legibility without visual noise.

---

## 2. Three-Layer Token Architecture

1. **Layer A — Primitive Tokens** (`styles/primitives.css` & `internal object CinematicPrimitives` in `CinematicDesignSystem.kt`):
   - Raw hex values only. Never referenced directly by UI components.
2. **Layer B — Semantic Tokens** (`styles/semantic-tokens.css`, `styles/themes.css` & `CinematicSemanticTokens`):
   - Purpose-driven tokens (`backgroundCanvas`, `surfaceDefault`, `foreground`, `foregroundSecondary`, `foregroundMuted`, `accentPrimary`, `accentSecondary`, `statusSuccessFg`, `chart1`..`chart6`, `borders`, `shadows`, `gradients`).
3. **Layer C — Component Tokens** (`CinematicComponentTokens`):
   - Component-level tokens (`btnPrimaryBg`, `btnPrimaryFg`, `inputBg`, `inputBorder`, `navBg`, `cardBg`, `tableHeaderBg`) mapped strictly from Layer B.

---

## 3. Adaptive Theme Engine (`Dark` / `Light` / `System`)
- **Dark Mode**: Deep midnight atmosphere (`#070B12` canvas, `#0D1521` primary surface, `#E4EDF5` primary foreground, `#B9C9D8` Moonlight Silver accent).
- **Light Mode**: Soft cloud atmosphere (`#F3F6F9` canvas, `#FFFFFF` primary surface, `#172A3D` primary foreground, `#526B80` Glacial Blue accent).
- **System Mode**: Automatically tracks `isSystemInDarkTheme()` / `prefers-color-scheme: dark` and persists the user's choice in Jetpack DataStore (`PreferencesManager.themeModeFlow`).

---

## 4. Typography System
Bundled locally in `app/src/main/res/font/`:
- **Display / Headlines**: `Manrope` (`manrope.ttf`)
- **UI & Body Copy**: `Inter` (`inter.ttf`)
- **Financial Figures, Ledger Tables & Token Codes**: `IBM Plex Mono` (`ibm_plex_mono.ttf`) with tabular numerals (`tnum`).

---

## 5. Fluid Responsive System (`320dp` to `2560dp`)
Implemented in `styles/fluid.css` and `CinematicFluidScale.forViewportWidthDp(widthDp)`:
- **Fluid Typography**: `--text-xs` through `--text-display` using exact `clamp(minRem, preferredRem + vwSlope, maxRem)` formulas.
- **Fluid Spacing**: `--space-1` through `--space-16` plus `--page-gutter: clamp(1rem, 3vw, 3rem)`.
- **Structural Breakpoints**:
  - `xs` (`360dp`): 1 column
  - `sm` (`640dp`): 2 columns
  - `md` (`768dp`): 6 columns
  - `lg` (`1024dp`): 12 columns
  - `xl` (`1280dp`): 12 columns
  - `2xl` (`1536dp`): 12 columns

---

## 6. Approved Gradient System
Only four approved gradients exist in the system (`styles/gradients.css` & `CinematicGradients`):
1. **Midnight Horizon**: `135deg, #070B12 -> #0D1521 -> #142235`
2. **Silver Moonlight**: `135deg, #B9C9D8 -> #6D91B2`
3. **Deep Atmosphere**: `radial-gradient(circle at top, rgba(109,145,178,0.16), rgba(7,11,18,0) 70%)`
4. **Lunar Silver**: `135deg, #E4EDF5 -> #B9C9D8 -> #6D91B2`
