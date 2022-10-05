package com.tantan.library.svga.data.cache

import com.tantan.library.svga.compose.SVGAVideoEntity
import com.tantan.library.svga.tracker.CacheTrackerManager
import com.tantan.library.svga.tracker.CacheType

class CachedResource(
  private val entity: SVGAVideoEntity
) : Resource<SVGAVideoEntity> {
  @Volatile
  private var acquired = 0

  override val resourceClass: Class<SVGAVideoEntity>
    get() = SVGAVideoEntity::class.java

  override fun get(): SVGAVideoEntity = entity

  override val size: Long
    get() = entity.memorySize()

  @Synchronized
  override fun acquire() {
    acquired += 1
    if (acquired == 1) {
      entity.load()
      CacheProvider.activeResource.activate(entity.key.cacheKey(), this)
      CacheTrackerManager.onCachePut(CacheType.ACTIVE, entity.key) {
        Pair(size, 0)
      }
    }
  }

  @Synchronized
  override fun release() {
    acquired -= 1
    if (acquired <= 0) {
      acquired = 0

      CacheProvider.activeResource.deactivate(entity.key.cacheKey())
      entity.clear()
      CacheTrackerManager.onCacheRemoved(CacheType.ACTIVE, entity.key)
    }
  }

  override fun getAbsolutePath(): String? {
    return CacheProvider.diskCache?.get(entity.key.cacheKey())?.absolutePath
  }
}