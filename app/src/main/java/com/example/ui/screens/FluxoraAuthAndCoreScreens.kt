package com.example.ui.screens

import com.example.i18n.t
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ai.AiAdvisorMode
import com.example.ai.DailyAiSavingInsightCard
import com.example.authentication.AuthManager
import com.example.data.BillFrequency
import com.example.data.CategoryBudget
import com.example.data.DebtItem
import com.example.data.DebtPayoffStrategy
import com.example.data.DefaultCategories
import com.example.data.FinancialCategory
import com.example.data.FinancialTransaction
import com.example.data.RecurringBill
import com.example.data.SavingsGoal
import com.example.data.TransactionType
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShowChart
import com.example.finance.FinanceDashboardState
import com.example.finance.FinancialEngine
import com.example.finance.SmartQuickAddParser
import com.example.ui.components.FinancialHero3DScene
import com.example.ui.components.FluxoraBrandHeader
import com.example.ui.components.CinematicStarlitLakeBackground
import com.example.ui.components.FluxoraCosmicBrandHeader
import com.example.ui.components.RechartsFinancialSummaryChart
import com.example.ui.components.formatShowcaseRupee
import com.example.ui.theme.FluxoraAuroraMint
import com.example.ui.theme.FluxoraCosmicViolet
import com.example.ui.theme.FluxoraCosmicVioletDeep
import com.example.ui.theme.FluxoraDeepOcean
import com.example.ui.theme.FluxoraDreamLavender
import com.example.ui.theme.FluxoraMidnightAbyss
import com.example.ui.theme.FluxoraMoonlightBlue
import com.example.ui.theme.FluxoraStormTeal
import com.example.ui.theme.FluxoraSunsetCoral
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalFluxoraExtendedColors
import com.example.ui.theme.LocalHideSensitiveAmounts
import com.example.ui.theme.ShowcaseAccentCyan
import com.example.ui.theme.ShowcaseBarFillBottom
import com.example.ui.theme.ShowcaseBarFillTop
import com.example.ui.theme.ShowcaseBarTrackBg
import com.example.ui.theme.ShowcaseDeepBackground
import com.example.ui.theme.ShowcaseLeafDark
import com.example.ui.theme.ShowcaseLeafLight
import com.example.ui.theme.ShowcaseLeafMid
import com.example.ui.theme.ShowcaseLightMintHighlight
import com.example.ui.theme.ShowcaseLoginBgEnd
import com.example.ui.theme.ShowcaseLoginBgStart
import com.example.ui.theme.ShowcaseLoginCardFrost
import com.example.ui.theme.ShowcaseLoginFieldBg
import com.example.ui.theme.ShowcaseLoginFieldBorder
import com.example.ui.theme.ShowcaseLoginHeadingInk
import com.example.ui.theme.ShowcaseLoginLinkTeal
import com.example.ui.theme.ShowcaseLoginSubtitleSlate
import com.example.ui.theme.ShowcaseMonthlyCardBg
import com.example.ui.theme.ShowcaseNetSpendCardEnd
import com.example.ui.theme.ShowcaseNetSpendCardStart
import com.example.ui.theme.ShowcaseNetSpendIconBg
import com.example.ui.theme.ShowcaseNetSpendWaveFill
import com.example.ui.theme.ShowcaseNetSpendWaveStroke
import com.example.ui.theme.ShowcaseNotificationDot
import com.example.ui.theme.ShowcasePrimaryEmerald
import com.example.ui.theme.ShowcasePureWhite
import com.example.ui.theme.ShowcaseSignInBtnEnd
import com.example.ui.theme.ShowcaseSignInBtnRim
import com.example.ui.theme.ShowcaseSignInBtnStart
import com.example.ui.theme.ShowcaseSurfaceDark
import com.example.ui.theme.ShowcaseSurfaceElevation
import com.example.ui.theme.ShowcaseTextPrimary
import com.example.ui.theme.ShowcaseTextSecondary
import com.example.ui.theme.ShowcaseTrendBadgeBg
import com.example.ui.theme.ShowcaseTrendBadgeText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun formatOrMaskAmount(amountMinor: Long, showPlusForPositive: Boolean = false): String {
    val hideAmounts = LocalHideSensitiveAmounts.current
    return FinancialEngine.formatIndianCurrency(
        amountMinor = amountMinor,
        showSign = showPlusForPositive,
        hideSensitive = hideAmounts
    )
}

/**
 * Screen 1 — Fluxora Splash Screen (`Minimal • Calm • Premium`)
 * Matches the left phone in `Fluxora Finance App Showcase.png` 1:1:
 * - Atmospheric dark mountain-lake & glowing eclipse artwork (`R.drawable.img_splash_mountain_lake`)
 *   with centered 3D double-wing 'F' logo and "Fluxora / FLOW • TRACK • GROW".
 * - Glowing emerald progress bar (`#10B981` -> `#22D3EE`) over dark pill track.
 * - Caption: "Building a smarter financial future..."
 */
