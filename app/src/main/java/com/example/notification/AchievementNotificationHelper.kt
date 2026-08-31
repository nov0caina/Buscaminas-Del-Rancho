package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AchievementEntity

object AchievementNotificationHelper {

    const val CHANNEL_ID = "rancho_achievements"
    const val EXTRA_DESTINATION = "destination"
    const val DESTINATION_ACHIEVEMENTS = "achievements"
    const val EXTRA_ACHIEVEMENT_ID = "achievement_id"
    const val EXTRA_ACHIEVEMENT_TITLE = "achievement_title"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val existing = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "🏆 Logros del Rancho",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificaciones y felicitaciones al desbloquear logros en el rancho"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 150, 80, 150, 80, 300)
                    setShowBadge(true)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    fun sendAchievementNotification(context: Context, achievement: AchievementEntity) {
        try {
            val notificationManagerCompat = NotificationManagerCompat.from(context)
            if (!notificationManagerCompat.areNotificationsEnabled()) {
                return
            }

            createNotificationChannel(context)

            val tapIntent = Intent(context, MainActivity::class.java).apply {
                action = "ACTION_VIEW_ACHIEVEMENT_${achievement.id}"
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_DESTINATION, DESTINATION_ACHIEVEMENTS)
                putExtra(EXTRA_ACHIEVEMENT_ID, achievement.id)
                putExtra(EXTRA_ACHIEVEMENT_TITLE, achievement.title)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                achievement.id.hashCode(),
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("🏆 ¡Logro Desbloqueado: ${achievement.title}! ✨")
                .setContentText("¡Ajúa vaquero! ${achievement.description}")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .setBigContentTitle("🏆 ¡Logro Desbloqueado: ${achievement.title}! ✨")
                        .bigText("¡Ajúa vaquero! Conseguiste \"${achievement.title}\".\n${achievement.description}\n\n👉 Toca aquí para ver tu colección de trofeos.")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setVibrate(longArrayOf(0, 150, 80, 150, 80, 300))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManagerCompat.notify(achievement.id.hashCode(), notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
