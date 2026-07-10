package com.example.workdayplanner.alarm

import android.app.PendingIntent
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.workdayplanner.MainActivity
import com.example.workdayplanner.R
import com.example.workdayplanner.data.PlannerRepository
import com.example.workdayplanner.data.RepeatRule
import com.example.workdayplanner.data.TaskRecurrence

class TaskAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra(EXTRA_TASK_ID).orEmpty()
        val repository = PlannerRepository(context)
        val task = repository.state.value.tasks.firstOrNull { it.id == taskId } ?: return
        if (task.completed) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val title = intent.getStringExtra(EXTRA_TASK_TITLE).orEmpty().ifBlank { "Task deadline" }
        val contentIntent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(MainActivity.EXTRA_OPEN_TASK_ID, taskId)
        val pendingContentIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val fullScreenIntent = Intent(context, TaskAlarmActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(EXTRA_TASK_ID, taskId)
            .putExtra(EXTRA_TASK_TITLE, title)
        val pendingFullScreenIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode() xor FULL_SCREEN_REQUEST_MASK,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText("Full alarm: this task needs your attention.")
            .setContentIntent(pendingContentIntent)
            .setFullScreenIntent(pendingFullScreenIntent, true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(Settings.System.DEFAULT_ALARM_ALERT_URI)
            .setVibrate(longArrayOf(0, 700, 350, 700, 350, 1200))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(taskId.hashCode(), notification)

        if (task.repeatRule != RepeatRule.None) {
            TaskRecurrence.nextOccurrence(task, repository.state.value)?.let { next ->
                val alreadyExists = repository.state.value.tasks.any {
                    it.id != task.id && it.title == next.title && it.deadline == next.deadline
                }
                if (!alreadyExists) {
                    repository.upsertTask(next)
                    AlarmScheduler(context).schedule(next)
                }
            }
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TASK_TITLE = "task_title"
        private const val FULL_SCREEN_REQUEST_MASK = 0x51F7
    }
}
