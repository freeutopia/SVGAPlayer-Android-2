package com.tantan.library.svga.data.load

import com.tantan.library.svga.data.cache.CacheProvider
import com.tantan.library.svga.data.cache.Resource
import com.tantan.library.svga.data.cache.CachedResource
import com.tantan.library.svga.ResourceKey
import com.tantan.library.svga.data.request.RequestCallback
import com.tantan.library.svga.exception.SVGAException
import com.tantan.library.svga.tracker.CacheType

class ActiveLoader(parent: DataLoader<*, Resource<*>>) :
  DataLoader<Resource<*>, CachedResource>(parent) {
  private var mCallback: RequestCallback? = null

  companion object {
    fun builder(key: ResourceKey): ActiveLoader {
      return ActiveLoader(MemoryLoader.builder(key))
    }
  }

  override fun findData(key: ResourceKey) = CacheProvider.activeResource[key.cacheKey()]

  override fun processData(key: ResourceKey, data: CachedResource?) {
    data?.apply {
      acquire()
      mCallback?.onResourceReady(key, this)
    }
  }

  override fun interceptException(key: ResourceKey, ex: Throwable?) {
    ex?.let {
      mCallback?.onLoadFailed(key, if (it is SVGAException) it else SVGAException(it))
    }
  }

  override fun transform(key: ResourceKey, source: Resource<*>?): CachedResource? {
    return source as? CachedResource
  }

  internal fun callback(callback: RequestCallback): ActiveLoader {
    this.mCallback = callback
    return this
  }

  override fun getCacheType() = CacheType.ACTIVE
}