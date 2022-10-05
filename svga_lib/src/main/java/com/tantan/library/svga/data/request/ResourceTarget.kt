package com.tantan.library.svga.data.request

import com.tantan.library.svga.ResourceKey
import com.tantan.library.svga.compose.SVGAVideoEntity
import com.tantan.library.svga.data.cache.Resource
import com.tantan.library.svga.exception.SVGAException

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