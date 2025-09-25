package com.androidphotoapp.sleepengine

import android.content.Context
import android.os.PowerManager
import android.util.Log
import com.androidphotoapp.sleepengine.storage.LockTimeStore
import com.androidphotoapp.sleepengine.storage.ScreenStateStore

object ScreenStateHandler {

  fun handleStateCheck(context: Context, tag: String = "ScreenStateHandler") {
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val isScreenOn = pm.isInteractive
    val previousState = ScreenStateStore.getLastState(context)

    Log.i(tag, "isScreenOn = $isScreenOn, previousState = $previousState")

    // Case 1: Screen just turned OFF
    if (!isScreenOn && previousState) {
      val lockTime = System.currentTimeMillis()
      LockTimeStore.saveLockTime(context, lockTime)
      Log.i(tag, "Screen OFF detected at $lockTime")
    }

    // Case 2: Screen just turned ON
    if (isScreenOn && !previousState) {
      val unlockTime = System.currentTimeMillis()
      val lockTime = LockTimeStore.getLockTime(context)

      if (lockTime != 0L) {
        val durationMillis = unlockTime - lockTime
        SleepUtils.checkSleep(durationMillis, lockTime, context)
        LockTimeStore.clearLockTime(context)
      }

      Log.i(tag, "Screen ON detected at $unlockTime")
    }

    // Always update last state
    ScreenStateStore.setLastState(context, isScreenOn)
  }
}
