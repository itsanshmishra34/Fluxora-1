package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Global CompositionLocal privacy toggle masking monetary figures across all screens.
 */
val LocalHideSensitiveAmounts = compositionLocalOf { false }

/**
 * Fluxora's 8 Signature Themes as defined in the authoritative specification.
 */
enum class AppTheme(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val previewPrimary: Color,
    val previewSecondary: Color,
    val previewBackground: Color
) {
    MIDNIGHT_EMERALD(
        id = "MIDNIGHT_EMERALD",
        displayName = "Midnight Emerald",
        subtitle = "Default • Deep obsidian & electric emerald",
        previewPrimary = EmeraldPrimaryDark,
        previewSecondary = EmeraldSecondaryDark,
        previewBackground = EmeraldBgDark
    ),
    CYBER_VIOLET(
        id = "CYBER_VIOLET",
        displayName = "Cyber Violet",
        subtitle = "Ultraviolet neon & deep space indigo",
        previewPrimary = VioletPrimaryDark,
        previewSecondary = VioletSecondaryDark,
        previewBackground = VioletBgDark
    ),
    SOLAR_GOLD(
        id = "SOLAR_GOLD",
        displayName = "Solar Gold",
        subtitle = "Sovereign amber & burnished gold",
        previewPrimary = GoldPrimaryDark,
        previewSecondary = GoldSecondaryDark,
        previewBackground = GoldBgDark
    ),
    ARCTIC_CYAN(
        id = "ARCTIC_CYAN",
        displayName = "Arctic Cyan",
        subtitle = "Glacial cyan & crisp cryo-slate",
        previewPrimary = CyanPrimaryDark,
        previewSecondary = CyanSecondaryDark,
        previewBackground = CyanBgDark
    ),
    CRIMSON_PULSE(
        id = "CRIMSON_PULSE",
        displayName = "Crimson Pulse",
        subtitle = "High-contrast ruby & warm carbon",
        previewPrimary = CrimsonPrimaryDark,
        previewSecondary = CrimsonSecondaryDark,
        previewBackground = CrimsonBgDark
    ),
    OBSIDIAN_ROSE(
        id = "OBSIDIAN_ROSE",
        displayName = "Obsidian Rose",
        subtitle = "Velvet rose-quartz & dark plum",
        previewPrimary = RosePrimaryDark,
        previewSecondary = RoseSecondaryDark,
        previewBackground = RoseBgDark
    ),
    AURORA_BOREALIS(
        id = "AURORA_BOREALIS",
        displayName = "Aurora Borealis",
        subtitle = "Northern lights teal & cosmic violet",
        previewPrimary = AuroraPrimaryDark,
        previewSecondary = AuroraSecondaryDark,
        previewBackground = AuroraBgDark
    ),
    MONOCHROME_SLATE(
        id = "MONOCHROME_SLATE",
        displayName = "Monochrome Slate",
        subtitle = "Architectural titanium & pure slate",
        previewPrimary = SlatePrimaryDark,
        previewSecondary = SlateSecondaryDark,
        previewBackground = SlateBgDark
    );

    companion object {
        fun fromId(id: String?): AppTheme {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) || it.displayName.equals(id, ignoreCase = true) }
                ?: MIDNIGHT_EMERALD
        }
    }
}

@Immutable
data class FluxoraExtendedColors(
    val incomeRingColor: Color,
    val expenseRingColor: Color,
    val reactorCoreGlow: Color,
    val warningColor: Color,
    val dangerColor: Color,
    val cardBorderBrushStart: Color,
    val cardBorderBrushEnd: Color
)

val LocalFluxoraExtendedColors = staticCompositionLocalOf {
    FluxoraExtendedColors(
        incomeRingColor = IncomeEmerald,
        expenseRingColor = ExpenseCoral,
        reactorCoreGlow = EmeraldPrimaryDark,
        warningColor = WarningAmber,
        dangerColor = ExpenseCoral,
        cardBorderBrushStart = EmeraldPrimaryDark.copy(alpha = 0.45f),
        cardBorderBrushEnd = EmeraldTertiaryDark.copy(alpha = 0.12f)
    )
}

