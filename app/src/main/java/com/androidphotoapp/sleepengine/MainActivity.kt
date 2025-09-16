package com.androidphotoapp.sleepengine

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.androidphotoapp.sleepengine.receiver.ScreenReceiver
import com.androidphotoapp.sleepengine.storage.LockTimeStore
import com.androidphotoapp.sleepengine.storage.ScreenStateStore
import com.androidphotoapp.sleepengine.storage.SleepLog
import com.androidphotoapp.sleepengine.storage.SleepLogStore
import com.androidphotoapp.sleepengine.ui.theme.SleepEngineTheme
import com.androidphotoapp.sleepengine.worker.ScreenCheckWorker
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

  private val screenReceiver = ScreenReceiver()

  @RequiresApi(Build.VERSION_CODES.O)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // 1️⃣ Detect missed screen events while app was closed
    checkScreenStateOnStart()

    // 2️⃣ Dynamically register screen on/off receiver
    val filter = IntentFilter().apply {
      addAction(Intent.ACTION_SCREEN_ON)
      addAction(Intent.ACTION_SCREEN_OFF)
    }
    registerReceiver(screenReceiver, filter)

    // 3️⃣ Schedule WorkManager
    scheduleScreenCheckWorker()

    enableEdgeToEdge()
    setContent {
      SleepEngineTheme {
        MainScreen(activity = this)
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    unregisterReceiver(screenReceiver)
  }

  private fun checkScreenStateOnStart() {
    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    val isScreenOn = powerManager.isInteractive
    val previousState = ScreenStateStore.getLastState(this)

    if (!isScreenOn && previousState) {
      val lockTime = System.currentTimeMillis()
      LockTimeStore.saveLockTime(this, lockTime)
      Log.i("MainActivity", "Screen OFF detected on app start: $lockTime")
    }

    if (isScreenOn && !previousState) {
      val unlockTime = System.currentTimeMillis()
      val lockTime = LockTimeStore.getLockTime(this)

      if (lockTime != 0L) {
        val durationMillis = unlockTime - lockTime
        SleepUtils.checkSleep(durationMillis, lockTime, this)
        LockTimeStore.clearLockTime(this)
      }

      Log.i("ScreenCheck", "Screen ON detected on app start: $unlockTime")
    }

    ScreenStateStore.setLastState(this, isScreenOn)
  }

  private fun scheduleScreenCheckWorker() {
    val workManager = WorkManager.getInstance(this)

    // Periodic work request with tag
    val periodicWork = PeriodicWorkRequestBuilder<ScreenCheckWorker>(
      SleepConstants.WORK_INTERVAL, TimeUnit.MINUTES
    )
      .addTag("screen_check_work")
      .build()

    // Enqueue unique periodic work
    workManager.enqueueUniquePeriodicWork(
      "screen_check_work",
      ExistingPeriodicWorkPolicy.REPLACE,
      periodicWork
    )
  }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(activity: MainActivity) {
  var sleepLogs by remember { mutableStateOf(SleepLogStore.getLogs(activity).toList()) }

  // Refresh logs every second
  LaunchedEffect(Unit) {
    while (true) {
      sleepLogs = SleepLogStore.getLogs(activity).toList()
      kotlinx.coroutines.delay(1000)
    }
  }

  Scaffold(
    content = { padding ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
          .padding(16.dp)
      ) {

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          "Sleep Logs",
          style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
          items(sleepLogs) { log ->
            SleepLogCard(log)
            Spacer(modifier = Modifier.height(8.dp))
          }
        }
      }
    }
  )
}

@Composable
fun SleepLogCard(log: SleepLog) {
  val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
  val start = sdf.format(Date(log.startTime))
  val end = sdf.format(Date(log.endTime))

  Card(
    modifier = Modifier.fillMaxWidth(),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text("Start: $start", style = MaterialTheme.typography.bodyLarge)
      Text("End: $end", style = MaterialTheme.typography.bodyLarge)
    }
  }
}
