# CINEMATIC v2.0 Accessibility & WCAG 2.1 AA Contrast Audit

## 1. Standards Enforced
- **Normal Body Text (`< 18sp` / `< 14sp bold`)**: Minimum `4.5:1` contrast ratio.
- **Large Headline Text (`>= 18sp`) & Interactive Boundaries**: Minimum `3.0:1` contrast ratio.
- **Touch Targets**: Minimum `48dp × 48dp` across Android Jetpack Compose interactive controls (`44px × 44px` minimum in companion CSS).
- **Focus Ring**: `2dp` `ringFocus` (`#B9C9D8` Dark / `#526B80` Light) with `2dp` `ringOffset`.

---

## 2. Verified Contrast Matrix (`scripts/validate-tokens.mjs` & `ColorContrastAuditor`)

### Dark Mode Pairings (13 / 13 Passed)
| Pairing | Foreground | Background | Measured Ratio | Required | Status |
|---|---|---|---|---|---|
| `--fg-primary` on `--bg-canvas` | `#E4EDF5` | `#070B12` | `16.64:1` | `4.5:1` | PASS |
| `--fg-primary` on `--bg-primary` | `#E4EDF5` | `#0D1521` | `15.47:1` | `4.5:1` | PASS |
| `--fg-primary` on `--bg-secondary` | `#E4EDF5` | `#142235` | `13.54:1` | `4.5:1` | PASS |
| `--fg-secondary` on `--bg-canvas` | `#B9C9D8` | `#070B12` | `11.64:1` | `4.5:1` | PASS |
| `--fg-secondary` on `--bg-primary` | `#B9C9D8` | `#0D1521` | `10.82:1` | `4.5:1` | PASS |
| `--fg-muted` on `--bg-canvas` | `#6D91B2` | `#070B12` | `5.95:1` | `4.5:1` | PASS |
| `--fg-muted` on `--bg-primary` | `#6D91B2` | `#0D1521` | `5.53:1` | `4.5:1` | PASS |
| `--btn-primary-fg` on `--btn-primary-bg` | `#070B12` | `#B9C9D8` | `11.64:1` | `4.5:1` | PASS |
| `--btn-secondary-fg` on `--btn-secondary-bg` | `#E4EDF5` | `#1D3045` | `11.36:1` | `4.5:1` | PASS |
| `--status-success-fg` on `--status-success-bg` | `#8DBEAA` | `#10261F` | `7.64:1` | `4.5:1` | PASS |
| `--status-warning-fg` on `--status-warning-bg` | `#E7BE79` | `#2B2111` | `9.06:1` | `4.5:1` | PASS |
| `--status-danger-fg` on `--status-danger-bg` | `#E58C94` | `#2D1519` | `6.90:1` | `4.5:1` | PASS |
| `--status-info-fg` on `--status-info-bg` | `#85B9DB` | `#102334` | `7.59:1` | `4.5:1` | PASS |

### Light Mode Pairings (12 / 12 Passed)
| Pairing | Foreground | Background | Measured Ratio | Required | Status |
|---|---|---|---|---|---|
| `--fg-primary` on `--bg-canvas` | `#172A3D` | `#F3F6F9` | `13.49:1` | `4.5:1` | PASS |
| `--fg-primary` on `--bg-primary` | `#172A3D` | `#FFFFFF` | `14.63:1` | `4.5:1` | PASS |
| `--fg-secondary` on `--bg-canvas` | `#526477` | `#F3F6F9` | `5.62:1` | `4.5:1` | PASS |
| `--fg-secondary` on `--bg-primary` | `#526477` | `#FFFFFF` | `6.09:1` | `4.5:1` | PASS |
| `--fg-muted` on `--bg-canvas` | `#526477` | `#F3F6F9` | `5.62:1` | `4.5:1` | PASS |
| `--fg-subtle` (Large Text) on `--bg-primary` | `#718398` | `#FFFFFF` | `3.89:1` | `3.0:1` | PASS |
| `--btn-primary-fg` on `--btn-primary-bg` | `#FFFFFF` | `#172A3D` | `14.63:1` | `4.5:1` | PASS |
| `--btn-secondary-fg` on `--btn-secondary-bg` | `#172A3D` | `#E5ECF2` | `12.27:1` | `4.5:1` | PASS |
| `--status-success-fg` on `--status-success-bg` | `#287A58` | `#E8F5EF` | `4.67:1` | `4.5:1` | PASS |
| `--status-warning-fg` on `--status-warning-bg` | `#956019` | `#FBF3E4` | `4.80:1` | `4.5:1` | PASS |
| `--status-danger-fg` on `--status-danger-bg` | `#B83D4B` | `#FCECEE` | `4.83:1` | `4.5:1` | PASS |
| `--status-info-fg` on `--status-info-bg` | `#286B99` | `#EAF3FA` | `5.12:1` | `4.5:1` | PASS |

---

## 3. Documented Pairing Adjustment (Section 6 Rule 7)
In Light mode, `--primitive-slate-500` (`#718398`) has a `3.62:1` contrast ratio on `--primitive-cloud-50` (`#F3F6F9`) and `3.89:1` on `--primitive-snow-0` (`#FFFFFF`). While this exceeds the `3.0:1` threshold for large text (`>= 18sp`) and UI boundaries (`--border-strong`), it is below `4.5:1` for normal body text. Therefore, normal-sized muted text (`--fg-muted`) and input placeholders (`--input-placeholder`) in Light mode are paired with `--primitive-slate-600` (`#526477`, `5.62:1` on `#F3F6F9` and `6.09:1` on `#FFFFFF`), while `--primitive-slate-500` (`#718398`) is preserved for `foregroundSubtleLarge` (`>= 18sp`) and `--border-strong`.
