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
import com.androidphotoapp.sleepengine.storage.SensorDataStore
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

        stopSensorService(context)
      }
    }
  }

  private fun startSensorService(context: Context) {
    val serviceIntent = Intent(context, SleepSensorService::class.java)
    Log.d("ScreenReceiver", "started foreground service")
    androidx.core.content.ContextCompat.startForegroundService(context, serviceIntent)
  }

  private fun stopSensorService(context: Context) {
    val serviceIntent = Intent(context, SleepSensorService::class.java)
    Log.d("ScreenReceiver", "Stopping SleepSensorService")
    context.stopService(serviceIntent)
  }

  /** Fetch sensor data between start and end time and calculate sleep score */
  private fun calculateSleepScoreAndSave(startTime: Long, endTime: Long, context: Context): Int {
    val motionData = SensorDataStore.getMotionData(context, startTime, endTime)
    val lightData = SensorDataStore.getLightData(context, startTime, endTime)
    val audioData = SensorDataStore.getAudioData(context, startTime, endTime)

    return calculateSleepScore(motionData, lightData, audioData)
  }

  /** Calculate sleep score based on motion, light, and audio */
  private fun calculateSleepScore(
    motionData: List<Float>,
    lightData: List<Float>,
    audioData: List<Float>
  ): Int {
    // Motion score: 0-5 (less movement -> higher score)
    val motionScore = (5 - motionData.size.coerceAtMost(5))

    // Light score: 0-3 (darker -> higher score)
    val lightScore = if (lightData.all { it < 5f }) 3 else 0

    // Audio score: 0-2 (quieter -> higher score)
    val audioScore = if (audioData.all { it < 2000f }) 2 else 0

    return (motionScore + lightScore + audioScore).coerceAtMost(10)
  }

  /** Check sleep and save sleep log */
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

      val startTime = lockTimeMillis + SleepConstants.MIN_SLEEP_DURATION_MILLIS
      val endTime = System.currentTimeMillis()
      val sleepScore = calculateSleepScoreAndSave(startTime, endTime, context)

      SleepLogStore.saveLog(context, SleepLog(startTime, endTime, sleepScore))
      SensorDataStore.clearData(context)

      Log.d(
        "ScreenReceiver",
        "Sleep log saved: Start=$startTime, End=$endTime, Score=$sleepScore"
      )
    }
  }
}
