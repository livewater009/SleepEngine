package com.androidphotoapp.sleepengine.worker

import android.content.Context
import android.os.PowerManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.androidphotoapp.sleepengine.SleepConstants
import com.androidphotoapp.sleepengine.SleepUtils
import com.androidphotoapp.sleepengine.storage.LockTimeStore
import com.androidphotoapp.sleepengine.storage.ScreenStateStore
import java.util.concurrent.TimeUnit

class ScreenCheckWorker(
  context: Context,
  workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    Log.e("ScreenCheckWorker", "🔥 Running ScreenCheckWorker")

    val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager

    val isScreenOn = powerManager.isInteractive

    val previousState = ScreenStateStore.getLastState(applicationContext)

    Log.i("ScreenCheckWorker", "isScreenOn = $isScreenOn")
    Log.i("ScreenCheckWorker", "previousState = $previousState")

    // Screen turned OFF
    if (!isScreenOn && previousState) {
      val lockTime = System.currentTimeMillis()
      LockTimeStore.saveLockTime(applicationContext, lockTime)
      Log.i("ScreenCheckWorker", "Screen OFF at $lockTime")
    }

    // Screen turned ON
    if (isScreenOn && !previousState) {
      val unlockTime = System.currentTimeMillis()
      val lockTime = LockTimeStore.getLockTime(applicationContext)

      if (lockTime != 0L) {
        val durationMillis = unlockTime - lockTime

        SleepUtils.checkSleep(durationMillis, lockTime, applicationContext)
        LockTimeStore.clearLockTime(applicationContext)
      }

      Log.i("ScreenCheckWorker", "Screen ON at $unlockTime")
    }

    // Update last state
    ScreenStateStore.setLastState(applicationContext, isScreenOn)

    Log.e("ScreenCheckWorker", "🔥 Reschedule next work")
    // Reschedule next worker
    scheduleNextWorker(SleepConstants.WORK_INTERVAL)

    Log.e("ScreenCheckWorker", "🔥 Completed")
    return Result.success()
  }

  /** Private helper to schedule the next worker run */
  private fun scheduleNextWorker(intervalMinutes: Int) {
    val work = OneTimeWorkRequestBuilder<ScreenCheckWorker>()
      .setInitialDelay(intervalMinutes.toLong(), TimeUnit.MINUTES)
      .build()

    WorkManager.getInstance(applicationContext).enqueueUniqueWork(
      "sleep_work_chain",
      ExistingWorkPolicy.REPLACE,
      listOf(work) // must wrap in list
    )

    Log.e("ScreenCheckWorker", "🔥 Rescheduled correctly.")
  }
}
