package com.androidphotoapp.sleepengine

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.androidphotoapp.sleepengine.receiver.ScreenReceiver
import com.androidphotoapp.sleepengine.storage.SleepLog
import com.androidphotoapp.sleepengine.storage.SleepLogStore
import com.androidphotoapp.sleepengine.ui.theme.SleepEngineTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

  private val screenReceiver = ScreenReceiver()

  @RequiresApi(Build.VERSION_CODES.O)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()

    setContent {
      SleepEngineTheme {
        MainScreen(activity = this)
      }
    }

    // Dynamically register screen on/off receiver
    val filter = IntentFilter().apply {
      addAction(Intent.ACTION_SCREEN_ON)
      addAction(Intent.ACTION_SCREEN_OFF)
    }
    registerReceiver(screenReceiver, filter)
  }

  override fun onDestroy() {
    super.onDestroy()
    unregisterReceiver(screenReceiver)
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
