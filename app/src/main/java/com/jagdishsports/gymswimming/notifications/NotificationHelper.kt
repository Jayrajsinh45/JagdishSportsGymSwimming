package com.jagdishsports.gymswimming.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.jagdishsports.gymswimming.MainActivity
import com.jagdishsports.gymswimming.R
import com.jagdishsports.gymswimming.data.MemberEntity
import com.jagdishsports.gymswimming.data.endDate
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue

object NotificationHelper {
    const val CHANNEL_ID = "membership_expiry_alerts"
    private const val CHANNEL_NAME = "Membership expiry alerts"

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminders for gym and swimming membership expiry"
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showExpiryNotification(context: Context, member: MemberEntity) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            member.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val body = "${member.fullName} - ${member.category} membership expires on ${
            member.endDate().format(dateFormatter)
        }"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.launcher_foreground))
            .setContentTitle("\u26A0\uFE0F Membership Expiring")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            (member.id * 31 + member.endDateEpochDay).toInt().absoluteValue,
            notification
        )
    }
}
