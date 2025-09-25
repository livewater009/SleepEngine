package com.androidphotoapp.sleepengine.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.androidphotoapp.sleepengine.SleepUtils
import com.androidphotoapp.sleepengine.storage.LockTimeStore
import com.androidphotoapp.sleepengine.storage.ScreenStateStore

class ScreenReceiver : BroadcastReceiver() {

  @RequiresApi(Build.VERSION_CODES.O)
  override fun onReceive(context: Context?, intent: Intent?) {
    if (context == null) return

    when (intent?.action) {
      // When phone is locked
      Intent.ACTION_SCREEN_OFF -> {
        val lockTime = System.currentTimeMillis()
        LockTimeStore.saveLockTime(context, lockTime)
        ScreenStateStore.setLastState(context, false)
        Log.e("ScreenReceiver", "🔥 Screen OFF (Locked) at $lockTime")
      }

      // When screen is turned on (but not necessarily unlocked yet)
      Intent.ACTION_SCREEN_ON -> {
        ScreenStateStore.setLastState(context, true)
        Log.e("ScreenReceiver", "🔥 Screen ON (but not yet unlocked)")
      }

      // When the user actually unlocks the phone
      Intent.ACTION_USER_PRESENT -> {
        val unlockTime = System.currentTimeMillis()
        val lockTime = LockTimeStore.getLockTime(context)

        if (lockTime != 0L) {
          val durationMillis = unlockTime - lockTime
          SleepUtils.checkSleep(durationMillis, lockTime, context)
          LockTimeStore.clearLockTime(context)
        }

        Log.e("ScreenReceiver", "🔥 User PRESENT (Unlocked) at $unlockTime\"")
      }
    }
  }
}
