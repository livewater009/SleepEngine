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
    val sleepStartMinutes = SleepConstants.SLEEP_START_HOUR * 60
    val sleepEndMinutes = SleepConstants.SLEEP_END_HOUR * 60

    val isOvernight = sleepEndMinutes < sleepStartMinutes

    val inSleepWindow = if (isOvernight) {
      // Sleep window crosses midnight: e.g., 18:00 - 07:00
      lockTimeMinutes >= sleepStartMinutes || lockTimeMinutes <= sleepEndMinutes
    } else {
      // Normal case: sleep window does not cross midnight
      lockTimeMinutes in sleepStartMinutes..sleepEndMinutes
    }

    if (inSleepWindow && durationMillis >= SleepConstants.MIN_SLEEP_DURATION_MILLIS) {
      val startTime = lockTimeMillis + SleepConstants.MIN_SLEEP_DURATION_MILLIS
      val endTime = System.currentTimeMillis()
      val sleepScore = 0

      SleepLogStore.saveLog(context, SleepLog(startTime, endTime, sleepScore))
      Log.e("SleepUtils", "Sleep log saved: Start=$startTime, End=$endTime, Score=$sleepScore")
    }
  }
}
