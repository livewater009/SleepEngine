package com.androidphotoapp.sleepengine.storage

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class SleepLog(
  val startTime: Long,
  val endTime: Long,
  val sleepScore: Int // e.g., 0-100
)

object SleepLogStore {
  private const val PREFS_NAME = "sleep_logs"
  private const val KEY_LOGS = "logs"
  private const val TAG = "SleepLogStore"

  private val gson = Gson()

  fun saveLog(context: Context, log: SleepLog) {
    val logs = getLogs(context).toMutableList()
    logs.add(log)
    val json = gson.toJson(logs)
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit { putString(KEY_LOGS, json) }
    Log.d(TAG, "Saved SleepLog: $log")
  }

  fun getLogs(context: Context): List<SleepLog> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val json = prefs.getString(KEY_LOGS, null) ?: return emptyList()
    val type = object : TypeToken<List<SleepLog>>() {}.type
    return gson.fromJson(json, type)
  }

  fun clearLogs(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit { clear() }
    Log.d(TAG, "Cleared all sleep logs")
  }
}