fun AppTheme.toColorScheme(darkTheme: Boolean): ColorScheme {
    return when (this) {
        AppTheme.MIDNIGHT_EMERALD -> if (darkTheme) {
            darkColorScheme(
                primary = ShowcasePrimaryEmerald,
                onPrimary = ShowcaseDeepBackground,
                primaryContainer = ShowcaseSurfaceElevation,
                onPrimaryContainer = ShowcaseLightMintHighlight,
                secondary = ShowcaseAccentCyan,
                onSecondary = ShowcaseDeepBackground,
                secondaryContainer = Color(0xFF163832),
                onSecondaryContainer = ShowcaseCyanIce,
                tertiary = EmeraldTertiaryDark,
                background = ShowcaseDeepBackground,
                onBackground = ShowcaseTextPrimary,
                surface = ShowcaseSurfaceDark,
                onSurface = ShowcaseTextPrimary,
                surfaceVariant = ShowcaseSurfaceElevation,
                onSurfaceVariant = ShowcaseTextSecondary,
                error = ShowcaseExpenseRed
            )
        } else {
            lightColorScheme(
                primary = EmeraldPrimaryLight,
                onPrimary = Color.White,
                primaryContainer = EmeraldSurfaceVariantLight,
                onPrimaryContainer = Color(0xFF00210E),
                secondary = EmeraldSecondaryLight,
                onSecondary = Color.White,
                tertiary = EmeraldTertiaryLight,
                background = EmeraldBgLight,
                onBackground = ShowcaseDeepBackground,
                surface = EmeraldSurfaceLight,
                onSurface = ShowcaseDeepBackground,
                surfaceVariant = EmeraldSurfaceVariantLight,
                onSurfaceVariant = ShowcaseLoginSubtitleSlate,
                error = Color(0xFFC62828)
            )
        }

        AppTheme.CYBER_VIOLET -> if (darkTheme) {
            darkColorScheme(
                primary = VioletPrimaryDark,
                onPrimary = Color(0xFF1F004D),
                primaryContainer = VioletSurfaceVariantDark,
                onPrimaryContainer = Color(0xFFEDE7F6),
                secondary = VioletSecondaryDark,
                onSecondary = Color(0xFF2A0033),
                secondaryContainer = Color(0xFF311F54),
                onSecondaryContainer = Color(0xFFF3E5F5),
                tertiary = VioletTertiaryDark,
                background = VioletBgDark,
                onBackground = Color(0xFFF3E5F5),
                surface = VioletSurfaceDark,
                onSurface = Color(0xFFF3E5F5),
                surfaceVariant = VioletSurfaceVariantDark,
                onSurfaceVariant = Color(0xFFC5B3E6),
                error = ExpenseCoral
            )
        } else {
            lightColorScheme(
                primary = VioletPrimaryLight,
                onPrimary = Color.White,
                primaryContainer = VioletSurfaceVariantLight,
                onPrimaryContainer = Color(0xFF1F004D),
                secondary = VioletSecondaryLight,
                onSecondary = Color.White,
                tertiary = VioletTertiaryLight,
                background = VioletBgLight,
                onBackground = Color(0xFF1B112C),
                surface = VioletSurfaceLight,
                onSurface = Color(0xFF1B112C),
                surfaceVariant = VioletSurfaceVariantLight,
                onSurfaceVariant = Color(0xFF4A3B69),
                error = Color(0xFFC62828)
            )
        }

        AppTheme.SOLAR_GOLD -> if (darkTheme) {
            darkColorScheme(
                primary = GoldPrimaryDark,
                onPrimary = Color(0xFF261900),
                primaryContainer = GoldSurfaceVariantDark,
                onPrimaryContainer = Color(0xFFFFF8E1),
                secondary = GoldSecondaryDark,
                onSecondary = Color(0xFF261900),
                secondaryContainer = Color(0xFF3E2F14),
                onSecondaryContainer = Color(0xFFFFECB3),
                tertiary = GoldTertiaryDark,
                background = GoldBgDark,
                onBackground = Color(0xFFFFF8E1),
                surface = GoldSurfaceDark,
                onSurface = Color(0xFFFFF8E1),
                surfaceVariant = GoldSurfaceVariantDark,
                onSurfaceVariant = Color(0xFFD7C397),
                error = ExpenseCoral
            )
        } else {
            lightColorScheme(
                primary = GoldPrimaryLight,
                onPrimary = Color.White,
                primaryContainer = GoldSurfaceVariantLight,
                onPrimaryContainer = Color(0xFF261900),
                secondary = GoldSecondaryLight,
                onSecondary = Color.White,
                tertiary = GoldTertiaryLight,
                background = GoldBgLight,
                onBackground = Color(0xFF241A04),
                surface = GoldSurfaceLight,
                onSurface = Color(0xFF241A04),
                surfaceVariant = GoldSurfaceVariantLight,
                onSurfaceVariant = Color(0xFF5E4B22),
                error = Color(0xFFC62828)
            )
        }

        AppTheme.ARCTIC_CYAN -> if (darkTheme) {
            darkColorScheme(
                primary = CyanPrimaryDark,
                onPrimary = Color(0xFF00262B),
                primaryContainer = CyanSurfaceVariantDark,
                onPrimaryContainer = Color(0xFFE0F7FA),
                secondary = CyanSecondaryDark,
                onSecondary = Color(0xFF00262B),
                secondaryContainer = Color(0xFF173D4F),
                onSecondaryContainer = Color(0xFFE1F5FE),
                tertiary = CyanTertiaryDark,
                background = CyanBgDark,
                onBackground = Color(0xFFE0F7FA),
                surface = CyanSurfaceDark,
                onSurface = Color(0xFFE0F7FA),
                surfaceVariant = CyanSurfaceVariantDark,
                onSurfaceVariant = Color(0xFFA2C9D9),
                error = ExpenseCoral
            )
        } else {
            lightColorScheme(
                primary = CyanPrimaryLight,
                onPrimary = Color.White,
                primaryContainer = CyanSurfaceVariantLight,
                onPrimaryContainer = Color(0xFF00262B),
                secondary = CyanSecondaryLight,
                onSecondary = Color.White,
                tertiary = CyanTertiaryLight,
                background = CyanBgLight,
                onBackground = Color(0xFF07202B),
                surface = CyanSurfaceLight,
                onSurface = Color(0xFF07202B),
                surfaceVariant = CyanSurfaceVariantLight,
                onSurfaceVariant = Color(0xFF335666),
                error = Color(0xFFC62828)
            )
        }

        AppTheme.CRIMSON_PULSE -> if (darkTheme) {
            darkColorScheme(
                primary = CrimsonPrimaryDark,
                onPrimary = Color(0xFF330003),
                primaryContainer = CrimsonSurfaceVariantDark,
                onPrimaryContainer = Color(0xFFFFEBEE),
                secondary = CrimsonSecondaryDark,
                onSecondary = Color(0xFF330003),
                secondaryContainer = Color(0xFF4A1F23),
                onSecondaryContainer = Color(0xFFFFCDD2),
                tertiary = CrimsonTertiaryDark,
                background = CrimsonBgDark,
                onBackground = Color(0xFFFFEBEE),
                surface = CrimsonSurfaceDark,
                onSurface = Color(0xFFFFEBEE),
                surfaceVariant = CrimsonSurfaceVariantDark,
                onSurfaceVariant = Color(0xFFE0AEB2),
                error = Color(0xFFFF8A80)
            )
        } else {
            lightColorScheme(
                primary = CrimsonPrimaryLight,
                onPrimary = Color.White,
                primaryContainer = CrimsonSurfaceVariantLight,
                onPrimaryContainer = Color(0xFF330003),
                secondary = CrimsonSecondaryLight,
                onSecondary = Color.White,
                tertiary = CrimsonTertiaryLight,
                background = CrimsonBgLight,
                onBackground = Color(0xFF2B0B0E),
                surface = CrimsonSurfaceLight,
                onSurface = Color(0xFF2B0B0E),
                surfaceVariant = CrimsonSurfaceVariantLight,
                onSurfaceVariant = Color(0xFF66373B),
                error = Color(0xFFB71C1C)
            )
        }

        AppTheme.OBSIDIAN_ROSE -> if (darkTheme) {
            darkColorScheme(
                primary = RosePrimaryDark,
                onPrimary = Color(0xFF330016),
                primaryContainer = RoseSurfaceVariantDark,
                onPrimaryContainer = Color(0xFFFCE4EC),
                secondary = RoseSecondaryDark,
                onSecondary = Color(0xFF330016),
                secondaryContainer = Color(0xFF472335),
                onSecondaryContainer = Color(0xFFF8BBD0),
                tertiary = RoseTertiaryDark,
                background = RoseBgDark,
                onBackground = Color(0xFFFCE4EC),
                surface = RoseSurfaceDark,
                onSurface = Color(0xFFFCE4EC),
                surfaceVariant = RoseSurfaceVariantDark,
                onSurfaceVariant = Color(0xFFDDB0C2),
                error = ExpenseCoral
            )
        } else {
            lightColorScheme(
                primary = RosePrimaryLight,
                onPrimary = Color.White,
                primaryContainer = RoseSurfaceVariantLight,
                onPrimaryContainer = Color(0xFF330016),
                secondary = RoseSecondaryLight,
                onSecondary = Color.White,
                tertiary = RoseTertiaryLight,
                background = RoseBgLight,
                onBackground = Color(0xFF290B18),
                surface = RoseSurfaceLight,
                onSurface = Color(0xFF290B18),
                surfaceVariant = RoseSurfaceVariantLight,
                onSurfaceVariant = Color(0xFF63384B),
                error = Color(0xFFC62828)
            )
        }

        AppTheme.AURORA_BOREALIS -> if (darkTheme) {
            darkColorScheme(
                primary = AuroraPrimaryDark,
                onPrimary = Color(0xFF00241B),
                primaryContainer = AuroraSurfaceVariantDark,
                onPrimaryContainer = Color(0xFFE0F2F1),
                secondary = AuroraSecondaryDark,
                onSecondary = Color.White,
                secondaryContainer = Color(0xFF243A52),
                onSecondaryContainer = Color(0xFFD1C4E9),
                tertiary = AuroraTertiaryDark,
                background = AuroraBgDark,
                onBackground = Color(0xFFE0F7FA),
                surface = AuroraSurfaceDark,
                onSurface = Color(0xFFE0F7FA),
                surfaceVariant = AuroraSurfaceVariantDark,
                onSurfaceVariant = Color(0xFFA3C6D9),
                error = ExpenseCoral
            )
        } else {
            lightColorScheme(
                primary = AuroraPrimaryLight,
                onPrimary = Color.White,
                primaryContainer = AuroraSurfaceVariantLight,
                onPrimaryContainer = Color(0xFF00241B),
                secondary = AuroraSecondaryLight,
                onSecondary = Color.White,
                tertiary = AuroraTertiaryLight,
                background = AuroraBgLight,
                onBackground = Color(0xFF092126),
                surface = AuroraSurfaceLight,
                onSurface = Color(0xFF092126),
                surfaceVariant = AuroraSurfaceVariantLight,
                onSurfaceVariant = Color(0xFF375961),
                error = Color(0xFFC62828)
            )
        }

        AppTheme.MONOCHROME_SLATE -> if (darkTheme) {
            darkColorScheme(
                primary = SlatePrimaryDark,
                onPrimary = Color(0xFF101417),
                primaryContainer = SlateSurfaceVariantDark,
                onPrimaryContainer = Color(0xFFECEFF1),
                secondary = SlateSecondaryDark,
                onSecondary = Color(0xFF101417),
                secondaryContainer = Color(0xFF2E383E),
                onSecondaryContainer = Color(0xFFCFD8DC),
                tertiary = SlateTertiaryDark,
                background = SlateBgDark,
                onBackground = Color(0xFFECEFF1),
                surface = SlateSurfaceDark,
                onSurface = Color(0xFFECEFF1),
                surfaceVariant = SlateSurfaceVariantDark,
                onSurfaceVariant = Color(0xFFB0BEC5),
                error = ExpenseCoral
            )
        } else {
            lightColorScheme(
                primary = SlatePrimaryLight,
                onPrimary = Color.White,
                primaryContainer = SlateSurfaceVariantLight,
                onPrimaryContainer = Color(0xFF101417),
                secondary = SlateSecondaryLight,
                onSecondary = Color.White,
                tertiary = SlateTertiaryLight,
                background = SlateBgLight,
                onBackground = Color(0xFF151C1F),
                surface = SlateSurfaceLight,
                onSurface = Color(0xFF151C1F),
                surfaceVariant = SlateSurfaceVariantLight,
                onSurfaceVariant = Color(0xFF455A64),
                error = Color(0xFFC62828)
            )
        }
    }
}

