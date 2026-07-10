package com.example.workdayplanner.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.workdayplanner.data.ShiftAlarmSettings
import com.example.workdayplanner.data.WorkShift
import java.time.LocalDateTime
import java.time.ZoneId

class ShiftAlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun reschedule(shifts: List<WorkShift>, settings: ShiftAlarmSettings) {
        shifts.forEach { cancel(it.id) }
        if (!settings.enabled) return
        val now = LocalDateTime.now()
        shifts
            .asSequence()
            .filter { shift -> shift.startDateTime().isAfter(now) }
            .filter { shift -> !settings.onlyEarlyShifts || shift.start.hour < settings.earlyShiftCutoffHour }
            .take(MAX_SCHEDULED_SHIFT_ALARMS)
            .forEach { schedule(it, settings) }
    }

    fun cancel(shiftId: String) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode(shiftId),
            Intent(context, ShiftAlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled shift alarm. shiftHash=${shiftId.hashCode()}")
        }
    }

    private fun schedule(shift: WorkShift, settings: ShiftAlarmSettings) {
        val alarmAt = shift.startDateTime().minusMinutes(settings.offsetMinutes.toLong())
        val triggerAtMillis = alarmAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (triggerAtMillis <= System.currentTimeMillis()) return
        val intent = Intent(context, ShiftAlarmReceiver::class.java)
            .putExtra(ShiftAlarmReceiver.EXTRA_SHIFT_ID, shift.id)
            .putExtra(ShiftAlarmReceiver.EXTRA_SHIFT_TITLE, shift.label.ifBlank { "Work shift" })
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode(shift.id),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
        Log.d(TAG, "Scheduled shift alarm. shiftHash=${shift.id.hashCode()}")
    }

    private fun WorkShift.startDateTime(): LocalDateTime = LocalDateTime.of(date, start)

    private fun requestCode(shiftId: String): Int = shiftId.hashCode() xor SHIFT_ALARM_MASK

    private companion object {
        const val TAG = "WorkdayShiftAlarm"
        const val SHIFT_ALARM_MASK = 0x5A17
        const val MAX_SCHEDULED_SHIFT_ALARMS = 60
    }
}
