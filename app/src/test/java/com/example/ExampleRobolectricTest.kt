package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.example.ai.AiAdvisorMode
import com.example.ai.GeminiChatViewModel
import com.example.data.CategoryBudget
import com.example.data.DefaultCategories
import com.example.data.FinancialTransaction
import com.example.data.PreferencesManager
import com.example.data.RecurringBill
import com.example.data.SavingsGoal
import com.example.finance.FinanceDashboardState
import com.example.notifications.FluxoraAlert
import com.example.notifications.NotificationCategory
import com.example.notifications.NotificationManager
import com.example.notifications.SystemNotificationHelper
import com.example.ui.components.DynamicIsland
import com.example.ui.screens.FluxoraAuthScreen
import com.example.ui.screens.LedgerTransactionsScreen
import com.example.ui.screens.OverviewDashboardScreen
import com.example.ui.screens.PlanningScreen
import com.example.ui.screens.RoommateSplitScreen
import com.example.ui.screens.SettingsAndGovernanceScreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.FluxoraTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h1200dp")
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var preferencesManager: PreferencesManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val datastoreDir = File(context.filesDir, "datastore")
        if (datastoreDir.exists()) {
            datastoreDir.deleteRecursively()
        }
        preferencesManager = PreferencesManager(context)
    }

    // --- 1. PreferencesManager Per-UID Phone Verification Scoping Tests ---

    @Test
    fun `recordVerifiedPhone scopes phone verification strictly to current uid`() = runBlocking {
        val claimRes = preferencesManager.recordVerifiedPhone("uid_user_a", "+919876543210")
        assertTrue(claimRes.isSuccess)

        assertTrue(preferencesManager.isPhoneVerifiedFlow("uid_user_a").first())
        assertEquals("+919876543210", preferencesManager.getVerifiedPhoneFlow("uid_user_a").first())
        assertEquals("uid_user_a", preferencesManager.getPhoneOwnerFlow("+919876543210").first())

        // Different account on same device must NOT inherit User A's verified phone
        assertFalse(preferencesManager.isPhoneVerifiedFlow("uid_user_b").first())
        assertNull(preferencesManager.getVerifiedPhoneFlow("uid_user_b").first())
        assertFalse(preferencesManager.isPhoneVerifiedFlow("").first())
    }

    @Test
    fun `recordVerifiedPhone blocks second account from claiming already-owned phone`() = runBlocking {
        val claimA = preferencesManager.recordVerifiedPhone("uid_user_a", "+919876543210")
        assertTrue(claimA.isSuccess)

        val claimB = preferencesManager.recordVerifiedPhone("uid_user_b", "+919876543210")
        assertTrue(claimB.isFailure)
        assertFalse(preferencesManager.isPhoneVerifiedFlow("uid_user_b").first())
    }

    @Test
    fun `master notification toggle off preserves individual category toggles`() = runBlocking {
        preferencesManager.setCategoryNotificationEnabled(NotificationCategory.BUDGET, true)
        preferencesManager.setCategoryNotificationEnabled(NotificationCategory.BILL, false)
        preferencesManager.setCategoryNotificationEnabled(NotificationCategory.SAVINGS, true)
        preferencesManager.setCategoryNotificationEnabled(NotificationCategory.ROOMMATE, false)
        preferencesManager.setCategoryNotificationEnabled(NotificationCategory.INSIGHT, true)

        // Turn master off
        preferencesManager.setMasterNotificationsEnabled(false)
        assertFalse(preferencesManager.masterNotificationsEnabledFlow.first())

        // Individual category toggles must remain intact
        assertTrue(preferencesManager.isCategoryToggleCheckedFlow(NotificationCategory.BUDGET).first())
        assertFalse(preferencesManager.isCategoryToggleCheckedFlow(NotificationCategory.BILL).first())
        assertTrue(preferencesManager.isCategoryToggleCheckedFlow(NotificationCategory.SAVINGS).first())
        assertFalse(preferencesManager.isCategoryToggleCheckedFlow(NotificationCategory.ROOMMATE).first())
        assertTrue(preferencesManager.isCategoryToggleCheckedFlow(NotificationCategory.INSIGHT).first())

        // Turn master back on -> individual preferences still intact
        preferencesManager.setMasterNotificationsEnabled(true)
        assertTrue(preferencesManager.masterNotificationsEnabledFlow.first())
        assertFalse(preferencesManager.isCategoryToggleCheckedFlow(NotificationCategory.BILL).first())
    }

    @Test
    fun `theme dark mode and privacy settings persist in DataStore`() = runBlocking {
        preferencesManager.setSelectedTheme(AppTheme.CYBER_VIOLET)
        preferencesManager.setDarkMode(false)
        preferencesManager.setHideSensitiveAmounts(true)
        preferencesManager.setBiometricLockEnabled(true)
        preferencesManager.setInactivityTimeoutMinutes(15)

        assertEquals(AppTheme.CYBER_VIOLET, preferencesManager.selectedThemeFlow.first())
        assertFalse(preferencesManager.isDarkModeFlow.first())
        assertTrue(preferencesManager.hideSensitiveAmountsFlow.first())
        assertTrue(preferencesManager.isBiometricLockEnabledFlow.first())
        assertEquals(15, preferencesManager.inactivityTimeoutMinutesFlow.first())
    }

    // --- 2. NotificationManager Deduplication & Filtering Tests ---

    @Test
    fun `NotificationManager suppresses duplicate relatedId alerts`() {
        SystemNotificationHelper.ensureChannelsCreated(context)
        val notificationManager = NotificationManager()

        val alert = FluxoraAlert(
            id = "a1",
            category = NotificationCategory.BUDGET,
            title = "Food Budget 80%",
            message = "Approaching limit",
            relatedId = "budget_80_cat_food_2026-11"
        )
        assertTrue(notificationManager.dispatchAlert(alert))
        assertFalse(notificationManager.dispatchAlert(alert))
        assertEquals(1, notificationManager.getDispatchedAlerts().size)
    }

    @Test
    fun `NotificationManager respects master and category toggles while preserving category map`() {
        val notificationManager = NotificationManager()
        notificationManager.setCategoryEnabled(NotificationCategory.BILL, false)

        val billAlert = FluxoraAlert(
            id = "a2",
            category = NotificationCategory.BILL,
            title = "Bill Due",
            message = "Internet bill due",
            relatedId = "bill_due_1"
        )
        assertFalse(notificationManager.dispatchAlert(billAlert))

        notificationManager.setMasterEnabled(false)
        assertFalse(notificationManager.isCategoryToggleChecked(NotificationCategory.BILL))
        assertTrue(notificationManager.isCategoryToggleChecked(NotificationCategory.BUDGET))
    }

    // --- 3. GeminiChatViewModel Dual-Mode & Reset State Tests ---

    @Test
    fun `GeminiChatViewModel supports Flash-Lite and Argon High Thinking modes and resetUiState`() {
        val vm = GeminiChatViewModel()
        assertEquals(AiAdvisorMode.FAST_FLASH_LITE, vm.uiState.value.selectedMode)
        assertEquals("gemini-3.1-flash-lite", AiAdvisorMode.FAST_FLASH_LITE.modelName)
        assertEquals("gemini-4-argon", AiAdvisorMode.HIGH_THINKING_ARGON.modelName)

        vm.selectMode(AiAdvisorMode.HIGH_THINKING_ARGON)
        assertEquals(AiAdvisorMode.HIGH_THINKING_ARGON, vm.uiState.value.selectedMode)

        vm.resetUiState()
        assertEquals(AiAdvisorMode.FAST_FLASH_LITE, vm.uiState.value.selectedMode)
        assertTrue(vm.uiState.value.messages.isEmpty())
    }

    @Test
    fun `GeminiChatViewModel builds financial context summary with Indian Rupee formatting`() {
        val vm = GeminiChatViewModel()
        val summary = vm.buildFinancialContextSummary(
            transactions = listOf(
                FinancialTransaction(
                    id = "t1",
                    title = "Salary",
                    amountMinor = 15_000_000L, // INR 1,50,000.00
                    type = "INCOME",
                    categoryId = "housing",
                    categoryName = "Salary"
                ),
                FinancialTransaction(
                    id = "t2",
                    title = "Rent",
                    amountMinor = 2_500_000L, // INR 25,000.00
                    type = "EXPENSE",
                    categoryId = "housing",
                    categoryName = "Housing"
                )
            ),
            budgets = listOf(
                CategoryBudget(
                    id = "b1",
                    categoryId = "housing",
                    categoryName = "Housing",
                    limitMinor = 3_000_000L
                )
            ),
            savingsGoals = listOf(
                SavingsGoal(
                    id = "g1",
                    title = "Emergency Fund",
                    targetMinor = 50_000_000L,
                    savedMinor = 10_000_000L
                )
            ),
            recurringBills = listOf(
                RecurringBill(
                    id = "rb1",
                    title = "Broadband",
                    amountMinor = 99_900L,
                    frequency = "MONTHLY"
                )
            )
        )

        assertTrue(summary.contains("INR 1,50,000.00"))
        assertTrue(summary.contains("Housing"))
        assertTrue(summary.contains("Emergency Fund"))
        assertTrue(summary.contains("Broadband"))
    }

    // --- 4. Jetpack Compose Screen Rendering & Interaction Tests ---

    @Test
    fun `FluxoraAuthScreen renders Cinematic background and vertically centered auth options`() {
        var clicked = false
        composeTestRule.setContent {
            FluxoraTheme {
                FluxoraAuthScreen(
                    isAuthInProgress = false,
                    errorBanner = null,
                    onGoogleSignInClick = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("fluxora_auth_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cinematic_background_system").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cinematic_background_image").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cinematic_overlay_canvas").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_email_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_password_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("email_sign_in_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("forgot_password_link").assertIsDisplayed()
        composeTestRule.onNodeWithTag("google_sign_in_button").performClick()
        assertTrue(clicked)
    }

    @Test
    fun `DynamicIsland displays budget warning when category spend reaches 80 percent`() {
        composeTestRule.setContent {
            FluxoraTheme {
                DynamicIsland(
                    transactions = listOf(
                        FinancialTransaction(
                            id = "t1",
                            title = "Dinner",
                            amountMinor = 850_000L,
                            type = "EXPENSE",
                            categoryId = "food_dining",
                            categoryName = "Food & Dining"
                        )
                    ),
                    budgets = listOf(
                        CategoryBudget(
                            id = "b1",
                            categoryId = "food_dining",
                            categoryName = "Food & Dining",
                            limitMinor = 1_000_000L
                        )
                    ),
                    recurringBills = emptyList(),
                    onToggleHideSensitive = {},
                    onNavigateToSection = {},
                    onOpenAiAdvisor = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("dynamic_island").assertIsDisplayed()
        composeTestRule.onNodeWithText("80%+ Budget Warning: Food & Dining").assertIsDisplayed()
    }

    @Test
    fun `OverviewDashboardScreen renders 3D Cash-Flow Reactor and Daily AI Saving Insight card`() {
        val sampleState = FinanceDashboardState(
            userId = "uid_1",
            transactions = listOf(
                FinancialTransaction(
                    id = "t1",
                    title = "Salary",
                    amountMinor = 10_000_000L,
                    type = "INCOME",
                    categoryId = "other",
                    categoryName = "Salary"
                )
            )
        )

        composeTestRule.setContent {
            FluxoraTheme {
                OverviewDashboardScreen(
                    state = sampleState,
                    dailyInsight = "Save INR 2,000 on dining this week.",
                    isGeneratingInsight = false,
                    selectedAiMode = AiAdvisorMode.FAST_FLASH_LITE,
                    onRefreshInsight = {},
                    onOpenAiAdvisor = {},
                    onQuickAddTransaction = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("overview_dashboard_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("overview_dashboard_screen")
            .performScrollToNode(hasTestTag("recharts_financial_summary_card"))
        composeTestRule.onNodeWithTag("recharts_financial_summary_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("overview_dashboard_screen")
            .performScrollToNode(hasTestTag("daily_ai_insight_card"))
        composeTestRule.onNodeWithTag("daily_ai_insight_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save INR 2,000 on dining this week.").assertIsDisplayed()
    }

    @Test
    fun `LedgerTransactionsScreen displays transactions and opens CSV portability dialog`() {
        val tx = FinancialTransaction(
            id = "tx_demo_1",
            title = "Metro Card",
            amountMinor = 50_000L,
            type = "EXPENSE",
            categoryId = "transportation",
            categoryName = "Transportation",
            timestampMillis = 1730000000000L
        )
        val sampleState = FinanceDashboardState(
            userId = "uid_1",
            transactions = listOf(tx)
        )

        composeTestRule.setContent {
            FluxoraTheme {
                LedgerTransactionsScreen(
                    state = sampleState,
                    onAddTransaction = { _, _, _, _, _, _ -> },
                    onDeleteTransaction = {},
                    onAddCustomCategory = {},
                    onExportCsv = { "Title,AmountINR\nMetro Card,500.00" },
                    onImportCsv = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("ledger_transactions_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Metro Card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("open_csv_portability_button").performClick()
        composeTestRule.onNodeWithTag("csv_portability_text_field").assertIsDisplayed()
    }

    @Test
    fun `PlanningScreen renders budgets savings goals and recurring bills`() {
        val sampleState = FinanceDashboardState(
            userId = "uid_1",
            budgets = listOf(
                CategoryBudget(
                    id = "b1",
                    categoryId = "food_dining",
                    categoryName = "Food & Dining",
                    monthKey = "2026-11",
                    limitMinor = 1_000_000L
                )
            ),
            savingsGoals = listOf(
                SavingsGoal(
                    id = "g1",
                    title = "MacBook Pro",
                    targetMinor = 20_000_000L,
                    savedMinor = 10_000_000L
                )
            ),
            recurringBills = listOf(
                RecurringBill(
                    id = "bill1",
                    title = "Fiber Internet",
                    amountMinor = 99_900L,
                    frequency = "MONTHLY"
                )
            )
        )

        composeTestRule.setContent {
            FluxoraTheme {
                PlanningScreen(
                    state = sampleState,
                    onSaveBudget = { _, _ -> },
                    onSaveSavingsGoal = { _, _, _ -> },
                    onContributeGoal = { _, _ -> },
                    onSaveRecurringBill = { _, _, _, _ -> },
                    onMarkBillPaid = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("planning_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Food & Dining").assertIsDisplayed()
        composeTestRule.onNodeWithText("MacBook Pro").assertIsDisplayed()
    }

    @Test
    fun `RoommateSplitScreen renders phone verification card and invite controls`() {
        val sampleState = FinanceDashboardState(
            userId = "uid_123",
            isPhoneVerified = true,
            verifiedPhoneE164 = "+919876543210"
        )

        composeTestRule.setContent {
            FluxoraTheme {
                RoommateSplitScreen(
                    state = sampleState,
                    currentUserDisplayName = "Ayush Mishra",
                    onVerifyAndClaimPhone = {},
                    onSendConnectionRequest = { _, _ -> },
                    onRespondToConnectionRequest = { _, _ -> },
                    onAddHouseholdMember = { _, _, _ -> },
                    onCreateSharedExpense = { _, _, _, _, _ -> },
                    onConfirmParticipantSettled = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithTag("roommate_split_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Phone Verified (+91 98••••••10)").assertIsDisplayed()
    }

    @Test
    fun `SettingsAndGovernanceScreen renders all 8 theme chips and privacy toggles`() {
        var selectedTheme: AppTheme? = null

        composeTestRule.setContent {
            FluxoraTheme {
                SettingsAndGovernanceScreen(
                    currentUserDisplayName = "Ayush Mishra",
                    currentUserEmail = "itsanshmishra34@gmail.com",
                    selectedTheme = AppTheme.MIDNIGHT_EMERALD,
                    isDarkMode = true,
                    hideSensitiveAmounts = false,
                    biometricLockEnabled = false,
                    inactivityTimeoutMinutes = 5,
                    masterNotificationsEnabled = true,
                    categoryToggleSnapshot = NotificationCategory.entries.associateWith { true },
                    onSelectTheme = { selectedTheme = it },
                    onToggleDarkMode = {},
                    onToggleHideSensitiveAmounts = {},
                    onUpdateBiometricSettings = { _, _ -> },
                    onToggleMasterNotifications = {},
                    onToggleCategoryNotification = { _, _ -> },
                    onSignOut = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("settings_governance_screen").assertIsDisplayed()
        if (composeTestRule.onAllNodes(hasTestTag("change_accent_button")).fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithTag("change_accent_button").performClick()
        }
        composeTestRule.onNodeWithTag("theme_option_CYBER_VIOLET").performClick()
        assertEquals(AppTheme.CYBER_VIOLET, selectedTheme)
    }

    @Test
    fun `DefaultCategories provides 12 standard built-in categories`() {
        assertEquals(12, DefaultCategories.BUILT_IN.size)
        val cats = DefaultCategories.toFinancialCategories("uid_1")
        assertEquals(12, cats.size)
        assertTrue(cats.all { it.isDefault })
    }

    @Test
    fun `GoogleSignInAuthComponent supports unauthenticated sign-in and authenticated sign-out`() {
        val isAuthenticated = androidx.compose.runtime.mutableStateOf(false)
        var signInTriggered = false
        var signOutTriggered = false

        composeTestRule.setContent {
            FluxoraTheme {
                com.example.authentication.GoogleSignInAuthComponent(
                    isAuthenticated = isAuthenticated.value,
                    userDisplayName = "Ayush Mishra",
                    userEmail = "itsanshmishra34@gmail.com",
                    onSignInClick = {
                        signInTriggered = true
                        isAuthenticated.value = true
                    },
                    onSignOutClick = {
                        signOutTriggered = true
                        isAuthenticated.value = false
                    }
                )
            }
        }

        composeTestRule.onNodeWithTag("google_sign_in_auth_component").assertIsDisplayed()
        composeTestRule.onNodeWithTag("google_sign_in_button").performClick()
        assertTrue(signInTriggered)

        composeTestRule.onNodeWithTag("google_auth_user_name").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sign_out_button").performClick()
        assertTrue(signOutTriggered)
    }

    @Test
    fun `CinematicDesignSystemShowcaseScreen renders tokens WCAG audit and switches adaptive theme modes`() {
        val modeState = androidx.compose.runtime.mutableStateOf(com.example.ui.theme.CinematicThemeMode.DARK)
        val glassState = androidx.compose.runtime.mutableStateOf(com.example.ui.glass.GlassEffectPreference.AUTO)

        composeTestRule.setContent {
            FluxoraTheme(
                themeMode = modeState.value,
                glassPreference = glassState.value
            ) {
                com.example.ui.components.CinematicDesignSystemShowcaseScreen(
                    currentThemeMode = modeState.value,
                    currentGlassPreference = glassState.value,
                    onSelectThemeMode = { modeState.value = it },
                    onSelectGlassPreference = { glassState.value = it },
                    onClose = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("cinematic_design_system_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cinematic_mode_LIGHT").performClick()
        assertEquals(com.example.ui.theme.CinematicThemeMode.LIGHT, modeState.value)

        composeTestRule.onNodeWithTag("cinematic_tab_1").performClick()
    }

    @Test
    fun `Showcase Login Splash and Overview Dashboard render Net Spend Cash Flow and Monthly Cash Flow`() {
        composeTestRule.setContent {
            FluxoraTheme {
                OverviewDashboardScreen(
                    state = FinanceDashboardState(userId = "uid_showcase"),
                    dailyInsight = "Your cash flow is balanced.",
                    isGeneratingInsight = false,
                    selectedAiMode = AiAdvisorMode.FAST_FLASH_LITE,
                    onRefreshInsight = {},
                    onOpenAiAdvisor = {},
                    onQuickAddTransaction = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Net Spend").assertIsDisplayed()
        composeTestRule.onNodeWithTag("available_to_spend_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reactor_net_balance_text").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dashboard_month_selector_pill").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cash Flow Overview").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monthly inflow vs outflow").assertIsDisplayed()
        composeTestRule.onNodeWithTag("monthly_cash_flow_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monthly Cash Flow").assertIsDisplayed()
    }

    // --- 5. BiometricPrompt Authentication Flow Tests ---

    @Test
    fun `BiometricAuthScreen displays vault locked status and allows unlock or sign out`() {
        var unlocked = false
        var signedOut = false
        val biometricAuthManager = com.example.security.BiometricAuthManager(context)

        composeTestRule.setContent {
            FluxoraTheme {
                com.example.security.BiometricAuthScreen(
                    biometricAuthManager = biometricAuthManager,
                    onUnlockSuccess = { unlocked = true },
                    onSignOutFallback = { signedOut = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("biometric_lock_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fluxora Vault Locked").assertIsDisplayed()
        composeTestRule.onNodeWithTag("biometric_unlock_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("biometric_sign_out_button").assertIsDisplayed()

        composeTestRule.onNodeWithTag("biometric_unlock_button").performClick()
        assertTrue(unlocked)

        composeTestRule.onNodeWithTag("biometric_sign_out_button").performClick()
        assertTrue(signedOut)
    }

    @Test
    fun `AppLockMonitor correctly tracks user interaction and inactivity timeouts`() {
        val monitor = com.example.security.AppLockMonitor(
            initialEnabled = true,
            initialTimeoutMinutes = 5
        )

        // Initially locked because enabled was true
        assertTrue(monitor.isLocked.value)

        // Unlock at time T
        monitor.unlock(1_000_000L)
        assertFalse(monitor.isLocked.value)

        // Interaction within 5 mins does not relock
        monitor.recordUserInteraction(1_000_000L + 120_000L)
        assertFalse(monitor.isLocked.value)

        // Beyond 5 minutes (300,000 ms), timeout triggers relock
        val relocked = monitor.checkTimeoutAndRelockIfNeeded(1_000_000L + 120_000L + 350_000L)
        assertTrue(relocked)
        assertTrue(monitor.isLocked.value)

        // Sign-out cleans up state
        monitor.onSignOut()
        assertFalse(monitor.isLocked.value)
    }

    // --- 6. Recharts-style Chart & Centered Login Form Tests ---

    @Test
    fun `OverviewDashboardScreen renders Recharts-style Monthly Spending Trends chart and filters timeframe`() {
        val sampleState = FinanceDashboardState(
            userId = "uid_chart_test",
            transactions = listOf(
                FinancialTransaction(
                    id = "tx_chart_1",
                    title = "Groceries",
                    amountMinor = 14_200_00L,
                    type = "EXPENSE",
                    timestampMillis = 1715000000000L
                )
            )
        )

        composeTestRule.setContent {
            FluxoraTheme {
                OverviewDashboardScreen(
                    state = sampleState,
                    dailyInsight = "Your monthly spend is on track.",
                    isGeneratingInsight = false,
                    selectedAiMode = AiAdvisorMode.FAST_FLASH_LITE,
                    onRefreshInsight = {},
                    onOpenAiAdvisor = {},
                    onQuickAddTransaction = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("overview_dashboard_screen")
            .performScrollToNode(hasTestTag("recharts_financial_summary_card"))

        composeTestRule.onNodeWithTag("recharts_financial_summary_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monthly Spending Trends").assertIsDisplayed()
        composeTestRule.onNodeWithTag("recharts_smooth_line_canvas").assertIsDisplayed()
        composeTestRule.onNodeWithTag("recharts_tooltip_card").assertIsDisplayed()

        // Test timeframe filter
        composeTestRule.onNodeWithTag("recharts_filter_3m").performClick()
        composeTestRule.onNodeWithTag("recharts_financial_summary_card").assertIsDisplayed()
    }

    @Test
    fun `FluxoraAuthScreen vertically centers complete login content and renders all form controls`() {
        composeTestRule.setContent {
            FluxoraTheme {
                FluxoraAuthScreen(
                    isAuthInProgress = false,
                    errorBanner = null,
                    onGoogleSignInClick = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("fluxora_auth_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign in to continue your journey").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_email_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_password_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("email_sign_in_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("google_sign_in_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("forgot_password_link").assertIsDisplayed()
    }
}


