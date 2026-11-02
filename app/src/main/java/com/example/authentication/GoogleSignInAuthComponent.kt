package com.example.authentication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.ui.theme.ShowcaseGoogleBlue
import com.example.ui.theme.ShowcaseGoogleBtnSurface
import com.example.ui.theme.ShowcaseGoogleGreen
import com.example.ui.theme.ShowcaseGoogleRed
import com.example.ui.theme.ShowcaseGoogleYellow
import com.example.ui.theme.ShowcaseLoginFieldBorder
import com.example.ui.theme.ShowcaseLoginHeadingInk

/**
 * Multi-color Google 'G' vector emblem matching the Login Screen in `Fluxora Finance App Showcase.png`.
 */
@Composable
fun GoogleMulticolorLogoIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(20.dp)) {
        val strokeWidth = size.width * 0.20f
        val diameter = size.width - strokeWidth
        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
        val arcSize = Size(diameter, diameter)

        // Red upper arc
        drawArc(
            color = ShowcaseGoogleRed,
            startAngle = -145f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Yellow left arc
        drawArc(
            color = ShowcaseGoogleYellow,
            startAngle = 140f,
            sweepAngle = 75f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Green bottom arc
        drawArc(
            color = ShowcaseGoogleGreen,
            startAngle = 45f,
            sweepAngle = 95f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Blue right arc
        drawArc(
            color = ShowcaseGoogleBlue,
            startAngle = -12f,
            sweepAngle = 57f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Blue horizontal crossbar
        drawLine(
            color = ShowcaseGoogleBlue,
            start = Offset(size.width * 0.52f, size.height * 0.50f),
            end = Offset(size.width * 0.94f, size.height * 0.50f),
            strokeWidth = strokeWidth * 0.92f,
            cap = StrokeCap.Butt
        )
    }
}

/**
 * Reusable Jetpack Compose UI component that implements Google Sign-In and Sign-Out
 * using Firebase Authentication (`com.google.firebase.auth`) and Jetpack Credential Manager
 * (`androidx.credentials` + `com.google.android.libraries.identity.googleid`).
 */
@Composable
fun GoogleSignInAuthComponent(
    isAuthenticated: Boolean,
    userDisplayName: String = "",
    userEmail: String = "",
    isAuthInProgress: Boolean = false,
    errorMessage: String? = null,
    onSignInClick: (() -> Unit)? = null,
    onSignOutClick: (() -> Unit)? = null,
    onAuthSuccess: () -> Unit = {},
    onAuthError: (String) -> Unit = {},
    onSignOutComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var internalLoading by remember { mutableStateOf(false) }
    var internalError by remember { mutableStateOf<String?>(null) }

    val effectiveLoading = isAuthInProgress || internalLoading
    val effectiveError = errorMessage ?: internalError

    val handleSignIn: () -> Unit = {
        if (onSignInClick != null) {
            onSignInClick()
        } else {
            internalLoading = true
            internalError = null
            AuthManager.onGoogleSignInClicked(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                    internalLoading = false
                    internalError = null
                    onAuthSuccess()
                },
                onAuthError = { err ->
                    internalLoading = false
                    internalError = err
                    onAuthError(err)
                },
                scope = scope,
                onAuthCancelled = {
                    internalLoading = false
                }
            )
        }
    }

    val handleSignOut: () -> Unit = {
        if (onSignOutClick != null) {
            onSignOutClick()
        } else {
            internalLoading = true
            AuthManager.signOut(
                context = context,
                credentialManager = credentialManager,
                onSignOutComplete = {
                    internalLoading = false
                    internalError = null
                    onSignOutComplete()
                },
                scope = scope
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("google_sign_in_auth_component"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (!effectiveError.isNullOrBlank()) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("google_auth_error_banner")
            ) {
                Text(
                    text = effectiveError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        if (isAuthenticated) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            val initial = userDisplayName.trim().firstOrNull()?.uppercaseChar()
                            if (initial != null) {
                                Text(
                                    text = initial.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Authenticated User",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = userDisplayName.ifBlank { "Fluxora Member" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.testTag("google_auth_user_name")
                                )
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Google Verified Account",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = userEmail.ifBlank { "Authenticated via Google" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.testTag("google_auth_user_email")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = handleSignOut,
                        enabled = !effectiveLoading,
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sign Out")
                    }
                }
            }
        } else {
            // Showcase White Pill "Continue with Google" Button
            Button(
                onClick = handleSignIn,
                enabled = !effectiveLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("google_sign_in_button"),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ShowcaseGoogleBtnSurface,
                    contentColor = ShowcaseLoginHeadingInk
                ),
                border = BorderStroke(1.dp, ShowcaseLoginFieldBorder),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                if (effectiveLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = ShowcaseLoginHeadingInk
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Authenticating with Google...",
                        color = ShowcaseLoginHeadingInk,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    GoogleMulticolorLogoIcon()
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continue with Google",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ShowcaseLoginHeadingInk
                    )
                }
            }
        }
    }
}
