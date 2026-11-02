package com.example.finance

import com.example.data.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.abs

/**
 * Value class wrapping integer minor units (paise: 1 INR = 100 paise)
 * with overflow-checked arithmetic via FinancialEngine.
 */
@JvmInline
value class Money(val amountMinor: Long) : Comparable<Money> {
    operator fun plus(other: Money): Money = Money(Math.addExact(this.amountMinor, other.amountMinor))
    operator fun minus(other: Money): Money = Money(Math.subtractExact(this.amountMinor, other.amountMinor))
    operator fun times(multiplier: Int): Money = Money(Math.multiplyExact(this.amountMinor, multiplier.toLong()))
    override fun compareTo(other: Money): Int = this.amountMinor.compareTo(other.amountMinor)
}

enum class CurrencyStyle {
    ISO_PREFIX, // INR 39,050.00
    SYMBOL      // ₹39,050.00
}

/**
 * Core Indian Currency & Minor-Unit Financial Engine.
 * Works strictly in integer minor units (paise: 1 INR = 100 paise)
 * to eliminate IEEE-754 floating-point rounding bugs.
 */
object FinancialEngine {

    /**
     * Maximum supported transaction/goal/budget amount in minor units (paise):
     * 100,000,000,000 paise = INR 1,000,000,000.00 (100 Crore INR).
     */
    const val MAX_AMOUNT_MINOR: Long = 100_000_000_000L
    const val MIN_AMOUNT_MINOR: Long = 1L

    const val MASKED_CURRENCY_DISPLAY: String = "INR ••••••"
    const val MASKED_SYMBOL_DISPLAY: String = "₹••••••"

    fun isValidMinorAmount(amountMinor: Long): Boolean {
        return amountMinor in MIN_AMOUNT_MINOR..MAX_AMOUNT_MINOR
    }

    fun safeAddMinor(a: Long, b: Long): Long = Math.addExact(a, b)
    fun safeSubtractMinor(a: Long, b: Long): Long = Math.subtractExact(a, b)
    fun safeMultiplyMinor(a: Long, factor: Long): Long = Math.multiplyExact(a, factor)

    /**
     * Formats [Money] value class into Indian numbering grouping (e.g., `INR 39,050.00` or `₹39,050.00`).
     */
    fun formatIndianCurrency(
        money: Money,
        showSign: Boolean = false,
        style: CurrencyStyle = CurrencyStyle.ISO_PREFIX,
        hideSensitive: Boolean = false
    ): String {
        return formatIndianCurrency(
            amountMinor = money.amountMinor,
            showSign = showSign,
            forceSignPrefix = null,
            hideSensitive = hideSensitive,
            style = style
        )
    }

    /**
     * Formats minor units (paise) into standard Indian numbering grouping (e.g., `INR 39,050.00`).
     * Supports optional `+` / `-` sign prefixes and global sensitive amount masking.
     */
    fun formatIndianCurrency(
        amountMinor: Long,
        showSign: Boolean = false,
        forceSignPrefix: String? = null,
        hideSensitive: Boolean = false,
        style: CurrencyStyle = CurrencyStyle.ISO_PREFIX
    ): String {
        if (hideSensitive) {
            return if (style == CurrencyStyle.SYMBOL) MASKED_SYMBOL_DISPLAY else MASKED_CURRENCY_DISPLAY
        }
        val isNegative = amountMinor < 0
        val absMinor = if (amountMinor == Long.MIN_VALUE) Long.MAX_VALUE else abs(amountMinor)
        val rupees = absMinor / 100L
        val paise = absMinor % 100L

        val groupedRupees = formatIndianIntegerGrouping(rupees)
        val paiseStr = paise.toString().padStart(2, '0')

        val prefix = when {
            forceSignPrefix != null -> forceSignPrefix
            isNegative -> "-"
            showSign && amountMinor > 0 -> "+"
            else -> ""
        }

        val unitLabel = if (style == CurrencyStyle.SYMBOL) "₹" else "INR "
        return "$prefix$unitLabel$groupedRupees.$paiseStr"
    }

