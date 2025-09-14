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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.androidphotoapp.sleepengine.receiver.ScreenReceiver
import com.androidphotoapp.sleepengine.service.SleepSensorService
import com.androidphotoapp.sleepengine.storage.SleepLogStore
import com.androidphotoapp.sleepengine.storage.SleepLog
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

    // Check permissions
    checkPermissions()

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

  /** Runtime permission check */
  private fun checkPermissions() {
    val permissionsToRequest = mutableListOf<String>()

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
      != PackageManager.PERMISSION_GRANTED
    ) {
      permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
    }

    if (permissionsToRequest.isNotEmpty()) {
      permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }
  }

  private val permissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
      results.forEach { (permission, granted) ->
        if (granted) {
          Toast.makeText(this, "$permission granted", Toast.LENGTH_SHORT).show()
        } else {
          Toast.makeText(this, "$permission denied", Toast.LENGTH_SHORT).show()
        }
      }
    }

  /** Simulate screen lock/unlock triggers for testing */
  @RequiresApi(Build.VERSION_CODES.O)
  fun triggerScreenReceiver(action: String) {
    val intent = Intent(action)
    screenReceiver.onReceive(this, intent)
    Toast.makeText(this, "Triggered: $action", Toast.LENGTH_SHORT).show()
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

  Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    TestScreenButtons(activity = activity)

    Spacer(modifier = Modifier.height(16.dp))

    Text("Sleep Logs:", style = MaterialTheme.typography.titleMedium)

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
      items(sleepLogs) { log ->
        SleepLogItem(log)
      }
    }
  }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TestScreenButtons(activity: MainActivity) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Button(
      onClick = { activity.triggerScreenReceiver(Intent.ACTION_SCREEN_OFF) },
      modifier = Modifier.weight(1f)
    ) {
      Text(text = "Screen Lock")
    }

    Button(
      onClick = { activity.triggerScreenReceiver(Intent.ACTION_SCREEN_ON) },
      modifier = Modifier.weight(1f)
    ) {
      Text(text = "Screen Unlock")
    }

    Button(
      onClick = {
        // Start foreground service explicitly
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          val serviceIntent = Intent(activity, SleepSensorService::class.java)
          ContextCompat.startForegroundService(activity, serviceIntent)
          Toast.makeText(activity, "Foreground Service Started", Toast.LENGTH_SHORT).show()
        }
      },
      modifier = Modifier.weight(1f)
    ) {
      Text(text = "Start Service")
    }
  }
}

@Composable
fun SleepLogItem(log: SleepLog) {
  val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
  val start = sdf.format(Date(log.startTime))
  val end = sdf.format(Date(log.endTime))

  Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
    Text("Start: $start")
    Text("End: $end")
    Text("Score: ${log.sleepScore}")
    HorizontalDivider(
      modifier = Modifier.padding(top = 4.dp),
      thickness = DividerDefaults.Thickness,
      color = DividerDefaults.color
    )
  }
}
