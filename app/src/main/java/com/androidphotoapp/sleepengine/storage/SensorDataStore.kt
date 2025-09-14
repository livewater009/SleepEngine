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

  fun saveMotionData(context: Context, x: Float, y: Float, z: Float) {
    Log.d(TAG, "Saving Motion Data -> x: $x, y: $y, z: $z")
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val previous = prefs.getString(KEY_MOTION, "") ?: ""
    prefs.edit { putString(KEY_MOTION, "$previous$x,$y,$z;") }
  }

  fun saveLightData(context: Context, light: Float) {
    Log.d(TAG, "Saving Light Data -> light: $light")
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val previous = prefs.getString(KEY_LIGHT, "") ?: ""
    prefs.edit { putString(KEY_LIGHT, "$previous$light;") }
  }

  fun saveAudioAmplitude(context: Context, amplitude: Float) {
    Log.d(TAG, "Saving Audio Amplitude -> $amplitude")
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val previous = prefs.getString(KEY_AUDIO, "") ?: ""
    prefs.edit { putString(KEY_AUDIO, "$previous$amplitude;") }
  }

  fun getMotionData(context: Context): String? {
    val data = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .getString(KEY_MOTION, "")
    Log.d(TAG, "Retrieved Motion Data: $data")
    return data
  }

  fun getLightData(context: Context): String? {
    val data = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .getString(KEY_LIGHT, "")
    Log.d(TAG, "Retrieved Light Data: $data")
    return data
  }

  fun getAudioData(context: Context): String? {
    val data = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .getString(KEY_AUDIO, "")
    Log.d(TAG, "Retrieved Audio Data: $data")
    return data
  }

  fun clearData(context: Context) {
    Log.d(TAG, "Clearing all sensor data")
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit { clear() }
  }
}
