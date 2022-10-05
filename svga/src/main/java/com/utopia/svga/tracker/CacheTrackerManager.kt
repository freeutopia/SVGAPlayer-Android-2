package com.utopia.svga.tracker

import com.utopia.svga.data.cache.Key

internal object CacheTrackerManager {
  private var trackerListener: CacheTrackerListener? = null

  fun register(listener: CacheTrackerListener) {
    trackerListener = listener
  }

  fun removeAll() {
    trackerListener = null
  }

  fun onCachePut(
    type: CacheType,
    key: Key,
    calSizeAction: () -> Pair<Long, Long>
  ) {
    trackerListener?.onCachePut(type, key, calSizeAction())
  }

  fun onCacheHit(type: CacheType, key: Key) {
    trackerListener?.onCacheHit(type, key)
  }

  fun onCacheMiss(type: CacheType, key: Key) {
    trackerListener?.onCacheMiss(type, key)
  }

  fun onCacheRemoved(type: CacheType, key: Key) {
    trackerListener?.onCacheRemoved(type, key)
  }
}