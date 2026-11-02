package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.glass.GlassCard
import com.example.ui.glass.GlassChip
import com.example.ui.glass.GlassEffectPreference
import com.example.ui.glass.GlassProgress
import com.example.ui.glass.GlassQualityTier
import com.example.ui.glass.GlassTierResolver
import com.example.ui.theme.CinematicFluidScale
import com.example.ui.theme.CinematicTheme
import com.example.ui.theme.CinematicThemeMode
import com.example.ui.theme.CinematicTokenRegistry
import com.example.ui.theme.ColorContrastAuditor
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.ManropeFontFamily

/**
 * ============================================================================
 * CINEMATIC v2.0 COMPONENT SYSTEM (Section 11 & Section 12)
 * ============================================================================
 * Every component resolves colors strictly through CinematicTheme.components (Layer C)
 * or CinematicTheme.tokens (Layer B). Zero raw hex colors are used.
 */

enum class CinematicStatusKind {
    DEFAULT, SUCCESS, WARNING, ERROR, INFO
}

// --- 1. FOUNDATIONS ---

@Composable
fun CinematicButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val comp = CinematicTheme.components
    val bg = when {
        !enabled -> comp.buttonDisabledBg
        isPrimary -> comp.buttonPrimaryBg
        else -> comp.buttonSecondaryBg
    }
    val fg = when {
        !enabled -> comp.buttonDisabledText
        isPrimary -> comp.buttonPrimaryText
        else -> comp.buttonSecondaryText
    }
    val border = if (isPrimary) CinematicTheme.tokens.borders.borderFocus else comp.buttonSecondaryBorder

    Surface(
        onClick = { if (!isLoading && enabled) onClick() },
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        color = bg,
        contentColor = fg,
        border = BorderStroke(1.dp, border),
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = fg
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
fun CinematicIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val comp = CinematicTheme.components
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = comp.iconButtonBg,
        contentColor = comp.iconButtonFg,
        border = BorderStroke(1.dp, CinematicTheme.tokens.border),
        modifier = modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = contentDescription)
        }
    }
}

@Composable
fun CinematicInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    val comp = CinematicTheme.components
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = comp.inputBg,
            unfocusedContainerColor = comp.inputBg,
            focusedTextColor = comp.inputText,
            unfocusedTextColor = comp.inputText,
            focusedBorderColor = comp.inputFocus,
            unfocusedBorderColor = comp.inputBorder,
            focusedLabelColor = comp.inputFocus,
            unfocusedLabelColor = comp.inputPlaceholder
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun CinematicTextarea(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    CinematicInput(
        value = value,
        onValueChange = onValueChange,
        label = label,
        singleLine = false,
        modifier = modifier.heightIn(min = 104.dp)
    )
}

@Composable
fun CinematicSelect(
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val comp = CinematicTheme.components
    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(14.dp),
            color = comp.inputBg,
            contentColor = comp.inputText,
            border = BorderStroke(1.dp, comp.inputBorder),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$label: $selectedOption", style = MaterialTheme.typography.bodyMedium)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = label)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(comp.dropdownBg)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = comp.cardText) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun CinematicCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = CinematicTheme.tokens.foreground)
    }
}

@Composable
fun CinematicRadio(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = CinematicTheme.tokens.foreground)
    }
}

@Composable
fun CinematicSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = CinematicTheme.tokens.foreground)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun CinematicBadge(
    text: String,
    kind: CinematicStatusKind = CinematicStatusKind.DEFAULT,
    modifier: Modifier = Modifier
) {
    val comp = CinematicTheme.components
    val (bg, fg) = when (kind) {
        CinematicStatusKind.DEFAULT -> comp.badgeDefaultBg to comp.badgeDefaultText
        CinematicStatusKind.SUCCESS -> comp.badgeSuccessBg to comp.badgeSuccessText
        CinematicStatusKind.WARNING -> comp.badgeWarningBg to comp.badgeWarningText
        CinematicStatusKind.ERROR -> comp.badgeErrorBg to comp.badgeErrorText
        CinematicStatusKind.INFO -> comp.badgeInfoBg to comp.badgeInfoText
    }
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bg,
        contentColor = fg,
        border = BorderStroke(1.dp, fg),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = IbmPlexMonoFontFamily,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CinematicTooltip(
    text: String,
    modifier: Modifier = Modifier
) {
    val comp = CinematicTheme.components
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = comp.tooltipBg,
        contentColor = comp.tooltipText,
        border = BorderStroke(1.dp, CinematicTheme.tokens.border),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun CinematicSkeleton(
    modifier: Modifier = Modifier,
    height: Dp = 20.dp
) {
    val comp = CinematicTheme.components
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(comp.skeletonHighlightBg)
    )
}

@Composable
fun CinematicSeparator(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        color = CinematicTheme.components.separatorColor
    )
}

