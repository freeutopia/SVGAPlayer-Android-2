package com.utopia.svga.data.request

import android.text.TextUtils
import com.utopia.svga.ResourceKey
import com.utopia.svga.SVGALoader
import com.utopia.svga.compose.SVGADynamicEntity
import com.utopia.svga.compose.SVGAVideoEntity
import com.utopia.svga.data.cache.Resource
import com.utopia.svga.exception.SVGAException

class ViewTarget(
  private val callback: RequestCallback? = null,
  private val dynamic: SVGADynamicEntity? = null
) :
  AbsTarget<Resource<SVGAVideoEntity>>() {


  override fun setResource(key: ResourceKey, resource: Resource<SVGAVideoEntity>) {
    var hasConsume = false
    val iterator = SVGALoader.targetViews.iterator()
    while (iterator.hasNext()) {
      val data = iterator.next()
      if (TextUtils.equals(data.value, key.cacheKey())) {
        if (hasConsume) {
          resource.acquire()
        }
        hasConsume = true
        data.key.setResource(resource, dynamic)
        data.key.requestCallback?.onResourceReady(key, resource)
        iterator.remove()
      }
    }

    if (!hasConsume) {
      resource.release()
    }
    callback?.onResourceReady(key, resource)
  }

  override fun onResourceLoadError(key: ResourceKey, e: SVGAException) {
    val iterator = SVGALoader.targetViews.iterator()
    while (iterator.hasNext()) {
      val data = iterator.next()
      if (TextUtils.equals(data.value, key.cacheKey())) {
        data.key.requestCallback?.onLoadFailed(key, e)
        iterator.remove()
      }
    }
    callback?.onLoadFailed(key, e)
  }
}