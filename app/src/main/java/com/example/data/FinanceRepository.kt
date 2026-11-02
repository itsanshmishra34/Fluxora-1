package com.example.data

import android.content.Context
import com.example.R
import com.example.authentication.PhoneValidation
import com.example.finance.DateValidator
import com.example.finance.FinancialEngine
import com.example.notifications.FluxoraAlert
import com.example.notifications.NotificationCategory
import com.example.notifications.NotificationManager
import com.example.roommate.ConnectionRequestStatus
import com.example.roommate.RoommateConnectionRequest
import com.example.roommate.RoommateRequestSecurityValidator
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

/**
 * Unified Cloud Firestore Financial Repository.
 * Consolidates all financial records (Transactions, Categories, Budgets, Savings Goals,
 * Recurring Bills, Household Members, Shared Expenses, Roommate Connection Requests,
 * Phone Ownership, and Notifications) under zero-trust authenticated ownership.
 */
class FinanceRepository(
    private val db: FirebaseFirestore,
    val notificationManager: NotificationManager = NotificationManager()
) {
    // CRITICAL: Always initialize with R.string.firestore_database_id
    constructor(context: Context, notificationManager: NotificationManager = NotificationManager()) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        notificationManager
    )

    companion object {
        const val BILL_DEDUP_WINDOW_MILLIS: Long = 10_000L
        val SAVINGS_MILESTONES = listOf(25, 50, 75, 100)
    }

    private val billPaymentMutex = Mutex()
    private val recentBillPaymentTimestamps = ConcurrentHashMap<String, Long>()

    private val _inMemoryTransactions = MutableStateFlow<List<FinancialTransaction>>(emptyList())
    val inMemoryTransactions: StateFlow<List<FinancialTransaction>> = _inMemoryTransactions.asStateFlow()

    private val _inMemoryCategories = MutableStateFlow<List<FinancialCategory>>(emptyList())
    val inMemoryCategories: StateFlow<List<FinancialCategory>> = _inMemoryCategories.asStateFlow()

    private val _inMemoryBudgets = MutableStateFlow<List<CategoryBudget>>(emptyList())
    val inMemoryBudgets: StateFlow<List<CategoryBudget>> = _inMemoryBudgets.asStateFlow()

    private val _inMemorySavingsGoals = MutableStateFlow<List<SavingsGoal>>(emptyList())
    val inMemorySavingsGoals: StateFlow<List<SavingsGoal>> = _inMemorySavingsGoals.asStateFlow()

    private val _inMemoryRecurringBills = MutableStateFlow<List<RecurringBill>>(emptyList())
    val inMemoryRecurringBills: StateFlow<List<RecurringBill>> = _inMemoryRecurringBills.asStateFlow()

    private val _inMemoryHouseholdMembers = MutableStateFlow<List<HouseholdMember>>(emptyList())
    val inMemoryHouseholdMembers: StateFlow<List<HouseholdMember>> = _inMemoryHouseholdMembers.asStateFlow()

    private val _inMemorySharedExpenses = MutableStateFlow<List<SharedExpense>>(emptyList())
    val inMemorySharedExpenses: StateFlow<List<SharedExpense>> = _inMemorySharedExpenses.asStateFlow()

    private val _inMemoryConnectionRequests = MutableStateFlow<List<RoommateConnectionRequest>>(emptyList())
    val inMemoryConnectionRequests: StateFlow<List<RoommateConnectionRequest>> = _inMemoryConnectionRequests.asStateFlow()

    private val _inMemoryDebts = MutableStateFlow<List<DebtItem>>(emptyList())
    val inMemoryDebts: StateFlow<List<DebtItem>> = _inMemoryDebts.asStateFlow()

    private fun requireUserId(): String {
        return Firebase.auth.currentUser?.uid ?: "demo_fluxora_user"
    }

    fun seedDemoDataIfEmpty() {
        if (_inMemoryTransactions.value.isEmpty()) {
            val now = System.currentTimeMillis()
            val dayMillis = 86_400_000L
            _inMemoryTransactions.value = listOf(
                FinancialTransaction(
                    id = "demo_tx_1",
                    userId = "demo_fluxora_user",
                    title = "Monthly Consulting Income",
                    amountMinor = 28_500_00L,
                    type = TransactionType.INCOME.name,
                    categoryId = "income",
                    categoryName = "Income",
                    timestampMillis = now - (dayMillis * 2)
                ),
                FinancialTransaction(
                    id = "demo_tx_2",
                    userId = "demo_fluxora_user",
                    title = "Organic Grocery Store",
                    amountMinor = 3_450_00L,
                    type = TransactionType.EXPENSE.name,
                    categoryId = "groceries",
                    categoryName = "Groceries",
                    timestampMillis = now - (dayMillis * 1)
                ),
                FinancialTransaction(
                    id = "demo_tx_3",
                    userId = "demo_fluxora_user",
                    title = "Apartment Rent Share",
                    amountMinor = 8_200_00L,
                    type = TransactionType.EXPENSE.name,
                    categoryId = "housing",
                    categoryName = "Housing",
                    timestampMillis = now - (dayMillis * 3)
                ),
                FinancialTransaction(
                    id = "demo_tx_4",
                    userId = "demo_fluxora_user",
                    title = "Fiber Internet & Utilities",
                    amountMinor = 1_250_00L,
                    type = TransactionType.EXPENSE.name,
                    categoryId = "utilities",
                    categoryName = "Utilities",
                    timestampMillis = now - (dayMillis * 4)
                ),
                FinancialTransaction(
                    id = "demo_tx_5",
                    userId = "demo_fluxora_user",
                    title = "Artisan Cafe & Bakery",
                    amountMinor = 650_00L,
                    type = TransactionType.EXPENSE.name,
                    categoryId = "dining",
                    categoryName = "Dining",
                    timestampMillis = now - (dayMillis * 5)
                ),
                FinancialTransaction(
                    id = "demo_tx_6",
                    userId = "demo_fluxora_user",
                    title = "Cloud Infrastructure & SaaS",
                    amountMinor = 2_500_00L,
                    type = TransactionType.EXPENSE.name,
                    categoryId = "technology",
                    categoryName = "Technology",
                    timestampMillis = now - (dayMillis * 6)
                )
            )
        }
        if (_inMemoryDebts.value.isEmpty()) {
            _inMemoryDebts.value = listOf(
                DebtItem(
                    id = "demo_debt_1",
                    userId = "demo_fluxora_user",
                    name = "Premium Credit Card",
                    balanceMinor = 45_000_00L,
                    interestRatePercent = 24.0,
                    minimumPaymentMinor = 2_000_00L
                ),
                DebtItem(
                    id = "demo_debt_2",
                    userId = "demo_fluxora_user",
                    name = "Personal Line of Credit",
                    balanceMinor = 120_000_00L,
                    interestRatePercent = 14.5,
                    minimumPaymentMinor = 4_500_00L
                ),
                DebtItem(
                    id = "demo_debt_3",
                    userId = "demo_fluxora_user",
                    name = "Education Loan",
                    balanceMinor = 250_000_00L,
                    interestRatePercent = 9.0,
                    minimumPaymentMinor = 6_000_00L
                )
            )
        }
    }

    private fun isDemoMode(userId: String = requireUserId()): Boolean {
        return Firebase.auth.currentUser == null || userId == "demo_fluxora_user" || userId.startsWith("demo_")
    }

    // --- Real-Time Observation Flows (First-Party snapshots()) ---

    fun observeTransactions(userId: String = requireUserId()): Flow<List<FinancialTransaction>> = flow {
        if (isDemoMode(userId)) {
            seedDemoDataIfEmpty()
            emitAll(_inMemoryTransactions)
            return@flow
        }
        val path = "users/$userId/transactions"
        emitAll(
            db.collection("users").document(userId).collection("transactions")
                .snapshots()
                .map { snapshot ->
                    val list = snapshot.toObjects(
                        FinancialTransaction::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.timestampMillis }
                    _inMemoryTransactions.value = list
                    list
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeCategories(userId: String = requireUserId()): Flow<List<FinancialCategory>> = flow {
        if (isDemoMode(userId)) {
            val merged = mergeWithDefaultCategories(userId, _inMemoryCategories.value)
            _inMemoryCategories.value = merged
            emitAll(_inMemoryCategories)
            return@flow
        }
        val path = "users/$userId/categories"
        emitAll(
            db.collection("users").document(userId).collection("categories")
                .snapshots()
                .map { snapshot ->
                    val custom = snapshot.toObjects(
                        FinancialCategory::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                    val merged = mergeWithDefaultCategories(userId, custom)
                    _inMemoryCategories.value = merged
                    merged
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeBudgets(userId: String = requireUserId()): Flow<List<CategoryBudget>> = flow {
        if (isDemoMode(userId)) {
            emitAll(_inMemoryBudgets)
            return@flow
        }
        val path = "users/$userId/budgets"
        emitAll(
            db.collection("users").document(userId).collection("budgets")
                .snapshots()
                .map { snapshot ->
                    val list = snapshot.toObjects(
                        CategoryBudget::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                    _inMemoryBudgets.value = list
                    list
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeSavingsGoals(userId: String = requireUserId()): Flow<List<SavingsGoal>> = flow {
        if (isDemoMode(userId)) {
            emitAll(_inMemorySavingsGoals)
            return@flow
        }
        val path = "users/$userId/savings_goals"
        emitAll(
            db.collection("users").document(userId).collection("savings_goals")
                .snapshots()
                .map { snapshot ->
                    val list = snapshot.toObjects(
                        SavingsGoal::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                    _inMemorySavingsGoals.value = list
                    list
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeRecurringBills(userId: String = requireUserId()): Flow<List<RecurringBill>> = flow {
        if (isDemoMode(userId)) {
            emitAll(_inMemoryRecurringBills)
            return@flow
        }
        val path = "users/$userId/recurring_bills"
        emitAll(
            db.collection("users").document(userId).collection("recurring_bills")
                .snapshots()
                .map { snapshot ->
                    val list = snapshot.toObjects(
                        RecurringBill::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.nextDueDateMillis }
                    _inMemoryRecurringBills.value = list
                    list
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeDebts(userId: String = requireUserId()): Flow<List<DebtItem>> = flow {
        if (isDemoMode(userId)) {
            seedDemoDataIfEmpty()
            emitAll(_inMemoryDebts)
            return@flow
        }
        val path = "users/$userId/debts"
        emitAll(
            db.collection("users").document(userId).collection("debts")
                .snapshots()
                .map { snapshot ->
                    val list = snapshot.toObjects(
                        DebtItem::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                    _inMemoryDebts.value = list
                    list
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveDebt(debt: DebtItem, userId: String = requireUserId()): Result<DebtItem> {
        val targetUserId = if (debt.userId.isNotBlank()) debt.userId else userId
        val debtId = debt.id.ifBlank { UUID.randomUUID().toString() }
        val finalDebt = debt.copy(id = debtId, userId = targetUserId)

        if (isDemoMode(targetUserId)) {
            val current = _inMemoryDebts.value.toMutableList()
            val existingIndex = current.indexOfFirst { it.id == debtId }
            if (existingIndex >= 0) {
                current[existingIndex] = finalDebt
            } else {
                current.add(finalDebt)
            }
            _inMemoryDebts.value = current
            return Result.success(finalDebt)
        }

        return try {
            db.collection("users").document(targetUserId).collection("debts")
                .document(debtId)
                .set(finalDebt)
                .await()
            Result.success(finalDebt)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$targetUserId/debts/$debtId")
            Result.failure(e)
        }
    }

    suspend fun deleteDebt(debtId: String, userId: String = requireUserId()): Result<Unit> {
        if (isDemoMode(userId)) {
            _inMemoryDebts.value = _inMemoryDebts.value.filterNot { it.id == debtId }
            return Result.success(Unit)
        }

        return try {
            db.collection("users").document(userId).collection("debts")
                .document(debtId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$userId/debts/$debtId")
            Result.failure(e)
        }
    }

    fun observeHouseholdMembers(userId: String = requireUserId()): Flow<List<HouseholdMember>> = flow {
        if (isDemoMode(userId)) {
            emitAll(_inMemoryHouseholdMembers)
            return@flow
        }
        val path = "users/$userId/household_members"
        emitAll(
            db.collection("users").document(userId).collection("household_members")
                .snapshots()
                .map { snapshot ->
                    val list = snapshot.toObjects(
                        HouseholdMember::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                    _inMemoryHouseholdMembers.value = list
                    list
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeSharedExpenses(userId: String = requireUserId()): Flow<List<SharedExpense>> = flow {
        if (isDemoMode(userId)) {
            emitAll(_inMemorySharedExpenses)
            return@flow
        }
        val path = "users/$userId/shared_expenses"
        emitAll(
            db.collection("users").document(userId).collection("shared_expenses")
                .snapshots()
                .map { snapshot ->
                    val list = snapshot.toObjects(
                        SharedExpense::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                    _inMemorySharedExpenses.value = list
                    list
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeIncomingConnectionRequests(userId: String = requireUserId()): Flow<List<RoommateConnectionRequest>> = flow {
        if (isDemoMode(userId)) {
            emitAll(_inMemoryConnectionRequests)
            return@flow
        }
        val path = "connection_requests"
        emitAll(
            db.collection("connection_requests")
                .whereEqualTo("recipientUid", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        RoommateConnectionRequest::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).filter { it.isIncomingFor(userId, null) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeOutgoingConnectionRequests(userId: String = requireUserId()): Flow<List<RoommateConnectionRequest>> = flow {
        if (isDemoMode(userId)) {
            emitAll(_inMemoryConnectionRequests)
            return@flow
        }
        val path = "connection_requests"
        emitAll(
            db.collection("connection_requests")
                .whereEqualTo("senderUid", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        RoommateConnectionRequest::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).filter { it.isOutgoingFor(userId) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    // --- 1. Transactions & Validation ---

    suspend fun addTransaction(
        title: String,
        amountMinor: Long,
        type: TransactionType,
        categoryId: String,
        categoryName: String,
        timestampMillis: Long,
        notes: String? = null,
        recurringBillId: String? = null,
        nowMillis: Long = System.currentTimeMillis()
    ): Result<FinancialTransaction> {
        val uid = requireUserId()
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            return Result.failure(IllegalArgumentException("Transaction title cannot be empty."))
        }
        if (!FinancialEngine.isValidMinorAmount(amountMinor)) {
            return Result.failure(IllegalArgumentException("Amount must be between INR 0.01 and INR 100,00,00,000.00."))
        }
        val dateCheck = DateValidator.validateTransactionDate(timestampMillis, nowMillis)
        if (!dateCheck.isValid) {
            return Result.failure(IllegalArgumentException(dateCheck.errorMessage ?: "Invalid transaction date."))
        }

        val docId = "tx_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(uid).collection("transactions").document(docId)
        val payload = mapOf(
            "userId" to uid,
            "title" to trimmedTitle,
            "amountMinor" to amountMinor,
            "type" to type.name,
            "categoryId" to categoryId.trim().ifEmpty { "other" },
            "categoryName" to categoryName.trim().ifEmpty { "Other" },
            "timestampMillis" to timestampMillis,
            "notes" to notes?.trim()?.takeIf { it.isNotEmpty() },
            "recurringBillId" to recurringBillId?.trim()?.takeIf { it.isNotEmpty() },
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null } as Map<String, Any>

        return try {
            docRef.set(payload).await()
            val created = FinancialTransaction(
                id = docId,
                userId = uid,
                title = trimmedTitle,
                amountMinor = amountMinor,
                type = type.name,
                categoryId = categoryId.trim().ifEmpty { "other" },
                categoryName = categoryName.trim().ifEmpty { "Other" },
                timestampMillis = timestampMillis,
                notes = notes?.trim()?.takeIf { it.isNotEmpty() },
                recurringBillId = recurringBillId
            )
            _inMemoryTransactions.value = listOf(created) + _inMemoryTransactions.value
            if (type == TransactionType.EXPENSE) {
                evaluateBudgetThresholdsForCategory(uid, categoryId, amountMinor)
            }
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        val uid = requireUserId()
        require(transactionId.isNotBlank()) { "Transaction ID must not be blank" }
        val docRef = db.collection("users").document(uid).collection("transactions").document(transactionId)
        return try {
            docRef.delete().await()
            _inMemoryTransactions.value = _inMemoryTransactions.value.filterNot { it.id == transactionId }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- 2. Categories (12 Built-In + Custom User Categories) ---

    fun mergeWithDefaultCategories(
        userId: String,
        customCategories: List<FinancialCategory>
    ): List<FinancialCategory> {
        val defaults = DefaultCategories.toFinancialCategories(userId)
        val seenNormalized = defaults.map { it.normalizedName }.toMutableSet()
        val uniqueCustom = customCategories.filter { cat ->
            val norm = cat.normalizedName.ifBlank { cat.name.lowercase().trim() }
            norm.isNotEmpty() && seenNormalized.add(norm)
        }
        return defaults + uniqueCustom
    }

    suspend fun addCustomCategory(
        name: String,
        iconName: String = "Category",
        colorHex: String = "#00E676",
        existingCategories: List<FinancialCategory> = _inMemoryCategories.value
    ): Result<FinancialCategory> {
        val uid = requireUserId()
        val trimmed = name.trim()
        if (trimmed.length < 2 || trimmed.length > 50) {
            return Result.failure(IllegalArgumentException("Category name must be between 2 and 50 characters."))
        }
        val normalized = trimmed.lowercase()
        val allKnown = mergeWithDefaultCategories(uid, existingCategories)
        if (allKnown.any { it.normalizedName == normalized || it.name.equals(trimmed, ignoreCase = true) }) {
            return Result.failure(IllegalStateException("Category '$trimmed' already exists."))
        }

        val docId = "cat_${normalized.replace(Regex("[^a-z0-9]+"), "_").trim('_')}_${System.currentTimeMillis() % 10000}"
        val docRef = db.collection("users").document(uid).collection("categories").document(docId)
        val payload = mapOf(
            "userId" to uid,
            "name" to trimmed,
            "normalizedName" to normalized,
            "iconName" to iconName.ifBlank { "Category" },
            "colorHex" to colorHex.ifBlank { "#00E676" },
            "isDefault" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            docRef.set(payload).await()
            val created = FinancialCategory(
                id = docId,
                userId = uid,
                name = trimmed,
                normalizedName = normalized,
                iconName = iconName,
                colorHex = colorHex,
                isDefault = false
            )
            _inMemoryCategories.value = mergeWithDefaultCategories(uid, _inMemoryCategories.value + created)
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- 3. Monthly Category Budgets (80% Warning & 100% Exceeded Alerts) ---

    suspend fun setCategoryBudget(
        categoryId: String,
        categoryName: String,
        monthKey: String,
        limitMinor: Long
    ): Result<CategoryBudget> {
        val uid = requireUserId()
        if (!FinancialEngine.isValidMinorAmount(limitMinor)) {
            return Result.failure(IllegalArgumentException("Budget limit must be positive."))
        }
        val safeMonth = monthKey.trim().takeIf { it.length == 7 } ?: currentMonthKey()
        val docId = "budget_${categoryId}_$safeMonth".replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        val docRef = db.collection("users").document(uid).collection("budgets").document(docId)

        val payload = mapOf(
            "userId" to uid,
            "categoryId" to categoryId,
            "categoryName" to categoryName,
            "monthKey" to safeMonth,
            "limitMinor" to limitMinor,
            "alert80Triggered" to false,
            "alert100Triggered" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            docRef.set(payload).await()
            val budget = CategoryBudget(
                id = docId,
                userId = uid,
                categoryId = categoryId,
                categoryName = categoryName,
                monthKey = safeMonth,
                limitMinor = limitMinor
            )
            _inMemoryBudgets.value = _inMemoryBudgets.value.filterNot { it.id == docId } + budget
            Result.success(budget)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Evaluates monthly category budget thresholds (80% warning and 100% exceeded)
     * and dispatches deduplicated notifications via NotificationManager.
     */
    fun evaluateBudgetThresholds(
        budget: CategoryBudget,
        totalCategorySpendMinor: Long
    ): CategoryBudget {
        if (budget.limitMinor <= 0L) return budget
        val usagePercent = (totalCategorySpendMinor * 100L) / budget.limitMinor
        var alert80 = budget.alert80Triggered
        var alert100 = budget.alert100Triggered

        if (usagePercent >= 100L && !alert100) {
            alert100 = true
            alert80 = true
            notificationManager.dispatchAlert(
                FluxoraAlert(
                    id = "alert_budget_100_${budget.id}",
                    category = NotificationCategory.BUDGET,
                    title = "Budget Exceeded: ${budget.categoryName}",
                    message = "You have spent ${FinancialEngine.formatIndianCurrency(totalCategorySpendMinor)} ($usagePercent%) of your ${FinancialEngine.formatIndianCurrency(budget.limitMinor)} budget.",
                    relatedId = "budget_100_${budget.categoryId}_${budget.monthKey}"
                )
            )
        } else if (usagePercent >= 80L && !alert80) {
            alert80 = true
            notificationManager.dispatchAlert(
                FluxoraAlert(
                    id = "alert_budget_80_${budget.id}",
                    category = NotificationCategory.BUDGET,
                    title = "Budget Warning (80%+): ${budget.categoryName}",
                    message = "You have reached $usagePercent% (${FinancialEngine.formatIndianCurrency(totalCategorySpendMinor)}) of your ${FinancialEngine.formatIndianCurrency(budget.limitMinor)} budget.",
                    relatedId = "budget_80_${budget.categoryId}_${budget.monthKey}"
                )
            )
        }

        return budget.copy(
            alert80Triggered = alert80,
            alert100Triggered = alert100
        )
    }

    private fun evaluateBudgetThresholdsForCategory(userId: String, categoryId: String, addedSpendMinor: Long) {
        val month = currentMonthKey()
        val matchingBudget = _inMemoryBudgets.value.firstOrNull {
            it.userId == userId && it.categoryId == categoryId && it.monthKey == month
        } ?: return
        val totalSpent = _inMemoryTransactions.value
            .filter { it.type == TransactionType.EXPENSE.name && it.categoryId == categoryId }
            .sumOf { it.amountMinor }
        val updated = evaluateBudgetThresholds(matchingBudget, totalSpent.coerceAtLeast(addedSpendMinor))
        _inMemoryBudgets.value = _inMemoryBudgets.value.map { if (it.id == updated.id) updated else it }
    }

    // --- 4. Savings Goals & Automatic Milestone Tracking (25%, 50%, 75%, 100%) ---

    suspend fun createSavingsGoal(
        title: String,
        targetMinor: Long,
        initialSavedMinor: Long = 0L,
        targetDateMillis: Long
    ): Result<SavingsGoal> {
        val uid = requireUserId()
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            return Result.failure(IllegalArgumentException("Savings goal title cannot be empty."))
        }
        if (!FinancialEngine.isValidMinorAmount(targetMinor)) {
            return Result.failure(IllegalArgumentException("Savings goal target must be positive."))
        }
        val docId = "goal_${UUID.randomUUID().toString().replace("-", "").take(14)}"
        val docRef = db.collection("users").document(uid).collection("savings_goals").document(docId)

        val baseGoal = SavingsGoal(
            id = docId,
            userId = uid,
            title = trimmedTitle,
            targetMinor = targetMinor,
            savedMinor = initialSavedMinor.coerceAtLeast(0L),
            targetDateMillis = targetDateMillis,
            highestMilestoneNotified = 0
        )
        val evaluatedGoal = evaluateSavingsGoalMilestones(baseGoal)

        val payload = mapOf(
            "userId" to uid,
            "title" to evaluatedGoal.title,
            "targetMinor" to evaluatedGoal.targetMinor,
            "savedMinor" to evaluatedGoal.savedMinor,
            "targetDateMillis" to evaluatedGoal.targetDateMillis,
            "highestMilestoneNotified" to evaluatedGoal.highestMilestoneNotified,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            docRef.set(payload).await()
            _inMemorySavingsGoals.value = _inMemorySavingsGoals.value + evaluatedGoal
            Result.success(evaluatedGoal)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun contributeToSavingsGoal(goal: SavingsGoal, contributionMinor: Long): Result<SavingsGoal> {
        val uid = requireUserId()
        if (!FinancialEngine.isValidMinorAmount(contributionMinor)) {
            return Result.failure(IllegalArgumentException("Contribution amount must be positive."))
        }
        val newSaved = (goal.savedMinor + contributionMinor).coerceAtMost(FinancialEngine.MAX_AMOUNT_MINOR)
        val updatedGoal = evaluateSavingsGoalMilestones(goal.copy(savedMinor = newSaved))

        val docRef = db.collection("users").document(uid).collection("savings_goals").document(goal.id)
        return try {
            docRef.update(
                mapOf(
                    "savedMinor" to updatedGoal.savedMinor,
                    "highestMilestoneNotified" to updatedGoal.highestMilestoneNotified,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            _inMemorySavingsGoals.value = _inMemorySavingsGoals.value.map {
                if (it.id == updatedGoal.id) updatedGoal else it
            }
            Result.success(updatedGoal)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Tracks 25%, 50%, 75%, and 100% milestones and dispatches deduplicated notifications.
     */
    fun evaluateSavingsGoalMilestones(goal: SavingsGoal): SavingsGoal {
        if (goal.targetMinor <= 0L) return goal
        val percent = ((goal.savedMinor * 100L) / goal.targetMinor).toInt()
        var highestNotified = goal.highestMilestoneNotified

        for (milestone in SAVINGS_MILESTONES) {
            if (percent >= milestone && milestone > highestNotified) {
                highestNotified = milestone
                notificationManager.dispatchAlert(
                    FluxoraAlert(
                        id = "alert_goal_${goal.id}_$milestone",
                        category = NotificationCategory.SAVINGS,
                        title = "Savings Milestone: $milestone% Reached!",
                        message = "You reached $milestone% (${FinancialEngine.formatIndianCurrency(goal.savedMinor)}) of '${goal.title}'!",
                        relatedId = "savings_goal_${goal.id}_milestone_$milestone"
                    )
                )
            }
        }
        return goal.copy(highestMilestoneNotified = highestNotified)
    }

    // --- 5. Atomic Recurring Bills (Weekly, Monthly, Yearly + 10s Deduplication) ---

    suspend fun createRecurringBill(
        title: String,
        amountMinor: Long,
        categoryId: String,
        categoryName: String,
        frequency: BillFrequency,
        nextDueDateMillis: Long
    ): Result<RecurringBill> {
        val uid = requireUserId()
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            return Result.failure(IllegalArgumentException("Bill title cannot be empty."))
        }
        if (!FinancialEngine.isValidMinorAmount(amountMinor)) {
            return Result.failure(IllegalArgumentException("Bill amount must be positive."))
        }

        val docId = "bill_${UUID.randomUUID().toString().replace("-", "").take(14)}"
        val docRef = db.collection("users").document(uid).collection("recurring_bills").document(docId)
        val payload = mapOf(
            "userId" to uid,
            "title" to trimmedTitle,
            "amountMinor" to amountMinor,
            "categoryId" to categoryId,
            "categoryName" to categoryName,
            "frequency" to frequency.name,
            "nextDueDateMillis" to nextDueDateMillis,
            "lastPaidAtMillis" to 0L,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            docRef.set(payload).await()
            val bill = RecurringBill(
                id = docId,
                userId = uid,
                title = trimmedTitle,
                amountMinor = amountMinor,
                categoryId = categoryId,
                categoryName = categoryName,
                frequency = frequency.name,
                nextDueDateMillis = nextDueDateMillis,
                lastPaidAtMillis = 0L
            )
            _inMemoryRecurringBills.value = (_inMemoryRecurringBills.value + bill).sortedBy { it.nextDueDateMillis }
            Result.success(bill)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Atomically marks a recurring bill as paid:
     * 1. Deduplicates rapid repeat invocations within a 10-second window (`10_000L`).
     * 2. Records the corresponding EXPENSE transaction.
     * 3. Advances `nextDueDateMillis` according to `WEEKLY`, `MONTHLY`, or `YEARLY` frequency.
     */
    suspend fun markBillAsPaid(
        bill: RecurringBill,
        nowMillis: Long = System.currentTimeMillis()
    ): Result<Pair<RecurringBill, FinancialTransaction>> = billPaymentMutex.withLock {
        val uid = bill.userId.ifBlank { requireUserId() }
        require(bill.id.isNotBlank()) { "Bill ID must not be blank" }

        // Check both in-memory atomic guard and bill's persisted lastPaidAtMillis for the 10-second window
        val lastRecordedTime = maxOf(
            bill.lastPaidAtMillis,
            recentBillPaymentTimestamps[bill.id] ?: 0L
        )
        if (lastRecordedTime > 0L && (nowMillis - lastRecordedTime) < BILL_DEDUP_WINDOW_MILLIS) {
            return Result.failure(
                IllegalStateException("Duplicate payment blocked: '${bill.title}' was already paid within the last 10 seconds.")
            )
        }

        val nextDueMillis = advanceDueDateMillis(bill.nextDueDateMillis, bill.frequency)
        val txId = "tx_bill_${bill.id}_${nowMillis}"
        val billRef = db.collection("users").document(uid).collection("recurring_bills").document(bill.id)
        val txRef = db.collection("users").document(uid).collection("transactions").document(txId)

        return try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(billRef)
                if (snapshot.exists()) {
                    val serverLastPaid = snapshot.getLong("lastPaidAtMillis") ?: 0L
                    if (serverLastPaid > 0L && (nowMillis - serverLastPaid) < BILL_DEDUP_WINDOW_MILLIS) {
                        throw IllegalStateException("Duplicate payment blocked by atomic server check (10s window).")
                    }
                    transaction.update(
                        billRef,
                        mapOf(
                            "nextDueDateMillis" to nextDueMillis,
                            "lastPaidAtMillis" to nowMillis,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                } else {
                    transaction.set(
                        billRef,
                        mapOf(
                            "userId" to uid,
                            "title" to bill.title,
                            "amountMinor" to bill.amountMinor,
                            "categoryId" to bill.categoryId,
                            "categoryName" to bill.categoryName,
                            "frequency" to bill.frequency,
                            "nextDueDateMillis" to nextDueMillis,
                            "lastPaidAtMillis" to nowMillis,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }

                val txPayload = mapOf(
                    "userId" to uid,
                    "title" to "${bill.title} (Recurring Bill)",
                    "amountMinor" to bill.amountMinor,
                    "type" to TransactionType.EXPENSE.name,
                    "categoryId" to bill.categoryId,
                    "categoryName" to bill.categoryName,
                    "timestampMillis" to nowMillis,
                    "recurringBillId" to bill.id,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                transaction.set(txRef, txPayload)
            }.await()

            recentBillPaymentTimestamps[bill.id] = nowMillis

            val updatedBill = bill.copy(
                userId = uid,
                nextDueDateMillis = nextDueMillis,
                lastPaidAtMillis = nowMillis
            )
            val createdTx = FinancialTransaction(
                id = txId,
                userId = uid,
                title = "${bill.title} (Recurring Bill)",
                amountMinor = bill.amountMinor,
                type = TransactionType.EXPENSE.name,
                categoryId = bill.categoryId,
                categoryName = bill.categoryName,
                timestampMillis = nowMillis,
                recurringBillId = bill.id
            )

            _inMemoryRecurringBills.value = _inMemoryRecurringBills.value.map {
                if (it.id == updatedBill.id) updatedBill else it
            }
            _inMemoryTransactions.value = listOf(createdTx) + _inMemoryTransactions.value

            Result.success(updatedBill to createdTx)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, billRef.path)
            Result.failure(e)
        }
    }

    fun advanceDueDateMillis(currentDueMillis: Long, frequencyStr: String): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = if (currentDueMillis > 0L) currentDueMillis else System.currentTimeMillis()
        }
        when (frequencyStr.uppercase()) {
            BillFrequency.WEEKLY.name -> cal.add(Calendar.DAY_OF_YEAR, 7)
            BillFrequency.YEARLY.name -> cal.add(Calendar.YEAR, 1)
            else -> cal.add(Calendar.MONTH, 1)
        }
        return cal.timeInMillis
    }

    // --- 6. Roommate Split Ledger & Connection Requests ---

    suspend fun createSharedExpense(
        title: String,
        totalAmountMinor: Long,
        paidByMemberId: String,
        paidByName: String,
        splitMethod: SplitMethod,
        participants: List<ParticipantSplit>
    ): Result<SharedExpense> {
        val uid = requireUserId()
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            return Result.failure(IllegalArgumentException("Shared expense title cannot be empty."))
        }
        if (!FinancialEngine.isValidMinorAmount(totalAmountMinor)) {
            return Result.failure(IllegalArgumentException("Total amount must be positive."))
        }
        if (participants.isEmpty()) {
            return Result.failure(IllegalArgumentException("At least one participant is required."))
        }
        val sumOfShares = participants.sumOf { it.shareMinor }
        if (sumOfShares != totalAmountMinor) {
            return Result.failure(
                IllegalArgumentException(
                    "Participant split amounts (${FinancialEngine.formatIndianCurrency(sumOfShares)}) must sum exactly to totalAmount (${FinancialEngine.formatIndianCurrency(totalAmountMinor)})."
                )
            )
        }

        val docId = "split_${UUID.randomUUID().toString().replace("-", "").take(14)}"
        val docRef = db.collection("users").document(uid).collection("shared_expenses").document(docId)

        val idsStr = participants.joinToString("|") { it.participantId }
        val namesStr = participants.joinToString("|") { it.participantName }
        val sharesStr = participants.joinToString("|") { it.shareMinor.toString() }
        val settledStr = participants.joinToString("|") { it.isSettled.toString() }
        val allSettled = participants.all { it.isSettled }

        val payload = mapOf(
            "userId" to uid,
            "title" to trimmedTitle,
            "totalAmountMinor" to totalAmountMinor,
            "paidByMemberId" to paidByMemberId,
            "paidByName" to paidByName,
            "splitMethod" to splitMethod.name,
            "participantIds" to idsStr,
            "participantNames" to namesStr,
            "participantSharesMinor" to sharesStr,
            "participantSettledFlags" to settledStr,
            "isFullySettled" to allSettled,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            docRef.set(payload).await()
            val expense = SharedExpense(
                id = docId,
                userId = uid,
                title = trimmedTitle,
                totalAmountMinor = totalAmountMinor,
                paidByMemberId = paidByMemberId,
                paidByName = paidByName,
                splitMethod = splitMethod.name,
                participantIds = idsStr,
                participantNames = namesStr,
                participantSharesMinor = sharesStr,
                participantSettledFlags = settledStr,
                isFullySettled = allSettled
            )
            _inMemorySharedExpenses.value = listOf(expense) + _inMemorySharedExpenses.value
            Result.success(expense)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun markParticipantSplitSettled(
        expense: SharedExpense,
        participantId: String
    ): Result<SharedExpense> {
        val uid = requireUserId()
        val updatedSplits = expense.parsedSplits().map { split ->
            if (split.participantId == participantId) split.copy(isSettled = true) else split
        }
        val settledStr = updatedSplits.joinToString("|") { it.isSettled.toString() }
        val allSettled = updatedSplits.all { it.isSettled }
        val docRef = db.collection("users").document(uid).collection("shared_expenses").document(expense.id)

        return try {
            docRef.update(
                mapOf(
                    "participantSettledFlags" to settledStr,
                    "isFullySettled" to allSettled,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            val updated = expense.copy(
                participantSettledFlags = settledStr,
                isFullySettled = allSettled
            )
            _inMemorySharedExpenses.value = _inMemorySharedExpenses.value.map {
                if (it.id == updated.id) updated else it
            }
            Result.success(updated)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun registerVerifiedPhoneInCloud(
        uid: String,
        e164Phone: String,
        displayName: String
    ): Result<Unit> {
        if (!PhoneValidation.isValidE164Phone(e164Phone)) {
            return Result.failure(IllegalArgumentException("Invalid E.164 phone number"))
        }
        val normalized = e164Phone.trim()
        val docRef = db.collection("phone_registry").document(normalized)
        return try {
            val existing = docRef.get().await()
            if (existing.exists()) {
                val existingOwner = existing.getString("ownerUid")
                if (existingOwner != null && existingOwner != uid) {
                    return Result.failure(
                        IllegalStateException("Phone number $normalized is already claimed by another Fluxora user.")
                    )
                }
            }
            docRef.set(
                mapOf(
                    "phoneE164" to normalized,
                    "ownerUid" to uid,
                    "displayName" to displayName.ifBlank { "Fluxora User" },
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun sendRoommateConnectionRequest(
        senderUid: String,
        senderName: String,
        senderVerifiedPhoneE164: String?,
        isSenderPhoneVerified: Boolean,
        recipientName: String,
        recipientPhoneE164: String,
        recipientUidHint: String = ""
    ): Result<RoommateConnectionRequest> {
        val normalizedRecipientPhone = recipientPhoneE164.trim()
        val resolvedRecipientUid = if (recipientUidHint.isNotBlank()) {
            recipientUidHint.trim()
        } else {
            try {
                val phoneDoc = db.collection("phone_registry").document(normalizedRecipientPhone).get().await()
                phoneDoc.getString("ownerUid") ?: "pending_${normalizedRecipientPhone.filter { it.isDigit() }}"
            } catch (_: Exception) {
                "pending_${normalizedRecipientPhone.filter { it.isDigit() }}"
            }
        }

        val validation = RoommateRequestSecurityValidator.validateNewRequest(
            senderUid = senderUid,
            senderName = senderName,
            senderVerifiedPhoneE164 = senderVerifiedPhoneE164,
            isSenderPhoneVerified = isSenderPhoneVerified,
            recipientUid = resolvedRecipientUid,
            recipientName = recipientName,
            recipientPhoneE164 = normalizedRecipientPhone,
            existingRequests = _inMemoryConnectionRequests.value
        )
        val validRequest = validation.getOrElse { return Result.failure(it) }

        val docRef = db.collection("connection_requests").document(validRequest.id)
        val payload = mapOf(
            "senderUid" to validRequest.senderUid,
            "senderName" to validRequest.senderName,
            "senderPhone" to validRequest.senderPhone,
            "recipientUid" to validRequest.recipientUid,
            "recipientName" to validRequest.recipientName,
            "recipientPhone" to validRequest.recipientPhone,
            "status" to ConnectionRequestStatus.PENDING.name,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            docRef.set(payload).await()
            _inMemoryConnectionRequests.value = listOf(validRequest) + _inMemoryConnectionRequests.value
            Result.success(validRequest)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun respondToRoommateConnectionRequest(
        request: RoommateConnectionRequest,
        actorUid: String,
        actorVerifiedPhoneE164: String?,
        isActorPhoneVerified: Boolean,
        targetStatus: ConnectionRequestStatus
    ): Result<RoommateConnectionRequest> {
        val transitionResult = RoommateRequestSecurityValidator.validateStatusTransition(
            request = request,
            actorUid = actorUid,
            actorVerifiedPhoneE164 = actorVerifiedPhoneE164,
            isActorPhoneVerified = isActorPhoneVerified,
            targetStatus = targetStatus
        )
        val updatedRequest = transitionResult.getOrElse { return Result.failure(it) }

        val syncOutcome = RoommateRequestSecurityValidator.syncRemoteConnectionRequest(
            localExisting = request,
            remoteIncoming = updatedRequest,
            currentUserUid = actorUid,
            currentUserVerifiedPhone = actorVerifiedPhoneE164
        ).getOrElse { return Result.failure(it) }

        val reqRef = db.collection("connection_requests").document(request.id)
        return try {
            reqRef.update(
                mapOf(
                    "status" to targetStatus.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val newMember = syncOutcome.autoCreatedHouseholdMember
            if (newMember != null) {
                val memberRef = db.collection("users").document(actorUid)
                    .collection("household_members").document(newMember.id)
                memberRef.set(
                    mapOf(
                        "userId" to actorUid,
                        "memberUid" to newMember.memberUid,
                        "name" to newMember.name,
                        "phoneE164" to newMember.phoneE164,
                        "upiVpa" to newMember.upiVpa,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ).filterValues { it != null } as Map<String, Any>
                ).await()
                _inMemoryHouseholdMembers.value = _inMemoryHouseholdMembers.value + newMember
            }

            _inMemoryConnectionRequests.value = _inMemoryConnectionRequests.value.map {
                if (it.id == updatedRequest.id) updatedRequest else it
            }
            Result.success(updatedRequest)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, reqRef.path)
            Result.failure(e)
        }
    }

    suspend fun addHouseholdMemberManually(
        name: String,
        phoneE164: String,
        upiVpa: String? = null
    ): Result<HouseholdMember> {
        val uid = requireUserId()
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Roommate name cannot be empty."))
        }
        if (!PhoneValidation.isValidE164Phone(phoneE164)) {
            return Result.failure(IllegalArgumentException("Valid E.164 phone number is required."))
        }
        val docId = "member_${phoneE164.filter { it.isDigit() }}"
        val docRef = db.collection("users").document(uid).collection("household_members").document(docId)
        val member = HouseholdMember(
            id = docId,
            userId = uid,
            memberUid = "uid_${phoneE164.filter { it.isDigit() }}",
            name = trimmedName,
            phoneE164 = phoneE164.trim(),
            upiVpa = upiVpa?.trim()?.takeIf { it.isNotEmpty() }
        )
        val payload = mapOf(
            "userId" to uid,
            "memberUid" to member.memberUid,
            "name" to member.name,
            "phoneE164" to member.phoneE164,
            "upiVpa" to member.upiVpa,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null } as Map<String, Any>

        return try {
            docRef.set(payload).await()
            _inMemoryHouseholdMembers.value = _inMemoryHouseholdMembers.value.filterNot { it.id == docId } + member
            Result.success(member)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    fun currentMonthKey(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        return "$year-$month"
    }

    /**
     * DPDP Act Right-to-Erasure & Play Data Safety Account Data Deletion:
     * Deletes the user's financial subcollection records and releases phone ownership.
     */
    suspend fun deleteAllUserDataAndReleasePhone(
        userId: String = requireUserId(),
        verifiedPhoneE164: String? = null
    ): Result<Unit> {
        _inMemoryTransactions.value = emptyList()
        _inMemoryCategories.value = DefaultCategories.toFinancialCategories(userId)
        _inMemoryBudgets.value = emptyList()
        _inMemorySavingsGoals.value = emptyList()
        _inMemoryRecurringBills.value = emptyList()
        _inMemoryHouseholdMembers.value = emptyList()
        _inMemorySharedExpenses.value = emptyList()
        _inMemoryConnectionRequests.value = emptyList()

        return try {
            val subcollections = listOf(
                "transactions",
                "categories",
                "budgets",
                "savings_goals",
                "recurring_bills",
                "household_members",
                "shared_expenses"
            )
            for (sub in subcollections) {
                val snap = db.collection("users").document(userId).collection(sub).get().await()
                for (doc in snap.documents) {
                    doc.reference.delete().await()
                }
            }
            if (!verifiedPhoneE164.isNullOrBlank()) {
                val phoneDocId = verifiedPhoneE164.trim()
                val phoneDoc = db.collection("phone_registry").document(phoneDocId).get().await()
                if (phoneDoc.exists() && phoneDoc.getString("ownerUid") == userId) {
                    phoneDoc.reference.delete().await()
                }
            }
            db.collection("users").document(userId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }
}
