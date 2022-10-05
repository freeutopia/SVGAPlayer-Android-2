package com.utopia.svga.data.cache.memory

import com.utopia.svga.data.cache.Resource

class ResourceLruCache(size: Long) : LruCache<String, Resource<*>>(size) {

  override fun getSize(item: Resource<*>?): Long {
    return item?.size ?: 0
  }

  override fun onItemEvicted(key: String, item: Resource<*>) {
    //...
  }

  fun getCurrentMemory(): Long {
    return currentSize
  }
}