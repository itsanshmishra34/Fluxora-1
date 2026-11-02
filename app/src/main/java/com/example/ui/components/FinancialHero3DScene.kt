package com.example.ui.components

import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalHideSensitiveAmounts
import com.example.ui.theme.ShowcaseAccentCyan
import com.example.ui.theme.ShowcaseCyanIce
import com.example.ui.theme.ShowcaseEmeraldDark
import com.example.ui.theme.ShowcaseEmeraldDeep
import com.example.ui.theme.ShowcaseExpenseRed
import com.example.ui.theme.ShowcaseLightMintHighlight
import com.example.ui.theme.ShowcaseLiveBadgeBg
import com.example.ui.theme.ShowcaseLoginHeadingInk
import com.example.ui.theme.ShowcaseLoginSubtitleSlate
import com.example.ui.theme.ShowcasePrimaryEmerald
import com.example.ui.theme.ShowcasePureWhite
import com.example.ui.theme.ShowcaseReactorCardBg
import com.example.ui.theme.ShowcaseReactorCardBorder
import com.example.ui.theme.ShowcaseSubCardBg
import com.example.ui.theme.ShowcaseSubCardHighlightBg
import com.example.ui.theme.ShowcaseTextPrimary
import com.example.ui.theme.ShowcaseTextSecondary
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Formats integer paise (`amountMinor`) with Indian Rupee formatting (`₹12,450`)
 * respecting `hideSensitive`.
 */
fun formatShowcaseRupee(
    amountMinor: Long,
    showSign: Boolean = false,
    hideSensitive: Boolean = false
): String {
    if (hideSensitive) return "₹ ••••••"
    val isNegative = amountMinor < 0L
    val absMinor = abs(amountMinor)
    val rupees = absMinor / 100L
    val paise = absMinor % 100L

    val rawRupees = rupees.toString()
    val groupedRupees = if (rawRupees.length <= 3) {
        rawRupees
    } else {
        val lastThree = rawRupees.takeLast(3)
        val remaining = rawRupees.dropLast(3)
        val pairs = remaining.reversed().chunked(2).joinToString(",").reversed()
        "$pairs,$lastThree"
    }

    val signPrefix = when {
        isNegative -> "-₹"
        showSign && amountMinor > 0L -> "+₹"
        else -> "₹"
    }

    return if (paise == 0L) {
        "$signPrefix$groupedRupees"
    } else {
        "$signPrefix$groupedRupees.${paise.toString().padStart(2, '0')}"
    }
}

/**
 * Fluxora Signature Double-Wing Glowing 'F' Emblem.
 */
