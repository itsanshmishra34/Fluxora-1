package com.example.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authentication.PhoneValidation
import com.example.data.BillFrequency
import com.example.data.CategoryBudget
import com.example.data.DefaultCategories
import com.example.data.FinanceRepository
import com.example.data.FinancialCategory
import com.example.data.FinancialTransaction
import com.example.data.HouseholdMember
import com.example.data.ParticipantSplit
import com.example.data.PreferencesManager
import com.example.data.RecurringBill
import com.example.data.SavingsGoal
import com.example.data.SharedExpense
import com.example.data.SplitMethod
import com.example.data.TransactionType
import com.example.notifications.FluxoraAlert
import com.example.notifications.NotificationCategory
import com.example.roommate.ConnectionRequestStatus
import com.example.roommate.RoommateConnectionRequest
import com.example.ui.glass.GlassEffectPreference
import com.example.ui.theme.AppTheme
import com.example.ui.theme.CinematicThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FinanceDashboardState(
    val userId: String = "",
    val transactions: List<FinancialTransaction> = emptyList(),
    val categories: List<FinancialCategory> = emptyList(),
    val budgets: List<CategoryBudget> = emptyList(),
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val recurringBills: List<RecurringBill> = emptyList(),
    val householdMembers: List<HouseholdMember> = emptyList(),
    val sharedExpenses: List<SharedExpense> = emptyList(),
    val incomingRequests: List<RoommateConnectionRequest> = emptyList(),
    val outgoingRequests: List<RoommateConnectionRequest> = emptyList(),
    val alerts: List<FluxoraAlert> = emptyList(),
    val isPhoneVerified: Boolean = false,
    val verifiedPhoneE164: String? = null,
    val statusBannerMessage: String? = null,
    val errorBannerMessage: String? = null,
    val isAdvancedMode: Boolean = false,
    val isAiAdvisorEnabled: Boolean = true,
    val selectedLanguageCode: String = "en",
    val themeMode: CinematicThemeMode = CinematicThemeMode.DARK,
    val glassEffectPreference: GlassEffectPreference = GlassEffectPreference.AUTO,
    val debts: List<com.example.data.DebtItem> = emptyList(),
    val debtStrategy: com.example.data.DebtPayoffStrategy = com.example.data.DebtPayoffStrategy.SNOWBALL,
    val extraMonthlyPaymentMinor: Long = 0L
) {
    val debtPayoffSimulation: DebtPayoffSimulationResult
        get() = DebtCalculatorEngine.calculateDebtPayoffSchedule(debts, debtStrategy, extraMonthlyPaymentMinor)

    val totalIncomeMinor: Long
        get() = transactions.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amountMinor }

    val totalExpenseMinor: Long
        get() = transactions.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amountMinor }

    val netBalanceMinor: Long
        get() = totalIncomeMinor - totalExpenseMinor

    val totalSavingsMinor: Long
        get() = savingsGoals.sumOf { it.savedMinor }

    /**
     * Available to spend = Net Balance minus Total Saved in Goals (Section 7 Dashboard definition).
     */
    val availableToSpendMinor: Long
        get() = netBalanceMinor - totalSavingsMinor

    /**
     * Safe-to-Spend Today metric (Wave 1 Page 48 of Master Engineering Prompt v2).
     * Returns null when the user has not set any budgets (shown as "—").
     */
    val safeToSpendTodayMinor: Long?
        get() {
            if (budgets.isEmpty()) return null
            val categoryPairs: List<Pair<Long, Long>> = budgets.map { budget ->
                val spentMinor = transactions
                    .filter {
                        it.type == TransactionType.EXPENSE.name &&
                            (it.categoryId == budget.categoryId || it.categoryName.equals(budget.categoryName, ignoreCase = true))
                    }
                    .sumOf { tx -> tx.amountMinor }
                budget.limitMinor to spentMinor
            }
            val nowMillis = System.currentTimeMillis()
            val endOfMonthApprox = nowMillis + (FinancialEngine.calculateDaysLeftInMonth(nowMillis) * 24L * 3600L * 1000L)
            val upcomingUnpaidBillsMinor = recurringBills
                .filter { it.lastPaidAtMillis < it.nextDueDateMillis && it.nextDueDateMillis <= endOfMonthApprox }
                .sumOf { bill -> bill.amountMinor }
            return FinancialEngine.calculateSafeToSpendToday(
                categoryBudgetsAndSpent = categoryPairs,
                upcomingBillsMinor = upcomingUnpaidBillsMinor,
                daysLeftInMonth = FinancialEngine.calculateDaysLeftInMonth(nowMillis)
            )?.amountMinor
        }
}

