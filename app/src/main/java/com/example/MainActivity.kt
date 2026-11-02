package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.testTag
import com.example.i18n.LocalLanguageCode
import com.example.i18n.t
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai.GeminiChatDialog
import com.example.ai.GeminiChatViewModel
import com.example.authentication.AuthManager
import com.example.data.FinanceRepository
import com.example.data.PreferencesManager
import com.example.finance.FinanceViewModel
import com.example.logging.FluxoraLogger
import com.example.notifications.NotificationCategory
import com.example.notifications.SystemNotificationHelper
import com.example.security.AppCheckConfiguration
import com.example.security.AppLockMonitor
import com.example.security.BiometricAuthManager
import com.example.security.BiometricAuthScreen
import com.example.ui.components.CinematicDesignSystemShowcaseScreen
import com.example.ui.components.DynamicIsland
import com.example.ui.components.FluxoraErrorBoundary
import com.example.ui.glass.GlassEffectPreference
import com.example.ui.screens.FluxoraAuthScreen
import com.example.ui.screens.FluxoraTasksScreen
import com.example.ui.screens.FluxoraProjectsScreen
import com.example.ui.screens.FluxoraCalendarScreen
import com.example.ui.screens.FluxoraAnalyticsScreen
import com.example.ui.screens.LedgerTransactionsScreen
import com.example.ui.screens.OverviewDashboardScreen
import com.example.ui.screens.PlanningScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.RoommateSplitScreen
import com.example.ui.screens.SettingsAndGovernanceScreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.CinematicThemeMode
import com.example.ui.theme.FluxoraAuroraMint
import com.example.ui.theme.FluxoraCosmicViolet
import com.example.ui.theme.FluxoraDeepOcean
import com.example.ui.theme.FluxoraDreamLavender
import com.example.ui.theme.FluxoraMidnightAbyss
import com.example.ui.theme.FluxoraMoonlightBlue
import com.example.ui.theme.FluxoraTheme
import com.example.ui.theme.ShowcaseDeepBackground
import com.example.ui.theme.ShowcaseFabBg
import com.example.ui.theme.ShowcaseFabGlow
import com.example.ui.theme.ShowcasePrimaryEmerald
import com.example.ui.theme.ShowcasePureWhite
import com.example.ui.theme.ShowcaseSurfaceDark
import com.example.ui.theme.ShowcaseSurfaceElevation
import com.example.ui.theme.ShowcaseTextSecondary
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.TrendingUp

enum class FluxoraTab(
    val route: String,
    val translationKey: String,
    val defaultLabel: String,
    val icon: ImageVector
) {
    OVERVIEW("overview", "dashboard", "Dashboard", Icons.Default.Dashboard),
    TASKS("tasks", "tasks", "Tasks", Icons.Default.Checklist),
    PROJECTS("projects", "projects", "Projects", Icons.Default.Folder),
    CALENDAR("calendar", "calendar", "Calendar", Icons.Default.CalendarMonth),
    ANALYTICS("analytics", "analytics", "Analytics", Icons.Default.TrendingUp),
    LEDGER("ledger", "ledger", "Transactions", Icons.Default.ReceiptLong),
    PLANNING("planning", "planning", "Budgets", Icons.Default.PieChart),
    ROOMMATES("roommates", "roommates", "Roommates", Icons.Default.Groups),
    SETTINGS("settings", "more", "More", Icons.Default.MoreHoriz)
}

class MainActivity : FragmentActivity() {

    private val appLockMonitor = AppLockMonitor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Install Centralized Logging & Exception Reporting Service
        FluxoraLogger.installGlobalExceptionHandler()

        // Configure Firebase App Check prior to Firestore or AI Logic calls
        AppCheckConfiguration.initialize(this, intent)
        SystemNotificationHelper.ensureChannelsCreated(this)
        SystemNotificationHelper.schedulePeriodicBillChecks(this)

        enableEdgeToEdge()

        val preferencesManager = PreferencesManager(applicationContext)
        val repository = FinanceRepository(applicationContext)
        val biometricAuthManager = BiometricAuthManager(applicationContext)

