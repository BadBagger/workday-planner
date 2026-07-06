package com.example.workdayplanner

import android.app.Application
import com.example.workdayplanner.alarm.NotificationHelper
import com.example.workdayplanner.widget.PlannerWidgetUpdater

class WorkdayPlannerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
        PlannerWidgetUpdater.updateAll(this)
    }
}
