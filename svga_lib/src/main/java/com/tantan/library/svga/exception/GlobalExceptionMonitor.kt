package com.tantan.library.svga.exception

import com.tantan.library.svga.LoadResourceListener

internal class GlobalExceptionMonitor private constructor(private var listener: LoadResourceListener) :
  LoadResourceListener {

  companion object {
    private var instance: GlobalExceptionMonitor? = null

    fun register(listener: LoadResourceListener) {
      if (instance == null) {
        instance = GlobalExceptionMonitor(listener)
      } else {
        instance?.listener = listener
      }
    }

    internal fun get(): GlobalExceptionMonitor? = instance
  }

  override fun onSuccess(path: String) {
    listener.onSuccess(path)
  }

  override fun onFailed(path: String, e: SVGAException) {
    listener.onFailed(path, e)
  }

}