package com.taskmesh.app.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TaskMeshBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED && context != null) {
            val prefs = PreferencesManager(context)
            if (prefs.areNotificationsEnabled()) {
                NudgeScheduler.scheduleNudgeWorker(context)
            }
        }
    }
}
