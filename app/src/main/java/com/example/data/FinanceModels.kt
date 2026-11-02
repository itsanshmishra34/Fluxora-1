package com.example.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class TransactionType {
    INCOME,
    EXPENSE
}

enum class DebtPayoffStrategy {
    SNOWBALL,   // Lowest balance first
    AVALANCHE   // Highest APR interest rate first
}

enum class BillFrequency(val label: String) {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}

enum class SplitMethod(val label: String) {
    EQUAL("Equal"),
    EXACT("Exact"),
    PERCENTAGE("Percentage"),
    SHARES("Shares")
}

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String? = null,
    val verifiedPhone: String? = null,
    val upiVpa: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PhoneOwnershipRecord(
    @DocumentId val id: String = "",
    val phoneE164: String = "",
    val ownerUid: String = "",
    val displayName: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FinancialTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amountMinor: Long = 0L,
    val type: String = TransactionType.EXPENSE.name,
    val categoryId: String = "other",
    val categoryName: String = "Other",
    val timestampMillis: Long = 0L,
    val notes: String? = null,
    val recurringBillId: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FinancialCategory(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val normalizedName: String = "",
    val iconName: String = "Category",
    val colorHex: String = "#00E676",
    val isDefault: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CategoryBudget(
    @DocumentId val id: String = "",
    val userId: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val monthKey: String = "",
    val limitMinor: Long = 0L,
    val alert80Triggered: Boolean = false,
    val alert100Triggered: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SavingsGoal(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val targetMinor: Long = 0L,
    val savedMinor: Long = 0L,
    val targetDateMillis: Long = 0L,
    val highestMilestoneNotified: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val progressFraction: Float
        get() = if (targetMinor <= 0L) 0f else (savedMinor.toDouble() / targetMinor.toDouble()).toFloat().coerceIn(0f, 1f)

    val progressPercentage: Int
        get() = if (targetMinor <= 0L) 0 else ((savedMinor * 100L) / targetMinor).toInt().coerceIn(0, 100)
}

@IgnoreExtraProperties
data class RecurringBill(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amountMinor: Long = 0L,
    val categoryId: String = "bills_utilities",
    val categoryName: String = "Bills & Utilities",
    val frequency: String = BillFrequency.MONTHLY.name,
    val nextDueDateMillis: Long = 0L,
    val lastPaidAtMillis: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class HouseholdMember(
    @DocumentId val id: String = "",
    val userId: String = "",
    val memberUid: String = "",
    val name: String = "",
    val phoneE164: String = "",
    val upiVpa: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class ParticipantSplit(
    val participantId: String,
    val participantName: String,
    val shareMinor: Long,
    val isSettled: Boolean = false
)

@IgnoreExtraProperties
data class SharedExpense(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val totalAmountMinor: Long = 0L,
    val paidByMemberId: String = "",
    val paidByName: String = "",
    val splitMethod: String = SplitMethod.EQUAL.name,
    val participantIds: String = "",
    val participantNames: String = "",
    val participantSharesMinor: String = "",
    val participantSettledFlags: String = "",
    val isFullySettled: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun parsedSplits(): List<ParticipantSplit> {
        if (participantIds.isBlank()) return emptyList()
        val ids = participantIds.split("|")
        val names = participantNames.split("|")
        val shares = participantSharesMinor.split("|").map { it.toLongOrNull() ?: 0L }
        val flags = participantSettledFlags.split("|").map { it.equals("true", ignoreCase = true) }
        return ids.indices.map { i ->
            ParticipantSplit(
                participantId = ids[i],
                participantName = names.getOrElse(i) { "Roommate" },
                shareMinor = shares.getOrElse(i) { 0L },
                isSettled = flags.getOrElse(i) { false }
            )
        }
    }
}

@IgnoreExtraProperties
data class AppNotificationRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val category: String = "INSIGHT",
    val title: String = "",
    val message: String = "",
    val relatedId: String = "",
    val isRead: Boolean = false,
    val timestampMillis: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

object DefaultCategories {
    data class SeedCategory(
        val id: String,
        val name: String,
        val iconName: String,
        val colorHex: String
    )

    /**
     * All 12 mandatory built-in Fluxora categories.
     */
    val BUILT_IN: List<SeedCategory> = listOf(
        SeedCategory("housing", "Housing", "Home", "#00E676"),
        SeedCategory("food_dining", "Food & Dining", "Restaurant", "#FFB300"),
        SeedCategory("groceries", "Groceries", "ShoppingCart", "#69F0AE"),
        SeedCategory("transportation", "Transportation", "DirectionsCar", "#00E5FF"),
        SeedCategory("education", "Education", "School", "#B388FF"),
        SeedCategory("shopping", "Shopping", "ShoppingBag", "#FF80AB"),
        SeedCategory("entertainment", "Entertainment", "Movie", "#EA80FC"),
        SeedCategory("health", "Health", "Favorite", "#FF5252"),
        SeedCategory("bills_utilities", "Bills & Utilities", "ReceiptLong", "#FFD54F"),
        SeedCategory("travel", "Travel", "Flight", "#40C4FF"),
        SeedCategory("subscriptions", "Subscriptions", "Autorenew", "#1DE9B6"),
        SeedCategory("other", "Other", "Category", "#B0BEC5")
    )

    fun toFinancialCategories(userId: String): List<FinancialCategory> {
        return BUILT_IN.map { seed ->
            FinancialCategory(
                id = seed.id,
                userId = userId,
                name = seed.name,
                normalizedName = seed.name.lowercase().trim(),
                iconName = seed.iconName,
                colorHex = seed.colorHex,
                isDefault = true
            )
        }
    }
}

@IgnoreExtraProperties
data class DebtItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val balanceMinor: Long = 0L,
    val interestRatePercent: Double = 0.0,
    val minimumPaymentMinor: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

