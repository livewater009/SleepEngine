package com.androidphotoapp.sleepengine.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.androidphotoapp.sleepengine.R
import com.androidphotoapp.sleepengine.storage.SensorDataStore

class SleepSensorService : Service(), SensorEventListener {

  private lateinit var sensorManager: SensorManager
  private var accelerometer: Sensor? = null
  private var lightSensor: Sensor? = null

  private val CHANNEL_ID = "sleep_sensor_channel"

  override fun onCreate() {
    super.onCreate()

    sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    startForeground(1, createNotification())
    startTracking()
  }

  override fun onDestroy() {
    super.onDestroy()
    stopTracking()
    Log.d("SleepSensorService", "Service destroyed")
  }

  private fun startTracking() {
    accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    lightSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    Log.d("SleepSensorService", "Started tracking sensors")
  }

  private fun stopTracking() {
    sensorManager.unregisterListener(this)
    Log.d("SleepSensorService", "Stopped tracking sensors")
  }

  override fun onSensorChanged(event: SensorEvent?) {
    event?.let {
      when (it.sensor.type) {
        Sensor.TYPE_ACCELEROMETER -> {
          val x = it.values[0]
          val y = it.values[1]
          val z = it.values[2]
          Log.d("SleepSensorService", "Accelerometer: x=$x, y=$y, z=$z")
          SensorDataStore.saveMotionData(applicationContext, x, y, z)
        }
        Sensor.TYPE_LIGHT -> {
          val light = it.values[0]
          Log.d("SleepSensorService", "Light: $light")
          SensorDataStore.saveLightData(applicationContext, light)
        }
      }
    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

  override fun onBind(intent: Intent?): IBinder? = null

  private fun createNotification(): Notification {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Sleep Sensor Service",
        NotificationManager.IMPORTANCE_LOW
      )
      val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.createNotificationChannel(channel)
    }

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("Sleep Tracking")
      .setContentText("Tracking motion & light sensors")
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setOngoing(true)
      .build()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    startTracking()
    return START_STICKY
  }
}
