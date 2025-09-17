package com.androidphotoapp.sleepengine.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.androidphotoapp.sleepengine.MainActivity

class BootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
      Log.i("BootReceiver", "Device rebooted, rescheduling alarm")
      val mainIntent = Intent(context, MainActivity::class.java)
      mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(mainIntent)
    }
  }
}
