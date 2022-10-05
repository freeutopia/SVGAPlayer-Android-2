package com.utopia.svga.utils

import android.util.Log
import com.tantan.utopia.svga.BuildConfig

object Log {
  private const val TAG = "lt-log"
  private const val MB = 1024 * 1024

  @JvmStatic
  fun e(message: String?) {
    if (BuildConfig.DEBUG) {
      Log.e(TAG, message!!)
    }
  }

  @JvmStatic
  fun printMemoryInfo() {
    val totalMemory = (Runtime.getRuntime().totalMemory() * 1.0 / MB).toFloat()
    val freeMemory = (Runtime.getRuntime().freeMemory() * 1.0 / MB).toFloat()
    e("app 已分配内存（全部）: $totalMemory")
    e("app 已分配内存（未使用）: $freeMemory")
  }
}