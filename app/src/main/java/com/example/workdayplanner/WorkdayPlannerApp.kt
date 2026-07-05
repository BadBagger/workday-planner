package com.example.workdayplanner

import android.app.Application
import com.example.workdayplanner.alarm.NotificationHelper

class WorkdayPlannerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
    }
}