    /**
     * Formats a non-negative integer using Indian numbering system grouping (lakhs, crores):
     * Last 3 digits grouped together, then every 2 digits to the left (e.g. 12,34,56,789).
     */
    fun formatIndianIntegerGrouping(number: Long): String {
        val raw = abs(number).toString()
        if (raw.length <= 3) return raw

        val lastThree = raw.substring(raw.length - 3)
        val remaining = raw.substring(0, raw.length - 3)

        val sb = StringBuilder()
        var i = remaining.length
        while (i > 0) {
            val start = (i - 2).coerceAtLeast(0)
            if (sb.isNotEmpty()) {
                sb.insert(0, ',')
            }
            sb.insert(0, remaining.substring(start, i))
            i = start
        }
        sb.append(',').append(lastThree)
        return sb.toString()
    }

    /**
     * Parses a major unit INR string (e.g., "39050", "39050.5", "39,050.75") into minor units (paise)
     * using exact BigDecimal arithmetic without floating-point drift.
     */
    fun parseInrMajorToMinor(input: String): Long? {
        val cleaned = input.trim()
            .removePrefix("INR")
            .removePrefix("₹")
            .replace(",", "")
            .trim()
        if (cleaned.isEmpty()) return null
        return try {
            val bd = BigDecimal(cleaned)
            if (bd.scale() > 2) return null
            val minorBd = bd.movePointRight(2).setScale(0, RoundingMode.HALF_UP)
            val minor = minorBd.longValueExact()
            if (isValidMinorAmount(minor)) minor else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Formats minor units into plain decimal major string (e.g. "39050.00") for CSV export or editing.
     */
    fun formatMinorToPlainDecimal(amountMinor: Long): String {
        val sign = if (amountMinor < 0) "-" else ""
        val absMinor = abs(amountMinor)
        val rupees = absMinor / 100L
        val paise = (absMinor % 100L).toString().padStart(2, '0')
        return "$sign$rupees.$paise"
    }

    /**
     * Deterministic minor-unit EQUAL split allocation:
     * Divides [totalAmountMinor] across [participantCount] participants and distributes
     * the integer remainder 1 paisa at a time to the first `remainder` participants so the sum
     * equals [totalAmountMinor] exactly.
     */
    fun calculateEqualSplits(totalAmountMinor: Long, participantCount: Int): List<Long> {
        require(isValidMinorAmount(totalAmountMinor)) { "Total amount must be within 1..MAX_AMOUNT_MINOR" }
        require(participantCount > 0) { "At least 1 participant is required" }

        val baseShare = totalAmountMinor / participantCount
        val remainder = (totalAmountMinor % participantCount).toInt()
        return List(participantCount) { index ->
            if (index < remainder) baseShare + 1L else baseShare
        }
    }

    /**
     * Validates an EXACT split allocation where each participant's minor-unit amount is specified.
     */
    fun validateExactSplits(totalAmountMinor: Long, exactAmountsMinor: List<Long>): Boolean {
        if (!isValidMinorAmount(totalAmountMinor) || exactAmountsMinor.isEmpty()) return false
        if (exactAmountsMinor.any { it < 0L }) return false
        return exactAmountsMinor.sum() == totalAmountMinor
    }

    /**
     * Calculates minor-unit shares from basis-point or integer/decimal percentages summing to 100%,
     * distributing any rounding remainder deterministically so shares sum strictly to [totalAmountMinor].
     */
    fun calculatePercentageSplits(totalAmountMinor: Long, percentages: List<Double>): List<Long> {
        require(isValidMinorAmount(totalAmountMinor)) { "Total amount must be within 1..MAX_AMOUNT_MINOR" }
        require(percentages.isNotEmpty()) { "Percentages list cannot be empty" }
        require(percentages.all { it >= 0.0 }) { "Percentages must be non-negative" }

        // Convert to basis points (100.00% = 10_000 bps)
        val basisPoints = percentages.map {
            BigDecimal.valueOf(it).movePointRight(2).setScale(0, RoundingMode.HALF_UP).toLong()
        }
        require(basisPoints.sum() == 10_000L) { "Percentages must sum to 100%" }

        val rawShares = basisPoints.map { (totalAmountMinor * it) / 10_000L }.toMutableList()
        var remainder = totalAmountMinor - rawShares.sum()
        var idx = 0
        while (remainder > 0L) {
            rawShares[idx % rawShares.size] += 1L
            remainder -= 1L
            idx++
        }
        return rawShares
    }

    /**
     * Calculates minor-unit shares from relative integer weights (SHARES method),
     * distributing remainder deterministically so participant shares sum strictly to [totalAmountMinor].
     */
    fun calculateShareWeightSplits(totalAmountMinor: Long, weights: List<Int>): List<Long> {
        require(isValidMinorAmount(totalAmountMinor)) { "Total amount must be within 1..MAX_AMOUNT_MINOR" }
        require(weights.isNotEmpty()) { "Weights list cannot be empty" }
        require(weights.all { it > 0 }) { "Each share weight must be positive" }

        val totalWeight = weights.sum().toLong()
        val rawShares = weights.map { (totalAmountMinor * it.toLong()) / totalWeight }.toMutableList()
        var remainder = totalAmountMinor - rawShares.sum()
        var idx = 0
        while (remainder > 0L) {
            rawShares[idx % rawShares.size] += 1L
            remainder -= 1L
            idx++
        }
        return rawShares
    }

    /**
     * Safe-to-spend today (Section 16 Wave 1 & Section 20 exact rules):
     * 1. remainingBudget = sum over budgeted categories of max(0, budgetLimitMinor - spentMinor)
     * 2. upcomingBills = sum of unpaid bills due on or before the end of the current month
     * 3. daysLeft = calendar days from today to month end, inclusive (always >= 1)
     * 4. safeToSpendToday = max(0, remainingBudget - upcomingBills) / daysLeft (integer division in paise)
     * 5. Returns null if no budgets exist this month (UI displays "—").
     */
    fun calculateSafeToSpendToday(
        categoryBudgetsAndSpent: List<Pair<Long, Long>>,
        upcomingBillsMinor: Long,
        daysLeftInMonth: Int
    ): Money? {
        if (categoryBudgetsAndSpent.isEmpty()) return null
        val safeDays = daysLeftInMonth.coerceAtLeast(1).toLong()
        var remainingBudgetMinor = 0L
        for ((limitMinor, spentMinor) in categoryBudgetsAndSpent) {
            val catRemaining = (limitMinor - spentMinor).coerceAtLeast(0L)
            remainingBudgetMinor = safeAddMinor(remainingBudgetMinor, catRemaining)
        }
        val netRemaining = (remainingBudgetMinor - upcomingBillsMinor.coerceAtLeast(0L)).coerceAtLeast(0L)
        return Money(netRemaining / safeDays)
    }

    /**
     * Computes inclusive calendar days remaining in the current month in the given [timeZone] (default Asia/Kolkata).
     */
    fun calculateDaysLeftInMonth(
        nowMillis: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getTimeZone("Asia/Kolkata")
    ): Int {
        val cal = Calendar.getInstance(timeZone).apply { timeInMillis = nowMillis }
        val todayDay = cal.get(Calendar.DAY_OF_MONTH)
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return (maxDay - todayDay + 1).coerceAtLeast(1)
    }
}

/**
 * Wave 1 Smart Quick-Add Parser (Section 16):
 * Parses natural-language English, Hindi, and Hinglish quick-add strings such as:
 * - "chai 40"
 * - "kirana 850 yesterday"
 * - "metro 60 kal"
 * - "electricity bijli 1450"
 * Never auto-saves without an explicit user tap.
 */
object SmartQuickAddParser {

    data class ParsedQuickAdd(
        val title: String,
        val amountMinor: Long,
        val suggestedCategoryId: String,
        val suggestedCategoryName: String,
        val timestampMillis: Long,
        val isYesterday: Boolean,
        val type: TransactionType = TransactionType.EXPENSE
    ) {
        val categoryName: String
            get() = suggestedCategoryName
    }

    private val HINGLISH_CATEGORY_KEYWORDS: List<Triple<Set<String>, String, String>> = listOf(
        Triple(setOf("chai", "coffee", "tea", "samosa", "lunch", "dinner", "breakfast", "swiggy", "zomato", "cafe", "dhaba", "nashta"), "food_dining", "Food & Dining"),
        Triple(setOf("kirana", "grocery", "groceries", "sabzi", "milk", "doodh", "blinkit", "zepto", "instamart", "ration"), "groceries", "Groceries"),
        Triple(setOf("auto", "rickshaw", "uber", "ola", "rapido", "metro", "bus", "petrol", "diesel", "fuel", "cab", "train"), "transportation", "Transportation"),
        Triple(setOf("rent", "kiraya", "pg", "hostel", "maintenance", "flat"), "housing", "Housing"),
        Triple(setOf("bijli", "electricity", "wifi", "broadband", "jio", "airtel", "water", "gas", "cylinder", "recharge", "bill"), "bills_utilities", "Bills & Utilities"),
        Triple(setOf("netflix", "spotify", "prime", "hotstar", "youtube", "subscription"), "subscriptions", "Subscriptions"),
        Triple(setOf("medicine", "dawai", "doctor", "pharmacy", "hospital", "gym"), "health", "Health"),
        Triple(setOf("book", "course", "tuition", "college", "fees", "exam"), "education", "Education")
    )

    fun parse(
        rawInput: String,
        nowMillis: Long = System.currentTimeMillis()
    ): ParsedQuickAdd? {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return null

        val numberMatch = Regex("""(\d+(?:\.\d{1,2})?)""").find(trimmed) ?: return null
        val amountMinor = FinancialEngine.parseInrMajorToMinor(numberMatch.value) ?: return null

        val lower = trimmed.lowercase()
        val isYesterday = lower.contains("yesterday") || lower.contains("kal")
        val targetMillis = if (isYesterday) nowMillis - 24L * 3600L * 1000L else nowMillis

        val tokens = lower
            .replace(numberMatch.value, " ")
            .replace(Regex("""\b(yesterday|kal|today|aaj|rs|inr|rupees|₹)\b"""), " ")
            .trim()
            .split(Regex("""\s+"""))
            .filter { it.isNotBlank() }

        var matchedCatId = "other"
        var matchedCatName = "Other"
        for ((keywords, catId, catName) in HINGLISH_CATEGORY_KEYWORDS) {
            if (tokens.any { it in keywords }) {
                matchedCatId = catId
                matchedCatName = catName
                break
            }
        }

        val cleanTitle = tokens.joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }.ifBlank { matchedCatName }

        return ParsedQuickAdd(
            title = cleanTitle,
            amountMinor = amountMinor,
            suggestedCategoryId = matchedCatId,
            suggestedCategoryName = matchedCatName,
            timestampMillis = targetMillis,
            isYesterday = isYesterday
        )
    }
}

/**
 * Validates transaction timestamps according to Fluxora domain rules:
 * - Past dates: ALLOWED
 * - Today's date (up to end of current local day): ALLOWED
 * - Future-dated transactions: REJECTED with clear user-facing error message.
 */
object DateValidator {

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun validateTransactionDate(
        timestampMillis: Long,
        nowMillis: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): ValidationResult {
        if (timestampMillis <= 0L) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Transaction date must be a valid positive timestamp."
            )
        }

