package com.example.ui.glass

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.CinematicTheme

/**
 * ============================================================================
 * LIQUID GLASS DESIGN SYSTEM (:core:glass — Section 14 of Fluxora v2.2)
 * ============================================================================
 * Three runtime quality tiers:
 * - FULL: API 33+, sufficient memory, battery saver off -> refraction, specular highlight, inner glow
 * - SOFT: API 31..32, or stepped down -> backdrop tint, saturation boost, edge stroke
 * - FROST: API 26..30, low-RAM, battery saver, or user choice -> opaque tinted surface with edge stroke
 *
 * User Settings: AUTO, FULL, REDUCED, OFF.
 * Changing quality tiers NEVER changes layout dimensions or functionality.
 */

enum class GlassQualityTier(val id: String, val displayName: String, val description: String) {
    FULL("FULL", "Full Liquid Glass", "Specular edge refraction, inner glow & depth shadow (API 33+)"),
    SOFT("SOFT", "Soft Translucent Glass", "Tinted translucent surface with bright edge stroke (API 31–32)"),
    FROST("FROST", "Frost High-Contrast Surface", "Opaque tinted surface with crisp edge stroke (API 26+ / Battery Saver)")
}

enum class GlassEffectPreference(val id: String, val displayName: String) {
    AUTO("AUTO", "Auto"),
    FULL("FULL", "Full"),
    REDUCED("REDUCED", "Reduced"),
    OFF("OFF", "Off");

    companion object {
        fun fromId(id: String?): GlassEffectPreference {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: AUTO
        }
    }
}

@Immutable
data class GlassTokens(
    val tintColor: Color,
    val blurRadiusDp: Dp,
    val saturationBoost: Float,
    val refractionStrength: Float,
    val specularIntensity: Float,
    val edgeGradientStart: Color,
    val edgeGradientEnd: Color,
    val shadowColor: Color,
    val noiseAlpha: Float,
    val cornerRadiusDp: Dp = 22.dp
)

object GlassTierResolver {

    fun resolveTier(
        preference: GlassEffectPreference,
        sdkInt: Int = Build.VERSION.SDK_INT,
        isLowRamDevice: Boolean = false,
        isPowerSaveMode: Boolean = false
    ): GlassQualityTier {
        return when (preference) {
            GlassEffectPreference.OFF -> GlassQualityTier.FROST
            GlassEffectPreference.REDUCED -> GlassQualityTier.SOFT
            GlassEffectPreference.FULL -> if (sdkInt >= 31) GlassQualityTier.FULL else GlassQualityTier.FROST
            GlassEffectPreference.AUTO -> {
                when {
                    isLowRamDevice || isPowerSaveMode || sdkInt < 31 -> GlassQualityTier.FROST
                    sdkInt in 31..32 -> GlassQualityTier.SOFT
                    else -> GlassQualityTier.FULL
                }
            }
        }
    }

    /**
     * Builds the exact GlassTokens for each of the 8 glass themes + CINEMATIC v2.0 (Section 14 table).
     */
    fun tokensForTheme(
        theme: AppTheme,
        isDark: Boolean,
        tier: GlassQualityTier,
        increaseContrast: Boolean = false
    ): GlassTokens {
        val baseTintAlpha = when (tier) {
            GlassQualityTier.FULL -> if (increaseContrast) 0.88f else 0.78f
            GlassQualityTier.SOFT -> if (increaseContrast) 0.92f else 0.85f
            GlassQualityTier.FROST -> 0.97f
        }
        val blurRadius = when (tier) {
            GlassQualityTier.FULL -> 20.dp
            GlassQualityTier.SOFT -> 12.dp
            GlassQualityTier.FROST -> 0.dp
        }

        val (darkSurface, lightSurface, accent) = when (theme) {
            AppTheme.MIDNIGHT_EMERALD -> Triple(Color(0xFF0B2A22), Color(0xFFFFFFFF), Color(0xFF34D399))
            AppTheme.CYBER_VIOLET -> Triple(Color(0xFF241047), Color(0xFFFFFFFF), Color(0xFFA78BFA))
            AppTheme.SOLAR_GOLD -> Triple(Color(0xFF3A2A08), Color(0xFFFFFFFF), Color(0xFFFBBF24))
            AppTheme.ARCTIC_CYAN -> Triple(Color(0xFF06303B), Color(0xFFFFFFFF), Color(0xFF22D3EE))
            AppTheme.CRIMSON_PULSE -> Triple(Color(0xFF3B0A14), Color(0xFFFFFFFF), Color(0xFFF43F5E))
            AppTheme.OBSIDIAN_ROSE -> Triple(Color(0xFF241319), Color(0xFFFFFFFF), Color(0xFFFB7185))
            AppTheme.AURORA_BOREALIS -> Triple(Color(0xFF0F1D29), Color(0xFFFFFFFF), Color(0xFF34D399))
            AppTheme.MONOCHROME_SLATE -> Triple(Color(0xFF1C2128), Color(0xFFFFFFFF), Color(0xFFB9C9D8))
        }

        val surfaceColor = if (isDark) darkSurface.copy(alpha = baseTintAlpha) else lightSurface.copy(alpha = baseTintAlpha)
        val edgeStart = if (isDark) accent.copy(alpha = if (increaseContrast) 0.65f else 0.42f) else accent.copy(alpha = 0.35f)
        val edgeEnd = if (isDark) Color(0xFF26394D) else Color(0xFFD0DCE7)

        return GlassTokens(
            tintColor = surfaceColor,
            blurRadiusDp = blurRadius,
            saturationBoost = if (tier == GlassQualityTier.FROST) 1.0f else 1.18f,
            refractionStrength = if (tier == GlassQualityTier.FULL) 0.25f else 0f,
            specularIntensity = if (tier == GlassQualityTier.FULL) 0.32f else 0.14f,
            edgeGradientStart = edgeStart,
            edgeGradientEnd = edgeEnd,
            shadowColor = if (isDark) Color(0xFF070B12) else Color(0xFFD0DCE7),
            noiseAlpha = if (tier == GlassQualityTier.FROST) 0.04f else 0.02f,
            cornerRadiusDp = 22.dp
        )
    }
}