        setContent {
            val geminiViewModel: GeminiChatViewModel = viewModel()

            FluxoraErrorBoundary {
                FluxoraRootContent(
                    activity = this,
                    repository = repository,
                    preferencesManager = preferencesManager,
                    biometricAuthManager = biometricAuthManager,
                    geminiViewModel = geminiViewModel,
                    appLockMonitor = appLockMonitor
                )
            }
        }
    }
}

@Composable
fun FluxoraRootContent(
    activity: FragmentActivity,
    repository: FinanceRepository,
    preferencesManager: PreferencesManager,
    biometricAuthManager: BiometricAuthManager,
    geminiViewModel: GeminiChatViewModel,
    appLockMonitor: AppLockMonitor
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val currentUser by AuthManager.observeAuthState().collectAsStateWithLifecycle(initialValue = null)
    var isAuthInProgress by remember { mutableStateOf(false) }
    var authErrorBanner by remember { mutableStateOf<String?>(null) }
    var isDemoModeActive by remember { mutableStateOf(false) }

    val selectedTheme by preferencesManager.selectedThemeFlow.collectAsStateWithLifecycle(
        initialValue = AppTheme.MIDNIGHT_EMERALD
    )
    val isDarkMode by preferencesManager.isDarkModeFlow.collectAsStateWithLifecycle(initialValue = true)
    val themeMode by preferencesManager.themeModeFlow.collectAsStateWithLifecycle(
        initialValue = CinematicThemeMode.DARK
    )
    val glassPreference by preferencesManager.glassEffectPreferenceFlow.collectAsStateWithLifecycle(
        initialValue = GlassEffectPreference.AUTO
    )
    val hideSensitiveAmounts by preferencesManager.hideSensitiveAmountsFlow.collectAsStateWithLifecycle(
        initialValue = false
    )
    val biometricEnabled by preferencesManager.isBiometricLockEnabledFlow.collectAsStateWithLifecycle(
        initialValue = false
    )
    val inactivityTimeoutMinutes by preferencesManager.inactivityTimeoutMinutesFlow.collectAsStateWithLifecycle(
        initialValue = 5
    )
    val onboardingCompleted by preferencesManager.onboardingCompletedFlow.collectAsStateWithLifecycle(
        initialValue = false
    )
    val masterNotificationsEnabled by preferencesManager.masterNotificationsEnabledFlow.collectAsStateWithLifecycle(
        initialValue = true
    )

    val budgetNotif by preferencesManager.isCategoryToggleCheckedFlow(NotificationCategory.BUDGET)
        .collectAsStateWithLifecycle(initialValue = true)
    val billNotif by preferencesManager.isCategoryToggleCheckedFlow(NotificationCategory.BILL)
        .collectAsStateWithLifecycle(initialValue = true)
    val savingsNotif by preferencesManager.isCategoryToggleCheckedFlow(NotificationCategory.SAVINGS)
        .collectAsStateWithLifecycle(initialValue = true)
    val roommateNotif by preferencesManager.isCategoryToggleCheckedFlow(NotificationCategory.ROOMMATE)
        .collectAsStateWithLifecycle(initialValue = true)
    val insightNotif by preferencesManager.isCategoryToggleCheckedFlow(NotificationCategory.INSIGHT)
        .collectAsStateWithLifecycle(initialValue = true)

    val categorySnapshot = remember(budgetNotif, billNotif, savingsNotif, roommateNotif, insightNotif) {
        mapOf(
            NotificationCategory.BUDGET to budgetNotif,
            NotificationCategory.BILL to billNotif,
            NotificationCategory.SAVINGS to savingsNotif,
            NotificationCategory.ROOMMATE to roommateNotif,
            NotificationCategory.INSIGHT to insightNotif
        )
    }

    val isLocked by appLockMonitor.isLocked.collectAsStateWithLifecycle()
    val aiState by geminiViewModel.uiState.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(FluxoraTab.OVERVIEW) }
    var showAiDialog by remember { mutableStateOf(false) }
    var showCinematicDesignSystem by remember { mutableStateOf(false) }

    // Attempt silent auto-sign-in once on launch
    LaunchedEffect(Unit) {
        AuthManager.attemptAutoSignIn(
            context = activity,
            credentialManager = credentialManager,
            onAuthSuccess = { authErrorBanner = null },
            onUnauthenticated = {},
            scope = scope
        )
    }

    LaunchedEffect(biometricEnabled, inactivityTimeoutMinutes) {
        appLockMonitor.updateConfiguration(biometricEnabled, inactivityTimeoutMinutes)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    appLockMonitor.checkTimeoutAndRelockIfNeeded()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Request POST_NOTIFICATIONS runtime permission on Android 13+ when authenticated
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(currentUser?.uid, masterNotificationsEnabled) {
        if (currentUser != null && masterNotificationsEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val selectedLanguageCode by preferencesManager.selectedLanguageCodeFlow.collectAsStateWithLifecycle(
        initialValue = "en"
    )

    BackHandler(enabled = showCinematicDesignSystem || currentTab != FluxoraTab.OVERVIEW) {
        if (showCinematicDesignSystem) {
            showCinematicDesignSystem = false
        } else {
            currentTab = FluxoraTab.OVERVIEW
        }
    }

    CompositionLocalProvider(
        LocalLanguageCode provides selectedLanguageCode
    ) {
        FluxoraTheme(
            appTheme = selectedTheme,
            darkTheme = isDarkMode,
            themeMode = themeMode,
            glassPreference = glassPreference,
            hideSensitiveAmounts = hideSensitiveAmounts
        ) {
        val activeUser = currentUser
        if (!onboardingCompleted) {
            OnboardingScreen(onComplete = {
                scope.launch { preferencesManager.setOnboardingCompleted(true) }
            })
        } else if (activeUser == null && !isDemoModeActive) {
            FluxoraAuthScreen(
                isAuthInProgress = isAuthInProgress,
                errorBanner = authErrorBanner,
                onGoogleSignInClick = {
                    isAuthInProgress = true
                    authErrorBanner = null
                    AuthManager.onGoogleSignInClicked(
                        context = activity,
                        credentialManager = credentialManager,
                        onAuthSuccess = {
                            isAuthInProgress = false
                            authErrorBanner = null
                        },
                        onAuthError = { err ->
                            isAuthInProgress = false
                            authErrorBanner = err
                        },
                        scope = scope,
                        onAuthCancelled = {
                            isAuthInProgress = false
                        }
                    )
                },
                onDemoLoginClick = {
                    isDemoModeActive = true
                    repository.seedDemoDataIfEmpty()
                }
            )
        } else if (biometricEnabled && isLocked) {
            BiometricAuthScreen(
                biometricAuthManager = biometricAuthManager,
                onUnlockSuccess = { appLockMonitor.unlock() },
                onSignOutFallback = {
                    isDemoModeActive = false
                    appLockMonitor.onSignOut()
                    geminiViewModel.resetUiState()
                    AuthManager.signOut(
                        context = activity,
                        credentialManager = credentialManager,
                        onSignOutComplete = {},
                        scope = scope
                    )
                }
            )
        } else {
            val effectiveUid = activeUser?.uid ?: "demo_fluxora_user"
            val effectiveDisplayName = activeUser?.displayName ?: "Ayush Mishra"
            val effectiveEmail = activeUser?.email ?: "ayush@fluxora.app"
            val financeViewModel = remember(effectiveUid) {
                FinanceViewModel(
                    repository = repository,
                    preferencesManager = preferencesManager,
                    currentUserId = effectiveUid
                )
            }
            val dashboardState by financeViewModel.dashboardState.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(dashboardState.statusBannerMessage, dashboardState.errorBannerMessage) {
                dashboardState.errorBannerMessage?.let {
                    snackbarHostState.showSnackbar(it)
                    financeViewModel.clearBanners()
                }
                dashboardState.statusBannerMessage?.let {
                    snackbarHostState.showSnackbar(it)
                    financeViewModel.clearBanners()
                }
            }

            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("fluxora_authenticated_scaffold"),
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                    ) {
                        DynamicIsland(
                            transactions = dashboardState.transactions,
                            budgets = dashboardState.budgets,
                            recurringBills = dashboardState.recurringBills,
                            onToggleHideSensitive = { financeViewModel.toggleHideSensitiveAmounts() },
                            onNavigateToSection = { section ->
                                currentTab = when (section) {
                                    "DASHBOARD" -> FluxoraTab.OVERVIEW
                                    "TRANSACTIONS" -> FluxoraTab.LEDGER
                                    "PLANNING" -> FluxoraTab.PLANNING
                                    "ROOMMATES" -> FluxoraTab.ROOMMATES
                                    else -> FluxoraTab.OVERVIEW
                                }
                            },
                            onOpenAiAdvisor = { showAiDialog = true }
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { currentTab = FluxoraTab.LEDGER },
                        shape = CircleShape,
                        containerColor = ShowcaseFabBg,
                        contentColor = ShowcasePureWhite,
                        modifier = Modifier
                            .size(56.dp)
                            .border(2.dp, ShowcaseFabGlow, CircleShape)
                            .testTag("fab_open_ai_advisor")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Quick Add Transaction",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                bottomBar = {
                    val bottomNavTabs = listOf(
                        FluxoraTab.OVERVIEW,
                        FluxoraTab.TASKS,
                        FluxoraTab.PROJECTS,
                        FluxoraTab.CALENDAR,
                        FluxoraTab.SETTINGS
                    )
                    NavigationBar(
                        containerColor = FluxoraDeepOcean.copy(alpha = 0.95f),
                        contentColor = FluxoraMoonlightBlue,
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .border(1.dp, FluxoraMoonlightBlue.copy(alpha = 0.18f), RoundedCornerShape(26.dp))
                            .testTag("fluxora_bottom_nav_bar")
                    ) {
                        bottomNavTabs.forEach { tab ->
                            val isSelected = currentTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    appLockMonitor.recordUserInteraction()
                                    currentTab = tab
                                },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = t(tab.translationKey),
                                        tint = if (isSelected) FluxoraCosmicViolet else FluxoraMoonlightBlue
                                    )
                                },
                                label = {
                                    Text(
                                        text = t(tab.translationKey),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) FluxoraDreamLavender else FluxoraMoonlightBlue
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = FluxoraCosmicViolet,
                                    selectedTextColor = FluxoraDreamLavender,
                                    indicatorColor = FluxoraCosmicViolet.copy(alpha = 0.22f),
                                    unselectedIconColor = FluxoraMoonlightBlue,
                                    unselectedTextColor = FluxoraMoonlightBlue
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.route}")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(FluxoraMidnightAbyss)
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        FluxoraTab.OVERVIEW -> OverviewDashboardScreen(
                            state = dashboardState,
                            dailyInsight = aiState.dailySavingInsight,
                            isGeneratingInsight = aiState.isGeneratingInsight,
                            selectedAiMode = aiState.selectedMode,
                            onRefreshInsight = {
                                geminiViewModel.generateDailySavingInsight(
                                    transactions = dashboardState.transactions,
                                    budgets = dashboardState.budgets,
                                    savingsGoals = dashboardState.savingsGoals,
                                    recurringBills = dashboardState.recurringBills
                                )
                            },
                            onOpenAiAdvisor = { showAiDialog = true },
                            onQuickAddTransaction = { currentTab = FluxoraTab.TASKS },
                            onNavigateToSettings = { currentTab = FluxoraTab.SETTINGS }
                        )

                        FluxoraTab.TASKS -> FluxoraTasksScreen(
                            onNavigateBack = { currentTab = FluxoraTab.OVERVIEW }
                        )

                        FluxoraTab.PROJECTS -> FluxoraProjectsScreen(
                            onNavigateBack = { currentTab = FluxoraTab.OVERVIEW }
                        )

                        FluxoraTab.CALENDAR -> FluxoraCalendarScreen(
                            onNavigateBack = { currentTab = FluxoraTab.OVERVIEW }
                        )

                        FluxoraTab.ANALYTICS -> FluxoraAnalyticsScreen(
                            onNavigateBack = { currentTab = FluxoraTab.OVERVIEW }
                        )

                        FluxoraTab.LEDGER -> LedgerTransactionsScreen(
                            state = dashboardState,
                            onAddTransaction = { title, amountMajor, type, category, ts, notes ->
                                financeViewModel.addTransaction(
                                    title = title,
                                    amountInrInput = amountMajor,
                                    type = type,
                                    category = category,
                                    timestampMillis = ts,
                                    notes = notes
                                )
                            },
                            onDeleteTransaction = { financeViewModel.deleteTransaction(it) },
                            onAddCustomCategory = { name ->
                                financeViewModel.addCustomCategory(name)
                            },
                            onExportCsv = { financeViewModel.exportTransactionsCsv() },
                            onImportCsv = { financeViewModel.importTransactionsCsv(it) }
                        )

                        FluxoraTab.PLANNING -> PlanningScreen(
                            state = dashboardState,
                            onSaveBudget = { category, limitInr ->
                                financeViewModel.saveCategoryBudget(category, limitInr)
                            },
                            onSaveSavingsGoal = { title, targetMajor, savedMajor ->
                                financeViewModel.addSavingsGoal(
                                    title = title,
                                    targetInrInput = targetMajor,
                                    initialSavedInrInput = savedMajor,
                                    targetDateMillis = System.currentTimeMillis() + 90L * 24L * 3600L * 1000L
                                )
                            },
                            onContributeGoal = { goal, addMajor ->
                                financeViewModel.contributeToGoal(goal, addMajor)
                            },
                            onSaveRecurringBill = { title, amountMajor, category, freq ->
                                financeViewModel.addRecurringBill(
                                    title = title,
                                    amountInrInput = amountMajor,
                                    category = category,
                                    frequency = freq,
                                    nextDueDateMillis = System.currentTimeMillis() + 7L * 24L * 3600L * 1000L
                                )
                            },
                            onMarkBillPaid = { financeViewModel.payRecurringBill(it) },
                            onSetDebtStrategy = { financeViewModel.setDebtStrategy(it) },
                            onSetExtraMonthlyPayment = { financeViewModel.setExtraMonthlyPayment(it) },
                            onSaveDebt = { id, name, balanceMajor, interestRate, minPay ->
                                financeViewModel.saveDebt(id, name, balanceMajor, interestRate, minPay)
                            },
                            onDeleteDebt = { financeViewModel.deleteDebt(it) }
                        )

                        FluxoraTab.ROOMMATES -> RoommateSplitScreen(
                            state = dashboardState,
                            currentUserDisplayName = effectiveDisplayName,
                            onVerifyAndClaimPhone = { e164 ->
                                financeViewModel.verifyAndClaimPhone(
                                    e164Phone = e164,
                                    displayName = effectiveDisplayName
                                )
                            },
                            onSendConnectionRequest = { recipientName, recipientPhoneE164 ->
                                financeViewModel.sendRoommateRequest(
                                    senderName = effectiveDisplayName,
                                    recipientName = recipientName,
                                    recipientPhoneE164 = recipientPhoneE164
                                )
                            },
                            onRespondToConnectionRequest = { req, accept ->
                                financeViewModel.respondToConnectionRequest(req, accept)
                            },
                            onAddHouseholdMember = { name, phone, upi ->
                                financeViewModel.addHouseholdMember(name, phone, upi)
                            },
                            onCreateSharedExpense = { title, totalInr, paidBy, splitMethod, participants ->
                                financeViewModel.createSharedExpense(
                                    title = title,
                                    totalInrInput = totalInr,
                                    paidByName = paidBy,
                                    splitMethod = splitMethod,
                                    participants = participants
                                )
                            },
                            onConfirmParticipantSettled = { expense, participantId ->
                                financeViewModel.confirmParticipantSettled(expense, participantId)
                            }
                        )

                        FluxoraTab.SETTINGS -> {
                            if (showCinematicDesignSystem) {
                                CinematicDesignSystemShowcaseScreen(
                                    currentThemeMode = themeMode,
                                    currentGlassPreference = glassPreference,
                                    onSelectThemeMode = { financeViewModel.setThemeMode(it) },
                                    onSelectGlassPreference = { financeViewModel.setGlassEffectPreference(it) },
                                    onClose = { showCinematicDesignSystem = false }
                                )
                            } else {
                                SettingsAndGovernanceScreen(
                                    currentUserDisplayName = effectiveDisplayName,
                                    currentUserEmail = effectiveEmail,
                                    selectedTheme = selectedTheme,
                                    isDarkMode = isDarkMode,
                                    hideSensitiveAmounts = hideSensitiveAmounts,
                                    biometricLockEnabled = biometricEnabled,
                                    inactivityTimeoutMinutes = inactivityTimeoutMinutes,
                                    masterNotificationsEnabled = masterNotificationsEnabled,
                                    categoryToggleSnapshot = categorySnapshot,
                                    onSelectTheme = { financeViewModel.selectTheme(it) },
                                    onToggleDarkMode = { financeViewModel.toggleDarkMode(it) },
                                    onToggleHideSensitiveAmounts = { financeViewModel.toggleHideSensitiveAmounts() },
                                    onUpdateBiometricSettings = { enabled, mins ->
                                        financeViewModel.updateBiometricSettings(enabled, mins)
                                    },
                                    onToggleMasterNotifications = { financeViewModel.setMasterNotifications(it) },
                                    onToggleCategoryNotification = { cat, enabled ->
                                        financeViewModel.setCategoryNotifications(cat, enabled)
                                    },
                                    onSignOut = {
                                        isDemoModeActive = false
                                        appLockMonitor.onSignOut()
                                        geminiViewModel.resetUiState()
                                        AuthManager.signOut(
                                            context = activity,
                                            credentialManager = credentialManager,
                                            onSignOutComplete = {},
                                            scope = scope
                                        )
                                    },
                                    isAdvancedMode = dashboardState.isAdvancedMode,
                                    isAiAdvisorEnabled = dashboardState.isAiAdvisorEnabled,
                                    selectedLanguageCode = dashboardState.selectedLanguageCode,
                                    onToggleAdvancedMode = { financeViewModel.setAdvancedMode(it) },
                                    onToggleAiAdvisorEnabled = { financeViewModel.setAiAdvisorEnabled(it) },
                                    onSelectLanguage = { financeViewModel.setSelectedLanguageCode(it) },
                                    onDeleteAllAccountData = {
                                        financeViewModel.deleteAllAccountData {
                                            isDemoModeActive = false
                                            appLockMonitor.onSignOut()
                                            geminiViewModel.resetUiState()
                                            AuthManager.signOut(
                                                context = activity,
                                                credentialManager = credentialManager,
                                                onSignOutComplete = {},
                                                scope = scope
                                            )
                                        }
                                    },
                                    themeMode = themeMode,
                                    glassPreference = glassPreference,
                                    onSelectThemeMode = { financeViewModel.setThemeMode(it) },
                                    onSelectGlassPreference = { financeViewModel.setGlassEffectPreference(it) },
                                    onOpenCinematicDesignSystem = { showCinematicDesignSystem = true }
                                )
                            }
                        }
                    }
                }
            }

            if (showAiDialog) {
                GeminiChatDialog(
                    uiState = aiState,
                    quickChips = geminiViewModel.quickChips,
                    onSelectMode = { geminiViewModel.selectMode(it) },
                    onSendQuestion = { q ->
                        geminiViewModel.sendQuestion(
                            userPrompt = q,
                            transactions = dashboardState.transactions,
                            budgets = dashboardState.budgets,
                            savingsGoals = dashboardState.savingsGoals,
                            recurringBills = dashboardState.recurringBills
                        )
                    },
                    onClearChat = { geminiViewModel.clearConversation() },
                    onDismiss = { showAiDialog = false }
                )
            }
        }
    }
}
}
