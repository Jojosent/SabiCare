package com.example.sabicare_j.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.sabicare_j.R
import com.example.sabicare_j.SabiCareApplication
import com.example.sabicare_j.data.local.entities.ChildEntity
import com.example.sabicare_j.data.local.entities.MeasurementType
import com.example.sabicare_j.ui.main.MainActivity
import java.util.concurrent.TimeUnit

/**
 * Schedules a daily WorkManager job that checks every child's measurement
 * intervals and posts notifications when something is due or out-of-range.
 */
object NotificationScheduler {

    const val CHANNEL_REMINDERS = "sabicare.reminders"
    const val CHANNEL_ALERTS    = "sabicare.alerts"
    private const val UNIQUE_WORK = "sabicare.daily_check"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val reminders = NotificationChannel(
            CHANNEL_REMINDERS,
            context.getString(R.string.notif_channel_reminders),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = context.getString(R.string.notif_channel_reminders_desc) }

        val alerts = NotificationChannel(
            CHANNEL_ALERTS,
            context.getString(R.string.notif_channel_alerts),
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = context.getString(R.string.notif_channel_alerts_desc) }

        nm.createNotificationChannel(reminders)
        nm.createNotificationChannel(alerts)
    }

    /** Schedule the daily check, idempotent. */
    fun schedule(context: Context) {
        ensureChannels(context)
        val request = PeriodicWorkRequestBuilder<DailyCheckWorker>(1, TimeUnit.DAYS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .setInitialDelay(2, TimeUnit.HOURS) // first run in a couple of hours
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(UNIQUE_WORK, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    /** Cancel the daily check. */
    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK)
    }
}

class DailyCheckWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        val prefs = ctx.getSharedPreferences("sabicare_prefs_simple", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notifications_enabled", true)) return Result.success()

        val app = ctx.applicationContext as SabiCareApplication
        val childRepo = app.childRepository
        val measRepo = app.measurementRepository

        val children = runCatching { childRepo.getAllChildren() }.getOrDefault(emptyList())
        if (children.isEmpty()) return Result.success()

        val notifAllowed = NotificationManagerCompat.from(ctx).areNotificationsEnabled()
        if (!notifAllowed) return Result.success()

        var notifId = 1000
        for (child in children) {
            if (prefs.getBoolean("reminder_notif_enabled", true)) {
                MeasurementType.values().forEach { type ->
                    val due = runCatching { measRepo.isDue(child.id, type) }.getOrDefault(false)
                    if (due) {
                        postReminderNotification(ctx, child, type, ++notifId)
                    }
                }
            }
            if (prefs.getBoolean("growth_alerts_enabled", true)) {
                checkGrowthAlerts(ctx, child, measRepo, ++notifId)
            }
        }
        return Result.success()
    }

    private fun postReminderNotification(
        ctx: Context,
        child: ChildEntity,
        type: MeasurementType,
        id: Int
    ) {
        val title = ctx.getString(R.string.notif_reminder_title, child.name)
        val body  = ctx.getString(
            R.string.notif_reminder_text,
            typeLabel(ctx, type),
            ageHint(ctx, child.birthDate)
        )
        post(ctx, NotificationScheduler.CHANNEL_REMINDERS, id, title, body)
    }

    private suspend fun checkGrowthAlerts(
        ctx: Context,
        child: ChildEntity,
        measRepo: com.example.sabicare_j.data.repository.MeasurementRepository,
        baseId: Int
    ) {
        val ageDays = ((System.currentTimeMillis() - child.birthDate) / (24L*60*60*1000)).toInt().coerceAtLeast(0)
        var id = baseId
        MeasurementType.values().forEach { type ->
            val latest = runCatching { measRepo.getLatestByType(child.id, type) }.getOrNull() ?: return@forEach
            val norm = when (type) {
                MeasurementType.HEIGHT          -> com.example.sabicare_j.utils.GrowthStandards.getHeightNorm(ageDays, child.gender)
                MeasurementType.WEIGHT          -> com.example.sabicare_j.utils.GrowthStandards.getWeightNorm(ageDays, child.gender)
                MeasurementType.FEEDINGS_COUNT  -> com.example.sabicare_j.utils.GrowthStandards.getFeedingsNorm(ageDays)
                MeasurementType.CALORIES        -> com.example.sabicare_j.utils.GrowthStandards.getCaloriesNorm(ageDays)
                MeasurementType.SLEEP_DURATION  -> com.example.sabicare_j.utils.GrowthStandards.getSleepNorm(ageDays)
            }
            val status = com.example.sabicare_j.utils.GrowthStandards.getStatus(latest.value, norm)
            if (status != com.example.sabicare_j.utils.GrowthStandards.ComplianceStatus.NORMAL) {
                val title = ctx.getString(R.string.notif_growth_alert_title, child.name)
                val body  = ctx.getString(
                    R.string.notif_growth_alert_text,
                    typeLabel(ctx, type),
                    formatVal(latest.value),
                    type.unit
                )
                post(ctx, NotificationScheduler.CHANNEL_ALERTS, ++id, title, body)
            }
        }
    }

    private fun post(ctx: Context, channelId: String, id: Int, title: String, body: String) {
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            ctx, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(ctx, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        runCatching {
            NotificationManagerCompat.from(ctx).notify(id, notif)
        }
    }

    private fun typeLabel(ctx: Context, t: MeasurementType): String = when (t) {
        MeasurementType.HEIGHT          -> ctx.getString(R.string.type_height)
        MeasurementType.WEIGHT          -> ctx.getString(R.string.type_weight)
        MeasurementType.FEEDINGS_COUNT  -> ctx.getString(R.string.type_feedings_count)
        MeasurementType.CALORIES        -> ctx.getString(R.string.type_calories)
        MeasurementType.SLEEP_DURATION  -> ctx.getString(R.string.type_sleep_duration)
    }

    private fun ageHint(ctx: Context, birthMillis: Long): String {
        val days = ((System.currentTimeMillis() - birthMillis) / (24L*60*60*1000)).toInt().coerceAtLeast(0)
        return when {
            days < 7 -> ctx.getString(R.string.child_age_days, days)
            days < 30 -> ctx.getString(R.string.child_age_weeks, days / 7)
            else -> ctx.getString(R.string.child_age_months, days / 30)
        }
    }

    private fun formatVal(v: Double): String =
        if (v == v.toLong().toDouble()) v.toLong().toString() else "%.1f".format(v)
}