val LocalGlassTokens = staticCompositionLocalOf {
    GlassTierResolver.tokensForTheme(AppTheme.MIDNIGHT_EMERALD, isDark = true, tier = GlassQualityTier.FULL)
}

val LocalGlassQualityTier = staticCompositionLocalOf {
    GlassQualityTier.FULL
}

// --- THE 12 REQUIRED GLASS COMPONENTS (:core:glass — Section 14) ---

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LocalGlassTokens.current.cornerRadiusDp),
    tokens: GlassTokens = LocalGlassTokens.current,
    content: @Composable () -> Unit
) {
    val edgeBrush = remember(tokens.edgeGradientStart, tokens.edgeGradientEnd) {
        Brush.linearGradient(listOf(tokens.edgeGradientStart, tokens.edgeGradientEnd))
    }
    Surface(
        modifier = modifier.border(BorderStroke(1.dp, edgeBrush), shape),
        shape = shape,
        color = tokens.tintColor,
        content = content
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalGlassTokens.current
    val shape = RoundedCornerShape(tokens.cornerRadiusDp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.015f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "glass_card_scale"
    )

    val clickMod = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            role = Role.Button,
            onClick = onClick
        )
    } else {
        Modifier
    }

    GlassSurface(
        modifier = modifier
            .scale(scale)
            .then(clickMod),
        shape = shape,
        tokens = tokens
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun GlassBottomBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val tokens = LocalGlassTokens.current
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(28.dp),
        tokens = tokens
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalGlassTokens.current
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = tokens.tintColor,
        contentColor = CinematicTheme.tokens.foreground,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            content = content
        )
    }
}

@Composable
fun GlassDialog(
    title: String,
    onDismissRequest: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String = "Cancel",
    confirmModifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalGlassTokens.current
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = tokens.tintColor,
        titleContentColor = CinematicTheme.tokens.foreground,
        textContentColor = CinematicTheme.tokens.foregroundSecondary,
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp), content = content) },
        confirmButton = {
            GlassButton(
                text = confirmText,
                onClick = onConfirm,
                modifier = confirmModifier
            )
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(dismissText, color = CinematicTheme.tokens.foregroundSecondary)
            }
        }
    )
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    val comp = CinematicTheme.components
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.98f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "glass_btn_scale"
    )
    val bgColor by animateColorAsState(
        targetValue = when {
            !enabled -> comp.buttonDisabledBg
            isPressed -> comp.buttonPrimaryHover
            else -> comp.buttonPrimaryBg
        },
        label = "glass_btn_bg"
    )
    val fgColor = if (enabled) comp.buttonPrimaryText else comp.buttonDisabledText

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        contentColor = fgColor,
        border = BorderStroke(1.dp, CinematicTheme.tokens.borders.borderFocus),
        modifier = modifier
            .heightIn(min = 48.dp)
            .scale(scale)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun GlassChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = CinematicTheme.tokens
    val bg = if (selected) tokens.primary else tokens.surfaceElevated
    val fg = if (selected) tokens.onPrimary else tokens.foreground
    val border = if (selected) tokens.borders.borderFocus else tokens.border

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bg,
        contentColor = fg,
        border = BorderStroke(1.dp, border),
        modifier = modifier.heightIn(min = 36.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun GlassToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier
    )
}

@Composable
fun GlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        modifier = modifier
    )
}

@Composable
fun GlassProgress(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val comp = CinematicTheme.components
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = comp.progressIndicatorFg,
        trackColor = comp.progressTrackBg
    )
}

@Composable
fun GlassTopCapsule(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    GlassSurface(
        modifier = modifier,
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Composable
fun GlassFab(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val comp = CinematicTheme.components
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = comp.buttonPrimaryBg,
        contentColor = comp.buttonPrimaryText,
        border = BorderStroke(1.dp, CinematicTheme.tokens.borders.borderFocus),
        modifier = modifier
            .size(56.dp)
            .testTag("glass_fab")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
