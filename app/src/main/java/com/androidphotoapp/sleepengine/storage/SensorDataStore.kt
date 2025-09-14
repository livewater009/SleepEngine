package com.androidphotoapp.sleepengine.storage

import android.content.Context
import android.util.Log
import androidx.core.content.edit

object SensorDataStore {
  private const val PREFS_NAME = "sensor_data"
  private const val KEY_MOTION = "motion_data"
  private const val KEY_LIGHT = "light_data"
  private const val KEY_AUDIO = "audio_data"
  private const val TAG = "SensorDataStore"

  /** Save motion data with timestamp */
  fun saveMotionData(context: Context, x: Float, y: Float, z: Float) {
    val timestamp = System.currentTimeMillis()
    val entry = "$timestamp:$x,$y,$z;"
    Log.d(TAG, "Saving Motion Data -> $entry")
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val previous = prefs.getString(KEY_MOTION, "") ?: ""
    prefs.edit { putString(KEY_MOTION, previous + entry) }
  }

  /** Save light data with timestamp */
  fun saveLightData(context: Context, light: Float) {
    val timestamp = System.currentTimeMillis()
    val entry = "$timestamp:$light;"
    Log.d(TAG, "Saving Light Data -> $entry")
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val previous = prefs.getString(KEY_LIGHT, "") ?: ""
    prefs.edit { putString(KEY_LIGHT, previous + entry) }
  }

  /** Save audio amplitude with timestamp */
  fun saveAudioAmplitude(context: Context, amplitude: Float) {
    val timestamp = System.currentTimeMillis()
    val entry = "$timestamp:$amplitude;"
    Log.d(TAG, "Saving Audio Amplitude -> $entry")
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val previous = prefs.getString(KEY_AUDIO, "") ?: ""
    prefs.edit { putString(KEY_AUDIO, previous + entry) }
  }

  /** Get motion data within a time range */
  fun getMotionData(context: Context, startTime: Long, endTime: Long): List<Float> {
    val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .getString(KEY_MOTION, "") ?: return emptyList()
    return raw.split(";")
      .mapNotNull {
        val parts = it.split(":")
        if (parts.size != 2) return@mapNotNull null
        val timestamp = parts[0].toLongOrNull() ?: return@mapNotNull null
        if (timestamp !in startTime..endTime) return@mapNotNull null
        val values = parts[1].split(",").mapNotNull { it.toFloatOrNull() }
        values.average().toFloat() // simple average of x, y, z
      }
  }

  /** Get light data within a time range */
  fun getLightData(context: Context, startTime: Long, endTime: Long): List<Float> {
    val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .getString(KEY_LIGHT, "") ?: return emptyList()
    return raw.split(";")
      .mapNotNull {
        val parts = it.split(":")
        val timestamp = parts.getOrNull(0)?.toLongOrNull() ?: return@mapNotNull null
        if (timestamp !in startTime..endTime) return@mapNotNull null
        parts.getOrNull(1)?.toFloatOrNull()
      }
  }

  /** Get audio amplitude data within a time range */
  fun getAudioData(context: Context, startTime: Long, endTime: Long): List<Float> {
    val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .getString(KEY_AUDIO, "") ?: return emptyList()
    return raw.split(";")
      .mapNotNull {
        val parts = it.split(":")
        val timestamp = parts.getOrNull(0)?.toLongOrNull() ?: return@mapNotNull null
        if (timestamp !in startTime..endTime) return@mapNotNull null
        parts.getOrNull(1)?.toFloatOrNull()
      }
  }

  /** Clear all sensor data */
  fun clearData(context: Context) {
    Log.d(TAG, "Clearing all sensor data")
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit { clear() }
  }
}
