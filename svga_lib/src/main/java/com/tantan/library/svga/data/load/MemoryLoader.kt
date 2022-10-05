package com.tantan.library.svga.data.load

import com.tantan.library.svga.data.cache.CacheProvider
import com.tantan.library.svga.data.cache.Resource
import com.tantan.library.svga.ResourceKey
import com.tantan.library.svga.tracker.CacheType

class MemoryLoader(parent: DataLoader<*, Resource<*>>) :
  DataLoader<Resource<*>, Resource<*>>(parent) {

  companion object {
    fun builder(key: ResourceKey): MemoryLoader {
      return if (key.get().startsWith("http")) {
        MemoryLoader(DiskLoader.builder(key))
      } else {
        MemoryLoader(AssetsLoader.builder(key, "svga"))
      }
    }
  }

  override fun findData(key: ResourceKey) = CacheProvider.memoryLruCache?.get(key.cacheKey())

  override fun processData(key: ResourceKey, data: Resource<*>?) {}

  override fun transform(key: ResourceKey, source: Resource<*>?) = source

  override fun getCacheType() = CacheType.MEMORY
}