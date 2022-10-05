package com.utopia.svga.data.cache.memory

import java.util.*

abstract class LruCache<T, Y>(initialMaxSize: Long) {
  internal val cache: MutableMap<T, Entry<Y>?> = LinkedHashMap(5, 0.75f, true)

  @get:Synchronized
  var maxSize: Long
    private set

  @get:Synchronized
  var currentSize: Long = 0
    private set

  init {
    maxSize = initialMaxSize
  }

  abstract fun getSize(item: Y?): Long

  @Synchronized
  protected fun getCount(): Int {
    return cache.size
  }

  abstract fun onItemEvicted(key: T, item: Y)

  @Synchronized
  operator fun contains(key: T): Boolean {
    return cache.containsKey(key)
  }

  @Synchronized
  operator fun get(key: T): Y? {
    return cache[key]?.value
  }

  @Synchronized
  open fun put(key: T, item: Y): Y? {
    val itemSize = getSize(item)
    if (itemSize >= maxSize) {
      onItemEvicted(key, item)
      return null
    }

    currentSize += itemSize
    val old = cache.put(key, Entry(item, itemSize))
    old?.apply {
      currentSize -= size
      if (value != item) {
        onItemEvicted(key, value)
      }
    }

    evict()
    return old?.value
  }

  @Synchronized
  open fun remove(key: T): Y? {
    val entry = cache.remove(key) ?: return null
    currentSize -= entry.size
    return entry.value
  }

  fun clearMemory() {
    trimToSize(0)
  }


  @Synchronized
  protected open fun trimToSize(size: Long) {
    var last: Map.Entry<T, Entry<Y>?>
    var cacheIterator: MutableIterator<Map.Entry<T, Entry<Y>?>?>
    while (currentSize > size) {
      cacheIterator = cache.entries.iterator()
      last = cacheIterator.next()
      val toRemove = last.value
      currentSize -= toRemove?.size ?: 0
      val key = last.key
      cacheIterator.remove()
      if (toRemove != null) {
        onItemEvicted(key, toRemove.value)
      }
    }
  }

  private fun evict() {
    trimToSize(maxSize)
  }

  internal data class Entry<Y>(val value: Y, val size: Long)
}