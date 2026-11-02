package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logging.FluxoraLogger
import com.example.logging.LogEntry
import com.example.ui.theme.FluxoraAuroraMint
import com.example.ui.theme.FluxoraCosmicViolet
import com.example.ui.theme.FluxoraDeepOcean
import com.example.ui.theme.FluxoraDreamLavender
import com.example.ui.theme.FluxoraMidnightAbyss
import com.example.ui.theme.FluxoraMoonlightBlue
import com.example.ui.theme.FluxoraStormTeal
import com.example.ui.theme.FluxoraSunsetCoral
import com.example.ui.theme.ShowcasePureWhite
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

/**
 * Controller class to allow composables to report runtime errors to the nearest boundary.
 */
class FluxoraErrorController(
    private val onErrorCaptured: (Throwable, String) -> Unit
) {
    fun reportError(throwable: Throwable, tag: String = "UiRuntime") {
        FluxoraLogger.recordException(throwable, tag)
        onErrorCaptured(throwable, tag)
    }
}

val LocalFluxoraErrorHandler = compositionLocalOf<FluxoraErrorController> {
    FluxoraErrorController { throwable, tag ->
        FluxoraLogger.recordException(throwable, tag)
    }
}

/**
 * Centralized Error Boundary Composable.
 * Wraps tree hierarchy, captures composable/coroutine failures, and presents a fallback UI
 * instead of letting the application crash or display a blank screen.
 */
@Composable
fun FluxoraErrorBoundary(
    modifier: Modifier = Modifier,
    onReset: () -> Unit = {},
    content: @Composable () -> Unit
) {
    var capturedThrowable by remember { mutableStateOf<Throwable?>(null) }
    var capturedTag by remember { mutableStateOf("FluxoraUi") }

    val controller = remember {
        FluxoraErrorController { throwable, tag ->
            capturedTag = tag
            capturedThrowable = throwable
        }
    }

    CompositionLocalProvider(LocalFluxoraErrorHandler provides controller) {
        Box(modifier = modifier.fillMaxSize().testTag("fluxora_error_boundary")) {
            if (capturedThrowable != null) {
                FluxoraErrorFallbackScreen(
                    throwable = capturedThrowable!!,
                    tag = capturedTag,
                    onRetry = {
                        capturedThrowable = null
                        onReset()
                    }
                )
            } else {
                content()
            }
        }
    }
}

@Composable
fun FluxoraErrorFallbackScreen(
    throwable: Throwable,
    tag: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isDetailsExpanded by remember { mutableStateOf(false) }

    val errorMessage = throwable.localizedMessage ?: throwable.message ?: "An unexpected error occurred."
    val stackTrace = throwable.stackTraceToString()

    Box(
        modifier = modifier
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
            .padding(24.dp)
            .testTag("fluxora_error_fallback_screen"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(1.dp, FluxoraStormTeal.copy(alpha = 0.5f)),
                    RoundedCornerShape(24.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = FluxoraDeepOcean.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning Icon Header
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(FluxoraSunsetCoral.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = FluxoraSunsetCoral,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Something Went Wrong",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = FluxoraDreamLavender,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Fluxora caught a runtime error gracefully to prevent the app from freezing or showing a blank screen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FluxoraMoonlightBlue,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Error Summary Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = FluxoraMidnightAbyss.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, FluxoraSunsetCoral.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Error Message:",
                            style = MaterialTheme.typography.labelMedium,
                            color = FluxoraSunsetCoral,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = FluxoraDreamLavender,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Expandable Technical Details Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isDetailsExpanded = !isDetailsExpanded }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isDetailsExpanded) "Hide Technical Stacktrace" else "Show Technical Stacktrace",
                        style = MaterialTheme.typography.labelMedium,
                        color = FluxoraStormTeal,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = if (isDetailsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle stacktrace",
                        tint = FluxoraStormTeal
                    )
                }

                AnimatedVisibility(
                    visible = isDetailsExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        color = FluxoraMidnightAbyss,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, FluxoraStormTeal.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = stackTrace.take(1500),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = FluxoraMoonlightBlue,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("error_retry_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FluxoraCosmicViolet,
                            contentColor = ShowcasePureWhite
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Try Again",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Fluxora Stacktrace", "Error: $errorMessage\n\nStacktrace:\n$stackTrace")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Error log copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("error_copy_button"),
                        border = BorderStroke(1.dp, FluxoraStormTeal),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = FluxoraMoonlightBlue
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Copy Log",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
