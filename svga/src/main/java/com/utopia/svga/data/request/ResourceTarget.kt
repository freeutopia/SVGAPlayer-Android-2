package com.utopia.svga.data.request

import com.utopia.svga.ResourceKey
import com.utopia.svga.compose.SVGAVideoEntity
import com.utopia.svga.data.cache.Resource
import com.utopia.svga.exception.SVGAException

class ResourceTarget(
  private val callback: RequestCallback? = null
) :
  AbsTarget<Resource<SVGAVideoEntity>>() {

  override fun setResource(key: ResourceKey, resource: Resource<SVGAVideoEntity>) {
    callback?.onResourceReady(key, resource)
  }

  override fun onResourceLoadError(key: ResourceKey, e: SVGAException) {
    callback?.onLoadFailed(key, e)
  }
}