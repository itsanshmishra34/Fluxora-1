package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CategoryBudget
import com.example.data.FinancialTransaction
import com.example.data.RecurringBill
import com.example.data.TransactionType
import com.example.finance.FinancialEngine
import com.example.ui.theme.LocalFluxoraExtendedColors
import com.example.ui.theme.LocalHideSensitiveAmounts

/**
 * Floating top status capsule (`DynamicIsland`) surfacing live cash-flow health,
 * active 80%/100% budget warnings, due recurring bills, privacy toggle, and quick navigation.
 * Uses only real application state.
 */
@Composable
fun DynamicIsland(
    transactions: List<FinancialTransaction>,
    budgets: List<CategoryBudget>,
    recurringBills: List<RecurringBill>,
    onToggleHideSensitive: () -> Unit,
    onNavigateToSection: (String) -> Unit,
    onOpenAiAdvisor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hideSensitive = LocalHideSensitiveAmounts.current
    val extendedColors = LocalFluxoraExtendedColors.current
    var expanded by remember { mutableStateOf(false) }

    val totalIncome = remember(transactions) {
        transactions.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amountMinor }
    }
    val totalExpense = remember(transactions) {
        transactions.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amountMinor }
    }
    val netFlow = totalIncome - totalExpense

    val spendByCategory = remember(transactions) {
        transactions.filter { it.type == TransactionType.EXPENSE.name }
            .groupBy { it.categoryId }
            .mapValues { (_, list) -> list.sumOf { it.amountMinor } }
    }

    val exceededBudgets = remember(budgets, spendByCategory) {
        budgets.filter { b ->
            b.limitMinor > 0L && (spendByCategory[b.categoryId] ?: 0L) >= b.limitMinor
        }
    }
    val warningBudgets = remember(budgets, spendByCategory) {
        budgets.filter { b ->
            val spent = spendByCategory[b.categoryId] ?: 0L
            b.limitMinor > 0L && spent < b.limitMinor && (spent * 100L / b.limitMinor) >= 80L
        }
    }

    val statusColor = when {
        exceededBudgets.isNotEmpty() -> extendedColors.dangerColor
        warningBudgets.isNotEmpty() -> extendedColors.warningColor
        else -> extendedColors.incomeRingColor
    }

    val statusHeadline = when {
        exceededBudgets.isNotEmpty() -> "Budget Exceeded: ${exceededBudgets.first().categoryName}"
        warningBudgets.isNotEmpty() -> "80%+ Budget Warning: ${warningBudgets.first().categoryName}"
        transactions.isEmpty() -> "Fluxora Ready • Add First Flow"
        netFlow >= 0L -> "Cash Flow Healthy • ${FinancialEngine.formatIndianCurrency(netFlow, showSign = true, hideSensitive = hideSensitive)}"
        else -> "Net Outflow • ${FinancialEngine.formatIndianCurrency(netFlow, showSign = true, hideSensitive = hideSensitive)}"
    }

    val statusIcon = when {
        exceededBudgets.isNotEmpty() -> Icons.Default.Error
        warningBudgets.isNotEmpty() -> Icons.Default.Warning
        else -> Icons.Default.CheckCircle
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(28.dp))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        statusColor.copy(alpha = 0.65f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .clickable { expanded = !expanded }
            .testTag("dynamic_island"),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = "Cash flow health status",
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = statusHeadline,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${transactions.size} records • ${recurringBills.size} active bills",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleHideSensitive,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("privacy_mask_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (hideSensitive) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (hideSensitive) "Show sensitive amounts" else "Hide sensitive amounts",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onOpenAiAdvisor,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("dynamic_island_ai_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Open AI Financial Advisor",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = {
                        expanded = false
                        onNavigateToSection("DASHBOARD")
                    }) {
                        Text("Reactor")
                    }
                    TextButton(onClick = {
                        expanded = false
                        onNavigateToSection("TRANSACTIONS")
                    }) {
                        Text("Ledger")
                    }
                    TextButton(onClick = {
                        expanded = false
                        onNavigateToSection("PLANNING")
                    }) {
                        Text("Budgets")
                    }
                    TextButton(onClick = {
                        expanded = false
                        onNavigateToSection("ROOMMATES")
                    }) {
                        Text("Splits")
                    }
                }
            }
        }
    }
}