fun AppTheme.toExtendedColors(darkTheme: Boolean): FluxoraExtendedColors {
    val scheme = toColorScheme(darkTheme)
    return FluxoraExtendedColors(
        incomeRingColor = if (darkTheme) ShowcasePrimaryEmerald else EmeraldPrimaryLight,
        expenseRingColor = if (darkTheme) ShowcaseExpenseRed else Color(0xFFC62828),
        reactorCoreGlow = scheme.primary,
        warningColor = if (darkTheme) WarningAmber else Color(0xFFE65100),
        dangerColor = if (darkTheme) ShowcaseExpenseRed else Color(0xFFC62828),
        cardBorderBrushStart = scheme.primary.copy(alpha = if (darkTheme) 0.42f else 0.28f),
        cardBorderBrushEnd = scheme.tertiary.copy(alpha = if (darkTheme) 0.14f else 0.10f)
    )
}

@Composable
fun FluxoraTheme(
    appTheme: AppTheme = AppTheme.MIDNIGHT_EMERALD,
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeMode: CinematicThemeMode = if (darkTheme) CinematicThemeMode.DARK else CinematicThemeMode.LIGHT,
    glassPreference: com.example.ui.glass.GlassEffectPreference = com.example.ui.glass.GlassEffectPreference.AUTO,
    hideSensitiveAmounts: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val effectiveDark = when (themeMode) {
        CinematicThemeMode.DARK -> true
        CinematicThemeMode.LIGHT -> false
        CinematicThemeMode.SYSTEM -> systemDark
    }
    val colorScheme = appTheme.toColorScheme(effectiveDark)
    val extendedColors = appTheme.toExtendedColors(effectiveDark)
    val cinematicSemantic = if (effectiveDark) {
        CinematicTokenRegistry.buildDarkSemanticTokens()
    } else {
        CinematicTokenRegistry.buildLightSemanticTokens()
    }
    val cinematicComponents = CinematicTokenRegistry.buildComponentTokens(cinematicSemantic)
    val glassTier = com.example.ui.glass.GlassTierResolver.resolveTier(glassPreference)
    val glassTokens = com.example.ui.glass.GlassTierResolver.tokensForTheme(
        theme = appTheme,
        isDark = effectiveDark,
        tier = glassTier
    )

    androidx.compose.foundation.layout.BoxWithConstraints {
        val fluidScale = CinematicFluidScale.forViewportWidthDp(maxWidth.value)

        CompositionLocalProvider(
            LocalHideSensitiveAmounts provides hideSensitiveAmounts,
            LocalFluxoraExtendedColors provides extendedColors,
            LocalCinematicSemanticTokens provides cinematicSemantic,
            LocalCinematicComponentTokens provides cinematicComponents,
            LocalCinematicFluidScale provides fluidScale,
            LocalCinematicThemeMode provides themeMode,
            com.example.ui.glass.LocalGlassTokens provides glassTokens,
            com.example.ui.glass.LocalGlassQualityTier provides glassTier
        ) {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = Typography,
                content = content
            )
        }
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    FluxoraTheme(
        appTheme = AppTheme.MIDNIGHT_EMERALD,
        darkTheme = darkTheme,
        content = content
    )
}