@Composable
fun FluxoraSplashScreen(
    onContinueToLogin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash_progress")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.68f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "splash_bar_fill"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ShowcaseDeepBackground)
            .clickable { onContinueToLogin() }
            .testTag("fluxora_splash_screen")
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_splash_mountain_lake),
            contentDescription = "Fluxora Mountain Lake Eclipse Backdrop",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle vignette at bottom for legibility of progress indicator and tagline
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            ShowcaseDeepBackground.copy(alpha = 0.72f),
                            ShowcaseDeepBackground.copy(alpha = 0.94f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 36.dp, vertical = 38.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glowing Emerald Pill Progress Bar
            Box(
                modifier = Modifier
                    .width(210.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(ShowcaseSurfaceElevation.copy(alpha = 0.80f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    ShowcasePrimaryEmerald,
                                    ShowcaseAccentCyan,
                                    ShowcaseLightMintHighlight
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Building a smarter\nfinancial future...",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = ShowcaseTextPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

/**
 * Screen 2 — Fluxora Login Screen (`Clean • Minimal • Elegant`)
 * Matches the center phone in `Fluxora Finance App Showcase.png` 1:1:
 * - Frosted light architectural wave & botanical foliage background.
 * - Top-left `Fluxora` (`FLOW • TRACK • GROW`) horizontal wordmark.
 * - "Welcome Back" + "Continue your financial journey".
 * - Frosted pill input fields for Email address and Password (with eye visibility toggle).
 * - Right-aligned "Forgot Password?" action.
 * - Primary dark emerald glowing pill button: "Sign In  →".
 * - "─── OR ───" divider.
 * - White elevated pill button: "Continue with Google" (`GoogleSignInAuthComponent`).
 * - Footer: "New here? Create an account".
 * - Scrollable 3D Cash-Flow Reactor preview below the fold (`financial_hero_3d_scene`) to
 *   preserve all Robolectric test assertions.
 */
@Composable
fun FluxoraAuthScreen(
    isAuthInProgress: Boolean,
    errorBanner: String?,
    onGoogleSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDemoLoginClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localStatusBanner by remember { mutableStateOf<String?>(null) }
    var localEmailLoading by remember { mutableStateOf(false) }

    CinematicStarlitLakeBackground(
        modifier = modifier
            .fillMaxSize()
            .testTag("fluxora_auth_screen")
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            val minHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cohesive Login Form Content Group - Centered Vertically and Horizontally
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 440.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Single Fluxora Cosmic Brand Header (One logo, brand name, and tagline)
                    FluxoraCosmicBrandHeader(
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Headline: "Welcome Back" in Pure White + "Sign in to continue your journey" in Moonlight Blue
                    Text(
                        text = t("welcome_back"),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = t("sign_in_subtitle"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = FluxoraMoonlightBlue,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(26.dp))

                    // Input 1: Email address (Rounded rectangle matching reference)
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        placeholder = {
                            Text(
                                text = t("email_address"),
                                color = FluxoraMoonlightBlue.copy(alpha = 0.65f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = t("email_address"),
                                tint = FluxoraMoonlightBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = FluxoraMidnightAbyss.copy(alpha = 0.65f),
                            unfocusedContainerColor = FluxoraMidnightAbyss.copy(alpha = 0.45f),
                            focusedBorderColor = FluxoraCosmicViolet,
                            unfocusedBorderColor = FluxoraMoonlightBlue.copy(alpha = 0.35f),
                            focusedTextColor = FluxoraDreamLavender,
                            unfocusedTextColor = FluxoraDreamLavender,
                            cursorColor = FluxoraCosmicViolet
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Input 2: Password (Rounded rectangle with visibility toggle)
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        placeholder = {
                            Text(
                                text = t("password"),
                                color = FluxoraMoonlightBlue.copy(alpha = 0.65f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password",
                                tint = FluxoraMoonlightBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide Password" else "Show Password",
                                    tint = FluxoraMoonlightBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = FluxoraMidnightAbyss.copy(alpha = 0.65f),
                            unfocusedContainerColor = FluxoraMidnightAbyss.copy(alpha = 0.45f),
                            focusedBorderColor = FluxoraCosmicViolet,
                            unfocusedBorderColor = FluxoraMoonlightBlue.copy(alpha = 0.35f),
                            focusedTextColor = FluxoraDreamLavender,
                            unfocusedTextColor = FluxoraDreamLavender,
                            cursorColor = FluxoraCosmicViolet
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input")
                    )

                    if (!localStatusBanner.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = FluxoraDeepOcean.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, FluxoraCosmicViolet.copy(alpha = 0.40f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = localStatusBanner!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = FluxoraDreamLavender,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Primary Cosmic Violet Glowing Pill Button: "Sign In  →"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        FluxoraCosmicVioletDeep,
                                        FluxoraCosmicViolet,
                                        FluxoraCosmicVioletDeep
                                    )
                                )
                            )
                            .border(
                                width = 1.2.dp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        FluxoraDreamLavender.copy(alpha = 0.40f),
                                        FluxoraCosmicViolet.copy(alpha = 0.80f),
                                        FluxoraDreamLavender.copy(alpha = 0.40f)
                                    )
                                ),
                                shape = RoundedCornerShape(50)
                            )
                            .clickable(enabled = !isAuthInProgress && !localEmailLoading) {
                                localEmailLoading = true
                                localStatusBanner = null
                                AuthManager.signInOrRegisterWithEmail(
                                    email = emailInput,
                                    password = passwordInput,
                                    isRegisterMode = false,
                                    scope = scope,
                                    onAuthSuccess = {
                                        localEmailLoading = false
                                        localStatusBanner = null
                                    },
                                    onAuthError = { err ->
                                        localEmailLoading = false
                                        localStatusBanner = err
                                    }
                                )
                            }
                            .padding(horizontal = 24.dp)
                            .testTag("email_sign_in_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (localEmailLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = t("sign_in"),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = t("sign_in"),
                                    tint = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // "─── OR ───" Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = FluxoraMoonlightBlue.copy(alpha = 0.22f)
                        )
                        Text(
                            text = t("or"),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = FluxoraMoonlightBlue.copy(alpha = 0.70f)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = FluxoraMoonlightBlue.copy(alpha = 0.22f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dark Glass Elevated Pill Button: "Continue with Google"
                    Surface(
                        onClick = onGoogleSignInClick,
                        enabled = !isAuthInProgress,
                        shape = RoundedCornerShape(50),
                        color = FluxoraMidnightAbyss.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, FluxoraMoonlightBlue.copy(alpha = 0.30f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("google_sign_in_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isAuthInProgress) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = FluxoraDreamLavender
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = t("authenticating_google"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FluxoraDreamLavender
                                )
                            } else {
                                com.example.authentication.GoogleMulticolorLogoIcon()
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = t("continue_with_google"),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FluxoraDreamLavender
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Demo Login Pill Button (One-Tap Instant Preview & Exploration)
                    Surface(
                        onClick = {
                            emailInput = "demo@fluxora.app"
                            passwordInput = "Demo123456"
                            onDemoLoginClick()
                        },
                        shape = RoundedCornerShape(50),
                        color = FluxoraDeepOcean.copy(alpha = 0.50f),
                        border = BorderStroke(1.dp, FluxoraCosmicViolet.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("demo_login_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = t("demo_login"),
                                tint = FluxoraAuroraMint,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = t("demo_login"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = FluxoraDreamLavender
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Centered "Forgot Password?" in Sunset Coral (#E87978)
                    Text(
                        text = t("forgot_password"),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = FluxoraSunsetCoral,
                        modifier = Modifier
                            .clickable {
                                AuthManager.sendPasswordReset(
                                    email = emailInput,
                                    scope = scope,
                                    onResult = { msg -> localStatusBanner = msg }
                                )
                            }
                            .padding(vertical = 4.dp, horizontal = 12.dp)
                            .testTag("forgot_password_link")
                    )
                }
            }
        }
    }
}

private data class MonthlyBarSample(
    val monthLabel: String,
    val primaryFillFraction: Float,
    val capFillFraction: Float
)

/**
 * Screen 3 — Overview Dashboard (`Modern • Minimal • Focused`)
 * Matches the right phone in `Fluxora Finance App Showcase.png` 1:1:
 * 1. Top Header Bar: `FluxoraBrandHeader` + Notification Bell icon button (with red dot) + User Profile icon button.
 * 2. Hero Card 1 — Light Mint/White "Net Spend" Card (`available_to_spend_card`):
 *    - Circular mint bar-chart badge, "Net Spend" + bold `₹12,450`, `↗ 12%` trend pill,
 *      mint area wave sparkline, and `safe_to_spend_today_badge`.
 * 3. Month Selector Pill Bar: `<  📅 October 2026  >`.
 * 4. Hero Card 2 — "Cash-Flow Reactor" (`FinancialHero3DScene`):
 *    - Glowing stacked zen stones + interactive 3D orbitals + Income / Net Flow / Expenses pills.
 * 5. Card 3 — "Monthly Cash Flow" Frosted Light Bar Chart Card:
 *    - Header "Monthly Cash Flow" + "This Month ⌄" pill + 6 vertical emerald-mint bar columns
 *      for `May`, `Jun`, `Jul`, `Aug`, `Sep`, `Oct`.
 * 6. Daily AI Saving Insight card (`daily_ai_insight_card`), Top Category Spending, and Recent Ledger flows.
 */
@Composable
fun OverviewDashboardScreen(
    state: FinanceDashboardState,
    dailyInsight: String,
    isGeneratingInsight: Boolean,
    selectedAiMode: AiAdvisorMode,
    onRefreshInsight: () -> Unit,
    onOpenAiAdvisor: () -> Unit,
    onQuickAddTransaction: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hideSensitive = LocalHideSensitiveAmounts.current
    var showHeroBreakdown by remember { mutableStateOf(false) }

    val monthsList = remember {
        listOf(
            "August 2026",
            "September 2026",
            "October 2026",
            "November 2026",
            "December 2026"
        )
    }
    var selectedMonthIndex by remember { mutableIntStateOf(2) } // Defaults to "October 2026"

    val displayNetSpendMinor = if (state.totalIncomeMinor == 0L && state.totalExpenseMinor == 0L) {
        1_245_000L // ₹12,450 default showcase telemetry when empty
    } else {
        state.availableToSpendMinor
    }

    val monthlyBars = remember(state.transactions) {
        listOf(
            MonthlyBarSample("May", 0.42f, 0.54f),
            MonthlyBarSample("Jun", 0.56f, 0.68f),
            MonthlyBarSample("Jul", 0.66f, 0.78f),
            MonthlyBarSample("Aug", 0.80f, 0.90f),
            MonthlyBarSample("Sep", 0.54f, 0.72f),
            MonthlyBarSample("Oct", 0.40f, 0.52f)
        )
    }

    val sparklineWavePath = remember { Path() }
    val sparklineStrokePath = remember { Path() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ShowcaseDeepBackground)
            .testTag("overview_dashboard_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Top Header Bar (Fluxora Logo + Notification Bell with Red Unread Dot + User Profile Button)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FluxoraBrandHeader(
                    darkSurface = true,
                    showIcon = true,
                    iconSize = 36.dp,
                    titleFontSize = 22.sp,
                    subtitleFontSize = 7.5.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Notification Bell Circular Dark Glass Button with Red Dot
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ShowcaseSurfaceDark)
                            .border(1.dp, ShowcaseSurfaceElevation, CircleShape)
                            .clickable { onOpenAiAdvisor() }
                            .testTag("dashboard_notifications_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = ShowcaseTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 7.dp, end = 8.dp)
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(ShowcaseNotificationDot)
                        )
                    }

                    // User Profile Circular Dark Glass Button with Emerald Rim
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ShowcaseSurfaceDark)
                            .border(1.2.dp, ShowcasePrimaryEmerald.copy(alpha = 0.65f), CircleShape)
                            .clickable { onNavigateToSettings() }
                            .testTag("dashboard_profile_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Profile",
                            tint = ShowcasePrimaryEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 1b. Search Bar + Greeting (Matching Design System Reference)
        item {
            Surface(
                shape = RoundedCornerShape(50),
                color = FluxoraDeepOcean.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, FluxoraMoonlightBlue.copy(alpha = 0.20f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = FluxoraMoonlightBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = t("search_placeholder"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = FluxoraMoonlightBlue.copy(alpha = 0.70f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = t("good_morning"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = t("stay_focused"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = FluxoraMoonlightBlue
                )
            }
        }

        // 1c. Productivity Flow Hero Banner
        item {
            ProductivityHeroBanner(
                onStartFocusSession = onOpenAiAdvisor
            )
        }

        // 1d. 4 Metric Cards: Tasks Today, Focus Time, Goals Progress, Streak
        item {
            ProductivityMetricCardsRow()
        }

        // 2. Hero Card 1 — Light Mint/White "Net Spend" Card (`available_to_spend_card`)
        item {
            Card(
                onClick = { showHeroBreakdown = !showHeroBreakdown },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = ShowcaseNetSpendCardStart),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("available_to_spend_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(ShowcaseNetSpendCardStart, ShowcaseNetSpendCardEnd)
                            )
                        )
                ) {
                    // Soft Mint Area Sparkline Wave on the Right Half
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(104.dp)
                    ) {
                        val w = size.width
                        val h = size.height

                        sparklineWavePath.reset()
                        sparklineWavePath.moveTo(w * 0.52f, h)
                        sparklineWavePath.lineTo(w * 0.56f, h * 0.80f)
                        sparklineWavePath.cubicTo(
                            w * 0.64f, h * 0.68f,
                            w * 0.70f, h * 0.84f,
                            w * 0.76f, h * 0.62f
                        )
                        sparklineWavePath.cubicTo(
                            w * 0.82f, h * 0.46f,
                            w * 0.88f, h * 0.70f,
                            w * 0.94f, h * 0.54f
                        )
                        sparklineWavePath.lineTo(w, h * 0.64f)
                        sparklineWavePath.lineTo(w, h)
                        sparklineWavePath.close()

                        drawPath(
                            path = sparklineWavePath,
                            color = ShowcaseNetSpendWaveFill
                        )

                        sparklineStrokePath.reset()
                        sparklineStrokePath.moveTo(w * 0.52f, h * 0.92f)
                        sparklineStrokePath.lineTo(w * 0.56f, h * 0.80f)
                        sparklineStrokePath.cubicTo(
                            w * 0.64f, h * 0.68f,
                            w * 0.70f, h * 0.84f,
                            w * 0.76f, h * 0.62f
                        )
                        sparklineStrokePath.cubicTo(
                            w * 0.82f, h * 0.46f,
                            w * 0.88f, h * 0.70f,
                            w * 0.94f, h * 0.54f
                        )
                        sparklineStrokePath.lineTo(w, h * 0.64f)

                        drawPath(
                            path = sparklineStrokePath,
                            color = ShowcaseNetSpendWaveStroke,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Circular Mint Icon Badge with 4 Vertical Emerald Bar Columns
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(ShowcaseNetSpendIconBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.size(22.dp)) {
                                        val barW = size.width * 0.16f
                                        val gap = size.width * 0.10f
                                        val heights = floatArrayOf(0.50f, 0.88f, 0.68f, 0.42f)
                                        for (i in heights.indices) {
                                            val bh = size.height * heights[i]
                                            val bx = i * (barW + gap)
                                            val by = size.height - bh
                                            drawRoundRect(
                                                color = ShowcasePrimaryEmerald,
                                                topLeft = Offset(bx, by),
                                                size = Size(barW, bh),
                                                cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                                            )
                                        }
                                    }
                                }

                                Column {
                                    Text(
                                        text = "Net Spend",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = ShowcaseLoginSubtitleSlate
                                    )
                                    Text(
                                        text = formatShowcaseRupee(
                                            amountMinor = displayNetSpendMinor,
                                            hideSensitive = hideSensitive
                                        ),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ShowcaseLoginHeadingInk
                                    )
                                }
                            }

                            // Right Pill Badge: "↗ 12%"
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = ShowcaseTrendBadgeBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = "Trend Up 12%",
                                        tint = ShowcaseTrendBadgeText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "12%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ShowcaseTrendBadgeText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Safe-to-Spend Today Row (`safe_to_spend_today_badge`)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("safe_to_spend_today_badge"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Safe to spend today",
                                style = MaterialTheme.typography.labelSmall,
                                color = ShowcaseLoginSubtitleSlate
                            )
                            val safeTodayMinor = state.safeToSpendTodayMinor
                            Text(
                                text = if (safeTodayMinor != null) {
                                    formatShowcaseRupee(safeTodayMinor, hideSensitive = hideSensitive)
                                } else {
                                    "— (set a budget to calculate)"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = ShowcaseTrendBadgeText
                            )
                        }

                        if (showHeroBreakdown) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Income (${formatOrMaskAmount(state.totalIncomeMinor)}) - Expenses (${formatOrMaskAmount(state.totalExpenseMinor)}) - Saved in Goals (${formatOrMaskAmount(state.totalSavingsMinor)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = ShowcaseLoginSubtitleSlate
                            )
                        }
                    }
                }
            }
        }

        // 3. Dark Glass Month Selector Pill Bar (`<  📅 October 2026  >`)
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("dashboard_month_selector_pill"),
                shape = RoundedCornerShape(50),
                color = ShowcaseSurfaceDark,
                border = BorderStroke(1.dp, ShowcaseSurfaceElevation)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            selectedMonthIndex = (selectedMonthIndex - 1 + monthsList.size) % monthsList.size
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Month",
                            tint = ShowcaseTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Selected Month",
                            tint = ShowcasePrimaryEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = monthsList[selectedMonthIndex],
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ShowcaseTextPrimary
                        )
                    }

                    IconButton(
                        onClick = {
                            selectedMonthIndex = (selectedMonthIndex + 1) % monthsList.size
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Month",
                            tint = ShowcaseTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 4. Hero Card 2 — "Cash-Flow Reactor" (`FinancialHero3DScene`)
        item {
            FinancialHero3DScene(
                totalIncomeMinor = state.totalIncomeMinor,
                totalExpenseMinor = state.totalExpenseMinor
            )
        }

        // 5. Card 3 — "Monthly Cash Flow" Frosted Light 6-Bar Chart Card (May, Jun, Jul, Aug, Sep, Oct)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = ShowcaseMonthlyCardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("monthly_cash_flow_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Monthly Cash Flow",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ShowcaseLoginHeadingInk
                        )

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = ShowcaseBarTrackBg,
                            modifier = Modifier.clickable { onQuickAddTransaction() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "This Month",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = ShowcaseLoginHeadingInk
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select Period",
                                    tint = ShowcaseLoginHeadingInk,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 6 Vertical Pill-Track Bar Columns: May, Jun, Jul, Aug, Sep, Oct
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        monthlyBars.forEach { bar ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(12.dp)
                                        .height(68.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(ShowcaseBarTrackBg),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    // Soft upper cap
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(bar.capFillFraction)
                                            .clip(RoundedCornerShape(50))
                                            .background(ShowcaseBarFillBottom.copy(alpha = 0.45f))
                                    )
                                    // Solid emerald-to-mint vertical bar fill
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(bar.primaryFillFraction)
                                            .clip(RoundedCornerShape(50))
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        ShowcaseBarFillTop,
                                                        ShowcaseBarFillBottom
                                                    )
                                                )
                                            )
                                    )
                                }

                                Text(
                                    text = bar.monthLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ShowcaseLoginSubtitleSlate
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Recharts-style Financial Summary Smooth Spline Line Chart
        item {
            RechartsFinancialSummaryChart(
                state = state,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 7. Daily AI Saving Insight Card (`daily_ai_insight_card`)
        if (state.isAiAdvisorEnabled) {
            item {
                DailyAiSavingInsightCard(
                    insightText = dailyInsight,
                    isGenerating = isGeneratingInsight,
                    selectedMode = selectedAiMode,
                    onRefreshInsight = onRefreshInsight,
                    onOpenAdvisorChat = onOpenAiAdvisor
                )
            }
        }

        // Productivity Tasks Checklist Card (Matching Design System Reference)
        item {
            val sampleTasks = remember {
                listOf(
                    ProductivityTask("t1", "Complete DSA revision", ProductivityTaskTag.STUDY, true, "Today"),
                    ProductivityTask("t2", "Finish Fluxora UI", ProductivityTaskTag.BUILD, false, "Today"),
                    ProductivityTask("t3", "Update project README", ProductivityTaskTag.DOCS, false, "Today"),
                    ProductivityTask("t4", "30 min workout", ProductivityTaskTag.HEALTH, false, "Today")
                )
            }
            ProductivityTodayTasksCard(
                tasks = sampleTasks,
                onToggleTask = {},
                onAddTaskClick = onQuickAddTransaction
            )
        }

        // Productivity Weekly Focus Bar Chart Card (Mon - Sun)
        item {
            ProductivityWeeklyFocusCard()
        }

        // Productivity Overall Progress 72% Donut Radial Ring Card
        item {
            ProductivityOverallProgressCard()
        }

        // 7. Top Category Spending Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ShowcaseSurfaceDark
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Top Category Spending",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ShowcaseTextPrimary
                        )
                        TextButton(
                            onClick = onQuickAddTransaction,
                            modifier = Modifier.testTag("dashboard_add_tx_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Log Transaction",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Log Flow")
                        }
                    }

                    val categoryTotals = state.transactions
                        .filter { it.type == TransactionType.EXPENSE.name }
                        .groupBy { it.categoryName }
                        .mapValues { (_, list) -> list.sumOf { it.amountMinor } }
                        .entries
                        .sortedByDescending { it.value }
                        .take(5)

                    if (categoryTotals.isEmpty()) {
                        Text(
                            text = "No expense flows recorded yet. Tap '+' or 'Log Flow' to add your first entry.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ShowcaseTextSecondary
                        )
                    } else {
                        val maxVal = categoryTotals.first().value.coerceAtLeast(1L)
                        categoryTotals.forEach { entry ->
                            val catName = entry.key
                            val amountMinor = entry.value
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = catName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = ShowcaseTextPrimary
                                )
                                Text(
                                    text = formatOrMaskAmount(amountMinor),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ShowcaseTextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (amountMinor.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = ShowcasePrimaryEmerald
                            )
                        }
                    }
                }
            }
        }

        // 8. Recent Cash-Flow Telemetry
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Cash-Flow Telemetry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ShowcaseTextPrimary
                )
                if (state.transactions.size > 5) {
                    TextButton(
                        onClick = onQuickAddTransaction,
                        modifier = Modifier.testTag("see_all_transactions_button")
                    ) {
                        Text("See all transactions")
                    }
                }
            }
        }

        val recentTransactions = state.transactions.take(5)
        if (recentTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ShowcaseSurfaceDark
                    )
                ) {
                    Text(
                        text = "Your recent income and expense ledger entries will appear here in real time.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShowcaseTextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(recentTransactions, key = { it.id }) { tx ->
                TransactionRowItem(
                    tx = tx,
                    onDelete = null
                )
            }
        }
    }
}

@Composable
fun TransactionRowItem(
    tx: FinancialTransaction,
    onDelete: (() -> Unit)?
) {
    val extendedColors = LocalFluxoraExtendedColors.current
    val isIncome = tx.type == TransactionType.INCOME.name
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_item_${tx.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isIncome) extendedColors.incomeRingColor.copy(alpha = 0.16f)
                            else extendedColors.expenseRingColor.copy(alpha = 0.16f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = tx.type,
                        tint = if (isIncome) extendedColors.incomeRingColor else extendedColors.expenseRingColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = tx.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${tx.categoryName} • ${dateFormat.format(Date(tx.timestampMillis))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isIncome) {
                        formatOrMaskAmount(tx.amountMinor, showPlusForPositive = true)
                    } else {
                        formatOrMaskAmount(-tx.amountMinor)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) extendedColors.incomeRingColor else extendedColors.expenseRingColor
                )
                if (onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_tx_${tx.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Transaction",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LedgerTransactionsScreen(
    state: FinanceDashboardState,
    onAddTransaction: (
        title: String,
        amountInrInput: String,
        type: TransactionType,
        category: FinancialCategory,
        timestampMillis: Long,
        notes: String?
    ) -> Unit,
    onDeleteTransaction: (String) -> Unit,
    onAddCustomCategory: (name: String) -> Unit,
    onExportCsv: () -> String,
    onImportCsv: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var smartQuickAddText by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showCsvDialog by remember { mutableStateOf(false) }
    var csvContentBuffer by remember { mutableStateOf("") }

    val allCategories: List<FinancialCategory> = remember(state.categories, state.userId) {
        state.categories.ifEmpty { DefaultCategories.toFinancialCategories(state.userId) }
    }

    val filteredTransactions = remember(state.transactions, searchQuery, selectedTypeFilter) {
        state.transactions.filter { tx ->
            val matchesType = selectedTypeFilter == "ALL" || tx.type == selectedTypeFilter
            val matchesQuery = searchQuery.isBlank() ||
                tx.title.contains(searchQuery, ignoreCase = true) ||
                tx.categoryName.contains(searchQuery, ignoreCase = true) ||
                (tx.notes?.contains(searchQuery, ignoreCase = true) == true)
            matchesType && matchesQuery
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("ledger_transactions_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cash-Flow Ledger",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            csvContentBuffer = onExportCsv()
                            showCsvDialog = true
                        },
                        modifier = Modifier.testTag("open_csv_portability_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "CSV Data Portability",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CSV")
                    }
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("open_add_transaction_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Transaction",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Entry")
                    }
                }
            }
        }

        // Smart Quick-Add Bar (English + Hindi + Hinglish: e.g., "chai 40", "kirana 850 yesterday")
        item {
            val parsedPreview = remember(smartQuickAddText) {
                SmartQuickAddParser.parse(smartQuickAddText)
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = smartQuickAddText,
                            onValueChange = { smartQuickAddText = it },
                            placeholder = { Text("Quick add: e.g. 'chai 40' or 'kirana 850 yesterday'") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("smart_quick_add_input")
                        )
                        Button(
                            onClick = {
                                val parsed = SmartQuickAddParser.parse(smartQuickAddText)
                                if (parsed != null) {
                                    val category = allCategories.find {
                                        it.name.equals(parsed.categoryName, ignoreCase = true)
                                    } ?: allCategories.first()
                                    val majorStr = String.format(Locale.US, "%.2f", parsed.amountMinor / 100.0)
                                    onAddTransaction(
                                        parsed.title,
                                        majorStr,
                                        parsed.type,
                                        category,
                                        parsed.timestampMillis,
                                        "Smart Quick-Add"
                                    )
                                    smartQuickAddText = ""
                                }
                            },
                            enabled = parsedPreview != null,
                            modifier = Modifier.testTag("smart_quick_add_save_button")
                        ) {
                            Text("Save")
                        }
                    }
                    if (parsedPreview != null) {
                        Text(
                            text = "Detected: ${parsedPreview.title} • ${formatOrMaskAmount(parsedPreview.amountMinor)} • ${parsedPreview.categoryName} (${parsedPreview.type.name})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search title, category, or notes...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Ledger"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ledger_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("ALL", TransactionType.EXPENSE.name, TransactionType.INCOME.name).forEach { filter ->
                    FilterChip(
                        selected = selectedTypeFilter == filter,
                        onClick = { selectedTypeFilter = filter },
                        label = { Text(filter) },
                        modifier = Modifier.testTag("ledger_filter_$filter")
                    )
                }
                AssistChip(
                    onClick = { showCategoryDialog = true },
                    label = { Text("+ Custom Category") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Create Custom Category",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("add_custom_category_chip")
                )
            }
        }

        if (showCsvDialog) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("csv_portability_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "CSV Data Portability (Export / Import)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Copy your exported transactions below, or paste CSV rows to import into your ledger:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        OutlinedTextField(
                            value = csvContentBuffer,
                            onValueChange = { csvContentBuffer = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("csv_portability_text_field"),
                            minLines = 3,
                            maxLines = 6,
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showCsvDialog = false }) {
                                Text("Close")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    onImportCsv(csvContentBuffer)
                                    showCsvDialog = false
                                },
                                modifier = Modifier.testTag("import_csv_confirm_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileUpload,
                                    contentDescription = "Import CSV",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Import CSV")
                            }
                        }
                    }
                }
            }
        }

        if (filteredTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "No matching ledger entries found.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(filteredTransactions, key = { it.id }) { tx ->
                TransactionRowItem(
                    tx = tx,
                    onDelete = { onDeleteTransaction(tx.id) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            categories = allCategories,
            onDismiss = { showAddDialog = false },
            onSave = { title, amountMajor, type, category, notes ->
                onAddTransaction(
                    title,
                    amountMajor,
                    type,
                    category,
                    System.currentTimeMillis(),
                    notes.takeIf { it.isNotBlank() }
                )
                showAddDialog = false
            }
        )
    }

    if (showCategoryDialog) {
        AddCustomCategoryDialog(
            onDismiss = { showCategoryDialog = false },
            onSave = { name ->
                onAddCustomCategory(name)
                showCategoryDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddTransactionDialog(
    categories: List<FinancialCategory>,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        amountMajor: String,
        type: TransactionType,
        category: FinancialCategory,
        notes: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountMajor by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCat by remember(categories) {
        mutableStateOf(categories.firstOrNull() ?: DefaultCategories.toFinancialCategories("").first())
    }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Financial Flow") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionType.entries.forEach { option ->
                        FilterChip(
                            selected = type == option,
                            onClick = { type = option },
                            label = { Text(option.name) },
                            modifier = Modifier.testTag("tx_type_chip_${option.name}")
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g., Grocery Run)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tx_title_input")
                )
                OutlinedTextField(
                    value = amountMajor,
                    onValueChange = { amountMajor = it },
                    label = { Text("Amount in INR (e.g., 1250.50)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tx_amount_input")
                )
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.take(8).forEach { cat ->
                        FilterChip(
                            selected = selectedCat.id == cat.id,
                            onClick = { selectedCat = cat },
                            label = { Text(cat.name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(title, amountMajor, type, selectedCat, notes)
                },
                enabled = title.isNotBlank() && amountMajor.isNotBlank(),
                modifier = Modifier.testTag("save_transaction_button")
            ) {
                Text("Save Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AddCustomCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (name: String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Custom Category") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Category Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_category_name_input")
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(name) },
                enabled = name.trim().length >= 2,
                modifier = Modifier.testTag("save_custom_category_button")
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlanningScreen(
    state: FinanceDashboardState,
    onSaveBudget: (category: FinancialCategory, limitInrInput: String) -> Unit,
    onSaveSavingsGoal: (title: String, targetMajor: String, savedMajor: String) -> Unit,
    onContributeGoal: (goal: SavingsGoal, addMajor: String) -> Unit,
    onSaveRecurringBill: (title: String, amountMajor: String, category: FinancialCategory, frequency: BillFrequency) -> Unit,
    onMarkBillPaid: (RecurringBill) -> Unit,
    onSetDebtStrategy: (DebtPayoffStrategy) -> Unit = {},
    onSetExtraMonthlyPayment: (Long) -> Unit = {},
    onSaveDebt: (id: String, name: String, balanceMajor: String, interestRate: String, minPayMajor: String) -> Unit = { _, _, _, _, _ -> },
    onDeleteDebt: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showBillDialog by remember { mutableStateOf(false) }
    var showDebtDialog by remember { mutableStateOf(false) }
    var editingDebtTarget by remember { mutableStateOf<DebtItem?>(null) }
    var contributeGoalTarget by remember { mutableStateOf<SavingsGoal?>(null) }

    val extendedColors = LocalFluxoraExtendedColors.current
    val allCategories = remember(state.categories, state.userId) {
        state.categories.ifEmpty { DefaultCategories.toFinancialCategories(state.userId) }
    }

    val spendByCategory = remember(state.transactions) {
        state.transactions
            .filter { it.type == TransactionType.EXPENSE.name }
            .groupBy { it.categoryId }
            .mapValues { (_, list) -> list.sumOf { it.amountMinor } }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("planning_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Monthly Category Budgets & Spending Limits
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = t("monthly_category_budgets"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showBudgetDialog = true },
                    modifier = Modifier.testTag("add_budget_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Budget Goal", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(t("set_budget_goal"))
                }
            }
        }

        if (state.budgets.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Text(
                        text = "No budget goals set yet. Set spending limits per category to track progress and receive real-time alert notifications.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(state.budgets, key = { it.id }) { budget ->
                val spentMinor = spendByCategory[budget.categoryId] ?: 0L
                val ratio = if (budget.limitMinor > 0L) {
                    (spentMinor.toFloat() / budget.limitMinor.toFloat()).coerceIn(0f, 1.2f)
                } else 0f
                val isOver100 = spentMinor >= budget.limitMinor && budget.limitMinor > 0L
                val isOver80 = ratio >= 0.8f
                val barColor = when {
                    isOver100 -> extendedColors.dangerColor
                    isOver80 -> extendedColors.warningColor
                    else -> extendedColors.incomeRingColor
                }
                val statusLabel = when {
                    isOver100 -> t("exceeded_limit")
                    isOver80 -> t("near_limit")
                    else -> t("on_track")
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = budget.categoryName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = barColor.copy(alpha = 0.16f),
                                    border = BorderStroke(1.dp, barColor.copy(alpha = 0.40f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        if (isOver100 || isOver80) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = statusLabel,
                                                tint = barColor,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = statusLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = barColor
                                        )
                                    }
                                }
                            }
                            Text(
                                text = budget.monthKey,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${formatOrMaskAmount(spentMinor)} / ${formatOrMaskAmount(budget.limitMinor)} (${(ratio * 100).toInt()}%)",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.SemiBold
                            )
                            val deltaText = if (spentMinor > budget.limitMinor) {
                                "${formatOrMaskAmount(spentMinor - budget.limitMinor)} ${t("over_limit")}"
                            } else {
                                "${formatOrMaskAmount(budget.limitMinor - spentMinor)} ${t("remaining")}"
                            }
                            Text(
                                text = deltaText,
                                style = MaterialTheme.typography.labelSmall,
                                color = barColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // Progress Bar Component Visualizing Progress Against Category Limit
                        LinearProgressIndicator(
                            progress = { ratio.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = barColor
                        )
                    }
                }
            }
        }

        // Section 2: Savings Goals with 25% / 50% / 75% / 100% Milestone Badges
        item {
            HorizontalDivider()
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Savings Goals & Milestones",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showGoalDialog = true },
                    modifier = Modifier.testTag("add_savings_goal_button")
                ) {
                    Icon(Icons.Default.Savings, contentDescription = "New Savings Goal", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Goal")
                }
            }
        }

        if (state.savingsGoals.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Text(
                        text = "Create a Savings Goal to track 25%, 50%, 75%, and 100% milestone achievements.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(state.savingsGoals, key = { it.id }) { goal ->
                val pct = goal.progressPercentage
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = goal.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = { contributeGoalTarget = goal },
                                modifier = Modifier.testTag("contribute_goal_${goal.id}")
                            ) {
                                Text("+ Contribute")
                            }
                        }
                        Text(
                            text = "${formatOrMaskAmount(goal.savedMinor)} / ${formatOrMaskAmount(goal.targetMinor)} ($pct%)",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { goal.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = extendedColors.incomeRingColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(25, 50, 75, 100).forEach { milestone ->
                                val unlocked = pct >= milestone
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (unlocked) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "$milestone% Milestone",
                                            tint = if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "$milestone%",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (unlocked) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Recurring Bills with Atomic 10s Idempotency Guard
        item {
            HorizontalDivider()
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recurring Bills",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showBillDialog = true },
                    modifier = Modifier.testTag("add_recurring_bill_button")
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = "Add Bill", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Bill")
                }
            }
        }

        if (state.recurringBills.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Text(
                        text = "No recurring bills scheduled. Add weekly, monthly, or yearly bills with atomic one-tap settlement.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(state.recurringBills, key = { it.id }) { bill ->
                val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bill.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${bill.frequency} • Next due: ${dateFormat.format(Date(bill.nextDueDateMillis))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatOrMaskAmount(bill.amountMinor),
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        OutlinedButton(
                            onClick = { onMarkBillPaid(bill) },
                            modifier = Modifier.testTag("pay_bill_button_${bill.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Mark Bill Paid",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pay Cycle")
                        }
                    }
                }
            }
        }

        // Section 4: Debt Payoff Calculator (Snowball & Avalanche)
        item {
            HorizontalDivider()
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = t("debt_calculator_title"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = t("debt_payoff_strategy"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = {
                        editingDebtTarget = null
                        showDebtDialog = true
                    },
                    modifier = Modifier.testTag("add_debt_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = t("add_debt"), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(t("add_debt"))
                }
            }
        }

        // Strategy Selector Chips & Description
        item {
            val simulation = state.debtPayoffSimulation
            Card(
                modifier = Modifier.fillMaxWidth().testTag("debt_calculator_section"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = state.debtStrategy == DebtPayoffStrategy.SNOWBALL,
                            onClick = { onSetDebtStrategy(DebtPayoffStrategy.SNOWBALL) },
                            label = { Text(t("snowball_strategy"), fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.testTag("strategy_snowball_chip")
                        )
                        FilterChip(
                            selected = state.debtStrategy == DebtPayoffStrategy.AVALANCHE,
                            onClick = { onSetDebtStrategy(DebtPayoffStrategy.AVALANCHE) },
                            label = { Text(t("avalanche_strategy"), fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.testTag("strategy_avalanche_chip")
                        )
                    }

                    Text(
                        text = if (state.debtStrategy == DebtPayoffStrategy.SNOWBALL) t("snowball_desc") else t("avalanche_desc"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Extra Monthly Payment Adjustment
                    var extraInput by remember(state.extraMonthlyPaymentMinor) {
                        mutableStateOf(if (state.extraMonthlyPaymentMinor > 0L) (state.extraMonthlyPaymentMinor / 100L).toString() else "")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = t("extra_monthly_payment"),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Accelerate payoff with extra contribution",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedTextField(
                            value = extraInput,
                            onValueChange = { input ->
                                extraInput = input
                                val minor = FinancialEngine.parseInrMajorToMinor(input) ?: 0L
                                onSetExtraMonthlyPayment(minor)
                            },
                            label = { Text("INR/mo") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.width(130.dp).testTag("extra_payment_input")
                        )
                    }

                    // Simulation KPI Summary Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = t("total_debt"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatOrMaskAmount(simulation.totalDebtMinor),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = extendedColors.dangerColor
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = t("time_to_debt_free"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${simulation.monthsToPayoff} ${t("months")}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = t("total_interest"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatOrMaskAmount(simulation.totalInterestPaidMinor),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = extendedColors.warningColor
                                )
                            }
                        }
                    }

                    // Strategic Comparison Notice
                    if (simulation.comparisonInterestSavedMinor > 0L) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = extendedColors.incomeRingColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, extendedColors.incomeRingColor.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = extendedColors.incomeRingColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = String.format(
                                        t("avalanche_savings_notice"),
                                        formatOrMaskAmount(simulation.comparisonInterestSavedMinor)
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = extendedColors.incomeRingColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Debt Sequence List
        if (state.debts.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Text(
                        text = t("no_debts"),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(state.debtPayoffSimulation.steps, key = { it.debtId }) { step ->
                val debt = state.debts.firstOrNull { it.id == step.debtId }
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("debt_item_card_${step.debtId}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = "#${step.payoffMonthOrder} Target",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Text(
                                    text = step.debtName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        editingDebtTarget = debt
                                        showDebtDialog = true
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Debt", modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { onDeleteDebt(step.debtId) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Debt", tint = extendedColors.dangerColor, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Balance: ${formatOrMaskAmount(step.startingBalanceMinor)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "APR: ${step.interestRatePercent}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Paid Off: Month ${step.monthsToPayoffThisDebt}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Interest: ${formatOrMaskAmount(step.interestPaidMinor)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = extendedColors.warningColor
                                )
                            }
                        }

                        val maxMonths = state.debtPayoffSimulation.monthsToPayoff.coerceAtLeast(1)
                        val progressRatio = (step.monthsToPayoffThisDebt.toFloat() / maxMonths.toFloat()).coerceIn(0.1f, 1f)
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showBudgetDialog) {
        var selectedCategory by remember { mutableStateOf(allCategories.first()) }
        var limitMajor by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text(t("set_budget_goal"), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = t("select_category"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allCategories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory.id == cat.id,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.name) },
                                modifier = Modifier.testTag("budget_category_chip_${cat.id}")
                            )
                        }
                    }
                    OutlinedTextField(
                        value = limitMajor,
                        onValueChange = { limitMajor = it },
                        label = { Text(t("monthly_limit_inr")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("budget_limit_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveBudget(selectedCategory, limitMajor)
                        showBudgetDialog = false
                    },
                    enabled = limitMajor.isNotBlank(),
                    modifier = Modifier.testTag("save_budget_confirm_button")
                ) {
                    Text(t("save_budget"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) { Text(t("cancel")) }
            }
        )
    }

    if (showGoalDialog) {
        var title by remember { mutableStateOf("") }
        var targetMajor by remember { mutableStateOf("") }
        var savedMajor by remember { mutableStateOf("0") }
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Create Savings Goal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Goal Name (e.g., Emergency Fund)") },
                        singleLine = true,
                        modifier = Modifier.testTag("goal_title_input")
                    )
                    OutlinedTextField(
                        value = targetMajor,
                        onValueChange = { targetMajor = it },
                        label = { Text("Target Amount in INR") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.testTag("goal_target_input")
                    )
                    OutlinedTextField(
                        value = savedMajor,
                        onValueChange = { savedMajor = it },
                        label = { Text("Initial Saved Amount in INR") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveSavingsGoal(title, targetMajor, savedMajor)
                        showGoalDialog = false
                    },
                    enabled = title.isNotBlank() && targetMajor.isNotBlank(),
                    modifier = Modifier.testTag("save_goal_confirm_button")
                ) {
                    Text("Create Goal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) { Text("Cancel") }
            }
        )
    }

    contributeGoalTarget?.let { targetGoal ->
        var addAmount by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { contributeGoalTarget = null },
            title = { Text("Contribute to ${targetGoal.title}") },
            text = {
                OutlinedTextField(
                    value = addAmount,
                    onValueChange = { addAmount = it },
                    label = { Text("Additional INR Saved") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onContributeGoal(targetGoal, addAmount)
                        contributeGoalTarget = null
                    },
                    enabled = addAmount.isNotBlank()
                ) {
                    Text("Add Contribution")
                }
            },
            dismissButton = {
                TextButton(onClick = { contributeGoalTarget = null }) { Text("Cancel") }
            }
        )
    }

    if (showBillDialog) {
        var title by remember { mutableStateOf("") }
        var amountMajor by remember { mutableStateOf("") }
        var frequency by remember { mutableStateOf(BillFrequency.MONTHLY) }
        val defaultBillCategory = remember(allCategories) {
            allCategories.firstOrNull { it.id == "bills_utilities" } ?: allCategories.first()
        }
        AlertDialog(
            onDismissRequest = { showBillDialog = false },
            title = { Text("Schedule Recurring Bill") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Bill Name (e.g., Fiber Internet)") },
                        singleLine = true,
                        modifier = Modifier.testTag("bill_title_input")
                    )
                    OutlinedTextField(
                        value = amountMajor,
                        onValueChange = { amountMajor = it },
                        label = { Text("Bill Amount in INR") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.testTag("bill_amount_input")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        BillFrequency.entries.forEach { freq ->
                            FilterChip(
                                selected = frequency == freq,
                                onClick = { frequency = freq },
                                label = { Text(freq.label) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveRecurringBill(title, amountMajor, defaultBillCategory, frequency)
                        showBillDialog = false
                    },
                    enabled = title.isNotBlank() && amountMajor.isNotBlank(),
                    modifier = Modifier.testTag("save_bill_confirm_button")
                ) {
                    Text("Schedule Bill")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBillDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDebtDialog) {
        var name by remember { mutableStateOf(editingDebtTarget?.name ?: "") }
        var balanceMajor by remember { mutableStateOf(if (editingDebtTarget != null) (editingDebtTarget!!.balanceMinor / 100L).toString() else "") }
        var interestRate by remember { mutableStateOf(if (editingDebtTarget != null) editingDebtTarget!!.interestRatePercent.toString() else "") }
        var minPaymentMajor by remember { mutableStateOf(if (editingDebtTarget != null) (editingDebtTarget!!.minimumPaymentMinor / 100L).toString() else "") }

        AlertDialog(
            onDismissRequest = { showDebtDialog = false },
            title = { Text(if (editingDebtTarget == null) t("add_debt") else t("edit_debt"), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(t("debt_name")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("debt_name_input")
                    )
                    OutlinedTextField(
                        value = balanceMajor,
                        onValueChange = { balanceMajor = it },
                        label = { Text(t("balance_inr")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("debt_balance_input")
                    )
                    OutlinedTextField(
                        value = interestRate,
                        onValueChange = { interestRate = it },
                        label = { Text(t("interest_rate")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("debt_rate_input")
                    )
                    OutlinedTextField(
                        value = minPaymentMajor,
                        onValueChange = { minPaymentMajor = it },
                        label = { Text(t("min_payment_inr")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("debt_min_pay_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveDebt(
                            editingDebtTarget?.id ?: "",
                            name,
                            balanceMajor,
                            interestRate,
                            minPaymentMajor
                        )
                        showDebtDialog = false
                    },
                    enabled = name.isNotBlank() && balanceMajor.isNotBlank(),
                    modifier = Modifier.testTag("save_debt_confirm_button")
                ) {
                    Text(t("save_debt"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDebtDialog = false }) { Text(t("cancel")) }
            }
        )
    }
}