@Composable
fun FluxoraWingLogoIcon(
    modifier: Modifier = Modifier,
    size: Dp = 38.dp
) {
    val upperPath = remember { Path() }
    val upperHighlightPath = remember { Path() }
    val lowerPath = remember { Path() }
    val lowerHighlightPath = remember { Path() }

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ShowcasePrimaryEmerald.copy(alpha = 0.28f),
                    ShowcaseAccentCyan.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.5f),
                radius = w * 0.56f
            ),
            radius = w * 0.56f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        upperPath.reset()
        upperPath.moveTo(w * 0.22f, h * 0.64f)
        upperPath.cubicTo(w * 0.16f, h * 0.36f, w * 0.42f, h * 0.18f, w * 0.84f, h * 0.12f)
        upperPath.cubicTo(w * 0.80f, h * 0.38f, w * 0.58f, h * 0.48f, w * 0.32f, h * 0.58f)
        upperPath.quadraticBezierTo(w * 0.25f, h * 0.61f, w * 0.22f, h * 0.64f)
        upperPath.close()

        drawPath(
            path = upperPath,
            brush = Brush.linearGradient(
                colors = listOf(
                    ShowcaseEmeraldDark,
                    ShowcasePrimaryEmerald,
                    ShowcaseAccentCyan,
                    ShowcaseCyanIce
                ),
                start = Offset(w * 0.20f, h * 0.64f),
                end = Offset(w * 0.84f, h * 0.12f)
            )
        )

        upperHighlightPath.reset()
        upperHighlightPath.moveTo(w * 0.24f, h * 0.58f)
        upperHighlightPath.cubicTo(w * 0.22f, h * 0.36f, w * 0.45f, h * 0.20f, w * 0.82f, h * 0.14f)
        upperHighlightPath.cubicTo(w * 0.62f, h * 0.26f, w * 0.38f, h * 0.38f, w * 0.24f, h * 0.58f)
        upperHighlightPath.close()
        drawPath(path = upperHighlightPath, color = ShowcaseCyanIce.copy(alpha = 0.45f))

        lowerPath.reset()
        lowerPath.moveTo(w * 0.30f, h * 0.88f)
        lowerPath.cubicTo(w * 0.25f, h * 0.66f, w * 0.42f, h * 0.52f, w * 0.72f, h * 0.44f)
        lowerPath.cubicTo(w * 0.68f, h * 0.64f, w * 0.52f, h * 0.73f, w * 0.37f, h * 0.81f)
        lowerPath.quadraticBezierTo(w * 0.32f, h * 0.84f, w * 0.30f, h * 0.88f)
        lowerPath.close()

        drawPath(
            path = lowerPath,
            brush = Brush.linearGradient(
                colors = listOf(
                    ShowcaseEmeraldDeep,
                    ShowcasePrimaryEmerald,
                    ShowcaseAccentCyan,
                    ShowcaseLightMintHighlight
                ),
                start = Offset(w * 0.30f, h * 0.88f),
                end = Offset(w * 0.72f, h * 0.44f)
            )
        )

        lowerHighlightPath.reset()
        lowerHighlightPath.moveTo(w * 0.32f, h * 0.82f)
        lowerHighlightPath.cubicTo(w * 0.30f, h * 0.66f, w * 0.45f, h * 0.54f, w * 0.70f, h * 0.46f)
        lowerHighlightPath.cubicTo(w * 0.55f, h * 0.56f, w * 0.40f, h * 0.66f, w * 0.32f, h * 0.82f)
        lowerHighlightPath.close()
        drawPath(path = lowerHighlightPath, color = ShowcaseLightMintHighlight.copy(alpha = 0.42f))
    }
}

/**
 * Fluxora Horizontal Brand Header.
 */
@Composable
fun FluxoraBrandHeader(
    modifier: Modifier = Modifier,
    darkSurface: Boolean = true,
    showIcon: Boolean = true,
    iconSize: Dp = 36.dp,
    titleFontSize: TextUnit = 22.sp,
    subtitleFontSize: TextUnit = 8.sp,
    centered: Boolean = false
) {
    val fluxColor = if (darkSurface) ShowcasePureWhite else ShowcaseLoginHeadingInk
    val subtitleColor = if (darkSurface) ShowcaseTextSecondary else ShowcaseLoginSubtitleSlate

    val brandWordmark = remember(fluxColor) {
        buildAnnotatedString {
            withStyle(SpanStyle(color = fluxColor, fontWeight = FontWeight.ExtraBold)) { append("Flux") }
            withStyle(
                SpanStyle(
                    brush = Brush.linearGradient(colors = listOf(ShowcasePrimaryEmerald, ShowcaseAccentCyan)),
                    fontWeight = FontWeight.ExtraBold
                )
            ) { append("ora") }
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (centered) Arrangement.Center else Arrangement.Start
    ) {
        if (showIcon) {
            FluxoraWingLogoIcon(size = iconSize)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start) {
            Text(
                text = brandWordmark,
                style = TextStyle(fontSize = titleFontSize, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.3).sp)
            )
            Text(
                text = "FLOW  •  TRACK  •  GROW",
                style = TextStyle(fontSize = subtitleFontSize, fontWeight = FontWeight.SemiBold, letterSpacing = 1.8.sp, color = subtitleColor)
            )
        }
    }
}

/**
 * Particle data class for zero-allocation 3D particle stream rendering.
 */
private class Particle(
    var radius3D: Float,
    var angleTheta: Float,
    var anglePhi: Float,
    var speedTheta: Float,
    var size: Float,
    var alpha: Float,
    val isIncome: Boolean
)

/**
 * 3D Interactive Canvas Cash-Flow Reactor (`FinancialHero3DScene`).
 *
 * Features:
 * - Touch-drag 3D rotation around X and Y axes.
 * - Dual particle streams:
 *   - Inflow: Emerald Green & Cyan particles spiraling inward toward central orbital core.
 *   - Outflow: Sunset Coral Red particles spiraling outward from central core.
 * - Concentric 3D orbital rings inclined at 3D tilt angles.
 * - Pulsing central reactor core emitting radial energy aura.
 * - Live KPI breakdown: Income, Net Balance (`testTag("reactor_net_balance_text")`), and Expenses.
 * - High performance: Pre-allocated particle arrays, zero heap allocation inside `onDraw`.
 */
