package com.androidphotoapp.sleepengine

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.androidphotoapp.sleepengine.receiver.ScreenReceiver
import com.androidphotoapp.sleepengine.service.SleepSensorService
import com.androidphotoapp.sleepengine.ui.theme.SleepEngineTheme

class MainActivity : ComponentActivity() {

  private val screenReceiver = ScreenReceiver()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent {
      SleepEngineTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          TestScreenButtons(
            modifier = Modifier.padding(innerPadding),
            activity = this
          )
        }
      }
    }

    // Check and request runtime permissions
    checkPermissions()
  }

  /** Runtime permission check */
  private fun checkPermissions() {
    val permissionsToRequest = mutableListOf<String>()

    // RECORD_AUDIO
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
          // Optional: show rationale dialog here if denied
        }
      }
    }

  /** Simulate screen lock/unlock triggers */
  fun triggerScreenReceiver(action: String) {
    val intent = Intent(action)
    screenReceiver.onReceive(this, intent)
    Toast.makeText(this, "Triggered: $action", Toast.LENGTH_SHORT).show()
  }
}

@Composable
fun TestScreenButtons(modifier: Modifier = Modifier, activity: MainActivity) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Button(
      onClick = {
        activity.triggerScreenReceiver(Intent.ACTION_SCREEN_OFF)
      },
      modifier = Modifier.weight(1f)
    ) {
      Text(text = "Screen Lock")
    }

    Button(
      onClick = {
        activity.triggerScreenReceiver(Intent.ACTION_SCREEN_ON)
      },
      modifier = Modifier.weight(1f)
    ) {
      Text(text = "Screen Unlock")
    }
  }
}
