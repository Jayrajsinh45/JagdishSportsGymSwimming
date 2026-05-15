package com.jagdishsports.gymswimming.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jagdishsports.gymswimming.data.MemberRepository
import java.time.LocalDate

class MembershipExpiryWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        return try {
            NotificationHelper.createNotificationChannel(applicationContext)

            val today = LocalDate.now()
            val members = MemberRepository
                .getInstance(applicationContext)
                .getMembersExpiringBetween(
                    startEpochDay = today.toEpochDay(),
                    endEpochDay = today.plusDays(3).toEpochDay()
                )

            members.forEach { member ->
                NotificationHelper.showExpiryNotification(applicationContext, member)
            }

            Result.success()
        } catch (_: SecurityException) {
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