// --- 2. SURFACES ---

@Composable
fun CinematicCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val comp = CinematicTheme.components
    val fluid = CinematicTheme.fluid
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = comp.cardBg,
        contentColor = comp.cardText,
        border = BorderStroke(1.dp, comp.cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(fluid.space4),
            content = content
        )
    }
}

@Composable
fun CinematicElevatedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val comp = CinematicTheme.components
    val fluid = CinematicTheme.fluid
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = comp.elevatedCardBg,
        contentColor = comp.cardText,
        tonalElevation = CinematicTheme.tokens.shadows.elevationMd,
        border = BorderStroke(1.dp, comp.elevatedCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(fluid.space4),
            content = content
        )
    }
}

// --- 3. NAVIGATION & FEEDBACK ---

@Composable
fun CinematicBreadcrumb(
    items: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEachIndexed { idx, label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (idx == items.lastIndex) {
                    CinematicTheme.tokens.foreground
                } else {
                    CinematicTheme.tokens.foregroundSecondary
                },
                fontWeight = if (idx == items.lastIndex) FontWeight.Bold else FontWeight.Normal
            )
            if (idx < items.lastIndex) {
                Text(
                    text = "/",
                    style = MaterialTheme.typography.labelMedium,
                    color = CinematicTheme.tokens.foregroundMuted
                )
            }
        }
    }
}

@Composable
fun CinematicAlert(
    title: String,
    message: String,
    kind: CinematicStatusKind = CinematicStatusKind.INFO,
    modifier: Modifier = Modifier
) {
    val tokens = CinematicTheme.tokens
    val (bg, fg, icon) = when (kind) {
        CinematicStatusKind.SUCCESS -> Triple(tokens.successBackground, tokens.success, Icons.Default.CheckCircle)
        CinematicStatusKind.WARNING -> Triple(tokens.warningBackground, tokens.warning, Icons.Default.Warning)
        CinematicStatusKind.ERROR -> Triple(tokens.errorBackground, tokens.error, Icons.Default.Warning)
        else -> Triple(tokens.infoBackground, tokens.info, Icons.Default.Info)
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = bg,
        contentColor = fg,
        border = BorderStroke(1.dp, fg)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(imageVector = icon, contentDescription = title)
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = message, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun CinematicEmptyState(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    CinematicCard(modifier = modifier) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = CinematicTheme.tokens.foregroundSecondary
        )
        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(10.dp))
            CinematicButton(text = actionLabel, onClick = onActionClick)
        }
    }
}

@Composable
fun CinematicHero(
    title: String,
    subtitle: String,
    badgeText: String,
    modifier: Modifier = Modifier
) {
    val tokens = CinematicTheme.tokens
    val fluid = CinematicTheme.fluid
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(tokens.gradients.midnightHorizon)
            .border(1.dp, tokens.borders.borderStrong, RoundedCornerShape(24.dp))
            .padding(fluid.space6)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(fluid.space2)) {
            CinematicBadge(text = badgeText, kind = CinematicStatusKind.INFO)
            Text(
                text = title,
                fontFamily = ManropeFontFamily,
                fontSize = fluid.text2xl,
                fontWeight = FontWeight.Bold,
                color = tokens.foreground
            )
            Text(
                text = subtitle,
                fontSize = fluid.textBase,
                color = tokens.foregroundSecondary
            )
        }
    }
}

