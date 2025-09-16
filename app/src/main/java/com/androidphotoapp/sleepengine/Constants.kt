package com.androidphotoapp.sleepengine

import java.util.concurrent.TimeUnit

object SleepConstants {
  const val SLEEP_START_HOUR = 22
  const val SLEEP_START_MINUTE = 0
  val MIN_SLEEP_DURATION_MILLIS = TimeUnit.MINUTES.toMillis(45)
}
