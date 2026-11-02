package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import java.util.concurrent.TimeUnit

enum class NotificationCategory(val displayName: String, val channelId: String) {
    BUDGET("Budget Alerts", "fluxora_channel_budget"),
    BILL("Bill Reminders", "fluxora_channel_bill"),
    SAVINGS("Savings Milestones", "fluxora_channel_savings"),
    ROOMMATE("Roommate Ledger", "fluxora_channel_roommate"),
    INSIGHT("AI Saving Insights", "fluxora_channel_insight")
}

data class FluxoraAlert(
    val id: String,
    val category: NotificationCategory,
    val title: String,
    val message: String,
    val relatedId: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

/**
 * Granular Notification Engine:
 * - Master notification switch preserves individual category toggles when disabled and re-enabled.
 * - Suppresses disabled categories.
 * - Deduplicates alerts by `relatedId`.
 */
class NotificationManager(
    initialMasterEnabled: Boolean = true,
    initialCategoryToggles: Map<NotificationCategory, Boolean> = NotificationCategory.entries.associateWith { true }
) {
    private var masterEnabled: Boolean = initialMasterEnabled
    private val categoryToggles: MutableMap<NotificationCategory, Boolean> =
        NotificationCategory.entries.associateWith { initialCategoryToggles[it] ?: true }.toMutableMap()

    private val dispatchedAlertsByRelatedId = LinkedHashMap<String, FluxoraAlert>()

    fun isMasterEnabled(): Boolean = masterEnabled

    fun isCategoryToggleChecked(category: NotificationCategory): Boolean {
        return categoryToggles[category] ?: true
    }

    fun isCategoryEffectivelyEnabled(category: NotificationCategory): Boolean {
        return masterEnabled && isCategoryToggleChecked(category)
    }

    /**
     * Toggles the master switch WITHOUT overwriting or losing the individual category toggles.
     */
    fun setMasterEnabled(enabled: Boolean) {
        masterEnabled = enabled
    }

    fun setCategoryEnabled(category: NotificationCategory, enabled: Boolean) {
        categoryToggles[category] = enabled
    }

    fun getCategoryToggleSnapshot(): Map<NotificationCategory, Boolean> {
        return categoryToggles.toMap()
    }

    /**
     * Attempts to record and dispatch an alert.
     * - Returns false if master switch or the specific category is disabled.
     * - Returns false if an alert with the same non-blank `relatedId` has already been dispatched (deduplication).
     */
    fun dispatchAlert(alert: FluxoraAlert): Boolean {
        if (!isCategoryEffectivelyEnabled(alert.category)) {
            return false
        }
        val key = alert.relatedId.trim()
        if (key.isEmpty()) return false
        if (dispatchedAlertsByRelatedId.containsKey(key)) {
            return false
        }
        dispatchedAlertsByRelatedId[key] = alert
        return true
    }

    fun getDispatchedAlerts(): List<FluxoraAlert> {
        return dispatchedAlertsByRelatedId.values.sortedByDescending { it.timestampMillis }
    }

    fun markAsRead(relatedId: String) {
        val existing = dispatchedAlertsByRelatedId[relatedId] ?: return
        dispatchedAlertsByRelatedId[relatedId] = existing.copy(isRead = true)
    }

    fun clearAll() {
        dispatchedAlertsByRelatedId.clear()
    }
}

object SystemNotificationHelper {

    fun ensureChannelsCreated(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val sysMgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
                ?: return
            NotificationCategory.entries.forEach { category ->
                val channel = NotificationChannel(
                    category.channelId,
                    category.displayName,
                    android.app.NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Fluxora ${category.displayName}"
                }
                sysMgr.createNotificationChannel(channel)
            }
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun postSystemNotification(context: Context, alert: FluxoraAlert) {
        ensureChannelsCreated(context)
        if (!hasNotificationPermission(context)) return

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            alert.relatedId.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, alert.category.channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(alert.title)
            .setContentText(alert.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(alert.message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (alert.category == NotificationCategory.BILL) {
            val dismissIntent = Intent(context, BillActionReceiver::class.java).apply {
                action = BillActionReceiver.ACTION_DISMISS_BILL_ALERT
                putExtra(BillActionReceiver.EXTRA_RELATED_ID, alert.relatedId)
            }
            val dismissPending = PendingIntent.getBroadcast(
                context,
                alert.relatedId.hashCode() + 1,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "Acknowledge", dismissPending)
        }

        try {
            NotificationManagerCompat.from(context).notify(alert.relatedId.hashCode(), builder.build())
        } catch (_: SecurityException) {
            // Permission revoked mid-flight
        }
    }

    fun schedulePeriodicBillChecks(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<BillReminderWorker>(12, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "fluxora_bill_reminder_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}

class BillReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        SystemNotificationHelper.ensureChannelsCreated(applicationContext)
        return Result.success()
    }
}

class BillActionReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_DISMISS_BILL_ALERT = "com.aistudio.financetrack.qwrxnv.ACTION_DISMISS_BILL_ALERT"
        const val EXTRA_RELATED_ID = "extra_related_id"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val relatedId = intent?.getStringExtra(EXTRA_RELATED_ID) ?: return
        NotificationManagerCompat.from(context).cancel(relatedId.hashCode())
    }
}
