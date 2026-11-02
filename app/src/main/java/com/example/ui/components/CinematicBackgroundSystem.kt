package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.FluxoraCosmicViolet
import com.example.ui.theme.FluxoraDeepOcean
import com.example.ui.theme.FluxoraDreamLavender
import com.example.ui.theme.FluxoraMidnightAbyss
import com.example.ui.theme.FluxoraMoonlightBlue

/**
 * ============================================================================
 * CINEMATIC BACKGROUND IMAGE SYSTEM — FLUXORA
 * ============================================================================
 * Full-screen, multi-layered atmospheric background experience inspired by the
 * Starlit Lake reference image (Fluxora Login by the Starlit Lake):
 *
 * Layer 0: High-resolution photographic starlit lake & pier asset (R.drawable.img_starlit_lake_background)
 * Layer 1: Base dark overlay — establishes the Midnight Abyss (#101722) foundation
 * Layer 2: Vertical readability gradient — protects header/form contrast while preserving starry depth
 * Layer 3: Subtle Cosmic Violet atmosphere (#7966B5) — soft celestial illumination
 * Layer 4: Edge vignette — subtle darkening near screen borders for cinematic focus
 *
 * Fallback: Graceful multi-stop gradient when assets are absent or in preview modes.
 */
@Composable
fun CinematicStarlitLakeBackground(
    modifier: Modifier = Modifier,
    baseOverlayAlpha: Float = 0.12f,
    showVioletAtmosphere: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Dedicated Background Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            FluxoraMidnightAbyss,
                            FluxoraDeepOcean,
                            FluxoraMidnightAbyss
                        )
                    )
                )
                .testTag("cinematic_background_system")
        ) {
            // Layer 0: Photographic Starlit Lake Canvas / Asset
            Image(
                painter = painterResource(id = R.drawable.img_starlit_lake_background),
                contentDescription = "Fluxora Starlit Lake Cinematic Backdrop",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("cinematic_background_image")
            )

        // Multi-Layer Cinematic Overlay System
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("cinematic_overlay_canvas")
        ) {
            val w = size.width
            val h = size.height

            // Layer 1: Base Dark Overlay (Midnight Abyss #101722 foundation)
            drawRect(
                color = FluxoraMidnightAbyss.copy(alpha = baseOverlayAlpha)
            )

            // Layer 2: Vertical Readability Gradient (Optimal contrast behind form while revealing dock & stars)
            drawRect(
                brush = Brush.verticalGradient(
                    0.00f to FluxoraMidnightAbyss.copy(alpha = 0.25f),
                    0.20f to FluxoraMidnightAbyss.copy(alpha = 0.40f),
                    0.45f to FluxoraMidnightAbyss.copy(alpha = 0.45f),
                    0.68f to FluxoraMidnightAbyss.copy(alpha = 0.20f),
                    0.82f to Color.Transparent,
                    1.00f to FluxoraMidnightAbyss.copy(alpha = 0.15f)
                )
            )

            // Layer 3: Subtle Cosmic Violet Celestial Atmosphere (#7966B5)
            if (showVioletAtmosphere) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            FluxoraCosmicViolet.copy(alpha = 0.22f),
                            FluxoraCosmicViolet.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.50f, h * 0.28f),
                        radius = w * 0.75f
                    ),
                    radius = w * 0.75f,
                    center = Offset(w * 0.50f, h * 0.28f)
                )
            }

            // Layer 4: Edge Vignette (Darkening outer boundaries)
            drawRect(
                brush = Brush.radialGradient(
                    0.00f to Color.Transparent,
                    0.65f to Color.Transparent,
                    0.90f to FluxoraMidnightAbyss.copy(alpha = 0.40f),
                    1.00f to FluxoraMidnightAbyss.copy(alpha = 0.75f),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = maxOf(w, h) * 0.72f
                )
            )
        }
        }

        // Foreground Glassmorphic Content
        content()
    }
}

/**
 * Celestial Wing / Flame Emblem in Cosmic Violet and Dream Lavender,
 * matching the starlit lake visual reference.
 */
@Composable
fun FluxoraCosmicEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Image(
        painter = painterResource(id = R.drawable.ic_fluxora_cosmic_logo),
        contentDescription = "Fluxora Logo Emblem",
        contentScale = ContentScale.Fit,
        modifier = modifier.size(size)
    )
}

/**
 * Centered Brand Header for the Starlit Lake screen:
 * Authentic Cosmic Emblem + "Fluxora" (Pure White + Cosmic Violet gradient) + "FLOW • TRACK • GROW"
 */
@Composable
fun FluxoraCosmicBrandHeader(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        FluxoraCosmicEmblem(size = 40.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Flux",
                    style = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )
                )
                Text(
                    text = "ora",
                    style = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        brush = Brush.horizontalGradient(
                            colors = listOf(FluxoraCosmicViolet, FluxoraDreamLavender)
                        ),
                        letterSpacing = (-0.5).sp
                    )
                )
            }
            Text(
                text = "FLOW  •  TRACK  •  GROW",
                style = TextStyle(
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = FluxoraMoonlightBlue,
                    letterSpacing = 2.4.sp
                )
            )
        }
    }
}