@Composable
fun FinancialHero3DScene(
    totalIncomeMinor: Long,
    totalExpenseMinor: Long,
    modifier: Modifier = Modifier
) {
    val hideSensitive = LocalHideSensitiveAmounts.current

    val effectiveIncomeMinor = if (totalIncomeMinor == 0L && totalExpenseMinor == 0L) 2_850_000L else totalIncomeMinor
    val effectiveExpenseMinor = if (totalIncomeMinor == 0L && totalExpenseMinor == 0L) 1_605_000L else totalExpenseMinor
    val netBalanceMinor = totalIncomeMinor - totalExpenseMinor
    val displayNetMinor = if (totalIncomeMinor == 0L && totalExpenseMinor == 0L) 1_245_000L else netBalanceMinor

    val totalFlow = (effectiveIncomeMinor + effectiveExpenseMinor).coerceAtLeast(1L)
    val incomePercent = ((effectiveIncomeMinor.toDouble() / totalFlow.toDouble()) * 100).toInt().coerceIn(1, 99)
    val expensePercent = 100 - incomePercent

    // 3D Rotation State
    var rotX by remember { mutableFloatStateOf(-0.35f) }
    var rotY by remember { mutableFloatStateOf(0.45f) }
    var targetRotX by remember { mutableFloatStateOf(-0.35f) }
    var targetRotY by remember { mutableFloatStateOf(0.45f) }

    // Pre-allocated Particle Array (40 Inflow particles + 30 Outflow particles)
    val particleCount = 70
    val particles = remember {
        Array(particleCount) { index ->
            val isIncome = index < 40
            Particle(
                radius3D = if (isIncome) Random.nextFloat() * 120f + 30f else Random.nextFloat() * 100f + 10f,
                angleTheta = Random.nextFloat() * 2f * Math.PI.toFloat(),
                anglePhi = Random.nextFloat() * Math.PI.toFloat() - Math.PI.toFloat() / 2f,
                speedTheta = (Random.nextFloat() * 0.02f + 0.01f) * if (isIncome) 1f else -1f,
                size = Random.nextFloat() * 4f + 2f,
                alpha = Random.nextFloat() * 0.6f + 0.3f,
                isIncome = isIncome
            )
        }
    }

    var pulsePhase by remember { mutableFloatStateOf(0f) }

    // Animation frame loop
    LaunchedEffect(Unit) {
        while (true) {
            withInfiniteAnimationFrameNanos { frameTimeNanos ->
                val dt = 0.016f
                rotX += (targetRotX - rotX) * 0.1f + 0.002f // subtle auto-drift
                rotY += (targetRotY - rotY) * 0.1f + 0.005f
                pulsePhase = (pulsePhase + 0.05f) % (2f * Math.PI.toFloat())

                // Update particle positions in 3D
                for (p in particles) {
                    p.angleTheta += p.speedTheta
                    if (p.isIncome) {
                        // Spiraling inward
                        p.radius3D -= 0.6f
                        if (p.radius3D < 20f) {
                            p.radius3D = 150f
                            p.angleTheta = Random.nextFloat() * 2f * Math.PI.toFloat()
                        }
                    } else {
                        // Spiraling outward
                        p.radius3D += 0.8f
                        if (p.radius3D > 140f) {
                            p.radius3D = 15f
                            p.angleTheta = Random.nextFloat() * 2f * Math.PI.toFloat()
                        }
                    }
                }
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        ShowcasePrimaryEmerald.copy(alpha = 0.45f),
                        ShowcaseReactorCardBorder,
                        ShowcaseAccentCyan.copy(alpha = 0.28f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("financial_hero_3d_scene"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ShowcaseReactorCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ShowcaseReactorCardBg,
                            ShowcasePrimaryEmerald.copy(alpha = 0.08f),
                            ShowcaseReactorCardBg
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Header Row: Title + "LIVE 3D REACTOR" badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Cash Flow Reactor ⚡",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ShowcaseTextPrimary
                    )
                    Text(
                        text = "Touch-drag to rotate 3D particle stream",
                        style = MaterialTheme.typography.bodySmall,
                        color = ShowcaseTextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = ShowcaseLiveBadgeBg,
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = ShowcasePrimaryEmerald.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(50)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(ShowcasePrimaryEmerald)
                        )
                        Text(
                            text = "LIVE 3D",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ShowcaseTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3D Canvas Scene Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0A1017))
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                ShowcasePrimaryEmerald.copy(alpha = 0.20f),
                                ShowcaseAccentCyan.copy(alpha = 0.15f)
                            )
                        ),
                        RoundedCornerShape(18.dp)
                    )
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            targetRotY += dragAmount.x * 0.01f
                            targetRotX -= dragAmount.y * 0.01f
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val centerX = w / 2f
                    val centerY = h / 2f
                    val focalLength = 220f

                    // 1. Draw 3D Radial Background Aura
                    val pulseScale = 1f + 0.08f * sin(pulsePhase)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ShowcasePrimaryEmerald.copy(alpha = 0.25f),
                                ShowcaseAccentCyan.copy(alpha = 0.10f),
                                Color.Transparent
                            ),
                            center = Offset(centerX, centerY),
                            radius = 90f * pulseScale
                        ),
                        radius = 90f * pulseScale,
                        center = Offset(centerX, centerY)
                    )

                    // 2. Draw 3D Orbital Rings
                    val cosX = cos(rotX)
                    val sinX = sin(rotX)
                    val cosY = cos(rotY)
                    val sinY = sin(rotY)

                    // Income Orbit Ring Points
                    draw3DOrbitRing(
                        centerX = centerX,
                        centerY = centerY,
                        focalLength = focalLength,
                        cosX = cosX, sinX = sinX,
                        cosY = cosY, sinY = sinY,
                        ringRadius = 85f,
                        color = ShowcasePrimaryEmerald,
                        tiltAngle = 0.4f
                    )

                    // Expense Orbit Ring Points
                    draw3DOrbitRing(
                        centerX = centerX,
                        centerY = centerY,
                        focalLength = focalLength,
                        cosX = cosX, sinX = sinX,
                        cosY = cosY, sinY = sinY,
                        ringRadius = 65f,
                        color = ShowcaseExpenseRed,
                        tiltAngle = -0.6f
                    )

                    // 3. Draw 3D Animated Particles
                    for (p in particles) {
                        // Calculate 3D sphere position
                        val x0 = p.radius3D * cos(p.angleTheta) * cos(p.anglePhi)
                        val y0 = p.radius3D * sin(p.anglePhi)
                        val z0 = p.radius3D * sin(p.angleTheta) * cos(p.anglePhi)

                        // Rotate around X-axis
                        val y1 = y0 * cosX - z0 * sinX
                        val z1 = y0 * sinX + z0 * cosX

                        // Rotate around Y-axis
                        val x2 = x0 * cosY + z1 * sinY
                        val z2 = -x0 * sinY + z1 * cosY

                        val scale = focalLength / (focalLength + z2 + 150f)
                        val screenX = centerX + x2 * scale
                        val screenY = centerY + y1 * scale

                        val drawRadius = (p.size * scale).coerceAtLeast(1f)
                        val pColor = if (p.isIncome) {
                            ShowcasePrimaryEmerald.copy(alpha = (p.alpha * scale).coerceIn(0.1f, 1f))
                        } else {
                            ShowcaseExpenseRed.copy(alpha = (p.alpha * scale).coerceIn(0.1f, 1f))
                        }

                        drawCircle(
                            color = pColor,
                            radius = drawRadius,
                            center = Offset(screenX, screenY)
                        )
                    }

                    // 4. Central Reactor Core Sphere
                    val coreRadius = 14f * pulseScale
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ShowcasePureWhite,
                                ShowcaseAccentCyan,
                                ShowcaseEmeraldDeep
                            ),
                            center = Offset(centerX - 3f, centerY - 3f),
                            radius = coreRadius
                        ),
                        radius = coreRadius,
                        center = Offset(centerX, centerY)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dual Flow Ratio Bar (Income vs Outflow)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Inflow $incomePercent%",
                        style = MaterialTheme.typography.labelSmall,
                        color = ShowcasePrimaryEmerald,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Outflow $expensePercent%",
                        style = MaterialTheme.typography.labelSmall,
                        color = ShowcaseExpenseRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(ShowcaseSubCardBg)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(incomePercent.toFloat())
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(ShowcasePrimaryEmerald, ShowcaseAccentCyan)
                                )
                            )
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .weight(expensePercent.toFloat())
                            .fillMaxSize()
                            .background(ShowcaseExpenseRed)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom 3 Glass Metric Pills (Income | Net Flow | Expenses)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Income Pill
                Surface(
                    modifier = Modifier.weight(1.05f),
                    shape = RoundedCornerShape(14.dp),
                    color = ShowcaseSubCardBg,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        ShowcasePrimaryEmerald.copy(alpha = 0.25f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(ShowcasePrimaryEmerald.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Income Trend",
                                tint = ShowcasePrimaryEmerald,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Income",
                                style = MaterialTheme.typography.labelSmall,
                                color = ShowcaseTextSecondary,
                                maxLines = 1
                            )
                            Text(
                                text = formatShowcaseRupee(
                                    amountMinor = effectiveIncomeMinor,
                                    hideSensitive = hideSensitive
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = ShowcasePrimaryEmerald,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // 2. Net Flow Center Highlight Pill
                Surface(
                    modifier = Modifier.weight(0.95f),
                    shape = RoundedCornerShape(14.dp),
                    color = ShowcaseSubCardHighlightBg,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        ShowcaseAccentCyan.copy(alpha = 0.30f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Net Flow",
                            style = MaterialTheme.typography.labelSmall,
                            color = ShowcaseTextSecondary,
                            maxLines = 1
                        )
                        Text(
                            text = formatShowcaseRupee(
                                amountMinor = displayNetMinor,
                                hideSensitive = hideSensitive
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = ShowcasePureWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("reactor_net_balance_text")
                        )
                    }
                }

                // 3. Expenses Pill
                Surface(
                    modifier = Modifier.weight(1.05f),
                    shape = RoundedCornerShape(14.dp),
                    color = ShowcaseSubCardBg,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        ShowcaseExpenseRed.copy(alpha = 0.22f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(ShowcaseExpenseRed.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Expense Trend",
                                tint = ShowcaseExpenseRed,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Expenses",
                                style = MaterialTheme.typography.labelSmall,
                                color = ShowcaseTextSecondary,
                                maxLines = 1
                            )
                            Text(
                                text = formatShowcaseRupee(
                                    amountMinor = effectiveExpenseMinor,
                                    hideSensitive = hideSensitive
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = ShowcaseExpenseRed,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders a 3D projected elliptical ring on Canvas with depth perspective.
 */
private fun DrawScope.draw3DOrbitRing(
    centerX: Float,
    centerY: Float,
    focalLength: Float,
    cosX: Float, sinX: Float,
    cosY: Float, sinY: Float,
    ringRadius: Float,
    color: Color,
    tiltAngle: Float
) {
    val steps = 36
    var prevX = 0f
    var prevY = 0f
    var prevValid = false

    val cosTilt = cos(tiltAngle)
    val sinTilt = sin(tiltAngle)

    for (i in 0..steps) {
        val angle = (i.toFloat() / steps) * 2f * Math.PI.toFloat()
        val x0 = ringRadius * cos(angle)
        val y0 = ringRadius * sin(angle) * sinTilt
        val z0 = ringRadius * sin(angle) * cosTilt

        val y1 = y0 * cosX - z0 * sinX
        val z1 = y0 * sinX + z0 * cosX

        val x2 = x0 * cosY + z1 * sinY
        val z2 = -x0 * sinY + z1 * cosY

        val scale = focalLength / (focalLength + z2 + 150f)
        val screenX = centerX + x2 * scale
        val screenY = centerY + y1 * scale

        if (prevValid) {
            val strokeAlpha = ((z2 + 100f) / 200f).coerceIn(0.15f, 0.75f)
            drawLine(
                color = color.copy(alpha = strokeAlpha),
                start = Offset(prevX, prevY),
                end = Offset(screenX, screenY),
                strokeWidth = (2f * scale).coerceAtLeast(1f)
            )
        }
        prevX = screenX
        prevY = screenY
        prevValid = true
    }
}

/**
 * Alias for Cash Flow Overview Card.
 */
@Composable
fun CashFlowOverviewCard(
    totalIncomeMinor: Long,
    totalExpenseMinor: Long,
    modifier: Modifier = Modifier
) {
    FinancialHero3DScene(
        totalIncomeMinor = totalIncomeMinor,
        totalExpenseMinor = totalExpenseMinor,
        modifier = modifier
    )
}
