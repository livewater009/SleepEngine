package com.androidphotoapp.sleepengine.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.androidphotoapp.sleepengine.SleepConstants
import com.androidphotoapp.sleepengine.service.SleepSensorService
import com.androidphotoapp.sleepengine.storage.LockTimeStore
import com.androidphotoapp.sleepengine.storage.SleepLog
import com.androidphotoapp.sleepengine.storage.SleepLogStore
import java.util.Calendar

class ScreenReceiver : BroadcastReceiver() {

  @RequiresApi(Build.VERSION_CODES.O)
  override fun onReceive(context: Context?, intent: Intent?) {
    if (context == null) return

    when (intent?.action) {

      Intent.ACTION_SCREEN_OFF -> {
        val lockTime = System.currentTimeMillis()
        LockTimeStore.saveLockTime(context, lockTime)
        Log.d("ScreenReceiver", "Screen OFF (Locked) at $lockTime")

        // Start sensor tracking
        startSensorService(context)
      }

      Intent.ACTION_SCREEN_ON -> {
        val unlockTime = System.currentTimeMillis()
        val lockTime = LockTimeStore.getLockTime(context)

        if (lockTime != 0L) {
          val durationMillis = unlockTime - lockTime
          checkSleep(durationMillis, lockTime, context)
          LockTimeStore.clearLockTime(context)
        }

        Log.d("ScreenReceiver", "Screen UNLOCKED at $unlockTime")

        // Stop sensor tracking
        stopSensorService(context)
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun startSensorService(context: Context) {
    val serviceIntent = Intent(context, SleepSensorService::class.java)
    Log.d("ScreenReceiver", "Starting SleepSensorService")
    context.startForegroundService(serviceIntent)
  }

  private fun stopSensorService(context: Context) {
    val serviceIntent = Intent(context, SleepSensorService::class.java)
    Log.d("ScreenReceiver", "Stopping SleepSensorService")
    context.stopService(serviceIntent)
  }

  private fun checkSleep(durationMillis: Long, lockTimeMillis: Long, context: Context) {
    val calendar = Calendar.getInstance().apply { timeInMillis = lockTimeMillis }
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    Log.d("ScreenReceiver", "Lock Time: $hour:$minute")

    val lockTimeMinutes = hour * 60 + minute
    val sleepStartMinutes = SleepConstants.SLEEP_START_HOUR * 60 + SleepConstants.SLEEP_START_MINUTE

    if (lockTimeMinutes >= sleepStartMinutes &&
      durationMillis >= SleepConstants.MIN_SLEEP_DURATION_MILLIS
    ) {
      val durationMins = (durationMillis / 60000).toInt()
      Log.d("ScreenReceiver", "User is sleeping! Duration: $durationMins mins")

      // ✅ Corrected start and end time
      val startTime = lockTimeMillis + SleepConstants.MIN_SLEEP_DURATION_MILLIS
      val endTime = System.currentTimeMillis()
      val sleepScore = 10 // simple fixed score

      SleepLogStore.saveLog(context, SleepLog(startTime, endTime, sleepScore))
      Log.d(
        "ScreenReceiver",
        "Sleep log saved: Start=$startTime, End=$endTime, Score=$sleepScore"
      )
    }
  }
}
