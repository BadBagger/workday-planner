package com.example.workdayplanner.alarm

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.workdayplanner.MainActivity
import com.example.workdayplanner.ui.WorkdayPlannerTheme
import java.util.concurrent.TimeUnit

class TaskAlarmActivity : ComponentActivity() {
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var alarmId: String = ""
    private var alarmTitle: String = ""
    private var alarmHeading: String = "Task alarm"
    private var alarmMessage: String = "This reminder is ringing because you set an alarm."
    private var openButtonLabel: String = "Open task"
    private var snoozeReceiver: String = SNOOZE_TASK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showAboveLockScreen()
        alarmId = intent.getStringExtra(EXTRA_ALARM_ID)
            ?: intent.getStringExtra(TaskAlarmReceiver.EXTRA_TASK_ID)
            ?: ""
        alarmTitle = intent.getStringExtra(EXTRA_ALARM_TITLE)
            ?: intent.getStringExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE)
            ?: "Task alarm"
        alarmHeading = intent.getStringExtra(EXTRA_ALARM_HEADING) ?: alarmHeading
        alarmMessage = intent.getStringExtra(EXTRA_ALARM_MESSAGE) ?: alarmMessage
        openButtonLabel = intent.getStringExtra(EXTRA_OPEN_BUTTON_LABEL) ?: openButtonLabel
        snoozeReceiver = intent.getStringExtra(EXTRA_SNOOZE_RECEIVER) ?: snoozeReceiver
        startAlarmSignal()

        setContent {
            WorkdayPlannerTheme {
                TaskAlarmScreen(
                    heading = alarmHeading,
                    title = alarmTitle,
                    message = alarmMessage,
                    openButtonLabel = openButtonLabel,
                    onOpenTask = ::openTask,
                    onSnooze = { snooze(minutes = 10) },
                    onDismiss = ::dismissAlarm
                )
            }
        }
    }

    override fun onDestroy() {
        stopAlarmSignal()
        super.onDestroy()
    }

    private fun showAboveLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun startAlarmSignal() {
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            }
            play()
        }
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        val pattern = longArrayOf(0, 700, 350, 700, 350, 1200)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 1)
        }
    }

    private fun stopAlarmSignal() {
        ringtone?.stop()
        ringtone = null
        vibrator?.cancel()
        vibrator = null
        if (alarmId.isNotBlank()) {
            getSystemService(NotificationManager::class.java).cancel(alarmId.hashCode())
        }
    }

    private fun dismissAlarm() {
        stopAlarmSignal()
        finish()
    }

    private fun snooze(minutes: Long) {
        stopAlarmSignal()
        if (alarmId.isNotBlank()) {
            val intent = if (snoozeReceiver == SNOOZE_SHIFT) {
                Intent(this, ShiftAlarmReceiver::class.java)
                    .putExtra(ShiftAlarmReceiver.EXTRA_SHIFT_ID, alarmId.removePrefix("shift_"))
                    .putExtra(ShiftAlarmReceiver.EXTRA_SHIFT_TITLE, alarmTitle.removePrefix("Shift alarm: ").trim())
            } else {
                Intent(this, TaskAlarmReceiver::class.java)
                    .putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, alarmId)
                    .putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, alarmTitle)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                alarmId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val triggerAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minutes)
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        }
        finish()
    }

    private fun openTask() {
        stopAlarmSignal()
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        if (snoozeReceiver != SNOOZE_SHIFT) {
            intent.putExtra(MainActivity.EXTRA_OPEN_TASK_ID, alarmId)
        }
        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_ALARM_TITLE = "alarm_title"
        const val EXTRA_ALARM_HEADING = "alarm_heading"
        const val EXTRA_ALARM_MESSAGE = "alarm_message"
        const val EXTRA_OPEN_BUTTON_LABEL = "open_button_label"
        const val EXTRA_SNOOZE_RECEIVER = "snooze_receiver"
        const val SNOOZE_TASK = "task"
        const val SNOOZE_SHIFT = "shift"
    }
}

@Composable
private fun TaskAlarmScreen(
    heading: String,
    title: String,
    message: String,
    openButtonLabel: String,
    onOpenTask: () -> Unit,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = heading,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onOpenTask,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(openButtonLabel)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onSnooze,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Snooze 10 minutes", textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dismiss")
            }
        }
    }
}
