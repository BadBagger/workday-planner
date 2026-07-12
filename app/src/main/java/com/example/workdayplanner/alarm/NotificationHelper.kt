package com.example.workdayplanner.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.provider.Settings

object NotificationHelper {
    const val CHANNEL_ID = "task_full_screen_alarms"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Task alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Full-screen alarms for work task deadlines and reminders."
            setSound(
                Settings.System.DEFAULT_ALARM_ALERT_URI,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 700, 350, 700, 350, 1200)
        }
        manager.createNotificationChannel(channel)
    }
}