        val endOfTodayCal = Calendar.getInstance(timeZone).apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        return if (timestampMillis > endOfTodayCal.timeInMillis) {
            ValidationResult(
                isValid = false,
                errorMessage = "Future-dated transactions are not allowed. Please select today's date or a past date."
            )
        } else {
            ValidationResult(isValid = true, errorMessage = null)
        }
    }
}

data class DebtPayoffStep(
    val debtId: String,
    val debtName: String,
    val startingBalanceMinor: Long,
    val interestRatePercent: Double,
    val payoffMonthOrder: Int,
    val monthsToPayoffThisDebt: Int,
    val interestPaidMinor: Long
)

data class DebtPayoffSimulationResult(
    val strategy: com.example.data.DebtPayoffStrategy,
    val totalDebtMinor: Long,
    val totalInterestPaidMinor: Long,
    val monthsToPayoff: Int,
    val steps: List<DebtPayoffStep>,
    val comparisonInterestSavedMinor: Long = 0L,
    val comparisonMonthsSaved: Int = 0
)

object DebtCalculatorEngine {

    fun calculateDebtPayoffSchedule(
        debts: List<com.example.data.DebtItem>,
        strategy: com.example.data.DebtPayoffStrategy,
        extraMonthlyPaymentMinor: Long = 0L
    ): DebtPayoffSimulationResult {
        if (debts.isEmpty()) {
            return DebtPayoffSimulationResult(
                strategy = strategy,
                totalDebtMinor = 0L,
                totalInterestPaidMinor = 0L,
                monthsToPayoff = 0,
                steps = emptyList()
            )
        }

        val primaryResult = runSimulation(debts, strategy, extraMonthlyPaymentMinor)

        val oppositeStrategy = if (strategy == com.example.data.DebtPayoffStrategy.SNOWBALL) {
            com.example.data.DebtPayoffStrategy.AVALANCHE
        } else {
            com.example.data.DebtPayoffStrategy.SNOWBALL
        }
        val oppositeResult = runSimulation(debts, oppositeStrategy, extraMonthlyPaymentMinor)

        val interestSaved = oppositeResult.totalInterestPaidMinor - primaryResult.totalInterestPaidMinor
        val monthsSaved = oppositeResult.monthsToPayoff - primaryResult.monthsToPayoff

        return primaryResult.copy(
            comparisonInterestSavedMinor = interestSaved,
            comparisonMonthsSaved = monthsSaved
        )
    }

