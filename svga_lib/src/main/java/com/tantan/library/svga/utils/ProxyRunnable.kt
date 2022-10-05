package com.tantan.library.svga.utils

class ProxyRunnable(val key: String, private val realRunnable: Runnable) : Runnable {
  override fun run() {
    realRunnable.run()
  }
}