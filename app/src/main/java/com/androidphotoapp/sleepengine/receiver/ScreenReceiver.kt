package com.androidphotoapp.sleepengine.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.androidphotoapp.sleepengine.SleepConstants
import com.androidphotoapp.sleepengine.SleepUtils
import com.androidphotoapp.sleepengine.storage.LockTimeStore
import com.androidphotoapp.sleepengine.storage.ScreenStateStore
import com.androidphotoapp.sleepengine.storage.SleepLog
import com.androidphotoapp.sleepengine.storage.SleepLogStore
import java.util.Calendar

class ScreenReceiver : BroadcastReceiver() {

  @RequiresApi(Build.VERSION_CODES.O)
  override fun onReceive(context: Context?, intent: Intent?) {
    if (context == null) return

    when (intent?.action) {
      Intent.ACTION_SCREEN_OFF -> {
        val lockTime = System.currentTimeMillis()
        LockTimeStore.saveLockTime(context, lockTime)
        ScreenStateStore.setLastState(context, false)
        Log.d("ScreenReceiver", "Screen OFF (Locked) at $lockTime")
      }

      Intent.ACTION_SCREEN_ON -> {
        val unlockTime = System.currentTimeMillis()
        val lockTime = LockTimeStore.getLockTime(context)

        if (lockTime != 0L) {
          val durationMillis = unlockTime - lockTime

          SleepUtils.checkSleep(durationMillis, lockTime, context)
          LockTimeStore.clearLockTime(context)
        }

        ScreenStateStore.setLastState(context, true)
        Log.d("ScreenReceiver", "Screen UNLOCKED at $unlockTime")
      }
    }
  }
}