class FinanceViewModel(
    private val repository: FinanceRepository,
    private val preferencesManager: PreferencesManager,
    private val currentUserId: String
) : ViewModel() {

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _lastCsvExport = MutableStateFlow<String?>(null)
    val lastCsvExport: StateFlow<String?> = _lastCsvExport.asStateFlow()

    val selectedTheme: StateFlow<AppTheme> = preferencesManager.selectedThemeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), AppTheme.MIDNIGHT_EMERALD)

    val isDarkMode: StateFlow<Boolean> = preferencesManager.isDarkModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), true)

    val themeMode: StateFlow<CinematicThemeMode> = preferencesManager.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), CinematicThemeMode.DARK)

    val glassEffectPreference: StateFlow<GlassEffectPreference> = preferencesManager.glassEffectPreferenceFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), GlassEffectPreference.AUTO)

    val hideSensitiveAmounts: StateFlow<Boolean> = preferencesManager.hideSensitiveAmountsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    val isBiometricEnabled: StateFlow<Boolean> = preferencesManager.isBiometricLockEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    val inactivityTimeoutMinutes: StateFlow<Int> = preferencesManager.inactivityTimeoutMinutesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 5)

    val masterNotificationsEnabled: StateFlow<Boolean> = preferencesManager.masterNotificationsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), true)

    val isAdvancedMode: StateFlow<Boolean> = preferencesManager.isAdvancedModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    val isAiAdvisorEnabled: StateFlow<Boolean> = preferencesManager.isAiAdvisorEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), true)

    val selectedLanguageCode: StateFlow<String> = preferencesManager.selectedLanguageCodeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), "en")

    val isPhoneVerified: StateFlow<Boolean> = preferencesManager.isPhoneVerifiedFlow(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    val verifiedPhoneE164: StateFlow<String?> = preferencesManager.getVerifiedPhoneFlow(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    private val transactionsFlow = repository.observeTransactions(currentUserId)
        .catch { emit(repository.inMemoryTransactions.value) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val categoriesFlow = repository.observeCategories(currentUserId)
        .catch { emit(DefaultCategories.toFinancialCategories(currentUserId)) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            DefaultCategories.toFinancialCategories(currentUserId)
        )

    private val budgetsFlow = repository.observeBudgets(currentUserId)
        .catch { emit(repository.inMemoryBudgets.value) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val savingsGoalsFlow = repository.observeSavingsGoals(currentUserId)
        .catch { emit(repository.inMemorySavingsGoals.value) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val recurringBillsFlow = repository.observeRecurringBills(currentUserId)
        .catch { emit(repository.inMemoryRecurringBills.value) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val householdMembersFlow = repository.observeHouseholdMembers(currentUserId)
        .catch { emit(repository.inMemoryHouseholdMembers.value) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val sharedExpensesFlow = repository.observeSharedExpenses(currentUserId)
        .catch { emit(repository.inMemorySharedExpenses.value) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val incomingRequestsFlow = repository.observeIncomingConnectionRequests(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val outgoingRequestsFlow = repository.observeOutgoingConnectionRequests(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val debtsFlow = repository.observeDebts(currentUserId)
        .catch { emit(repository.inMemoryDebts.value) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _debtStrategy = MutableStateFlow(com.example.data.DebtPayoffStrategy.SNOWBALL)
    val debtStrategy: StateFlow<com.example.data.DebtPayoffStrategy> = _debtStrategy.asStateFlow()

    private val _extraMonthlyPaymentMinor = MutableStateFlow(0L)
    val extraMonthlyPaymentMinor: StateFlow<Long> = _extraMonthlyPaymentMinor.asStateFlow()

    val dashboardState: StateFlow<FinanceDashboardState> = combine(
        combine(
            combine(transactionsFlow, categoriesFlow, budgetsFlow) { txs, cats, bgs -> Triple(txs, cats, bgs) },
            combine(savingsGoalsFlow, recurringBillsFlow, householdMembersFlow) { goals, bills, members -> Triple(goals, bills, members) },
            combine(sharedExpensesFlow, incomingRequestsFlow, outgoingRequestsFlow) { expenses, inc, out -> Triple(expenses, inc, out) }
        ) { c, p, r -> Triple(c, p, r) },
        combine(isPhoneVerified, verifiedPhoneE164, _statusMessage, _errorMessage) { verified, phone, status, err ->
            listOf(verified, phone, status, err)
        },
        combine(
            combine(isAdvancedMode, isAiAdvisorEnabled, selectedLanguageCode) { adv, ai, lang -> Triple(adv, ai, lang) },
            themeMode,
            glassEffectPreference
        ) { prefs, mode, glass ->
            listOf(prefs.first, prefs.second, prefs.third, mode, glass)
        },
        combine(debtsFlow, _debtStrategy, _extraMonthlyPaymentMinor) { d, strat, extra ->
            Triple(d, strat, extra)
        }
    ) { tri, meta, prefs, debtData ->
        @Suppress("UNCHECKED_CAST")
        FinanceDashboardState(
            userId = currentUserId,
            transactions = tri.first.first,
            categories = tri.first.second,
            budgets = tri.first.third,
            savingsGoals = tri.second.first,
            recurringBills = tri.second.second,
            householdMembers = tri.second.third,
            sharedExpenses = tri.third.first,
            incomingRequests = tri.third.second,
            outgoingRequests = tri.third.third,
            alerts = repository.notificationManager.getDispatchedAlerts(),
            isPhoneVerified = meta[0] as Boolean,
            verifiedPhoneE164 = meta[1] as String?,
            statusBannerMessage = meta[2] as String?,
            errorBannerMessage = meta[3] as String?,
            isAdvancedMode = prefs[0] as Boolean,
            isAiAdvisorEnabled = prefs[1] as Boolean,
            selectedLanguageCode = prefs[2] as String,
            themeMode = prefs[3] as CinematicThemeMode,
            glassEffectPreference = prefs[4] as GlassEffectPreference,
            debts = debtData.first,
            debtStrategy = debtData.second,
            extraMonthlyPaymentMinor = debtData.third
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        FinanceDashboardState(
            userId = currentUserId,
            categories = DefaultCategories.toFinancialCategories(currentUserId)
        )
    )

    private fun friendlyError(error: Throwable?, fallback: String): String {
        val raw = error?.message?.trim().orEmpty()
        if (raw.isEmpty()) return fallback
        val lower = raw.lowercase()
        return when {
            lower.contains("firestore") ||
                lower.contains("firebase") ||
                lower.contains("permission_denied") ||
                lower.contains("missing or insufficient permissions") ||
                lower.contains("unauthenticated") ||
                lower.contains("unavailable") ||
                lower.contains("deadline_exceeded") -> fallback
            else -> raw
        }
    }

    fun clearBanners() {
        _statusMessage.value = null
        _errorMessage.value = null
    }

    fun selectTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferencesManager.setSelectedTheme(theme)
            _statusMessage.value = "Theme switched to ${theme.displayName}"
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setDarkMode(enabled)
        }
    }

    fun setThemeMode(mode: CinematicThemeMode) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
            _statusMessage.value = "Appearance mode: ${mode.displayName}"
        }
    }

    fun setGlassEffectPreference(preference: GlassEffectPreference) {
        viewModelScope.launch {
            preferencesManager.setGlassEffectPreference(preference)
            _statusMessage.value = "Liquid Glass quality: ${preference.displayName}"
        }
    }

    fun toggleHideSensitiveAmounts() {
        viewModelScope.launch {
            preferencesManager.setHideSensitiveAmounts(!hideSensitiveAmounts.value)
        }
    }

    fun updateBiometricSettings(enabled: Boolean, timeoutMinutes: Int) {
        viewModelScope.launch {
            preferencesManager.setBiometricLockEnabled(enabled)
            preferencesManager.setInactivityTimeoutMinutes(timeoutMinutes)
            _statusMessage.value = if (enabled) {
                "Biometric lock enabled (${timeoutMinutes}m timeout)"
            } else {
                "Biometric lock disabled"
            }
        }
    }

    fun setMasterNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setMasterNotificationsEnabled(enabled)
            repository.notificationManager.setMasterEnabled(enabled)
        }
    }

    fun setCategoryNotifications(category: NotificationCategory, enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setCategoryNotificationEnabled(category, enabled)
            repository.notificationManager.setCategoryEnabled(category, enabled)
        }
    }

    fun setAdvancedMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAdvancedMode(enabled)
        }
    }

    fun setAiAdvisorEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAiAdvisorEnabled(enabled)
        }
    }

    fun setSelectedLanguageCode(code: String) {
        viewModelScope.launch {
            preferencesManager.setSelectedLanguageCode(code)
            _statusMessage.value = if (code == "hi") "भाषा: हिन्दी" else "Language: English"
        }
    }

    fun deleteAllAccountData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            clearBanners()
            val currentPhone = verifiedPhoneE164.value
            preferencesManager.releaseVerifiedPhone(currentUserId)
            repository.deleteAllUserDataAndReleasePhone(currentUserId, currentPhone)
            _statusMessage.value = "Account financial data deleted & phone ownership released."
            onComplete()
        }
    }

    fun verifyAndClaimPhone(e164Phone: String, displayName: String) {
        viewModelScope.launch {
            clearBanners()
            val localClaim = preferencesManager.recordVerifiedPhone(currentUserId, e164Phone)
            if (localClaim.isFailure) {
                _errorMessage.value = friendlyError(localClaim.exceptionOrNull(), "Unable to verify phone ownership.")
                return@launch
            }
            val cloudClaim = repository.registerVerifiedPhoneInCloud(currentUserId, e164Phone, displayName)
            if (cloudClaim.isFailure) {
                _errorMessage.value = friendlyError(
                    cloudClaim.exceptionOrNull(),
                    "Phone verified on this device. Account sync will complete when online."
                )
            } else {
                _statusMessage.value = "Phone verified & linked to your account ($e164Phone)"
            }
        }
    }

    fun addTransaction(
        title: String,
        amountInrInput: String,
        type: TransactionType,
        category: FinancialCategory,
        timestampMillis: Long,
        notes: String?
    ) {
        viewModelScope.launch {
            clearBanners()
            val amountMinor = FinancialEngine.parseInrMajorToMinor(amountInrInput)
            if (amountMinor == null) {
                _errorMessage.value = "Enter a valid INR amount (e.g., 1250.50)."
                return@launch
            }
            val result = repository.addTransaction(
                title = title,
                amountMinor = amountMinor,
                type = type,
                categoryId = category.id,
                categoryName = category.name,
                timestampMillis = timestampMillis,
                notes = notes
            )
            result.fold(
                onSuccess = {
                    _statusMessage.value = "Added ${type.name.lowercase()} of ${FinancialEngine.formatIndianCurrency(amountMinor)}"
                },
                onFailure = { err ->
                    _errorMessage.value = friendlyError(err, "Unable to save transaction. Please try again.")
                }
            )
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            repository.deleteTransaction(transactionId)
        }
    }

    fun addCustomCategory(name: String) {
        viewModelScope.launch {
            clearBanners()
            val result = repository.addCustomCategory(
                name = name,
                existingCategories = dashboardState.value.categories
            )
            result.fold(
                onSuccess = { _statusMessage.value = "Created custom category '${it.name}'" },
                onFailure = { _errorMessage.value = friendlyError(it, "Could not add category.") }
            )
        }
    }

    fun saveCategoryBudget(category: FinancialCategory, limitInrInput: String) {
        viewModelScope.launch {
            clearBanners()
            val limitMinor = FinancialEngine.parseInrMajorToMinor(limitInrInput)
            if (limitMinor == null) {
                _errorMessage.value = "Enter a valid monthly budget amount in INR."
                return@launch
            }
            val result = repository.setCategoryBudget(
                categoryId = category.id,
                categoryName = category.name,
                monthKey = repository.currentMonthKey(),
                limitMinor = limitMinor
            )
            result.fold(
                onSuccess = { _statusMessage.value = "Budget set for ${category.name}: ${FinancialEngine.formatIndianCurrency(limitMinor)}" },
                onFailure = { _errorMessage.value = friendlyError(it, "Unable to save budget. Please try again.") }
            )
        }
    }

    fun addSavingsGoal(title: String, targetInrInput: String, initialSavedInrInput: String, targetDateMillis: Long) {
        viewModelScope.launch {
            clearBanners()
            val targetMinor = FinancialEngine.parseInrMajorToMinor(targetInrInput)
            if (targetMinor == null) {
                _errorMessage.value = "Enter a valid savings target in INR."
                return@launch
            }
            val savedMinor = if (initialSavedInrInput.isBlank()) {
                0L
            } else {
                FinancialEngine.parseInrMajorToMinor(initialSavedInrInput) ?: 0L
            }
            val result = repository.createSavingsGoal(
                title = title,
                targetMinor = targetMinor,
                initialSavedMinor = savedMinor,
                targetDateMillis = targetDateMillis
            )
            result.fold(
                onSuccess = { _statusMessage.value = "Savings goal '${it.title}' created!" },
                onFailure = { _errorMessage.value = friendlyError(it, "Unable to create savings goal.") }
            )
        }
    }

    fun contributeToGoal(goal: SavingsGoal, contributionInrInput: String) {
        viewModelScope.launch {
            clearBanners()
            val amountMinor = FinancialEngine.parseInrMajorToMinor(contributionInrInput)
            if (amountMinor == null) {
                _errorMessage.value = "Enter a valid contribution amount."
                return@launch
            }
            val result = repository.contributeToSavingsGoal(goal, amountMinor)
            result.fold(
                onSuccess = { updated ->
                    _statusMessage.value = "Updated '${updated.title}' to ${updated.progressPercentage}% (${FinancialEngine.formatIndianCurrency(updated.savedMinor)})"
                },
                onFailure = { _errorMessage.value = friendlyError(it, "Unable to update savings goal.") }
            )
        }
    }

    fun addRecurringBill(
        title: String,
        amountInrInput: String,
        category: FinancialCategory,
        frequency: BillFrequency,
        nextDueDateMillis: Long
    ) {
        viewModelScope.launch {
            clearBanners()
            val amountMinor = FinancialEngine.parseInrMajorToMinor(amountInrInput)
            if (amountMinor == null) {
                _errorMessage.value = "Enter a valid bill amount in INR."
                return@launch
            }
            val result = repository.createRecurringBill(
                title = title,
                amountMinor = amountMinor,
                categoryId = category.id,
                categoryName = category.name,
                frequency = frequency,
                nextDueDateMillis = nextDueDateMillis
            )
            result.fold(
                onSuccess = { _statusMessage.value = "Recurring bill '${it.title}' scheduled (${frequency.label})" },
                onFailure = { _errorMessage.value = friendlyError(it, "Unable to schedule recurring bill.") }
            )
        }
    }

    fun payRecurringBill(bill: RecurringBill) {
        viewModelScope.launch {
            clearBanners()
            val result = repository.markBillAsPaid(bill)
            result.fold(
                onSuccess = { (updatedBill, _) ->
                    _statusMessage.value = "Paid '${updatedBill.title}' & advanced next due date!"
                },
                onFailure = { err ->
                    _errorMessage.value = friendlyError(err, "Could not mark bill as paid.")
                }
            )
        }
    }

    fun sendRoommateRequest(
        senderName: String,
        recipientName: String,
        recipientPhoneE164: String
    ) {
        viewModelScope.launch {
            clearBanners()
            val result = repository.sendRoommateConnectionRequest(
                senderUid = currentUserId,
                senderName = senderName,
                senderVerifiedPhoneE164 = verifiedPhoneE164.value,
                isSenderPhoneVerified = isPhoneVerified.value,
                recipientName = recipientName,
                recipientPhoneE164 = recipientPhoneE164
            )
            result.fold(
                onSuccess = { _statusMessage.value = "Connection request sent to ${it.recipientName} (${it.recipientPhone})" },
                onFailure = { _errorMessage.value = friendlyError(it, "Unable to send connection request.") }
            )
        }
    }

    fun respondToConnectionRequest(request: RoommateConnectionRequest, accept: Boolean) {
        viewModelScope.launch {
            clearBanners()
            val target = if (accept) ConnectionRequestStatus.ACCEPTED else ConnectionRequestStatus.DECLINED
            val result = repository.respondToRoommateConnectionRequest(
                request = request,
                actorUid = currentUserId,
                actorVerifiedPhoneE164 = verifiedPhoneE164.value,
                isActorPhoneVerified = isPhoneVerified.value,
                targetStatus = target
            )
            result.fold(
                onSuccess = {
                    _statusMessage.value = if (accept) {
                        "Accepted roommate request from ${request.senderName} and added to Household!"
                    } else {
                        "Declined request from ${request.senderName}."
                    }
                },
                onFailure = { _errorMessage.value = friendlyError(it, "Unable to update request status.") }
            )
        }
    }

    fun addHouseholdMember(name: String, phoneE164: String, upiVpa: String?) {
        viewModelScope.launch {
            clearBanners()
            if (!isPhoneVerified.value) {
                _errorMessage.value = "Verify your own phone number first before adding household members."
                return@launch
            }
            val result = repository.addHouseholdMemberManually(name, phoneE164, upiVpa)
            result.fold(
                onSuccess = { _statusMessage.value = "Added ${it.name} to Household Ledger" },
                onFailure = { _errorMessage.value = friendlyError(it, "Unable to add household member.") }
            )
        }
    }

    fun createSharedExpense(
        title: String,
        totalInrInput: String,
        paidByName: String,
        splitMethod: SplitMethod,
        participants: List<ParticipantSplit>
    ) {
        viewModelScope.launch {
            clearBanners()
            val totalMinor = FinancialEngine.parseInrMajorToMinor(totalInrInput)
            if (totalMinor == null) {
                _errorMessage.value = "Enter a valid total expense amount in INR."
                return@launch
            }
            val result = repository.createSharedExpense(
                title = title,
                totalAmountMinor = totalMinor,
                paidByMemberId = currentUserId,
                paidByName = paidByName.ifBlank { "Me" },
                splitMethod = splitMethod,
                participants = participants
            )
            result.fold(
                onSuccess = { _statusMessage.value = "Recorded shared expense '${it.title}' (${splitMethod.label})" },
                onFailure = { _errorMessage.value = friendlyError(it, "Unable to create shared expense.") }
            )
        }
    }

    fun confirmParticipantSettled(expense: SharedExpense, participantId: String) {
        viewModelScope.launch {
            clearBanners()
            val result = repository.markParticipantSplitSettled(expense, participantId)
            result.fold(
                onSuccess = { _statusMessage.value = "Recorded verified settlement for '${expense.title}'" },
                onFailure = { _errorMessage.value = friendlyError(it, "Could not update settlement status.") }
            )
        }
    }

    fun exportTransactionsCsv(): String {
        val csv = CsvDataPortability.exportToCsv(dashboardState.value.transactions)
        _lastCsvExport.value = csv
        _statusMessage.value = "Exported ${dashboardState.value.transactions.size} transactions to CSV."
        return csv
    }

    fun importTransactionsCsv(csvContent: String) {
        viewModelScope.launch {
            clearBanners()
            val summary = CsvDataPortability.importFromCsv(csvContent, currentUserId)
            for (tx in summary.importedTransactions) {
                repository.addTransaction(
                    title = tx.title,
                    amountMinor = tx.amountMinor,
                    type = TransactionType.valueOf(tx.type),
                    categoryId = tx.categoryId,
                    categoryName = tx.categoryName,
                    timestampMillis = tx.timestampMillis,
                    notes = tx.notes
                )
            }
            if (summary.malformedRowErrors.isNotEmpty() && summary.importedTransactions.isEmpty()) {
                _errorMessage.value = "CSV Import Error: ${summary.malformedRowErrors.first()}"
            } else {
                _statusMessage.value =
                    "Imported ${summary.importedTransactions.size} rows (${summary.skippedDuplicatesCount} duplicates skipped, ${summary.malformedRowErrors.size} malformed)."
            }
        }
    }

    fun setDebtStrategy(strategy: com.example.data.DebtPayoffStrategy) {
        _debtStrategy.value = strategy
    }

    fun setExtraMonthlyPayment(extraMinor: Long) {
        _extraMonthlyPaymentMinor.value = extraMinor.coerceAtLeast(0L)
    }

    fun saveDebt(
        id: String = "",
        name: String,
        balanceMajorStr: String,
        interestRateStr: String,
        minPaymentMajorStr: String
    ) {
        val balanceMinor = FinancialEngine.parseInrMajorToMinor(balanceMajorStr) ?: 0L
        val minPaymentMinor = FinancialEngine.parseInrMajorToMinor(minPaymentMajorStr) ?: 0L
        val interestRate = interestRateStr.toDoubleOrNull() ?: 0.0

        if (name.isBlank() || balanceMinor <= 0L) {
            _errorMessage.value = "Please enter a valid debt name and current balance."
            return
        }

        viewModelScope.launch {
            clearBanners()
            val debt = com.example.data.DebtItem(
                id = id,
                userId = currentUserId,
                name = name,
                balanceMinor = balanceMinor,
                interestRatePercent = interestRate,
                minimumPaymentMinor = minPaymentMinor
            )
            val result = repository.saveDebt(debt, currentUserId)
            result.fold(
                onSuccess = { _statusMessage.value = "Saved debt '${debt.name}'" },
                onFailure = { _errorMessage.value = friendlyError(it, "Could not save debt.") }
            )
        }
    }

    fun deleteDebt(id: String) {
        viewModelScope.launch {
            clearBanners()
            val result = repository.deleteDebt(id, currentUserId)
            result.fold(
                onSuccess = { _statusMessage.value = "Debt removed." },
                onFailure = { _errorMessage.value = friendlyError(it, "Could not delete debt.") }
            )
        }
    }
}
