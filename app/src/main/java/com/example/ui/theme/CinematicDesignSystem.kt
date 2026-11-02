package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * ============================================================================
 * CINEMATIC v2.0 DESIGN SYSTEM — THREE-LAYER COLOR & FLUID ARCHITECTURE
 * ============================================================================
 * Visual Direction: Cinematic Minimalism
 * Signature Accent: Moonlight Silver (#B9C9D8)
 * Secondary Accent: Glacial Blue (#6D91B2)
 *
 * Layer A: Primitive Tokens (internal — never used directly in UI components)
 * Layer B: Semantic Tokens (15 categories — identical names in Dark & Light)
 * Layer C: Component Tokens (inherited from Layer B semantic tokens)
 */

enum class CinematicThemeMode(val id: String, val displayName: String) {
    DARK("DARK", "Dark"),
    LIGHT("LIGHT", "Light"),
    SYSTEM("SYSTEM", "System");

    companion object {
        fun fromId(id: String?): CinematicThemeMode {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DARK
        }
    }
}

/**
 * Typography families specified in Section 10:
 * - Manrope: headings and display typography
 * - Inter: body and interface typography
 * - IBM Plex Mono: technical labels and metadata
 */
val ManropeFontFamily = FontFamily(
    Font(R.font.manrope, FontWeight.Normal),
    Font(R.font.manrope, FontWeight.Medium),
    Font(R.font.manrope, FontWeight.SemiBold),
    Font(R.font.manrope, FontWeight.Bold)
)

val InterFontFamily = FontFamily(
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter, FontWeight.Medium),
    Font(R.font.inter, FontWeight.SemiBold),
    Font(R.font.inter, FontWeight.Bold)
)

val IbmPlexMonoFontFamily = FontFamily(
    Font(R.font.ibm_plex_mono, FontWeight.Normal),
    Font(R.font.ibm_plex_mono, FontWeight.Medium),
    Font(R.font.ibm_plex_mono, FontWeight.Bold)
)

/**
 * LAYER A: Approved Primitive Palette (Section 4).
 * Marked `internal` so application screens must consume Layer B or Layer C tokens.
 */
internal object CinematicPrimitives {
    // Dark Palette Primitives (Section 4)
    val Void950 = Color(0xFF070B12)      // Deepest background
    val Midnight900 = Color(0xFF0D1521)  // Main background
    val Ocean850 = Color(0xFF142235)     // Main surfaces
    val Storm800 = Color(0xFF1D3045)     // Elevated surfaces
    val Storm700 = Color(0xFF26394D)     // Dark border
    val Storm600 = Color(0xFF40566C)     // Strong border
    val Glacial500 = Color(0xFF6D91B2)   // Secondary accent
    val Moonlight400 = Color(0xFFB9C9D8) // Signature silver
    val Starlight100 = Color(0xFFE4EDF5) // Primary text
    val Mist400 = Color(0xFF8BA9A9)      // Atmospheric accent

    // Light Palette Primitives (Section 4)
    val Cloud50 = Color(0xFFF3F6F9)      // Main background
    val Snow0 = Color(0xFFFFFFFF)        // Primary surfaces
    val Silver100 = Color(0xFFE5ECF2)    // Secondary background
    val Frost200 = Color(0xFFD0DCE7)     // Borders
    val Slate500 = Color(0xFF718398)     // Subtle text (3.62:1 on Cloud50 -> large text / decorative)
    val Slate600 = Color(0xFF526477)     // Secondary text & accessible muted text (5.57:1 on Cloud50)
    val Slate700 = Color(0xFF526B80)     // Primary accent
    val Night900 = Color(0xFF172A3D)     // Primary text

    // Semantic Status Primitives — Dark (Section 4)
    val SuccessDark = Color(0xFF8DBEAA)
    val WarningDark = Color(0xFFE7BE79)
    val ErrorDark = Color(0xFFE58C94)
    val InfoDark = Color(0xFF85B9DB)

    // Semantic Status Primitives — Light (Section 4)
    val SuccessLight = Color(0xFF287A58)
    val WarningLight = Color(0xFF956019)
    val ErrorLight = Color(0xFFB83D4B)
    val InfoLight = Color(0xFF286B99)

    // Documented Status Surface Primitives (Section 4 & 5: separate status backgrounds from foregrounds)
    val SuccessSurfaceDark = Color(0xFF112622)
    val WarningSurfaceDark = Color(0xFF282115)
    val ErrorSurfaceDark = Color(0xFF2A181E)
    val InfoSurfaceDark = Color(0xFF122332)

    val SuccessSurfaceLight = Color(0xFFE6F3EE)
    val WarningSurfaceLight = Color(0xFFF9F1E6)
    val ErrorSurfaceLight = Color(0xFFFAEAEB)
    val InfoSurfaceLight = Color(0xFFE6F0F7)

    // Approved Gradient Stops (Section 7)
    val HorizonStopEnd = Color(0xFF34546C)
    val MoonlightStart = Color(0xFF101B2B)
    val MoonlightMid = Color(0xFF29445B)
    val AtmosphereTop = Color(0xFF365C78)
    val LunarSilverEnd = Color(0xFF91A9BC)
}

@Immutable
data class CinematicTokenMetadata(
    val name: String,
    val category: String,
    val hexDark: String,
    val hexLight: String,
    val purpose: String,
    val intendedUsage: String
)

@Immutable
data class CinematicGradients(
    val midnightHorizon: Brush,
    val silverMoonlight: Brush,
    val deepAtmosphere: Brush,
    val lunarSilver: Brush
)

@Immutable
data class CinematicShadows(
    val shadowXsColor: Color,
    val shadowSmColor: Color,
    val shadowMdColor: Color,
    val shadowLgColor: Color,
    val shadowXlColor: Color,
    val shadowGlowColor: Color,
    val shadowOverlayColor: Color,
    val elevationXs: Dp = 1.dp,
    val elevationSm: Dp = 2.dp,
    val elevationMd: Dp = 4.dp,
    val elevationLg: Dp = 8.dp,
    val elevationXl: Dp = 14.dp
)

@Immutable
data class CinematicBorders(
    val borderSubtle: Color,
    val borderDefault: Color,
    val borderStrong: Color,
    val borderFocus: Color,
    val borderInteractive: Color
)

@Immutable
data class CinematicChartPalette(
    val seriesPrimarySilver: Color,
    val seriesGlacialBlue: Color,
    val seriesAtmosphericMist: Color,
    val seriesIncomePositive: Color,
    val seriesExpenseNegative: Color,
    val seriesWarningCaution: Color,
    val gridLine: Color
)

/**
 * LAYER B: Semantic Tokens (Section 3 & Section 5).
 */
@Immutable
data class CinematicSemanticTokens(
    val isDark: Boolean,
    // Surfaces & Backgrounds
    val backgroundDeepest: Color,
    val background: Color,
    val backgroundSecondary: Color,
    val surface: Color,
    val surfaceElevated: Color,
    // Typography
    val foreground: Color,
    val foregroundSecondary: Color,
    val foregroundMuted: Color,
    val foregroundSubtleLarge: Color,
    val onPrimary: Color,
    val onSecondary: Color,
    // Interactive Brand
    val primary: Color,
    val primaryHover: Color,
    val primaryActive: Color,
    val secondary: Color,
    val secondaryHover: Color,
    val atmosphericAccent: Color,
    val disabledBackground: Color,
    val disabledForeground: Color,
    // Borders & Focus
    val border: Color,
    val borders: CinematicBorders,
    val focus: Color,
    // Status (Foreground + Distinct Background)
    val success: Color,
    val successBackground: Color,
    val warning: Color,
    val warningBackground: Color,
    val error: Color,
    val errorBackground: Color,
    val info: Color,
    val infoBackground: Color,
    // Overlays
    val overlayBackdrop: Color,
    val overlayScrim: Color,
    val overlaySubtle: Color,
    // Gradients, Shadows, Charts
    val gradients: CinematicGradients,
    val shadows: CinematicShadows,
    val charts: CinematicChartPalette
)

/**
 * LAYER C: Component Tokens (Section 3 & Section 12).
 * Every component inherits strictly from Layer B semantic tokens.
 */
@Immutable
data class CinematicComponentTokens(
    // Buttons & IconButtons
    val buttonPrimaryBg: Color,
    val buttonPrimaryText: Color,
    val buttonPrimaryHover: Color,
    val buttonSecondaryBg: Color,
    val buttonSecondaryText: Color,
    val buttonSecondaryBorder: Color,
    val buttonDisabledBg: Color,
    val buttonDisabledText: Color,
    val iconButtonBg: Color,
    val iconButtonFg: Color,
    // Cards & Surfaces
    val cardBg: Color,
    val cardText: Color,
    val cardBorder: Color,
    val elevatedCardBg: Color,
    val elevatedCardBorder: Color,
    val glassCardBg: Color,
    val glassCardBorder: Color,
    // Inputs, Select, Checkbox, Radio, Switch
    val inputBg: Color,
    val inputText: Color,
    val inputPlaceholder: Color,
    val inputBorder: Color,
    val inputFocus: Color,
    val controlCheckedBg: Color,
    val controlCheckedFg: Color,
    val controlUncheckedBorder: Color,
    // Navigation (Navbar, Sidebar, MobileNavigation, Tabs, Breadcrumb, Pagination)
    val navBg: Color,
    val navText: Color,
    val navActiveIndicatorBg: Color,
    val navActiveText: Color,
    val navBorder: Color,
    // Modals, Dialogs, Drawers, Popovers, Dropdowns
    val modalBg: Color,
    val modalBorder: Color,
    val modalBackdrop: Color,
    val dropdownBg: Color,
    val dropdownHoverBg: Color,
    // Badges, Tooltips, Skeletons, Separators
    val badgeDefaultBg: Color,
    val badgeDefaultText: Color,
    val badgeSuccessBg: Color,
    val badgeSuccessText: Color,
    val badgeWarningBg: Color,
    val badgeWarningText: Color,
    val badgeErrorBg: Color,
    val badgeErrorText: Color,
    val badgeInfoBg: Color,
    val badgeInfoText: Color,
    val tooltipBg: Color,
    val tooltipText: Color,
    val skeletonBaseBg: Color,
    val skeletonHighlightBg: Color,
    val separatorColor: Color,
    // Feedback (Alert, Toast, Progress, EmptyState, ErrorState)
    val alertBg: Color,
    val alertBorder: Color,
    val toastBg: Color,
    val toastText: Color,
    val progressTrackBg: Color,
    val progressIndicatorFg: Color
)

/**
 * FLUID DESIGN ENGINE (Section 9).
 * Calculates responsive typography (`--text-xs`..`--text-display`),
 * spacing (`--space-1`..`--space-16`), and container gutters (`--page-gutter`)
 * using mathematical `clamp()` functions across 320dp..2560dp viewports.
 */
@Immutable
data class CinematicFluidScale(
    val viewportWidthDp: Float = 411f,
    val textXs: TextUnit,
    val textSm: TextUnit,
    val textBase: TextUnit,
    val textLg: TextUnit,
    val textXl: TextUnit,
    val text2xl: TextUnit,
    val text3xl: TextUnit,
    val textDisplay: TextUnit,
    val space1: Dp,
    val space2: Dp,
    val space3: Dp,
    val space4: Dp,
    val space6: Dp,
    val space8: Dp,
    val space12: Dp,
    val space16: Dp,
    val pageGutter: Dp,
    val isCompact: Boolean,
    val isMedium: Boolean,
    val isExpanded: Boolean,
    val gridColumns: Int
) {
    companion object {
        private fun clampValue(minPx: Float, preferredPx: Float, maxPx: Float): Float {
            return max(minPx, min(preferredPx, maxPx))
        }

        fun forViewportWidthDp(widthDp: Float): CinematicFluidScale {
            val safeWidth = widthDp.coerceIn(320f, 2560f)
            val vw = safeWidth / 100f

            // 1rem = 16sp/16dp base
            val textXsSp = clampValue(12f, 11.52f + 0.12f * vw, 14f)
            val textSmSp = clampValue(14f, 13.12f + 0.18f * vw, 16f)
            val textBaseSp = clampValue(15f, 14.24f + 0.20f * vw, 18f)
            val textLgSp = clampValue(17f, 15.68f + 0.35f * vw, 20f)
            val textXlSp = clampValue(20f, 17.6f + 0.65f * vw, 24f)
            val text2xlSp = clampValue(24f, 19.2f + 1.0f * vw, 32f)
            val text3xlSp = clampValue(30f, 22.4f + 1.6f * vw, 40f)
            val textDisplaySp = clampValue(41.6f, 22.4f + 5.0f * vw, 88f)

            val space1Dp = clampValue(4f, 3.2f + 0.15f * vw, 8f)
            val space2Dp = clampValue(8f, 6.4f + 0.30f * vw, 12f)
            val space3Dp = clampValue(12f, 9.6f + 0.40f * vw, 16f)
            val space4Dp = clampValue(16f, 12.8f + 0.60f * vw, 24f)
            val space6Dp = clampValue(24f, 16f + 1.50f * vw, 40f)
            val space8Dp = clampValue(32f, 20.8f + 2.0f * vw, 56f)
            val space12Dp = clampValue(48f, 32f + 4.0f * vw, 112f)
            val space16Dp = clampValue(64f, 32f + 6.0f * vw, 144f)
            val gutterDp = clampValue(16f, 4.0f * vw, 64f)

            val usableWidth = (safeWidth - 2f * gutterDp).coerceAtLeast(280f)
            // auto-fit minmax(min(100%, 17rem = 272dp), 1fr)
            val columns = (usableWidth / 272f).toInt().coerceIn(1, 6)

            return CinematicFluidScale(
                viewportWidthDp = safeWidth,
                textXs = textXsSp.sp,
                textSm = textSmSp.sp,
                textBase = textBaseSp.sp,
                textLg = textLgSp.sp,
                textXl = textXlSp.sp,
                text2xl = text2xlSp.sp,
                text3xl = text3xlSp.sp,
                textDisplay = textDisplaySp.sp,
                space1 = space1Dp.dp,
                space2 = space2Dp.dp,
                space3 = space3Dp.dp,
                space4 = space4Dp.dp,
                space6 = space6Dp.dp,
                space8 = space8Dp.dp,
                space12 = space12Dp.dp,
                space16 = space16Dp.dp,
                pageGutter = gutterDp.dp,
                isCompact = safeWidth < 600f,
                isMedium = safeWidth in 600f..839.9f,
                isExpanded = safeWidth >= 840f,
                gridColumns = columns
            )
        }
    }
}

object CinematicTokenRegistry {

    fun buildDarkSemanticTokens(): CinematicSemanticTokens {
        val borders = CinematicBorders(
            borderSubtle = CinematicPrimitives.Storm700,
            borderDefault = CinematicPrimitives.Storm700,
            borderStrong = CinematicPrimitives.Storm600,
            borderFocus = CinematicPrimitives.Moonlight400,
            borderInteractive = CinematicPrimitives.Glacial500
        )
        val gradients = CinematicGradients(
            midnightHorizon = Brush.linearGradient(
                colorStops = arrayOf(
                    0.05f to CinematicPrimitives.Void950,
                    0.60f to CinematicPrimitives.Ocean850,
                    1.00f to CinematicPrimitives.HorizonStopEnd
                ),
                start = Offset.Zero,
                end = Offset(1000f, 1000f)
            ),
            silverMoonlight = Brush.linearGradient(
                colorStops = arrayOf(
                    0.00f to CinematicPrimitives.MoonlightStart,
                    0.50f to CinematicPrimitives.MoonlightMid,
                    1.00f to CinematicPrimitives.Moonlight400
                ),
                start = Offset.Zero,
                end = Offset(1000f, 1000f)
            ),
            deepAtmosphere = Brush.radialGradient(
                colorStops = arrayOf(
                    0.00f to CinematicPrimitives.AtmosphereTop,
                    0.45f to CinematicPrimitives.Ocean850,
                    1.00f to CinematicPrimitives.Void950
                ),
                center = Offset(500f, 900f),
                radius = 900f
            ),
            lunarSilver = Brush.linearGradient(
                colorStops = arrayOf(
                    0.00f to CinematicPrimitives.Moonlight400,
                    0.50f to CinematicPrimitives.Starlight100,
                    1.00f to CinematicPrimitives.LunarSilverEnd
                ),
                start = Offset.Zero,
                end = Offset(1000f, 360f)
            )
        )
        val shadows = CinematicShadows(
            shadowXsColor = CinematicPrimitives.Void950,
            shadowSmColor = CinematicPrimitives.Void950,
            shadowMdColor = CinematicPrimitives.Void950,
            shadowLgColor = CinematicPrimitives.Void950,
            shadowXlColor = CinematicPrimitives.Void950,
            shadowGlowColor = CinematicPrimitives.Glacial500,
            shadowOverlayColor = CinematicPrimitives.Void950
        )
        val charts = CinematicChartPalette(
            seriesPrimarySilver = CinematicPrimitives.Moonlight400,
            seriesGlacialBlue = CinematicPrimitives.Glacial500,
            seriesAtmosphericMist = CinematicPrimitives.Mist400,
            seriesIncomePositive = CinematicPrimitives.SuccessDark,
            seriesExpenseNegative = CinematicPrimitives.ErrorDark,
            seriesWarningCaution = CinematicPrimitives.WarningDark,
            gridLine = CinematicPrimitives.Storm700
        )
        return CinematicSemanticTokens(
            isDark = true,
            backgroundDeepest = CinematicPrimitives.Void950,
            background = CinematicPrimitives.Midnight900,
            backgroundSecondary = CinematicPrimitives.Void950,
            surface = CinematicPrimitives.Ocean850,
            surfaceElevated = CinematicPrimitives.Storm800,
            foreground = CinematicPrimitives.Starlight100,
            foregroundSecondary = CinematicPrimitives.Moonlight400,
            foregroundMuted = CinematicPrimitives.Moonlight400,
            foregroundSubtleLarge = CinematicPrimitives.Glacial500,
            onPrimary = CinematicPrimitives.Void950,
            onSecondary = CinematicPrimitives.Void950,
            primary = CinematicPrimitives.Moonlight400,
            primaryHover = CinematicPrimitives.Starlight100,
            primaryActive = CinematicPrimitives.Glacial500,
            secondary = CinematicPrimitives.Glacial500,
            secondaryHover = CinematicPrimitives.Moonlight400,
            atmosphericAccent = CinematicPrimitives.Mist400,
            disabledBackground = CinematicPrimitives.Storm700,
            disabledForeground = CinematicPrimitives.Glacial500,
            border = CinematicPrimitives.Storm700,
            borders = borders,
            focus = CinematicPrimitives.Moonlight400,
            success = CinematicPrimitives.SuccessDark,
            successBackground = CinematicPrimitives.SuccessSurfaceDark,
            warning = CinematicPrimitives.WarningDark,
            warningBackground = CinematicPrimitives.WarningSurfaceDark,
            error = CinematicPrimitives.ErrorDark,
            errorBackground = CinematicPrimitives.ErrorSurfaceDark,
            info = CinematicPrimitives.InfoDark,
            infoBackground = CinematicPrimitives.InfoSurfaceDark,
            overlayBackdrop = CinematicPrimitives.Void950,
            overlayScrim = CinematicPrimitives.Midnight900,
            overlaySubtle = CinematicPrimitives.Storm800,
            gradients = gradients,
            shadows = shadows,
            charts = charts
        )
    }

    fun buildLightSemanticTokens(): CinematicSemanticTokens {
        val borders = CinematicBorders(
            borderSubtle = CinematicPrimitives.Silver100,
            borderDefault = CinematicPrimitives.Frost200,
            borderStrong = CinematicPrimitives.Slate500,
            borderFocus = CinematicPrimitives.Slate700,
            borderInteractive = CinematicPrimitives.Slate600
        )
        val gradients = CinematicGradients(
            midnightHorizon = Brush.linearGradient(
                colorStops = arrayOf(
                    0.05f to CinematicPrimitives.Cloud50,
                    0.60f to CinematicPrimitives.Silver100,
                    1.00f to CinematicPrimitives.Frost200
                ),
                start = Offset.Zero,
                end = Offset(1000f, 1000f)
            ),
            silverMoonlight = Brush.linearGradient(
                colorStops = arrayOf(
                    0.00f to CinematicPrimitives.Snow0,
                    0.50f to CinematicPrimitives.Cloud50,
                    1.00f to CinematicPrimitives.Silver100
                ),
                start = Offset.Zero,
                end = Offset(1000f, 1000f)
            ),
            deepAtmosphere = Brush.radialGradient(
                colorStops = arrayOf(
                    0.00f to CinematicPrimitives.Silver100,
                    0.45f to CinematicPrimitives.Cloud50,
                    1.00f to CinematicPrimitives.Snow0
                ),
                center = Offset(500f, 900f),
                radius = 900f
            ),
            lunarSilver = Brush.linearGradient(
                colorStops = arrayOf(
                    0.00f to CinematicPrimitives.Silver100,
                    0.50f to CinematicPrimitives.Snow0,
                    1.00f to CinematicPrimitives.Frost200
                ),
                start = Offset.Zero,
                end = Offset(1000f, 360f)
            )
        )
        val shadows = CinematicShadows(
            shadowXsColor = CinematicPrimitives.Frost200,
            shadowSmColor = CinematicPrimitives.Frost200,
            shadowMdColor = CinematicPrimitives.Slate500,
            shadowLgColor = CinematicPrimitives.Slate500,
            shadowXlColor = CinematicPrimitives.Slate600,
            shadowGlowColor = CinematicPrimitives.Frost200,
            shadowOverlayColor = CinematicPrimitives.Night900
        )
        val charts = CinematicChartPalette(
            seriesPrimarySilver = CinematicPrimitives.Slate700,
            seriesGlacialBlue = CinematicPrimitives.InfoLight,
            seriesAtmosphericMist = CinematicPrimitives.Slate600,
            seriesIncomePositive = CinematicPrimitives.SuccessLight,
            seriesExpenseNegative = CinematicPrimitives.ErrorLight,
            seriesWarningCaution = CinematicPrimitives.WarningLight,
            gridLine = CinematicPrimitives.Frost200
        )
        return CinematicSemanticTokens(
            isDark = false,
            backgroundDeepest = CinematicPrimitives.Silver100,
            background = CinematicPrimitives.Cloud50,
            backgroundSecondary = CinematicPrimitives.Silver100,
            surface = CinematicPrimitives.Snow0,
            surfaceElevated = CinematicPrimitives.Snow0,
            foreground = CinematicPrimitives.Night900,
            foregroundSecondary = CinematicPrimitives.Slate600,
            // Accessible normal-text muted pairing (5.57:1 on Cloud50 and 6.14:1 on Snow0)
            foregroundMuted = CinematicPrimitives.Slate600,
            // Slate500 (#718398) reserved for >=18sp large text and >=3:1 UI boundaries
            foregroundSubtleLarge = CinematicPrimitives.Slate500,
            onPrimary = CinematicPrimitives.Snow0,
            onSecondary = CinematicPrimitives.Snow0,
            primary = CinematicPrimitives.Slate700,
            primaryHover = CinematicPrimitives.Night900,
            primaryActive = CinematicPrimitives.Night900,
            secondary = CinematicPrimitives.Slate600,
            secondaryHover = CinematicPrimitives.Night900,
            atmosphericAccent = CinematicPrimitives.Slate700,
            disabledBackground = CinematicPrimitives.Silver100,
            disabledForeground = CinematicPrimitives.Slate600,
            border = CinematicPrimitives.Frost200,
            borders = borders,
            focus = CinematicPrimitives.Slate700,
            success = CinematicPrimitives.SuccessLight,
            successBackground = CinematicPrimitives.SuccessSurfaceLight,
            warning = CinematicPrimitives.WarningLight,
            warningBackground = CinematicPrimitives.WarningSurfaceLight,
            error = CinematicPrimitives.ErrorLight,
            errorBackground = CinematicPrimitives.ErrorSurfaceLight,
            info = CinematicPrimitives.InfoLight,
            infoBackground = CinematicPrimitives.InfoSurfaceLight,
            overlayBackdrop = CinematicPrimitives.Night900,
            overlayScrim = CinematicPrimitives.Silver100,
            overlaySubtle = CinematicPrimitives.Silver100,
            gradients = gradients,
            shadows = shadows,
            charts = charts
        )
    }

    fun buildComponentTokens(semantic: CinematicSemanticTokens): CinematicComponentTokens {
        return CinematicComponentTokens(
            buttonPrimaryBg = semantic.primary,
            buttonPrimaryText = semantic.onPrimary,
            buttonPrimaryHover = semantic.primaryHover,
            buttonSecondaryBg = semantic.surfaceElevated,
            buttonSecondaryText = semantic.foreground,
            buttonSecondaryBorder = semantic.borders.borderStrong,
            buttonDisabledBg = semantic.disabledBackground,
            buttonDisabledText = semantic.disabledForeground,
            iconButtonBg = semantic.surfaceElevated,
            iconButtonFg = semantic.foreground,
            cardBg = semantic.surface,
            cardText = semantic.foreground,
            cardBorder = semantic.border,
            elevatedCardBg = semantic.surfaceElevated,
            elevatedCardBorder = semantic.borders.borderStrong,
            glassCardBg = semantic.surfaceElevated,
            glassCardBorder = semantic.borders.borderFocus,
            inputBg = semantic.surface,
            inputText = semantic.foreground,
            inputPlaceholder = semantic.foregroundMuted,
            inputBorder = semantic.border,
            inputFocus = semantic.focus,
            controlCheckedBg = semantic.primary,
            controlCheckedFg = semantic.onPrimary,
            controlUncheckedBorder = semantic.borders.borderStrong,
            navBg = semantic.backgroundSecondary,
            navText = semantic.foregroundSecondary,
            navActiveIndicatorBg = semantic.surfaceElevated,
            navActiveText = semantic.foreground,
            navBorder = semantic.border,
            modalBg = semantic.surfaceElevated,
            modalBorder = semantic.border,
            modalBackdrop = semantic.overlayBackdrop,
            dropdownBg = semantic.surfaceElevated,
            dropdownHoverBg = semantic.surface,
            badgeDefaultBg = semantic.surfaceElevated,
            badgeDefaultText = semantic.foreground,
            badgeSuccessBg = semantic.successBackground,
            badgeSuccessText = semantic.success,
            badgeWarningBg = semantic.warningBackground,
            badgeWarningText = semantic.warning,
            badgeErrorBg = semantic.errorBackground,
            badgeErrorText = semantic.error,
            badgeInfoBg = semantic.infoBackground,
            badgeInfoText = semantic.info,
            tooltipBg = semantic.surfaceElevated,
            tooltipText = semantic.foreground,
            skeletonBaseBg = semantic.surface,
            skeletonHighlightBg = semantic.surfaceElevated,
            separatorColor = semantic.border,
            alertBg = semantic.surfaceElevated,
            alertBorder = semantic.borders.borderStrong,
            toastBg = semantic.surfaceElevated,
            toastText = semantic.foreground,
            progressTrackBg = semantic.border,
            progressIndicatorFg = semantic.primary
        )
    }

    /**
     * Complete 15-Category Token Inventory required by Section 5.
     */
    val tokenInventory: List<CinematicTokenMetadata> = listOf(
        // 1. Brand Primitives
        CinematicTokenMetadata("moonlight-400", "1. Brand Primitives", "#B9C9D8", "#526B80", "Signature silver / primary brand accent", "Primary interactive surfaces & highlights"),
        CinematicTokenMetadata("glacial-500", "1. Brand Primitives", "#6D91B2", "#526477", "Secondary glacial blue accent", "Secondary accents, charts, interactive borders"),
        CinematicTokenMetadata("mist-400", "1. Brand Primitives", "#8BA9A9", "#526B80", "Atmospheric mist accent", "Ambient badges & tertiary data series"),
        // 2. Neutral Primitives
        CinematicTokenMetadata("void-950 / cloud-50", "2. Neutral Primitives", "#070B12", "#F3F6F9", "Deepest / base canvas primitive", "Root background layer"),
        CinematicTokenMetadata("midnight-900 / snow-0", "2. Neutral Primitives", "#0D1521", "#FFFFFF", "Primary surface/background neutral", "Main screen canvas and cards"),
        CinematicTokenMetadata("ocean-850 / silver-100", "2. Neutral Primitives", "#142235", "#E5ECF2", "Secondary surface neutral", "Cards, navigation bars, secondary containers"),
        CinematicTokenMetadata("storm-800 / frost-200", "2. Neutral Primitives", "#1D3045", "#D0DCE7", "Elevated neutral & border primitive", "Modals, elevated cards, structural dividers"),
        // 3. Dark Semantic Colors
        CinematicTokenMetadata("--background (dark)", "3. Dark Semantic Colors", "#0D1521", "#F3F6F9", "Main application background in dark mode", "Scaffold & root screen background"),
        CinematicTokenMetadata("--surface (dark)", "3. Dark Semantic Colors", "#142235", "#FFFFFF", "Primary card/container surface in dark mode", "Standard cards, inputs, list items"),
        // 4. Light Semantic Colors
        CinematicTokenMetadata("--background (light)", "4. Light Semantic Colors", "#0D1521", "#F3F6F9", "Main application background in light mode", "Scaffold & root screen background"),
        CinematicTokenMetadata("--surface (light)", "4. Light Semantic Colors", "#142235", "#FFFFFF", "Primary card/container surface in light mode", "Standard cards, inputs, list items"),
        // 5. Typography Colors
        CinematicTokenMetadata("--foreground", "5. Typography Colors", "#E4EDF5", "#172A3D", "Primary body and heading text", "Headings, primary labels, monetary figures"),
        CinematicTokenMetadata("--foreground-secondary", "5. Typography Colors", "#B9C9D8", "#526477", "Secondary supporting text", "Subtitles, metadata, timestamps"),
        CinematicTokenMetadata("--foreground-muted", "5. Typography Colors", "#B9C9D8", "#526477", "WCAG AA verified muted text", "Helper text, input placeholders"),
        CinematicTokenMetadata("--foreground-subtle-large", "5. Typography Colors", "#6D91B2", "#718398", "Subtle text for large >=18sp display captions", "Large editorial subheadings"),
        // 6. Surface Colors
        CinematicTokenMetadata("--surface-elevated", "6. Surface Colors", "#1D3045", "#FFFFFF", "Elevated surface for dialogs & popovers", "Modals, drawers, elevated cards"),
        CinematicTokenMetadata("--background-secondary", "6. Surface Colors", "#070B12", "#E5ECF2", "Recessed or navigation bar background", "Bottom navigation, sidebar, recessed bands"),
        // 7. Border Colors
        CinematicTokenMetadata("--border-subtle", "7. Border Colors", "#26394D", "#E5ECF2", "Subtle hairline divider", "Internal list dividers"),
        CinematicTokenMetadata("--border-default", "7. Border Colors", "#26394D", "#D0DCE7", "Standard card and input border", "Card outlines, form inputs"),
        CinematicTokenMetadata("--border-strong", "7. Border Colors", "#40566C", "#718398", "High-contrast structural border", "Active cards, table headers, 3:1 boundaries"),
        // 8. Interactive States
        CinematicTokenMetadata("--primary", "8. Interactive States", "#B9C9D8", "#526B80", "Primary interactive action color", "Primary CTA buttons, active toggles"),
        CinematicTokenMetadata("--primary-hover", "8. Interactive States", "#E4EDF5", "#172A3D", "Hover/pressed state for primary action", "Button hover/press feedback"),
        CinematicTokenMetadata("--secondary", "8. Interactive States", "#6D91B2", "#526477", "Secondary interactive color", "Secondary chips, filter actions"),
        // 9. Status Colors
        CinematicTokenMetadata("--success / --success-bg", "9. Status Colors", "#8DBEAA / #112622", "#287A58 / #E6F3EE", "Semantic positive/settled status", "Income badges, settled confirmations"),
        CinematicTokenMetadata("--warning / --warning-bg", "9. Status Colors", "#E7BE79 / #282115", "#956019 / #F9F1E6", "Semantic budget/bill threshold warning", "80% budget alerts, pending confirmations"),
        CinematicTokenMetadata("--error / --error-bg", "9. Status Colors", "#E58C94 / #2A181E", "#B83D4B / #FAEAEB", "Semantic error/overspend status", "100% budget exceeded, validation errors"),
        CinematicTokenMetadata("--info / --info-bg", "9. Status Colors", "#85B9DB / #122332", "#286B99 / #E6F0F7", "Semantic informational notice", "AI advisor notices, sync tips"),
        // 10. Focus Colors
        CinematicTokenMetadata("--focus", "10. Focus Colors", "#B9C9D8", "#526B80", "Keyboard & accessibility focus ring", "Focus-visible outlines on interactive controls"),
        // 11. Overlay Colors
        CinematicTokenMetadata("--modal-backdrop", "11. Overlay Colors", "#070B12", "#172A3D", "Modal & sheet backdrop scrim base", "Dialog and drawer backdrops"),
        // 12. Gradient Colors
        CinematicTokenMetadata("--gradient-midnight-horizon", "12. Gradient Colors", "#070B12 -> #142235 -> #34546C", "#F3F6F9 -> #E5ECF2 -> #D0DCE7", "Hero & atmospheric banner gradient", "Hero cards, reactor backdrop"),
        CinematicTokenMetadata("--gradient-silver-moonlight", "12. Gradient Colors", "#101B2B -> #29445B -> #B9C9D8", "#FFFFFF -> #F3F6F9 -> #E5ECF2", "Moonlit metallic highlight gradient", "Featured banners & premium cards"),
        // 13. Shadow Colors
        CinematicTokenMetadata("--shadow-xs..xl / --shadow-glow", "13. Shadow Colors", "#070B12 / #6D91B2", "#D0DCE7 / #526477", "Centralized elevation & ambient glow shadows", "Cards, modals, floating bars"),
        // 14. Chart Colors
        CinematicTokenMetadata("--chart-series-1..6", "14. Chart Colors", "#B9C9D8, #6D91B2, #8BA9A9, #8DBEAA, #E58C94, #E7BE79", "#526B80, #286B99, #526477, #287A58, #B83D4B, #956019", "Accessible data visualization series", "Reactor rings, category bars, cash-flow charts"),
        // 15. Component-Specific Tokens
        CinematicTokenMetadata("--button-primary-bg / --card-bg / --input-bg / --nav-bg", "15. Component Tokens", "Mapped from Dark Semantic Layer B", "Mapped from Light Semantic Layer B", "Component-level token contract", "All Foundation, Surface, Nav & Feedback components")
    )
}

/**
 * WCAG 2.1 AA Automated Contrast Auditor (Section 6 Rule 7 & Section 15.C).
 */
@Immutable
data class ContrastAuditEntry(
    val pairName: String,
    val themeMode: String,
    val foregroundHex: String,
    val backgroundHex: String,
    val contrastRatio: Double,
    val requiredThreshold: Double,
    val passed: Boolean
)

object ColorContrastAuditor {

    private fun srgbChannelToLinear(channel: Float): Double {
        val c = channel.toDouble().coerceIn(0.0, 1.0)
        return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }

    fun relativeLuminance(color: Color): Double {
        val r = srgbChannelToLinear(color.red)
        val g = srgbChannelToLinear(color.green)
        val b = srgbChannelToLinear(color.blue)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    fun calculateContrastRatio(fg: Color, bg: Color): Double {
        val l1 = relativeLuminance(fg)
        val l2 = relativeLuminance(bg)
        val lighter = max(l1, l2)
        val darker = min(l1, l2)
        val raw = (lighter + 0.05) / (darker + 0.05)
        return kotlin.math.round(raw * 100.0) / 100.0
    }

    fun colorToHex(color: Color): String {
        val r = (color.red * 255).toInt().coerceIn(0, 255)
        val g = (color.green * 255).toInt().coerceIn(0, 255)
        val b = (color.blue * 255).toInt().coerceIn(0, 255)
        return String.format("#%02X%02X%02X", r, g, b)
    }

    fun runFullContrastAudit(): List<ContrastAuditEntry> {
        val dark = CinematicTokenRegistry.buildDarkSemanticTokens()
        val light = CinematicTokenRegistry.buildLightSemanticTokens()
        val darkComp = CinematicTokenRegistry.buildComponentTokens(dark)
        val lightComp = CinematicTokenRegistry.buildComponentTokens(light)

        val entries = mutableListOf<ContrastAuditEntry>()

        fun check(name: String, mode: String, fg: Color, bg: Color, threshold: Double = 4.5) {
            val ratio = calculateContrastRatio(fg, bg)
            entries += ContrastAuditEntry(
                pairName = name,
                themeMode = mode,
                foregroundHex = colorToHex(fg),
                backgroundHex = colorToHex(bg),
                contrastRatio = ratio,
                requiredThreshold = threshold,
                passed = ratio >= threshold
            )
        }

        // Dark Theme Audited Pairs
        check("foreground on background", "DARK", dark.foreground, dark.background, 4.5)
        check("foreground on surface", "DARK", dark.foreground, dark.surface, 4.5)
        check("foreground on surface-elevated", "DARK", dark.foreground, dark.surfaceElevated, 4.5)
        check("foreground-secondary on background", "DARK", dark.foregroundSecondary, dark.background, 4.5)
        check("foreground-secondary on surface", "DARK", dark.foregroundSecondary, dark.surface, 4.5)
        check("foreground-subtle-large on background (Large Text)", "DARK", dark.foregroundSubtleLarge, dark.background, 3.0)
        check("button-primary-text on button-primary-bg", "DARK", darkComp.buttonPrimaryText, darkComp.buttonPrimaryBg, 4.5)
        check("success on success-bg", "DARK", dark.success, dark.successBackground, 4.5)
        check("warning on warning-bg", "DARK", dark.warning, dark.warningBackground, 4.5)
        check("error on error-bg", "DARK", dark.error, dark.errorBackground, 4.5)
        check("info on info-bg", "DARK", dark.info, dark.infoBackground, 4.5)

        // Light Theme Audited Pairs
        check("foreground on background", "LIGHT", light.foreground, light.background, 4.5)
        check("foreground on surface", "LIGHT", light.foreground, light.surface, 4.5)
        check("foreground-secondary on background", "LIGHT", light.foregroundSecondary, light.background, 4.5)
        check("foreground-secondary on surface", "LIGHT", light.foregroundSecondary, light.surface, 4.5)
        check("foreground-muted on surface", "LIGHT", light.foregroundMuted, light.surface, 4.5)
        check("foreground-subtle-large on surface (Large Text)", "LIGHT", light.foregroundSubtleLarge, light.surface, 3.0)
        check("button-primary-text on button-primary-bg", "LIGHT", lightComp.buttonPrimaryText, lightComp.buttonPrimaryBg, 4.5)
        check("success on success-bg", "LIGHT", light.success, light.successBackground, 4.5)
        check("warning on warning-bg", "LIGHT", light.warning, light.warningBackground, 4.5)
        check("error on error-bg", "LIGHT", light.error, light.errorBackground, 4.5)
        check("info on info-bg", "LIGHT", light.info, light.infoBackground, 4.5)

        return entries
    }
}

val LocalCinematicSemanticTokens = staticCompositionLocalOf {
    CinematicTokenRegistry.buildDarkSemanticTokens()
}

val LocalCinematicComponentTokens = staticCompositionLocalOf {
    CinematicTokenRegistry.buildComponentTokens(CinematicTokenRegistry.buildDarkSemanticTokens())
}

val LocalCinematicFluidScale = staticCompositionLocalOf {
    CinematicFluidScale.forViewportWidthDp(411f)
}

val LocalCinematicThemeMode = staticCompositionLocalOf {
    CinematicThemeMode.DARK
}

object CinematicTheme {
    val tokens: CinematicSemanticTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalCinematicSemanticTokens.current

    val components: CinematicComponentTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalCinematicComponentTokens.current

    val fluid: CinematicFluidScale
        @Composable
        @ReadOnlyComposable
        get() = LocalCinematicFluidScale.current

    val mode: CinematicThemeMode
        @Composable
        @ReadOnlyComposable
        get() = LocalCinematicThemeMode.current
}
