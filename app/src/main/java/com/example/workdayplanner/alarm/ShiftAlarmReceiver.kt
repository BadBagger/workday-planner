package com.example.workdayplanner.alarm

import android.Manifest
import android.app.PendingIntent
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
import java.time.format.DateTimeFormatter

class ShiftAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val shiftId = intent.getStringExtra(EXTRA_SHIFT_ID).orEmpty()
        val repository = PlannerRepository(context)
        val settings = repository.state.value.shiftAlarmSettings
        if (!settings.enabled) return
        val shift = repository.state.value.shifts.firstOrNull { it.id == shiftId } ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val shiftTitle = intent.getStringExtra(EXTRA_SHIFT_TITLE).orEmpty().ifBlank { shift.label.ifBlank { "Work shift" } }
        val title = "Shift alarm: $shiftTitle"
        val content = "Starts at ${shift.start.format(timeFormatter)}."
        val contentIntent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingContentIntent = PendingIntent.getActivity(
            context,
            shiftId.hashCode() xor CONTENT_MASK,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val fullScreenIntent = Intent(context, TaskAlarmActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(TaskAlarmActivity.EXTRA_ALARM_ID, "shift_$shiftId")
            .putExtra(TaskAlarmActivity.EXTRA_ALARM_TITLE, title)
            .putExtra(TaskAlarmActivity.EXTRA_ALARM_HEADING, "Shift alarm")
            .putExtra(TaskAlarmActivity.EXTRA_ALARM_MESSAGE, content)
            .putExtra(TaskAlarmActivity.EXTRA_OPEN_BUTTON_LABEL, "Open app")
            .putExtra(TaskAlarmActivity.EXTRA_SNOOZE_RECEIVER, TaskAlarmActivity.SNOOZE_SHIFT)
        val pendingFullScreenIntent = PendingIntent.getActivity(
            context,
            shiftId.hashCode() xor FULL_SCREEN_MASK,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pendingContentIntent)
            .setFullScreenIntent(pendingFullScreenIntent, true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(Settings.System.DEFAULT_ALARM_ALERT_URI)
            .setVibrate(longArrayOf(0, 700, 350, 700, 350, 1200))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(("shift_$shiftId").hashCode(), notification)
    }

    companion object {
        const val EXTRA_SHIFT_ID = "shift_id"
        const val EXTRA_SHIFT_TITLE = "shift_title"
        private const val CONTENT_MASK = 0x4417
        private const val FULL_SCREEN_MASK = 0x7717
        private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    }
}
