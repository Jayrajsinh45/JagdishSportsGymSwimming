package com.jagdishsports.gymswimming.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    private const val DAILY_EXPIRY_WORK = "daily_membership_expiry_check"

    fun scheduleDailyExpiryCheck(context: Context) {
        val request = PeriodicWorkRequestBuilder<MembershipExpiryWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setInitialDelay(delayUntilNextNineAmMillis(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_EXPIRY_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun delayUntilNextNineAmMillis(now: ZonedDateTime = ZonedDateTime.now()): Long {
        var nextRun = now
            .withHour(9)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1)
        }

        return Duration.between(now, nextRun).toMillis().coerceAtLeast(0)
    }
}
