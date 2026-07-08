package com.example.workdayplanner.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.workdayplanner.data.PlannerRepository

class TaskReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action !in supportedActions) return
        val repository = PlannerRepository(context)
        AlarmScheduler(context).rescheduleOpenTasks(repository.state.value.tasks)
    }

    companion object {
        private val supportedActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED
        )
    }
}
