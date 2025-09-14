package com.androidphotoapp.sleepengine

import java.util.concurrent.TimeUnit

object SleepConstants {
  // Sleep start time: 10:00 PM
//  const val SLEEP_START_HOUR = 22
  const val SLEEP_START_HOUR = 17
  const val SLEEP_START_MINUTE = 0

  // Minimum duration to consider as sleeping: 45 minutes
//  val MIN_SLEEP_DURATION_MILLIS = TimeUnit.MINUTES.toMillis(45)
  val MIN_SLEEP_DURATION_MILLIS = TimeUnit.MINUTES.toMillis(1)
}