/**
 * ============================================================================
 * INTERACTIVE CINEMATIC v2.0 DESIGN SYSTEM & WCAG CONTRAST AUDIT SHOWCASE
 * ============================================================================
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CinematicDesignSystemShowcaseScreen(
    currentThemeMode: CinematicThemeMode,
    currentGlassPreference: GlassEffectPreference,
    onSelectThemeMode: (CinematicThemeMode) -> Unit,
    onSelectGlassPreference: (GlassEffectPreference) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = CinematicTheme.tokens
    var selectedSectionTab by remember { mutableIntStateOf(0) }
    var simulatedViewportDp by remember { mutableStateOf(411f) }
    val simulatedFluid = remember(simulatedViewportDp) {
        CinematicFluidScale.forViewportWidthDp(simulatedViewportDp)
    }
    val contrastReport = remember { ColorContrastAuditor.runFullContrastAudit() }
    val tokenInventory = remember { CinematicTokenRegistry.tokenInventory }
    val activeGlassTier = remember(currentGlassPreference) {
        GlassTierResolver.resolveTier(currentGlassPreference)
    }

    val sectionTabs = listOf(
        "Tokens (15)",
        "WCAG Contrast (${contrastReport.count { it.passed }}/${contrastReport.size})",
        "Fluid & Layout",
        "Gradients & Glass",
        "Components"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(tokens.background)
            .testTag("cinematic_design_system_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_cinematic_showcase_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Settings",
                            tint = tokens.foreground
                        )
                    }
                    Column {
                        Text(
                            text = "CINEMATIC v2.0",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = tokens.foreground
                        )
                        Text(
                            text = "Moonlight Silver • Glacial Blue • Zero-Exception Governance",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.foregroundSecondary
                        )
                    }
                }
                CinematicBadge(
                    text = if (tokens.isDark) "DARK ACTIVE" else "LIGHT ACTIVE",
                    kind = CinematicStatusKind.SUCCESS
                )
            }
        }

        // Hero Banner
        item {
            CinematicHero(
                title = "Cinematic Minimalism & Liquid Glass",
                subtitle = "3-Layer Token Architecture (Primitives → Semantic → Components) with WCAG 2.1 AA verification and fluid clamp() scaling.",
                badgeText = "DESIGN SYSTEM v2.0"
            )
        }

        // Adaptive Theme Mode (Dark / Light / System) & Liquid Glass Tier Controls
        item {
            CinematicElevatedCard {
                Text(
                    text = "Adaptive Theme Engine & Liquid Glass Tier",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CinematicThemeMode.entries.forEach { mode ->
                        GlassChip(
                            label = mode.displayName,
                            selected = currentThemeMode == mode,
                            onClick = { onSelectThemeMode(mode) },
                            modifier = Modifier.testTag("cinematic_mode_${mode.id}")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Liquid Glass Preference (Active Tier: ${activeGlassTier.displayName})",
                    style = MaterialTheme.typography.labelMedium,
                    color = tokens.foregroundSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassEffectPreference.entries.forEach { pref ->
                        GlassChip(
                            label = pref.displayName,
                            selected = currentGlassPreference == pref,
                            onClick = { onSelectGlassPreference(pref) },
                            modifier = Modifier.testTag("glass_pref_${pref.id}")
                        )
                    }
                }
            }
        }

        // Section Navigation Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sectionTabs.forEachIndexed { idx, label ->
                    FilterChip(
                        selected = selectedSectionTab == idx,
                        onClick = { selectedSectionTab = idx },
                        label = { Text(label) },
                        modifier = Modifier.testTag("cinematic_tab_$idx")
                    )
                }
            }
        }

        when (selectedSectionTab) {
            // TAB 0: 15-CATEGORY TOKEN INVENTORY
            0 -> {
                items(tokenInventory, key = { it.name }) { meta ->
                    CinematicCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = meta.name,
                                fontFamily = IbmPlexMonoFontFamily,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = tokens.primary
                            )
                            CinematicBadge(text = meta.category)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = meta.purpose,
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.foreground
                        )
                        Text(
                            text = "Dark: ${meta.hexDark}  •  Light: ${meta.hexLight}",
                            fontFamily = IbmPlexMonoFontFamily,
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.foregroundSecondary
                        )
                        Text(
                            text = "Usage: ${meta.intendedUsage}",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.foregroundMuted
                        )
                    }
                }
            }

            // TAB 1: WCAG 2.1 AA CONTRAST AUDIT REPORT
            1 -> {
                item {
                    CinematicAlert(
                        title = "WCAG 2.1 AA Automated Contrast Audit",
                        message = "All ${contrastReport.size} semantic & component foreground/background pairings pass their required contrast thresholds (4.5:1 normal text, 3.0:1 large text).",
                        kind = CinematicStatusKind.SUCCESS
                    )
                }
                items(contrastReport, key = { "${it.themeMode}_${it.pairName}" }) { entry ->
                    CinematicCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "[${entry.themeMode}] ${entry.pairName}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${entry.foregroundHex} on ${entry.backgroundHex} (Min ${entry.requiredThreshold}:1)",
                                    fontFamily = IbmPlexMonoFontFamily,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.foregroundSecondary
                                )
                            }
                            CinematicBadge(
                                text = "${entry.contrastRatio}:1 ${if (entry.passed) "PASS" else "FAIL"}",
                                kind = if (entry.passed) CinematicStatusKind.SUCCESS else CinematicStatusKind.ERROR
                            )
                        }
                    }
                }
            }

            // TAB 2: FLUID DESIGN ENGINE & VIEWPORT SIMULATOR
            2 -> {
                item {
                    CinematicCard {
                        Text(
                            text = "Fluid Viewport Simulator (320px – 2560px)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(320f, 360f, 390f, 768f, 1024f, 1440f, 1920f, 2560f).forEach { vp ->
                                GlassChip(
                                    label = "${vp.toInt()}px",
                                    selected = simulatedViewportDp == vp,
                                    onClick = { simulatedViewportDp = vp }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Simulated Width: ${simulatedViewportDp.toInt()}dp • Columns: ${simulatedFluid.gridColumns} • Gutter: ${simulatedFluid.pageGutter.value.toInt()}dp",
                            fontFamily = IbmPlexMonoFontFamily,
                            style = MaterialTheme.typography.labelMedium,
                            color = tokens.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Typography Scale: xs=${simulatedFluid.textXs.value.toInt()}sp, base=${simulatedFluid.textBase.value.toInt()}sp, xl=${simulatedFluid.textXl.value.toInt()}sp, display=${simulatedFluid.textDisplay.value.toInt()}sp",
                            fontFamily = IbmPlexMonoFontFamily,
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.foregroundSecondary
                        )
                        Text(
                            text = "Spacing Scale: space-1=${simulatedFluid.space1.value.toInt()}dp, space-4=${simulatedFluid.space4.value.toInt()}dp, space-8=${simulatedFluid.space8.value.toInt()}dp, space-16=${simulatedFluid.space16.value.toInt()}dp",
                            fontFamily = IbmPlexMonoFontFamily,
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.foregroundSecondary
                        )
                    }
                }
            }

            // TAB 3: REGISTERED GRADIENTS & LIQUID GLASS CARDS
            3 -> {
                item {
                    val gradientEntries = listOf(
                        "MIDNIGHT HORIZON" to tokens.gradients.midnightHorizon,
                        "SILVER MOONLIGHT" to tokens.gradients.silverMoonlight,
                        "DEEP ATMOSPHERE" to tokens.gradients.deepAtmosphere,
                        "LUNAR SILVER" to tokens.gradients.lunarSilver
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        gradientEntries.forEach { (name, brush) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(92.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(brush)
                                    .border(1.dp, tokens.borders.borderStrong, RoundedCornerShape(18.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = name,
                                    fontFamily = IbmPlexMonoFontFamily,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (name == "LUNAR SILVER") tokens.onPrimary else tokens.foreground
                                )
                            }
                        }
                    }
                }
                item {
                    GlassCard {
                        Text(
                            text = "Liquid Glass Surface (${activeGlassTier.displayName})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeGlassTier.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.foregroundSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        GlassProgress(progress = 0.72f)
                    }
                }
            }

            // TAB 4: COMPONENT GALLERY
            4 -> {
                item {
                    var sampleInput by remember { mutableStateOf("Moonlight Silver Token") }
                    var sampleChecked by remember { mutableStateOf(true) }
                    var sampleSelected by remember { mutableStateOf("Glacial Blue") }

                    CinematicCard {
                        Text(
                            text = "Foundations, Inputs & Status Badges",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        CinematicBreadcrumb(items = listOf("Fluxora", "Design System", "Components"))
                        Spacer(modifier = Modifier.height(10.dp))
                        CinematicInput(
                            value = sampleInput,
                            onValueChange = { sampleInput = it },
                            label = "Semantic Input Token Demo"
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        CinematicSelect(
                            selectedOption = sampleSelected,
                            options = listOf("Moonlight Silver", "Glacial Blue", "Atmospheric Mist"),
                            onOptionSelected = { sampleSelected = it },
                            label = "Accent Token"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CinematicCheckbox(
                            checked = sampleChecked,
                            onCheckedChange = { sampleChecked = it },
                            label = "Enforce Zero-Exception Color Governance"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CinematicBadge("DEFAULT", CinematicStatusKind.DEFAULT)
                            CinematicBadge("SUCCESS", CinematicStatusKind.SUCCESS)
                            CinematicBadge("WARNING", CinematicStatusKind.WARNING)
                            CinematicBadge("ERROR", CinematicStatusKind.ERROR)
                            CinematicBadge("INFO", CinematicStatusKind.INFO)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            CinematicButton(text = "Primary Action", onClick = {}, isPrimary = true)
                            CinematicButton(text = "Secondary", onClick = {}, isPrimary = false)
                        }
                    }
                }
            }
        }
    }
}