    private fun runSimulation(
        debts: List<com.example.data.DebtItem>,
        strategy: com.example.data.DebtPayoffStrategy,
        extraMonthlyPaymentMinor: Long
    ): DebtPayoffSimulationResult {
        class ActiveDebt(
            val item: com.example.data.DebtItem,
            var currentBalanceMinor: Long,
            var totalInterestPaidMinor: Long = 0L,
            var payoffMonth: Int = 0
        )

        val sortedDebts = debts.map { ActiveDebt(it, it.balanceMinor) }.let { list ->
            if (strategy == com.example.data.DebtPayoffStrategy.SNOWBALL) {
                list.sortedBy { it.currentBalanceMinor }
            } else {
                list.sortedByDescending { it.item.interestRatePercent }
            }
        }

        val totalInitialMinPayment = debts.sumOf { it.minimumPaymentMinor }
        var month = 0
        var finishedCount = 0
        val totalDebtsCount = sortedDebts.size
        val totalInitialDebt = debts.sumOf { it.balanceMinor }

        while (finishedCount < totalDebtsCount && month < 600) {
            month++

            for (d in sortedDebts) {
                if (d.currentBalanceMinor > 0) {
                    val monthlyRate = (d.item.interestRatePercent / 100.0) / 12.0
                    val interestAccrued = (d.currentBalanceMinor * monthlyRate).toLong()
                    d.currentBalanceMinor += interestAccrued
                    d.totalInterestPaidMinor += interestAccrued
                }
            }

            var paymentPool = totalInitialMinPayment + extraMonthlyPaymentMinor

            for (d in sortedDebts) {
                if (d.currentBalanceMinor > 0) {
                    val minPay = minOf(d.item.minimumPaymentMinor, d.currentBalanceMinor)
                    d.currentBalanceMinor -= minPay
                    paymentPool -= minPay
                }
            }

            if (paymentPool > 0) {
                val target = sortedDebts.firstOrNull { it.currentBalanceMinor > 0 }
                if (target != null) {
                    val extraPay = minOf(paymentPool, target.currentBalanceMinor)
                    target.currentBalanceMinor -= extraPay
                }
            }

            for (d in sortedDebts) {
                if (d.currentBalanceMinor <= 0 && d.payoffMonth == 0) {
                    d.payoffMonth = month
                    finishedCount++
                }
            }
        }

        val totalInterestPaid = sortedDebts.sumOf { it.totalInterestPaidMinor }

        val steps = sortedDebts.mapIndexed { index, d ->
            DebtPayoffStep(
                debtId = d.item.id,
                debtName = d.item.name,
                startingBalanceMinor = d.item.balanceMinor,
                interestRatePercent = d.item.interestRatePercent,
                payoffMonthOrder = index + 1,
                monthsToPayoffThisDebt = if (d.payoffMonth == 0) month else d.payoffMonth,
                interestPaidMinor = d.totalInterestPaidMinor
            )
        }

        return DebtPayoffSimulationResult(
            strategy = strategy,
            totalDebtMinor = totalInitialDebt,
            totalInterestPaidMinor = totalInterestPaid,
            monthsToPayoff = month,
            steps = steps
        )
    }
}


