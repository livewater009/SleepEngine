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
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.androidphotoapp.sleepengine.R
import com.androidphotoapp.sleepengine.storage.SensorDataStore
import java.io.IOException

class SleepSensorService : Service(), SensorEventListener {

  private lateinit var sensorManager: SensorManager
  private var accelerometer: Sensor? = null
  private var lightSensor: Sensor? = null

  private var mediaRecorder: MediaRecorder? = null

  private val CHANNEL_ID = "sleep_sensor_channel"

  override fun onCreate() {
    super.onCreate()

    // Initialize sensors
    sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    startForeground(1, createNotification())
    startTracking()
    startAudioTracking()
  }

  override fun onDestroy() {
    super.onDestroy()
    stopTracking()
    stopAudioTracking()
    Log.d("SleepSensorService", "Service destroyed")
  }

  /** Start motion & light sensor tracking */
  private fun startTracking() {
    accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    lightSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    Log.d("SleepSensorService", "Started tracking sensors")
  }

  private fun stopTracking() {
    sensorManager.unregisterListener(this)
    Log.d("SleepSensorService", "Stopped tracking sensors")
  }

  /** Start audio recording to measure ambient noise */
  private fun startAudioTracking() {
    mediaRecorder = MediaRecorder().apply {
      setAudioSource(MediaRecorder.AudioSource.MIC)
      setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
      setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
      setOutputFile("/dev/null") // Discard audio, we just want amplitude
      try {
        prepare()
        start()
        Log.d("SleepSensorService", "Started audio tracking")
      } catch (e: IOException) {
        Log.e("SleepSensorService", "Audio tracking failed: ${e.message}")
      }
    }

    // Optional: poll amplitude periodically
    Thread {
      while (mediaRecorder != null) {
        try {
          val amplitude = mediaRecorder?.maxAmplitude ?: 0
          Log.d("SleepSensorService", "Audio amplitude: $amplitude")
          SensorDataStore.saveAudioAmplitude(applicationContext, amplitude.toFloat())
          Thread.sleep(1000) // 1-second interval
        } catch (e: InterruptedException) {
          break
        }
      }
    }.start()
  }

  private fun stopAudioTracking() {
    mediaRecorder?.apply {
      stop()
      release()
    }
    mediaRecorder = null
    Log.d("SleepSensorService", "Stopped audio tracking")
  }

  override fun onSensorChanged(event: SensorEvent?) {
    event?.let {
      Log.d("SensorDebug", "Sensor type: ${it.sensor.type}, values: ${it.values.joinToString()}")
    }

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
      .setContentText("Tracking motion, light, and audio")
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setOngoing(true)
      .build()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    startTracking()
    startAudioTracking()
    return START_STICKY
  }
}
