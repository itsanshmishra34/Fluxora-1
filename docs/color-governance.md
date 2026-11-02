# CINEMATIC v2.0 Color Governance

## 1. Non-Negotiable Color Rules
1. **Zero Raw Hex in UI Components**: Components in `app/src/main/java/com/example/ui/components/` and `app/src/main/java/com/example/ui/screens/` must never hardcode `#RRGGBB` or `Color(0xFF...)`.
2. **Single Source of Truth**:
   - Primitive tokens are restricted to `styles/primitives.css` and `internal object CinematicPrimitives` in `CinematicDesignSystem.kt` (plus registered theme/glass files in `scripts/color-exceptions.json`).
3. **Identical Token Names Across Themes**:
   - Both Dark and Light modes expose the exact same 101 semantic/component CSS variables and `CinematicSemanticTokens` properties.

---

## 2. Approved Primitive Palette

### Dark Core Primitives
| Token | Hex | Role |
|---|---|---|
| `--primitive-midnight-950` | `#070B12` | Primary app background (`--bg-canvas`) |
| `--primitive-midnight-900` | `#0D1521` | Primary surface / card base (`--bg-primary`) |
| `--primitive-midnight-850` | `#142235` | Elevated surface / panel (`--bg-secondary`) |
| `--primitive-midnight-800` | `#1D3045` | Interactive surface / hover (`--bg-tertiary`) |
| `--primitive-midnight-750` | `#26394D` | Active surface / selection (`--surface-active`) |
| `--primitive-slate-700` | `#40566C` | Muted border / divider / subtle text |
| `--primitive-glacial-400` | `#6D91B2` | Secondary accent / muted text (`5.95:1` on canvas) |
| `--primitive-moonlight-200` | `#B9C9D8` | Primary brand accent / secondary text (`11.64:1` on canvas) |
| `--primitive-moonlight-100` | `#E4EDF5` | Primary headline & body text (`16.64:1` on canvas) |
| `--primitive-frost-300` | `#8BA9A9` | Cool atmospheric highlight (`--accent-tertiary`) |

### Light Core Primitives
| Token | Hex | Role |
|---|---|---|
| `--primitive-cloud-50` | `#F3F6F9` | Light app background (`--bg-canvas`) |
| `--primitive-snow-0` | `#FFFFFF` | Primary card surface (`--bg-primary`) |
| `--primitive-cloud-100` | `#E5ECF2` | Secondary surface (`--bg-secondary`) |
| `--primitive-cloud-200` | `#D0DCE7` | Interactive surface / border (`--bg-tertiary`) |
| `--primitive-slate-500` | `#718398` | Subtle large text (`>=18sp`) & strong border (`3.89:1` on `#FFFFFF`) |
| `--primitive-slate-600` | `#526477` | Secondary & normal-sized muted text (`5.62:1` on `#F3F6F9`, `6.09:1` on `#FFFFFF`) |
| `--primitive-glacial-600` | `#526B80` | Light mode primary accent (`5.66:1` on `#FFFFFF`) |
| `--primitive-midnight-900-ink` | `#172A3D` | Primary text in light mode (`13.49:1` on `#F3F6F9`, `14.63:1` on `#FFFFFF`) |

---

## 3. Automated Enforcement Commands
- **Token & Contrast Validation**:
  ```bash
  node scripts/validate-tokens.mjs
  ```
- **Raw Color & Forbidden Utility Audit**:
  ```bash
  node scripts/audit-colors.mjs
  ```
Both scripts exit with code `0` on full compliance and `1` on any violation.
