package com.taskmesh.app.data

import android.app.NotificationManager
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NudgeScheduler {
    private const val WORK_NAME = "TaskMeshNudgeWorker"

    fun scheduleNudgeWorker(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<TaskMeshNudgeWorker>(6, TimeUnit.HOURS)
            .addTag("TaskMeshNudgeWorkerTag")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun cancelNudgeWorker(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(TaskMeshNudgeWorker.NOTIFICATION_ID)
    }
}
