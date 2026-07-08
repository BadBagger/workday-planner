package com.example.workdayplanner.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.workdayplanner.data.TaskItem
import java.time.ZoneId

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(task: TaskItem) {
        if (task.completed) return
        val alarmAt = task.alarmAt ?: task.deadline ?: return
        val triggerAt = if (alarmAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() <= System.currentTimeMillis()) {
            task.deadline?.takeIf { it.isAfter(alarmAt) } ?: alarmAt
        } else {
            alarmAt
        }
        val triggerAtMillis = triggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (triggerAtMillis <= System.currentTimeMillis()) {
            Log.d(TAG, "Skipped reminder because trigger time is in the past. taskHash=${task.id.hashCode()}")
            return
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

    fun rescheduleOpenTasks(tasks: List<TaskItem>) {
        tasks.forEach { task ->
            cancel(task.id)
            schedule(task)
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
        // If exact alarms are not available, keep the reminder rather than failing silently.
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    private companion object {
        const val TAG = "WorkdayPlannerAlarm"
    }
}
