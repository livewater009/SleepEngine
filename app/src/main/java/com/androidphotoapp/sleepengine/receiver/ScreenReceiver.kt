package com.androidphotoapp.sleepengine.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScreenReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context?, intent: Intent?) {
    when(intent?.action) {
      Intent.ACTION_SCREEN_ON -> {
        Log.d("ScreenReceiver", "Screen ON (Unlocked)")
        // Call function to calculate sleep score here
      }
      Intent.ACTION_SCREEN_OFF -> {
        Log.d("ScreenReceiver", "Screen OFF (Locked)")
        // Enable sensor tracking here
      }
    }
  }
}
