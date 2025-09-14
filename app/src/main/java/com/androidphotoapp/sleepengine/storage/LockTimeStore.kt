package com.androidphotoapp.sleepengine.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object LockTimeStore {

  private const val PREFS_NAME = "sleep_prefs"
  private const val KEY_LOCK_TIME = "lock_time"

  fun saveLockTime(context: Context, timeMillis: Long) {
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit { putLong(KEY_LOCK_TIME, timeMillis) }
  }

  fun getLockTime(context: Context): Long {
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getLong(KEY_LOCK_TIME, 0L)
  }

  fun clearLockTime(context: Context) {
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit { remove(KEY_LOCK_TIME) }
  }
}