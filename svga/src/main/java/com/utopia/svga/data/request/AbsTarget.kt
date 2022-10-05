package com.utopia.svga.data.request

import com.utopia.svga.ResourceKey
import com.utopia.svga.exception.GlobalExceptionMonitor
import com.utopia.svga.exception.SVGAException
import com.utopia.svga.utils.ProxyRunnable
import com.utopia.svga.utils.SVGAExecutors

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