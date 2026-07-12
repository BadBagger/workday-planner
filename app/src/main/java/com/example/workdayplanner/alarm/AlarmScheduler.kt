package com.example.workdayplanner.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.AlarmClock
import android.util.Log
import com.example.workdayplanner.data.AlarmDelivery
import com.example.workdayplanner.data.AlarmCancelStatus
import com.example.workdayplanner.data.AlarmDispatchStatus
import com.example.workdayplanner.data.TaskItem
import java.time.ZoneId

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(task: TaskItem): AlarmDispatchStatus {
        if (task.completed) return AlarmDispatchStatus.NoAlarmRequested
        val alarmAt = task.alarmAt ?: task.deadline ?: return AlarmDispatchStatus.NoAlarmRequested
        var systemClockFallback = false
        if (task.alarmDelivery == AlarmDelivery.SystemClockAlarm) {
            when (dispatchSystemClockAlarm(task, alarmAt)) {
                AlarmDispatchStatus.SentToSystemClock -> return AlarmDispatchStatus.SentToSystemClock
                AlarmDispatchStatus.NoClockAppAvailable -> systemClockFallback = true
                else -> systemClockFallback = true
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.d(TAG, "Exact alarm access needed. taskHash=${task.id.hashCode()}")
            return AlarmDispatchStatus.ExactAlarmAccessNeeded
        }
        val triggerAt = if (alarmAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() <= System.currentTimeMillis()) {
            task.deadline?.takeIf { it.isAfter(alarmAt) } ?: alarmAt
        } else {
            alarmAt
        }
        val triggerAtMillis = triggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (triggerAtMillis <= System.currentTimeMillis()) {
            Log.d(TAG, "Skipped reminder because trigger time is in the past. taskHash=${task.id.hashCode()}")
            return AlarmDispatchStatus.SkippedPastAlarm
        }

        val intent = Intent(context, TaskAlarmReceiver::class.java)
            .putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, task.id)
            .putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, task.title)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleAlarm(triggerAtMillis, pendingIntent)
        Log.d(TAG, "Scheduled reminder. taskHash=${task.id.hashCode()}")
        return if (systemClockFallback) {
            AlarmDispatchStatus.SystemClockFallbackScheduled
        } else if (task.alarmDelivery == AlarmDelivery.StandardNotification) {
            AlarmDispatchStatus.ScheduledNotification
        } else {
            AlarmDispatchStatus.ScheduledInApp
        }
    }

    fun cancel(taskId: String) {
        val intent = Intent(context, TaskAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled reminder. taskHash=${taskId.hashCode()}")
        }
    }

    fun cancel(task: TaskItem): AlarmCancelStatus {
        cancel(task.id)
        if (task.alarmDelivery != AlarmDelivery.SystemClockAlarm || task.alarmAt == null) {
            return AlarmCancelStatus.AppAlarmCancelled
        }
        val label = task.alarmLabel.ifBlank { "Workday Planner - ${task.title}" }
        val dismissIntent = Intent(AlarmClock.ACTION_DISMISS_ALARM).apply {
            putExtra(AlarmClock.EXTRA_ALARM_SEARCH_MODE, AlarmClock.ALARM_SEARCH_MODE_LABEL)
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (dismissIntent.resolveActivity(context.packageManager) == null) {
            Log.d(TAG, "No Clock dismiss handler. taskHash=${task.id.hashCode()}")
            return AlarmCancelStatus.SystemClockDismissUnavailable
        }
        return runCatching {
            context.startActivity(dismissIntent)
            Log.d(TAG, "Dispatched system Clock alarm dismiss. taskHash=${task.id.hashCode()}")
            AlarmCancelStatus.SystemClockDismissSent
        }.getOrElse {
            Log.d(TAG, "System Clock alarm dismiss failed. taskHash=${task.id.hashCode()}")
            AlarmCancelStatus.SystemClockDismissUnavailable
        }
    }

    fun rescheduleOpenTasks(tasks: List<TaskItem>) {
        tasks.forEach { task ->
            cancel(task.id)
            schedule(task)
        }
    }

    private fun dispatchSystemClockAlarm(task: TaskItem, alarmAt: java.time.LocalDateTime): AlarmDispatchStatus? {
        val label = task.alarmLabel.ifBlank { "Workday Planner - ${task.title}" }
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, alarmAt.hour)
            putExtra(AlarmClock.EXTRA_MINUTES, alarmAt.minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_VIBRATE, true)
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) == null) {
            Log.d(TAG, "No Clock app handled system alarm. taskHash=${task.id.hashCode()}")
            return AlarmDispatchStatus.NoClockAppAvailable
        }
        return runCatching {
            context.startActivity(intent)
            Log.d(TAG, "Dispatched system Clock alarm. taskHash=${task.id.hashCode()}")
            AlarmDispatchStatus.SentToSystemClock
        }.getOrElse {
            Log.d(TAG, "System Clock alarm dispatch failed. taskHash=${task.id.hashCode()}")
            AlarmDispatchStatus.NoClockAppAvailable
        }
    }

    private fun scheduleAlarm(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            return
        }
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent),
            pendingIntent
        )
    }

    private companion object {
        const val TAG = "WorkdayPlannerAlarm"
    }
}
