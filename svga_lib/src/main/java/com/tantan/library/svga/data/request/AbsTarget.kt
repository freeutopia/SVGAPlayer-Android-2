package com.tantan.library.svga.data.request

import com.tantan.library.svga.ResourceKey
import com.tantan.library.svga.exception.GlobalExceptionMonitor
import com.tantan.library.svga.exception.SVGAException
import com.tantan.library.svga.utils.ProxyRunnable
import com.tantan.library.svga.utils.SVGAExecutors

abstract class AbsTarget<R> {
  var request: ResourceRequest? = null

  protected abstract fun setResource(key: ResourceKey, resource: R)

  protected abstract fun onResourceLoadError(key: ResourceKey, e: SVGAException)

  fun begin() {
    request?.key?.get()?.let {
      SVGAExecutors.io.execute(ProxyRunnable(it) { request?.begin() })
    }
  }

  fun onLoadFailed(key: ResourceKey, e: SVGAException) {
    SVGAExecutors.postOnUiThread {
      GlobalExceptionMonitor.get()?.onFailed(key.path, e)
      onResourceLoadError(key, e)
    }
  }

  fun onLoadSuccess(key: ResourceKey, resource: R) {
    SVGAExecutors.postOnUiThread {
      GlobalExceptionMonitor.get()?.onSuccess(key.path)
      setResource(key, resource)
    }
  }
}