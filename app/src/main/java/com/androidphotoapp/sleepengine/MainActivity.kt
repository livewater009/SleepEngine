package com.androidphotoapp.sleepengine

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
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
import androidx.compose.ui.unit.dp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.androidphotoapp.sleepengine.receiver.ScreenReceiver
import com.androidphotoapp.sleepengine.storage.SleepLog
import com.androidphotoapp.sleepengine.storage.SleepLogStore
import com.androidphotoapp.sleepengine.ui.theme.SleepEngineTheme
import com.androidphotoapp.sleepengine.worker.ScreenCheckWorker
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import android.provider.Settings
import android.widget.Toast
import androidx.core.net.toUri

class MainActivity : ComponentActivity() {

  private val screenReceiver = ScreenReceiver()

  @RequiresApi(Build.VERSION_CODES.O)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Prompt user to disable battery optimization
    requestBatteryOptimizationIgnore()

    enableEdgeToEdge()

    // 1️⃣ Detect missed screen events while app was closed
    ScreenStateHandler.handleStateCheck(this, "MainActivity")

    // 2️⃣ Dynamically register screen ON/OFF receiver
    val filter = IntentFilter().apply {
      addAction(Intent.ACTION_SCREEN_ON)
      addAction(Intent.ACTION_SCREEN_OFF)
      addAction(Intent.ACTION_USER_PRESENT)
    }
    registerReceiver(screenReceiver, filter)

    // 3️⃣ Schedule the chained worker
    scheduleChainedWorker(intervalMinutes = SleepConstants.WORK_INTERVAL)

    setContent {
      SleepEngineTheme {
        MainScreen(activity = this)
      }
    }
  }

  private fun requestBatteryOptimizationIgnore() {
    val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
    val packageName = packageName

    if (!pm.isIgnoringBatteryOptimizations(packageName)) {
      // Open the system settings screen
      val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
      intent.data = "package:$packageName".toUri()
      startActivity(intent)

      Toast.makeText(
        this,
        "Please select 'Unrestricted' or 'Don't optimize' for this app to run properly in background.",
        Toast.LENGTH_LONG
      ).show()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    unregisterReceiver(screenReceiver)
  }

  /** Schedule a repeating worker using OneTimeWorkRequest chaining */
  fun scheduleChainedWorker(intervalMinutes: Int = 5) {
    val workManager = WorkManager.getInstance(applicationContext)

    // Cancel any existing chained work first
    workManager.cancelUniqueWork("sleep_work_chain")

    // Schedule the first worker
    val initialWork = OneTimeWorkRequestBuilder<ScreenCheckWorker>()
      .setInitialDelay(intervalMinutes.toLong(), TimeUnit.MINUTES)
      .addTag("sleep_worker")
      .build()

    WorkManager.getInstance(applicationContext).enqueueUniqueWork(
      "sleep_work_chain",
      ExistingWorkPolicy.REPLACE,
      listOf(initialWork)
    )

    // 🔹 15-min periodic safety net
    val periodicWork = PeriodicWorkRequestBuilder<ScreenCheckWorker>(
      15, TimeUnit.MINUTES
    ).addTag("sleep_worker").build()

    workManager.enqueueUniquePeriodicWork(
      "sleep_work_periodic",
      ExistingPeriodicWorkPolicy.KEEP,
      periodicWork
    )

    Log.e("MainActivity", "🔥 Scheduled 10-min chain + 15-min periodic safety net")
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

        Text("Sleep Logs", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

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
