package com.androidphotoapp.sleepengine

import android.content.Context
import android.util.Log
import com.androidphotoapp.sleepengine.storage.SleepLog
import com.androidphotoapp.sleepengine.storage.SleepLogStore
import java.util.Calendar

object SleepUtils {
  fun checkSleep(durationMillis: Long, lockTimeMillis: Long, context: Context) {
    val calendar = Calendar.getInstance().apply { timeInMillis = lockTimeMillis }
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val lockTimeMinutes = hour * 60 + minute
    val sleepStartMinutes = SleepConstants.SLEEP_START_HOUR * 60 + SleepConstants.SLEEP_START_MINUTE

    if (lockTimeMinutes >= sleepStartMinutes && durationMillis >= SleepConstants.MIN_SLEEP_DURATION_MILLIS) {
      val startTime = lockTimeMillis + SleepConstants.MIN_SLEEP_DURATION_MILLIS
      val endTime = System.currentTimeMillis()
      val sleepScore = 0

      SleepLogStore.saveLog(context, SleepLog(startTime, endTime, sleepScore))
      Log.d("SleepUtils", "Sleep log saved: Start=$startTime, End=$endTime, Score=$sleepScore")
    }
  }
}
