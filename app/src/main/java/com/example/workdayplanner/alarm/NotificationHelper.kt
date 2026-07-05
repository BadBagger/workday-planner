package com.example.workdayplanner.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {
    const val CHANNEL_ID = "task_deadlines"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Task deadlines",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Deadline and reminder alarms for work tasks."
        }
        manager.createNotificationChannel(channel)
    }
}
