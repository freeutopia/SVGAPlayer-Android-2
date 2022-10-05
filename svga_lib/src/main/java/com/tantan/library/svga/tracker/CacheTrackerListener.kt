package com.tantan.library.svga.tracker

import com.tantan.library.svga.data.cache.Key

interface CacheTrackerListener {
  /**
   * 新增缓存
   *
   * @param key 资源标识
   * @param size 资源大小
   */
  fun onCachePut(type: CacheType, key: Key, size: Pair<Long, Long>)

  /**
   * 命中缓存
   *
   * @param key 资源标识
   */
  fun onCacheHit(type: CacheType, key: Key)

  /** 未命中缓存  */
  fun onCacheMiss(type: CacheType, key: Key)

  fun onCacheRemoved(type: CacheType, key: Key)
}